package io.dataease.plugin.datasource.oceanbase;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.extensions.datasource.plugin.DataEaseDatasourcePlugin;
import io.dataease.extensions.datasource.vo.Configuration;
import io.dataease.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;

/**
 * OceanBase(Oracle 兼容模式) 数据源插件后端实现。
 */
public class OceanBaseOracleDatasourcePlugin extends DataEaseDatasourcePlugin {

    private static final String DEFAULT_DRIVER = "com.oceanbase.jdbc.Driver";
    private static final int DEFAULT_PORT = 2881;

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) {
        try (ConnectionObj connectionObj = getConnection(datasourceRequest.getDatasource())) {
            List<String> schemas = new ArrayList<>();
            DatabaseMetaData metaData = connectionObj.getConnection().getMetaData();
            try (ResultSet rs = metaData.getSchemas()) {
                while (rs.next()) {
                    String schema = rs.getString("TABLE_SCHEM");
                    if (StringUtils.isNotBlank(schema)) {
                        schemas.add(schema);
                    }
                }
            }
            schemas.sort(String::compareToIgnoreCase);
            return schemas;
        } catch (Exception e) {
            DEException.throwException("获取 schema 失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) {
        try (ConnectionObj connectionObj = getConnection(datasourceRequest.getDatasource())) {
            List<DatasetTableDTO> tables = new ArrayList<>();
            Configuration cfg = parseConfig(datasourceRequest.getDatasource());
            String schemaPattern = StringUtils.defaultIfBlank(cfg.getSchema(), cfg.getUsername());
            DatabaseMetaData metaData = connectionObj.getConnection().getMetaData();
            try (ResultSet rs = metaData.getTables(null, normalizeSchema(schemaPattern), "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    DatasetTableDTO dto = new DatasetTableDTO();
                    dto.setName(rs.getString("TABLE_NAME"));
                    dto.setTableName(rs.getString("TABLE_NAME"));
                    dto.setRemarks(rs.getString("REMARKS"));
                    tables.add(dto);
                }
            }
            return tables;
        } catch (Exception e) {
            DEException.throwException("获取表失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public ConnectionObj getConnection(DatasourceDTO coreDatasource) throws Exception {
        io.dataease.extensions.datasource.vo.DatasourceConfiguration cfg = parseConfig(coreDatasource);
        cfg.convertJdbcUrl();

        ConnectionObj connectionObj = new ConnectionObj();
        startSshSession(cfg, connectionObj, coreDatasource.getId());

        String jdbcUrl = resolveJdbcUrl(cfg);
        String username = cfg.getUsername();
        String password = cfg.getPassword();

        String driver = StringUtils.defaultIfBlank(cfg.getDriver(), DEFAULT_DRIVER);
        Class.forName(driver);

        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        connectionObj.setConnection(connection);
        connectionObj.setConfiguration(cfg);
        return connectionObj;
    }

    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        try (ConnectionObj ignored = getConnection(datasourceRequest.getDatasource())) {
            return "success";
        }
    }

    @Override
    public Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException {
        String sql = datasourceRequest.getQuery();
        if (StringUtils.isBlank(sql)) {
            DEException.throwException("SQL 不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        List<TableField> fields = new ArrayList<>();

        try (ConnectionObj connectionObj = getConnection(datasourceRequest.getDatasource())) {
            Integer queryTimeout = connectionObj.getConfiguration() == null ? null : connectionObj.getConfiguration().getQueryTimeout();
            int finalTimeout = queryTimeout == null || queryTimeout <= 0 ? 30 : queryTimeout;

            try (Statement stmt = getStatement(connectionObj.getConnection(), finalTimeout);
                 ResultSet rs = stmt.executeQuery(sql)) {

                ResultSetMetaData md = rs.getMetaData();
                int colCount = md.getColumnCount();

                for (int i = 1; i <= colCount; i++) {
                    TableField field = new TableField();
                    field.setName(md.getColumnLabel(i));
                    field.setOriginName(md.getColumnName(i));
                    field.setType(md.getColumnTypeName(i));
                    field.setTypeNumber(md.getColumnType(i));
                    field.setPrecision(md.getPrecision(i));
                    field.setScale(md.getScale(i));
                    fields.add(field);
                }

                int maxRows = 500;
                int count = 0;
                while (rs.next() && count < maxRows) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(md.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                    count++;
                }
            }

            result.put("fields", fields);
            result.put("rows", rows);
            result.put("rowCount", rows.size());
            return result;
        } catch (Exception e) {
            DEException.throwException("执行 SQL 失败: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public List<TableField> fetchTableField(DatasourceRequest datasourceRequest) throws DEException {
        String table = datasourceRequest.getTable();
        if (StringUtils.isBlank(table)) {
            DEException.throwException("表名不能为空");
        }

        try (ConnectionObj connectionObj = getConnection(datasourceRequest.getDatasource())) {
            Configuration cfg = parseConfig(datasourceRequest.getDatasource());
            String schemaPattern = normalizeSchema(StringUtils.defaultIfBlank(cfg.getSchema(), cfg.getUsername()));

            Set<String> pkSet = new HashSet<>();
            DatabaseMetaData metaData = connectionObj.getConnection().getMetaData();
            String upperTable = table.toUpperCase(Locale.ROOT);
            try (ResultSet pkRs = metaData.getPrimaryKeys(null, schemaPattern, upperTable)) {
                while (pkRs.next()) {
                    pkSet.add(pkRs.getString("COLUMN_NAME"));
                }
            }

            List<TableField> fields = new ArrayList<>();
            try (ResultSet rs = metaData.getColumns(null, schemaPattern, upperTable, "%")) {
                fillTableFields(rs, pkSet, fields);
            }
            if (fields.isEmpty()) {
                try (ResultSet rs = metaData.getColumns(null, schemaPattern, table, "%")) {
                    fillTableFields(rs, pkSet, fields);
                }
            }
            return fields;
        } catch (Exception e) {
            DEException.throwException("获取字段失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void hidePW(DatasourceDTO datasourceDTO) {
        if (datasourceDTO == null || StringUtils.isBlank(datasourceDTO.getConfiguration())) {
            return;
        }
        Configuration cfg = JsonUtil.parseObject(datasourceDTO.getConfiguration(), io.dataease.extensions.datasource.vo.DatasourceConfiguration.class);
        if (cfg != null && StringUtils.isNotBlank(cfg.getPassword())) {
            cfg.setPassword("******");
            datasourceDTO.setConfiguration((String) JsonUtil.toJSONString(cfg));
        }
    }

    private void fillTableFields(ResultSet rs, Set<String> pkSet, List<TableField> fields) throws SQLException {
        while (rs.next()) {
            TableField field = new TableField();
            String columnName = rs.getString("COLUMN_NAME");
            field.setName(columnName);
            field.setOriginName(columnName);
            field.setType(rs.getString("TYPE_NAME"));
            field.setTypeNumber(rs.getInt("DATA_TYPE"));
            field.setSize(rs.getLong("COLUMN_SIZE"));
            field.setScale(rs.getInt("DECIMAL_DIGITS"));
            field.setPrimaryKey(pkSet.contains(columnName));
            field.setPrimary(pkSet.contains(columnName));
            field.setAutoIncrement("YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")));
            field.setRemarks(rs.getString("REMARKS"));
            fields.add(field);
        }
    }

    private io.dataease.extensions.datasource.vo.DatasourceConfiguration parseConfig(DatasourceDTO datasourceDTO) {
        io.dataease.extensions.datasource.vo.DatasourceConfiguration cfg = JsonUtil.parseObject(
                datasourceDTO.getConfiguration(),
                io.dataease.extensions.datasource.vo.DatasourceConfiguration.class
        );
        if (cfg == null) {
            DEException.throwException("数据源配置解析失败");
        }
        return cfg;
    }

    private String resolveJdbcUrl(Configuration cfg) {
        if (StringUtils.isNotBlank(cfg.getJdbcUrl())) {
            return cfg.getJdbcUrl();
        }
        if (StringUtils.isNotBlank(cfg.getUrl())) {
            return cfg.getUrl();
        }
        if (StringUtils.isNotBlank(cfg.getJdbc())) {
            return cfg.getJdbc();
        }

        String host = StringUtils.defaultIfBlank(cfg.getLHost(), "127.0.0.1");
        Integer port = cfg.getLPort() == null ? DEFAULT_PORT : cfg.getLPort();
        String db = StringUtils.defaultIfBlank(cfg.getDataBase(), "test");

        String jdbcUrl = String.format("jdbc:oceanbase://%s:%d/%s", host, port, db);

        String extraParams = cfg.getExtraParams();
        if (StringUtils.isBlank(extraParams)) {
            return jdbcUrl + "?compatibleMode=oracle";
        }

        String normalized = extraParams.trim();
        if (normalized.startsWith("?")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("&")) {
            normalized = normalized.substring(1);
        }
        if (StringUtils.isBlank(normalized)) {
            return jdbcUrl + "?compatibleMode=oracle";
        }

        if (!StringUtils.containsIgnoreCase(normalized, "compatibleMode=")) {
            normalized = "compatibleMode=oracle&" + normalized;
        }
        return jdbcUrl + "?" + normalized;
    }

    private String normalizeSchema(String schema) {
        if (StringUtils.isBlank(schema)) {
            return null;
        }
        return schema.toUpperCase(Locale.ROOT);
    }
}

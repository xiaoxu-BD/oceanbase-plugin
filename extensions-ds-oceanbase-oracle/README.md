# OceanBase Oracle 兼容数据源插件（backend）

这是一个可导入 DataEase 的 OceanBase（Oracle 兼容）插件后端实现，结构参考 README 中的 Hive 示例。

## 目录建议（与 README 对齐）

- `extensions-ds-oceanbase-oracle`：后端（本目录）
- `extensions-ds-oceanbase-oracle-frontend`：前端开发态
- `extensions-ds-oceanbase-oracle-frontend-package`：前端打包态

## 后端打包

在当前目录执行：

```bash
mvn clean package
```

生成的 jar（例如 `oceanbase-oracle-backend-2.10.20.jar`）可在 DataEase 插件管理页面上传。

## 配置建议

- `type`: `oceanbaseOracle`（需与 `plugin/extensions-ds-oceanbase-oracle.json` 一致）
- `flag`: `31`（确保不与现有数据源冲突）
- 默认 JDBC URL：

```text
jdbc:oceanbase://<host>:2881/<db>?compatibleMode=oracle
```

也可直接在数据源配置中填写 `jdbcUrl` / `url` 覆盖默认拼接逻辑。

## 驱动

插件会从插件 jar 中提取内置驱动到 `driverPath`。你可以将 OceanBase JDBC 驱动 jar 放入本项目 `src/main/resources`（或子目录）后再打包。

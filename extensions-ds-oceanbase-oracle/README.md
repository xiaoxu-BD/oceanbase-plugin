# OceanBase Oracle 兼容数据源插件（backend）

这是一个可导入 DataEase 的数据源后端插件示例，结构参考 README 中的 hive-backend。

## 打包

在当前目录执行：

```bash
mvn clean package
```

生成的 jar（例如 `oceanbase-oracle-backend-2.10.20.jar`）即可在 DataEase 插件管理页面上传。

## 配置建议

- `type`: `oceanbaseOracle`（需和 `plugin/*.json` 中一致）
- `flag`: `31`（确保与已有数据源不冲突）
- 默认 JDBC URL 形如：

```text
jdbc:oceanbase://<host>:2881/<db>?compatibleMode=oracle
```

也可以直接在数据源配置中填写 `jdbcUrl` / `url` 覆盖。

## 驱动

插件支持从插件 jar 内释放驱动到 `driverPath`；请将 OceanBase JDBC 驱动 jar 放入本项目 `src/main/resources`（或其子目录）后再打包。

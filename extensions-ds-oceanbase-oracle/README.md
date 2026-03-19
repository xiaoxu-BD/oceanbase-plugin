# OceanBase Oracle 兼容数据源插件（backend）

这是一个可导入 DataEase 的 OceanBase（Oracle 兼容）插件后端实现，结构参考 README 中的 Hive 示例。

## 目录建议（与 README 对齐）

- `extensions-ds-oceanbase-oracle`：后端（本目录）
- `extensions-ds-oceanbase-oracle-frontend`：前端开发态
- `extensions-ds-oceanbase-oracle-frontend-package`：前端打包态（产物会打进后端 jar）

## 如何把前端一起打进同一个 jar（与 hive 一致）

后端 `pom.xml` 已配置 `maven-antrun-plugin`，在 `generate-resources` 阶段会把：

`../extensions-ds-oceanbase-oracle-frontend-package/dist/*.js`

复制到后端资源目录：

`src/main/resources/static/de2api/`

最终打包进 jar 后，结构会和 hive 插件一样，前端入口在：

`/static/de2api/extensions-ds-oceanbase-oracle.js`

> 如果没有执行前端打包，仓库里也提供了一个可直接使用的兜底入口：
> `src/main/resources/static/de2api/extensions-ds-oceanbase-oracle.js`。

## 后端打包

在当前目录执行：

```bash
mvn clean package
```

打包后可用下面命令检查 jar 内是否包含前端文件（和 hive 类似）：

```bash
jar tf target/extensions-ds-oceanbase-oracle-2.10.20.jar | grep -E 'plugin/|static/de2api/'
```

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

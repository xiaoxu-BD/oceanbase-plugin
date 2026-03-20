# OceanBase 插件“无法联调”排查结论（2026-03-20）

## 结论摘要

当前仓库内，OceanBase 插件“无法联调”主要不是单一 bug，而是**联调链路本身不完整**，至少包含以下 5 类阻断点：

1. 前端开发态/打包态目录缺少 Node 工程文件（`package.json`、`vite.config.*`、构建脚本等），无法执行标准前端构建与联调启动。
2. 后端 `pom.xml` 依赖 `../extensions-ds-oceanbase-oracle-frontend-package/dist/*.js`，但仓库中不存在该 `dist` 目录，复制步骤无实际输入。
3. 根工程聚合 `pom.xml` 仅包含 `sdk` 模块，OceanBase 三个目录未纳入 reactor；在根目录直接执行聚合构建不会触发插件完整构建链路。
4. 插件元数据将 `requireVersion` 固定为 `v2.10.20`，若联调目标 DataEase 不是该版本，会出现兼容门槛问题。
5. 实测本环境执行 `mvn clean package` 时，Maven central 请求返回 403（插件解析失败），构建流程被外部依赖下载直接阻断。

## 证据明细

### 1) 前端目录缺少工程化构建文件

- `extensions-ds-oceanbase-oracle-frontend` 仅有 `README.md` 与 `src/component/index.vue`。
- `extensions-ds-oceanbase-oracle-frontend-package` 仅有 `README.md` 与 `src/component/index.vue`。
- 未发现 `package.json`、`vite.config.ts`、`rollup.config.js`、`pom.xml`（前端模块）等联调所需文件。

### 2) 后端复制前端产物依赖 `dist`，但 dist 缺失

后端 POM 在 `generate-resources` 阶段配置了如下复制来源：

- 来源目录：`../extensions-ds-oceanbase-oracle-frontend-package/dist`
- 包含：`**.js`

但当前仓库不存在该 `dist` 目录，导致该步骤无法复制真实前端产物。

### 3) 根 POM 未聚合插件模块

根 `pom.xml` 的 `<modules>` 当前仅声明：

- `sdk`

说明 OceanBase 插件模块并未纳入根聚合构建。

### 4) 插件版本要求可能与联调环境不匹配

`extensions-ds-oceanbase-oracle.json` 中：

- `version`: `v2.10.20`
- `requireVersion`: `v2.10.20`

如果联调环境非该版本，插件可能被判定不兼容。

### 5) 本地实测构建被 Maven 仓库访问阻断

实测命令：

```bash
cd extensions-ds-oceanbase-oracle && mvn -q -DskipTests clean package
```

报错关键点：

- `maven-clean-plugin:3.2.0 ... Could not transfer ... status code: 403`

说明即使代码链路修正，也需先解决依赖仓库访问策略。

## 建议优先级（按落地顺序）

1. 先补齐前端模块工程文件（最小集：`package.json` + 构建配置 + build 脚本）。
2. 明确“开发态联调”与“打包态产物”流程（建议脚本化同步而不是手工复制）。
3. 根据实际使用方式决定是否将 OceanBase 模块加入根聚合构建。
4. 校准 `requireVersion` 与目标 DataEase 版本（必要时按分支分别维护）。
5. 处理 Maven 仓库访问（私服镜像/代理/白名单），恢复构建可用性。

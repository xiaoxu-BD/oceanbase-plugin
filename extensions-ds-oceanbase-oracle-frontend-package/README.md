# OceanBase Oracle 插件前端（打包态）

- 入口文件：`src/component/index.vue`
- 打包入口：`src/entry.js`
- 该模块用于插件打包产物，路径需与 plugin json 的 `staticMap.index` 对齐。

## 本地打包

```bash
cd extensions-ds-oceanbase-oracle-frontend-package
npm install
npm run build
```

打包后产物：

- `dist/extensions-ds-oceanbase-oracle.js`

后端模块 `extensions-ds-oceanbase-oracle/pom.xml` 会在 `generate-resources` 阶段复制该文件到后端资源目录。

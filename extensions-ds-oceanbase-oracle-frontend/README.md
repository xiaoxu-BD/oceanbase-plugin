# OceanBase Oracle 插件前端（开发态）

- 入口文件：`src/component/index.vue`
- 本模块用于本地开发调试（与 DataEase 主工程联调）。

## 本地联调（独立预览）

```bash
cd extensions-ds-oceanbase-oracle-frontend
npm install
npm run dev
```

默认打开后可直接编辑 `src/component/index.vue` 进行交互调试。

## 与打包态同步

打包前请将组件改动同步到 `extensions-ds-oceanbase-oracle-frontend-package/src/component/index.vue`。

# OceanBase 插件联调快速开始

## 1. 前端开发态联调

```bash
cd extensions-ds-oceanbase-oracle-frontend
npm install
npm run dev
```

说明：该步骤用于快速调试 `src/component/index.vue` 交互行为。

## 2. 生成打包态产物

```bash
cd extensions-ds-oceanbase-oracle-frontend-package
npm install
npm run build
```

构建产物为：

- `extensions-ds-oceanbase-oracle-frontend-package/dist/extensions-ds-oceanbase-oracle.js`

## 3. 后端打包并复制前端产物

```bash
cd extensions-ds-oceanbase-oracle
mvn clean package
```

后端 `pom.xml` 已配置在 `generate-resources` 阶段将 `frontend-package/dist/*.js` 复制到 `src/main/resources/static/de2api`。

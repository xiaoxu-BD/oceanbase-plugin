import { createApp, reactive } from 'vue'
import OceanBaseOracleForm from './component/index.vue'

const Root = {
  components: { OceanBaseOracleForm },
  setup() {
    const data = reactive({
      urlType: 'hostName',
      host: '127.0.0.1',
      port: 2881,
      dataBase: 'test',
      schema: '',
      extraParams: 'compatibleMode=oracle',
      username: '',
      password: ''
    })

    return { data }
  },
  template: `
    <div style="max-width: 960px; margin: 24px auto; padding: 0 16px;">
      <h2>OceanBase Oracle 插件联调页</h2>
      <OceanBaseOracleForm v-model:data="data" />
      <pre style="margin-top: 16px; background: #f5f7fa; padding: 12px; border-radius: 6px;">{{ JSON.stringify(data, null, 2) }}</pre>
    </div>
  `
}

createApp(Root).mount('#app')

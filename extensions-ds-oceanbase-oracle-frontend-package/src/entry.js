import OceanBaseOracleForm from './component/index.vue'

const KEY = 'ZXh0ZW5zaW9ucy1kcy1vY2VhbmJhc2Utb3JhY2xlL2NvbXBvbmVudC9pbmRleA=='

const exported = {
  mapping: {
    [KEY]: Promise.resolve({ default: OceanBaseOracleForm })
  }
}

window['extensions-ds-oceanbase-oracle'] = exported

export default exported

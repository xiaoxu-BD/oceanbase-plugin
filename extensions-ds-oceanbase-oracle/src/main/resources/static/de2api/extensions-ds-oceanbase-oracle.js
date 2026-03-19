(function () {
  'use strict'

  const Vue = window.VueDe || window.Vue
  if (!Vue) {
    console.error('[oceanbase-oracle-plugin] Vue runtime not found.')
    return
  }

  const { defineComponent, h } = Vue

  const KEY = 'ZXh0ZW5zaW9ucy1kcy1vY2VhbmJhc2Utb3JhY2xlL2NvbXBvbmVudC9pbmRleA=='

  const withDefault = (val, fallback) => (val === undefined || val === null ? fallback : val)

  const OceanBaseOracleForm = defineComponent({
    name: 'OceanBaseOraclePluginForm',
    props: {
      form: {
        type: Object,
        required: true
      },
      activeStep: {
        type: Number,
        default: 1
      }
    },
    emits: ['submitForm'],
    setup(props, { emit, expose }) {
      const ensureCfg = () => {
        if (!props.form.configuration || typeof props.form.configuration !== 'object') {
          props.form.configuration = {}
        }
        const cfg = props.form.configuration
        cfg.urlType = withDefault(cfg.urlType, 'hostName')
        cfg.host = withDefault(cfg.host, '127.0.0.1')
        cfg.port = withDefault(cfg.port, 2881)
        cfg.dataBase = withDefault(cfg.dataBase, 'test')
        cfg.schema = withDefault(cfg.schema, '')
        cfg.extraParams = withDefault(cfg.extraParams, '')
        cfg.username = withDefault(cfg.username, '')
        cfg.password = withDefault(cfg.password, '')
        cfg.jdbcUrl = withDefault(
          cfg.jdbcUrl,
          'jdbc:oceanbase://127.0.0.1:2881/test?compatibleMode=oracle'
        )
        cfg.queryTimeout = withDefault(cfg.queryTimeout, 30)
        cfg.initialPoolSize = withDefault(cfg.initialPoolSize, 5)
        cfg.minPoolSize = withDefault(cfg.minPoolSize, 5)
        cfg.maxPoolSize = withDefault(cfg.maxPoolSize, 30)
        return cfg
      }

      const initForm = type => {
        props.form.type = type || props.form.type || 'oceanbaseOracle'
        ensureCfg()
      }

      const clearForm = () => {}

      const resetForm = () => {
        ensureCfg()
      }

      const submitForm = payload => {
        emit('submitForm', payload)
        return cb => cb(true)
      }

      expose({
        initForm,
        clearForm,
        resetForm,
        submitForm
      })

      const setField = (key, value) => {
        const cfg = ensureCfg()
        cfg[key] = value
      }

      const field = (label, child) =>
        h('div', { style: 'margin-bottom: 12px;' }, [
          h('div', { style: 'font-size:12px;color:#606266;margin-bottom:6px;' }, label),
          child
        ])

      const input = (key, placeholder, type) => {
        const cfg = ensureCfg()
        return h('input', {
          value: cfg[key] || '',
          type: type || 'text',
          placeholder,
          onInput: e => setField(key, e.target.value),
          style:
            'width:100%;height:32px;border:1px solid #dcdfe6;border-radius:4px;padding:0 10px;box-sizing:border-box;'
        })
      }

      const numberInput = (key, placeholder) => {
        const cfg = ensureCfg()
        return h('input', {
          value: cfg[key],
          type: 'number',
          placeholder,
          onInput: e => setField(key, Number(e.target.value || 0)),
          style:
            'width:100%;height:32px;border:1px solid #dcdfe6;border-radius:4px;padding:0 10px;box-sizing:border-box;'
        })
      }

      return () => {
        const cfg = ensureCfg()
        const mainChildren = [
          field(
            '连接方式',
            h('div', { style: 'display:flex;gap:16px;align-items:center;' }, [
              h('label', [
                h('input', {
                  type: 'radio',
                  name: 'ob_url_type',
                  checked: cfg.urlType === 'hostName',
                  onChange: () => setField('urlType', 'hostName')
                }),
                ' 主机名'
              ]),
              h('label', [
                h('input', {
                  type: 'radio',
                  name: 'ob_url_type',
                  checked: cfg.urlType === 'jdbcUrl',
                  onChange: () => setField('urlType', 'jdbcUrl')
                }),
                ' JDBC URL'
              ])
            ])
          )
        ]

        if (cfg.urlType === 'jdbcUrl') {
          mainChildren.push(field('JDBC URL', input('jdbcUrl', 'jdbc:oceanbase://127.0.0.1:2881/test?compatibleMode=oracle')))
        } else {
          mainChildren.push(field('Host', input('host', '127.0.0.1')))
          mainChildren.push(field('Port', numberInput('port', '2881')))
          mainChildren.push(field('Database', input('dataBase', 'test')))
          mainChildren.push(field('Schema（可选）', input('schema', 'SYS')))
          mainChildren.push(field('Extra Params（可选）', input('extraParams', 'useUnicode=true&characterEncoding=UTF-8')))
        }

        mainChildren.push(field('Username', input('username', '请输入用户名')))
        mainChildren.push(field('Password', input('password', '请输入密码', 'password')))

        mainChildren.push(
          h('div', { style: 'display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;' }, [
            field('Initial Pool Size', numberInput('initialPoolSize', '5')),
            field('Min Pool Size', numberInput('minPoolSize', '5')),
            field('Max Pool Size', numberInput('maxPoolSize', '30'))
          ])
        )

        mainChildren.push(field('Query Timeout（秒）', numberInput('queryTimeout', '30')))

        return h('div', { style: 'padding: 8px 0 0; max-width: 720px;' }, mainChildren)
      }
    }
  })

  const exported = {
    mapping: {
      [KEY]: Promise.resolve({ default: OceanBaseOracleForm })
    }
  }

  window['extensions-ds-oceanbase-oracle'] = exported
})()

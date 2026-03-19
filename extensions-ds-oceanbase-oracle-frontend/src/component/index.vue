<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:data'])

const form = computed({
  get: () => props.data || {},
  set: val => emit('update:data', val)
})

const connectionType = computed(() => form.value.urlType || 'hostName')

const updateField = (key, val) => {
  form.value = {
    ...form.value,
    [key]: val
  }
}

const onConnectionTypeChange = type => {
  updateField('urlType', type)
}
</script>

<template>
  <el-form label-position="top" size="default">
    <el-form-item label="连接方式">
      <el-radio-group :model-value="connectionType" @update:model-value="onConnectionTypeChange">
        <el-radio-button label="hostName">主机名</el-radio-button>
        <el-radio-button label="jdbcUrl">JDBC URL</el-radio-button>
      </el-radio-group>
    </el-form-item>

    <el-form-item v-if="connectionType === 'jdbcUrl'" label="JDBC URL">
      <el-input
        :model-value="form.jdbcUrl || ''"
        placeholder="jdbc:oceanbase://127.0.0.1:2881/test?compatibleMode=oracle"
        @update:model-value="v => updateField('jdbcUrl', v)"
      />
    </el-form-item>

    <template v-else>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="Host">
            <el-input
              :model-value="form.host || ''"
              placeholder="127.0.0.1"
              @update:model-value="v => updateField('host', v)"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Port">
            <el-input-number
              :model-value="form.port || 2881"
              :min="1"
              :max="65535"
              style="width: 100%"
              @update:model-value="v => updateField('port', v)"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="Database">
            <el-input
              :model-value="form.dataBase || ''"
              placeholder="test"
              @update:model-value="v => updateField('dataBase', v)"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Schema（可选）">
            <el-input
              :model-value="form.schema || ''"
              placeholder="SYS"
              @update:model-value="v => updateField('schema', v)"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="Extra Params（可选）">
        <el-input
          :model-value="form.extraParams || ''"
          placeholder="useUnicode=true&characterEncoding=UTF-8"
          @update:model-value="v => updateField('extraParams', v)"
        />
      </el-form-item>
    </template>

    <el-row :gutter="12">
      <el-col :span="12">
        <el-form-item label="Username">
          <el-input
            :model-value="form.username || ''"
            @update:model-value="v => updateField('username', v)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="Password">
          <el-input
            :model-value="form.password || ''"
            type="password"
            show-password
            @update:model-value="v => updateField('password', v)"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="8">
        <el-form-item label="Initial Pool Size">
          <el-input-number
            :model-value="form.initialPoolSize || 5"
            :min="1"
            style="width: 100%"
            @update:model-value="v => updateField('initialPoolSize', v)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="Min Pool Size">
          <el-input-number
            :model-value="form.minPoolSize || 5"
            :min="1"
            style="width: 100%"
            @update:model-value="v => updateField('minPoolSize', v)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="Max Pool Size">
          <el-input-number
            :model-value="form.maxPoolSize || 30"
            :min="1"
            style="width: 100%"
            @update:model-value="v => updateField('maxPoolSize', v)"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item label="Query Timeout（秒）">
      <el-input-number
        :model-value="form.queryTimeout || 30"
        :min="1"
        style="width: 100%"
        @update:model-value="v => updateField('queryTimeout', v)"
      />
    </el-form-item>
  </el-form>
</template>

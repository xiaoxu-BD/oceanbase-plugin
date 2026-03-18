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

const updateField = (key, val) => {
  form.value = {
    ...form.value,
    [key]: val
  }
}
</script>

<template>
  <el-form label-position="top" size="default">
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

    <el-form-item label="JDBC URL（可选，优先于 host/port）">
      <el-input
        :model-value="form.jdbcUrl || ''"
        placeholder="jdbc:oceanbase://127.0.0.1:2881/test?compatibleMode=oracle"
        @update:model-value="v => updateField('jdbcUrl', v)"
      />
    </el-form-item>
  </el-form>
</template>

<template>
  <div>
    <h3>系统配置</h3>
    <el-table :data="configs" stripe>
      <el-table-column prop="configKey" label="配置键" />
      <el-table-column label="配置值">
        <template #default="{ row }">
          <el-input v-model="row.configValue" size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="saveConfig(row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigList, updateConfig } from '../../api/config'

const configs = ref([])

const fetchConfigs = async () => {
  try {
    configs.value = await getConfigList()
  } catch {
    configs.value = []
  }
}

const saveConfig = async (row) => {
  try {
    await updateConfig(row.configKey, row.configValue)
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  }
}

onMounted(fetchConfigs)
</script>

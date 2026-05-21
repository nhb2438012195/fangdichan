<template>
  <div>
    <h3>用户管理</h3>
    <el-table v-loading="loading" :data="users" stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="角色" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">{{ row.status ? '启用' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button
            :type="row.status ? 'warning' : 'success'"
            :loading="togglingId === row.id"
            @click="handleToggleStatus(row)"
          >
            {{ row.status ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, toggleUserStatus } from '../../api/user'

const users = ref([])
const loading = ref(false)
const togglingId = ref(null)

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await getUserList()
  } catch {
    users.value = []
  } finally {
    loading.value = false
  }
}

const handleToggleStatus = async (row) => {
  togglingId.value = row.id
  try {
    await toggleUserStatus(row.id, row.status)
    row.status = row.status ? 0 : 1
    ElMessage.success('操作成功')
  } catch {
    ElMessage.error('操作失败')
  } finally {
    togglingId.value = null
  }
}

onMounted(fetchUsers)
</script>

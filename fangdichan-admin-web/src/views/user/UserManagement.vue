<template>
  <div>
    <h3>用户管理</h3>
    <el-table :data="users" stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="角色" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">{{ row.status ? '启用' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="toggleStatus(row)" :type="row.status ? 'warning' : 'success'">
            {{ row.status ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const users = ref([])

const fetchUsers = async () => {
  const res = await request.get('/admin/users')
  users.value = res.data
}

const toggleStatus = async (row) => {
  await request.put(`/admin/users/${row.id}/status`, { status: row.status ? 0 : 1 })
  row.status = row.status ? 0 : 1
}

onMounted(fetchUsers)
</script>

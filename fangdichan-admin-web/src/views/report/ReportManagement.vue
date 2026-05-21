<template>
  <div>
    <h3>举报处理</h3>
    <el-table :data="reports" stripe>
      <el-table-column prop="reason" label="举报原因" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="handleStatus(row.id, 'DISMISSED')" v-if="row.status === 'PENDING'">驳回</el-button>
          <el-button type="primary" @click="handleStatus(row.id, 'PROCESSED')" v-if="row.status === 'PENDING'">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const reports = ref([])

const fetchReports = async () => {
  const res = await request.get('/admin/report/list')
  reports.value = res.data.list || res.data
}

const handleStatus = async (id, status) => {
  await request.put(`/admin/report/${id}/status`, {}, { params: { status } })
  ElMessage.success('操作成功')
  fetchReports()
}

onMounted(fetchReports)
</script>

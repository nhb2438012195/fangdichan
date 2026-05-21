<template>
  <div>
    <h3>举报处理</h3>
    <el-table v-loading="loading" :data="reports" stripe>
      <el-table-column prop="reason" label="举报原因" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            :loading="processingId === row.id"
            @click="handleStatus(row.id, 'DISMISSED')"
            >驳回</el-button
          >
          <el-button
            v-if="row.status === 'PENDING'"
            type="primary"
            :loading="processingId === row.id"
            @click="handleStatus(row.id, 'PROCESSED')"
            >处理</el-button
          >
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
const loading = ref(false)
const processingId = ref(null)

const fetchReports = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/report/list')
    reports.value = res.data.list || res.data || []
  } catch {
    reports.value = []
  } finally {
    loading.value = false
  }
}

const handleStatus = async (id, status) => {
  processingId.value = id
  try {
    await request.put(`/admin/report/${id}/status`, null, { params: { status } })
    ElMessage.success('操作成功')
    fetchReports()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    processingId.value = null
  }
}

onMounted(fetchReports)
</script>

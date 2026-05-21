<template>
  <div>
    <h3>审核管理</h3>
    <el-table v-loading="loading" :data="pendingList" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="district" label="区域" />
      <el-table-column prop="price" label="价格" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="success" :loading="processingId === row.id" @click="approve(row.id)"
            >通过</el-button
          >
          <el-button type="danger" :loading="processingId === row.id" @click="reject(row.id)"
            >拒绝</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <div v-if="!loading && pendingList.length === 0" class="empty-state">
      <el-empty description="暂无待审核房源" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingList, approveProperty, rejectProperty } from '../../api/property'

const pendingList = ref([])
const loading = ref(false)
const processingId = ref(null)

const fetchList = async () => {
  loading.value = true
  try {
    pendingList.value = await getPendingList()
  } catch {
    pendingList.value = []
  } finally {
    loading.value = false
  }
}

const approve = async (id) => {
  processingId.value = id
  try {
    await approveProperty(id)
    ElMessage.success('已通过')
    fetchList()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    processingId.value = null
  }
}

const reject = async (id) => {
  processingId.value = id
  try {
    await rejectProperty(id)
    ElMessage.success('已拒绝')
    fetchList()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    processingId.value = null
  }
}

onMounted(fetchList)
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>

<template>
  <div>
    <h3>订单管理</h3>
    <el-table v-loading="loading" :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            type="primary"
            :loading="processingId === row.id"
            @click="confirm(row.id)"
            >确认成交</el-button
          >
          <el-button
            v-if="row.status === 'PENDING'"
            type="danger"
            :loading="processingId === row.id"
            @click="cancel(row.id)"
            >取消</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <div
      v-if="!loading && orders.length === 0"
      style="text-align: center; padding: 40px; color: #999"
    >
      <el-empty description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const orders = ref([])
const loading = ref(false)
const processingId = ref(null)

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/agent/order/list', { params: { page: 1, size: 100 } })
    orders.value = res.data.list || []
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const confirm = async (id) => {
  processingId.value = id
  try {
    await request.put(`/agent/order/${id}/confirm`)
    ElMessage.success('已确认')
    fetchOrders()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    processingId.value = null
  }
}

const cancel = async (id) => {
  processingId.value = id
  try {
    await request.put(`/agent/order/${id}/cancel`)
    ElMessage.success('已取消')
    fetchOrders()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    processingId.value = null
  }
}

onMounted(fetchOrders)
</script>

<template>
  <div>
    <h3>我的订单</h3>
    <el-table v-loading="loading" :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            size="small"
            type="danger"
            :loading="cancellingId === row.id"
            @click="cancelOrder(row.id)"
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
const cancellingId = ref(null)

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/customer/order/list', { params: { page: 1, size: 100 } })
    orders.value = res.data.list || []
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const cancelOrder = async (id) => {
  cancellingId.value = id
  try {
    await request.put(`/customer/order/${id}/cancel`)
    ElMessage.success('已取消')
    fetchOrders()
  } catch {
    ElMessage.error('取消失败')
  } finally {
    cancellingId.value = null
  }
}

onMounted(fetchOrders)
</script>

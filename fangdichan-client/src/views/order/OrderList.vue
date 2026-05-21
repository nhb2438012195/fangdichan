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
            @click="handleCancelOrder(row.id)"
            >取消</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > pageSize" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchOrders"
      />
    </div>
    <div v-if="!loading && orders.length === 0" class="empty-state">
      <el-empty description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrderList, cancelOrder } from '../../api/order'

const orders = ref([])
const loading = ref(false)
const cancellingId = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchOrders = async () => {
  loading.value = true
  try {
    const data = await getOrderList({ page: currentPage.value, size: pageSize.value })
    orders.value = data.list || []
    total.value = data.total || 0
  } catch {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleCancelOrder = async (id) => {
  cancellingId.value = id
  try {
    await cancelOrder(id)
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

<style scoped>
.pagination-wrapper {
  margin-top: 16px;
  text-align: center;
}
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>

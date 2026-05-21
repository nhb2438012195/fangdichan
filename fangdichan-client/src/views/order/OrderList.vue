<template>
  <div>
    <h3>我的订单</h3>
    <el-table :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="cancelOrder(row.id)" v-if="row.status === 'PENDING'">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const orders = ref([])

const fetchOrders = async () => {
  const res = await request.get('/customer/order/list', { params: { page: 1, size: 100 } })
  orders.value = res.data.list || []
}

const cancelOrder = async (id) => {
  await request.put(`/customer/order/${id}/cancel`)
  ElMessage.success('已取消')
  fetchOrders()
}

onMounted(fetchOrders)
</script>

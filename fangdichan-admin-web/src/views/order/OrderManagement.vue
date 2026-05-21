<template>
  <div>
    <h3>订单管理</h3>
    <el-table :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="primary" @click="confirm(row.id)" v-if="row.status === 'PENDING'">确认成交</el-button>
          <el-button type="danger" @click="cancel(row.id)" v-if="row.status === 'PENDING'">取消</el-button>
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
  const res = await request.get('/agent/order/list', { params: { page: 1, size: 100 } })
  orders.value = res.data.list
}

const confirm = async (id) => {
  await request.put(`/agent/order/${id}/confirm`)
  ElMessage.success('已确认')
  fetchOrders()
}

const cancel = async (id) => {
  await request.put(`/agent/order/${id}/cancel`)
  ElMessage.success('已取消')
  fetchOrders()
}

onMounted(fetchOrders)
</script>

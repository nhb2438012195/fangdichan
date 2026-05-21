<template>
  <div>
    <h3>审核管理</h3>
    <el-table :data="pendingList" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="district" label="区域" />
      <el-table-column prop="price" label="价格" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="success" @click="approve(row.id)">通过</el-button>
          <el-button type="danger" @click="reject(row.id)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const pendingList = ref([])

const fetchList = async () => {
  const res = await request.get('/admin/property/pending')
  pendingList.value = res.data
}

const approve = async (id) => {
  await request.put(`/admin/property/${id}/approve`)
  ElMessage.success('已通过')
  fetchList()
}

const reject = async (id) => {
  await request.put(`/admin/property/${id}/reject`)
  ElMessage.success('已拒绝')
  fetchList()
}

onMounted(fetchList)
</script>

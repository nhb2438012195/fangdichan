<template>
  <div>
    <h3>房地产公司</h3>
    <div v-if="loading" style="text-align: center; padding: 40px; color: #999">加载中...</div>
    <div v-else-if="companies.length === 0" style="text-align: center; padding: 40px; color: #999">
      <el-empty description="暂无公司信息" />
    </div>
    <div v-else>
      <div v-for="c in companies" :key="c.id" style="margin-bottom: 12px">
        <el-card style="cursor: pointer" @click="$router.push('/company/' + c.id)">
          <h4>{{ c.companyName }}</h4>
          <p>{{ c.address }} | {{ c.contactPhone }}</p>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const companies = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/customer/company/list')
    companies.value = res.data || []
  } catch {
    companies.value = []
  } finally {
    loading.value = false
  }
})
</script>

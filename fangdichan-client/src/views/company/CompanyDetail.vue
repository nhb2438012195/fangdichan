<template>
  <div v-loading="loading">
    <div v-if="company">
      <el-card>
        <h3>{{ company.companyName }}</h3>
        <p>地址: {{ company.address }}</p>
        <p>电话: {{ company.contactPhone }}</p>
        <p>简介: {{ company.description }}</p>
      </el-card>
      <h4 style="margin-top: 16px">该公司房源</h4>
      <div v-if="properties.length === 0" style="color: #999; padding: 16px; text-align: center">
        暂无房源
      </div>
      <div v-for="p in properties" :key="p.id" style="margin-bottom: 8px">
        <el-card style="cursor: pointer" @click="$router.push('/detail/' + p.id)">
          <h4>{{ p.title }}</h4>
          <p>¥{{ p.price }} | {{ p.area }}㎡ | {{ p.roomType }}</p>
        </el-card>
      </div>
    </div>
    <div v-else-if="!loading" style="text-align: center; padding: 40px; color: #999">
      公司信息不存在
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../api/request'

const route = useRoute()
const company = ref(null)
const properties = ref([])
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    const [res, res2] = await Promise.all([
      request.get(`/customer/company/${route.params.id}`),
      request.get('/customer/property/search', {
        params: { companyId: route.params.id, page: 1, size: 100 }
      })
    ])
    company.value = res.data
    properties.value = res2.data.list || []
  } catch {
    company.value = null
  } finally {
    loading.value = false
  }
})
</script>

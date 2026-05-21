<template>
  <div v-if="company">
    <el-card>
      <h3>{{ company.companyName }}</h3>
      <p>地址: {{ company.address }}</p>
      <p>电话: {{ company.contactPhone }}</p>
      <p>简介: {{ company.description }}</p>
    </el-card>
    <h4 style="margin-top:16px">该公司房源</h4>
    <div v-for="p in properties" :key="p.id" style="margin-bottom:8px">
      <el-card @click="$router.push('/detail/'+p.id)" style="cursor:pointer">
        <h4>{{ p.title }}</h4>
        <p>¥{{ p.price }} | {{ p.area }}㎡ | {{ p.roomType }}</p>
      </el-card>
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

onMounted(async () => {
  const res = await request.get(`/customer/company/${route.params.id}`)
  company.value = res.data
  const res2 = await request.get('/customer/property/search', { params: { companyId: route.params.id, page: 1, size: 100 } })
  properties.value = res2.data.list || []
})
</script>

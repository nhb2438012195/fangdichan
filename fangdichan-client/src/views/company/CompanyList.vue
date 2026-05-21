<template>
  <div>
    <h3>房地产公司</h3>
    <div v-for="c in companies" :key="c.id" style="margin-bottom:12px">
      <el-card @click="$router.push('/company/'+c.id)" style="cursor:pointer">
        <h4>{{ c.companyName }}</h4>
        <p>{{ c.address }} | {{ c.contactPhone }}</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const companies = ref([])

onMounted(async () => {
  const res = await request.get('/customer/company/list')
  companies.value = res.data || []
})
</script>

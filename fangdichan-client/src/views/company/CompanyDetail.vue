<template>
  <div v-loading="loading">
    <div v-if="company">
      <el-card>
        <h3>{{ company.companyName }}</h3>
        <p>地址: {{ company.address }}</p>
        <p>电话: {{ company.contactPhone }}</p>
        <p>简介: {{ company.description }}</p>
      </el-card>
      <h4 class="section-title">该公司房源</h4>
      <div v-if="properties.length === 0" class="empty-state">暂无房源</div>
      <div v-for="p in properties" :key="p.id" class="property-item">
        <el-card class="clickable-card" @click="$router.push('/detail/' + p.id)">
          <h4>{{ p.title }}</h4>
          <p>¥{{ p.price }} | {{ p.area }}㎡ | {{ p.roomType }}</p>
        </el-card>
      </div>
    </div>
    <div v-else-if="!loading" class="empty-state">公司信息不存在</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCompanyDetail } from '../../api/company'
import { searchProperties } from '../../api/property'

const route = useRoute()
const company = ref(null)
const properties = ref([])
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    const [companyData, searchData] = await Promise.all([
      getCompanyDetail(route.params.id),
      searchProperties({ companyId: route.params.id, page: 1, size: 100 })
    ])
    company.value = companyData
    properties.value = searchData.list || []
  } catch {
    company.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.section-title {
  margin-top: 16px;
}
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
.property-item {
  margin-bottom: 8px;
}
.clickable-card {
  cursor: pointer;
}
</style>

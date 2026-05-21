<template>
  <div>
    <h3>房地产公司</h3>
    <div v-if="loading" class="empty-state">加载中...</div>
    <div v-else-if="companies.length === 0" class="empty-state">
      <el-empty description="暂无公司信息" />
    </div>
    <div v-else>
      <div v-for="c in companies" :key="c.id" class="company-item">
        <el-card class="clickable-card" @click="$router.push('/company/' + c.id)">
          <h4>{{ c.companyName }}</h4>
          <p>{{ c.address }} | {{ c.contactPhone }}</p>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCompanyList } from '../../api/company'

const companies = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    companies.value = await getCompanyList()
  } catch {
    companies.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
.company-item {
  margin-bottom: 12px;
}
.clickable-card {
  cursor: pointer;
}
</style>

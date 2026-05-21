<template>
  <div>
    <h3>我的收藏</h3>
    <div
      v-if="favorites.length"
      style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px"
    >
      <el-card v-for="f in favorites" :key="f.id" shadow="hover">
        <h4>{{ f.title }}</h4>
        <p>¥{{ f.price }}</p>
        <el-button
          size="small"
          type="danger"
          :loading="removingId === f.id"
          @click="removeFavorite(f.id)"
          >取消收藏</el-button
        >
      </el-card>
    </div>
    <el-empty v-else description="暂无收藏" />
    <div style="margin-top: 16px; text-align: center">
      <el-pagination
        v-if="total > size"
        v-model:current-page="currentPage"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchFavs"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const favorites = ref([])
const currentPage = ref(1)
const size = ref(12)
const total = ref(0)
const removingId = ref(null)

const fetchFavs = async () => {
  try {
    const res = await request.get('/customer/favorite/list', {
      params: { page: currentPage.value, size: size.value }
    })
    favorites.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } catch {
    ElMessage.error('获取收藏列表失败')
  }
}

const removeFavorite = async (propertyId) => {
  removingId.value = propertyId
  try {
    await request.post(`/customer/favorite/${propertyId}`)
    ElMessage.success('已取消收藏')
    fetchFavs()
  } catch {
    ElMessage.error('取消收藏失败')
  } finally {
    removingId.value = null
  }
}

onMounted(fetchFavs)
</script>

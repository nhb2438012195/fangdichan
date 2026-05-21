<template>
  <div>
    <h3>我的收藏</h3>
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px">
      <el-card v-for="f in favorites" :key="f.id" shadow="hover">
        <h4>{{ f.title }}</h4>
        <p>¥{{ f.price }}</p>
        <el-button size="small" type="danger" @click="removeFavorite(f.id)">取消收藏</el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const favorites = ref([])

const fetchFavs = async () => {
  const res = await request.get('/customer/favorite/list')
  favorites.value = res.data.list || res.data
}

const removeFavorite = async (id) => {
  await request.delete(`/customer/favorite/${id}`)
  ElMessage.success('已取消收藏')
  fetchFavs()
}

onMounted(fetchFavs)
</script>

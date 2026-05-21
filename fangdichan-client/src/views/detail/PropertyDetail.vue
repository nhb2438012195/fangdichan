<template>
  <div v-if="property">
    <el-carousel height="300px" indicator-position="none">
      <el-carousel-item v-for="i in 5" :key="i"><div style="background:#e0e0e0;height:100%;display:flex;align-items:center;justify-content:center;color:#999">图片 {{ i }}</div></el-carousel-item>
    </el-carousel>
    <el-card style="margin-top:16px">
      <h2>{{ property.title }}</h2>
      <p style="color:#f56c6c;font-size:24px">¥{{ property.price }}</p>
      <p>单价: ¥{{ property.unitPrice }}/㎡ | 面积: {{ property.area }}㎡ | 户型: {{ property.roomType }}</p>
      <p>区域: {{ property.district }} | 楼层: {{ property.floor || '-' }}/{{ property.floorTotal }}层</p>
      <p>地址: {{ property.location }}</p>
      <p>{{ property.description }}</p>
    </el-card>
    <div style="display:flex;gap:8px;margin-top:16px">
      <el-button type="danger" @click="toggleFavorite">{{ isFav ? '取消收藏' : '❤️ 收藏' }}</el-button>
      <el-button type="primary" @click="createOrder">立即购买</el-button>
      <el-button @click="$router.push('/message')">联系房地产商</el-button>
      <el-button @click="$router.push('/report/'+property.id)">举报</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const route = useRoute()
const property = ref(null)
const isFav = ref(false)

onMounted(async () => {
  const res = await request.get(`/customer/property/${route.params.id}`)
  property.value = res.data
})

const toggleFavorite = async () => {
  if (isFav.value) {
    await request.delete(`/customer/favorite/${route.params.id}`)
    isFav.value = false
  } else {
    await request.post('/customer/favorite', { propertyId: route.params.id })
    isFav.value = true
  }
  ElMessage.success(isFav.value ? '已收藏' : '已取消收藏')
}

const createOrder = async () => {
  await request.post('/customer/order', { propertyId: route.params.id, message: '我想购买' })
  ElMessage.success('订单已提交')
}
</script>

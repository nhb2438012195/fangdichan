<template>
  <div v-if="property">
    <el-carousel height="300px" indicator-position="none">
      <el-carousel-item v-for="i in 5" :key="i"
        ><div
          style="
            background: #e0e0e0;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #999;
          "
        >
          图片 {{ i }}
        </div></el-carousel-item
      >
    </el-carousel>
    <el-card style="margin-top: 16px">
      <h2>{{ property.title }}</h2>
      <p style="color: #f56c6c; font-size: 24px">¥{{ property.price }}</p>
      <p>
        单价: ¥{{ property.unitPrice }}/㎡ | 面积: {{ property.area }}㎡ | 户型:
        {{ property.roomType }}
      </p>
      <p>
        区域: {{ property.district }} | 楼层: {{ property.floor || '-' }}/{{
          property.floorTotal
        }}层
      </p>
      <p>地址: {{ property.location }}</p>
      <p>{{ property.description }}</p>
    </el-card>
    <div style="display: flex; gap: 8px; margin-top: 16px">
      <el-button type="danger" @click="toggleFavorite">{{
        isFav ? '取消收藏' : '❤️ 收藏'
      }}</el-button>
      <el-button type="primary" @click="createOrder">立即购买</el-button>
      <el-button :loading="contacting" @click="contactAgent">联系房地产商</el-button>
      <el-button @click="$router.push('/report/' + property.id)">举报</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const route = useRoute()
const router = useRouter()
const property = ref(null)
const isFav = ref(false)
const contacting = ref(false)

onMounted(async () => {
  const res = await request.get(`/customer/property/${route.params.id}`)
  property.value = res.data
  try {
    const favRes = await request.get(`/customer/favorite/check/${route.params.id}`)
    isFav.value = favRes.data
  } catch {}
})

const toggleFavorite = async () => {
  const res = await request.post(`/customer/favorite/${route.params.id}`)
  isFav.value = res.data
  ElMessage.success(isFav.value ? '已收藏' : '已取消收藏')
}

const contactAgent = async () => {
  if (!property.value?.companyId) {
    ElMessage.warning('该房源暂无关联房地产商')
    return
  }
  contacting.value = true
  try {
    const res = await request.post('/customer/conversation', null, {
      params: { companyId: property.value.companyId, propertyId: property.value.id }
    })
    router.push(`/message?conversationId=${res.data.id}`)
  } catch {
    ElMessage.error('创建会话失败')
  } finally {
    contacting.value = false
  }
}

const createOrder = async () => {
  const params = new URLSearchParams()
  params.append('propertyId', route.params.id)
  params.append('message', '我想购买')
  await request.post('/customer/order', params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
  ElMessage.success('订单已提交')
}
</script>

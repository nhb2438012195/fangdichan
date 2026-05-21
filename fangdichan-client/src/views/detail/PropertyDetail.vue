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
      <el-button type="danger" @click="handleToggleFavorite">{{
        isFav ? '取消收藏' : '❤️ 收藏'
      }}</el-button>
      <el-button type="primary" @click="handleCreateOrder">立即购买</el-button>
      <el-button :loading="contacting" @click="handleContactAgent">联系房地产商</el-button>
      <el-button @click="$router.push('/report/' + property.id)">举报</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPropertyDetail } from '../../api/property'
import { toggleFavorite, checkFavorite } from '../../api/favorite'
import { createConversation } from '../../api/message'
import { createOrder } from '../../api/order'

const route = useRoute()
const router = useRouter()
const property = ref(null)
const isFav = ref(false)
const contacting = ref(false)

onMounted(async () => {
  try {
    property.value = await getPropertyDetail(route.params.id)
  } catch {
    return
  }
  try {
    isFav.value = await checkFavorite(route.params.id)
  } catch {}
})

const handleToggleFavorite = async () => {
  try {
    isFav.value = await toggleFavorite(route.params.id)
    ElMessage.success(isFav.value ? '已收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleContactAgent = async () => {
  if (!property.value?.companyId) {
    ElMessage.warning('该房源暂无关联房地产商')
    return
  }
  contacting.value = true
  try {
    const data = await createConversation(property.value.companyId, property.value.id)
    router.push(`/message?conversationId=${data.id}`)
  } catch {
    ElMessage.error('创建会话失败')
  } finally {
    contacting.value = false
  }
}

const handleCreateOrder = async () => {
  try {
    await createOrder(route.params.id, '我想购买')
    ElMessage.success('订单已提交')
  } catch {
    ElMessage.error('订单提交失败')
  }
}
</script>

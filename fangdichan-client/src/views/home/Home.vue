<template>
  <div>
    <div style="display: flex; gap: 8px; margin-bottom: 16px">
      <el-input
        v-model="keyword"
        placeholder="搜索房源"
        style="flex: 1"
        @keyup.enter="quickSearch(keyword)"
      />
      <el-button type="primary" @click="quickSearch(keyword)">搜索</el-button>
      <el-button @click="openGuide">帮我找房</el-button>
    </div>
    <div style="margin-bottom: 16px">
      <el-tag
        v-for="tag in quickTags"
        :key="tag"
        style="margin-right: 8px; cursor: pointer"
        @click="quickSearch(tag)"
        >{{ tag }}</el-tag
      >
    </div>
    <h3>推荐房源</h3>
    <div v-if="loading" style="text-align: center; padding: 40px; color: #999">加载中...</div>
    <div v-else-if="properties.length === 0" style="text-align: center; padding: 40px; color: #999">
      暂无推荐房源
    </div>
    <div v-else style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px">
      <el-card
        v-for="p in properties"
        :key="p.id"
        shadow="hover"
        style="cursor: pointer"
        @click="$router.push('/detail/' + p.id)"
      >
        <div
          style="
            height: 140px;
            background: #e0e0e0;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #999;
          "
        >
          图片
        </div>
        <h4>{{ p.title }}</h4>
        <p style="color: #f56c6c; font-size: 18px">¥{{ p.price }}</p>
        <p style="font-size: 12px; color: #999">
          {{ p.area }}㎡ / {{ p.roomType }} / {{ p.floor || '-' }}层
        </p>
      </el-card>
    </div>

    <el-dialog v-model="showGuide" title="帮我找房" width="500px" @close="resetGuide">
      <div v-if="guideStep === 1">
        <h4>选择区域</h4>
        <el-select v-model="guideForm.district" placeholder="区域">
          <el-option v-for="d in districts" :key="d" :value="d" />
        </el-select>
      </div>
      <div v-if="guideStep === 2">
        <h4>选择预算</h4>
        <el-input-number v-model="guideForm.priceMin" placeholder="最低价" /> ~
        <el-input-number v-model="guideForm.priceMax" placeholder="最高价" />
      </div>
      <div v-if="guideStep === 3">
        <h4>选择户型</h4>
        <el-select v-model="guideForm.roomType" placeholder="户型">
          <el-option v-for="r in roomTypes" :key="r" :value="r" />
        </el-select>
      </div>
      <div v-if="guideStep === 4">
        <h4>推荐结果</h4>
        <div v-if="guideLoading" style="text-align: center; padding: 20px; color: #999">
          搜索中...
        </div>
        <div
          v-else-if="guideResults.length === 0"
          style="text-align: center; padding: 20px; color: #999"
        >
          未找到符合条件的房源
        </div>
        <div v-else>
          <div
            v-for="p in guideResults"
            :key="p.id"
            style="padding: 8px; border-bottom: 1px solid #eee"
          >
            <router-link :to="'/detail/' + p.id">{{ p.title }} - ¥{{ p.price }}</router-link>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="guideStep > 1 ? guideStep-- : (showGuide = false)">上一步</el-button>
        <el-button type="primary" :loading="guideLoading" @click="nextGuide">{{
          guideStep < 4 ? '下一步' : '完成'
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api/request'
import { DISTRICTS, ROOM_TYPES } from '../../constants'

const router = useRouter()
const keyword = ref('')
const properties = ref([])
const loading = ref(false)
const showGuide = ref(false)
const guideStep = ref(1)
const guideLoading = ref(false)
const guideForm = ref({ district: '', priceMin: null, priceMax: null, roomType: '' })
const guideResults = ref([])
const districts = DISTRICTS
const roomTypes = ROOM_TYPES
const quickTags = ['两室', '三室', '朝阳区', '海淀区']

const fetchRecommended = async () => {
  loading.value = true
  try {
    const res = await request.get('/customer/property/recommended')
    properties.value = res.data.list || []
  } catch {
    properties.value = []
  } finally {
    loading.value = false
  }
}

const quickSearch = (tag) => {
  router.push(`/search?q=${encodeURIComponent(tag)}`)
}

const openGuide = () => {
  resetGuide()
  showGuide.value = true
}

const resetGuide = () => {
  guideStep.value = 1
  guideForm.value = { district: '', priceMin: null, priceMax: null, roomType: '' }
  guideResults.value = []
  guideLoading.value = false
}

const nextGuide = () => {
  if (guideStep.value < 3) {
    guideStep.value++
    return
  }
  if (guideStep.value === 3) {
    guideLoading.value = true
    const params = Object.fromEntries(
      Object.entries({ ...guideForm.value, page: 1, size: 10 }).filter(
        ([, v]) => v !== null && v !== undefined && v !== ''
      )
    )
    request
      .get('/customer/property/search', { params })
      .then((res) => {
        guideResults.value = res.data.list || []
        guideStep.value++
      })
      .catch(() => {
        guideResults.value = []
      })
      .finally(() => {
        guideLoading.value = false
      })
  } else {
    showGuide.value = false
    resetGuide()
  }
}

onMounted(fetchRecommended)
</script>

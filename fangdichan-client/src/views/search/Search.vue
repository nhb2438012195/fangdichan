<template>
  <div class="search-layout">
    <div class="filter-sidebar">
      <el-card>
        <h4>筛选</h4>
        <el-form label-width="60px">
          <el-form-item label="关键词"
            ><el-input v-model="filters.keyword" placeholder="搜索房源" clearable
          /></el-form-item>
          <el-form-item label="区域"
            ><el-select v-model="filters.district" placeholder="全部" clearable class="full-width">
              <el-option v-for="d in districts" :key="d" :value="d" /> </el-select
          ></el-form-item>
          <el-form-item label="户型"
            ><el-select v-model="filters.roomType" placeholder="全部" clearable class="full-width">
              <el-option v-for="r in roomTypes" :key="r" :value="r" /> </el-select
          ></el-form-item>
          <el-form-item label="最低价"
            ><el-input-number v-model="filters.priceMin" :min="0" class="full-width"
          /></el-form-item>
          <el-form-item label="最高价"
            ><el-input-number v-model="filters.priceMax" :min="0" class="full-width"
          /></el-form-item>
          <el-form-item label="最小面积"
            ><el-input-number v-model="filters.areaMin" :min="0" class="full-width"
          /></el-form-item>
          <el-form-item label="最大面积"
            ><el-input-number v-model="filters.areaMax" :min="0" class="full-width"
          /></el-form-item>
          <el-button type="primary" class="full-width" @click="search">搜索</el-button>
        </el-form>
      </el-card>
    </div>
    <div class="result-area">
      <div v-if="loading" class="empty-state">加载中...</div>
      <div v-else-if="list.length === 0" class="empty-state">
        <p>未找到符合条件的房源</p>
        <p class="empty-state-tip">请尝试调整筛选条件或关键词</p>
      </div>
      <div v-else>
        <div v-for="p in list" :key="p.id" class="card-item">
          <el-card shadow="hover" class="card-item-inner" @click="$router.push('/detail/' + p.id)">
            <div class="card-inner">
              <div class="image-box">图片</div>
              <div>
                <h4>{{ p.title }}</h4>
                <p>¥{{ p.price }} | {{ p.area }}㎡ | {{ p.roomType }} | {{ p.district }}</p>
                <p class="meta-text">{{ p.location }}</p>
              </div>
            </div>
          </el-card>
        </div>
        <el-pagination
          v-model:current-page="page"
          background
          layout="prev,pager,next"
          :total="total"
          :page-size="10"
          @current-change="loadPage"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchProperties } from '../../api/property'
import { DISTRICTS, ROOM_TYPES } from '../../constants'

const route = useRoute()
const districts = DISTRICTS
const roomTypes = ROOM_TYPES
const filters = ref({
  keyword: '',
  district: '',
  roomType: '',
  priceMin: null,
  priceMax: null,
  areaMin: null,
  areaMax: null
})
const list = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

function cleanParams(params) {
  const cleaned = {}
  for (const [key, value] of Object.entries(params)) {
    if (value !== null && value !== undefined && value !== '') {
      if (['priceMin', 'priceMax', 'areaMin', 'areaMax'].includes(key) && value === 0) {
        continue
      }
      cleaned[key] = value
    }
  }
  return cleaned
}

const search = async () => {
  page.value = 1
  await loadPage()
}

const loadPage = async () => {
  loading.value = true
  try {
    const params = cleanParams({ ...filters.value, page: page.value, size: 10 })
    const data = await searchProperties(params)
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // Read keyword from URL query param (e.g. from quick tags on home page)
  if (route.query.q) {
    filters.value.keyword = route.query.q
  }
  loadPage()
})
</script>

<style scoped>
.search-layout {
  display: flex;
  gap: 16px;
}
.filter-sidebar {
  width: 240px;
  flex-shrink: 0;
}
.result-area {
  flex: 1;
  min-width: 0;
}
.card-item {
  margin-bottom: 12px;
}
.card-item-inner {
  cursor: pointer;
}
.card-inner {
  display: flex;
  gap: 16px;
}
.image-box {
  width: 120px;
  height: 90px;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  flex-shrink: 0;
}
.full-width {
  width: 100%;
}
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
.empty-state-tip {
  font-size: 12px;
}
.meta-text {
  font-size: 12px;
  color: #999;
}
@media (max-width: 768px) {
  .search-layout {
    flex-direction: column;
  }
  .filter-sidebar {
    width: 100%;
  }
}
</style>

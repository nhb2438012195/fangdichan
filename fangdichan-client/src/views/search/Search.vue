<template>
  <div style="display:flex;gap:16px">
    <div style="width:240px">
      <el-card>
        <h4>筛选</h4>
        <el-form label-width="60px">
          <el-form-item label="区域"><el-select v-model="filters.district" placeholder="全部" clearable style="width:100%">
            <el-option v-for="d in districts" :key="d" :value="d" />
          </el-select></el-form-item>
          <el-form-item label="户型"><el-select v-model="filters.roomType" placeholder="全部" clearable style="width:100%">
            <el-option v-for="r in roomTypes" :key="r" :value="r" />
          </el-select></el-form-item>
          <el-form-item label="最低价"><el-input-number v-model="filters.priceMin" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="最高价"><el-input-number v-model="filters.priceMax" :min="0" style="width:100%" /></el-form-item>
          <el-button type="primary" @click="search" style="width:100%">搜索</el-button>
        </el-form>
      </el-card>
    </div>
    <div style="flex:1">
      <div v-for="p in list" :key="p.id" style="margin-bottom:12px">
        <el-card shadow="hover" @click="$router.push('/detail/'+p.id)" style="cursor:pointer">
          <div style="display:flex;gap:16px">
            <div style="width:120px;height:90px;background:#e0e0e0;display:flex;align-items:center;justify-content:center;color:#999">图片</div>
            <div>
              <h4>{{ p.title }}</h4>
              <p>¥{{ p.price }} | {{ p.area }}㎡ | {{ p.roomType }} | {{ p.district }}</p>
              <p style="font-size:12px;color:#999">{{ p.location }}</p>
            </div>
          </div>
        </el-card>
      </div>
      <el-pagination background layout="prev,pager,next" :total="total" @current-change="loadPage" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const districts = ['朝阳区', '海淀区', '东城区', '西城区', '丰台区']
const roomTypes = ['一室', '两室', '三室', '四室']
const filters = ref({ district: '', roomType: '', priceMin: null, priceMax: null })
const list = ref([])
const total = ref(0)
const page = ref(1)

const search = async () => {
  page.value = 1
  loadPage()
}

const loadPage = async () => {
  const res = await request.get('/customer/property/search', { params: { ...filters.value, page: page.value, size: 10 } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

onMounted(loadPage)
</script>

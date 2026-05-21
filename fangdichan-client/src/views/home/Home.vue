<template>
  <div>
    <div style="display:flex;gap:8px;margin-bottom:16px">
      <el-input v-model="keyword" placeholder="搜索房源" style="flex:1" />
      <el-button type="primary" @click="$router.push('/search')">搜索</el-button>
      <el-button @click="showGuide = true">帮我找房</el-button>
    </div>
    <div style="margin-bottom:16px">
      <el-tag v-for="tag in quickTags" :key="tag" style="margin-right:8px;cursor:pointer"
              @click="quickSearch(tag)">{{ tag }}</el-tag>
    </div>
    <h3>推荐房源</h3>
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px">
      <el-card v-for="p in properties" :key="p.id" shadow="hover" @click="$router.push('/detail/'+p.id)" style="cursor:pointer">
        <div style="height:140px;background:#e0e0e0;display:flex;align-items:center;justify-content:center;color:#999">图片</div>
        <h4>{{ p.title }}</h4>
        <p style="color:#f56c6c;font-size:18px">¥{{ p.price }}</p>
        <p style="font-size:12px;color:#999">{{ p.area }}㎡ / {{ p.roomType }} / {{ p.floor || '-' }}层</p>
      </el-card>
    </div>

    <el-dialog v-model="showGuide" title="帮我找房" width="500px">
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
        <div v-for="p in guideResults" :key="p.id" style="padding:8px;border-bottom:1px solid #eee">
          <router-link :to="'/detail/'+p.id">{{ p.title }} - ¥{{ p.price }}</router-link>
        </div>
      </div>
      <template #footer>
        <el-button @click="guideStep > 1 ? guideStep-- : (showGuide=false)">上一步</el-button>
        <el-button type="primary" @click="nextGuide">{{ guideStep < 4 ? '下一步' : '完成' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api/request'

const router = useRouter()
const keyword = ref('')
const properties = ref([])
const showGuide = ref(false)
const guideStep = ref(1)
const guideForm = ref({ district: '', priceMin: null, priceMax: null, roomType: '' })
const guideResults = ref([])
const districts = ['朝阳区', '海淀区', '东城区', '西城区', '丰台区']
const roomTypes = ['一室', '两室', '三室', '四室']
const quickTags = ['两室', '三室', '朝阳区', '海淀区', '总价<300万']

const fetchRecommended = async () => {
  const res = await request.get('/customer/property/recommended')
  properties.value = res.data || []
}

const quickSearch = (tag) => {
  router.push(`/search?q=${tag}`)
}

const nextGuide = () => {
  if (guideStep.value < 3) { guideStep.value++; return }
  if (guideStep.value === 3) {
    request.get('/customer/property/search', { params: { ...guideForm.value, page: 1, size: 10 } })
      .then(res => { guideResults.value = res.data.list || [] })
    guideStep.value++
  } else {
    showGuide.value = false
    guideStep.value = 1
  }
}

onMounted(fetchRecommended)
</script>

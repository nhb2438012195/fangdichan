<template>
  <div>
    <h3>关联分析</h3>
    <div v-loading="loading">
      <div v-if="error" style="text-align: center; padding: 40px; color: #999">数据加载失败</div>
      <template v-else>
        <h4>按区域空置率</h4>
        <div ref="districtChart" style="height: 300px"></div>
        <h4>按楼层空置率</h4>
        <div ref="floorChart" style="height: 300px"></div>
        <h4>按户型空置率</h4>
        <div ref="roomTypeChart" style="height: 300px"></div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../../api/request'

const loading = ref(true)
const error = ref(false)
const districtChart = ref(null)
const floorChart = ref(null)
const roomTypeChart = ref(null)
let charts = []

const renderChart = (el, data, title) => {
  const chart = echarts.init(el.value)
  chart.setOption({
    title: { text: title },
    xAxis: { type: 'category', data: data.map((d) => d.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.map((d) => d.rate) }],
    tooltip: { trigger: 'axis' }
  })
  charts.push(chart)
}

const handleResize = () => charts.forEach((c) => c.resize())

onMounted(async () => {
  try {
    const res = await request.get('/agent/analysis/vacancy')
    nextTick(() => {
      const data = res.data || {}
      if (data.district) renderChart(districtChart, data.district, '按区域空置率')
      if (data.floor) renderChart(floorChart, data.floor, '按楼层空置率')
      if (data.roomType) renderChart(roomTypeChart, data.roomType, '按户型空置率')
    })
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  charts.forEach((c) => c.dispose())
  charts = []
  window.removeEventListener('resize', handleResize)
})
</script>

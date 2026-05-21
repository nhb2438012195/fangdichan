<template>
  <div>
    <h3>关联分析</h3>
    <h4>按区域空置率</h4>
    <div ref="districtChart" style="height:300px"></div>
    <h4>按楼层空置率</h4>
    <div ref="floorChart" style="height:300px"></div>
    <h4>按户型空置率</h4>
    <div ref="roomTypeChart" style="height:300px"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../../api/request'

const districtChart = ref(null)
const floorChart = ref(null)
const roomTypeChart = ref(null)

const renderChart = (el, data, title) => {
  const chart = echarts.init(el.value)
  chart.setOption({
    title: { text: title },
    xAxis: { type: 'category', data: data.map(d => d.name) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.map(d => d.rate) }],
    tooltip: { trigger: 'axis' }
  })
}

onMounted(async () => {
  const res = await request.get('/agent/analysis/vacancy')
  nextTick(() => {
    const data = res.data || {}
    if (data.district) renderChart(districtChart, data.district, '按区域空置率')
    if (data.floor) renderChart(floorChart, data.floor, '按楼层空置率')
    if (data.roomType) renderChart(roomTypeChart, data.roomType, '按户型空置率')
  })
})
</script>

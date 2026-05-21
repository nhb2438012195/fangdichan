<template>
  <div>
    <h3>公司信息</h3>
    <el-form :model="form" label-width="120px">
      <el-form-item label="公司名称"><el-input v-model="form.companyName" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
      <el-form-item label="简介"><el-input type="textarea" v-model="form.description" /></el-form-item>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const form = ref({ companyName: '', address: '', contactPhone: '', description: '' })

const fetchCompany = async () => {
  const res = await request.get('/agent/company')
  if (res.data) form.value = res.data
}

const handleSave = async () => {
  await request.put('/agent/company', form.value)
  ElMessage.success('保存成功')
}

onMounted(fetchCompany)
</script>

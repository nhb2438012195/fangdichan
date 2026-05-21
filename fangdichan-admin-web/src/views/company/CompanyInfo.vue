<template>
  <div v-loading="loading">
    <h3>公司信息</h3>
    <div v-if="loaded">
      <el-form :model="form" label-width="120px" :rules="formRules">
        <el-form-item label="公司名称" prop="companyName"
          ><el-input v-model="form.companyName"
        /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="联系电话" prop="contactPhone"
          ><el-input v-model="form.contactPhone"
        /></el-form-item>
        <el-form-item label="简介"
          ><el-input v-model="form.description" type="textarea"
        /></el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCompanyInfo, updateCompanyInfo } from '../../api/company'

const formRules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  contactPhone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确手机号', trigger: 'blur' }]
}

const form = ref({ companyName: '', address: '', contactPhone: '', description: '' })
const loading = ref(false)
const saving = ref(false)
const loaded = ref(false)

const fetchCompany = async () => {
  loading.value = true
  try {
    const data = await getCompanyInfo()
    if (data) form.value = data
    loaded.value = true
  } catch {
    // will show empty form
    loaded.value = true
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateCompanyInfo(form.value)
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchCompany)
</script>

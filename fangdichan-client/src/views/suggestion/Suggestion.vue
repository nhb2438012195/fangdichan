<template>
  <div>
    <h3>提建议</h3>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="目标公司" prop="companyId">
        <el-select v-model="form.companyId" filterable style="width: 100%">
          <el-option v-for="c in companies" :key="c.id" :value="c.id" :label="c.companyName" />
        </el-select>
      </el-form-item>
      <el-form-item label="期望房型" prop="desiredType">
        <el-select v-model="form.desiredType" style="width: 100%">
          <el-option value="一室" /><el-option value="两室" /><el-option value="三室" /><el-option
            value="四室"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="期望价格">
        <el-input-number v-model="form.desiredPriceMin" :min="0" /> ~
        <el-input-number v-model="form.desiredPriceMax" :min="0" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" />
      </el-form-item>
      <el-button type="primary" :loading="submitting" @click="submit">提交建议</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const formRef = ref(null)
const submitting = ref(false)
const companies = ref([])
const form = ref({
  companyId: '',
  desiredType: '',
  desiredPriceMin: null,
  desiredPriceMax: null,
  content: ''
})
const rules = {
  companyId: [{ required: true, message: '请选择目标公司', trigger: 'change' }],
  desiredType: [{ required: true, message: '请选择期望房型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const res = await request.get('/customer/company/list')
    companies.value = res.data || []
  } catch {
    companies.value = []
  }
})

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const params = new URLSearchParams()
    Object.entries(form.value).forEach(([k, v]) => {
      if (v !== null && v !== '') params.append(k, v)
    })
    await request.post('/customer/suggestion', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    ElMessage.success('建议已提交')
    form.value = {
      companyId: '',
      desiredType: '',
      desiredPriceMin: null,
      desiredPriceMax: null,
      content: ''
    }
  } catch {
    if (e?.response) ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

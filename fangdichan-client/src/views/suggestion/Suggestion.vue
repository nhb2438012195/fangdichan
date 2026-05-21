<template>
  <div>
    <h3>提建议</h3>
    <el-form :model="form" label-width="100px">
      <el-form-item label="目标公司">
        <el-select v-model="form.companyId" filterable style="width:100%">
          <el-option v-for="c in companies" :key="c.id" :value="c.id" :label="c.companyName" />
        </el-select>
      </el-form-item>
      <el-form-item label="期望房型">
        <el-select v-model="form.desiredType">
          <el-option value="一室" /><el-option value="两室" /><el-option value="三室" /><el-option value="四室" />
        </el-select>
      </el-form-item>
      <el-form-item label="期望价格">
        <el-input-number v-model="form.desiredPriceMin" /> ~ <el-input-number v-model="form.desiredPriceMax" />
      </el-form-item>
      <el-form-item label="内容"><el-input type="textarea" v-model="form.content" /></el-form-item>
      <el-button type="primary" @click="submit">提交建议</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const companies = ref([])
const form = ref({ companyId: '', desiredType: '', desiredPriceMin: null, desiredPriceMax: null, content: '' })

onMounted(async () => {
  const res = await request.get('/customer/company/list')
  companies.value = res.data || []
})

const submit = async () => {
  await request.post('/customer/suggestion', form.value)
  ElMessage.success('建议已提交')
  form.value = { companyId: '', desiredType: '', desiredPriceMin: null, desiredPriceMax: null, content: '' }
}
</script>

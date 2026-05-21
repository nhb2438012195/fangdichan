<template>
  <div>
    <h3>举报房源</h3>
    <el-form :model="form" label-width="100px">
      <el-form-item label="举报原因"><el-input type="textarea" v-model="form.reason" /></el-form-item>
      <el-button type="primary" @click="submit">提交举报</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const route = useRoute()
const router = useRouter()
const form = ref({ reason: '' })

const submit = async () => {
  await request.post('/customer/report', { propertyId: route.params.propertyId, reason: form.value.reason })
  ElMessage.success('举报已提交')
  router.push('/home')
}
</script>

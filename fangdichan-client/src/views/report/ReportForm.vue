<template>
  <div>
    <h3>举报房源</h3>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="举报原因" prop="reason">
        <el-input v-model="form.reason" type="textarea" placeholder="请详细描述举报原因" />
      </el-form-item>
      <el-button type="primary" :loading="submitting" @click="submit">提交举报</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitReport } from '../../api/report'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({ reason: '' })
const rules = { reason: [{ required: true, message: '请输入举报原因', trigger: 'blur' }] }

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await submitReport(route.params.propertyId, form.reason)
    ElMessage.success('举报已提交')
    router.push('/home')
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

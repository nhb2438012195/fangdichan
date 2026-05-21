<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 style="text-align: center; margin-bottom: 24px">注册账号</h2>
      <el-form :model="form">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item
          ><el-input v-model="form.password" type="password" placeholder="密码"
        /></el-form-item>
        <el-form-item
          ><el-input v-model="form.confirm" type="password" placeholder="确认密码"
        /></el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="handleRegister"
          >注册</el-button
        >
      </el-form>
      <div style="margin-top: 12px; text-align: center">
        <router-link to="/login">返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '../../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirm: '' })

const handleRegister = async () => {
  if (form.password !== form.confirm) {
    ElMessage.error('密码不一致')
    return
  }
  loading.value = true
  try {
    await registerApi(form.username, form.password, 'CUSTOMER')
    ElMessage.success('注册成功')
    router.push('/login')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.login-card {
  width: 380px;
}
</style>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 style="text-align: center; margin-bottom: 24px">🏡 购房通</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item
          ><el-input v-model="form.password" type="password" placeholder="密码"
        /></el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin"
          >登录</el-button
        >
      </el-form>
      <div style="margin-top: 12px; text-align: center">
        <router-link to="/register">注册账号</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    router.push('/home')
  } catch {
    ElMessage.error(e.response?.data?.msg || '登录失败')
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

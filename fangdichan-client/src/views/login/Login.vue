<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">🏡 购房通</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item
          ><el-input v-model="form.password" type="password" placeholder="密码"
        /></el-form-item>
        <el-button type="primary" :loading="loading" class="full-width" @click="handleLogin"
          >登录</el-button
        >
      </el-form>
      <div class="login-footer">
        <router-link to="/register">注册账号</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'

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
  max-width: 90vw;
}
.login-title {
  text-align: center;
  margin-bottom: 24px;
}
.full-width {
  width: 100%;
}
.login-footer {
  margin-top: 12px;
  text-align: center;
}
@media (max-width: 768px) {
  .login-container {
    padding: 16px;
  }
}
</style>

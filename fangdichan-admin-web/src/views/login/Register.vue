<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>注册账号</h2>
      <el-form :model="form" @submit.prevent="handleRegister">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item
          ><el-input v-model="form.password" type="password" placeholder="密码"
        /></el-form-item>
        <el-form-item
          ><el-input v-model="form.confirm" type="password" placeholder="确认密码"
        /></el-form-item>
        <el-form-item>
          <el-select v-model="form.role">
            <el-option value="AGENT" label="房地产商" />
            <el-option value="CUSTOMER" label="客户" />
          </el-select>
        </el-form-item>
        <el-button type="primary" :loading="loading" native-type="submit">注册</el-button>
        <el-button @click="router.push('/login')">返回登录</el-button>
      </el-form>
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
const form = reactive({ username: '', password: '', confirm: '', role: 'AGENT' })

const handleRegister = async () => {
  if (form.password !== form.confirm) {
    ElMessage.error('密码不一致')
    return
  }
  loading.value = true
  try {
    await registerApi(form.username, form.password, form.role)
    ElMessage.success('注册成功')
    router.push('/login')
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
  width: 400px;
}
.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
}
</style>

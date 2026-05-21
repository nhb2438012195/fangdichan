<template>
  <div>
    <h2>仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card
          ><h3>欢迎</h3>
          <p>{{ role === 'ADMIN' ? '管理员' : '房地产商' }}，欢迎使用购房通管理后台</p></el-card
        >
      </el-col>
      <el-col :span="8">
        <el-card
          ><h3>快捷入口</h3>
          <el-button v-if="role === 'ADMIN'" @click="$router.push('/audit')">审核管理</el-button>
          <el-button v-if="role === 'AGENT'" @click="$router.push('/property')">房源管理</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useAuthStore } from '../../store/auth'
const { role } = useAuthStore()
</script>

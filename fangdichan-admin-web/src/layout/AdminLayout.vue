<template>
  <el-container style="height: 100vh">
    <el-aside width="220px">
      <el-menu
        :router="true"
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">📊 仪表盘</el-menu-item>
        <el-menu-item v-if="auth.role === 'ADMIN'" index="/user">👥 用户管理</el-menu-item>
        <el-menu-item v-if="auth.role === 'AGENT'" index="/company">🏢 公司信息</el-menu-item>
        <el-menu-item v-if="auth.role === 'AGENT'" index="/property">🏠 房源管理</el-menu-item>
        <el-menu-item v-if="auth.role === 'ADMIN'" index="/audit">📋 审核管理</el-menu-item>
        <el-menu-item v-if="auth.role === 'AGENT'" index="/order">📦 订单管理</el-menu-item>
        <el-menu-item v-if="auth.role === 'AGENT'" index="/analysis">📈 关联分析</el-menu-item>
        <el-menu-item v-if="auth.role === 'ADMIN'" index="/report-handle">⚠️ 举报处理</el-menu-item>
        <el-menu-item index="/message">💬 消息</el-menu-item>
        <el-menu-item v-if="auth.role === 'ADMIN'" index="/config">⚙️ 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header
        style="
          display: flex;
          align-items: center;
          justify-content: space-between;
          background: #fff;
          border-bottom: 1px solid #dcdfe6;
        "
      >
        <span>购房通管理后台</span>
        <div>
          <span style="margin-right: 16px">{{
            auth.role === 'ADMIN' ? '管理员' : auth.role === 'AGENT' ? '房地产商' : '客户'
          }}</span>
          <el-button size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()

const handleLogout = () => {
  auth.logout()
  router.push('/login')
}
</script>

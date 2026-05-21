<template>
  <div class="app-window">
    <div class="title-bar">
      <span class="title">🏡 购房通</span>
      <div class="window-controls">
        <button @click="minimize">—</button>
        <button @click="toggleMaximize">□</button>
        <button @click="close">✕</button>
      </div>
    </div>
    <div class="app-body">
      <div class="activity-bar">
        <div
          v-for="item in navItems"
          :key="item.path"
          :class="['nav-icon', { active: $route.path.startsWith(item.path) }]"
          :title="item.label"
          @click="router.push(item.path)"
        >
          {{ item.icon }}
        </div>
      </div>
      <div class="main-area">
        <router-view />
      </div>
    </div>
    <div class="status-bar">
      <span>已登录 | {{ username }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = computed(() => localStorage.getItem('username') || '')

const navItems = [
  { path: '/home', icon: '🏠', label: '首页' },
  { path: '/search', icon: '🔍', label: '搜索' },
  { path: '/favorite', icon: '❤️', label: '收藏' },
  { path: '/message', icon: '💬', label: '消息' },
  { path: '/order', icon: '📄', label: '订单' },
  { path: '/company', icon: '🏢', label: '公司' },
  { path: '/profile', icon: '👤', label: '个人' }
]

const minimize = () => {
  if (window.electronAPI) window.electronAPI.minimize()
}
const toggleMaximize = () => {
  if (window.electronAPI) window.electronAPI.toggleMaximize()
}
const close = () => {
  if (window.electronAPI) window.electronAPI.close()
}
</script>

<style scoped>
.app-window {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.title-bar {
  background: #2c2c2c;
  color: #ccc;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  -webkit-app-region: drag;
  font-size: 13px;
}
.window-controls button {
  -webkit-app-region: no-drag;
  background: none;
  border: none;
  color: #ccc;
  padding: 4px 12px;
  cursor: pointer;
}
.window-controls button:hover {
  background: #505050;
}
.app-body {
  display: flex;
  flex: 1;
}
.activity-bar {
  width: 50px;
  background: #3c3c3c;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
  gap: 8px;
}
.nav-icon {
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 18px;
  color: #ccc;
}
.nav-icon:hover {
  background: #505050;
}
.nav-icon.active {
  background: #007acc;
}
.main-area {
  flex: 1;
  background: #f5f5f5;
  overflow: auto;
  padding: 16px;
}
.status-bar {
  background: #007acc;
  color: white;
  padding: 2px 12px;
  font-size: 11px;
  display: flex;
  justify-content: space-between;
}
</style>

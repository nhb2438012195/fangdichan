import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layout/AdminLayout.vue'

const routes = [
  { path: '/login', component: () => import('../views/login/Login.vue') },
  { path: '/register', component: () => import('../views/login/Register.vue') },
  {
    path: '/',
    component: AdminLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('../views/dashboard/Dashboard.vue') },
      { path: 'user', component: () => import('../views/user/UserManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'company', component: () => import('../views/company/CompanyInfo.vue'), meta: { role: 'AGENT' } },
      { path: 'property', component: () => import('../views/property/PropertyManagement.vue'), meta: { role: 'AGENT' } },
      { path: 'audit', component: () => import('../views/audit/AuditManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'order', component: () => import('../views/order/OrderManagement.vue'), meta: { role: 'AGENT' } },
      { path: 'analysis', component: () => import('../views/analysis/AnalysisView.vue'), meta: { role: 'AGENT' } },
      { path: 'report-handle', component: () => import('../views/report/ReportManagement.vue'), meta: { role: 'ADMIN' } },
      { path: 'message', component: () => import('../views/message/MessageView.vue') },
      { path: 'config', component: () => import('../views/config/SystemConfig.vue'), meta: { role: 'ADMIN' } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) return next('/login')
  next()
})

export default router

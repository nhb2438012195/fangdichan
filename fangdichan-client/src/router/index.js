import { createRouter, createWebHistory } from 'vue-router'
import ClientLayout from '../layout/ClientLayout.vue'

const routes = [
  { path: '/login', component: () => import('../views/login/Login.vue') },
  { path: '/register', component: () => import('../views/login/Register.vue') },
  {
    path: '/',
    component: ClientLayout,
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', component: () => import('../views/home/Home.vue') },
      { path: 'search', component: () => import('../views/search/Search.vue') },
      { path: 'detail/:id', component: () => import('../views/detail/PropertyDetail.vue') },
      { path: 'favorite', component: () => import('../views/favorite/Favorite.vue') },
      { path: 'order', component: () => import('../views/order/OrderList.vue') },
      { path: 'message', component: () => import('../views/message/MessageView.vue') },
      { path: 'company', component: () => import('../views/company/CompanyList.vue') },
      { path: 'company/:id', component: () => import('../views/company/CompanyDetail.vue') },
      { path: 'profile', component: () => import('../views/profile/Profile.vue') },
      { path: 'suggestion', component: () => import('../views/suggestion/Suggestion.vue') },
      { path: 'report/:propertyId', component: () => import('../views/report/ReportForm.vue') }
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

import { defineStore } from 'pinia'
import { loginApi } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    userId: localStorage.getItem('userId') || ''
  }),
  actions: {
    async login(username, password) {
      const res = await loginApi(username, password)
      this.token = res.data.token
      this.role = res.data.role
      this.userId = res.data.userId
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('role', res.data.role)
      localStorage.setItem('userId', res.data.userId)
    },
    logout() {
      this.token = ''
      this.role = ''
      this.userId = ''
      localStorage.clear()
    }
  }
})

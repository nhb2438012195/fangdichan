import { defineStore } from 'pinia'
import { loginApi } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || ''
  }),
  actions: {
    async login(username, password) {
      const res = await loginApi(username, password)
      this.token = res.token
      this.role = res.role
      this.userId = res.userId
      this.username = username
      localStorage.setItem('token', res.token)
      localStorage.setItem('role', res.role)
      localStorage.setItem('userId', res.userId)
      localStorage.setItem('username', username)
    },
    logout() {
      this.token = ''
      this.role = ''
      this.userId = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
    }
  }
})

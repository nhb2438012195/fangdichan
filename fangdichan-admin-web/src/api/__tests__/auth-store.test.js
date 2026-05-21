import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('../auth', () => ({
  loginApi: vi.fn()
}))

import { useAuthStore } from '../../store/auth'
import { loginApi } from '../auth'

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('login should set token, role, userId from unwrapped API response', async () => {
    loginApi.mockResolvedValue({
      token: 'admin-token',
      role: 'ADMIN',
      userId: '1'
    })

    const store = useAuthStore()
    await store.login('admin', 'admin123')

    expect(store.token).toBe('admin-token')
    expect(store.role).toBe('ADMIN')
    expect(store.userId).toBe('1')
    expect(store.username).toBe('admin')
    expect(localStorage.getItem('token')).toBe('admin-token')
    expect(localStorage.getItem('role')).toBe('ADMIN')
    expect(localStorage.getItem('userId')).toBe('1')
    expect(localStorage.getItem('username')).toBe('admin')
  })

  it('logout should clear all auth state', async () => {
    localStorage.setItem('token', 'some-token')
    localStorage.setItem('role', 'ADMIN')
    localStorage.setItem('userId', '1')
    localStorage.setItem('username', 'admin')

    const store = useAuthStore()
    store.token = 'some-token'
    store.role = 'ADMIN'
    store.userId = '1'
    store.username = 'admin'

    store.logout()

    expect(store.token).toBe('')
    expect(store.role).toBe('')
    expect(store.userId).toBe('')
    expect(store.username).toBe('')
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('role')).toBeNull()
    expect(localStorage.getItem('userId')).toBeNull()
    expect(localStorage.getItem('username')).toBeNull()
  })
})

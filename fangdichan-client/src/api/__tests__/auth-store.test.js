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
      token: 'mock-token',
      role: 'USER',
      userId: '42'
    })

    const store = useAuthStore()
    await store.login('testuser', 'password123')

    expect(store.token).toBe('mock-token')
    expect(store.role).toBe('USER')
    expect(store.userId).toBe('42')
    expect(store.username).toBe('testuser')
    expect(localStorage.getItem('token')).toBe('mock-token')
    expect(localStorage.getItem('role')).toBe('USER')
    expect(localStorage.getItem('userId')).toBe('42')
    expect(localStorage.getItem('username')).toBe('testuser')
  })

  it('logout should clear all auth state', async () => {
    localStorage.setItem('token', 'some-token')
    localStorage.setItem('role', 'USER')
    localStorage.setItem('userId', '42')
    localStorage.setItem('username', 'testuser')

    const store = useAuthStore()
    store.token = 'some-token'
    store.role = 'USER'
    store.userId = '42'
    store.username = 'testuser'

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

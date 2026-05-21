import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'

vi.mock('../../../store/auth', () => ({
  useAuthStore: vi.fn()
}))

import { useAuthStore } from '../../../store/auth'
import LoginView from '../Login.vue'

// Element Plus stubs
const stubs = {
  'el-card': { template: '<div><slot /></div>' },
  'el-form': { template: '<form><slot /></form>' },
  'el-form-item': { template: '<div><slot /></div>' },
  'el-input': {
    props: ['modelValue', 'placeholder'],
    template:
      '<input :placeholder="placeholder" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
  },
  'el-button': {
    props: ['loading', 'type'],
    template: '<button :disabled="loading" @click="$emit(\'click\')"><slot /></button>'
  },
  'router-link': { template: '<a><slot /></a>' }
}

describe('LoginView', () => {
  let router
  let mockAuth

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/home', name: 'home', component: { template: '<div>Home</div>' } }]
    })

    mockAuth = { login: vi.fn() }
    useAuthStore.mockReturnValue(mockAuth)
  })

  it('renders login form with title', () => {
    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })
    expect(wrapper.find('h2').text()).toContain('购房通')
    expect(wrapper.find('input[placeholder="用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="密码"]').exists()).toBe(true)
  })

  it('navigates to /home on successful login', async () => {
    mockAuth.login.mockResolvedValue({ token: 'test-token' })

    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })

    await wrapper.find('input[placeholder="用户名"]').setValue('testuser')
    await wrapper.find('input[placeholder="密码"]').setValue('password123')

    // Spy on router.push after mounting
    const pushSpy = vi.spyOn(router, 'push')

    await wrapper.find('button').trigger('click')

    expect(mockAuth.login).toHaveBeenCalledWith('testuser', 'password123')

    // Wait for async operations
    await wrapper.vm.$nextTick()
    await new Promise((r) => setTimeout(r, 50))

    expect(pushSpy).toHaveBeenCalledWith('/home')
  })

  it('handles login failure without throwing ReferenceError', async () => {
    // This test exposes the bug: bare `catch {` referencing undefined `e`
    mockAuth.login.mockRejectedValue(new Error('Login failed'))

    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })

    await wrapper.find('input[placeholder="用户名"]').setValue('testuser')
    await wrapper.find('input[placeholder="密码"]').setValue('password123')

    // Should not throw any error (including ReferenceError from undefined `e`)
    await expect(wrapper.find('button').trigger('click')).resolves.toBeUndefined()

    // Loading should be reset to false after failure
    await wrapper.vm.$nextTick()
    expect(wrapper.find('button').attributes('disabled')).toBeUndefined()
  })
})

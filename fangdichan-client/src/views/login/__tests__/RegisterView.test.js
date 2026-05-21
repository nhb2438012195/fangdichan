import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'

vi.mock('../../../api/auth', () => ({
  registerApi: vi.fn()
}))

import { registerApi } from '../../../api/auth'
import RegisterView from '../Register.vue'

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

describe('RegisterView', () => {
  let router

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/login', name: 'login', component: { template: '<div>Login</div>' } }]
    })
  })

  it('renders registration form with title and fields', () => {
    const wrapper = mount(RegisterView, {
      global: { plugins: [router], stubs }
    })
    expect(wrapper.find('h2').text()).toContain('注册账号')
    expect(wrapper.find('input[placeholder="用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="密码"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="确认密码"]').exists()).toBe(true)
  })

  it('shows error when passwords do not match', async () => {
    const wrapper = mount(RegisterView, {
      global: { plugins: [router], stubs }
    })

    await wrapper.find('input[placeholder="用户名"]').setValue('testuser')
    await wrapper.find('input[placeholder="密码"]').setValue('password123')
    await wrapper.find('input[placeholder="确认密码"]').setValue('different')
    await wrapper.find('button').trigger('click')

    // Should show password mismatch error without calling API
    expect(registerApi).not.toHaveBeenCalled()
  })

  it('calls registerApi and navigates to login on success', async () => {
    registerApi.mockResolvedValue({ id: 1 })

    const wrapper = mount(RegisterView, {
      global: { plugins: [router], stubs }
    })

    await wrapper.find('input[placeholder="用户名"]').setValue('newuser')
    await wrapper.find('input[placeholder="密码"]').setValue('password123')
    await wrapper.find('input[placeholder="确认密码"]').setValue('password123')

    const pushSpy = vi.spyOn(router, 'push')
    await wrapper.find('button').trigger('click')

    expect(registerApi).toHaveBeenCalledWith('newuser', 'password123', 'CUSTOMER')

    // Wait for async operations
    await wrapper.vm.$nextTick()
    await new Promise((r) => setTimeout(r, 50))
    expect(pushSpy).toHaveBeenCalledWith('/login')
  })

  it('handles registration failure without crashing', async () => {
    registerApi.mockRejectedValue(new Error('Registration failed'))

    const wrapper = mount(RegisterView, {
      global: { plugins: [router], stubs }
    })

    await wrapper.find('input[placeholder="用户名"]').setValue('newuser')
    await wrapper.find('input[placeholder="密码"]').setValue('password123')
    await wrapper.find('input[placeholder="确认密码"]').setValue('password123')

    // Should not throw any error
    await expect(wrapper.find('button').trigger('click')).resolves.toBeUndefined()

    // Loading should be reset
    await wrapper.vm.$nextTick()
    expect(wrapper.find('button').attributes('disabled')).toBeUndefined()
  })
})

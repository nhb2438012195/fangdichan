import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'

vi.mock('axios', () => ({
  default: {
    post: vi.fn(),
    create: vi.fn(() => ({
      post: vi.fn(),
      interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } }
    }))
  }
}))

const ElInput = {
  props: ['modelValue', 'type', 'placeholder'],
  template:
    '<input :type="type || \'text\'" :placeholder="placeholder" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
}

const Stubs = {
  'el-card': { template: '<div><slot /></div>' },
  'el-form': { template: '<form><slot /></form>' },
  'el-form-item': { template: '<div><slot /></div>' },
  'el-input': ElInput,
  'el-button': { template: '<button :loading="loading"><slot /></button>', props: ['loading'] }
}

describe('Login.vue', () => {
  let wrapper

  beforeEach(async () => {
    const Login = (await import('../Login.vue')).default
    const pinia = createPinia()
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/login', name: 'Login', component: Login },
        { path: '/', name: 'Dashboard', component: { template: '<div>Dashboard</div>' } }
      ]
    })
    wrapper = mount(Login, {
      global: {
        plugins: [pinia, router],
        stubs: Stubs
      }
    })
  })

  it('should render login form with username, password and submit button', () => {
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('should render submit button with text', () => {
    expect(wrapper.find('button').text()).toContain('登录')
  })
})

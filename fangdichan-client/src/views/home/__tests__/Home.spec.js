import { describe, it, expect, vi, beforeEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'

// Mock vue-router completely to avoid router initialization issues
vi.mock('vue-router', () => {
  const mockPush = vi.fn()
  return {
    useRouter: () => ({ push: mockPush, beforeEach: vi.fn() }),
    useRoute: () => ({ params: {}, query: {} }),
    createRouter: vi.fn(() => ({ beforeEach: vi.fn(), push: mockPush })),
    createWebHistory: vi.fn(),
    RouterLink: { render: () => {} },
    RouterView: { render: () => {} }
  }
})

// Mock the property API module
vi.mock('../../../api/property', () => ({
  getRecommended: vi.fn(),
  searchProperties: vi.fn()
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn(() => Promise.resolve()) },
  default: { install: vi.fn() }
}))

import { getRecommended, searchProperties } from '../../../api/property'
import HomeVue from '../Home.vue'

describe('Home.vue - property API integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getRecommended.mockResolvedValue({ list: [], total: 0, page: 1, size: 10 })
  })

  it('should call getRecommended on mount', async () => {
    const mockData = {
      list: [
        {
          id: 1,
          title: '朝阳区精装三居室',
          price: 5000000,
          area: 120,
          roomType: '三室两厅',
          floor: 7
        }
      ],
      total: 1,
      page: 1,
      size: 10
    }
    getRecommended.mockResolvedValue(mockData)

    shallowMount(HomeVue, {
      global: {
        stubs: [
          'el-input',
          'el-button',
          'el-tag',
          'el-card',
          'el-dialog',
          'el-select',
          'el-option',
          'el-input-number',
          'router-link'
        ]
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 50))

    expect(getRecommended).toHaveBeenCalledTimes(1)
  })

  it('should call searchProperties when guide nextStep reaches step 3', async () => {
    const mockData = {
      list: [{ id: 2, title: '海淀区学区两居室', price: 3500000 }],
      total: 1,
      page: 1,
      size: 10
    }
    searchProperties.mockResolvedValue(mockData)

    const wrapper = shallowMount(HomeVue, {
      global: {
        stubs: [
          'el-input',
          'el-button',
          'el-tag',
          'el-card',
          'el-dialog',
          'el-select',
          'el-option',
          'el-input-number',
          'router-link'
        ]
      }
    })

    wrapper.vm.guideForm = { district: '朝阳区', priceMin: null, priceMax: null, roomType: '' }
    wrapper.vm.guideStep = 3
    wrapper.vm.nextGuide()

    await new Promise((resolve) => setTimeout(resolve, 50))

    expect(searchProperties).toHaveBeenCalledTimes(1)
    expect(searchProperties).toHaveBeenCalledWith({
      district: '朝阳区',
      page: 1,
      size: 10
    })
  })
})

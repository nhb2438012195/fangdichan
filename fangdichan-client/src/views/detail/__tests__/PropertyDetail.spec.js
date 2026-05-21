import { describe, it, expect, vi, beforeEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'

// Mock vue-router completely
vi.mock('vue-router', () => {
  const mockPush = vi.fn()
  return {
    useRouter: () => ({ push: mockPush, beforeEach: vi.fn() }),
    useRoute: () => ({ params: { id: '1' }, query: {} }),
    createRouter: vi.fn(() => ({ beforeEach: vi.fn(), push: mockPush })),
    createWebHistory: vi.fn(),
    RouterLink: { render: () => {} },
    RouterView: { render: () => {} }
  }
})

// Mock the property API module
vi.mock('../../../api/property', () => ({
  getPropertyDetail: vi.fn()
}))

// Mock request for other APIs (favorite, order, message)
vi.mock('../../../api/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn(() => Promise.resolve()) },
  default: { install: vi.fn() }
}))

import { getPropertyDetail } from '../../../api/property'
import PropertyDetailVue from '../PropertyDetail.vue'

describe('PropertyDetail.vue - property API integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call getPropertyDetail on mount with route param id', async () => {
    const mockProperty = {
      id: 1,
      title: '朝阳区精装三居室',
      price: 5000000,
      unitPrice: 41666,
      area: 120,
      roomType: '三室两厅',
      district: '朝阳区',
      floor: '7',
      floorTotal: 18,
      location: '朝阳路100号',
      description: '精装修，南北通透',
      companyId: 1
    }
    getPropertyDetail.mockResolvedValue(mockProperty)

    shallowMount(PropertyDetailVue, {
      global: {
        stubs: ['el-carousel', 'el-carousel-item', 'el-card', 'el-button', 'router-link']
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 50))

    expect(getPropertyDetail).toHaveBeenCalledTimes(1)
    expect(getPropertyDetail).toHaveBeenCalledWith('1')
  })

  it('should render property data from getPropertyDetail response', async () => {
    const mockProperty = {
      id: 1,
      title: '朝阳区精装三居室',
      price: 5000000,
      unitPrice: 41666,
      area: 120,
      roomType: '三室两厅',
      district: '朝阳区',
      floor: '7',
      floorTotal: 18,
      location: '朝阳路100号',
      description: '精装修，南北通透',
      companyId: 1
    }
    getPropertyDetail.mockResolvedValue(mockProperty)

    const wrapper = shallowMount(PropertyDetailVue, {
      global: {
        stubs: ['el-carousel', 'el-carousel-item', 'el-card', 'el-button', 'router-link']
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 50))

    expect(wrapper.vm.property).toEqual(mockProperty)
    expect(wrapper.vm.property.title).toBe('朝阳区精装三居室')
  })
})

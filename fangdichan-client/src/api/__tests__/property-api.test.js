import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    get: vi.fn()
  }
}))

import request from '../request'
import { searchProperties, getRecommended, getPropertyDetail } from '../property'

describe('property API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('searchProperties should call GET /customer/property/search with params and unwrap data', async () => {
    const businessData = { list: [{ id: 1, title: 'Test' }], total: 1, page: 1, size: 10 }
    request.get.mockResolvedValue({ data: businessData })

    const params = { district: '朝阳区', page: 1, size: 10 }
    const result = await searchProperties(params)

    expect(request.get).toHaveBeenCalledWith('/customer/property/search', { params })
    expect(result).toEqual(businessData)
    expect(result.list).toHaveLength(1)
    expect(result.total).toBe(1)
  })

  it('getRecommended should call GET /customer/property/recommended and unwrap data', async () => {
    const businessData = { list: [{ id: 1, title: '推荐房源' }], total: 1, page: 1, size: 10 }
    request.get.mockResolvedValue({ data: businessData })

    const result = await getRecommended()

    expect(request.get).toHaveBeenCalledWith('/customer/property/recommended')
    expect(result).toEqual(businessData)
    expect(result.list).toHaveLength(1)
  })

  it('getPropertyDetail should call GET /customer/property/:id and unwrap data', async () => {
    const businessData = { id: 1, title: '朝阳区精装三居室', price: 5000000, district: '朝阳区' }
    request.get.mockResolvedValue({ data: businessData })

    const result = await getPropertyDetail(1)

    expect(request.get).toHaveBeenCalledWith('/customer/property/1')
    expect(result).toEqual(businessData)
    expect(result.title).toBe('朝阳区精装三居室')
  })
})

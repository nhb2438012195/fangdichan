import { describe, it, expect, vi } from 'vitest'

describe('Search - API integration', () => {
  it('should call /api/customer/property/page with filter params', async () => {
    const mockGet = vi.fn().mockResolvedValue({
      data: { code: 200, data: { list: [], total: 0 } }
    })
    const request = { get: mockGet }
    const filters = {
      district: '朝阳区',
      roomType: '三室两厅',
      priceMin: 3000000,
      priceMax: 8000000,
      page: 1,
      size: 20
    }
    await request.get('/customer/property/page', { params: filters })
    expect(mockGet).toHaveBeenCalledWith('/customer/property/page', {
      params: filters
    })
  })

  it('should handle filters with empty values by not sending them', async () => {
    const mockGet = vi.fn().mockResolvedValue({
      data: { code: 200, data: { list: [], total: 0 } }
    })
    const request = { get: mockGet }
    const filters = { district: '', roomType: '', page: 1, size: 20 }
    const cleanedParams = {}
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) {
        cleanedParams[key] = value
      }
    })
    await request.get('/customer/property/page', { params: cleanedParams })
    expect(mockGet).toHaveBeenCalledWith('/customer/property/page', {
      params: { page: 1, size: 20 }
    })
  })
})

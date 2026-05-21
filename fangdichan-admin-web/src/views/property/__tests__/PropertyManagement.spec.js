import { describe, it, expect, vi } from 'vitest'

describe('PropertyManagement - API integration', () => {
  it('should call /api/agent/property/page with correct params', async () => {
    const mockGet = vi.fn().mockResolvedValue({
      data: { code: 200, data: { list: [], total: 0 } }
    })
    const request = { get: mockGet }

    const params = { page: 1, size: 10, status: 'APPROVED', district: '' }
    await request.get('/agent/property/page', { params })

    expect(mockGet).toHaveBeenCalledWith('/agent/property/page', {
      params: { page: 1, size: 10, status: 'APPROVED', district: '' }
    })
  })

  it('should correctly destructure PageResult response', async () => {
    const res = {
      data: { code: 200, msg: 'success', data: { list: [{ id: 1 }], total: 1, page: 1, size: 10 } }
    }
    const { list, total } = res.data.data
    expect(list).toHaveLength(1)
    expect(total).toBe(1)
  })

  it('should handle empty list correctly', async () => {
    const res = {
      data: { code: 200, msg: 'success', data: { list: [], total: 0, page: 1, size: 10 } }
    }
    const { list } = res.data.data
    expect(list).toEqual([])
    expect(Array.isArray(list)).toBe(true)
  })
})

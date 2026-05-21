import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

import request from '../request'
import { getFavoriteList, toggleFavorite, checkFavorite } from '../favorite'

describe('favorite API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getFavoriteList should call GET /customer/favorite/list with params and unwrap data', async () => {
    const businessData = {
      list: [{ id: 1, title: 'Test', price: 100 }],
      total: 1,
      page: 1,
      size: 12
    }
    request.get.mockResolvedValue({ data: businessData })

    const params = { page: 1, size: 12 }
    const result = await getFavoriteList(params)

    expect(request.get).toHaveBeenCalledWith('/customer/favorite/list', { params })
    expect(result).toEqual(businessData)
    expect(result.list).toHaveLength(1)
    expect(result.total).toBe(1)
  })

  it('toggleFavorite should call POST /customer/favorite/:id and unwrap boolean data', async () => {
    request.post.mockResolvedValue({ data: true })

    const result = await toggleFavorite(1)

    expect(request.post).toHaveBeenCalledWith('/customer/favorite/1')
    expect(result).toBe(true)
  })

  it('checkFavorite should call GET /customer/favorite/check/:id and unwrap boolean data', async () => {
    request.get.mockResolvedValue({ data: true })

    const result = await checkFavorite(1)

    expect(request.get).toHaveBeenCalledWith('/customer/favorite/check/1')
    expect(result).toBe(true)
  })
})

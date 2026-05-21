import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn()
  }
}))

import request from '../request'
import { getOrderList, createOrder, cancelOrder } from '../order'

describe('order API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getOrderList should call GET /customer/order/list with params and unwrap data', async () => {
    const businessData = {
      list: [{ id: 1, orderNo: 'ORD001', status: 'PENDING' }],
      total: 1,
      page: 1,
      size: 100
    }
    request.get.mockResolvedValue({ data: businessData })

    const params = { page: 1, size: 100 }
    const result = await getOrderList(params)

    expect(request.get).toHaveBeenCalledWith('/customer/order/list', { params })
    expect(result).toEqual(businessData)
    expect(result.list).toHaveLength(1)
  })

  it('createOrder should call POST /customer/order with form-urlencoded body', async () => {
    const businessData = { id: 1, orderNo: 'ORD002' }
    request.post.mockResolvedValue({ data: businessData })

    const result = await createOrder(123, '我想购买')

    expect(request.post).toHaveBeenCalledWith('/customer/order', expect.any(URLSearchParams), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    expect(result).toEqual(businessData)
  })

  it('cancelOrder should call PUT /customer/order/:id/cancel and unwrap data', async () => {
    const businessData = { id: 1, status: 'CANCELLED' }
    request.put.mockResolvedValue({ data: businessData })

    const result = await cancelOrder(1)

    expect(request.put).toHaveBeenCalledWith('/customer/order/1/cancel')
    expect(result).toEqual(businessData)
  })
})

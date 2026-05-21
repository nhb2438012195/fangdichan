import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import { getOrderList, confirmOrder, cancelOrder } from '../order'

describe('agent order API', () => {
  afterEach(() => server.resetHandlers())

  it('getOrderList should fetch order list with params', async () => {
    server.use(
      http.get('/api/agent/order/list', () => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: {
            list: [
              { id: 1, propertyTitle: '房源A', status: 'PENDING' },
              { id: 2, propertyTitle: '房源B', status: 'CONFIRMED' }
            ],
            total: 2
          }
        })
      })
    )

    const result = await getOrderList({ status: 'PENDING' })

    expect(result.list).toHaveLength(2)
    expect(result.total).toBe(2)
  })

  it('confirmOrder should confirm an order', async () => {
    server.use(
      http.put('/api/agent/order/:id/confirm', ({ params }) => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), status: 'CONFIRMED' }
        })
      })
    )

    const result = await confirmOrder(1)

    expect(result.id).toBe(1)
    expect(result.status).toBe('CONFIRMED')
  })

  it('cancelOrder should cancel an order', async () => {
    server.use(
      http.put('/api/agent/order/:id/cancel', ({ params }) => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), status: 'CANCELLED' }
        })
      })
    )

    const result = await cancelOrder(2)

    expect(result.id).toBe(2)
    expect(result.status).toBe('CANCELLED')
  })
})

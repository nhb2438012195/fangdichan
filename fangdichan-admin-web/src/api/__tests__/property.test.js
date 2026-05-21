import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import { getPendingList, approveProperty, rejectProperty } from '../property'

describe('admin property (audit) API', () => {
  afterEach(() => server.resetHandlers())

  it('getPendingList should fetch pending property list', async () => {
    server.use(
      http.get('/api/admin/property/pending', () => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: [
            { id: 1, title: '房源1', status: 'PENDING' },
            { id: 2, title: '房源2', status: 'PENDING' }
          ]
        })
      })
    )

    const result = await getPendingList()

    expect(Array.isArray(result)).toBe(true)
    expect(result).toHaveLength(2)
    expect(result[0].status).toBe('PENDING')
    expect(result[1].title).toBe('房源2')
  })

  it('approveProperty should approve a property', async () => {
    server.use(
      http.put('/api/admin/property/:id/approve', ({ params }) => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), status: 'APPROVED' }
        })
      })
    )

    const result = await approveProperty(1)

    expect(result.id).toBe(1)
    expect(result.status).toBe('APPROVED')
  })

  it('rejectProperty should reject a property', async () => {
    server.use(
      http.put('/api/admin/property/:id/reject', ({ params }) => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), status: 'REJECTED' }
        })
      })
    )

    const result = await rejectProperty(5)

    expect(result.id).toBe(5)
    expect(result.status).toBe('REJECTED')
  })
})

import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import {
  getMyPropertyList,
  createProperty,
  updateProperty,
  takeOffProperty
} from '../agent-property'

describe('agent property API', () => {
  afterEach(() => server.resetHandlers())

  it('getMyPropertyList should fetch property list with params', async () => {
    server.use(
      http.get('/api/agent/property/list', ({ request }) => {
        const url = new URL(request.url)
        const page = parseInt(url.searchParams.get('page') || '1')
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: {
            list: [
              { id: 1, title: '我的房源1', status: 'APPROVED' },
              { id: 2, title: '我的房源2', status: 'PENDING' }
            ],
            total: 2,
            page,
            size: 10
          }
        })
      })
    )

    const result = await getMyPropertyList({ page: 1, size: 10 })

    expect(result.list).toHaveLength(2)
    expect(result.total).toBe(2)
    expect(result.list[0].title).toBe('我的房源1')
  })

  it('createProperty should create a new property', async () => {
    server.use(
      http.post('/api/agent/property', async ({ request }) => {
        const body = await request.json()
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: 10, ...body, status: 'PENDING' }
        })
      })
    )

    const data = { title: '新房源', price: 3000000, area: 100 }
    const result = await createProperty(data)

    expect(result.id).toBe(10)
    expect(result.title).toBe('新房源')
    expect(result.status).toBe('PENDING')
  })

  it('updateProperty should update an existing property', async () => {
    server.use(
      http.put('/api/agent/property/:id', async ({ params, request }) => {
        const body = await request.json()
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), ...body }
        })
      })
    )

    const data = { title: '更新房源', price: 3500000 }
    const result = await updateProperty(5, data)

    expect(result.id).toBe(5)
    expect(result.title).toBe('更新房源')
    expect(result.price).toBe(3500000)
  })

  it('takeOffProperty should take a property off market', async () => {
    server.use(
      http.put('/api/agent/property/:id/off-market', ({ params }) => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), status: 'OFF_MARKET' }
        })
      })
    )

    const result = await takeOffProperty(3)

    expect(result.id).toBe(3)
    expect(result.status).toBe('OFF_MARKET')
  })
})

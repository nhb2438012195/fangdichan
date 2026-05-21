import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
  headers: { Authorization: 'Bearer mock-token' }
})

request.interceptors.request.use((config) => {
  config.headers['Content-Type'] = 'application/json'
  return config
})

describe('API Contract: Customer API endpoints', () => {
  afterEach(() => server.resetHandlers())

  it('GET /api/customer/property/page should return PageResult with property list', async () => {
    const res = await request.get('/customer/property/page', {
      params: { page: 1, size: 10 }
    })
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('list')
    expect(res.data.data).toHaveProperty('total')
    expect(res.data.data).toHaveProperty('page')
    expect(res.data.data).toHaveProperty('size')
    expect(Array.isArray(res.data.data.list)).toBe(true)
    if (res.data.data.list.length > 0) {
      const item = res.data.data.list[0]
      expect(item).toHaveProperty('id')
      expect(item).toHaveProperty('title')
      expect(item).toHaveProperty('price')
      expect(item).toHaveProperty('district')
    }
  })

  it('GET /api/customer/property/:id should return property detail', async () => {
    const res = await request.get('/customer/property/1')
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('title')
    expect(res.data.data).toHaveProperty('district')
    expect(res.data.data).toHaveProperty('roomType')
    expect(res.data.data).toHaveProperty('area')
    expect(res.data.data).toHaveProperty('price')
    expect(res.data.data).toHaveProperty('status')
  })

  it('GET /api/customer/favorite/list should return favorite items array', async () => {
    const res = await request.get('/customer/favorite/list')
    expect(res.data.code).toBe(200)
    expect(Array.isArray(res.data.data)).toBe(true)
    if (res.data.data.length > 0) {
      const item = res.data.data[0]
      expect(item).toHaveProperty('propertyId')
      expect(item).toHaveProperty('title')
      expect(item).toHaveProperty('price')
    }
  })

  it('GET /api/customer/property/recommended should return PageResult', async () => {
    const res = await request.get('/customer/property/recommended')
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('list')
    expect(res.data.data).toHaveProperty('total')
  })
})

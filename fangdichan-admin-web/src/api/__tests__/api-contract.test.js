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

describe('API Contract: PageResult data format', () => {
  afterEach(() => server.resetHandlers())

  it('GET /api/admin/property/page should return { list, total, page, size }', async () => {
    const res = await request.get('/admin/property/page', {
      params: { page: 1, size: 10, status: 'PENDING' }
    })
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('list')
    expect(res.data.data).toHaveProperty('total')
    expect(res.data.data).toHaveProperty('page')
    expect(res.data.data).toHaveProperty('size')
    expect(Array.isArray(res.data.data.list)).toBe(true)
  })

  it('GET /api/admin/user/page should return standard PageResult', async () => {
    const res = await request.get('/admin/user/page', {
      params: { page: 1, size: 10 }
    })
    expect(res.data.code).toBe(200)
    expect(res.data.data.list).toBeDefined()
    expect(res.data.data.total).toBeDefined()
  })

  it('GET /api/agent/company/info should return company data', async () => {
    const res = await request.get('/agent/company/info')
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('companyName')
    expect(res.data.data).toHaveProperty('address')
    expect(res.data.data).toHaveProperty('contactPhone')
  })
})

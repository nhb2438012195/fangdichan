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

  it('GET /api/admin/property/pending should return { list, total, page, size }', async () => {
    const res = await request.get('/admin/property/pending', {
      params: { page: 1, size: 10 }
    })
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('list')
    expect(res.data.data).toHaveProperty('total')
    expect(res.data.data).toHaveProperty('page')
    expect(res.data.data).toHaveProperty('size')
    expect(Array.isArray(res.data.data.list)).toBe(true)
  })

  it('GET /api/admin/users should return users array', async () => {
    const res = await request.get('/admin/users')
    expect(res.data.code).toBe(200)
    expect(Array.isArray(res.data.data)).toBe(true)
  })

  it('GET /api/agent/company should return company data', async () => {
    const res = await request.get('/agent/company')
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('companyName')
    expect(res.data.data).toHaveProperty('address')
    expect(res.data.data).toHaveProperty('contactPhone')
  })
})

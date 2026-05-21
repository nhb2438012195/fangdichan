import { http, HttpResponse } from 'msw'

const BASE = '/api'

export const handlers = [
  // Login
  http.post(`${BASE}/public/login`, async ({ request }) => {
    const body = await request.json()
    if (body.username === 'admin' && body.password === 'admin123') {
      return HttpResponse.json({
        code: 200,
        msg: 'success',
        data: { token: 'mock-jwt-token', role: 'ADMIN', username: 'admin' }
      })
    }
    return HttpResponse.json({ code: 401, msg: '用户名或密码错误', data: null }, { status: 401 })
  }),

  // Admin: property page
  http.get(`${BASE}/admin/property/page`, ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const size = parseInt(url.searchParams.get('size') || '10')
    const list = Array.from({ length: size }, (_, i) => ({
      id: (page - 1) * size + i + 1,
      title: `测试房源${(page - 1) * size + i + 1}`,
      district: '朝阳区',
      roomType: '三室两厅',
      area: 120.5,
      price: 5000000.0,
      status: 'PENDING',
      companyName: '测试房产公司'
    }))
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: { list, total: 50, page, size }
    })
  }),

  // Admin: user page
  http.get(`${BASE}/admin/user/page`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        list: [
          { id: 1, username: 'admin', role: 'ADMIN', status: 1, createdAt: '2026-01-01T00:00:00' },
          { id: 2, username: 'agent1', role: 'AGENT', status: 1, createdAt: '2026-01-02T00:00:00' },
          {
            id: 3,
            username: 'customer1',
            role: 'CUSTOMER',
            status: 1,
            createdAt: '2026-01-03T00:00:00'
          }
        ],
        total: 3,
        page: 1,
        size: 10
      }
    })
  }),

  // Agent: company info
  http.get(`${BASE}/agent/company/info`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        id: 1,
        companyName: '测试房产公司',
        address: '北京市朝阳区测试路100号',
        contactPhone: '010-88888888',
        description: '一家专业的房地产公司'
      }
    })
  })
]

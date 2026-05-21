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

  // Admin: property pending list
  http.get(`${BASE}/admin/property/pending`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        list: [
          { id: 1, title: '待审核房源1', district: '朝阳区', price: 5000000, status: 'PENDING' },
          { id: 2, title: '待审核房源2', district: '海淀区', price: 3500000, status: 'PENDING' }
        ],
        total: 2,
        page: 1,
        size: 10
      }
    })
  }),

  // Admin: approve property
  http.put(`${BASE}/admin/property/:id/approve`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Admin: reject property
  http.put(`${BASE}/admin/property/:id/reject`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Admin: user list
  http.get(`${BASE}/admin/users`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: [
        { id: 1, username: 'admin', role: 'ADMIN', status: 1, createdAt: '2026-01-01T00:00:00' },
        { id: 2, username: 'agent1', role: 'AGENT', status: 1, createdAt: '2026-01-02T00:00:00' },
        {
          id: 3,
          username: 'customer1',
          role: 'CUSTOMER',
          status: 1,
          createdAt: '2026-01-03T00:00:00'
        }
      ]
    })
  }),

  // Admin: user status toggle
  http.put(`${BASE}/admin/users/:id/status`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Admin: config list
  http.get(`${BASE}/admin/config/list`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        list: [
          { configKey: 'site_title', configValue: '购房通', description: '网站标题' },
          { configKey: 'contact_email', configValue: 'admin@example.com', description: '联系邮箱' }
        ],
        total: 2,
        page: 1,
        size: 10
      }
    })
  }),

  // Admin: config update
  http.put(`${BASE}/admin/config/:key`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Admin: report list
  http.get(`${BASE}/admin/report/list`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        list: [
          { id: 1, reason: '虚假信息', status: 'PENDING' },
          { id: 2, reason: '价格不符', status: 'PENDING' }
        ],
        total: 2,
        page: 1,
        size: 10
      }
    })
  }),

  // Admin: report handle
  http.put(`${BASE}/admin/report/:id/status`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Agent: property list
  http.get(`${BASE}/agent/property/list`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        list: [
          {
            id: 1,
            title: '我的房源1',
            district: '朝阳区',
            price: 5000000,
            status: 'APPROVED',
            area: 120,
            roomType: '三室两厅',
            floor: '10',
            floorTotal: 18,
            location: '测试路100号',
            description: '精装修'
          },
          {
            id: 2,
            title: '我的房源2',
            district: '海淀区',
            price: 3500000,
            status: 'PENDING',
            area: 85,
            roomType: '两室一厅',
            floor: '5',
            floorTotal: 12,
            location: '测试路200号',
            description: '学区房'
          }
        ],
        total: 2,
        page: 1,
        size: 10
      }
    })
  }),

  // Agent: create property
  http.post(`${BASE}/agent/property`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: { id: 3 } })
  }),

  // Agent: update property
  http.put(`${BASE}/agent/property/:id`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Agent: off-market
  http.put(`${BASE}/agent/property/:id/off-market`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Agent: order list
  http.get(`${BASE}/agent/order/list`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: { list: [], total: 0, page: 1, size: 10 }
    })
  }),

  // Agent: confirm order
  http.put(`${BASE}/agent/order/:id/confirm`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Agent: cancel order
  http.put(`${BASE}/agent/order/:id/cancel`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Agent: company info
  http.get(`${BASE}/agent/company`, () => {
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
  }),

  // Agent: update company
  http.put(`${BASE}/agent/company`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: null })
  }),

  // Agent: vacancy analysis
  http.get(`${BASE}/agent/analysis/vacancy`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        district: [
          { name: '朝阳区', rate: 0.3 },
          { name: '海淀区', rate: 0.2 }
        ],
        floor: [
          { name: '低层', rate: 0.25 },
          { name: '中层', rate: 0.15 }
        ],
        roomType: [
          { name: '两室', rate: 0.2 },
          { name: '三室', rate: 0.1 }
        ]
      }
    })
  }),

  // Agent: conversation list
  http.get(`${BASE}/agent/conversation/list`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: { list: [], total: 0, page: 1, size: 10 }
    })
  }),

  // Agent: conversation messages
  http.get(`${BASE}/agent/conversation/:id/messages`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: [] })
  }),

  // Agent: send message
  http.post(`${BASE}/agent/conversation/:id/message`, () => {
    return HttpResponse.json({ code: 200, msg: 'success', data: { id: 1 } })
  })
]

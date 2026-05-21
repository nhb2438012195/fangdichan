import { http, HttpResponse } from 'msw'

const BASE = '/api'

export const handlers = [
  // Customer: property page (search)
  http.get(`${BASE}/customer/property/page`, ({ request }) => {
    const url = new URL(request.url)
    const keyword = url.searchParams.get('keyword') || ''
    const list = [
      {
        id: 1,
        title: '朝阳区精装三居室',
        district: '朝阳区',
        roomType: '三室两厅',
        area: 120,
        price: 5000000,
        unitPrice: 41666,
        isVacant: true
      },
      {
        id: 2,
        title: '海淀区学区两居室',
        district: '海淀区',
        roomType: '两室一厅',
        area: 85,
        price: 3500000,
        unitPrice: 41176,
        isVacant: true
      }
    ].filter((p) => !keyword || p.title.includes(keyword))
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: { list, total: list.length, page: 1, size: 10 }
    })
  }),

  // Customer: property search (with filters)
  http.get(`${BASE}/customer/property/search`, ({ request }) => {
    const url = new URL(request.url)
    const district = url.searchParams.get('district') || ''
    const list = [
      {
        id: 1,
        title: '朝阳区精装三居室',
        district: '朝阳区',
        roomType: '三室两厅',
        area: 120,
        price: 5000000,
        unitPrice: 41666,
        isVacant: true
      },
      {
        id: 2,
        title: '海淀区学区两居室',
        district: '海淀区',
        roomType: '两室一厅',
        area: 85,
        price: 3500000,
        unitPrice: 41176,
        isVacant: true
      }
    ].filter((p) => !district || p.district === district)
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: { list, total: list.length, page: 1, size: 10 }
    })
  }),

  // Customer: recommended properties (BEFORE :id to avoid route conflict)
  http.get(`${BASE}/customer/property/recommended`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: { list: [], total: 0, page: 1, size: 10 }
    })
  }),

  // Customer: property detail
  http.get(`${BASE}/customer/property/:id`, ({ params }) => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        id: parseInt(params.id),
        title: '朝阳区精装三居室',
        district: '朝阳区',
        roomType: '三室两厅',
        area: 120,
        price: 5000000,
        unitPrice: 41666,
        floor: '7-10F',
        floorTotal: 18,
        isVacant: true,
        status: 'APPROVED',
        description: '精装修，南北通透',
        images: [{ id: 1, imageUrl: 'http://example.com/img1.jpg', sortOrder: 0 }],
        companyName: '测试房产公司'
      }
    })
  }),

  // Customer: favorite list
  http.get(`${BASE}/customer/favorite/list`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: [
        {
          id: 1,
          propertyId: 1,
          title: '朝阳区精装三居室',
          price: 5000000,
          district: '朝阳区',
          createdAt: '2026-05-01T10:00:00'
        }
      ]
    })
  })
]

import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import { getCompanyInfo, updateCompanyInfo } from '../company'

describe('agent company API', () => {
  afterEach(() => server.resetHandlers())

  it('getCompanyInfo should fetch company information', async () => {
    server.use(
      http.get('/api/agent/company', () => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: {
            id: 1,
            companyName: '测试房产公司',
            address: '北京市朝阳区测试路100号',
            contactPhone: '010-88888888'
          }
        })
      })
    )

    const result = await getCompanyInfo()

    expect(result.companyName).toBe('测试房产公司')
    expect(result.address).toContain('朝阳区')
    expect(result.contactPhone).toBe('010-88888888')
  })

  it('updateCompanyInfo should update company information', async () => {
    server.use(
      http.put('/api/agent/company', async ({ request }) => {
        const body = await request.json()
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: 1, ...body }
        })
      })
    )

    const data = { companyName: '新公司名', contactPhone: '010-99999999' }
    const result = await updateCompanyInfo(data)

    expect(result.companyName).toBe('新公司名')
    expect(result.contactPhone).toBe('010-99999999')
    expect(result.id).toBe(1)
  })
})

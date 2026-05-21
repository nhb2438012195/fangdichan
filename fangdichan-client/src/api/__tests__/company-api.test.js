import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    get: vi.fn()
  }
}))

import request from '../request'
import { getCompanyList, getCompanyDetail } from '../company'

describe('company API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getCompanyList should call GET /customer/company/list and unwrap data', async () => {
    const businessData = [
      { id: 1, companyName: '测试房产公司', address: '北京市朝阳区', contactPhone: '13800000000' }
    ]
    request.get.mockResolvedValue({ data: businessData })

    const result = await getCompanyList()

    expect(request.get).toHaveBeenCalledWith('/customer/company/list')
    expect(result).toEqual(businessData)
    expect(result).toHaveLength(1)
    expect(result[0].companyName).toBe('测试房产公司')
  })

  it('getCompanyDetail should call GET /customer/company/:id and unwrap data', async () => {
    const businessData = { id: 1, companyName: '测试房产公司', description: '专业房产中介' }
    request.get.mockResolvedValue({ data: businessData })

    const result = await getCompanyDetail(1)

    expect(request.get).toHaveBeenCalledWith('/customer/company/1')
    expect(result).toEqual(businessData)
    expect(result.description).toBe('专业房产中介')
  })
})

import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    post: vi.fn()
  }
}))

import request from '../request'
import { submitReport } from '../report'

describe('report API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('submitReport should POST /customer/report with URLSearchParams and unwrap data', async () => {
    const businessData = { id: 1, msg: '举报成功' }
    request.post.mockResolvedValue({ data: businessData })

    const result = await submitReport(1, '虚假房源信息')

    expect(request.post).toHaveBeenCalledTimes(1)
    const [url, params, config] = request.post.mock.calls[0]
    expect(url).toBe('/customer/report')
    expect(params).toBeInstanceOf(URLSearchParams)
    expect(params.get('propertyId')).toBe('1')
    expect(params.get('reason')).toBe('虚假房源信息')
    expect(config).toEqual({ headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
    expect(result).toEqual(businessData)
  })
})

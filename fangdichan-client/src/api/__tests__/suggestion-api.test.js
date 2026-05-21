import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    post: vi.fn()
  }
}))

import request from '../request'
import { submitSuggestion } from '../suggestion'

describe('suggestion API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('submitSuggestion should POST /customer/suggestion with URLSearchParams and unwrap data', async () => {
    const businessData = { id: 1, msg: '提交成功' }
    request.post.mockResolvedValue({ data: businessData })

    const formData = {
      companyId: '1',
      desiredType: '两室',
      desiredPriceMin: 1000000,
      desiredPriceMax: 2000000,
      content: '希望有更多两室房源'
    }
    const result = await submitSuggestion(formData)

    expect(request.post).toHaveBeenCalledTimes(1)
    const [url, params, config] = request.post.mock.calls[0]
    expect(url).toBe('/customer/suggestion')
    expect(params).toBeInstanceOf(URLSearchParams)
    expect(params.get('companyId')).toBe('1')
    expect(params.get('desiredType')).toBe('两室')
    expect(params.get('desiredPriceMin')).toBe('1000000')
    expect(params.get('content')).toBe('希望有更多两室房源')
    expect(config).toEqual({ headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
    expect(result).toEqual(businessData)
  })

  it('submitSuggestion should skip null or empty values', async () => {
    const businessData = { id: 2, msg: '提交成功' }
    request.post.mockResolvedValue({ data: businessData })

    const formData = {
      companyId: '2',
      desiredType: '一室',
      desiredPriceMin: null,
      desiredPriceMax: '',
      content: 'test'
    }
    await submitSuggestion(formData)

    const params = request.post.mock.calls[0][1]
    expect(params.get('companyId')).toBe('2')
    expect(params.get('desiredType')).toBe('一室')
    expect(params.get('content')).toBe('test')
    expect(params.get('desiredPriceMin')).toBeNull()
    expect(params.get('desiredPriceMax')).toBeNull()
  })
})

import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    post: vi.fn()
  }
}))

import request from '../request'
import { loginApi, registerApi } from '../auth'

describe('auth API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loginApi should unwrap res.data to return business data', async () => {
    const businessData = { token: 'admin-token', role: 'ADMIN', userId: '1' }
    request.post.mockResolvedValue({ data: businessData })

    const result = await loginApi('admin', 'admin123')

    expect(request.post).toHaveBeenCalledWith('/public/login', {
      username: 'admin',
      password: 'admin123'
    })
    expect(result).toEqual(businessData)
    expect(result.token).toBe('admin-token')
  })

  it('registerApi should unwrap res.data to return business data', async () => {
    const businessData = { token: 'new-token', role: 'AGENT', userId: '3' }
    request.post.mockResolvedValue({ data: businessData })

    const result = await registerApi('newadmin', 'pass789', 'AGENT')

    expect(request.post).toHaveBeenCalledWith(
      '/public/register',
      { username: 'newadmin', password: 'pass789' },
      { params: { role: 'AGENT' } }
    )
    expect(result).toEqual(businessData)
    expect(result.token).toBe('new-token')
  })
})

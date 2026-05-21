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
    const businessData = { token: 'test-token', role: 'USER', userId: '1' }
    request.post.mockResolvedValue({ data: businessData })

    const result = await loginApi('testuser', 'password123')

    expect(request.post).toHaveBeenCalledWith('/public/login', {
      username: 'testuser',
      password: 'password123'
    })
    expect(result).toEqual(businessData)
    expect(result.token).toBe('test-token')
  })

  it('registerApi should unwrap res.data to return business data', async () => {
    const businessData = { token: 'test-token', role: 'AGENT', userId: '2' }
    request.post.mockResolvedValue({ data: businessData })

    const result = await registerApi('newuser', 'pass456', 'AGENT')

    expect(request.post).toHaveBeenCalledWith(
      '/public/register',
      { username: 'newuser', password: 'pass456' },
      { params: { role: 'AGENT' } }
    )
    expect(result).toEqual(businessData)
    expect(result.token).toBe('test-token')
  })
})

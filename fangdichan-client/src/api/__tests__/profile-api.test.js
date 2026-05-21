import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn()
  }
}))

import request from '../request'
import { getProfile, updateProfile, changePassword } from '../profile'

describe('profile API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getProfile should call GET /customer/profile and unwrap data', async () => {
    const businessData = { realName: '张三', phone: '13800138000', email: 'zhangsan@test.com' }
    request.get.mockResolvedValue({ data: businessData })

    const result = await getProfile()

    expect(request.get).toHaveBeenCalledWith('/customer/profile')
    expect(result).toEqual(businessData)
    expect(result.realName).toBe('张三')
  })

  it('updateProfile should call PUT /customer/profile with data and unwrap data', async () => {
    const profileData = { realName: '李四', phone: '13900139000', email: 'lisi@test.com' }
    const businessData = { realName: '李四', phone: '13900139000', email: 'lisi@test.com' }
    request.put.mockResolvedValue({ data: businessData })

    const result = await updateProfile(profileData)

    expect(request.put).toHaveBeenCalledWith('/customer/profile', profileData)
    expect(result).toEqual(businessData)
    expect(result.realName).toBe('李四')
  })

  it('updateProfile with buyIntent should pass serialized intent in the body', async () => {
    const profileWithIntent = {
      realName: '王五',
      buyIntent: JSON.stringify({
        district: '朝阳区',
        priceMin: 300,
        priceMax: 500,
        roomType: '三室'
      })
    }
    request.put.mockResolvedValue({ data: profileWithIntent })

    const result = await updateProfile(profileWithIntent)

    expect(request.put).toHaveBeenCalledWith('/customer/profile', profileWithIntent)
    expect(result.buyIntent).toContain('district')
    expect(result.buyIntent).toContain('朝阳区')
  })

  it('changePassword should call PUT /customer/profile/password with old and new password', async () => {
    const businessData = { success: true }
    request.put.mockResolvedValue({ data: businessData })

    const result = await changePassword('old123', 'new456')

    expect(request.put).toHaveBeenCalledWith('/customer/profile/password', {
      oldPassword: 'old123',
      newPassword: 'new456'
    })
    expect(result).toEqual(businessData)
    expect(result.success).toBe(true)
  })
})

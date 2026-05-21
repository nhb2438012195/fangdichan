import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

import request from '../request'
import { getConversationList, getMessages, sendMessage, createConversation } from '../message'

describe('message API functions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getConversationList should call GET /customer/conversation/list and unwrap data', async () => {
    const businessData = {
      list: [
        { id: 1, propertyId: 101, companyId: 201 },
        { id: 2, propertyId: 102, companyId: 202 }
      ]
    }
    request.get.mockResolvedValue({ data: businessData })

    const result = await getConversationList()

    expect(request.get).toHaveBeenCalledWith('/customer/conversation/list')
    expect(result).toEqual(businessData)
    expect(result.list).toHaveLength(2)
  })

  it('getMessages should call GET /customer/conversation/:id/messages and unwrap data', async () => {
    const businessData = [
      { id: 1, content: '你好', senderRole: 'CUSTOMER' },
      { id: 2, content: '您好', senderRole: 'AGENT' }
    ]
    request.get.mockResolvedValue({ data: businessData })

    const result = await getMessages(1)

    expect(request.get).toHaveBeenCalledWith('/customer/conversation/1/messages')
    expect(result).toEqual(businessData)
    expect(result).toHaveLength(2)
  })

  it('sendMessage should call POST /customer/conversation/:id/message with content and unwrap data', async () => {
    const businessData = { id: 3, content: '测试消息', senderRole: 'CUSTOMER' }
    request.post.mockResolvedValue({ data: businessData })

    const result = await sendMessage(1, '测试消息')

    expect(request.post).toHaveBeenCalledWith('/customer/conversation/1/message', {
      content: '测试消息'
    })
    expect(result).toEqual(businessData)
  })

  it('createConversation should call POST /customer/conversation with params and unwrap data', async () => {
    const businessData = { id: 10 }
    request.post.mockResolvedValue({ data: businessData })

    const result = await createConversation(201, 101)

    expect(request.post).toHaveBeenCalledWith('/customer/conversation', null, {
      params: { companyId: 201, propertyId: 101 }
    })
    expect(result).toEqual(businessData)
    expect(result.id).toBe(10)
  })
})

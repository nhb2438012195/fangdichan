import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import { getConversationList, getMessages, sendMessage } from '../message'

describe('agent message API', () => {
  afterEach(() => server.resetHandlers())

  it('getConversationList should fetch conversation list', async () => {
    server.use(
      http.get('/api/agent/conversation/list', () => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: [
            { id: 1, customerName: '客户A', lastMessage: '你好', unread: 2 },
            { id: 2, customerName: '客户B', lastMessage: '在吗？', unread: 0 }
          ]
        })
      })
    )

    const result = await getConversationList()

    expect(Array.isArray(result)).toBe(true)
    expect(result).toHaveLength(2)
    expect(result[0].customerName).toBe('客户A')
    expect(result[0].unread).toBe(2)
  })

  it('getMessages should fetch messages for a conversation', async () => {
    const conversationId = 1
    server.use(
      http.get('/api/agent/conversation/:conversationId/messages', ({ params }) => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: [
            {
              id: 1,
              conversationId: parseInt(params.conversationId),
              content: '你好',
              senderRole: 'CUSTOMER'
            },
            {
              id: 2,
              conversationId: parseInt(params.conversationId),
              content: '你好，有什么可以帮助您？',
              senderRole: 'AGENT'
            }
          ]
        })
      })
    )

    const result = await getMessages(conversationId)

    expect(Array.isArray(result)).toBe(true)
    expect(result).toHaveLength(2)
    expect(result[0].content).toBe('你好')
    expect(result[0].senderRole).toBe('CUSTOMER')
  })

  it('sendMessage should send a message in a conversation', async () => {
    server.use(
      http.post('/api/agent/conversation/:conversationId/message', async ({ params, request }) => {
        const body = await request.json()
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: {
            id: 100,
            conversationId: parseInt(params.conversationId),
            content: body.content,
            senderRole: 'AGENT'
          }
        })
      })
    )

    const result = await sendMessage(1, '好的，已为您安排')

    expect(result.id).toBe(100)
    expect(result.conversationId).toBe(1)
    expect(result.content).toBe('好的，已为您安排')
    expect(result.senderRole).toBe('AGENT')
  })
})

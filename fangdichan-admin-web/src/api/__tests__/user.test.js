import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import { getUserList, toggleUserStatus } from '../user'

describe('user API', () => {
  afterEach(() => server.resetHandlers())

  it('getUserList should fetch user list', async () => {
    server.use(
      http.get('/api/admin/users', () => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: [
            { id: 1, username: 'admin', role: 'ADMIN', status: 1 },
            { id: 2, username: 'agent1', role: 'AGENT', status: 1 }
          ]
        })
      })
    )

    const result = await getUserList()

    expect(Array.isArray(result)).toBe(true)
    expect(result).toHaveLength(2)
    expect(result[0].username).toBe('admin')
    expect(result[0].role).toBe('ADMIN')
  })

  it('toggleUserStatus should toggle user status to disabled when currently enabled', async () => {
    server.use(
      http.put('/api/admin/users/:id/status', async ({ params, request }) => {
        const body = await request.json()
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), status: body.status }
        })
      })
    )

    const result = await toggleUserStatus(1, true)

    expect(result.id).toBe(1)
    expect(result.status).toBe(0)
  })

  it('toggleUserStatus should toggle user status to enabled when currently disabled', async () => {
    server.use(
      http.put('/api/admin/users/:id/status', async ({ params, request }) => {
        const body = await request.json()
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), status: body.status }
        })
      })
    )

    const result = await toggleUserStatus(2, false)

    expect(result.id).toBe(2)
    expect(result.status).toBe(1)
  })
})

import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import { getReportList, handleReport } from '../report'

describe('admin report API', () => {
  afterEach(() => server.resetHandlers())

  it('getReportList should fetch report list', async () => {
    server.use(
      http.get('/api/admin/report/list', () => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: [
            { id: 1, title: '举报1', status: 'PENDING' },
            { id: 2, title: '举报2', status: 'RESOLVED' }
          ]
        })
      })
    )

    const result = await getReportList()

    expect(Array.isArray(result)).toBe(true)
    expect(result).toHaveLength(2)
    expect(result[0].title).toBe('举报1')
    expect(result[1].status).toBe('RESOLVED')
  })

  it('handleReport should update report status', async () => {
    server.use(
      http.put('/api/admin/report/:id/status', ({ params, request }) => {
        const url = new URL(request.url)
        const status = url.searchParams.get('status')
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { id: parseInt(params.id), status }
        })
      })
    )

    const result = await handleReport(1, 'RESOLVED')

    expect(result.id).toBe(1)
    expect(result.status).toBe('RESOLVED')
  })
})

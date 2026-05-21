import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import { getConfigList, updateConfig } from '../config'

describe('admin config API', () => {
  afterEach(() => server.resetHandlers())

  it('getConfigList should fetch config list', async () => {
    server.use(
      http.get('/api/admin/config/list', () => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: [
            { key: 'site_name', value: '房产管理系统' },
            { key: 'page_size', value: '20' }
          ]
        })
      })
    )

    const result = await getConfigList()

    expect(Array.isArray(result)).toBe(true)
    expect(result).toHaveLength(2)
    expect(result[0].key).toBe('site_name')
    expect(result[0].value).toBe('房产管理系统')
  })

  it('updateConfig should update a config value', async () => {
    server.use(
      http.put('/api/admin/config/:key', async ({ params, request }) => {
        const body = await request.json()
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: { key: params.key, value: body.value }
        })
      })
    )

    const result = await updateConfig('site_name', '新系统名称')

    expect(result.key).toBe('site_name')
    expect(result.value).toBe('新系统名称')
  })
})

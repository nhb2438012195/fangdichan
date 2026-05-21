import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import { getVacancyAnalysis } from '../analysis'

describe('analysis API', () => {
  afterEach(() => server.resetHandlers())

  it('getVacancyAnalysis should fetch vacancy analysis data', async () => {
    server.use(
      http.get('/api/agent/analysis/vacancy', () => {
        return HttpResponse.json({
          code: 200,
          msg: 'success',
          data: {
            total: 100,
            vacant: 15,
            occupancyRate: '85.00%',
            districtBreakdown: [
              { district: '朝阳区', total: 40, vacant: 5 },
              { district: '海淀区', total: 60, vacant: 10 }
            ]
          }
        })
      })
    )

    const result = await getVacancyAnalysis()

    expect(result.total).toBe(100)
    expect(result.vacant).toBe(15)
    expect(result.occupancyRate).toBe('85.00%')
    expect(result.districtBreakdown).toHaveLength(2)
    expect(result.districtBreakdown[0].district).toBe('朝阳区')
  })
})

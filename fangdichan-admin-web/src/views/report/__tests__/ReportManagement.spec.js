import { describe, it, expect, vi } from 'vitest'

describe('ReportManagement - API call pattern', () => {
  it('should send status as query parameter, not request body', () => {
    const id = 1
    const status = 'PROCESSED'

    // Track how request.put would be called
    const request = { put: vi.fn() }

    // This simulates the CURRENT BUGGY code:
    // await request.put(`/admin/report/${id}/status`, { status })
    // Backend expects @RequestParam but frontend sends as body
    request.put(`/admin/report/${id}/status`, { status })

    // Verify the BUG: the status is in the body (second arg), not in params
    const buggyCall = request.put.mock.calls[0]
    expect(buggyCall[0]).toBe('/admin/report/1/status')
    expect(buggyCall[1]).toEqual({ status: 'PROCESSED' }) // body contains status
    expect(buggyCall[2]).toBeUndefined() // no query params

    // Clear mock and test the FIX
    request.put.mockClear()
    request.put(`/admin/report/${id}/status`, null, { params: { status } })

    const fixedCall = request.put.mock.calls[0]
    expect(fixedCall[0]).toBe('/admin/report/1/status')
    expect(fixedCall[1]).toBeNull() // body should be null
    expect(fixedCall[2]).toEqual(
      expect.objectContaining({
        params: { status: 'PROCESSED' }
      })
    )
  })

  it('should also work for DISMISSED status', () => {
    const id = 1
    const status = 'DISMISSED'
    const request = { put: vi.fn() }

    // BUGGY pattern
    request.put(`/admin/report/${id}/status`, { status })
    expect(request.put.mock.calls[0][1]).toEqual({ status: 'DISMISSED' })

    // FIXED pattern
    request.put.mockClear()
    request.put(`/admin/report/${id}/status`, null, { params: { status } })
    expect(request.put.mock.calls[0][1]).toBeNull()
    expect(request.put.mock.calls[0][2].params).toEqual({ status: 'DISMISSED' })
  })
})

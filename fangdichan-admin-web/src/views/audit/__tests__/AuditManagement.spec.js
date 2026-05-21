import { describe, it, expect } from 'vitest'

// Test the PageResult data access pattern used by AuditManagement
// The bug: current code does `res.data || []` but should do `res.data.list || []`
describe('AuditManagement - PageResult data access', () => {
  it('should extract the list array from a PageResult response', () => {
    // Simulate the API response shape from AdminPropertyController
    // Backend returns Result<PageResult<Property>> which gives:
    // { code: 200, msg: "success", data: { list: [...], total: N, page: 1, size: 10 } }
    const apiResponse = {
      code: 200,
      msg: 'success',
      data: {
        list: [{ id: 1, title: '测试房源', district: '朝阳', price: 5000, status: 'PENDING' }],
        total: 1,
        page: 1,
        size: 10
      }
    }

    // This is what the current BUGGY code evaluates to:
    const buggyResult = apiResponse.data || []

    // This is what the FIXED code should evaluate to:
    const fixedResult = apiResponse.data.list || []

    // The bug: buggyResult is the PageResult object { list: [...], total: 1, ... }, not an array
    expect(Array.isArray(buggyResult)).toBe(false)
    expect(buggyResult).toEqual({
      list: [{ id: 1, title: '测试房源', district: '朝阳', price: 5000, status: 'PENDING' }],
      total: 1,
      page: 1,
      size: 10
    })

    // The fix: fixedResult should be the actual list array
    expect(Array.isArray(fixedResult)).toBe(true)
    expect(fixedResult).toHaveLength(1)
    expect(fixedResult[0].title).toBe('测试房源')
  })

  it('should handle empty PageResult data correctly', () => {
    const apiResponse = { data: { list: [], total: 0 } }

    const buggyResult = apiResponse.data || []
    const fixedResult = apiResponse.data.list || []

    // Bug: buggyResult in a v-for would render nothing useful (it's an object with list property)
    expect(Array.isArray(buggyResult)).toBe(false)

    // Fix: fixedResult is an empty array which is correct for v-for
    expect(fixedResult).toEqual([])
  })
})

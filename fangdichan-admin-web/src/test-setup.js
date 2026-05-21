import { vi, beforeAll, afterAll, afterEach } from 'vitest'
import { server } from './mocks/server'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn(() => Promise.resolve()) },
  default: { install: vi.fn() }
}))

// Integrate MSW for contract testing
beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

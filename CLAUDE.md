# CLAUDE.md

Monorepo for a real estate property search system:
- **fangdichan-server** — Spring Boot 3.2 (Java 17, MyBatis Plus, Spring Security, JWT, MinIO, MySQL)
- **fangdichan-client** — Vue 3 desktop client (Vite, port 5173)
- **fangdichan-admin-web** — Vue 3 admin SPA (Element Plus, port 3000)

## Business Domain Knowledge

### Database values for filter fields
These values must match exactly in tests, MSW mocks, and frontend constants. Backend uses `eq()` for exact match.

| Field | Actual DB values | Notes |
|-------|-----------------|-------|
| `property.room_type` | `一室一厅`, `两室一厅`, `三室一厅`, `三室两厅`, `四室两厅`, `五室两厅` | 前端 constants.js 缺 `三室一厅` 和 `五室两厅` |
| `property.district` | 来自 regions.json 的完整行政区划（北京各区、上海各区、广州、深圳等） | 搜索用 value 需与 DB `eq()` 精确匹配 |
| `property.status` | `APPROVED`, `PENDING`, `REJECTED` | 英文大写字符串，非中文 |
| `purchase_order.status` | `PENDING`, `CONFIRMED` | 订单状态 |

### Search/filter critical rules
- **district** / **roomType**: `eq()` 精确匹配 — 必须用完整值（`三室两厅`，不是 `三室`）
- **keyword**: LIKE on `title` + `location`
- **priceMin/priceMax**, **areaMin/areaMax**: range filter
- `cleanParams()` strips null/undefined/empty-string/zero before sending

### Full-stack data chain rule
When writing query interfaces, verify in order:
1. Actual DB values for filter fields
2. Backend tests use those real values
3. Frontend MSW mock data uses same values
4. Frontend contract tests assert correct filtering behavior

### API contract consistency rule
- Before adding or modifying any API that touches both frontend and backend, read `docs/api-contract.md` for the current contract
- Modify `docs/api-contract.md` whenever API paths, parameters, or response structures change
- After backend changes (new controller method, modified response structure), verify frontend has a matching API call and MSW handler with identical path and data format
- After frontend changes (new API module, updated MSW handler), verify the backend endpoint actually exists at the expected path
- Response structure shape (plain string vs VO object, field names, nesting) must match exactly between backend, MSW mocks, and frontend consumers—path alone is not enough

### Options/dropdowns
- Prefer dict API (`/api/public/dict/districts`, `/api/public/dict/room-types`) over hardcoded constants
- `constants.js` is offline fallback — keep synced with DB values

### Property entity computed field
- `unitPrice` is auto-computed in `Property.java` setters: `setPrice()` and `setArea()` both calculate `unitPrice = price / area` (via `divide(area, 2, RoundingMode.HALF_UP)`)
- **Service layer manually manages unitPrice during updates**: `updateProperty()` nulls `unitPrice` first, then conditionally recomputes from the incoming price/area. This is intentional — the entity auto-compute is a safety net, but the service needs precise control for partial updates.
- If adding a new code path that modifies price or area, follow the same pattern: null unitPrice, then recompute if both fields are present and area > 0.

## Quick Start

```bash
npm run lint                                         # Lint both frontends
npm test                                             # Test both frontends
cd fangdichan-client && npm run dev                  # Client on :5173
cd fangdichan-admin-web && npm run dev               # Admin on :3000
cd fangdichan-server && ./mvnw spring-boot:run -Dspring.profiles.active=dev
cd fangdichan-server && ./mvnw test                  # Backend tests (MySQL)
cd fangdichan-server && ./mvnw validate              # Checkstyle + PMD
npx vitest run path/to/test.js                       # Single frontend test file
```

## Architecture

### API routing (SecurityConfig)
| Pattern | Access |
|---------|--------|
| `/api/public/**` | Open (login, register, dict) |
| `/api/admin/**` | ADMIN only |
| `/api/agent/**` | ADMIN or AGENT |
| `/api/customer/**` | ADMIN or CUSTOMER |
| `/ws/**` | Open (WebSocket) |

### Response format
- Success: `{ code: 200, msg: "success", data: ... }`
- Paginated: `PageResult` inside `Result.data`: `{ list, page, size, total, pages }`
- Interceptor chain: axios response → `res.data` (interceptor) → `.then(res => res.data)` (API fn) → `data` field

### Backend layering
`Controller` → `Service`/`ServiceImpl` → `Mapper` (MyBatis Plus) → `Entity`
- Modules cannot depend on each other at Service level; shared logic goes in `common`
- New modules follow the 4-layer structure

### Module inventory

| Module | Entity | Mapper | Service | Controller | DTO | Notes |
|--------|:-:|:-:|:-:|:-:|:-:|-------|
| `user` | ✓ | ✓ | ✓ | ✓ | ✓ | 用户管理 + 认证 |
| `company` | ✓ | ✓ | ✓ | ✓ | | 房地产公司 |
| `property` | ✓ | ✓ | ✓ | ✓ | | 房源 CRUD + 审核 |
| `order` | ✓ | ✓ | ✓ | ✓ | | 购房订单 |
| `favorite` | ✓ | ✓ | ✓ | ✓ | | 收藏 |
| `message` | ✓ | ✓ | ✓ | ✓ | | 在线沟通 |
| `analysis` | | | ✓ | ✓ | ✓ | 关联分析（无 entity/mapper） |
| `suggestion` | ✓ | ✓ | ✓ | ✓ | | 建议反馈 |
| `report` | ✓ | ✓ | ✓ | ✓ | | 举报 |
| `config` | ✓ | ✓ | ✓ | ✓ | | 系统配置 |
| `region` | ✓ | ✓ | ✓ | ✓ | ✓ | 区域管理 |
| `roomtype` | ✓ | ✓ | ✓ | ✓ | ✓ | 房型管理 |

Before adding a new module, check this table to avoid duplicates.

### Business rules
- Property `status` transitions: `PENDING` → `APPROVED`/`REJECTED` (via ADMIN audit only)
- Properties have no delete operation — use `setOffMarket()` to take a property off market instead
- `BusinessException(code, message)` → `GlobalExceptionHandler` → `{ code, msg }` JSON response (business error codes start at 1000)
- Validation errors (`@Valid`) → `MethodArgumentNotValidException` → `400` with field-level message

### Database conventions
- Table names: lowercase_underscore, PK `id` BIGINT AUTO_INCREMENT
- Every table: `created_at` / `updated_at`
- Foreign keys: `xxx_id` with CONSTRAINT

### Error codes
| Code | Meaning |
|------|---------|
| 200 | Success |
| 400 | Bad request |
| 401 | Unauthenticated |
| 403 | Forbidden |
| 404 | Not found |
| 500 | Internal error |
| 1000+ | Business errors |

## Key Conventions

### Frontend API modules
```js
export function searchProperties(params) {
  return request.get('/customer/property/search', { params }).then(res => res.data)
}
```

### Vue components
- `<script setup>`, typed `defineProps` + validator, `defineEmits`
- State via Pinia (no `provide/inject` for shared state)
- Always handle three states: loading (`v-loading`), data display, error (`ElMessage.error`)

### Auth flow
Login stores `token`, `role`, `userId`, `username` in Pinia + localStorage. Axios interceptor attaches `Authorization: Bearer {token}`. 401 clears token → redirect `/login`.

### Frontend routes — fangdichan-client

| Path | View | Auth required |
|------|------|:-------------:|
| `/login` | Login.vue | |
| `/register` | Register.vue | |
| `/home` | Home.vue | |
| `/search` | Search.vue | |
| `/detail/:id` | PropertyDetail.vue | |
| `/favorite` | Favorite.vue | ✓ |
| `/order` | OrderList.vue | ✓ |
| `/message` | MessageView.vue | ✓ |
| `/company` | CompanyList.vue | |
| `/company/:id` | CompanyDetail.vue | |
| `/profile` | Profile.vue | ✓ |
| `/suggestion` | Suggestion.vue | ✓ |
| `/report/:propertyId` | ReportForm.vue | ✓ |

### Frontend routes — fangdichan-admin-web

| Path | View | Required role |
|------|------|:-------------:|
| `/login` | Login.vue | |
| `/register` | Register.vue | |
| `/dashboard` | Dashboard.vue | |
| `/user` | UserManagement.vue | ADMIN |
| `/company` | CompanyInfo.vue | AGENT |
| `/property` | PropertyManagement.vue | AGENT |
| `/audit` | AuditManagement.vue | ADMIN |
| `/order` | OrderManagement.vue | AGENT |
| `/analysis` | AnalysisView.vue | AGENT |
| `/report-handle` | ReportManagement.vue | ADMIN |
| `/message` | MessageView.vue | |
| `/config` | SystemConfig.vue | ADMIN |

## Testing

- **Backend**: JUnit 5 + MySQL (远程测试库 `182.92.176.70:3306/fangdichan_db`), MyBatis Plus `BaseMapper` integration tests
  - Config: `src/test/resources/application-test.yml`
  - Run all: `cd fangdichan-server && ./mvnw test`
  - Run single: `./mvnw test -Dtest=PropertyIntegrationTest`
  - ⚠️ Tests require network access to the remote DB; offline runs will fail
- **Frontend**: Vitest + happy-dom, MSW for API mocking
  - MSW handlers: `src/mocks/handlers.js`（每个前端项目独立）
  - Server starts in `test-setup.js`
  - Element Plus (ElMessage, ElMessageBox) mocked globally in `test-setup.js`
  - Test files: `src/views/**/__tests__/*.spec.js` or `src/api/__tests__/*.test.js`
  - Run all: `npm test`
  - Run single: `npx vitest run path/to/test.js`

## Commit Convention

Follow Conventional Commits: `feat:|fix:|refactor:|test:|docs:|chore:|style:|perf:|ci:`. Enforced by commitlint + husky.

## AI Behavior

- Prefer editing existing files over creating new ones
- Follow existing code patterns in the codebase
- After API contract changes, update frontend tests and MSW mock data synchronously
- Use `eq()` for exact-match fields in MyBatis Plus queries
- Always verify test data consistency across backend → mock → frontend
- Read `docs/api-contract.md` before starting any task touching both frontend and backend; update it when API contracts change
- New backend controller method → check frontend has a corresponding API call with the same path
- New frontend API module → check backend endpoint exists at the expected path and response format matches

### CLAUDE.md maintenance

Keep this file in sync with project evolution. Update it when:

| Trigger | What to update |
|---------|----------------|
| 新增业务模块 | Module inventory 表 + 路由表（如有） |
| 修改 DB 字段或枚举值 | Database values 表，特别是 filter 字段 |
| 新增 API 路由模式 | API routing 表 |
| 产生新的关键业务规则 | Business rules 小节 |
| 前端新增页面 | Frontend routes 表 |
| 测试基础设施变动 | Testing 节（如加了 H2 配置能离线跑） |
| 发现文档与实际不符 | 立即修正 |

**不需要更新**：细小的代码变更、修 bug、重构内部实现但不改接口——这些交给 git log。

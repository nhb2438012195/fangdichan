# CLAUDE.md

该文档CLAUDE.md中若有不合理，不能满足项目需求，不能实现的内容，可以提出修改建议

房地产房源搜索系统 monorepo：
- **fangdichan-server** — Spring Boot 3.2（Java 17, MyBatis Plus, Spring Security, JWT, MinIO, MySQL）
- **fangdichan-client** — Vue 3 桌面端（Vite, 端口 5173）
- **fangdichan-admin-web** — Vue 3 管理后台（Element Plus, 端口 3000）

## 业务领域知识

### 数据库筛选字段实际值
以下值必须在测试、MSW mock 和前端常量中完全一致。后端使用 `eq()` 精确匹配。

| 字段 | 数据库实际值 | 说明 |
|------|-------------|------|
| `property.room_type` | `一室一厅`, `两室一厅`, `三室一厅`, `三室两厅`, `四室两厅`, `五室两厅` | 前端 constants.js 缺 `三室一厅` 和 `五室两厅` |
| `property.district` | 来自 regions.json 的完整行政区划（北京各区、上海各区、广州、深圳等） | 搜索用 value 需与 DB `eq()` 精确匹配 |
| `property.status` | `APPROVED`, `PENDING`, `REJECTED` | 英文大写字符串，非中文 |
| `purchase_order.status` | `PENDING`, `CONFIRMED` | 订单状态 |

### 搜索/筛选关键规则
- **district** / **roomType**：`eq()` 精确匹配—必须用完整值（`三室两厅`，不是 `三室`）
- **keyword**：LIKE 模糊匹配 `title` + `location`
- **priceMin/priceMax**、**areaMin/areaMax**：范围筛选
- `cleanParams()` 发送前会剔除 null/undefined/空字符串/零值

### 全栈数据链规则
编写查询接口时，按顺序验证：
1. 确认筛选字段的数据库实际值
2. 后端测试必须包含超过半数的数据库实际值
3. 前端 MSW mock 数据必须包含超过半数的数据库实际值
4. 前端契约测试断言正确的筛选行为

### API 契约一致性规则
- 在新增或修改涉及前后端的 API 之前，先读取 `docs/api-contract.md` 了解当前契约
- 每当 API 路径、参数或响应结构发生变化时，同步更新 `docs/api-contract.md`
- 后端变更后（新增 Controller 方法、修改响应结构），验证前端有匹配的 API 调用和 MSW handler，且路径和数据格式一致，如果不符合预期，则在`docs/api-contract.md`中指出问题
- 前端变更后（新增 API 模块、更新 MSW handler），验证后端端点确实存在于预期路径，如果不符合预期，则修改前端代码，以求与后端接口对齐
- 响应结构形状（字符串数组 vs VO 对象、字段名、嵌套层级）必须在后端、MSW mock、前端消费方之间完全一致—仅路径一致不够
- 当新增修改代码后发现本次修改导致项目与`docs/api-contract.md`的描述不一致时，需要在`docs/api-contract.md`中声明并给出修改建议

### 选项/下拉框
- 优先使用 dict API（`/api/public/dict/districts`、`/api/public/dict/room-types`）而非硬编码常量
- `constants.js` 是离线 fallback——需与数据库值保持同步

### Property 实体计算字段
- `unitPrice` 由 `Property.java` 的 setter 自动计算：`setPrice()` 和 `setArea()` 都会计算 `unitPrice = price / area`（使用 `divide(area, 2, RoundingMode.HALF_UP)`）
- **Service 层在更新时手动控制 unitPrice**：`updateProperty()` 先将 `unitPrice` 置 null，然后根据传入的 price/area 有条件地重新计算。这是有意为之—实体的自动计算是安全网，但 service 需要对部分更新有精确控制。
- 如果新增修改 price 或 area 的代码路径，遵循相同模式：先将 unitPrice 置 null，如果 price 和 area 都存在且 area > 0 则重新计算。

## 快速开始

```bash
npm run lint                                         # 检查两个前端项目
npm test                                             # 测试两个前端项目
cd fangdichan-client && npm run dev                  # 客户端 :5173
cd fangdichan-admin-web && npm run dev               # 管理后台 :3000
cd fangdichan-server && ./mvnw spring-boot:run -Dspring.profiles.active=dev
cd fangdichan-server && ./mvnw test                  # 后端测试（需要 MySQL）
cd fangdichan-server && ./mvnw validate              # Checkstyle + PMD
npx vitest run path/to/test.js                       # 单个前端测试文件
```

## 架构

### API 路由（SecurityConfig）
| 匹配模式 | 访问权限 |
|---------|---------|
| `/api/public/**` | 公开（登录、注册、字典） |
| `/api/admin/**` | 仅 ADMIN |
| `/api/agent/**` | ADMIN 或 AGENT |
| `/api/customer/**` | ADMIN 或 CUSTOMER |
| `/ws/**` | 公开（WebSocket） |

### 响应格式
- 成功：`{ code: 200, msg: "success", data: ... }`
- 分页：`PageResult` 放在 `Result.data` 内：`{ list, page, size, total, pages }`
- 拦截器链：axios 响应 → `res.data`（拦截器）→ `.then(res => res.data)`（API 函数）→ `data` 字段

### 后端分层
`Controller` → `Service`/`ServiceImpl` → `Mapper`（MyBatis Plus）→ `Entity`
- 模块之间不能在 Service 层互相依赖；共享逻辑放在 `common` 中
- 新模块遵循 4 层结构

### 模块清单

| 模块 | Entity | Mapper | Service | Controller | DTO | 说明 |
|------|:-:|:-:|:-:|:-:|:-:|------|
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

新增模块前先检查此表，避免重复。

### 业务规则
- Property `status` 流转：`PENDING` → `APPROVED`/`REJECTED`（仅 ADMIN 审核）
- 房产不支持删除——使用 `setOffMarket()` 下架代替
- `BusinessException(code, message)` → `GlobalExceptionHandler` → 返回 `{ code, msg }` JSON（业务错误码从 1000 开始）
- 校验错误（`@Valid`）→ `MethodArgumentNotValidException` → `400` 状态码，返回字段级错误信息

### 数据库约定
- 表名：小写下划线，主键 `id` BIGINT AUTO_INCREMENT
- 每张表：`created_at` / `updated_at`
- 外键：`xxx_id` 并带 CONSTRAINT

### 错误码
| 状态码 | 含义 |
|--------|------|
| 200 | 成功 |
| 400 | 请求错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 未找到 |
| 500 | 服务器内部错误 |
| 1000+ | 业务错误 |

## 关键约定

### 前端 API 模块
```js
export function searchProperties(params) {
  return request.get('/customer/property/search', { params }).then(res => res.data)
}
```

### Vue 组件
- `<script setup>`，类型化的 `defineProps` + 校验器，`defineEmits`
- 状态管理用 Pinia（不使用 `provide/inject` 共享状态）
- 始终处理三种状态：加载中（`v-loading`）、数据显示、错误（`ElMessage.error`）

### 认证流程
登录后存储 `token`、`role`、`userId`、`username` 到 Pinia 和 localStorage。Axios 拦截器自动添加 `Authorization: Bearer {token}`。收到 401 时清除 token 并重定向到 `/login`。

### 前端路由 — fangdichan-client

| 路径 | 视图 | 需要登录 |
|------|------|:-------:|
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

### 前端路由 — fangdichan-admin-web

| 路径 | 视图 | 所需角色 |
|------|------|:--------:|
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

## 测试

- **后端**：JUnit 5 + MySQL（远程测试库 `182.92.176.70:3306/fangdichan_db`），MyBatis Plus `BaseMapper` 集成测试
  - 配置：`src/test/resources/application-test.yml`
  - 运行全部：`cd fangdichan-server && ./mvnw test`
  - 运行单个：`./mvnw test -Dtest=PropertyIntegrationTest`
  - ⚠️ 测试需要网络连接远程数据库；离线时无法运行
- **前端**：Vitest + happy-dom，MSW 模拟 API
  - MSW handlers：`src/mocks/handlers.js`（每个前端项目独立）
  - 服务在 `test-setup.js` 中启动
  - Element Plus（ElMessage、ElMessageBox）在 `test-setup.js` 中全局 mock
  - 测试文件：`src/views/**/__tests__/*.spec.js` 或 `src/api/__tests__/*.test.js`
  - 运行全部：`npm test`
  - 运行单个：`npx vitest run path/to/test.js`

## 提交规范

遵循 Conventional Commits：`feat:|fix:|refactor:|test:|docs:|chore:|style:|perf:|ci:`。由 commitlint + husky 强制执行。

## AI 行为规范

- 优先编辑已有文件而非新建文件
- 遵循代码库中的现有模式
- API 契约变更后，同步更新前端测试和 MSW mock 数据
- MyBatis Plus 查询中精确匹配字段使用 `eq()`
- 始终验证测试数据在 后端 → mock → 前端 之间的一致性
- 在开始任何涉及前后端的任务前，先读取 `docs/api-contract.md`；API 契约变更时同步更新
- 新增后端 Controller 方法 → 检查前端是否有对应路径的 API 调用
- 新增前端 API 模块 → 检查后端端点是否存在于预期路径，且响应格式匹配

### CLAUDE.md 维护

跟随项目演进保持此文件同步。以下情况需要更新：

| 触发条件 | 更新内容 |
|---------|---------|
| 新增业务模块 | 模块清单表 + 路由表（如有） |
| 修改 DB 字段或枚举值 | 数据库值表，特别是筛选字段 |
| 新增 API 路由模式 | API 路由表 |
| 产生新的关键业务规则 | 业务规则小节 |
| 前端新增页面 | 前端路由表 |
| 测试基础设施变动 | 测试小节（如加了 H2 配置能离线跑） |
| 发现文档与实际不符 | 立即修正 |

**不需要更新**：细小的代码变更、修 bug、重构内部实现但不改接口——这些交给 git log。

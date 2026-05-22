# API Contract

> 前后端接口契约定义。修改 API 时须同步更新此文件。
> 这是前后端联调的唯一可信来源，AI Agent 在涉及前后端对接的开发前必须先读取此文件。

## 约定

- **baseURL**: `/api`（axios 实例配置）
- **成功响应**: `{ code: 200, msg: "success", data: ... }`
- **分页响应**: `data` 内为 `{ list, page, size, total, pages }`
- **请求方式**: 前端路径以 `/` 开头，拼接 baseURL 后为完整路径
- **记号**:
  - ✅ 前后端匹配
  - ⚠️ 路径或格式不一致（见备注）
  - ❌ 前端调用了但后端无对应端点
  - ➕ 后端有但前端未对接

---

## 1. 公共接口 `/api/public/**`

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 1.1 | POST | `/api/public/login` | `loginApi()` (client + admin) | `AuthController.login()` | ✅ |
| 1.2 | POST | `/api/public/register` | `registerApi()` (client + admin) | `AuthController.register()` | ✅ |
| 1.3 | GET | `/api/public/region/provinces` | — | `RegionController.getProvinces()` | ➕ |
| 1.4 | GET | `/api/public/region/children?parentId=` | — | `RegionController.getChildren()` | ➕ |
| 1.5 | GET | `/api/public/region/search?keyword=` | — | `RegionController.search()` | ➕ |
| 1.6 | GET | `/api/public/room-types?regionId=` | — | `RoomTypeController.getRoomTypes()` | ➕ |
| 1.7 | GET | `/api/public/dict/districts` | `getDistricts()` (client + admin) | — | ❌ |
| 1.8 | GET | `/api/public/dict/room-types` | `getRoomTypes()` (client + admin) | — | ❌ |

### 1.7 ~ 1.8 说明

前端 `dict.js` 调用的 `/public/dict/districts` 和 `/public/dict/room-types` 路径在后端不存在。
后端实际提供的是 `/api/public/region/` 和 `/api/public/room-types`，且响应格式不同：

- 前端 mock 期望 `data` 为**字符串数组**：`["朝阳区", "海淀区"]`
- 后端实际返回**VO 对象数组**：`[{id, name, level, type}]` 或 `[{id, bedroomCount, livingRoomCount, displayName}]`

**待决策**：确定统一路径和格式后再联调。

### 1.6 room-types 的 regionId 参数

后端 `RoomTypeController.getRoomTypes()` 声明了 `@RequestParam(required = false) Long regionId` 但未使用——传入任何值结果不变。

---

## 2. 客户接口 `/api/customer/**`

### 2.1 房产搜索

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 2.1.1 | GET | `/api/customer/property/search` | `searchProperties(params)` | `CustomerPropertyController.search()` | ✅ |
| 2.1.2 | GET | `/api/customer/property/{id}` | `getPropertyDetail(id)` | `CustomerPropertyController.detail()` | ✅ |
| 2.1.3 | GET | `/api/customer/property/recommended` | `getRecommended()` | — | ❌ |

**search 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `keyword` | string | 模糊匹配 title + location |
| `district` | string | 精确匹配（eq），见 DB values 表 |
| `roomType` | string | 精确匹配（eq），见 DB values 表 |
| `priceMin` | BigDecimal | 范围下限 |
| `priceMax` | BigDecimal | 范围上限 |
| `areaMin` | BigDecimal | 范围下限 |
| `areaMax` | BigDecimal | 范围上限 |
| `page` | int | 默认 1 |
| `size` | int | 默认 10 |

**响应**: `PageResult<Property>` — `{ list, page, size, total, pages }`

### 2.2 收藏

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 2.2.1 | POST | `/api/customer/favorite/{propertyId}` | `toggleFavorite(propertyId)` | `CustomerFavoriteController.toggle()` | ✅ |
| 2.2.2 | GET | `/api/customer/favorite/list` | `getFavoriteList(params)` | `CustomerFavoriteController.list()` | ✅ |
| 2.2.3 | GET | `/api/customer/favorite/check/{propertyId}` | `checkFavorite(propertyId)` | — | ❌ |

### 2.3 订单

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 2.3.1 | POST | `/api/customer/order` | `createOrder(propertyId, message)` | `CustomerOrderController.createOrder()` | ✅ |
| 2.3.2 | GET | `/api/customer/order/list` | `getOrderList(params)` | `CustomerOrderController.list()` | ✅ |
| 2.3.3 | PUT | `/api/customer/order/{id}/cancel` | `cancelOrder(id)` | `CustomerOrderController.cancel()` | ✅ |

### 2.4 消息

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 2.4.1 | POST | `/api/customer/conversation` | `createConversation(companyId, propertyId)` | `CustomerMessageController.createConversation()` | ⚠️ |
| 2.4.2 | GET | `/api/customer/conversation/list` | `getConversationList()` | `CustomerMessageController.listConversations()` | ⚠️ |
| 2.4.3 | GET | `/api/customer/conversation/{id}/messages` | `getMessages(conversationId)` | `CustomerMessageController.getMessages()` | ⚠️ |
| 2.4.4 | POST | `/api/customer/conversation/{id}/message` | `sendMessage(conversationId, content)` | — | ❌ |

#### 消息模块路径差异

前端路径和实际后端路径不一致：

| 前端调用 | 后端实际路径 |
|----------|-------------|
| `POST /customer/conversation` | `POST /api/customer/message/conversation` |
| `GET /customer/conversation/list` | `GET /api/customer/message/conversations` |
| `GET /customer/conversation/{id}/messages` | `GET /api/customer/message/{conversationId}` |
| `POST /customer/conversation/{id}/message` | （后端无客户发消息端点，只有 agent 端有 `POST /{conversationId}/send`）|

**待决策**：统一路径前缀为 `/message/` 或 `/conversation/`，并为客户补充发消息端点。

### 2.5 公司

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 2.5.1 | GET | `/api/customer/company/list` | `getCompanyList()` | `CustomerCompanyController.list()` | ✅ |
| 2.5.2 | GET | `/api/customer/company/{id}` | `getCompanyDetail(id)` | `CustomerCompanyController.detail()` | ✅ |

### 2.6 个人资料

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 2.6.1 | GET | `/api/customer/profile` | `getProfile()` | `CustomerController.getProfile()` | ✅ |
| 2.6.2 | PUT | `/api/customer/profile` | `updateProfile(data)` | `CustomerController.updateProfile()` | ✅ |
| 2.6.3 | PUT | `/api/customer/profile/password` | `changePassword(oldPassword, newPassword)` | `CustomerController.changePassword()` | ✅ |

### 2.7 建议反馈

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 2.7.1 | POST | `/api/customer/suggestion` | `submitSuggestion(data)` | `CustomerSuggestionController.submit()` | ✅ |
| 2.7.2 | GET | `/api/customer/suggestion/list` | — | `CustomerSuggestionController.listMySuggestions()` | ➕ |

### 2.8 举报

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 2.8.1 | POST | `/api/customer/report` | `submitReport(propertyId, reason)` | `CustomerReportController.create()` | ✅ |

---

## 3. 经纪人接口 `/api/agent/**`

### 3.1 房产管理

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 3.1.1 | GET | `/api/agent/property/list` | `getMyPropertyList(params)` | `AgentPropertyController.list()` | ✅ |
| 3.1.2 | POST | `/api/agent/property` | `createProperty(data)` | `AgentPropertyController.create()` | ✅ |
| 3.1.3 | PUT | `/api/agent/property/{id}` | `updateProperty(id, data)` | `AgentPropertyController.update()` | ✅ |
| 3.1.4 | PUT | `/api/agent/property/{id}/off-market` | `takeOffProperty(id)` | `AgentPropertyController.offMarket()` | ✅ |
| 3.1.5 | POST | `/api/agent/property/image/upload` | — | `AgentPropertyController.uploadImage()` | ➕ |
| 3.1.6 | DELETE | `/api/agent/property/image/{id}` | — | `AgentPropertyController.deleteImage()` | ➕ |

### 3.2 订单管理

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 3.2.1 | GET | `/api/agent/order/list` | `getOrderList(params)` | `AgentOrderController.list()` | ✅ |
| 3.2.2 | PUT | `/api/agent/order/{id}/confirm` | `confirmOrder(id)` | `AgentOrderController.confirm()` | ✅ |
| 3.2.3 | PUT | `/api/agent/order/{id}/cancel` | `cancelOrder(id)` | `AgentOrderController.cancel()` | ✅ |

### 3.3 消息

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 3.3.1 | GET | `/api/agent/conversation/list` | `getConversationList()` (admin) | `AgentMessageController.listConversations()` | ⚠️ |
| 3.3.2 | GET | `/api/agent/conversation/{id}/messages` | `getMessages(conversationId)` (admin) | `AgentMessageController.getMessages()` | ⚠️ |
| 3.3.3 | POST | `/api/agent/conversation/{id}/message` | `sendMessage(conversationId, content)` (admin) | `AgentMessageController.sendMessage()` | ⚠️ |

#### 路径差异

| 前端调用 | 后端实际路径 |
|----------|-------------|
| `GET /agent/conversation/list` | `GET /api/agent/message/conversations` |
| `GET /agent/conversation/{id}/messages` | `GET /api/agent/message/{conversationId}` |
| `POST /agent/conversation/{id}/message` | `POST /api/agent/message/{conversationId}/send` |

### 3.4 公司

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 3.4.1 | GET | `/api/agent/company` | `getCompanyInfo()` | `AgentCompanyController.get()` | ✅ |
| 3.4.2 | PUT | `/api/agent/company` | `updateCompanyInfo(data)` | `AgentCompanyController.update()` | ✅ |

### 3.5 数据分析

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 3.5.1 | GET | `/api/agent/analysis/vacancy` | `getVacancyAnalysis()` | `AnalysisController.vacancy()` | ✅ |

### 3.6 经纪人建议管理

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 3.6.1 | GET | `/api/agent/suggestion/list` | — | `AgentSuggestionController.list()` | ➕ |

### 3.7 报表导出

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 3.7.1 | GET | `/api/agent/report/property-export` | — | `ReportExportController.exportProperties()` | ➕ |

---

## 4. 管理员接口 `/api/admin/**`

### 4.1 房产审核

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 4.1.1 | GET | `/api/admin/property/pending` | `getPendingList()` | — | ❌ |
| 4.1.2 | PUT | `/api/admin/property/{id}/approve` | `approveProperty(id)` | `AdminPropertyController.approve()` | ✅ |
| 4.1.3 | PUT | `/api/admin/property/{id}/reject` | `rejectProperty(id)` | `AdminPropertyController.reject()` | ✅ |

### 4.2 用户管理

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 4.2.1 | GET | `/api/admin/users` | `getUserList()` | `AdminUserController.list()` | ✅ |
| 4.2.2 | PUT | `/api/admin/users/{id}/status` | `toggleUserStatus(id, status)` | `AdminUserController.toggleStatus()` | ✅ |

### 4.3 系统配置

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 4.3.1 | GET | `/api/admin/config/list` | `getConfigList()` | `SystemConfigController.listAll()` | ⚠️ |
| 4.3.2 | PUT | `/api/admin/config/{key}` | `updateConfig(key, value)` | `SystemConfigController.updateConfig()` | ⚠️ |

#### 4.3 差异说明

| 项目 | 前端 | 后端 |
|------|------|------|
| GET 路径 | `/admin/config/list` | `/api/admin/config`（无 `/list`） |
| PUT 请求体 | JSON `{ value: "xxx" }` | `@RequestParam String value`（期望 form-data / query params）|
| PUT 响应处理 | `res.data`（解一层） | 返回 `Result` 对象 |

### 4.4 举报管理

| # | 方法 | 路径 | 前端函数 | 后端 | 状态 |
|---|------|------|----------|------|------|
| 4.4.1 | GET | `/api/admin/report/list` | `getReportList()` | `AdminReportController.listPending()` | ⚠️ |
| 4.4.2 | PUT | `/api/admin/report/{id}/status?status=` | `handleReport(id, status)` | — | ⚠️ |

#### 4.4 差异说明

| 项目 | 前端 | 后端 |
|------|------|------|
| GET 路径 | `/admin/report/list` | `/api/admin/report/pending` |
| 处理举报 | `PUT /{id}/status?status=X` | 后端有 `PUT /{id}/dismiss` 和 `PUT /{id}/process` 两个独立端点 |

---

## 5. 跨域 / 未对接的后端端点

| # | 方法 | 路径 | 后端 | 说明 |
|---|------|------|------|------|
| 5.1 | GET | `/api/agent/suggestion/list` | `AgentSuggestionController.list()` | 经纪人查看建议列表 |
| 5.2 | GET | `/api/customer/suggestion/list` | `CustomerSuggestionController.listMySuggestions()` | 客户查看自己的建议 |
| 5.3 | POST | `/api/agent/property/image/upload` | `AgentPropertyController.uploadImage()` | 图片上传 |
| 5.4 | DELETE | `/api/agent/property/image/{id}` | `AgentPropertyController.deleteImage()` | 图片删除 |
| 5.5 | GET | `/api/public/region/provinces` | `RegionController.getProvinces()` | 获取省级区域 |
| 5.6 | GET | `/api/public/region/children` | `RegionController.getChildren()` | 获取子区域 |
| 5.7 | GET | `/api/public/region/search` | `RegionController.search()` | 搜索区域 |
| 5.8 | GET | `/api/public/room-types` | `RoomTypeController.getRoomTypes()` | 获取房型列表 |
| 5.9 | GET | `/api/agent/report/property-export` | `ReportExportController.exportProperties()` | 导出房产报表 |

---

## 6. 关键枚举值

> 后端用 `eq()` 精确匹配，值必须完全一致。

### property.room_type

`一室一厅`, `两室一厅`, `三室一厅`, `三室两厅`, `四室两厅`, `五室两厅`

### property.district

来自 `regions.json` 的完整行政区划名称。所有省级、市级、区级名称都可能作为 district 值。

### property.status

`APPROVED`, `PENDING`, `REJECTED`

### purchase_order.status

`PENDING`, `CONFIRMED`

---

## 7. 响应格式规范

### 成功

```json
{ "code": 200, "msg": "success", "data": ... }
```

### 分页

```json
{ "code": 200, "msg": "success", "data": { "list": [], "page": 1, "size": 10, "total": 0, "pages": 0 } }
```

### 业务错误

```json
{ "code": 1001, "msg": "错误描述", "data": null }
```

### 校验错误 (400)

```json
{ "code": 400, "msg": "字段名: 错误信息", "data": null }
```

---

## 8. MSW Mock 覆盖要求

每个前端项目的 `mocks/handlers.js` 必须覆盖其使用的所有接口。

当前覆盖状态：

| 前端项目 | handler 文件 | 覆盖接口数 | 缺失接口 |
|----------|-------------|-----------|---------|
| fangdichan-client | `src/mocks/handlers.js` | 10 | `/api/public/dict/*`, `/api/customer/favorite/check/*`, `/api/customer/property/recommended`, `/api/customer/conversation/*`, `/api/customer/order/*` 等 |
| fangdichan-admin-web | `src/mocks/handlers.js` | 17 | `/api/public/dict/*`, `/api/admin/config/*`, `/api/admin/property/pending`, `/api/admin/report/*` 等 |

---

## 修改记录

| 日期 | 修改内容 |
|------|----------|
| 2026-05-22 | 初始版本，基于当前代码审计结果创建 |

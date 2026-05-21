# 代码规范

## 后端 (Java / Spring Boot)

### 命名规范
- 类名：PascalCase (`UserService`, `PropertyController`)
- 方法名：camelCase (`findByUserId`, `createOrder`)
- 常量：UPPER_SNAKE_CASE (`MAX_PAGE_SIZE`)
- 包名：全小写 (`com.fdsc.module.user`)

### 分层职责

| 层 | 职责 | 命名后缀 |
|---|------|---------|
| Controller | 接收请求、参数校验、调用 Service | `XxxController` |
| Service | 业务逻辑、事务管理 | `XxxService` / `XxxServiceImpl` |
| Mapper | 数据库访问 (MyBatis-Plus) | `XxxMapper` |
| Entity | 数据库映射实体 | `Xxx`（与表名对应）|
| DTO | 数据传输对象 | `XxxDTO` / `XxxVO` |

### 异常处理
- 业务异常使用 `BusinessException`，code + message
- Controller 层不 try-catch，由全局 `GlobalExceptionHandler` 统一处理
- 不允许在 Service 层抛出 `RuntimeException` 原始类型

### API 命名规范
- RESTful 风格
- 路径前缀：`/api/{role}/{resource}`
- 分页接口统一用 `page` 参数名，返回 `PageResult<T>` 包装
- 所有响应统一使用 `Result<T>` 包装

## 前端 (Vue 3)

### 文件命名
- Vue 组件：PascalCase (`UserManagement.vue`)
- JS 工具模块：camelCase (`api.js`, `auth.js`)
- 测试文件：`Xxx.spec.js`（与组件同目录）

### 组件规范
- 使用 `<script setup>` 语法
- Props 用 `defineProps` + 类型校验
- 组件内事件用 `defineEmits` 声明
- 状态管理用 Pinia（不使用 provide/inject 做状态共享）

### API 调用规范
- 统一通过 `src/api/` 下的模块调用
- 使用 axios 实例（预配置 baseURL 和拦截器）
- 响应数据必须解构 PageResult：`res.data.data.list`
- 查询参数用 `params: { key: value }` 传递

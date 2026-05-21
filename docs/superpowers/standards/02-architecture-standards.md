# 架构规范

## 后端模块划分

```
common/        # 公共工具、配置、异常处理、统一响应
security/      # Spring Security + JWT 认证授权
module/
  ├── user/        # 用户管理 + 认证
  ├── company/     # 房地产公司
  ├── property/    # 房源 CRUD + 审核
  ├── order/       # 购房订单
  ├── favorite/    # 收藏
  ├── message/     # 在线沟通
  ├── analysis/    # 关联分析
  ├── suggestion/  # 建议
  ├── report/      # 举报
  └── config/      # 系统配置
```

### 模块依赖规则
- module/* 可以依赖 common、security
- module/* 之间禁止相互依赖（通过 controller 或 service 调用需提取公共逻辑到 common）
- 新模块必须遵循 `controller → service → mapper → entity` 四层结构
- entity 只包含字段定义，不包含业务方法

### 数据库设计规范
- 表名：小写 + 下划线 (`sys_user`, `purchase_order`)
- 所有表必须有 `id` BIGINT AUTO_INCREMENT 主键
- 所有表必须有 `created_at` / `updated_at` 时间戳
- 外键用 `xxx_id` 命名，必须建立 FOREIGN KEY
- 状态字段用 ENUM 类型，Java 中用 String
- JSON 字段用 MySQL JSON 类型，Java 中用 String + @TableField(typeHandler = JacksonTypeHandler)

### API 响应规范

```json
{
  "code": 200,
  "msg": "success",
  "data": {}  // T 泛型
}
```

分页响应 data 结构：

```json
{
  "list": [],
  "total": 0,
  "page": 1,
  "size": 10
}
```

### 异常码规范

| 范围 | 用途 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

业务异常 code 从 1000 开始自定义。

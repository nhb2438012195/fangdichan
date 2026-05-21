# 房地产客户购房查询系统 — 设计文档

**日期**: 2026-05-21
**状态**: 已定稿

## 1. 概述

房地产客户购房查询系统，为学习目的开发。系统支持三类用户角色：管理员、房地产商、客户，实现房源发布、审核、查询、购房申请、数据分析等全流程功能。

## 2. 技术栈

| 层 | 技术 |
|---|------|
| 后端框架 | Spring Boot 3.x |
| ORM | MyBatis-Plus |
| 安全认证 | Spring Security + JWT |
| 数据库 | MySQL 8.x |
| 前端框架 | Vue 3 + Vite |
| UI 组件库 (后台) | Element Plus |
| 桌面客户端 | Electron（自定义桌面风格 UI） |
| 图表 | ECharts |
| 对象存储 | MinIO (自建 S3 兼容存储) |
| 实时通信 | WebSocket (Spring WebSocket) |
| 报表导出 | Apache POI / JasperReports |

## 3. 架构设计

### 3.1 总体架构

```
┌─────────────────────────────────────────────────┐
│  fangdichan-admin-web (浏览器)                    │
│  Vue 3 + Element Plus                           │
│  管理员 / 房地产商                               │
├─────────────────────────────────────────────────┤
│  fangdichan-client (桌面应用)                     │
│  Vue 3 + Element Plus(桌面风覆写) + Electron      │
│  客户                                            │
├──────────────────────┬──────────────────────────┤
│     REST API (JSON)  │                          │
├──────────────────────┘                          │
│  fangdichan-server                               │
│  Spring Boot 单体 + 分层模块                      │
│  common → security → module/* (controller/service/mapper/entity) │
│  src/main/resources/mapper/ (MyBatis XML)       │
├─────────────────────────────────────────────────┤
│  MySQL 8.x                                       │
└─────────────────────────────────────────────────┘
```

### 3.2 后端项目结构

```
fangdichan-server/
├── src/main/java/com/fdsc/
│   ├── common/                  # 公共模块
│   │   ├── config/              # 全局配置 (CORS, Jackson, Swagger...)
│   │   ├── constant/            # 常量定义
│   │   ├── exception/           # 统一异常处理
│   │   ├── result/              # 统一响应封装 Result<T>
│   │   └── util/                # 工具类
│   │
│   ├── security/                # 安全认证模块
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── SecurityConfig.java
│   │
│   └── module/                  # 业务模块
│       ├── user/                # 用户模块
│       │   ├── controller/      # UserController (登录注册同理)
│       │   ├── service/         # UserService + impl
│       │   ├── mapper/          # UserMapper
│       │   └── entity/          # SysUser, CustomerProfile
│       │
│       ├── company/             # 公司模块
│       │   ├── controller/      # CompanyController
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/          # Company
│       │
│       ├── property/            # 房源模块
│       │   ├── controller/      # PropertyController
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/          # Property, PropertyImage
│       │
│       ├── order/               # 购房订单模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/          # PurchaseOrder
│       │
│       ├── favorite/            # 收藏模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/          # Favorite
│       │
│       ├── analysis/            # 关联分析模块
│       │   ├── controller/      # AnalysisController
│       │   ├── service/         # 统计分析逻辑 + ECharts 数据
│       │   └── entity/          # 分析结果 DTO
│       │
│       ├── suggestion/          # 建议模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/          # Suggestion
│       │
│       ├── report/              # 举报模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/          # Report
│       │
│       ├── message/             # 沟通消息模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   └── entity/          # Conversation, Message
│       │
│       └── config/              # 系统配置模块
│           ├── controller/
│           ├── service/
│           ├── mapper/
│           └── entity/          # SystemConfig
│
└── src/main/resources/
    ├── application.yml          # 主配置
    ├── application-dev.yml      # 开发环境配置
    ├── application-prod.yml     # 生产环境配置
    └── mapper/                  # MyBatis XML 映射文件
```

### 3.3 前端项目

```
fangdichan-admin-web/
├── src/
│   ├── views/
│   │   ├── login/
│   │   ├── dashboard/           # 仪表盘（按角色展示不同内容）
│   │   ├── user/                # 用户管理（管理员）
│   │   ├── company/             # 公司信息（房地产商）
│   │   ├── property/            # 房源管理（房地产商）
│   │   ├── audit/               # 房源审核（管理员）
│   │   ├── order/               # 购房订单（房地产商）
│   │   ├── analysis/            # 关联分析（房地产商）
│   │   ├── report/              # 举报处理（管理员）
│   │   └── config/              # 系统配置（管理员）
│   ├── router/                  # 路由配置
│   ├── store/                   # Pinia 状态管理
│   └── api/                     # API 接口封装

fangdichan-client/
├── src/
│   ├── views/
│   │   ├── home/                # 首页推荐
│   │   ├── search/              # 房源搜索 + 筛选
│   │   ├── detail/              # 房源详情
│   │   ├── purchase/            # 购房申请
│   │   ├── profile/             # 个人中心（信息编辑、意向管理）
│   │   ├── favorite/            # 收藏夹
│   │   ├── suggestion/          # 建议提交
│   │   ├── message/             # 在线沟通
│   │   ├── company/             # 查看房地产商信息
│   │   └── report/              # 举报
│   ├── router/
│   ├── store/
│   └── api/
```

### 3.4 前端 UI 设计

#### 3.4.1 后台管理 (fangdichan-admin-web)

- **样式**: Element Plus 浅色主题，传统的侧边栏布局
- **布局**: 顶部状态栏 + 左侧菜单侧边栏 + 主内容区
- **菜单按角色动态显示**: 管理员和房地产商登录后看到不同的菜单项

```
┌───────────────────────────────────────────────┐
│  🏡 购房通 管理后台              👤 用户名     │
├──────────┬────────────────────────────────────┤
│ 📊 仪表盘 │                                    │
│ 👥 用户管理│                                    │
│ 🏢 公司管理│           主内容区域                │
│ 🏠 房源管理│         (表格/表单/图表)            │
│ 📋 审核管理│                                    │
│ 📦 订单管理│                                    │
│ 📈 关联分析│                                    │
│ 💬 消息管理│                                    │
│ ⚙️ 系统配置│                                    │
└──────────┴────────────────────────────────────┘
```

#### 3.4.2 客户桌面端 (fangdichan-client)

- **样式**: 桌面原生风格，电商浏览模式（卡片网格展示房源）
- **布局**: 自定义标题栏 + 左侧图标导航栏 + 紧凑工具栏 + 主内容区 + 底部状态栏

```
┌────────────────────────────────────────────────┐
│ 🏡 购房通                         —  □  ✕     │  ← 自定义标题栏
├─────┬──────────────────────────────────────────┤
│     │  [搜索框...        搜索]  🔥热门 最近浏览 │  ← 工具栏
│ 🏠  ├──────────────────────────────────────────┤
│ 🔍  │  ┌──────┐ ┌──────┐ ┌──────┐            │
│ ❤️  │  │      │ │      │ │      │            │
│ 💬  │  │房源卡 │ │房源卡 │ │房源卡 │            │  ← 卡片网格
│ 📄  │  │      │ │      │ │      │            │
│ 🏢  │  └──────┘ └──────┘ └──────┘            │
│     │  ┌──────┐ ┌──────┐ ┌──────┐            │
│ 👤  │  │      │ │      │ │      │            │
│     │  │房源卡 │ │房源卡 │ │房源卡 │            │
│     │  │      │ │      │ │      │            │
│     │  └──────┘ └──────┘ └──────┘            │
├─────┴──────────────────────────────────────────┤
│ 已登录 | 用户名                共 128 套房源    │  ← 状态栏
└────────────────────────────────────────────────┘
```

- **导航栏图标及对应页面**:

| 图标 | 页面 | 说明 |
|------|------|------|
| 🏠 | 首页推荐 | 推荐房源卡片，热门区域快捷入口 |
| 🔍 | 房源查询 | 左侧筛选面板 + 右侧搜索结果列表 |
| ❤️ | 我的收藏 | 收藏列表，可按区域筛选 |
| 💬 | 消息 | 会话列表 + 聊天窗口 |
| 📄 | 购房订单 | 订单列表 + 详情 |
| 🏢 | 房产公司 | 房地产商列表 + 公司详情 |
| 👤 | 个人中心 | 资料编辑、购房意向、建议提交、举报 |

## 4. 数据库设计

### 4.1 完整表结构

> 所有列表查询接口均支持分页（pageNum/pageSize 参数），后端使用 MyBatis-Plus 分页插件，返回页码、总条数、总页数。

**sys_user** — 用户表
```sql
CREATE TABLE sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('ADMIN','AGENT','CUSTOMER') NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1-启用 0-禁用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**customer_profile** — 客户信息表
```sql
CREATE TABLE customer_profile (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE,
    real_name   VARCHAR(50),
    phone       VARCHAR(20),
    email       VARCHAR(100),
    buy_intent  JSON COMMENT '{"districts":[], "room_types":[], "price_min":0, "price_max":0}',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
```

**company** — 房地产公司表
```sql
CREATE TABLE company (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE,
    company_name    VARCHAR(100) NOT NULL,
    address         VARCHAR(255),
    contact_phone   VARCHAR(20),
    description     TEXT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
```

**property** — 房源表
```sql
CREATE TABLE property (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id      BIGINT       NOT NULL,
    title           VARCHAR(200) NOT NULL,
    location        VARCHAR(255) NOT NULL COMMENT '地址',
    district        VARCHAR(50)  COMMENT '所属区域',
    floor           VARCHAR(20)  COMMENT '所在楼层区间 如 1-3F/4-6F/7-10F',
    floor_total     INT          COMMENT '总楼层数',
    room_type       VARCHAR(50)  NOT NULL COMMENT '户型 如 三室两厅',
    area            DECIMAL(10,2) COMMENT '面积 ㎡',
    price           DECIMAL(15,2) COMMENT '总价',
    unit_price      DECIMAL(10,2) COMMENT '单价',
    is_vacant       BOOLEAN      DEFAULT TRUE COMMENT '是否空置',
    status          ENUM('PENDING','APPROVED','REJECTED','SOLD','OFF_MARKET') DEFAULT 'PENDING',
    description     TEXT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id)
);
```

**property_image** — 房源图片表
```sql
CREATE TABLE property_image (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT       NOT NULL,
    image_url   VARCHAR(500) NOT NULL COMMENT '图片访问路径',
    sort_order  INT          DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES property(id) ON DELETE CASCADE
);
```

> 图片上传至 MinIO（自建对象存储服务），数据库存储 MinIO 文件访问路径，后端集成 MinIO Java SDK 处理文件读写。

**purchase_order** — 购房订单表
```sql
CREATE TABLE purchase_order (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(32) NOT NULL UNIQUE,
    customer_id BIGINT      NOT NULL,
    property_id BIGINT      NOT NULL,
    status      ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING',
    message     TEXT        COMMENT '客户留言',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES sys_user(id),
    FOREIGN KEY (property_id) REFERENCES property(id)
);
```

**favorite** — 收藏表
```sql
CREATE TABLE favorite (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customer_property (customer_id, property_id),
    FOREIGN KEY (customer_id) REFERENCES sys_user(id),
    FOREIGN KEY (property_id) REFERENCES property(id)
);
```

**suggestion** — 建议表
```sql
CREATE TABLE suggestion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    company_id      BIGINT       NOT NULL COMMENT '目标房地产公司',
    desired_type    VARCHAR(50)  COMMENT '期望房型',
    desired_price_min DECIMAL(15,2),
    desired_price_max DECIMAL(15,2),
    content         TEXT         NOT NULL COMMENT '建议内容',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES sys_user(id),
    FOREIGN KEY (company_id) REFERENCES company(id)
);
```

**report** — 举报表
```sql
CREATE TABLE report (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT  NOT NULL,
    customer_id BIGINT  NOT NULL,
    reason      TEXT    NOT NULL,
    status      ENUM('PENDING','DISMISSED','PROCESSED') DEFAULT 'PENDING',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES property(id),
    FOREIGN KEY (customer_id) REFERENCES sys_user(id)
);
```

**conversation** — 会话表
```sql
CREATE TABLE conversation (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT  NOT NULL,
    company_id  BIGINT  NOT NULL,
    property_id BIGINT,
    status      ENUM('OPEN','CLOSED') DEFAULT 'OPEN',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES sys_user(id),
    FOREIGN KEY (company_id) REFERENCES company(id),
    FOREIGN KEY (property_id) REFERENCES property(id)
);
```

**message** — 消息表
```sql
CREATE TABLE message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT   NOT NULL,
    sender_id       BIGINT   NOT NULL,
    sender_role     ENUM('CUSTOMER','AGENT') NOT NULL,
    content         TEXT     NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE
);
```

**system_config** — 系统配置表
```sql
CREATE TABLE system_config (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key  VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 4.2 关键字段说明

- **用户角色体系**：`sys_user.role` 区分 ADMIN / AGENT / CUSTOMER，统一登录表
- **房源审核流程**：`property.status` 从 `PENDING` → `APPROVED`（管理员审核）或 `REJECTED`
- **房源下架**：售出后标记 `SOLD`，房地产商可手动标记 `OFF_MARKET`
- **unit_price 处理**：`unit_price` 保留字段，后端在创建/更新房源时根据 `price / area` 自动计算写入，不作为存储原始数据
- **删除策略**：房源不提供物理删除操作，通过 `status`（`OFF_MARKET` / `SOLD`）控制下架。收藏中已下架的房源前端提示"已下架"
- **关联分析**：按 `district`、`floor`、`room_type` 维度统计 `is_vacant` 比率，输出 ECharts 图表数据
- **沟通流程**：客户发起会话 → 双方在会话下发送消息

## 5. 功能清单（按角色）

### 5.1 管理员（fangdichan-admin-web）
- 登录/注册管理账号
- 用户管理：查看客户和房地产商列表，启用/禁用账户
- 房源审核：审核房地产商提交的房源（通过/拒绝）
- 举报处理：查看举报记录，处理后下架房源或驳回举报
- 系统配置：维护系统参数
- 修改密码
- 报表打印（整体统计报表）

### 5.2 房地产商（fangdichan-admin-web）
- 登录/注册企业账号
- 公司信息管理：编辑公司名称、地址、联系方式、简介
- 房源管理：发布新房源、修改房源属性、下架房源
- 购房订单管理：查看客户购房申请、确认/取消订单
- 关联分析：按地理位置/楼层/房型维度查看空置率图表
- 在线沟通：回复客户消息
- 修改密码
- 报表打印：房源查询结果导出为报表

### 5.3 客户（fangdichan-client 桌面端）
- 注册/登录
- 个人信息管理：编辑基本信息，添/删购房意向，变更联系方式，修改密码（原密码 → 新密码）
- 首页推荐：浏览推荐房源、热门区域、"大家都在看"标签
- 引导查询：首页提供"帮我找房"入口，点击进入步骤式向导（区域→预算→户型→展示结果）
- 首页同时展示热门搜索推荐标签，一键触发搜索
- 自定义搜索：按区域、价格、户型、面积等条件组合筛选（后端 MyBatis-Plus 动态 SQL 拼接 WHERE）
- 房源详情：查看完整信息、图片
- 购房申请：提交购房意向订单
- 收藏：收藏/取消收藏房源
- 浏览房地产商公司信息
- 建议提交：指出期望房型与可接受价格
- 举报：举报问题房源
- 在线沟通：与房地产商实时交流
- 报表打印：查询结果导出为报表

## 6. 关联分析模块设计

房地产商在后台查看分析图表，数据由后端统计后以 JSON 格式返回前端，前端使用 ECharts 渲染。

### 6.1 分析维度

| 维度 | 说明 | 图表类型 |
|------|------|---------|
| 地理位置 | 按 district 分组统计空置率 | 柱状图 |
| 所在楼层 | 按 floor 区间分组统计空置率 | 柱状图 |
| 房型 | 按 room_type 分组统计空置率 | 柱状图/饼图 |

### 6.2 API 响应格式示例

```json
{
  "district": [
    {"name": "朝阳区", "total": 120, "vacant": 15, "vacancyRate": 0.125},
    {"name": "海淀区", "total": 95,  "vacant": 8,  "vacancyRate": 0.084}
  ],
  "floor": [
    {"name": "1-3F",   "total": 60, "vacant": 12, "vacancyRate": 0.200},
    {"name": "4-6F",   "total": 80, "vacant": 10, "vacancyRate": 0.125},
    {"name": "7-10F",  "total": 55, "vacant": 5,  "vacancyRate": 0.091}
  ],
  "roomType": [
    {"name": "一室一厅", "total": 40, "vacant": 8,  "vacancyRate": 0.200},
    {"name": "两室一厅", "total": 90, "vacant": 12, "vacancyRate": 0.133},
    {"name": "三室两厅", "total": 70, "vacant": 5,  "vacancyRate": 0.071}
  ]
}
```

## 7. 消息通信设计

- 采用 WebSocket（Spring WebSocket）实现实时消息推送
- 客户和房地产商建立连接后，新消息即时送达，无需轮询
- 消息存储仍写入 MySQL（message 表），WebSocket 仅负责实时推送通知
- 客户端断线重连机制：重新连接后拉取离线期间未读消息
- **WebSocket 认证方式**：客户端在连接 URL 上携带 JWT token 参数（如 `ws://host/ws?token=xxx`），服务端实现 `HandshakeInterceptor` 在校验拦截器中验证 token 有效性，验证通过才允许建立连接

## 9. 报表打印设计

- 使用 Apache POI 生成 Excel 报表
- 后端生成文件后返回下载链接
- 前端触发下载，Electron 端可调用系统打印
- 报表内容：房源查询结果表、分析统计表

## 10. 安全设计

- 密码：BCrypt 加密存储
- 认证：JWT Token，登录后返回 token，后续请求携带在 Header 中
- 授权：Spring Security 拦截 URL，按角色（ADMIN/AGENT/CUSTOMER）控制访问权限
- API 路径设计：
  - `/api/admin/**` — 管理员接口
  - `/api/agent/**` — 房地产商接口
  - `/api/customer/**` — 客户接口
  - `/api/public/**` — 公开接口（登录注册等）

## 11. 部署方案

- 服务器运行 fangdichan-server（Spring Boot jar）+ MySQL
- fangdichan-admin-web 构建为静态文件，由 Nginx 或 Spring Boot 内嵌容器托管
- fangdichan-client 构建为 Electron 桌面安装包，客户安装后通过 HTTP 远程访问服务器 API

## 12. 测试策略

### 12.1 后端测试

- **框架**: JUnit 5 + Mockito + Spring Boot Test
- **层次**:
  - **Service 层单元测试**: Mock Mapper 层，验证业务逻辑和异常分支
  - **Controller 层集成测试**: 使用 `@WebMvcTest` + MockMvc，Mock Service 层，验证 HTTP 响应和权限控制
  - **Repository 层测试**: 使用 `@MybatisPlusTest` 或 H2 内嵌数据库验证 SQL
- **覆盖要求**: 每个 Service 方法至少一个正常路径用例 + 一个异常路径用例
- **测试目录结构**: `fangdichan-server/src/test/java/com/fdsc/`，与主代码包路径一致

### 12.2 前端测试

- **框架**: Vitest + @vue/test-utils
- **范围**: 组件渲染测试、用户交互测试
- **测试目录**: 每个组件同级目录下创建 `*.test.js` 文件
- **覆盖要求**: 核心业务页面（登录、搜索、详情）至少一个基础渲染测试

### 12.3 测试数据

- Service 层测试使用 Mock 数据
- Controller 集成测试使用 MockMvc 模拟 HTTP 请求
- 不依赖外部数据库/服务的测试优先

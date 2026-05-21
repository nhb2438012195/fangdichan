# Region & RoomType 重构设计文档

> **Goal:** 将区域和户型从 property 表的字符串字段重构为独立规范表，实现全国区域层级化、户型结构化、搜索人性化

**Architecture:**
- 新增 `region` 表（省/直辖市→市/区 两级）和 `room_type` 表（bedroom_count + living_room_count 结构化）
- property 表改为 FK 关联 region + room_type，冗余 province_id 以支持省级查询
- 用户地址存入 customer_profile.region_id，控制默认推荐范围
- 前端级联选择 + 按城市过滤可用户型 + 人性化价格快速选择

**Tech Stack:** Spring Boot 3.2 / MyBatis Plus / MySQL / Vue 3 / Element Plus

---

## 1. 数据库设计

### 1.1 region 表

全国区域两级结构，省/直辖市为 level=1，市/区为 level=2。

```sql
CREATE TABLE region (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '区域名称，如 北京市/朝阳区/广东省/广州市',
    level       TINYINT NOT NULL COMMENT '层级: 1-省/直辖市, 2-市/区',
    parent_id   BIGINT DEFAULT NULL COMMENT '父级ID，省/直辖市为 NULL',
    type        VARCHAR(20) DEFAULT NULL COMMENT '类型: province/municipality/city/district',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES region(id),
    INDEX idx_parent (parent_id),
    INDEX idx_level (level)
) COMMENT '全国区域';
```

**示例数据：**

| id | name | level | parent_id | type |
|----|------|-------|-----------|------|
| 1 | 北京市 | 1 | NULL | municipality |
| 2 | 朝阳区 | 2 | 1 | district |
| 3 | 海淀区 | 2 | 1 | district |
| 4 | 广东省 | 1 | NULL | province |
| 5 | 广州市 | 2 | 4 | city |
| 6 | 深圳市 | 2 | 4 | city |

### 1.2 room_type 表

结构化户型定义，前端按几室+几厅级联选择。

```sql
CREATE TABLE room_type (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    bedroom_count     TINYINT NOT NULL COMMENT '几室: 1,2,3,4,5',
    living_room_count TINYINT NOT NULL COMMENT '几厅: 1,2',
    display_name      VARCHAR(50) NOT NULL COMMENT '显示名称 如三室两厅',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bedroom_living (bedroom_count, living_room_count)
) COMMENT '户型';
```

**示例数据：**

| id | bedroom_count | living_room_count | display_name |
|----|--------------|-------------------|--------------|
| 1 | 1 | 1 | 一室一厅 |
| 2 | 2 | 1 | 两室一厅 |
| 3 | 3 | 1 | 三室一厅 |
| 4 | 3 | 2 | 三室两厅 |
| 5 | 4 | 2 | 四室两厅 |
| 6 | 5 | 2 | 五室两厅 |

### 1.3 property 表变更

| 字段 | 变更 | 说明 |
|------|------|------|
| ~~district~~ | 删除 | 由 region_id 替代 |
| ~~room_type~~ | 删除 | 由 room_type_id 替代 |
| + region_id | 新增 BIGINT FK→region(id) | 关联到市/区级别 |
| + province_id | 新增 BIGINT FK→region(id) | 省级冗余，创建时由 region_id 推导 |
| + room_type_id | 新增 BIGINT FK→room_type(id) | 关联户型表 |
| location | 保留 | 存详细街道地址 |

```sql
ALTER TABLE property
    ADD COLUMN region_id    BIGINT  AFTER company_id,
    ADD COLUMN province_id  BIGINT  AFTER region_id,
    ADD COLUMN room_type_id BIGINT  AFTER province_id,
    ADD INDEX idx_region (region_id),
    ADD INDEX idx_province (province_id),
    ADD INDEX idx_room_type (room_type_id),
    DROP COLUMN district,
    DROP COLUMN room_type;

ALTER TABLE property
    ADD FOREIGN KEY (region_id) REFERENCES region(id),
    ADD FOREIGN KEY (province_id) REFERENCES region(id),
    ADD FOREIGN KEY (room_type_id) REFERENCES room_type(id);
```

### 1.4 customer_profile 表变更

| 字段 | 变更 | 说明 |
|------|------|------|
| + region_id | 新增 BIGINT FK→region(id) | 用户所在市/区，NULL 时默认北京市朝阳区 |

```sql
ALTER TABLE customer_profile
    ADD COLUMN region_id BIGINT AFTER buy_intent,
    ADD FOREIGN KEY (region_id) REFERENCES region(id);
```

### 1.5 suggestion 表变更

| 字段 | 变更 | 说明 |
|------|------|------|
| ~~desired_type~~ | 删除 | VARCHAR 改为 FK |
| + room_type_id | 新增 BIGINT FK→room_type(id) | 期望户型 |

```sql
ALTER TABLE suggestion
    ADD COLUMN room_type_id BIGINT AFTER company_id,
    ADD INDEX idx_room_type (room_type_id),
    DROP COLUMN desired_type;

ALTER TABLE suggestion
    ADD FOREIGN KEY (room_type_id) REFERENCES room_type(id);
```

---

## 2. API 设计

### 2.1 区域接口（公开，无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/public/region/provinces` | 获取所有省/直辖市列表 |
| GET | `/api/public/region/children?parentId={id}` | 获取某省的下级市/某直辖市的下辖区 |
| GET | `/api/public/region/search?keyword={kw}` | 按名称搜索区域（支持省/市/区名） |

**返回格式：** `Result<List<RegionVO>>`

RegionVO:
```json
{
  "id": 1,
  "name": "北京市",
  "level": 1,
  "type": "municipality",
  "parentName": null
}
```

### 2.2 户型接口（公开，无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/public/room-types` | 获取所有户型列表；可传 `?regionId=` 过滤该区域有房源的户型 |
| GET | `/api/public/room-types/bedroom-counts` | 获取可选卧室数（用于前端下拉） |

**返回格式：** `Result<List<RoomTypeVO>>`

RoomTypeVO:
```json
{
  "id": 1,
  "bedroomCount": 3,
  "livingRoomCount": 2,
  "displayName": "三室两厅"
}
```

### 2.3 搜索接口变更

**旧：**
```
GET /api/customer/property/search?keyword=&district=&roomType=&priceMin=&priceMax=...
```

**新：**
```
GET /api/customer/property/search
  ?keyword=
  &regionId=          -- 省/市/区均可，后端自动判断层级
  &bedroomCount=      -- 卧室数筛选
  &livingRoomCount=   -- 客厅数筛选
  &priceLevel=        -- 价格区间预设: none(不限)/lt100(100万以下)/100-200/200-300/300-500/gte500(500万以上)/custom(自定义)
                       -- 价格区间映射:
                       --   none    → 不筛选价格
                       --   lt100   → priceMax < 1000000
                       --   100-200 → priceMin >= 1000000 AND priceMax <= 2000000
                       --   200-300 → priceMin >= 2000000 AND priceMax <= 3000000
                       --   300-500 → priceMin >= 3000000 AND priceMax <= 5000000
                       --   gte500  → priceMin >= 5000000
                       --   custom  → 使用 priceMin/priceMax 参数（单位: 元）
  &priceMin=          -- 自定义最低价（配合 priceLevel=custom 使用）
  &priceMax=          -- 自定义最高价
  &areaMin=
  &areaMax=
  &page=&size=
```

**搜索逻辑伪代码：**
```
if regionId 是省/直辖市级别:
    provinceId = regionId
    properties WHERE province_id = provinceId
if regionId 是市/区级别:
    properties WHERE region_id = regionId
if bedroomCount 有值:
    roomTypeIds IN (SELECT id FROM room_type WHERE bedroom_count = bedroomCount)
if livingRoomCount 有值:
    roomTypeIds IN (SELECT id FROM room_type WHERE living_room_count = livingRoomCount)
```

### 2.4 推荐接口变更

推荐接口 `/api/customer/property/recommended` 改为根据用户 region 推荐：

```
GET /api/customer/property/recommended?page=&size=
-- 从 JWT 获取 userId → customerProfile.regionId
-- 如果 region 的 type=city（普通城市）→ 推荐该市房源（WHERE region_id = ?）
-- 如果 region 的 type=district（直辖市辖区）→ 推荐该区房源（WHERE region_id = ?）
-- 如果 regionId 为 NULL → 默认北京市朝阳区
```

**不按省推荐。** 用户所在的 region_id 始终指向 level=2 的节点（市或区），推荐时精确匹配该节点的房源。

### 2.5 用户区域接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/customer/profile/region` | 获取用户当前区域 |
| PUT | `/api/customer/profile/region` | 修改用户区域 `{ regionId: xxx }` |

---

## 3. 前端设计

### 3.1 区域级联选择器

搜索页和表单中的区域选择采用两层级联：

1. **第一步：选择省/直辖市**
   - 下拉菜单显示所有省/直辖市
   - 支持 filterable（输入搜索）
2. **第二步：选择市/区**
   - 根据选中的省/直辖市加载下级
   - 普通省显示城市列表，直辖市显示区列表
   - 支持 filterable
3. **快捷搜索**
   - 用户可直接输入区域名搜索匹配的省/市/区
   - 搜索结果中标注层级关系（如"广州市 / 广东省"）
4. **价格区间选择**
   - 快捷选项：不限 / 100万以下 / 100-200万 / 200-300万 / 300-500万 / 500万以上
   - 自定义：最低(万) ~ 最高(万)，用户输入以万为单位

### 3.2 户型级联选择器

用户选择区域后，户型下拉选项从 `GET /api/public/room-types?regionId={regionId}` 加载：

1. **几室下拉**：从返回数据中 DISTINCT bedroom_count（如 2室/3室/4室）
2. **几厅下拉**：根据已选几室过滤可用的 living_room_count（如选3室 → 可选的厅：1厅/2厅）
3. 均支持 filterable 搜索
4. 如区域切换，户型选项自动重新加载

### 3.3 用户区域设置

- Profile.vue 增加区域选择器
- 首次注册用户默认为北京市朝阳区
- 修改区域后推荐页内容更新

### 3.4 涉及的视图

| 视图 | 变更 |
|------|------|
| Search.vue | 区域改为级联选择、户型改为几室+几厅下拉、价格改为快速选择 |
| Home.vue | 推荐按用户区域、找房向导级联 |
| PropertyDetail.vue | 显示区域路径（省/市/区） |
| Profile.vue | 增加区域选择 |
| PropertyManagement.vue (admin) | 区域+户型级联 |
| Suggestion.vue | desiredType → room_type_id 联动 |

---

## 4. 数据初始化

### 4.1 区域基础数据

- 所有省份（34个省级行政区）
- 所有地级市（约 350 个）
- 直辖市辖区（北京16区、上海16区、天津16区、重庆26区县）
- 约 400 条记录

### 4.2 户型基础数据

- 约 6-8 种标准户型组合

### 4.3 测试房源数据

- 每个主要城市 3-5 条房源
- 北京：朝阳区 3条、海淀区 2条
- 上海：浦东新区 2条
- 广州：天河区 2条、越秀区 1条
- 深圳：南山区 2条
- 杭州：西湖区 2条
- 约 20-30 条房源

---

## 5. 不纳入范围

以下在 brainstorming 中已讨论但确认不纳入本次实现：

- Floor 楼层抽象为 low_floor/high_floor — 维持当前字符串格式
- property status 独立表 — 维持 ENUM
- Company 关联区域 — 暂不添加

---

## 6. 影响范围检查

### 后端
- 新增：Region 实体 + Mapper + Controller
- 新增：RoomType 实体 + Mapper + Controller
- 修改：Property 实体 + ServiceImpl（搜索/创建逻辑）
- 修改：CustomerProfile 实体
- 修改：Suggestion 实体
- 修改：PropertyServiceImplTest 等单元测试
- 新增：RegionServiceImplTest / RoomTypeServiceImplTest
- 修改：DictController（可删除或改为调用新接口）
- 修改：init.sql（新增表 + 修改 property 表）

### 前端 client
- 新增：api/region.js + MSW handlers + 测试
- 新增：api/room-type.js + MSW handlers + 测试
- 修改：Search.vue（级联区域 + 户型 + 价格）
- 修改：Home.vue（区域推荐 + 向导）
- 修改：Profile.vue（区域选择）
- 修改：Suggestion.vue（户型联动）
- 修改：PropertyDetail.vue（区域路径显示）
- 修改：api-contract.test.js（更新契约测试）

### 前端 admin-web
- 新增：api/region.js + api/room-type.js + MSW handlers + 测试
- 修改：PropertyManagement.vue（级联选择）
- 修改：SuggestionManagement 相关（如有）

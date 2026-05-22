# Claude Code 高效开发指南

> 基于房地产房源搜索系统的实战经验总结
> 适用对象：你（人类开发者）—— 不是给 AI 执行的计划

---

## 目录

1. [现状诊断：三个痛点的根因](#1-现状诊断三个痛点的根因)
2. [Session 管理：每次对话的正确姿势](#2-session-管理每次对话的正确姿势)
3. [验证体系：在 bug 到达你之前截住它](#3-验证体系在-bug-到达你之前截住它)
4. [审查协议：让 AI 帮你定位问题的标准流程](#4-审查协议让-ai-帮你定位问题的标准流程)
5. [速查清单](#5-速查清单)

---

## 1. 现状诊断：三个痛点的根因

### 1.1 为什么 AI 会"理解偏差"

这不是 AI "笨"，而是 context 管理问题。看当前项目的一个具体例子：

```
docs/superpowers/plans/2026-05-21-region-roomtype-refactor.md
├── Task 1-5:  后端 Region/RoomType（已完成）
├── Task 6-8:  Property 实体改造 + Service（未开始）
├── Task 9:    CustomerProfile regionId（未开始）
├── Task 10:   Suggestion roomTypeId（未开始）
├── Task 11:   后端清理（未开始）
├── Task 12-18: 客户端前端改造（未开始）
└── Task 19-21: 管理端前端改造（未开始）
```

**根因：** 21 个任务在一个计划文件里。当 AI 在一个 session 中执行到 Task 13（前端 Search.vue），它的 context 里仍然塞着 Task 1 的 Region 实体细节、Task 6 的 Property 字段变更、Task 9 的 CustomerProfile……每个任务带来的上下文都在消耗 attention budget。

结果是：AI 开始忘记细节。比如它可能在一个前端组件里用了 `district` 而不是 `regionId`，因为 1000 行对话之前的 context 已经模糊了。

### 1.2 为什么测试经常挂

测试失败的噪音掩盖了真正的信号。

```
mvn test 结果：
  Tests run: 86
  Failures: 0
  Errors: 31   ← 全是集成测试，因远程 MySQL 不可用
  Success: 55  ← 单元测试全过
```

31 个错误全部来自 `*IntegrationTest`，根本原因只有一个：`application-test.yml` 配置的远程数据库 `182.92.176.70:3306` 不可用。这不是代码问题，是环境问题。

但每次跑 `mvn test` 都会看到 BUILD FAILURE，你的第一反应是"又坏了"——这消耗你的信任和精力。

### 1.3 为什么 AI 审查找不到 bug

当你说"这段代码有问题，帮我看看"时，AI 面临两个困境：

**困境一：context 锚定。** 如果当前 AI 刚刚写了这段代码，它对自己的输出有"路径依赖"——它会沿着自己写代码时的逻辑再走一遍，难以跳出框架发现问题。这就像自己 review 自己的代码，很难看到盲点。

**困境二：探索成本。** 如果是在新 session 里审查看一段陌生代码，AI 需要从头理解整个调用链。没有明确的"怀疑范围"，它可能花大量时间在不相关的文件上。

---

## 2. Session 管理：每次对话的正确姿势

### 2.1 核心原则

```
一个 session = 可独立验证的原子变更
           ≠ "尽可能多做"
```

具体来说：
- **一个 session 改 2-3 个文件，不超过 1 个模块**
- **一个 session 只改后端或只改前端，不要跨层**
- **一个 session 结束时必须能跑通所有相关测试**

### 2.2 启动清单（每次开始 Claude Code 前必做）

```
□ 明确告诉 AI 当前分支和已完成的工作
□ 引用相关的计划文件（不超过 1 个）
□ 引用 CLAUDE.md 中的相关章节
□ 说清楚"做完后怎么验证"
```

**推荐话术模板：**

```
当前分支: feat/region-roomtype-refactor
已完成: Task 1-5（后端 Region/RoomType 实体、Mapper、Service、Controller、DataInitializer）
当前任务: Task 6 — 修改 Property.java 实体，把 district/roomType 替换为 regionId/provinceId/roomTypeId

关键约定（来自 CLAUDE.md）:
- unitPrice 由 setter 自动计算: price / area
- Service 层更新时先将 unitPrice 置 null，有条件地重新计算

验证方式: 改完后运行 mvn compile 确认编译通过
```

### 2.3 当前分支的 session 拆分方案

以你现在的 `feat/region-roomtype-refactor` 分支为例，剩余的 16 个 task 分拆如下：

**Session 1：后端核心改造（Task 6→8→9→10→11）**

```
涉及文件:
  - Property.java（修改实体）
  - PropertyServiceImpl.java（修改 search/create/update）
  - CustomerPropertyController.java（修改接口签名）
  - CustomerProfile.java（加 regionId）
  - Suggestion.java / Service / Controller（desiredType → roomTypeId）
  - PropertyMapper.java（清理旧 dict 方法）

验证:
  mvn compile && mvn test -Dtest='*Test,!*IntegrationTest'
```

注意：这是最大的一次 session，因为 5 个 task 紧密耦合。如果发现 AI 开始出错，就拆成两轮：先 Task 6→8（Property），再 Task 9→10→11（CustomerProfile + Suggestion + 清理）。

**Session 2：客户端前端（Task 12→13→14→15→16→17→18）**

```
涉及文件:
  - fangdichan-client/src/api/region.js（新增）
  - fangdichan-client/src/api/room-type.js（新增）
  - fangdichan-client/src/mocks/handlers.js（修改）
  - Search.vue、Home.vue、Profile.vue、Suggestion.vue、PropertyDetail.vue
  - 删除 dict.js、更新 constants.js、更新 api-contract.test.js

验证:
  cd fangdichan-client && npx vitest run
```

⚠️ 这里最好再拆：先 Task 12（API 模块 + MSW），验证通过后，再做 Task 13-17（视图组件）。

**Session 3：管理端前端（Task 19→20→21）**

```
涉及文件:
  - fangdichan-admin-web/src/api/region.js（新增）
  - fangdichan-admin-web/src/api/room-type.js（新增）
  - fangdichan-admin-web/src/mocks/handlers.js（修改）
  - PropertyManagement.vue（修改）
  - 删除 dict.js、更新 api-contract.test.js

验证:
  cd fangdichan-admin-web && npx vitest run
```

### 2.4 Session Checkpoint 模板

每次 session 结束时，在 CLAUDE.md 底部追加：

```markdown
## Session Checkpoint

当前分支: feat/region-roomtype-refactor
已完成:
- Task 6: Property.java 字段变更
- Task 7: PropertyService.search() 新签名 + regionMapper 注入
下一个: Task 8 — province_id 自动派生
已知问题: PropertyServiceImplTest 已被删除，新测试在 Task 8 后写
```

下次开始新 session 时，AI 读这个就能瞬间恢复上下文。

---

## 3. 验证体系：在 bug 到达你之前截住它

### 3.1 分离集成测试和单元测试

当前问题：`mvn test` 跑全部测试，31 个集成测试因远程 MySQL 不可用而报错，导致 BUILD FAILURE。

**修复方案：**

第一步，在 `fangdichan-server/pom.xml` 的 `<properties>` 中添加：

```xml
<skipIntegrationTests>true</skipIntegrationTests>
```

第二步，运行测试时：

```bash
# 日常开发——只跑单元测试（不需要数据库）
mvn test -Dtest='*Test,!*IntegrationTest'

# 需要验证集成时（需要有网络）
mvn test -Dtest='*IntegrationTest' -DskipIntegrationTests=false
```

第三步（推荐但可选），配置 H2 内存数据库让集成测试离线可跑：

在 `src/test/resources/application-test.yml` 中添加 H2 配置：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL
    driver-class-name: org.h2.Driver
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

这需要把建表语句提取到 `src/test/resources/schema.sql`。投入大约 30 分钟，换来的是集成测试在任何环境都能跑。

### 3.2 Pre-commit 验证脚本

在项目根目录创建 `scripts/verify.sh`：

```bash
#!/bin/bash
set -e

echo "=== 1. 后端编译 ==="
cd fangdichan-server && mvn compile -q

echo "=== 2. 后端单元测试 ==="
mvn test -Dtest='*Test,!*IntegrationTest' -q

echo "=== 3. 客户端测试 ==="
cd ../fangdichan-client && npx vitest run

echo "=== 4. 管理端测试 ==="
cd ../fangdichan-admin-web && npx vitest run

echo "=== 5. Lint ==="
cd .. && npm run lint

echo ""
echo "✅ 全部验证通过"
```

然后在 `package.json` 中添加：

```json
{
  "scripts": {
    "verify": "bash scripts/verify.sh"
  }
}
```

以后提交前只需：

```bash
npm run verify
```

如果某个环节失败了，你能立刻知道，不需要等 AI 把代码全部写完才发现问题。

### 3.3 契约测试策略

不是所有 API 都需要写契约测试。以下情况**必须**加：

| 场景 | 测试重点 | 示例 |
|------|---------|------|
| 新增 API 端点 | 响应结构 + 状态码 | `GET /api/public/region/provinces` |
| 修改响应字段 | 新旧字段都存在/不存在 | district → regionId |
| 修改请求参数 | 参数名、类型、是否必填 | district → regionId |
| 筛选条件变更 | 参数正确传递到后端 | bedroomCount 作为 query param |

**具体做法：**

在 `api-contract.test.js` 中，不 mock 数据，而是验证结构：

```javascript
it('GET /api/public/region/provinces 返回正确的结构', async () => {
  const res = await request.get('/public/region/provinces')
  expect(res.data.code).toBe(200)
  expect(Array.isArray(res.data.data)).toBe(true)
  if (res.data.data.length > 0) {
    const item = res.data.data[0]
    expect(item).toHaveProperty('id')
    expect(item).toHaveProperty('name')
    expect(item).toHaveProperty('level')
    expect(item).toHaveProperty('type')
    // 故意不断言 parentName —— 它在 search 接口才有
  }
})
```

这样当 AI 改了后端 VO 结构但没更新前端时，测试直接报错。

---

## 4. 审查协议：让 AI 帮你定位问题的标准流程

### 4.1 问题报告四要素

下一次发现问题时，用这个格式告诉 AI：

```
文件: fangdichan-client/src/views/search/Search.vue:45
现象: 搜索页面的省份下拉框是空的，没有加载任何省份
预期: 应该显示省份列表（北京市、广东省、上海市...）
怀疑范围:
  - 可能是 onMounted 没有调用 getProvinces()
  - 可能是 getProvinces API 路径错了
  - 可能是 MSW handler 没有匹配到请求
```

对比一下两种问法的效果：

```
❌ "搜索页下拉框是空的，帮我看看怎么回事"
   → AI 需要从头探索，可能花 5 分钟看路由、看组件、看 API、看 MSW

✅ 按四要素描述
   → AI 直接 focus 在 onMounted、API 调用、MSW handler 三个点，2 分钟定位
```

### 4.2 二分法排查决策树

发现问题时，按这个顺序定位：

```
问题出现了
│
├─ 页面能加载吗？
│  ├─ 不能 → 检查路由 + 组件是否注册 + JS 报错
│  └─ 能 → 数据对吗？
│       ├─ 不对
│       │  ├─ 前端发出请求了吗？（浏览器 Network tab 检查）
│       │  │  ├─ 没发出 → 检查 API 函数是否被调用
│       │  │  └─ 发出了
│       │  │     ├─ 返回 200 但数据不对 → 检查 MSW handler
│       │  │     └─ 返回 4xx/5xx → 检查后端接口路径和参数
│       │  └─ 后端返回数据对但前端显示不对 → 检查组件的 template/data 绑定
│       └─ 对 → 问题不在这个模块，查其他模块
```

让 AI 按这个决策树走，而不是随机探索。

### 4.3 Test-to-Fail 模式

发现 bug 时，不要直接让 AI 修复。改为两步：

**第一步：写一个会暴露 bug 的测试**

```
"PropertyServiceImpl 的 search() 方法，传入 regionId=3（朝阳区，level=2）时，
应该按 regionId 精确匹配。当前代码可能用了 provinceId 来匹配。
请先写一个测试验证这个行为，确认测试会失败。"
```

**第二步：修复代码让测试通过**

```
"现在修复 search() 方法，让刚才的测试通过。
修复后跑全部单元测试确保没有回归。"
```

**为什么这样更有效？**
- 测试失败 = bug 可复现（排除环境干扰）
- 测试通过 = bug 已修复（不是"貌似好了"）
- 测试保留 = 永久防回归（下次 AI 不会再次引入同一个 bug）

### 4.4 第二意见机制

当当前 AI 找不到问题原因时：

```
启动一个新 Agent，专门审查代码。

"请审查以下问题，带着'新鲜的眼睛'独立分析：

问题: Property 详情页显示的区域路径不对，
预期显示 '北京市 / 朝阳区'，实际显示空字符串

怀疑文件:
  - fangdichan-client/src/views/detail/PropertyDetail.vue
  - fangdichan-client/src/api/region.js
  - fangdichan-client/src/mocks/handlers.js

请独立分析，不要受之前判断的影响。"
```

新 Agent 没有之前的 context 锚定，常常能发现当前 AI 忽略的问题。

---

## 5. 速查清单

### 开始新 Session 前

```
□ 当前分支名是什么？
□ 上次做到了哪里？（检查 CLAUDE.md 的 Session Checkpoint）
□ 这次要改哪些文件？（列出来，不超过 5 个）
□ 改完之后怎么验证？（具体命令）
□ 引用了哪个计划/规范文档？（不超过 1 个）
```

### 执行中

```
□ 改完一个 task 就编译/跑测试，不要攒到一起
□ 发现 AI 写的不对 → 立即纠正，不要将就
□ 如果 AI 开始"转圈"（反复改同一段代码）→ 停止，重新描述需求
```

### 完成后

```
□ 确认你要求的改动全部做了
□ 跑一遍验证：npm run verify（或者分步跑）
□ 更新 CLAUDE.md 的 Session Checkpoint
□ 写 commit message（conventional commits）
```

### 发现问题时

```
□ 按四要素描述：文件 / 现象 / 预期 / 怀疑范围
□ 先确定问题在哪一层（前端/后端/MSW mock）
□ 再定位到具体文件/函数
□ 先在当前 session 问，不行就开新 Agent
```

---

## 附录：为什么当前方案有效（可选阅读）

如果你好奇这些建议背后的原理：

| 建议 | 应对的 AI 局限 |
|------|---------------|
| 每个 session 只改 2-3 个文件 | Claude 的 context window 有限，文件越多越容易忘记约定 |
| 关键约定写在 CLAUDE.md | AI 的"记忆"来自 system prompt，CLAUDE.md 是最高优先级的上下文 |
| Test-to-fail 先写测试再修代码 | AI 容易"确认偏误"——它倾向于证明自己是对的而不是找到问题 |
| 用新 Agent 做第二意见 | 新 Agent 没有 context 锚定，能跳出之前的思维框架 |
| 每次结束写 checkpoint | Claude 每次启动是"失忆"的，checkpoint 是最低成本恢复上下文的方式 |
| 分离集成测试 | 错误噪音会消耗你对测试结果的信任——"狼来了"效应 |
| Pre-commit 脚本 | 反馈越快，修复成本越低。等 CI 跑完再发现不如本地就发现 |

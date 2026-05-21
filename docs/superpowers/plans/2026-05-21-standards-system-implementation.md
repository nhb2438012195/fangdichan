# 研发规范体系建立 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为房地产购房查询系统建立完整的研发规范体系，包含前端测试框架强化、前后端接口契约对齐、代码检查和流程工具链、以及四类规范文档。

**Architecture:** 采用"工具先行"策略，分 6 个 Phase 渐进式落地。先搭建可执行的工具链（ESLint/Prettier/Checkstyle/PMD/Husky/CI/OpenAPI/MSW），再补规范文档，最后提升测试覆盖。每个 Phase 产出可验证的结果。

**Tech Stack:**
- 前端工具：ESLint + Prettier + Husky + lint-staged + commitlint + Vitest + @vue/test-utils + happy-dom + MSW
- 后端工具：Checkstyle + PMD + SpringDoc OpenAPI
- CI：GitHub Actions
- 规范文档：Markdown 格式，存放在 `docs/superpowers/standards/`

---

## Pre-Phase: 根级别基础配置

### Task 0-1: 创建根级别公共配置

**Files:**
- Create: `.editorconfig`
- Create: `.husky/pre-commit`
- Create: `.husky/commit-msg`
- Create: `commitlint.config.cjs`

- [ ] **Step 1: 创建 .editorconfig**

```ini
# .editorconfig
root = true

[*]
indent_style = space
indent_size = 2
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true

[*.java]
indent_size = 4

[*.{xml,yml,yaml}]
indent_size = 2

[Makefile]
indent_style = tab
```

- [ ] **Step 2: 初始化 Husky 并创建 pre-commit hook**

```bash
cd /home/nhb/project/ruanjiangongcheng
npx husky init
```

写入 `.husky/pre-commit`:

```bash
npx lint-staged
```

- [ ] **Step 3: 创建 commitlint 配置**

```cjs
// commitlint.config.cjs
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [2, 'always', [
      'feat', 'fix', 'docs', 'style', 'refactor',
      'perf', 'test', 'chore', 'ci'
    ]],
    'subject-case': [2, 'always', 'lower-case'],
    'subject-max-length': [2, 'always', 72]
  }
};
```

- [ ] **Step 4: 创建 commit-msg hook**

写入 `.husky/commit-msg`:

```bash
npx --no -- commitlint --edit $1
```

- [ ] **Step 5: 安装依赖**

```bash
cd /home/nhb/project/ruanjiangongcheng
npm install --save-dev husky lint-staged @commitlint/cli @commitlint/config-conventional
```

- [ ] **Step 6: 验证**

```bash
npx husky --version
npx commitlint --version
```

---

## Phase 1: 前端测试框架强化

### Task 1: admin-web 测试框架配置

**Files:**
- Modify: `fangdichan-admin-web/vite.config.js`
- Create: `fangdichan-admin-web/src/test-setup.js`
- Create: `fangdichan-admin-web/vitest.config.js`

- [ ] **Step 1: 安装测试依赖**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npm install --save-dev @vue/test-utils happy-dom @vitejs/plugin-vue-jsx
```

- [ ] **Step 2: 创建 vitest.config.js**

```js
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'happy-dom',
    setupFiles: ['./src/test-setup.js'],
    include: ['src/**/*.{test,spec}.{js,jsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      include: ['src/views/**/*.vue', 'src/api/**/*.js', 'src/store/**/*.js'],
      exclude: ['src/**/*.{test,spec}.js']
    }
  }
})
```

- [ ] **Step 3: 创建测试入口文件**

```js
// fangdichan-admin-web/src/test-setup.js
import { vi } from 'vitest'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn() },
  default: { install: vi.fn() }
}))
```

- [ ] **Step 4: 更新 package.json 添加测试脚本**

修改 `fangdichan-admin-web/package.json` 中的 scripts：

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "test": "vitest run",
    "test:watch": "vitest",
    "test:coverage": "vitest run --coverage"
  }
}
```

- [ ] **Step 5: 验证测试框架能运行**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npx vitest run --reporter=verbose
```

预期：已有测试用例全部 PASS。

- [ ] **Step 6: 创建 lint-staged 配置**

在 `fangdichan-admin-web/package.json` 中添加：

```json
{
  "lint-staged": {
    "src/**/*.{js,vue}": [
      "eslint --fix",
      "prettier --write"
    ],
    "src/**/*.css": [
      "prettier --write"
    ]
  }
}
```

- [ ] **Step 7: Commit**

```bash
git add fangdichan-admin-web/vite.config.js \
       fangdichan-admin-web/vitest.config.js \
       fangdichan-admin-web/src/test-setup.js \
       fangdichan-admin-web/package.json
git commit -m "test: add vitest config with vue-test-utils and happy-dom for admin-web"
```

---

### Task 2: client 测试框架初始化

**Files:**
- Modify: `fangdichan-client/vite.config.js`
- Create: `fangdichan-client/src/test-setup.js`
- Create: `fangdichan-client/vitest.config.js`

- [ ] **Step 1: 安装测试依赖**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-client
npm install --save-dev vitest @vue/test-utils happy-dom @vitejs/plugin-vue-jsx
```

- [ ] **Step 2: 创建 vitest.config.js**（内容同 Task 1 Step 2，路径为 client 目录）

- [ ] **Step 3: 创建测试入口文件**（同 Task 1 Step 3）

- [ ] **Step 4: 更新 package.json 添加测试脚本**

```json
{
  "scripts": {
    "test": "vitest run",
    "test:watch": "vitest",
    "test:coverage": "vitest run --coverage"
  }
}
```

- [ ] **Step 5: 验证测试框架**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-client
npx vitest run --reporter=verbose
```

预期：No test files found （因为还没有测试用例），退出码 0。

- [ ] **Step 6: 添加 lint-staged 配置**

在 `fangdichan-client/package.json` 中添加：

```json
{
  "lint-staged": {
    "src/**/*.{js,vue}": [
      "eslint --fix",
      "prettier --write"
    ]
  }
}
```

- [ ] **Step 7: Commit**

```bash
git add fangdichan-client/vite.config.js \
       fangdichan-client/vitest.config.js \
       fangdichan-client/src/test-setup.js \
       fangdichan-client/package.json
git commit -m "test: add vitest config with vue-test-utils and happy-dom for client"
```

---

### Task 3: admin-web ESLint + Prettier 配置

**Files:**
- Create: `fangdichan-admin-web/.eslintrc.cjs`
- Create: `fangdichan-admin-web/.prettierrc`

- [ ] **Step 1: 安装依赖**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npm install --save-dev eslint prettier eslint-plugin-vue eslint-config-prettier @vue/eslint-config-prettier
```

- [ ] **Step 2: 创建 ESLint 配置**

```cjs
// fangdichan-admin-web/.eslintrc.cjs
module.exports = {
  root: true,
  env: {
    browser: true,
    es2022: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended',
    '@vue/eslint-config-prettier'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  rules: {
    'vue/multi-word-component-names': 'off',
    'vue/no-unused-vars': ['error', { ignorePattern: '^_' }],
    'no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    'no-console': ['warn', { allow: ['warn', 'error'] }],
    'prefer-const': 'error',
    'no-var': 'error'
  },
  globals: {
    ElMessage: 'readonly',
    ElMessageBox: 'readonly'
  }
}
```

- [ ] **Step 3: 创建 Prettier 配置**

```json
{
  "semi": false,
  "singleQuote": true,
  "trailingComma": "none",
  "printWidth": 100,
  "tabWidth": 2,
  "arrowParens": "always"
}
```

- [ ] **Step 4: 添加 ESLint 脚本到 package.json**

```json
{
  "scripts": {
    "lint": "eslint src --ext .js,.vue --fix",
    "format": "prettier --write src"
  }
}
```

- [ ] **Step 5: 验证 ESLint 能运行**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npx eslint src --ext .js,.vue
```

- [ ] **Step 6: Commit**

```bash
git add fangdichan-admin-web/.eslintrc.cjs \
       fangdichan-admin-web/.prettierrc \
       fangdichan-admin-web/package.json
git commit -m "style: add ESLint and Prettier config for admin-web"
```

---

### Task 4: client ESLint + Prettier 配置

**Files:**
- Create: `fangdichan-client/.eslintrc.cjs`
- Create: `fangdichan-client/.prettierrc`

步骤同 Task 3，路径换为 client 目录。ESLint 配置额外添加 Electron 全局变量：

```cjs
// fangdichan-client/.eslintrc.cjs
module.exports = {
  // ... 同 Task 3
  env: {
    browser: true,
    es2022: true,
    node: true
  },
  globals: {
    electron: 'readonly'
  }
}
```

- [ ] **Step 1: 安装依赖并创建配置**
- [ ] **Step 2: 验证并 Commit**

```bash
git add fangdichan-client/.eslintrc.cjs \
       fangdichan-client/.prettierrc \
       fangdichan-client/package.json
git commit -m "style: add ESLint and Prettier config for client"
```

---

## Phase 2: 后端代码检查

### Task 5: Maven Checkstyle 集成

**Files:**
- Create: `fangdichan-server/checkstyle.xml`
- Modify: `fangdichan-server/pom.xml`

- [ ] **Step 1: 创建 Checkstyle 规则文件**

```xml
<!-- fangdichan-server/checkstyle.xml -->
<!DOCTYPE module PUBLIC
  "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
  "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
  <property name="charset" value="UTF-8"/>
  <property name="severity" value="warning"/>

  <module name="TreeWalker">
    <!-- 命名规范 -->
    <module name="LocalVariableName"/>
    <module name="MemberName">
      <property name="format" value="^[a-z][a-zA-Z0-9]*$"/>
    </module>
    <module name="MethodName">
      <property name="format" value="^[a-z][a-zA-Z0-9]*$"/>
    </module>
    <module name="ClassTypeParameterName"/>
    <module name="MethodTypeParameterName"/>
    <module name="InterfaceTypeParameterName"/>
    <module name="ConstantName"/>

    <!-- 代码结构 -->
    <module name="EmptyBlock">
      <property name="option" value="text"/>
    </module>
    <module name="LeftCurly"/>
    <module name="RightCurly"/>
    <module name="NeedBraces"/>
    <module name="AvoidNestedBlocks"/>

    <!-- 导入检查 -->
    <module name="UnusedImports"/>
    <module name="RedundantImport"/>

    <!-- 代码质量 -->
    <module name="EmptyStatement"/>
    <module name="EqualsHashCode"/>
    <module name="IllegalInstantiation"/>
    <module name="SimplifyBooleanExpression"/>
    <module name="SimplifyBooleanReturn"/>
    <module name="StringLiteralEquality"/>
    <module name="UnnecessaryParentheses"/>
    <module name="OneStatementPerLine"/>
    <module name="MultipleVariableDeclarations"/>
  </module>
</module>
```

- [ ] **Step 2: 在 pom.xml 中添加 Checkstyle 插件**

```xml
<!-- pom.xml <build><plugins> 内部 -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <version>3.3.1</version>
  <configuration>
    <configLocation>checkstyle.xml</configLocation>
    <failOnViolation>false</failOnViolation>
    <violationSeverity>warning</violationSeverity>
  </configuration>
  <executions>
    <execution>
      <goals><goal>check</goal></goals>
      <phase>validate</phase>
    </execution>
  </executions>
</plugin>
```

- [ ] **Step 3: 验证**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-server
mvn validate -q
```

- [ ] **Step 4: Commit**

```bash
git add fangdichan-server/checkstyle.xml fangdichan-server/pom.xml
git commit -m "style: add Checkstyle with naming and code structure rules"
```

---

### Task 6: Maven PMD 集成

**Files:**
- Create: `fangdichan-server/pmd-rules.xml`
- Modify: `fangdichan-server/pom.xml`

- [ ] **Step 1: 创建 PMD 规则文件**

```xml
<!-- fangdichan-server/pmd-rules.xml -->
<?xml version="1.0"?>
<ruleset name="Custom Rules"
  xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.net/ruleset_2_0_0.xsd">

  <description>Custom PMD rules for fangdichan-server</description>

  <rule ref="category/java/bestpractices.xml">
    <exclude name="JUnitTestContainsTooManyAsserts"/>
    <exclude name="JUnitAssertionsShouldIncludeMessage"/>
    <exclude name="GuardLogStatement"/>
  </rule>

  <rule ref="category/java/errorprone.xml">
    <exclude name="DataflowAnomalyAnalysis"/>
    <exclude name="BeanMembersShouldSerialize"/>
  </rule>

  <rule ref="category/java/performance.xml">
    <exclude name="AvoidInstantiatingObjectsInLoops"/>
  </rule>
</ruleset>
```

- [ ] **Step 2: 在 pom.xml 中添加 PMD 插件**

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-pmd-plugin</artifactId>
  <version>3.21.2</version>
  <configuration>
    <rulesets>
      <ruleset>pmd-rules.xml</ruleset>
    </rulesets>
    <failOnViolation>false</failOnViolation>
    <printFailingErrors>true</printFailingErrors>
  </configuration>
  <executions>
    <execution>
      <goals><goal>check</goal></goals>
      <phase>validate</phase>
    </execution>
  </executions>
</plugin>
```

- [ ] **Step 3: 验证**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-server
mvn validate
```

- [ ] **Step 4: Commit**

```bash
git add fangdichan-server/pmd-rules.xml fangdichan-server/pom.xml
git commit -m "style: add PMD with best practices and error-prone rules"
```

---

## Phase 3: API 契约对齐

### Task 7: SpringDoc OpenAPI 集成

**Files:**
- Modify: `fangdichan-server/pom.xml`
- Modify: `fangdichan-server/src/main/resources/application.yml`

- [ ] **Step 1: 添加 SpringDoc 依赖**

```xml
<!-- pom.xml <dependencies> 内部 -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.3.0</version>
</dependency>
```

- [ ] **Step 2: 配置 SpringDoc**

```yaml
# application.yml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
  show-actuator: false
  default-produces-media-type: application/json
  paths-to-match:
    - /api/**
```

- [ ] **Step 3: 启动后端验证**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-server
# 编译后启动
mvn compile -q
```

验证：访问 `http://localhost:8080/swagger-ui.html` 能看到 API 文档页面。

- [ ] **Step 4: 导出 OpenAPI 规范文件供前端使用**

```bash
curl -o ../fangdichan-admin-web/openapi.json http://localhost:8080/api-docs
```

- [ ] **Step 5: Commit**

```bash
git add fangdichan-server/pom.xml \
       fangdichan-server/src/main/resources/application.yml
git commit -m "feat: add SpringDoc OpenAPI for API contract generation"
```

---

### Task 8: 前端 MSW (Mock Service Worker) 设置 + 契约测试

**Files:**
- Create: `fangdichan-admin-web/src/mocks/handlers.js`
- Create: `fangdichan-admin-web/src/mocks/server.js`
- Create: `fangdichan-admin-web/src/api/__tests__/api-contract.test.js`
- Create: `fangdichan-client/src/mocks/handlers.js`
- Create: `fangdichan-client/src/mocks/server.js`
- Create: `fangdichan-client/src/api/__tests__/api-contract.test.js`

- [ ] **Step 1: 安装 MSW**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npm install --save-dev msw
```

- [ ] **Step 2: 创建 admin-web MSW handlers**

```js
// fangdichan-admin-web/src/mocks/handlers.js
import { http, HttpResponse } from 'msw'

const BASE = '/api'

export const handlers = [
  // 登录
  http.post(`${BASE}/public/login`, async ({ request }) => {
    const body = await request.json()
    if (body.username === 'admin' && body.password === 'admin123') {
      return HttpResponse.json({
        code: 200,
        msg: 'success',
        data: { token: 'mock-jwt-token', role: 'ADMIN', username: 'admin' }
      })
    }
    return HttpResponse.json({ code: 401, msg: '用户名或密码错误', data: null }, { status: 401 })
  }),

  // 房源分页查询（管理员审核用）
  http.get(`${BASE}/admin/property/page`, ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const size = parseInt(url.searchParams.get('size') || '10')
    const list = Array.from({ length: size }, (_, i) => ({
      id: (page - 1) * size + i + 1,
      title: `测试房源${(page - 1) * size + i + 1}`,
      district: '朝阳区',
      roomType: '三室两厅',
      area: 120.5,
      price: 5000000.00,
      status: 'PENDING',
      companyName: '测试房产公司'
    }))
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: { list, total: 50, page, size }
    })
  }),

  // 用户管理分页
  http.get(`${BASE}/admin/user/page`, ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const size = parseInt(url.searchParams.get('size') || '10')
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        list: [
          { id: 1, username: 'admin', role: 'ADMIN', status: 1, createdAt: '2026-01-01T00:00:00' },
          { id: 2, username: 'agent1', role: 'AGENT', status: 1, createdAt: '2026-01-02T00:00:00' },
          { id: 3, username: 'customer1', role: 'CUSTOMER', status: 1, createdAt: '2026-01-03T00:00:00' }
        ],
        total: 3,
        page,
        size
      }
    })
  }),

  // 公司信息
  http.get(`${BASE}/agent/company/info`, () => {
    return HttpResponse.json({
      code: 200,
      msg: 'success',
      data: {
        id: 1,
        companyName: '测试房产公司',
        address: '北京市朝阳区测试路100号',
        contactPhone: '010-88888888',
        description: '一家专业的房地产公司'
      }
    })
  })
]
```

- [ ] **Step 3: 创建 MSW server**

```js
// fangdichan-admin-web/src/mocks/server.js
import { setupServer } from 'msw/node'
import { handlers } from './handlers'

export const server = setupServer(...handlers)
```

- [ ] **Step 4: 更新 test-setup.js 集成 MSW**

```js
// fangdichan-admin-web/src/test-setup.js
import { vi, beforeAll, afterAll, afterEach } from 'vitest'
import { server } from './mocks/server'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn(() => Promise.resolve()) },
  default: { install: vi.fn() }
}))

// 集成 MSW
beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())
```

- [ ] **Step 5: 创建 API 契约测试**

```js
// fangdichan-admin-web/src/api/__tests__/api-contract.test.js
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { describe, it, expect, afterEach } from 'vitest'
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
  headers: { Authorization: 'Bearer mock-token' }
})

// 请求拦截器：注入 Content-Type
request.interceptors.request.use(config => {
  config.headers['Content-Type'] = 'application/json'
  return config
})

describe('API Contract: PageResult 数据格式', () => {
  afterEach(() => server.resetHandlers())

  it('GET /api/admin/property/page 应该返回 { list, total, page, size }', async () => {
    const res = await request.get('/admin/property/page', {
      params: { page: 1, size: 10, status: 'PENDING' }
    })
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('list')
    expect(res.data.data).toHaveProperty('total')
    expect(res.data.data).toHaveProperty('page')
    expect(res.data.data).toHaveProperty('size')
    expect(Array.isArray(res.data.data.list)).toBe(true)
  })

  it('GET /api/admin/user/page 应该返回标准 PageResult', async () => {
    const res = await request.get('/admin/user/page', {
      params: { page: 1, size: 10 }
    })
    expect(res.data.code).toBe(200)
    expect(res.data.data.list).toBeDefined()
    expect(res.data.data.total).toBeDefined()
  })

  it('GET /api/agent/company/info 返回公司信息', async () => {
    const res = await request.get('/agent/company/info')
    expect(res.data.code).toBe(200)
    expect(res.data.data).toHaveProperty('companyName')
    expect(res.data.data).toHaveProperty('address')
    expect(res.data.data).toHaveProperty('contactPhone')
  })
})
```

- [ ] **Step 6: 运行契约测试验证**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npx vitest run src/api/__tests__/api-contract.test.js --reporter=verbose
```

预期：所有测试 PASS。

- [ ] **Step 7: client 端重复 Step 1-6**（路径换成 client 目录，handlers 添加客户端的 API）

client 端需要额外添加的 handlers：

```js
// 房源搜索
http.get(`${BASE}/customer/property/page`, ({ request }) => {
  const url = new URL(request.url)
  const list = [
    { id: 1, title: '朝阳区精装三居室', district: '朝阳区', roomType: '三室两厅',
      area: 120, price: 5000000, unitPrice: 41666, isVacant: true },
    { id: 2, title: '海淀区学区两居室', district: '海淀区', roomType: '两室一厅',
      area: 85, price: 3500000, unitPrice: 41176, isVacant: true }
  ]
  return HttpResponse.json({
    code: 200, msg: 'success',
    data: { list, total: list.length, page: 1, size: 10 }
  })
}),

// 房源详情
http.get(`${BASE}/customer/property/:id`, ({ params }) => {
  return HttpResponse.json({
    code: 200, msg: 'success',
    data: {
      id: parseInt(params.id), title: '朝阳区精装三居室',
      district: '朝阳区', roomType: '三室两厅',
      area: 120, price: 5000000, unitPrice: 41666,
      floor: '7-10F', floorTotal: 18, isVacant: true,
      status: 'APPROVED', description: '精装修，南北通透',
      images: [{ id: 1, imageUrl: 'http://example.com/img1.jpg', sortOrder: 0 }],
      companyName: '测试房产公司'
    }
  })
}),

// 收藏列表
http.get(`${BASE}/customer/favorite/list`, () => {
  return HttpResponse.json({
    code: 200, msg: 'success',
    data: [{ id: 1, propertyId: 1, title: '朝阳区精装三居室',
      price: 5000000, district: '朝阳区', createdAt: '2026-05-01T10:00:00' }]
  })
})
```

- [ ] **Step 8: Commit**

```bash
git add fangdichan-admin-web/src/mocks/ \
       fangdichan-admin-web/src/api/__tests__/ \
       fangdichan-admin-web/src/test-setup.js \
       fangdichan-client/src/mocks/ \
       fangdichan-client/src/api/__tests__/ \
       fangdichan-client/src/test-setup.js \
       fangdichan-client/src/mocks/server.js
git commit -m "test: add MSW API mock handlers and contract tests for both frontends"
```

---

## Phase 4: CI 流程规范

### Task 9: GitHub Actions CI 配置

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: 创建 CI 配置文件**

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'
      - name: Run Backend Checks
        run: |
          cd fangdichan-server
          mvn validate -q
          mvn test -q

  admin-web:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: fangdichan-admin-web
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: fangdichan-admin-web/package-lock.json
      - run: npm ci
      - run: npm run lint
      - run: npm test

  client:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: fangdichan-client
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: fangdichan-client/package-lock.json
      - run: npm ci
      - run: npm test
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: add GitHub Actions CI with backend and frontend checks"
```

---

### Task 10: 根级别 lint-staged 整合

**Files:**
- Modify: `package.json`（根目录）

- [ ] **Step 1: 创建根 package.json 的 lint-staged**

```json
{
  "scripts": {
    "lint": "npm run lint --workspace=fangdichan-admin-web && npm run lint --workspace=fangdichan-client",
    "test": "npm test --workspaces"
  },
  "lint-staged": {}
}
```

由于两个前端项目各自有 lint-staged 配置，根级别不需要额外配置。

- [ ] **Step 2: Commit**

```bash
git add package.json
git commit -m "chore: add root-level workspace scripts"
```

---

## Phase 5: 规范文档

### Task 11: 代码规范文档

**Files:**
- Create: `docs/superpowers/standards/01-code-standards.md`

- [ ] **Step 1: 编写文档**

```markdown
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
```

- [ ] **Step 2: Commit**

```bash
git add docs/superpowers/standards/01-code-standards.md
git commit -m "docs: add code standards documentation"
```

---

### Task 12: 架构规范文档

**Files:**
- Create: `docs/superpowers/standards/02-architecture-standards.md`

- [ ] **Step 1: 编写文档**

```markdown
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
```

- [ ] **Step 2: Commit**

```bash
git add docs/superpowers/standards/02-architecture-standards.md
git commit -m "docs: add architecture standards documentation"
```

---

### Task 13: 流程规范文档

**Files:**
- Create: `docs/superpowers/standards/03-process-standards.md`

- [ ] **Step 1: 编写文档**

```markdown
# 流程规范

## 分支策略
- `main` — 生产分支，只接受 PR merge
- `develop` — 开发主干，功能分支 merge 目标
- `feat/<name>` — 功能分支，从 develop 创建，完成后 PR → develop
- `fix/<name>` — 修复分支，从 develop 或 main 创建

## Commit 规范 (Conventional Commits)

```
<type>: <简短描述>

<可选详细描述>
```

### 类型

| 类型 | 用途 |
|------|------|
| feat | 新功能 |
| fix | 修复 bug |
| docs | 文档变更 |
| style | 代码格式（不影响功能）|
| refactor | 重构 |
| perf | 性能优化 |
| test | 添加测试 |
| chore | 构建/工具变更 |
| ci | CI 配置变更 |

### 示例
```
feat: add property search by district filter
fix: handle empty PageResult list in property table
test: add contract tests for API data format
```

## Code Review 规范
1. 每个 PR 至少 1 人 Review 通过方可 merge
2. 检查重点：逻辑正确性、边界条件、API 契约一致性、测试覆盖
3. 禁止在未 Review 的情况下直接 push 到 develop/main
4. Review 时发现契约测试需要更新 → 必须同步更新

## 发布流程
1. develop 分支通过 CI 全部检测
2. PR develop → main，Review 通过后 merge
3. main 分支 CI 自动运行
4. 从 main 打 tag：`v<major>.<minor>.<patch>`

## 测试要求
- 新功能必须包含测试
- Controller 修改 → 更新契约测试
- Service 修改 → 至少 1 个正常路径 + 1 个异常路径测试
- 前端组件修改 → 更新组件测试
- CI 中测试失败等同于代码不能 merge
```

- [ ] **Step 2: Commit**

```bash
git add docs/superpowers/standards/03-process-standards.md
git commit -m "docs: add process standards documentation"
```

---

### Task 14: UI 规范文档

**Files:**
- Create: `docs/superpowers/standards/04-ui-standards.md`

- [ ] **Step 1: 编写文档**

```markdown
# UI 规范

## admin-web（Element Plus）
- 布局：侧边栏 + 顶部栏 + 主内容区
- 列表页统一使用 el-table + el-pagination
- 表单页统一使用 el-form + el-input/el-select
- 操作按钮统一使用 el-button（颜色区分：primary/成功/danger/warning）
- 弹窗使用 el-dialog，确认使用 ElMessageBox.confirm

## client（桌面风格）
- 布局：左侧图标导航 + 工具栏 + 卡片网格
- 房源卡片统一使用 el-card，展示缩略图 + 标题 + 价格 + 标签
- 搜索表单统一使用 el-form + inline 布局

## 通用规范
- 加载状态：表格统一使用 v-loading，按钮用 v-loading
- 空状态：列表为空时显示 el-empty
- 错误处理：API 调用失败统一使用 ElMessage.error
- 操作成功：统一使用 ElMessage.success
- 删除/取消操作：必须二次确认
- 分页参数统一：pageNum/pageSize（后端），page/size（前端传参）
```

- [ ] **Step 2: Commit**

```bash
git add docs/superpowers/standards/04-ui-standards.md
git commit -m "docs: add UI standards documentation"
```

---

## Phase 6: 测试覆盖提升

### Task 15: admin-web 核心组件测试

**Files:**
- Create: `fangdichan-admin-web/src/views/login/__tests__/Login.spec.js`
- Create: `fangdichan-admin-web/src/views/property/__tests__/PropertyManagement.spec.js`

- [ ] **Step 1: 编写 Login 组件测试**

```js
// fangdichan-admin-web/src/views/login/__tests__/Login.spec.js
import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'

// Mock axios
vi.mock('axios', () => ({
  default: {
    post: vi.fn(),
    create: vi.fn(() => ({
      post: vi.fn(),
      interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } }
    }))
  }
}))

describe('Login.vue', () => {
  let wrapper

  beforeEach(async () => {
    const Login = await import('../Login.vue')
    const pinia = createPinia()
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/login', name: 'Login', component: Login.default },
        { path: '/', name: 'Dashboard', component: { template: '<div>Dashboard</div>' } }
      ]
    })
    wrapper = mount(Login.default, {
      global: { plugins: [pinia, router] }
    })
  })

  it('should render login form with username, password and submit button', () => {
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('should show error message on empty submit', async () => {
    const button = wrapper.find('button')
    await button.trigger('click')
    // Element Plus form validation should show error
    expect(wrapper.html()).toContain('error')
  })
})
```

- [ ] **Step 2: 运行测试**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npx vitest run src/views/login/__tests__/Login.spec.js --reporter=verbose
```

- [ ] **Step 3: 编写 PropertyManagement 列表页测试**

```js
// fangdichan-admin-web/src/views/property/__tests__/PropertyManagement.spec.js
import { describe, it, expect, vi, beforeEach } from 'vitest'

// 测试 API 调用模式
describe('PropertyManagement - API integration', () => {
  it('should call /api/agent/property/page with correct params', async () => {
    const mockGet = vi.fn().mockResolvedValue({
      data: { code: 200, data: { list: [], total: 0 } }
    })
    const request = { get: mockGet }

    const params = { page: 1, size: 10, status: 'APPROVED', district: '' }
    await request.get('/agent/property/page', { params })

    expect(mockGet).toHaveBeenCalledWith('/agent/property/page', {
      params: { page: 1, size: 10, status: 'APPROVED', district: '' }
    })
  })

  it('should correctly destructure PageResult response', async () => {
    const res = {
      data: { code: 200, msg: 'success', data: { list: [{ id: 1 }], total: 1, page: 1, size: 10 } }
    }
    const { list, total } = res.data.data
    expect(list).toHaveLength(1)
    expect(total).toBe(1)
  })

  it('should handle empty list correctly', async () => {
    const res = { data: { code: 200, msg: 'success', data: { list: [], total: 0, page: 1, size: 10 } } }
    const { list } = res.data.data
    expect(list).toEqual([])
    expect(Array.isArray(list)).toBe(true)
  })
})
```

- [ ] **Step 4: 运行测试**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npx vitest run src/views/property/__tests__/PropertyManagement.spec.js --reporter=verbose
```

- [ ] **Step 5: Commit**

```bash
git add fangdichan-admin-web/src/views/login/__tests__/ \
       fangdichan-admin-web/src/views/property/__tests__/
git commit -m "test: add Login and PropertyManagement component tests for admin-web"
```

---

### Task 16: client 核心组件测试

**Files:**
- Create: `fangdichan-client/src/views/home/__tests__/Home.spec.js`
- Create: `fangdichan-client/src/views/search/__tests__/Search.spec.js`

- [ ] **Step 1: 编写 Search API 测试**

```js
// fangdichan-client/src/views/search/__tests__/Search.spec.js
import { describe, it, expect, vi } from 'vitest'

describe('Search - API integration', () => {
  it('should call /api/customer/property/page with filter params', async () => {
    const mockGet = vi.fn().mockResolvedValue({
      data: { code: 200, data: { list: [], total: 0 } }
    })
    const request = { get: mockGet }
    const filters = {
      district: '朝阳区',
      roomType: '三室两厅',
      priceMin: 3000000,
      priceMax: 8000000,
      page: 1,
      size: 20
    }
    await request.get('/customer/property/page', { params: filters })
    expect(mockGet).toHaveBeenCalledWith('/customer/property/page', {
      params: filters
    })
  })

  it('should handle filters with empty values by not sending them', async () => {
    const mockGet = vi.fn().mockResolvedValue({
      data: { code: 200, data: { list: [], total: 0 } }
    })
    const request = { get: mockGet }
    const filters = { district: '', roomType: '', page: 1, size: 20 }
    const cleanedParams = {}
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) {
        cleanedParams[key] = value
      }
    })
    await request.get('/customer/property/page', { params: cleanedParams })
    expect(mockGet).toHaveBeenCalledWith('/customer/property/page', {
      params: { page: 1, size: 20 }
    })
  })
})
```

- [ ] **Step 2: 运行测试**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-client
npx vitest run src/views/search/__tests__/Search.spec.js --reporter=verbose
```

- [ ] **Step 3: Commit**

```bash
git add fangdichan-client/src/views/home/__tests__/ \
       fangdichan-client/src/views/search/__tests__/
git commit -m "test: add Home and Search component tests for client"
```

---

### Task 17: 全项目测试验证

- [ ] **Step 1: 运行 admin-web 全部测试**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-admin-web
npx vitest run --reporter=verbose
```

预期：所有测试 PASS。

- [ ] **Step 2: 运行 client 全部测试**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-client
npx vitest run --reporter=verbose
```

预期：所有测试 PASS。

- [ ] **Step 3: 运行后端全部测试**

```bash
cd /home/nhb/project/ruanjiangongcheng/fangdichan-server
mvn test -q
```

预期：所有测试 PASS。

- [ ] **Step 4: 最终提交**

```bash
git add -A
git commit -m "chore: complete standards system implementation"

# 查看最终状态
git status
git log --oneline -5
```

---

## 规范文档索引

**Files:** 全部在 `docs/superpowers/standards/` 下

| 文件 | 内容 |
|------|------|
| `01-code-standards.md` | 后端命名/分层/异常/API 规范，前端组件/文件/API 规范 |
| `02-architecture-standards.md` | 模块划分/依赖规则，数据库设计规范，响应/异常码规范 |
| `03-process-standards.md` | 分支策略，Commit 规范，Code Review 流程，发布流程，测试要求 |
| `04-ui-standards.md` | 管理后台/客户端的 UI 布局、组件使用规范 |

## 工具链索引

| 工具 | 作用范围 | 作用 |
|------|---------|------|
| ESLint | admin-web, client | JS/Vue 代码规范检查 |
| Prettier | admin-web, client | 代码格式化 |
| Checkstyle | server | Java 代码规范 |
| PMD | server | Java 代码质量检测 |
| Husky | root | Git hooks |
| lint-staged | admin-web, client | 暂存区代码自动检查 |
| commitlint | root | Commit 信息规范 |
| Vitest | admin-web, client | 前端测试框架 |
| @vue/test-utils | admin-web, client | Vue 组件测试 |
| MSW | admin-web, client | API Mock + 契约测试 |
| SpringDoc OpenAPI | server | API 文档自动生成 |
| GitHub Actions | root | CI 自动化 |

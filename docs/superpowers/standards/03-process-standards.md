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

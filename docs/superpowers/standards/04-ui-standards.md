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

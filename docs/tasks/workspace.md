# workspace 模块现状

## 已实现

- [x] Review/Re-review 共用 `ReviewWorkspaceView.vue`
- [x] Paper PDF 与 Review Markdown 按钮切换查看
- [x] PDF 页码显示和跳转
- [x] Team 编辑/确认和 Full Review 启动
- [x] 五角色报告、Decision、Roadmap、Questions 标签切换
- [x] Markdown 安全渲染和维度评分表
- [x] Re-review Checklist 展示
- [x] Workflow 阶段事件、连接状态和错误展示
- [x] 完成后 Markdown/PDF 导出
- [x] Object URL 和 SSE 连接清理

## 当前边界

- [ ] 当前不是 PDF/Review 左右同时展示，而是单面板切换
- [ ] 没有报告文本实时增量；SSE 只驱动 Workflow
- [ ] SSE 完成事件不会自动重新拉取详情，最终结果依赖重新加载/已有请求返回
- [ ] Workspace 页面本身缺少独立的完整交互测试文件

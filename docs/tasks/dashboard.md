# dashboard 模块现状

## 已实现

- [x] `GET /api/dashboard`
- [x] Paper 总数、Review 总数、活跃数和完成数
- [x] 最近 5 条 Review 及 Paper 标题
- [x] Dashboard 页面、最近 Review 跳转、空状态、错误状态和测试
- [x] AppShell 导航提供 Upload、History、Settings 入口

## 当前边界

- [ ] Dashboard API 不返回“最近 Paper”独立列表
- [ ] 不返回单独的未完成任务列表，活跃数仅为统计值
- [ ] 最近记录数量固定为 5，不能分页

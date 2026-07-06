# history 模块现状

## 已实现

- [x] 使用 `GET /api/reviews` 展示当前用户 Review 历史
- [x] 列表包含 Paper 标题、类型、状态和创建时间
- [x] 打开历史 Workspace
- [x] 从已完成 Review 发起 Re-review
- [x] 二次确认后删除单次 Review
- [x] 删除关联 Re-review、导出、事件/报告/Team 和文件
- [x] Paper 无其他 Review 时一并删除 Paper 和原文件
- [x] History 页面和删除服务测试

## 当前边界

- [ ] 没有 Paper 聚合详情页或独立 Paper 删除 UI/API
- [ ] 没有 Re-review 历史列表
- [ ] 列表不分页、不筛选、不搜索
- [ ] 删除为不可恢复的物理删除

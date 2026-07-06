# rereview 模块现状

## 已实现

- [x] 仅允许对 `COMPLETED` Review 发起复审
- [x] 上传修改稿和作者回复两个 PDF，并复用 Paper 校验/提取
- [x] 创建、start、详情 API
- [x] `CREATED → VERIFYING → COMPLETED/FAILED` 主流程
- [x] 使用原 Revision Roadmap，缺失时回退到原报告
- [x] 保存 resultMarkdown 和 checklist JSON
- [x] 发布原 Review 事件流上的 Re-review 事件
- [x] 上传表单、Checklist 组件和权限/状态测试

## 当前边界

- [ ] 没有 Re-review 列表和独立删除 API
- [ ] 枚举中的 `UPLOADING`、`EXTRACTING`、`DELETED` 未接入当前主流程
- [ ] Workspace 只展示修改稿 PDF，不展示作者回复 PDF

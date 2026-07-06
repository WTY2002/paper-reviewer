# export 模块现状

## 已实现

- [x] `MARKDOWN`、`PDF` 类型和导出状态
- [x] 已完成 Full/Quick Review 的内容组装
- [x] 已完成 Re-review 的 Checklist/结果组装
- [x] Review 与 Re-review 创建导出 API
- [x] `GET /api/exports/{exportId}` 下载和归属校验
- [x] CommonMark + OpenHTMLToPDF 渲染 PDF
- [x] 本地文件和 exports 记录持久化
- [x] 前端 ExportActions 及导出服务/组件测试

## 当前边界

- [ ] Re-review 导出目前以 Paper ID 标识修改稿/回复，不嵌入文件内容
- [ ] 同一任务同一类型再次导出会覆盖固定文件名，但新增数据库记录
- [ ] 没有导出列表或单独删除 API

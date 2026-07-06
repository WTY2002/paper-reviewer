# stream 模块现状

## 已实现

- [x] ReviewEvent 领域对象、Repository、sequence 和数据库落库
- [x] `GET /api/reviews/{reviewId}/stream`
- [x] Bearer Token 鉴权和 Review 归属校验
- [x] 连接时完整历史回放，随后注册实时 `SseEmitter`
- [x] 连接关闭/超时清理
- [x] 前端 fetch-event-source、workflowStore、顺序排序和去重
- [x] 失败事件展示及 SSE 单元/集成测试

## 当前边界

- [ ] 没有 `REVIEWER_REPORT_DELTA` 事件或增量文本合并
- [ ] 没有 `Last-Event-ID` 增量恢复；重连时回放全部历史并由前端去重
- [ ] Re-review 复用原 Review 事件流，没有独立 Re-review SSE 路径

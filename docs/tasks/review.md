# review 模块现状

## 已实现

- [x] Review、ReviewReport、Repository 和状态机
- [x] `POST /api/reviews/analysis` 创建 Full/Quick Review
- [x] Full：领域分析、Team、确认、start、五报告、Decision/Roadmap/Questions
- [x] Quick：领域分析后直接生成 EIC 报告
- [x] Review 列表、详情、删除 API
- [x] 用户归属、Team 状态和异常状态校验
- [x] 失败保存 `error_message` 并发布 `REVIEW_FAILED`
- [x] Full/Quick 状态流和服务测试

## 当前边界

- [ ] analysis/start 是同步长请求
- [ ] Review 列表不分页、不筛选
- [ ] `ReviewType.REREVIEW` 仅为枚举保留值，复审使用独立领域和 API
- [ ] 没有失败任务重试或取消

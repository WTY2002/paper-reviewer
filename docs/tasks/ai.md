# ai 模块现状

## 已实现

- [x] Spring AI OpenAI 兼容客户端连接 DashScope/Qwen
- [x] `ChatModelProvider` 与 `QwenChatModelProvider`
- [x] 通用安全规则及 Field、Team、五角色、Decision、Roadmap、Questions、Quick、Re-review Prompt
- [x] Field Analysis、Reviewer Team、Dimension Scores、Re-review Checklist 解析
- [x] `FieldAnalysisOrchestrator`、`FullReviewOrchestrator`、`QuickReviewOrchestrator`、`ReReviewOrchestrator`
- [x] 阶段事件发布和 `AI_PROVIDER_ERROR` 映射
- [x] Prompt、解析器和编排器测试

## 当前边界

- [ ] 没有独立 `OpenAiChatModelProvider` 实现
- [ ] Full Review 五位 Reviewer 当前顺序调用，不是并行
- [ ] AI 调用不在后台任务中执行
- [ ] 没有流式 Token/文本输出
- [ ] 没有重试、熔断、调用用量或成本统计

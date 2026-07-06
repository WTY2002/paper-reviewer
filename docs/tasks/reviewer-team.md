# reviewer-team 模块现状

## 已实现

- [x] AI 领域分析后生成固定五角色 Team
- [x] Team JSON 持久化和 `REVIEWER_TEAM_GENERATED` 事件
- [x] Team GET、PUT 和 confirm API
- [x] PUT 必须恰好提交五个固定、唯一角色
- [x] 用户可编辑 targetVenue、identityDescription、expertise、reviewFocus
- [x] 确认写入 `confirmed_at`，Review 转为 `TEAM_CONFIRMED`
- [x] 前端五张编辑卡、保存、确认和状态测试

## 当前边界

- [ ] 不能增删 Reviewer 或自定义角色
- [ ] Team 确认后不能再次编辑或撤销确认

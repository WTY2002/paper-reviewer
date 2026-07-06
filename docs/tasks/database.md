# database 模块现状

## 已实现

- [x] MySQL 8.0 + MyBatis-Plus 3.5.16
- [x] `users`、`papers`、`paper_extractions`、`reviews`、`reviewer_teams`、`review_reports`、`review_events`、`rereviews`、`exports`
- [x] 归属和查询索引、唯一约束、外键 `ON DELETE CASCADE`
- [x] JSON 字段序列化/反序列化
- [x] H2 持久化测试及手工初始化说明

## 当前边界

- [ ] 没有 Flyway/Liquibase；`paper_reviewer.sql` 只适合空库一次性初始化
- [ ] 没有逻辑删除字段，删除为物理删除
- [ ] 没有数据库级集成环境自动迁移或生产备份方案

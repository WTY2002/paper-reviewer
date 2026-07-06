# 总体进度

## 说明

本页按 2026-07-06 的代码核对。`[x]` 表示当前代码和测试中存在对应实现，`[ ]` 表示明确缺口，不代表承诺的排期。

## 已完成模块

- [x] `common-infra`：统一响应、业务错误、全局异常、配置与 CORS
- [x] `database`：MySQL 9 张业务表、MyBatis-Plus Repository、JSON TypeHandler
- [x] `auth`：注册、登录、JWT、当前用户、前端路由保护
- [x] `storage`：临时/Paper/Export 本地存储、配额、路径防护、物理删除
- [x] `paper`：PDF 上传校验、PDFBox 提取、列表和受保护 PDF
- [x] `ai`：Qwen 适配、Prompt、解析器、Full/Quick/Re-review 编排
- [x] `stream`：阶段事件落库、历史回放、Bearer Token SSE
- [x] `reviewer-team`：五人 Team 生成、受限编辑、确认
- [x] `review`：Full/Quick 状态流、报告与综合结果持久化
- [x] `workspace`：PDF/Review 切换、Team、报告、评分、Workflow、导出
- [x] `rereview`：双 PDF 上传、核查编排、Checklist 与结果
- [x] `export`：Review/Re-review 的 Markdown/PDF 导出和下载鉴权
- [x] `dashboard`：统计和最近 Review
- [x] `history`：Review 列表、打开、发起 Re-review、级联物理删除
- [x] `settings`：显示名、默认语言和当前模型展示

## 已确认缺口

- [ ] AI 长任务异步化；当前 analysis/start HTTP 请求同步等待
- [ ] SSE 报告文本 delta；当前只有阶段事件
- [ ] 五位 Full Reviewer 并行执行；当前顺序执行
- [ ] Paper 详情、独立删除和分页 API
- [ ] Review 历史分页、筛选及 Re-review 列表/删除
- [ ] Settings 默认语言自动应用到新任务
- [ ] 服务端 logout/Token 吊销
- [ ] 数据库版本迁移与软删除/回收站
- [ ] 真实浏览器 + MySQL + AI Provider 的端到端测试

## 验证入口

```powershell
cd paper-reviewer-server
.\mvnw.cmd verify

cd ..\paper-reviewer-web
npm run test
npm run build
```

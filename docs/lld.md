# AI Academic Paper Reviewer 详细设计文档

## 1. 文档基线

本文描述 2026-07-06 代码库的实际实现，以 Java Controller/DTO、Vue Router/API 封装和 `paper_reviewer.sql` 为契约来源，不再使用“建议包结构”或待定技术选型。

## 2. 技术架构

### 2.1 后端

- Java 17、Spring Boot 4.1、Spring MVC、Spring Security。
- Spring AI 2.0 的 OpenAI 兼容客户端，连接 DashScope，默认模型 `qwen3.5-plus`。
- MyBatis-Plus 3.5.16、MySQL 8.0；测试使用 H2。
- PDFBox 3.0.5 负责 PDF 检查、元数据和文本提取。
- CommonMark + OpenHTMLToPDF 负责 Markdown/PDF 导出。
- JJWT 负责 JWT；SSE 使用 Spring `SseEmitter`。

### 2.2 前端

- Vue 3.5、Vite 8、Vue Router 5、Pinia 3。
- `vue-pdf-embed` 展示 PDF，`marked` + DOMPurify 渲染 Markdown。
- `@microsoft/fetch-event-source` 携带 Bearer Token 订阅 SSE。
- Vitest + Vue Test Utils + jsdom。

### 2.3 运行拓扑

```text
Browser :5173
  ├─ Vue SPA
  └─ /api (Vite proxy)
        ↓
Spring Boot :8080
  ├─ MySQL paper_reviewer
  ├─ storage/papers
  ├─ storage/exports
  └─ DashScope OpenAI-compatible API
```

## 3. 模块与代码位置

| 模块 | 后端职责 | 前端入口 |
| --- | --- | --- |
| auth/user | 注册、登录、JWT、当前用户 | `authApi.js`、`authStore.js`、Login/Register |
| paper/extraction | 上传校验、PDFBox 解析、Paper 查询和 PDF 读取 | `paperApi.js`、Upload、PdfViewer |
| review | Full/Quick 生命周期、报告持久化、历史列表 | `reviewApi.js`、Review Workspace、History |
| reviewerteam | 五人 Team 生成后的读取、编辑、确认 | ReviewerTeamEditor |
| ai | Prompt、Qwen 调用、输出解析和编排 | 无直接调用 |
| stream | 事件落库、历史回放、实时 SSE | `reviewEventSource.js`、workflowStore |
| rereview | 两文件上传、复审核查和结果查询 | ReReviewUploadForm、VerificationChecklist |
| export | Markdown 组装、PDF 渲染、下载鉴权 | ExportActions |
| dashboard | 统计和最近 5 条 Review | DashboardView |
| settings | 显示名、默认语言、模型展示 | SettingsView |
| history | Review 级联物理删除和文件清理 | HistoryView |
| storage | 临时目录、Paper/Export 文件、路径防护、配额 | 无直接调用 |

## 4. 后端设计

### 4.1 分层

各业务模块大体遵循：

```text
controller/web → service/orchestrator → domain + repository
                                      → infrastructure/persistence mapper/entity
```

统一响应为：

```json
{"success": true, "data": {}, "error": null}
```

错误响应为：

```json
{"success": false, "data": null, "error": {"code": "REVIEW_NOT_FOUND", "message": "..."}}
```

PDF 与导出下载返回二进制响应，不包裹 `ApiResponse`；SSE 返回 `text/event-stream`。

### 4.2 鉴权

- `/api/auth/register`、`/api/auth/login` 为公开接口，其他 `/api/**` 需 Bearer Token。
- `JwtAuthenticationFilter` 解析 Token，Controller 通过 `AuthenticatedUser` 获取 `userId`。
- Repository/Service 查询使用 `userId + resourceId` 校验归属；无权访问与不存在统一返回 not found。
- 前端 logout 仅删除本地 Token。

### 4.3 Paper 上传事务

校验顺序：

1. 非空、`.pdf` 扩展名、`application/pdf` MIME。
2. 文件大小不超过 `app.upload.max-file-size-mb`。
3. 保存随机临时目录并检查 `%PDF-` 魔数。
4. PDFBox 检查文档并读取页数，校验 `max-page-count`。
5. 校验当前用户 Paper 文件总量与 `max-user-storage-mb`。
6. 创建 `papers` 记录，状态 `EXTRACTING`。
7. 原子移动至 Paper 目录、提取文本、保存 `paper_extractions` 和 `extraction.txt`。
8. 更新 Paper 为 `EXTRACTED`。

异常时清理本次临时目录和已创建的 Paper 文件目录。

### 4.4 Review 工作流

`POST /api/reviews/analysis` 是同步编排入口。

Full：

```text
ANALYZING
  → FieldAnalysisOrchestrator（领域分析 + Team AI 输出）
  → ReviewerTeamService.saveGeneratedTeam
  → TEAM_PENDING
  → 用户 PUT team / POST confirm-team
  → TEAM_CONFIRMED
  → POST start
  → REVIEWING
  → FullReviewOrchestrator
  → 5 review_reports + 3 个综合 Markdown 字段
  → COMPLETED
```

Quick：

```text
ANALYZING
  → FieldAnalysisOrchestrator.analyzeField
  → REVIEWING
  → QuickReviewOrchestrator
  → EIC review_report
  → COMPLETED
```

`FullReviewOrchestrator` 当前顺序调用五位审稿人，随后生成 Editorial Decision、Revision Roadmap、Questions for Authors。任何 RuntimeException 会将 Review 标记为 `FAILED` 并发布失败事件。

### 4.5 Reviewer Team

请求必须恰好包含 5 个 Reviewer，角色集合固定为：

```text
EIC, METHODOLOGY, DOMAIN, PERSPECTIVE, DEVILS_ADVOCATE
```

用户可提交 `targetVenue`、`identityDescription`、`expertise`、`reviewFocus`；服务层校验角色数量、唯一性和编辑时机。确认写入 `confirmed_at` 并将 Review 转为 `TEAM_CONFIRMED`。

### 4.6 AI 抽象

`ChatModelProvider` 是统一调用接口，当前实现为 `QwenChatModelProvider`，底层使用 Spring AI OpenAI ChatModel。不存在独立 `OpenAiChatModelProvider` 占位类。

编排器：

- `FieldAnalysisOrchestrator`
- `FullReviewOrchestrator`
- `QuickReviewOrchestrator`
- `ReReviewOrchestrator`

解析器保留 Markdown，并解析 Field Analysis、Reviewer Team、Dimension Scores 和 Re-review Checklist。Provider 异常映射为 `AI_PROVIDER_ERROR`。

### 4.7 SSE

事件结构：

```json
{
  "id": 1,
  "reviewId": 10,
  "type": "REVIEWER_REPORT_COMPLETED",
  "stage": "REVIEWER_REPORT",
  "reviewerRole": "EIC",
  "sequence": 3,
  "payload": null,
  "createdAt": "2026-07-06T10:00:00"
}
```

事件类型：

`FIELD_ANALYSIS_STARTED`、`FIELD_ANALYSIS_COMPLETED`、`REVIEWER_TEAM_GENERATED`、`REVIEWER_TEAM_CONFIRMED`、`FULL_REVIEW_STARTED`、`QUICK_REVIEW_STARTED`、`REREVIEW_STARTED`、`REVIEWER_REPORT_STARTED`、`REVIEWER_REPORT_COMPLETED`、`EDITORIAL_DECISION_STARTED`、`REVISION_ROADMAP_STARTED`、`AUTHOR_QUESTIONS_STARTED`、`REVIEW_COMPLETED`、`REVIEW_FAILED`、`EXPORT_COMPLETED`。

连接时先查询并发送该 Review 的所有历史事件，再注册实时连接。`sequence_no` 用于排序和前端去重。当前没有 `REVIEWER_REPORT_DELTA`，也没有断点 `Last-Event-ID` 协议。

### 4.8 Re-review

创建接口同步上传并解析两个 PDF，成功后写入 `rereviews`，状态 `CREATED`。开始接口读取：

- 原 Review 的 Revision Roadmap；为空时合并所有原审稿报告；
- 修改稿提取文本；
- 作者回复提取文本。

随后进入 `VERIFYING`，保存 `result_markdown` 和 `checklist_json`，最终为 `COMPLETED`。事件写入原 Review 的事件流，所以前端订阅 `originalReviewId`。

### 4.9 Export

只有完成状态允许导出。Review 导出拼装 Paper 信息、Team（Full）、报告和综合输出；Re-review 导出包含原 Review/两个 Paper 的 ID、Checklist JSON 和结果 Markdown。

- Markdown 直接以 UTF-8 写盘。
- PDF 先用 CommonMark 生成 HTML，再由 OpenHTMLToPDF 渲染。
- 同类型文件名固定为 `review.md|pdf` 或 `rereview.md|pdf`，再次导出会覆盖同名文件并新增一条 exports 记录。

### 4.10 删除

`DELETE /api/reviews/{reviewId}` 执行物理删除：

1. 删除该 Review 的所有 Re-review 记录及两个上传 Paper 的文件和记录。
2. 删除 Review 的导出记录和文件。
3. 删除 Review；Team、Reports、Events 由 MySQL 外键级联。
4. 若原 Paper 已无其他 Review，删除 Paper 和文件目录。

数据库没有 `deleted_at`，也没有软删除过滤器。

## 5. 前端设计

### 5.1 路由

| 路径 | 页面 | 公开 |
| --- | --- | --- |
| `/login` | LoginView | 是 |
| `/register` | RegisterView | 是 |
| `/` | DashboardView | 否 |
| `/upload` | UploadView / Re-review 上传模式 | 否 |
| `/reviews/:reviewId` | ReviewWorkspaceView | 否 |
| `/rereviews/:rereviewId` | ReviewWorkspaceView | 否 |
| `/history` | HistoryView | 否 |
| `/settings` | SettingsView | 否 |

### 5.2 状态

- `authStore`：Token、当前用户、初始化状态。
- `reviewStore`：当前 Review 和以角色为键的报告映射。
- `workflowStore`：事件、连接状态和错误；按 sequence 去重排序。

### 5.3 Workspace

Workspace 首次加载详情和受保护 PDF；Full Review 的相关状态额外加载 Team。PDF 通过 API 获取 Blob 后生成 Object URL，离开页面时回收。

页面使用 `Paper PDF / Review Markdown` 切换，而不是双栏。SSE 只更新 WorkflowBar，不会直接增量写入报告；完成后需要重新加载详情才能取得最终内容。

### 5.4 API 客户端

`api/http.js` 统一注入 Bearer Token、序列化 JSON、解包 `ApiResponse.data`，并对二进制响应返回 Blob。业务 API 按 auth、paper、review、rereview、export、dashboard、settings 拆分。

## 6. 数据库

初始化脚本创建 9 张表：

| 表 | 关键内容 |
| --- | --- |
| `users` | 邮箱、密码哈希、显示名、默认输出语言 |
| `papers` | 归属、路径、大小、页数、状态 |
| `paper_extractions` | 全文、页数、提取状态、错误 |
| `reviews` | 类型/状态、领域分析、三类综合 Markdown |
| `reviewer_teams` | 目标 venue、Team JSON、确认时间 |
| `review_reports` | 角色、Markdown、scores JSON、状态 |
| `review_events` | 类型、阶段、角色、序号、payload |
| `rereviews` | 原 Review、两个 Paper、结果、Checklist |
| `exports` | Review/Re-review 归属、类型、路径、状态 |

所有关系使用外键 `ON DELETE CASCADE`。脚本是一次性初始化脚本，不含版本号和增量迁移。

## 7. API 契约

所有接口除注册、登录外均需 `Authorization: Bearer <JWT>`。

| 方法 | 路径 | 请求/用途 |
| --- | --- | --- |
| POST | `/api/auth/register` | JSON: email, password, displayName |
| POST | `/api/auth/login` | JSON: email, password |
| GET | `/api/auth/me` | 当前用户 |
| POST | `/api/papers` | multipart `file` |
| GET | `/api/papers` | 当前用户 Paper 列表 |
| GET | `/api/papers/{paperId}/pdf` | PDF Blob |
| POST | `/api/reviews/analysis` | JSON: paperId, reviewType(FULL/QUICK), outputLanguage |
| GET | `/api/reviews` | Review 列表 |
| GET | `/api/reviews/{reviewId}` | Review 详情及 reports |
| DELETE | `/api/reviews/{reviewId}` | 级联物理删除 |
| GET | `/api/reviews/{reviewId}/team` | Team 与 Review 状态 |
| PUT | `/api/reviews/{reviewId}/team` | 编辑 5 人 Team |
| POST | `/api/reviews/{reviewId}/confirm-team` | 确认 Team |
| POST | `/api/reviews/{reviewId}/start` | 启动 Full Review |
| GET | `/api/reviews/{reviewId}/stream` | SSE |
| POST | `/api/reviews/{reviewId}/rereviews` | multipart revisedFile, responseFile, outputLanguage |
| POST | `/api/rereviews/{rereviewId}/start` | 启动复审 |
| GET | `/api/rereviews/{rereviewId}` | 复审详情 |
| POST | `/api/reviews/{reviewId}/exports` | JSON: exportType |
| POST | `/api/rereviews/{rereviewId}/exports` | JSON: exportType |
| GET | `/api/exports/{exportId}` | 下载 |
| GET | `/api/dashboard` | 统计与最近 Review |
| GET | `/api/settings` | 用户设置与模型名 |
| PUT | `/api/settings` | JSON: displayName, defaultOutputLanguage |

当前列表接口忽略前端可传入的查询参数，返回普通数组而非分页对象。

## 8. 错误码

`AUTH_INVALID_CREDENTIALS`、`AUTH_TOKEN_INVALID`、`AUTH_EMAIL_ALREADY_EXISTS`、`PAPER_NOT_FOUND`、`PAPER_INVALID_TYPE`、`PAPER_FILE_TOO_LARGE`、`PAPER_PAGE_LIMIT_EXCEEDED`、`PAPER_STORAGE_QUOTA_EXCEEDED`、`PDF_EXTRACTION_FAILED`、`REVIEW_NOT_FOUND`、`REVIEW_INVALID_STATUS`、`REVIEW_TEAM_NOT_CONFIRMED`、`AI_PROVIDER_ERROR`、`EXPORT_FAILED`、`FILE_DELETE_FAILED`、`REQUEST_VALIDATION_FAILED`、`INTERNAL_SERVER_ERROR`。

## 9. 配置

```yaml
spring.servlet.multipart.max-file-size: 20MB
spring.servlet.multipart.max-request-size: 20MB
spring.datasource.*: MySQL 连接
spring.ai.openai.base-url: DashScope compatible-mode URL
spring.ai.openai.api-key: ${OPENAI_API_KEY}
spring.ai.openai.chat.model: qwen3.5-plus
spring.ai.openai.chat.timeout: 10m
app.storage.paper-root: storage/papers
app.storage.export-root: storage/exports
app.storage.temp-root: storage/tmp # 有代码默认值，yml 未显式配置
app.upload.max-file-size-mb: 20
app.upload.max-page-count: 300
app.upload.max-user-storage-mb: 500
app.jwt.secret: ${JWT_SECRET:...}
app.jwt.expiration-minutes: 1440
```

生产环境必须覆盖默认 JWT secret 和数据库凭据，API Key 不得提交到仓库。

## 10. 验证命令

```powershell
cd paper-reviewer-server
.\mvnw.cmd verify

cd ..\paper-reviewer-web
npm run test
npm run build
```

测试覆盖后端核心服务/权限/持久化/SSE 和前端关键 Store、页面与组件；当前没有真实 MySQL + DashScope 的自动化端到端测试。

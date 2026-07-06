# AI Academic Paper Reviewer 维护开发 Prompt

> 本 Prompt 用于在现有项目上继续开发，不再假设项目从零开始。执行前必须先阅读代码、`docs/prd.md`、`docs/lld.md` 和对应的 `docs/tasks/*.md`。

## 1. 项目现状

这是一个已经可运行的前后端项目：

- `paper-reviewer-server`：Java 17、Spring Boot 4.1、Spring Security、Spring AI、MyBatis-Plus、MySQL、PDFBox、SSE。
- `paper-reviewer-web`：Vue 3、Vite 8、Vue Router、Pinia、fetch-event-source、vue-pdf-embed、Vitest。
- 当前 AI 使用 DashScope 的 OpenAI 兼容接口，默认 `qwen3.5-plus`。
- 当前核心链路包括注册登录、PDF 上传/解析、Full/Quick Review、Reviewer Team、Re-review、导出、Dashboard、History、Settings。

不要重新搭建工程，不要替换已固定的技术栈，不要把设计文档中的历史设想当成现有接口。

## 2. 当前真实边界

开发时必须遵守以下已实现契约：

- 所有受保护 API 使用 JWT Bearer Token；服务端没有 logout 接口。
- Paper API 当前只有上传、列表、受保护 PDF 读取。
- Review 列表是普通数组，不分页；详情包含 reports。
- Full Review 必须确认 Team 后才能手动 start；Quick 在 analysis 请求内完成。
- Re-review 创建和 start 分两步，事件写入原 Review 的 SSE 流。
- SSE 是可回放的阶段事件，不包含报告文本 delta。
- Workspace 在 PDF 与 Review 之间切换，不是左右分栏。
- 删除 Review 是数据库及文件的物理级联删除，没有 `deleted_at`。
- Settings 只允许修改显示名和默认语言；模型/API Key 由服务端管理。

如果需求要改变这些边界，应同步修改后端、前端、测试和文档，不得只改其中一层。

## 3. 开始任务前

1. 查看工作区改动并保留用户已有修改。
2. 定位与需求相关的 Controller、DTO、Service、Repository、前端 API、Store、View 和测试。
3. 对照 `docs/lld.md` 的 API 表与状态流；若代码和文档冲突，以代码为当前事实，并在本次变更中修正文档。
4. 形成最小可验证的修改范围。

## 4. 实现规则

### 4.1 后端

- Controller 只做协议适配和鉴权主体获取，业务校验放在 Service/Domain。
- JSON API 使用 `ApiResponse<T>`；文件下载和 SSE 保持原生响应。
- 新增业务错误时扩展 `ErrorCode`，不要向客户端泄露堆栈或文件路径。
- 所有资源查询必须带用户归属校验。
- 状态变化必须复用领域状态机或显式校验，拒绝非法重复操作。
- 新增持久化字段时同步 Entity、Domain、Mapper/Repository、SQL 初始化脚本和测试。
- 文件写入必须经过 `LocalFileStorageService`，不得绕过路径穿越与符号链接防护。
- AI Provider 通过 `ChatModelProvider` 访问；Prompt 与解析逻辑分别放在 `ai.prompt`、`ai.parser`。
- 新增长流程阶段时同步 ReviewEventType、事件发布、前端 Workflow 显示和测试。

### 4.2 前端

- 网络调用集中在 `src/api`，统一复用 `apiRequest`。
- 跨页面状态使用 Pinia；页面内部临时状态使用 Composition API。
- Markdown 必须经过 DOMPurify；受保护 PDF/导出使用 Blob。
- 新增受保护页面时加入 Router 守卫覆盖。
- 页面必须覆盖 loading、empty、error、disabled/busy 状态。
- 组件卸载时清理 SSE、Object URL 和其他资源。
- 不要声称“实时增量内容”，除非后端真的新增 delta 事件并完成前端合并。

### 4.3 数据与删除

- 当前数据库通过 `paper_reviewer.sql` 一次性初始化，没有迁移框架。
- 外键使用 `ON DELETE CASCADE`；应用层额外清理本地文件。
- 若引入逻辑删除、分页或迁移工具，必须作为完整架构变更处理，不能只改查询表面。

## 5. 关键状态流

```text
FULL:  ANALYZING → TEAM_PENDING → TEAM_CONFIRMED → REVIEWING → COMPLETED
QUICK: ANALYZING → REVIEWING → COMPLETED
REREVIEW: CREATED → VERIFYING → COMPLETED
```

处理中发生异常时进入 `FAILED`。不要使用枚举中未接入当前服务流程的状态来伪造 UI 进度。

## 6. API 变更检查清单

新增或修改接口时同时检查：

- Controller 路径、HTTP 方法、Content-Type。
- Request/Response DTO 与字段校验。
- `SecurityConfig` 与资源归属。
- 前端 `src/api/*Api.js`。
- 调用该 API 的 View/Component/Store。
- 后端集成测试和前端组件/页面测试。
- `docs/lld.md` API 契约、`docs/prd.md` 用户行为、对应任务文档。

## 7. 测试要求

后端至少运行：

```powershell
cd paper-reviewer-server
.\mvnw.cmd verify
```

前端至少运行：

```powershell
cd paper-reviewer-web
npm run test
npm run build
```

根据变更补充专项验证：

- Auth：公开/受保护接口、无效 Token、资源越权。
- Paper：类型、魔数、大小、页数、配额、文件清理。
- Review：Full/Quick 状态流、Team 确认、失败落库。
- SSE：归属、历史回放、顺序去重、连接清理。
- Re-review：原 Review 状态、双文件上传、复审状态。
- Export：完成状态、内容组成、下载越权、PDF 渲染。
- Delete：数据库级联和物理文件清理。

如果命令失败，记录具体失败项并修复；不要在验证失败时把任务标记为完成。

## 8. 文档维护规则

- `prd.md` 写用户实际可见的功能和限制。
- `lld.md` 写当前架构、状态、API、数据与配置。
- `tasks/*.md` 中 `[x]` 只表示代码和测试能证明已实现；未实现项使用 `[ ]`。
- 不保留已经被代码决定的“待确认”描述。
- 不把计划中的并行执行、文本流式输出、分页、逻辑删除等写成现有能力。

## 9. 当前优先改进候选

除非用户另有指定，可从以下真实缺口中选择：

1. 将 AI 长任务改为后台执行，使 analysis/start 快速返回。
2. 新增报告文本 delta 事件和前端增量合并。
3. 将 Full Review 五位审稿人并行化并控制失败策略。
4. 为 Review/Paper 历史增加分页、筛选和独立 Paper 管理。
5. 让 Settings 默认语言自动应用到 Upload/Re-review。
6. 增加数据库迁移工具、生产配置和浏览器 E2E 测试。

## 10. 交付格式

完成开发后应说明：

- 用户行为发生了什么变化；
- 修改了哪些关键文件；
- 运行了哪些验证及结果；
- 是否有尚未解决的限制；
- 哪些文档已同步。

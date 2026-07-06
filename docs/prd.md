# AI Academic Paper Reviewer 产品需求文档

## 1. 文档信息

| 项目 | 内容 |
| --- | --- |
| 文档状态 | 与当前代码同步 |
| 更新时间 | 2026-07-06 |
| 产品形态 | 单体 Web 应用，面向个人科研作者 |
| 当前 AI Provider | 阿里云百炼兼容 OpenAI 接口，模型默认 `qwen3.5-plus` |

本文只描述当前前后端已经提供的能力。尚未实现的需求集中列在“已知限制与后续候选”中。

## 2. 产品定位

用户上传 PDF 论文后，系统提取全文并调用大模型完成领域分析。用户可以选择：

- Full Review：生成并允许编辑五人 Reviewer Team，确认后生成五份独立报告、编辑决定、修改路线图和作者问题。
- Quick Assessment：完成领域分析后直接生成一份 EIC 快速评估。
- Re-review：基于已完成审稿，上传修改稿和作者回复 PDF，生成修改核查清单与复审结论。

所有 AI 结果仅用于辅助判断，不能替代期刊或会议的正式同行评审。

## 3. 用户与权限

当前只有“注册用户”一种角色。用户通过邮箱、密码和显示名注册，通过 JWT Bearer Token 访问除注册、登录外的所有 API。

- 用户只能访问自己的 Paper、Review、Re-review、Export 和 SSE 事件。
- JWT 默认有效期为 1440 分钟。
- 前端将 Token 保存到本地，并在刷新时通过 `GET /api/auth/me` 恢复登录状态。
- 退出登录由前端清除 Token 完成，当前没有服务端 logout 或 Token 吊销机制。

## 4. 当前功能范围

### 4.1 账号

- 邮箱、密码、显示名注册；密码长度 8～72。
- 邮箱、密码登录并获取 JWT。
- 获取当前用户信息。
- 路由登录保护，登录用户访问登录/注册页时返回 Dashboard。

### 4.2 Paper 上传与解析

- 只接受非空 PDF，同时检查扩展名、MIME 和 `%PDF-` 文件头。
- 单文件默认最大 20 MiB，最大 300 页，单用户论文存储默认最大 500 MiB。
- 使用 PDFBox 读取页数、文档标题和全文；标题缺失时使用文件名。
- 原文保存到 `storage/papers/{userId}/{paperId}/original.pdf`，提取文本保存为 `extraction.txt` 并写入数据库。
- 用户可以查看自己的 Paper 列表及受保护的 PDF 原文。

### 4.3 Full Review

1. 用户上传 Paper，选择 `FULL` 并发起分析。
2. 系统同步完成领域分析和五人 Reviewer Team 生成，Review 进入 `TEAM_PENDING`。
3. 用户可编辑目标期刊/会议、审稿人身份描述、专业领域和关注点，但不能改变五个固定角色。
4. 用户确认 Team，Review 进入 `TEAM_CONFIRMED`。
5. 用户手动开始审稿；系统依次生成五份审稿报告及其维度评分。
6. 系统继续生成编辑决定、修改路线图和作者问题，Review 进入 `COMPLETED`。

固定角色为：`EIC`、`METHODOLOGY`、`DOMAIN`、`PERSPECTIVE`、`DEVILS_ADVOCATE`。

### 4.4 Quick Assessment

用户选择 `QUICK` 后，系统同步完成领域分析和 EIC 快速评估，不生成或确认 Reviewer Team。完成后保存一份 EIC Markdown 报告。

### 4.5 Re-review

- 仅能从 `COMPLETED` 的 Review 发起。
- 用户分别上传修改稿 PDF 和作者回复 PDF，两个文件沿用 Paper 的格式、页数、大小和配额校验。
- 创建后状态为 `CREATED`，用户手动开始复审后进入 `VERIFYING`。
- AI 使用原审稿修改路线图（为空时回退到原审稿报告）、修改稿全文和作者回复全文生成结果。
- 完成后保存 `resultMarkdown` 和结构化 `checklist`。

### 4.6 Review Workspace

- 同一页面通过按钮切换 Paper PDF 和 Review Markdown，不是左右分栏同时展示。
- Full Review 在 Team 待确认阶段显示编辑器，在确认后显示启动按钮。
- Full Review 完成后可按五位审稿人、编辑决定、修改路线图、作者问题切换。
- Quick Assessment 显示 EIC 报告。
- Re-review 显示结构化核查清单。
- 页面底部显示 SSE 工作流事件、连接状态和失败信息。
- 仅在结果完成后显示 Markdown/PDF 导出操作。

### 4.7 SSE 工作流

前端使用支持自定义 Header 的 fetch-event-source，以 `Authorization: Bearer <token>` 订阅：

`GET /api/reviews/{reviewId}/stream`

后端先回放已落库事件，再推送实时阶段事件。当前事件是阶段通知，不提供 Token/字符级 AI 文本增量；完整报告在任务完成后通过详情 API 获取。

### 4.8 Dashboard、历史与设置

Dashboard 展示 Paper 总数、Review 总数、活跃 Review 数、已完成 Review 数及最近 5 条 Review。

History 展示当前用户的 Review 列表，可打开 Workspace、从已完成 Review 发起 Re-review，并在二次确认后删除 Review。

Settings 展示邮箱、显示名、默认输出语言和当前模型名。用户可修改显示名及 `AUTO`、`zh`、`en` 三种语言偏好；模型和 API Key 由服务端统一管理。

### 4.9 导出与删除

- 已完成的 Review 和 Re-review 可导出 `MARKDOWN` 或 `PDF`。
- 导出文件保存到 `storage/exports/{userId}/{originalReviewId}`，下载前检查用户归属。
- 删除 Review 会物理删除相关数据库记录（依赖外键级联）、关联 Re-review 上传文件及导出文件。
- 如果该 Paper 不再关联其他 Review，同时删除原始 Paper 记录和文件。
- 当前 UI/API 不支持单独删除 Paper，也不支持单独删除 Re-review。

## 5. 状态模型

### 5.1 Review

| 类型 | 正常流转 |
| --- | --- |
| FULL | `ANALYZING → TEAM_PENDING → TEAM_CONFIRMED → REVIEWING → COMPLETED` |
| QUICK | `ANALYZING → REVIEWING → COMPLETED` |

任一非终态均可在异常时进入 `FAILED`。枚举中保留 `CREATED` 和 `VERIFYING`，但当前 Full/Quick 创建时直接写入 `ANALYZING`。

### 5.2 Re-review

当前实际主流程为 `CREATED → VERIFYING → COMPLETED`，失败进入 `FAILED`。枚举还保留 `UPLOADING`、`EXTRACTING`、`DELETED`，当前服务流程未使用这些中间状态。

### 5.3 Paper

上传解析期间为 `EXTRACTING`，成功后为 `EXTRACTED`；只有成功完成上传的方法才会向前端返回 Paper。

## 6. 输出内容

Full Review 包含：

- 五位角色各自的 Markdown 报告；
- 各报告的 originality、methodology、clarity、significance、reproducibility、overall 等结构化评分（以 AI 可解析结果为准）；
- 编辑决定；
- 修改路线图；
- 作者问题。

Quick Assessment 包含一份 EIC Markdown 报告。Re-review 包含 Markdown 总结和 JSON 核查清单。

输出语言可在每次请求中指定；未指定时后端归一化为 `AUTO`。当前上传页固定提交 `AUTO`，Settings 的默认语言尚未自动带入上传页。

## 7. 非功能要求

- 安全：JWT 鉴权、资源归属检查、上传三重格式校验、存储路径规范化和符号链接防护。
- 隐私：论文和导出物保存在本机文件系统，不向前端暴露真实路径。
- 可靠性：事件先落库，SSE 重连可回放；AI 失败保存错误并发布 `REVIEW_FAILED`。
- 可用性：长任务展示阶段事件，但当前 HTTP 创建/开始请求仍同步等待 AI 完成。
- 数据：MySQL 8.0，初始化脚本需手工执行；当前没有数据库迁移工具。

## 8. 当前验收标准

- 注册、登录、Token 恢复和受保护路由可用。
- 合法 PDF 可上传并提取文本，非法类型、超限文件和超配额请求被拒绝。
- Full Review 可完成 Team 编辑/确认、五报告和三类综合输出。
- Quick Assessment 可直接生成 EIC 报告。
- Re-review 可上传两个 PDF 并生成核查结果。
- Workspace 可查看 PDF、Markdown、评分/清单和工作流事件。
- 完成结果可导出 Markdown/PDF，且资源归属受保护。
- Dashboard、History、Settings 与当前 API 可正常联动。

## 9. 已知限制与后续候选

- AI 调用和审稿启动目前是同步请求，长论文可能导致浏览器长时间等待。
- SSE 只有阶段事件，没有报告文本增量。
- Full Review 的五位审稿人当前顺序调用，不是并行执行。
- History 和 Paper 列表当前不分页、不筛选。
- 没有独立 Paper 详情/删除 API，也没有 Re-review 列表/删除 API。
- 删除采用物理删除，没有回收站或逻辑删除。
- Settings 默认语言未自动应用到新 Review。
- Re-review Workspace 的 SSE 复用原 Review 事件流。
- 当前只有 Qwen 兼容接口实现，没有独立 OpenAI Provider 实现或用户级模型选择。
- 缺少浏览器级端到端测试和生产部署方案。

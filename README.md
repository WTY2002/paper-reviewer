# AI Academic Paper Reviewer

AI Academic Paper Reviewer 是一个面向科研作者的智能论文审稿系统。用户上传 PDF 论文后，系统通过大模型分析研究领域，并提供完整审稿、快速评估、修改后复审、结果导出和历史记录管理等功能。

## 核心功能

- 用户注册、登录与 JWT 鉴权
- PDF 上传、格式校验、页数限制和文本提取
- Full Review：5 位独立审稿人并行评审
- Quick Assessment：EIC 快速评估
- 自动生成编辑决定、修改路线图和作者问题
- 多维度评分与 Markdown 审稿报告
- PDF 原文与审稿结果切换查看
- Re-review：结合修改稿和作者回复核查修改情况
- SSE 审稿进度通知
- Markdown、PDF 完整报告导出
- Dashboard 统计和历史审稿记录管理
- 数据库及本地文件级联删除

## 技术栈

### 后端

- Java 17
- Spring Boot 4.1
- Spring AI 2.0
- Spring Security + JWT
- MyBatis-Plus 3.5
- MySQL 8.0
- Apache PDFBox：PDF 检查与文本提取
- CommonMark + OpenHTMLToPDF：Markdown/PDF 导出
- Server-Sent Events：进度事件推送
- Maven Wrapper

### 前端

- Vue 3
- Vite 8
- Vue Router
- Pinia
- vue-pdf-embed
- marked + DOMPurify
- `@microsoft/fetch-event-source`
- Vitest

### 存储

- 业务数据：MySQL
- 原始论文：`paper-reviewer-server/storage/papers`
- 导出文件：`paper-reviewer-server/storage/exports`

## 项目结构

```text
paper-reviewer/
├── paper-reviewer-server/   Spring Boot 后端
├── paper-reviewer-web/      Vue 前端
├── docs/                    需求和设计文档
└── README.md
```

## 环境要求

- JDK 17
- MySQL 8.0
- Node.js 22（建议使用当前 LTS 版本）
- npm
- 可用的阿里云百炼/DashScope API Key

## 初始化数据库

后端不会自动创建或修改数据库表，需要提前手动初始化。

首先创建数据库：

```sql
CREATE DATABASE paper_reviewer
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

随后在 Navicat 等数据库工具中选中 `paper_reviewer` 数据库，执行：

```text
paper-reviewer-server/src/main/resources/db/paper_reviewer.sql
```

该脚本会创建 9 张业务表、索引、外键和级联删除规则。脚本应在空数据库中执行一次。

默认数据库配置位于：

```text
paper-reviewer-server/src/main/resources/application.yml
```

当前默认配置为：

```yaml
url: jdbc:mysql://localhost:3306/paper_reviewer
username: root
password: 123456
```

如本机 MySQL 账号不同，请在启动前修改上述配置。

## 配置千问 API

后端从系统环境变量 `OPENAI_API_KEY` 读取 DashScope API Key。请在操作系统中创建该环境变量，然后完全重启 IDEA 或终端，使新进程能够读取它。

当前模型和接口配置位于 `application.yml`：

```yaml
spring:
  ai:
    openai:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${OPENAI_API_KEY}
      chat:
        model: qwen3.5-plus
        timeout: 10m
```

不要把真实 API Key 直接提交到代码仓库。

## 启动后端

### 使用 IDEA

1. 用 IDEA 打开 `paper-reviewer-server`。
2. 确认 Project SDK 为 Java 17。
3. 等待 Maven 依赖加载完成。
4. 确认 MySQL 已启动，数据库和表已经初始化。
5. 确认 IDEA 能读取 `OPENAI_API_KEY`。
6. 运行 `PaperReviewerServerApplication`。

后端默认地址：

```text
http://localhost:8080
```

也可以在命令行运行：

```powershell
cd paper-reviewer-server
.\mvnw.cmd spring-boot:run
```

## 启动前端

打开另一个终端：

```powershell
cd paper-reviewer-web
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

Vite 已配置 `/api` 代理到 `http://localhost:8080`，本地开发不需要额外设置 API 地址。

## 使用流程

### Full Review

1. 注册并登录。
2. 进入 Upload 页面，选择 PDF 论文。
3. 选择 `Full review` 并上传。
4. 查看系统生成的五人 Reviewer Team。
5. 根据需要修改审稿人描述和关注点，然后确认团队。
6. 点击 `Start full review`。
7. 等待五位审稿人并行完成评审。
8. 查看独立报告、评分、编辑决定、修改路线图和作者问题。
9. 根据需要导出 Markdown 或 PDF。

### Quick Assessment

1. 上传 PDF 时选择 `Quick assessment`。
2. 系统完成领域分析后，由 EIC 生成快速评估。
3. 在结果页查看或导出报告。

### Re-review

1. 在 History 中找到已完成的审稿记录。
2. 点击 `Re-review`。
3. 上传修改后的论文 PDF 和作者回复 PDF。
4. 点击 `Start verification review`。
5. 查看逐项修改核查、残留问题和复审结论。

## 常用命令

后端测试：

```powershell
cd paper-reviewer-server
.\mvnw.cmd verify
```

前端测试：

```powershell
cd paper-reviewer-web
npm run test
```

前端生产构建：

```powershell
cd paper-reviewer-web
npm run build
```

## 使用限制

- 单个 PDF 最大 20 MB
- 单篇论文最多 300 页
- 单用户本地存储上限默认 500 MB
- Re-review 的修改稿和作者回复目前仅支持 PDF
- Full Review 会调用模型多次，完成时间取决于论文长度、模型负载和 API 限流情况
- AI 生成内容仅作为辅助意见，不能替代真实期刊或会议的专业同行评审

## 常见问题

### 页面提示 `Qwen model request failed`

检查：

- `OPENAI_API_KEY` 是否存在且有效
- 设置环境变量后是否重启 IDEA
- DashScope 账户是否有可用额度
- 网络能否访问 DashScope
- 后端日志中最底层的 `Caused by` 信息

### 后端提示表不存在

确认已经在 `paper_reviewer` 数据库中执行 `paper_reviewer.sql`。后端已关闭自动建表，不会替你初始化数据库。

### 前端无法访问后端

确认后端正在监听 8080 端口，前端通过 `npm run dev` 启动在 5173 端口。


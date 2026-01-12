# 0004：前端管理台（Admin Console）提案

## 背景与问题陈述

当前项目已明确：

- 核心底座提供企业级 RAG 能力，主路径为模式A：最终答案生成由底座统一负责（`openspec/decisions/0008-generation-owned-by-core.md`）。
- 平台接入主路径为方案A：平台/前端透传终端用户 JWT（`openspec/proposals/0003-platform-auth-and-identity-mapping.md`）。
- 文件存储默认选择 S3（`openspec/decisions/0005-storage-default-s3.md`）。

在此基础上，需要一个最小但可用的前端，用于：

- 演示与验证全链路：登录拿 JWT → 上传文档 → 观察索引状态 → 问答验证 citations。
- 支撑后续平台（Coze/Dify）工具调用联调：可直接对底座 API 做可视化测试与问题定位。

## 目标（Goals）

- 实现一个“管理台 Web”（而非完整业务前台），覆盖 MVP 必需的管理与验证能力。
- 上传文件采用“直传 S3（presigned URL）”主路径，降低底座带宽压力并贴近企业实践。
- 遵循方案A：终端用户在底座侧登录获取 JWT，前端所有请求都携带用户 JWT。
- 前端不绑定任何平台（Coze/Dify），只是独立的验证入口。

## 非目标（Non-Goals）

- 不做复杂的会话管理、运营管理、权限后台（先用底座最小权限能力）。
- 不做多端（小程序/APP）与国际化。
- 不做复杂的文件预览/在线编辑。
- 不把 Coze/Dify 的编排能力“搬到前端”，避免职责重叠。

## 范围（Scope）

### P1（必须交付）

- 登录与鉴权
  - 用户登录并获取 JWT（或粘贴 JWT 进入系统，作为开发/演示模式）。
  - 全局拦截：未登录状态跳转登录页；请求自动携带 `Authorization: Bearer <user_jwt>`。
- 文档上传（S3 直传）
  - 向底座请求 presigned URL（由底座根据用户身份决定对象 key）。
  - 浏览器直传到 S3（PUT/POST 方式由实现阶段决定）。
  - 上传完成后回调底座创建文档记录并触发索引。
- 文档列表与详情
  - 列表：文件名、上传时间、状态（UPLOADED/INDEXING/INDEXED/FAILED）、失败原因摘要。
  - 详情：状态变更时间、失败原因（若有）、可选展示 chunk/引用信息（增强项）。
- Chat 测试页
  - 输入问题，调用底座 `/api/chat`。
  - 展示答案与 citations（文档/片段来源），便于验证 RAG 与权限过滤。

### P2（增强项，后续增加）

- 更完整的文档检索调试页（仅检索不生成）。
- 索引失败重试按钮与查看索引日志入口（需底座支持）。
- 基础指标面板（请求耗时、错误率等）。

## 方案概述

### 关键交互流程：S3 直传

1. 前端携带用户 JWT 调用底座“获取 presigned”接口。
2. 底座返回：
   - 上传目标（bucket/key）
   - presigned URL（或 presigned POST 表单字段）
   - 约束（content-type、max size、过期时间等）
3. 前端把文件直传 S3。
4. 前端调用底座“上传完成确认/创建文档”接口，提交：
   - 文件名、contentType、size、（可选）sha256
   - `storage_uri = s3://bucket/key`
5. 底座创建 `documents` 记录并发布 `DocumentUploadedEvent`，进入异步索引。

### 安全与约束（设计要求）

- presigned 必须短期有效（例如分钟级），避免长期泄露。
- bucket/key 必须由底座决定并与用户身份绑定，避免前端可写任意 key。
- 上传完成后必须由底座二次校验并落库（防止仅靠 S3 上传即“视为可用”）。

## 里程碑（Milestones）

- M1：信息架构与 API 契约定稿（spec）
- M2：登录/鉴权 + Chat 测试页可用
- M3：S3 直传 + 文档列表/状态闭环可用

## 验收标准（Acceptance Criteria）

- 使用管理台可完成：登录拿 JWT → 上传文件（直传 S3）→ 文档状态到 INDEXED → `/api/chat` 返回答案与 citations。
- 上传的文件只能归属于当前用户，切换用户后不可在列表/检索中看到他人数据。
- presigned URL 过期后不可继续上传，且前端能给出明确错误提示。

## 开放问题（Open Questions）

- 前端技术栈：React/Next.js/Vue 任选其一，需结合仓库整体规划确定。
- 上传方式：主路径选择 presigned PUT（见 `openspec/decisions/0010-frontend-login-and-s3-presign-put.md`），后续是否需要增加 presigned POST 作为增强能力？
- 登录方式：主路径选择标准登录（见 `openspec/decisions/0010-frontend-login-and-s3-presign-put.md`），开发/演示模式是否允许提供“粘贴 JWT”作为后置能力？

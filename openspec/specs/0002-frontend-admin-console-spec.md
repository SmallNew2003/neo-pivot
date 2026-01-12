# 0002：前端管理台（Admin Console）规格（Spec）

> 对应提案：`openspec/proposals/0004-frontend-admin-console.md`

## 1. 范围与原则

### 1.1 范围

本规格定义“管理台 Web”的信息架构、关键页面、与底座 API 的契约要求，用于支撑：

- 方案A：终端用户 JWT 透传（`openspec/proposals/0003-platform-auth-and-identity-mapping.md`）
- 模式A：最终答案生成由底座负责（`openspec/decisions/0008-generation-owned-by-core.md`）
- S3 直传：前端通过 presigned URL 直传对象存储

### 1.2 关键原则

- 管理台是“验证入口”，不承担平台编排能力。
- 前端不持久化长期用户 token；若需要持久化，必须有显式安全提示与退出机制。
- 所有业务请求必须携带 `Authorization: Bearer <user_jwt>`。

## 2. 信息架构（Pages）

### 2.1 登录页

目的：获取可用的用户 JWT。

MVP 主路径：标准登录（见 `openspec/decisions/0010-frontend-login-and-s3-presign-put.md`）。

- 登录表单：用户名/密码 → 调用底座登录接口 → 返回 JWT。

登录接口详细契约见：`openspec/specs/0003-auth-login-spec.md`

可选（开发/演示模式，后置）：

- 手动粘贴 JWT（用于本地快速联调，但不得作为默认入口）。

### 2.2 文档页

包含：

- 上传区（直传 S3）
- 文档列表（分页/搜索可后置）
- 文档详情（点击进入）

### 2.3 Chat 测试页

目的：以最小交互验证 RAG 闭环与 citations。

包含：

- 输入框（question）
- TopK 配置（可选）
- 输出：answer + citations 列表（可展开查看片段）

## 3. 核心流程与契约

> 本节定义前端与底座交互必须满足的“语义契约”。路径命名可在实现阶段微调，但语义与字段必须保持一致。

### 3.1 鉴权头

所有需要登录的接口都必须要求：

- `Authorization: Bearer <user_jwt>`

JWT 的 `sub` 将作为 `owner_id` 用于数据隔离。

### 3.2 S3 直传（presigned）流程

#### 3.2.1 获取 presigned

- `POST /api/storage/presign`

请求（JSON）建议字段：

- `filename`：原始文件名
- `contentType`：MIME
- `sizeBytes`：大小
- `sha256`：可选（前端可后置计算）

响应（JSON）建议字段：

- `storageUri`：`s3://bucket/key`（底座生成）
- `uploadMethod`：固定为 `PUT`（见 `openspec/decisions/0010-frontend-login-and-s3-presign-put.md`）
- `uploadUrl`：上传 URL
- `headers`：上传需要的请求头（例如 `Content-Type`）
- `expiresAt`：过期时间
- `constraints`：可选（maxSize、allowedContentTypes 等）

语义约束：

- `bucket/key` 必须由底座生成并与当前用户绑定。
- presigned 必须短期有效（分钟级）。

#### 3.2.2 直传到 S3

前端执行上传：

- 向 `uploadUrl` 发起 HTTP PUT
- 请求头带 `headers`
- body 为文件内容

#### 3.2.3 上传完成确认（落库触发索引）

- `POST /api/documents`

请求（JSON）建议字段：

- `storageUri`：来自 presign 响应的 `storageUri`
- `filename`
- `contentType`
- `sizeBytes`
- `sha256`：可选

响应：

- `documentId`
- `status`（初始应为 `UPLOADED`）

语义约束：

- 底座必须校验 `storageUri` 的 bucket/key 是否属于当前用户允许的命名空间，防止写入他人对象。
- 成功创建文档后发布 `DocumentUploadedEvent(documentId)`，进入异步索引。

### 3.3 文档列表与状态

- `GET /api/documents`
  - 返回当前用户的文档列表（owner 过滤）

- `GET /api/documents/{documentId}`
  - 返回文档详情（owner 或 admin）

### 3.4 Chat（模式A）

- `POST /api/chat`
  - 输入：`question`、可选 `topK`
  - 输出：`answer`、`citations[]`

管理台必须展示 citations，以便验证检索与权限过滤。

## 4. UX 与错误处理要求（MVP）

- Token 过期（401）：统一跳转登录页，并提示“登录已过期，请重新登录”。
- 上传失败：
  - presigned 过期：提示“上传链接已过期，请重新获取”。
  - S3 CORS/鉴权错误：提示“对象存储拒绝上传，请检查配置或稍后重试”。
- 索引失败：文档列表需显示失败状态与 `errorMessage` 摘要，并提供“复制错误信息”按钮（MVP 可选）。

## 5. 安全要求

- 浏览器端不允许持久化长期 token（若必须使用 localStorage，应提供显式开关并在文档中说明风险）。
- presigned URL 仅用于上传，不应暴露对象读取权限（读取路径通过底座控制）。
- 上传对象 key 的组织规则必须可审计（建议包含 userId 与 documentId 前缀）。

## 6. 待定项

- 前端技术栈选择与目录结构（是否与后端同仓库、是否 monorepo）。
- 登录接口路径与字段命名（建议提供 `/api/auth/login`，具体字段见实现阶段）。
- S3 CORS 最小配置与环境初始化方式（AWS S3 vs MinIO）。

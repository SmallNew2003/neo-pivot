# 0002：前端管理台（Admin Console）规格（Spec）

> 对应提案：`openspec/changes/archive/2026-01-12-v0.1.0-add-frontend-admin-console/proposal.md`

## 1. 范围与原则

### 1.1 范围

本规格定义“管理台 Web”的信息架构、关键页面、与底座 API 的契约要求，用于支撑：

- 方案A：终端用户 JWT 透传（`openspec/changes/archive/2026-01-12-v0.1.0-add-platform-auth-and-identity-mapping/proposal.md`）
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
  - 下载（通过底座签发短期 presigned GET，见 `openspec/decisions/0016-storage-object-key-and-presign-security.md`）

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

JWT 的 `sub` 表示内部用户 ID，用于映射为 `owner_id` 执行数据隔离（见 `openspec/decisions/0017-primary-key-strategy.md`）。

### 3.2 S3 直传（presigned）流程

#### 3.2.1 获取 presigned

- `POST /api/storage/presign`

请求（JSON）建议字段：

- `filename`：原始文件名（底座会生成安全化后的 key 文件名）
- `contentType`：MIME
- `sizeBytes`：大小
- `sha256`：可选（前端可后置计算）

响应（JSON）建议字段：

- `documentId`：底座预分配的文档 ID（用于构造对象 key 与后续落库关联）
- `storageUri`：`s3://neo-pivot/<userId>/<documentId>/<filename>`（底座生成，见 `openspec/decisions/0016-storage-object-key-and-presign-security.md`）
- `uploadMethod`：固定为 `PUT`（见 `openspec/decisions/0010-frontend-login-and-s3-presign-put.md`）
- `uploadUrl`：上传 URL
- `headers`：上传需要的请求头（例如 `Content-Type`）
- `expiresAt`：过期时间
- `presignId`：本次签发的凭证 ID（用于后续“上传确认/禁止复用”）
- `constraints`：可选（maxSize、allowedContentTypes 等）

语义约束：

- `bucket/key` 必须由底座生成并与当前用户绑定，Key 组织规则见：`openspec/decisions/0016-storage-object-key-and-presign-security.md`。
- presigned 必须短期有效：上传默认 10 分钟（强约束）。
- 禁止复用：同一个 `presignId` 只能被“上传确认”消费一次（见下文 3.2.3）。
- 可选：IP 绑定（作为风险降低，不作为强安全保证），见：`openspec/decisions/0016-storage-object-key-and-presign-security.md`。

#### 3.2.2 直传到 S3

前端执行上传：

- 向 `uploadUrl` 发起 HTTP PUT
- 请求头带 `headers`
- body 为文件内容

#### 3.2.3 上传完成确认（落库触发索引）

- `POST /api/documents`

请求（JSON）建议字段：

- `presignId`：必填（对应 3.2.1 返回值，用于消费与审计）
- `storageUri`：来自 presign 响应的 `storageUri`
- `documentId`：必填（对应 3.2.1 返回值；用于对齐 key 与落库语义）
- `filename`
- `contentType`
- `sizeBytes`
- `sha256`：可选

响应：

- `documentId`
- `status`（初始应为 `UPLOADED`）

语义约束：

- 底座必须校验 `storageUri` 的 bucket/key 是否属于当前用户允许的命名空间，防止写入他人对象。
- 底座必须校验 `presignId` 属于当前用户且未被消费；重复消费必须拒绝（实现阶段确定返回码）。
- 底座必须校验 `documentId` 与 `storageUri` 一致（避免“借用他人 documentId 或 key”）。
- 成功创建文档后发布 `DocumentUploadedEvent(documentId)`，进入异步索引。

### 3.2.4 文档下载（presigned GET）

为满足“下载场景”的最小演示与运维需求，底座需要提供短期下载链接签发。

- `POST /api/storage/presign-download`

请求（JSON）建议字段：

- `documentId`：必填

响应（JSON）建议字段：

- `storageUri`：`s3://...`（用于审计/展示）
- `downloadMethod`：固定为 `GET`
- `downloadUrl`：短期有效的下载 URL
- `expiresAt`：过期时间（默认 5 分钟，强约束）
- `presignId`：本次签发的凭证 ID（用于审计）

语义约束：

- 只能对 owner（或 admin）签发
- 审计日志必须记录签发（不得记录完整 URL），见 `openspec/decisions/0016-storage-object-key-and-presign-security.md`

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

### 4.1 Presign 错误码建议（MVP）

为便于前端与审计联动，建议使用清晰的 HTTP 状态码区分失败原因：

- `POST /api/storage/presign`、`POST /api/storage/presign-download`
  - `401`：未登录/Token 过期
  - `403`：无权限（例如非 owner 且非 admin 请求下载）
- `POST /api/documents`（上传确认/落库）
  - `409`：`presignId` 已被消费（禁止复用）
  - `410`：`presignId` 或 presigned 已过期
  - `403`：IP 绑定校验失败（仅当开启该安全开关）

## 5. 安全要求

- 浏览器端不允许持久化长期 token（若必须使用 localStorage，应提供显式开关并在文档中说明风险）。
- presigned URL 应为短期临时权限：
  - 上传：10 分钟
  - 下载：5 分钟
- 对 presign 的签发、消费、过期必须可审计（见 `openspec/decisions/0016-storage-object-key-and-presign-security.md`）。
- 上传对象 key 的组织规则必须可审计（建议包含 userId 与 documentId 前缀）。

## 6. 待定项

- 前端技术栈选择与目录结构（是否与后端同仓库、是否 monorepo）。
- 登录接口路径与字段命名（建议提供 `/api/auth/login`，具体字段见实现阶段）。
- S3 CORS 最小配置与环境初始化方式（AWS S3 vs MinIO）。

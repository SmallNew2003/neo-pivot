# 0016：S3 对象 Key 规则与 Presign 安全/审计约定

## 状态

已决定

## 背景

MVP 主路径采用 S3 presigned PUT 直传（见 `openspec/decisions/0010-frontend-login-and-s3-presign-put.md`）。为保证：

- 可审计：对象 key 可反推 user/document
- 可授权：防止用户构造他人 `storageUri` 越权落库
- 可控：presign 过期、使用与异常可追溯

需要固化对象 key 组织规则与 presign 的安全约束。

## 决策

### 1) 对象 Key 规则（强约束）

- `storageUri` 统一为：`s3://neo-pivot/<userId>/<documentId>/<filename>`
- 其中：
  - `userId`：内部用户 ID（JWT `sub`，数值型，以字符串形式承载）
  - `documentId`：文档 ID（数据库主键，见 `openspec/decisions/0017-primary-key-strategy.md`）
  - `filename`：安全化后的文件名（避免路径穿越/特殊字符；原始文件名仍需在 `documents.filename` 保留）

约束：

- `documentId` 由底座在签发上传 presign 时“预分配”，以便 key 中包含稳定的文档 ID
- 文件名安全化建议：
  - 仅保留 basename（去掉路径信息）
  - 过滤控制字符/危险字符（如 `/`、`\\`、`..` 等）
  - 最终用于 key 的文件名建议做 URL encode（实现阶段固化）

### 2) Presign TTL（强约束）

- 上传（PUT）presign 有效期：10 分钟
- 下载（GET）presign 有效期：5 分钟

### 3) 禁止复用（语义约束 + 风险降低）

> 说明：S3 presigned URL 本质上是“签名后的临时权限”，对象存储层通常无法强制“同一 URL 只能使用一次”。
> 本项目的“禁止复用”以应用侧语义约束落地：一次 presign 只允许一次“确认/消费”。

- 后端必须为每次 presign 生成一个可审计的“凭证记录”（例如 `presign_id`），并记录其状态：
  - `ISSUED`（已签发）→ `CONSUMED`（已被确认/使用）→ `EXPIRED`（过期）
- 上传场景：前端上传完成后调用 `POST /api/documents` 时必须携带该 `presignId`，后端将其标记为 `CONSUMED`；重复消费必须拒绝（409/400，具体实现阶段确定）
- 上传场景：`presignId` 必须与签发时预分配的 `documentId` 一一对应，后端在 `POST /api/documents` 需要校验 `presignId + documentId + storageUri` 的一致性
- 下载场景：后端签发下载 presign 时也应生成记录，并在下载签发/过期维度可追踪（下载是否“使用”无法完全确认，但至少可审计“谁拿到过链接”）

### 4) 可选：IP 绑定（风险降低）

- 默认关闭；配置开启后才校验
- 后端在签发 presign 时记录客户端 IP（与 User-Agent 可选）
- 上传确认（`POST /api/documents`）时可校验“确认请求 IP == 签发 IP”（作为可选安全开关）
- IP 口径建议可配置切换：
  - 默认不信任代理：使用 `request.getRemoteAddr()`
  - 开启代理信任：使用 `X-Forwarded-For`，并限制可信代理节点（实现阶段固化）
- 说明：该措施用于降低 presign 泄露后的滥用风险，但不能替代更强的边界（如专用上传网关/STS/专属下载通道）

### 5) 审计日志（强约束）

必须记录以下事件（至少日志，后续可入审计表）：

- presign 签发：用户、IP、UA、用途（upload/download）、`storageUri`（或 bucket/key）、到期时间、`presignId`
- presign 消费（上传确认）：用户、IP、UA、`storageUri`、`presignId`、结果（成功/失败原因）
- presign 过期：可按清理任务或查询时标记（MVP 可先按访问时判断）

安全要求：

- 日志不得输出完整 presigned URL（含签名参数），避免二次泄露

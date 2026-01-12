# 0001：企业级 RAG 知识库系统 MVP 规格（Spec）

> 对应提案：`openspec/proposals/0001-mvp-rag-knowledge-nexus.md`

## 1. 目标与范围

本规格用于定义 MVP 阶段“可落地实现”的系统边界、模块职责、接口与数据模型，确保实现能够满足演示闭环与验收标准。

### 1.1 目标（MVP 必达）

- 完成 RAG 闭环：上传文档 → 异步索引（解析/分块/向量化/入库）→ 检索 → 基于上下文问答。
- 认证采用 JWT（Bearer Token），实现最小权限隔离：默认只允许访问/检索本人上传文档。
- 数据存储采用 PostgreSQL，向量检索采用 PGVector。
- 文件存储默认采用 S3（或 S3 兼容实现），并保留本地文件系统作为可选实现。
- LLM/Embedding Provider 采用可插拔设计，并保留 OpenAI / Azure OpenAI / Ollama 的扩展位；默认落地采用 OpenAI Compatible API（见 `openspec/decisions/0009-llm-default-openai.md`）。
- 系统按模块化单体组织，模块间通过事件驱动解耦（不做微服务）。

### 1.2 非目标（MVP 不做）

- 不做多租户隔离与复杂计费。
- 不做“向量级权限控制到 chunk 级别的复杂 RBAC”（MVP 仅 owner 级隔离 + 可选 admin）。
- 不引入消息队列与分布式事务；一致性以“应用内事件 + 最终一致性”实现。

## 2. 总体架构

### 2.1 模块边界

- `auth`：认证（JWT 解析）、用户上下文（UserContext）与最小授权策略。
- `document`：上传、元数据管理、状态机、存储定位（文件路径/对象存储占位）。
- `ai`：文档解析、分块、向量化、向量入库；对外只暴露“索引能力”与事件监听。
- `search`：相似度检索与问答编排（检索→拼接上下文→调用模型）。

### 2.2 核心数据流（文本版）

1. 客户端上传文件（携带 JWT）。
2. `document`：
   - 计算文件哈希（可选：用于去重）。
   - 写入 `documents` 元数据，状态置为 `UPLOADED`。
   - 事务提交后发布 `DocumentUploadedEvent(documentId)`。
3. `ai` 监听事件：
   - 将状态置为 `INDEXING`（可包含进度/时间戳）。
   - 读取文件 → 解析为纯文本 → 分块 → 生成 embedding → 写入向量表/索引表。
   - 成功：状态置为 `INDEXED`；失败：状态置为 `FAILED` 并记录错误信息。
4. `search`：
   - 输入问题 → 生成 query embedding → 在 PGVector 里做 Top-K 相似度检索（带 owner 过滤）→ 组装上下文 → 调用 LLM → 返回答案与引用片段。

## 3. 认证与授权（JWT）

### 3.1 认证方式

- HTTP 请求使用 `Authorization: Bearer <JWT>` 进行身份识别。
- JWT 采用 `RS256`（非对称签名）。服务端仅负责“验签与鉴权”，签发端可独立部署（MVP 可先用内置 dev 签发工具或脚本生成 token）。
- 推荐使用 JWKS（JSON Web Key Set）方式下发公钥，以支持密钥轮换；MVP 如需简化，也可先用“配置文件注入公钥”的方式验签。

### 3.2 JWT 声明（Claims）约定

至少需要以下字段：

- `sub`：用户唯一标识（建议使用字符串 userId）。
- `roles`：角色数组（可选；MVP 用于 admin 放行）。
- `iss`：签发者（建议必填，用于校验）。
- `aud`：受众（建议必填，用于校验）。

建议字段：

- `exp`：过期时间（必须校验）。
- `iat`：签发时间。
- `kid`：密钥 ID（用于 JWKS 轮换）。

### 3.3 授权规则（MVP）

- 默认规则：用户只能访问/检索 `owner_id == sub` 的数据。
- 可选增强：当 `roles` 包含 `ADMIN` 时允许访问全部文档（便于演示与运维）。

### 3.4 平台接入鉴权与身份映射（主路径）

- 平台接入主路径采用“方案 A：透传终端用户 JWT”，见 `openspec/proposals/0003-platform-auth-and-identity-mapping.md`。
- 平台调用底座 API 时必须携带用户级 `Authorization: Bearer <user_jwt>`，底座以 JWT `sub` 作为 `owner_id` 执行权限过滤与审计。

## 4. 数据模型（PostgreSQL + PGVector）

> 表结构字段名为建议值，最终以实现为准；但必须满足本规格的语义与约束。

### 4.1 documents（文档元数据）

- `id`：主键（UUID 或 BIGSERIAL，二选一）。
- `owner_id`：文档所属用户（来自 JWT `sub`）。
- `filename`：原始文件名。
- `content_type`：MIME 类型（可选）。
- `size_bytes`：文件大小（可选）。
- `sha256`：文件哈希（可选，便于去重/幂等）。
- `storage_uri`：存储定位（例如本地路径或 `s3://...`，MVP 可先用本地）。
- `status`：`UPLOADED` / `INDEXING` / `INDEXED` / `FAILED`。
- `error_message`：失败原因（仅当 `FAILED`）。
- `created_at` / `updated_at`：时间戳。

约束建议：

- `owner_id` 必填。
- `status` 必填。
- `sha256` 若启用去重，可与 `owner_id` 组成唯一约束（同一用户重复上传相同文件时幂等返回已有文档）。

### 4.2 document_chunks（文档分块）

用于保存可引用的 chunk 文本与元数据，便于“返回引用片段”与调试：

- `id`：主键。
- `document_id`：外键关联 `documents.id`。
- `owner_id`：冗余字段（便于检索过滤与索引）。
- `chunk_index`：分块序号（从 0 开始）。
- `content`：chunk 纯文本。
- `content_hash`：chunk 哈希（可选，用于去重/校验）。
- `created_at`：时间戳。

### 4.3 document_chunk_embeddings（向量数据）

用于向量检索：

- `id`：主键。
- `chunk_id`：外键关联 `document_chunks.id`。
- `owner_id`：冗余字段（必须有，用于权限过滤）。
- `embedding`：`vector(<dimension>)`（PGVector 类型）。
- `model`：embedding 模型标识（可选，便于迁移）。
- `created_at`：时间戳。

索引建议：

- 对 `embedding` 建立向量索引（ivfflat 或 hnsw，结合 PGVector 版本与性能权衡）。
- 对 `owner_id` 建立 btree 索引（配合过滤）。

## 5. 事件与一致性

### 5.1 领域事件定义

- `DocumentUploadedEvent`
  - 字段：`documentId`
  - 触发时机：`documents` 写入成功且事务提交之后（避免“已发布事件但数据库回滚”）。

### 5.2 一致性与幂等

- 索引流程必须具备幂等性：同一个 `documentId` 事件重复投递时，不应产生重复 chunk/embedding（实现时可通过“先清理再写入”或“唯一约束 + upsert”保证）。
- 文档状态更新遵循最终一致性：上传成功不代表已可检索，需通过状态 `INDEXED` 判断。

## 6. 接口（REST API）草案

> 本节只定义 MVP 需要的最小接口集合；路径与字段名可在实现阶段微调，但需保持语义一致。

### 6.0 认证（标准登录）

为支持前端管理台与平台接入的用户级 JWT 透传（方案A），建议提供标准登录接口（具体字段见实现阶段）：

- `POST /api/auth/login`
  - 响应：`accessToken`（JWT）、`tokenType`（`Bearer`）、（可选）`expiresInSeconds`

详细契约见：`openspec/specs/0003-auth-login-spec.md`

### 6.1 文档上传

- `POST /api/documents`
  - 请求（方式一）：`multipart/form-data`，字段 `file`（实现简单，带宽由底座承担）
  - 请求（方式二，推荐）：JSON 提交 `storageUri` 等元数据（配合 S3 presigned 直传），见 `openspec/specs/0002-frontend-admin-console-spec.md`
  - 响应：`documentId`、`status`
  - 权限：需要 JWT

### 6.1.1 获取对象存储直传凭证（建议）

为支持“前端直传 S3”，建议提供 presigned 能力（具体字段见 `openspec/specs/0002-frontend-admin-console-spec.md`）：

- `POST /api/storage/presign`
  - 权限：需要 JWT

### 6.2 查询文档状态

- `GET /api/documents/{documentId}`
  - 响应：元数据（含 `status`、`errorMessage`）
  - 权限：owner 或 admin

### 6.3 问答（RAG）

- `POST /api/chat`
  - 请求：`question`、（可选）`topK`
  - 响应：`answer`、`citations[]`（每条包含 `documentId`、`chunkId`、`chunkIndex`、`contentSnippet`）
  - 权限：需要 JWT；检索必须带 owner 过滤（或 admin 例外）

## 7. 索引与检索策略（MVP 默认值建议）

### 7.1 文本解析

- 优先支持 `txt`、`md`、`pdf`（具体解析器在实现阶段确定；MVP 不追求覆盖所有格式）。

### 7.2 分块策略

- 默认：按 token/字符长度分块（具体参数实现时可配置）。
- 建议参数：chunk size 500、overlap 50（仅作为起点，可在演示中调优）。

### 7.3 检索策略

- Top-K：默认 5（可配置）。
- 返回引用：至少返回 chunk 的简短片段（用于解释答案来源）。

## 8. 可观测性（MVP）

- 日志必须覆盖关键事件：上传成功/失败、索引开始/完成/失败、检索命中数量、问答耗时。
- 需要可定位单次请求：建议引入 requestId/correlationId（实现阶段确定方式）。

## 9. 配置与安全

- 任何密钥（JWT secret、LLM key、数据库密码）必须通过环境变量或外部配置注入，不写死在仓库中。
- 对外接口默认开启鉴权；若提供 dev 模式跳过鉴权，必须显式开关并仅用于本地。

## 9.1 AI Provider 与存储的可扩展性约定

为满足“高扩展性”，MVP 在设计上要求“可插拔（Pluggable）”，即便实现阶段优先落地其中一种，也不能把某个 Provider/存储写死到业务代码中。

### 9.1.1 LLM/Embedding Provider（可插拔）

- 目标：同一套业务编排（索引、检索、问答）可以通过配置切换 Provider，不影响接口与数据模型。
- 约定：需要同时抽象两类能力：
  - `Chat`：用于问答生成。
  - `Embedding`：用于文档与 query 的向量化。
- MVP 计划支持的 Provider（至少预留配置位与实现路径）：
  - `OpenAI`
  - `Azure OpenAI`
  - `Ollama`（本地模型）

### 9.1.2 文件存储（可插拔）

- 目标：文档存储可在“本地文件系统”与 “S3/MinIO（S3 兼容）”之间切换，不影响上层索引与检索流程。
- 约定：`documents.storage_uri` 统一使用“带 scheme 的定位符”，例如：
  - 本地：`file:///abs/path/to/file`
  - S3：`s3://bucket/key`
  - MinIO：`s3://bucket/key`（端点通过配置区分）

默认落地策略（MVP）：

- 默认使用 `s3://`（AWS S3 或 MinIO 等 S3 兼容实现通过 endpoint 配置区分）。

## 10. 验收标准（与提案一致）

- 可通过一组 HTTP 请求完成：上传 → 等待 `INDEXED` → 提问得到答案与引用片段。
- 索引失败可在 `GET /api/documents/{id}` 看到失败原因。
- 用户隔离生效：用户 A 的检索不会返回用户 B 的 chunk（admin 例外）。

## 11. 待定项（实现前必须补齐）

- 默认 LLM/Embedding Provider 已决定采用 OpenAI Compatible API（见 `openspec/decisions/0009-llm-default-openai.md`）；仍需补齐默认模型名称与参数（Chat 模型、Embedding 模型/维度）。
- `embedding` 维度（与模型绑定），以及 PGVector 索引类型选择（hnsw/ivfflat）。
- S3 的默认落地环境：AWS S3 vs MinIO（本地/CI）及其初始化方式（bucket、权限策略、生命周期策略）。

## 12. 多平台适配（Coze / Dify / n8n）

> 总体策略见：`openspec/proposals/0002-platform-integration-strategy.md` 与 `openspec/decisions/0006-platform-strategy-core-adapters.md`

### 12.0 范围说明（避免误解）

- 本节仅定义“多平台可选”的设计约束与扩展位，**不要求在 MVP 阶段把 Coze/Dify/n8n 全部落地实现**。
- MVP 阶段默认只落地 1 个平台入口（当前默认：Coze）；Dify/n8n 的实际接入作为增强项后置。

### 12.1 核心原则

- 核心底座对外提供稳定 API 与（可选）Webhook 事件；上层平台作为“客户端/编排器”接入。
- 平台差异通过适配层吸收，不进入核心领域模型。
- 索引与权限过滤的权威来源必须在核心底座，避免出现两套 RAG 真相来源。
- 主路径采用模式 A：最终答案生成由核心底座负责（见 `openspec/decisions/0008-generation-owned-by-core.md`）。

### 12.2 推荐接入模式（设计约定）

- Coze：作为默认 Chat 应用层入口（可替换/可新增），通过适配层调用底座 `/api/chat` 获取最终答案与 citations。
- Dify：作为 AI 应用生命周期与工作流编排层，**仅做编排/会话运营**，通过工具调用底座 `/api/chat` 获取最终答案与 citations（避免平台侧自行生成系统答案）。
- n8n：作为流程自动化编排层，通过 HTTP/Webhook 对接底座 API，实现跨系统同步、告警、工单等流程。

### 12.3 预留扩展（不强制在 MVP 实现）

- Webhook 事件（建议）：索引成功/失败、重试、文档删除等（具体事件名与签名机制另行 spec）。
- 检索工具接口（建议）：对外提供仅检索（不生成）的 API，用于 Dify/n8n 作为工具调用（具体路径与结构另行 spec）。

补充说明：

- Webhook 的事件集合与安全策略见 `openspec/decisions/0007-webhook-events-and-security.md`（实现可后置，但规范不再变化）。

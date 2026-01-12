# 0005：整体模块划分与目录结构提案（单仓库：server + console）

## 背景与问题陈述

当前仓库 `neo-pivot` 已采用单仓库模式，包含：

- `neo-pivot-server/`：Spring Boot 后端（核心底座）
- `neo-pivot-console/`：Vue 管理台（验证入口）
- `docker-compose.yml`：本地依赖（PostgreSQL+PGVector、MinIO）
- `openspec/`：提案/规格/决策

为了在进入功能实现前保持“高扩展性 + 不锁定平台 + 可逐步演进”，需要先统一：

1. 后端模块边界（避免泥球）
2. 仓库目录结构（避免后续频繁重构）
3. 平台适配层的位置与约束（避免把平台字段/逻辑污染到核心）

## 目标（Goals）

- 采用模块化单体（Modular Monolith）落地清晰的模块边界与依赖方向。
- 单仓库内前后端并存，但构建/运行互不耦合（独立启动、独立依赖）。
- 平台入口（Coze/Dify/n8n）可替换：平台差异限制在“适配层”，核心领域保持平台中立。
- 目录结构对后续引入更多组件（worker、adapter、cli）预留扩展位，但不提前上复杂度。

## 非目标（Non-Goals）

- 不在本提案阶段把单体拆成微服务。
- 不在本提案阶段确定前端 UI 细节（仅确定目录与工程边界）。
- 不在本提案阶段引入过多基础设施（MQ、网关、服务治理平台等后置）。

## 模块划分（后端）

### 模块清单（建议）

以 `com.jelvin.neopivot` 为根包，模块按包/组件划分：

1. `auth`：认证与授权（标准登录、JWT RS256、JWKS、公用 UserContext）
2. `storage`：对象存储（S3 presign、storage_uri 规则、bucket/key 命名与校验）
3. `document`：文档元数据与状态机（UPLOADED/INDEXING/INDEXED/FAILED）、事件发布
4. `ai`：解析/分块/向量化/入库（Embedding Provider 可插拔；默认 OpenAI）
5. `search`：相似度检索（PGVector）、权限过滤（owner）
6. `chat`：RAG 编排（模式A：检索→上下文→LLM→答案+citations）
7. `platform`（可选，建议单独子包/子模块）：平台适配器（Coze/Dify/n8n 的差异吸收层）
8. `common`：通用组件（错误码、统一响应、审计/日志相关工具类）

### 依赖方向（必须约束）

核心依赖建议为：

- `auth` 为基础模块，其他模块可依赖 `auth` 获取当前用户信息
- `storage`、`document`、`search`、`ai` 不得依赖任何平台适配模块
- `chat` 可以依赖 `search` 与 `ai`，但不反向依赖
- `platform` 只能依赖核心底座 API（或调用内部服务），不得反向侵入核心领域模型

## 目录结构（仓库）

### 顶层目录建议

- `neo-pivot-server/`：后端（Spring Boot / Maven）
- `neo-pivot-console/`：管理台（Vue / Vite）
- `docker/`：本地依赖初始化脚本（postgres/minio 等）
- `docker-compose.yml`：本地依赖编排
- `openspec/`：提案/规格/决策
- `docs/`：说明文档与材料

### 后端目录建议（server）

- `neo-pivot-server/src/main/java/com/jelvin/neopivot/`
  - `auth/`
  - `storage/`
  - `document/`
  - `ai/`
  - `search/`
  - `chat/`
  - `platform/`（后置，可选）
  - `common/`
  - `api/`（对外 controller 与 DTO，保持薄层）
- `neo-pivot-server/src/main/resources/`
  - `application.yml`
  - `db/changelog/`（Liquibase）

### 前端目录建议（console）

- `neo-pivot-console/src/`
  - `views/`（登录/文档/Chat）
  - `services/`（auth/http）
  - `components/`（后续增加）
  - `router.ts`

## 增强路线图与扩展点（按模块）

> 说明：本节用于把“后续扩展/增强”在设计层面明确到模块边界里，避免实现阶段为了扩展性过度加复杂度。
>
> 中间件分阶段基线见：`openspec/decisions/0012-middleware-baseline.md`

### P1（MVP 必选）与 P2/P3（增强）的总体原则

- P1：保证闭环可跑通（存储/索引/检索/问答），不引入不必要的基础设施。
- P2：增强稳定性与可观测性，不改变核心 API 语义。
- P3：规模化与企业化能力（MQ、网关、配置与密钥管理等）在有明确触发条件时再引入。

### auth（认证与授权）

- P1（必选）
  - 标准登录 + JWT RS256 + JWKS（已落地骨架）
  - 最小 roles（USER/ADMIN）与 owner 隔离（`sub -> owner_id`）
- P2（增强）
  - Redis 限流（登录防爆破）、失败审计、IP 黑白名单
  - Token Exchange（方案C）作为平台能力受限时的企业化形态
- P3（企业化）
  - 对接企业 IdP（Keycloak/Azure AD 等）、MFA、细粒度 RBAC/ABAC

### storage（对象存储）

- P1（必选）
  - S3 presigned PUT（直传）+ `storage_uri` 统一协议（`s3://bucket/key`）
  - bucket/key 命名与校验（防止越权写入）
- P2（增强）
  - Multipart upload、上传大小/类型白名单、病毒扫描/内容安全（如需）
  - 对象生命周期策略（归档/清理）
- P3（企业化）
  - KMS 加密、跨区域复制、合规审计（按场景）

### document（文档元数据与状态机）

- P1（必选）
  - `documents` 元数据 + 状态机（UPLOADED/INDEXING/INDEXED/FAILED）
  - 事件发布 `DocumentUploadedEvent`（应用内事件）
- P2（增强）
  - SHA256 去重/秒传、版本管理、软删除与恢复
  - 索引失败重试与可观测的重放能力
- P3（企业化）
  - 多租户（Tenant）隔离、数据保留策略与合规导出

### ai（解析/分块/向量化/入库）

- P1（必选）
  - 文档解析（先支持少量格式）+ 分块策略 + Embedding 生成 + 向量入库（PGVector）
  - Provider 可插拔，默认 OpenAI（`openspec/decisions/0009-llm-default-openai.md`）
- P2（增强）
  - 更丰富解析器（如 Tika）、更可控分块策略（按段落/语义）、rerank（如需）
  - 索引异步执行的增强：任务队列化/并发控制（仍可先不用 MQ）
- P3（企业化）
  - Outbox + MQ（Kafka/RabbitMQ）实现跨进程可靠投递
  - 索引 worker 进程独立部署（仍可保持单仓库）

### search（检索）

- P1（必选）
  - PGVector 相似度检索 + owner 过滤
- P2（增强）
  - 混合检索（BM25 + 向量）、多条件 metadata filter、rerank
  - 热点缓存（Redis）与限流
- P3（企业化）
  - 多向量索引/多模型并存与迁移策略（embedding 维度变更）

### chat（RAG 编排）

- P1（必选）
  - 模式A：检索→上下文→LLM→答案+citations（`openspec/decisions/0008-generation-owned-by-core.md`）
- P2（增强）
  - Prompt 模板化、对话记忆（可控）、答案质量评测（offline eval）
  - 可观测性增强（trace/metrics/logs）
- P3（企业化）
  - 策略化路由（按场景选择模型/检索策略）、灰度与回滚

### platform（平台适配层，可选）

- P1（必选）
  - 仅定义边界与接入方式（工具调用/HTTP API），不要求全部平台落地
- P2（增强）
  - Coze/Dify/n8n 的适配器与可复现接入说明
  - Webhook 事件投递与安全策略（见 `openspec/decisions/0007-webhook-events-and-security.md`）
- P3（企业化）
  - API 网关统一鉴权/限流/审计（Kong/APISIX/SCG 等）

### common（通用能力）

- P1（必选）
  - 统一错误码与响应结构（后续实现时补齐）
- P2（增强）
  - OpenTelemetry、Prometheus、结构化日志
- P3（企业化）
  - 全链路审计入库、合规报表

## 里程碑（Milestones）

- M1：模块边界与目录结构提案评审通过
- M2：在代码中按模块移动/归位（仅骨架，不做业务逻辑扩张）
- M3：为每个模块补齐对应的 OpenSpec spec/decision（按需）

## 验收标准（Acceptance Criteria）

- 新增业务能力时能明确归属到某个模块，且依赖方向符合约束。
- 平台适配（Coze/Dify/n8n）不会导致核心领域模型变更（或变更最小）。
- `neo-pivot-server/` 与 `neo-pivot-console/` 可以独立启动与演进。

## 开放问题（Open Questions）

- 是否引入 Spring Modulith 的显式模块声明（注解/测试）来强制边界？（建议后续 decision）
- `platform` 适配层是否拆为独立 Maven module（`server-adapters`）？（目前可先包级隔离）

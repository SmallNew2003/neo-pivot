# 0006：后端模块边界与包结构规范提案（单模块工程）

## 背景与问题陈述

当前后端代码在包结构上同时存在：

- “按技术分层”的部分（例如集中 `api/` controller）
- “按领域/能力分模块”的部分（例如 `auth/`）

随着后续补齐 `storage/document/ai/search/chat/platform` 等模块，这种混合结构会导致：

- 目录/包不断膨胀，定位成本变高
- 模块边界不清，容易出现跨模块误依赖
- 难以贯彻 `0011` 的模块化单体约束与 `0015` 的“按模块扫描 Mapper”约束

因此需要在进入大规模功能实现前，先固化“模块边界 + 包结构 + 依赖方向”，以保证高扩展性与可演进性。

## 目标（Goals）

- 以“模块化单体”的方式在**单 Maven 模块**内建立清晰模块边界与包结构。
- Controller/DTO/Service/Repository 按模块归位，避免集中式 `api/` 变成新泥球。
- 形成可执行的迁移步骤：重构包结构但不改变业务语义，且可逐步落地。
- 与既有 OpenSpec 对齐：
  - `0011` 模块化单体
  - `0015` MyBatis-Flex 按模块拆分扫描
  - `0016` storage/presign 安全与审计
  - `0017` 用户/主键策略

## 非目标（Non-Goals）

- 本提案不将仓库拆成多 Maven module（后置讨论）。
- 本提案不引入新中间件，不改变对外 API 语义，不实现新业务能力。
- 本提案不强制引入 DDD 全套复杂度；仅引入“可持续演进所需的最小分层”。

## 范围（Scope）

### 适用范围

- `neo-pivot-server/src/main/java/com/jelvin/neopivot/**`

### 模块清单（后端）

- `common`：通用能力（错误模型、审计/日志约定、工具类、跨模块共享配置）
- `auth`：登录签发、JWT/JWKS、用户与角色
- `storage`：S3/MinIO 适配、presign 签发与审计、对象 key 规则校验
- `document`：文档元数据、状态机、上传确认（消费 presign）、事件发布
- `ai`：解析/分块/向量化/索引（后续）
- `search`：PGVector 检索与权限过滤（后续）
- `chat`：RAG 编排（后续）
- `platform`：平台适配层（Coze/Dify/n8n；后置）

## 方案概述

### 1) 包结构规范（每个模块统一结构）

每个模块使用如下子包（按需取用，不要求一次全部补齐）：

- `<module>.api`：对外 Controller + API DTO（只做参数校验/适配，不承载业务规则）
- `<module>.application`：用例编排（Service/UseCase），串联 domain/persistence/infra
- `<module>.domain`：领域模型与业务规则（尽量无框架依赖）
- `<module>.persistence`：数据库访问（Entity/Mapper/DAO），与 `0015` 对齐
- `<module>.infra`：外部系统适配（S3 client、第三方 SDK 包装等）

示例：

- `com.jelvin.neopivot.document.api`
- `com.jelvin.neopivot.document.application`
- `com.jelvin.neopivot.document.domain`
- `com.jelvin.neopivot.document.persistence`
- `com.jelvin.neopivot.document.infra`

### 2) 依赖方向约束（原则）

- `api` 只依赖本模块 `application`（不直接依赖其他模块）
- `application` 可依赖本模块的 `domain/persistence/infra`；跨模块依赖应尽量通过“稳定接口/事件”而非直接调用内部实现
- `domain` 不依赖其他模块的 `application/persistence/infra`
- `persistence/infra` 可依赖本模块 `domain`（必要时）与通用 `common`

### 3) MyBatis-Flex Mapper 扫描（按模块）

与 `0015` 对齐：每个模块提供自己的持久层配置类，并显式扫描该模块 Mapper：

- `com.jelvin.neopivot.<module>.persistence.<Module>PersistenceConfig`
- `@MapperScan("com.jelvin.neopivot.<module>.persistence.mapper")`

避免全局扫描整个根包，降低跨模块误依赖风险。

### 4) 现状到目标的迁移策略（可逐步）

迁移优先级按“变更少/收益大”排序：

1. 将现有集中式 controller 按模块移动：
   - `com.jelvin.neopivot.api.DocumentController` → `com.jelvin.neopivot.document.api.DocumentController`
   - `com.jelvin.neopivot.api.StorageController` → `com.jelvin.neopivot.storage.api.StorageController`
   - `com.jelvin.neopivot.api.ChatController` → `com.jelvin.neopivot.chat.api.ChatController`
2. DTO 按归属模块移动：
   - 与文档相关 DTO → `document.api.dto`
   - 与 storage presign 相关 DTO → `storage.api.dto`
   - 与 chat 相关 DTO → `chat.api.dto`
3. 按模块补齐 `application/persistence/infra` 的目录（先空壳也可），确保后续新增代码有“放置规则”。
4. 将 `ApiNotImplementedException` 等通用类迁入 `common`（若仍需要骨架占位）。

## 里程碑（Milestones）

1. 完成包结构迁移（不改业务语义）：controller + dto 归位
2. 每个已落地模块补齐最小 `application/persistence` 目录与配置类（按模块扫描）
3. 用新结构继续实现 `/api/storage/presign`、`/api/documents`、`/api/storage/presign-download`

## 风险与对策

- 风险：重构导致 import/包名改动较多，影响联调
  - 对策：分批迁移、一次只动一个模块；每一步都可编译运行
- 风险：模块边界只停留在目录层，仍可能产生强耦合
  - 对策：在实现阶段通过事件/接口隔离，避免跨模块直接调用内部实现

## 验收标准（Acceptance Criteria）

- 后端 `controller` 不再集中在 `com.jelvin.neopivot.api`，而是按模块分布在 `<module>.api`
- MyBatis-Flex Mapper 扫描按模块配置，不使用全局扫描
- 跨模块事件定义有统一落点：`com.jelvin.neopivot.common.events`
- 存在架构约束自动化检查（ArchUnit），用于约束依赖方向与包层次（见 `openspec/decisions/0019-architecture-enforcement-archunit.md`）
- 新增功能时有明确落点（按模块/分层），并能保持结构一致

## 开放问题（Open Questions）

- 暂无（已确定引入 `common.events` 与 ArchUnit 约束）。

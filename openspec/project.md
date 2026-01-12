# Project Context

## Purpose
本项目用于实现一个“企业级 RAG 知识库系统（认知知识枢纽）”的可演示 MVP，并逐步演进为一个可持续迭代的开源项目。

核心目标：

- 以 Java 原生底座体现企业级工程能力：权限隔离、对象存储、向量检索、可观测性、最终一致性。
- 上层平台（Coze/Dify/n8n 等）可插拔：平台仅做入口与编排，不绑定核心底座。
- 主路径采用模式A：最终答案生成由核心底座统一负责（见 `openspec/decisions/0008-generation-owned-by-core.md`）。

## Tech Stack
- Java 21
- Spring Boot 3.x
- Spring Security（JWT RS256 Resource Server）
- MyBatis-Flex（数据访问层）
- Liquibase（数据库 schema 版本化迁移）
- Spring Modulith（模块化单体，规划中/逐步引入）
- Spring AI（LLM/Embedding Provider 抽象与适配，规划中/逐步引入）
- PostgreSQL + PGVector（关系数据 + 向量检索）
- S3（或 S3 兼容对象存储，例如 MinIO）
- 可选：Redis（缓存/限流/幂等辅助，按后续提案决定）
- 可选：Coze / Dify / n8n（上层入口与编排平台，非强绑定）

## Implementation Snapshot（以仓库当前代码为准）

> 该小节用于回答“现在到底用了哪些技术/依赖”，避免只写规划不写落地。

### Backend（`neo-pivot-server/`）

- 已引入依赖（见 `neo-pivot-server/pom.xml`）
  - Web：`spring-boot-starter-web`
  - Validation：`spring-boot-starter-validation`
  - Actuator：`spring-boot-starter-actuator`
  - Security：`spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server`（JWT RS256 验签）
  - DB：`mybatis-flex-spring-boot3-starter` + `postgresql`
  - Migration：`liquibase-core`
- 已实现能力（最小可联调）
  - 标准登录：`POST /api/auth/login`（签发 RS256 JWT）
  - JWKS 发布：`GET /.well-known/jwks.json`
- 骨架占位（当前会返回 501/Not Implemented）
  - `POST /api/storage/presign`、`POST/GET /api/documents`、`POST /api/chat`

### Frontend（`neo-pivot-console/`）

- 技术栈：Vue 3 + Vite + Vue Router（见 `neo-pivot-console/package.json`）
- 已实现页面：Login / Documents（占位）/ Chat（调用后端骨架接口）

### Local Infra（`docker-compose.yml` + `docker/`）

- PostgreSQL：`pgvector/pgvector:pg16`，并在 `docker/postgres/init.sql` 里启用 `vector` 扩展
- MinIO：S3 兼容对象存储（本地演示）
- 约定：`vector` 扩展由 DB 初始化脚本负责；其余表结构通过 Liquibase 迁移管理（见 `openspec/decisions/0014-database-migrations-liquibase.md`）

## Project Conventions

### Code Style
- Java 代码必须添加详细 Javadoc 注释，作者为 Jelvin。
- Java 禁止使用全限定类名，必须使用 import。
- 核心领域与业务编排保持平台中立（平台差异只能在适配层）。

### Architecture Patterns
- 模块化单体：按 `auth/document/ai/search` 等模块拆分边界，模块间优先事件驱动解耦。
- 最终一致性：文档上传与索引异步解耦，状态机可观测。
- 统一权威来源：
  - 权限过滤与检索上下文由底座负责。
  - 最终答案生成由底座负责（模式A）。

### Testing Strategy
- 单元测试：业务规则与领域对象。
- 集成测试：API、数据库（建议 Testcontainers + PostgreSQL/PGVector），S3（可用 MinIO）。
- 平台适配：以契约测试（request/response）为主，避免把平台行为写死在核心测试里。

### Git Workflow
- 建议：小步提交，提交信息可追溯到 OpenSpec 编号（例如 `openspec-0001`）。
- 文档先行：涉及架构/方案/范围变更，优先写 proposal/spec/decision，再进入实现。

## Domain Context
- 文档系统：上传文档存 S3，元数据入 PostgreSQL，索引后写入 PGVector。
- 检索增强生成（RAG）：检索结果（citations）必须可解释、可追踪、可审计。

## Important Constraints
- 默认不把上层平台（Coze/Dify）视为 LLM Provider；它们是应用层入口与编排平台。
- 平台可替换：选择 Coze 仅作为默认入口，不构成平台锁定。
- 安全：密钥与凭证必须通过外部配置注入，禁止写死在仓库中。

## External Dependencies
- OpenAI Compatible API（默认落地可指向 SiliconFlow 等服务，见 `openspec/decisions/0009-llm-default-openai.md`）
- S3 / MinIO（对象存储）
- Coze / Dify / n8n（可选平台入口与编排）

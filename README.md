# neo-pivot

本仓库用于实现“企业级 RAG 知识库系统（认知知识枢纽）”的最小可行版本（MVP），并逐步演进为一个可持续迭代的开源项目。

## 文档入口

- 变更提案与规格：`openspec/AGENTS.md`
- MVP 提案：`openspec/proposals/0001-mvp-rag-knowledge-nexus.md`
- 多平台适配提案：`openspec/proposals/0002-platform-integration-strategy.md`
- 平台鉴权与身份映射提案：`openspec/proposals/0003-platform-auth-and-identity-mapping.md`
- 前端管理台提案：`openspec/proposals/0004-frontend-admin-console.md`
- 模块与目录结构提案：`openspec/proposals/0005-repo-structure-and-modules.md`
- MVP 规格：`openspec/specs/0001-mvp-rag-knowledge-nexus-spec.md`
- 前端管理台规格：`openspec/specs/0002-frontend-admin-console-spec.md`
- 标准登录规格：`openspec/specs/0003-auth-login-spec.md`
- 关键决策：
  - `openspec/decisions/0001-auth-jwt-rs256.md`
  - `openspec/decisions/0002-ai-providers-pluggable.md`
  - `openspec/decisions/0003-storage-pluggable.md`
  - `openspec/decisions/0004-platform-default-coze.md`
  - `openspec/decisions/0005-storage-default-s3.md`
  - `openspec/decisions/0006-platform-strategy-core-adapters.md`
  - `openspec/decisions/0007-webhook-events-and-security.md`
  - `openspec/decisions/0008-generation-owned-by-core.md`
  - `openspec/decisions/0009-llm-default-openai.md`
  - `openspec/decisions/0010-frontend-login-and-s3-presign-put.md`
  - `openspec/decisions/0011-architecture-modular-monolith-not-microservices.md`
  - `openspec/decisions/0012-middleware-baseline.md`
  - `openspec/decisions/0013-database-migrations-flyway.md`
  - `openspec/decisions/0014-database-migrations-liquibase.md`
  - `openspec/decisions/0015-persistence-mybatisflex.md`
  - `openspec/decisions/0016-storage-object-key-and-presign-security.md`
  - `openspec/decisions/0017-primary-key-strategy.md`
  - `openspec/decisions/0018-user-roles-join-table.md`

## 项目结构

- 后端（Spring Boot / Maven）：`neo-pivot-server/`
- 前端管理台（Vue / Vite）：`neo-pivot-console/`
- 本地依赖（PostgreSQL+PGVector / MinIO）：`docker-compose.yml`

## 本地启动（骨架阶段）

1) 启动依赖

- `docker compose up -d`

2) 启动后端

- `mvn -f neo-pivot-server/pom.xml spring-boot:run`

3) 启动前端

- `cd neo-pivot-console && npm i && npm run dev`

默认配置（仅用于本地演示）：

- 登录：
  - 普通用户：`demo / demo`
  - 超管（ADMIN）：`admin / admin`
- API：`http://localhost:8080`
- 前端：`http://localhost:5173`
- PostgreSQL：`localhost:5432`（db=`neo_pivot` user=`neo` password=`neo`）
- MinIO：
  - S3 endpoint：`http://localhost:9000`
  - Console：`http://localhost:9001`
  - root user=`minio` password=`minio123456`
  - bucket=`neo-pivot`

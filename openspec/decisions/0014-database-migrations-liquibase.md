# 0014：数据库 Schema 迁移采用 Liquibase

## 状态

已决定

## 背景

项目需要“可复现、可审计、可回放”的数据库 schema 演进机制，避免依赖运行时自动建表或手工改库带来的环境漂移。

同时，为了让 migration 与“模块化单体逐步演进”匹配，迁移脚本需要具备清晰的版本/变更集组织方式。

## 决策

- 数据库 schema 版本化迁移统一采用 Liquibase（`liquibase-core`）。
- `db/changelog` 作为迁移入口与组织目录：
  - `neo-pivot-server/src/main/resources/db/changelog/db.changelog-master.sql`（Liquibase formatted SQL）

## 约束与约定

- pgvector `vector` 扩展只由数据库初始化脚本负责启用（本仓库默认：`docker/postgres/init.sql`）。
  - 非 Docker/非本地环境需要 DBA 或初始化脚本提前执行 `CREATE EXTENSION vector;`（应用不做兜底启用）
- 除扩展外，其余数据库表结构变更必须通过 Liquibase changeSet 管理（formatted SQL）。
- changeSet 必须可重复回放（在空库上可完整构建），并尽量保持幂等。

## 影响

- 开发/CI 启动时 Liquibase 自动对齐数据库结构，降低环境差异。
- 数据库变更可追溯到对应的 OpenSpec 编号与具体 changeSet。

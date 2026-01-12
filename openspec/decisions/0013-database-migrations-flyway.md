# 0013：数据库 Schema 迁移采用 Flyway（已废弃）

## 状态

已废弃（被 `openspec/decisions/0014-database-migrations-liquibase.md` 取代）

## 背景

本项目 MVP 阶段需要做到：

- 本地可复现：不同开发机/不同时间启动，数据库结构能一致
- 可追踪：数据库结构变化可审计、可回滚（至少可定位到版本）
- 与“模块化单体 + 最终一致性”匹配：允许逐步演进领域模型与表结构

如果依赖 Hibernate `ddl-auto` 自动建表，容易导致：

- 环境差异（不同配置/启动顺序）引发结构不一致
- 变更无法审计（缺少明确的迁移脚本与版本号）
- 生产环境不可控（不应依赖运行时自动改表）

## 决策

- 数据库 schema 版本化迁移统一采用 Flyway（`flyway-core`）。
- 应用侧禁用 Hibernate 自动 DDL：`spring.jpa.hibernate.ddl-auto: none`。

## 约束与约定

- 除 `vector` 扩展外，数据库表结构变更必须通过 Flyway migration 管理：
  - 扩展启用仍通过 `docker/postgres/init.sql`（本地容器初始化）
  - 表结构与索引等通过 `neo-pivot-server/src/main/resources/db/migration/` 下的版本脚本管理
- migration 命名遵循 Flyway 约定，例如：`V0001__init_schema.sql`。
- 迁移脚本需要满足可重复执行与可回放（在空库上能完整构建）。

## 影响

- 开发/CI 启动时 Flyway 会自动对齐数据库结构，降低环境差异成本。
- 后续涉及表结构演进时，需要同步新增 migration 文件，并在 OpenSpec 编号下可追溯。

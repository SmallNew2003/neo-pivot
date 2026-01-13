# 0017：主键策略（BIGSERIAL → 可选 Snowflake）与用户表

## 状态

已决定

## 背景

MVP 需要在 PostgreSQL 上获得稳定、简单且性能友好的主键策略；同时为后续可能的服务拆分与多租户预留演进路径。

此外，认证侧需要一个“内部用户”概念，用于：

- 登录签发 JWT（`sub`）
- 数据隔离（`owner_id`）
- 审计与追踪

## 决策

### 1) MVP 主键（强约束）

- 核心业务表（例如 `users`、`documents`）主键采用 PostgreSQL 自增：`BIGSERIAL`（`BIGINT` 自增）

### 2) 演进：可选 Snowflake（后置）

- 若后续拆分为微服务/多实例写入，需要分布式无冲突 ID，则可切换为 Snowflake：
  - 保留 `BIGINT` 作为物理类型（兼容排序/索引/存储效率）
  - Snowflake 可嵌入租户 ID、机器 ID 等信息，便于多租户与运维定位

### 3) 用户表与 owner_id（强约束）

- 建立 `users` 表（MVP 最小字段见实现阶段 migration）
- 业务表使用 `owner_id BIGINT NOT NULL`，外键关联 `users.id`
- JWT `sub` 必须表示“内部用户 ID”（数值型，以字符串形式存储在 JWT 中），以便资源服务侧解析后映射为 `owner_id`

## 影响

- 需要调整相关 OpenSpec 文档中“`sub` 直接等于 `owner_id`（字符串）”的表述：现在语义为 `sub -> userId(BIGINT) -> owner_id`
- 登录响应的 `user.id` 与 JWT `sub` 取值保持一致（均为内部 `users.id`）


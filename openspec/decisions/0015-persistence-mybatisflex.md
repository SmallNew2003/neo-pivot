# 0015：持久层从 JPA/Hibernate 切换为 MyBatis-Flex

## 状态

已决定

## 背景

MVP 阶段持久层需要：

- SQL 可控、可解释（便于展示与调试）
- 与 PostgreSQL/PGVector 的能力结合更直接（索引、函数、向量检索 SQL 等）
- 降低 ORM 隐式行为带来的不确定性

## 决策

- 持久层默认采用 MyBatis-Flex（`mybatis-flex-spring-boot3-starter`）。
- 不引入 JPA/Hibernate（避免双 ORM 并存带来的复杂度与认知成本）。

## 约束与约定

- 数据访问以 Mapper/QueryWrapper 为主，复杂查询允许直接写 SQL（需可读、可测试）。
- 与权限隔离相关的过滤（例如 `owner_id == sub`）在查询层必须显式体现，避免遗漏。
- 建议落地结构（后端代码目录约定）：
  - 每个业务模块（例如 `auth/document/search`）内部新增 `persistence/` 子包
  - `persistence/entity/`：表对应的 POJO（不使用 JPA 注解）
  - `persistence/mapper/`：MyBatis-Flex Mapper（优先 `BaseMapper<T>`）
  - `persistence/convert/`：Entity 与 API DTO 的转换（如需要，避免 Controller 直接拼装）
- SQL 组织建议：
  - 简单 CRUD 使用 MyBatis-Flex Wrapper/Query API
  - 复杂查询（尤其是 PGVector 检索）允许写明确 SQL，但必须有 owner 过滤与必要索引假设
- 扫描与注册：
  - Mapper 必须可被 Spring 扫描（后续实现时统一用 `@MapperScan` 或 starter 默认扫描策略，二选一并在实现代码中固化）

## 影响

- 需要补充 Mapper 与表模型的组织规范（后续在实现文档或模块结构提案中固化）。

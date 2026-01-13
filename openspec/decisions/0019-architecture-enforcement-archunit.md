# 0019：使用 ArchUnit 自动约束包结构与依赖方向

## 状态

已决定

## 背景

仅靠“目录结构约定”无法长期保证模块边界与依赖方向不被破坏。随着功能增加，容易出现：

- `api` 直接依赖 `persistence`（Controller 拼 SQL/Mapper）
- `common` 反向依赖业务模块，导致公共包被污染
- 模块间出现隐式耦合，破坏模块化单体可演进性

需要一套轻量、可自动化执行的架构约束检查，作为“持续集成里的护栏”。

## 决策

- 引入 ArchUnit（JUnit5）作为架构约束测试框架。
- 在 `neo-pivot-server` 的测试中加入最小规则集，用于：
  - 禁止 `..api..` 依赖 `..persistence..`
  - 禁止 `..persistence..` 依赖 `..api..`
  - 禁止 `..common..` 依赖业务模块（`auth/storage/document/chat/ai/search/platform`）

## 影响

- 每次构建/测试都会执行架构约束检查，提前暴露不符合包结构规范的变更。
- 规则集可随模块演进逐步增强，但 MVP 阶段保持“最小有效”以免阻塞迭代。


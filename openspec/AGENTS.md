# OpenSpec（本仓库规范入口）

本目录用于存放“可评审、可追踪、可落地”的变更提案与规格说明（proposal/spec）。当需要做计划、方案设计、架构调整、引入新能力或可能产生破坏性变更时，优先在此输出文档，再进入代码实现。

## 1. 目录约定

- `openspec/proposals/`：提案（proposal），用于讨论“要做什么、为什么做、范围边界、风险与里程碑”。
- `openspec/specs/`：规格（spec），用于沉淀“怎么做、接口与数据结构、模块边界、验收标准”。
- `openspec/decisions/`：关键决策记录（ADR/Decision），用于记录已做出的选择与权衡（可选）。

## 2. 文档命名

- 提案：`openspec/proposals/NNNN-<kebab-name>.md`
- 规格：`openspec/specs/NNNN-<kebab-name>.md`
- 其中 `NNNN` 为四位递增编号，从 `0001` 开始。

## 3. 提案模板（建议）

提案需要用中文撰写，建议包含以下章节：

- 背景与问题陈述
- 目标（Goals）
- 非目标（Non-Goals）
- 范围（Scope）
- 方案概述（含模块边界/数据流/关键依赖）
- 里程碑（Milestones）
- 风险与对策
- 验收标准（Acceptance Criteria）
- 开放问题（Open Questions）

## 4. 工作流（建议）

1. 新建提案（proposal），在 PR 或评审中确认范围与里程碑。
2. 当提案达成一致后，补充/拆分为规格（spec），明确接口、数据结构与验收标准。
3. 进入代码实现阶段：每个实现提交应能回溯到对应的提案/规格编号。


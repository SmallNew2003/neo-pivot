# 0002：LLM/Embedding 多 Provider 可插拔设计

## 状态

已决定

## 背景

项目需要兼顾：

- 可沟通性：能够讲清楚“如何避免供应商锁定（vendor lock-in）”。
- 可扩展性：未来切换或新增模型/服务方时不改业务编排逻辑。
- 落地效率：MVP 阶段仍需保证实现可控，不引入过多分支复杂度。

## 决策

采用“Provider 可插拔（Pluggable）”设计：

- 业务层仅依赖两类能力抽象：`Chat` 与 `Embedding`。
- 通过配置选择 Provider，实现可在 `OpenAI`、`Azure OpenAI`、`Ollama` 间切换。
- Provider 的差异通过“适配层”吸收，不渗透到文档索引/检索/问答编排逻辑。

说明：

- `Coze` 是上层平台/应用承载与编排入口，不作为底座层的 LLM Provider 纳入本决策；相关内容见平台适配提案与决策。

## 设计要点

- 统一配置入口（示例）：
  - `ai.chat.provider = openai|azure-openai|ollama`
  - `ai.embedding.provider = openai|azure-openai|ollama`
- 统一能力约束：
  - `Embedding` 必须固定维度（由模型决定），并写入数据（用于迁移与排障）。
  - `Chat` 必须支持基于上下文的问答模板与可控输出（最小化幻觉）。
- 统一失败策略：
  - Provider 调用失败要可观测（错误码/耗时/重试策略在实现阶段补齐）。

## 影响与权衡

优点：

- 降低锁定风险，便于展示“工程化抽象能力”。
- 后续扩展（新增 Provider/替换模型）改动面更小。

代价：

- 配置与测试矩阵变大，需要明确“默认落地 Provider”作为主路径。

## 后续工作

- 默认 Provider 已决定采用 OpenAI Compatible API（见 `openspec/decisions/0009-llm-default-openai.md`）。
- 明确 embedding 维度与 PGVector 索引策略（与默认 embedding 模型绑定）。

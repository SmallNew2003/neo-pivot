# 0009：底座默认 LLM/Embedding Provider 采用 OpenAI Compatible API（默认可指向 SiliconFlow）

## 状态

已决定

## 背景

在模式 A（最终答案生成由核心底座负责）的前提下，底座必须选择一个默认 LLM/Embedding Provider 作为 MVP 主路径，保证：

- 可快速跑通闭环（工程落地优先）
- 可控的成本与可观测性
- 未来不锁定单一厂商（高扩展性）

## 决策

- MVP 主路径默认 Provider 采用 `OpenAI Compatible API`（协议与 OpenAI 接近/兼容），包含：
  - Chat（用于问答生成）
  - Embedding（用于文档与 query 向量化）
- 底座必须保留多 Provider 扩展能力，新增 Provider 不得影响核心 API 语义与数据模型。

说明：

- 默认落地可以配置为 OpenAI 官方服务，也可以配置为提供 OpenAI 兼容接口的第三方服务（例如 SiliconFlow）。

## 扩展策略（面向“支持市面上基本所有模型”）

为最大化兼容性，采用两条扩展路径并行：

1. 基于 Spring AI 的 Provider 适配
   - 优先使用 Spring AI 已支持/后续新增的官方 Provider 适配（例如 Azure OpenAI、Ollama 等）。
2. OpenAI 兼容接口（推荐的“通用落地”路径）
   - 对于提供 OpenAI 兼容 API 的服务（同协议/相近协议），优先以“OpenAI Compatible”方式接入，通过配置切换 baseUrl 与模型名，实现快速覆盖更多模型。

## 影响与权衡

优点：

- MVP 落地快：OpenAI Compatible API 作为主路径最容易跑通。
- 扩展性强：Spring AI + OpenAI Compatible 可以覆盖大量“市面模型/网关”。

代价：

- “支持所有模型”在实现上仍需逐个验证能力差异（embedding 维度、流式输出、工具调用等）。

## 后续工作

- 在规格中补齐：默认模型名称与 embedding 维度（与 PGVector schema/索引强相关）。
- 定义 Provider 配置结构与切换策略（含灰度、回滚、审计字段）。

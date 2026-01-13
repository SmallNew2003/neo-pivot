## MODIFIED Requirements

### Requirement: RAG 检索链路可插拔

系统 SHALL 将“Embedding 生成”与“Chunk 召回（检索）”从 Chat 主流程中解耦为可替换组件，以支持后续高级检索能力（Hybrid/过滤/Rerank/Query Rewrite）在不重构 Chat 主链路的情况下迭代。

#### Scenario: 使用默认实现完成检索

- **WHEN** 用户提交 question 调用 `/api/chat`
- **THEN** 系统使用 `EmbeddingProvider` 生成查询向量
- **AND** 系统使用 `ChunkRetriever` 返回 TopK chunk
- **AND** 响应包含可解释的 citations

### Requirement: 向量检索不得混用不同模型

系统 SHALL 在向量检索时按 `model` 过滤 embedding 记录，避免不同 embedding 模型的向量在同一检索请求中混检造成排序失真。

#### Scenario: 查询向量指定模型时仅检索同模型向量

- **WHEN** `EmbeddingProvider` 返回 `model=A`
- **THEN** 系统仅使用 `model=A` 的 embeddings 参与相似度排序


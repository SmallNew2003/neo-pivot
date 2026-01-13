# Change: RAG 检索解耦（EmbeddingProvider + ChunkRetriever）

## 里程碑

- v0.1.0（2026-01-13）：完成抽象拆分与主链路接入，并归档本提案。

## 修订记录

- v0.1.0（2026-01-13）：初版归档，补齐 OpenSpec 变更记录与验收基线说明。

## Why

当前 RAG 主链路已实现“分块入库 + 向量检索 + citations 返回”的最小闭环，但 Embedding 生成与检索策略存在耦合点，后续引入高级检索（Hybrid/过滤/Rerank/Query Rewrite）时容易被迫大改。需要先把变化最频繁的部分抽象出来，降低后续重构风险。

## What Changes

- 新增 Embedding 抽象：`EmbeddingProvider` 输出 `EmbeddingResult(model, dimension, vectorLiteral)`
- 新增检索抽象：`ChunkRetriever`，默认实现为 `PgVectorChunkRetriever`
- Chat 检索链路改为依赖抽象接口：`EmbeddingProvider -> ChunkRetriever -> citations`
- 索引写入链路改为依赖抽象接口：chunk content -> `EmbeddingProvider` -> embeddings 表写入
- **行为变更**：向量检索增加 `model` 过滤，避免不同模型向量混检

## Impact

- Affected specs:
  - `rag`（检索与 citations 主链路的扩展性）
- Affected code:
  - `neo-pivot-server`：`ai/application/*`、`chat/application/*`
  - 数据访问：`DocumentChunkEmbeddingMapper.searchTopK` 方法签名与 SQL 条件增加 model 过滤

## Notes

本变更在工作区已完成实现，用于补齐 OpenSpec 变更记录与后续迭代的验收基线；若需继续推进高级检索，将基于本次抽象新增实现而非重写主链路。

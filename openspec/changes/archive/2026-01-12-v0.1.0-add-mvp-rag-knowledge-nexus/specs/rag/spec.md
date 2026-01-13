## ADDED Requirements

### Requirement: RAG MVP 闭环
系统 SHALL 支持一个可演示的 RAG 知识库闭环：上传文档 → 异步索引（解析/分块/向量化入库）→ 相似度检索 → 基于上下文问答。

#### Scenario: 上传后完成问答闭环
- **WHEN** 用户上传一份支持的文档并等待索引完成
- **THEN** 用户可以发起提问并得到基于检索上下文的回答


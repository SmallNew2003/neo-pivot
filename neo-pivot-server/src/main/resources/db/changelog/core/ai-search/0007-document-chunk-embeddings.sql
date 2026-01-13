--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库变更（core/ai-search）
--
--changeset Jelvin:0007-create-document-chunk-embeddings
--comment: 创建向量数据表（document_chunk_embeddings），用于 PGVector 相似度检索。
CREATE TABLE IF NOT EXISTS document_chunk_embeddings
(
    id         BIGSERIAL PRIMARY KEY,
    chunk_id   BIGINT      NOT NULL REFERENCES document_chunks (id) ON DELETE CASCADE,
    owner_id   BIGINT      NOT NULL REFERENCES users (id),
    embedding  vector(32)  NOT NULL,
    model      TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_document_chunk_embeddings_owner_id ON document_chunk_embeddings (owner_id);
CREATE INDEX IF NOT EXISTS idx_document_chunk_embeddings_chunk_id ON document_chunk_embeddings (chunk_id);

COMMENT ON TABLE document_chunk_embeddings IS '文档分块向量表：保存 chunk embedding，用于相似度检索与权限过滤。';
COMMENT ON COLUMN document_chunk_embeddings.id IS '向量记录主键（自增）。';
COMMENT ON COLUMN document_chunk_embeddings.chunk_id IS '关联分块 ID（外键指向 document_chunks.id）。';
COMMENT ON COLUMN document_chunk_embeddings.owner_id IS '所属用户 ID（冗余字段，便于过滤与索引）。';
COMMENT ON COLUMN document_chunk_embeddings.embedding IS 'embedding 向量（PGVector vector(32)）。';
COMMENT ON COLUMN document_chunk_embeddings.model IS 'embedding 模型标识（可选）。';
COMMENT ON COLUMN document_chunk_embeddings.created_at IS '创建时间（服务端写入）。';
COMMENT ON INDEX idx_document_chunk_embeddings_owner_id IS '按 owner_id 过滤向量的索引。';
COMMENT ON INDEX idx_document_chunk_embeddings_chunk_id IS '按 chunk_id 关联向量的索引。';


--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库变更（core/document）
--
--changeset Jelvin:0006-create-document-chunks
--comment: 创建文档分块表（document_chunks），用于引用片段与索引数据存储。
CREATE TABLE IF NOT EXISTS document_chunks
(
    id           BIGSERIAL PRIMARY KEY,
    document_id  BIGINT      NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    owner_id     BIGINT      NOT NULL REFERENCES users (id),
    chunk_index  INT         NOT NULL,
    content      TEXT        NOT NULL,
    content_hash TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_document_chunks_owner_id ON document_chunks (owner_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_document_id ON document_chunks (document_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_document_chunks_document_id_chunk_index ON document_chunks (document_id, chunk_index);

COMMENT ON TABLE document_chunks IS '文档分块表：保存可引用的 chunk 文本与元数据，用于 citations 与调试。';
COMMENT ON COLUMN document_chunks.id IS '分块主键（自增）。';
COMMENT ON COLUMN document_chunks.document_id IS '关联文档 ID（外键指向 documents.id）。';
COMMENT ON COLUMN document_chunks.owner_id IS '所属用户 ID（便于检索过滤与权限隔离）。';
COMMENT ON COLUMN document_chunks.chunk_index IS '分块序号（从 0 开始）。';
COMMENT ON COLUMN document_chunks.content IS 'chunk 纯文本内容。';
COMMENT ON COLUMN document_chunks.content_hash IS 'chunk 内容哈希（可选，用于去重/校验）。';
COMMENT ON COLUMN document_chunks.created_at IS '创建时间（服务端写入）。';
COMMENT ON INDEX idx_document_chunks_owner_id IS '按 owner_id 过滤分块的索引。';
COMMENT ON INDEX idx_document_chunks_document_id IS '按 document_id 查询分块的索引。';
COMMENT ON INDEX uk_document_chunks_document_id_chunk_index IS '确保同一文档同一分块序号唯一。';


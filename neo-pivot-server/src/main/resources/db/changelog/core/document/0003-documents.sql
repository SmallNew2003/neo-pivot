--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库变更（core/document）
--
--changeset Jelvin:0003-create-documents
--comment: 创建文档表（documents），用于记录文档元数据与状态机（后续用于索引/检索链路）。
CREATE TABLE IF NOT EXISTS documents
(
    id            BIGSERIAL PRIMARY KEY,
    owner_id      BIGINT      NOT NULL REFERENCES users (id),
    filename      TEXT        NOT NULL,
    content_type  TEXT        NOT NULL,
    size_bytes    BIGINT,
    sha256        TEXT,
    storage_uri   TEXT        NOT NULL,
    status        TEXT        NOT NULL,
    error_message TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_documents_owner_id ON documents (owner_id);
CREATE INDEX IF NOT EXISTS idx_documents_status ON documents (status);

COMMENT ON TABLE documents IS '文档表：记录文档元数据、对象存储地址与处理状态，用于后续索引与检索。';
COMMENT ON COLUMN documents.id IS '文档主键（自增）。';
COMMENT ON COLUMN documents.owner_id IS '文档所属用户 ID（外键指向 users.id）。';
COMMENT ON COLUMN documents.filename IS '原始文件名（展示用）。';
COMMENT ON COLUMN documents.content_type IS '文件 MIME 类型（如 application/pdf）。';
COMMENT ON COLUMN documents.size_bytes IS '文件大小（字节）。';
COMMENT ON COLUMN documents.sha256 IS '文件内容 SHA-256（十六进制字符串），用于去重/一致性校验。';
COMMENT ON COLUMN documents.storage_uri IS '对象存储 URI（如 s3://bucket/key），作为文件内容的唯一定位。';
COMMENT ON COLUMN documents.status IS '文档处理状态（预期：UPLOADED/INDEXING/INDEXED/FAILED；以应用层状态机为准）。';
COMMENT ON COLUMN documents.error_message IS '失败原因（当 status=FAILED 时记录）。';
COMMENT ON COLUMN documents.created_at IS '创建时间（服务端写入）。';
COMMENT ON COLUMN documents.updated_at IS '更新时间（服务端写入）。';
COMMENT ON INDEX idx_documents_owner_id IS '按 owner_id 查询文档列表的索引。';
COMMENT ON INDEX idx_documents_status IS '按 status 过滤/统计文档的索引。';


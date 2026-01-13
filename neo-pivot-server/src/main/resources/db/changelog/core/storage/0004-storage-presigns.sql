--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库变更（core/storage）
--
--changeset Jelvin:0004-create-storage-presigns
--comment: 创建对象存储预签名记录表（storage_presigns），用于审计 presigned URL 的签发与消费。
CREATE TABLE IF NOT EXISTS storage_presigns
(
    id                BIGSERIAL PRIMARY KEY,
    owner_id          BIGINT      NOT NULL REFERENCES users (id),
    document_id       BIGINT      NOT NULL,
    purpose           TEXT        NOT NULL,
    method            TEXT        NOT NULL,
    storage_uri       TEXT        NOT NULL,
    status            TEXT        NOT NULL,
    expires_at        TIMESTAMPTZ NOT NULL,
    issued_ip         INET,
    issued_user_agent TEXT,
    consumed_at       TIMESTAMPTZ,
    consumed_ip       INET,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_storage_presigns_owner_id ON storage_presigns (owner_id);
CREATE INDEX IF NOT EXISTS idx_storage_presigns_status_expires ON storage_presigns (status, expires_at);

COMMENT ON TABLE storage_presigns IS '对象存储预签名审计表：记录 presigned URL 的签发参数、有效期与消费痕迹。';
COMMENT ON COLUMN storage_presigns.id IS '预签名记录主键（自增）。';
COMMENT ON COLUMN storage_presigns.owner_id IS '所属用户 ID（外键指向 users.id）。';
COMMENT ON COLUMN storage_presigns.document_id IS '关联文档 ID（逻辑外键，后续可按业务需要补充 FK 到 documents.id）。';
COMMENT ON COLUMN storage_presigns.purpose IS '预签名用途（如 UPLOAD/DOWNLOAD/CONFIRM 等；以业务枚举为准）。';
COMMENT ON COLUMN storage_presigns.method IS 'HTTP 方法（如 PUT/GET）。';
COMMENT ON COLUMN storage_presigns.storage_uri IS '对象存储 URI（如 s3://bucket/key），与预签名 URL 对应。';
COMMENT ON COLUMN storage_presigns.status IS '预签名状态（如 ISSUED/CONSUMED/EXPIRED/REVOKED；以业务枚举为准）。';
COMMENT ON COLUMN storage_presigns.expires_at IS '过期时间（超过后不得再使用）。';
COMMENT ON COLUMN storage_presigns.issued_ip IS '签发时客户端 IP（用于审计，可为空）。';
COMMENT ON COLUMN storage_presigns.issued_user_agent IS '签发时客户端 UA（用于审计，可为空）。';
COMMENT ON COLUMN storage_presigns.consumed_at IS '消费时间（首次成功使用时写入，可为空）。';
COMMENT ON COLUMN storage_presigns.consumed_ip IS '消费时客户端 IP（用于审计，可为空）。';
COMMENT ON COLUMN storage_presigns.created_at IS '记录创建时间（服务端写入）。';
COMMENT ON INDEX idx_storage_presigns_owner_id IS '按 owner_id 查询预签名记录的索引。';
COMMENT ON INDEX idx_storage_presigns_status_expires IS '按 status 与 expires_at 扫描过期/可用记录的索引。';


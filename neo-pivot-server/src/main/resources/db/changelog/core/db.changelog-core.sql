--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql

-- Neo Pivot 数据库主变更日志（core）
-- - 用途：定义系统内置（core）表结构与本地开发环境的演示数据
-- - 说明：每个 changeset 都应包含脚本注释（--comment），并尽量补齐数据库元数据注释（COMMENT ON ...）

--changeset Jelvin:0000-baseline
--comment: 基线 changeset，用于验证 Liquibase 管道可用。
SELECT 1;

--changeset Jelvin:0001-create-users
--comment: 创建用户表（users），用于用户名/密码登录与 JWT subject 绑定。
CREATE TABLE IF NOT EXISTS users
(
    id            BIGSERIAL PRIMARY KEY,
    username      TEXT        NOT NULL UNIQUE,
    password_hash TEXT        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE users IS '系统用户表：用于用户名/密码登录、角色绑定与 JWT subject（sub）映射。';
COMMENT ON COLUMN users.id IS '用户主键（自增）。';
COMMENT ON COLUMN users.username IS '登录用户名（全局唯一）。';
COMMENT ON COLUMN users.password_hash IS '密码哈希（推荐 bcrypt/argon2；禁止存明文）。';
COMMENT ON COLUMN users.created_at IS '创建时间（服务端写入）。';
COMMENT ON COLUMN users.updated_at IS '更新时间（服务端写入）。';

--changeset Jelvin:0002-create-user-roles
--comment: 创建用户角色关联表（user_roles），用于在 JWT claims 中携带 roles。
CREATE TABLE IF NOT EXISTS user_roles
(
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role    TEXT   NOT NULL,
    PRIMARY KEY (user_id, role)
);

COMMENT ON TABLE user_roles IS '用户-角色关联表：一个用户可拥有多个角色；用于鉴权与 JWT roles claim。';
COMMENT ON COLUMN user_roles.user_id IS '用户 ID（外键指向 users.id）。';
COMMENT ON COLUMN user_roles.role IS '角色编码（如 USER/ADMIN）。';

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

--changeset Jelvin:0100-seed-demo-admin context:local
--comment: 本地环境演示账号与角色（仅用于 local 环境；真实环境请替换/禁用）。
INSERT INTO users (id, username, password_hash)
VALUES (1, 'demo', '$2y$10$DDxIaejxNt3b0D1Bd7Nv2.P9vn2IfQ9Bk7xRlc6rybN/X8uFaDjKa')
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, username, password_hash)
VALUES (2, 'admin', '$2a$10$Dud3nloZZ7IZPQmth4UlreNuOXs7uziJpM/KC.4rTEb4AygfAyfJm')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role)
VALUES (1, 'USER')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
VALUES (2, 'USER')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
VALUES (2, 'ADMIN')
ON CONFLICT DO NOTHING;

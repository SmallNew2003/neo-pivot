--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库变更（core/auth）
--
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


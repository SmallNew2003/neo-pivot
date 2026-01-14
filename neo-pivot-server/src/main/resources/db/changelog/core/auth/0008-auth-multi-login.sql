--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库变更（core/auth）
--
--changeset Jelvin:0008-create-external-identities
--comment: 创建外部身份映射表（external_identities），用于第三方登录与账号绑定（provider + external_subject -> user_id）。
CREATE TABLE IF NOT EXISTS external_identities
(
    id               BIGSERIAL PRIMARY KEY,
    tenant_code      TEXT        NOT NULL DEFAULT 'default',
    provider         TEXT        NOT NULL,
    external_subject TEXT        NOT NULL,
    user_id          BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_external_identities_tenant_provider_subject
    ON external_identities (tenant_code, provider, external_subject);
CREATE INDEX IF NOT EXISTS idx_external_identities_user_id ON external_identities (user_id);

COMMENT ON TABLE external_identities IS '外部身份映射表：按租户隔离的外部主体（provider+external_subject）到内部用户（user_id）映射。';
COMMENT ON COLUMN external_identities.id IS '主键（自增）。';
COMMENT ON COLUMN external_identities.tenant_code IS '租户标识（MVP：tenantCode 字符串；默认 default）。';
COMMENT ON COLUMN external_identities.provider IS '身份提供方（如 WECHAT/SMS_OTP/EMAIL_OTP）。';
COMMENT ON COLUMN external_identities.external_subject IS '外部主体标识（如 openid/unionid/phone/email）。';
COMMENT ON COLUMN external_identities.user_id IS '内部用户 ID（外键指向 users.id）。';
COMMENT ON COLUMN external_identities.created_at IS '创建时间（服务端写入）。';

--changeset Jelvin:0009-create-otp-challenges
--comment: 创建 OTP challenge 表（otp_challenges），用于验证码 Challenge/Verify、一次性使用与失败次数限制。
CREATE TABLE IF NOT EXISTS otp_challenges
(
    id              BIGSERIAL PRIMARY KEY,
    tenant_code     TEXT        NOT NULL DEFAULT 'default',
    channel         TEXT        NOT NULL,
    target          TEXT        NOT NULL,
    challenge_id    TEXT        NOT NULL,
    code_hash       TEXT        NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    used_at         TIMESTAMPTZ,
    verify_attempts INT         NOT NULL DEFAULT 0,
    last_sent_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_otp_challenges_challenge_id ON otp_challenges (challenge_id);
CREATE INDEX IF NOT EXISTS idx_otp_challenges_tenant_channel_target_created_at
    ON otp_challenges (tenant_code, channel, target, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_otp_challenges_expires_at ON otp_challenges (expires_at);

COMMENT ON TABLE otp_challenges IS 'OTP challenge 表：记录验证码发送与校验的挑战信息（含 TTL、一次性使用、失败次数）。';
COMMENT ON COLUMN otp_challenges.id IS '主键（自增）。';
COMMENT ON COLUMN otp_challenges.tenant_code IS '租户标识（MVP：tenantCode 字符串；默认 default）。';
COMMENT ON COLUMN otp_challenges.channel IS '验证码通道（SMS/EMAIL）。';
COMMENT ON COLUMN otp_challenges.target IS '目标地址（手机号或邮箱）。';
COMMENT ON COLUMN otp_challenges.challenge_id IS '挑战标识（服务端生成，供排障与追溯）。';
COMMENT ON COLUMN otp_challenges.code_hash IS '验证码哈希（bcrypt）；禁止存明文。';
COMMENT ON COLUMN otp_challenges.expires_at IS '过期时间（服务端写入）。';
COMMENT ON COLUMN otp_challenges.used_at IS '使用时间（成功校验后写入）；非空表示已一次性消费。';
COMMENT ON COLUMN otp_challenges.verify_attempts IS '校验失败次数（用于风控与锁定）。';
COMMENT ON COLUMN otp_challenges.last_sent_at IS '最后一次发送时间（用于频控）。';
COMMENT ON COLUMN otp_challenges.created_at IS '创建时间（服务端写入）。';
COMMENT ON COLUMN otp_challenges.updated_at IS '更新时间（服务端写入）。';

--changeset Jelvin:0010-create-login-audit-logs
--comment: 创建登录行为日志表（login_audit_logs），用于审计、风控与问题排查（关联 traceId、IP、User-Agent）。
CREATE TABLE IF NOT EXISTS login_audit_logs
(
    id             BIGSERIAL PRIMARY KEY,
    tenant_code    TEXT        NOT NULL DEFAULT 'default',
    grant_type     TEXT        NOT NULL,
    provider       TEXT,
    user_id        BIGINT REFERENCES users (id),
    success        BOOLEAN     NOT NULL,
    failure_reason TEXT,
    target         TEXT,
    trace_id       TEXT,
    ip             INET,
    user_agent     TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_login_audit_logs_user_id ON login_audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_login_audit_logs_tenant_created_at ON login_audit_logs (tenant_code, created_at DESC);

COMMENT ON TABLE login_audit_logs IS '登录行为日志表：记录登录成功/失败、请求上下文与 traceId，用于审计与风控。';
COMMENT ON COLUMN login_audit_logs.id IS '主键（自增）。';
COMMENT ON COLUMN login_audit_logs.tenant_code IS '租户标识（MVP：tenantCode 字符串；默认 default）。';
COMMENT ON COLUMN login_audit_logs.grant_type IS '登录方式（grantType，如 PASSWORD/SMS_OTP/EMAIL_OTP/WECHAT_CODE）。';
COMMENT ON COLUMN login_audit_logs.provider IS '外部身份提供方（如 WECHAT/SMS_OTP/EMAIL_OTP）。';
COMMENT ON COLUMN login_audit_logs.user_id IS '内部用户 ID（可空；失败场景可能无法解析）。';
COMMENT ON COLUMN login_audit_logs.success IS '是否成功。';
COMMENT ON COLUMN login_audit_logs.failure_reason IS '失败原因（内部审计用）；对外响应需保持不可枚举。';
COMMENT ON COLUMN login_audit_logs.target IS '登录目标标识（用户名/手机号/邮箱/外部 subject 等）。';
COMMENT ON COLUMN login_audit_logs.trace_id IS '链路追踪 ID（来自 X-Request-Id 或服务端生成）。';
COMMENT ON COLUMN login_audit_logs.ip IS '来源 IP（请求 remoteAddr）。';
COMMENT ON COLUMN login_audit_logs.user_agent IS 'User-Agent。';
COMMENT ON COLUMN login_audit_logs.created_at IS '创建时间（服务端写入）。';


--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库变更（core/local seed）
--
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


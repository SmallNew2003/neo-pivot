--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库主变更日志（core）
-- - 用途：定义系统内置（core）表结构与本地开发环境的演示数据
-- - 说明：每个 changeset 都应包含脚本注释（--comment），并尽量补齐数据库元数据注释（COMMENT ON ...）
--
--changeset Jelvin:0000-baseline
--comment: 基线 changeset，用于验证 Liquibase 管道可用。
SELECT 1;


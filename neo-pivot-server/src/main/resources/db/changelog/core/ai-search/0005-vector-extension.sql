--liquibase formatted sql
--logicalFilePath:db/changelog/db.changelog-master.sql
--
-- Neo Pivot 数据库变更（core/ai-search）
--
--changeset Jelvin:0005-create-vector-extension context:local
--comment: 启用 PGVector 扩展（本地/演示环境）。如生产环境权限受限，可通过初始化脚本启用。
CREATE EXTENSION IF NOT EXISTS vector;


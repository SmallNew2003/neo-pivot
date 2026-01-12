--liquibase formatted sql
--logicalFilePath:db/changelog/custom/db.changelog-custom.sql

-- Neo Pivot 数据库变更日志（custom）
-- - 用途：供“二开/业务定制”追加迁移脚本，避免直接修改 core 目录产生长期合并冲突
-- - 建议：二开脚本统一加 context:custom，在需要启用的环境配置 `spring.liquibase.contexts` 包含 custom

--changeset custom:0000-placeholder context:custom
--comment: custom 目录占位 changeset（确保 master include 后结构清晰；可按需删除/替换）。
SELECT 1;

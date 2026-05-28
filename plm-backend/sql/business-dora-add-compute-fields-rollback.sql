-- @no-menu: 回滚 P0-3B 聚合字段 + Quartz 任务
-- =============================================================================
-- DORA 真聚合 — 回滚 (Proposal 0028 P0-3B)
-- 反向逆操作:删 sys_job seed → 删索引 → 删 5 列
-- =============================================================================

-- ---------- 1. 删 sys_job seed ----------
DELETE FROM sys_job WHERE job_name = 'DORA 全量项目指标聚合' AND job_group = 'DEFAULT';

-- ---------- 2. 删索引(幂等) ----------
SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND INDEX_NAME = 'idx_dora_project_period') > 0,
    "ALTER TABLE tb_dora_metric DROP INDEX idx_dora_project_period",
    "SELECT 'idx_dora_project_period not exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 3. 删 5 列(幂等) ----------
SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'computed_at') > 0,
    "ALTER TABLE tb_dora_metric DROP COLUMN computed_at",
    "SELECT 'computed_at not exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'is_computed') > 0,
    "ALTER TABLE tb_dora_metric DROP COLUMN is_computed",
    "SELECT 'is_computed not exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'period_days') > 0,
    "ALTER TABLE tb_dora_metric DROP COLUMN period_days",
    "SELECT 'period_days not exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'period_end') > 0,
    "ALTER TABLE tb_dora_metric DROP COLUMN period_end",
    "SELECT 'period_end not exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'period_start') > 0,
    "ALTER TABLE tb_dora_metric DROP COLUMN period_start",
    "SELECT 'period_start not exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

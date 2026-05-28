-- @no-menu: 字段扩展回滚,无菜单变更
-- =============================================================================
-- tb_testreport 回滚聚合元数据字段 — Proposal 0028 P0-3A
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-testreport-add-aggregate-fields-rollback.sql
-- 幂等：检测后再删
-- =============================================================================

-- 删索引 idx_testreport_testplan_id（如果存在）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_testreport' AND INDEX_NAME = 'idx_testreport_testplan_id');
SET @sql := IF(@idx_exists > 0,
    'ALTER TABLE tb_testreport DROP INDEX idx_testreport_testplan_id',
    'SELECT ''Index idx_testreport_testplan_id does not exist, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删列 is_manual_override（如果存在）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_testreport' AND COLUMN_NAME = 'is_manual_override');
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE tb_testreport DROP COLUMN is_manual_override',
    'SELECT ''Column is_manual_override does not exist on tb_testreport, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删列 aggregated_at（如果存在）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_testreport' AND COLUMN_NAME = 'aggregated_at');
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE tb_testreport DROP COLUMN aggregated_at',
    'SELECT ''Column aggregated_at does not exist on tb_testreport, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删列 is_aggregated（如果存在）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_testreport' AND COLUMN_NAME = 'is_aggregated');
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE tb_testreport DROP COLUMN is_aggregated',
    'SELECT ''Column is_aggregated does not exist on tb_testreport, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

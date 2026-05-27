-- =============================================================================
-- tb_prd 回滚 requirement_id 反向关联列
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-prd-add-requirement-id-rollback.sql
-- 幂等：检测后再删
-- =============================================================================

-- 删索引（如果存在）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_prd' AND INDEX_NAME = 'idx_prd_requirement');
SET @sql := IF(@idx_exists > 0,
    'ALTER TABLE tb_prd DROP INDEX idx_prd_requirement',
    'SELECT ''Index idx_prd_requirement does not exist, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删列（如果存在）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_prd' AND COLUMN_NAME = 'requirement_id');
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE tb_prd DROP COLUMN requirement_id',
    'SELECT ''Column requirement_id does not exist on tb_prd, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

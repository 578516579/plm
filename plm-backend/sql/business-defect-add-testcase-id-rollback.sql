-- @no-menu: 字段扩展回滚,无菜单变更
-- =============================================================================
-- tb_defect 回滚 testcase_id 溯源列
-- 关联：proposal 0028 P0-1 (b) rollback / 配 business-defect-add-testcase-id.sql
-- 日期: 2026-05-28
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-defect-add-testcase-id-rollback.sql
-- 幂等：检测后再删
-- =============================================================================

-- 删索引（如果存在）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_defect' AND INDEX_NAME = 'idx_defect_testcase');
SET @sql := IF(@idx_exists > 0,
    'ALTER TABLE tb_defect DROP INDEX idx_defect_testcase',
    'SELECT ''Index idx_defect_testcase does not exist, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删列（如果存在）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_defect' AND COLUMN_NAME = 'testcase_id');
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE tb_defect DROP COLUMN testcase_id',
    'SELECT ''Column testcase_id does not exist on tb_defect, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

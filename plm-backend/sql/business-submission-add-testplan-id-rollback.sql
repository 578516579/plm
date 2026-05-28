-- @no-menu: 字段扩展回滚,无菜单变更
-- =============================================================================
-- tb_submission 回滚 testplan_id 关联列
-- 关联：proposal 0028 P0-1 (a) rollback / 配 business-submission-add-testplan-id.sql
-- 日期: 2026-05-28
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-submission-add-testplan-id-rollback.sql
-- 幂等：检测后再删
-- =============================================================================

-- 删索引（如果存在）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_submission' AND INDEX_NAME = 'idx_submission_testplan');
SET @sql := IF(@idx_exists > 0,
    'ALTER TABLE tb_submission DROP INDEX idx_submission_testplan',
    'SELECT ''Index idx_submission_testplan does not exist, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删列（如果存在）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_submission' AND COLUMN_NAME = 'testplan_id');
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE tb_submission DROP COLUMN testplan_id',
    'SELECT ''Column testplan_id does not exist on tb_submission, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

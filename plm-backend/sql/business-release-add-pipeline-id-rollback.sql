-- @no-menu: 字段扩展回滚,无菜单变更
-- =============================================================================
-- tb_release 回滚 pipeline_id 关联列
-- 关联：proposal 0028 P0-1 (c) rollback / 配 business-release-add-pipeline-id.sql
-- 日期: 2026-05-28
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-release-add-pipeline-id-rollback.sql
-- 幂等：检测后再删
-- =============================================================================

-- 删索引（如果存在）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_release' AND INDEX_NAME = 'idx_release_pipeline');
SET @sql := IF(@idx_exists > 0,
    'ALTER TABLE tb_release DROP INDEX idx_release_pipeline',
    'SELECT ''Index idx_release_pipeline does not exist, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删列（如果存在）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_release' AND COLUMN_NAME = 'pipeline_id');
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE tb_release DROP COLUMN pipeline_id',
    'SELECT ''Column pipeline_id does not exist on tb_release, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

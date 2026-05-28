-- @no-menu: 字段扩展回滚,无菜单变更
-- =============================================================================
-- tb_pipeline 回滚 release_id 反向关联列
-- 关联：proposal 0028 P0-1 (d) rollback / 配 business-pipeline-add-release-id.sql
-- 日期: 2026-05-28
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-pipeline-add-release-id-rollback.sql
-- 幂等：检测后再删
-- =============================================================================

-- 删索引（如果存在）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pipeline' AND INDEX_NAME = 'idx_pipeline_release');
SET @sql := IF(@idx_exists > 0,
    'ALTER TABLE tb_pipeline DROP INDEX idx_pipeline_release',
    'SELECT ''Index idx_pipeline_release does not exist, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删列（如果存在）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pipeline' AND COLUMN_NAME = 'release_id');
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE tb_pipeline DROP COLUMN release_id',
    'SELECT ''Column release_id does not exist on tb_pipeline, skipping DROP'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

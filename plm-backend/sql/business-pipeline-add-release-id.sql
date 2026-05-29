-- @no-menu: 字段扩展,无菜单变更
-- =============================================================================
-- tb_pipeline 加 release_id 反向关联列 — ALTER TABLE
-- 关联：proposal 0028 P0-1 (d) 流水线→所属发布 (反向) / PRD-MAPPING.md §2 tb_pipeline
-- 落地: pipeline 模块反向关联补全 — Pipeline.releaseId → tb_release.id (与 §c Release.pipelineId 对称)
-- 日期: 2026-05-28
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-pipeline-add-release-id.sql
-- 回滚：sql/business-pipeline-add-release-id-rollback.sql
-- 前置：业务表 tb_pipeline / tb_release 已存在
-- 幂等：使用 INFORMATION_SCHEMA 检测列已存在则跳过
-- 备注：无 FK CONSTRAINT,projectId 一致性在 Service 层校验(同 prd-add-requirement-id 范式)
-- =============================================================================

-- 检测+加列（幂等）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pipeline' AND COLUMN_NAME = 'release_id');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_pipeline ADD COLUMN release_id BIGINT(20) DEFAULT NULL COMMENT ''反向关联发布 FK→tb_release.id (可空，2026-05-28 新增，proposal 0028 P0-1d)'' AFTER project_id',
    'SELECT ''Column release_id already exists on tb_pipeline, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检测+加索引（幂等）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_pipeline' AND INDEX_NAME = 'idx_pipeline_release');
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE tb_pipeline ADD INDEX idx_pipeline_release (release_id)',
    'SELECT ''Index idx_pipeline_release already exists on tb_pipeline, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

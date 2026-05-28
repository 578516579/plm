-- @no-menu: 字段扩展,无菜单变更
-- =============================================================================
-- tb_release 加 pipeline_id 关联列 — ALTER TABLE
-- 关联：proposal 0028 P0-1 (c) 发布→流水线 / PRD-MAPPING.md §2 tb_release
-- 落地: release 模块发布→运维主线贯通 — Release.pipelineId → tb_pipeline.id
-- 日期: 2026-05-28
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-release-add-pipeline-id.sql
-- 回滚：sql/business-release-add-pipeline-id-rollback.sql
-- 前置：业务表 tb_release / tb_pipeline 已存在
-- 幂等：使用 INFORMATION_SCHEMA 检测列已存在则跳过
-- 备注：无 FK CONSTRAINT,projectId 一致性在 Service 层校验(同 prd-add-requirement-id 范式)
--       业务规则:release.pipelineId 仅在发布单状态 ≥01(已提交) 后必填,Service 层 enforce
-- =============================================================================

-- 检测+加列（幂等）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_release' AND COLUMN_NAME = 'pipeline_id');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_release ADD COLUMN pipeline_id BIGINT(20) DEFAULT NULL COMMENT ''关联流水线 FK→tb_pipeline.id (可空，2026-05-28 新增，proposal 0028 P0-1c)'' AFTER project_id',
    'SELECT ''Column pipeline_id already exists on tb_release, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检测+加索引（幂等）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_release' AND INDEX_NAME = 'idx_release_pipeline');
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE tb_release ADD INDEX idx_release_pipeline (pipeline_id)',
    'SELECT ''Index idx_release_pipeline already exists on tb_release, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

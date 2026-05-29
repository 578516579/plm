-- @no-menu: 字段扩展,无菜单变更
-- =============================================================================
-- tb_submission 加 testplan_id 关联列 — ALTER TABLE
-- 关联：proposal 0028 P0-1 (a) 提测拉起测试方案 / PRD-MAPPING.md §2 tb_submission
-- 落地: submission 模块研发→测试主线贯通 — Submission.testplanId → tb_testplan.id
-- 日期: 2026-05-28
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-submission-add-testplan-id.sql
-- 回滚：sql/business-submission-add-testplan-id-rollback.sql
-- 前置：业务表 tb_submission / tb_testplan 已存在
-- 幂等：使用 INFORMATION_SCHEMA 检测列已存在则跳过
-- 备注：无 FK CONSTRAINT,projectId 一致性在 Service 层校验(同 prd-add-requirement-id 范式)
-- =============================================================================

-- 检测+加列（幂等）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_submission' AND COLUMN_NAME = 'testplan_id');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_submission ADD COLUMN testplan_id BIGINT(20) DEFAULT NULL COMMENT ''关联测试方案 FK→tb_testplan.id (可空，2026-05-28 新增，proposal 0028 P0-1a)'' AFTER project_id',
    'SELECT ''Column testplan_id already exists on tb_submission, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检测+加索引（幂等）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_submission' AND INDEX_NAME = 'idx_submission_testplan');
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE tb_submission ADD INDEX idx_submission_testplan (testplan_id)',
    'SELECT ''Index idx_submission_testplan already exists on tb_submission, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

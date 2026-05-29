-- @no-menu: 字段扩展,无菜单变更
-- =============================================================================
-- tb_defect 加 testcase_id 溯源列 — ALTER TABLE
-- 关联：proposal 0028 P0-1 (b) 用例失败→缺陷溯源 / PRD-MAPPING.md §2 tb_defect
-- 落地: defect 模块测试→缺陷主线贯通 — Defect.testcaseId → tb_testcase.id (可空,自由提交场景)
-- 日期: 2026-05-28
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-defect-add-testcase-id.sql
-- 回滚：sql/business-defect-add-testcase-id-rollback.sql
-- 前置：业务表 tb_defect / tb_testcase 已存在
-- 幂等：使用 INFORMATION_SCHEMA 检测列已存在则跳过
-- 备注：无 FK CONSTRAINT,projectId 一致性在 Service 层校验(同 prd-add-requirement-id 范式)
-- =============================================================================

-- 检测+加列（幂等）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_defect' AND COLUMN_NAME = 'testcase_id');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_defect ADD COLUMN testcase_id BIGINT(20) DEFAULT NULL COMMENT ''关联失败用例 FK→tb_testcase.id (可空，2026-05-28 新增，proposal 0028 P0-1b)'' AFTER project_id',
    'SELECT ''Column testcase_id already exists on tb_defect, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检测+加索引（幂等）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_defect' AND INDEX_NAME = 'idx_defect_testcase');
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE tb_defect ADD INDEX idx_defect_testcase (testcase_id)',
    'SELECT ''Index idx_defect_testcase already exists on tb_defect, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

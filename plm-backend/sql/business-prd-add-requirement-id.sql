-- =============================================================================
-- tb_prd 加 requirement_id 反向关联列 — ALTER TABLE
-- 关联：PRD §F2.2 PRD 来源于需求 / PRD-MAPPING.md §15 tb_prd (2026-05-25 新增)
-- 落地: prd 模块反向关联补全 — 与 §20 Ued.requirementId 对称
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-prd-add-requirement-id.sql
-- 回滚：sql/business-prd-add-requirement-id-rollback.sql
-- 前置：业务表 tb_prd / tb_requirement 已存在
-- 幂等：使用 INFORMATION_SCHEMA 检测列已存在则跳过
-- =============================================================================

-- 检测+加列（幂等）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_prd' AND COLUMN_NAME = 'requirement_id');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_prd ADD COLUMN requirement_id BIGINT(20) DEFAULT NULL COMMENT ''反向关联需求 FK→tb_requirement.requirement_id (可空，2026-05-25 新增)'' AFTER project_id',
    'SELECT ''Column requirement_id already exists on tb_prd, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检测+加索引（幂等）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_prd' AND INDEX_NAME = 'idx_prd_requirement');
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE tb_prd ADD INDEX idx_prd_requirement (requirement_id)',
    'SELECT ''Index idx_prd_requirement already exists on tb_prd, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

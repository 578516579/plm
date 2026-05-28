-- @no-menu: 字段扩展,无菜单变更
-- =============================================================================
-- tb_testreport 加聚合元数据字段 — Proposal 0028 P0-3A
-- 关联：PRD §F4.7 "AI 自动生成测试报告" — 改成按 testplanId/projectId 实时聚合 testcase + defect
-- 字段:
--   is_aggregated       CHAR(1) DEFAULT 'N'   Y/N 标记是否经聚合算出(N = 人工录入)
--   aggregated_at       DATETIME              上次聚合时间
--   is_manual_override  CHAR(1) DEFAULT 'N'   Y = 用户手工覆盖了聚合值,后续聚合跳过此条
-- 索引:
--   idx_testreport_testplan_id   testplan_id 列若无索引则补
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-testreport-add-aggregate-fields.sql
-- 回滚：sql/business-testreport-add-aggregate-fields-rollback.sql
-- 前置：业务表 tb_testreport 已存在 (business-testreport.sql)
-- 幂等：使用 INFORMATION_SCHEMA 检测列/索引已存在则跳过
-- =============================================================================

-- 检测+加列 is_aggregated（幂等）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_testreport' AND COLUMN_NAME = 'is_aggregated');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_testreport ADD COLUMN is_aggregated CHAR(1) DEFAULT ''N'' COMMENT ''Y/N 是否经聚合算出 (P0028 P0-3A)'' AFTER reviewer_user_id',
    'SELECT ''Column is_aggregated already exists on tb_testreport, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检测+加列 aggregated_at（幂等）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_testreport' AND COLUMN_NAME = 'aggregated_at');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_testreport ADD COLUMN aggregated_at DATETIME DEFAULT NULL COMMENT ''上次聚合时间 (P0028 P0-3A)'' AFTER is_aggregated',
    'SELECT ''Column aggregated_at already exists on tb_testreport, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检测+加列 is_manual_override（幂等）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_testreport' AND COLUMN_NAME = 'is_manual_override');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_testreport ADD COLUMN is_manual_override CHAR(1) DEFAULT ''N'' COMMENT ''Y=用户手工覆盖聚合值,后续聚合跳过 (P0028 P0-3A)'' AFTER aggregated_at',
    'SELECT ''Column is_manual_override already exists on tb_testreport, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 检测+加索引 idx_testreport_testplan_id（幂等）
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_testreport' AND INDEX_NAME = 'idx_testreport_testplan_id');
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE tb_testreport ADD INDEX idx_testreport_testplan_id (testplan_id)',
    'SELECT ''Index idx_testreport_testplan_id already exists on tb_testreport, skipping ADD'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

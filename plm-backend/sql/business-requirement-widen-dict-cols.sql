-- =============================================================================
-- tb_requirement 字典码列加宽 VARCHAR(2) → VARCHAR(20) — 纵深防御 ALTER
-- 关联：前端已对齐字典码 (commit ca16891 修 source='customer' 超 VARCHAR(2) 截断)
--       本 SQL 给字典码列留足冗余, 杜绝未来字典码 >2 字符再触发 Data too long
-- =============================================================================
-- @no-menu: 本 SQL 仅加宽既有列宽, 不新增菜单/字典/子表
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-requirement-widen-dict-cols.sql
-- 回滚：sql/business-requirement-widen-dict-cols-rollback.sql
-- 前置：业务表 tb_requirement 已存在
-- 幂等：使用 INFORMATION_SCHEMA.CHARACTER_MAXIMUM_LENGTH 检测, 已 ≥20 则跳过
-- =============================================================================

-- source（字典 biz_req_source）
SET @len := (SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_requirement' AND COLUMN_NAME = 'source');
SET @sql := IF(@len IS NULL OR @len >= 20,
    'SELECT ''tb_requirement.source already >= 20, skipping'' AS msg',
    'ALTER TABLE tb_requirement MODIFY COLUMN source VARCHAR(20) NOT NULL DEFAULT ''01'' COMMENT ''需求来源（字典 biz_req_source）''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- priority（字典 biz_req_priority）
SET @len := (SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_requirement' AND COLUMN_NAME = 'priority');
SET @sql := IF(@len IS NULL OR @len >= 20,
    'SELECT ''tb_requirement.priority already >= 20, skipping'' AS msg',
    'ALTER TABLE tb_requirement MODIFY COLUMN priority VARCHAR(20) NOT NULL DEFAULT ''02'' COMMENT ''优先级（字典 biz_req_priority）''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- status（字典 biz_req_status）
SET @len := (SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_requirement' AND COLUMN_NAME = 'status');
SET @sql := IF(@len IS NULL OR @len >= 20,
    'SELECT ''tb_requirement.status already >= 20, skipping'' AS msg',
    'ALTER TABLE tb_requirement MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT ''00'' COMMENT ''状态（字典 biz_req_status）''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

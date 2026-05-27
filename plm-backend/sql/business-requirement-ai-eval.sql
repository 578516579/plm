-- =============================================================================
-- 迁移：tb_requirement 增列 ai_evaluation（AI 优先级初评，PRD §F2.1 req-priority-flow）
-- @no-menu: 仅给已有表增列，不涉及菜单（需求菜单见 business-requirement.sql 2020-2025）
-- =============================================================================
-- 应用：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-requirement-ai-eval.sql
-- 幂等：列已存在则跳过，可重复执行（information_schema 预检 + 动态 DDL）
-- 回滚：ALTER TABLE tb_requirement DROP COLUMN ai_evaluation;
-- 说明：aiEvaluation 为前端约定值 high/medium/low（非字典），故不动 sys_dict_*
-- =============================================================================

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tb_requirement'
      AND COLUMN_NAME = 'ai_evaluation'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE tb_requirement ADD COLUMN ai_evaluation VARCHAR(20) DEFAULT NULL COMMENT ''AI 优先级初评 high/medium/low（PRD §F2.1，非字典前端约定值）'' AFTER review_note',
    'SELECT ''ai_evaluation already exists, skip'' AS msg'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

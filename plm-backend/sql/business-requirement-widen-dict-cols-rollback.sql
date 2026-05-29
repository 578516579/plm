-- =============================================================================
-- 回滚 business-requirement-widen-dict-cols.sql — 列宽 VARCHAR(20) → VARCHAR(2)
-- ⚠ 仅在确认这三列内无 >2 字符数据时执行（字典码恒为 2 字符, 正常情况安全）
--   有超长数据时 MySQL 会按 sql_mode 截断或报错, 执行前务必核对。
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-requirement-widen-dict-cols-rollback.sql
-- =============================================================================
ALTER TABLE tb_requirement MODIFY COLUMN source   VARCHAR(2) NOT NULL DEFAULT '01' COMMENT '需求来源（字典 biz_req_source）';
ALTER TABLE tb_requirement MODIFY COLUMN priority VARCHAR(2) NOT NULL DEFAULT '02' COMMENT '优先级（字典 biz_req_priority）';
ALTER TABLE tb_requirement MODIFY COLUMN status   VARCHAR(2) NOT NULL DEFAULT '00' COMMENT '状态（字典 biz_req_status）';

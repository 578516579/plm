-- =============================================================================
-- Sprint 业务模块 — 回滚脚本
-- =============================================================================
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2040 AND 2046;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2040 AND 2046;
DELETE FROM sys_dict_data WHERE dict_type = 'biz_sprint_status';
DELETE FROM sys_dict_type WHERE dict_type = 'biz_sprint_status';
DROP TABLE IF EXISTS tb_sprint;

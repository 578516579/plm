-- =============================================================================
-- Defect 业务模块 — 回滚脚本
-- =============================================================================
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2050 AND 2056;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2050 AND 2056;
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_defect_severity', 'biz_defect_category', 'biz_defect_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_defect_severity', 'biz_defect_category', 'biz_defect_status');
DROP TABLE IF EXISTS tb_defect;

-- =============================================================================
-- Task 业务模块 — 回滚脚本
-- =============================================================================
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2030 AND 2037;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2030 AND 2037;
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_task_status', 'biz_task_priority');
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_task_status', 'biz_task_priority');
DROP TABLE IF EXISTS tb_task;

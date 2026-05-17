-- =============================================================================
-- Requirement 业务模块 — 回滚脚本
-- 关联：sql/business-requirement.sql
-- =============================================================================
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2020 AND 2025;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2020 AND 2025;
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_req_source', 'biz_req_priority', 'biz_req_status', 'biz_req_ai_value');
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_req_source', 'biz_req_priority', 'biz_req_status', 'biz_req_ai_value');
DROP TABLE IF EXISTS tb_requirement;

-- =============================================================================
-- 回滚：外部集成 (plm-integration) 业务模块
-- =============================================================================

DROP TABLE IF EXISTS tb_integration_webhook_event;
DROP TABLE IF EXISTS tb_integration_connector;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2500 AND 2599;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2500 AND 2599;

DELETE FROM sys_dict_data WHERE dict_type IN
    ('biz_integration_type', 'biz_integration_auth', 'biz_integration_status', 'biz_webhook_status');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('biz_integration_type', 'biz_integration_auth', 'biz_integration_status', 'biz_webhook_status');

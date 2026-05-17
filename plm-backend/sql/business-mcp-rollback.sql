-- =============================================================================
-- 回滚：MCP Server (plm-mcp) 业务模块
-- =============================================================================

DROP TABLE IF EXISTS tb_mcp_tool_audit;
DROP TABLE IF EXISTS tb_mcp_server;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2400 AND 2499;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2400 AND 2499;

DELETE FROM sys_dict_data WHERE dict_type IN ('biz_mcp_protocol', 'biz_mcp_auth', 'biz_mcp_status', 'biz_audit_result');
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_mcp_protocol', 'biz_mcp_auth', 'biz_mcp_status', 'biz_audit_result');

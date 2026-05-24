-- =============================================================================
-- 菜单 seed — AI 调用审计 (V3, 2026-05-18)
-- 挂在 AI Agent 编排 (menu_id=2320) 同级 (parent_id=2000),order=72
-- 权限 perms = business:ai-agent:list (复用 ai-agent 模块权限,Controller 已加 @PreAuthorize)
-- =============================================================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, create_by, create_time, status, visible)
VALUES
(2326, 'AI 调用审计', 2000, 72, 'ai-invocation-log', 'business/ai-invocation-log/index',
       'C', 'business:ai-agent:list', 'log', 'admin', NOW(), '0', '0')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    order_num = VALUES(order_num),
    path      = VALUES(path),
    component = VALUES(component),
    perms     = VALUES(perms),
    icon      = VALUES(icon);

-- 给 admin (role_id=1) 授权(角色-菜单关联)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2326)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 验证:
-- SELECT menu_id, menu_name, path, component, perms FROM sys_menu WHERE menu_id = 2326;

-- =============================================================================
-- 回滚 menu-regroup-remaining-7.sql — 删除本脚本创建的 7 模块菜单 + 授权
-- =============================================================================
-- 范围: 2280-2305 (manual-impl/manual-ops/analytics) + 2330-2365 (openspec/pipeline/feature-flag/dora)
-- 不动 2320-2325 (ai-agent,由 menu-fill-missing-8 管理)
-- =============================================================================
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2280 AND 2305 OR menu_id BETWEEN 2330 AND 2365;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2280 AND 2305 OR menu_id BETWEEN 2330 AND 2365;

SELECT '回滚完成,7 模块菜单已删除' AS result,
       (SELECT COUNT(*) FROM sys_menu WHERE menu_id BETWEEN 2280 AND 2305 OR menu_id BETWEEN 2330 AND 2365) AS remaining;

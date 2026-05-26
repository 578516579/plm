-- =============================================================================
-- 回滚 menu-regroup-remaining-7.sql — 把 7 模块菜单还原到 seed 状态 (parent_id=2000)
-- =============================================================================
-- 还原到 menu-seed-prd-aligned-modules.sql 的 order_num (52/53/61/72/81/82/83)
-- =============================================================================
UPDATE sys_menu SET parent_id=2000, order_num=52 WHERE menu_id=2280;   -- manual-impl
UPDATE sys_menu SET parent_id=2000, order_num=53 WHERE menu_id=2290;   -- manual-ops
UPDATE sys_menu SET parent_id=2000, order_num=61 WHERE menu_id=2300;   -- analytics
UPDATE sys_menu SET parent_id=2000, order_num=72 WHERE menu_id=2330;   -- openspec
UPDATE sys_menu SET parent_id=2000, order_num=81 WHERE menu_id=2340;   -- pipeline
UPDATE sys_menu SET parent_id=2000, order_num=82 WHERE menu_id=2350;   -- feature-flag
UPDATE sys_menu SET parent_id=2000, order_num=83 WHERE menu_id=2360;   -- dora

SELECT '回滚完成,7 模块已还原到 parent_id=2000' AS result,
       COUNT(*) AS cnt FROM sys_menu
WHERE menu_id IN (2280,2290,2300,2330,2340,2350,2360) AND parent_id=2000;

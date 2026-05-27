-- =============================================================================
-- 回滚: 业务菜单 8 阶段分组规整 (menu-regroup-by-phase.sql 的逆操作)
-- =============================================================================
-- 对齐 DB 实际有的 26 个 C-type 菜单 + 2700 重复 + 2037 错挂根
-- =============================================================================

-- ============ 1. C-type 菜单 parent_id 回到 2000 ============
UPDATE sys_menu SET parent_id=2000
WHERE parent_id IN (2900,2910,2920,2930,2940,2950,2960,2970);

-- 2037 我的任务 原本就 parent_id=0 (bug),保险起见恢复回 0
UPDATE sys_menu SET parent_id=0 WHERE menu_id=2037;

-- ============ 2. order_num 恢复 (DB 实际原始值,基于 SQL seed) ============
UPDATE sys_menu SET order_num=2  WHERE menu_id=2010;  -- project
UPDATE sys_menu SET order_num=3  WHERE menu_id=2020;  -- requirement
UPDATE sys_menu SET order_num=4  WHERE menu_id=2030;  -- task
UPDATE sys_menu SET order_num=4  WHERE menu_id=2036;  -- task kanban
UPDATE sys_menu SET order_num=4  WHERE menu_id=2037;  -- my task
UPDATE sys_menu SET order_num=5  WHERE menu_id=2040;  -- sprint
UPDATE sys_menu SET order_num=6  WHERE menu_id=2050;  -- defect
UPDATE sys_menu SET order_num=7  WHERE menu_id=2060;  -- testcase
UPDATE sys_menu SET order_num=8  WHERE menu_id=2070;  -- document
UPDATE sys_menu SET order_num=9  WHERE menu_id=2080;  -- submission
UPDATE sys_menu SET order_num=10 WHERE menu_id=2090;  -- release
UPDATE sys_menu SET order_num=11 WHERE menu_id=2100;  -- testplan
UPDATE sys_menu SET order_num=12 WHERE menu_id=2110;  -- testreport
UPDATE sys_menu SET order_num=13 WHERE menu_id=2120;  -- apidoc
UPDATE sys_menu SET order_num=14 WHERE menu_id=2130;  -- manual-product
UPDATE sys_menu SET order_num=42 WHERE menu_id=2270;  -- autotest (PRD seed)
UPDATE sys_menu SET order_num=72 WHERE menu_id=2326;  -- ai-invocation-log
UPDATE sys_menu SET order_num=15 WHERE menu_id=2710;  -- manual-impl
UPDATE sys_menu SET order_num=16 WHERE menu_id=2720;  -- manual-ops
UPDATE sys_menu SET order_num=17 WHERE menu_id=2730;  -- analytics
UPDATE sys_menu SET order_num=18 WHERE menu_id=2740;  -- dashboard
UPDATE sys_menu SET order_num=19 WHERE menu_id=2750;  -- openspec
UPDATE sys_menu SET order_num=20 WHERE menu_id=2760;  -- pipeline
UPDATE sys_menu SET order_num=21 WHERE menu_id=2770;  -- feature-flag
UPDATE sys_menu SET order_num=22 WHERE menu_id=2780;  -- dora

-- 2700 重复 autotest:恢复 visible=0 + 原 order_num(seed 编号也是 42)
UPDATE sys_menu SET visible='0', order_num=42, remark=''
WHERE menu_id=2700;

-- ============ 3. 删除新增的 8 个一级目录 + UED 6 条 ============
DELETE FROM sys_role_menu
WHERE menu_id IN (2900,2910,2920,2930,2940,2950,2960,2970,
                  2140,2141,2142,2143,2144,2145);
DELETE FROM sys_menu
WHERE menu_id IN (2900,2910,2920,2930,2940,2950,2960,2970,
                  2140,2141,2142,2143,2144,2145);

-- ============ 4. 旧「业务管理」visible 恢复 ============
UPDATE sys_menu SET visible='0', remark='业务管理目录' WHERE menu_id=2000;

-- ============ 5. MCP/外部集成 order_num 恢复 ============
UPDATE sys_menu SET order_num=10 WHERE menu_id=2400;
UPDATE sys_menu SET order_num=11 WHERE menu_id=2500;

-- ============ 6. 验证 ============
SELECT '业务子菜单全回 2000 下' AS check_name, COUNT(*) AS cnt
FROM sys_menu WHERE parent_id=2000 AND menu_type='C';

SELECT '阶段一级目录已删' AS check_name, COUNT(*) AS cnt
FROM sys_menu WHERE menu_id BETWEEN 2900 AND 2970;

SELECT 'UED 菜单已删' AS check_name, COUNT(*) AS cnt
FROM sys_menu WHERE menu_id BETWEEN 2140 AND 2145;

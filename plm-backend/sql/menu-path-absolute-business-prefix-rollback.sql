-- =============================================================================
-- 回滚: 业务子菜单 path 改回相对路径(原 RuoYi 子菜单约定)
-- 配套 menu-path-absolute-business-prefix.sql 反向操作
-- =============================================================================

-- 工作台
UPDATE sys_menu SET path='dashboard'           WHERE menu_id=2740;

-- 规划阶段 (2910)
UPDATE sys_menu SET path='project'             WHERE menu_id=2010;
UPDATE sys_menu SET path='inception'           WHERE menu_id=2200;
UPDATE sys_menu SET path='competitive'         WHERE menu_id=2220;

-- 需求与设计阶段 (2920)
UPDATE sys_menu SET path='requirement'         WHERE menu_id=2020;
UPDATE sys_menu SET path='prd'                 WHERE menu_id=2210;
UPDATE sys_menu SET path='ued'                 WHERE menu_id=2140;
UPDATE sys_menu SET path='arch'                WHERE menu_id=2230;
UPDATE sys_menu SET path='dbdesign'            WHERE menu_id=2240;
UPDATE sys_menu SET path='apidesign'           WHERE menu_id=2250;
UPDATE sys_menu SET path='document'            WHERE menu_id=2070;

-- 开发阶段 (2930)
UPDATE sys_menu SET path='sprint'              WHERE menu_id=2040;
UPDATE sys_menu SET path='task'                WHERE menu_id=2030;
UPDATE sys_menu SET path='taskkanban'          WHERE menu_id=2036;
UPDATE sys_menu SET path='mytask'              WHERE menu_id=2037;

-- 测试阶段 (2940)
UPDATE sys_menu SET path='testplan'            WHERE menu_id=2100;
UPDATE sys_menu SET path='testcase'            WHERE menu_id=2060;
UPDATE sys_menu SET path='testdata'            WHERE menu_id=2260;
UPDATE sys_menu SET path='submission'          WHERE menu_id=2080;
UPDATE sys_menu SET path='autotest'            WHERE menu_id=2270;
UPDATE sys_menu SET path='defect'              WHERE menu_id=2050;
UPDATE sys_menu SET path='testreport'          WHERE menu_id=2110;
UPDATE sys_menu SET path='autotest'            WHERE menu_id=2700;

-- 部署阶段 (2950)
UPDATE sys_menu SET path='apidoc'              WHERE menu_id=2120;
UPDATE sys_menu SET path='manual-product'      WHERE menu_id=2130;
UPDATE sys_menu SET path='manual-impl'         WHERE menu_id=2710;
UPDATE sys_menu SET path='manual-ops'          WHERE menu_id=2720;
UPDATE sys_menu SET path='pipeline'            WHERE menu_id=2760;
UPDATE sys_menu SET path='release'             WHERE menu_id=2090;
UPDATE sys_menu SET path='feature-flag'        WHERE menu_id=2770;
UPDATE sys_menu SET path='dora'                WHERE menu_id=2780;

-- AI 阶段 (2960)
UPDATE sys_menu SET path='openspec'            WHERE menu_id=2750;
UPDATE sys_menu SET path='ai-agent'            WHERE menu_id=2320;
UPDATE sys_menu SET path='ai-invocation-log'   WHERE menu_id=2326;

-- 运营/报表 (2970)
UPDATE sys_menu SET path='analytics'           WHERE menu_id=2730;

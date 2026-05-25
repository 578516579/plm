-- =============================================================================
-- 业务子菜单 path 改绝对路径 (/business/<entity>) 修复 — P0 troubleshoot
-- =============================================================================
-- 故障现象: 菜单按阶段分组(menu-regroup-by-phase.sql 把 parent_id 从 2000 业务管理
--          改成 2910-2970 阶段分组)后,RuoYi 路由生成把父子 path 拼接成
--          /phase-plan/project 等新路径,导致历史约定 /business/<entity> URL
--          404。E2E spec 36 文件 / 368 处引用 /business/* 全部失效。
-- =============================================================================
-- 根因: RuoYi SysMenuServiceImpl.getRouterPath() 行 461-471:
--      子菜单(parent_id != 0)直接返回 menu.getPath(),Vue Router 嵌套渲染
--      → 路径 = 父path + '/' + 子path 当子path 不以 '/' 开头
--      → 当子path 以 '/' 开头(绝对路径)时,Vue Router 视为绝对路径,
--        不拼父 path
-- =============================================================================
-- 修复方案 B(治本): 子菜单 path 改绝对路径
--   - 后端 0 改 / 前端 0 改 / E2E 0 改
--   - 保留 PRD-MAPPING /business/<module> URL 契约
--   - 仅修菜单数据层,符合 "数据驱动路由" 原则
--   - 配套 rollback 已建: menu-path-absolute-business-prefix-rollback.sql
-- =============================================================================
-- 35 个业务子菜单(menu_type='C' 在 phase-* 父节点下),逐项 SET path='/business/<orig>'
-- =============================================================================

-- 工作台
UPDATE sys_menu SET path='/business/dashboard'           WHERE menu_id=2740;

-- 规划阶段 (2910)
UPDATE sys_menu SET path='/business/project'             WHERE menu_id=2010;
UPDATE sys_menu SET path='/business/inception'           WHERE menu_id=2200;
UPDATE sys_menu SET path='/business/competitive'         WHERE menu_id=2220;

-- 需求与设计阶段 (2920)
UPDATE sys_menu SET path='/business/requirement'         WHERE menu_id=2020;
UPDATE sys_menu SET path='/business/prd'                 WHERE menu_id=2210;
UPDATE sys_menu SET path='/business/ued'                 WHERE menu_id=2140;
UPDATE sys_menu SET path='/business/arch'                WHERE menu_id=2230;
UPDATE sys_menu SET path='/business/dbdesign'            WHERE menu_id=2240;
UPDATE sys_menu SET path='/business/apidesign'           WHERE menu_id=2250;
UPDATE sys_menu SET path='/business/document'            WHERE menu_id=2070;

-- 开发阶段 (2930)
UPDATE sys_menu SET path='/business/sprint'              WHERE menu_id=2040;
UPDATE sys_menu SET path='/business/task'                WHERE menu_id=2030;
UPDATE sys_menu SET path='/business/taskkanban'          WHERE menu_id=2036;
UPDATE sys_menu SET path='/business/mytask'              WHERE menu_id=2037;

-- 测试阶段 (2940)
UPDATE sys_menu SET path='/business/testplan'            WHERE menu_id=2100;
UPDATE sys_menu SET path='/business/testcase'            WHERE menu_id=2060;
UPDATE sys_menu SET path='/business/testdata'            WHERE menu_id=2260;
UPDATE sys_menu SET path='/business/submission'          WHERE menu_id=2080;
UPDATE sys_menu SET path='/business/autotest'            WHERE menu_id=2270;
UPDATE sys_menu SET path='/business/defect'              WHERE menu_id=2050;
UPDATE sys_menu SET path='/business/testreport'          WHERE menu_id=2110;
UPDATE sys_menu SET path='/business/autotest'            WHERE menu_id=2700;   -- 重复 2270, 兼容老 seed

-- 部署阶段 (2950)
UPDATE sys_menu SET path='/business/apidoc'              WHERE menu_id=2120;
UPDATE sys_menu SET path='/business/manual-product'      WHERE menu_id=2130;
UPDATE sys_menu SET path='/business/manual-impl'         WHERE menu_id=2710;
UPDATE sys_menu SET path='/business/manual-ops'          WHERE menu_id=2720;
UPDATE sys_menu SET path='/business/pipeline'            WHERE menu_id=2760;
UPDATE sys_menu SET path='/business/release'             WHERE menu_id=2090;
UPDATE sys_menu SET path='/business/feature-flag'        WHERE menu_id=2770;
UPDATE sys_menu SET path='/business/dora'                WHERE menu_id=2780;

-- AI 阶段 (2960)
UPDATE sys_menu SET path='/business/openspec'            WHERE menu_id=2750;
UPDATE sys_menu SET path='/business/ai-agent'            WHERE menu_id=2320;
UPDATE sys_menu SET path='/business/ai-invocation-log'   WHERE menu_id=2326;

-- 运营/报表 (2970)
UPDATE sys_menu SET path='/business/analytics'           WHERE menu_id=2730;

-- =============================================================================
-- 验证
-- =============================================================================
-- 期望: 35 个业务菜单 path 均以 /business/ 开头,父+子路径拼接被 Vue Router
--       识别为绝对路径
-- SELECT menu_id, menu_name, path FROM sys_menu WHERE path LIKE '/business/%' ORDER BY menu_id;
-- 期望: 35 行,每行 path = '/business/<entity>'
-- =============================================================================

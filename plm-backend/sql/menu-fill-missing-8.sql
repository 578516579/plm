-- =============================================================================
-- 补 8 个 PRD-aligned 模块的缺失菜单 (idempotent)
-- =============================================================================
-- 背景: menu-seed-prd-aligned-modules.sql 在当前 DB 上未跑/被回滚, DB 里
--       这 8 个模块的 sys_menu 记录全空。前端 view 已存在但用户看不到菜单入口。
--
-- 处理:
--   1. 复用 menu-seed-prd-aligned-modules.sql 原始 menu_id 分配(2200-2325)
--      perms 字符串与 seed 完全一致,前端 v-hasPermi 无需改
--   2. parent_id 直接挂到 menu-regroup-by-phase.sql 新建的阶段目录
--      (不是旧 2000 业务管理)
--   3. order_num 按业务流程序填入预留槽位
--      (规划 2 立项 / 规划 3 竞品 / 设计 2 PRD / 设计 4-6 架构/DB/API / 测试 3 数据 / AI 2 Agent)
--   4. 授权 admin (role_id=1)
--
-- 8 模块分配:
--   规划阶段 (2910): inception(2200-5) order=2, competitive(2220-5) order=3
--   需求设计 (2920): prd(2210-5) order=2, arch(2230-5) order=4,
--                    dbdesign(2240-5) order=5, apidesign(2250-5) order=6
--   测试阶段 (2940): testdata(2260-5) order=3
--   AI 能力  (2960): ai-agent(2320-5) order=2
--
-- 回滚: menu-fill-missing-8-rollback.sql
-- =============================================================================

-- ============ 规划阶段 (2910) ============
-- 项目立项 → 2910 order=2
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2200, '项目立项', 2910, 2, 'inception', 'business/inception/index', 'C', '0', '0', 'business:inception:list',   'guide', 'admin', NOW(), 'PRD F1.1'),
(2201, '立项查询', 2200, 1, '#',         '',                          'F', '0', '0', 'business:inception:query',  '#',     'admin', NOW(), ''),
(2202, '立项新增', 2200, 2, '#',         '',                          'F', '0', '0', 'business:inception:add',    '#',     'admin', NOW(), ''),
(2203, '立项修改', 2200, 3, '#',         '',                          'F', '0', '0', 'business:inception:edit',   '#',     'admin', NOW(), ''),
(2204, '立项删除', 2200, 4, '#',         '',                          'F', '0', '0', 'business:inception:remove', '#',     'admin', NOW(), ''),
(2205, '立项导出', 2200, 5, '#',         '',                          'F', '0', '0', 'business:inception:export', '#',     'admin', NOW(), ''),
-- 竞品情报 → 2910 order=3
(2220, '竞品情报', 2910, 3, 'competitive', 'business/competitive/index', 'C', '0', '0', 'business:competitive:list',   'rate', 'admin', NOW(), 'PRD F1.3'),
(2221, '竞品查询', 2220, 1, '#',           '',                            'F', '0', '0', 'business:competitive:query',  '#',    'admin', NOW(), ''),
(2222, '竞品新增', 2220, 2, '#',           '',                            'F', '0', '0', 'business:competitive:add',    '#',    'admin', NOW(), ''),
(2223, '竞品修改', 2220, 3, '#',           '',                            'F', '0', '0', 'business:competitive:edit',   '#',    'admin', NOW(), ''),
(2224, '竞品删除', 2220, 4, '#',           '',                            'F', '0', '0', 'business:competitive:remove', '#',    'admin', NOW(), ''),
(2225, '竞品导出', 2220, 5, '#',           '',                            'F', '0', '0', 'business:competitive:export', '#',    'admin', NOW(), '')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num);

-- ============ 需求与设计 (2920) ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
-- AI PRD 生成 → 2920 order=2
(2210, 'AI PRD 生成', 2920, 2, 'prd', 'business/prd/index', 'C', '0', '0', 'business:prd:list',   'documentation', 'admin', NOW(), 'PRD F2.2'),
(2211, 'PRD 查询',    2210, 1, '#',   '',                    'F', '0', '0', 'business:prd:query',  '#',             'admin', NOW(), ''),
(2212, 'PRD 新增',    2210, 2, '#',   '',                    'F', '0', '0', 'business:prd:add',    '#',             'admin', NOW(), ''),
(2213, 'PRD 修改',    2210, 3, '#',   '',                    'F', '0', '0', 'business:prd:edit',   '#',             'admin', NOW(), ''),
(2214, 'PRD 删除',    2210, 4, '#',   '',                    'F', '0', '0', 'business:prd:remove', '#',             'admin', NOW(), ''),
(2215, 'PRD 导出',    2210, 5, '#',   '',                    'F', '0', '0', 'business:prd:export', '#',             'admin', NOW(), ''),
-- 系统架构 → 2920 order=4
(2230, '系统架构', 2920, 4, 'arch', 'business/arch/index', 'C', '0', '0', 'business:arch:list',   'build', 'admin', NOW(), 'PRD F3.1'),
(2231, '架构查询', 2230, 1, '#',    '',                     'F', '0', '0', 'business:arch:query',  '#',     'admin', NOW(), ''),
(2232, '架构新增', 2230, 2, '#',    '',                     'F', '0', '0', 'business:arch:add',    '#',     'admin', NOW(), ''),
(2233, '架构修改', 2230, 3, '#',    '',                     'F', '0', '0', 'business:arch:edit',   '#',     'admin', NOW(), ''),
(2234, '架构删除', 2230, 4, '#',    '',                     'F', '0', '0', 'business:arch:remove', '#',     'admin', NOW(), ''),
(2235, '架构导出', 2230, 5, '#',    '',                     'F', '0', '0', 'business:arch:export', '#',     'admin', NOW(), ''),
-- 数据库设计 → 2920 order=5
(2240, '数据库设计', 2920, 5, 'dbdesign', 'business/dbdesign/index', 'C', '0', '0', 'business:dbdesign:list',   'druid', 'admin', NOW(), 'PRD F3.2'),
(2241, 'DB 查询',    2240, 1, '#',        '',                         'F', '0', '0', 'business:dbdesign:query',  '#',     'admin', NOW(), ''),
(2242, 'DB 新增',    2240, 2, '#',        '',                         'F', '0', '0', 'business:dbdesign:add',    '#',     'admin', NOW(), ''),
(2243, 'DB 修改',    2240, 3, '#',        '',                         'F', '0', '0', 'business:dbdesign:edit',   '#',     'admin', NOW(), ''),
(2244, 'DB 删除',    2240, 4, '#',        '',                         'F', '0', '0', 'business:dbdesign:remove', '#',     'admin', NOW(), ''),
(2245, 'DB 导出',    2240, 5, '#',        '',                         'F', '0', '0', 'business:dbdesign:export', '#',     'admin', NOW(), ''),
-- 接口详细设计 → 2920 order=6
(2250, '接口详细设计', 2920, 6, 'apidesign', 'business/apidesign/index', 'C', '0', '0', 'business:apidesign:list',   'system', 'admin', NOW(), 'PRD F3.3'),
(2251, '接口查询',     2250, 1, '#',         '',                          'F', '0', '0', 'business:apidesign:query',  '#',      'admin', NOW(), ''),
(2252, '接口新增',     2250, 2, '#',         '',                          'F', '0', '0', 'business:apidesign:add',    '#',      'admin', NOW(), ''),
(2253, '接口修改',     2250, 3, '#',         '',                          'F', '0', '0', 'business:apidesign:edit',   '#',      'admin', NOW(), ''),
(2254, '接口删除',     2250, 4, '#',         '',                          'F', '0', '0', 'business:apidesign:remove', '#',      'admin', NOW(), ''),
(2255, '接口导出',     2250, 5, '#',         '',                          'F', '0', '0', 'business:apidesign:export', '#',      'admin', NOW(), '')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num);

-- ============ 测试阶段 (2940) ============
-- 测试数据工厂 → 2940 order=3
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2260, '测试数据工厂', 2940, 3, 'testdata', 'business/testdata/index', 'C', '0', '0', 'business:testdata:list',   'job', 'admin', NOW(), 'PRD F4.3'),
(2261, '数据集查询',   2260, 1, '#',        '',                         'F', '0', '0', 'business:testdata:query',  '#',   'admin', NOW(), ''),
(2262, '数据集新增',   2260, 2, '#',        '',                         'F', '0', '0', 'business:testdata:add',    '#',   'admin', NOW(), ''),
(2263, '数据集修改',   2260, 3, '#',        '',                         'F', '0', '0', 'business:testdata:edit',   '#',   'admin', NOW(), ''),
(2264, '数据集删除',   2260, 4, '#',        '',                         'F', '0', '0', 'business:testdata:remove', '#',   'admin', NOW(), ''),
(2265, '数据集导出',   2260, 5, '#',        '',                         'F', '0', '0', 'business:testdata:export', '#',   'admin', NOW(), '')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num);

-- ============ AI 能力 (2960) ============
-- AI Agent 编排 → 2960 order=2
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2320, 'AI Agent 编排', 2960, 2, 'ai-agent', 'business/ai-agent/index', 'C', '0', '0', 'business:ai-agent:list',   'star', 'admin', NOW(), 'PRD F3.5'),
(2321, 'Agent 查询',    2320, 1, '#',        '',                         'F', '0', '0', 'business:ai-agent:query',  '#',    'admin', NOW(), ''),
(2322, 'Agent 新增',    2320, 2, '#',        '',                         'F', '0', '0', 'business:ai-agent:add',    '#',    'admin', NOW(), ''),
(2323, 'Agent 修改',    2320, 3, '#',        '',                         'F', '0', '0', 'business:ai-agent:edit',   '#',    'admin', NOW(), ''),
(2324, 'Agent 删除',    2320, 4, '#',        '',                         'F', '0', '0', 'business:ai-agent:remove', '#',    'admin', NOW(), ''),
(2325, 'Agent 导出',    2320, 5, '#',        '',                         'F', '0', '0', 'business:ai-agent:export', '#',    'admin', NOW(), '')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num);

-- ============ 授权 admin (role_id=1) — 8 模块 × 6 条 = 48 条 ============
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_id BETWEEN 2200 AND 2265
   OR menu_id BETWEEN 2320 AND 2325;

-- ============ 验证 ============
SELECT '补缺总数应=48' AS check_name,
       (SELECT COUNT(*) FROM sys_menu WHERE menu_id BETWEEN 2200 AND 2265)
       + (SELECT COUNT(*) FROM sys_menu WHERE menu_id BETWEEN 2320 AND 2325) AS actual;

SELECT '阶段填充结果' AS section,
       p.menu_id AS phase_id, p.menu_name AS phase, m.menu_id, m.menu_name AS module, m.order_num AS ord
FROM sys_menu m JOIN sys_menu p ON m.parent_id=p.menu_id
WHERE p.menu_id BETWEEN 2900 AND 2970 AND m.menu_type='C'
ORDER BY p.order_num, m.order_num;

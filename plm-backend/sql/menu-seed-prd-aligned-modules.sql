-- ============================================================================
-- PRD-aligned 模块菜单种子 — 17 个新模块的左侧导航 + 权限按钮 + admin 授权
-- ============================================================================
-- 已有菜单的 14 个模块: project/requirement/sprint/task/defect/testcase/document/
--                       submission/release/testplan/testreport/apidoc/manual-product/ued
-- 本脚本新增 17 个模块菜单 (6 条/模块 = 1 个 'C' 目录入口 + 5 个 'F' 按钮),
-- 全部挂在 menu_id=2000 "业务管理" 父目录下。
-- 编号规约: 2200..2365 (与现有 2010-2142 不冲突)
-- 同步授权给 admin (role_id=1)。
-- ============================================================================

-- 防重入: 先清掉之前 seed 的菜单 + 关联
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2200 AND 2369;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2200 AND 2369;

-- --- F1 域: inception / competitive (prd 在 F2.2) ---
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, perms, icon, create_by, create_time, status, visible) VALUES
(2200, '项目立项',  2000, 11, 'inception', 'business/inception/index', 'C', 'business:inception:list',  'guide',      'admin', NOW(), '0', '0'),
(2201, '立项查询',  2200, 1,  '#',         '',                          'F', 'business:inception:query', '#',          'admin', NOW(), '0', '0'),
(2202, '立项新增',  2200, 2,  '#',         '',                          'F', 'business:inception:add',   '#',          'admin', NOW(), '0', '0'),
(2203, '立项修改',  2200, 3,  '#',         '',                          'F', 'business:inception:edit',  '#',          'admin', NOW(), '0', '0'),
(2204, '立项删除',  2200, 4,  '#',         '',                          'F', 'business:inception:remove','#',          'admin', NOW(), '0', '0'),
(2205, '立项导出',  2200, 5,  '#',         '',                          'F', 'business:inception:export','#',          'admin', NOW(), '0', '0'),

-- --- F2.2 域: prd ---
(2210, 'AI PRD 生成', 2000, 12, 'prd', 'business/prd/index', 'C', 'business:prd:list',   'documentation', 'admin', NOW(), '0', '0'),
(2211, 'PRD 查询',     2210, 1,  '#',   '',                    'F', 'business:prd:query',  '#',             'admin', NOW(), '0', '0'),
(2212, 'PRD 新增',     2210, 2,  '#',   '',                    'F', 'business:prd:add',    '#',             'admin', NOW(), '0', '0'),
(2213, 'PRD 修改',     2210, 3,  '#',   '',                    'F', 'business:prd:edit',   '#',             'admin', NOW(), '0', '0'),
(2214, 'PRD 删除',     2210, 4,  '#',   '',                    'F', 'business:prd:remove', '#',             'admin', NOW(), '0', '0'),
(2215, 'PRD 导出',     2210, 5,  '#',   '',                    'F', 'business:prd:export', '#',             'admin', NOW(), '0', '0'),

-- --- F1.3 域: competitive ---
(2220, '竞品情报',   2000, 13, 'competitive', 'business/competitive/index', 'C', 'business:competitive:list',  'rate',   'admin', NOW(), '0', '0'),
(2221, '竞品查询',   2220, 1,  '#',           '',                            'F', 'business:competitive:query', '#',      'admin', NOW(), '0', '0'),
(2222, '竞品新增',   2220, 2,  '#',           '',                            'F', 'business:competitive:add',   '#',      'admin', NOW(), '0', '0'),
(2223, '竞品修改',   2220, 3,  '#',           '',                            'F', 'business:competitive:edit',  '#',      'admin', NOW(), '0', '0'),
(2224, '竞品删除',   2220, 4,  '#',           '',                            'F', 'business:competitive:remove','#',      'admin', NOW(), '0', '0'),
(2225, '竞品导出',   2220, 5,  '#',           '',                            'F', 'business:competitive:export','#',      'admin', NOW(), '0', '0'),

-- --- F3.1 域: arch ---
(2230, '系统架构', 2000, 21, 'arch', 'business/arch/index', 'C', 'business:arch:list',   'build', 'admin', NOW(), '0', '0'),
(2231, '架构查询', 2230, 1,  '#',    '',                     'F', 'business:arch:query',  '#',     'admin', NOW(), '0', '0'),
(2232, '架构新增', 2230, 2,  '#',    '',                     'F', 'business:arch:add',    '#',     'admin', NOW(), '0', '0'),
(2233, '架构修改', 2230, 3,  '#',    '',                     'F', 'business:arch:edit',   '#',     'admin', NOW(), '0', '0'),
(2234, '架构删除', 2230, 4,  '#',    '',                     'F', 'business:arch:remove', '#',     'admin', NOW(), '0', '0'),
(2235, '架构导出', 2230, 5,  '#',    '',                     'F', 'business:arch:export', '#',     'admin', NOW(), '0', '0'),

-- --- F3.2 域: dbdesign ---
(2240, '数据库设计', 2000, 22, 'dbdesign', 'business/dbdesign/index', 'C', 'business:dbdesign:list',   'druid',  'admin', NOW(), '0', '0'),
(2241, 'DB 查询',    2240, 1,  '#',        '',                         'F', 'business:dbdesign:query',  '#',      'admin', NOW(), '0', '0'),
(2242, 'DB 新增',    2240, 2,  '#',        '',                         'F', 'business:dbdesign:add',    '#',      'admin', NOW(), '0', '0'),
(2243, 'DB 修改',    2240, 3,  '#',        '',                         'F', 'business:dbdesign:edit',   '#',      'admin', NOW(), '0', '0'),
(2244, 'DB 删除',    2240, 4,  '#',        '',                         'F', 'business:dbdesign:remove', '#',      'admin', NOW(), '0', '0'),
(2245, 'DB 导出',    2240, 5,  '#',        '',                         'F', 'business:dbdesign:export', '#',      'admin', NOW(), '0', '0'),

-- --- F3.3 域: apidesign ---
(2250, '接口详细设计', 2000, 23, 'apidesign', 'business/apidesign/index', 'C', 'business:apidesign:list',   'system', 'admin', NOW(), '0', '0'),
(2251, '接口查询',     2250, 1,  '#',         '',                          'F', 'business:apidesign:query',  '#',      'admin', NOW(), '0', '0'),
(2252, '接口新增',     2250, 2,  '#',         '',                          'F', 'business:apidesign:add',    '#',      'admin', NOW(), '0', '0'),
(2253, '接口修改',     2250, 3,  '#',         '',                          'F', 'business:apidesign:edit',   '#',      'admin', NOW(), '0', '0'),
(2254, '接口删除',     2250, 4,  '#',         '',                          'F', 'business:apidesign:remove', '#',      'admin', NOW(), '0', '0'),
(2255, '接口导出',     2250, 5,  '#',         '',                          'F', 'business:apidesign:export', '#',      'admin', NOW(), '0', '0'),

-- --- F4.3 域: testdata ---
(2260, '测试数据工厂', 2000, 41, 'testdata', 'business/testdata/index', 'C', 'business:testdata:list',   'job',    'admin', NOW(), '0', '0'),
(2261, '数据集查询',   2260, 1,  '#',        '',                         'F', 'business:testdata:query',  '#',      'admin', NOW(), '0', '0'),
(2262, '数据集新增',   2260, 2,  '#',        '',                         'F', 'business:testdata:add',    '#',      'admin', NOW(), '0', '0'),
(2263, '数据集修改',   2260, 3,  '#',        '',                         'F', 'business:testdata:edit',   '#',      'admin', NOW(), '0', '0'),
(2264, '数据集删除',   2260, 4,  '#',        '',                         'F', 'business:testdata:remove', '#',      'admin', NOW(), '0', '0'),
(2265, '数据集导出',   2260, 5,  '#',        '',                         'F', 'business:testdata:export', '#',      'admin', NOW(), '0', '0'),

-- --- F4.5 域: autotest ---
(2270, '自动化测试', 2000, 42, 'autotest', 'business/autotest/index', 'C', 'business:autotest:list',   'monitor','admin', NOW(), '0', '0'),
(2271, '套件查询',   2270, 1,  '#',        '',                         'F', 'business:autotest:query',  '#',      'admin', NOW(), '0', '0'),
(2272, '套件新增',   2270, 2,  '#',        '',                         'F', 'business:autotest:add',    '#',      'admin', NOW(), '0', '0'),
(2273, '套件修改',   2270, 3,  '#',        '',                         'F', 'business:autotest:edit',   '#',      'admin', NOW(), '0', '0'),
(2274, '套件删除',   2270, 4,  '#',        '',                         'F', 'business:autotest:remove', '#',      'admin', NOW(), '0', '0'),
(2275, '套件导出',   2270, 5,  '#',        '',                         'F', 'business:autotest:export', '#',      'admin', NOW(), '0', '0'),

-- --- F5.2 域: manual-impl ---
(2280, '实施手册', 2000, 52, 'manual-impl', 'business/manual-impl/index', 'C', 'business:manual-impl:list',   'guide', 'admin', NOW(), '0', '0'),
(2281, '手册查询', 2280, 1,  '#',           '',                            'F', 'business:manual-impl:query',  '#',     'admin', NOW(), '0', '0'),
(2282, '手册新增', 2280, 2,  '#',           '',                            'F', 'business:manual-impl:add',    '#',     'admin', NOW(), '0', '0'),
(2283, '手册修改', 2280, 3,  '#',           '',                            'F', 'business:manual-impl:edit',   '#',     'admin', NOW(), '0', '0'),
(2284, '手册删除', 2280, 4,  '#',           '',                            'F', 'business:manual-impl:remove', '#',     'admin', NOW(), '0', '0'),
(2285, '手册导出', 2280, 5,  '#',           '',                            'F', 'business:manual-impl:export', '#',     'admin', NOW(), '0', '0'),

-- --- F5.3 域: manual-ops ---
(2290, '运维手册', 2000, 53, 'manual-ops', 'business/manual-ops/index', 'C', 'business:manual-ops:list',   'tool',  'admin', NOW(), '0', '0'),
(2291, '运维查询', 2290, 1,  '#',          '',                           'F', 'business:manual-ops:query',  '#',      'admin', NOW(), '0', '0'),
(2292, '运维新增', 2290, 2,  '#',          '',                           'F', 'business:manual-ops:add',    '#',      'admin', NOW(), '0', '0'),
(2293, '运维修改', 2290, 3,  '#',          '',                           'F', 'business:manual-ops:edit',   '#',      'admin', NOW(), '0', '0'),
(2294, '运维删除', 2290, 4,  '#',          '',                           'F', 'business:manual-ops:remove', '#',      'admin', NOW(), '0', '0'),
(2295, '运维导出', 2290, 5,  '#',          '',                           'F', 'business:manual-ops:export', '#',      'admin', NOW(), '0', '0'),

-- --- F6 域: analytics ---
(2300, '效能分析', 2000, 61, 'analytics', 'business/analytics/index', 'C', 'business:analytics:list',   'chart', 'admin', NOW(), '0', '0'),
(2301, '快照查询', 2300, 1,  '#',         '',                          'F', 'business:analytics:query',  '#',     'admin', NOW(), '0', '0'),
(2302, '快照新增', 2300, 2,  '#',         '',                          'F', 'business:analytics:add',    '#',     'admin', NOW(), '0', '0'),
(2303, '快照修改', 2300, 3,  '#',         '',                          'F', 'business:analytics:edit',   '#',     'admin', NOW(), '0', '0'),
(2304, '快照删除', 2300, 4,  '#',         '',                          'F', 'business:analytics:remove', '#',     'admin', NOW(), '0', '0'),
(2305, '快照导出', 2300, 5,  '#',         '',                          'F', 'business:analytics:export', '#',     'admin', NOW(), '0', '0'),

-- --- UI §4.2: dashboard ---
(2310, '工作台',   2000, 62, 'dashboard', 'business/dashboard/index', 'C', 'business:dashboard:list',   'dashboard','admin', NOW(), '0', '0'),
(2311, '看板查询', 2310, 1,  '#',         '',                          'F', 'business:dashboard:query',  '#',        'admin', NOW(), '0', '0'),
(2312, '看板新增', 2310, 2,  '#',         '',                          'F', 'business:dashboard:add',    '#',        'admin', NOW(), '0', '0'),
(2313, '看板修改', 2310, 3,  '#',         '',                          'F', 'business:dashboard:edit',   '#',        'admin', NOW(), '0', '0'),
(2314, '看板删除', 2310, 4,  '#',         '',                          'F', 'business:dashboard:remove', '#',        'admin', NOW(), '0', '0'),
(2315, '看板导出', 2310, 5,  '#',         '',                          'F', 'business:dashboard:export', '#',        'admin', NOW(), '0', '0'),

-- --- F3.5 域: ai-agent ---
(2320, 'AI Agent 编排', 2000, 71, 'ai-agent', 'business/ai-agent/index', 'C', 'business:ai-agent:list',   'ai',  'admin', NOW(), '0', '0'),
(2321, 'Agent 查询',    2320, 1,  '#',        '',                         'F', 'business:ai-agent:query',  '#',   'admin', NOW(), '0', '0'),
(2322, 'Agent 新增',    2320, 2,  '#',        '',                         'F', 'business:ai-agent:add',    '#',   'admin', NOW(), '0', '0'),
(2323, 'Agent 修改',    2320, 3,  '#',        '',                         'F', 'business:ai-agent:edit',   '#',   'admin', NOW(), '0', '0'),
(2324, 'Agent 删除',    2320, 4,  '#',        '',                         'F', 'business:ai-agent:remove', '#',   'admin', NOW(), '0', '0'),
(2325, 'Agent 导出',    2320, 5,  '#',        '',                         'F', 'business:ai-agent:export', '#',   'admin', NOW(), '0', '0'),

-- --- F3.5 域: openspec ---
(2330, 'AI OpenSpec', 2000, 72, 'openspec', 'business/openspec/index', 'C', 'business:openspec:list',   'log', 'admin', NOW(), '0', '0'),
(2331, 'Spec 查询',   2330, 1,  '#',        '',                         'F', 'business:openspec:query',  '#',   'admin', NOW(), '0', '0'),
(2332, 'Spec 新增',   2330, 2,  '#',        '',                         'F', 'business:openspec:add',    '#',   'admin', NOW(), '0', '0'),
(2333, 'Spec 修改',   2330, 3,  '#',        '',                         'F', 'business:openspec:edit',   '#',   'admin', NOW(), '0', '0'),
(2334, 'Spec 删除',   2330, 4,  '#',        '',                         'F', 'business:openspec:remove', '#',   'admin', NOW(), '0', '0'),
(2335, 'Spec 导出',   2330, 5,  '#',        '',                         'F', 'business:openspec:export', '#',   'admin', NOW(), '0', '0'),

-- --- DevOps 扩展: pipeline ---
(2340, 'CI/CD 流水线', 2000, 81, 'pipeline', 'business/pipeline/index', 'C', 'business:pipeline:list',   'github', 'admin', NOW(), '0', '0'),
(2341, '流水线查询',   2340, 1,  '#',        '',                         'F', 'business:pipeline:query',  '#',      'admin', NOW(), '0', '0'),
(2342, '流水线新增',   2340, 2,  '#',        '',                         'F', 'business:pipeline:add',    '#',      'admin', NOW(), '0', '0'),
(2343, '流水线修改',   2340, 3,  '#',        '',                         'F', 'business:pipeline:edit',   '#',      'admin', NOW(), '0', '0'),
(2344, '流水线删除',   2340, 4,  '#',        '',                         'F', 'business:pipeline:remove', '#',      'admin', NOW(), '0', '0'),
(2345, '流水线导出',   2340, 5,  '#',        '',                         'F', 'business:pipeline:export', '#',      'admin', NOW(), '0', '0'),

-- --- DevOps 扩展: feature-flag ---
(2350, 'Feature Flag', 2000, 82, 'feature-flag', 'business/feature-flag/index', 'C', 'business:feature-flag:list',   'switch', 'admin', NOW(), '0', '0'),
(2351, 'Flag 查询',    2350, 1,  '#',            '',                              'F', 'business:feature-flag:query',  '#',      'admin', NOW(), '0', '0'),
(2352, 'Flag 新增',    2350, 2,  '#',            '',                              'F', 'business:feature-flag:add',    '#',      'admin', NOW(), '0', '0'),
(2353, 'Flag 修改',    2350, 3,  '#',            '',                              'F', 'business:feature-flag:edit',   '#',      'admin', NOW(), '0', '0'),
(2354, 'Flag 删除',    2350, 4,  '#',            '',                              'F', 'business:feature-flag:remove', '#',      'admin', NOW(), '0', '0'),
(2355, 'Flag 导出',    2350, 5,  '#',            '',                              'F', 'business:feature-flag:export', '#',      'admin', NOW(), '0', '0'),

-- --- DevOps 扩展: dora ---
(2360, 'DORA 指标', 2000, 83, 'dora', 'business/dora/index', 'C', 'business:dora:list',   'chart',  'admin', NOW(), '0', '0'),
(2361, '指标查询', 2360, 1,  '#',    '',                     'F', 'business:dora:query',  '#',      'admin', NOW(), '0', '0'),
(2362, '指标新增', 2360, 2,  '#',    '',                     'F', 'business:dora:add',    '#',      'admin', NOW(), '0', '0'),
(2363, '指标修改', 2360, 3,  '#',    '',                     'F', 'business:dora:edit',   '#',      'admin', NOW(), '0', '0'),
(2364, '指标删除', 2360, 4,  '#',    '',                     'F', 'business:dora:remove', '#',      'admin', NOW(), '0', '0'),
(2365, '指标导出', 2360, 5,  '#',    '',                     'F', 'business:dora:export', '#',      'admin', NOW(), '0', '0');

-- 授权给 admin (role_id=1) — 所有新菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2200 AND 2365;

-- 验证
SELECT '已新增菜单数' AS metric, COUNT(*) AS value FROM sys_menu WHERE menu_id BETWEEN 2200 AND 2365
UNION ALL
SELECT '已授权数 (role_id=1)', COUNT(*) FROM sys_role_menu WHERE menu_id BETWEEN 2200 AND 2365
UNION ALL
SELECT 'business 菜单总数', COUNT(*) FROM sys_menu WHERE perms LIKE 'business:%';

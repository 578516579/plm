-- =============================================================================
-- PRD-align 收官 18 个模块的 sys_menu 补全
-- (跟 business-v04-stubs-menus.sql 一个套路，2140-2315 区段连续分配)
--
-- 模块 ID 段 (每模块 10 个 slot,1 个 C + 5 个 F + 4 个备用):
--   2140-2145 inception        立项 AI
--   2150-2155 competitive      竞品情报
--   2160-2165 prd              PRD
--   2170-2175 ued              UED 设计
--   2180-2185 arch             架构设计
--   2190-2195 dbdesign         数据库设计
--   2200-2205 apidesign        接口设计
--   2210-2215 ai-agent         AI Agent
--   2220-2225 openspec         AI OpenSpec
--   2230-2235 testdata         测试数据工厂
--   2240-2245 autotest         自动化测试
--   2250-2255 manual-impl      实施手册
--   2260-2265 manual-ops       运维手册
--   2270-2275 analytics        效能分析
--   2280-2285 dashboard        工作台
--   2290-2295 pipeline         CI/CD 流水线
--   2300-2305 feature-flag     Feature Flag
--   2310-2315 dora             DORA 指标
--
-- AI 端点 (POST /business/<entity>/ai/<verb>/{id}) 复用 :edit 权限,不单列菜单
-- (跟 controller 里 @PreAuthorize("@ss.hasPermi('business:<x>:edit')") 一致)
-- =============================================================================

-- =========================================
-- F1 立项: inception (2140-2145) + competitive (2150-2155)
-- =========================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2140, '立项管理',   2000, 15, 'inception', 'business/inception/index', 'C', '0', '0', 'business:inception:list',   'opportunity', 'admin', SYSDATE(), 'PRD §F1.1'),
(2141, '立项查询',   2140, 1,  '#', '', 'F', '0', '0', 'business:inception:query',  '#', 'admin', SYSDATE(), ''),
(2142, '立项新增',   2140, 2,  '#', '', 'F', '0', '0', 'business:inception:add',    '#', 'admin', SYSDATE(), ''),
(2143, '立项修改',   2140, 3,  '#', '', 'F', '0', '0', 'business:inception:edit',   '#', 'admin', SYSDATE(), 'AI bootstrap 也用此权限'),
(2144, '立项删除',   2140, 4,  '#', '', 'F', '0', '0', 'business:inception:remove', '#', 'admin', SYSDATE(), ''),
(2145, '立项导出',   2140, 5,  '#', '', 'F', '0', '0', 'business:inception:export', '#', 'admin', SYSDATE(), ''),

(2150, '竞品情报',   2000, 16, 'competitive', 'business/competitive/index', 'C', '0', '0', 'business:competitive:list',   'discover', 'admin', SYSDATE(), 'PRD §F1.3'),
(2151, '竞品查询',   2150, 1,  '#', '', 'F', '0', '0', 'business:competitive:query',  '#', 'admin', SYSDATE(), ''),
(2152, '竞品新增',   2150, 2,  '#', '', 'F', '0', '0', 'business:competitive:add',    '#', 'admin', SYSDATE(), ''),
(2153, '竞品修改',   2150, 3,  '#', '', 'F', '0', '0', 'business:competitive:edit',   '#', 'admin', SYSDATE(), ''),
(2154, '竞品删除',   2150, 4,  '#', '', 'F', '0', '0', 'business:competitive:remove', '#', 'admin', SYSDATE(), ''),
(2155, '竞品导出',   2150, 5,  '#', '', 'F', '0', '0', 'business:competitive:export', '#', 'admin', SYSDATE(), ''),

-- =========================================
-- F2 设计: prd (2160) + ued (2170)
-- =========================================
(2160, 'PRD 管理',  2000, 17, 'prd', 'business/prd/index', 'C', '0', '0', 'business:prd:list',   'documentation', 'admin', SYSDATE(), 'PRD §F2.2'),
(2161, 'PRD 查询',  2160, 1,  '#', '', 'F', '0', '0', 'business:prd:query',  '#', 'admin', SYSDATE(), ''),
(2162, 'PRD 新增',  2160, 2,  '#', '', 'F', '0', '0', 'business:prd:add',    '#', 'admin', SYSDATE(), ''),
(2163, 'PRD 修改',  2160, 3,  '#', '', 'F', '0', '0', 'business:prd:edit',   '#', 'admin', SYSDATE(), ''),
(2164, 'PRD 删除',  2160, 4,  '#', '', 'F', '0', '0', 'business:prd:remove', '#', 'admin', SYSDATE(), ''),
(2165, 'PRD 导出',  2160, 5,  '#', '', 'F', '0', '0', 'business:prd:export', '#', 'admin', SYSDATE(), ''),

(2170, 'UED 设计',  2000, 18, 'ued', 'business/ued/index', 'C', '0', '0', 'business:ued:list',   'magic-stick', 'admin', SYSDATE(), 'PRD §F2.3'),
(2171, 'UED 查询',  2170, 1,  '#', '', 'F', '0', '0', 'business:ued:query',  '#', 'admin', SYSDATE(), ''),
(2172, 'UED 新增',  2170, 2,  '#', '', 'F', '0', '0', 'business:ued:add',    '#', 'admin', SYSDATE(), ''),
(2173, 'UED 修改',  2170, 3,  '#', '', 'F', '0', '0', 'business:ued:edit',   '#', 'admin', SYSDATE(), ''),
(2174, 'UED 删除',  2170, 4,  '#', '', 'F', '0', '0', 'business:ued:remove', '#', 'admin', SYSDATE(), ''),
(2175, 'UED 导出',  2170, 5,  '#', '', 'F', '0', '0', 'business:ued:export', '#', 'admin', SYSDATE(), ''),

-- =========================================
-- F3 研发: arch (2180) + dbdesign (2190) + apidesign (2200) + ai-agent (2210) + openspec (2220)
-- =========================================
(2180, '架构设计',   2000, 19, 'arch', 'business/arch/index', 'C', '0', '0', 'business:arch:list',   'cascader', 'admin', SYSDATE(), 'PRD §F3.1'),
(2181, '架构查询',   2180, 1,  '#', '', 'F', '0', '0', 'business:arch:query',  '#', 'admin', SYSDATE(), ''),
(2182, '架构新增',   2180, 2,  '#', '', 'F', '0', '0', 'business:arch:add',    '#', 'admin', SYSDATE(), ''),
(2183, '架构修改',   2180, 3,  '#', '', 'F', '0', '0', 'business:arch:edit',   '#', 'admin', SYSDATE(), ''),
(2184, '架构删除',   2180, 4,  '#', '', 'F', '0', '0', 'business:arch:remove', '#', 'admin', SYSDATE(), ''),
(2185, '架构导出',   2180, 5,  '#', '', 'F', '0', '0', 'business:arch:export', '#', 'admin', SYSDATE(), ''),

(2190, '数据库设计', 2000, 20, 'dbdesign', 'business/dbdesign/index', 'C', '0', '0', 'business:dbdesign:list',   'database', 'admin', SYSDATE(), 'PRD §F3.2'),
(2191, 'DB 查询',   2190, 1,  '#', '', 'F', '0', '0', 'business:dbdesign:query',  '#', 'admin', SYSDATE(), ''),
(2192, 'DB 新增',   2190, 2,  '#', '', 'F', '0', '0', 'business:dbdesign:add',    '#', 'admin', SYSDATE(), ''),
(2193, 'DB 修改',   2190, 3,  '#', '', 'F', '0', '0', 'business:dbdesign:edit',   '#', 'admin', SYSDATE(), ''),
(2194, 'DB 删除',   2190, 4,  '#', '', 'F', '0', '0', 'business:dbdesign:remove', '#', 'admin', SYSDATE(), ''),
(2195, 'DB 导出',   2190, 5,  '#', '', 'F', '0', '0', 'business:dbdesign:export', '#', 'admin', SYSDATE(), ''),

(2200, '接口设计',   2000, 21, 'apidesign', 'business/apidesign/index', 'C', '0', '0', 'business:apidesign:list',   'link', 'admin', SYSDATE(), 'PRD §F3.3'),
(2201, '接口查询',   2200, 1,  '#', '', 'F', '0', '0', 'business:apidesign:query',  '#', 'admin', SYSDATE(), ''),
(2202, '接口新增',   2200, 2,  '#', '', 'F', '0', '0', 'business:apidesign:add',    '#', 'admin', SYSDATE(), ''),
(2203, '接口修改',   2200, 3,  '#', '', 'F', '0', '0', 'business:apidesign:edit',   '#', 'admin', SYSDATE(), ''),
(2204, '接口删除',   2200, 4,  '#', '', 'F', '0', '0', 'business:apidesign:remove', '#', 'admin', SYSDATE(), ''),
(2205, '接口导出',   2200, 5,  '#', '', 'F', '0', '0', 'business:apidesign:export', '#', 'admin', SYSDATE(), ''),

(2210, 'AI Agent',  2000, 22, 'ai-agent', 'business/ai-agent/index', 'C', '0', '0', 'business:ai-agent:list',   'bug', 'admin', SYSDATE(), 'PRD §F3.5'),
(2211, 'Agent 查询', 2210, 1,  '#', '', 'F', '0', '0', 'business:ai-agent:query',  '#', 'admin', SYSDATE(), ''),
(2212, 'Agent 新增', 2210, 2,  '#', '', 'F', '0', '0', 'business:ai-agent:add',    '#', 'admin', SYSDATE(), ''),
(2213, 'Agent 修改', 2210, 3,  '#', '', 'F', '0', '0', 'business:ai-agent:edit',   '#', 'admin', SYSDATE(), 'invoke 复用此权限'),
(2214, 'Agent 删除', 2210, 4,  '#', '', 'F', '0', '0', 'business:ai-agent:remove', '#', 'admin', SYSDATE(), ''),
(2215, 'Agent 导出', 2210, 5,  '#', '', 'F', '0', '0', 'business:ai-agent:export', '#', 'admin', SYSDATE(), ''),

(2220, 'OpenSpec',  2000, 23, 'openspec', 'business/openspec/index', 'C', '0', '0', 'business:openspec:list',   'star', 'admin', SYSDATE(), 'PRD §F3.5'),
(2221, '规范查询',   2220, 1,  '#', '', 'F', '0', '0', 'business:openspec:query',  '#', 'admin', SYSDATE(), ''),
(2222, '规范新增',   2220, 2,  '#', '', 'F', '0', '0', 'business:openspec:add',    '#', 'admin', SYSDATE(), ''),
(2223, '规范修改',   2220, 3,  '#', '', 'F', '0', '0', 'business:openspec:edit',   '#', 'admin', SYSDATE(), ''),
(2224, '规范删除',   2220, 4,  '#', '', 'F', '0', '0', 'business:openspec:remove', '#', 'admin', SYSDATE(), ''),
(2225, '规范导出',   2220, 5,  '#', '', 'F', '0', '0', 'business:openspec:export', '#', 'admin', SYSDATE(), ''),

-- =========================================
-- F4 质量: testdata (2230) + autotest (2240)
-- =========================================
(2230, '测试数据',   2000, 24, 'testdata', 'business/testdata/index', 'C', '0', '0', 'business:testdata:list',   'guide', 'admin', SYSDATE(), 'PRD §F4.3'),
(2231, '数据查询',   2230, 1,  '#', '', 'F', '0', '0', 'business:testdata:query',  '#', 'admin', SYSDATE(), ''),
(2232, '数据新增',   2230, 2,  '#', '', 'F', '0', '0', 'business:testdata:add',    '#', 'admin', SYSDATE(), ''),
(2233, '数据修改',   2230, 3,  '#', '', 'F', '0', '0', 'business:testdata:edit',   '#', 'admin', SYSDATE(), ''),
(2234, '数据删除',   2230, 4,  '#', '', 'F', '0', '0', 'business:testdata:remove', '#', 'admin', SYSDATE(), ''),
(2235, '数据导出',   2230, 5,  '#', '', 'F', '0', '0', 'business:testdata:export', '#', 'admin', SYSDATE(), ''),

(2240, '自动化测试', 2000, 25, 'autotest', 'business/autotest/index', 'C', '0', '0', 'business:autotest:list',   'pie-chart', 'admin', SYSDATE(), 'PRD §F4.5'),
(2241, '套件查询',   2240, 1,  '#', '', 'F', '0', '0', 'business:autotest:query',  '#', 'admin', SYSDATE(), ''),
(2242, '套件新增',   2240, 2,  '#', '', 'F', '0', '0', 'business:autotest:add',    '#', 'admin', SYSDATE(), ''),
(2243, '套件修改',   2240, 3,  '#', '', 'F', '0', '0', 'business:autotest:edit',   '#', 'admin', SYSDATE(), ''),
(2244, '套件删除',   2240, 4,  '#', '', 'F', '0', '0', 'business:autotest:remove', '#', 'admin', SYSDATE(), ''),
(2245, '套件导出',   2240, 5,  '#', '', 'F', '0', '0', 'business:autotest:export', '#', 'admin', SYSDATE(), ''),

-- =========================================
-- F5 文档: manual-impl (2250) + manual-ops (2260)
-- =========================================
(2250, '实施手册',   2000, 26, 'manual-impl', 'business/manual-impl/index', 'C', '0', '0', 'business:manual-impl:list',   'edit', 'admin', SYSDATE(), 'PRD §F5.2'),
(2251, '手册查询',   2250, 1,  '#', '', 'F', '0', '0', 'business:manual-impl:query',  '#', 'admin', SYSDATE(), ''),
(2252, '手册新增',   2250, 2,  '#', '', 'F', '0', '0', 'business:manual-impl:add',    '#', 'admin', SYSDATE(), ''),
(2253, '手册修改',   2250, 3,  '#', '', 'F', '0', '0', 'business:manual-impl:edit',   '#', 'admin', SYSDATE(), ''),
(2254, '手册删除',   2250, 4,  '#', '', 'F', '0', '0', 'business:manual-impl:remove', '#', 'admin', SYSDATE(), ''),
(2255, '手册导出',   2250, 5,  '#', '', 'F', '0', '0', 'business:manual-impl:export', '#', 'admin', SYSDATE(), ''),

(2260, '运维手册',   2000, 27, 'manual-ops', 'business/manual-ops/index', 'C', '0', '0', 'business:manual-ops:list',   'monitor', 'admin', SYSDATE(), 'PRD §F5.3'),
(2261, '手册查询',   2260, 1,  '#', '', 'F', '0', '0', 'business:manual-ops:query',  '#', 'admin', SYSDATE(), ''),
(2262, '手册新增',   2260, 2,  '#', '', 'F', '0', '0', 'business:manual-ops:add',    '#', 'admin', SYSDATE(), ''),
(2263, '手册修改',   2260, 3,  '#', '', 'F', '0', '0', 'business:manual-ops:edit',   '#', 'admin', SYSDATE(), ''),
(2264, '手册删除',   2260, 4,  '#', '', 'F', '0', '0', 'business:manual-ops:remove', '#', 'admin', SYSDATE(), ''),
(2265, '手册导出',   2260, 5,  '#', '', 'F', '0', '0', 'business:manual-ops:export', '#', 'admin', SYSDATE(), ''),

-- =========================================
-- F6 效能: analytics (2270) + dashboard (2280)
-- =========================================
(2270, '效能分析',   2000, 28, 'analytics', 'business/analytics/index', 'C', '0', '0', 'business:analytics:list',   'chart', 'admin', SYSDATE(), 'PRD §F6 + DORA'),
(2271, '快照查询',   2270, 1,  '#', '', 'F', '0', '0', 'business:analytics:query',  '#', 'admin', SYSDATE(), ''),
(2272, '快照新增',   2270, 2,  '#', '', 'F', '0', '0', 'business:analytics:add',    '#', 'admin', SYSDATE(), ''),
(2273, '快照修改',   2270, 3,  '#', '', 'F', '0', '0', 'business:analytics:edit',   '#', 'admin', SYSDATE(), 'AI 复盘也用此权限'),
(2274, '快照删除',   2270, 4,  '#', '', 'F', '0', '0', 'business:analytics:remove', '#', 'admin', SYSDATE(), ''),
(2275, '快照导出',   2270, 5,  '#', '', 'F', '0', '0', 'business:analytics:export', '#', 'admin', SYSDATE(), ''),

(2280, '工作台',     2000, 29, 'dashboard', 'business/dashboard/index', 'C', '0', '0', 'business:dashboard:list',   'dashboard', 'admin', SYSDATE(), 'UI §4.2'),
(2281, '工作台查询', 2280, 1,  '#', '', 'F', '0', '0', 'business:dashboard:query',  '#', 'admin', SYSDATE(), '/aggregate 复用'),
(2282, '工作台新增', 2280, 2,  '#', '', 'F', '0', '0', 'business:dashboard:add',    '#', 'admin', SYSDATE(), ''),
(2283, '工作台修改', 2280, 3,  '#', '', 'F', '0', '0', 'business:dashboard:edit',   '#', 'admin', SYSDATE(), ''),
(2284, '工作台删除', 2280, 4,  '#', '', 'F', '0', '0', 'business:dashboard:remove', '#', 'admin', SYSDATE(), ''),
(2285, '工作台导出', 2280, 5,  '#', '', 'F', '0', '0', 'business:dashboard:export', '#', 'admin', SYSDATE(), ''),

-- =========================================
-- DevOps: pipeline (2290) + feature-flag (2300) + dora (2310)
-- =========================================
(2290, 'CI/CD 流水线', 2000, 30, 'pipeline', 'business/pipeline/index', 'C', '0', '0', 'business:pipeline:list',   'build', 'admin', SYSDATE(), 'DevOps 扩展'),
(2291, '流水线查询',   2290, 1,  '#', '', 'F', '0', '0', 'business:pipeline:query',  '#', 'admin', SYSDATE(), ''),
(2292, '流水线新增',   2290, 2,  '#', '', 'F', '0', '0', 'business:pipeline:add',    '#', 'admin', SYSDATE(), ''),
(2293, '流水线修改',   2290, 3,  '#', '', 'F', '0', '0', 'business:pipeline:edit',   '#', 'admin', SYSDATE(), 'trigger 复用此权限'),
(2294, '流水线删除',   2290, 4,  '#', '', 'F', '0', '0', 'business:pipeline:remove', '#', 'admin', SYSDATE(), ''),
(2295, '流水线导出',   2290, 5,  '#', '', 'F', '0', '0', 'business:pipeline:export', '#', 'admin', SYSDATE(), ''),

(2300, 'Feature Flag', 2000, 31, 'feature-flag', 'business/feature-flag/index', 'C', '0', '0', 'business:feature-flag:list',   'switch', 'admin', SYSDATE(), 'DevOps 扩展 灰度发布'),
(2301, 'Flag 查询',    2300, 1,  '#', '', 'F', '0', '0', 'business:feature-flag:query',  '#', 'admin', SYSDATE(), '/check 复用'),
(2302, 'Flag 新增',    2300, 2,  '#', '', 'F', '0', '0', 'business:feature-flag:add',    '#', 'admin', SYSDATE(), ''),
(2303, 'Flag 修改',    2300, 3,  '#', '', 'F', '0', '0', 'business:feature-flag:edit',   '#', 'admin', SYSDATE(), ''),
(2304, 'Flag 删除',    2300, 4,  '#', '', 'F', '0', '0', 'business:feature-flag:remove', '#', 'admin', SYSDATE(), ''),
(2305, 'Flag 导出',    2300, 5,  '#', '', 'F', '0', '0', 'business:feature-flag:export', '#', 'admin', SYSDATE(), ''),

(2310, 'DORA 指标',    2000, 32, 'dora', 'business/dora/index', 'C', '0', '0', 'business:dora:list',   'rate', 'admin', SYSDATE(), 'DevOps 扩展 DORA 4 指标'),
(2311, 'DORA 查询',    2310, 1,  '#', '', 'F', '0', '0', 'business:dora:query',  '#', 'admin', SYSDATE(), ''),
(2312, 'DORA 新增',    2310, 2,  '#', '', 'F', '0', '0', 'business:dora:add',    '#', 'admin', SYSDATE(), ''),
(2313, 'DORA 修改',    2310, 3,  '#', '', 'F', '0', '0', 'business:dora:edit',   '#', 'admin', SYSDATE(), 'AI suggest 复用'),
(2314, 'DORA 删除',    2310, 4,  '#', '', 'F', '0', '0', 'business:dora:remove', '#', 'admin', SYSDATE(), ''),
(2315, 'DORA 导出',    2310, 5,  '#', '', 'F', '0', '0', 'business:dora:export', '#', 'admin', SYSDATE(), '');

-- admin 角色授权 (108 项)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2140 AND 2315;

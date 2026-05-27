-- =============================================================================
-- 末批 7 个 🟡→🟢 模块菜单创建 + 阶段归位 (idempotent, create-or-reparent)
-- =============================================================================
-- 背景:
--   menu-seed-prd-aligned-modules.sql 给这 7 模块分配 menu_id 2280-2365 但
--   该 seed 在当前 DB 上未跑/被回滚(同 menu-fill-missing-8.sql 注记),DB 里
--   这 7 个模块的 sys_menu 记录全空 → 左侧导航看不到入口。
--   menu-regroup-by-phase.sql 行 92-104 想重挂这些模块,但引用了与 seed 不一致
--   的 stale id(2710-2780)→ UPDATE 命中 0 行 → 没生效。
--
--   本脚本直接按 menu-fill-missing-8.sql 同款模式,用 seed 的真实 id(2280-2365)
--   CREATE 这 7 模块的 C 目录 + 5 F 按钮,并挂到阶段目录;path 用绝对
--   /business/<entity>(与 2130/2200 等现存模块一致,避免相对路径 404)。
--   ON DUPLICATE KEY UPDATE 保证可重复执行(若 seed 旧行存在则纠正 parent/path)。
--
--   (ai-agent 2320 已由 menu-fill-missing-8.sql 挂 2960,不在本脚本范围)
--
-- 阶段目录: 2950 交付与运维 / 2960 AI 能力 / 2970 分析报表
-- 回滚: menu-regroup-remaining-7-rollback.sql
-- =============================================================================

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
-- ===== 交付与运维 (2950) =====
-- 实施手册 manual-impl → order 3 (PRD F5.2)
(2280, '实施手册', 2950, 3, '/business/manual-impl', 'business/manual-impl/index', 'C', '0', '0', 'business:manual-impl:list',   'guide', 'admin', NOW(), 'PRD F5.2'),
(2281, '手册查询', 2280, 1, '#', '', 'F', '0', '0', 'business:manual-impl:query',  '#', 'admin', NOW(), ''),
(2282, '手册新增', 2280, 2, '#', '', 'F', '0', '0', 'business:manual-impl:add',    '#', 'admin', NOW(), ''),
(2283, '手册修改', 2280, 3, '#', '', 'F', '0', '0', 'business:manual-impl:edit',   '#', 'admin', NOW(), ''),
(2284, '手册删除', 2280, 4, '#', '', 'F', '0', '0', 'business:manual-impl:remove', '#', 'admin', NOW(), ''),
(2285, '手册导出', 2280, 5, '#', '', 'F', '0', '0', 'business:manual-impl:export', '#', 'admin', NOW(), ''),
-- 运维手册 manual-ops → order 4 (PRD F5.3)
(2290, '运维手册', 2950, 4, '/business/manual-ops', 'business/manual-ops/index', 'C', '0', '0', 'business:manual-ops:list',   'tool', 'admin', NOW(), 'PRD F5.3'),
(2291, '运维查询', 2290, 1, '#', '', 'F', '0', '0', 'business:manual-ops:query',  '#', 'admin', NOW(), ''),
(2292, '运维新增', 2290, 2, '#', '', 'F', '0', '0', 'business:manual-ops:add',    '#', 'admin', NOW(), ''),
(2293, '运维修改', 2290, 3, '#', '', 'F', '0', '0', 'business:manual-ops:edit',   '#', 'admin', NOW(), ''),
(2294, '运维删除', 2290, 4, '#', '', 'F', '0', '0', 'business:manual-ops:remove', '#', 'admin', NOW(), ''),
(2295, '运维导出', 2290, 5, '#', '', 'F', '0', '0', 'business:manual-ops:export', '#', 'admin', NOW(), ''),
-- CI/CD 流水线 pipeline → order 5
(2340, 'CI/CD 流水线', 2950, 5, '/business/pipeline', 'business/pipeline/index', 'C', '0', '0', 'business:pipeline:list',   'github', 'admin', NOW(), 'DevOps'),
(2341, '流水线查询', 2340, 1, '#', '', 'F', '0', '0', 'business:pipeline:query',  '#', 'admin', NOW(), ''),
(2342, '流水线新增', 2340, 2, '#', '', 'F', '0', '0', 'business:pipeline:add',    '#', 'admin', NOW(), ''),
(2343, '流水线修改', 2340, 3, '#', '', 'F', '0', '0', 'business:pipeline:edit',   '#', 'admin', NOW(), ''),
(2344, '流水线删除', 2340, 4, '#', '', 'F', '0', '0', 'business:pipeline:remove', '#', 'admin', NOW(), ''),
(2345, '流水线导出', 2340, 5, '#', '', 'F', '0', '0', 'business:pipeline:export', '#', 'admin', NOW(), ''),
-- Feature Flag feature-flag → order 7
(2350, 'Feature Flag', 2950, 7, '/business/feature-flag', 'business/feature-flag/index', 'C', '0', '0', 'business:feature-flag:list',   'switch', 'admin', NOW(), 'DevOps'),
(2351, 'Flag 查询', 2350, 1, '#', '', 'F', '0', '0', 'business:feature-flag:query',  '#', 'admin', NOW(), ''),
(2352, 'Flag 新增', 2350, 2, '#', '', 'F', '0', '0', 'business:feature-flag:add',    '#', 'admin', NOW(), ''),
(2353, 'Flag 修改', 2350, 3, '#', '', 'F', '0', '0', 'business:feature-flag:edit',   '#', 'admin', NOW(), ''),
(2354, 'Flag 删除', 2350, 4, '#', '', 'F', '0', '0', 'business:feature-flag:remove', '#', 'admin', NOW(), ''),
(2355, 'Flag 导出', 2350, 5, '#', '', 'F', '0', '0', 'business:feature-flag:export', '#', 'admin', NOW(), ''),
-- DORA 指标 dora → order 8
(2360, 'DORA 指标', 2950, 8, '/business/dora', 'business/dora/index', 'C', '0', '0', 'business:dora:list',   'chart', 'admin', NOW(), 'DevOps'),
(2361, '指标查询', 2360, 1, '#', '', 'F', '0', '0', 'business:dora:query',  '#', 'admin', NOW(), ''),
(2362, '指标新增', 2360, 2, '#', '', 'F', '0', '0', 'business:dora:add',    '#', 'admin', NOW(), ''),
(2363, '指标修改', 2360, 3, '#', '', 'F', '0', '0', 'business:dora:edit',   '#', 'admin', NOW(), ''),
(2364, '指标删除', 2360, 4, '#', '', 'F', '0', '0', 'business:dora:remove', '#', 'admin', NOW(), ''),
(2365, '指标导出', 2360, 5, '#', '', 'F', '0', '0', 'business:dora:export', '#', 'admin', NOW(), ''),
-- ===== AI 能力 (2960) =====
-- AI OpenSpec openspec → order 1 (PRD F3.5)
(2330, 'AI OpenSpec', 2960, 1, '/business/openspec', 'business/openspec/index', 'C', '0', '0', 'business:openspec:list',   'log', 'admin', NOW(), 'PRD F3.5'),
(2331, 'Spec 查询', 2330, 1, '#', '', 'F', '0', '0', 'business:openspec:query',  '#', 'admin', NOW(), ''),
(2332, 'Spec 新增', 2330, 2, '#', '', 'F', '0', '0', 'business:openspec:add',    '#', 'admin', NOW(), ''),
(2333, 'Spec 修改', 2330, 3, '#', '', 'F', '0', '0', 'business:openspec:edit',   '#', 'admin', NOW(), ''),
(2334, 'Spec 删除', 2330, 4, '#', '', 'F', '0', '0', 'business:openspec:remove', '#', 'admin', NOW(), ''),
(2335, 'Spec 导出', 2330, 5, '#', '', 'F', '0', '0', 'business:openspec:export', '#', 'admin', NOW(), ''),
-- ===== 分析报表 (2970) =====
-- 效能分析 analytics → order 1 (PRD F6)
(2300, '效能分析', 2970, 1, '/business/analytics', 'business/analytics/index', 'C', '0', '0', 'business:analytics:list',   'chart', 'admin', NOW(), 'PRD F6'),
(2301, '快照查询', 2300, 1, '#', '', 'F', '0', '0', 'business:analytics:query',  '#', 'admin', NOW(), ''),
(2302, '快照新增', 2300, 2, '#', '', 'F', '0', '0', 'business:analytics:add',    '#', 'admin', NOW(), ''),
(2303, '快照修改', 2300, 3, '#', '', 'F', '0', '0', 'business:analytics:edit',   '#', 'admin', NOW(), ''),
(2304, '快照删除', 2300, 4, '#', '', 'F', '0', '0', 'business:analytics:remove', '#', 'admin', NOW(), ''),
(2305, '快照导出', 2300, 5, '#', '', 'F', '0', '0', 'business:analytics:export', '#', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE
    menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num),
    path=VALUES(path), component=VALUES(component), perms=VALUES(perms),
    visible=VALUES(visible), status=VALUES(status);

-- ===== 授权 admin (role_id=1) — 7 模块 × 6 条 (ai-agent 2320-2325 已由 fill-8 授权) =====
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2280 AND 2365;

-- ===== 验证 =====
SELECT '末批 7 模块阶段归位' AS section,
       p.menu_id AS phase_id, p.menu_name AS phase, m.menu_id, m.menu_name AS module, m.order_num AS ord
FROM sys_menu m JOIN sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_id IN (2280,2290,2300,2330,2340,2350,2360)
ORDER BY p.order_num, m.order_num;

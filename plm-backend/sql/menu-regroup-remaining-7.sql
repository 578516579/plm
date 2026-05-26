-- =============================================================================
-- 末批 7 个 🟡→🟢 模块菜单可见性修复 (idempotent)
-- =============================================================================
-- 背景:
--   menu-seed-prd-aligned-modules.sql 给这 7 模块分配的 menu_id 是
--     manual-impl 2280 / manual-ops 2290 / analytics 2300 / openspec 2330 /
--     pipeline 2340 / feature-flag 2350 / dora 2360
--   并把它们挂在 parent_id=2000「业务管理」(menu-regroup-by-phase.sql 已把 2000
--   置 visible='1' 隐藏) → 这 7 个模块入口在左侧导航不可见。
--
--   menu-regroup-by-phase.sql 行 92-104 本想重挂这些模块到阶段目录,但引用了
--   与 seed 不一致的 stale menu_id (2710/2720/2730/2760/2770/2780),那些 id
--   在 DB 里不存在 → UPDATE 命中 0 行 → 没生效。本脚本用 seed 的真实 id 修正。
--
--   (ai-agent 2320 已由 menu-fill-missing-8.sql 正确挂到 2960,不在本脚本范围)
--
-- 阶段目录 (menu-regroup-by-phase.sql 建): 2950 交付与运维 / 2960 AI 能力 / 2970 分析报表
-- 回滚: menu-regroup-remaining-7-rollback.sql
-- =============================================================================

-- ============ 交付与运维 (2950): 手册 → CI/CD → Flag → DORA ============
UPDATE sys_menu SET parent_id=2950, order_num=3 WHERE menu_id=2280;   -- 实施手册 manual-impl (PRD F5.2)
UPDATE sys_menu SET parent_id=2950, order_num=4 WHERE menu_id=2290;   -- 运维手册 manual-ops  (PRD F5.3)
UPDATE sys_menu SET parent_id=2950, order_num=5 WHERE menu_id=2340;   -- CI/CD 流水线 pipeline
UPDATE sys_menu SET parent_id=2950, order_num=7 WHERE menu_id=2350;   -- Feature Flag feature-flag
UPDATE sys_menu SET parent_id=2950, order_num=8 WHERE menu_id=2360;   -- DORA 指标 dora

-- ============ AI 能力 (2960): OpenSpec → Agent(已挂) → 调用审计 ============
UPDATE sys_menu SET parent_id=2960, order_num=1 WHERE menu_id=2330;   -- AI OpenSpec openspec (PRD F3.5)

-- ============ 分析报表 (2970): 效能分析 ============
UPDATE sys_menu SET parent_id=2970, order_num=1 WHERE menu_id=2300;   -- 效能分析 analytics (PRD F6)

-- ============ 确保 admin (role_id=1) 已授权 (seed 已做, 这里兜底 idempotent) ============
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_id IN (2280,2281,2282,2283,2284,2285,
                  2290,2291,2292,2293,2294,2295,
                  2300,2301,2302,2303,2304,2305,
                  2330,2331,2332,2333,2334,2335,
                  2340,2341,2342,2343,2344,2345,
                  2350,2351,2352,2353,2354,2355,
                  2360,2361,2362,2363,2364,2365);

-- ============ 验证 ============
SELECT '末批 7 模块阶段归位' AS section,
       p.menu_id AS phase_id, p.menu_name AS phase, m.menu_id, m.menu_name AS module, m.order_num AS ord
FROM sys_menu m JOIN sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_id IN (2280,2290,2300,2330,2340,2350,2360)
ORDER BY p.order_num, m.order_num;

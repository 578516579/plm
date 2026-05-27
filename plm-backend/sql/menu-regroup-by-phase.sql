-- =============================================================================
-- 业务菜单按 PRD 原型 8 阶段重新分组 (idempotent, 基于 DB 实际现状)
-- =============================================================================
-- 背景:
--   DB 中业务菜单 26 个 C-type 平铺在 menu_id=2000「业务管理」下,无分组。
--   另发现历史遗留问题(本脚本顺手修):
--     a) menu-seed-prd-aligned-modules.sql 未跑全 — DB 缺 8 模块菜单
--        (inception/prd/competitive/arch/dbdesign/apidesign/testdata/ai-agent)
--        本脚本【不补缺】,缺菜单作单独任务处理。
--     b) business-ued.sql 漏 INSERT — 本脚本顺手补 UED 菜单 (2140-2145)
--     c) 2037「我的任务」parent_id=0 错挂根目录 — 本脚本归位到研发阶段
--     d) 2700 autotest 与 2270 autotest 重复 (path 相同) — 本脚本将 2700
--        设 visible='1' 隐藏,保留 2270 (SQL seed 标准编号) 为可见版
--
-- 目标:
--   对齐 PRD 原型 agriplm_split/index.html 的 8 阶段流程分组,业务阶段为:
--   工作台 → 规划 → 需求设计 → 研发 → 测试 → 交付与运维 → AI 能力 → 分析报表
--
-- 影响 (idempotent — 可重复执行, 无破坏性 DELETE):
--   1. 新增 8 个一级目录 M-type (menu_id 2900-2970), 挂 parent_id=0
--   2. 补 UED 菜单 1 目录 + 5 按钮 (menu_id 2140-2145), 挂 2920 order=3
--   3. 旧「业务管理」(2000) 设 visible='1' 隐藏 (保留 audit trail, 不删)
--   4. DB 实际有的 26 个 C-type UPDATE parent_id + order_num 到新目录
--   5. 2700 重复 autotest 设 visible='1' 隐藏
--   6. 2400 MCP / 2500 外部集成 order_num 后移到 8 阶段之后 (13/14)
--
-- 回滚: menu-regroup-by-phase-rollback.sql
-- =============================================================================

-- ============ 1. 新增 8 个一级目录 (M-type, parent_id=0) ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2900, '工作台',     0, 5,  'workbench',    NULL, 'M', '0', '0', '', 'dashboard',  'admin', NOW(), '阶段分组(0) 工作台'),
(2910, '规划阶段',   0, 6,  'phase-plan',   NULL, 'M', '0', '0', '', 'guide',      'admin', NOW(), '阶段分组(1) 项目→立项→竞品'),
(2920, '需求与设计', 0, 7,  'phase-design', NULL, 'M', '0', '0', '', 'edit',       'admin', NOW(), '阶段分组(2) 需求→PRD→UED→架构→DB→API→文档'),
(2930, '研发阶段',   0, 8,  'phase-dev',    NULL, 'M', '0', '0', '', 'code',       'admin', NOW(), '阶段分组(3) 迭代→任务→看板'),
(2940, '测试阶段',   0, 9,  'phase-test',   NULL, 'M', '0', '0', '', 'bug',        'admin', NOW(), '阶段分组(4) 方案→用例→数据→提测→自动化→缺陷→报告'),
(2950, '交付与运维', 0, 10, 'phase-deploy', NULL, 'M', '0', '0', '', 'guide',      'admin', NOW(), '阶段分组(5) API/手册→CI/CD→发布→Flag→DORA'),
(2960, 'AI 能力',    0, 11, 'phase-ai',     NULL, 'M', '0', '0', '', 'star',       'admin', NOW(), '阶段分组(6) OpenSpec→Agent→调用审计'),
(2970, '分析报表',   0, 12, 'phase-report', NULL, 'M', '0', '0', '', 'chart',      'admin', NOW(), '阶段分组(7) 效能分析')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), order_num=VALUES(order_num), icon=VALUES(icon), remark=VALUES(remark);

-- ============ 2. 补 UED 菜单 (business-ued.sql 漏 INSERT 修复) ============
-- menu_id 2140-2145, 挂 2920 需求与设计 order=3
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2140, 'UED 设计', 2920, 3, 'ued', 'business/ued/index', 'C', '0', '0', 'business:ued:list',   'rate', 'admin', NOW(), 'UED 设计协同 F2.3 — 补 business-ued.sql 漏 INSERT'),
(2141, 'UED 查询', 2140, 1, '#',   '',                   'F', '0', '0', 'business:ued:query',  '#',    'admin', NOW(), ''),
(2142, 'UED 新增', 2140, 2, '#',   '',                   'F', '0', '0', 'business:ued:add',    '#',    'admin', NOW(), ''),
(2143, 'UED 修改', 2140, 3, '#',   '',                   'F', '0', '0', 'business:ued:edit',   '#',    'admin', NOW(), ''),
(2144, 'UED 删除', 2140, 4, '#',   '',                   'F', '0', '0', 'business:ued:remove', '#',    'admin', NOW(), ''),
(2145, 'UED 导出', 2140, 5, '#',   '',                   'F', '0', '0', 'business:ued:export', '#',    'admin', NOW(), '')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num);

-- ============ 3. 隐藏旧「业务管理」目录 (保留 audit trail) ============
UPDATE sys_menu SET visible='1', remark='已按阶段分组(2900-2970), 本目录隐藏'
WHERE menu_id=2000;

-- ============ 4. 迁移 26 个 C-type 菜单到新一级目录, 按业务流程重排 ============

-- 4.1 工作台 (2900)
UPDATE sys_menu SET parent_id=2900, order_num=1 WHERE menu_id=2740;   -- dashboard

-- 4.2 规划阶段 (2910): 项目 (currently only project; inception/competitive 缺)
UPDATE sys_menu SET parent_id=2910, order_num=1 WHERE menu_id=2010;   -- project

-- 4.3 需求与设计 (2920): 需求→UED(2140 已挂)→文档
UPDATE sys_menu SET parent_id=2920, order_num=1 WHERE menu_id=2020;   -- requirement
-- UED (2140) order=3 已在 §2 INSERT
UPDATE sys_menu SET parent_id=2920, order_num=7 WHERE menu_id=2070;   -- document
-- (prd/arch/dbdesign/apidesign 缺,占位 order=2/4/5/6 留给后续 seed 补)

-- 4.4 研发阶段 (2930): 迭代→任务→看板→我的任务
UPDATE sys_menu SET parent_id=2930, order_num=1 WHERE menu_id=2040;   -- sprint
UPDATE sys_menu SET parent_id=2930, order_num=2 WHERE menu_id=2030;   -- task
UPDATE sys_menu SET parent_id=2930, order_num=3 WHERE menu_id=2036;   -- task kanban
UPDATE sys_menu SET parent_id=2930, order_num=4 WHERE menu_id=2037;   -- my task (修复错挂 parent_id=0)

-- 4.5 测试阶段 (2940): 方案→用例→提测→自动化→缺陷→报告
UPDATE sys_menu SET parent_id=2940, order_num=1 WHERE menu_id=2100;   -- testplan
UPDATE sys_menu SET parent_id=2940, order_num=2 WHERE menu_id=2060;   -- testcase
UPDATE sys_menu SET parent_id=2940, order_num=4 WHERE menu_id=2080;   -- submission
UPDATE sys_menu SET parent_id=2940, order_num=5 WHERE menu_id=2270;   -- autotest (保留 SQL seed 编号)
UPDATE sys_menu SET parent_id=2940, order_num=6 WHERE menu_id=2050;   -- defect
UPDATE sys_menu SET parent_id=2940, order_num=7 WHERE menu_id=2110;   -- testreport
-- 2700 autotest 重复菜单 — 设 visible='1' 隐藏 (保留 audit, 不删)
UPDATE sys_menu SET parent_id=2940, order_num=99, visible='1',
                   remark='与 2270 重复(path=autotest),已隐藏 2026-05-25'
WHERE menu_id=2700;

-- 4.6 交付与运维 (2950): API→手册→CI/CD→发布→Flag→DORA
UPDATE sys_menu SET parent_id=2950, order_num=1 WHERE menu_id=2120;   -- apidoc
UPDATE sys_menu SET parent_id=2950, order_num=2 WHERE menu_id=2130;   -- manual-product
UPDATE sys_menu SET parent_id=2950, order_num=3 WHERE menu_id=2710;   -- manual-impl
UPDATE sys_menu SET parent_id=2950, order_num=4 WHERE menu_id=2720;   -- manual-ops
UPDATE sys_menu SET parent_id=2950, order_num=5 WHERE menu_id=2760;   -- pipeline
UPDATE sys_menu SET parent_id=2950, order_num=6 WHERE menu_id=2090;   -- release
UPDATE sys_menu SET parent_id=2950, order_num=7 WHERE menu_id=2770;   -- feature-flag
UPDATE sys_menu SET parent_id=2950, order_num=8 WHERE menu_id=2780;   -- dora

-- 4.7 AI 能力 (2960): OpenSpec→Agent(缺)→调用审计
UPDATE sys_menu SET parent_id=2960, order_num=1 WHERE menu_id=2750;   -- openspec (AI规范)
UPDATE sys_menu SET parent_id=2960, order_num=3 WHERE menu_id=2326;   -- ai-invocation-log

-- 4.8 分析报表 (2970)
UPDATE sys_menu SET parent_id=2970, order_num=1 WHERE menu_id=2730;   -- analytics

-- ============ 5. MCP/外部集成 order_num 后移到 8 阶段之后 ============
UPDATE sys_menu SET order_num=13 WHERE menu_id=2400;
UPDATE sys_menu SET order_num=14 WHERE menu_id=2500;

-- ============ 6. 授权 admin (role_id=1) — 新增 8 个一级目录 + UED 6 条 ============
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_id IN (2900,2910,2920,2930,2940,2950,2960,2970,
                  2140,2141,2142,2143,2144,2145);

-- ============ 7. 验证 ============
SELECT '一级目录(visible=0, ord)' label, menu_id, menu_name, order_num
FROM sys_menu WHERE parent_id=0 AND menu_type='M' AND visible='0' AND menu_id>=2400
ORDER BY order_num;

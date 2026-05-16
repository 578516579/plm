-- =============================================================================
-- v0.4 6 模块脚手架的菜单 + 字典补全（生成器输出 SQL 没含菜单,本文件补）
-- 关联: 03-开发/Stubs-Roadmap.md v0.4.1-2 排期
--
-- 菜单 ID 段:
--   2080-2086 plm-submission   提测管理
--   2090-2096 plm-release      发布管理
--   2100-2106 plm-testplan     测试方案
--   2110-2116 plm-testreport   测试报告
--   2120-2126 plm-apidoc       API 文档
--   2130-2136 plm-manual-product 产品手册
-- =============================================================================

-- =========================================
-- 1. plm-submission 提测管理 (2080-2086)
-- =========================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2080, '提测管理', 2000, 9,  'submission', 'business/submission/index', 'C', '0', '0', 'business:submission:list',   'eleme', 'admin', SYSDATE(), 'v0.4 scaffold'),
(2081, '提测查询', 2080, 1,  '#', '', 'F', '0', '0', 'business:submission:query',  '#', 'admin', SYSDATE(), ''),
(2082, '提测新增', 2080, 2,  '#', '', 'F', '0', '0', 'business:submission:add',    '#', 'admin', SYSDATE(), ''),
(2083, '提测修改', 2080, 3,  '#', '', 'F', '0', '0', 'business:submission:edit',   '#', 'admin', SYSDATE(), ''),
(2084, '提测删除', 2080, 4,  '#', '', 'F', '0', '0', 'business:submission:remove', '#', 'admin', SYSDATE(), ''),
(2085, '提测导出', 2080, 5,  '#', '', 'F', '0', '0', 'business:submission:export', '#', 'admin', SYSDATE(), ''),

(2090, '发布管理', 2000, 10, 'release', 'business/release/index', 'C', '0', '0', 'business:release:list',   'sell', 'admin', SYSDATE(), 'v0.4 scaffold'),
(2091, '发布查询', 2090, 1,  '#', '', 'F', '0', '0', 'business:release:query',  '#', 'admin', SYSDATE(), ''),
(2092, '发布新增', 2090, 2,  '#', '', 'F', '0', '0', 'business:release:add',    '#', 'admin', SYSDATE(), ''),
(2093, '发布修改', 2090, 3,  '#', '', 'F', '0', '0', 'business:release:edit',   '#', 'admin', SYSDATE(), ''),
(2094, '发布删除', 2090, 4,  '#', '', 'F', '0', '0', 'business:release:remove', '#', 'admin', SYSDATE(), ''),
(2095, '发布导出', 2090, 5,  '#', '', 'F', '0', '0', 'business:release:export', '#', 'admin', SYSDATE(), ''),

(2100, '测试方案', 2000, 11, 'testplan', 'business/testplan/index', 'C', '0', '0', 'business:testplan:list',   'notebook', 'admin', SYSDATE(), 'v0.4 scaffold'),
(2101, '方案查询', 2100, 1,  '#', '', 'F', '0', '0', 'business:testplan:query',  '#', 'admin', SYSDATE(), ''),
(2102, '方案新增', 2100, 2,  '#', '', 'F', '0', '0', 'business:testplan:add',    '#', 'admin', SYSDATE(), ''),
(2103, '方案修改', 2100, 3,  '#', '', 'F', '0', '0', 'business:testplan:edit',   '#', 'admin', SYSDATE(), ''),
(2104, '方案删除', 2100, 4,  '#', '', 'F', '0', '0', 'business:testplan:remove', '#', 'admin', SYSDATE(), ''),
(2105, '方案导出', 2100, 5,  '#', '', 'F', '0', '0', 'business:testplan:export', '#', 'admin', SYSDATE(), ''),

(2110, '测试报告', 2000, 12, 'testreport', 'business/testreport/index', 'C', '0', '0', 'business:testreport:list',   'tickets', 'admin', SYSDATE(), 'v0.4 scaffold'),
(2111, '报告查询', 2110, 1,  '#', '', 'F', '0', '0', 'business:testreport:query',  '#', 'admin', SYSDATE(), ''),
(2112, '报告新增', 2110, 2,  '#', '', 'F', '0', '0', 'business:testreport:add',    '#', 'admin', SYSDATE(), ''),
(2113, '报告修改', 2110, 3,  '#', '', 'F', '0', '0', 'business:testreport:edit',   '#', 'admin', SYSDATE(), ''),
(2114, '报告删除', 2110, 4,  '#', '', 'F', '0', '0', 'business:testreport:remove', '#', 'admin', SYSDATE(), ''),
(2115, '报告导出', 2110, 5,  '#', '', 'F', '0', '0', 'business:testreport:export', '#', 'admin', SYSDATE(), ''),

(2120, 'API 文档', 2000, 13, 'apidoc', 'business/apidoc/index', 'C', '0', '0', 'business:apidoc:list',   'documentation', 'admin', SYSDATE(), 'v0.4 scaffold'),
(2121, '文档查询', 2120, 1,  '#', '', 'F', '0', '0', 'business:apidoc:query',  '#', 'admin', SYSDATE(), ''),
(2122, '文档新增', 2120, 2,  '#', '', 'F', '0', '0', 'business:apidoc:add',    '#', 'admin', SYSDATE(), ''),
(2123, '文档修改', 2120, 3,  '#', '', 'F', '0', '0', 'business:apidoc:edit',   '#', 'admin', SYSDATE(), ''),
(2124, '文档删除', 2120, 4,  '#', '', 'F', '0', '0', 'business:apidoc:remove', '#', 'admin', SYSDATE(), ''),
(2125, '文档导出', 2120, 5,  '#', '', 'F', '0', '0', 'business:apidoc:export', '#', 'admin', SYSDATE(), ''),

(2130, '产品手册', 2000, 14, 'manual-product', 'business/manual-product/index', 'C', '0', '0', 'business:manual-product:list',   'reading', 'admin', SYSDATE(), 'v0.4 scaffold'),
(2131, '手册查询', 2130, 1,  '#', '', 'F', '0', '0', 'business:manual-product:query',  '#', 'admin', SYSDATE(), ''),
(2132, '手册新增', 2130, 2,  '#', '', 'F', '0', '0', 'business:manual-product:add',    '#', 'admin', SYSDATE(), ''),
(2133, '手册修改', 2130, 3,  '#', '', 'F', '0', '0', 'business:manual-product:edit',   '#', 'admin', SYSDATE(), ''),
(2134, '手册删除', 2130, 4,  '#', '', 'F', '0', '0', 'business:manual-product:remove', '#', 'admin', SYSDATE(), ''),
(2135, '手册导出', 2130, 5,  '#', '', 'F', '0', '0', 'business:manual-product:export', '#', 'admin', SYSDATE(), '');

-- admin 角色授权 (36 项)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2080 AND 2135;

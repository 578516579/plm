-- =============================================================================
-- 回滚: 撤销 menu-fill-missing-8.sql 补的 8 模块 × 6 条 = 48 条菜单
-- =============================================================================

-- 1. 角色授权先删
DELETE FROM sys_role_menu
WHERE menu_id BETWEEN 2200 AND 2265
   OR menu_id BETWEEN 2320 AND 2325;

-- 2. 菜单本身
DELETE FROM sys_menu
WHERE menu_id BETWEEN 2200 AND 2205    -- inception
   OR menu_id BETWEEN 2210 AND 2215    -- prd
   OR menu_id BETWEEN 2220 AND 2225    -- competitive
   OR menu_id BETWEEN 2230 AND 2235    -- arch
   OR menu_id BETWEEN 2240 AND 2245    -- dbdesign
   OR menu_id BETWEEN 2250 AND 2255    -- apidesign
   OR menu_id BETWEEN 2260 AND 2265    -- testdata
   OR menu_id BETWEEN 2320 AND 2325;   -- ai-agent

-- 3. 验证
SELECT '剩余记录应=0' AS check_name,
       COUNT(*) AS cnt
FROM sys_menu
WHERE menu_id BETWEEN 2200 AND 2265
   OR menu_id BETWEEN 2320 AND 2325;

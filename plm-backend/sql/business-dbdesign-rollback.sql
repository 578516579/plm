-- =============================================================================
-- 数据库设计 (DbDesign) — 回滚脚本
-- 对应正向脚本: business-dbdesign.sql + menu-fill-missing-8.sql (行 60-66)
-- 顺序: sys_role_menu → sys_menu → sys_dict_data → sys_dict_type → DROP TABLE
-- =============================================================================
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2240 AND 2245;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2240 AND 2245;
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_dbdesign_engine', 'biz_dbdesign_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_dbdesign_engine', 'biz_dbdesign_status');
DROP TABLE IF EXISTS tb_dbdesign;

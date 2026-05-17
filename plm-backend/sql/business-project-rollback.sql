-- =============================================================================
-- Project 业务模块 — 回滚脚本
-- 撤销 business-project.sql 创建的所有对象。
-- 注意:执行后所有项目数据丢失,仅用于 dev 重置或回退 v2 → v1 schema。
-- =============================================================================
-- 用法:mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-project-rollback.sql
-- =============================================================================

-- 1. 角色-菜单 关联
DELETE FROM sys_role_menu WHERE menu_id IN (2000, 2010, 2011, 2012, 2013, 2014, 2015);

-- 2. 菜单
DELETE FROM sys_menu WHERE menu_id IN (2000, 2010, 2011, 2012, 2013, 2014, 2015);

-- 3. 字典数据(6 类字典的所有值)
DELETE FROM sys_dict_data WHERE dict_type IN (
    'biz_project_type',
    'biz_project_status',
    'biz_project_business_line',
    'biz_project_priority',
    'biz_project_phase',
    'biz_project_health'
);

-- 4. 字典类型
DELETE FROM sys_dict_type WHERE dict_type IN (
    'biz_project_type',
    'biz_project_status',
    'biz_project_business_line',
    'biz_project_priority',
    'biz_project_phase',
    'biz_project_health'
);

-- 5. 业务表
DROP TABLE IF EXISTS tb_project;

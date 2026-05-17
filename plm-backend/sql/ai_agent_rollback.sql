-- ============================================================
-- AI Agent 模块回滚脚本
-- ============================================================

SET NAMES utf8mb4;

-- 菜单
DELETE FROM sys_menu WHERE menu_id IN (2600, 2610, 2611, 2612, 2613, 2614, 2615);

-- 字典数据
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_agent_role', 'biz_agent_type', 'biz_agent_status');

-- 字典类型
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_agent_role', 'biz_agent_type', 'biz_agent_status');

-- 表
DROP TABLE IF EXISTS `tb_ai_agent`;

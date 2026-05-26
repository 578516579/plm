-- =============================================================================
-- 禅道(ZenTao)双向同步 — 回滚脚本
-- 对应：business-integration-zentao.sql
-- =============================================================================

-- 1. 删除菜单 + 权限映射(2530-2535)
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2530 AND 2535;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2530 AND 2535;

-- 2. 删除字典数据
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_integration_user_dir', 'biz_zentao_severity', 'biz_zentao_pri');
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_defect_status','biz_req_status','biz_task_status','biz_testcase_status') AND dict_value = '99';
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_integration_user_dir', 'biz_zentao_severity', 'biz_zentao_pri');

-- 3. 删除用户映射表
DROP TABLE IF EXISTS tb_integration_user_mapping;

-- 4. 删除 4 张业务表的 external_* 列 + 索引
ALTER TABLE tb_defect
    DROP INDEX uk_defect_external,
    DROP COLUMN external_url,
    DROP COLUMN external_id,
    DROP COLUMN external_source;

ALTER TABLE tb_requirement
    DROP INDEX uk_req_external,
    DROP COLUMN external_url,
    DROP COLUMN external_id,
    DROP COLUMN external_source;

ALTER TABLE tb_task
    DROP INDEX uk_task_external,
    DROP COLUMN external_url,
    DROP COLUMN external_id,
    DROP COLUMN external_source;

ALTER TABLE tb_testcase
    DROP INDEX uk_tc_external,
    DROP COLUMN external_url,
    DROP COLUMN external_id,
    DROP COLUMN external_source;

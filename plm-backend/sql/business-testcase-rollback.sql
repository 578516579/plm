DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2060 AND 2067;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2060 AND 2067;
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_testcase_category', 'biz_testcase_priority', 'biz_testcase_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_testcase_category', 'biz_testcase_priority', 'biz_testcase_status');
DROP TABLE IF EXISTS tb_testcase;

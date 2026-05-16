DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2070 AND 2075;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2070 AND 2075;
DELETE FROM sys_dict_data WHERE dict_type IN ('biz_doc_type', 'biz_doc_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('biz_doc_type', 'biz_doc_status');
DROP TABLE IF EXISTS tb_document;

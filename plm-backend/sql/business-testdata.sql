-- =============================================================================
-- 测试数据工厂 (TestData) — PRD §F4.3 + 原型 testdata.html
-- 基于字段语义 + AgriKB 生成农业场景真实感测试数据 (土壤/气象/作物/病虫害/灌溉)
-- =============================================================================
DROP TABLE IF EXISTS tb_testdata;
CREATE TABLE tb_testdata (
    testdata_id            BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    testdata_no            VARCHAR(32)   NOT NULL                 COMMENT '编号 TD-YYYY-NNNN',
    project_id             BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title                  VARCHAR(200)  NOT NULL                 COMMENT '生成任务标题',
    target_table           VARCHAR(50)   NOT NULL                 COMMENT 'biz_testdata_table 目标数据表',
    generate_count         INT           DEFAULT 1000             COMMENT '生成数量',
    output_format          VARCHAR(20)   DEFAULT 'json'           COMMENT 'biz_testdata_format json/sql/csv',
    field_semantics        TEXT                                   COMMENT 'AI 识别的字段语义 JSON',
    rule_china_coord       CHAR(1)       DEFAULT 'Y'              COMMENT '坐标限定中国农田',
    rule_time_continuity   CHAR(1)       DEFAULT 'Y'              COMMENT '时间序列连续性',
    rule_sensor_range      CHAR(1)       DEFAULT 'Y'              COMMENT '传感器正常范围',
    rule_include_outliers  CHAR(1)       DEFAULT 'N'              COMMENT '包含异常值(边界测试)',
    generated_content      LONGTEXT                               COMMENT '生成的数据集',
    generated_at           DATETIME      DEFAULT NULL             COMMENT '生成时间',
    ai_generated           CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    status                 VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_testdata_status 3 状态',
    author_user_id         BIGINT(20)    NOT NULL                 COMMENT '创建人',
    create_by              VARCHAR(64)   DEFAULT '',
    create_time            DATETIME      DEFAULT NULL,
    update_by              VARCHAR(64)   DEFAULT '',
    update_time            DATETIME      DEFAULT NULL,
    remark                 VARCHAR(500)  DEFAULT '',
    del_flag               CHAR(1)       DEFAULT '0',
    PRIMARY KEY (testdata_id),
    UNIQUE KEY uk_testdata_no (testdata_no),
    KEY idx_testdata_project (project_id),
    KEY idx_testdata_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试数据工厂';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('测试数据表',   'biz_testdata_table',  '0', 'admin', SYSDATE(), '5 农业垂直表'),
('测试数据格式', 'biz_testdata_format', '0', 'admin', SYSDATE(), '3 格式'),
('测试数据状态', 'biz_testdata_status', '0', 'admin', SYSDATE(), '3 态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '土壤传感器数据', 'soil_sensor', 'biz_testdata_table', '', 'primary', 'Y', '0', 'admin', SYSDATE(), 't_soil_sensor_data'),
(2, '气象记录',       'weather',     'biz_testdata_table', '', 'success', 'N', '0', 'admin', SYSDATE(), 't_weather_record'),
(3, '作物信息',       'crop',        'biz_testdata_table', '', 'warning', 'N', '0', 'admin', SYSDATE(), 't_crop_info'),
(4, '病虫害记录',     'pest',        'biz_testdata_table', '', 'info',    'N', '0', 'admin', SYSDATE(), 't_pest_record'),
(5, '灌溉计划',       'irrigation',  'biz_testdata_table', '', 'danger',  'N', '0', 'admin', SYSDATE(), 't_irrigation_plan');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'JSON',       'json', 'biz_testdata_format', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'SQL INSERT', 'sql',  'biz_testdata_format', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'CSV',        'csv',  'biz_testdata_format', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_testdata_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已生成', '01', 'biz_testdata_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已归档', '02', 'biz_testdata_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

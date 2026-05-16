-- ============================================================
-- 测试数据工厂 (tb_testdata) — PRD §F4.3
-- ============================================================
CREATE TABLE IF NOT EXISTS `tb_testdata` (
  `testdata_id`        BIGINT(20)      NOT NULL AUTO_INCREMENT         COMMENT '主键',
  `testdata_no`        VARCHAR(32)     NOT NULL                        COMMENT '编号 TD-YYYY-NNNN',
  `project_id`         BIGINT(20)      NOT NULL                        COMMENT '关联项目 ID',
  `title`              VARCHAR(200)    NOT NULL                        COMMENT '数据集名称',
  `target_table`       VARCHAR(100)    DEFAULT NULL                    COMMENT '目标数据表 (biz_testdata_table)',
  `generate_count`     INT             NOT NULL DEFAULT 1000           COMMENT '生成数量',
  `output_format`      VARCHAR(20)     DEFAULT 'json'                  COMMENT '输出格式 (biz_testdata_format)',
  `rule_coordinate`    CHAR(1)         NOT NULL DEFAULT 'Y'            COMMENT '坐标限定中国农田 Y/N',
  `rule_time_series`   CHAR(1)         NOT NULL DEFAULT 'Y'            COMMENT '时间序列业务连续性 Y/N',
  `rule_sensor_range`  CHAR(1)         NOT NULL DEFAULT 'Y'            COMMENT '传感器正常范围约束 Y/N',
  `rule_include_abnormal` CHAR(1)      NOT NULL DEFAULT 'N'            COMMENT '包含异常值(边界测试) Y/N',
  `generated_data`     LONGTEXT        DEFAULT NULL                    COMMENT '生成的数据内容',
  `write_target`       VARCHAR(20)     DEFAULT NULL                    COMMENT '写入目标环境 (biz_testdata_target)',
  `write_mode`         VARCHAR(20)     DEFAULT NULL                    COMMENT '写入模式 (biz_testdata_write_mode)',
  `ai_generated`       CHAR(1)         NOT NULL DEFAULT 'N'            COMMENT 'AI 生成标志 Y/N',
  `ai_generated_at`    DATETIME        DEFAULT NULL                    COMMENT 'AI 生成时间',
  `status`             VARCHAR(2)      NOT NULL DEFAULT '00'           COMMENT '状态 (biz_testdata_status)',
  `author_user_id`     BIGINT(20)      NOT NULL                        COMMENT '创建者用户 ID',
  -- BaseEntity
  `create_by`          VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '创建者',
  `create_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '更新者',
  `update_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`             VARCHAR(500)    DEFAULT NULL                    COMMENT '备注',
  `del_flag`           CHAR(1)         NOT NULL DEFAULT '0'            COMMENT '删除标志 0正常 2删除',
  PRIMARY KEY (`testdata_id`),
  UNIQUE KEY `uk_testdata_no` (`testdata_no`),
  KEY `idx_testdata_project` (`project_id`),
  KEY `idx_testdata_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试数据工厂表 PRD §F4.3';

-- 字典: 目标数据表
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('测试数据目标表', 'biz_testdata_table', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.3 目标数据表')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '土壤传感器数据',  't_soil_sensor_data',  'biz_testdata_table', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '气象记录',        't_weather_record',     'biz_testdata_table', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '作物信息',        't_crop_info',          'biz_testdata_table', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, '病虫害记录',      't_pest_record',        'biz_testdata_table', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (5, '灌溉计划',        't_irrigation_plan',    'biz_testdata_table', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- 字典: 输出格式
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('测试数据输出格式', 'biz_testdata_format', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.3 输出格式')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'JSON',       'json',       'biz_testdata_format', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'SQL INSERT', 'sql_insert', 'biz_testdata_format', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'CSV',        'csv',        'biz_testdata_format', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- 字典: 写入目标环境
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('测试数据写入目标', 'biz_testdata_target', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.3 写入目标')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '测试环境 TEST', 'test', 'biz_testdata_target', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '开发环境 DEV',  'dev',  'biz_testdata_target', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- 字典: 写入模式
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('测试数据写入模式', 'biz_testdata_write_mode', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.3 写入模式')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '追加写入', 'append',   'biz_testdata_write_mode', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '清空写入', 'truncate', 'biz_testdata_write_mode', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'UPSERT',   'upsert',   'biz_testdata_write_mode', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- 字典: 状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('测试数据状态', 'biz_testdata_status', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.3 状态')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '草稿',   '00', 'biz_testdata_status', '', 'info',    'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '生成中', '01', 'biz_testdata_status', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '已完成', '02', 'biz_testdata_status', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, '已废弃', '03', 'biz_testdata_status', '', 'danger',  'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

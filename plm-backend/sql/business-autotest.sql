-- ============================================================
-- 自动化测试 (tb_autotest) — PRD §F4.5
-- ============================================================
CREATE TABLE IF NOT EXISTS `tb_autotest` (
  `autotest_id`       BIGINT(20)      NOT NULL AUTO_INCREMENT         COMMENT '主键',
  `autotest_no`       VARCHAR(32)     NOT NULL                        COMMENT '编号 AT-YYYY-NNNN',
  `project_id`        BIGINT(20)      NOT NULL                        COMMENT '关联项目 ID',
  `title`             VARCHAR(200)    NOT NULL                        COMMENT '测试套件名称',
  `suite_type`        VARCHAR(20)     DEFAULT NULL                    COMMENT '套件类型 (biz_autotest_type)',
  `framework`         VARCHAR(20)     DEFAULT NULL                    COMMENT '测试框架 (biz_autotest_framework)',
  `script_content`    LONGTEXT        DEFAULT NULL                    COMMENT '测试脚本内容',
  `last_run_at`       DATETIME        DEFAULT NULL                    COMMENT '最近执行时间',
  `last_run_result`   VARCHAR(10)     DEFAULT NULL                    COMMENT '最近执行结果 (biz_autotest_result)',
  `pass_rate`         DECIMAL(5,2)    DEFAULT NULL                    COMMENT '通过率 0-100',
  `total_cases`       INT             DEFAULT 0                       COMMENT '总用例数',
  `failed_cases`      INT             DEFAULT 0                       COMMENT '失败用例数',
  `execution_time`    INT             DEFAULT 0                       COMMENT '最近执行耗时(ms)',
  `schedule_cron`     VARCHAR(100)    DEFAULT NULL                    COMMENT '定时执行 Cron 表达式',
  `ai_generated`      CHAR(1)         NOT NULL DEFAULT 'N'            COMMENT 'AI 生成脚本标志 Y/N',
  `ai_generated_at`   DATETIME        DEFAULT NULL                    COMMENT 'AI 生成时间',
  `status`            VARCHAR(2)      NOT NULL DEFAULT '00'           COMMENT '状态 (biz_autotest_status)',
  `author_user_id`    BIGINT(20)      NOT NULL                        COMMENT '创建者用户 ID',
  -- BaseEntity
  `create_by`         VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '创建者',
  `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`         VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '更新者',
  `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`            VARCHAR(500)    DEFAULT NULL                    COMMENT '备注',
  `del_flag`          CHAR(1)         NOT NULL DEFAULT '0'            COMMENT '删除标志 0正常 2删除',
  PRIMARY KEY (`autotest_id`),
  UNIQUE KEY `uk_autotest_no` (`autotest_no`),
  KEY `idx_autotest_project` (`project_id`),
  KEY `idx_autotest_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自动化测试套件表 PRD §F4.5';

-- 字典: 套件类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('自动化测试套件类型', 'biz_autotest_type', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.5 套件类型')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'API 接口测试',  'api',         'biz_autotest_type', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'E2E 端到端',    'e2e',         'biz_autotest_type', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '单元测试',      'unit',        'biz_autotest_type', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, '性能测试',      'performance', 'biz_autotest_type', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- 字典: 测试框架
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('自动化测试框架', 'biz_autotest_framework', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.5 测试框架')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'Playwright',  'playwright', 'biz_autotest_framework', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'pytest',      'pytest',     'biz_autotest_framework', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'Jest',        'jest',       'biz_autotest_framework', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, 'JMeter',      'jmeter',     'biz_autotest_framework', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- 字典: 执行结果
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('自动化测试执行结果', 'biz_autotest_result', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.5 执行结果')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '通过', 'passed', 'biz_autotest_result', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '失败', 'failed', 'biz_autotest_result', '', 'danger',  'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '错误', 'error',  'biz_autotest_result', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- 字典: 状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('自动化测试状态', 'biz_autotest_status', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F4.5 状态')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '草稿',   '00', 'biz_autotest_status', '', 'info',    'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '已激活', '01', 'biz_autotest_status', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '已归档', '02', 'biz_autotest_status', '', 'danger',  'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

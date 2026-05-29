-- =============================================================================
-- 自动化测试 (AutoTest) — PRD §F4.5 + 原型 autotest.html
-- AI 生成测试脚本 + 定时执行 + 智能根因分析
-- =============================================================================
DROP TABLE IF EXISTS tb_autotest;
CREATE TABLE tb_autotest (
    autotest_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    autotest_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 AT-YYYY-NNNN',
    project_id               BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title                    VARCHAR(200)  NOT NULL                 COMMENT '测试套件名称',
    test_suite_type          VARCHAR(20)   NOT NULL                 COMMENT 'biz_autotest_suite_type: ui/api/perf/regression',
    framework                VARCHAR(20)   NOT NULL                 COMMENT 'biz_autotest_framework: playwright/selenium/jmeter/cypress',
    target_url               VARCHAR(500)  DEFAULT NULL             COMMENT '测试目标 URL',
    script_content           LONGTEXT                                COMMENT '脚本内容 (AI 生成或手写)',
    schedule_enabled         CHAR(1)       DEFAULT 'N'              COMMENT 'Y/N 定时执行开关',
    schedule_cron            VARCHAR(50)   DEFAULT NULL             COMMENT '定时 cron 表达式',
    total_cases              INT           DEFAULT 0                COMMENT '用例总数',
    passed_cases             INT           DEFAULT 0                COMMENT '通过用例数',
    failed_cases             INT           DEFAULT 0                COMMENT '失败用例数',
    pass_rate                DECIMAL(5,2)  DEFAULT 0                COMMENT '通过率 0-100',
    execution_duration_sec   INT           DEFAULT 0                COMMENT '最近执行时长(秒)',
    last_executed_at         DATETIME      DEFAULT NULL             COMMENT '最近执行时间',
    last_root_cause_analysis LONGTEXT                                COMMENT 'AI 智能根因分析 (失败用例)',
    ai_generated             CHAR(1)       DEFAULT 'N'              COMMENT 'Y/N AI 生成的脚本',
    ai_generated_at          DATETIME      DEFAULT NULL,
    status                   VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_autotest_status',
    author_user_id           BIGINT(20)    NOT NULL                 COMMENT '创建人',
    create_by                VARCHAR(64)   DEFAULT '',
    create_time              DATETIME      DEFAULT NULL,
    update_by                VARCHAR(64)   DEFAULT '',
    update_time              DATETIME      DEFAULT NULL,
    remark                   VARCHAR(500)  DEFAULT '',
    del_flag                 CHAR(1)       DEFAULT '0',
    PRIMARY KEY (autotest_id),
    UNIQUE KEY uk_autotest_no (autotest_no),
    KEY idx_autotest_project (project_id),
    KEY idx_autotest_type (test_suite_type),
    KEY idx_autotest_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自动化测试套件 (AutoTest)';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('自动化套件类型', 'biz_autotest_suite_type', '0', 'admin', SYSDATE(), 'PRD §F4.5'),
('自动化测试框架', 'biz_autotest_framework', '0', 'admin', SYSDATE(), '原型 autotest.html'),
('自动化套件状态', 'biz_autotest_status', '0', 'admin', SYSDATE(), '3 状态机');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'UI 测试',          'ui',          'biz_autotest_suite_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'API 测试',         'api',         'biz_autotest_suite_type', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '性能测试',         'perf',        'biz_autotest_suite_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '回归测试',         'regression',  'biz_autotest_suite_type', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),

(1, 'Playwright', 'playwright', 'biz_autotest_framework', '', 'primary', 'Y', '0', 'admin', SYSDATE(), 'UI 推荐'),
(2, 'Selenium',   'selenium',   'biz_autotest_framework', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(3, 'JMeter',     'jmeter',     'biz_autotest_framework', '', 'warning', 'N', '0', 'admin', SYSDATE(), '性能推荐'),
(4, 'Cypress',    'cypress',    'biz_autotest_framework', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),

(1, '草稿',     '00', 'biz_autotest_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已激活',   '01', 'biz_autotest_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '可定时执行'),
(3, '已禁用',   '02', 'biz_autotest_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '');

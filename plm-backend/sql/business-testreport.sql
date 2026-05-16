-- =============================================================================
-- 测试报告 (TestReport) — PRD §F4.7 + 原型 testreport.html
-- AI 自动生成测试报告 + 上线风险评级（绿/黄/红）
-- =============================================================================
DROP TABLE IF EXISTS tb_testreport;
CREATE TABLE tb_testreport (
    testreport_id        BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    testreport_no        VARCHAR(32)   NOT NULL                 COMMENT '编号 TR-YYYY-NNNN',
    project_id           BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    sprint_id            BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_sprint',
    testplan_id          BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_testplan',
    title                VARCHAR(200)  NOT NULL                 COMMENT '报告标题',
    total_cases          INT           DEFAULT 0                COMMENT '总用例数',
    passed_cases         INT           DEFAULT 0                COMMENT '通过用例数',
    failed_cases         INT           DEFAULT 0                COMMENT '失败用例数',
    coverage_rate        DECIMAL(5,2)  DEFAULT NULL             COMMENT '覆盖率 %',
    defect_summary       TEXT                                   COMMENT '缺陷分布 JSON',
    p0_defects           INT           DEFAULT 0                COMMENT 'P0 缺陷数',
    p1_defects           INT           DEFAULT 0                COMMENT 'P1 缺陷数',
    p2_defects           INT           DEFAULT 0                COMMENT 'P2 缺陷数',
    risk_level           VARCHAR(10)   NOT NULL DEFAULT 'green' COMMENT 'biz_testreport_risk green/yellow/red',
    risk_evaluation      TEXT                                   COMMENT '风险评估说明',
    recommendations      TEXT                                   COMMENT '改进建议',
    ai_generated         CHAR(1)       DEFAULT 'N'              COMMENT 'Y/N',
    status               VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_testreport_status',
    generated_at         DATETIME      DEFAULT NULL             COMMENT '生成时间',
    reviewer_user_id     BIGINT(20)    DEFAULT NULL             COMMENT '审核人',
    create_by            VARCHAR(64)   DEFAULT '',
    create_time          DATETIME      DEFAULT NULL,
    update_by            VARCHAR(64)   DEFAULT '',
    update_time          DATETIME      DEFAULT NULL,
    remark               VARCHAR(500)  DEFAULT '',
    del_flag             CHAR(1)       DEFAULT '0',
    PRIMARY KEY (testreport_id),
    UNIQUE KEY uk_testreport_no (testreport_no),
    KEY idx_testreport_project (project_id),
    KEY idx_testreport_sprint (sprint_id),
    KEY idx_testreport_risk (risk_level),
    KEY idx_testreport_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试报告（TestReport）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('测试报告风险', 'biz_testreport_risk',   '0', 'admin', SYSDATE(), '绿/黄/红 三级'),
('测试报告状态', 'biz_testreport_status', '0', 'admin', SYSDATE(), '3 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '绿 (低风险)', 'green',  'biz_testreport_risk', '', 'success', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '黄 (中风险)', 'yellow', 'biz_testreport_risk', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '红 (高风险)', 'red',    'biz_testreport_risk', '', 'danger',  'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_testreport_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '审核中', '01', 'biz_testreport_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已发布', '02', 'biz_testreport_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态');

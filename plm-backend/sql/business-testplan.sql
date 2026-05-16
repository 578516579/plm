-- =============================================================================
-- 测试方案 (TestPlan) — PRD §F4.1 + 原型 testplan.html
-- AI 生成测试策略、范围、资源分配计划
-- =============================================================================
DROP TABLE IF EXISTS tb_testplan;
CREATE TABLE tb_testplan (
    testplan_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    testplan_no          VARCHAR(32)   NOT NULL                 COMMENT '编号 TP-YYYY-NNNN',
    project_id           BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    sprint_id            BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_sprint',
    title                VARCHAR(200)  NOT NULL                 COMMENT '方案标题',
    test_types           VARCHAR(200)  NOT NULL                 COMMENT 'CSV: functional,api,performance,automation,security',
    test_cycle_days      INT           DEFAULT 10               COMMENT '测试周期(天)',
    scope                TEXT                                   COMMENT '测试范围（需求/接口 CSV）',
    strategy             TEXT                                   COMMENT '测试策略',
    tools_recommended    VARCHAR(500)  DEFAULT NULL             COMMENT '推荐工具 CSV (selenium/jmeter...)',
    resources_plan       TEXT                                   COMMENT '资源分配（人员/时间/环境）',
    risk_assessment      TEXT                                   COMMENT '风险评估',
    ai_generated         CHAR(1)       DEFAULT 'N'              COMMENT 'Y/N AI 生成',
    status               VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_testplan_status',
    author_user_id       BIGINT(20)    NOT NULL                 COMMENT '撰写人',
    create_by            VARCHAR(64)   DEFAULT '',
    create_time          DATETIME      DEFAULT NULL,
    update_by            VARCHAR(64)   DEFAULT '',
    update_time          DATETIME      DEFAULT NULL,
    remark               VARCHAR(500)  DEFAULT '',
    del_flag             CHAR(1)       DEFAULT '0',
    PRIMARY KEY (testplan_id),
    UNIQUE KEY uk_testplan_no (testplan_no),
    KEY idx_testplan_project (project_id),
    KEY idx_testplan_sprint (sprint_id),
    KEY idx_testplan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试方案（TestPlan）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('测试方案状态', 'biz_testplan_status', '0', 'admin', SYSDATE(), '4 状态机');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',     '00', 'biz_testplan_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已确认',   '01', 'biz_testplan_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '执行中',   '02', 'biz_testplan_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已完成',   '03', 'biz_testplan_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态');

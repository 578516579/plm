-- =============================================================================
-- 提测管理 (Submission) — PRD §F4.4 + 原型 submit.html
-- AI 质量门禁: 单测覆盖率 ≥60% / 代码扫描 0 高危 / PRD 文档完整 / 接口文档已更新
-- =============================================================================
DROP TABLE IF EXISTS tb_submission;
CREATE TABLE tb_submission (
    submission_id         BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    submission_no         VARCHAR(32)   NOT NULL                 COMMENT '提测编号 SUB-YYYY-NNNN',
    project_id            BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    sprint_id             BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_sprint',
    title                 VARCHAR(200)  NOT NULL                 COMMENT '提测标题',
    scope                 TEXT                                   COMMENT '提测范围（涉及需求/任务CSV）',
    environment           VARCHAR(20)   NOT NULL DEFAULT 'test'  COMMENT '测试环境(biz_submission_environment:test/pre/dev/staging/prod)',
    expected_test_days    INT           DEFAULT 5                COMMENT '期望测试周期(天)',
    risk_notes            TEXT                                   COMMENT '风险提示',
    unit_test_coverage    DECIMAL(5,2)  DEFAULT NULL             COMMENT '单测覆盖率 %',
    code_scan_passed      CHAR(1)       DEFAULT 'N'              COMMENT '代码扫描 Y=0 高危',
    prd_completed         CHAR(1)       DEFAULT 'N'              COMMENT 'PRD 文档完整 Y/N',
    api_doc_updated       CHAR(1)       DEFAULT 'N'              COMMENT '接口文档已更新 Y/N',
    quality_gate_passed   CHAR(1)       DEFAULT 'N'              COMMENT 'AI 质量门禁综合判定',
    status                VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_submission_status',
    reject_reason         VARCHAR(500)  DEFAULT NULL             COMMENT '退回原因 (status=04 必填)',
    submitter_user_id     BIGINT(20)    NOT NULL                 COMMENT '提测人',
    reviewer_user_id      BIGINT(20)    DEFAULT NULL             COMMENT '测试经理审批人',
    submitted_at          DATETIME      DEFAULT NULL             COMMENT '提交时间',
    approved_at           DATETIME      DEFAULT NULL             COMMENT '通过时间',
    create_by             VARCHAR(64)   DEFAULT '',
    create_time           DATETIME      DEFAULT NULL,
    update_by             VARCHAR(64)   DEFAULT '',
    update_time           DATETIME      DEFAULT NULL,
    remark                VARCHAR(500)  DEFAULT '',
    del_flag              CHAR(1)       DEFAULT '0',
    PRIMARY KEY (submission_id),
    UNIQUE KEY uk_submission_no (submission_no),
    KEY idx_submission_project (project_id),
    KEY idx_submission_sprint (sprint_id),
    KEY idx_submission_status (status),
    KEY idx_submission_submitter (submitter_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提测单（Submission）';

-- biz_submission_status 5x5 状态机 + biz_submission_environment 5 值字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('提测状态',   'biz_submission_status',      '0', 'admin', SYSDATE(), 'PRD F4.4 5 状态'),
('提测环境', 'biz_submission_environment', '0', 'admin', SYSDATE(), '兼容原型 modal-newsubmit (test/pre) + SQL 历史 (dev/staging/prod) — 5 值');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',       '00', 'biz_submission_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已提交',     '01', 'biz_submission_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(3, '质量门禁中', '02', 'biz_submission_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'AI 检测中'),
(4, '已通过',     '03', 'biz_submission_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),
(5, '已退回',     '04', 'biz_submission_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '可回 00 重新填');

-- 提测环境 5 值(D1):对齐原型 modal-newsubmit "测试环境 TEST" / "预发环境 PRE" + 兼容历史值 dev/staging/prod
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '测试环境 TEST',    'test',    'biz_submission_environment', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '原型 ns-env 第 1 选项 / 默认值'),
(2, '预发环境 PRE',     'pre',     'biz_submission_environment', '', 'warning', 'N', '0', 'admin', SYSDATE(), '原型 ns-env 第 2 选项'),
(3, '开发环境 DEV',     'dev',     'biz_submission_environment', '', 'info',    'N', '0', 'admin', SYSDATE(), '兼容旧数据'),
(4, 'Staging',          'staging', 'biz_submission_environment', '', 'info',    'N', '0', 'admin', SYSDATE(), '兼容 E2E + featureflag 命名'),
(5, '生产环境 PROD',    'prod',    'biz_submission_environment', '', 'danger',  'N', '0', 'admin', SYSDATE(), '极少用,生产环境提测特殊场景');

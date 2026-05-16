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
    environment           VARCHAR(20)   NOT NULL DEFAULT 'dev'   COMMENT '测试环境 dev/staging/prod',
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

-- biz_submission_status 5x5 状态机
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('提测状态', 'biz_submission_status', '0', 'admin', SYSDATE(), 'PRD F4.4 5 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',       '00', 'biz_submission_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已提交',     '01', 'biz_submission_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(3, '质量门禁中', '02', 'biz_submission_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'AI 检测中'),
(4, '已通过',     '03', 'biz_submission_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),
(5, '已退回',     '04', 'biz_submission_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '可回 00 重新填');

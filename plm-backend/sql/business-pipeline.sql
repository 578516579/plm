-- =============================================================================
-- CI/CD 流水线 (Pipeline) — DevOps 扩展 + 原型 pipeline.html
-- 代码仓库 / 触发方式 / YAML 流水线定义 / 执行统计
-- =============================================================================
DROP TABLE IF EXISTS tb_pipeline;
CREATE TABLE tb_pipeline (
    pipeline_id       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    pipeline_no       VARCHAR(32)   NOT NULL                 COMMENT '编号 PIPE-YYYY-NNNN',
    project_id        BIGINT(20)                             COMMENT 'FK→tb_project (可空)',
    pipeline_name     VARCHAR(200)  NOT NULL                 COMMENT '流水线名称',
    repo_name         VARCHAR(200)  NOT NULL                 COMMENT '代码仓库 (org/repo)',
    repo_branch       VARCHAR(100)  DEFAULT 'main'           COMMENT '分支',
    cicd_tool         VARCHAR(30)                            COMMENT '字典 biz_pipeline_tool: jenkins/gitlab/github/gitea',
    trigger_type      VARCHAR(20)                            COMMENT '字典 biz_pipeline_trigger: manual/push/cron/tag',
    cron_expr         VARCHAR(50)                            COMMENT 'Cron 表达式 (trigger=cron 时)',
    yaml_content      LONGTEXT                               COMMENT '流水线 YAML 定义',
    last_run_status   VARCHAR(20)                            COMMENT '字典 biz_pipeline_result: success/failed/running/skipped',
    last_run_at       DATETIME      DEFAULT NULL             COMMENT '最近执行时间',
    total_runs        INT           DEFAULT 0                COMMENT '总执行次数',
    success_count     INT           DEFAULT 0                COMMENT '成功次数',
    success_rate      DECIMAL(5,2)  DEFAULT 0                COMMENT '成功率 %',
    status            VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_pipeline_status',
    author_user_id    BIGINT(20)    NOT NULL                 COMMENT '创建者',
    create_by         VARCHAR(64)   DEFAULT '',
    create_time       DATETIME      DEFAULT NULL,
    update_by         VARCHAR(64)   DEFAULT '',
    update_time       DATETIME      DEFAULT NULL,
    remark            VARCHAR(500)  DEFAULT '',
    del_flag          CHAR(1)       DEFAULT '0',
    PRIMARY KEY (pipeline_id),
    UNIQUE KEY uk_pipeline_no (pipeline_no),
    KEY idx_pipeline_repo (repo_name),
    KEY idx_pipeline_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CI/CD 流水线（Pipeline）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('流水线状态',   'biz_pipeline_status',  '0', 'admin', SYSDATE(), '2 状态'),
('流水线工具',   'biz_pipeline_tool',    '0', 'admin', SYSDATE(), 'jenkins/gitlab/github/gitea'),
('流水线触发',   'biz_pipeline_trigger', '0', 'admin', SYSDATE(), 'manual/push/cron/tag'),
('流水线结果',   'biz_pipeline_result',  '0', 'admin', SYSDATE(), 'success/failed/running/skipped');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '启用', '00', 'biz_pipeline_status', '', 'success', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '停用', '01', 'biz_pipeline_status', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),

(1, 'Jenkins',       'jenkins', 'biz_pipeline_tool', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'GitLab CI',     'gitlab',  'biz_pipeline_tool', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'GitHub Actions','github',  'biz_pipeline_tool', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(4, 'Gitea Actions', 'gitea',   'biz_pipeline_tool', '', 'success', 'N', '0', 'admin', SYSDATE(), '国产化'),

(1, '手动触发', 'manual', 'biz_pipeline_trigger', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, 'Push 触发', 'push',  'biz_pipeline_trigger', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(3, '定时触发', 'cron',   'biz_pipeline_trigger', '', 'warning', 'N', '0', 'admin', SYSDATE(), '需 cronExpr'),
(4, 'Tag 触发', 'tag',    'biz_pipeline_trigger', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),

(1, '成功',  'success', 'biz_pipeline_result', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(2, '失败',  'failed',  'biz_pipeline_result', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(3, '运行中','running', 'biz_pipeline_result', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已跳过','skipped', 'biz_pipeline_result', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

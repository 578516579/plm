-- =============================================================================
-- 发布管理 (Release) — 原型 release.html 蓝绿/金丝雀/滚动发布
-- DORA 4 指标 + AI 评审 + 一键回滚
-- =============================================================================
DROP TABLE IF EXISTS tb_release;
CREATE TABLE tb_release (
    release_id           BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    release_no           VARCHAR(32)   NOT NULL                 COMMENT '发布编号 REL-YYYY-NNNN',
    version              VARCHAR(50)   NOT NULL                 COMMENT '发布版本号 如 v1.2.3',
    project_id           BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    sprint_id            BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_sprint',
    strategy             VARCHAR(20)   NOT NULL DEFAULT 'rolling' COMMENT 'biz_release_strategy 蓝绿/金丝雀/滚动',
    environment          VARCHAR(20)   NOT NULL DEFAULT 'prod'  COMMENT '发布环境',
    release_notes        TEXT                                   COMMENT '发布说明 (Markdown)',
    planned_at           DATETIME      DEFAULT NULL             COMMENT '计划发布时间',
    released_at          DATETIME      DEFAULT NULL             COMMENT '实际发布时间',
    rollback_at          DATETIME      DEFAULT NULL             COMMENT '回滚时间',
    rollback_reason      VARCHAR(500)  DEFAULT NULL             COMMENT '回滚原因',
    status               VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_release_status',
    ai_review_score      DECIMAL(3,1)  DEFAULT NULL             COMMENT 'AI 发布评审分 0-10',
    ai_review_notes      TEXT                                   COMMENT 'AI 发布评审意见',
    deployment_frequency DECIMAL(5,2)  DEFAULT NULL             COMMENT 'DORA: 部署频率',
    lead_time_hours      DECIMAL(8,2)  DEFAULT NULL             COMMENT 'DORA: 变更前置时间(小时)',
    mttr_minutes         DECIMAL(8,2)  DEFAULT NULL             COMMENT 'DORA: 平均恢复时间',
    change_failure_rate  DECIMAL(5,2)  DEFAULT NULL             COMMENT 'DORA: 变更失败率 %',
    released_by_user_id  BIGINT(20)    NOT NULL                 COMMENT '发布人',
    create_by            VARCHAR(64)   DEFAULT '',
    create_time          DATETIME      DEFAULT NULL,
    update_by            VARCHAR(64)   DEFAULT '',
    update_time          DATETIME      DEFAULT NULL,
    remark               VARCHAR(500)  DEFAULT '',
    del_flag             CHAR(1)       DEFAULT '0',
    PRIMARY KEY (release_id),
    UNIQUE KEY uk_release_no (release_no),
    UNIQUE KEY uk_release_project_version (project_id, version),
    KEY idx_release_status (status),
    KEY idx_release_planned (planned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布单（Release）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('发布策略', 'biz_release_strategy', '0', 'admin', SYSDATE(), '蓝绿/金丝雀/滚动'),
('发布状态', 'biz_release_status',   '0', 'admin', SYSDATE(), '5 状态机');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '蓝绿',   'blue_green', 'biz_release_strategy', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(2, '金丝雀', 'canary',     'biz_release_strategy', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '滚动',   'rolling',    'biz_release_strategy', '', 'success', 'Y', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '计划中',   '00', 'biz_release_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '发布中',   '01', 'biz_release_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已发布',   '02', 'biz_release_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已回滚',   '03', 'biz_release_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '反向边目标'),
(5, '已废弃',   '04', 'biz_release_status', '', '',        'N', '0', 'admin', SYSDATE(), '终态');

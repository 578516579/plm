-- =============================================================================
-- CI/CD 流水线 (Pipeline) — 原型 pipeline.html
-- 跟踪 GitLab CI/CD 流水线执行状态和成功率
-- 菜单 ID 段: 2760-2766
-- =============================================================================
DROP TABLE IF EXISTS tb_pipeline;
CREATE TABLE tb_pipeline (
    pipeline_id         BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    pipeline_no         VARCHAR(32)   NOT NULL                 COMMENT '编号 PIP-YYYY-NNNN',
    project_id          BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    pipeline_name       VARCHAR(200)  NOT NULL                 COMMENT '流水线名称',
    repository          VARCHAR(40)   DEFAULT 'backend'        COMMENT 'biz_pipeline_repo',
    branch              VARCHAR(100)  DEFAULT 'main'           COMMENT '触发分支',
    trigger_type        VARCHAR(20)   DEFAULT 'push'           COMMENT 'biz_pipeline_trigger',
    stages              VARCHAR(500)  DEFAULT '["build","test","scan","deploy"]' COMMENT 'JSON阶段列表',
    last_run_status     VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_pipeline_run_status',
    last_run_at         DATETIME      DEFAULT NULL,
    last_run_duration   VARCHAR(20)   DEFAULT NULL,
    success_count       INT           DEFAULT 0,
    failed_count        INT           DEFAULT 0,
    success_rate        DECIMAL(5,2)  DEFAULT NULL             COMMENT '成功率%',
    status              VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_pipeline_status',
    author_user_id      BIGINT(20)    DEFAULT NULL,
    create_by           VARCHAR(64)   DEFAULT '',
    create_time         DATETIME      DEFAULT NULL,
    update_by           VARCHAR(64)   DEFAULT '',
    update_time         DATETIME      DEFAULT NULL,
    remark              VARCHAR(500)  DEFAULT '',
    del_flag            CHAR(1)       DEFAULT '0',
    PRIMARY KEY (pipeline_id),
    UNIQUE KEY uk_pipeline_no (pipeline_no),
    KEY idx_pipeline_project (project_id),
    KEY idx_pipeline_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CI/CD 流水线';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('流水线仓库',     'biz_pipeline_repo',       '0', 'admin', SYSDATE(), '4 仓库'),
('流水线触发方式', 'biz_pipeline_trigger',    '0', 'admin', SYSDATE(), '4 方式'),
('流水线执行状态', 'biz_pipeline_run_status', '0', 'admin', SYSDATE(), '4 状态'),
('流水线状态',     'biz_pipeline_status',     '0', 'admin', SYSDATE(), '4 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '后端',      'backend',     'biz_pipeline_repo', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, '前端',      'frontend',    'biz_pipeline_repo', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, 'AI服务',    'ai_service',  'biz_pipeline_repo', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, '基础设施',  'infra',       'biz_pipeline_repo', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'Push触发',  'push',            'biz_pipeline_trigger', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, '定时触发',  'schedule',        'biz_pipeline_trigger', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, '手动触发',  'manual',          'biz_pipeline_trigger', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, 'MR触发',    'mr',              'biz_pipeline_trigger', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '成功',   'success',   'biz_pipeline_run_status', '', 'success', 'N', '0', 'admin', SYSDATE()),
(2, '失败',   'failed',    'biz_pipeline_run_status', '', 'danger',  'N', '0', 'admin', SYSDATE()),
(3, '运行中', 'running',   'biz_pipeline_run_status', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, '已取消', 'cancelled', 'biz_pipeline_run_status', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '空闲',   '00', 'biz_pipeline_status', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '运行中', '01', 'biz_pipeline_status', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(3, '已暂停', '02', 'biz_pipeline_status', '', 'primary', 'N', '0', 'admin', SYSDATE()),
(4, '已停用', '03', 'biz_pipeline_status', '', 'danger',  'N', '0', 'admin', SYSDATE());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2760, '流水线管理', 2000, 29, 'pipeline', 'business/pipeline/index', 'C', '0', '0', 'business:pipeline:list',   'connection', 'admin', SYSDATE(), ''),
(2761, '流水线查询', 2760, 1,  '#', '', 'F', '0', '0', 'business:pipeline:query',  '#', 'admin', SYSDATE(), ''),
(2762, '流水线新增', 2760, 2,  '#', '', 'F', '0', '0', 'business:pipeline:add',    '#', 'admin', SYSDATE(), ''),
(2763, '流水线修改', 2760, 3,  '#', '', 'F', '0', '0', 'business:pipeline:edit',   '#', 'admin', SYSDATE(), ''),
(2764, '流水线删除', 2760, 4,  '#', '', 'F', '0', '0', 'business:pipeline:remove', '#', 'admin', SYSDATE(), ''),
(2765, '流水线导出', 2760, 5,  '#', '', 'F', '0', '0', 'business:pipeline:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2760 AND 2765;

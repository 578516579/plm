-- =============================================================================
-- 工作台 (Dashboard) — UI §4.2 + 原型 dashboard.html
-- 用户自定义工作台预设 (widget 布局) + 聚合查询
-- =============================================================================
DROP TABLE IF EXISTS tb_dashboard;
CREATE TABLE tb_dashboard (
    dashboard_id      BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    dashboard_no      VARCHAR(32)   NOT NULL                 COMMENT '编号 DASH-YYYY-NNNN',
    title             VARCHAR(200)  NOT NULL                 COMMENT '工作台名称',
    owner_user_id     BIGINT(20)    NOT NULL                 COMMENT '所属用户 FK→sys_user',
    layout_json       LONGTEXT                               COMMENT 'widget 布局 JSON',
    widget_types      VARCHAR(500)                           COMMENT 'CSV 启用 widget: stats,active_projects,my_todos,quality_snapshot,lifecycle,ai_metrics',
    refresh_interval  INT           DEFAULT 60               COMMENT '刷新间隔 (秒); 默认 60',
    is_default        CHAR(1)       DEFAULT 'N'              COMMENT 'Y=用户默认工作台',
    status            VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_dashboard_status',
    create_by         VARCHAR(64)   DEFAULT '',
    create_time       DATETIME      DEFAULT NULL,
    update_by         VARCHAR(64)   DEFAULT '',
    update_time       DATETIME      DEFAULT NULL,
    remark            VARCHAR(500)  DEFAULT '',
    del_flag          CHAR(1)       DEFAULT '0',
    PRIMARY KEY (dashboard_id),
    UNIQUE KEY uk_dashboard_no (dashboard_no),
    KEY idx_dashboard_owner (owner_user_id),
    KEY idx_dashboard_default (owner_user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作台预设（Dashboard）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('工作台状态', 'biz_dashboard_status', '0', 'admin', SYSDATE(), '2 状态'),
('工作台 Widget', 'biz_dashboard_widget', '0', 'admin', SYSDATE(), '6 类 widget');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '启用', '00', 'biz_dashboard_status', '', 'success', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '停用', '01', 'biz_dashboard_status', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),

(1, '统计卡片',       'stats',            'biz_dashboard_widget', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '4 大顶部卡片'),
(2, '在办项目进度',   'active_projects',  'biz_dashboard_widget', '', 'success', 'Y', '0', 'admin', SYSDATE(), ''),
(3, '我的待办',       'my_todos',         'biz_dashboard_widget', '', 'warning', 'Y', '0', 'admin', SYSDATE(), ''),
(4, '本迭代质量快照', 'quality_snapshot', 'biz_dashboard_widget', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(5, '项目生命周期',   'lifecycle',        'biz_dashboard_widget', '', 'primary', 'N', '0', 'admin', SYSDATE(), '17 阶段泳道'),
(6, 'AI 改进指标',    'ai_metrics',       'biz_dashboard_widget', '', 'danger',  'N', '0', 'admin', SYSDATE(), '');

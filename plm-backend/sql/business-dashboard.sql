-- =============================================================================
-- 工作台看板 (Dashboard) — 原型 dashboard.html
-- 用户个人看板组件配置（快速入口/图表卡片/数据表格）
-- 菜单 ID 段: 2740-2746
-- =============================================================================
DROP TABLE IF EXISTS tb_dashboard;
CREATE TABLE tb_dashboard (
    dashboard_id   BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    dashboard_no   VARCHAR(32)   NOT NULL                 COMMENT '编号 DSB-YYYY-NNNN',
    project_id     BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_project',
    widget_name    VARCHAR(200)  NOT NULL                 COMMENT '看板名称',
    widget_type    VARCHAR(20)   DEFAULT 'card'           COMMENT 'biz_dashboard_widget',
    data_source    VARCHAR(100)  DEFAULT ''               COMMENT '数据源标识',
    config         TEXT                                   COMMENT 'JSON 配置',
    sort_order     INT           DEFAULT 0                COMMENT '排序',
    visible        CHAR(1)       DEFAULT 'Y'              COMMENT 'Y=显示',
    user_id        BIGINT(20)    DEFAULT NULL             COMMENT '所属用户',
    status         VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_dashboard_status',
    create_by      VARCHAR(64)   DEFAULT '',
    create_time    DATETIME      DEFAULT NULL,
    update_by      VARCHAR(64)   DEFAULT '',
    update_time    DATETIME      DEFAULT NULL,
    remark         VARCHAR(500)  DEFAULT '',
    del_flag       CHAR(1)       DEFAULT '0',
    PRIMARY KEY (dashboard_id),
    UNIQUE KEY uk_dashboard_no (dashboard_no),
    KEY idx_dashboard_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作台看板配置';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('看板组件类型', 'biz_dashboard_widget', '0', 'admin', SYSDATE(), '4 类型'),
('看板状态',     'biz_dashboard_status', '0', 'admin', SYSDATE(), '2 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '图表',   'chart', 'biz_dashboard_widget', '', 'primary', 'N', '0', 'admin', SYSDATE()),
(2, '卡片',   'card',  'biz_dashboard_widget', '', 'success', 'Y', '0', 'admin', SYSDATE()),
(3, '表格',   'table', 'biz_dashboard_widget', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, '快捷链接','link', 'biz_dashboard_widget', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '草稿',   '00', 'biz_dashboard_status', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '已发布', '01', 'biz_dashboard_status', '', 'success', 'N', '0', 'admin', SYSDATE());

-- 默认看板组件种子数据
INSERT INTO tb_dashboard (dashboard_no, widget_name, widget_type, data_source, sort_order, visible, status, create_by, create_time) VALUES
('DSB-2026-0001', '项目健康度',     'card',  'project.health',       1, 'Y', '01', 'admin', SYSDATE()),
('DSB-2026-0002', '需求吞吐量趋势', 'chart', 'analytics.requirement', 2, 'Y', '01', 'admin', SYSDATE()),
('DSB-2026-0003', '待处理缺陷',     'table', 'defect.pending',        3, 'Y', '01', 'admin', SYSDATE()),
('DSB-2026-0004', '流水线状态',     'card',  'pipeline.status',       4, 'Y', '01', 'admin', SYSDATE()),
('DSB-2026-0005', 'AI效率统计',     'chart', 'analytics.ai_saving',   5, 'Y', '01', 'admin', SYSDATE());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2740, '工作台',   2000, 26, 'dashboard',   'business/dashboard/index',   'C', '0', '0', 'business:dashboard:list',   'monitor',  'admin', SYSDATE(), ''),
(2741, '看板查询', 2740, 1,  '#', '', 'F', '0', '0', 'business:dashboard:query',  '#', 'admin', SYSDATE(), ''),
(2742, '看板新增', 2740, 2,  '#', '', 'F', '0', '0', 'business:dashboard:add',    '#', 'admin', SYSDATE(), ''),
(2743, '看板修改', 2740, 3,  '#', '', 'F', '0', '0', 'business:dashboard:edit',   '#', 'admin', SYSDATE(), ''),
(2744, '看板删除', 2740, 4,  '#', '', 'F', '0', '0', 'business:dashboard:remove', '#', 'admin', SYSDATE(), ''),
(2745, '看板导出', 2740, 5,  '#', '', 'F', '0', '0', 'business:dashboard:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2740 AND 2745;

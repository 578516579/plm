-- =============================================================================
-- 运维手册 (ManualOps) — PRD §F5.3 + 原型 opsmanual.html
-- AI生成监控告警/故障排查/IoT运维手册
-- 菜单 ID 段: 2720-2726
-- =============================================================================
DROP TABLE IF EXISTS tb_manual_ops;
CREATE TABLE tb_manual_ops (
    manual_ops_id     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    manual_ops_no     VARCHAR(32)   NOT NULL                 COMMENT '编号 MOP-YYYY-NNNN',
    project_id        BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title             VARCHAR(200)  NOT NULL                 COMMENT '手册标题',
    monitoring_plan   VARCHAR(40)   DEFAULT 'prometheus_grafana' COMMENT 'biz_manual_ops_monitor',
    alert_channels    VARCHAR(200)  DEFAULT '[]'             COMMENT 'JSON 告警渠道',
    iot_device_types  VARCHAR(500)  DEFAULT '[]'             COMMENT 'JSON IoT设备类型',
    content           LONGTEXT                               COMMENT '手册正文 Markdown',
    ai_generated      CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI生成',
    ai_generated_at   DATETIME      DEFAULT NULL,
    status            VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_manual_ops_status',
    author_user_id    BIGINT(20)    DEFAULT NULL,
    reviewer_user_id  BIGINT(20)    DEFAULT NULL,
    create_by         VARCHAR(64)   DEFAULT '',
    create_time       DATETIME      DEFAULT NULL,
    update_by         VARCHAR(64)   DEFAULT '',
    update_time       DATETIME      DEFAULT NULL,
    remark            VARCHAR(500)  DEFAULT '',
    del_flag          CHAR(1)       DEFAULT '0',
    PRIMARY KEY (manual_ops_id),
    UNIQUE KEY uk_manual_ops_no (manual_ops_no),
    KEY idx_manual_ops_project (project_id),
    KEY idx_manual_ops_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维手册';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('运维监控方案', 'biz_manual_ops_monitor', '0', 'admin', SYSDATE(), '3 方案'),
('运维手册状态', 'biz_manual_ops_status',  '0', 'admin', SYSDATE(), '5 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'Prometheus+Grafana', 'prometheus_grafana', 'biz_manual_ops_monitor', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, '阿里云监控',         'aliyun',             'biz_manual_ops_monitor', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(3, 'Zabbix',             'zabbix',             'biz_manual_ops_monitor', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '草稿',   '00', 'biz_manual_ops_status', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '生成中', '01', 'biz_manual_ops_status', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(3, '已生成', '02', 'biz_manual_ops_status', '', 'primary', 'N', '0', 'admin', SYSDATE()),
(4, '已审核', '03', 'biz_manual_ops_status', '', 'success', 'N', '0', 'admin', SYSDATE()),
(5, '已发布', '04', 'biz_manual_ops_status', '', 'danger',  'N', '0', 'admin', SYSDATE());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2720, '运维手册',   2000, 24, 'manual-ops', 'business/manual-ops/index', 'C', '0', '0', 'business:manual-ops:list',   'tools',  'admin', SYSDATE(), 'PRD §F5.3'),
(2721, '手册查询',   2720, 1,  '#', '', 'F', '0', '0', 'business:manual-ops:query',  '#', 'admin', SYSDATE(), ''),
(2722, '手册新增',   2720, 2,  '#', '', 'F', '0', '0', 'business:manual-ops:add',    '#', 'admin', SYSDATE(), ''),
(2723, '手册修改',   2720, 3,  '#', '', 'F', '0', '0', 'business:manual-ops:edit',   '#', 'admin', SYSDATE(), ''),
(2724, '手册删除',   2720, 4,  '#', '', 'F', '0', '0', 'business:manual-ops:remove', '#', 'admin', SYSDATE(), ''),
(2725, '手册导出',   2720, 5,  '#', '', 'F', '0', '0', 'business:manual-ops:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2720 AND 2725;

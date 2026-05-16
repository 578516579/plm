-- =============================================================================
-- 运维手册 (ManualOps) — PRD §F5.3 + 原型 opsmanual.html
-- AI 一键生成 + 监控方案/告警渠道/IoT 设备类型多选 + 多格式导出
-- =============================================================================
DROP TABLE IF EXISTS tb_manual_ops;
CREATE TABLE tb_manual_ops (
    manualops_id        BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    manualops_no        VARCHAR(32)   NOT NULL                 COMMENT '编号 OM-YYYY-NNNN',
    project_id          BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title               VARCHAR(200)  NOT NULL                 COMMENT '手册标题',
    monitoring_plan     VARCHAR(30)                            COMMENT '监控方案 字典 biz_manualops_monitoring',
    alert_channels      VARCHAR(200)                           COMMENT '告警通知渠道 CSV: dingtalk,feishu,wework,email',
    iot_device_types    VARCHAR(300)                           COMMENT 'IoT 设备类型 CSV: soil_sensor,weather_station,drone,irrigation_controller',
    content             LONGTEXT                               COMMENT 'Markdown 全文',
    output_formats      VARCHAR(100)  NOT NULL DEFAULT 'pdf'   COMMENT 'CSV: word,pdf,html,markdown',
    ai_generated        CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    generated_at        DATETIME      DEFAULT NULL             COMMENT '生成完成时间',
    status              VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_manualops_status',
    author_user_id      BIGINT(20)    NOT NULL                 COMMENT '作者',
    create_by           VARCHAR(64)   DEFAULT '',
    create_time         DATETIME      DEFAULT NULL,
    update_by           VARCHAR(64)   DEFAULT '',
    update_time         DATETIME      DEFAULT NULL,
    remark              VARCHAR(500)  DEFAULT '',
    del_flag            CHAR(1)       DEFAULT '0',
    PRIMARY KEY (manualops_id),
    UNIQUE KEY uk_manualops_no (manualops_no),
    KEY idx_manualops_project (project_id),
    KEY idx_manualops_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维手册（ManualOps）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('运维手册状态',     'biz_manualops_status',     '0', 'admin', SYSDATE(), '4 状态'),
('运维监控方案',     'biz_manualops_monitoring', '0', 'admin', SYSDATE(), 'prom/aliyun/zabbix'),
('运维告警渠道',     'biz_manualops_alert',      '0', 'admin', SYSDATE(), '钉钉/飞书/企微/邮件 (CSV 值)'),
('运维IoT设备类型',  'biz_manualops_iot',        '0', 'admin', SYSDATE(), '土壤/气象/无人机/灌溉 (CSV 值)');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_manualops_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '生成中', '01', 'biz_manualops_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'AI 处理中'),
(3, '已生成', '02', 'biz_manualops_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已发布', '03', 'biz_manualops_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),

(1, 'Prometheus + Grafana', 'prometheus_grafana', 'biz_manualops_monitoring', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '阿里云监控',           'aliyun_cms',         'biz_manualops_monitoring', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'Zabbix',               'zabbix',             'biz_manualops_monitoring', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),

(1, '钉钉',     'dingtalk', 'biz_manualops_alert', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '飞书',     'feishu',   'biz_manualops_alert', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '企业微信', 'wework',   'biz_manualops_alert', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(4, '邮件',     'email',    'biz_manualops_alert', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),

(1, '土壤传感器',    'soil_sensor',           'biz_manualops_iot', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '气象站',        'weather_station',       'biz_manualops_iot', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '无人机',        'drone',                 'biz_manualops_iot', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(4, '灌溉控制器',    'irrigation_controller', 'biz_manualops_iot', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

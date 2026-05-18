-- =============================================================================
-- DORA效能指标 (Dora) — 原型 devops.html
-- 4 DORA指标: 部署频率/变更前置时间/变更失败率/平均恢复时间
-- 菜单 ID 段: 2780-2786
-- =============================================================================
DROP TABLE IF EXISTS tb_dora;
CREATE TABLE tb_dora (
    dora_id               BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    dora_no               VARCHAR(32)   NOT NULL                 COMMENT '编号 DOR-YYYY-NNNN',
    project_id            BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    period                VARCHAR(20)   NOT NULL                 COMMENT '周期 YYYY-MM 或 YYYY-QN',
    deploy_frequency      DECIMAL(8,2)  DEFAULT NULL             COMMENT '部署频率 次/天',
    lead_time_hours       DECIMAL(8,2)  DEFAULT NULL             COMMENT '变更前置时间 小时',
    change_failure_rate   DECIMAL(5,2)  DEFAULT NULL             COMMENT '变更失败率%',
    mttr_hours            DECIMAL(8,2)  DEFAULT NULL             COMMENT '平均恢复时间 小时',
    dora_level            VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_dora_level',
    ai_suggestions        TEXT                                   COMMENT 'AI改进建议',
    ai_generated          CHAR(1)       DEFAULT 'N',
    ai_generated_at       DATETIME      DEFAULT NULL,
    status                VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_dora_status',
    author_user_id        BIGINT(20)    DEFAULT NULL,
    create_by             VARCHAR(64)   DEFAULT '',
    create_time           DATETIME      DEFAULT NULL,
    update_by             VARCHAR(64)   DEFAULT '',
    update_time           DATETIME      DEFAULT NULL,
    remark                VARCHAR(500)  DEFAULT '',
    del_flag              CHAR(1)       DEFAULT '0',
    PRIMARY KEY (dora_id),
    UNIQUE KEY uk_dora_no (dora_no),
    KEY idx_dora_project_period (project_id, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DORA效能指标';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('DORA效能等级', 'biz_dora_level',  '0', 'admin', SYSDATE(), '4 等级'),
('DORA状态',     'biz_dora_status', '0', 'admin', SYSDATE(), '2 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'Elite 精英', 'elite',  'biz_dora_level', '', 'danger',  'N', '0', 'admin', SYSDATE()),
(2, 'High 高',    'high',   'biz_dora_level', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, 'Medium 中',  'medium', 'biz_dora_level', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, 'Low 低',     'low',    'biz_dora_level', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '草稿',   '00', 'biz_dora_status', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '已生成', '01', 'biz_dora_status', '', 'success', 'N', '0', 'admin', SYSDATE());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2780, 'DORA效能',   2000, 31, 'dora', 'business/dora/index', 'C', '0', '0', 'business:dora:list',   'odometer', 'admin', SYSDATE(), ''),
(2781, 'DORA查询',   2780, 1,  '#', '', 'F', '0', '0', 'business:dora:query',  '#', 'admin', SYSDATE(), ''),
(2782, 'DORA新增',   2780, 2,  '#', '', 'F', '0', '0', 'business:dora:add',    '#', 'admin', SYSDATE(), ''),
(2783, 'DORA修改',   2780, 3,  '#', '', 'F', '0', '0', 'business:dora:edit',   '#', 'admin', SYSDATE(), ''),
(2784, 'DORA删除',   2780, 4,  '#', '', 'F', '0', '0', 'business:dora:remove', '#', 'admin', SYSDATE(), ''),
(2785, 'DORA导出',   2780, 5,  '#', '', 'F', '0', '0', 'business:dora:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2780 AND 2785;

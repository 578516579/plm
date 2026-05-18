-- =============================================================================
-- 实施手册 (ManualImpl) — PRD §F5.2 + 原型 implmanual.html
-- AI生成部署/初始化/升级实施手册，支持多种部署模式和OS
-- 菜单 ID 段: 2710-2716
-- =============================================================================
DROP TABLE IF EXISTS tb_manual_impl;
CREATE TABLE tb_manual_impl (
    manual_impl_id    BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    manual_impl_no    VARCHAR(32)   NOT NULL                 COMMENT '编号 MIM-YYYY-NNNN',
    project_id        BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title             VARCHAR(200)  NOT NULL                 COMMENT '手册标题',
    deployment_mode   VARCHAR(30)   DEFAULT 'docker'         COMMENT 'biz_manual_impl_deploy',
    os                VARCHAR(30)   DEFAULT 'centos7'        COMMENT 'biz_manual_impl_os',
    `database`        VARCHAR(30)   DEFAULT 'mysql8'         COMMENT 'biz_manual_impl_db',
    env_vars          TEXT                                   COMMENT '环境变量 JSON',
    content           LONGTEXT                               COMMENT '手册正文 Markdown',
    ai_generated      CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI生成',
    ai_generated_at   DATETIME      DEFAULT NULL             COMMENT 'AI生成时间',
    status            VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_manual_impl_status',
    author_user_id    BIGINT(20)    DEFAULT NULL             COMMENT '创建人',
    reviewer_user_id  BIGINT(20)    DEFAULT NULL             COMMENT '审核人',
    create_by         VARCHAR(64)   DEFAULT '',
    create_time       DATETIME      DEFAULT NULL,
    update_by         VARCHAR(64)   DEFAULT '',
    update_time       DATETIME      DEFAULT NULL,
    remark            VARCHAR(500)  DEFAULT '',
    del_flag          CHAR(1)       DEFAULT '0',
    PRIMARY KEY (manual_impl_id),
    UNIQUE KEY uk_manual_impl_no (manual_impl_no),
    KEY idx_manual_impl_project (project_id),
    KEY idx_manual_impl_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实施手册';

-- 字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('实施部署模式', 'biz_manual_impl_deploy', '0', 'admin', SYSDATE(), '3 模式'),
('实施操作系统', 'biz_manual_impl_os',     '0', 'admin', SYSDATE(), '3 OS'),
('实施数据库',   'biz_manual_impl_db',     '0', 'admin', SYSDATE(), '3 DB'),
('实施手册状态', 'biz_manual_impl_status', '0', 'admin', SYSDATE(), '5 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'Docker Compose', 'docker',     'biz_manual_impl_deploy', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, 'Kubernetes',     'k8s',        'biz_manual_impl_deploy', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, '裸机部署',       'bare_metal', 'biz_manual_impl_deploy', '', 'warning', 'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'CentOS 7+',    'centos7',  'biz_manual_impl_os', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, 'Ubuntu 20.04', 'ubuntu20', 'biz_manual_impl_os', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, '麒麟OS',       'kylin',    'biz_manual_impl_os', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'MySQL 8.0', 'mysql8',    'biz_manual_impl_db', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, 'PostgreSQL 14', 'pg14',  'biz_manual_impl_db', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, '人大金仓',   'kingbase', 'biz_manual_impl_db', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '草稿',   '00', 'biz_manual_impl_status', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '生成中', '01', 'biz_manual_impl_status', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(3, '已生成', '02', 'biz_manual_impl_status', '', 'primary', 'N', '0', 'admin', SYSDATE()),
(4, '已审核', '03', 'biz_manual_impl_status', '', 'success', 'N', '0', 'admin', SYSDATE()),
(5, '已发布', '04', 'biz_manual_impl_status', '', 'danger',  'N', '0', 'admin', SYSDATE());

-- 菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2710, '实施手册',   2000, 23, 'manual-impl', 'business/manual-impl/index', 'C', '0', '0', 'business:manual-impl:list',   'document',  'admin', SYSDATE(), 'PRD §F5.2'),
(2711, '手册查询',   2710, 1,  '#', '', 'F', '0', '0', 'business:manual-impl:query',  '#', 'admin', SYSDATE(), ''),
(2712, '手册新增',   2710, 2,  '#', '', 'F', '0', '0', 'business:manual-impl:add',    '#', 'admin', SYSDATE(), ''),
(2713, '手册修改',   2710, 3,  '#', '', 'F', '0', '0', 'business:manual-impl:edit',   '#', 'admin', SYSDATE(), ''),
(2714, '手册删除',   2710, 4,  '#', '', 'F', '0', '0', 'business:manual-impl:remove', '#', 'admin', SYSDATE(), ''),
(2715, '手册导出',   2710, 5,  '#', '', 'F', '0', '0', 'business:manual-impl:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2710 AND 2715;

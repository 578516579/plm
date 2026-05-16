-- =============================================================================
-- 实施手册 (ManualImpl) — PRD §F5.2 + 原型 implmanual.html
-- AI 一键生成 + 部署模式/OS/DB 维度配置 + 多格式导出
-- =============================================================================
DROP TABLE IF EXISTS tb_manual_impl;
CREATE TABLE tb_manual_impl (
    manualimpl_id    BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    manualimpl_no    VARCHAR(32)   NOT NULL                 COMMENT '编号 IM-YYYY-NNNN',
    project_id       BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title            VARCHAR(200)  NOT NULL                 COMMENT '手册标题',
    deploy_mode      VARCHAR(30)                            COMMENT '部署模式 字典 biz_manualimpl_deploy',
    os_type          VARCHAR(30)                            COMMENT '操作系统 字典 biz_manualimpl_os',
    db_type          VARCHAR(30)                            COMMENT '数据库 字典 biz_manualimpl_db',
    env_config       TEXT                                   COMMENT '环境变量 (JSON 格式)',
    content          LONGTEXT                               COMMENT 'Markdown 全文',
    output_formats   VARCHAR(100)  NOT NULL DEFAULT 'pdf'   COMMENT 'CSV: word,pdf,html,markdown',
    ai_generated     CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    generated_at     DATETIME      DEFAULT NULL             COMMENT '生成完成时间',
    status           VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_manualimpl_status',
    author_user_id   BIGINT(20)    NOT NULL                 COMMENT '作者',
    create_by        VARCHAR(64)   DEFAULT '',
    create_time      DATETIME      DEFAULT NULL,
    update_by        VARCHAR(64)   DEFAULT '',
    update_time      DATETIME      DEFAULT NULL,
    remark           VARCHAR(500)  DEFAULT '',
    del_flag         CHAR(1)       DEFAULT '0',
    PRIMARY KEY (manualimpl_id),
    UNIQUE KEY uk_manualimpl_no (manualimpl_no),
    KEY idx_manualimpl_project (project_id),
    KEY idx_manualimpl_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实施手册（ManualImpl）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('实施手册状态',  'biz_manualimpl_status', '0', 'admin', SYSDATE(), '4 状态'),
('实施部署模式',  'biz_manualimpl_deploy', '0', 'admin', SYSDATE(), 'docker/k8s/baremetal'),
('实施操作系统',  'biz_manualimpl_os',     '0', 'admin', SYSDATE(), 'centos/ubuntu/kylin'),
('实施数据库',    'biz_manualimpl_db',     '0', 'admin', SYSDATE(), 'pg/mysql/kdb');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_manualimpl_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '生成中', '01', 'biz_manualimpl_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'AI 处理中'),
(3, '已生成', '02', 'biz_manualimpl_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已发布', '03', 'biz_manualimpl_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),

(1, 'Docker Compose',  'docker_compose', 'biz_manualimpl_deploy', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'Kubernetes',      'kubernetes',     'biz_manualimpl_deploy', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '裸机部署',         'baremetal',      'biz_manualimpl_deploy', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),

(1, 'CentOS 7+',       'centos7',  'biz_manualimpl_os', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'Ubuntu 20.04',    'ubuntu20', 'biz_manualimpl_os', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '麒麟 OS',         'kylin',    'biz_manualimpl_os', '', 'warning', 'N', '0', 'admin', SYSDATE(), '国产化适配'),

(1, 'PostgreSQL 14',   'postgresql14', 'biz_manualimpl_db', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'MySQL 8.0',       'mysql8',       'biz_manualimpl_db', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '人大金仓 KingbaseES', 'kdb',      'biz_manualimpl_db', '', 'warning', 'N', '0', 'admin', SYSDATE(), '国产化适配');

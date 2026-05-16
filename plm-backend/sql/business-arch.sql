-- =============================================================================
-- 系统概要设计 HLD (Arch) — PRD §F3.1 + 原型 archdesign.html
-- AI 根据 PRD 自动推荐技术架构 + C4 模型容器图 + NFR 映射
-- =============================================================================
DROP TABLE IF EXISTS tb_arch;
CREATE TABLE tb_arch (
    arch_id            BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    arch_no            VARCHAR(32)   NOT NULL                 COMMENT '编号 ARCH-YYYY-NNNN',
    project_id         BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    prd_id             BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_prd (可选,关联 PRD)',
    title              VARCHAR(200)  NOT NULL                 COMMENT '架构方案标题',
    arch_mode          VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_arch_mode 架构模式',
    primary_stack      VARCHAR(50)   DEFAULT NULL             COMMENT 'biz_arch_stack 主要技术栈',
    database_choice    VARCHAR(50)   DEFAULT NULL             COMMENT 'biz_arch_database 数据库选型',
    ai_orchestration   VARCHAR(50)   DEFAULT NULL             COMMENT 'biz_arch_ai_engine AI 编排引擎',
    deployment_type    VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_arch_deployment 部署方式',
    iot_protocol       VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_arch_iot_protocol IoT 协议',
    design_content     LONGTEXT                               COMMENT '架构方案描述 Markdown',
    c4_diagram_content LONGTEXT                               COMMENT 'C4 容器图 Mermaid',
    nfr_mapping        TEXT                                   COMMENT '非功能需求映射 (性能/安全/兼容性)',
    ai_generated       CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    ai_generated_at    DATETIME      DEFAULT NULL             COMMENT 'AI 生成时间',
    status             VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_arch_status 4 状态',
    author_user_id     BIGINT(20)    NOT NULL                 COMMENT '架构师',
    reviewer_user_id   BIGINT(20)    DEFAULT NULL             COMMENT '评审人',
    create_by          VARCHAR(64)   DEFAULT '',
    create_time        DATETIME      DEFAULT NULL,
    update_by          VARCHAR(64)   DEFAULT '',
    update_time        DATETIME      DEFAULT NULL,
    remark             VARCHAR(500)  DEFAULT '',
    del_flag           CHAR(1)       DEFAULT '0',
    PRIMARY KEY (arch_id),
    UNIQUE KEY uk_arch_no (arch_no),
    KEY idx_arch_project (project_id),
    KEY idx_arch_prd (prd_id),
    KEY idx_arch_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统概要设计 HLD';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('架构模式',   'biz_arch_mode',         '0', 'admin', SYSDATE(), '4 选项: 微服务/单体/Serverless/分层'),
('技术栈',     'biz_arch_stack',        '0', 'admin', SYSDATE(), '4 选项'),
('数据库选型', 'biz_arch_database',     '0', 'admin', SYSDATE(), '3 选项含国产化'),
('AI 编排',    'biz_arch_ai_engine',    '0', 'admin', SYSDATE(), '3 选项'),
('部署方式',   'biz_arch_deployment',   '0', 'admin', SYSDATE(), '3 选项'),
('IoT 协议',   'biz_arch_iot_protocol', '0', 'admin', SYSDATE(), '3 选项'),
('架构状态',   'biz_arch_status',       '0', 'admin', SYSDATE(), '4 状态机含反向边 01→00');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '微服务架构', 'microservice', 'biz_arch_mode', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '单体架构',   'monolith',     'biz_arch_mode', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'Serverless', 'serverless',   'biz_arch_mode', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '分层架构',   'layered',      'biz_arch_mode', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'Java (SpringBoot3)', 'java_sb3',       'biz_arch_stack', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'Go (Gin)',           'go_gin',         'biz_arch_stack', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'Python (FastAPI)',   'python_fastapi', 'biz_arch_stack', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, 'Node.js',            'nodejs',         'biz_arch_stack', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'PostgreSQL + Redis', 'pg_redis',     'biz_arch_database', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'MySQL + Redis',      'mysql_redis',  'biz_arch_database', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '人大金仓(国产化)',   'kingbase',     'biz_arch_database', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'Dify + DeepSeek-V3', 'dify_deepseek',    'biz_arch_ai_engine', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'Dify + ChatGLM',     'dify_chatglm',     'biz_arch_ai_engine', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '自建 LangChain',     'self_langchain',   'biz_arch_ai_engine', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'Kubernetes',      'k8s',            'biz_arch_deployment', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'Docker Compose',  'docker_compose', 'biz_arch_deployment', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '裸机部署',        'baremetal',      'biz_arch_deployment', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'MQTT (EMQ X)',         'mqtt',          'biz_arch_iot_protocol', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'HTTP Long-polling',    'http_longpoll', 'biz_arch_iot_protocol', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'WebSocket',            'websocket',     'biz_arch_iot_protocol', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_arch_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '评审中', '01', 'biz_arch_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已确认', '02', 'biz_arch_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已废弃', '03', 'biz_arch_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

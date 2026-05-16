-- ============================================================
-- 架构设计 (tb_arch) — PRD §F3.1 系统架构设计
-- ============================================================
CREATE TABLE IF NOT EXISTS `tb_arch` (
  `arch_id`          BIGINT(20)      NOT NULL AUTO_INCREMENT         COMMENT '主键',
  `arch_no`          VARCHAR(32)     NOT NULL                        COMMENT '编号 ARCH-YYYY-NNNN',
  `project_id`       BIGINT(20)      NOT NULL                        COMMENT '关联项目 ID',
  `title`            VARCHAR(200)    NOT NULL                        COMMENT '架构方案标题',
  `arch_mode`        VARCHAR(30)     DEFAULT NULL                    COMMENT '架构模式 (biz_arch_mode)',
  `tech_stack`       VARCHAR(50)     DEFAULT NULL                    COMMENT '技术语言栈 (biz_arch_tech)',
  `db_stack`         VARCHAR(50)     DEFAULT NULL                    COMMENT '数据库方案 (biz_arch_db)',
  `ai_orchestration` VARCHAR(50)     DEFAULT NULL                    COMMENT 'AI 编排方案 (biz_arch_ai)',
  `deploy_mode`      VARCHAR(30)     DEFAULT NULL                    COMMENT '部署模式 (biz_arch_deploy)',
  `iot_protocol`     VARCHAR(30)     DEFAULT NULL                    COMMENT 'IoT 协议 (biz_arch_iot)',
  `arch_content`     LONGTEXT        DEFAULT NULL                    COMMENT '架构方案 Markdown 文档',
  `c4_diagram`       LONGTEXT        DEFAULT NULL                    COMMENT 'C4 容器图 (Mermaid/PlantUML)',
  `nfr_content`      TEXT            DEFAULT NULL                    COMMENT '非功能需求说明',
  `review_report`    LONGTEXT        DEFAULT NULL                    COMMENT 'AI 架构评审报告',
  `ai_generated`     CHAR(1)         NOT NULL DEFAULT 'N'            COMMENT 'AI 生成标志 Y/N',
  `ai_generated_at`  DATETIME        DEFAULT NULL                    COMMENT 'AI 生成时间',
  `status`           VARCHAR(2)      NOT NULL DEFAULT '00'           COMMENT '状态 (biz_arch_status)',
  `author_user_id`   BIGINT(20)      NOT NULL                        COMMENT '方案作者用户 ID',
  `reviewer_user_id` BIGINT(20)      DEFAULT NULL                    COMMENT '评审人用户 ID',
  -- BaseEntity
  `create_by`        VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '创建者',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '更新者',
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`           VARCHAR(500)    DEFAULT NULL                    COMMENT '备注',
  `del_flag`         CHAR(1)         NOT NULL DEFAULT '0'            COMMENT '删除标志 0正常 2删除',
  PRIMARY KEY (`arch_id`),
  UNIQUE KEY `uk_arch_no` (`arch_no`),
  KEY `idx_arch_project` (`project_id`),
  KEY `idx_arch_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='架构设计表 PRD §F3.1';

-- ============================================================
-- 字典: 架构模式
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('架构模式', 'biz_arch_mode', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.1 架构模式')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '微服务',   'microservice',  'biz_arch_mode', '', 'primary', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '单体应用', 'monolith',      'biz_arch_mode', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'Serverless', 'serverless',  'biz_arch_mode', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, '分层架构', 'layered',       'biz_arch_mode', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- ============================================================
-- 字典: 技术语言栈
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('技术语言栈', 'biz_arch_tech', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.1 技术栈')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'Java / Spring Boot',    'java_springboot3',  'biz_arch_tech', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'Go / Gin',              'go_gin',            'biz_arch_tech', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'Python / FastAPI',      'python_fastapi',    'biz_arch_tech', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, 'Node.js',               'nodejs',            'biz_arch_tech', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- ============================================================
-- 字典: 数据库方案
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('数据库方案', 'biz_arch_db', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.1 数据库方案')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'PostgreSQL + Redis',  'postgresql_redis', 'biz_arch_db', '', 'primary', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'MySQL + Redis',       'mysql_redis',      'biz_arch_db', '', 'success', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'KDB+',                'kdb',              'biz_arch_db', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- ============================================================
-- 字典: AI 编排方案
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('AI编排方案', 'biz_arch_ai', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.1 AI编排')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'Dify + DeepSeek',   'dify_deepseek',  'biz_arch_ai', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'Dify + ChatGLM',    'dify_chatglm',   'biz_arch_ai', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'LangChain 自建',    'langchain',      'biz_arch_ai', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- ============================================================
-- 字典: 部署模式
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('部署模式', 'biz_arch_deploy', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.1 部署模式')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'Kubernetes',       'kubernetes',       'biz_arch_deploy', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'Docker Compose',   'docker_compose',   'biz_arch_deploy', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '裸机部署',          'baremetal',        'biz_arch_deploy', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- ============================================================
-- 字典: IoT 协议
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('IoT协议', 'biz_arch_iot', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.1 IoT协议')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'MQTT + EMQX',      'mqtt_emqx',    'biz_arch_iot', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'HTTP 轮询',         'http_polling', 'biz_arch_iot', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'WebSocket',         'websocket',    'biz_arch_iot', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- ============================================================
-- 字典: 架构状态
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('架构状态', 'biz_arch_status', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.1 架构状态')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '草稿',   '00', 'biz_arch_status', '', 'info',    'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '评审中', '01', 'biz_arch_status', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '已确认', '02', 'biz_arch_status', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, '已废弃', '03', 'biz_arch_status', '', 'danger',  'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

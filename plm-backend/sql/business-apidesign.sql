-- ============================================================
-- 接口详细设计 (tb_apidesign) — PRD §F3.3 接口设计
-- ============================================================
CREATE TABLE IF NOT EXISTS `tb_apidesign` (
  `apidesign_id`     BIGINT(20)      NOT NULL AUTO_INCREMENT         COMMENT '主键',
  `apidesign_no`     VARCHAR(32)     NOT NULL                        COMMENT '编号 APID-YYYY-NNNN',
  `project_id`       BIGINT(20)      NOT NULL                        COMMENT '关联项目 ID',
  `title`            VARCHAR(200)    NOT NULL                        COMMENT '接口设计标题',
  `http_method`      VARCHAR(10)     DEFAULT NULL                    COMMENT 'HTTP 方法 (biz_apidesign_method)',
  `api_path`         VARCHAR(500)    DEFAULT NULL                    COMMENT '接口路径',
  `description`      TEXT            DEFAULT NULL                    COMMENT '接口功能描述',
  `request_schema`   LONGTEXT        DEFAULT NULL                    COMMENT '请求参数 JSON Schema',
  `response_schema`  LONGTEXT        DEFAULT NULL                    COMMENT '响应参数 JSON Schema',
  `error_codes`      TEXT            DEFAULT NULL                    COMMENT '错误码说明',
  `openapi_content`  LONGTEXT        DEFAULT NULL                    COMMENT '完整 OpenAPI 规范 YAML',
  `mock_enabled`     CHAR(1)         NOT NULL DEFAULT 'N'            COMMENT 'Mock 服务开关 Y/N',
  `version`          VARCHAR(20)     NOT NULL DEFAULT 'v1.0'         COMMENT '接口版本',
  `review_report`    LONGTEXT        DEFAULT NULL                    COMMENT 'AI OpenAPI 生成报告',
  `ai_generated`     CHAR(1)         NOT NULL DEFAULT 'N'            COMMENT 'AI 生成标志 Y/N',
  `ai_generated_at`  DATETIME        DEFAULT NULL                    COMMENT 'AI 生成时间',
  `status`           VARCHAR(2)      NOT NULL DEFAULT '00'           COMMENT '状态 (biz_apidesign_status)',
  `author_user_id`   BIGINT(20)      NOT NULL                        COMMENT '设计者用户 ID',
  `reviewer_user_id` BIGINT(20)      DEFAULT NULL                    COMMENT '评审人用户 ID',
  -- BaseEntity
  `create_by`        VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '创建者',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '更新者',
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`           VARCHAR(500)    DEFAULT NULL                    COMMENT '备注',
  `del_flag`         CHAR(1)         NOT NULL DEFAULT '0'            COMMENT '删除标志 0正常 2删除',
  PRIMARY KEY (`apidesign_id`),
  UNIQUE KEY `uk_apidesign_no` (`apidesign_no`),
  KEY `idx_apidesign_project` (`project_id`),
  KEY `idx_apidesign_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口设计表 PRD §F3.3';

-- ============================================================
-- 字典: HTTP 方法
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('HTTP方法', 'biz_apidesign_method', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.3 HTTP方法')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'GET',    'get',    'biz_apidesign_method', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'POST',   'post',   'biz_apidesign_method', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'PUT',    'put',    'biz_apidesign_method', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, 'DELETE', 'delete', 'biz_apidesign_method', '', 'danger',  'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (5, 'PATCH',  'patch',  'biz_apidesign_method', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- ============================================================
-- 字典: 接口设计状态
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('接口设计状态', 'biz_apidesign_status', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.3 接口设计状态')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '草稿',   '00', 'biz_apidesign_status', '', 'info',    'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '评审中', '01', 'biz_apidesign_status', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '已确认', '02', 'biz_apidesign_status', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, '已废弃', '03', 'biz_apidesign_status', '', 'danger',  'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

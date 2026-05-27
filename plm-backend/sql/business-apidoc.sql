-- =============================================================================
-- API 文档 (ApiDoc) — PRD §F5.4 + 原型 apidoc.html
-- 从 GitLab 代码注释自动提取 + OpenAPI 规范 + 在线调试
-- =============================================================================
DROP TABLE IF EXISTS tb_apidoc;
CREATE TABLE tb_apidoc (
    apidoc_id            BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    apidoc_no            VARCHAR(32)   NOT NULL                 COMMENT '编号 API-YYYY-NNNN',
    project_id           BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title                VARCHAR(200)  NOT NULL                 COMMENT '接口标题',
    http_method          VARCHAR(10)   NOT NULL                 COMMENT 'GET/POST/PUT/DELETE/PATCH',
    path                 VARCHAR(500)  NOT NULL                 COMMENT '接口路径 /api/...',
    description          TEXT                                   COMMENT '接口描述',
    request_schema       TEXT                                   COMMENT '请求 JSON Schema',
    response_schema      TEXT                                   COMMENT '响应 JSON Schema',
    openapi_spec         LONGTEXT                               COMMENT 'OpenAPI 3.0 YAML',
    source_class         VARCHAR(200)  DEFAULT NULL             COMMENT '源代码类全限定名',
    source_method        VARCHAR(100)  DEFAULT NULL             COMMENT '源方法名',
    version              VARCHAR(20)   NOT NULL DEFAULT 'v1.0'  COMMENT '接口版本',
    status               VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_apidoc_status',
    last_synced_at       DATETIME      DEFAULT NULL             COMMENT '最近同步时间',
    auto_extracted       CHAR(1)       DEFAULT 'N'              COMMENT 'Y=代码注释自动提取',
    create_by            VARCHAR(64)   DEFAULT '',
    create_time          DATETIME      DEFAULT NULL,
    update_by            VARCHAR(64)   DEFAULT '',
    update_time          DATETIME      DEFAULT NULL,
    remark               VARCHAR(500)  DEFAULT '',
    del_flag             CHAR(1)       DEFAULT '0',
    PRIMARY KEY (apidoc_id),
    UNIQUE KEY uk_apidoc_no (apidoc_no),
    UNIQUE KEY uk_apidoc_method_path (http_method, path, version),
    KEY idx_apidoc_project (project_id),
    KEY idx_apidoc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API 文档（ApiDoc）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('API 文档状态', 'biz_apidoc_status', '0', 'admin', SYSDATE(), '3 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_apidoc_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已发布', '01', 'biz_apidoc_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已废弃', '02', 'biz_apidoc_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

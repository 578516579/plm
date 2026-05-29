-- =============================================================================
-- LLD 接口详细设计 (ApiDesign) — PRD §F3.3 + 原型 apidesign.html
-- AI 生成 OpenAPI 3.0 规范 + 类图 + 时序图,支持 Mock 服务
-- 与 tb_apidoc (F5.4 发布期) 区分: 本表是「设计期」产物
-- =============================================================================
DROP TABLE IF EXISTS tb_apidesign;
CREATE TABLE tb_apidesign (
    apidesign_id     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    apidesign_no     VARCHAR(32)   NOT NULL                 COMMENT '编号 APID-YYYY-NNNN',
    project_id       BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    arch_id          BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_arch (可选)',
    title            VARCHAR(200)  NOT NULL                 COMMENT '接口设计标题/资源名',
    http_method      VARCHAR(10)   NOT NULL                 COMMENT 'GET/POST/PUT/DELETE/PATCH',
    path             VARCHAR(500)  NOT NULL                 COMMENT '接口路径 /api/v1/...',
    description      TEXT                                   COMMENT '接口描述',
    request_schema   TEXT                                   COMMENT '请求 JSON Schema',
    response_schema  TEXT                                   COMMENT '响应 JSON Schema',
    openapi_spec     LONGTEXT                               COMMENT 'OpenAPI 3.0 YAML',
    mock_enabled     CHAR(1)       DEFAULT 'N'              COMMENT 'Y=开启 Mock 服务 (F3.6 联调)',
    mock_response    TEXT                                   COMMENT 'Mock 响应体 JSON',
    ai_generated     CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    ai_generated_at  DATETIME      DEFAULT NULL             COMMENT 'AI 生成时间',
    status           VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_apidesign_status 4 状态',
    author_user_id   BIGINT(20)    NOT NULL                 COMMENT '设计者',
    reviewer_user_id BIGINT(20)    DEFAULT NULL             COMMENT '评审人',
    create_by        VARCHAR(64)   DEFAULT '',
    create_time      DATETIME      DEFAULT NULL,
    update_by        VARCHAR(64)   DEFAULT '',
    update_time      DATETIME      DEFAULT NULL,
    remark           VARCHAR(500)  DEFAULT '',
    del_flag         CHAR(1)       DEFAULT '0',
    PRIMARY KEY (apidesign_id),
    UNIQUE KEY uk_apidesign_no (apidesign_no),
    UNIQUE KEY uk_apidesign_project_method_path (project_id, http_method, path),
    KEY idx_apidesign_arch (arch_id),
    KEY idx_apidesign_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LLD 接口详细设计';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('LLD 接口状态', 'biz_apidesign_status', '0', 'admin', SYSDATE(), '4 态 含反向边 01→00');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_apidesign_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '评审中', '01', 'biz_apidesign_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已确认', '02', 'biz_apidesign_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已废弃', '03', 'biz_apidesign_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

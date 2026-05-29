-- =============================================================================
-- AI OpenSpec (Openspec) — PRD §F3.5 + 原型 aispec.html
-- Spec as Code: OpenAPI 3.1 / AsyncAPI 3.0 / AI Function Spec / GraphQL + AgriKB 引用
-- =============================================================================
DROP TABLE IF EXISTS tb_openspec;
CREATE TABLE tb_openspec (
    openspec_id      BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    openspec_no      VARCHAR(32)   NOT NULL                 COMMENT '编号 SPEC-YYYY-NNNN',
    spec_name        VARCHAR(200)  NOT NULL                 COMMENT '规范名称',
    spec_type        VARCHAR(30)   NOT NULL                 COMMENT '字典 biz_openspec_type: openapi/asyncapi/ai_function/graphql',
    description      VARCHAR(500)                           COMMENT '规范描述',
    spec_content     LONGTEXT                               COMMENT '规范内容 (YAML/JSON)',
    version          VARCHAR(30)   NOT NULL                 COMMENT '版本号 (语义化)',
    agri_kb_ref      VARCHAR(200)                           COMMENT 'AgriKB 引用 (x-agrikb-ref)',
    ai_generated     CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    ai_generated_at  DATETIME      DEFAULT NULL             COMMENT 'AI 生成时间',
    status           VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_openspec_status',
    author_user_id   BIGINT(20)    NOT NULL                 COMMENT '创建者',
    create_by        VARCHAR(64)   DEFAULT '',
    create_time      DATETIME      DEFAULT NULL,
    update_by        VARCHAR(64)   DEFAULT '',
    update_time      DATETIME      DEFAULT NULL,
    remark           VARCHAR(500)  DEFAULT '',
    del_flag         CHAR(1)       DEFAULT '0',
    PRIMARY KEY (openspec_id),
    UNIQUE KEY uk_openspec_no (openspec_no),
    UNIQUE KEY uk_openspec_name_ver (spec_name, version, del_flag),
    KEY idx_openspec_type (spec_type),
    KEY idx_openspec_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI OpenSpec（Openspec）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('OpenSpec 状态', 'biz_openspec_status', '0', 'admin', SYSDATE(), '3 状态'),
('OpenSpec 类型', 'biz_openspec_type',   '0', 'admin', SYSDATE(), '4 类规范');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_openspec_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已发布', '01', 'biz_openspec_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已弃用', '02', 'biz_openspec_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态'),

(1, 'OpenAPI 3.1',     'openapi',     'biz_openspec_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), 'REST'),
(2, 'AsyncAPI 3.0',    'asyncapi',    'biz_openspec_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '事件流/MQTT'),
(3, 'AI Function Spec','ai_function', 'biz_openspec_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'Agent 函数定义'),
(4, 'GraphQL Schema',  'graphql',     'biz_openspec_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

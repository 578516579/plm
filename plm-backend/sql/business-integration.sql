-- =============================================================================
-- 外部集成 (plm-integration) 业务模块 — DDL + 菜单 + 权限 + 字典
-- 关联：02-设计/MCP-集成-设计.md / Proposal 0007 / PRD-MAPPING.md §33
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-integration.sql
-- 回滚：sql/business-integration-rollback.sql
-- =============================================================================

-- ----------------------------
-- 1. tb_integration_connector
-- ----------------------------
DROP TABLE IF EXISTS tb_integration_connector;
CREATE TABLE tb_integration_connector (
    id                  BIGINT(20)      NOT NULL AUTO_INCREMENT   COMMENT '主键',
    connector_code      VARCHAR(64)     NOT NULL                  COMMENT '编码(唯一)',
    connector_name      VARCHAR(128)    NOT NULL                  COMMENT '名称',
    connector_type      VARCHAR(32)     NOT NULL                  COMMENT '类型(biz_integration_type):feishu/gitlab/dingtalk/jira/figma/zentao/ztf',
    endpoint            VARCHAR(512)    DEFAULT ''                COMMENT '外部系统基址',
    auth_type           VARCHAR(16)     DEFAULT 'app_secret'      COMMENT '鉴权(biz_integration_auth)',
    credential_enc      VARCHAR(2048)   DEFAULT ''                COMMENT '凭据 JSON 密文(AES-256-GCM)',
    webhook_secret      VARCHAR(256)    DEFAULT ''                COMMENT 'Webhook 验签密钥(HMAC)',
    config_json         TEXT                                      COMMENT '类型特定配置 JSON',
    status              CHAR(1)         DEFAULT '0'               COMMENT '状态(biz_integration_status):0=启用 1=停用 2=异常',
    last_sync_at        DATETIME        DEFAULT NULL              COMMENT '最后同步时间',
    create_by           VARCHAR(64)     DEFAULT ''                COMMENT '创建者',
    create_time         DATETIME        DEFAULT NULL              COMMENT '创建时间',
    update_by           VARCHAR(64)     DEFAULT ''                COMMENT '更新者',
    update_time         DATETIME        DEFAULT NULL              COMMENT '更新时间',
    remark              VARCHAR(500)    DEFAULT ''                COMMENT '备注',
    del_flag            CHAR(1)         DEFAULT '0'               COMMENT '删除标志',
    PRIMARY KEY (id),
    UNIQUE KEY uk_int_connector_code (connector_code),
    KEY idx_int_connector_type_status (connector_type, status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='集成连接器';

-- ----------------------------
-- 2. tb_integration_webhook_event
-- ----------------------------
DROP TABLE IF EXISTS tb_integration_webhook_event;
CREATE TABLE tb_integration_webhook_event (
    id                  BIGINT(20)      NOT NULL AUTO_INCREMENT   COMMENT '主键',
    connector_id        BIGINT(20)      NOT NULL                  COMMENT '来源 connector FK',
    event_type          VARCHAR(128)    DEFAULT ''                COMMENT '事件类型',
    external_event_id   VARCHAR(128)    DEFAULT ''                COMMENT '外部 event id (幂等键)',
    payload_json        LONGTEXT                                  COMMENT '原始 payload',
    signature           VARCHAR(512)    DEFAULT ''                COMMENT '签名头',
    signature_verified  CHAR(1)         DEFAULT '0'               COMMENT '验签是否通过 0/1',
    process_status      CHAR(1)         DEFAULT '0'               COMMENT '处理状态(biz_webhook_status):0=待 1=中 2=成 3=败 4=略',
    process_error       VARCHAR(2000)   DEFAULT ''                COMMENT '失败原因',
    retry_count         INT             DEFAULT 0                 COMMENT '重试次数',
    source_ip           VARCHAR(64)     DEFAULT ''                COMMENT '调用方 IP',
    process_time        DATETIME        DEFAULT NULL              COMMENT '处理完成时间',
    create_by           VARCHAR(64)     DEFAULT ''                COMMENT '创建者',
    create_time         DATETIME        DEFAULT NULL              COMMENT '创建时间',
    remark              VARCHAR(500)    DEFAULT ''                COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_int_webhook_idem (connector_id, external_event_id),
    KEY idx_int_webhook_connector_time (connector_id, create_time),
    KEY idx_int_webhook_status_time (process_status, create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='Webhook 入站事件流水';

-- ----------------------------
-- 3. 字典类型 + 字典数据
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('集成类型',     'biz_integration_type',   '0', 'admin', SYSDATE(), '外部系统类型'),
('集成鉴权',     'biz_integration_auth',   '0', 'admin', SYSDATE(), '集成鉴权方式'),
('集成状态',     'biz_integration_status', '0', 'admin', SYSDATE(), '连接器状态'),
('Webhook 状态', 'biz_webhook_status',     '0', 'admin', SYSDATE(), 'Webhook 处理状态');

-- 集成类型（首批 7 个，可后续扩）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '飞书 Feishu', 'feishu',   'biz_integration_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), '飞书 OpenAPI'),
(2, 'GitLab',      'gitlab',   'biz_integration_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'GitLab 自建/SaaS'),
(3, '钉钉',        'dingtalk', 'biz_integration_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '钉钉开放平台'),
(4, 'Jira',        'jira',     'biz_integration_type', '', 'info',    'N', '0', 'admin', SYSDATE(), 'Atlassian Jira'),
(5, 'Figma',       'figma',    'biz_integration_type', '', 'info',    'N', '0', 'admin', SYSDATE(), 'Figma API'),
(6, '禅道',        'zentao',   'biz_integration_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '禅道'),
(7, 'ZTF',         'ztf',      'biz_integration_type', '', 'info',    'N', '0', 'admin', SYSDATE(), 'ZTF 自动化');

-- 集成鉴权
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'App Secret', 'app_secret',   'biz_integration_auth', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '应用密钥'),
(2, 'Access Token', 'access_token','biz_integration_auth', '', 'info',    'N', '0', 'admin', SYSDATE(), '长效 token'),
(3, 'OAuth2',     'oauth2',       'biz_integration_auth', '', 'success', 'N', '0', 'admin', SYSDATE(), 'OAuth 2.0'),
(4, 'Personal AT','pat',          'biz_integration_auth', '', 'warning', 'N', '0', 'admin', SYSDATE(), '个人访问令牌');

-- 集成状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '启用', '0', 'biz_integration_status', '', 'success', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '停用', '1', 'biz_integration_status', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(3, '异常', '2', 'biz_integration_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '');

-- Webhook 状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待处理', '0', 'biz_webhook_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '处理中', '1', 'biz_webhook_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '成功',   '2', 'biz_webhook_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '失败',   '3', 'biz_webhook_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(5, '已忽略', '4', 'biz_webhook_status', '', 'info',    'N', '0', 'admin', SYSDATE(), '验签失败等');

-- ----------------------------
-- 4. 菜单 + 权限（菜单 ID 段 2500-2599）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name,        parent_id, order_num, path,         component,                                  menu_type, visible, status, perms,                                       icon,         create_by, create_time, remark) VALUES
(2500, '外部集成',         0,    11, 'integration',     NULL,                                         'M', '0', '0', '',                                          'link',        'admin', SYSDATE(), '集成一级目录'),
(2510, '连接器配置',       2500, 1,  'connector',       'business/integration/connector/index',       'C', '0', '0', 'business:integration:connector:list',       'tool',        'admin', SYSDATE(), '飞书/GitLab/... 连接器'),
(2511, '连接器查询',       2510, 1,  '#',               '',                                           'F', '0', '0', 'business:integration:connector:query',      '#',           'admin', SYSDATE(), ''),
(2512, '连接器新增',       2510, 2,  '#',               '',                                           'F', '0', '0', 'business:integration:connector:add',        '#',           'admin', SYSDATE(), ''),
(2513, '连接器修改',       2510, 3,  '#',               '',                                           'F', '0', '0', 'business:integration:connector:edit',       '#',           'admin', SYSDATE(), ''),
(2514, '连接器删除',       2510, 4,  '#',               '',                                           'F', '0', '0', 'business:integration:connector:remove',     '#',           'admin', SYSDATE(), ''),
(2515, '连接器导出',       2510, 5,  '#',               '',                                           'F', '0', '0', 'business:integration:connector:export',     '#',           'admin', SYSDATE(), ''),
(2516, '连接器测试',       2510, 6,  '#',               '',                                           'F', '0', '0', 'business:integration:connector:test',       '#',           'admin', SYSDATE(), '连通性测试'),
(2520, 'Webhook 事件',     2500, 2,  'webhook',         'business/integration/webhook/index',         'C', '0', '0', 'business:integration:webhook:list',         'message',     'admin', SYSDATE(), '入站事件审计'),
(2521, '事件查询',         2520, 1,  '#',               '',                                           'F', '0', '0', 'business:integration:webhook:query',        '#',           'admin', SYSDATE(), ''),
(2522, '事件重试',         2520, 2,  '#',               '',                                           'F', '0', '0', 'business:integration:webhook:retry',        '#',           'admin', SYSDATE(), '失败事件手动重试'),
(2525, '事件导出',         2520, 3,  '#',               '',                                           'F', '0', '0', 'business:integration:webhook:export',       '#',           'admin', SYSDATE(), '');

-- ----------------------------
-- 5. admin (role_id=1) 授权
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2500), (1, 2510), (1, 2511), (1, 2512), (1, 2513), (1, 2514), (1, 2515), (1, 2516),
(1, 2520), (1, 2521), (1, 2522), (1, 2525);

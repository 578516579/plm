-- =============================================================================
-- MCP Server (plm-mcp) 业务模块 — DDL + 菜单 + 权限 + 字典
-- 关联：02-设计/MCP-集成-设计.md / Proposal 0007 / PRD-MAPPING.md §32
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-mcp.sql
-- 回滚：sql/business-mcp-rollback.sql
-- =============================================================================

-- ----------------------------
-- 1. tb_mcp_server
-- ----------------------------
DROP TABLE IF EXISTS tb_mcp_server;
CREATE TABLE tb_mcp_server (
    id                          BIGINT(20)      NOT NULL AUTO_INCREMENT   COMMENT '主键',
    server_code                 VARCHAR(64)     NOT NULL                  COMMENT 'Server 编码(唯一)',
    server_name                 VARCHAR(128)    NOT NULL                  COMMENT 'Server 名称',
    protocol                    VARCHAR(16)     DEFAULT 'http'            COMMENT '协议(biz_mcp_protocol):stdio/sse/http',
    endpoint                    VARCHAR(512)    DEFAULT ''                COMMENT '访问端点(http 模式)',
    auth_type                   VARCHAR(16)     DEFAULT 'token'           COMMENT '鉴权(biz_mcp_auth):none/token/oauth2',
    oauth_client_id             VARCHAR(128)    DEFAULT ''                COMMENT 'OAuth 客户端 ID',
    oauth_client_secret_enc     VARCHAR(1024)   DEFAULT ''                COMMENT 'OAuth secret 密文(AES-256-GCM)',
    tools_json                  TEXT                                      COMMENT '工具列表 JSON',
    status                      CHAR(1)         DEFAULT '0'               COMMENT '状态(biz_mcp_status):0=启用 1=停用 2=异常',
    last_health_at              DATETIME        DEFAULT NULL              COMMENT '最后心跳',
    description                 VARCHAR(500)    DEFAULT ''                COMMENT '描述',
    create_by                   VARCHAR(64)     DEFAULT ''                COMMENT '创建者',
    create_time                 DATETIME        DEFAULT NULL              COMMENT '创建时间',
    update_by                   VARCHAR(64)     DEFAULT ''                COMMENT '更新者',
    update_time                 DATETIME        DEFAULT NULL              COMMENT '更新时间',
    remark                      VARCHAR(500)    DEFAULT ''                COMMENT '备注',
    del_flag                    CHAR(1)         DEFAULT '0'               COMMENT '删除标志',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mcp_server_code (server_code),
    KEY idx_mcp_server_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='MCP Server 注册表';

-- ----------------------------
-- 2. tb_mcp_tool_audit
-- ----------------------------
DROP TABLE IF EXISTS tb_mcp_tool_audit;
CREATE TABLE tb_mcp_tool_audit (
    id              BIGINT(20)      NOT NULL AUTO_INCREMENT   COMMENT '主键',
    server_id       BIGINT(20)      NOT NULL                  COMMENT 'Server FK',
    tool_name       VARCHAR(128)    NOT NULL                  COMMENT '工具名(project.list / requirement.create / ...)',
    caller_type     VARCHAR(16)     DEFAULT 'agent'           COMMENT '调用方类型:user/agent/system',
    caller_id       VARCHAR(128)    DEFAULT ''                COMMENT '调用方ID',
    params_json     TEXT                                      COMMENT '调用参数 JSON',
    result_status   CHAR(1)         DEFAULT '0'               COMMENT '结果(biz_audit_result):0=成功 1=失败 2=超时',
    result_brief    VARCHAR(2000)   DEFAULT ''                COMMENT '结果摘要(≤2KB)',
    latency_ms      INT                                       COMMENT '耗时(ms)',
    call_time       DATETIME        DEFAULT NULL              COMMENT '调用时间',
    create_by       VARCHAR(64)     DEFAULT ''                COMMENT '创建者',
    create_time     DATETIME        DEFAULT NULL              COMMENT '创建时间',
    remark          VARCHAR(500)    DEFAULT ''                COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_mcp_audit_server_time (server_id, create_time),
    KEY idx_mcp_audit_tool (tool_name),
    KEY idx_mcp_audit_create (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='MCP 工具调用审计';

-- ----------------------------
-- 3. 字典类型 + 字典数据
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('MCP 协议',  'biz_mcp_protocol', '0', 'admin', SYSDATE(), 'MCP 协议类型'),
('MCP 鉴权',  'biz_mcp_auth',     '0', 'admin', SYSDATE(), 'MCP 鉴权类型'),
('MCP 状态',  'biz_mcp_status',   '0', 'admin', SYSDATE(), 'MCP Server 状态'),
('审计结果',  'biz_audit_result', '0', 'admin', SYSDATE(), '调用结果状态');

-- MCP 协议
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'stdio', 'stdio', 'biz_mcp_protocol', '', 'info',    'N', '0', 'admin', SYSDATE(), '本地进程'),
(2, 'SSE',   'sse',   'biz_mcp_protocol', '', 'primary', 'N', '0', 'admin', SYSDATE(), 'Server-Sent Events'),
(3, 'HTTP',  'http',  'biz_mcp_protocol', '', 'success', 'Y', '0', 'admin', SYSDATE(), 'HTTP/JSON-RPC');

-- MCP 鉴权
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '无鉴权', 'none',   'biz_mcp_auth', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(2, 'Token',  'token',  'biz_mcp_auth', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '长效 token'),
(3, 'OAuth2', 'oauth2', 'biz_mcp_auth', '', 'success', 'N', '0', 'admin', SYSDATE(), 'OAuth 2.0');

-- MCP 状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '启用', '0', 'biz_mcp_status', '', 'success', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '停用', '1', 'biz_mcp_status', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(3, '异常', '2', 'biz_mcp_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '');

-- 审计结果
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '成功', '0', 'biz_audit_result', '', 'success', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '失败', '1', 'biz_audit_result', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(3, '超时', '2', 'biz_audit_result', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

-- ----------------------------
-- 4. 菜单 + 权限（菜单 ID 段 2400-2499）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name,    parent_id, order_num, path,      component,                       menu_type, visible, status, perms,                              icon,        create_by, create_time, remark) VALUES
(2400, 'MCP 集成',     0,    10, 'mcp',           NULL,                              'M', '0', '0', '',                                 'connection',  'admin', SYSDATE(), 'MCP 一级目录'),
(2410, 'MCP Server',   2400, 1,  'mcpserver',     'business/mcp/server/index',       'C', '0', '0', 'business:mcp:server:list',        'cloud',       'admin', SYSDATE(), 'MCP Server 列表'),
(2411, 'Server查询',   2410, 1,  '#',             '',                                'F', '0', '0', 'business:mcp:server:query',       '#',           'admin', SYSDATE(), ''),
(2412, 'Server新增',   2410, 2,  '#',             '',                                'F', '0', '0', 'business:mcp:server:add',         '#',           'admin', SYSDATE(), ''),
(2413, 'Server修改',   2410, 3,  '#',             '',                                'F', '0', '0', 'business:mcp:server:edit',        '#',           'admin', SYSDATE(), ''),
(2414, 'Server删除',   2410, 4,  '#',             '',                                'F', '0', '0', 'business:mcp:server:remove',      '#',           'admin', SYSDATE(), ''),
(2415, 'Server导出',   2410, 5,  '#',             '',                                'F', '0', '0', 'business:mcp:server:export',      '#',           'admin', SYSDATE(), ''),
(2420, '调用审计',     2400, 2,  'audit',         'business/mcp/audit/index',        'C', '0', '0', 'business:mcp:audit:list',         'documentation','admin', SYSDATE(), '工具调用审计'),
(2421, '审计查询',     2420, 1,  '#',             '',                                'F', '0', '0', 'business:mcp:audit:query',        '#',           'admin', SYSDATE(), ''),
(2425, '审计导出',     2420, 2,  '#',             '',                                'F', '0', '0', 'business:mcp:audit:export',       '#',           'admin', SYSDATE(), '');

-- ----------------------------
-- 5. admin (role_id=1) 授权
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2400), (1, 2410), (1, 2411), (1, 2412), (1, 2413), (1, 2414), (1, 2415),
(1, 2420), (1, 2421), (1, 2425);

-- ----------------------------
-- 6. 种子: plm-core MCP Server (本系统对外暴露)
-- 注意: oauth_client_secret_enc 留空，运行时通过 UI 用本机的 MCP_ENCRYPT_KEY 加密后写入
-- ----------------------------
INSERT INTO tb_mcp_server (server_code, server_name, protocol, endpoint, auth_type,
                          oauth_client_id, oauth_client_secret_enc, tools_json, status,
                          description, create_by, create_time, del_flag) VALUES
('plm-core', 'PLM 核心工具集', 'http', '/mcp', 'token',
 'plm-core', '', NULL, '0',
 '暴露 project/requirement/task/testcase 等 PLM 业务工具给外部 LLM Agent',
 'admin', SYSDATE(), '0');

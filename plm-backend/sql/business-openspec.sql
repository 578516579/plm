-- =============================================================================
-- AI规范中心 (OpenSpec) — 原型 aispec.html
-- OpenAPI/AsyncAPI/GraphQL/AI Function Spec 管理，AgriKB农业语义增强
-- 菜单 ID 段: 2750-2756
-- =============================================================================
DROP TABLE IF EXISTS tb_openspec;
CREATE TABLE tb_openspec (
    openspec_id       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    openspec_no       VARCHAR(32)   NOT NULL                 COMMENT '编号 OSP-YYYY-NNNN',
    project_id        BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    spec_name         VARCHAR(200)  NOT NULL                 COMMENT '规范名称',
    spec_type         VARCHAR(30)   DEFAULT 'openapi31'      COMMENT 'biz_openspec_type',
    version           VARCHAR(30)   DEFAULT 'v1.0'           COMMENT '版本',
    content           LONGTEXT                               COMMENT 'YAML/JSON规范内容',
    ai_enhanced       CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI增强(AgriKB标注)',
    agrikb_ref        CHAR(1)       DEFAULT 'N'              COMMENT 'Y=含农业知识库引用',
    ai_generated      CHAR(1)       DEFAULT 'N',
    ai_generated_at   DATETIME      DEFAULT NULL,
    status            VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_openspec_status',
    author_user_id    BIGINT(20)    DEFAULT NULL,
    reviewer_user_id  BIGINT(20)    DEFAULT NULL,
    create_by         VARCHAR(64)   DEFAULT '',
    create_time       DATETIME      DEFAULT NULL,
    update_by         VARCHAR(64)   DEFAULT '',
    update_time       DATETIME      DEFAULT NULL,
    remark            VARCHAR(500)  DEFAULT '',
    del_flag          CHAR(1)       DEFAULT '0',
    PRIMARY KEY (openspec_id),
    UNIQUE KEY uk_openspec_no (openspec_no),
    KEY idx_openspec_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI规范中心';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('规范类型', 'biz_openspec_type',   '0', 'admin', SYSDATE(), '4 类型'),
('规范状态', 'biz_openspec_status', '0', 'admin', SYSDATE(), '4 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'OpenAPI 3.1',    'openapi31',   'biz_openspec_type', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, 'AsyncAPI 3.0',   'asyncapi30',  'biz_openspec_type', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, 'GraphQL',        'graphql',     'biz_openspec_type', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, 'AI Function',    'ai_function', 'biz_openspec_type', '', 'danger',  'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '草稿',   '00', 'biz_openspec_status', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '审核中', '01', 'biz_openspec_status', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(3, '已发布', '02', 'biz_openspec_status', '', 'success', 'N', '0', 'admin', SYSDATE()),
(4, '已废弃', '03', 'biz_openspec_status', '', 'danger',  'N', '0', 'admin', SYSDATE());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2750, 'AI规范',   2000, 28, 'openspec', 'business/openspec/index', 'C', '0', '0', 'business:openspec:list',   'document-checked', 'admin', SYSDATE(), ''),
(2751, '规范查询', 2750, 1,  '#', '', 'F', '0', '0', 'business:openspec:query',  '#', 'admin', SYSDATE(), ''),
(2752, '规范新增', 2750, 2,  '#', '', 'F', '0', '0', 'business:openspec:add',    '#', 'admin', SYSDATE(), ''),
(2753, '规范修改', 2750, 3,  '#', '', 'F', '0', '0', 'business:openspec:edit',   '#', 'admin', SYSDATE(), ''),
(2754, '规范删除', 2750, 4,  '#', '', 'F', '0', '0', 'business:openspec:remove', '#', 'admin', SYSDATE(), ''),
(2755, '规范导出', 2750, 5,  '#', '', 'F', '0', '0', 'business:openspec:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2750 AND 2755;

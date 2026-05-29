-- =============================================================================
-- Document (文档) 业务模块 — DDL + 字典 + 菜单 + 权限
-- 关联: 02-设计/Document-数据库设计.md / ADR-0007 (DOC-<TYPE>-YYYY-NNNN)
-- =============================================================================

DROP TABLE IF EXISTS tb_document;
CREATE TABLE tb_document (
    document_id           BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    document_no           VARCHAR(32)   NOT NULL                 COMMENT '编号 DOC-<TYPE>-YYYY-NNNN ADR-0007',
    project_id            BIGINT(20)    NOT NULL                 COMMENT '所属项目',
    related_entity_type   VARCHAR(20)   DEFAULT NULL             COMMENT '关联实体类型',
    related_entity_id     BIGINT(20)    DEFAULT NULL             COMMENT '关联实体 ID',
    doc_type              VARCHAR(20)   NOT NULL                 COMMENT 'biz_doc_type',
    title                 VARCHAR(200)  NOT NULL                 COMMENT '文档标题',
    content               LONGTEXT                               COMMENT 'Markdown 全文',
    version               VARCHAR(20)   NOT NULL DEFAULT 'v1.0'  COMMENT '版本号',
    status                VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_doc_status',
    author_user_id        BIGINT(20)    NOT NULL                 COMMENT '作者',
    reviewer_user_id      BIGINT(20)    DEFAULT NULL             COMMENT '审核人',
    tags                  VARCHAR(200)  DEFAULT NULL             COMMENT 'CSV 标签',
    create_by             VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time           DATETIME      DEFAULT NULL             COMMENT '创建时间',
    update_by             VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time           DATETIME      DEFAULT NULL             COMMENT '更新时间',
    remark                VARCHAR(500)  DEFAULT ''               COMMENT '备注',
    del_flag              CHAR(1)       DEFAULT '0'              COMMENT '0=正常 2=删除',
    PRIMARY KEY (document_id),
    UNIQUE KEY uk_document_no (document_no),
    KEY idx_document_project (project_id),
    KEY idx_document_type_status (doc_type, status),
    KEY idx_document_related (related_entity_type, related_entity_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='文档（Document）';

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('文档类型', 'biz_doc_type',     '0', 'admin', SYSDATE(), '12 种 doc_type'),
('文档状态', 'biz_doc_status',   '0', 'admin', SYSDATE(), '4 状态机含反向边');

-- doc_type (12)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1,  'PRD 产品需求',  'prd',             'biz_doc_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '替代 plm-prd'),
(2,  '系统架构',      'arch',            'biz_doc_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '替代 plm-arch'),
(3,  '数据库设计',    'db_design',       'biz_doc_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '替代 plm-dbdesign'),
(4,  'API 详细设计',  'api_design',      'biz_doc_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '替代 plm-apidesign'),
(5,  '立项建议书',    'proposal',        'biz_doc_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), '替代 plm-proposal'),
(6,  'UED 设计稿',    'ued',             'biz_doc_type', '', 'success', 'N', '0', 'admin', SYSDATE(), 'v0.5'),
(7,  '测试方案',      'test_plan',       'biz_doc_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'v0.4'),
(8,  '测试报告',      'test_report',     'biz_doc_type', '', 'success', 'N', '0', 'admin', SYSDATE(), 'v0.4'),
(9,  'API 文档',      'api_doc',         'biz_doc_type', '', 'info',    'N', '0', 'admin', SYSDATE(), 'v0.4'),
(10, '产品手册',      'manual_product',  'biz_doc_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), 'v0.4'),
(11, '实施手册',      'manual_impl',     'biz_doc_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), 'v0.5'),
(12, '运维手册',      'manual_ops',      'biz_doc_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), 'v0.5');

-- doc_status (4) 4×4 状态机
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',    '00', 'biz_doc_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '待评审',  '01', 'biz_doc_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已发布',  '02', 'biz_doc_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已归档',  '03', 'biz_doc_status', '', '',        'N', '0', 'admin', SYSDATE(), '终态');

-- 菜单 2070-2075
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2070, '文档管理', 2000, 8, 'document', 'business/document/index', 'C', '0', '0', 'business:document:list',   'documentation', 'admin', SYSDATE(), '替代 5 文档类 stub'),
(2071, '文档查询', 2070, 1, '#', '', 'F', '0', '0', 'business:document:query',  '#', 'admin', SYSDATE(), ''),
(2072, '文档新增', 2070, 2, '#', '', 'F', '0', '0', 'business:document:add',    '#', 'admin', SYSDATE(), ''),
(2073, '文档修改', 2070, 3, '#', '', 'F', '0', '0', 'business:document:edit',   '#', 'admin', SYSDATE(), ''),
(2074, '文档删除', 2070, 4, '#', '', 'F', '0', '0', 'business:document:remove', '#', 'admin', SYSDATE(), ''),
(2075, '文档导出', 2070, 5, '#', '', 'F', '0', '0', 'business:document:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2070), (1, 2071), (1, 2072), (1, 2073), (1, 2074), (1, 2075);

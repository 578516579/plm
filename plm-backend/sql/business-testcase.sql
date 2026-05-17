-- =============================================================================
-- TestCase (测试用例) 业务模块 — DDL + 字典 + 菜单 + 权限
-- 关联: 02-设计/TestCase-数据库设计.md / ADR-0006 (TC-YYYY-NNNN)
-- =============================================================================

DROP TABLE IF EXISTS tb_testcase;
CREATE TABLE tb_testcase (
    testcase_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    testcase_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 TC-YYYY-NNNN（ADR-0006）',
    project_id               BIGINT(20)    NOT NULL                 COMMENT '所属项目',
    requirement_id           BIGINT(20)    DEFAULT NULL             COMMENT '关联需求（可空）',
    title                    VARCHAR(200)  NOT NULL                 COMMENT '用例标题',
    description              TEXT                                   COMMENT '概述',
    category                 VARCHAR(20)   NOT NULL DEFAULT 'functional' COMMENT 'biz_testcase_category 8 值字符串(ADR-B Option B),proposal 0300',
    priority                 VARCHAR(2)    NOT NULL DEFAULT '01'    COMMENT 'biz_testcase_priority',
    status                   VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_testcase_status',
    preconditions            TEXT                                   COMMENT '前置条件',
    steps                    TEXT          NOT NULL                 COMMENT '测试步骤',
    expected_result          TEXT          NOT NULL                 COMMENT '期望结果',
    actual_result            TEXT                                   COMMENT '实际结果（最近一次）',
    is_automated             CHAR(1)       NOT NULL DEFAULT 'N'     COMMENT 'Y/N',
    automation_script_path   VARCHAR(500)  DEFAULT NULL             COMMENT '自动化脚本路径',
    execution_count          INT           NOT NULL DEFAULT 0       COMMENT '累计执行次数',
    last_executed_at         DATETIME      DEFAULT NULL             COMMENT '最近执行时间',
    tags                     VARCHAR(200)  DEFAULT NULL             COMMENT 'CSV 标签',
    create_by                VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time              DATETIME      DEFAULT NULL             COMMENT '创建时间',
    update_by                VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time              DATETIME      DEFAULT NULL             COMMENT '更新时间',
    remark                   VARCHAR(500)  DEFAULT ''               COMMENT '备注',
    del_flag                 CHAR(1)       DEFAULT '0'              COMMENT '0=正常 2=删除',
    PRIMARY KEY (testcase_id),
    UNIQUE KEY uk_testcase_no (testcase_no),
    KEY idx_testcase_project (project_id),
    KEY idx_testcase_req (requirement_id),
    KEY idx_testcase_status_priority (status, priority),
    KEY idx_testcase_automated (is_automated)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='测试用例（TestCase）';

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('用例分类',     'biz_testcase_category', '0', 'admin', SYSDATE(), '8 值融合 — 原型 4 (功能/边界/异常/农业) + SQL 4 (接口/性能/安全/兼容性);ADR-B Option B / proposal 0300'),
('用例优先级',   'biz_testcase_priority', '0', 'admin', SYSDATE(), 'P0-P2'),
('用例状态',     'biz_testcase_status',   '0', 'admin', SYSDATE(), '5 状态机');

-- category (8 值字符串编码,ADR-B Option B)
-- 注:旧 dict_value '06'/'07' (E2E/烟雾) 转入 tags 字段承载,不再作分类
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '功能',     'functional',    'biz_testcase_category', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '默认值;原型 + SQL 共有'),
(2, '边界',     'boundary',      'biz_testcase_category', '', 'warning', 'N', '0', 'admin', SYSDATE(), '原型独有 — 边界测试'),
(3, '异常',     'exception',     'biz_testcase_category', '', 'danger',  'N', '0', 'admin', SYSDATE(), '原型独有 — 异常场景'),
(4, '农业专项', 'agri',          'biz_testcase_category', '', 'success', 'N', '0', 'admin', SYSDATE(), 'AgriPLM 业务场景专项 — 灌溉/植保/IoT(PRD §F4.2)'),
(5, '接口',     'api',           'biz_testcase_category', '', 'info',    'N', '0', 'admin', SYSDATE(), '接口测试'),
(6, '性能',     'performance',   'biz_testcase_category', '', 'warning', 'N', '0', 'admin', SYSDATE(), '性能 / 负载'),
(7, '安全',     'security',      'biz_testcase_category', '', 'danger',  'N', '0', 'admin', SYSDATE(), '安全 / 渗透'),
(8, '兼容性',   'compatibility', 'biz_testcase_category', '', 'info',    'N', '0', 'admin', SYSDATE(), '浏览器 / 设备 / 系统版本兼容');

-- priority (3)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'P0 关键', '00', 'biz_testcase_priority', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(2, 'P1 主要', '01', 'biz_testcase_priority', '', 'warning', 'Y', '0', 'admin', SYSDATE(), ''),
(3, 'P2 次要', '02', 'biz_testcase_priority', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

-- status (5)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',    '00', 'biz_testcase_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '待执行',  '01', 'biz_testcase_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '执行中',  '02', 'biz_testcase_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已通过',  '03', 'biz_testcase_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),
(5, '已失败',  '04', 'biz_testcase_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

-- 菜单 2060-2067
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2060, '测试用例', 2000, 7, 'testcase', 'business/testcase/index', 'C', '0', '0', 'business:testcase:list',    'star',    'admin', SYSDATE(), '测试用例管理'),
(2061, '用例查询', 2060, 1, '#', '', 'F', '0', '0', 'business:testcase:query',   '#', 'admin', SYSDATE(), ''),
(2062, '用例新增', 2060, 2, '#', '', 'F', '0', '0', 'business:testcase:add',     '#', 'admin', SYSDATE(), ''),
(2063, '用例修改', 2060, 3, '#', '', 'F', '0', '0', 'business:testcase:edit',    '#', 'admin', SYSDATE(), ''),
(2064, '用例删除', 2060, 4, '#', '', 'F', '0', '0', 'business:testcase:remove',  '#', 'admin', SYSDATE(), ''),
(2065, '用例导出', 2060, 5, '#', '', 'F', '0', '0', 'business:testcase:export',  '#', 'admin', SYSDATE(), ''),
(2066, '用例执行', 2060, 6, '#', '', 'F', '0', '0', 'business:testcase:execute', '#', 'admin', SYSDATE(), '/execute 端点权限'),
(2067, '用例指派', 2060, 7, '#', '', 'F', '0', '0', 'business:testcase:assign',  '#', 'admin', SYSDATE(), 'v0.4 备用');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2060), (1, 2061), (1, 2062), (1, 2063), (1, 2064), (1, 2065), (1, 2066), (1, 2067);

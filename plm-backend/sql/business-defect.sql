-- =============================================================================
-- Defect (缺陷) 业务模块 — 数据库 DDL + 菜单 + 权限 + 字典
-- 关联: 02-设计/Defect-数据库设计.md / ADR-0005 (DEFECT-YYYY-NNNN)
-- =============================================================================

-- 1. 业务表
DROP TABLE IF EXISTS tb_defect;
CREATE TABLE tb_defect (
    defect_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    defect_no          VARCHAR(32)   NOT NULL                 COMMENT '编号 DEFECT-YYYY-NNNN（ADR-0005）',
    project_id         BIGINT(20)    NOT NULL                 COMMENT '所属项目 FK→tb_project.id',
    sprint_id          BIGINT(20)    DEFAULT NULL             COMMENT '关联迭代（可空）',
    task_id            BIGINT(20)    DEFAULT NULL             COMMENT '关联任务（可空）',
    title              VARCHAR(200)  NOT NULL                 COMMENT '缺陷标题',
    description        TEXT                                   COMMENT '详细描述',
    severity           VARCHAR(20)   NOT NULL DEFAULT '02'    COMMENT '严重级别（biz_defect_severity）',
    category           VARCHAR(20)   NOT NULL DEFAULT '01'    COMMENT '缺陷分类（biz_defect_category）',
    status             VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT '状态（biz_defect_status）',
    assignee_user_id   BIGINT(20)    DEFAULT NULL             COMMENT '指派开发',
    reporter_user_id   BIGINT(20)    NOT NULL                 COMMENT '报告人 FK→sys_user',
    reproduce_steps    TEXT                                   COMMENT '重现步骤',
    expected_result    TEXT                                   COMMENT '期望结果',
    actual_result      TEXT                                   COMMENT '实际结果',
    resolution         VARCHAR(500)  DEFAULT NULL             COMMENT '解决说明（推 03 时填）',
    tags               VARCHAR(200)  DEFAULT NULL             COMMENT '标签 CSV（如 regression,flaky）',
    create_by          VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME      DEFAULT NULL             COMMENT '创建时间',
    update_by          VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME      DEFAULT NULL             COMMENT '更新时间',
    remark             VARCHAR(500)  DEFAULT ''               COMMENT '备注',
    del_flag           CHAR(1)       DEFAULT '0'              COMMENT '0=正常 2=删除',
    PRIMARY KEY (defect_id),
    UNIQUE KEY uk_defect_no (defect_no),
    KEY idx_defect_project (project_id),
    KEY idx_defect_sprint (sprint_id),
    KEY idx_defect_assignee (assignee_user_id),
    KEY idx_defect_status_severity (status, severity)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='缺陷（Defect）';

-- 2. 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('缺陷严重级别', 'biz_defect_severity', '0', 'admin', SYSDATE(), 'P0-P3'),
('缺陷分类',     'biz_defect_category', '0', 'admin', SYSDATE(), ''),
('缺陷状态',     'biz_defect_status',   '0', 'admin', SYSDATE(), '5 状态机');

-- 3. 字典数据
-- severity
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'P0 阻塞', '00', 'biz_defect_severity', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(2, 'P1 严重', '01', 'biz_defect_severity', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'P2 一般', '02', 'biz_defect_severity', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(4, 'P3 轻微', '03', 'biz_defect_severity', '', 'success', 'N', '0', 'admin', SYSDATE(), '');

-- category
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '功能',    '01', 'biz_defect_category', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '性能',    '02', 'biz_defect_category', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '兼容性',  '03', 'biz_defect_category', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(4, '安全',    '04', 'biz_defect_category', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(5, '易用性',  '05', 'biz_defect_category', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(99, '其他',   '99', 'biz_defect_category', '', '',        'N', '0', 'admin', SYSDATE(), '');

-- status (5×5 状态机)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '新建',    '00', 'biz_defect_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已确认',  '01', 'biz_defect_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '处理中',  '02', 'biz_defect_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已解决',  '03', 'biz_defect_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(5, '已关闭',  '04', 'biz_defect_status', '', '',        'N', '0', 'admin', SYSDATE(), '终态');

-- 4. 菜单（2050-2056）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2050, '缺陷管理', 2000, 6, 'defect', 'business/defect/index', 'C', '0', '0', 'business:defect:list',   'bug',  'admin', SYSDATE(), '缺陷管理菜单'),
(2051, '缺陷查询', 2050, 1, '#', '', 'F', '0', '0', 'business:defect:query',  '#', 'admin', SYSDATE(), ''),
(2052, '缺陷新增', 2050, 2, '#', '', 'F', '0', '0', 'business:defect:add',    '#', 'admin', SYSDATE(), ''),
(2053, '缺陷修改', 2050, 3, '#', '', 'F', '0', '0', 'business:defect:edit',   '#', 'admin', SYSDATE(), ''),
(2054, '缺陷删除', 2050, 4, '#', '', 'F', '0', '0', 'business:defect:remove', '#', 'admin', SYSDATE(), ''),
(2055, '缺陷导出', 2050, 5, '#', '', 'F', '0', '0', 'business:defect:export', '#', 'admin', SYSDATE(), ''),
(2056, '缺陷指派', 2050, 6, '#', '', 'F', '0', '0', 'business:defect:assign', '#', 'admin', SYSDATE(), '指派权限独立');

-- admin 角色全量授权
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2050), (1, 2051), (1, 2052), (1, 2053), (1, 2054), (1, 2055), (1, 2056);

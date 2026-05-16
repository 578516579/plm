-- =============================================================================
-- Requirement (需求) 业务模块 — 数据库 DDL + 菜单 + 权限 + 字典
-- 关联：02-设计/Requirement-数据库设计.md / ADR-0002 (REQ-YYYY-NNNN 编号规则)
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-requirement.sql
-- 回滚：sql/business-requirement-rollback.sql
-- =============================================================================

-- ----------------------------
-- 1. 业务表
-- ----------------------------
DROP TABLE IF EXISTS tb_requirement;
CREATE TABLE tb_requirement (
    requirement_id    BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    requirement_no    VARCHAR(32)   NOT NULL                 COMMENT '需求编号 REQ-YYYY-NNNN（ADR-0002）',
    project_id        BIGINT(20)    NOT NULL                 COMMENT '所属项目 FK→tb_project.id',
    title             VARCHAR(200)  NOT NULL                 COMMENT '需求标题',
    description       TEXT                                   COMMENT '详细描述（Markdown 兼容）',
    source            VARCHAR(2)    NOT NULL DEFAULT '01'    COMMENT '需求来源（字典 biz_req_source）',
    priority          VARCHAR(2)    NOT NULL DEFAULT '02'    COMMENT '优先级（字典 biz_req_priority）',
    status            VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT '状态（字典 biz_req_status）',
    assignee_user_id  BIGINT(20)    DEFAULT NULL             COMMENT '指派给的用户 FK→sys_user.user_id',
    review_note       VARCHAR(500)  DEFAULT NULL             COMMENT '评审简要纪要（状态推进时填）',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME      DEFAULT NULL             COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME      DEFAULT NULL             COMMENT '更新时间',
    remark            VARCHAR(500)  DEFAULT ''               COMMENT '备注',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '0=正常 2=删除',
    PRIMARY KEY (requirement_id),
    UNIQUE KEY uk_requirement_no (requirement_no),
    KEY idx_requirement_project (project_id),
    KEY idx_requirement_status (status),
    KEY idx_requirement_priority_status (priority, status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='需求（Requirement）';

-- ----------------------------
-- 2. 字典类型（3 个）
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('需求来源',   'biz_req_source',   '0', 'admin', SYSDATE(), '需求来源分类'),
('需求优先级', 'biz_req_priority', '0', 'admin', SYSDATE(), '需求优先级 P0/P1/P2'),
('需求状态',   'biz_req_status',   '0', 'admin', SYSDATE(), '需求生命周期状态');

-- ----------------------------
-- 3. 字典数据（4 + 3 + 4 = 11 条）
-- ----------------------------
-- 需求来源
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '客户反馈', '01', 'biz_req_source',   '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '内部提案', '02', 'biz_req_source',   '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(3, '运营数据', '03', 'biz_req_source',   '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '竞品分析', '04', 'biz_req_source',   '', 'success', 'N', '0', 'admin', SYSDATE(), '');

-- 优先级（P0/P1/P2）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'P0 紧急', '00', 'biz_req_priority', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(2, 'P1 重要', '01', 'biz_req_priority', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'P2 一般', '02', 'biz_req_priority', '', 'info',    'Y', '0', 'admin', SYSDATE(), '');

-- 状态（与 PRD §3.3 4×4 状态机一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待评审', '00', 'biz_req_status',   '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '开发中', '01', 'biz_req_status',   '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已完成', '02', 'biz_req_status',   '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),
(4, '已取消', '03', 'biz_req_status',   '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

-- ----------------------------
-- 4. 菜单与权限（菜单 ID 2020-2025，挂在业务管理 2000 下）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path,    component,                    menu_type, visible, status, perms,                            icon,    create_by, create_time, remark) VALUES
(2020, '需求管理', 2000, 2, 'requirement',    'business/requirement/index',     'C', '0', '0', 'business:requirement:list',   'edit',  'admin', SYSDATE(), '需求管理菜单'),
(2021, '需求查询', 2020, 1, '#',              '',                                'F', '0', '0', 'business:requirement:query',  '#',     'admin', SYSDATE(), ''),
(2022, '需求新增', 2020, 2, '#',              '',                                'F', '0', '0', 'business:requirement:add',    '#',     'admin', SYSDATE(), ''),
(2023, '需求修改', 2020, 3, '#',              '',                                'F', '0', '0', 'business:requirement:edit',   '#',     'admin', SYSDATE(), ''),
(2024, '需求删除', 2020, 4, '#',              '',                                'F', '0', '0', 'business:requirement:remove', '#',     'admin', SYSDATE(), ''),
(2025, '需求导出', 2020, 5, '#',              '',                                'F', '0', '0', 'business:requirement:export', '#',     'admin', SYSDATE(), '');

-- ----------------------------
-- 5. admin (role_id=1) 授权
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2020), (1, 2021), (1, 2022), (1, 2023), (1, 2024), (1, 2025);

-- =============================================================================
-- Sprint (迭代) 业务模块 — 数据库 DDL + 菜单 + 权限 + 字典
-- 关联：02-设计/Sprint-数据库设计.md / ADR-0004 (SPR-YYYY-NNNN 编号规则)
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-sprint.sql
-- 回滚：sql/business-sprint-rollback.sql
-- =============================================================================

-- ----------------------------
-- 1. 业务表
-- ----------------------------
DROP TABLE IF EXISTS tb_sprint;
CREATE TABLE tb_sprint (
    sprint_id            BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    sprint_no            VARCHAR(32)   NOT NULL                 COMMENT '迭代编号 SPR-YYYY-NNNN（ADR-0004）',
    project_id           BIGINT(20)    NOT NULL                 COMMENT '所属项目 FK→tb_project.id',
    name                 VARCHAR(100)  NOT NULL                 COMMENT '迭代名称（如 Sprint 26W21）',
    goal                 VARCHAR(500)  DEFAULT NULL             COMMENT '迭代目标（一句话）',
    status               VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT '状态（字典 biz_sprint_status）',
    planned_start_date   DATE          NOT NULL                 COMMENT '计划开始日',
    planned_end_date     DATE          NOT NULL                 COMMENT '计划结束日',
    actual_start_date    DATE          DEFAULT NULL             COMMENT '实际开始（00→01 时自动填）',
    actual_end_date      DATE          DEFAULT NULL             COMMENT '实际结束（01→02 时自动填）',
    duration_days        INT           DEFAULT 14               COMMENT '周期天数（冗余，便于查询）',
    create_by            VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time          DATETIME      DEFAULT NULL             COMMENT '创建时间',
    update_by            VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time          DATETIME      DEFAULT NULL             COMMENT '更新时间',
    remark               VARCHAR(500)  DEFAULT ''               COMMENT '备注',
    del_flag             CHAR(1)       DEFAULT '0'              COMMENT '0=正常 2=删除',
    PRIMARY KEY (sprint_id),
    UNIQUE KEY uk_sprint_no (sprint_no),
    KEY idx_sprint_project_status (project_id, status),
    KEY idx_sprint_planned_dates (planned_start_date, planned_end_date)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='迭代（Sprint）';

-- ----------------------------
-- 2. 字典类型（1 个）
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('迭代状态', 'biz_sprint_status', '0', 'admin', SYSDATE(), '迭代 4 状态机');

-- ----------------------------
-- 3. 字典数据（4 条）
-- ----------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '计划中', '00', 'biz_sprint_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '进行中', '01', 'biz_sprint_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已完成', '02', 'biz_sprint_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),
(4, '已取消', '03', 'biz_sprint_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

-- ----------------------------
-- 4. 菜单与权限（菜单 ID 2040-2046）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path,    component,                    menu_type, visible, status, perms,                          icon,    create_by, create_time, remark) VALUES
(2040, '迭代管理', 2000, 4, 'sprint',         'business/sprint/index',          'C', '0', '0', 'business:sprint:list',     'time',  'admin', SYSDATE(), '迭代管理菜单'),
(2041, '迭代查询', 2040, 1, '#',              '',                                'F', '0', '0', 'business:sprint:query',    '#',     'admin', SYSDATE(), ''),
(2042, '迭代新增', 2040, 2, '#',              '',                                'F', '0', '0', 'business:sprint:add',      '#',     'admin', SYSDATE(), ''),
(2043, '迭代修改', 2040, 3, '#',              '',                                'F', '0', '0', 'business:sprint:edit',     '#',     'admin', SYSDATE(), ''),
(2044, '迭代删除', 2040, 4, '#',              '',                                'F', '0', '0', 'business:sprint:remove',   '#',     'admin', SYSDATE(), ''),
(2045, '迭代导出', 2040, 5, '#',              '',                                'F', '0', '0', 'business:sprint:export',   '#',     'admin', SYSDATE(), ''),
(2046, '迭代统计', 2040, 6, '#',              '',                                'F', '0', '0', 'business:sprint:stats',    '#',     'admin', SYSDATE(), '健康度统计（S-009）');

-- ----------------------------
-- 5. admin 角色全量授权
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2040), (1, 2041), (1, 2042), (1, 2043), (1, 2044), (1, 2045), (1, 2046);

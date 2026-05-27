-- =============================================================================
-- Task (任务) 业务模块 — 数据库 DDL + 菜单 + 权限 + 字典
-- 关联：02-设计/Task-数据库设计.md / ADR-0003 (TASK-YYYY-NNNN 编号规则)
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-task.sql
-- 回滚：sql/business-task-rollback.sql
-- =============================================================================

-- ----------------------------
-- 1. 业务表
-- ----------------------------
DROP TABLE IF EXISTS tb_task;
CREATE TABLE tb_task (
    task_id           BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    task_no           VARCHAR(32)   NOT NULL                 COMMENT '任务编号 TASK-YYYY-NNNN（ADR-0003）',
    project_id        BIGINT(20)    NOT NULL                 COMMENT '所属项目 FK→tb_project.id',
    requirement_id    BIGINT(20)    DEFAULT NULL             COMMENT '关联需求 FK→tb_requirement（可空）',
    sprint_id         BIGINT(20)    DEFAULT NULL             COMMENT '关联迭代 FK→tb_sprint（可空）',
    title             VARCHAR(200)  NOT NULL                 COMMENT '任务标题',
    description       TEXT                                   COMMENT '详细描述',
    status            VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT '状态（字典 biz_task_status）',
    priority          VARCHAR(20)   NOT NULL DEFAULT '02'    COMMENT '优先级（字典 biz_task_priority）',
    assignee_user_id  BIGINT(20)    DEFAULT NULL             COMMENT '负责人 FK→sys_user.user_id',
    estimated_hours   DECIMAL(5, 1) DEFAULT NULL             COMMENT '预估工时（小时）',
    actual_hours      DECIMAL(5, 1) DEFAULT NULL             COMMENT '实际工时（进入「已完成」时填）',
    mr_url            VARCHAR(500)  DEFAULT NULL             COMMENT '关联 MR/PR 链接',
    mr_branch         VARCHAR(100)  DEFAULT NULL             COMMENT 'MR 分支名',
    create_by         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time       DATETIME      DEFAULT NULL             COMMENT '创建时间',
    update_by         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time       DATETIME      DEFAULT NULL             COMMENT '更新时间',
    remark            VARCHAR(500)  DEFAULT ''               COMMENT '备注',
    del_flag          CHAR(1)       DEFAULT '0'              COMMENT '0=正常 2=删除',
    PRIMARY KEY (task_id),
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_task_project (project_id),
    KEY idx_task_requirement (requirement_id),
    KEY idx_task_sprint (sprint_id),
    KEY idx_task_assignee (assignee_user_id),
    KEY idx_task_status_priority (status, priority)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='任务（Task）';

-- ----------------------------
-- 2. 字典类型（2 个）
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('任务状态',   'biz_task_status',   '0', 'admin', SYSDATE(), '任务 6 状态机'),
('任务优先级', 'biz_task_priority', '0', 'admin', SYSDATE(), '任务优先级 P0/P1/P2');

-- ----------------------------
-- 3. 字典数据（6 + 3 = 9 条）
-- ----------------------------
-- 状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待开发',   '00', 'biz_task_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '开发中',   '01', 'biz_task_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(3, '代码评审', '02', 'biz_task_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '测试中',   '03', 'biz_task_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(5, '已完成',   '04', 'biz_task_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),
(6, '已取消',   '05', 'biz_task_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

-- 优先级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'P0 紧急', '00', 'biz_task_priority', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(2, 'P1 重要', '01', 'biz_task_priority', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'P2 一般', '02', 'biz_task_priority', '', 'info',    'Y', '0', 'admin', SYSDATE(), '');

-- ----------------------------
-- 4. 菜单与权限（菜单 ID 2030-2037）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path,    component,                    menu_type, visible, status, perms,                          icon,     create_by, create_time, remark) VALUES
(2030, '任务管理', 2000, 3, 'task',           'business/task/index',            'C', '0', '0', 'business:task:list',       'list',   'admin', SYSDATE(), '任务管理菜单'),
(2031, '任务查询', 2030, 1, '#',              '',                                'F', '0', '0', 'business:task:query',      '#',      'admin', SYSDATE(), ''),
(2032, '任务新增', 2030, 2, '#',              '',                                'F', '0', '0', 'business:task:add',        '#',      'admin', SYSDATE(), ''),
(2033, '任务修改', 2030, 3, '#',              '',                                'F', '0', '0', 'business:task:edit',       '#',      'admin', SYSDATE(), ''),
(2034, '任务删除', 2030, 4, '#',              '',                                'F', '0', '0', 'business:task:remove',     '#',      'admin', SYSDATE(), ''),
(2035, '任务导出', 2030, 5, '#',              '',                                'F', '0', '0', 'business:task:export',     '#',      'admin', SYSDATE(), ''),
(2036, '任务看板', 2000, 5, 'taskkanban',     'business/task/kanban',           'C', '0', '0', 'business:task:kanban',     'tree',   'admin', SYSDATE(), '看板视图（只读）'),
(2037, '我的任务', 0,    8, 'mytask',         'business/task/my',               'C', '0', '0', 'business:task:list',       'people', 'admin', SYSDATE(), '一级菜单，顶级显示');

-- ----------------------------
-- 5. admin 角色全量授权
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2030), (1, 2031), (1, 2032), (1, 2033), (1, 2034), (1, 2035), (1, 2036), (1, 2037);

-- =============================================================================
-- Project (项目) 业务模块 — 数据库 DDL + 菜单 + 权限 + 字典
-- 关联：02-设计/Project-数据库设计.md / 03-开发/ADR/0001-project-no-rule.md
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-project.sql
-- 回滚：sql/business-project-rollback.sql
-- =============================================================================

-- ----------------------------
-- 1. 业务表
-- ----------------------------
DROP TABLE IF EXISTS tb_project;
CREATE TABLE tb_project (
    id                BIGINT(20)     NOT NULL AUTO_INCREMENT      COMMENT '主键',
    project_no        VARCHAR(64)    NOT NULL                     COMMENT '项目编号 PRJ-YYYY-NNNN（ADR-0001）',
    project_name      VARCHAR(128)   NOT NULL                     COMMENT '项目名称',
    project_type      VARCHAR(32)    DEFAULT ''                   COMMENT '项目类型（biz_project_type）',
    status            CHAR(1)        DEFAULT '0'                  COMMENT '状态（biz_project_status）',
    manager_user_id   BIGINT(20)     DEFAULT NULL                 COMMENT '负责人 user_id',
    start_date        DATE           DEFAULT NULL                 COMMENT '起始日期',
    end_date          DATE           DEFAULT NULL                 COMMENT '结束日期',
    budget            DECIMAL(18, 2) DEFAULT NULL                 COMMENT '预算（万元）',
    description       VARCHAR(1000)  DEFAULT ''                   COMMENT '项目描述',
    create_by         VARCHAR(64)    DEFAULT ''                   COMMENT '创建者',
    create_time       DATETIME       DEFAULT NULL                 COMMENT '创建时间',
    update_by         VARCHAR(64)    DEFAULT ''                   COMMENT '更新者',
    update_time       DATETIME       DEFAULT NULL                 COMMENT '更新时间',
    remark            VARCHAR(500)   DEFAULT ''                   COMMENT '备注',
    del_flag          CHAR(1)        DEFAULT '0'                  COMMENT '删除标志（0=正常 2=删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_no (project_no),
    KEY idx_project_status (status),
    KEY idx_project_manager (manager_user_id),
    KEY idx_project_create_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='项目（Project）';

-- ----------------------------
-- 2. 字典类型
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('项目类型', 'biz_project_type',   '0', 'admin', SYSDATE(), '项目分类'),
('项目状态', 'biz_project_status', '0', 'admin', SYSDATE(), '项目生命周期状态');

-- ----------------------------
-- 3. 字典数据
-- ----------------------------
-- 项目类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '研发', 'rnd',     'biz_project_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), '研发类项目'),
(2, '改造', 'upgrade', 'biz_project_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '改造类项目'),
(3, '运维', 'ops',     'biz_project_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '运维类项目');

-- 项目状态（与 PRD §3.3 状态机一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '未启动', '0', 'biz_project_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '进行中', '1', 'biz_project_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(3, '暂停',   '2', 'biz_project_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已完成', '3', 'biz_project_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(5, '已取消', '4', 'biz_project_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '');

-- ----------------------------
-- 4. 菜单与权限（菜单 ID 从 2000 起）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path,    component,                menu_type, visible, status, perms,                            icon,         create_by, create_time, remark) VALUES
(2000, '业务管理', 0,    5, 'business',           NULL,                         'M', '0', '0', '',                            'component', 'admin', SYSDATE(), '业务管理目录'),
(2010, '项目管理', 2000, 1, 'project',            'business/project/index',     'C', '0', '0', 'business:project:list',       'tree-table', 'admin', SYSDATE(), '项目管理菜单'),
(2011, '项目查询', 2010, 1, '#',                  '',                           'F', '0', '0', 'business:project:query',      '#',          'admin', SYSDATE(), ''),
(2012, '项目新增', 2010, 2, '#',                  '',                           'F', '0', '0', 'business:project:add',        '#',          'admin', SYSDATE(), ''),
(2013, '项目修改', 2010, 3, '#',                  '',                           'F', '0', '0', 'business:project:edit',       '#',          'admin', SYSDATE(), ''),
(2014, '项目删除', 2010, 4, '#',                  '',                           'F', '0', '0', 'business:project:remove',     '#',          'admin', SYSDATE(), ''),
(2015, '项目导出', 2010, 5, '#',                  '',                           'F', '0', '0', 'business:project:export',     '#',          'admin', SYSDATE(), '');

-- ----------------------------
-- 5. admin (role_id=1) 授权
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2000), (1, 2010), (1, 2011), (1, 2012), (1, 2013), (1, 2014), (1, 2015);

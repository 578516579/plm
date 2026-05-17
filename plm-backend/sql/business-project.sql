-- =============================================================================
-- Project (项目) 业务模块 — 数据库 DDL + 菜单 + 权限 + 字典
-- 关联：
--   - PRD-MAPPING.md §2 "Project (F1.2)" 字段对照表 (commit 20b5bb6)
--   - ADR-0001 (PRJ-YYYY-NNNN 编号规则)
--   - 02-设计/Project-数据库设计.md (若存在,需同步)
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-project.sql
-- 回滚：sql/business-project-rollback.sql
-- =============================================================================
-- v2 (2026-05-17) PRD-align:
--   + 加字段: business_line / priority / lifecycle_phase / progress / health
--   - 删字段: budget (PRD/原型未要求,脚手架样板)
--   ~ status: CHAR(1) 0/1/2/3/4 → VARCHAR(2) 00/01/02/03 (4 态,删除"未启动",该语义由 lifecycle_phase=00 表达)
--   + 新字典: biz_project_business_line / biz_project_priority / biz_project_phase / biz_project_health
-- =============================================================================

-- ----------------------------
-- 1. 业务表
-- ----------------------------
DROP TABLE IF EXISTS tb_project;
CREATE TABLE tb_project (
    id                BIGINT(20)     NOT NULL AUTO_INCREMENT      COMMENT '主键',
    project_no        VARCHAR(64)    NOT NULL                     COMMENT '项目编号 PRJ-YYYY-NNNN(ADR-0001)',
    project_name      VARCHAR(200)   NOT NULL                     COMMENT '项目名称(原型 np-name)',
    business_line     VARCHAR(20)    NOT NULL                     COMMENT '业务线(biz_project_business_line,原型 np-biz,PRD §F1.2 必填)',
    project_type      VARCHAR(20)    DEFAULT NULL                 COMMENT '项目类型(biz_project_type,PRD §F1.2 "类型")',
    priority          VARCHAR(8)     DEFAULT NULL                 COMMENT '优先级(biz_project_priority,PRD §F1.2 "优先级")',
    lifecycle_phase   VARCHAR(2)     NOT NULL DEFAULT '00'        COMMENT '交付阶段(biz_project_phase,原型列"阶段":00规划/01研发/02测试/03验收)',
    status            VARCHAR(2)     NOT NULL DEFAULT '00'        COMMENT '总状态(biz_project_status:00进行中/01暂停/02已完成/03已取消)',
    progress          INT            DEFAULT NULL                 COMMENT '进度 0-100(原型列"进度")',
    health            VARCHAR(8)     DEFAULT NULL                 COMMENT '健康度(biz_project_health:green/amber/red,PRD §F1.2 三色预警)',
    manager_user_id   BIGINT(20)     DEFAULT NULL                 COMMENT '负责人 user_id(原型 np-owner 自由文本→FK,见 PRD-MAPPING §2 D1)',
    start_date        DATE           DEFAULT NULL                 COMMENT '起始日期(原型 np-start)',
    end_date          DATE           DEFAULT NULL                 COMMENT '结束日期(原型 np-end)',
    description       TEXT                                        COMMENT '项目描述(扩展字段,PRD-MAPPING §2 D3)',
    create_by         VARCHAR(64)    DEFAULT ''                   COMMENT '创建者',
    create_time       DATETIME       DEFAULT NULL                 COMMENT '创建时间',
    update_by         VARCHAR(64)    DEFAULT ''                   COMMENT '更新者',
    update_time       DATETIME       DEFAULT NULL                 COMMENT '更新时间',
    remark            VARCHAR(500)   DEFAULT ''                   COMMENT '备注',
    del_flag          CHAR(1)        DEFAULT '0'                  COMMENT '删除标志(0=正常 2=删除)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_no (project_no),
    KEY idx_project_status (status),
    KEY idx_project_phase (lifecycle_phase),
    KEY idx_project_business_line (business_line),
    KEY idx_project_manager (manager_user_id),
    KEY idx_project_create_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='项目(Project)';

-- ----------------------------
-- 2. 字典类型
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('项目类型',   'biz_project_type',          '0', 'admin', SYSDATE(), '项目分类(PRD §F1.2)'),
('项目状态',   'biz_project_status',        '0', 'admin', SYSDATE(), '项目总状态机(4 态)'),
('项目业务线', 'biz_project_business_line', '0', 'admin', SYSDATE(), 'AgriPLM 业务线(PRD §F1.2,与 Inception 4 值对齐)'),
('项目优先级', 'biz_project_priority',      '0', 'admin', SYSDATE(), '项目优先级(PRD §F1.2)'),
('项目阶段',   'biz_project_phase',         '0', 'admin', SYSDATE(), '项目交付阶段(原型列"阶段")'),
('项目健康度', 'biz_project_health',        '0', 'admin', SYSDATE(), '项目健康度三色预警(PRD §F1.2)');

-- ----------------------------
-- 3. 字典数据
-- ----------------------------
-- 项目类型(沿用旧 3 值)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '研发', 'rnd',     'biz_project_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), '研发类项目'),
(2, '改造', 'upgrade', 'biz_project_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '改造类项目'),
(3, '运维', 'ops',     'biz_project_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '运维类项目');

-- 项目状态(4 态,与 PRD-MAPPING §3 状态机 1 一致)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '进行中', '00', 'biz_project_status', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '默认初始状态'),
(2, '暂停',   '01', 'biz_project_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已完成', '02', 'biz_project_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态'),
(4, '已取消', '03', 'biz_project_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

-- 项目业务线(与 Inception biz_inception_biz_line 4 值对齐,转项目时复制)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '植保服务', 'plant_protection', 'biz_project_business_line', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(2, '精准农业', 'precision_agri',   'biz_project_business_line', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '农资流通', 'agri_supply',      'biz_project_business_line', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(4, '质量溯源', 'quality_trace',    'biz_project_business_line', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

-- 项目优先级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'P0 紧急', 'P0', 'biz_project_priority', '', 'danger',  'N', '0', 'admin', SYSDATE(), '最高优先级'),
(2, 'P1 重要', 'P1', 'biz_project_priority', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'P2 一般', 'P2', 'biz_project_priority', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, 'P3 低',   'P3', 'biz_project_priority', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

-- 项目阶段(交付推进 4 阶段)
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '规划中', '00', 'biz_project_phase', '', 'info',    'Y', '0', 'admin', SYSDATE(), '默认起始阶段'),
(2, '研发中', '01', 'biz_project_phase', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(3, '测试中', '02', 'biz_project_phase', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '验收中', '03', 'biz_project_phase', '', 'success', 'N', '0', 'admin', SYSDATE(), '');

-- 项目健康度
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '健康', 'green', 'biz_project_health', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(2, '注意', 'amber', 'biz_project_health', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '风险', 'red',   'biz_project_health', '', 'danger',  'N', '0', 'admin', SYSDATE(), '');

-- ----------------------------
-- 4. 菜单与权限(菜单 ID 从 2000 起)
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

-- =============================================================================
-- 自动化测试 (AutoTest) — PRD §F4.5 + 原型 autotest.html
-- AI辅助生成测试脚本，支持 pytest/JUnit/Playwright/Cypress 框架
-- 菜单 ID 段: 2700-2706
-- =============================================================================
DROP TABLE IF EXISTS tb_autotest;
CREATE TABLE tb_autotest (
    autotest_id         BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    autotest_no         VARCHAR(32)   NOT NULL                 COMMENT '编号 ATS-YYYY-NNNN',
    project_id          BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    suite_name          VARCHAR(200)  NOT NULL                 COMMENT '套件名称',
    description         VARCHAR(500)  DEFAULT ''               COMMENT '描述',
    framework           VARCHAR(30)   DEFAULT 'pytest'         COMMENT 'biz_autotest_framework',
    target_module       VARCHAR(100)  DEFAULT ''               COMMENT '目标模块',
    script_content      LONGTEXT                               COMMENT '测试脚本内容',
    ai_generated        CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI生成',
    last_run_at         DATETIME      DEFAULT NULL             COMMENT '最近执行时间',
    last_run_pass_rate  DECIMAL(5,2)  DEFAULT NULL             COMMENT '最近通过率%',
    last_run_duration   VARCHAR(20)   DEFAULT NULL             COMMENT '最近执行时长',
    failed_case_count   INT           DEFAULT 0                COMMENT '失败用例数',
    status              VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_autotest_status 5状态',
    author_user_id      BIGINT(20)    NOT NULL                 COMMENT '创建人',
    create_by           VARCHAR(64)   DEFAULT '',
    create_time         DATETIME      DEFAULT NULL,
    update_by           VARCHAR(64)   DEFAULT '',
    update_time         DATETIME      DEFAULT NULL,
    remark              VARCHAR(500)  DEFAULT '',
    del_flag            CHAR(1)       DEFAULT '0',
    PRIMARY KEY (autotest_id),
    UNIQUE KEY uk_autotest_no (autotest_no),
    KEY idx_autotest_project (project_id),
    KEY idx_autotest_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自动化测试套件';

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('自动化测试框架', 'biz_autotest_framework', '0', 'admin', SYSDATE(), '4 框架'),
('自动化测试状态', 'biz_autotest_status',    '0', 'admin', SYSDATE(), '5 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'Pytest',      'pytest',      'biz_autotest_framework', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, 'JUnit',       'junit',       'biz_autotest_framework', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, 'Playwright',  'playwright',  'biz_autotest_framework', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, 'Cypress',     'cypress',     'biz_autotest_framework', '', 'info',    'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '草稿',   '00', 'biz_autotest_status', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '待执行', '01', 'biz_autotest_status', '', 'primary', 'N', '0', 'admin', SYSDATE()),
(3, '执行中', '02', 'biz_autotest_status', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, '已完成', '03', 'biz_autotest_status', '', 'success', 'N', '0', 'admin', SYSDATE()),
(5, '已归档', '04', 'biz_autotest_status', '', 'danger',  'N', '0', 'admin', SYSDATE());

-- 菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2700, '自动化测试', 2000, 22, 'autotest',    'business/autotest/index',    'C', '0', '0', 'business:autotest:list',   'aim',         'admin', SYSDATE(), 'PRD §F4.5'),
(2701, '测试查询',   2700, 1,  '#', '', 'F', '0', '0', 'business:autotest:query',  '#', 'admin', SYSDATE(), ''),
(2702, '测试新增',   2700, 2,  '#', '', 'F', '0', '0', 'business:autotest:add',    '#', 'admin', SYSDATE(), ''),
(2703, '测试修改',   2700, 3,  '#', '', 'F', '0', '0', 'business:autotest:edit',   '#', 'admin', SYSDATE(), ''),
(2704, '测试删除',   2700, 4,  '#', '', 'F', '0', '0', 'business:autotest:remove', '#', 'admin', SYSDATE(), ''),
(2705, '测试导出',   2700, 5,  '#', '', 'F', '0', '0', 'business:autotest:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2700 AND 2705;

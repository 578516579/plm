-- =============================================================================
-- 功能开关 (FeatureFlag) — 原型 featureflag.html
-- 灰度发布/环境隔离/全量开关管理
-- 菜单 ID 段: 2770-2776
-- =============================================================================
DROP TABLE IF EXISTS tb_feature_flag;
CREATE TABLE tb_feature_flag (
    flag_id            BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    flag_key           VARCHAR(100)  NOT NULL                 COMMENT 'Flag唯一键 如 enable_ai_prd',
    flag_name          VARCHAR(200)  NOT NULL                 COMMENT 'Flag显示名称',
    description        VARCHAR(500)  DEFAULT ''               COMMENT '描述',
    environment        VARCHAR(20)   DEFAULT 'dev'            COMMENT 'biz_feature_flag_env',
    rollout_strategy   VARCHAR(30)   DEFAULT 'all_off'        COMMENT 'biz_feature_flag_strategy',
    rollout_percentage INT           DEFAULT 0                COMMENT '灰度百分比 0-100',
    user_whitelist     TEXT                                   COMMENT 'JSON 用户ID白名单',
    enabled            CHAR(1)       DEFAULT 'N'              COMMENT 'Y=开启 N=关闭',
    project_id         BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_project',
    author_user_id     BIGINT(20)    DEFAULT NULL,
    create_by          VARCHAR(64)   DEFAULT '',
    create_time        DATETIME      DEFAULT NULL,
    update_by          VARCHAR(64)   DEFAULT '',
    update_time        DATETIME      DEFAULT NULL,
    remark             VARCHAR(500)  DEFAULT '',
    del_flag           CHAR(1)       DEFAULT '0',
    PRIMARY KEY (flag_id),
    UNIQUE KEY uk_flag_key_env (flag_key, environment),
    KEY idx_feature_flag_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能开关管理';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('功能开关环境',     'biz_feature_flag_env',      '0', 'admin', SYSDATE(), '4 环境'),
('功能开关灰度策略', 'biz_feature_flag_strategy', '0', 'admin', SYSDATE(), '4 策略');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '开发环境', 'dev',  'biz_feature_flag_env', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '测试环境', 'test', 'biz_feature_flag_env', '', 'primary', 'N', '0', 'admin', SYSDATE()),
(3, '预发环境', 'pre',  'biz_feature_flag_env', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, '生产环境', 'prod', 'biz_feature_flag_env', '', 'danger',  'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '全量开启', 'all_on',    'biz_feature_flag_strategy', '', 'success', 'N', '0', 'admin', SYSDATE()),
(2, '全量关闭', 'all_off',   'biz_feature_flag_strategy', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(3, '灰度发布', 'percentage','biz_feature_flag_strategy', '', 'warning', 'N', '0', 'admin', SYSDATE()),
(4, '白名单',   'user_list', 'biz_feature_flag_strategy', '', 'primary', 'N', '0', 'admin', SYSDATE());

-- 种子数据
INSERT INTO tb_feature_flag (flag_key, flag_name, description, environment, rollout_strategy, rollout_percentage, enabled, create_by, create_time) VALUES
('enable_ai_prd',       'AI PRD生成',         '开启AI自动生成PRD功能',         'prod', 'percentage', 30,  'N', 'admin', SYSDATE()),
('enable_ai_testcase',  'AI用例生成',         '开启AI自动生成测试用例',         'prod', 'all_on',     100, 'Y', 'admin', SYSDATE()),
('enable_agrikb',       'AgriKB知识库',       '开启农业知识库增强功能',         'prod', 'all_on',     100, 'Y', 'admin', SYSDATE()),
('enable_dora_metrics', 'DORA效能指标',       '开启DORA效能自动采集',           'test', 'all_on',     100, 'N', 'admin', SYSDATE()),
('enable_mock_server',  'Mock服务器',         '开启接口Mock功能',               'dev',  'all_on',     100, 'Y', 'admin', SYSDATE());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2770, '功能开关',   2000, 30, 'feature-flag', 'business/feature-flag/index', 'C', '0', '0', 'business:feature-flag:list',   'flag',  'admin', SYSDATE(), ''),
(2771, '开关查询',   2770, 1,  '#', '', 'F', '0', '0', 'business:feature-flag:query',  '#', 'admin', SYSDATE(), ''),
(2772, '开关新增',   2770, 2,  '#', '', 'F', '0', '0', 'business:feature-flag:add',    '#', 'admin', SYSDATE(), ''),
(2773, '开关修改',   2770, 3,  '#', '', 'F', '0', '0', 'business:feature-flag:edit',   '#', 'admin', SYSDATE(), ''),
(2774, '开关删除',   2770, 4,  '#', '', 'F', '0', '0', 'business:feature-flag:remove', '#', 'admin', SYSDATE(), ''),
(2775, '开关导出',   2770, 5,  '#', '', 'F', '0', '0', 'business:feature-flag:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2770 AND 2775;

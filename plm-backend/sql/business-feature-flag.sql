-- =============================================================================
-- Feature Flag (FeatureFlag) — DevOps 扩展 + 原型 featureflag.html
-- 灰度发布 / 紧急开关 / 环境隔离
-- =============================================================================
DROP TABLE IF EXISTS tb_feature_flag;
CREATE TABLE tb_feature_flag (
    flag_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    flag_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 FF-YYYY-NNNN',
    flag_key             VARCHAR(120)  NOT NULL                 COMMENT 'Flag Key (snake_case 唯一)',
    title                VARCHAR(200)  NOT NULL                 COMMENT '功能说明',
    description          VARCHAR(500)                           COMMENT '详细描述',
    environment          VARCHAR(20)   NOT NULL                 COMMENT '字典 biz_ff_env: test/staging/prod',
    rollout_percentage   INT           NOT NULL DEFAULT 0       COMMENT '灰度百分比 0-100',
    rollout_strategy     VARCHAR(20)   NOT NULL DEFAULT 'all_off' COMMENT '字典 biz_ff_strategy: all_on/canary/all_off',
    target_user_segment  VARCHAR(500)                           COMMENT '目标用户分群 (CSV 用户ID 或表达式)',
    status               VARCHAR(2)    NOT NULL DEFAULT '01'    COMMENT 'biz_ff_status: 00 开启/01 关闭',
    author_user_id       BIGINT(20)    NOT NULL                 COMMENT '创建者',
    create_by            VARCHAR(64)   DEFAULT '',
    create_time          DATETIME      DEFAULT NULL,
    update_by            VARCHAR(64)   DEFAULT '',
    update_time          DATETIME      DEFAULT NULL,
    remark               VARCHAR(500)  DEFAULT '',
    del_flag             CHAR(1)       DEFAULT '0',
    PRIMARY KEY (flag_id),
    UNIQUE KEY uk_ff_no (flag_no),
    UNIQUE KEY uk_ff_key_env (flag_key, environment, del_flag),
    KEY idx_ff_env (environment),
    KEY idx_ff_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Feature Flag（FeatureFlag）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('Feature Flag 状态', 'biz_ff_status',   '0', 'admin', SYSDATE(), '开启/关闭'),
('Feature Flag 环境', 'biz_ff_env',      '0', 'admin', SYSDATE(), 'test/staging/prod'),
('Feature Flag 策略', 'biz_ff_strategy', '0', 'admin', SYSDATE(), '灰度策略 3 类');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '开启', '00', 'biz_ff_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(2, '关闭', '01', 'biz_ff_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),

(1, 'TEST',    'test',    'biz_ff_env', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'STAGING', 'staging', 'biz_ff_env', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'PROD',    'prod',    'biz_ff_env', '', 'danger',  'N', '0', 'admin', SYSDATE(), '生产'),

(1, '全量开启 (100%)', 'all_on',  'biz_ff_strategy', '', 'success', 'N', '0', 'admin', SYSDATE(), 'percent=100'),
(2, '灰度 (1-99%)',    'canary',  'biz_ff_strategy', '', 'warning', 'N', '0', 'admin', SYSDATE(), '按用户ID哈希'),
(3, '关闭 (0%)',       'all_off', 'biz_ff_strategy', '', 'info',    'Y', '0', 'admin', SYSDATE(), '紧急开关');

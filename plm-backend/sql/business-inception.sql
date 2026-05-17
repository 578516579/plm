-- =============================================================================
-- 项目立项 (Inception) — PRD §F1.1 + 原型 inception.html
-- AI 辅助生成立项建议书 + 风险识别 + 审批流
-- =============================================================================
DROP TABLE IF EXISTS tb_inception;
CREATE TABLE tb_inception (
    inception_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    inception_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 INC-YYYY-NNNN',
    project_name              VARCHAR(200)  NOT NULL                 COMMENT '项目名称',
    business_line             VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_inception_biz_line 业务线',
    inception_type            VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_inception_type 项目类型',
    background                TEXT                                   COMMENT '背景与诉求 (自然语言)',
    estimated_duration_months INT           DEFAULT NULL             COMMENT '预计工期(月)',
    estimated_team            VARCHAR(200)  DEFAULT NULL             COMMENT '预计团队规模 (如 前端×2 后端×3)',
    -- AI 立项建议书 4 块结构化输出 (原型 runInceptionAI 生成的 4 个 <h4>)
    ai_background             LONGTEXT                               COMMENT 'AI 项目背景分析 (原型 一、项目背景)',
    ai_market_opportunity     LONGTEXT                               COMMENT 'AI 市场机会分析 (原型 二、市场机会)',
    ai_roi_estimate           LONGTEXT                               COMMENT 'AI ROI 预估散文段落 (原型 三、ROI预估)',
    ai_recommend_decision     LONGTEXT                               COMMENT 'AI 建议决策 (原型 四、建议决策)',
    -- 8 个结构化数值字段 (原型 ROI 计算硬编码的数,业务方可改)
    market_size               DECIMAL(10,2) DEFAULT NULL             COMMENT '市场规模(亿元),原型 580',
    digital_penetration       DECIMAL(5,2)  DEFAULT NULL             COMMENT '数字化渗透率(%) 原型 8',
    dev_cost_estimate         DECIMAL(10,2) DEFAULT NULL             COMMENT '开发成本(万元) 原型 180',
    first_year_revenue        DECIMAL(10,2) DEFAULT NULL             COMMENT '首年营收(万元) 原型 3000',
    roi_multiple              DECIMAL(8,2)  DEFAULT NULL             COMMENT 'ROI 倍数,原型 16.7',
    recommended_priority      VARCHAR(4)    DEFAULT NULL             COMMENT '建议优先级 P0/P1/P2',
    recommended_start_quarter VARCHAR(20)   DEFAULT NULL             COMMENT '推荐启动季度,如 Q3-2026',
    delivery_phases           INT           DEFAULT NULL             COMMENT '分期交付期数,原型 3 期',
    -- AI 风险识别 JSON 数组: [{level:warning|critical,title,description}, ...]
    ai_risks_json             TEXT                                   COMMENT 'AI 风险识别 JSON 数组 (原型 incRisks innerHTML 多条 risk)',
    ai_generated              CHAR(1)       DEFAULT 'N'              COMMENT 'Y=已 AI 生成建议书',
    ai_generated_at           DATETIME      DEFAULT NULL             COMMENT 'AI 生成时间',
    status                    VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_inception_status 5 状态机',
    reject_reason             VARCHAR(500)  DEFAULT NULL             COMMENT '驳回原因 (status=04 必填)',
    submitter_user_id         BIGINT(20)    NOT NULL                 COMMENT '提交人',
    approver_user_id          BIGINT(20)    DEFAULT NULL             COMMENT '审批人',
    approved_at               DATETIME      DEFAULT NULL             COMMENT '审批时间 (02→03 自动填)',
    project_id                BIGINT(20)    DEFAULT NULL             COMMENT '审批通过后转项目的 tb_project.id',
    create_by                 VARCHAR(64)   DEFAULT '',
    create_time               DATETIME      DEFAULT NULL,
    update_by                 VARCHAR(64)   DEFAULT '',
    update_time               DATETIME      DEFAULT NULL,
    remark                    VARCHAR(500)  DEFAULT '',
    del_flag                  CHAR(1)       DEFAULT '0',
    PRIMARY KEY (inception_id),
    UNIQUE KEY uk_inception_no (inception_no),
    KEY idx_inception_status (status),
    KEY idx_inception_biz_line (business_line),
    KEY idx_inception_submitter (submitter_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目立项（Inception）';

-- 字典: 业务线
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('立项业务线',     'biz_inception_biz_line', '0', 'admin', SYSDATE(), '4 选项 - 农业垂直'),
('立项项目类型',   'biz_inception_type',     '0', 'admin', SYSDATE(), '4 选项'),
('立项状态',       'biz_inception_status',   '0', 'admin', SYSDATE(), '5 状态机含反向边 04→00'),
('立项建议优先级', 'biz_inception_priority', '0', 'admin', SYSDATE(), 'AI 建议决策的 P0/P1/P2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '植保服务', 'plant_protection', 'biz_inception_biz_line', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '精准农业', 'precision_farming','biz_inception_biz_line', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '农资流通', 'agri_supply',      'biz_inception_biz_line', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '质量溯源', 'traceability',     'biz_inception_biz_line', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '新产品研发', 'new_product',  'biz_inception_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '版本迭代',   'iteration',    'biz_inception_type', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '技术重构',   'refactor',     'biz_inception_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '平台建设',   'platform',     'biz_inception_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',     '00', 'biz_inception_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已提交',   '01', 'biz_inception_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '审批中',   '02', 'biz_inception_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已批准',   '03', 'biz_inception_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态,可转项目'),
(5, '已驳回',   '04', 'biz_inception_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '反向边 04→00');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'P0 - 紧急',  'P0', 'biz_inception_priority', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(2, 'P1 - 重要',  'P1', 'biz_inception_priority', '', 'warning', 'Y', '0', 'admin', SYSDATE(), '原型默认推荐'),
(3, 'P2 - 一般',  'P2', 'biz_inception_priority', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

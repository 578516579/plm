-- =============================================================================
-- 竞品情报 (Competitive) — PRD §F1.3 + 原型 competitive.html
-- AI 自动爬取竞品官网/App Store + SWOT 分析 + 订阅推送
-- =============================================================================
DROP TABLE IF EXISTS tb_competitive;
CREATE TABLE tb_competitive (
    competitive_id        BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    competitive_no        VARCHAR(32)   NOT NULL                 COMMENT '编号 COMP-YYYY-NNNN',
    project_id            BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    competitor_name       VARCHAR(100)  NOT NULL                 COMMENT '竞品名称 (如 禅道/LigaAI/Jira)',
    vendor                VARCHAR(100)  DEFAULT NULL             COMMENT '竞品厂商',
    website               VARCHAR(200)  DEFAULT NULL             COMMENT '竞品官网',
    pricing_model         VARCHAR(200)  DEFAULT NULL             COMMENT '价格模型 (如 $17.5/用户/月)',
    pricing_tier          VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_competitive_tier 价格档',
    feature_matrix        TEXT                                   COMMENT '功能矩阵 JSON (PRD §1.3 12 维度)',
    strengths             TEXT                                   COMMENT 'SWOT-S 优势',
    weaknesses            TEXT                                   COMMENT 'SWOT-W 劣势',
    opportunities         TEXT                                   COMMENT 'SWOT-O 机会',
    threats               TEXT                                   COMMENT 'SWOT-T 威胁',
    ai_analysis_report    LONGTEXT                               COMMENT 'AI 综合报告 Markdown',
    ai_generated          CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    ai_generated_at       DATETIME      DEFAULT NULL             COMMENT 'AI 生成时间',
    monitor_enabled       CHAR(1)       DEFAULT 'N'              COMMENT 'Y=订阅竞品动态推送',
    monitor_keywords      VARCHAR(500)  DEFAULT NULL             COMMENT '订阅关键词 CSV',
    last_monitored_at     DATETIME      DEFAULT NULL             COMMENT '最近一次监控时间',
    status                VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_competitive_status 3 状态',
    author_user_id        BIGINT(20)    NOT NULL                 COMMENT '创建人',
    create_by             VARCHAR(64)   DEFAULT '',
    create_time           DATETIME      DEFAULT NULL,
    update_by             VARCHAR(64)   DEFAULT '',
    update_time           DATETIME      DEFAULT NULL,
    remark                VARCHAR(500)  DEFAULT '',
    del_flag              CHAR(1)       DEFAULT '0',
    PRIMARY KEY (competitive_id),
    UNIQUE KEY uk_competitive_no (competitive_no),
    KEY idx_competitive_project (project_id),
    KEY idx_competitive_name (competitor_name),
    KEY idx_competitive_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞品情报';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('竞品价格档', 'biz_competitive_tier',   '0', 'admin', SYSDATE(), '3 档'),
('竞品状态',   'biz_competitive_status', '0', 'admin', SYSDATE(), '3 态: 草稿/已发布/已归档');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '免费',   'free',       'biz_competitive_tier', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(2, '中端',   'midrange',   'biz_competitive_tier', '', 'warning', 'Y', '0', 'admin', SYSDATE(), ''),
(3, '企业级', 'enterprise', 'biz_competitive_tier', '', 'danger',  'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',     '00', 'biz_competitive_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已发布',   '01', 'biz_competitive_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已归档',   '02', 'biz_competitive_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

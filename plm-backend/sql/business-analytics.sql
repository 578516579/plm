-- =============================================================================
-- 效能分析快照 (Analytics) — PRD §F6 + 原型 analytics.html + devops.html
-- 周期性快照存储 DORA + PLM 关键指标; AI 复盘建议
-- =============================================================================
DROP TABLE IF EXISTS tb_analytics_snapshot;
CREATE TABLE tb_analytics_snapshot (
    snapshot_id            BIGINT(20)     NOT NULL AUTO_INCREMENT  COMMENT '主键',
    snapshot_no            VARCHAR(32)    NOT NULL                 COMMENT '编号 AS-YYYY-NNNN',
    project_id             BIGINT(20)                              COMMENT 'FK→tb_project (NULL=全局)',
    title                  VARCHAR(200)   NOT NULL                 COMMENT '快照标题',
    period_type            VARCHAR(20)    NOT NULL                 COMMENT '字典 biz_analytics_period: month/quarter/year',
    snapshot_date          DATE           NOT NULL                 COMMENT '快照日期 (周期起点)',
    -- PLM 吞吐 / 质量 指标
    requirement_throughput INT            DEFAULT 0                COMMENT '需求吞吐量',
    sprint_on_time_rate    DECIMAL(5,2)   DEFAULT 0                COMMENT '迭代准时率 %',
    defect_density         DECIMAL(8,2)   DEFAULT 0                COMMENT '缺陷密度 (个/KLOC)',
    auto_test_coverage     DECIMAL(5,2)   DEFAULT 0                COMMENT '自动化覆盖率 %',
    -- DORA 指标
    deployment_frequency   DECIMAL(10,2)  DEFAULT 0                COMMENT '部署频率 (次/天)',
    lead_time_hours        DECIMAL(10,2)  DEFAULT 0                COMMENT '前置时间 (小时)',
    mttr_hours             DECIMAL(10,2)  DEFAULT 0                COMMENT '平均恢复时间 (小时)',
    change_failure_rate    DECIMAL(5,2)   DEFAULT 0                COMMENT '变更失败率 %',
    -- AI / 项目风险
    ai_hours_saved         DECIMAL(10,2)  DEFAULT 0                COMMENT 'AI 节省工时 (小时)',
    active_projects        INT            DEFAULT 0                COMMENT '在办项目数',
    projects_at_risk       INT            DEFAULT 0                COMMENT '风险项目数',
    -- AI 复盘建议
    ai_recommendations     LONGTEXT                                COMMENT 'AI 改进建议 Markdown',
    ai_generated           CHAR(1)        DEFAULT 'N'              COMMENT 'Y=AI 生成',
    ai_generated_at        DATETIME       DEFAULT NULL             COMMENT 'AI 生成时间',
    status                 VARCHAR(20)    NOT NULL DEFAULT '00'    COMMENT 'biz_analytics_status',
    author_user_id         BIGINT(20)     NOT NULL                 COMMENT '作者',
    create_by              VARCHAR(64)    DEFAULT '',
    create_time            DATETIME       DEFAULT NULL,
    update_by              VARCHAR(64)    DEFAULT '',
    update_time            DATETIME       DEFAULT NULL,
    remark                 VARCHAR(500)   DEFAULT '',
    del_flag               CHAR(1)        DEFAULT '0',
    PRIMARY KEY (snapshot_id),
    UNIQUE KEY uk_analytics_no (snapshot_no),
    KEY idx_analytics_project (project_id),
    KEY idx_analytics_period (period_type, snapshot_date),
    KEY idx_analytics_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='效能分析快照（Analytics）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('效能分析状态', 'biz_analytics_status', '0', 'admin', SYSDATE(), '3 状态'),
('效能分析周期', 'biz_analytics_period', '0', 'admin', SYSDATE(), 'month/quarter/year');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_analytics_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已发布', '01', 'biz_analytics_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '快照定版'),
(3, '已归档', '02', 'biz_analytics_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), '终态'),

(1, '本月',   'month',   'biz_analytics_period', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '本季度', 'quarter', 'biz_analytics_period', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '本年',   'year',    'biz_analytics_period', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

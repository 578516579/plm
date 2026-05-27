-- =============================================================================
-- DORA 效能指标 (Dora) — DevOps 扩展 + 原型 devops.html
-- DORA 4 指标 + 部署热力图 + 前置时间拆解 + AI 持续改进建议
-- =============================================================================
DROP TABLE IF EXISTS tb_dora_metric;
CREATE TABLE tb_dora_metric (
    dora_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    dora_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 DORA-YYYY-NNNN',
    project_id           BIGINT(20)                             COMMENT 'FK→tb_project (NULL=全局)',
    metric_name          VARCHAR(200)  NOT NULL                 COMMENT '指标名称',
    metric_type          VARCHAR(30)   NOT NULL                 COMMENT '字典 biz_dora_type: deploy_freq/lead_time/mttr/change_fail_rate',
    metric_value         DECIMAL(12,2) NOT NULL                 COMMENT '指标值',
    metric_unit          VARCHAR(30)                            COMMENT '单位 (次/天, 小时, %)',
    period_type          VARCHAR(20)   NOT NULL                 COMMENT '字典 biz_dora_period: month/quarter',
    snapshot_date        DATE          NOT NULL                 COMMENT '记录日期',
    trend_chart_json     LONGTEXT                               COMMENT '趋势图数据 JSON',
    heatmap_json         LONGTEXT                               COMMENT '部署热力图 JSON (仅 deploy_freq)',
    leadtime_breakdown   LONGTEXT                               COMMENT '前置时间拆解 JSON (code/review/merge/deploy)',
    ai_suggestions       LONGTEXT                               COMMENT 'AI 改进建议 Markdown',
    ai_generated         CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    ai_generated_at      DATETIME      DEFAULT NULL             COMMENT 'AI 生成时间',
    status               VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_dora_status',
    author_user_id       BIGINT(20)    NOT NULL                 COMMENT '创建者',
    create_by            VARCHAR(64)   DEFAULT '',
    create_time          DATETIME      DEFAULT NULL,
    update_by            VARCHAR(64)   DEFAULT '',
    update_time          DATETIME      DEFAULT NULL,
    remark               VARCHAR(500)  DEFAULT '',
    del_flag             CHAR(1)       DEFAULT '0',
    PRIMARY KEY (dora_id),
    UNIQUE KEY uk_dora_no (dora_no),
    KEY idx_dora_type (metric_type, snapshot_date),
    KEY idx_dora_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DORA 效能指标（Dora）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('DORA 状态',  'biz_dora_status', '0', 'admin', SYSDATE(), '3 状态'),
('DORA 指标',  'biz_dora_type',   '0', 'admin', SYSDATE(), '4 大指标'),
('DORA 周期',  'biz_dora_period', '0', 'admin', SYSDATE(), 'month/quarter');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_dora_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '已发布', '01', 'biz_dora_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已归档', '02', 'biz_dora_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), '终态'),

(1, '部署频率',     'deploy_freq',       'biz_dora_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '次/天'),
(2, '前置时间',     'lead_time',         'biz_dora_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '小时'),
(3, '平均恢复时间', 'mttr',              'biz_dora_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), '小时'),
(4, '变更失败率',   'change_fail_rate',  'biz_dora_type', '', 'danger',  'N', '0', 'admin', SYSDATE(), '%'),

(1, '本月',   'month',   'biz_dora_period', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '本季度', 'quarter', 'biz_dora_period', '', 'success', 'N', '0', 'admin', SYSDATE(), '');

-- =============================================================================
-- 效能分析 (Analytics) — 原型 analytics.html
-- 项目效能快照：需求吞吐量/迭代准时率/缺陷密度/AI节省工时/健康度
-- 菜单 ID 段: 2730-2736
-- =============================================================================
DROP TABLE IF EXISTS tb_analytics;
CREATE TABLE tb_analytics (
    analytics_id            BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    analytics_no            VARCHAR(32)   NOT NULL                 COMMENT '编号 ANL-YYYY-NNNN',
    project_id              BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title                   VARCHAR(200)  NOT NULL                 COMMENT '分析标题',
    period                  VARCHAR(20)   DEFAULT 'monthly'        COMMENT 'biz_analytics_period',
    period_value            VARCHAR(20)   DEFAULT NULL             COMMENT '周期值 2026-05/2026-Q2',
    requirement_throughput  INT           DEFAULT NULL             COMMENT '需求吞吐量(个/周期)',
    iteration_on_time_rate  DECIMAL(5,2)  DEFAULT NULL             COMMENT '迭代准时率%',
    defect_density          DECIMAL(5,2)  DEFAULT NULL             COMMENT '缺陷密度(个/千行)',
    ai_time_saved           DECIMAL(8,2)  DEFAULT NULL             COMMENT 'AI节省工时(h)',
    project_health_score    DECIMAL(5,2)  DEFAULT NULL             COMMENT '项目健康度0-100',
    ai_suggestions          TEXT                                   COMMENT 'AI改进建议',
    ai_generated            CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI生成',
    ai_generated_at         DATETIME      DEFAULT NULL,
    status                  VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_analytics_status',
    author_user_id          BIGINT(20)    DEFAULT NULL,
    create_by               VARCHAR(64)   DEFAULT '',
    create_time             DATETIME      DEFAULT NULL,
    update_by               VARCHAR(64)   DEFAULT '',
    update_time             DATETIME      DEFAULT NULL,
    remark                  VARCHAR(500)  DEFAULT '',
    del_flag                CHAR(1)       DEFAULT '0',
    PRIMARY KEY (analytics_id),
    UNIQUE KEY uk_analytics_no (analytics_no),
    KEY idx_analytics_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='效能分析快照';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('效能分析周期', 'biz_analytics_period', '0', 'admin', SYSDATE(), '3 周期'),
('效能分析状态', 'biz_analytics_status', '0', 'admin', SYSDATE(), '2 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '本月',   'monthly',   'biz_analytics_period', '', 'primary', 'Y', '0', 'admin', SYSDATE()),
(2, '本季度', 'quarterly', 'biz_analytics_period', '', 'success', 'N', '0', 'admin', SYSDATE()),
(3, '本年',   'yearly',    'biz_analytics_period', '', 'warning', 'N', '0', 'admin', SYSDATE());

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, '草稿',   '00', 'biz_analytics_status', '', 'info',    'Y', '0', 'admin', SYSDATE()),
(2, '已生成', '01', 'biz_analytics_status', '', 'success', 'N', '0', 'admin', SYSDATE());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2730, '效能分析', 2000, 25, 'analytics', 'business/analytics/index', 'C', '0', '0', 'business:analytics:list',   'data-analysis', 'admin', SYSDATE(), ''),
(2731, '分析查询', 2730, 1,  '#', '', 'F', '0', '0', 'business:analytics:query',  '#', 'admin', SYSDATE(), ''),
(2732, '分析新增', 2730, 2,  '#', '', 'F', '0', '0', 'business:analytics:add',    '#', 'admin', SYSDATE(), ''),
(2733, '分析修改', 2730, 3,  '#', '', 'F', '0', '0', 'business:analytics:edit',   '#', 'admin', SYSDATE(), ''),
(2734, '分析删除', 2730, 4,  '#', '', 'F', '0', '0', 'business:analytics:remove', '#', 'admin', SYSDATE(), ''),
(2735, '分析导出', 2730, 5,  '#', '', 'F', '0', '0', 'business:analytics:export', '#', 'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2730 AND 2735;

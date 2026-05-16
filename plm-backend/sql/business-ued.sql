-- =============================================================================
-- UED 设计协同 (UED) — PRD §F2.3 + 原型 ued.html
-- 与 Figma MCP 集成，AI 辅助设计规范检查与标注生成
-- 状态机: 00 草稿 → 01 评审中 ⇆ 02 已确认 → 03 已废弃
-- =============================================================================
DROP TABLE IF EXISTS tb_ued;
CREATE TABLE tb_ued (
    ued_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    ued_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 UED-YYYY-NNNN',
    project_id          BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title               VARCHAR(200)  NOT NULL                 COMMENT '设计稿名称 (如 农情大屏 v2 交互稿)',
    version             VARCHAR(20)   NOT NULL DEFAULT 'v1.0'  COMMENT '版本号 (如 v1.0 / v1.1)',
    figma_url           VARCHAR(500)  DEFAULT NULL             COMMENT 'Figma 文件链接 (MCP 同步入口)',
    figma_synced_at     DATETIME      DEFAULT NULL             COMMENT '最近 Figma 同步时间',
    ai_review_result    LONGTEXT                               COMMENT 'AI 设计规范检查报告 Markdown (ued-review-flow)',
    ai_review_score     DECIMAL(5,2)  DEFAULT NULL             COMMENT 'AI 评审分 0-100，≥80 视为合格',
    ai_reviewed_at      DATETIME      DEFAULT NULL             COMMENT 'AI 评审时间',
    ai_generated        CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 参与生成',
    status              VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_ued_status: 00草稿/01评审中/02已确认/03已废弃',
    designer_user_id    BIGINT(20)    NOT NULL                 COMMENT '设计师 (创建人) FK→sys_user',
    reviewer_user_id    BIGINT(20)    DEFAULT NULL             COMMENT '评审人 FK→sys_user',
    create_by           VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time         DATETIME      DEFAULT NULL             COMMENT '创建时间',
    update_by           VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time         DATETIME      DEFAULT NULL             COMMENT '更新时间',
    remark              VARCHAR(500)  DEFAULT ''               COMMENT '备注',
    del_flag            CHAR(1)       DEFAULT '0'              COMMENT '0=正常 2=已删',
    PRIMARY KEY (ued_id),
    UNIQUE KEY uk_ued_no (ued_no),
    KEY idx_ued_project (project_id),
    KEY idx_ued_status (status),
    KEY idx_ued_designer (designer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='UED 设计稿版本管理';

-- 字典: 状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('UED 状态', 'biz_ued_status', '0', 'admin', SYSDATE(), '4 状态: 草稿/评审中/已确认/已废弃');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_ued_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '评审中', '01', 'biz_ued_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已确认', '02', 'biz_ued_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已废弃', '03', 'biz_ued_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

-- 菜单: 需求&设计 → UED 设计协同
-- 父节点需先确认 menu_id，这里使用动态查询插入方式保证可重复执行
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '需求&设计', 0, 2, 'design', '', '', 1, 0, 'M', '0', '0', '', 'edit', 'admin', SYSDATE(), 'AgriPLM 需求设计域'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '需求&设计' AND parent_id = 0);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 'UED设计协同',
       (SELECT menu_id FROM sys_menu WHERE menu_name = '需求&设计' AND parent_id = 0 LIMIT 1),
       30, 'ued', 'business/ued/index', '', 1, 0, 'C', '0', '0', 'business:ued:list', 'druid', 'admin', SYSDATE(), 'PRD §F2.3'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:ued:list');

-- 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '查询', (SELECT menu_id FROM sys_menu WHERE perms = 'business:ued:list' LIMIT 1), 1, '', '', '', 1, 0, 'F', '0', '0', 'business:ued:query',  '#', 'admin', SYSDATE(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:ued:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '新增', (SELECT menu_id FROM sys_menu WHERE perms = 'business:ued:list' LIMIT 1), 2, '', '', '', 1, 0, 'F', '0', '0', 'business:ued:add',    '#', 'admin', SYSDATE(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:ued:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '修改', (SELECT menu_id FROM sys_menu WHERE perms = 'business:ued:list' LIMIT 1), 3, '', '', '', 1, 0, 'F', '0', '0', 'business:ued:edit',   '#', 'admin', SYSDATE(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:ued:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '删除', (SELECT menu_id FROM sys_menu WHERE perms = 'business:ued:list' LIMIT 1), 4, '', '', '', 1, 0, 'F', '0', '0', 'business:ued:remove', '#', 'admin', SYSDATE(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:ued:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '导出', (SELECT menu_id FROM sys_menu WHERE perms = 'business:ued:list' LIMIT 1), 5, '', '', '', 1, 0, 'F', '0', '0', 'business:ued:export', '#', 'admin', SYSDATE(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:ued:export');

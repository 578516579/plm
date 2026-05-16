-- =============================================================================
-- UED 设计协同 (UED) — PRD §F2.3 + 原型 ued.html
-- 与 Figma 集成,AI 辅助设计规范检查与标注生成,农业场景 UI 组件库
-- =============================================================================
DROP TABLE IF EXISTS tb_ued;
CREATE TABLE tb_ued (
    ued_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    ued_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 UED-YYYY-NNNN',
    project_id          BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title               VARCHAR(200)  NOT NULL                 COMMENT '设计稿名称',
    design_type         VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_ued_design_type 设计类型',
    platform            VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_ued_platform 目标平台',
    figma_file_key      VARCHAR(200)  DEFAULT NULL             COMMENT 'Figma 文件 Key (MCP 同步用)',
    figma_url           VARCHAR(500)  DEFAULT NULL             COMMENT 'Figma 链接',
    version             VARCHAR(20)   NOT NULL DEFAULT 'v1.0'  COMMENT '版本号',
    description         TEXT                                   COMMENT '设计说明',
    review_report       LONGTEXT                               COMMENT 'AI 设计评审报告 Markdown',
    compliance_score    DECIMAL(5,2)  DEFAULT NULL             COMMENT '规范遵从度 0-100,≥80 通过',
    ai_generated        CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 已评审',
    ai_generated_at     DATETIME      DEFAULT NULL             COMMENT 'AI 评审时间',
    requirement_id      BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_requirement (可选双向关联)',
    status              VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_ued_status',
    designer_user_id    BIGINT(20)    NOT NULL                 COMMENT '设计师',
    reviewer_user_id    BIGINT(20)    DEFAULT NULL             COMMENT '评审人',
    create_by           VARCHAR(64)   DEFAULT '',
    create_time         DATETIME      DEFAULT NULL,
    update_by           VARCHAR(64)   DEFAULT '',
    update_time         DATETIME      DEFAULT NULL,
    remark              VARCHAR(500)  DEFAULT '',
    del_flag            CHAR(1)       DEFAULT '0',
    PRIMARY KEY (ued_id),
    UNIQUE KEY uk_ued_no (ued_no),
    KEY idx_ued_project (project_id),
    KEY idx_ued_status (status),
    KEY idx_ued_designer (designer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UED 设计协同';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('UED 设计类型', 'biz_ued_design_type', '0', 'admin', SYSDATE(), '4 类: UE/UI/动效/图标'),
('UED 目标平台', 'biz_ued_platform',    '0', 'admin', SYSDATE(), '农业场景平台: Web/移动/IoT/小程序'),
('UED 状态',     'biz_ued_status',      '0', 'admin', SYSDATE(), '4 态: 草稿/评审中/已确认/已废弃');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'UE 交互设计', 'ue',     'biz_ued_design_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'UI 视觉设计', 'ui',     'biz_ued_design_type', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '动效设计',   'motion', 'biz_ued_design_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '图标设计',   'icon',   'biz_ued_design_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'Web 大屏',    'web',     'biz_ued_platform', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '农情大屏组件'),
(2, '移动端',      'mobile',  'biz_ued_platform', '', 'success', 'N', '0', 'admin', SYSDATE(), '移动端农事记录'),
(3, 'IoT 看板',    'iot',     'biz_ued_platform', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'IoT数据看板'),
(4, '微信小程序',  'miniapp', 'biz_ued_platform', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_ued_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '评审中', '01', 'biz_ued_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已确认', '02', 'biz_ued_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已废弃', '03', 'biz_ued_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

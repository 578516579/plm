-- =============================================================================
-- UED 设计协同 (Ued) — PRD §F2.3 + 原型 ued.html
-- Figma MCP 集成 + AI 规范检查 + 农业 UI 组件库标签
-- =============================================================================
DROP TABLE IF EXISTS tb_ued;
CREATE TABLE tb_ued (
    ued_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    ued_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 UED-YYYY-NNNN',
    project_id          BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    requirement_id      BIGINT(20)    DEFAULT NULL             COMMENT 'F2.3 双向关联需求 FK→tb_requirement',
    title               VARCHAR(200)  NOT NULL                 COMMENT '设计稿名称',
    figma_url           VARCHAR(500)  DEFAULT NULL             COMMENT 'Figma 同步链接',
    figma_file_key      VARCHAR(100)  DEFAULT NULL             COMMENT 'Figma 文件 key (MCP 调用)',
    version_label       VARCHAR(20)   DEFAULT NULL             COMMENT '设计稿版本 v1.0/v1.1',
    preview_url         VARCHAR(500)  DEFAULT NULL             COMMENT '预览缩略图 URL',
    annotation_content  TEXT                                   COMMENT '标注 JSON (间距/颜色/字体)',
    ai_review_report    LONGTEXT                               COMMENT 'AI 设计评审报告 Markdown',
    ai_review_score     DECIMAL(5,2)  DEFAULT NULL             COMMENT 'AI 评分 0-100',
    compliance_check    TEXT                                   COMMENT '规范遵从度 JSON',
    usability_issues    TEXT                                   COMMENT '可用性问题列表',
    agri_component_tags VARCHAR(500)  DEFAULT NULL             COMMENT '农业 UI 组件 CSV',
    ai_generated        CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成评审',
    ai_generated_at     DATETIME      DEFAULT NULL             COMMENT 'AI 评审时间',
    status              VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_ued_status 4 状态',
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
    KEY idx_ued_requirement (requirement_id),
    KEY idx_ued_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UED 设计协同';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('UED 设计状态', 'biz_ued_status', '0', 'admin', SYSDATE(), '4 态含反向边 01→00');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_ued_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '评审中', '01', 'biz_ued_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已确认', '02', 'biz_ued_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已废弃', '03', 'biz_ued_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

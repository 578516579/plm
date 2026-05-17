-- =============================================================================
-- AI PRD 生成器 (PRD) — PRD §F2.2 + 原型 prd.html
-- AI 基于 AgriKB 知识库自动生成完整 PRD (7 段: 背景/用户故事/功能/非功能/验收/原型/版本)
-- =============================================================================
DROP TABLE IF EXISTS tb_prd;
CREATE TABLE tb_prd (
    prd_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    prd_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 PRD-YYYY-NNNN',
    project_id          BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title               VARCHAR(200)  NOT NULL                 COMMENT '功能名称 (如 AI 灌溉推荐引擎)',
    description         TEXT                                   COMMENT '需求描述 (自然语言, AI 输入)',
    scene_template      VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_prd_scene 业务场景模板',
    target_user         VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_prd_target_user 目标用户',
    content             LONGTEXT                               COMMENT 'PRD 全文 Markdown (AI 4 段拼接,导出/分享用)',
    -- 4 段 AI 结构化输出 (原型 generatePRD 输出的 4 个命名 section)
    ai_background       LONGTEXT                               COMMENT 'AI 一、背景与目标 (含硬数值如灌溉用水降低20%)',
    ai_user_stories     LONGTEXT                               COMMENT 'AI 二、用户故事 JSON [{role,want,why}, ...]',
    ai_core_features    LONGTEXT                               COMMENT 'AI 三、核心功能 JSON [{code,name,description}, ...]',
    ai_acceptance       LONGTEXT                               COMMENT 'AI 四、验收标准 JSON [{category,criterion,target}, ...]',
    completeness_score  DECIMAL(5,2)  DEFAULT NULL             COMMENT '完整度 0-100, §F2.2 验收 ≥80,原型 89%',
    version             VARCHAR(20)   NOT NULL DEFAULT 'v1.0'  COMMENT '版本号',
    ai_generated        CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    ai_generated_at     DATETIME      DEFAULT NULL             COMMENT 'AI 生成时间',
    status              VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_prd_status',
    author_user_id      BIGINT(20)    NOT NULL                 COMMENT '作者',
    reviewer_user_id    BIGINT(20)    DEFAULT NULL             COMMENT '评审人',
    create_by           VARCHAR(64)   DEFAULT '',
    create_time         DATETIME      DEFAULT NULL,
    update_by           VARCHAR(64)   DEFAULT '',
    update_time         DATETIME      DEFAULT NULL,
    remark              VARCHAR(500)  DEFAULT '',
    del_flag            CHAR(1)       DEFAULT '0',
    PRIMARY KEY (prd_id),
    UNIQUE KEY uk_prd_no (prd_no),
    KEY idx_prd_project (project_id),
    KEY idx_prd_status (status),
    KEY idx_prd_author (author_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI PRD 文档';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('PRD 业务场景', 'biz_prd_scene',       '0', 'admin', SYSDATE(), '4 选项 - 农业垂直'),
('PRD 目标用户', 'biz_prd_target_user', '0', 'admin', SYSDATE(), '3 类用户角色'),
('PRD 状态',     'biz_prd_status',     '0', 'admin', SYSDATE(), '4 状态: 草稿/评审/确认/废弃');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '精准灌溉管理', 'irrigation',    'biz_prd_scene', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '农资销售',     'agri_sales',    'biz_prd_scene', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '病虫害防治',   'pest_control',  'biz_prd_scene', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '农产品溯源',   'traceability',  'biz_prd_scene', '', 'info',    'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '农场主/种植户', 'farmer',     'biz_prd_target_user', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '农技人员',     'agronomist', 'biz_prd_target_user', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '企业管理员',   'admin',      'biz_prd_target_user', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_prd_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '评审中', '01', 'biz_prd_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已确认', '02', 'biz_prd_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已废弃', '03', 'biz_prd_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

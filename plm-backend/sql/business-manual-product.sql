-- =============================================================================
-- 产品手册 (ManualProduct) — PRD §F5.1 + 原型 productmanual.html
-- AI 一键生成 + 截图上传自动描述 + 多格式导出
-- =============================================================================
DROP TABLE IF EXISTS tb_manual_product;
CREATE TABLE tb_manual_product (
    manualproduct_id     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    manualproduct_no     VARCHAR(32)   NOT NULL                 COMMENT '编号 PM-YYYY-NNNN',
    project_id           BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    title                VARCHAR(200)  NOT NULL                 COMMENT '手册标题',
    product_version      VARCHAR(50)   NOT NULL                 COMMENT '产品版本 (如 v2.1)',
    include_modules      VARCHAR(500)  NOT NULL                 COMMENT 'CSV: 系统概述,快速上手,详细说明,FAQ,视频',
    content              LONGTEXT                               COMMENT 'Markdown 全文',
    screenshots_urls     TEXT                                   COMMENT '截图 URL CSV',
    screenshots_count    INT           DEFAULT 0                COMMENT '截图数量',
    output_formats       VARCHAR(100)  NOT NULL DEFAULT 'pdf'   COMMENT 'CSV: word,pdf,html,h5',
    ai_generated         CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    generated_at         DATETIME      DEFAULT NULL             COMMENT '生成完成时间',
    status               VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_manualproduct_status',
    author_user_id       BIGINT(20)    NOT NULL                 COMMENT '作者',
    create_by            VARCHAR(64)   DEFAULT '',
    create_time          DATETIME      DEFAULT NULL,
    update_by            VARCHAR(64)   DEFAULT '',
    update_time          DATETIME      DEFAULT NULL,
    remark               VARCHAR(500)  DEFAULT '',
    del_flag             CHAR(1)       DEFAULT '0',
    PRIMARY KEY (manualproduct_id),
    UNIQUE KEY uk_manualproduct_no (manualproduct_no),
    KEY idx_manualproduct_project (project_id),
    KEY idx_manualproduct_version (product_version),
    KEY idx_manualproduct_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品手册（ManualProduct）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('产品手册状态', 'biz_manualproduct_status', '0', 'admin', SYSDATE(), '4 状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_manualproduct_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '生成中', '01', 'biz_manualproduct_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'AI 处理中'),
(3, '已生成', '02', 'biz_manualproduct_status', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已发布', '03', 'biz_manualproduct_status', '', 'success', 'N', '0', 'admin', SYSDATE(), '终态');

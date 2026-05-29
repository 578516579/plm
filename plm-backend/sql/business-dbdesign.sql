-- =============================================================================
-- 数据库设计 (DbDesign) — PRD §F3.2 + 原型 dbdesign.html
-- AI 生成 ER 图 (Mermaid) + 数据字典 + 建表 SQL + 规范检查 (命名/索引/范式)
-- =============================================================================
DROP TABLE IF EXISTS tb_dbdesign;
CREATE TABLE tb_dbdesign (
    dbdesign_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    dbdesign_no          VARCHAR(32)   NOT NULL                 COMMENT '编号 DB-YYYY-NNNN',
    project_id           BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    arch_id              BIGINT(20)    DEFAULT NULL             COMMENT 'FK→tb_arch (可选)',
    title                VARCHAR(200)  NOT NULL                 COMMENT '设计标题',
    db_engine            VARCHAR(20)   DEFAULT NULL             COMMENT 'biz_dbdesign_engine 引擎',
    er_diagram_content   LONGTEXT                               COMMENT 'ER 图 Mermaid erDiagram',
    data_dictionary      LONGTEXT                               COMMENT '数据字典 Markdown',
    ddl_script           LONGTEXT                               COMMENT 'CREATE TABLE 集合',
    normalization_check  TEXT                                   COMMENT '规范检查 JSON (命名/索引/范式)',
    ai_generated         CHAR(1)       DEFAULT 'N'              COMMENT 'Y=AI 生成',
    ai_generated_at      DATETIME      DEFAULT NULL             COMMENT 'AI 生成时间',
    status               VARCHAR(20)   NOT NULL DEFAULT '00'    COMMENT 'biz_dbdesign_status 4 状态',
    author_user_id       BIGINT(20)    NOT NULL                 COMMENT 'DBA',
    reviewer_user_id     BIGINT(20)    DEFAULT NULL             COMMENT '评审人',
    create_by            VARCHAR(64)   DEFAULT '',
    create_time          DATETIME      DEFAULT NULL,
    update_by            VARCHAR(64)   DEFAULT '',
    update_time          DATETIME      DEFAULT NULL,
    remark               VARCHAR(500)  DEFAULT '',
    del_flag             CHAR(1)       DEFAULT '0',
    PRIMARY KEY (dbdesign_id),
    UNIQUE KEY uk_dbdesign_no (dbdesign_no),
    KEY idx_dbdesign_project (project_id),
    KEY idx_dbdesign_arch (arch_id),
    KEY idx_dbdesign_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库设计';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('DB 引擎',     'biz_dbdesign_engine', '0', 'admin', SYSDATE(), '3 选项含国产化'),
('DB 设计状态', 'biz_dbdesign_status', '0', 'admin', SYSDATE(), '4 态含反向边 01→00');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'MySQL',      'mysql',      'biz_dbdesign_engine', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'PostgreSQL', 'postgresql', 'biz_dbdesign_engine', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '人大金仓',   'kingbase',   'biz_dbdesign_engine', '', 'warning', 'N', '0', 'admin', SYSDATE(), '');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '草稿',   '00', 'biz_dbdesign_status', '', 'info',    'Y', '0', 'admin', SYSDATE(), ''),
(2, '评审中', '01', 'biz_dbdesign_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, '已确认', '02', 'biz_dbdesign_status', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(4, '已废弃', '03', 'biz_dbdesign_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '终态');

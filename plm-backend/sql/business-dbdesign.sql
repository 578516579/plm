-- ============================================================
-- 数据库设计 (tb_dbdesign) — PRD §F3.2 数据库设计
-- ============================================================
CREATE TABLE IF NOT EXISTS `tb_dbdesign` (
  `dbdesign_id`      BIGINT(20)      NOT NULL AUTO_INCREMENT         COMMENT '主键',
  `dbdesign_no`      VARCHAR(32)     NOT NULL                        COMMENT '编号 DB-YYYY-NNNN',
  `project_id`       BIGINT(20)      NOT NULL                        COMMENT '关联项目 ID',
  `title`            VARCHAR(200)    NOT NULL                        COMMENT '数据库设计标题',
  `db_type`          VARCHAR(20)     DEFAULT NULL                    COMMENT '数据库类型 (biz_dbdesign_type)',
  `er_content`       LONGTEXT        DEFAULT NULL                    COMMENT 'ER 图 (Mermaid/PlantUML 源码)',
  `dict_content`     LONGTEXT        DEFAULT NULL                    COMMENT '数据字典 Markdown',
  `ddl_content`      LONGTEXT        DEFAULT NULL                    COMMENT '建表 DDL SQL',
  `review_report`    LONGTEXT        DEFAULT NULL                    COMMENT 'AI ER 图审查报告',
  `ai_generated`     CHAR(1)         NOT NULL DEFAULT 'N'            COMMENT 'AI 生成标志 Y/N',
  `ai_generated_at`  DATETIME        DEFAULT NULL                    COMMENT 'AI 生成时间',
  `status`           VARCHAR(2)      NOT NULL DEFAULT '00'           COMMENT '状态 (biz_dbdesign_status)',
  `author_user_id`   BIGINT(20)      NOT NULL                        COMMENT '设计者用户 ID',
  `reviewer_user_id` BIGINT(20)      DEFAULT NULL                    COMMENT '评审人用户 ID',
  -- BaseEntity
  `create_by`        VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '创建者',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        VARCHAR(64)     NOT NULL DEFAULT ''             COMMENT '更新者',
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`           VARCHAR(500)    DEFAULT NULL                    COMMENT '备注',
  `del_flag`         CHAR(1)         NOT NULL DEFAULT '0'            COMMENT '删除标志 0正常 2删除',
  PRIMARY KEY (`dbdesign_id`),
  UNIQUE KEY `uk_dbdesign_no` (`dbdesign_no`),
  KEY `idx_dbdesign_project` (`project_id`),
  KEY `idx_dbdesign_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库设计表 PRD §F3.2';

-- ============================================================
-- 字典: 数据库类型
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('数据库类型', 'biz_dbdesign_type', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.2 数据库类型')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, 'MySQL',        'mysql',      'biz_dbdesign_type', '', 'primary', 'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, 'PostgreSQL',   'postgresql', 'biz_dbdesign_type', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, 'KDB+',         'kdb',        'biz_dbdesign_type', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, 'SQLite',       'sqlite',     'biz_dbdesign_type', '', 'info',    'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

-- ============================================================
-- 字典: 数据库设计状态
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('数据库设计状态', 'biz_dbdesign_status', '0', 'admin', NOW(), 'admin', NOW(), 'PRD §F3.2 数据库设计状态')
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name);

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
  (1, '草稿',   '00', 'biz_dbdesign_status', '', 'info',    'Y', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (2, '评审中', '01', 'biz_dbdesign_status', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (3, '已确认', '02', 'biz_dbdesign_status', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), ''),
  (4, '已废弃', '03', 'biz_dbdesign_status', '', 'danger',  'N', '0', 'admin', NOW(), 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);

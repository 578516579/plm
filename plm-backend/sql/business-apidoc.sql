-- API 文档业务模块 DDL (生成器脚手架,需扩展字段)

DROP TABLE IF EXISTS tb_apidoc;
CREATE TABLE tb_apidoc (
    apidoc_id   BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    apidoc_no   VARCHAR(32)  NOT NULL                 COMMENT '编号',
    project_id           BIGINT(20)   NOT NULL                 COMMENT 'FK→tb_project',
    title                VARCHAR(200) NOT NULL                 COMMENT '标题',
    status               VARCHAR(2)   NOT NULL DEFAULT '00'    COMMENT '状态',
    create_by            VARCHAR(64)  DEFAULT '',
    create_time          DATETIME     DEFAULT NULL,
    update_by            VARCHAR(64)  DEFAULT '',
    update_time          DATETIME     DEFAULT NULL,
    remark               VARCHAR(500) DEFAULT '',
    del_flag             CHAR(1)      DEFAULT '0',
    PRIMARY KEY (apidoc_id),
    UNIQUE KEY uk_apidoc_no (apidoc_no),
    KEY idx_apidoc_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API 文档';

-- 菜单 (调整 menu_id 段, 当前 9000+ 是脚手架占位)
-- INSERT INTO sys_menu ... ;
-- INSERT INTO sys_role_menu ... ;

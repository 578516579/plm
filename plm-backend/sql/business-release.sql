-- 发布管理业务模块 DDL (生成器脚手架,需扩展字段)

DROP TABLE IF EXISTS tb_release;
CREATE TABLE tb_release (
    release_id   BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    release_no   VARCHAR(32)  NOT NULL                 COMMENT '编号',
    project_id           BIGINT(20)   NOT NULL                 COMMENT 'FK→tb_project',
    title                VARCHAR(200) NOT NULL                 COMMENT '标题',
    status               VARCHAR(2)   NOT NULL DEFAULT '00'    COMMENT '状态',
    create_by            VARCHAR(64)  DEFAULT '',
    create_time          DATETIME     DEFAULT NULL,
    update_by            VARCHAR(64)  DEFAULT '',
    update_time          DATETIME     DEFAULT NULL,
    remark               VARCHAR(500) DEFAULT '',
    del_flag             CHAR(1)      DEFAULT '0',
    PRIMARY KEY (release_id),
    UNIQUE KEY uk_release_no (release_no),
    KEY idx_release_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布管理';

-- 菜单 (调整 menu_id 段, 当前 9000+ 是脚手架占位)
-- INSERT INTO sys_menu ... ;
-- INSERT INTO sys_role_menu ... ;

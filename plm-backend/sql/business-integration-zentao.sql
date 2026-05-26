-- =============================================================================
-- 禅道(ZenTao)双向同步 — DDL + 字典补充
-- 关联：99-跨阶段/proposals/0014-zentao-bidirectional-sync.md / 02-设计/Zentao-集成-设计.md
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-integration-zentao.sql
-- 回滚：sql/business-integration-zentao-rollback.sql
-- 前置：business-integration.sql、business-defect.sql、business-requirement.sql、business-task.sql、business-testcase.sql 已导入
-- =============================================================================

-- ----------------------------
-- 1. ALTER 4 张业务表：加 external_source / external_id / external_url
-- ----------------------------
-- ⚠ 设计说明：MySQL 唯一索引中任一列 NULL 都不参与唯一约束，
--    因此未同步行(两列都 NULL)可共存，只对真正同步过的行做幂等约束。

ALTER TABLE tb_defect
    ADD COLUMN external_source VARCHAR(32)  DEFAULT NULL COMMENT '外部来源(zentao/jira)，NULL=未同步' AFTER del_flag,
    ADD COLUMN external_id     VARCHAR(64)  DEFAULT NULL COMMENT '外部主键 id，NULL=未同步' AFTER external_source,
    ADD COLUMN external_url    VARCHAR(512) DEFAULT NULL COMMENT '外部详情 URL' AFTER external_id,
    ADD UNIQUE KEY uk_defect_external (external_source, external_id);

ALTER TABLE tb_requirement
    ADD COLUMN external_source VARCHAR(32)  DEFAULT NULL COMMENT '外部来源(zentao/jira)，NULL=未同步' AFTER del_flag,
    ADD COLUMN external_id     VARCHAR(64)  DEFAULT NULL COMMENT '外部主键 id，NULL=未同步' AFTER external_source,
    ADD COLUMN external_url    VARCHAR(512) DEFAULT NULL COMMENT '外部详情 URL' AFTER external_id,
    ADD UNIQUE KEY uk_req_external (external_source, external_id);

ALTER TABLE tb_task
    ADD COLUMN external_source VARCHAR(32)  DEFAULT NULL COMMENT '外部来源(zentao/jira)，NULL=未同步' AFTER del_flag,
    ADD COLUMN external_id     VARCHAR(64)  DEFAULT NULL COMMENT '外部主键 id，NULL=未同步' AFTER external_source,
    ADD COLUMN external_url    VARCHAR(512) DEFAULT NULL COMMENT '外部详情 URL' AFTER external_id,
    ADD UNIQUE KEY uk_task_external (external_source, external_id);

ALTER TABLE tb_testcase
    ADD COLUMN external_source VARCHAR(32)  DEFAULT NULL COMMENT '外部来源(zentao/jira)，NULL=未同步' AFTER del_flag,
    ADD COLUMN external_id     VARCHAR(64)  DEFAULT NULL COMMENT '外部主键 id，NULL=未同步' AFTER external_source,
    ADD COLUMN external_url    VARCHAR(512) DEFAULT NULL COMMENT '外部详情 URL' AFTER external_id,
    ADD UNIQUE KEY uk_tc_external (external_source, external_id);

-- ----------------------------
-- 2. 新表 tb_integration_user_mapping
-- ----------------------------
DROP TABLE IF EXISTS tb_integration_user_mapping;
CREATE TABLE tb_integration_user_mapping (
    id                  BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '主键',
    connector_id        BIGINT(20)   NOT NULL                  COMMENT 'FK→tb_integration_connector.id',
    external_account    VARCHAR(64)  NOT NULL                  COMMENT '外部账号(禅道 account)',
    user_id             BIGINT(20)   DEFAULT NULL              COMMENT 'PLM sys_user.user_id，NULL=未映射，容忍',
    sync_direction      VARCHAR(16)  DEFAULT 'both'            COMMENT '同步方向(biz_integration_user_dir):inbound/outbound/both',
    last_used_at        DATETIME     DEFAULT NULL              COMMENT '最近一次使用',
    create_by           VARCHAR(64)  DEFAULT ''                COMMENT '创建者',
    create_time         DATETIME     DEFAULT NULL              COMMENT '创建时间',
    update_by           VARCHAR(64)  DEFAULT ''                COMMENT '更新者',
    update_time         DATETIME     DEFAULT NULL              COMMENT '更新时间',
    remark              VARCHAR(500) DEFAULT ''                COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_map (connector_id, external_account),
    KEY idx_user_map_user (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='集成用户映射';

-- ----------------------------
-- 3. 字典补充
-- ----------------------------
-- 同步方向字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('集成用户同步方向', 'biz_integration_user_dir', '0', 'admin', SYSDATE(), '入站/出站/双向');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '双向',   'both',     'biz_integration_user_dir', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '入站',   'inbound',  'biz_integration_user_dir', '', 'info',    'N', '0', 'admin', SYSDATE(), '禅道→PLM'),
(3, '出站',   'outbound', 'biz_integration_user_dir', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'PLM→禅道');

-- 4 个业务表 status 加 '99=外部同步' 兜底
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(99, '外部同步', '99', 'biz_defect_status',  '', 'info', 'N', '0', 'admin', SYSDATE(), '未识别的外部状态兜底'),
(99, '外部同步', '99', 'biz_req_status',     '', 'info', 'N', '0', 'admin', SYSDATE(), '未识别的外部状态兜底'),
(99, '外部同步', '99', 'biz_task_status',    '', 'info', 'N', '0', 'admin', SYSDATE(), '未识别的外部状态兜底'),
(99, '外部同步', '99', 'biz_testcase_status','', 'info', 'N', '0', 'admin', SYSDATE(), '未识别的外部状态兜底');

-- 禅道反查辅助字典(展示用，前端 list 表头 dictType 可选用)
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('禅道严重度', 'biz_zentao_severity', '0', 'admin', SYSDATE(), '禅道 bug.severity 1-4'),
('禅道优先级', 'biz_zentao_pri',      '0', 'admin', SYSDATE(), '禅道 pri 1-4');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, 'Blocker',  '1', 'biz_zentao_severity', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(2, 'Critical', '2', 'biz_zentao_severity', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'Major',    '3', 'biz_zentao_severity', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, 'Minor',    '4', 'biz_zentao_severity', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(1, 'Highest',  '1', 'biz_zentao_pri',      '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(2, 'High',     '2', 'biz_zentao_pri',      '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(3, 'Normal',   '3', 'biz_zentao_pri',      '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),
(4, 'Low',      '4', 'biz_zentao_pri',      '', 'info',    'N', '0', 'admin', SYSDATE(), '');

-- ----------------------------
-- 4. 用户映射菜单(挂在 2500 外部集成下，菜单 ID 段 2530-2539)
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name,        parent_id, order_num, path,         component,                                  menu_type, visible, status, perms,                                       icon,         create_by, create_time, remark) VALUES
(2530, '用户映射',         2500, 3,  'user-mapping',    'business/integration/user-mapping/index',    'C', '0', '0', 'business:integration:userMapping:list',    'people',      'admin', SYSDATE(), '禅道 account ↔ sys_user 映射'),
(2531, '映射查询',         2530, 1,  '#',               '',                                           'F', '0', '0', 'business:integration:userMapping:query',   '#',           'admin', SYSDATE(), ''),
(2532, '映射新增',         2530, 2,  '#',               '',                                           'F', '0', '0', 'business:integration:userMapping:add',     '#',           'admin', SYSDATE(), ''),
(2533, '映射修改',         2530, 3,  '#',               '',                                           'F', '0', '0', 'business:integration:userMapping:edit',    '#',           'admin', SYSDATE(), ''),
(2534, '映射删除',         2530, 4,  '#',               '',                                           'F', '0', '0', 'business:integration:userMapping:remove',  '#',           'admin', SYSDATE(), ''),
(2535, '映射导出',         2530, 5,  '#',               '',                                           'F', '0', '0', 'business:integration:userMapping:export',  '#',           'admin', SYSDATE(), '');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2530), (1, 2531), (1, 2532), (1, 2533), (1, 2534), (1, 2535);

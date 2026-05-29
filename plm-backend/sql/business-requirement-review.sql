-- =============================================================================
-- RequirementReview (需求评审记录) 业务子表 — 数据库 DDL + 字典 + 菜单 + 权限
-- 关联：PRD §F2.4 需求评审管理 / PRD-MAPPING.md §2 tb_requirement_review (2026-05-25 新增)
-- 落地: requirement 模块过程能力补全 — 评审闭环
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-requirement-review.sql
-- 回滚：sql/business-requirement-review-rollback.sql
-- 前置：业务表 tb_requirement 已存在 (business-requirement.sql)
-- =============================================================================

-- ----------------------------
-- 1. 评审记录子表
-- ----------------------------
DROP TABLE IF EXISTS tb_requirement_review;
CREATE TABLE tb_requirement_review (
    review_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    requirement_id     BIGINT(20)    NOT NULL                 COMMENT '需求 FK→tb_requirement.requirement_id (必填)',
    reviewer_user_id   BIGINT(20)    NOT NULL                 COMMENT '评审人 FK→sys_user.user_id (必填)',
    review_result      VARCHAR(20)   NOT NULL                 COMMENT '评审结果（字典 biz_req_review_result：00=通过 01=打回）',
    review_comment     VARCHAR(1000) DEFAULT NULL             COMMENT '评审意见正文（打回必填）',
    review_at          DATETIME      DEFAULT NULL             COMMENT '评审时间（提交时回填）',
    create_by          VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME      DEFAULT NULL             COMMENT '创建时间',
    update_by          VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME      DEFAULT NULL             COMMENT '更新时间',
    remark             VARCHAR(500)  DEFAULT ''               COMMENT '备注',
    del_flag           CHAR(1)       DEFAULT '0'              COMMENT '0=正常 2=删除',
    PRIMARY KEY (review_id),
    KEY idx_req_review_requirement (requirement_id),
    KEY idx_req_review_user (reviewer_user_id),
    KEY idx_req_review_result (review_result)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='需求评审记录（RequirementReview，PRD §F2.4）';

-- ----------------------------
-- 2. 字典类型（1 个）
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('需求评审结果', 'biz_req_review_result', '0', 'admin', SYSDATE(), '需求评审通过/打回二元结果');

-- ----------------------------
-- 3. 字典数据（2 条：通过 / 打回）
-- ----------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '通过', '00', 'biz_req_review_result', '', 'success', 'Y', '0', 'admin', SYSDATE(), '准予推进到开发中'),
(2, '打回', '01', 'biz_req_review_result', '', 'danger',  'N', '0', 'admin', SYSDATE(), '退回需求方修改');

-- ----------------------------
-- 4. 菜单与权限（菜单 ID 2026，挂在 requirement 主菜单 2020 下，作为按钮）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2026, '需求评审', 2020, 6, '#', '', 'F', '0', '0', 'business:requirement:review', '#', 'admin', SYSDATE(), '需求评审按钮，2026-05-25 新增（PRD §F2.4）');

-- ----------------------------
-- 5. admin (role_id=1) 授权
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2026);

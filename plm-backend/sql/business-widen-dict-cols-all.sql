-- =============================================================================
-- 全业务表字典码列加宽 VARCHAR(2) → VARCHAR(20) — 纵深防御 ALTER（项目级 sweep）
-- 关联：承接 tb_requirement 单表加宽 (commit 9b6be35) 推广到全部业务表。
--       字典码列（status/priority/severity/category/review_result...）原为 VARCHAR(2)，
--       仅够放 2 字符字典值；前端误传 label 或未来码值变长即触发 "Data too long"。
--       统一加宽到 VARCHAR(20)，从数据层根除此类截断。
-- 范围：38 列 / 33 张业务表（按 INFORMATION_SCHEMA 实测的 VARCHAR(2) 字典码列）。
--       含 tb_requirement 的 3 列（已是 VARCHAR(20)，此处为幂等无副作用重申）。
--       不含孤儿表 tb_analytics / tb_dora（无规范 DDL、无 Mapper 引用，疑似历史遗留）。
-- =============================================================================
-- @no-menu: 本 SQL 仅加宽既有列宽, 不新增菜单/字典/子表
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-widen-dict-cols-all.sql
-- 回滚：sql/business-widen-dict-cols-all-rollback.sql
-- 前置：各业务表已存在
-- 幂等：MODIFY COLUMN 目标定义恒定，列已 VARCHAR(20) 时再次执行为无副作用重建（不报错）。
-- 注：每条 MODIFY 必带原 COMMENT/DEFAULT/NOT NULL，避免丢失列元数据。
-- =============================================================================

-- 规划阶段
ALTER TABLE tb_inception          MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_inception_status 5 状态机';
ALTER TABLE tb_competitive        MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_competitive_status 3 状态';
ALTER TABLE tb_dashboard          MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_dashboard_status';

-- 需求与设计阶段
ALTER TABLE tb_requirement        MODIFY COLUMN source        VARCHAR(20) NOT NULL DEFAULT '01' COMMENT '需求来源（字典 biz_req_source）';
ALTER TABLE tb_requirement        MODIFY COLUMN priority      VARCHAR(20) NOT NULL DEFAULT '02' COMMENT '优先级（字典 biz_req_priority）';
ALTER TABLE tb_requirement        MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT '状态（字典 biz_req_status）';
ALTER TABLE tb_requirement_review MODIFY COLUMN review_result VARCHAR(20) NOT NULL COMMENT '评审结果（字典 biz_req_review_result：00=通过 01=打回）';
ALTER TABLE tb_prd                MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_prd_status';
ALTER TABLE tb_ued                MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_ued_status 4 状态';
ALTER TABLE tb_arch               MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_arch_status 4 状态';
ALTER TABLE tb_dbdesign           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_dbdesign_status 4 状态';
ALTER TABLE tb_apidesign          MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_apidesign_status 4 状态';
ALTER TABLE tb_document           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_doc_status';

-- 研发阶段
ALTER TABLE tb_sprint             MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT '状态（字典 biz_sprint_status）';
ALTER TABLE tb_task               MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT '状态（字典 biz_task_status）';
ALTER TABLE tb_task               MODIFY COLUMN priority      VARCHAR(20) NOT NULL DEFAULT '02' COMMENT '优先级（字典 biz_task_priority）';

-- 测试阶段
ALTER TABLE tb_testplan           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_testplan_status';
ALTER TABLE tb_testcase           MODIFY COLUMN category      VARCHAR(20) NOT NULL DEFAULT '01' COMMENT 'biz_testcase_category';
ALTER TABLE tb_testcase           MODIFY COLUMN priority      VARCHAR(20) NOT NULL DEFAULT '01' COMMENT 'biz_testcase_priority';
ALTER TABLE tb_testcase           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_testcase_status';
ALTER TABLE tb_testdata           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_testdata_status 3 状态';
ALTER TABLE tb_submission         MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_submission_status';
ALTER TABLE tb_autotest           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_autotest_status';
ALTER TABLE tb_defect             MODIFY COLUMN severity      VARCHAR(20) NOT NULL DEFAULT '02' COMMENT '严重级别（biz_defect_severity）';
ALTER TABLE tb_defect             MODIFY COLUMN category      VARCHAR(20) NOT NULL DEFAULT '01' COMMENT '缺陷分类（biz_defect_category）';
ALTER TABLE tb_defect             MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT '状态（biz_defect_status）';
ALTER TABLE tb_testreport         MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_testreport_status';

-- 交付与运维阶段
ALTER TABLE tb_apidoc             MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_apidoc_status';
ALTER TABLE tb_manual_product     MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_manualproduct_status';
ALTER TABLE tb_manual_impl        MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_manualimpl_status';
ALTER TABLE tb_manual_ops         MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_manualops_status';
ALTER TABLE tb_pipeline           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_pipeline_status';
ALTER TABLE tb_release            MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_release_status';
ALTER TABLE tb_feature_flag       MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '01' COMMENT 'biz_ff_status: 00 开启/01 关闭';
ALTER TABLE tb_dora_metric        MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_dora_status';

-- 效能分析
ALTER TABLE tb_analytics_snapshot MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_analytics_status';

-- AI 能力
ALTER TABLE tb_openspec           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_openspec_status';
ALTER TABLE tb_ai_agent           MODIFY COLUMN status        VARCHAR(20) NOT NULL DEFAULT '00' COMMENT 'biz_aiagent_status: 00 运行中/01 已停止/02 错误';

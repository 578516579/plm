-- =============================================================================
-- ZTF自动化测试框架(ztf)集成 — DDL(单向 Inbound:仅扩 tb_autotest external_* 列)
-- 关联：02-设计/ZTF-集成-设计.md / 99-跨阶段/proposals/0019(integration-connector skill 首次 pilot)
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-integration-ztf.sql
-- 回滚：sql/business-integration-ztf-rollback.sql
-- 前置：business-integration.sql(biz_integration_type 已含 'ztf' 行 79,无需补字典)+ business-autotest.sql
-- @no-menu: ZTF 单向集成本期不做 UI,仅 ALTER tb_autotest 加 external_* 列;无新菜单。biz_integration_type 'ztf' 已在 business-integration.sql seed。
-- =============================================================================

-- ----------------------------
-- ZTF run 结果回传：给 tb_autotest 加 external_source / external_id / external_url + 幂等唯一索引
-- ⚠ 两列必须 DEFAULT NULL(非 DEFAULT ''):MySQL 唯一索引中任一列 NULL 都不参与唯一约束,
--   因此大量未关联 ZTF 的 autotest 行(两列 NULL)可共存,只对真正绑定 ZTF taskId 的行做幂等(反思 O9)。
-- ----------------------------
ALTER TABLE tb_autotest
    ADD COLUMN external_source VARCHAR(32)  DEFAULT NULL COMMENT '外部来源(ztf),NULL=非外部同步' AFTER del_flag,
    ADD COLUMN external_id     VARCHAR(64)  DEFAULT NULL COMMENT 'ZTF 任务/run id,NULL=未关联' AFTER external_source,
    ADD COLUMN external_url    VARCHAR(512) DEFAULT NULL COMMENT 'ZTF run 详情 url' AFTER external_id,
    ADD UNIQUE KEY uk_autotest_external (external_source, external_id);

-- 说明：
-- 1. ZTF 是单向 Inbound(执行结果回传),不改 tb_autotest.status 生命周期(00/01/02),只更新结果字段
--    (total_cases / passed_cases / failed_cases / pass_rate / execution_duration_sec / last_executed_at /
--     last_root_cause_analysis),由 ZtfInboundSyncService 写入。
-- 2. 无 tb_integration_user_mapping(本期 ZTF executor 容忍 null,不做用户映射)。
-- 3. 无 biz_*_status 的 99 兜底(ZTF 不映射状态机)。
-- 4. 无 sys_menu(本期不做 ZTF 专属前端页,结果在既有 autotest 详情页展示已有字段)。

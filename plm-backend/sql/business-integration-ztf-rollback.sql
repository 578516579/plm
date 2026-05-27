-- =============================================================================
-- ZTF自动化测试框架(ztf)集成 — 回滚脚本
-- 对应正向脚本：business-integration-ztf.sql
-- 顺序：DROP 唯一索引 → DROP external_* 列
-- 注：biz_integration_type 'ztf' 由 business-integration.sql seed(连接器框架共享),不在此回滚。
-- =============================================================================
ALTER TABLE tb_autotest
    DROP INDEX uk_autotest_external,
    DROP COLUMN external_url,
    DROP COLUMN external_id,
    DROP COLUMN external_source;

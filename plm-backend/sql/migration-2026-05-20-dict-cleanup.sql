-- =============================================================================
-- 增量迁移 — 2026-05-20 — 业务字典漂移清理 (PRD § F4.5 等多模块字典对齐)
--
-- 背景: 运行库 sys_dict_data 与各 business-*.sql seed 文件、PRD-MAPPING.md 漂移。
--       db-ops 全量审计发现 13 处 biz_* dict_type 漂移:
--         - 6 个 dict_type 存在 seed 已无、DB 仍残留的多余 dict_value (例: pytest/junit)
--         - 8 个 dict_type 完全是幽灵 (seed 已改名,旧 dict_type 整组孤儿)
--         - biz_autotest_status 的 01/02 label 与 PRD 文案不符
--
-- 用户决策 (2026-05-20): 强制对齐 PRD(SSoT),清理 DB + 全量 13 个 dict_type
--
-- 安全说明:
--   * 全程事务包裹,失败可 ROLLBACK
--   * 仅清理 biz_* (业务字典),不动 sys_* / customer_* / DataSetting 等系统字典
--   * 删除前先 SELECT COUNT(*) 做 DRY RUN (头部),便于审计
--   * 删除全部限定 dict_value/dict_type,精确白名单,不用 LIKE
-- =============================================================================

-- ------------------- A. DRY RUN: 预计影响行数 -------------------
-- 在生产应用前先跑这段,看 expected 列数字与本文件注释吻合
SELECT 'A1 autotest_status 多余 (expect 2)'  AS step, COUNT(*) AS cnt FROM sys_dict_data WHERE dict_type='biz_autotest_status'    AND dict_value IN ('03','04')
UNION ALL SELECT 'A2 autotest_framework 多余 (expect 2)',  COUNT(*) FROM sys_dict_data WHERE dict_type='biz_autotest_framework' AND dict_value IN ('pytest','junit')
UNION ALL SELECT 'A3 analytics_period 多余 (expect 3)',    COUNT(*) FROM sys_dict_data WHERE dict_type='biz_analytics_period'   AND dict_value IN ('monthly','quarterly','yearly')
UNION ALL SELECT 'A4 dashboard_widget 多余 (expect 4)',    COUNT(*) FROM sys_dict_data WHERE dict_type='biz_dashboard_widget'   AND dict_value IN ('chart','card','table','link')
UNION ALL SELECT 'A5 openspec_type 多余 (expect 2)',       COUNT(*) FROM sys_dict_data WHERE dict_type='biz_openspec_type'      AND dict_value IN ('openapi31','asyncapi30')
UNION ALL SELECT 'A6 pipeline_trigger 多余 (expect 2)',    COUNT(*) FROM sys_dict_data WHERE dict_type='biz_pipeline_trigger'   AND dict_value IN ('schedule','mr')
UNION ALL SELECT 'A7 幽灵 dict_data 整组 (expect 30)',     COUNT(*) FROM sys_dict_data WHERE dict_type IN (
    'biz_manual_impl_db','biz_manual_impl_deploy','biz_manual_impl_os','biz_manual_impl_status',
    'biz_manual_ops_monitor','biz_manual_ops_status',
    'biz_feature_flag_env','biz_feature_flag_strategy'
)
UNION ALL SELECT 'A8 幽灵 dict_type 整组 (expect 8)',      COUNT(*) FROM sys_dict_type WHERE dict_type IN (
    'biz_manual_impl_db','biz_manual_impl_deploy','biz_manual_impl_os','biz_manual_impl_status',
    'biz_manual_ops_monitor','biz_manual_ops_status',
    'biz_feature_flag_env','biz_feature_flag_strategy'
)
UNION ALL SELECT 'A9 autotest_status 01/02 label 待修 (expect 2)', COUNT(*) FROM sys_dict_data WHERE dict_type='biz_autotest_status' AND dict_value IN ('01','02') AND dict_label IN ('待执行','执行中');

-- ------------------- B. 执行清理 (事务包裹) -------------------
START TRANSACTION;

-- B1. 6 个 dict_type 多余 dict_value (15 rows)
DELETE FROM sys_dict_data WHERE dict_type='biz_autotest_status'    AND dict_value IN ('03','04');
DELETE FROM sys_dict_data WHERE dict_type='biz_autotest_framework' AND dict_value IN ('pytest','junit');
DELETE FROM sys_dict_data WHERE dict_type='biz_analytics_period'   AND dict_value IN ('monthly','quarterly','yearly');
DELETE FROM sys_dict_data WHERE dict_type='biz_dashboard_widget'   AND dict_value IN ('chart','card','table','link');
DELETE FROM sys_dict_data WHERE dict_type='biz_openspec_type'      AND dict_value IN ('openapi31','asyncapi30');
DELETE FROM sys_dict_data WHERE dict_type='biz_pipeline_trigger'   AND dict_value IN ('schedule','mr');

-- B2. 8 个幽灵 dict_type 整组数据 (30 rows)
DELETE FROM sys_dict_data WHERE dict_type IN (
    'biz_manual_impl_db','biz_manual_impl_deploy','biz_manual_impl_os','biz_manual_impl_status',
    'biz_manual_ops_monitor','biz_manual_ops_status',
    'biz_feature_flag_env','biz_feature_flag_strategy'
);

-- B3. 8 个幽灵 dict_type 定义 (8 rows in sys_dict_type)
DELETE FROM sys_dict_type WHERE dict_type IN (
    'biz_manual_impl_db','biz_manual_impl_deploy','biz_manual_impl_os','biz_manual_impl_status',
    'biz_manual_ops_monitor','biz_manual_ops_status',
    'biz_feature_flag_env','biz_feature_flag_strategy'
);

-- B4. autotest_status label 对齐 PRD (2 rows UPDATE)
UPDATE sys_dict_data SET dict_label='已激活', list_class='success', remark='可定时执行' WHERE dict_type='biz_autotest_status' AND dict_value='01';
UPDATE sys_dict_data SET dict_label='已禁用', list_class='danger',  remark=''            WHERE dict_type='biz_autotest_status' AND dict_value='02';

-- COMMIT;  -- ⚠ 应用时由 db-ops 显式 COMMIT,DRY RUN 时保持 ROLLBACK

-- ------------------- C. 验证: 清理后状态应与 seed 一致 -------------------
SELECT 'C1 autotest_status 应剩 3 (00/01/02)',   GROUP_CONCAT(CONCAT(dict_value,'=',dict_label) ORDER BY dict_value SEPARATOR ' | ') AS result FROM sys_dict_data WHERE dict_type='biz_autotest_status'
UNION ALL SELECT 'C2 autotest_framework 应剩 4',        GROUP_CONCAT(dict_value ORDER BY dict_value SEPARATOR ',') FROM sys_dict_data WHERE dict_type='biz_autotest_framework'
UNION ALL SELECT 'C3 analytics_period 应剩 3',          GROUP_CONCAT(dict_value ORDER BY dict_value SEPARATOR ',') FROM sys_dict_data WHERE dict_type='biz_analytics_period'
UNION ALL SELECT 'C4 dashboard_widget 应剩 6',          GROUP_CONCAT(dict_value ORDER BY dict_value SEPARATOR ',') FROM sys_dict_data WHERE dict_type='biz_dashboard_widget'
UNION ALL SELECT 'C5 openspec_type 应剩 4',             GROUP_CONCAT(dict_value ORDER BY dict_value SEPARATOR ',') FROM sys_dict_data WHERE dict_type='biz_openspec_type'
UNION ALL SELECT 'C6 pipeline_trigger 应剩 4',          GROUP_CONCAT(dict_value ORDER BY dict_value SEPARATOR ',') FROM sys_dict_data WHERE dict_type='biz_pipeline_trigger'
UNION ALL SELECT 'C7 幽灵 dict_data 应 0',              CAST(COUNT(*) AS CHAR)                                     FROM sys_dict_data WHERE dict_type LIKE 'biz_manual_impl_%' OR dict_type LIKE 'biz_manual_ops_%' OR dict_type LIKE 'biz_feature_flag_%'
UNION ALL SELECT 'C8 幽灵 dict_type 应 0',              CAST(COUNT(*) AS CHAR)                                     FROM sys_dict_type WHERE dict_type LIKE 'biz_manual_impl_%' OR dict_type LIKE 'biz_manual_ops_%' OR dict_type LIKE 'biz_feature_flag_%';

-- ------------------- D. 应用流程 -------------------
-- 1. 跑 A 段 (DRY RUN COUNT) — 确认所有 expect 数字符合
-- 2. 跑 B 段 — START TRANSACTION 后立刻看影响行数,确认 15+30+8+2 = 55 rows changed
-- 3. 跑 C 段 — 验证 SELECT,看 C1-C8 全部符合期望
-- 4. 如全部正确,执行 COMMIT;
-- 5. 如有异常,执行 ROLLBACK;

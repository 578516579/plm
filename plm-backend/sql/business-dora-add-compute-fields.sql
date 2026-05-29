-- @no-menu: 仅扩 tb_dora_metric 列 + 新增 DORA 聚合 Quartz 任务 seed,无新菜单
-- =============================================================================
-- DORA 真聚合 — 扩列 + Quartz 任务 (Proposal 0028 P0-3B)
-- =============================================================================
-- 1. tb_dora_metric 加 5 列(period_start/period_end/period_days/is_computed/computed_at)
--    幂等:通过 INFORMATION_SCHEMA 检测后才 ALTER
-- 2. (project_id, metric_type, period_start) 联合索引(upsert 查重 + 列表过滤)
-- 3. sys_job 加 DORA 每日聚合任务 (cron 0 0 3 * * ?,可在 /system/job 调整)
-- =============================================================================

-- ---------- 1. ALTER TABLE 加列(幂等) ----------
SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'period_start') = 0,
    "ALTER TABLE tb_dora_metric ADD COLUMN period_start DATETIME DEFAULT NULL COMMENT '聚合窗口开始时间(P0-3B)' AFTER snapshot_date",
    "SELECT 'period_start exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'period_end') = 0,
    "ALTER TABLE tb_dora_metric ADD COLUMN period_end DATETIME DEFAULT NULL COMMENT '聚合窗口结束时间(P0-3B)' AFTER period_start",
    "SELECT 'period_end exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'period_days') = 0,
    "ALTER TABLE tb_dora_metric ADD COLUMN period_days INT DEFAULT 30 COMMENT '聚合窗口天数,默认 30 天(P0-3B)' AFTER period_end",
    "SELECT 'period_days exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'is_computed') = 0,
    "ALTER TABLE tb_dora_metric ADD COLUMN is_computed CHAR(1) DEFAULT 'N' COMMENT 'Y=自动算出 N=人工录入(P0-3B)' AFTER period_days",
    "SELECT 'is_computed exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND COLUMN_NAME = 'computed_at') = 0,
    "ALTER TABLE tb_dora_metric ADD COLUMN computed_at DATETIME DEFAULT NULL COMMENT '上次自动计算时间(P0-3B)' AFTER is_computed",
    "SELECT 'computed_at exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. 联合索引 (project_id, metric_type, period_start) ----------
SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tb_dora_metric'
          AND INDEX_NAME = 'idx_dora_project_period') = 0,
    "ALTER TABLE tb_dora_metric ADD INDEX idx_dora_project_period (project_id, metric_type, period_start)",
    "SELECT 'idx_dora_project_period exists, skip' AS info"
));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 3. sys_job seed: 每日 03:00 全量项目聚合 ----------
-- job_name + job_group 不是 unique key,这里用 NOT EXISTS 保证幂等
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT 'DORA 全量项目指标聚合', 'DEFAULT', 'doraComputeTask.computeAllProjects(30)', '0 0 3 * * ?', '1', '1', '0', 'admin', SYSDATE(), 'Proposal 0028 P0-3B 每日凌晨 3 点聚合 4 个 DORA 指标'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_job
    WHERE job_name = 'DORA 全量项目指标聚合' AND job_group = 'DEFAULT'
);

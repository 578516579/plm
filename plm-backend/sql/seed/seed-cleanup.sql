-- =============================================================================
-- seed-cleanup.sql — 清理所有 seed 数据
-- 调用: mysql -uroot -p... --default-character-set=utf8mb4 plm < seed-cleanup.sql
-- 危险:这会删除所有 remark='seed' AND create_by='seed-runner' 的数据
-- =============================================================================

-- 按 FK 依赖逆序清理:子表先于父表
-- (FK 在 Service 层 / 索引上, 非 DB 硬约束, 但保持逻辑顺序便于人工检查)

-- 子表(指向多个父表)
DELETE FROM tb_defect      WHERE remark='seed' AND create_by='seed-runner';   -- 引 project / testcase / sprint / task
DELETE FROM tb_testreport  WHERE remark='seed' AND create_by='seed-runner';   -- 引 project / sprint
DELETE FROM tb_prd         WHERE remark='seed' AND create_by='seed-runner';   -- 引 project / requirement

-- 二级父表(被 defect 引用)
DELETE FROM tb_testcase    WHERE remark='seed' AND create_by='seed-runner';   -- 引 project
DELETE FROM tb_task        WHERE remark='seed' AND create_by='seed-runner';   -- 引 project / sprint / requirement
DELETE FROM tb_sprint      WHERE remark='seed' AND create_by='seed-runner';   -- 引 project
DELETE FROM tb_requirement WHERE remark='seed' AND create_by='seed-runner';   -- 引 project

-- 根父表
DELETE FROM tb_project     WHERE remark='seed' AND create_by='seed-runner';

SELECT 'seed cleanup done' AS status;

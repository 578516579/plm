-- =============================================================================
-- tb_project 基线 seed — 保留 PRJ-2026-0001 给 E2E 测试用
-- 之前 db cleanup 误删后,可重新执行此脚本恢复
-- =============================================================================
INSERT INTO tb_project (project_no, project_name, project_type, status, manager_user_id,
                        start_date, end_date, budget, description, create_by, create_time, del_flag)
VALUES ('PRJ-2026-0001', 'AgriPLM AI 示例项目', 'product', '1', 1,
        '2026-01-01', '2026-12-31', 500000.00, 'E2E 基线 seed,请勿删除', 'admin', NOW(), '0')
ON DUPLICATE KEY UPDATE
    project_name = VALUES(project_name),
    project_type = VALUES(project_type),
    status       = VALUES(status),
    del_flag     = '0';

-- 验证:
-- SELECT id, project_no, project_name, status FROM tb_project WHERE project_no='PRJ-2026-0001';

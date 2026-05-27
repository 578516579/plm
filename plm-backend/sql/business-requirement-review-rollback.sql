-- =============================================================================
-- RequirementReview 回滚脚本 — 反向操作 business-requirement-review.sql
-- =============================================================================
-- 导入：mysql -uroot -p... --default-character-set=utf8mb4 plm < sql/business-requirement-review-rollback.sql
-- =============================================================================

-- 1. 撤回 admin 授权
DELETE FROM sys_role_menu WHERE role_id=1 AND menu_id=2026;

-- 2. 删菜单按钮
DELETE FROM sys_menu WHERE menu_id=2026;

-- 3. 删字典数据
DELETE FROM sys_dict_data WHERE dict_type='biz_req_review_result';

-- 4. 删字典类型
DELETE FROM sys_dict_type WHERE dict_type='biz_req_review_result';

-- 5. 删评审表
DROP TABLE IF EXISTS tb_requirement_review;

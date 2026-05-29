-- =============================================================================
-- seed-testreport.sql — 30 个测试报告
-- 用途:E2E / 性能测试前批量种入,跑完用 seed-cleanup.sql 清理
-- 前置:先跑过 seed-project.sql + seed-sprint.sql
--       + 已 apply business-testreport-add-aggregate-fields.sql (is_aggregated 等列存在)
-- 调用: mysql -uroot -p... --default-character-set=utf8mb4 plm < seed-testreport.sql
-- =============================================================================

-- ----------------------------
-- 1. 幂等:先清掉之前的 seed
-- ----------------------------
DELETE FROM tb_testreport WHERE remark = 'seed' AND create_by = 'seed-runner';

-- ----------------------------
-- 2. 种入 30 个测试报告
--    id 区间 600-629
--    覆盖 3 状态(biz_testreport_status):00 草稿(10) / 01 审核中(10) / 02 已发布(10)
--    覆盖 3 风险(biz_testreport_risk):green(12) / yellow(11) / red(7)
-- ----------------------------
INSERT INTO tb_testreport
    (testreport_id, testreport_no, project_id, sprint_id, testplan_id, title,
     total_cases, passed_cases, failed_cases, coverage_rate, defect_summary,
     p0_defects, p1_defects, p2_defects, risk_level, risk_evaluation, recommendations,
     ai_generated, status, generated_at, reviewer_user_id,
     is_aggregated, aggregated_at, is_manual_override,
     create_by, create_time, remark, del_flag)
VALUES
-- 状态 00 草稿 (10 个)
(600, 'TR-2026-SEED001', 100, 200, NULL, '【seed】大屏 1 首版报告',         80,  75, 5,  93.75, '{"p0":0,"p1":1,"p2":4}',  0, 1, 4, 'green',  '低风险,可发布',   '补 1 个边界用例', 'N', '00', NULL, NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(601, 'TR-2026-SEED002', 101, 201, NULL, '【seed】大屏 2 第 1 轮',          60,  50, 10, 83.33, '{"p0":0,"p1":2,"p2":8}',  0, 2, 8, 'yellow', '中等风险,需修 P1', '修 P1 后复测',    'N', '00', NULL, NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(602, 'TR-2026-SEED003', 102, 202, NULL, '【seed】无人机航线 1 轮',         40,  32, 8,  80.00, '{"p0":1,"p1":3,"p2":4}',  1, 3, 4, 'red',    '高风险,P0 阻塞',   '修 P0 后再发',    'N', '00', NULL, NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(603, 'TR-2026-SEED004', 103, 203, NULL, '【seed】温室 PoC 报告',           20,  18, 2,  90.00, '{"p0":0,"p1":1,"p2":1}',  0, 1, 1, 'green',  '低风险',           '可继续',          'N', '00', NULL, NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(604, 'TR-2026-SEED005', 104, 204, NULL, '【seed】溯源链改造 1 轮',         30,  20, 10, 66.67, '{"p0":2,"p1":4,"p2":4}',  2, 4, 4, 'red',    '高风险,双花未修', '修双花',          'N', '00', NULL, NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(605, 'TR-2026-SEED006', 105, 205, NULL, '【seed】气象 API 验证',           25,  22, 3,  88.00, '{"p0":0,"p1":1,"p2":2}',  0, 1, 2, 'green',  '低风险',           '补时区用例',      'N', '00', NULL, NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(606, 'TR-2026-SEED007', 106, 206, NULL, '【seed】土壤传感器 1 轮',         35,  30, 5,  85.71, '{"p0":0,"p1":2,"p2":3}',  0, 2, 3, 'yellow', '中等',             '修告警漏发',      'N', '00', NULL, NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(607, 'TR-2026-SEED008', 107, 207, NULL, '【seed】病虫害模型 PoC',          15,  13, 2,  86.67, '{"p0":0,"p1":1,"p2":1}',  0, 1, 1, 'yellow', '中,推理慢',       '优化推理速度',    'Y', '00', NOW(), NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(608, 'TR-2026-SEED009', 108, 208, NULL, '【seed】画像系统 1 轮',           45,  40, 5,  88.89, '{"p0":0,"p1":1,"p2":4}',  0, 1, 4, 'green',  '低',               '可',              'Y', '00', NOW(), NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
(609, 'TR-2026-SEED010', 109, 209, NULL, '【seed】网关重构 1 轮',           50,  44, 6,  88.00, '{"p0":0,"p1":2,"p2":4}',  0, 2, 4, 'yellow', '中',               '改 SSE 断流',      'N', '00', NULL, NULL, 'N', NULL, 'N', 'seed-runner', NOW(), 'seed', '0'),
-- 状态 01 审核中 (10 个)
(610, 'TR-2025-SEED001', 115, 210, NULL, '【seed】架构升级 sprint 报告',   90,  88, 2,  97.78, '{"p0":0,"p1":0,"p2":2}',  0, 0, 2, 'green',  '可发布',           '无',              'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(611, 'TR-2025-SEED002', 116, 211, NULL, '【seed】中台 sprint 报告',        120, 115,5,  95.83, '{"p0":0,"p1":1,"p2":4}',  0, 1, 4, 'green',  '可发布',           '补缓存击穿',      'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(612, 'TR-2025-SEED003', 117, 212, NULL, '【seed】PLM 模块 sprint 报告',    80,  72, 8,  90.00, '{"p0":1,"p1":2,"p2":5}',  1, 2, 5, 'red',    '高,编号重复',     '修并发',          'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(613, 'TR-2025-SEED004', 118, 213, NULL, '【seed】app sprint 报告',         60,  55, 5,  91.67, '{"p0":0,"p1":1,"p2":4}',  0, 1, 4, 'yellow', '中,启动 push',    '改启动弹窗',      'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(614, 'TR-2025-SEED005', 119, 214, NULL, '【seed】数据中台 1 轮',           70,  62, 8,  88.57, '{"p0":1,"p1":2,"p2":5}',  1, 2, 5, 'red',    '高,权限漏',       '修权限',          'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(615, 'TR-2025-SEED006', 120, 215, NULL, '【seed】支付通道验收',           40,  38, 2,  95.00, '{"p0":0,"p1":1,"p2":1}',  0, 1, 1, 'yellow', '中,重放未修',     '修重放',          'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(616, 'TR-2025-SEED007', 121, 216, NULL, '【seed】CDN 切流验收',           20,  19, 1,  95.00, '{"p0":0,"p1":0,"p2":1}',  0, 0, 1, 'green',  '低',               '可',              'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(617, 'TR-2025-SEED008', 122, 217, NULL, '【seed】会员迁移验收',           55,  52, 3,  94.55, '{"p0":0,"p1":1,"p2":2}',  0, 1, 2, 'yellow', '中,负数边界',     '修边界',          'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(618, 'TR-2025-SEED009', 123, 218, NULL, '【seed】反爬 V2 验收',           35,  30, 5,  85.71, '{"p0":0,"p1":2,"p2":3}',  0, 2, 3, 'yellow', '中,误杀',         '调阈值',          'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(619, 'TR-2025-SEED010', 124, 219, NULL, '【seed】备份脚本验收',           18,  17, 1,  94.44, '{"p0":0,"p1":1,"p2":0}',  0, 1, 0, 'yellow', '中,IOPS 挤压',    '错峰',            'Y', '01', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
-- 状态 02 已发布 (10 个)
(620, 'TR-2024-SEED001', 138, 225, NULL, '【seed】CMS 上线终版报告',       150, 148,2,  98.67, '{"p0":0,"p1":0,"p2":2}',  0, 0, 2, 'green',  '可上线',           '无',              'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(621, 'TR-2024-SEED002', 139, 226, NULL, '【seed】OA 改造终版',             100, 96, 4,  96.00, '{"p0":0,"p1":1,"p2":3}',  0, 1, 3, 'green',  '可上线',           '观察 1 周',       'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(622, 'TR-2024-SEED003', 140, 227, NULL, '【seed】CRM 上线报告',           130, 126,4,  96.92, '{"p0":0,"p1":0,"p2":4}',  0, 0, 4, 'green',  '可上线',           '无',              'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(623, 'TR-2024-SEED004', 141, 228, NULL, '【seed】门户首页上线',           45,  44, 1,  97.78, '{"p0":0,"p1":0,"p2":1}',  0, 0, 1, 'green',  '可上线',           '无',              'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(624, 'TR-2024-SEED005', 142, 229, NULL, '【seed】SSO 上线终版',           80,  78, 2,  97.50, '{"p0":0,"p1":0,"p2":2}',  0, 0, 2, 'green',  '可上线',           '观察 1 周',       'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(625, 'TR-2024-SEED006', 143, NULL, NULL, '【seed】导出工具上线',           25,  24, 1,  96.00, '{"p0":0,"p1":1,"p2":0}',  0, 1, 0, 'yellow', '中,OOM 已修',     '观察',            'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(626, 'TR-2024-SEED007', 144, NULL, NULL, '【seed】定时任务平台上线',       35,  33, 2,  94.29, '{"p0":0,"p1":1,"p2":1}',  0, 1, 1, 'yellow', '中,漏触发已修',   '观察',            'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(627, 'TR-2024-SEED008', 138, 225, NULL, '【seed】CMS 第 2 版回归报告',    155, 153,2,  98.71, '{"p0":0,"p1":0,"p2":2}',  0, 0, 2, 'green',  '可上线',           '无',              'Y', '02', NOW(), 1, 'Y', NOW(), 'Y', 'seed-runner', NOW(), 'seed', '0'),
(628, 'TR-2024-SEED009', 140, 227, NULL, '【seed】CRM 月度回归',           135, 132,3,  97.78, '{"p0":0,"p1":1,"p2":2}',  0, 1, 2, 'green',  '可',               '无',              'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0'),
(629, 'TR-2024-SEED010', 142, 229, NULL, '【seed】SSO 季度回归',           85,  83, 2,  97.65, '{"p0":0,"p1":0,"p2":2}',  0, 0, 2, 'green',  '可',               '无',              'Y', '02', NOW(), 1, 'Y', NOW(), 'N', 'seed-runner', NOW(), 'seed', '0');

-- ----------------------------
-- 3. 验证
-- ----------------------------
SELECT COUNT(*) AS seed_count, status, COUNT(*) cnt
  FROM tb_testreport
 WHERE remark='seed' AND create_by='seed-runner'
 GROUP BY status WITH ROLLUP;
-- 期望:30 行总计;00=10, 01=10, 02=10

SELECT risk_level, COUNT(*) cnt
  FROM tb_testreport
 WHERE remark='seed' AND create_by='seed-runner'
 GROUP BY risk_level WITH ROLLUP;
-- 期望:green=12, yellow=11, red=7

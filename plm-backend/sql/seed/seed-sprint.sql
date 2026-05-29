-- =============================================================================
-- seed-sprint.sql — 35 个测试迭代
-- 用途:E2E / 性能测试前批量种入,跑完用 seed-cleanup.sql 清理
-- 前置:已经先跑过 seed-project.sql(project id 100-149 必须存在)
-- 调用: mysql -uroot -p... --default-character-set=utf8mb4 plm < seed-sprint.sql
-- =============================================================================

-- ----------------------------
-- 1. 幂等:先清掉之前的 seed
-- ----------------------------
DELETE FROM tb_sprint WHERE remark = 'seed' AND create_by = 'seed-runner';

-- ----------------------------
-- 2. 种入 35 个迭代
--    id 区间 200-234
--    覆盖 4 种状态(字典 biz_sprint_status):00 计划中(10) / 01 进行中(12) / 02 已完成(8) / 03 已取消(5)
--    project_id 引 seed-project id 100-149
-- ----------------------------
INSERT INTO tb_sprint
    (sprint_id, sprint_no, project_id, name, goal, status,
     planned_start_date, planned_end_date, actual_start_date, actual_end_date, duration_days,
     create_by, create_time, remark, del_flag)
VALUES
-- 状态 00 计划中 (10 个)
(200, 'SPR-2026-SEED001', 100, '【seed】Sprint 26W22', '大屏 1 首版静态页', '00', '2026-06-01', '2026-06-14', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(201, 'SPR-2026-SEED002', 101, '【seed】Sprint 26W23', '大屏 2 数据接入',   '00', '2026-06-15', '2026-06-28', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(202, 'SPR-2026-SEED003', 102, '【seed】Sprint 26W24', '无人机航线规划',     '00', '2026-06-15', '2026-06-28', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(203, 'SPR-2026-SEED004', 103, '【seed】Sprint 26W25', '温室控制 PoC',       '00', '2026-07-01', '2026-07-14', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(204, 'SPR-2026-SEED005', 104, '【seed】Sprint 26W26', '溯源链改造启动',     '00', '2026-07-15', '2026-07-28', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(205, 'SPR-2026-SEED006', 105, '【seed】Sprint 26W27', '气象数据接入',       '00', '2026-08-01', '2026-08-14', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(206, 'SPR-2026-SEED007', 106, '【seed】Sprint 26W28', '土壤传感器调试',     '00', '2026-08-15', '2026-08-28', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(207, 'SPR-2026-SEED008', 107, '【seed】Sprint 26W29', '病虫害模型训练',     '00', '2026-09-01', '2026-09-14', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(208, 'SPR-2026-SEED009', 108, '【seed】Sprint 26W30', '用户画像 PoC',       '00', '2026-09-15', '2026-09-28', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(209, 'SPR-2026-SEED010', 109, '【seed】Sprint 26W31', '网关重构启动',       '00', '2026-10-01', '2026-10-14', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
-- 状态 01 进行中 (12 个)
(210, 'SPR-2025-SEED001', 115, '【seed】Sprint 26W21', '架构升级 P1',        '01', '2026-05-15', '2026-05-28', '2026-05-15', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(211, 'SPR-2025-SEED002', 116, '【seed】Sprint 26W21B','中台核心服务',       '01', '2026-05-15', '2026-05-28', '2026-05-15', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(212, 'SPR-2025-SEED003', 117, '【seed】Sprint 26W21C','PLM 模块脚手架',     '01', '2026-05-15', '2026-05-28', '2026-05-15', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(213, 'SPR-2025-SEED004', 118, '【seed】Sprint 26W21D','app 登录模块',       '01', '2026-05-15', '2026-05-28', '2026-05-15', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(214, 'SPR-2025-SEED005', 119, '【seed】Sprint 26W21E','数据中台元数据',     '01', '2026-05-15', '2026-05-28', '2026-05-15', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(215, 'SPR-2025-SEED006', 120, '【seed】Sprint 26W21F','支付通道联调',       '01', '2026-05-15', '2026-05-28', '2026-05-15', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(216, 'SPR-2025-SEED007', 121, '【seed】Sprint 26W21G','CDN 切流',           '01', '2026-05-20', '2026-06-02', '2026-05-20', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(217, 'SPR-2025-SEED008', 122, '【seed】Sprint 26W21H','会员迁移脚本',       '01', '2026-05-20', '2026-06-02', '2026-05-20', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(218, 'SPR-2025-SEED009', 123, '【seed】Sprint 26W21I','反爬规则升级',       '01', '2026-05-20', '2026-06-02', '2026-05-20', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(219, 'SPR-2025-SEED010', 124, '【seed】Sprint 26W21J','备份脚本演练',       '01', '2026-05-22', '2026-06-04', '2026-05-22', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(220, 'SPR-2025-SEED011', 125, '【seed】Sprint 26W21K','图像识别 PoC',       '01', '2026-05-22', '2026-06-04', '2026-05-22', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(221, 'SPR-2025-SEED012', 126, '【seed】Sprint 26W21L','推送平台联调',       '01', '2026-05-22', '2026-06-04', '2026-05-22', NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
-- 状态 02 已完成 (8 个)
(222, 'SPR-2025-SEED013', 127, '【seed】Sprint 26W18', '对账核心算法',       '02', '2026-04-20', '2026-05-03', '2026-04-20', '2026-05-03', 14, 'seed-runner', NOW(), 'seed', '0'),
(223, 'SPR-2025-SEED014', 128, '【seed】Sprint 26W18B','工单基础页',         '02', '2026-04-20', '2026-05-03', '2026-04-20', '2026-05-03', 14, 'seed-runner', NOW(), 'seed', '0'),
(224, 'SPR-2025-SEED015', 129, '【seed】Sprint 26W18C','报表平台首版',       '02', '2026-04-22', '2026-05-05', '2026-04-22', '2026-05-05', 14, 'seed-runner', NOW(), 'seed', '0'),
(225, 'SPR-2024-SEED001', 138, '【seed】Sprint 25W50', 'CMS 上线 sprint',    '02', '2025-12-10', '2025-12-23', '2025-12-10', '2025-12-23', 14, 'seed-runner', NOW(), 'seed', '0'),
(226, 'SPR-2024-SEED002', 139, '【seed】Sprint 26W04', 'OA 改造收尾',        '02', '2026-01-20', '2026-02-02', '2026-01-20', '2026-02-02', 14, 'seed-runner', NOW(), 'seed', '0'),
(227, 'SPR-2024-SEED003', 140, '【seed】Sprint 26W08', 'CRM 客户表完工',     '02', '2026-02-15', '2026-02-28', '2026-02-15', '2026-02-28', 14, 'seed-runner', NOW(), 'seed', '0'),
(228, 'SPR-2024-SEED004', 141, '【seed】Sprint 26W12', '门户首版上线',       '02', '2026-03-15', '2026-03-28', '2026-03-15', '2026-03-28', 14, 'seed-runner', NOW(), 'seed', '0'),
(229, 'SPR-2024-SEED005', 142, '【seed】Sprint 26W16', 'SSO 联调收尾',       '02', '2026-04-10', '2026-04-23', '2026-04-10', '2026-04-23', 14, 'seed-runner', NOW(), 'seed', '0'),
-- 状态 03 已取消 (5 个)
(230, 'SPR-2024-SEED006', 145, '【seed】Sprint 26W11', '小程序商城撤回',     '03', '2026-03-10', '2026-03-23', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(231, 'SPR-2024-SEED007', 146, '【seed】Sprint 26W12', 'Wiki 预算砍掉',      '03', '2026-03-15', '2026-03-28', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(232, 'SPR-2024-SEED008', 147, '【seed】Sprint 26W13', 'SDK 技术路线变',     '03', '2026-03-22', '2026-04-04', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(233, 'SPR-2024-SEED009', 148, '【seed】Sprint 26W14', '视频会议切外采',     '03', '2026-03-29', '2026-04-11', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0'),
(234, 'SPR-2024-SEED010', 149, '【seed】Sprint 26W17', '区块链溯源暂缓',     '03', '2026-04-20', '2026-05-03', NULL, NULL, 14, 'seed-runner', NOW(), 'seed', '0');

-- ----------------------------
-- 3. 验证
-- ----------------------------
SELECT COUNT(*) AS seed_count, status, COUNT(*) cnt
  FROM tb_sprint
 WHERE remark='seed' AND create_by='seed-runner'
 GROUP BY status WITH ROLLUP;
-- 期望:35 行总计;00=10, 01=12, 02=8, 03=5

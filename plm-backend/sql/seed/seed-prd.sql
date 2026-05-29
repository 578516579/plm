-- =============================================================================
-- seed-prd.sql — 30 个测试 PRD 文档
-- 用途:E2E / 性能测试前批量种入,跑完用 seed-cleanup.sql 清理
-- 前置:先跑过 seed-project.sql + seed-requirement.sql
--       + 已 apply business-prd-add-requirement-id.sql (requirement_id 列存在)
-- 调用: mysql -uroot -p... --default-character-set=utf8mb4 plm < seed-prd.sql
-- =============================================================================

-- ----------------------------
-- 1. 幂等:先清掉之前的 seed
-- ----------------------------
DELETE FROM tb_prd WHERE remark = 'seed' AND create_by = 'seed-runner';

-- ----------------------------
-- 2. 种入 30 个 PRD
--    id 区间 800-829
--    覆盖 4 状态(biz_prd_status):00 草稿(10) / 01 评审中(8) / 02 已确认(8) / 03 已废弃(4)
--    覆盖 4 场景(biz_prd_scene):irrigation / agri_sales / pest_control / traceability
--    覆盖 3 目标用户(biz_prd_target_user):farmer / agronomist / admin
--    project_id 引 seed-project (100-149); requirement_id 部分引 seed-requirement (700-749)
-- ----------------------------
INSERT INTO tb_prd
    (prd_id, prd_no, project_id, requirement_id, title, description,
     scene_template, target_user, content, completeness_score, version,
     ai_generated, ai_generated_at, status, author_user_id, reviewer_user_id,
     create_by, create_time, remark, del_flag)
VALUES
-- 状态 00 草稿 (10 个)
(800, 'PRD-2026-SEED001', 100, NULL, '【seed】首页 KPI 卡片 PRD',                '基于客户反馈',  'agri_sales',   'admin',      '# 背景\n首页缺核心 KPI...\n# 功能\n4 张卡片...', 45.00, 'v0.1', 'N', NULL, '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(801, 'PRD-2026-SEED002', 100, NULL, '【seed】首页暗色模式 PRD',                  '夜间使用',     'agri_sales',   'farmer',     '# 背景\n夜间使用...\n# 功能\ntoggle...', 55.00, 'v0.1', 'N', NULL, '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(802, 'PRD-2026-SEED003', 102, 703,  '【seed】无人机多机协同 PRD',                '田间多机',     'pest_control', 'agronomist', '# 背景\n大田单机不够...\n# 功能\n分区...', 65.00, 'v0.2', 'Y', NOW(), '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(803, 'PRD-2026-SEED004', 103, 704,  '【seed】温室加湿器联动 PRD',                '阈值自动',     'irrigation',   'farmer',     '# 背景\n手动操作低效...\n# 功能\n阈值联动...', 70.00, 'v0.2', 'Y', NOW(), '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(804, 'PRD-2026-SEED005', 104, 705,  '【seed】溯源链对外查询 PRD',                '消费者扫码',   'traceability', 'admin',      '# 背景\n消费者要查...\n# 功能\nQR 查...', 75.00, 'v0.2', 'Y', NOW(), '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(805, 'PRD-2026-SEED006', 105, 706,  '【seed】气象多城市并查 PRD',                '看板',         'agri_sales',   'admin',      '# 背景\n多城市数据看板...\n# 功能\n3 城市并查...', 50.00, 'v0.1', 'N', NULL, '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(806, 'PRD-2026-SEED007', 107, 708,  '【seed】病虫害移动端 PRD',                  '田间手机用',   'pest_control', 'farmer',     '# 背景\n田间无电脑...\n# 功能\nH5 拍照...', 60.00, 'v0.1', 'Y', NOW(), '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(807, 'PRD-2026-SEED008', 108, 709,  '【seed】画像自定义标签 PRD',                '运营标签',     'agri_sales',   'admin',      '# 背景\n运营要标签...\n# 功能\n自定义...', 40.00, 'v0.1', 'N', NULL, '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(808, 'PRD-2026-SEED009', 113, 714,  '【seed】文档协作评论 PRD',                  '协同',         'agri_sales',   'admin',      '# 背景\n评论缺...\n# 功能\n锚定行评论...', 35.00, 'v0.1', 'N', NULL, '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(809, 'PRD-2026-SEED010', 114, NULL, '【seed】协作平台权限 PRD',                  '细粒度权限', 'agri_sales',   'admin',      '# 背景\n权限太粗...\n# 功能\n按文件夹...', 45.00, 'v0.1', 'N', NULL, '00', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
-- 状态 01 评审中 (8 个)
(810, 'PRD-2025-SEED001', 115, 715,  '【seed】架构升级 P1 PRD',                   'ZK→etcd',     'agri_sales',   'admin',      '# 背景\nZK 退役...\n# 功能\n迁移...', 82.00, 'v1.0', 'Y', NOW(), '01', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(811, 'PRD-2025-SEED002', 116, 716,  '【seed】中台用户服务 PRD',                  '统一身份',     'agri_sales',   'admin',      '# 背景\n各系统重复...\n# 功能\n统一 user...', 85.00, 'v1.0', 'Y', NOW(), '01', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(812, 'PRD-2025-SEED003', 117, 718,  '【seed】PLM 项目模块 PRD',                  'CRUD',         'agri_sales',   'admin',      '# 背景\nPLM 基础...\n# 功能\n项目状态机...', 88.00, 'v1.0', 'Y', NOW(), '01', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(813, 'PRD-2025-SEED004', 117, 719,  '【seed】PLM 需求模块 PRD',                  '需求评审',     'agri_sales',   'admin',      '# 背景\nPLM 二级...\n# 功能\n需求状态...', 87.00, 'v1.0', 'Y', NOW(), '01', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(814, 'PRD-2025-SEED005', 118, 720,  '【seed】app 登录 PRD',                       '手机号',       'agri_sales',   'farmer',     '# 背景\napp 用户...\n# 功能\n手机号+OTP...', 83.00, 'v1.0', 'Y', NOW(), '01', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(815, 'PRD-2025-SEED006', 119, 722,  '【seed】数据中台 metadata PRD',             '元数据',       'agri_sales',   'admin',      '# 背景\n元数据散乱...\n# 功能\n元数据 CRUD...', 86.00, 'v1.0', 'Y', NOW(), '01', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(816, 'PRD-2025-SEED007', 120, 723,  '【seed】支付通道签名 PRD',                  'HMAC',         'agri_sales',   'admin',      '# 背景\n通道接入安全...\n# 功能\nHMAC-SHA256...', 90.00, 'v1.0', 'Y', NOW(), '01', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(817, 'PRD-2025-SEED008', 122, 725,  '【seed】会员等级新规则 PRD',                '5→7 段',       'agri_sales',   'admin',      '# 背景\n会员体系升级...\n# 功能\n7 段...', 84.00, 'v1.0', 'Y', NOW(), '01', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
-- 状态 02 已确认 (8 个)
(818, 'PRD-2024-SEED001', 138, 733,  '【seed】CMS PRD 已上线',                    '已上线',       'agri_sales',   'admin',      '# 背景\nCMS 一期...\n# 功能\n文章...', 95.00, 'v1.2', 'Y', NOW(), '02', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(819, 'PRD-2024-SEED002', 138, 734,  '【seed】CMS 富文本 PRD',                    '已上线',       'agri_sales',   'admin',      '# 背景\n编辑器升级...\n# 功能\n富文本...', 92.00, 'v1.1', 'Y', NOW(), '02', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(820, 'PRD-2024-SEED003', 139, 735,  '【seed】OA 审批 PRD',                       '已上线',       'agri_sales',   'admin',      '# 背景\nOA 审批...\n# 功能\n5 节点流...', 96.00, 'v1.0', 'Y', NOW(), '02', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(821, 'PRD-2024-SEED004', 140, 736,  '【seed】CRM 客户管理 PRD',                  '已上线',       'agri_sales',   'admin',      '# 背景\n销售要客户档案...\n# 功能\n客户+联系人+机会...', 94.00, 'v1.0', 'Y', NOW(), '02', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(822, 'PRD-2024-SEED005', 141, 737,  '【seed】门户改版 PRD',                      '已上线',       'agri_sales',   'admin',      '# 背景\n门户老旧...\n# 功能\n响应式+CMS...', 91.00, 'v1.0', 'Y', NOW(), '02', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(823, 'PRD-2024-SEED006', 142, 738,  '【seed】SSO 单点 PRD',                      '已上线',       'agri_sales',   'admin',      '# 背景\n多站重复登录...\n# 功能\nCAS+OAuth2...', 97.00, 'v1.0', 'Y', NOW(), '02', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(824, 'PRD-2024-SEED007', 143, 739,  '【seed】导出工具 PRD',                      '已上线',       'agri_sales',   'admin',      '# 背景\n导出场景多...\n# 功能\nCSV+Excel+PDF...', 89.00, 'v1.0', 'Y', NOW(), '02', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
(825, 'PRD-2024-SEED008', 144, 740,  '【seed】定时任务平台 PRD',                  '已上线',       'agri_sales',   'admin',      '# 背景\nCron 难管...\n# 功能\nUI+日志+监控...', 93.00, 'v1.0', 'Y', NOW(), '02', 1, 1, 'seed-runner', NOW(), 'seed', '0'),
-- 状态 03 已废弃 (4 个)
(826, 'PRD-2024-SEED009', 145, 742,  '【seed】小程序商城 PRD',                    '已废弃',       'agri_sales',   'farmer',     '# 备注\n需求方撤回...', 30.00, 'v0.5', 'N', NULL, '03', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(827, 'PRD-2024-SEED010', 146, 743,  '【seed】Wiki PRD',                          '已废弃',       'agri_sales',   'admin',      '# 备注\n预算砍掉...', 25.00, 'v0.3', 'N', NULL, '03', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(828, 'PRD-2024-SEED011', 148, 745,  '【seed】视频会议 PRD',                      '已废弃',       'agri_sales',   'admin',      '# 备注\n采购成熟方案...', 40.00, 'v0.7', 'N', NULL, '03', 1, NULL, 'seed-runner', NOW(), 'seed', '0'),
(829, 'PRD-2024-SEED012', 149, 746,  '【seed】区块链溯源 PRD',                    '已废弃',       'traceability', 'admin',      '# 备注\n业务方暂缓...', 50.00, 'v0.8', 'N', NULL, '03', 1, NULL, 'seed-runner', NOW(), 'seed', '0');

-- ----------------------------
-- 3. 验证
-- ----------------------------
SELECT COUNT(*) AS seed_count, status, COUNT(*) cnt
  FROM tb_prd
 WHERE remark='seed' AND create_by='seed-runner'
 GROUP BY status WITH ROLLUP;
-- 期望:30 行总计;00=10, 01=8, 02=8, 03=4

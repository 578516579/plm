-- =============================================================================
-- seed-task.sql — 50 个测试任务
-- 用途:E2E / 性能测试前批量种入,跑完用 seed-cleanup.sql 清理
-- 前置:先跑过 seed-project.sql + seed-sprint.sql
-- 调用: mysql -uroot -p... --default-character-set=utf8mb4 plm < seed-task.sql
-- =============================================================================

-- ----------------------------
-- 1. 幂等:先清掉之前的 seed
-- ----------------------------
DELETE FROM tb_task WHERE remark = 'seed' AND create_by = 'seed-runner';

-- ----------------------------
-- 2. 种入 50 个任务
--    id 区间 300-349
--    覆盖 6 种状态(字典 biz_task_status):00 待开发(10) / 01 开发中(12) / 02 代码评审(8) /
--                                          03 测试中(8) / 04 已完成(8) / 05 已取消(4)
--    覆盖 3 种优先级(biz_task_priority):00 P0(10) / 01 P1(20) / 02 P2(20)
--    project_id 引 seed-project(100-149), sprint_id 引 seed-sprint(200-234)
-- ----------------------------
INSERT INTO tb_task
    (task_id, task_no, project_id, requirement_id, sprint_id, title, description,
     status, priority, assignee_user_id, estimated_hours, actual_hours,
     mr_url, mr_branch, create_by, create_time, remark, del_flag)
VALUES
-- 状态 00 待开发 (10 个)
(300, 'TASK-2026-SEED001', 100, NULL, 200, '【seed】大屏首页布局',         '布局栅格 + 头部组件', '00', '01', 1, 8.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(301, 'TASK-2026-SEED002', 100, NULL, 200, '【seed】大屏数据卡片组件',     '4 个 KPI 卡片',       '00', '02', 1, 6.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(302, 'TASK-2026-SEED003', 101, NULL, 201, '【seed】大屏 2 折线图',        'echarts 折线',        '00', '02', 1, 10.0, NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(303, 'TASK-2026-SEED004', 102, NULL, 202, '【seed】航线规划地图',         '高德地图集成',        '00', '00', 1, 16.0, NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(304, 'TASK-2026-SEED005', 103, NULL, 203, '【seed】温室控制 PLC 协议',    'Modbus 调试',         '00', '01', 1, 20.0, NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(305, 'TASK-2026-SEED006', 104, NULL, 204, '【seed】溯源链 hash 节点',     '联调 BSN',            '00', '01', 1, 12.0, NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(306, 'TASK-2026-SEED007', 105, NULL, 205, '【seed】气象 API 对接',        '中国气象网 V3',       '00', '02', 1, 4.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(307, 'TASK-2026-SEED008', 106, NULL, 206, '【seed】土壤湿度传感器驱动',   'I2C 接入',            '00', '02', 1, 8.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(308, 'TASK-2026-SEED009', 107, NULL, 207, '【seed】病虫害标注数据收集',   '田间样本 500 张',     '00', '01', 1, 40.0, NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(309, 'TASK-2026-SEED010', 108, NULL, 208, '【seed】用户画像标签体系',     '草拟 30 标签',        '00', '02', 1, 6.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
-- 状态 01 开发中 (12 个)
(310, 'TASK-2025-SEED001', 115, NULL, 210, '【seed】架构升级 ZK 切 etcd',  '注册中心迁移',        '01', '00', 1, 24.0, NULL, NULL, 'feature/zk-to-etcd',   'seed-runner', NOW(), 'seed', '0'),
(311, 'TASK-2025-SEED002', 116, NULL, 211, '【seed】中台用户服务',          '核心 user-service',   '01', '00', 1, 16.0, NULL, NULL, 'feature/user-svc',      'seed-runner', NOW(), 'seed', '0'),
(312, 'TASK-2025-SEED003', 116, NULL, 211, '【seed】中台 token 缓存',       'redis token',         '01', '01', 1, 8.0,  NULL, NULL, 'feature/token-cache',   'seed-runner', NOW(), 'seed', '0'),
(313, 'TASK-2025-SEED004', 117, NULL, 212, '【seed】PLM 项目 CRUD',         '项目模块代码生成',    '01', '01', 1, 12.0, NULL, NULL, 'feature/plm-project',   'seed-runner', NOW(), 'seed', '0'),
(314, 'TASK-2025-SEED005', 117, NULL, 212, '【seed】PLM 需求 CRUD',         '需求模块代码生成',    '01', '01', 1, 12.0, NULL, NULL, 'feature/plm-req',       'seed-runner', NOW(), 'seed', '0'),
(315, 'TASK-2025-SEED006', 118, NULL, 213, '【seed】移动 app 登录页',       'Vue NativeScript',    '01', '02', 1, 10.0, NULL, NULL, 'feature/mobile-login',  'seed-runner', NOW(), 'seed', '0'),
(316, 'TASK-2025-SEED007', 118, NULL, 213, '【seed】移动 app 我的页',       'profile 页面',        '01', '02', 1, 8.0,  NULL, NULL, 'feature/mobile-me',     'seed-runner', NOW(), 'seed', '0'),
(317, 'TASK-2025-SEED008', 119, NULL, 214, '【seed】数据中台 metadata',     '元数据存储',          '01', '01', 1, 20.0, NULL, NULL, 'feature/metadata',      'seed-runner', NOW(), 'seed', '0'),
(318, 'TASK-2025-SEED009', 120, NULL, 215, '【seed】支付通道签名校验',     'HMAC-SHA256',         '01', '00', 1, 6.0,  NULL, NULL, 'feature/pay-sig',       'seed-runner', NOW(), 'seed', '0'),
(319, 'TASK-2025-SEED010', 121, NULL, 216, '【seed】CDN 切流脚本',          'cli 自动切',          '01', '01', 1, 4.0,  NULL, NULL, 'feature/cdn-switch',    'seed-runner', NOW(), 'seed', '0'),
(320, 'TASK-2025-SEED011', 122, NULL, 217, '【seed】会员等级算法重写',     '积分体系',            '01', '02', 1, 14.0, NULL, NULL, 'feature/member-rank',   'seed-runner', NOW(), 'seed', '0'),
(321, 'TASK-2025-SEED012', 123, NULL, 218, '【seed】反爬规则 V2',           'WAF 联动',            '01', '01', 1, 10.0, NULL, NULL, 'feature/anti-bot-v2',   'seed-runner', NOW(), 'seed', '0'),
-- 状态 02 代码评审 (8 个)
(322, 'TASK-2025-SEED013', 124, NULL, 219, '【seed】备份脚本演练',         'mysqldump cron',      '02', '02', 1, 4.0,  NULL, 'https://gitlab/mr/101', 'feature/backup',  'seed-runner', NOW(), 'seed', '0'),
(323, 'TASK-2025-SEED014', 125, NULL, 220, '【seed】OCR 识别接入 PoC',     'baidu OCR API',       '02', '01', 1, 12.0, NULL, 'https://gitlab/mr/102', 'feature/ocr-poc', 'seed-runner', NOW(), 'seed', '0'),
(324, 'TASK-2025-SEED015', 126, NULL, 221, '【seed】推送服务 HTTP/2',      '换 grpc-go',          '02', '01', 1, 8.0,  NULL, 'https://gitlab/mr/103', 'feature/push-h2', 'seed-runner', NOW(), 'seed', '0'),
(325, 'TASK-2025-SEED016', 127, NULL, 222, '【seed】对账核心精度修正',     'BigDecimal',          '02', '00', 1, 6.0,  NULL, 'https://gitlab/mr/104', 'feature/recon',   'seed-runner', NOW(), 'seed', '0'),
(326, 'TASK-2025-SEED017', 128, NULL, 223, '【seed】工单 SLA 计算',         '超时上报',            '02', '01', 1, 10.0, NULL, 'https://gitlab/mr/105', 'feature/sla',     'seed-runner', NOW(), 'seed', '0'),
(327, 'TASK-2025-SEED018', 129, NULL, 224, '【seed】报表平台分组下钻',     'pivot table',         '02', '02', 1, 14.0, NULL, 'https://gitlab/mr/106', 'feature/pivot',   'seed-runner', NOW(), 'seed', '0'),
(328, 'TASK-2024-SEED001', 138, NULL, 225, '【seed】CMS 富文本编辑器',     'tinymce 接入',        '02', '02', 1, 8.0,  NULL, 'https://gitlab/mr/107', 'feature/cms-rt',  'seed-runner', NOW(), 'seed', '0'),
(329, 'TASK-2024-SEED002', 139, NULL, 226, '【seed】OA 工单导出 Excel',    'POI 库',              '02', '02', 1, 6.0,  NULL, 'https://gitlab/mr/108', 'feature/oa-exp',  'seed-runner', NOW(), 'seed', '0'),
-- 状态 03 测试中 (8 个)
(330, 'TASK-2024-SEED003', 140, NULL, 227, '【seed】CRM 客户列表',         '分页 + 搜索',         '03', '02', 1, 8.0,  NULL, 'https://gitlab/mr/109', 'feature/crm-list','seed-runner', NOW(), 'seed', '0'),
(331, 'TASK-2024-SEED004', 140, NULL, 227, '【seed】CRM 客户详情',         '详情 + tab',          '03', '02', 1, 6.0,  NULL, 'https://gitlab/mr/110', 'feature/crm-det', 'seed-runner', NOW(), 'seed', '0'),
(332, 'TASK-2024-SEED005', 141, NULL, 228, '【seed】门户首页 banner',      '轮播组件',            '03', '01', 1, 4.0,  NULL, 'https://gitlab/mr/111', 'feature/portal-b','seed-runner', NOW(), 'seed', '0'),
(333, 'TASK-2024-SEED006', 141, NULL, 228, '【seed】门户新闻列表',         '搜索 + 分类',         '03', '02', 1, 6.0,  NULL, 'https://gitlab/mr/112', 'feature/portal-n','seed-runner', NOW(), 'seed', '0'),
(334, 'TASK-2024-SEED007', 142, NULL, 229, '【seed】SSO 票据签发',         'JWT RS256',           '03', '00', 1, 10.0, NULL, 'https://gitlab/mr/113', 'feature/sso-jwt', 'seed-runner', NOW(), 'seed', '0'),
(335, 'TASK-2024-SEED008', 142, NULL, 229, '【seed】SSO 第三方接入示例',   'cas/oauth2 mix',      '03', '01', 1, 12.0, NULL, 'https://gitlab/mr/114', 'feature/sso-3rd', 'seed-runner', NOW(), 'seed', '0'),
(336, 'TASK-2024-SEED009', 143, NULL, NULL,'【seed】导出 CSV 引擎',        '通用导出',            '03', '02', 1, 6.0,  NULL, 'https://gitlab/mr/115', 'feature/exp-csv', 'seed-runner', NOW(), 'seed', '0'),
(337, 'TASK-2024-SEED010', 144, NULL, NULL,'【seed】定时任务 Cron 解析',   '复杂表达式',          '03', '02', 1, 8.0,  NULL, 'https://gitlab/mr/116', 'feature/cron-p',  'seed-runner', NOW(), 'seed', '0'),
-- 状态 04 已完成 (8 个)
(338, 'TASK-2024-SEED011', 138, NULL, 225, '【seed】CMS 用户权限模块',     '已上线终态',          '04', '01', 1, 16.0, 18.5, 'https://gitlab/mr/117', 'feature/cms-perm','seed-runner', NOW(), 'seed', '0'),
(339, 'TASK-2024-SEED012', 138, NULL, 225, '【seed】CMS 文章发布流',       '已上线终态',          '04', '02', 1, 12.0, 14.0, 'https://gitlab/mr/118', 'feature/cms-pub', 'seed-runner', NOW(), 'seed', '0'),
(340, 'TASK-2024-SEED013', 139, NULL, 226, '【seed】OA 审批流引擎',        '已上线终态',          '04', '00', 1, 24.0, 28.0, 'https://gitlab/mr/119', 'feature/oa-app',  'seed-runner', NOW(), 'seed', '0'),
(341, 'TASK-2024-SEED014', 140, NULL, 227, '【seed】CRM 销售机会模块',     '已上线终态',          '04', '01', 1, 20.0, 22.5, 'https://gitlab/mr/120', 'feature/crm-opp', 'seed-runner', NOW(), 'seed', '0'),
(342, 'TASK-2024-SEED015', 141, NULL, 228, '【seed】门户首页改版上线',     '已上线终态',          '04', '02', 1, 10.0, 11.0, 'https://gitlab/mr/121', 'feature/portal-r','seed-runner', NOW(), 'seed', '0'),
(343, 'TASK-2024-SEED016', 142, NULL, 229, '【seed】SSO 客户端 SDK',       '已上线终态',          '04', '00', 1, 14.0, 16.0, 'https://gitlab/mr/122', 'feature/sso-sdk', 'seed-runner', NOW(), 'seed', '0'),
(344, 'TASK-2024-SEED017', 143, NULL, NULL,'【seed】通用导出 PDF',         '已上线终态',          '04', '02', 1, 8.0,  9.5,  'https://gitlab/mr/123', 'feature/exp-pdf', 'seed-runner', NOW(), 'seed', '0'),
(345, 'TASK-2024-SEED018', 144, NULL, NULL,'【seed】定时任务监控页',       '已上线终态',          '04', '02', 1, 6.0,  7.0,  'https://gitlab/mr/124', 'feature/cron-mon','seed-runner', NOW(), 'seed', '0'),
-- 状态 05 已取消 (4 个)
(346, 'TASK-2024-SEED019', 145, NULL, 230, '【seed】小程序商品页',         '需求方撤回',          '05', '02', 1, 0.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(347, 'TASK-2024-SEED020', 146, NULL, 231, '【seed】Wiki 全文检索',        '预算砍掉',            '05', '02', 1, 0.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(348, 'TASK-2024-SEED021', 147, NULL, 232, '【seed】SDK iOS 打包',         '技术路线调整',        '05', '01', 1, 0.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0'),
(349, 'TASK-2024-SEED022', 148, NULL, 233, '【seed】视频会议 SFU',         '采购成熟方案',        '05', '00', 1, 0.0,  NULL, NULL, NULL, 'seed-runner', NOW(), 'seed', '0');

-- ----------------------------
-- 3. 验证
-- ----------------------------
SELECT COUNT(*) AS seed_count, status, COUNT(*) cnt
  FROM tb_task
 WHERE remark='seed' AND create_by='seed-runner'
 GROUP BY status WITH ROLLUP;
-- 期望:50 行总计;00=10, 01=12, 02=8, 03=8, 04=8, 05=4

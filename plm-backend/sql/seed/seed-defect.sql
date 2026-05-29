-- =============================================================================
-- seed-defect.sql — 50 个测试缺陷
-- 用途:E2E / 性能测试前批量种入,跑完用 seed-cleanup.sql 清理
-- 前置:先跑过 seed-project.sql + seed-testcase.sql
--       + 已 apply business-defect-add-testcase-id.sql (testcase_id 列存在)
-- 调用: mysql -uroot -p... --default-character-set=utf8mb4 plm < seed-defect.sql
-- =============================================================================

-- ----------------------------
-- 1. 幂等:先清掉之前的 seed
-- ----------------------------
DELETE FROM tb_defect WHERE remark = 'seed' AND create_by = 'seed-runner';

-- ----------------------------
-- 2. 种入 50 个缺陷
--    id 区间 500-549
--    覆盖 5 状态(biz_defect_status):00 新建(12) / 01 已确认(10) / 02 处理中(10) /
--                                     03 已解决(10) / 04 已关闭(8)
--    覆盖 4 严重级别(biz_defect_severity):00 P0(8) / 01 P1(15) / 02 P2(20) / 03 P3(7)
--    覆盖 5 主要 category:01 功能 / 02 性能 / 03 兼容 / 04 安全 / 05 易用
--    project_id 引 seed-project(100-149), testcase_id 部分引 seed-testcase(400-449)
-- ----------------------------
INSERT INTO tb_defect
    (defect_id, defect_no, project_id, testcase_id, sprint_id, task_id, title, description,
     severity, category, status, assignee_user_id, reporter_user_id,
     reproduce_steps, expected_result, actual_result, resolution, tags,
     create_by, create_time, remark, del_flag)
VALUES
-- 状态 00 新建 (12 个)
(500, 'DEFECT-2026-SEED001', 100, 442, NULL, NULL, '【seed】首页加载白屏',         'chrome 91 偶发',           '00', '01', '00', 1, 1, '1. 打开\n2. 偶发', '正常', '白屏 5s',     NULL, 'flaky',         'seed-runner', NOW(), 'seed', '0'),
(501, 'DEFECT-2026-SEED002', 100, NULL, NULL, NULL, '【seed】KPI 卡片数字错位',     '小屏适配',                 '02', '03', '00', 1, 1, '小屏 < 1366',     '对齐', '错位',         NULL, 'css',           'seed-runner', NOW(), 'seed', '0'),
(502, 'DEFECT-2026-SEED003', 101, NULL, NULL, NULL, '【seed】折线 hover tooltip',   'tooltip 闪现',             '03', '01', '00', 1, 1, 'hover 节点',       '驻留', '闪',          NULL, 'ux',            'seed-runner', NOW(), 'seed', '0'),
(503, 'DEFECT-2026-SEED004', 102, NULL, NULL, NULL, '【seed】无人机航线点丢失',    '画 100 点后丢 5',           '00', '01', '00', 1, 1, '画 100 点保存',   '100 点', '95 点',     NULL, 'data-loss',     'seed-runner', NOW(), 'seed', '0'),
(504, 'DEFECT-2026-SEED005', 103, NULL, NULL, NULL, '【seed】温室 PLC 偶发断连',   '长连接 60min 自动断',       '01', '02', '00', 1, 1, '运行 60min',       'keep', '断',          NULL, 'iot-net',       'seed-runner', NOW(), 'seed', '0'),
(505, 'DEFECT-2026-SEED006', 104, NULL, NULL, NULL, '【seed】溯源链双花',         '同 hash 上 2 次',          '00', '04', '00', 1, 1, '提交 2 次同 hash', 'reject', 'accept',   NULL, 'blockchain',    'seed-runner', NOW(), 'seed', '0'),
(506, 'DEFECT-2026-SEED007', 105, NULL, NULL, NULL, '【seed】气象 API 时区错乱',   'V3 UTC vs Asia',           '01', '01', '00', 1, 1, '查询 today',       '本地', 'UTC',         NULL, 'timezone',      'seed-runner', NOW(), 'seed', '0'),
(507, 'DEFECT-2026-SEED008', 106, NULL, NULL, NULL, '【seed】湿度告警漏发',       '< 20% 但无消息',           '00', '01', '00', 1, 1, '湿度调 15%',       '告警', '无',          NULL, 'critical',      'seed-runner', NOW(), 'seed', '0'),
(508, 'DEFECT-2026-SEED009', 107, NULL, NULL, NULL, '【seed】模型推理慢',         'p95 1.2s',                 '02', '02', '00', 1, 1, '调推理',           '< 200ms', '1.2s',     NULL, 'perf',          'seed-runner', NOW(), 'seed', '0'),
(509, 'DEFECT-2026-SEED010', 108, NULL, NULL, NULL, '【seed】画像批量超时',       '1000 行 30s',              '02', '02', '00', 1, 1, '批 1000',          '< 10s', '30s',         NULL, 'perf',          'seed-runner', NOW(), 'seed', '0'),
(510, 'DEFECT-2026-SEED011', 109, NULL, NULL, NULL, '【seed】网关 SSE 断流',      'sse 30s 断',               '01', '01', '00', 1, 1, 'sse 30s',          'keep', '断',          NULL, 'streaming',     'seed-runner', NOW(), 'seed', '0'),
(511, 'DEFECT-2026-SEED012', 110, NULL, NULL, NULL, '【seed】钉钉审批回调延迟',   '30s+',                     '03', '01', '00', 1, 1, '提交单',           '< 5s', '30s+',         NULL, 'integration',   'seed-runner', NOW(), 'seed', '0'),
-- 状态 01 已确认 (10 个)
(512, 'DEFECT-2025-SEED001', 115, NULL, 210, 310, '【seed】etcd 切主丢数据',    'leader 切换 1 条丢',       '00', '01', '01', 1, 1, '杀 leader',       '0 丢', '1 丢',         NULL, 'consistency',   'seed-runner', NOW(), 'seed', '0'),
(513, 'DEFECT-2025-SEED002', 116, 411, 211, 311, '【seed】login 大小写敏感',     'admin vs Admin',           '01', '04', '01', 1, 1, '大写 username',   '一致', '区分',         NULL, 'auth',          'seed-runner', NOW(), 'seed', '0'),
(514, 'DEFECT-2025-SEED003', 116, 412, 211, 312, '【seed】token 缓存击穿',       '雪崩',                     '00', '02', '01', 1, 1, '集中过期',         '平稳', '雪崩',         NULL, 'cache',         'seed-runner', NOW(), 'seed', '0'),
(515, 'DEFECT-2025-SEED004', 117, 413, 212, 313, '【seed】PLM 项目编号重复',    '并发 2 用户同时建',         '01', '01', '01', 1, 1, '并发 2',          '唯一', '重复',         NULL, 'concurrency',   'seed-runner', NOW(), 'seed', '0'),
(516, 'DEFECT-2025-SEED005', 117, 414, 212, 314, '【seed】PLM 列表慢',          '1k 行 3s',                 '02', '02', '01', 1, 1, '1k 行',           '< 1s', '3s',          NULL, 'perf',          'seed-runner', NOW(), 'seed', '0'),
(517, 'DEFECT-2025-SEED006', 118, 416, 213, 315, '【seed】app 启动 push 弹窗', '冷启动闪 push',             '03', '05', '01', 1, 1, '冷启动',           '不弹', '闪现',         NULL, 'ux',            'seed-runner', NOW(), 'seed', '0'),
(518, 'DEFECT-2025-SEED007', 119, 418, 214, 317, '【seed】metadata 查询权限漏', '低权可查 sys',             '00', '04', '01', 1, 1, '低权 token',       'deny', 'allow',      NULL, 'security',      'seed-runner', NOW(), 'seed', '0'),
(519, 'DEFECT-2025-SEED008', 120, 419, 215, 318, '【seed】支付重放',           '同 sign 2 次',             '00', '04', '01', 1, 1, '重放',             'reject', 'accept',   NULL, 'security',      'seed-runner', NOW(), 'seed', '0'),
(520, 'DEFECT-2025-SEED009', 121, 420, 216, 319, '【seed】CDN 切流回滚阻塞', 'rollback 卡死',             '01', '01', '01', 1, 1, '回滚',             '< 5s', '卡',          NULL, 'devops',        'seed-runner', NOW(), 'seed', '0'),
(521, 'DEFECT-2025-SEED010', 122, 421, 217, 320, '【seed】会员积分负数',       '边界 0-1',                 '01', '01', '01', 1, 1, '扣分 -1',          '0', '-1',            NULL, 'precision',     'seed-runner', NOW(), 'seed', '0'),
-- 状态 02 处理中 (10 个)
(522, 'DEFECT-2025-SEED011', 123, 422, 218, 321, '【seed】反爬误杀正常用户',   'rate too low',             '01', '04', '02', 1, 1, '正常用户被封',     '不封', '封',          NULL, 'false-positive','seed-runner', NOW(), 'seed', '0'),
(523, 'DEFECT-2025-SEED012', 124, 423, 219, 322, '【seed】dump 占满 IOPS',     '挤压主库',                 '00', '02', '02', 1, 1, 'dump 期间',        'iops ok', '挤压',     NULL, 'ops',           'seed-runner', NOW(), 'seed', '0'),
(524, 'DEFECT-2025-SEED013', 125, 424, 220, 323, '【seed】OCR 中文 OCR 错',    '辨识率 70%',               '01', '02', '02', 1, 1, '中文票据',         '> 92%', '70%',        NULL, 'ai',            'seed-runner', NOW(), 'seed', '0'),
(525, 'DEFECT-2025-SEED014', 126, 425, 221, 324, '【seed】推送丢消息',         'qos=0',                    '00', '01', '02', 1, 1, '推 10k',           '0 丢', '50 丢',        NULL, 'reliability',   'seed-runner', NOW(), 'seed', '0'),
(526, 'DEFECT-2025-SEED015', 127, 426, 222, 325, '【seed】对账精度误差',       '0.01 误差',                '01', '01', '02', 1, 1, '0.1+0.2',          '0.3', '0.30000004',    NULL, 'precision',     'seed-runner', NOW(), 'seed', '0'),
(527, 'DEFECT-2025-SEED016', 128, 427, 223, 326, '【seed】SLA 计算时区',       'UTC 算',                   '02', '01', '02', 1, 1, '8h 上海',          '正确', '+8 错',       NULL, 'timezone',      'seed-runner', NOW(), 'seed', '0'),
(528, 'DEFECT-2025-SEED017', 129, 428, 224, 327, '【seed】报表下钻无数据',    'NPE',                      '01', '01', '02', 1, 1, '下钻空',           '空状态', '500',         NULL, 'bug',           'seed-runner', NOW(), 'seed', '0'),
(529, 'DEFECT-2024-SEED001', 138, 429, 225, 328, '【seed】CMS 富文本 XSS',     'script 注入',              '00', '04', '02', 1, 1, '粘 <script>',      '过滤', '执行',         NULL, 'security',      'seed-runner', NOW(), 'seed', '0'),
(530, 'DEFECT-2024-SEED002', 138, 430, 225, 329, '【seed】CMS 登录验证码缓存', '缓存穿透',                 '02', '04', '02', 1, 1, '空 captcha',       'reject', '过',         NULL, 'security',      'seed-runner', NOW(), 'seed', '0'),
(531, 'DEFECT-2024-SEED003', 139, 433, 226, 329, '【seed】OA 驳回回写人错',    '不是原提交人',             '02', '01', '02', 1, 1, '驳回',             '原人', '上级',         NULL, 'workflow',      'seed-runner', NOW(), 'seed', '0'),
-- 状态 03 已解决 (10 个)
(532, 'DEFECT-2024-SEED004', 138, 432, 225, 338, '【seed】CMS 权限 403 死循环',  '跳登录又被踢回',         '01', '01', '03', 1, 1, '低权访问',         '403 + stop', '循环',     '加 redirect=null', 'fixed-router', 'seed-runner', NOW(), 'seed', '0'),
(533, 'DEFECT-2024-SEED005', 139, 434, 226, 340, '【seed】OA 工单导出格式',     'date 错',                 '03', '05', '03', 1, 1, '导出 excel',       'YYYY-MM-DD', '14天',  '改用 Date 格式器',  'fixed-poi',    'seed-runner', NOW(), 'seed', '0'),
(534, 'DEFECT-2024-SEED006', 140, 435, 227, 341, '【seed】CRM 分页 OFFSET 大',  '50w 行慢',                '02', '02', '03', 1, 1, 'offset 100k',      '< 200ms', '5s',        '改 cursor 分页',    'fixed-sql',    'seed-runner', NOW(), 'seed', '0'),
(535, 'DEFECT-2024-SEED007', 140, 436, 227, 341, '【seed】CRM 搜索特殊字符',   'SQL 注入',                '00', '04', '03', 1, 1, 'name like 注入 payload', 'escape', 'inject', '加预编译参数',      'fixed-sqli',   'seed-runner', NOW(), 'seed', '0'),
(536, 'DEFECT-2024-SEED008', 141, 437, 228, 342, '【seed】门户 banner 死链',    '404',                     '02', '01', '03', 1, 1, '点 banner',        '正常页', '404',        '修资源 url',        'fixed-link',   'seed-runner', NOW(), 'seed', '0'),
(537, 'DEFECT-2024-SEED009', 142, 438, 229, 343, '【seed】SSO 跨域 cookie 丢', 'samesite=lax',           '01', '04', '03', 1, 1, '跨域调',           'cookie 带', '丢',      '改 samesite=None',  'fixed-cors',   'seed-runner', NOW(), 'seed', '0'),
(538, 'DEFECT-2024-SEED010', 142, 439, 229, 343, '【seed】SSO logout 残留',     '部分子站漏踢',           '01', '04', '03', 1, 1, '登出',             '全踢', '剩 1 站',     '改广播登出',        'fixed-bus',    'seed-runner', NOW(), 'seed', '0'),
(539, 'DEFECT-2024-SEED011', 143, 440, NULL, 344, '【seed】CSV 大文件 OOM',     '10w 行 heap',           '00', '02', '03', 1, 1, '10w 行',           '不 OOM', 'OOM',        '改流式写',          'fixed-stream', 'seed-runner', NOW(), 'seed', '0'),
(540, 'DEFECT-2024-SEED012', 144, 441, NULL, 345, '【seed】Cron 漏触发',         '高负载',                '01', '01', '03', 1, 1, '负载 80%',         '准点', '漏 1 次',     '提高线程池',        'fixed-pool',   'seed-runner', NOW(), 'seed', '0'),
(541, 'DEFECT-2024-SEED013', 138, 431, 225, 339, '【seed】CMS 富文本图片粘贴', 'base64 撑爆',           '01', '01', '03', 1, 1, '粘 5MB 图',        '提示压', '保存爆',     '> 1MB 提示压',      'fixed-validate','seed-runner', NOW(), 'seed', '0'),
-- 状态 04 已关闭 (8 个)
(542, 'DEFECT-2024-SEED014', 138, NULL, 225, 338, '【seed】CMS rejected 已关闭', 'wont fix',                '03', '99', '04', 1, 1, NULL, NULL, NULL, '产品决定不修', 'wontfix',     'seed-runner', NOW(), 'seed', '0'),
(543, 'DEFECT-2024-SEED015', 138, NULL, 225, 338, '【seed】CMS 重复 bug',       'duplicate',               '03', '99', '04', 1, 1, NULL, NULL, NULL, 'dup #532',     'duplicate',    'seed-runner', NOW(), 'seed', '0'),
(544, 'DEFECT-2024-SEED016', 139, NULL, 226, 340, '【seed】OA 无法复现',         'cannot reproduce',        '02', '99', '04', 1, 1, NULL, NULL, NULL, '无法复现',     'cnr',         'seed-runner', NOW(), 'seed', '0'),
(545, 'DEFECT-2024-SEED017', 140, NULL, 227, 341, '【seed】CRM 环境问题',        'env issue',               '03', '99', '04', 1, 1, NULL, NULL, NULL, '测试环境问题', 'env',         'seed-runner', NOW(), 'seed', '0'),
(546, 'DEFECT-2024-SEED018', 141, NULL, 228, 342, '【seed】门户 IE 兼容',       'IE EOL',                  '03', '03', '04', 1, 1, NULL, NULL, NULL, 'IE EOL 不再支持', 'eol',     'seed-runner', NOW(), 'seed', '0'),
(547, 'DEFECT-2024-SEED019', 142, NULL, 229, 343, '【seed】SSO 老版浏览器',     'TLS 1.0',                 '03', '03', '04', 1, 1, NULL, NULL, NULL, '已淘汰',        'eol',         'seed-runner', NOW(), 'seed', '0'),
(548, 'DEFECT-2024-SEED020', 143, NULL, NULL, 344, '【seed】CSV 特殊文件名',     '中文 + 空格',             '03', '05', '04', 1, 1, NULL, NULL, NULL, '已修',          'fixed',       'seed-runner', NOW(), 'seed', '0'),
(549, 'DEFECT-2024-SEED021', 144, NULL, NULL, 345, '【seed】Cron UI 显示偏移',   'timezone display',        '03', '05', '04', 1, 1, NULL, NULL, NULL, '已修',          'fixed',       'seed-runner', NOW(), 'seed', '0');

-- ----------------------------
-- 3. 验证
-- ----------------------------
SELECT COUNT(*) AS seed_count, status, COUNT(*) cnt
  FROM tb_defect
 WHERE remark='seed' AND create_by='seed-runner'
 GROUP BY status WITH ROLLUP;
-- 期望:50 行总计;00=12, 01=10, 02=10, 03=10, 04=8

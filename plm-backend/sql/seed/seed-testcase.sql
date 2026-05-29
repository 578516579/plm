-- =============================================================================
-- seed-testcase.sql — 50 个测试用例
-- 用途:E2E / 性能测试前批量种入,跑完用 seed-cleanup.sql 清理
-- 前置:先跑过 seed-project.sql
-- 调用: mysql -uroot -p... --default-character-set=utf8mb4 plm < seed-testcase.sql
-- =============================================================================

-- ----------------------------
-- 1. 幂等:先清掉之前的 seed
-- ----------------------------
DELETE FROM tb_testcase WHERE remark = 'seed' AND create_by = 'seed-runner';

-- ----------------------------
-- 2. 种入 50 个测试用例
--    id 区间 400-449
--    覆盖 5 状态(biz_testcase_status):00 草稿(10) / 01 待执行(12) / 02 执行中(8) /
--                                       03 已通过(12) / 04 已失败(8)
--    覆盖 3 优先级(biz_testcase_priority):00 P0(10) / 01 P1(25) / 02 P2(15)
--    覆盖 4 主要 category:01 功能 / 02 接口 / 03 性能 / 06 E2E
-- ----------------------------
INSERT INTO tb_testcase
    (testcase_id, testcase_no, project_id, requirement_id, title, description,
     category, priority, status, preconditions, steps, expected_result, actual_result,
     is_automated, automation_script_path, execution_count, last_executed_at, tags,
     create_by, create_time, remark, del_flag)
VALUES
-- 状态 00 草稿 (10 个)
(400, 'TC-2026-SEED001', 100, NULL, '【seed】大屏首页加载', '验证首页能加载',           '01', '01', '00', '已登录', '1. 打开首页\n2. 等 3 秒', '页面渲染正常',         NULL, 'N', NULL, 0, NULL, 'smoke',            'seed-runner', NOW(), 'seed', '0'),
(401, 'TC-2026-SEED002', 100, NULL, '【seed】大屏组件刷新', '5s 自动刷新',               '01', '02', '00', '已登录', '观察 5s 后数据更新',     '数据变化',             NULL, 'N', NULL, 0, NULL, 'smoke',            'seed-runner', NOW(), 'seed', '0'),
(402, 'TC-2026-SEED003', 101, NULL, '【seed】大屏 2 折线渲染', '折线图正常',             '01', '01', '00', '已登录', '进入页面看图表',         '折线渲染',             NULL, 'N', NULL, 0, NULL, 'chart',            'seed-runner', NOW(), 'seed', '0'),
(403, 'TC-2026-SEED004', 102, NULL, '【seed】航线规划保存',   '规划成功保存',           '01', '00', '00', '已登录', '画点 → 保存',           '保存成功',             NULL, 'N', NULL, 0, NULL, 'core',             'seed-runner', NOW(), 'seed', '0'),
(404, 'TC-2026-SEED005', 103, NULL, '【seed】PLC 协议握手',   'Modbus 连通',             '02', '00', '00', '设备就绪', '发起握手包',          'ACK 返回',             NULL, 'N', NULL, 0, NULL, 'iot',              'seed-runner', NOW(), 'seed', '0'),
(405, 'TC-2026-SEED006', 104, NULL, '【seed】溯源 hash 上链', 'BSN 接入',                '02', '01', '00', '链就绪', '提交批次 hash',          '返回 tx',              NULL, 'N', NULL, 0, NULL, 'blockchain',       'seed-runner', NOW(), 'seed', '0'),
(406, 'TC-2026-SEED007', 105, NULL, '【seed】气象 API 重试', 'V3 网络抖动',              '02', '02', '00', '网络可达', '故意断网 1s',          '自动重试 3 次',        NULL, 'N', NULL, 0, NULL, 'integration',      'seed-runner', NOW(), 'seed', '0'),
(407, 'TC-2026-SEED008', 106, NULL, '【seed】土壤湿度告警', '阈值触发',                 '01', '01', '00', '传感器接入', '湿度<20%',            '告警推送',             NULL, 'N', NULL, 0, NULL, 'alert',            'seed-runner', NOW(), 'seed', '0'),
(408, 'TC-2026-SEED009', 107, NULL, '【seed】病虫害模型推理','输入 1 张图',              '03', '02', '00', '模型加载', '调推理接口',          'top1 类别 + 置信度',   NULL, 'N', NULL, 0, NULL, 'ai',               'seed-runner', NOW(), 'seed', '0'),
(409, 'TC-2026-SEED010', 108, NULL, '【seed】画像批量打标',  '批 1000',                  '03', '02', '00', '清单就绪', '批量打',              '<10s 完成',            NULL, 'N', NULL, 0, NULL, 'bulk',             'seed-runner', NOW(), 'seed', '0'),
-- 状态 01 待执行 (12 个)
(410, 'TC-2025-SEED001', 115, NULL, '【seed】etcd 主节点切换',  '故障转移',              '01', '00', '01', '已部署 etcd', '杀掉 leader',       '30s 内重选',           NULL, 'N', NULL, 0, NULL, 'ha',               'seed-runner', NOW(), 'seed', '0'),
(411, 'TC-2025-SEED002', 116, NULL, '【seed】user-service 登录','POST /login',          '02', '00', '01', '中台启动', '用 admin/admin123',    '返回 token',           NULL, 'Y', '/auto/login.spec.ts', 0, NULL, 'auth',  'seed-runner', NOW(), 'seed', '0'),
(412, 'TC-2025-SEED003', 116, NULL, '【seed】token 缓存命中率','redis 验证',            '03', '01', '01', '中台启动', '压测 100 qps',        '命中率 > 95%',         NULL, 'N', NULL, 0, NULL, 'perf',             'seed-runner', NOW(), 'seed', '0'),
(413, 'TC-2025-SEED004', 117, NULL, '【seed】PLM 项目新增',     'POST /business/project','02', '01', '01', 'PLM 启动', '提交合法 body',       '200 + id',             NULL, 'Y', '/auto/project-create.spec.ts', 0, NULL, 'core', 'seed-runner', NOW(), 'seed', '0'),
(414, 'TC-2025-SEED005', 117, NULL, '【seed】PLM 项目列表分页','分页 page=2',          '02', '01', '01', '50 条数据', '调 list page=2 size=10','返回 10 行',          NULL, 'Y', '/auto/project-list.spec.ts',   0, NULL, 'pagination', 'seed-runner', NOW(), 'seed', '0'),
(415, 'TC-2025-SEED006', 117, NULL, '【seed】PLM 需求状态推进', '00 → 01',              '01', '00', '01', '需求新增', '点开发中',             '状态变 01',            NULL, 'N', NULL, 0, NULL, 'state-machine',    'seed-runner', NOW(), 'seed', '0'),
(416, 'TC-2025-SEED007', 118, NULL, '【seed】移动 app 启动时间','首屏 < 2s',            '03', '01', '01', '装机', '冷启动',                 '<2s',                  NULL, 'N', NULL, 0, NULL, 'perf',             'seed-runner', NOW(), 'seed', '0'),
(417, 'TC-2025-SEED008', 118, NULL, '【seed】app 我的页跳转',   '路由正确',              '06', '02', '01', '已登录', '点底部 Tab',            '跳转 my',              NULL, 'N', NULL, 0, NULL, 'e2e',              'seed-runner', NOW(), 'seed', '0'),
(418, 'TC-2025-SEED009', 119, NULL, '【seed】metadata 查询',    'GET /meta/:table',      '02', '01', '01', '中台启动', '查询 user 表',          '返回字段列表',         NULL, 'N', NULL, 0, NULL, 'meta',             'seed-runner', NOW(), 'seed', '0'),
(419, 'TC-2025-SEED010', 120, NULL, '【seed】支付签名校验',     'HMAC 错签',             '04', '00', '01', '通道连通', '篡改签名',              '拒绝请求',             NULL, 'N', NULL, 0, NULL, 'security',         'seed-runner', NOW(), 'seed', '0'),
(420, 'TC-2025-SEED011', 121, NULL, '【seed】CDN 切流回滚',     'rollback 5s 内',        '01', '01', '01', '切流脚本', '执行 rollback',         '< 5s',                 NULL, 'N', NULL, 0, NULL, 'devops',           'seed-runner', NOW(), 'seed', '0'),
(421, 'TC-2025-SEED012', 122, NULL, '【seed】会员等级迁移幂等', '重跑无副作用',          '02', '01', '01', '老库存在', '迁移脚本跑 2 次',       '结果一致',             NULL, 'N', NULL, 0, NULL, 'migration',        'seed-runner', NOW(), 'seed', '0'),
-- 状态 02 执行中 (8 个)
(422, 'TC-2025-SEED013', 123, NULL, '【seed】反爬 V2 IP 黑名单','加黑 + 拦截',          '04', '01', '02', 'WAF 就绪', '注入测试 IP',           '拦截',                 NULL, 'N', NULL, 1, NOW(), 'security',        'seed-runner', NOW(), 'seed', '0'),
(423, 'TC-2025-SEED014', 124, NULL, '【seed】mysqldump 全量备份','15GB 库',              '03', '01', '02', '主库在', '执行 dump',              '< 30 min',             NULL, 'N', NULL, 1, NOW(), 'backup',          'seed-runner', NOW(), 'seed', '0'),
(424, 'TC-2025-SEED015', 125, NULL, '【seed】OCR 准确率',        'baidu V3',             '03', '00', '02', '票据 100 张', '批量调',             '准确率 > 92%',         NULL, 'N', NULL, 2, NOW(), 'ai',              'seed-runner', NOW(), 'seed', '0'),
(425, 'TC-2025-SEED016', 126, NULL, '【seed】推送 H2 吞吐',      '10k msg/s',            '03', '01', '02', '推送启动', 'k6 压',                 '< 100ms p95',          NULL, 'N', NULL, 1, NOW(), 'perf',            'seed-runner', NOW(), 'seed', '0'),
(426, 'TC-2025-SEED017', 127, NULL, '【seed】对账精度小数',     '12 位小数',             '01', '00', '02', '账户在', '提交 0.1+0.2',          '0.3 不丢',             NULL, 'N', NULL, 1, NOW(), 'precision',       'seed-runner', NOW(), 'seed', '0'),
(427, 'TC-2025-SEED018', 128, NULL, '【seed】工单 SLA 超时',     '8h 后告警',             '01', '01', '02', '工单创建', '等 8h',                 '推送告警',             NULL, 'N', NULL, 1, NOW(), 'sla',             'seed-runner', NOW(), 'seed', '0'),
(428, 'TC-2025-SEED019', 129, NULL, '【seed】报表透视下钻',     'pivot 3 层',           '06', '02', '02', '数据就绪', '点 GMV 下钻',           '展开 3 层',            NULL, 'N', NULL, 1, NOW(), 'e2e',             'seed-runner', NOW(), 'seed', '0'),
(429, 'TC-2024-SEED001', 138, NULL, '【seed】CMS 富文本粘贴',    'word 粘贴',            '01', '02', '02', '编辑器加载', '粘 100 行 word',       '格式保留',             NULL, 'N', NULL, 1, NOW(), 'editor',          'seed-runner', NOW(), 'seed', '0'),
-- 状态 03 已通过 (12 个)
(430, 'TC-2024-SEED002', 138, NULL, '【seed】CMS 用户登录',     '登录主流程',           '06', '00', '03', '环境就绪', '登录',                  '跳到 home',            '通过',  'Y', '/auto/cms-login.spec.ts',   5, NOW(), 'e2e',          'seed-runner', NOW(), 'seed', '0'),
(431, 'TC-2024-SEED003', 138, NULL, '【seed】CMS 文章保存草稿', '保存 + 重读',          '01', '01', '03', '编辑器打开', '点保存',               '重读一致',             '通过',  'Y', '/auto/cms-save.spec.ts',    8, NOW(), 'core',         'seed-runner', NOW(), 'seed', '0'),
(432, 'TC-2024-SEED004', 138, NULL, '【seed】CMS 权限拒绝',     '低权用户',             '04', '00', '03', '低权 token', '调 admin 接口',        '403',                  '通过',  'Y', '/auto/cms-perm.spec.ts',    7, NOW(), 'security',     'seed-runner', NOW(), 'seed', '0'),
(433, 'TC-2024-SEED005', 139, NULL, '【seed】OA 审批驳回流',    '驳回回到草稿',         '01', '01', '03', '提交单', '驳回',                    '状态草稿',             '通过',  'Y', '/auto/oa-reject.spec.ts',   4, NOW(), 'flow',         'seed-runner', NOW(), 'seed', '0'),
(434, 'TC-2024-SEED006', 139, NULL, '【seed】OA 工单导出 Excel', 'POI 库',              '01', '02', '03', '工单 50 条', '导出',                 '50 行 excel',          '通过',  'N', NULL, 3, NOW(), 'export',         'seed-runner', NOW(), 'seed', '0'),
(435, 'TC-2024-SEED007', 140, NULL, '【seed】CRM 列表分页',     'page 1/2/3',           '06', '01', '03', '50 条数据', '翻页',                 '准确',                 '通过',  'Y', '/auto/crm-list.spec.ts',    6, NOW(), 'pagination',   'seed-runner', NOW(), 'seed', '0'),
(436, 'TC-2024-SEED008', 140, NULL, '【seed】CRM 客户搜索',     'name like',            '06', '01', '03', '客户 100', '搜 张三',               '只匹配的',             '通过',  'Y', '/auto/crm-search.spec.ts',  9, NOW(), 'search',       'seed-runner', NOW(), 'seed', '0'),
(437, 'TC-2024-SEED009', 141, NULL, '【seed】门户首页 banner',  '轮播 5 张',            '01', '02', '03', '已上线', '看首页',                  '自动轮播',             '通过',  'N', NULL, 2, NOW(), 'frontend',       'seed-runner', NOW(), 'seed', '0'),
(438, 'TC-2024-SEED010', 142, NULL, '【seed】SSO 一次登录多站', '票据共享',             '06', '00', '03', 'sso 部署', '登 A 站,跳 B',         'B 站自动登',           '通过',  'Y', '/auto/sso-cross.spec.ts',   10, NOW(), 'sso',         'seed-runner', NOW(), 'seed', '0'),
(439, 'TC-2024-SEED011', 142, NULL, '【seed】SSO logout 全站',   '登出',                '04', '00', '03', '已登录 SSO', '登出',                'A/B 全失效',           '通过',  'Y', '/auto/sso-logout.spec.ts',  10, NOW(), 'sso',         'seed-runner', NOW(), 'seed', '0'),
(440, 'TC-2024-SEED012', 143, NULL, '【seed】导出 CSV 大文件',  '100k 行',              '03', '01', '03', '数据就绪', '导出',                  '< 60s',                '通过',  'N', NULL, 1, NOW(), 'export',         'seed-runner', NOW(), 'seed', '0'),
(441, 'TC-2024-SEED013', 144, NULL, '【seed】Cron 复杂表达式',  '*/15 * * * * *',       '02', '02', '03', '调度启动', '15s 跑 1 次',           '准点',                 '通过',  'Y', '/auto/cron-15s.spec.ts',    20, NOW(), 'schedule',    'seed-runner', NOW(), 'seed', '0'),
-- 状态 04 已失败 (8 个)
(442, 'TC-2024-SEED014', 145, NULL, '【seed】小程序商品上下架', '已撤回',              '01', '02', '04', NULL, '撤回',                       NULL,                   '撤回',  'N', NULL, 1, NOW(), 'cancelled',      'seed-runner', NOW(), 'seed', '0'),
(443, 'TC-2024-SEED015', 146, NULL, '【seed】Wiki 全文检索',   '预算砍掉',              '01', '02', '04', NULL, '砍掉',                       NULL,                   '砍掉',  'N', NULL, 1, NOW(), 'cancelled',      'seed-runner', NOW(), 'seed', '0'),
(444, 'TC-2024-SEED016', 147, NULL, '【seed】SDK iOS 打包',     '签名失败',              '02', '01', '04', '证书有效', '打包',                   '应该成功',             '签名错','N', NULL, 1, NOW(), 'flaky',          'seed-runner', NOW(), 'seed', '0'),
(445, 'TC-2024-SEED017', 148, NULL, '【seed】视频会议 SFU 加载','超时',                 '03', '01', '04', '服务在', '入会',                    '< 3s',                 '6s',    'N', NULL, 2, NOW(), 'perf-fail',      'seed-runner', NOW(), 'seed', '0'),
(446, 'TC-2024-SEED018', 149, NULL, '【seed】区块链溯源回查',  '业务暂缓',              '02', '02', '04', NULL, '回查',                       NULL,                   '暂缓',  'N', NULL, 1, NOW(), 'cancelled',      'seed-runner', NOW(), 'seed', '0'),
(447, 'TC-2024-SEED019', 138, NULL, '【seed】CMS 并发编辑冲突',  '乐观锁冲突',          '01', '00', '04', '2 用户编辑同条', 'A 保存后 B 保存',     'B 应报冲突',           'B 覆盖','N', NULL, 1, NOW(), 'concurrency',    'seed-runner', NOW(), 'seed', '0'),
(448, 'TC-2024-SEED020', 140, NULL, '【seed】CRM 批量删除',     '批 100 行',            '01', '01', '04', '100 客户', '勾选删除',                '全部删除',             '40 失败','N',NULL, 2, NOW(), 'bulk-fail',      'seed-runner', NOW(), 'seed', '0'),
(449, 'TC-2024-SEED021', 142, NULL, '【seed】SSO 跨域 cookie', 'cross-domain',          '04', '00', '04', '已部署', '跨域调 sso',              'cookie 带上',          '丢',    'N', NULL, 3, NOW(), 'cors-bug',       'seed-runner', NOW(), 'seed', '0');

-- ----------------------------
-- 3. 验证
-- ----------------------------
SELECT COUNT(*) AS seed_count, status, COUNT(*) cnt
  FROM tb_testcase
 WHERE remark='seed' AND create_by='seed-runner'
 GROUP BY status WITH ROLLUP;
-- 期望:50 行总计;00=10, 01=12, 02=8, 03=12, 04=8

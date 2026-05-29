-- =============================================================================
-- seed-requirement.sql — 50 个测试需求
-- 用途:E2E / 性能测试前批量种入,跑完用 seed-cleanup.sql 清理
-- 前置:先跑过 seed-project.sql
--       + 已 apply business-requirement-widen-dict-cols.sql (VARCHAR(20) 字典列)
-- 调用: mysql -uroot -p... --default-character-set=utf8mb4 plm < seed-requirement.sql
-- =============================================================================

-- ----------------------------
-- 1. 幂等:先清掉之前的 seed
-- ----------------------------
DELETE FROM tb_requirement WHERE remark = 'seed' AND create_by = 'seed-runner';

-- ----------------------------
-- 2. 种入 50 个需求
--    id 区间 700-749
--    覆盖 4 状态(biz_req_status):00 待评审(15) / 01 开发中(15) / 02 已完成(12) / 03 已取消(8)
--    覆盖 3 优先级(biz_req_priority):00 P0(10) / 01 P1(20) / 02 P2(20)
--    覆盖 4 来源(biz_req_source):01 客户(20) / 02 内部(15) / 03 运营(10) / 04 竞品(5)
-- ----------------------------
INSERT INTO tb_requirement
    (requirement_id, requirement_no, project_id, title, description,
     source, priority, status, assignee_user_id, review_note, ai_evaluation,
     create_by, create_time, remark, del_flag)
VALUES
-- 状态 00 待评审 (15 个)
(700, 'REQ-2026-SEED001', 100, '【seed】首页 KPI 卡片新增 4 项',           '客户希望首页直接看 GMV/PV/UV/订单数',  '01', '01', '00', 1, NULL,        'high',   'seed-runner', NOW(), 'seed', '0'),
(701, 'REQ-2026-SEED002', 100, '【seed】首页支持暗色模式',                 '夜间使用频繁',                          '01', '02', '00', 1, NULL,        'medium', 'seed-runner', NOW(), 'seed', '0'),
(702, 'REQ-2026-SEED003', 101, '【seed】折线图支持区间选择',               '日/周/月切换',                          '02', '02', '00', 1, NULL,        'medium', 'seed-runner', NOW(), 'seed', '0'),
(703, 'REQ-2026-SEED004', 102, '【seed】航线规划支持多机协同',             '多无人机分区',                          '04', '00', '00', 1, NULL,        'high',   'seed-runner', NOW(), 'seed', '0'),
(704, 'REQ-2026-SEED005', 103, '【seed】温室加湿器联动控制',               '湿度<阈值自动开',                       '01', '00', '00', 1, NULL,        'high',   'seed-runner', NOW(), 'seed', '0'),
(705, 'REQ-2026-SEED006', 104, '【seed】溯源链对外查询页',                 '消费者扫码',                            '01', '01', '00', 1, NULL,        'high',   'seed-runner', NOW(), 'seed', '0'),
(706, 'REQ-2026-SEED007', 105, '【seed】气象数据支持 3 个城市并查',         '内部数据看板',                          '03', '02', '00', 1, NULL,        'low',    'seed-runner', NOW(), 'seed', '0'),
(707, 'REQ-2026-SEED008', 106, '【seed】土壤传感器接入硬件 V2',             'V2 协议升级',                           '02', '01', '00', 1, NULL,        'medium', 'seed-runner', NOW(), 'seed', '0'),
(708, 'REQ-2026-SEED009', 107, '【seed】病虫害识别支持移动端',             '田间手机用',                            '01', '00', '00', 1, NULL,        'high',   'seed-runner', NOW(), 'seed', '0'),
(709, 'REQ-2026-SEED010', 108, '【seed】用户画像支持自定义标签',           '内部标签运营',                          '03', '02', '00', 1, NULL,        'low',    'seed-runner', NOW(), 'seed', '0'),
(710, 'REQ-2026-SEED011', 109, '【seed】网关支持 gRPC',                     'API 协议',                              '02', '01', '00', 1, NULL,        'medium', 'seed-runner', NOW(), 'seed', '0'),
(711, 'REQ-2026-SEED012', 110, '【seed】钉钉审批支持加签',                 '组织升级',                              '01', '02', '00', 1, NULL,        'medium', 'seed-runner', NOW(), 'seed', '0'),
(712, 'REQ-2026-SEED013', 111, '【seed】日志按服务自动归档',               '运维侧',                                '03', '02', '00', 1, NULL,        'low',    'seed-runner', NOW(), 'seed', '0'),
(713, 'REQ-2026-SEED014', 112, '【seed】权限系统支持数据权限',             '行级权限',                              '02', '00', '00', 1, NULL,        'high',   'seed-runner', NOW(), 'seed', '0'),
(714, 'REQ-2026-SEED015', 113, '【seed】文档协作支持评论',                 '协同编辑场景',                          '04', '01', '00', 1, NULL,        'medium', 'seed-runner', NOW(), 'seed', '0'),
-- 状态 01 开发中 (15 个)
(715, 'REQ-2025-SEED001', 115, '【seed】架构升级 P1',                       '注册中心 ZK 到 etcd',                   '02', '00', '01', 1, '架构组已评审通过',  'high',   'seed-runner', NOW(), 'seed', '0'),
(716, 'REQ-2025-SEED002', 116, '【seed】中台用户服务',                     '统一身份',                              '02', '00', '01', 1, '架构组已评审通过',  'high',   'seed-runner', NOW(), 'seed', '0'),
(717, 'REQ-2025-SEED003', 116, '【seed】中台 token 缓存',                  '减少 DB 压力',                          '02', '01', '01', 1, '已评审',            'medium', 'seed-runner', NOW(), 'seed', '0'),
(718, 'REQ-2025-SEED004', 117, '【seed】PLM 项目模块',                      'CRUD + 状态机',                         '02', '01', '01', 1, 'PLM 内核',         'high',   'seed-runner', NOW(), 'seed', '0'),
(719, 'REQ-2025-SEED005', 117, '【seed】PLM 需求模块',                      'CRUD + 评审',                           '02', '01', '01', 1, 'PLM 内核',         'high',   'seed-runner', NOW(), 'seed', '0'),
(720, 'REQ-2025-SEED006', 118, '【seed】移动 app 登录',                     '手机号登录',                            '01', '02', '01', 1, '已评审',            'medium', 'seed-runner', NOW(), 'seed', '0'),
(721, 'REQ-2025-SEED007', 118, '【seed】app 个人中心',                      'profile + 设置',                        '01', '02', '01', 1, '已评审',            'low',    'seed-runner', NOW(), 'seed', '0'),
(722, 'REQ-2025-SEED008', 119, '【seed】数据中台 metadata',                 '元数据管理',                            '02', '01', '01', 1, '架构组已评审',      'medium', 'seed-runner', NOW(), 'seed', '0'),
(723, 'REQ-2025-SEED009', 120, '【seed】支付通道签名',                      'HMAC',                                  '01', '00', '01', 1, '安全组已评审',      'high',   'seed-runner', NOW(), 'seed', '0'),
(724, 'REQ-2025-SEED010', 121, '【seed】CDN 切流脚本',                      '自动切',                                '03', '01', '01', 1, '运维已评审',        'medium', 'seed-runner', NOW(), 'seed', '0'),
(725, 'REQ-2025-SEED011', 122, '【seed】会员等级新规则',                    '5 段 → 7 段',                           '01', '02', '01', 1, '产品已评审',        'medium', 'seed-runner', NOW(), 'seed', '0'),
(726, 'REQ-2025-SEED012', 123, '【seed】反爬 V2 规则',                      '动态阈值',                              '03', '01', '01', 1, '安全已评审',        'medium', 'seed-runner', NOW(), 'seed', '0'),
(727, 'REQ-2025-SEED013', 124, '【seed】每日全量备份',                      '15GB 库',                               '03', '02', '01', 1, '已评审',            'low',    'seed-runner', NOW(), 'seed', '0'),
(728, 'REQ-2025-SEED014', 125, '【seed】OCR PoC',                            '票据识别',                              '04', '01', '01', 1, '产品已评审',        'medium', 'seed-runner', NOW(), 'seed', '0'),
(729, 'REQ-2025-SEED015', 126, '【seed】推送 HTTP/2',                        '吞吐升级',                              '02', '01', '01', 1, '架构已评审',        'medium', 'seed-runner', NOW(), 'seed', '0'),
-- 状态 02 已完成 (12 个)
(730, 'REQ-2025-SEED016', 127, '【seed】对账精度修正',                      '已上线',                                '01', '00', '02', 1, '上线后无回滚',      'high',   'seed-runner', NOW(), 'seed', '0'),
(731, 'REQ-2025-SEED017', 128, '【seed】工单 SLA 计算',                     '已上线',                                '01', '01', '02', 1, '运营已确认',        'medium', 'seed-runner', NOW(), 'seed', '0'),
(732, 'REQ-2025-SEED018', 129, '【seed】报表平台分组下钻',                  '已上线',                                '03', '02', '02', 1, '已观察 1 周稳定',   'medium', 'seed-runner', NOW(), 'seed', '0'),
(733, 'REQ-2024-SEED001', 138, '【seed】CMS 上线',                          '已上线',                                '01', '01', '02', 1, '已上线',            'high',   'seed-runner', NOW(), 'seed', '0'),
(734, 'REQ-2024-SEED002', 138, '【seed】CMS 富文本',                        '已上线',                                '01', '02', '02', 1, '已上线',            'medium', 'seed-runner', NOW(), 'seed', '0'),
(735, 'REQ-2024-SEED003', 139, '【seed】OA 审批改造',                       '已上线',                                '01', '00', '02', 1, '已上线',            'high',   'seed-runner', NOW(), 'seed', '0'),
(736, 'REQ-2024-SEED004', 140, '【seed】CRM 客户管理',                      '已上线',                                '01', '01', '02', 1, '已上线',            'high',   'seed-runner', NOW(), 'seed', '0'),
(737, 'REQ-2024-SEED005', 141, '【seed】门户改版',                          '已上线',                                '01', '02', '02', 1, '已上线',            'medium', 'seed-runner', NOW(), 'seed', '0'),
(738, 'REQ-2024-SEED006', 142, '【seed】SSO 单点',                          '已上线',                                '02', '00', '02', 1, '已上线',            'high',   'seed-runner', NOW(), 'seed', '0'),
(739, 'REQ-2024-SEED007', 143, '【seed】导出工具',                          '已上线',                                '03', '02', '02', 1, '已上线',            'low',    'seed-runner', NOW(), 'seed', '0'),
(740, 'REQ-2024-SEED008', 144, '【seed】定时任务平台',                      '已上线',                                '02', '02', '02', 1, '已上线',            'medium', 'seed-runner', NOW(), 'seed', '0'),
(741, 'REQ-2024-SEED009', 138, '【seed】CMS 权限模块',                      '已上线',                                '02', '01', '02', 1, '已上线',            'medium', 'seed-runner', NOW(), 'seed', '0'),
-- 状态 03 已取消 (8 个)
(742, 'REQ-2024-SEED010', 145, '【seed】小程序商城',                        '需求方撤回',                            '01', '02', '03', 1, '客户撤回',          'low',    'seed-runner', NOW(), 'seed', '0'),
(743, 'REQ-2024-SEED011', 146, '【seed】内部 Wiki',                          '预算砍掉',                              '02', '02', '03', 1, '预算砍',            'low',    'seed-runner', NOW(), 'seed', '0'),
(744, 'REQ-2024-SEED012', 147, '【seed】SDK 多端打包',                       '技术路线调整',                          '02', '01', '03', 1, '路线变',            'medium', 'seed-runner', NOW(), 'seed', '0'),
(745, 'REQ-2024-SEED013', 148, '【seed】视频会议',                          '采购成熟方案',                          '04', '02', '03', 1, '改买不做',          'low',    'seed-runner', NOW(), 'seed', '0'),
(746, 'REQ-2024-SEED014', 149, '【seed】区块链溯源',                        '业务方暂缓',                            '04', '02', '03', 1, '业务暂缓',          'medium', 'seed-runner', NOW(), 'seed', '0'),
(747, 'REQ-2024-SEED015', 145, '【seed】小程序订单',                        '商城撤回连带',                          '01', '02', '03', 1, '连带撤回',          'low',    'seed-runner', NOW(), 'seed', '0'),
(748, 'REQ-2024-SEED016', 146, '【seed】Wiki 全文检索',                     'Wiki 砍掉连带',                         '02', '01', '03', 1, '连带砍',            'low',    'seed-runner', NOW(), 'seed', '0'),
(749, 'REQ-2024-SEED017', 147, '【seed】SDK iOS 优化',                      '路线连带',                              '02', '02', '03', 1, '连带',              'low',    'seed-runner', NOW(), 'seed', '0');

-- ----------------------------
-- 3. 验证
-- ----------------------------
SELECT COUNT(*) AS seed_count, status, COUNT(*) cnt
  FROM tb_requirement
 WHERE remark='seed' AND create_by='seed-runner'
 GROUP BY status WITH ROLLUP;
-- 期望:50 行总计;00=15, 01=15, 02=12, 03=8

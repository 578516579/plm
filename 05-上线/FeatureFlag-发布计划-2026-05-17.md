# FeatureFlag 模块 — 发布计划 (骨架,2026-05-17)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f) |
| 关联 PRD | [FeatureFlag-PRD.md](../01-立项/FeatureFlag-PRD.md) |
| 关联测试计划 | [FeatureFlag-测试计划-2026-05-17.md](../04-测试/FeatureFlag-测试计划-2026-05-17.md) |
| 范本 | [Project-发布计划-2026-05-15.md](Project-发布计划-2026-05-15.md) |

## 1. 发布范围
FeatureFlag 模块 v1.0 — 字段 + 状态机 + API + 字典 + 菜单全部就绪 (PRD-MAPPING §2 已对齐)。

## 2. 发布步骤
1. 数据库迁移: `mysql plm < sql/business-feature-flag.sql`
2. 后端发布: `mvn -pl plm-feature-flag install` + 重启 plm-admin
3. 前端发布: `npm run build` + 部署 plm-frontend
4. 烟雾测试: 通过菜单访问 + 创建 1 条数据 + 关键状态机演进
5. E2E 全套: `npm run test:e2e -g "FeatureFlag"`

## 3. 回滚预案
- DB: `mysql plm < sql/business-feature-flag-rollback.sql`
- Code: git revert <feat commit>
- 菜单/字典:rollback SQL 自动清理

## 4. 监控

### 4.1 监控点 (3 维)

| 维度 | 指标 | 数据源 | 健康阈值 |
|---|---|---|---|
| **业务** | `tb_feature_flag` 日新增行数 | MySQL count(create_time>=今日) | 小模块 0-30 / 日 |
| **业务** | Flag 切换失败数 (toggle 调用错) | 应用日志 + Prometheus | ≤ 3 / 日 |
| **业务** | 灰度规则失效数 (rule_invalid) | MySQL count(rule_status='invalid') | ≤ 2 |
| **接口** | `/business/feature-flag/*` 错误率 | Spring Actuator + Prometheus | 错误率 ≤ 1% |
| **接口** | `/business/feature-flag/list` P95 响应时间 | APM | < 1000 ms |
| **接口** | `/business/feature-flag/evaluate` P95 (热路径) | APM + 本地缓存 | < 50 ms |
| **资源** | tb_feature_flag 表 size 增长率 | MySQL information_schema | 月增 < 5% |

### 4.2 告警阈值

| 等级 | 触发条件 | 通知渠道 | 处理 SLA |
|---|---|---|---|
| P0 | 接口 5xx 持续 > 5 分钟 | PagerDuty + 飞书 | 30 分钟响应 |
| P0 | Flag 切换失败 > 5 / 小时 (影响发布) | PagerDuty + 飞书 | 30 分钟响应 |
| P0 | evaluate P95 > 200 ms (影响业务热路径) | PagerDuty + 飞书 | 30 分钟响应 |
| P1 | 列表查询 P95 > 3s 连续 10 分钟 | 飞书 | 2 小时响应 |
| P2 | 表行数月增 > 50% (异常增长) | 邮件 | 工作日响应 |

### 4.3 日志关键字

后端 Logback 输出, ELK / Loki 索引:
- `ERROR.*FeatureFlagService` — Service 层异常
- `WARN.*FeatureFlagController.*RejectedException` — 业务规则拒绝
- `INFO.*FeatureFlag.*toggle` — Flag 切换审计 (含 flag / operator / before / after)
- `WARN.*FeatureFlag.*rule-invalid` — 灰度规则失效审计
- `WARN.*FeatureFlag.*deadlock` — MySQL 死锁

## 5. Changelog
追加到 [Changelog.md](Changelog.md) - FeatureFlag 模块 v1.0 上线 (2026-05-17)

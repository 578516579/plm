# Competitive 模块 — 发布计划 (骨架,2026-05-17)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f) |
| 关联 PRD | [Competitive-PRD.md](../01-立项/Competitive-PRD.md) |
| 关联测试计划 | [Competitive-测试计划-2026-05-17.md](../04-测试/Competitive-测试计划-2026-05-17.md) |
| 范本 | [Project-发布计划-2026-05-15.md](Project-发布计划-2026-05-15.md) |

## 1. 发布范围
Competitive 模块 v1.0 — 字段 + 状态机 + API + 字典 + 菜单全部就绪 (PRD-MAPPING §2 已对齐)。

## 2. 发布步骤
1. 数据库迁移: `mysql plm < sql/business-competitive.sql`
2. 后端发布: `mvn -pl plm-competitive install` + 重启 plm-admin
3. 前端发布: `npm run build` + 部署 plm-frontend
4. 烟雾测试: 通过菜单访问 + 创建 1 条数据 + 关键状态机演进
5. E2E 全套: `npm run test:e2e -g "Competitive"`

## 3. 回滚预案
- DB: `mysql plm < sql/business-competitive-rollback.sql`
- Code: git revert <feat commit>
- 菜单/字典:rollback SQL 自动清理

## 4. 监控

### 4.1 监控点 (3 维)

| 维度 | 指标 | 数据源 | 健康阈值 |
|---|---|---|---|
| **业务** | `tb_competitive` 日新增行数 | MySQL count(create_time>=今日) | 小模块 0-30 / 日 |
| **业务** | 竞品分析未指派分析师数 | MySQL count(analyst_id IS NULL) | ≤ 5 |
| **业务** | AI 竞品摘要生成失败数 | 应用日志 + Prometheus | ≤ 3 / 日 |
| **接口** | `/business/competitive/*` 错误率 | Spring Actuator + Prometheus | 错误率 ≤ 1% |
| **接口** | `/business/competitive/list` P95 响应时间 | APM | < 1000 ms |
| **接口** | `/business/competitive/ai-summarize` P95 延迟 | APM | < 8000 ms (AI 链路) |
| **资源** | tb_competitive 表 size 增长率 | MySQL information_schema | 月增 < 10% |

### 4.2 告警阈值

| 等级 | 触发条件 | 通知渠道 | 处理 SLA |
|---|---|---|---|
| P0 | 接口 5xx 持续 > 5 分钟 | PagerDuty + 飞书 | 30 分钟响应 |
| P1 | 列表查询 P95 > 3s 连续 10 分钟 | 飞书 | 2 小时响应 |
| P1 | AI 竞品摘要失败率 > 30% (滑动 10 分钟) | 飞书 | 2 小时响应 |
| P2 | 表行数月增 > 50% (异常增长) | 邮件 | 工作日响应 |

### 4.3 日志关键字

后端 Logback 输出, ELK / Loki 索引:
- `ERROR.*CompetitiveService` — Service 层异常
- `WARN.*CompetitiveController.*RejectedException` — 业务规则拒绝
- `INFO.*Competitive.*state-transit` — 状态机推进审计 (draft→analyzed→archived)
- `ERROR.*Competitive.*AI.*` — AI 调用失败 (含 timeout / quota / parse)
- `WARN.*Competitive.*deadlock` — MySQL 死锁

## 5. Changelog
追加到 [Changelog.md](Changelog.md) - Competitive 模块 v1.0 上线 (2026-05-17)

---
name: canary-monitor
description: PLM canary 监控 — 灰度各阶段触发阈值表 (错误率 / API P99 / 业务核心指标). 用户说"canary 监控 / 灰度阈值 / canary 阈值 / 触发回滚条件"时调用. 输出: 05-上线/Release-Plan-<release>.md §2. **release-captain agent 的子工具**。
---

# canary-monitor — canary 监控 skill v0.1

## 1. 何时调用
- "canary / 灰度阈值 / 触发回滚 / 监控阈值"
- release-captain §2.2 触发

## 2. 触发阈值表 (per rollback-planner §3)

| 阶段 | 错误率 | API P99 | 业务指标下降 | 决策时间 |
|---|---|---|---|---|
| 内部 / 5% | P0 (功能阻断/数据损坏/安全) ≥ 1 | — | — | 即时 |
| 5% → 50% | > 1% (或 > 上版本 2×) | — | — | 5 min |
| 50% → 100% | > 0.5% | > 1s (上版本 2×) | — | 15 min |
| 100% 观察期 | > 0.3% | > 700ms | > 20% | 30 min |

## 3. early 简化 (per proposal 0010 substrate-only)

PLM 当前 (solo + early) 替代方案:
- 错误率: 手动 curl healthcheck 间隔 5 min
- API P99: 看 backend log + EXPLAIN 慢查询
- 业务指标: 关键 CRUD 端点手测

## 4. 输出模板
```markdown
## §2 canary 监控阈值

| 阶段 | 错误率阈值 | P99 阈值 | 业务阈值 | 决策时间 | 监控手段 |
|---|---|---|---|---|---|
| 1 | P0 ≥ 1 | — | — | 即时 | 手动 curl |
| 2 | > 1% | — | — | 5 min | journalctl |
| 3 | > 0.5% | > 1s | — | 15 min | EXPLAIN |
| 4 | > 0.3% | > 700ms | > 20% | 30 min | (待 stable 期补正式看板) |
```

## 5. 衔接
- 上游: gray-release-strategy (各阶段定义)
- 下游: incident-commander (触发时启动应急)

## 6. 历史
| v0.1 | 2026-05-19 | 首版; release-captain 配套 2/4 |

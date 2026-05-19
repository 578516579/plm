---
name: incident-triage
description: PLM 事故 triage — 定级 P0/P1/P2 + 第一响应 (回滚 vs hotfix vs 观察). 用户说"事故定级 / triage / P0 故障 / P1 故障 / 第一响应 / incident severity"时调用. 输出: 06-运营/incident-<id>-<date>.md §1+§2. **incident-commander agent 的子工具**。
---

# incident-triage — 事故 triage skill v0.1

## 1. 何时调用
- "事故定级 / triage / P0 / P1 / 第一响应"
- incident-commander §2.1 触发

## 2. 定级标准 (per ops §1.4)

| 级别 | 标准 | 第一响应 |
|---|---|---|
| **P0** | 功能阻断 ≥ 50% 用户 / 数据损坏 / 安全漏洞 / 服务不可达 | **立即回滚**, 5 min 内启动应急 |
| **P1** | 功能降级 / 性能严重下降 (P99 > 2×) / 单点故障 | 评估回滚 vs hotfix, 30 min 决议 |
| **P2** | 局部 bug / UI 异常 / 非核心功能 | 排查 + 下个 Sprint 修, 不应急 |

## 3. 第一响应决策树

```
观察到异常
  ↓
是 P0 吗? (5 min 内判定)
  ├─ 是 → 立即触发回滚 (per rollback-planner.md)
  │       同时启动 incident-comms (timeline + 通知)
  └─ 否 → P1?
       ├─ 是 → 评估回滚 vs hotfix
       │      回滚成本 < hotfix → 回滚
       │      hotfix < 30 min → hotfix
       │      其余 → 临时降级 + 等下窗口
       └─ 否 (P2) → 记 backlog, 不应急
```

## 4. 输出模板
```markdown
## §1 事故定级
- ID: INC-2026-NNN
- 级别: P0 / P1 / P2
- 影响: <用户范围 / 业务模块>
- 首次发现: 2026-XX-XX HH:MM
- 报告人: <name>

## §2 第一响应
- 决策: 回滚 / hotfix / 观察
- 决策时间: 2026-XX-XX HH:MM (距首次发现 N min)
- 执行人: <name>
```

## 5. 衔接
- 上游: canary-monitor 阈值触发 / 用户报告
- 下游: incident-comms (沟通) + incident-runbook-lookup (执行)

## 6. 历史
| v0.1 | 2026-05-19 | 首版; incident-commander 配套 1/4 |

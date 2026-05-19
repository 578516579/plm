---
name: orchestrator-monthly-cycle
description: PLM 月度自进化编排 — 月底 tracking 终结 + 调度 reflect-monthly + rule-health (Phase D 激活后). 用户说"月度编排 / monthly cycle / tracking 终结 / 月底调度"时调用. **self-evolution-orchestrator agent 的子工具**。
---

# orchestrator-monthly-cycle — 月度编排 skill v0.1

## 1. 何时调用
- "月度编排 / monthly / tracking 终结"
- self-evolution-orchestrator §2.2
- 每月最后 3 天 (28/29/30/31)

## 2. 月度节奏

| 时点 | 动作 |
|---|---|
| 月底 -3d | tracking 期到达的 proposal 走 7 步终结判定 (per reflect-monthly skill) |
| 月底 -1d | 调度 reflect-monthly |
| 月初 +1d | 跑 rule-health.sh (Phase D 激活后) + 决议升降级 |

## 3. Tracking 7 步终结判定 (per reflect-monthly v0.1)

每 tracking 到期 proposal:
1. 取 §10 实际数据
2. 与 §8 目标对比
3. ≥ 70% 达标 → "tracking 关闭"
4. 30-70% → 续 1 期 + 整改
5. < 30% → revert / 走新 proposal
6. ≥ 1 P0 因此规则发生 → revert
7. 综合结论入 reflect-monthly §跟踪段

## 4. 输出
```markdown
# Monthly Cycle Orchestration — YYYY-MM

## Tracking 终结
| Proposal | 状态 | 数据 vs 目标 | 决议 |
|---|---|---|---|

## reflect-monthly 调度
- [ ] 99-跨阶段/reflect/YYYY-MM.md
```

## 5. 历史
| v0.1 | 2026-05-19 | 首版; self-evolution-orchestrator 配套 2/4 |

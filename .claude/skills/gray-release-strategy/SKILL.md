---
name: gray-release-strategy
description: PLM 灰度策略制定 — 4 阶段 (内部 / 5% / 50% / 100%) + 维度差异化 (per Phase 05 §D). 用户说"灰度策略 / gray release / 灰度阶段 / canary 阶段"时调用. 输出: 05-上线/Release-Plan-<release>.md §1. **release-captain agent 的子工具**。
---

# gray-release-strategy — 灰度策略 skill v0.1

## 1. 何时调用
- "灰度策略 / 灰度阶段 / canary 阶段 / 4 阶段"
- release-captain §2.1 触发

## 2. 4 阶段标准 (per Phase 05 §D)

| 阶段 | 用户范围 | 观察期 (early/stable/mature) | 决策 |
|---|---|---|---|
| 1 | 内部用户 (PLM 团队) | 30min / 2h / 24h | 内部测试通过 → 进 5% |
| 2 | 5% 真实用户 | 30min / 2h / 24h | 错误率 < 1% → 进 50% |
| 3 | 50% 真实用户 | 30min / 2h / 24h | 错误率 < 0.5% + P99 < 500ms → 进 100% |
| 4 | 100% 全量 | 持续观察 (per Phase 06 §F) | 进入 Phase 06 cycle |

## 3. 维度差异化

按 4D 参数化 (per proposal 0007/0010/0011/0012):
- **early × solo + internal-tool**: 可压缩为 3 阶段 (内部 / 50% / 100%), 跳过 5% (用户基数小)
- **stable + small team**: 完整 4 阶段
- **mature + medium+**: 加阶段 0 (staging full regression)

## 4. 输出模板
```markdown
# Release Plan: vX.Y.Z

## §1 灰度阶段

| # | 范围 | 时段 | 观察期 | go/no-go 决议 | Owner |
|---|---|---|---|---|---|
| 1 | 内部 PLM 团队 | 00:00-00:30 | 30min | 错误率=0 | Wjl |
| 2 | 5% (5 用户) | 00:30-01:00 | 30min | 错误率<1% | Wjl |
| 3 | 50% | 01:00-01:30 | 30min | 错误率<0.5% + P99<500ms | Wjl |
| 4 | 100% | 01:30+ | 持续 | 进 Phase 06 | Wjl |

## §1.1 跳过 / 调整理由
- early × solo + internal-tool: 跳过 5% (per proposal 0007 §B)
```

## 5. 衔接
- 上游: deploy-checklist (上线 Checklist)
- 下游: canary-monitor (各阶段阈值), release-window-planner (具体时间)

## 6. 历史
| v0.1 | 2026-05-19 | 首版; release-captain 配套 1/4 |

---
name: orchestrator-proposal-flow
description: PLM Proposal 状态流转 — proposed→accepted→merged→tracking→closed + 超期警告 + bundle 判据. 用户说"proposal 状态 / proposal lifecycle / 超期警告 / bundle 判据"时调用. **self-evolution-orchestrator agent 的子工具**。
---

# orchestrator-proposal-flow — Proposal 生命周期管理 skill v0.1

## 1. 何时调用
- "proposal 状态 / proposal lifecycle / 超期警告"
- self-evolution-orchestrator §2.3
- 每周一次 (周一 check)

## 2. 状态流转

```
proposed → (评审通过) → accepted → (落地) → merged → (tracking 14d) → tracking → (关闭判定) → closed
                                                                              ↓
                                                                            revert (失败时)
```

## 3. 超期警告

- accepted 状态 > 7d 未 apply → 警告 "拖延 apply"
- merged 状态 > 14d 未进 tracking → 警告 "tracking 期未开始"
- tracking 状态 > 28d 未关闭 → 警告 "终结判定逾期"

## 4. Bundle 判据 (per proposal 0040 §3.3)

新 proposal 可与已有合并 (bundle) 条件:
- 同目标文件 (e.g. settings.json)
- 同评审人
- 同时间窗 (< 7d)
- 同类型 (e.g. 工具链 / 流程 / 规范)

任一不满 → split, 独立 proposal。

## 5. 输出
```markdown
# Proposal Flow Status — 2026-XX-XX

## 状态分布
- proposed: N 个 (id 列表)
- accepted: N 个
- merged: N 个
- tracking: N 个
- closed: N 个 (累计)

## 超期警告
- 0NNN: accepted 状态 10d > 7d, 拖延 apply

## Bundle 决议建议
- 0NNN + 0MMM 可合 (同 settings.json + 同人 + 同周)
```

## 6. 历史
| v0.1 | 2026-05-19 | 首版; self-evolution-orchestrator 配套 3/4 |

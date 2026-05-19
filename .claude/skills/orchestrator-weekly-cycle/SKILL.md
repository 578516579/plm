---
name: orchestrator-weekly-cycle
description: PLM 周度自进化编排 — 周一启动 (调度 signals-collect) + 周五收尾 (调度 reflect-weekly). 用户说"周度编排 / weekly cycle / 周报调度 / 周一启动"时调用. 输出: 调用日志 + 提醒. **self-evolution-orchestrator agent 的子工具**。
---

# orchestrator-weekly-cycle — 周度编排 skill v0.1

## 1. 何时调用
- "周度编排 / weekly cycle / 周报调度"
- self-evolution-orchestrator §2.1
- 每周一 (启动) + 周五 (收尾)

## 2. 周度节奏

| 时点 | 动作 | 调子 skill |
|---|---|---|
| 周一 09:00 | 启动新周, 跑信号采集 | signals-collect |
| 周五 17:00 | 收尾, 写周报 | reflect-weekly |
| 周日 23:59 | 候选 → 升格 / 入 backlog 决议 | proposal Mode A |

## 3. 输出
```markdown
# Weekly Cycle Orchestration — WNN

## 周一: 启动
- [x] signals-collect → 99-跨阶段/signals/YYYY-MM-supplementary.md
- [x] 当前 backlog 状态: N 待办 / N 已完成

## 周五: 收尾
- [ ] reflect-weekly → 99-跨阶段/reflect/YYYY-WNN.md
- [ ] 本周候选: N 个 → 升格?
```

## 4. 历史
| v0.1 | 2026-05-19 | 首版; self-evolution-orchestrator 配套 1/4 |

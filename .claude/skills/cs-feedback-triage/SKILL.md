---
name: cs-feedback-triage
description: PLM 用户反馈 triage — 收集 + 分类 (bug/feature/UX/perf/其他) + 优先级 + 入 Phase 06 §D. 用户说"用户反馈 / 反馈分类 / feedback triage / Phase 06 §D"时调用. 输出: Phase 06 §D 用户反馈段填值 + 反馈 → Sprint backlog / proposal 转发. **customer-support agent 的子工具**。
---

# cs-feedback-triage — 反馈 triage skill v0.1

## 1. 何时调用
- "用户反馈 / 反馈分类 / feedback triage"
- customer-support §2.4
- Phase 06 cycle 中持续

## 2. 5 类分类

| 类型 | 处置 |
|---|---|
| bug | 转 tester defect-triage |
| feature request | 转 product-manager (入下个 PRD / 路线图) |
| UX 改进 | 转 ux-designer |
| 性能问题 | 转 backend-coder / data-engineer |
| 其他 (问候 / 抱怨 / 表扬) | 归档, 月底汇总 |

## 3. 优先级 (per RICE)

- Reach: 影响多少用户?
- Impact: 单用户感受多深?
- Confidence: 反馈数据多可靠?
- Effort: 解决成本?

RICE 高 → P1, 中 → P2, 低 → P3。

## 4. 输出: Phase 06 §D 段
```markdown
## §D 用户反馈

| # | 时间 | 来源 | 反馈 | 类型 | 优先级 | 处置 | 状态 |
|---|---|---|---|---|---|---|---|
| 1 | 2026-05-20 | 邮件 | "想能批量改任务状态" | feature | P2 | → PM 入路线图 | 已转 |
| 2 | 2026-05-21 | 客服 | "导出报表慢" | perf | P2 | → backend | 处理中 |
```

## 5. 历史
| v0.1 | 2026-05-19 | 首版; customer-support 配套 4/4 (完结) |

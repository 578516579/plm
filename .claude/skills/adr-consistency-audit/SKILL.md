---
name: adr-consistency-audit
description: PLM ADR 一致性审 — 6 维评分 (状态/上下文/决策/后果/备选/历史) + ADR vs 实际代码 drift. 用户说"ADR 审 / ADR 一致性 / ADR drift / ADR 6 维"时调用. 输出: 99-跨阶段/knowledge-audit-adr-<date>.md. **knowledge-curator agent 的子工具**。
---

# adr-consistency-audit — ADR 一致性审 skill v0.1

## 1. 何时调用
- "ADR 审 / ADR 一致性 / drift"
- knowledge-curator §2.2
- reflect-quarterly 时

## 2. 6 维评分

对每个 ADR (03-开发/ADR/NNNN-*.md), 评 6 维 (每维 0-2 分, 满分 12):

| 维度 | 评分标准 |
|---|---|
| Status | proposed / accepted / superseded 明确 |
| Context | 背景 (业务/技术/约束) 清晰 |
| Decision | 决策本身 (要做什么) 具体 |
| Consequences | 正向 + 负向后果都列 |
| Alternatives | 至少列 2 个备选 + 否决理由 |
| History | 含修订日期 + superseded 链 |

≥ 10 分 = ✅, 7-9 = ⚠️, < 7 = ❌

## 3. ADR vs 实际代码 drift

每个 ADR-NNNN 决议的代码层面落地:
- grep 关键代码模式
- 看实际实现是否符合 ADR

例: ADR-0005 状态机反向边 → grep ServiceImpl 是否真有反向边处理。

## 4. 输出模板
```markdown
# ADR Consistency Audit — 2026-XX-XX

## 6 维评分

| ADR | Status | Context | Decision | Consequences | Alternatives | History | 总分 | 状态 |
|---|---|---|---|---|---|---|---|---|
| ADR-0001 | 2 | 2 | 2 | 1 | 2 | 1 | 10 | ✅ |
| ADR-0005 | 2 | 2 | 2 | 2 | 1 | 1 | 10 | ✅ |

## drift 检测
- ADR-0005: 反向边代码全在 → ✅
- ADR-NNN: 决议 X 但代码缺 → ❌ 触发 proposal 整改
```

## 5. 历史
| v0.1 | 2026-05-19 | 首版; knowledge-curator 配套 2/4 |

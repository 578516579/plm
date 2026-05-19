---
name: ssot-curator
description: PLM SSoT 治理 — 检测多源真相 (同字段 在 PRD / DDL / DTO / 测试用例 中定义不一致) 并决议归一. 用户说"SSoT / 单一事实源 / 多源真相 / 归一字段"时调用. 输出: 99-跨阶段/knowledge-audit-ssot-<date>.md. **knowledge-curator agent 的子工具**。
---

# ssot-curator — SSoT 治理 skill v0.1

## 1. 何时调用
- "SSoT / 单一事实源 / 多源真相 / 归一"
- knowledge-curator §2.4
- 跨模块字段冲突时

## 2. SSoT 违反检测

对关键字段, 跨文档对账:
- PRD § 章节定义
- 原型 HTML form input name
- DB DDL 列名 + 类型
- DTO 字段名
- 测试用例 Given-When-Then 引用

任一处不一致 → SSoT 违反。

## 3. 决议归一

per rules.md §M PRD-MAPPING.md 是 SSoT:
- 以 PRD-MAPPING.md 为准
- 其余文档对齐
- 如 PRD-MAPPING.md 本身不全, 先走 product-manager 补全 PRD

## 4. 输出模板
```markdown
# SSoT Audit — 2026-XX-XX

## 字段冲突表
| 字段 | PRD § | 原型 | DDL | DTO | TC | 决议 |
|---|---|---|---|---|---|---|
| project.name | name (PRD §2.1) | name (input) | project_name | name | name | ❌ DDL 不一致, 建议改 DDL |
| sprint.startTime | startDate | start_date | start_time | startTime | startTime | ❌ DDL & 测试名不一 |
```

## 5. 历史
| v0.1 | 2026-05-19 | 首版; knowledge-curator 配套 4/4 |

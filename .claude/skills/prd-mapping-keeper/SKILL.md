---
name: prd-mapping-keeper
description: PLM PRD-MAPPING.md 同步维护 — 新模块字段映射增量 + 模块完整度审 + 与 PRD HTML 原型一致性. 用户说"PRD-MAPPING 同步 / 字段映射增量 / 模块完整度 / 原型对齐审"时调用. 输出: PRD-MAPPING.md 增量 + 99-跨阶段/knowledge-audit-mapping-<date>.md. **knowledge-curator agent 的子工具**。
---

# prd-mapping-keeper — PRD-MAPPING.md 维护 skill v0.1

## 1. 何时调用
- "PRD-MAPPING / 字段映射 / 模块完整度"
- knowledge-curator §2.1
- 新模块 Phase 02 设计完成后

## 2. 步骤

### 2.1 模块完整度审
扫 PRD-MAPPING.md §1 模块表:
- 🟢 已 PRD-aligned 模块
- 🟡 空壳 模块 (有目录无字段)
- 🔴 缺模块

每模块校验:
- §2 字段映射完整 (PRD § + 原型 HTML 元素 → DB 字段 → DTO)
- §3 状态机存在 (如适用)
- §4 错误码完整
- §5 URL / 菜单文案完整

### 2.2 与 PRD HTML 原型对账
对 PRD-MAPPING §2 每行字段:
- 反查 prd和原型/AgriPLM-DevOps-原型/agriplm_split/<module>.html
- 字段在原型出现? 在 PRD § 章节出现?

### 2.3 增量写入

```markdown
## 模块 <name>

### §2 字段映射
| 字段 | PRD § | 原型元素 | DB 列 | DTO 字段 |
|---|---|---|---|---|

### §3 状态机
...
```

## 3. 输出
- PRD-MAPPING.md 增量 (新 §)
- knowledge-audit-mapping-<date>.md (审计报告)

## 4. 历史
| v0.1 | 2026-05-19 | 首版; knowledge-curator 配套 1/4 |

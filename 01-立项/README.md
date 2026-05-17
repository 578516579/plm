# 01-立项 — Phase 01 Inception

> Phase 01 是模块进入开发的**准入凭证**。必备产出物:`<Module>-PRD.md`(每模块 1 份),通过 [Phase 01 Gate](../99-跨阶段/gate-checklists/Phase01-立项-Gate.md) 才能进入 Phase 02。

## 内容索引

| 文件 | 类型 | 说明 |
|---|---|---|
| `PRD.md` / `商业计划.md` / `市场调研.md` / `立项评审纪要.md` | 通用模板 | 4 份顶层模板,供新模块复制 |
| `<Module>-PRD.md` | 每模块必备 | 模块产品需求文档,引用 [PRD-MAPPING.md §2](../PRD-MAPPING.md) 字段表 + 原型 HTML |
| `Project-商业计划.md` / `Project-市场调研.md` / `Project-立项评审纪要-YYYY-MM-DD.md` | 重要模块辅助 | Project 模块作为 v0.1.0 首发完整范本 |

## 当前模块清单(31 个 PRD-aligned)

01-立项 下应有 31 份 `<Module>-PRD.md`,截至 2026-05-17:**31 份骨架已齐**(详 [审计 2026-05-17](../99-跨阶段/audits/2026-05-17-process-docs-completeness-audit.md))。

**实质内容填充优先级**:
- P0 已实质化: Project / Inception (2 份)
- P0 骨架待实质: requirement / sprint / task / testcase / defect / document + 其余 23 个

## 模板使用

```bash
cp PRD.md <Module>-PRD.md
# 然后:
# 1. 填头部"文档信息"表
# 2. 引用 PRD-MAPPING.md §2 字段表(不重复字段表)
# 3. 实质内容:背景/目标/角色/场景/验收
# 4. 走 Phase 01 Gate 评审,签字打勾,commit
```

## 相关规则

- [.claude/rules.md §M PRD/原型驱动开发](../.claude/rules.md) — Claude 硬约束
- [.claude/rules.md §O 无文档不执行](../.claude/rules.md) — 2026-05-17 生效
- [03-开发/开发规范.md §6 文档规范](../03-开发/开发规范.md)

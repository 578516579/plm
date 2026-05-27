---
name: plm-product-design
description: PLM 产品设计编排 SOP — 当用户要「这个模块怎么设计」「加个 XX 需求」「Phase 01→02→03 准入」「PRD 对得上吗」「需求拆一下」时,按产品设计漏斗(澄清→范围→PRD建模→数据/架构→原型对齐→契约→文档)出计划、分派子 agent(requirement-clarifier/scope-decider/prd-author/db-modeler/system-architect/ux-prototype-aligner/api-contract-keeper/technical-writer/prompt-engineer)、裁决"设计就绪"Gate、把结果沉淀成 signals 自进化。本 skill 是 product-orchestrator agent 的执行手册;只对齐原型不编排时用 ux-prototype-aligner,只补字段不编排时用 prd-author。
---

# plm-product-design — PLM 产品设计编排 SOP

把"开发**之前**该怎么做产品设计"固化成可重复的编排流程。**编排 + 裁决 + 沉淀**在本 skill;**角色判断**在 [`product-orchestrator` agent](../../agents/product-orchestrator.md);**全流程/角色矩阵**在 [`产品设计工作流.md`](../../../99-跨阶段/产品设计工作流.md)。

> 一句话边界:`plm-product-design`(本 skill,开发前)= 需求拆什么/字段哪来/原型对不对得上/算不算设计就绪;`plm-test-orchestrate`(开发后)= 测什么/算不算过。**一前一后两个总管**,串成完整生命周期。

---

## 何时触发

| 语义 | 用户原话举例 | 走本 skill 还是子 agent |
|---|---|---|
| 设计一个模块 | "这模块怎么设计"、"把 X 需求落出来" | **本 skill**(漏斗编排) |
| Phase 准入 | "需求设计完了"、"Phase 02 准入"、"可以开发了吗" | **本 skill**(编排+裁决设计就绪) |
| 需求拆解 | "加个 XX 功能"、"这需求拆一下" | **本 skill**(先查 PRD-MAPPING) |
| 可追溯核查 | "PRD 对得上吗"、"这字段哪来的" | **本 skill**(可追溯性) |
| 只补字段 | "给 X 补 3 个字段" | `prd-author`(不必编排) |
| 只核原型 | "这页面对得上原型吗" | `ux-prototype-aligner`(不必编排) |

---

## 编排 5 步法

### Step 0 — 先查 PRD-MAPPING(MUST,红线前置)
任何业务设计动作前,读 [PRD-MAPPING.md](../../../PRD-MAPPING.md) 确认对应模块的 PRD § / 原型 HTML / 字段 / 状态机 / 错误码。**需求不在 PRD/原型里 → 停**,走 requirement-clarifier 让 user 在 §M.1 三选项里拍板(按 PRD 走 / 走 proposal 改 PRD-MAPPING / 记 proposal 评审),**禁止凭直觉补字段**。

### Step 1 — 判范围(设计什么)
看需求落在哪,决定设计深度(用 scope-decider 协助分级):

```
改文案/UI 微调(不动字段/状态)  → 仅 ux-prototype-aligner 核 §N
加/改 1 个业务字段              → prd-author 补字段对照表 → db-modeler 补列
改状态机                       → prd-author 核状态来源(§M.4)→ ux-prototype-aligner 核徽章
新模块 / Phase 02→03 准入       → 全漏斗(强制)
需求不在 PRD                    → requirement-clarifier(§M.1)→ 停
```

### Step 2 — 出漏斗计划(分几层)
按产品设计漏斗列本次要走的层(见 [agent 漏斗图](../../agents/product-orchestrator.md)):
- **L1 澄清** requirement-clarifier:模糊指令 → 明确选项
- **L2 范围** scope-decider:P0/P1/P2 分级
- **L3 PRD 建模** prd-author ★:字段对照表+状态机+错误码,锚 PRD-MAPPING §2/§3/§4
- **L4 数据/架构** db-modeler + system-architect:DDL / SPI / 抽象层
- **L5 原型对齐** ux-prototype-aligner ★:表单/徽章/AI 按钮 ↔ 原型 + §N 守门
- **L6 契约** api-contract-keeper:5 层命名一致(为开发铺路)
- **L7 文档** technical-writer:概念稳定后出设计 .md
- **AI 旁路** prompt-engineer:模块含 ✨ AI 功能时

> 原则:**字段对照表(L3)必须先于代码 commit**(§M.2);原型指不出的字段不进 L3。

### Step 3 — 分派子 agent(谁来做)
按矩阵下发,**主 Claude 按顺序调 Agent**(子 agent 不能再 spawn):

| 子任务 | 分派 |
|---|---|
| 模糊指令拆解 | `requirement-clarifier` |
| 范围分级 | `scope-decider` |
| 字段/状态/错误码建模 | `prd-author` ★ |
| DDL/字典 | `db-modeler` |
| 抽象层/SPI | `system-architect` |
| 原型/§N 守门 | `ux-prototype-aligner` ★ |
| 5 层命名对齐 | `api-contract-keeper` |
| 设计文档 | `technical-writer` |
| AI prompt | `prompt-engineer` |

复杂(≥5 agent)时让 `product-orchestrator` 出 Mermaid DAG,再 `task-tracker` 拆 TodoWrite。

### Step 4 — 裁决"设计就绪"Gate(算不算可开发)
逐条核对(§M.9.3),全满足才判**就绪**:

- [ ] **可追溯**:每个字段/状态/错误码/文案都指得出 PRD § + 原型元素(prd-author 矩阵无空行)
- [ ] **字段表先行**:PRD-MAPPING §2 字段对照表已 commit,且先于代码 commit(§M.2)
- [ ] **状态合法**:状态只来自 PRD §3.2 / 原型徽章类 / PRD-MAPPING §3(§M.4)
- [ ] **错误码登记**:新错误码已登 PRD-MAPPING §4,无裸数字(§M.5)
- [ ] **原型保真**:ux-prototype-aligner 确认 §N 无违规
- [ ] **三者一致**:PRD/原型/MAPPING 一致;不一致已按 §M.1 走 proposal

任一不满足 → **驳回**,指明回哪个 agent;**禁**"先开发着回头补设计"、**禁**凭直觉补字段。

### Step 5 — 沉淀 signals(自进化)
把本轮设计结果记进 [`signals/YYYY-MM.md` 产品设计编排段](../../../99-跨阶段/signals/README.md):
- `prd_drift_count`(需求/代码与 PRD 不符被拦截次数)
- `prototype_deviation_count`(§N 违规数)
- `untraceable_field_count`(指不出出处的字段/状态/错误码,应趋 0)
- `field_table_lag`(字段表晚于代码 commit 次数,应=0)
- `prd_change_via_proposal`(PRD 演化正确走 proposal 的占比)

**触发提案**(主动建议):同类 PRD drift 月 ≥3 → PRD-MAPPING 补全提案;某类字段反复指不出原型 → 原型补画提案;§N 违规集中某子项 → UED 加 hook 提案。

---

## 编排速查卡

```
请求 → [Step0 查 PRD-MAPPING] → 不在 PRD? → requirement-clarifier(§M.1) → 停
                │ 在 PRD
                ▼
[Step1 范围] → [Step2 漏斗分层] → [Step3 分派 DAG]
                                          │
        requirement-clarifier ─ L1 澄清 ──┤
        scope-decider ─ L2 范围 ──────────┤
        prd-author ★ ─ L3 字段/状态/错误码 ┤  ← 字段表先 commit
        db-modeler/system-architect ─ L4 ─┤
        ux-prototype-aligner ★ ─ L5 §N 守门┤
        api-contract-keeper ─ L6 契约 ─────┤
        technical-writer ─ L7 文档 ────────┤
                                          ▼
        [Step4 裁决] 可追溯+字段表先行+状态合法+错误码登记+原型保真+三者一致
                       全过→设计就绪(交 coder)  /  有缺→驳回
                                          ▼
        [Step5 signals] PRD drift / 原型偏离 / 不可追溯字段 / 字段表滞后
```

---

## 反模式(一票否决)

- ❌ 跳过 Step0 查 PRD-MAPPING,直接开始设计
- ❌ 需求不在 PRD/原型也"顺手补全"字段/状态/错误码(§M.1 最严红线)
- ❌ 字段对照表还没 commit 就让 coder 开写(§M.2 倒挂)
- ❌ "先开发着,设计回头补"(设计就绪 Gate 形同虚设)
- ❌ 三者冲突时默默对齐一边,不让 user 拍板
- ❌ 编排只下发不裁决(设计了等于没设计)

---

## 引用

| 文件 | 用途 |
|---|---|
| [`.claude/agents/product-orchestrator.md`](../../agents/product-orchestrator.md) | 本 skill 的角色/裁决判断 |
| [`.claude/agents/prd-author.md`](../../agents/prd-author.md) | L3 需求建模执行细节 |
| [`.claude/agents/ux-prototype-aligner.md`](../../agents/ux-prototype-aligner.md) | L5 原型守门执行细节 |
| [`.claude/rules.md` §M(PRD 驱动)+ §M.9(产品设计编排)+ §N(UED)](../../rules.md) | 硬卡控 |
| [`99-跨阶段/产品设计工作流.md`](../../../99-跨阶段/产品设计工作流.md) | 全流程 + 角色矩阵 + 进化节律 |
| [`PRD-MAPPING.md`](../../../PRD-MAPPING.md) | 单一事实来源(§2/§3/§4/§8) |
| [`.claude/skills/plm-test-orchestrate/SKILL.md`](../plm-test-orchestrate/SKILL.md) | 下一阶段(测试)的对位 SOP |

## 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-27 | 首次创建:固化产品设计编排 SOP(proposal 0024) |

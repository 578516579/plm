---
name: plm-ued-design
description: PLM UED 设计编排 SOP — 当用户要「这页面怎么设计」「这模块 UI 怎么做」「信息架构怎么排」「交互流怎么走」「用什么组件」「这状态什么色」「Phase 02 UED 准入」「对得上 UED 规范/原型吗」时,按 UED 设计漏斗(信息架构→交互流→视觉Token/组件→原型保真→无障碍→设计交付)出计划、分派子 agent(ued-designer/ux-prototype-aligner/accessibility-reviewer/system-architect/technical-writer/prompt-engineer)、裁决"UED 设计就绪"Gate、把结果沉淀成 signals 自进化。本 skill 是 ued-orchestrator agent 的执行手册;只核原型不编排时用 ux-prototype-aligner,只核无障碍时用 accessibility-reviewer,只建模 UI 规格不编排时用 ued-designer。
---

# plm-ued-design — PLM UED 设计编排 SOP

把"写前端代码**之前**该怎么做 UI/交互设计"固化成可重复的编排流程。**编排 + 裁决 + 沉淀**在本 skill;**角色判断**在 [`ued-orchestrator` agent](../../agents/ued-orchestrator.md);**全流程/角色矩阵**在 [`UED设计工作流.md`](../../../99-跨阶段/UED设计工作流.md)。

> 一句话边界:`plm-product-design`(产品设计)管「需求→规格可追溯」,把 UI 当**一个盒子**;`plm-ued-design`(本 skill)**把那个盒子拆开**——管 UI 这个维度从信息架构到设计交付的完整 UED 设计生命周期。两者是 Phase 02 设计期**平级维度**,product 出字段/状态/错误码,UED 出组件/Token/交互/无障碍。

---

## 何时触发

| 语义 | 用户原话举例 | 走本 skill 还是子 agent |
|---|---|---|
| 设计一个页面/模块 UI | "这页面怎么设计"、"这模块 UI 怎么做" | **本 skill**(UED 漏斗编排) |
| Phase 02 UED 准入 | "UI 设计完了"、"可以让前端写了吗"、"UED 准入" | **本 skill**(编排+裁决 UED 设计就绪) |
| 信息架构/交互 | "这进哪个导航分组"、"交互流怎么走"、"三态怎么处理" | **本 skill**(U1/U2) |
| 组件/视觉 | "用什么组件"、"这状态什么色"、"间距多少" | **本 skill**(U3,或直接 ued-designer) |
| 保真核查 | "对得上原型吗"、"对得上 UED 规范吗" | `ux-prototype-aligner`(不必编排) |
| 无障碍核查 | "无障碍达标吗"、"对比度够吗"、"色盲能用吗" | `accessibility-reviewer`(不必编排) |
| 只出 UI 规格 | "给 X 页面出 UI 规格表" | `ued-designer`(不必编排) |

---

## 编排 5 步法

### Step 0 — 先读 SSoT(MUST,红线前置)
任何 UI 设计动作前,读 [02-设计/UED规范.md](../../../02-设计/UED规范.md)(Token/组件/AI UI/无障碍/附录A CSS 类)+ 对应原型 `prd和原型/AgriPLM-DevOps-原型/agriplm_split/<模块>.html` + [PRD-MAPPING.md](../../../PRD-MAPPING.md) §2/§3(字段+状态机)。
- **原型里没有这个页面 → 停**:回 `product-orchestrator` 走 §M.1(需求层缺失,不是 UED 能补)
- **规范里没有这个颜色/组件 → 停**:走 §N.9 先在 UED规范.md 对应章节注册,再用。**禁止前端"自由发挥"/裸 hex/匿名颜色**。

### Step 1 — 判范围(设计多深)
```
改文案/微调(不动布局/组件)        → 仅 ux-prototype-aligner 核 §N
加/改 1 个组件(用库内已有类)       → ued-designer 出组件用法 → ux-prototype-aligner 核保真
改状态展示(徽章色)               → ued-designer 核状态来源(对 PRD-MAPPING §3)→ ux-prototype-aligner 核 §N.2
新页面 / Phase 02 UED 准入         → 全漏斗(强制)
要用规范没有的颜色/组件            → §N.9 先在 UED规范.md 注册
原型没这个页面                    → 回 product-orchestrator 走 §M.1
```

### Step 2 — 出 UED 漏斗计划(分几层)
按 UED 设计漏斗列本次要走的层(见 [agent 漏斗图](../../agents/ued-orchestrator.md)):
- **U1 信息架构** ued-designer(+system-architect):导航位置/分组/Tab(§4/§6)
- **U2 交互流** ued-designer:操作路径/hover/focus/三态(§9)
- **U3 视觉/组件** ued-designer ★:Token/栅格/按钮/徽章/表单/表格/卡片,选自 UED规范库(§1/§5)
- **U4 原型保真** ux-prototype-aligner ★:表单 label/徽章色/AI 按钮 ↔ 原型 + §N 守门
- **U5 无障碍** accessibility-reviewer ★:WCAG AA 对比度/focus/label/不靠颜色(§11)
- **U6 交付** technical-writer:UED §12.3 设计交付 6 项 DoD
- **AI UI 旁路** prompt-engineer+ued-designer:模块含 ✨AI 功能时(命令栏/Panel/.btn-ai,§7)

> 原则:**视觉决策必须指得出 UED规范 § + 原型出处**(§N.1/N.9);UI 规格 commit 先于前端实现 commit(`ued_handoff_lag`=0)。

### Step 3 — 分派子 agent(谁来做)
按矩阵下发,**主 Claude 按顺序调 Agent**(子 agent 不能再 spawn):

| 子任务 | 分派 |
|---|---|
| 模糊指令拆解 | `requirement-clarifier` |
| UI 范围分级 | `scope-decider` |
| 信息架构+交互+视觉/组件建模 | `ued-designer` ★ |
| 跨模块导航结构 | `system-architect` |
| 原型/§N 保真守门 | `ux-prototype-aligner` ★ |
| 无障碍守门 | `accessibility-reviewer` ★ |
| 设计交付文档 | `technical-writer` |
| AI UI + prompt | `prompt-engineer` + `ued-designer` |

复杂(≥5 agent)时让 `ued-orchestrator` 出 Mermaid DAG,再 `task-tracker` 拆 TodoWrite。

### Step 4 — 裁决"UED 设计就绪"Gate(算不算可让前端写)
逐条核对(§N.10.3),全满足才判**就绪**(与 product-orchestrator 的"设计就绪"共同构成 Phase 02→03 准入):

- [ ] **Token 合规**:颜色全走 `var(--xx)` 无裸 hex(§N.1);间距 4px 倍数(§N.6)
- [ ] **组件选自库**:用 UED规范 附录A CSS 类;新类/新色已先走 §N.9 注册(§N.9)
- [ ] **状态徽章正确**:状态色对 PRD-MAPPING §3(§N.2)
- [ ] **AI 区分**:AI 触发用 `.btn-ai`✨,非 AI 禁用(§N.3)
- [ ] **三态齐全**:空/载/错都设计了(§N.5)
- [ ] **原型保真**:ux-prototype-aligner 确认 §N 无违规
- [ ] **无障碍达标**:accessibility-reviewer 确认 WCAG AA(对比度/focus/label/不靠色,§11)
- [ ] **设计交付 DoD**:UED规范 §12.3 的 6 项
- [ ] **与产品设计一致**:状态/字段对 prd-author 的 PRD-MAPPING §2/§3,不另起状态

任一不满足 → **驳回**,指明回哪个 agent;**禁**"先让前端写着 UED 回头补"、**禁**前端自由发挥。

### Step 5 — 沉淀 signals(自进化)
把本轮 UED 设计结果记进 [`signals/YYYY-MM.md` UED 设计编排段](../../../99-跨阶段/signals/README.md):
- `ued_token_violation_count`(裸 hex / 非 4px 间距 / 匿名颜色,应=0)
- `component_reuse_gap`(没用库组件 / 用未登记新类,应趋 0)
- `a11y_violation_count`(WCAG AA 违规:对比度/focus/label/不靠色)
- `three_state_miss_count`(空/载/错三态缺失,应=0)
- `ued_handoff_lag`(前端实现先于 UED 规格就绪的次数,应=0)

**触发提案**(主动建议):同类 token 违规月≥3 → 加 stylelint/PreToolUse hook 提案;a11y 反复某项 → axe-core 纳入 E2E 提案;反复用未登记组件 → UED规范补组件章节提案;三态反复遗漏 → 组件模板默认带骨架提案。

---

## 编排速查卡

```
请求 → [Step0 读 UED规范+原型] → 原型没页面? → 回 product-orchestrator(§M.1) → 停
                │ 规范没组件? → §N.9 注册 → 继续
                ▼
[Step1 范围] → [Step2 UED 漏斗分层] → [Step3 分派 DAG]
                                          │
        ued-designer ★ ─ U1 信息架构 ─────┤
        ued-designer ★ ─ U2 交互流 ───────┤
        ued-designer ★ ─ U3 视觉/Token/组件┤  ← UI 规格先 commit
        ux-prototype-aligner ★ ─ U4 §N 保真┤
        accessibility-reviewer ★ ─ U5 无障碍┤
        technical-writer ─ U6 交付 DoD ────┤
                                          ▼
        [Step4 裁决] Token+组件+徽章+AI+三态+保真+无障碍+DoD+产品一致
                       全过→UED 设计就绪(交 frontend-coder)  /  有缺→驳回
                                          ▼
        [Step5 signals] token 违规 / 组件复用缺口 / a11y / 三态缺失 / 规格滞后
```

---

## 反模式(一票否决)

- ❌ 跳过 Step0 读 UED规范/原型,直接开始设计 UI
- ❌ 规范没有的颜色/组件也"顺手用"(§N.1/N.9,先注册再用)
- ❌ 原型没这个页面也让前端"自由发挥"(回 §M.1,需求层缺失)
- ❌ 状态徽章色凭感觉(§N.2 一票否决,必须对 PRD-MAPPING §3)
- ❌ 只设计正常态,漏空/载/错三态(§N.5 高频遗漏)
- ❌ 无障碍"差不多"(对比度/focus/label/不靠色是 §11 MUST)
- ❌ "先让前端写着,UED 回头补"(UED 设计就绪 Gate 形同虚设)
- ❌ 编排只下发不裁决(设计了等于没设计)
- ❌ 抢 product-orchestrator 的活(字段/状态建模是 prd-author 的)

---

## 引用

| 文件 | 用途 |
|---|---|
| [`.claude/agents/ued-orchestrator.md`](../../agents/ued-orchestrator.md) | 本 skill 的角色/裁决判断 |
| [`.claude/agents/ued-designer.md`](../../agents/ued-designer.md) | U1-U3 UI 规格建模执行细节 |
| [`.claude/agents/ux-prototype-aligner.md`](../../agents/ux-prototype-aligner.md) | U4 原型保真守门执行细节 |
| [`.claude/agents/accessibility-reviewer.md`](../../agents/accessibility-reviewer.md) | U5 无障碍守门执行细节 |
| [`.claude/rules.md` §N(UED 9 项)+ §N.10(UED 编排)+ §M.9(产品设计,上游)](../../rules.md) | 硬卡控 |
| [`99-跨阶段/UED设计工作流.md`](../../../99-跨阶段/UED设计工作流.md) | 全流程 + 角色矩阵 + 进化节律 |
| [`02-设计/UED规范.md`](../../../02-设计/UED规范.md) | 单一事实来源(§1/§5/§7/§11/§12.3/§13/附录A) |
| [`PRD-MAPPING.md`](../../../PRD-MAPPING.md) | 字段/状态机来源(§2/§3) |
| [`.claude/skills/plm-product-design/SKILL.md`](../plm-product-design/SKILL.md) | 上游(产品设计)对位 SOP |
| [`.claude/skills/plm-test-orchestrate/SKILL.md`](../plm-test-orchestrate/SKILL.md) | 下游(测试)对位 SOP |

## 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-27 | 首次创建:固化 UED 设计编排 SOP(proposal 0026) |

---
name: plm-arch-design
description: PLM 系统架构设计编排 SOP — 当用户要「这模块/系统架构怎么设计」「分层依赖对不对」「要不要上抽象层(门面/SPI)」「跨模块共享能力怎么横切」「引入新维度怎么抽象」「演进路径/向后兼容怎么走」「Phase 02 架构准入」「架构对得上 模块拆分架构.md 吗」时,按架构设计漏斗(架构驱动力→概念架构→抽象层→演进路径→架构评审守门→契约→装配→交付ADR)出计划、分派子 agent(system-architect/arch-reviewer/api-contract-keeper/config-engineer/db-modeler/security-reviewer/technical-writer/prompt-engineer)、裁决"架构设计就绪"Gate、把结果沉淀成 signals 自进化。本 skill 是 arch-orchestrator agent 的执行手册;只出架构设计不编排时用 system-architect,只评审守门不编排时用 arch-reviewer。
---

# plm-arch-design — PLM 系统架构设计编排 SOP

把"写代码**之前**该怎么做架构设计"固化成可重复的编排流程。**编排 + 裁决 + 沉淀**在本 skill;**角色判断**在 [`arch-orchestrator` agent](../../agents/arch-orchestrator.md);**全流程/角色矩阵**在 [`架构设计工作流.md`](../../../99-跨阶段/架构设计工作流.md)。

> 一句话边界:`plm-product-design`(需求维度,字段)、`plm-db-design`(数据维度,schema)、`plm-ued-design`(UI 维度,组件)是 Phase 02 的另三个设计维度;`plm-arch-design`(本 skill)管**架构维度**——分层/边界/抽象/演进。四者各自就绪 Gate 都过 = Phase 02→03 准入。

---

## 何时触发

| 语义 | 用户原话举例 | 走本 skill 还是子 agent |
|---|---|---|
| 设计一个模块/系统架构 | "这模块架构怎么设计"、"这能力怎么落架构" | **本 skill**(架构漏斗编排) |
| Phase 02 架构准入 | "架构设计完了"、"可以让后端写了吗"、"架构准入" | **本 skill**(编排+裁决架构设计就绪) |
| 引入新维度/抽象 | "1 Provider→N Provider 怎么抽象"、"要不要上门面/SPI" | **本 skill**(A2/A3) |
| 跨模块共享能力 | "审计/限流/事件总线怎么横切"、"这两模块怎么解耦" | **本 skill**(A2/A3 可选注入/SPI) |
| 演进/兼容 | "改这接口会破坏兼容吗"、"演进路径怎么走" | **本 skill**(A4) |
| 分层评审 | "分层对吗"、"有循环依赖吗"、"这能 import 那个吗" | `arch-reviewer`(不必编排) |
| 只出架构设计 | "给 X 能力出门面+SPI 设计草案" | `system-architect`(不必编排) |

---

## 编排 5 步法

### Step 0 — 先读 SSoT(MUST,红线前置)
任何架构设计动作前,读 [03-开发/模块拆分架构.md](../../../03-开发/模块拆分架构.md)(分层/模块边界)+ [03-开发/ADR/](../../../03-开发/ADR/)(已有决策)+ 根 [CLAUDE.md](../../../CLAUDE.md) "Architecture"(依赖图 admin→framework→system→common)+ [.claude/rules.md §A](../../rules.md)(包名/分层/命名)。
- **重大/不可逆决策(选型/拆合模块/引入新抽象) → 先停**:走 §L.1 落 ADR(`03-开发/ADR/NNNN-*.md`),决策"为什么"必须留痕,再继续。
- **引入跨模块依赖 → 先确认方向**:依赖必须单向,跨模块共享走 SPI/接口反向依赖(common 定接口下游实现),**禁循环依赖/反向 import**。

### Step 1 — 判范围(设计多深)
```
纯 CRUD 新模块(沿用现有分层,无新抽象)  → 仅 arch-reviewer 核分层/边界/命名(§A)
引入 1 个抽象层(门面/SPI)              → system-architect 出模式选型(防过度)→ arch-reviewer 核依赖方向
跨模块共享能力(审计/限流/事件)         → system-architect 出可选注入/SPI → arch-reviewer 核无反向 import
改公开接口(可能破坏兼容)               → system-architect 出兼容性表 → arch-reviewer 核向后兼容
重大/不可逆决策                        → 必走 §L.1 落 ADR,再 arch-reviewer 核 ADR 完整
新引入维度 / Phase 02 架构准入          → 全漏斗(强制)
架构含新表                            → 转 db-orchestrator(数据维度)
```

### Step 2 — 出架构漏斗计划(分几层)
按架构设计漏斗列本次要走的层(见 [agent 漏斗图](../../agents/arch-orchestrator.md)):
- **A1 架构驱动力** scope-decider+system-architect:质量属性/约束/为什么要这架构(性能/扩展/解耦)
- **A2 概念架构** system-architect ★:C4 Context/Container、模块边界、依赖方向
- **A3 抽象层设计** system-architect ★:门面+SPI+Provider / 可选注入 / 横切独立事务(选自三模式)
- **A4 演进路径** system-architect:兼容性表、V1→V2→V3、迁移
- **A5 架构评审守门** arch-reviewer ★:分层依赖方向/循环/边界/ADR 完整/接口兼容/§13 校准
- **A6 契约** api-contract-keeper:跨层/SPI 接口 interface↔impl↔调用方 一致
- **A7 装配** config-engineer:Bean/AutoConfiguration/yml `${VAR:default}` 占位(配置外置)
- **A8 交付** technical-writer:架构设计 .md + ADR + §13 落地校准承诺
- **安全旁路** security-reviewer:认证/权限/数据隔离/横切安全架构
- **数据旁路** db-modeler(转 db-orchestrator):架构含新表/跨模块共享表
- **AI 旁路** prompt-engineer:AI Provider/门面路由架构

> 原则:**架构决策必须指得出 模块拆分架构.md/ADR/§A 依据**;重大决策落 ADR 先于代码;架构设计文档/ADR commit 先于实现 commit(`arch_calibration_lag` 维度)。

### Step 3 — 分派子 agent(谁来做)
按矩阵下发,**主 Claude 按顺序调 Agent**(子 agent 不能再 spawn):

| 子任务 | 分派 |
|---|---|
| 模糊架构指令拆解 | `requirement-clarifier` |
| 架构范围分级 + 驱动力 | `scope-decider` |
| 概念架构+抽象层+演进路径建模 | `system-architect` ★ |
| 架构评审守门 | `arch-reviewer` ★ |
| 跨层/SPI 契约一致 | `api-contract-keeper` |
| Bean 装配/yml 占位 | `config-engineer` |
| 横切安全架构 | `security-reviewer` |
| 架构含新表 | `db-modeler`(经 db-orchestrator) |
| 架构设计文档 + ADR | `technical-writer` |
| AI 架构 + prompt | `prompt-engineer` + `system-architect` |

复杂(≥5 agent)时让 `arch-orchestrator` 出 Mermaid DAG,再 `task-tracker` 拆 TodoWrite。

### Step 4 — 裁决"架构设计就绪"Gate(算不算可让后端写)
逐条核对(§Q.3),全满足才判**就绪**(与 product/db/ued 维度共同构成 Phase 02→03 准入):

- [ ] **分层合规**:依赖方向单向(admin→framework→system→common),无循环依赖/反向 import;业务在 system.business.<entity>(§A)
- [ ] **边界清晰**:模块职责单一;跨模块共享走 SPI/接口反向依赖
- [ ] **抽象适度**:不过度(1 实现不上 SPI)+ 不欠设计(N 实现不硬编码 if-else)
- [ ] **演进可追溯**:重大决策有 ADR(§L.1);有兼容性表/演进路径
- [ ] **接口稳定**:公开 API 向后兼容;破坏性变更有迁移说明
- [ ] **决策点收尾**:草案给 user 1-3 个决策点(system-architect §12)
- [ ] **落地校准承诺**:头部标状态 + 约定落地后补 §13(system-architect §13)
- [ ] **配置外置 + 装配合理**:secret 走 `${VAR:default}`(§C);横切独立事务/AutoConfiguration
- [ ] **安全合规**:涉认证/权限/数据隔离经 security-reviewer(涉密时)

任一不满足 → **驳回**,指明回哪个 agent;**禁**"先开发着架构回头补"、**禁**放过循环依赖/反向 import(P0)。

### Step 5 — 沉淀 signals(自进化)
把本轮架构设计结果记进 [`signals/YYYY-MM.md` 架构设计编排段](../../../99-跨阶段/signals/README.md):
- `layering_violation_count`(依赖方向倒置/循环依赖,**应=0**)
- `boundary_breach_count`(跨模块越界/业务塞错模块/反向 import)
- `abstraction_fit_gap`(过度/欠设计被发现数)
- `missing_adr_count`(重大决策无 ADR,§L.1,**应=0**)
- `interface_break_count`(破坏性接口变更无迁移/兼容表)
- `arch_calibration_lag`(落地后未补 §13 校准,**应=0**)

**触发提案**(主动建议):`layering_violation_count` > 0 → **P0 复盘** + 加 ArchUnit/依赖检查 hook 提案;`missing_adr_count` 反复 → ADR 模板/commit 关联强化提案;`abstraction_fit_gap` 集中过度设计 → YAGNI checklist 提案;`interface_break_count` > 0 → 接口版本化 + 兼容性测试纳入 CI 提案。

---

## 编排速查卡

```
请求 → [Step0 读 模块拆分架构+ADR+依赖图] → 重大决策? → 先落 ADR(§L.1) → 继续
                │ 引入跨模块依赖? → 确认单向+SPI 反向依赖 → 继续
                ▼
[Step1 范围] → [Step2 架构漏斗分层] → [Step3 分派 DAG]
                                          │
        system-architect ★ ─ A1 架构驱动力─┤
        system-architect ★ ─ A2 概念架构 ──┤
        system-architect ★ ─ A3 抽象层 ────┤  ← 架构设计/ADR 先 commit
        system-architect   ─ A4 演进路径 ──┤
        arch-reviewer ★ ─ A5 分层/ADR守门 ─┤
        api-contract-keeper ─ A6 契约 ─────┤
        config-engineer ─ A7 装配 ─────────┤
        technical-writer ─ A8 交付 ADR ────┤
                                          ▼
        [Step4 裁决] 分层+边界+抽象+ADR+接口兼容+决策点+校准+装配+安全
                       全过→架构设计就绪(交 backend-coder)  /  有缺→驳回
                                          ▼
        [Step5 signals] 分层违规 / 边界越界 / 抽象失配 / 缺 ADR / 接口破坏 / 校准滞后
```

---

## 反模式(一票否决)

- ❌ 跳过 Step0 读 模块拆分架构/ADR,直接开始设计架构
- ❌ 放过循环依赖/反向 import("先这么写着回头改"——分层是地基,P0)
- ❌ 重大决策"口头定了就写",不落 ADR(§L.1 红线)
- ❌ 过度设计:1 个实现也上门面/SPI/抽象工厂(YAGNI)
- ❌ 欠设计:N 个 Provider 硬编码 if-else 还说"架构就绪"
- ❌ 破坏性接口变更不出兼容性表/迁移说明
- ❌ "先开发着,架构回头补"(架构设计就绪 Gate 形同虚设)
- ❌ 编排只下发不裁决(设计了等于没设计)
- ❌ 抢 db-orchestrator 的活(表怎么建是 db-modeler 的;你只管"表归哪个模块、共享表横切")

---

## 引用

| 文件 | 用途 |
|---|---|
| [`.claude/agents/arch-orchestrator.md`](../../agents/arch-orchestrator.md) | 本 skill 的角色/裁决判断 |
| [`.claude/agents/system-architect.md`](../../agents/system-architect.md) | A1-A4 架构建模执行细节(门面/SPI/可选注入/演进/§12 决策点/§13 校准) |
| [`.claude/agents/arch-reviewer.md`](../../agents/arch-reviewer.md) | A5 架构评审守门执行细节 |
| [`.claude/rules.md` §Q(架构编排)+ §A(分层/命名)+ §L.1(ADR)](../../rules.md) | 硬卡控 |
| [`99-跨阶段/架构设计工作流.md`](../../../99-跨阶段/架构设计工作流.md) | 全流程 + 角色矩阵 + 进化节律 |
| [`03-开发/模块拆分架构.md`](../../../03-开发/模块拆分架构.md) | 分层/模块边界 SSoT |
| [`03-开发/ADR/`](../../../03-开发/ADR/) | 架构决策记录(决策"为什么") |
| [`.claude/skills/plm-product-design/SKILL.md`](../plm-product-design/SKILL.md) / [`plm-db-design`](../plm-db-design/SKILL.md) / [`plm-ued-design`](../plm-ued-design/SKILL.md) | 另三个平级设计维度 SOP |
| [`.claude/skills/plm-test-orchestrate/SKILL.md`](../plm-test-orchestrate/SKILL.md) | 下游(测试)对位 SOP |

## 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-27 | 首次创建:固化系统架构设计编排 SOP(proposal 0027,对位 0023 测试 / 0024 产品 / 0025 数据库 / 0026 UED)|

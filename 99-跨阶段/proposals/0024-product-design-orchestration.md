# Proposal 0024: 产品设计编排自进化系统（product-orchestrator + prd-author + ux-prototype-aligner + skill + rule + workflow + signals）

> §L.2 记录：本提案对应**用户明确请求**的规范改动（改 `.claude/rules.md` 加 §M.9 + 新增 `99-跨阶段/产品设计工作流.md`），用户在 AskUserQuestion 中选择"全套现在落地 + proposal 作记录",按 §L.2 例外条款 **User-requested** 同步落地并在此事后补录。是 [proposal 0023 测试编排](0023-test-orchestration-self-evolution.md) 的**开发前对位版**(设计 vs 测试)。

---

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0024 |
| 标题 | 产品设计编排自进化系统 |
| 状态 | **approved → 待 commit 转 merged**（User-requested;文件已落地,commit hash 待回填见 §10）|
| 类型 | 流程 / 工具链 |
| 提出人 | 用户 + Claude |
| 提出日期 | 2026-05-27 |
| 评审人 | 用户（项目 owner,AskUserQuestion 选"全套现在落地 + proposal 记录" + "+2 新子agent"）|
| 评审日期 | 2026-05-27 |
| Tracking 截止 | 2026-06-24（merged 后 4 周）|

---

## 1. 背景（What's the problem?）

项目已有丰富的 PRD 驱动**硬规则**(§M.1~M.8 + §N UED + PRD-MAPPING SSoT + 9 项 DoD),也有零散的设计相关子 agent(`requirement-clarifier` 澄清、`scope-decider` 分级、`system-architect` 架构、`technical-writer` 文档、`db-modeler` 建模、`api-contract-keeper` 契约),但缺两样:

1. **一个把"开发前的产品设计"编排成一条线的总管**:需求拆什么、字段从哪来、原型对不对得上、什么算"设计就绪可以开发"、跑偏怎么处置、结果如何回流自进化——目前散在各 rule/agent 里,靠 Claude 临场拼。
2. **产品设计域的专属执行者**:现有 agent roster 偏实现/测试,**没有**"需求→PRD 字段建模"(锚定 PRD-MAPPING)和"原型/交互保真守门"(§N)这两个**最核心的产品经理动作**的专属 agent。

用户希望对位刚落地的 [测试编排系统(0023)](0023-test-orchestration-self-evolution.md),建一个**能自己做、自己进化**的产品设计体系——开发**之前**的"设计就绪"总管,与开发**之后**的"测得过"总管串成完整生命周期。

## 2. 证据（Evidence）

- **用户请求**：2026-05-27 会话原话——"增加一个开发过程中的产品经理的 agent,分管多个子 agent,增加测试的 skill 和 rule、workflow,让我在设计 PLM 项目产品设计过程能够自己去做和去进化"。该句是 [proposal 0023](0023-test-orchestration-self-evolution.md) §2 用户请求(测试编排)的**近逐字孪生**(测试→产品经理 / 测试过程→产品设计过程),确认意图是"把测试编排的整套机制,对位复制给产品设计"。
- **§L.2 User-requested-bypass**：用户在 AskUserQuestion 中选择"全套现在落地 + proposal 作记录"+"+2 新子agent(推荐)",明确授权先落地后记录、并新建 2 个产品设计专属子 agent。
- 关联现状：产品设计域无总管 agent;PRD 驱动硬规则(§M)散落,无"漏斗分层 + 设计就绪裁决 + 自进化"的统一编排层;signals 无产品设计专项指标(PRD drift / 原型偏离未统计)。

## 3. 提案（What's the change?）

### 改动文件清单

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `.claude/agents/product-orchestrator.md` | 新增 | 产品经理(产品设计编排总管):漏斗+分派+裁决+沉淀 |
| `.claude/agents/prd-author.md` | 新增 ★ | L3 需求建模:user ask → 字段对照表+状态机+错误码,锚 PRD-MAPPING |
| `.claude/agents/ux-prototype-aligner.md` | 新增 ★ | L5 原型对齐守门:表单/徽章/AI 按钮 ↔ 原型 HTML,§N 一票否决 |
| `.claude/skills/plm-product-design/SKILL.md` | 新增 | 编排 5 步法 SOP |
| `.claude/rules.md` §M.9 | 新增 | 产品设计编排与自进化硬约束(漏斗/总管/设计就绪裁决/自进化)|
| `99-跨阶段/产品设计工作流.md` | 新增 | 全流程 + 角色矩阵 + 升级路径 + 进化节律 + 与测试工作流衔接 |
| `99-跨阶段/signals/2099-12.template.md` §9 | 新增 | 产品设计编排 signals 模板 |
| `99-跨阶段/signals/2026-05.md` §10 | 新增 | 当月产品设计 signals 实例(落地基线)|

★ = 用户选"+2 新子agent"对应新建的产品设计专属子 agent。

### 核心设计

- **产品设计漏斗**：L1 澄清(requirement-clarifier) / L2 范围(scope-decider) / L3 PRD 建模(prd-author ★) / L4 数据·架构(db-modeler+system-architect) / L5 原型对齐(ux-prototype-aligner ★) / L6 契约(api-contract-keeper) / L7 文档(technical-writer) + AI 旁路(prompt-engineer);从模糊想法收敛到可 100% 追溯到 PRD+原型 的规格。
- **总管不动手**：`product-orchestrator` 只出"漏斗计划 + DAG + 裁决标准",子 agent 不能再 spawn,主 Claude 按 DAG 顺序调。
- **设计就绪 Gate(并入 §M.9.3)**：可追溯 + 字段表先行(§M.2) + 状态合法(§M.4) + 错误码登记(§M.5) + 原型保真(§N) + 三者一致(§M.1)。Phase 02→03 准入。
- **自进化闭环**：每轮收口记产品设计 signals(prd_drift / 原型偏离 / 不可追溯字段 / 字段表滞后 / PRD 演化走 proposal 占比)→ reflect 发现模式 → proposal 改规则/工具。
- **生命周期闭合**：与 0023 串成 `product-orchestrator`(设计就绪,Phase 02→03)→ coder 开发 → `test-orchestrator`(测得过,Phase 03→04)。

## 4. 影响范围（Impact）

| 受众 | 影响 |
|---|---|
| 开发者 | 多一个"这模块怎么设计 / 需求怎么拆"的统一入口;Phase 02 准入更标准化 |
| Claude | rules §M.9 下次会话起生效;遇设计/需求任务优先走 product-orchestrator;遇"需求不在 PRD"必停走 §M.1 |
| 产品质量 | PRD drift / 原型偏离从此可量化;不可追溯字段被主动拦截 |
| 已有资产 | 不改现有子 agent / §M.1~M.8 / §N(向后兼容),只在其上加编排层 + 2 个新子 agent |

## 5. 风险（Risks）

- 风险 1：3 个新 agent + skill + rule 与现有 §M 硬规则职责重叠、用户混淆 → 缓解:agent/skill/workflow 三处写明边界(产品经理 = 编排+裁决,prd-author = 建模,ux-prototype-aligner = 原型守门),并明确 §M.9 是把 §M.1~M.8 编排起来而非替代。
- 风险 2：总管变成"形式主义",2-3 agent 小任务也摆 DAG → 缓解:反模式明列,小改动直接顺序调(skill Step 1 给定向路径)。
- 风险 3：产品设计 signals 没人填 = 自进化空转 → 缓解:§M.9.4 设为 MUST,收口即记;后续 `/reflect-monthly` 自动采集。
- 风险 4：体系建好但因当前 31 模块已全 🟢,短期无新需求触发、无法 dogfood → 缓解:tracking 期(至 2026-06-24)盯首个新需求/新模块设计是否真走编排;若整个 tracking 期 0 次使用 → §10 判 reverted/简化。

## 6. 备选方案（Alternatives Considered）

- 方案 A：只加总管 agent,复用全部现有子 agent,不新建 → **未选**(用户选"+2 新子agent"):产品设计最核心的两个动作(PRD 字段建模 / 原型保真)无专属执行者,总管会"分管"一个缺核心成员的 roster。
- 方案 B：再多建 competitive-analyst / acceptance-author 等 → 未选:超出"对位 0023"的最小必要,先上 2 个核心子 agent,后续按 signals 缺口再议(留作 P2)。
- 方案 C：复用 flow-orchestrator → 未选:那是通用 DAG,不懂 PRD 漏斗/PRD-MAPPING 可追溯/§M 9 项 DoD/§N 原型保真,产品设计域需要专门总管。
- 方案 D：纯文档(只写 workflow)→ 未选:无 agent/skill 则 Claude 不自动触发,"自己去做"落空(与 0023 同理)。

## 7. 实施计划（Implementation Plan）

```
[x] Step 1: 建 product-orchestrator agent(产品经理总管)
[x] Step 2: 建 prd-author agent(L3 需求建模)
[x] Step 3: 建 ux-prototype-aligner agent(L5 原型守门)
[x] Step 4: 建 plm-product-design skill(编排 5 步法)
[x] Step 5: rules.md 加 §M.9(注:§M.6~M.8 已占用,故用 §M.9)
[x] Step 6: 建 产品设计工作流.md
[x] Step 7: signals 模板加 §9 + 当月实例加 §10 产品设计编排
[x] Step 8: 建本 proposal 0024
[ ] Step 9: commit(待用户确认 [solo-review] commit)+ 回填 §10 commit hash → 转 merged
[ ] Step 10: 进入 tracking 期,看首个新需求设计是否走 product-orchestrator 编排
[ ] Step 11: 后续 — 模块工作流.md Phase 02 段加链接到本工作流(小改,下次顺带)
```

## 8. 衡量指标（How will we know it worked?）

- `prd_drift_count` / `untraceable_field_count`：从"未统计"改善为**每轮设计分开记录**(基线=0/未统计 → 目标=tracking 期有真实数据)。
- 设计任务是否经 product-orchestrator 编排：tracking 期内新模块/新需求设计 100% 走编排(基线=0 → 目标=首个用例即走)。
- `field_table_lag`：字段对照表晚于代码 commit 的次数,维持 = 0(§M.2 不倒挂)。

跟踪期：`2026-05-27` ~ `2026-06-24`（4 周）。

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| 用户（owner）| 通过（AskUserQuestion 选"全套现在落地 + proposal 记录" + "+2 新子agent"）| 2026-05-27 | §L.2 User-requested 授权 |

---

## 10. 实施后跟踪（merged 后填）

### 实际 PR / commit
- PR: —（分支 `chore/local-start-backend-script`,solo-review 直接合入,未走 PR）
- 合入 commit: **待回填**（本提案落地待用户确认 [solo-review] commit 后补 hash,参照 0023 的 c172420 回填模式）
- 实际 merged 日期：待 commit

### Tracking 数据

| 信号 | 基线 | 目标 | 实际（周 1）| 实际（周 2）| 实际（周 N）|
|---|---|---|---|---|---|
| prd_drift_count / untraceable_field_count | 0/未统计 | 每轮分开记 | | | |
| 设计任务经 product-orchestrator 编排 | 0 | 首个用例即走 | | | |
| field_table_lag | 0 | 维持 0 | | | |

### 最终判定
- [ ] done（产品设计编排被持续使用 + signals 持续记录）
- [ ] reverted（整个 tracking 期 0 次使用 → 回滚或简化为纯 workflow 文档）

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude | 首次创建：8 artifacts 落地记录(对位 proposal 0023 测试编排)|

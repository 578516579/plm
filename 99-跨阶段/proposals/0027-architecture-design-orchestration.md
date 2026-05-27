# Proposal 0027: 系统架构设计编排自进化系统（arch-orchestrator + arch-reviewer + skill + rule + workflow + signals）

> §L.2 记录：本提案对应**用户明确请求**的规范改动（改 `.claude/rules.md` 加 §Q + 新增 `99-跨阶段/架构设计工作流.md` + signals 段），用户原话"增加一个开发过程中的架构师的 agent,分管多个子 agent,增加测试的 skill 和 rule、workflow,让我在设计 PLM 项目系统架构设计过程能够自己去做和去进化"。按 §L.2 例外条款 **User-requested** 同步落地并在此事后补录。是 [proposal 0023 测试编排](0023-test-orchestration-self-evolution.md) / [0024 产品设计编排](0024-product-design-orchestration.md) / [0025 数据库设计编排](0025-db-design-orchestration.md) / [0026 UED 设计编排](0026-ued-design-orchestration.md) 的**第五个同范式**(测试·开发后 → 产品·需求维度 → 数据库·数据维度 → UED·UI 维度 → **架构·架构维度**)。

---

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0027 |
| 标题 | 系统架构设计编排自进化系统 |
| 状态 | **implementing**（User-requested;6 新文件 + 5 处 SSoT 编辑已落,待用户授权 commit → 届时按 [0021](0021-proposal-merged-requires-commit.md) 回填真实 commit hash 转 merged → tracking）|
| 类型 | 流程 / 工具链 |
| 提出人 | 用户 + Claude |
| 提出日期 | 2026-05-27 |
| 评审人 | 用户（项目 owner,AskUserQuestion 选"是,建架构设计编排体系" + "全套现在落地 + proposal 0027 记录" + "+1:新建 arch-reviewer 守门"）|
| 评审日期 | 2026-05-27 |
| Tracking 截止 | commit 后 4 周 |

> **编号/章节说明**:0023(测试 §G.5)/ 0024(产品 §M.9)/ 0025(数据库 §M.10)/ 0026(UED §N.10)已占。本提案(架构)取 **0027**;rule 章节 §M(PRD 驱动)、§N(UED/GitHub)、§O(并行 session)、§P(前端 TS)已占,架构维度自成一节取 **§Q**;signals 段 db §11、UED §12 已占,架构取模板 **§12** / 当月实例 **§13**,顺位无冲突。

---

## 1. 背景（What's the problem?）

项目已有丰富的架构**硬规则**(§A 包名/分层/命名 + CLAUDE.md "Architecture" 依赖图 + [03-开发/模块拆分架构.md](../../03-开发/模块拆分架构.md) + [03-开发/ADR/](../../03-开发/ADR/) 决策留痕),也有一个架构相关子 agent(`system-architect` 出门面/SPI/演进路径/决策点),但缺三样:

1. **一个把"开发前的系统架构设计"编排成一条线的总管**:引入新维度怎么抽象、模块边界怎么切、分层依赖方向对不对、跨模块共享能力怎么横切、要不要上门面/SPI、演进与向后兼容怎么保证、什么算"架构设计就绪可以让后端写"、跑偏(循环依赖/过度设计)怎么处置、结果如何回流自进化——目前散在 §A/CLAUDE.md/模块拆分架构.md 里,靠 Claude 临场拼。
2. **架构设计域的"评审守门"执行者**:现有 `system-architect` 只**建模**(出设计草案),没有从分层/依赖方向/ADR 完整性/接口兼容角度**守门**的角色——这是架构版的 `db-schema-reviewer`(db 有建模 db-modeler + 守门 db-schema-reviewer;UED 有建模 ued-designer + 守门 accessibility-reviewer/ux-prototype-aligner;架构只有建模 system-architect,缺守门)。
3. **架构设计域的自进化指标**:signals 无架构专项指标(分层违规 / 边界越界 / 抽象失配 / 缺 ADR / 接口破坏 / 草案落地偏离 未统计)。

`product-orchestrator`(0024)漏斗里把"架构维度"当 **L4 一道 `system-architect` 设计**;本提案**把那个盒子拆开**,建一个**能自己做、自己进化**的架构设计体系——与产品设计(需求维度)、数据库设计(数据维度)、UED 设计(UI 维度)并列的 **Phase 02 第四个设计维度总管**。**至此 Phase 02 四维度(需求/数据/UI/架构)编排体系全部建成**。

## 2. 证据（Evidence）

- **用户请求**：2026-05-27 会话原话——"增加一个开发过程中的架构师的 agent,分管多个子 agent,增加测试的 skill 和 rule、workflow,让我在设计 PLM 项目系统架构设计过程能够自己去做和去进化"。该句是 [proposal 0023](0023-test-orchestration-self-evolution.md) / [0024](0024-product-design-orchestration.md) / [0025](0025-db-design-orchestration.md) / [0026](0026-ued-design-orchestration.md) §2 用户请求的**近逐字孪生**(测试/产品/数据库/UED → 架构),确认意图是"把编排系统的整套机制,对位复制给系统架构设计"。"增加测试的 skill 和 rule、workflow"中的"测试"指向**已存在的测试编排体系(0023)作为要照搬的模板**,落点(由"系统架构设计过程能够自己去做和去进化"明确)是架构维度;经 AskUserQuestion 确认。
- **§L.2 User-requested-bypass**：用户在 AskUserQuestion 中明确选择"是,建架构设计编排体系" + "全套现在落地 + proposal 0027 记录" + "+1:新建 arch-reviewer 守门",授权先落地后记录、并新建 1 个架构专属守门子 agent。
- 关联现状：架构域无总管 agent;架构硬规则(§A/CLAUDE.md/模块拆分架构.md)散落,无"漏斗分层 + 架构设计就绪裁决 + 自进化"统一编排层;`system-architect` 只建模无守门;signals 无架构专项指标。
- **同范式佐证**:同会话已对位建成 0023(测试)/0024(产品)/0025(数据库)/0026(UED)四个编排体系,证明"编排系统对位复制到各设计/开发维度"是项目当前主动演进方向;架构是最后一个未覆盖的 Phase 02 设计维度。

## 3. 提案（What's the change?）

### 改动文件清单

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `.claude/agents/arch-orchestrator.md` | 新增 | 系统架构设计编排总管(架构师):漏斗 A1-A8 + 分派 + 裁决 + 沉淀 |
| `.claude/agents/arch-reviewer.md` | 新增 ★ | A5 架构评审守门:分层依赖方向/循环/边界/ADR 完整/接口兼容/§13 校准,一票否决(循环依赖 + 缺 ADR) |
| `.claude/skills/plm-arch-design/SKILL.md` | 新增 | 编排 5 步法 SOP |
| `.claude/rules.md` §Q | 新增 | 系统架构设计编排与自进化硬约束(漏斗/总管/架构设计就绪裁决/自进化)|
| `99-跨阶段/架构设计工作流.md` | 新增 | 全流程 + 角色矩阵 + 升级路径 + 进化节律 + 与产品/数据库/UED/测试工作流衔接 |
| `99-跨阶段/signals/2099-12.template.md` §12 | 新增 | 架构设计编排 signals 模板 |
| `99-跨阶段/signals/2026-05.md` §13 | 新增 | 当月架构 signals 实例(落地基线)|
| `.claude/agents/system-architect.md` | 编辑 | 补一段:现作为 arch-orchestrator 漏斗 A1-A4 的核心建模者(架构维度"建模者",对位 prd-author/db-modeler/ued-designer)|
| `99-跨阶段/proposals/README.md` | 编辑 | 状态索引追加 0027 行 |
| `99-跨阶段/在途任务.md` | 编辑 | 进行中段追加 0027 落地台账行 |

★ = 用户选"+1:新建 arch-reviewer 守门"对应新建的架构专属守门子 agent。`system-architect` 复用为核心建模者(不重建),其余(api-contract-keeper/config-engineer/security-reviewer/technical-writer/prompt-engineer/scope-decider/requirement-clarifier/db-modeler)全复用。

### 核心设计

- **架构设计漏斗**：A1 架构驱动力(scope-decider+system-architect) / A2 概念架构(system-architect ★) / A3 抽象层设计(system-architect ★) / A4 演进路径(system-architect) / A5 架构评审守门(arch-reviewer ★) / A6 契约(api-contract-keeper) / A7 装配(config-engineer) / A8 交付 ADR(technical-writer) + 安全/数据/AI 旁路;从一句架构需求收敛到分层合规、抽象适度、可演进、能追溯到 模块拆分架构.md + ADR 的架构设计。
- **总管不动手**：`arch-orchestrator` 只出"漏斗计划 + DAG + 裁决标准",子 agent 不能再 spawn,主 Claude 按 DAG 顺序调。
- **架构设计就绪 Gate(并入 §Q.3)**：分层合规(单向依赖/无循环/无反向 import,§A) + 边界清晰(SPI 反向依赖) + 抽象适度(防过度/欠设计) + 演进可追溯(ADR §L.1 + 兼容性表) + 接口稳定 + 决策点收尾(§12) + 落地校准承诺(§13) + 配置外置(§C) + 安全合规。与 product/db/ued 维度共同构成 Phase 02→03 准入。
- **自进化闭环**：每轮收口记架构 signals(分层违规 / 边界越界 / 抽象失配 / 缺 ADR / 接口破坏 / 校准滞后)→ reflect 发现模式 → proposal 改规则/工具(如加 ArchUnit 依赖检查 hook / ADR commit 关联 / YAGNI checklist)。
- **生命周期闭合**：与 0024/0025/0026/0023 串成 `product-orchestrator`(需求) ∥ `db-orchestrator`(数据) ∥ `ued-orchestrator`(UI) ∥ `arch-orchestrator`(架构) **四维度皆绿**(Phase 02→03)→ coder 开发 → `test-orchestrator`(测得过,Phase 03→04)。**Phase 02 四维度编排体系至此全部建成**。
- **不过度强制**:架构维度区别于其他三维——**纯 CRUD 沿用现有分层不强制全漏斗**,仅 arch-reviewer 轻量核分层/命名;仅"引入新维度/跨模块共享/抽象不够用/改公开接口/重大选型"时强制全漏斗(避免小模块也摆架构 DAG)。

## 4. 影响范围（Impact）

| 受众 | 影响 |
|---|---|
| 后端/架构开发者 | 多一个"这架构怎么设计 / 分层对不对 / 要不要上抽象层 / 跨模块怎么解耦"的统一入口;Phase 02 架构准入更标准化 |
| Claude | rules §Q 下次会话起生效;遇架构设计任务(引入新维度/跨模块/改接口)优先走 arch-orchestrator;遇循环依赖/反向 import 必 P0 阻断、重大决策必落 ADR(§L.1) |
| 架构质量 | 分层违规 / 抽象失配 / 缺 ADR / 接口破坏从此可量化;"循环依赖地基塌方""过度/欠设计"被主动拦截 |
| 已有资产 | 不改 §A / CLAUDE.md 依赖图 / 模块拆分架构.md / `system-architect` 职责(向后兼容),只在其上加编排层 + 1 个新守门子 agent + system-architect 一段复用说明 |

## 5. 风险（Risks）

- 风险 1：与 `product-orchestrator`(0024)的 L4 `system-architect`、`db-orchestrator`(0025)的 L2 `system-architect` 职责重叠、用户混淆 → 缓解:三处(agent/skill/workflow)写明边界——product 管"需求→规格"把架构当一道设计,db 管"概念建模/共享表抽象"(数据视角),arch 把架构维度拆开管全生命周期(分层/边界/抽象/演进);`system-architect` 是多个总管共用的核心建模子 agent,不重建。
- 风险 2：架构维度 vs 数据维度的"system-architect 共享" → 缓解:db-orchestrator 用 system-architect 限于"实体关系/范式/共享表"(数据视角);arch-orchestrator 用其全部能力(分层/抽象/演进);架构含新表时 arch 显式转 db-orchestrator,不抢 db-modeler 的活。
- 风险 3：总管变成"形式主义",纯 CRUD 小模块也摆架构 DAG → 缓解:§Q SSoT 注 + workflow §8 注 + skill Step 1 明列"纯 CRUD 仅 arch-reviewer 轻量核",仅引入新维度/跨模块/改接口才全漏斗。
- 风险 4：架构 signals 没人填 = 自进化空转 → 缓解:§Q.4 设为 MUST,收口即记;后续 `/reflect-monthly` 自动采集。
- 风险 5：体系建好但当前 31 模块已全 🟢、架构稳定,短期无新维度/跨模块需求触发、无法 dogfood → 缓解:tracking 期盯首个引入新维度(如真实 AI 多 Provider 接入 / MCP / Integration 扩展)的架构设计是否真走编排;若整个 tracking 期 0 次使用 → §10 判 reverted/简化。**注**:MCP/Integration(proposal 0007)正在真实接入中,是架构编排体系最可能的首个 dogfood 场景。

## 6. 备选方案（Alternatives Considered）

- 方案 A：只加总管 agent,复用 `system-architect` 直接裁决,不新建守门 → **未选**(用户选"+1 arch-reviewer"):架构最核心的守门动作(分层依赖方向/循环依赖/ADR 完整性)无独立"一票否决"执行者,总管会"既当运动员又当裁判"(system-architect 出设计又自审),与 db/UED 维度"建模+守门分离"的范式不一致。
- 方案 B：+2(arch-reviewer + dependency-analyzer 跑 mvn dependency/扫包导入) → 未选:当前 38 模块手工 grep import 方向 + 看 pom 依赖已够;独立 dependency-analyzer 在无 ArchUnit/自动化依赖检查工具前价值有限,过度。其能力先并进 arch-reviewer 第 1 维(用 Bash 跑 `mvn dependency:tree`/grep import);若 tracking 期发现依赖检查频繁且手工成本高 → 再起 proposal 加 ArchUnit hook + 专属 analyzer。
- 方案 C：把架构编排并进 product-orchestrator(不另立总管)→ 未选:product 已管需求维度漏斗(7 层),再塞架构全维度会过载;且架构的 SSoT 是 模块拆分架构.md/ADR 而非 PRD-MAPPING,裁决标准(分层/抽象/演进)不同。
- 方案 D：纯文档(只写 workflow)→ 未选:无 agent/skill 则 Claude 不自动触发,"自己去做"落空(与 0023/0024/0025/0026 同理)。

## 7. 实施计划（Implementation Plan）

```
[x] Step 1: 建 arch-orchestrator agent(系统架构设计编排总管)
[x] Step 2: 建 arch-reviewer agent(A5 架构评审守门)
[x] Step 3: 建 plm-arch-design skill(编排 5 步法)
[x] Step 4: rules.md 加 §Q(M/N/O/P 已占,架构自成一节取 §Q)
[x] Step 5: 建 架构设计工作流.md
[x] Step 6: signals 模板加 §12 + 当月实例加 §13 架构设计编排
[x] Step 7: 建本 proposal 0027 + proposals/README 索引行
[x] Step 8: 编辑 system-architect.md 补"作为 arch-orchestrator 核心建模者"一段
[x] Step 9: 在途任务.md 加落地台账行
[ ] Step 10: commit(待用户授权"提交")→ 按 0021 回填真实 commit hash → 转 merged → tracking
[ ] Step 11: 进入 tracking 期,看首个引入新维度/跨模块的架构设计是否走 arch-orchestrator 编排(MCP/Integration 最可能首发)
[ ] Step 12: 后续 — 模块工作流.md Phase 02 段加链接到本工作流(小改,下次顺带)
```

## 8. 衡量指标（How will we know it worked?）

- `layering_violation_count`：从"未统计"改善为**每轮架构设计分开记录**(基线=0/未统计 → 目标=tracking 期有真实数据,且循环依赖/反向 import 维持 0)。
- 架构设计任务是否经 arch-orchestrator 编排：tracking 期内引入新维度/跨模块共享的架构设计 100% 走编排(基线=0 → 目标=首个用例即走)。
- `missing_adr_count` / `arch_calibration_lag`：维持 = 0(重大决策必有 ADR、草案落地后必校准)。

跟踪期：commit 当日 ~ +4 周。

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| 用户（owner）| 通过（AskUserQuestion 选"是,建架构设计编排体系" + "全套现在落地 + proposal 0027 记录" + "+1:新建 arch-reviewer 守门"）| 2026-05-27 | §L.2 User-requested 授权 |

---

## 10. 实施后跟踪（merged 后填）

### 实际 PR / commit
- PR: —（分支 `chore/local-start-backend-script`,solo-review）
- 合入 commit: **待 commit**（6 新文件 + 5 处 SSoT 编辑已落文件系统,待用户授权提交;按 [proposal 0021](0021-proposal-merged-requires-commit.md) "merged = git 事实",未 commit 前状态停 `implementing`,commit 后回填真实 hash 转 merged → tracking)
- 实际 merged 日期：待填

### Tracking 数据

| 信号 | 基线 | 目标 | 实际（周 1）| 实际（周 2）| 实际（周 N）|
|---|---|---|---|---|---|
| layering_violation_count | 0/未统计 | 每轮分开记,循环依赖维持 0 | | | |
| 架构设计任务经 arch-orchestrator 编排 | 0 | 首个用例即走 | | | |
| missing_adr_count / arch_calibration_lag | 0 | 维持 0 | | | |

### 最终判定
- [ ] done（架构编排被持续使用 + signals 持续记录）
- [ ] reverted（整个 tracking 期 0 次使用 → 回滚或简化为纯 workflow 文档）

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude | 首次创建：10 artifacts 落地记录(对位 proposal 0024 产品 / 0025 数据库 / 0026 UED;取 0027 / §Q / signals §12·§13;Phase 02 第四个、也是最后一个设计维度编排体系)|

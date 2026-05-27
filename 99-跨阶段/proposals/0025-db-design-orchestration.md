# Proposal 0025: 数据库设计编排自进化系统（db-orchestrator + db-schema-reviewer + skill + rule + workflow + signals）

> §L.2 记录：本提案对应**用户明确请求**的规范改动（改 `.claude/rules.md` 加 §M.10 + 新增 `99-跨阶段/数据库设计工作流.md`），用户延续 0023/0024 的"全套现在落地 + proposal 作记录"模式,按 §L.2 例外条款 **User-requested** 同步落地并在此事后补录。是 [proposal 0024 产品设计编排](0024-product-design-orchestration.md) 的**下游对位版**(库设计接产品设计的字段对照表)、[proposal 0023 测试编排](0023-test-orchestration-self-evolution.md) 的同范式第 3 例。

---

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0025 |
| 标题 | 数据库设计编排自进化系统 |
| 状态 | **merged → tracking**（User-requested;commit 见 §10）|
| 类型 | 流程 / 工具链 |
| 提出人 | 用户 + Claude |
| 提出日期 | 2026-05-27 |
| 评审人 | 用户（项目 owner,延续 0023/0024 "全套落地 + proposal 记录" 模式）|
| 评审日期 | 2026-05-27 |
| Tracking 截止 | 2026-06-24（merged 后 4 周）|

---

## 1. 背景（What's the problem?）

项目已有强的数据库**执行件**:`db-modeler`(设计期:DDL/字典/索引/迁移草稿)、`db-ops`(运维期:应用 SQL/dedupe/seed/一致性修复)、`api-contract-keeper`(5 层命名契约)、§M.2/M.3/M.7(DoD/字段映射/跨模块一致)、gotcha #2(utf8mb4)、gotcha #7(business-*.sql 必含 sys_menu)+ pre-commit hook。但缺两样:

1. **一个把"建表前的库设计"编排成一条线的总管**:表怎么建、类型字典怎么定、索引够不够、迁移安不安全、charset 对不对、算不算 schema 就绪、跑偏怎么处置、结果如何回流自进化——目前散在 db-modeler/db-ops/§M 各处,靠 Claude 临场拼。
2. **一个设计质量"守门/评审"角色**:db-modeler **产出** DDL,db-ops **应用** SQL,但没有谁在应用前**评审设计质量**(范式取舍 / 索引覆盖查询模式 / charset 合规 / FK 完整性 / 迁移锁表安全 / 命名漂移 / sys_menu 缺失)。这是 DB 域唯一的真空位(对位产品设计的 ux-prototype-aligner、测试的 encoding 守门)。

用户希望对位 0023(测试)/0024(产品设计),建一个**能自己做、自己进化**的数据库设计体系,与前两者串成 **产品设计 → 数据库设计 → 开发 → 测试** 的生命周期链。

## 2. 证据（Evidence）

- **用户请求**：2026-05-27 会话原话——"增加一个开发过程中的数据库设计工程师的 agent,分管多个子 agent,增加测试的 skill 和 rule、workflow,让我在设计 PLM 项目数据库设计过程能够自己去做和去进化"。该句是 0023(测试)/0024(产品经理)用户请求的**第 3 次同结构复刻**(测试→产品经理→数据库设计工程师),确认意图是"把同一套编排机制对位复制给数据库设计"。
- **§L.2 User-requested-bypass**：用户延续 0023/0024 的"全套现在落地 + proposal 作记录"授权模式。
- **roster 差异证据**:读 `db-modeler.md`(设计期 producer,无 review 职责)+ `db-ops.md`(运维期 applier)确认 DB 域已有强 producer/applier 但缺 design-quality reviewer —— 故本提案只新建 1 个守门 agent(db-schema-reviewer),其余复用(对比 0024 因产品设计域空白而新建 2 个)。

## 3. 提案（What's the change?）

### 改动文件清单

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `.claude/agents/db-orchestrator.md` | 新增 | 数据库设计工程师(编排总管):漏斗+分派+裁决 schema 就绪+沉淀 |
| `.claude/agents/db-schema-reviewer.md` | 新增 ★ | L7 设计守门:范式/索引/charset/FK/迁移安全/命名/sys_menu 评审 |
| `.claude/skills/plm-db-design/SKILL.md` | 新增 | 编排 5 步法 SOP |
| `.claude/rules.md` §M.10 | 新增 | 数据库设计编排与自进化硬约束(漏斗/总管/schema 就绪裁决/自进化)|
| `99-跨阶段/数据库设计工作流.md` | 新增 | 全流程 + 角色矩阵 + 升级路径 + 进化节律 + 上下游衔接 |
| `99-跨阶段/signals/2099-12.template.md` §10 | 新增 | 数据库设计编排 signals 模板 |
| `99-跨阶段/signals/2026-05.md` §11 | 新增 | 当月 DB signals 实例(落地基线)|

★ = DB 域唯一真空位(设计质量守门),新建 1 个;db-modeler/db-ops/api-contract-keeper/prd-author/system-architect/config-engineer/technical-writer/security-reviewer **8 个复用**。

### 核心设计

- **数据库设计漏斗**：L1 字段来源(prd-author) / L2 概念逻辑(system-architect) / L3~L6 物理 DDL·字典·索引·迁移(db-modeler ★核心) / L7 评审守门(db-schema-reviewer ★) / L8 契约(api-contract-keeper) / L9 应用核验(db-ops) + 配置旁路(config-engineer)/安全旁路(security-reviewer);从字段对照表收敛到可建表 schema。
- **总管不动手**：`db-orchestrator` 只出"漏斗计划 + DAG + 裁决标准",子 agent 不能再 spawn,主 Claude 按 DAG 顺序调。
- **schema 就绪 Gate(§M.10.3)**：可追溯(列↔PRD-MAPPING §2) + 命名合规(§M.3/§M.7) + charset utf8mb4(gotcha #2) + 索引充分 + 迁移安全 + sys_menu(gotcha #7) + 契约一致 + 应用核验。Phase 02 数据库设计准入。
- **一票否决**：charset 非 utf8mb4(gotcha #2 P0 复发坑)、business-*.sql 缺 sys_menu(gotcha #7 前端不可达)。
- **自进化闭环**：每轮收口记 DB signals(命名漂移/索引缺口/charset 违规/迁移不安全/缺 sys_menu/schema 漂移)→ reflect → proposal。
- **生命周期链**：`product-orchestrator`(字段从哪来)→ **`db-orchestrator`(schema 怎么落地)** → coder 开发 → `test-orchestrator`(测得过)。

## 4. 影响范围（Impact）

| 受众 | 影响 |
|---|---|
| 开发者 | 多一个"这表怎么设计 / 加字段"的统一入口;Phase 02 库准入更标准化 |
| Claude | rules §M.10 下次会话起生效;遇建表/库设计任务优先走 db-orchestrator;charset/sys_menu 一票否决 |
| 数据质量 | 命名漂移/索引缺口/charset 违规从此可量化;设计质量有专门守门(db-schema-reviewer) |
| 已有资产 | 不改 db-modeler/db-ops/api-contract-keeper/§M.2-M.7(向后兼容),只加编排层 + 1 个守门 agent |

## 5. 风险（Risks）

- 风险 1：db-schema-reviewer 与 db-modeler(设计)/api-contract-keeper(命名)/security-reviewer(敏感字段)职责重叠 → 缓解:agent 内写明边界——db-modeler **产出**、db-schema-reviewer **评审守门**(范式/索引/charset/迁移整体质量,api-contract 只管 5 层命名)、security 只管敏感/注入。
- 风险 2：总管形式主义,小改 1 列也摆 DAG → 缓解:skill Step 1 给定向路径(加 1 列只走 prd-author→db-modeler→reviewer,不整漏斗)。
- 风险 3：DB signals 没人填 = 自进化空转 → 缓解:§M.10.4 设为 MUST,收口即记。
- 风险 4：31 模块已 🟢,短期无新表触发、无法 dogfood → 缓解:tracking 期盯首个新表/新字段设计是否走编排;整个 tracking 期 0 次使用 → §10 判简化。

## 6. 备选方案（Alternatives Considered）

- 方案 A：只加总管,复用全部现有(含 db-modeler/db-ops),不新建守门 → **未选**:DB 域有强 producer/applier 但无 design-quality reviewer,缺一个"应用前把质量关"的角色(对位 ux-prototype-aligner/encoding 守门)。
- 方案 B：新建多个(范式分析师/索引顾问/迁移审计…)→ 未选:过度;db-modeler 已覆盖建模,1 个综合守门足够,后续按 signals 缺口再拆。
- 方案 C：复用 flow-orchestrator → 未选:不懂 charset utf8mb4/§M.7 跨模块命名/sys_menu/迁移锁表,DB 域需专门总管。
- 方案 D：纯文档(只写 workflow)→ 未选:无 agent/skill 则 Claude 不自动触发,"自己去做"落空。

## 7. 实施计划（Implementation Plan）

```
[x] Step 1: 建 db-orchestrator agent(数据库设计工程师总管)
[x] Step 2: 建 db-schema-reviewer agent(L7 设计守门)
[x] Step 3: 建 plm-db-design skill(编排 5 步法)
[x] Step 4: rules.md 加 §M.10
[x] Step 5: 建 数据库设计工作流.md
[x] Step 6: signals 模板加 §10 + 当月实例加 §11
[x] Step 7: 建本 proposal 0025
[ ] Step 8: commit(feat 2 agent+skill / docs rule+workflow+template+proposal / backfill / signals 实例)→ 回填 §10 → 转 merged
[ ] Step 9: 进入 tracking 期,看首个新表设计是否走 db-orchestrator 编排
[ ] Step 10: 后续 — 模块工作流.md Phase 02 段加链接到本工作流(小改,下次顺带)
```

## 8. 衡量指标（How will we know it worked?）

- `charset_violation_count` 维持 **0**(gotcha #2 不复发);`schema_naming_drift_count` 被主动拦截记录。
- 库设计任务是否经 db-orchestrator 编排:tracking 期内新表/新字段设计 100% 走编排(基线=0 → 目标=首个用例即走)。
- `missing_sys_menu_count`:business-*.sql 缺 sys_menu 维持被 hook+守门双重拦截。

跟踪期：`2026-05-27` ~ `2026-06-24`（4 周）。

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| 用户（owner）| 通过（延续 0023/0024 "全套落地 + proposal 记录" 模式）| 2026-05-27 | §L.2 User-requested 授权 |

---

## 10. 实施后跟踪（merged 后填）

### 实际 PR / commit
- PR: —（分支 `chore/local-start-backend-script`,solo-review 直接合入,未走 PR）
- 合入 commit: **待回填**（commit 后补 hash,参照 0023/0024 回填模式）
- 实际 merged 日期：待 commit

### Tracking 数据

| 信号 | 基线 | 目标 | 实际（周 1）| 实际（周 N）|
|---|---|---|---|---|
| charset_violation_count | 0 | 维持 0 | | |
| schema_naming_drift_count | 0/未统计 | 被拦截即记 | | |
| 库设计任务经 db-orchestrator 编排 | 0 | 首个用例即走 | | |

### 最终判定
- [ ] done（数据库设计编排被持续使用 + signals 持续记录）
- [ ] reverted（整个 tracking 期 0 次使用 → 回滚或简化为纯 workflow 文档）

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude | 首次创建：7 artifacts 落地记录(同范式第 3 例,对位 0023 测试 / 0024 产品设计)|

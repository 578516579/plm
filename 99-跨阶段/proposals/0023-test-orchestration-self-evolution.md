# Proposal 0023: 测试编排自进化系统（test-orchestrator + skill + rule + workflow + signals）

> §L.2 记录：本提案对应**用户明确请求**的规范改动（改 `.claude/rules.md` + 新增 `99-跨阶段/测试工作流.md`），用户选择"全套现在落地 + proposal 作记录"，按 §L.2 例外条款 **User-requested** 同步落地并在此事后补录。

---

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0023 |
| 标题 | 测试编排自进化系统 |
| 状态 | **merged → tracking**（User-requested；commit `fb95e50` + `92f97de`）|
| 类型 | 流程 / 工具链 |
| 提出人 | 用户 + Claude |
| 提出日期 | 2026-05-27 |
| 评审人 | 用户（项目 owner，AskUserQuestion 选"全套现在落地 + proposal 作记录"）|
| 评审日期 | 2026-05-27 |
| Tracking 截止 | 2026-06-24（merged 后 4 周）|

---

## 1. 背景（What's the problem?）

项目已有丰富的测试**执行件**（`plm-e2e` skill、`test-engineer`/`e2e-validator`/`troubleshooter`/`api-contract-keeper`/`security-reviewer` 子 agent、§G.4 E2E 准入、Phase 04 Gate），但缺一个把它们**编排成一条线**的"总管"：测什么、分几层、谁先谁后、什么算过、失败怎么升级、结果如何回流自进化——目前散在各 agent/skill/规则里，靠 Claude 临场拼。用户希望有一个**能自己做、自己进化**的测试体系。

## 2. 证据（Evidence）

- **用户请求**：2026-05-27 会话原话——"增加一个开发过程中的测试的 agent，分管多个子 agent，增加测试的 skill 和 rule、workflow，让我在设计 PLM 项目开发的测试过程能够自己去做和去进化"。
- **§L.2 User-requested-bypass**：用户在 AskUserQuestion 中选择"全套现在落地 + proposal 作记录（推荐）"，明确授权先落地后记录。
- 关联现状：`plm-e2e` skill v1.0 只覆盖 E2E 执行，无金字塔编排层；signals 模板无测试专项指标（flake 与真退步未分开统计）。

## 3. 提案（What's the change?）

### 改动文件清单

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `.claude/agents/test-orchestrator.md` | 新增 | 测试编排总管 agent（计划+分派+裁决+沉淀）|
| `.claude/skills/plm-test-orchestrate/SKILL.md` | 新增 | 编排 5 步法 SOP |
| `.claude/rules.md` §G.5 | 新增 | 测试编排与自进化硬约束（金字塔/总管/裁决/自进化）|
| `99-跨阶段/测试工作流.md` | 新增 | 全流程 + 角色矩阵 + 升级路径 + 进化节律 |
| `99-跨阶段/signals/2099-12.template.md` §8 | 新增 | 测试编排 signals 模板 |
| `99-跨阶段/signals/2026-05.md` §8 | 新增 | 当月测试 signals 实例 |

### 核心设计

- **金字塔分层**：L1 单元(JUnit5+Mockito) / L2 组件(Vitest+MSW) / L3 契约(5 层命名) / L4 E2E(Playwright) + encoding 守门；铁律是不许金字塔倒挂。
- **总管不动手**：`test-orchestrator` 只出"计划 + DAG + 裁决标准"，子 agent 不能再 spawn，主 Claude 按 DAG 顺序调。
- **裁决并入 §G.4**：encoding 6/6 + 全套件 0 fail/0 did-not-run + 5 类覆盖 + 契约一致 + 真实证据。
- **自进化闭环**：每轮收口记测试 signals(flake vs 真退步分开) → reflect 发现模式 → proposal 改规则/工具。

## 4. 影响范围（Impact）

| 受众 | 影响 |
|---|---|
| 开发者 | 多一个"测试怎么搭"的统一入口；提测/准入更标准化 |
| Claude | rules §G.5 下次会话起生效；遇测试任务优先走 test-orchestrator |
| 测试 | flake 与真退步从此分开统计，可量化稳定性 |
| 已有资产 | 不改 plm-e2e/各子 agent（向后兼容），只在其上加编排层 |

## 5. 风险（Risks）

- 风险 1：编排层与 plm-e2e 职责重叠、用户混淆 → 缓解：skill/agent/workflow 三处都写明边界（编排+裁决 vs 执行）。
- 风险 2：总管变成"形式主义"，2-3 agent 小任务也摆 DAG → 缓解：反模式明列，小任务直接顺序调。
- 风险 3：测试 signals 没人填 = 自进化空转 → 缓解：§G.5.4 设为 MUST，收口即记；后续 `/reflect-monthly` 自动采集。

## 6. 备选方案（Alternatives Considered）

- 方案 A：只扩 plm-e2e skill 加金字塔段——不选：skill 是执行手册，塞编排+裁决会臃肿且无"总管 agent"语义。
- 方案 B：复用 flow-orchestrator——不选：那是通用 DAG，不懂测试金字塔/覆盖门槛/§G.4，测试域需要专门 agent。
- 方案 C：纯文档(只写 workflow)——不选：无 agent/skill 则 Claude 不会自动触发，"自己去做"落空。

## 7. 实施计划（Implementation Plan）

```
[x] Step 1: 建 test-orchestrator agent
[x] Step 2: 建 plm-test-orchestrate skill
[x] Step 3: rules.md 加 §G.5
[x] Step 4: 建 测试工作流.md
[x] Step 5: signals 模板 + 当月实例加 §8 测试编排
[x] Step 6: commit(`fb95e50` + `92f97de`)+ push → 回填 §10 → 转 merged
[ ] Step 7: 后续 — 模块工作流.md Phase 04 段加链接到本工作流（小改，下次顺带）
[ ] Step 8: 进入 tracking 期，看测试 signals 是否被持续记录
```

## 8. 衡量指标（How will we know it worked?）

- `e2e_flake_count` / `e2e_real_fail_count`：从"混在一起"改善为**每轮分开记录**（基线=未统计 → 目标=月度有数）。
- `coverage_gap`：缺口被主动识别并转提案的次数 > 0。
- 测试任务是否经 test-orchestrator 编排：tracking 期内新模块准入 100% 走编排。

跟踪期：`2026-05-27` ~ `2026-06-24`（4 周）。

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| 用户（owner）| 通过（"全套现在落地 + proposal 作记录"）| 2026-05-27 | §L.2 User-requested 授权 |

---

## 10. 实施后跟踪（merged 后填）

### 实际 PR / commit
- PR: —（分支 `chore/local-start-backend-script`,solo-review 直接合入,未走 PR）
- 合入 commit: `fb95e50`(test-orchestrator agent + plm-test-orchestrate skill) + `92f97de`(§G.5 rule + 测试工作流.md + 本 proposal + signals 测试段)
- 实际 merged 日期：2026-05-27

### Tracking 数据

| 信号 | 基线 | 目标 | 实际（周 1）| 实际（周 2）| 实际（周 N）|
|---|---|---|---|---|---|
| e2e_flake_count / e2e_real_fail_count | 未分开 | 每轮分开 | | | |
| coverage_gap 转提案 | 0 | >0 | | | |

### 最终判定
- [ ] done（测试编排被持续使用 + signals 持续记录）
- [ ] reverted（无人用 → 回滚或简化）

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude | 首次创建：5 artifacts 落地记录 |

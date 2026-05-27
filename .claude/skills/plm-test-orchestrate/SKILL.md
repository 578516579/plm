---
name: plm-test-orchestrate
description: PLM 测试编排 SOP — 当用户要「测一个模块/任务怎么搭测试」「提测/Phase 03→04 准入」「全回归」「测试覆盖够不够」时,按测试金字塔(单元→组件→契约→E2E)出计划、分派子 agent(test-engineer/api-contract-keeper/e2e-validator/troubleshooter/security-reviewer)、汇总裁决 Gate、把结果沉淀成 signals 自进化。本 skill 是 test-orchestrator agent 的执行手册;只跑 E2E 不编排时用 plm-e2e。
---

# plm-test-orchestrate — PLM 测试编排 SOP

把"一个开发任务该怎么测"固化成可重复的编排流程。**编排 + 裁决 + 沉淀**在本 skill;**执行细节**(怎么跑 playwright/排查失败)在 [`plm-e2e`](../plm-e2e/SKILL.md);**角色判断**在 [`test-orchestrator` agent](../../agents/test-orchestrator.md)。

> 一句话边界:`plm-test-orchestrate`(本 skill)= 测什么/分几层/谁来做/算不算过;`plm-e2e` = 怎么把 E2E 跑出来。

---

## 何时触发

| 语义 | 用户原话举例 | 走本 skill 还是 plm-e2e |
|---|---|---|
| 搭测试体系 | "这个模块测试怎么搭"、"帮我把 X 测一遍" | **本 skill**(分层编排) |
| 提测/准入 | "开发完了"、"提测"、"Phase 04 准入" | **本 skill**(编排+裁决)→ 内部调 plm-e2e 跑 |
| 覆盖审查 | "测试够不够"、"缺哪些用例" | **本 skill**(覆盖缺口) |
| 纯跑 E2E | "跑一遍 e2e"、"npm run test:e2e" | plm-e2e |
| 纯调试单 spec | "调一下 defect.spec" | e2e-debug |

---

## 编排 5 步法

### Step 1 — 判范围(测什么)
看改动落在哪,决定回归范围(用 scope-decider 协助分级):

```
改 typo / 文案 / 非业务       → 仅 smoke
改某模块业务字段/状态机/FK     → 该模块定向 + encoding + 全套件
改 domain/DTO/Mapper/interface → 先契约对齐(api-contract-keeper)再回归
改 yml encoding/JDBC/mybatis   → encoding 守门(P0)优先
新模块 / Phase 03→04 准入       → 全金字塔 + 全套件(强制)
```

### Step 2 — 出分层计划(分几层)
按测试金字塔列本次要补/要跑的层(见 [agent 金字塔表](../../agents/test-orchestrator.md)):
- **L1 单元**(JUnit5+Mockito):ServiceImpl 有分支/状态机/校验 → 必补
- **L2 组件**(Vitest+MSW):复杂前端组合式逻辑 → 按需
- **L3 契约**:动了 5 层命名(interface↔domain↔column↔DTO↔resultMap)→ 必查
- **L4 E2E**(Playwright):CRUD+状态机+FK+编码 HEX+UI 可达 → 准入必跑
- **守门** encoding 6 case:**一票否决**

> 原则:能用单测覆盖的逻辑分支别推到 E2E(金字塔别倒挂)。

### Step 3 — 分派子 agent(谁来做)
按矩阵下发,**主 Claude 按顺序调 Agent**(子 agent 不能再 spawn):

| 子任务 | 分派 |
|---|---|
| 写/补 单测·组件测·E2E spec | `test-engineer` |
| 5 层命名字段对齐 | `api-contract-keeper` |
| 跑全套 + flake 分类 | `e2e-validator` |
| 失败根因 | `troubleshooter` |
| 涉密/权限/注入 预审 | `security-reviewer` |
| stale JVM / 构建 | `build-deployer` |

复杂(≥5 agent)时让 `test-orchestrator` 出 Mermaid DAG,再 `task-tracker` 拆 TodoWrite。

### Step 4 — 裁决 Gate(算不算过)
逐条核对(§G.4),全满足才判**通过**:

- [ ] encoding 守门 6/6,DB 全字段 HEX 无 `EFBFBD`
- [ ] 全套件 `N passed`,**0 fail / 0 did-not-run**(flake 经 `--retries=1` 复测仍绿)
- [ ] 新模块覆盖 5 类(CRUD/状态机合法+非法/FK/编码/UI 可达)
- [ ] 契约改动经 api-contract-keeper 确认一致
- [ ] 证据为**本轮真实输出**,已落进 Phase 03 Gate 实例 §I

任一不满足 → **驳回**,指明回哪个 agent 修;**禁**"再跑一次试试"、**禁**贴历史证据。

### Step 5 — 沉淀 signals(自进化)
把本轮测试结果记进 [`signals/YYYY-MM.md §8 测试编排`](../../../99-跨阶段/signals/README.md):
- `e2e_flake_count` / `e2e_real_fail_count`(必须分开记)
- `coverage_gap`(缺哪层/哪模块)
- `rca_category`(env/schema/stale-jvm/code/contract/encoding)
- `gate_evidence_backfill_attempt`(贴历史证据企图,应为 0)

**触发提案**(主动建议):同类 flake 月 ≥3 → 稳定性提案;覆盖缺口反复 → 补模板提案;守门连续 N 轮 0 问题 → 守门降频实验。

---

## 编排速查卡

```
请求 → [Step1 范围] → [Step2 分层] → [Step3 分派 DAG]
                                          │
        test-engineer ─ 补 L1/L2/L4 ──────┤
        api-contract-keeper ─ L3 对齐 ─────┤
                                          ▼
        前置自检(4 端口+UTF8标志+tb_*表) → e2e-validator(encoding→全套件)
                                          │ 失败→troubleshooter→修→回跑
                                          ▼
        [Step4 裁决] 全绿→落 Gate §I  /  有 fail→驳回
                                          ▼
        [Step5 signals] flake vs 真退步 / 覆盖缺口 / RCA 分类
```

---

## 反模式(一票否决)

- ❌ 跳过 encoding 守门(P0)
- ❌ flake 与真退步混为一谈("反正能过")
- ❌ 用 E2E 替代单测(金字塔倒挂)
- ❌ 贴历史 `N passed` 糊弄 Gate(hook 拒 + 记 signals)
- ❌ 编排只下发不裁决(测了等于没测)

---

## 引用

| 文件 | 用途 |
|---|---|
| [`.claude/agents/test-orchestrator.md`](../../agents/test-orchestrator.md) | 本 skill 的角色/裁决判断 |
| [`.claude/skills/plm-e2e/SKILL.md`](../plm-e2e/SKILL.md) | E2E 执行/失败排查细节 |
| [`.claude/rules.md` §G.4 + §测试编排](../../rules.md) | 硬卡控 |
| [`99-跨阶段/测试工作流.md`](../../../99-跨阶段/测试工作流.md) | 全流程 + 角色矩阵 + 进化节律 |
| [`04-测试/测试用例库/E2E-测试矩阵.md`](../../../04-测试/测试用例库/E2E-测试矩阵.md) | 用例总览 |

## 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-27 | 首次创建:固化测试编排 SOP(proposal 0023) |

# Proposals — 流程改进提案

自进化机制的**输出端**。把 [reflect](../reflect/) 出来的"建议"转化为可执行的小 PR：改规范、改 hook、改 Gate Checklist 等。

> **核心理念**：规范本身是"代码"，演进路径必须像代码一样走"提案 → 评审 → 合入 → 跟踪效果"。

---

## 一份 proposal 的生命周期

```
draft       由 /reflect 自动产出 OR 人工提
   ↓
proposed    完成填写、关联数据，等 review
   ↓
   ├─→  accepted     评审通过，进入 implementing
   │       ↓
   │    implementing  Claude / 人 在写规范变更 PR
   │       ↓
   │    merged        PR 已 merge，规范升级生效
   │       ↓
   │    tracking      2-4 周观察期，看相关 signals
   │       ↓
   │       ├─→ done         指标好转，提案归档
   │       └─→ reverted     指标无改善 / 恶化 → 走回滚 + 写"失败提案"备忘
   │
   ├─→  rejected    评审不通过，留作记录（rejected 不删，保留学习价值）
   └─→  superseded  被更新的提案替代（指向新提案）
```

---

## 文件命名

`NNNN-<标题简写>.md`，编号递增。例如：

- `0001-pre-push-build-check.md`
- `0002-relax-L3-coverage-threshold.md`
- `0003-add-jdk17-jenv-check-to-hook.md`

号段建议（避免编号冲突）：
- `0001-0099`：流程 / Gate Checklist 类
- `0100-0199`：编码规范 / 代码风格类
- `0200-0299`：工具链 / hook 类
- `0300-0399`：架构 / 技术债类
- `0900-0999`：实验性提案（高失败容忍）

---

## 状态索引（手动维护）

> **本表是所有 proposal 的元数据快照**。新增/状态变更时同步更新。

| 编号 | 标题 | 状态 | 提出 | 关联触发 | merged commit | tracking 期 |
|---|---|---|---|---|---|---|
| [0001](0001-internal-tool-track.md) | 引入"项目类型"维度（外部产品/内部工具/框架升级） | **merged → tracking** | 2026-05-15 | [reflect/2026-W20-project-phase01-dogfood](../reflect/2026-W20-project-phase01-dogfood.md) F5 | apply 0001/0002/0003（2026-05-15）| 2026-05-15 → 05-29 |
| [0002](0002-team-size-adjusted-thresholds.md) | 按"团队规模"自动调整 Gate 阈值 | **merged → tracking** | 2026-05-15 | reflect F1/F2/F3/F4 | 同上 | 同上 |
| [0003](0003-require-triage-rationale.md) | Gate 实例头部"分级理由"必填 | **merged → tracking** | 2026-05-15 | reflect F10 | 同上 | 同上 |
| [0004](0004-staged-test-dod.md) | 拆 Phase 03 / 04 的 DoD（代码骨架 vs 测试稳定） | **merged → tracking** | 2026-05-15 | [reflect/2026-W20-project-phase03-dogfood](../reflect/2026-W20-project-phase03-dogfood.md) F-P03-01 | apply 0004/0005/0006（2026-05-15）| 2026-05-15 → 05-29 |
| [0005](0005-solo-sprint-merge.md) | solo 模式 Sprint 文档可并入 Gate 实例 | **merged → tracking** | 2026-05-15 | reflect F-P03-02 | 同上 | 同上 |
| [0006](0006-project-maturity-stage.md) | 引入"项目成熟度"维度（early/stable/mature），4 维参数化 | **merged → tracking** | 2026-05-15 | reflect F-P03-03 | 同上 | 同上 |
| [0007](0007-mcp-integration-modules-uplift.md) | 把 MCP/Integration 模块从 v0.5+ 提到当前迭代 | **merged → tracking** (User-requested-bypass) | 2026-05-17 | 用户明确请求 + AgriPLM-模块映射 drift | 0f75294 / eabbbe4 | 2026-05-17 → 06-30 |
| [0008](0008-parallel-session-collaboration.md) | 并行 Session 协作规范 + 把硬性条款沉淀进 .claude/rules.md § O | **merged → tracking** (solo-review) | 2026-05-17 | 用户明确请求 + `git worktree list` 6 工作树并行 | 0fc27a3 + 2d7308d + follow-up | 2026-05-17 → 06-30 |
| [0009](0009-session-handoff-agent.md) | 加 session-handoff Agent — 跨时间维度交接 + 防重复造轮子(补 0008) | **proposed** | 2026-05-20 | 用户明确请求 + 本会话亲历"差点重复造 0008"失败案例 | 待 merged | 待 merged → 2026-06-17 |
| [0013](0013-main-worktree-occupation-rule.md) | 非 main 主工作树多 session 共享占用规则(补 0008 §1 盲点) | **proposed** | 2026-05-24 | W21 反思 §2 模式 2 + 本会话亲历 4 次 HEAD 漂移 + worktree 7 天 +133% | 待 merged | 待 merged → 2026-06-21 |
| [0014](0014-zentao-bidirectional-sync.md) | 禅道(ZenTao)双向同步 — 修订 Proposal 0007 / 设计文档 §1.2 "先做单向" 取舍 | **merged → tracking** (User-requested-bypass) ⚠ **纸面merged待核** | 2026-05-25 | 用户明确请求 + 真实禅道环境可联调 | **待填 — 代码仍 untracked,见 [0021](0021-proposal-merged-requires-commit.md) + [reflect 2026-W22-zentao](../reflect/2026-W22-zentao-integration.md) 模式3** | 2026-05-25 → 06-30 |
| [0015](0015-plm-module-uplift-skill.md) | PLM 模块批量改造 SOP 固化为 skill(🟡→🟢)— 单日 6 模块同 SOP 显形 | **draft** | 2026-05-25 | [reflect/2026-W22-modules-bulk-uplift](../reflect/2026-W22-modules-bulk-uplift.md) 模式 1 / A1 行动 | 待 merged | 待 merged → 2026-06-22 |
| [0016](0016-business-sql-template-lint.md) | business-*.sql 模板 lint hook + sys_menu URL 改动前端硬编码扫描 | **implementing**(hook 已落 c2e8b99,待回填) | 2026-05-25 | [reflect/2026-W22-modules-bulk-uplift](../reflect/2026-W22-modules-bulk-uplift.md) 模式 2 + 3 / A3+A4 | c2e8b99(待补 §10)| 待 merged → 2026-06-22 |
| [0019](0019-integration-connector-skill.md) | 集成连接器三件套 SOP 固化为 `integration-connector` skill(与 0015 正交)| **draft** | 2026-05-27 | [reflect/2026-W22-zentao-integration](../reflect/2026-W22-zentao-integration.md) 模式 1 / B4 | 待 merged | 待 merged → 2026-06-24 |
| [0020](0020-bidirectional-sync-loop-guard-gotcha.md) | 双向同步回环防护沉淀 gotcha + 扩 §L.1 收"正确范式" | **draft** | 2026-05-27 | [reflect/2026-W22-zentao-integration](../reflect/2026-W22-zentao-integration.md) 模式 2 / B5 | 待 merged | 待 merged → 2026-06-24 |
| [0021](0021-proposal-merged-requires-commit.md) | proposal `merged` 必须绑定真实 commit hash(防"纸面 merged")| **draft** | 2026-05-27 | [reflect/2026-W22-zentao-integration](../reflect/2026-W22-zentao-integration.md) 模式 3 / B3 + 0014 实例 | 待 merged | 待 merged → 2026-06-24 |

---

## 触发来源（一份 proposal 不能凭空写）

每份 proposal 必须有"证据"。允许的触发来源：

| 来源 | 例子 | 证据格式 |
|---|---|---|
| signals 数据 | "上月 commit_bypass_count = 5" | 链 signals/YYYY-MM.md |
| reflect 报告建议 | "周报点出 Phase 03 平均超时 30%" | 链 reflect/YYYY-WW.md |
| 真实事故 | "线上 P0 故障，根因是规范盲区" | 链事故复盘文件 |
| gotcha 频次 | "同一坑 3 次会话踩到" | 链 gotchas.md 段落 |
| 用户明确请求 | "我们想要 XX" | 标"User-requested"，简单写背景即可 |

**禁止**："感觉规范有点啰嗦" / "我觉得 X 应该改 Y" — 没有数据/事故支撑的提案直接 rejected。

---

## 评审节奏

| Proposal 类型 | 评审人 | 评审耗时上限 |
|---|---|---|
| 流程 / Gate 类（0001-0099） | 项目经理 + 技术 lead | 1 周内 |
| 编码规范类（0100-0199） | 后端 lead + 前端 lead | 3 个工作日 |
| 工具链类（0200-0299） | DevOps + 提出方 | 2 个工作日 |
| 架构类（0300-0399） | 技术 lead + 必要 ADR | 2 周内 |

超出上限 → 自动升到下次"流程 Sprint"必须决议。

---

## 模板

复制 [0000-template.md](0000-template.md) 起步。

---

## 反模式

- ❌ proposal 写得很长但没有"数据支撑"段
- ❌ accepted 后没人写 PR、长期挂在 implementing
- ❌ merged 后没追 tracking → 不知道改了之后规范是否真的好用了
- ❌ 用 proposal 当吵架工具（"我早就说过 X 不行"）→ 评审会要拍板，不是辩论
- ❌ rejected 后偷偷删文件 → 必须保留 rejected 提案作为学习材料

---

## 跟其他文档的关系

| 文档 | 关系 |
|---|---|
| [signals/](../signals/) | proposal 的"输入证据" |
| [reflect/](../reflect/) | proposal 的"种子建议来源" |
| [03-开发/ADR/](../../03-开发/ADR/) | 架构类 proposal 的下游产物（accepted → 写 ADR） |
| [开发规范.md](../../03-开发/开发规范.md) / [模块工作流.md](../模块工作流.md) / [.claude/rules.md](../../.claude/rules.md) | proposal 的"修改对象" |

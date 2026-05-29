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
>
> ⚠ **`merged` = git 事实,不是决议**（[proposal 0021](0021-proposal-merged-requires-commit.md)）：状态标 `merged`/`merged → tracking` 的行,`merged commit` 列**必须**是真实 commit hash,不允许 `待填/待开`。决议要做但代码未提交 → 状态停 `implementing`。`User-requested-bypass` 绕过的是评审流程,**不**绕过"merged 须有 commit"这一事实约束。

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
| [0015](0015-plm-module-uplift-skill.md) | PLM 模块批量改造 SOP 固化为 skill(🟡→🟢)— 单日 6 模块同 SOP 显形 | **merged → tracking** (solo-review) | 2026-05-25 | [reflect/2026-W22-modules-bulk-uplift](../reflect/2026-W22-modules-bulk-uplift.md) 模式 1 / A1 行动 | 5e9a17f(skill 在 ~/.claude/skills/;pilot 待)| 2026-05-27 → 06-24 |
| [0016](0016-business-sql-template-lint.md) | business-*.sql 模板 lint hook + sys_menu URL 改动前端硬编码扫描 | **merged → tracking** (solo-review) | 2026-05-25 | [reflect/2026-W22-modules-bulk-uplift](../reflect/2026-W22-modules-bulk-uplift.md) 模式 2 + 3 / A3+A4 | c2e8b99 | 2026-05-27 → 06-24 |
| [0019](0019-integration-connector-skill.md) | 集成连接器三件套 SOP 固化为 `integration-connector` skill(与 0015 正交)| **merged → tracking** (solo-review) | 2026-05-27 | [reflect/2026-W22-zentao-integration](../reflect/2026-W22-zentao-integration.md) 模式 1 / B4 | 5ee6676(skill 在 ~/.claude/skills/)| 2026-05-27 → 06-24 |
| [0020](0020-bidirectional-sync-loop-guard-gotcha.md) | 双向同步回环防护沉淀 gotcha + 扩 §L.1 收"正确范式" | **implementing**(Q-INTEG-01 已落;§L.1 待授权)| 2026-05-27 | [reflect/2026-W22-zentao-integration](../reflect/2026-W22-zentao-integration.md) 模式 2 / B5 | 待(§L.1 SSoT)| 待 merged → 2026-06-24 |
| [0021](0021-proposal-merged-requires-commit.md) | proposal `merged` 必须绑定真实 commit hash(防"纸面 merged")| **merged → tracking** (solo-review) | 2026-05-27 | [reflect/2026-W22-zentao-integration](../reflect/2026-W22-zentao-integration.md) 模式 3 / B3 + 0014 实例 | ea5cd37 | 2026-05-27 → 06-24 |
| [0022](0022-dirty-tree-stop-nudge.md) | working tree dirty>15 → Stop hook nudge 分批 commit | **accepted**(settings.json 自修改待显式授权)| 2026-05-27 | [reflect/2026-W22-modules-bulk-uplift](../reflect/2026-W22-modules-bulk-uplift.md) 模式4 / A5 | 待(auto-mode 拦截)| 待 merged |
| [0023](0023-test-orchestration-self-evolution.md) | 测试编排自进化系统(test-orchestrator agent + plm-test-orchestrate skill + §G.5 rule + 测试工作流.md + signals 测试段) | **merged → tracking** (User-requested) | 2026-05-27 | 用户明确请求(本会话:"增加测试 agent+skill+rule+workflow,能自己做和进化")| fb95e50 + 92f97de | 2026-05-27 → 06-24 |
| [0024](0024-product-design-orchestration.md) | 产品设计编排自进化系统(product-orchestrator + prd-author + ux-prototype-aligner agent + plm-product-design skill + §M.9 rule + 产品设计工作流.md + signals 产品设计段) | **merged → tracking** (User-requested)| 2026-05-27 | 用户明确请求(本会话:"增加产品经理 agent+分管子agent+skill+rule+workflow,产品设计能自己做和进化",0023 孪生)| 86bd488 + 619ac06 | 2026-05-27 → 06-24 |
| [0025](0025-db-design-orchestration.md) | 数据库设计编排自进化系统(db-orchestrator + db-schema-reviewer agent + plm-db-design skill + §M.10 rule + 数据库设计工作流.md + signals DB 段;复用 db-modeler/db-ops 等 8 agent) | **merged → tracking** (User-requested)| 2026-05-27 | 用户明确请求(本会话:"增加数据库设计工程师 agent+分管子agent+skill+rule+workflow,数据库设计能自己做和进化",0023/0024 同范式第 3 例)| 53ab1d1 + b2634e9 | 2026-05-27 → 06-24 |
| [0026](0026-ued-design-orchestration.md) | UED 设计编排自进化系统(ued-orchestrator + ued-designer + accessibility-reviewer agent + plm-ued-design skill + §N.10 rule + UED设计工作流.md + signals UED 段;复用 ux-prototype-aligner) | **merged → tracking** (User-requested)| 2026-05-27 | 用户明确请求(本会话:"UED 设计流程总结成 agent+子agent+skill+rule+workflow",0023/0024/0025 同范式第 4 例)| 511aa17 + e1fa150 + 4374451 (+回填)| 2026-05-27 → 06-24 |
| [0027](0027-architecture-design-orchestration.md) | 系统架构设计编排自进化系统(arch-orchestrator + arch-reviewer agent + plm-arch-design skill + §Q rule + 架构设计工作流.md + signals 架构段;复用 system-architect 为核心建模者 + 7 现成 agent) | **merged → tracking** (User-requested)| 2026-05-27 | 用户明确请求(本会话:"增加架构师 agent+分管子agent+skill+rule+workflow,系统架构设计能自己做和进化",0023/0024/0025/0026 同范式第 5 例,Phase 02 最后一个设计维度)| dff0e77 + 08a7c19 (+回填)| 2026-05-27 → 06-24 |
| [0028](0028-product-mainline-uplift-epic.md) | 产品主线贯通迭代 epic — P0-1 跨模块外键 + P0-2 详情页跨模块跳转 + P0-3 TestReport/DORA 真聚合 + P0-4 AiButton 紫渐变 + P0-5 Dashboard 错态显形 + Step 7 PRD-MAPPING/ADR/Gate follow-up | **✅ epic 100% merged → tracking**(经 Wjl 2026-05-28 会话批签,solo-review,User-requested)| 2026-05-28 | 2026-05-28 PM 验收会话:5 主线 4 断 + AI 按钮 6/6 反模式 | 代码 9 commit(`3ae00fd` ⚠ / `21b7166` SPI+endpoint / `5c01814` P0-4+5 / `656a6a4` ⚠ / `5f93f77` P0-3 后端 / `9467bd1` P0-3 前端 等)+ Step 7 文档 4 commit(`eb58ffd` PRD-MAPPING §2 6 节 / `d510877` ADR-0010/0011/0012 / `28ea950` 4 Phase03 Gate / 配套 reviewer `15be2d4` + 批签 commit);贯通度 **15→60%**;3 ADR + 4 Gate + reviewer 7 维评分 | 2026-05-28 → 06-25 |
| [0029](0029-frontend-dict-ssot-drift-aggregate.md) | 前端 *Dict.ts 与 SQL `biz_*_*` 字典层漂移聚合评审 — 2026-W22 solo-review 27 模块抽取批次发现 15 处漂移,分 A 契约层(4)/ B 显示层(6+)/ C 结构性(5)三类,提出 P0~P3 处置优先级 + 6-10 子任务实施计划 | **draft** | 2026-05-28 | 自 [reflect/2026-W22-solo-review-dict-campaign.md](../reflect/2026-W22-solo-review-dict-campaign.md);所有漂移文档化锚点在 15 个 *Dict.spec.ts § ⚠ drift describe 段(可执行测试)| _(待开)_ | _(待 merged + 4 周)_ |
| [0030](0030-race-add-all-hard-block.md) | 并行 session race add-all 从 nudge 升级到硬拦 — session-guard.sh bulk add + dirty>=1 默认 exit 2 + `CLAUDE_BULK_OK="<reason>"` 显式后门 + `CLAUDE_BYPASS_SESSION_GUARD=1` 一次性绕过 | **✅ merged → tracking**(经 Wjl 2026-05-28 会话批签)| 2026-05-28 | 2026-05-28 epic 0028 单日 race 事故 2 次复发:`3ae00fd` + `656a6a4` 共偷 33 文件;proposal 0008 留 nudge 已证失效 | `9ed456e`(Step 1 hook 升级 + 5 自检全绿)+ `2af35df`(Step 2-5 文档同步:rules §L.5 + CLAUDE gotcha 9 + Q-COLLAB-01 + ledger)+ `15be2d4`(reviewer C-1/C-2 补正)+ 批签 commit;**Fast-track 例外条款范本**(详 `0ee203d` rules §L.2)| 2026-05-28 → 06-25 |
| [0032](0032-gate-instance-compact-template.md) | L1 末批模块紧凑型 Phase 03 Gate 模板入仓(60 行 vs 完整 200 行)— 完整模板末尾加附录 A 紧凑型 + README 适用矩阵 + plm-module-uplift skill 资产 | **draft**(2026-05-28 起草,等 Wjl review 转 proposed)| 2026-05-28 | 自 [reflect/2026-W22-mainline-uplift-and-race-guard.md §2 模式 4 + §3 候选 C2](../reflect/2026-W22-mainline-uplift-and-race-guard.md);5 次复用证据(`b3fb0f1` dora 首例 + `28ea950` today 4 模块)+ 末批模块完整模板填写率仅 30% | _(待 Wjl review)_ | _(待 merged + 4 周)_ |
| [0033](0033-reviewer-self-eval-7d-scorecard.md) | Reviewer 7 维度评分卡(同会话独立审视工具)— rules §L.2 加子条款 + skill 资产 + 适用任何 proposal/ADR/Gate 转 implementing 前 | **draft**(2026-05-28 起草,⚠ preemptive — 证据 1 次复用,差 2 次到阈值;1 周内复用 < 1 次自动 rejected)| 2026-05-28 | 自 [reflect/2026-W22-mainline-uplift-and-race-guard.md §2 模式 5 + §3 候选 C3](../reflect/2026-W22-mainline-uplift-and-race-guard.md);today 1 次实质成型(0028/0030 评分);**自评 7.4/10 Approve with comments** | _(待 Wjl review:推迟 vs 接受 preemptive)_ | _(待触发 / 待 merged + 4 周)_ |
| [0035](0035-posttooluse-incremental-test-hook.md) | PostToolUse Edit/Write 增量测试 hook(0 PostToolUse 盲点)— 改 `*Dict.ts` 跑该 spec / 改 `*ServiceImpl.java` 跑该 Test 类,nudge 模式永远 exit 0,文件路径白名单 | **draft**(2026-05-28 起草,⚠ preemptive — automation-recommender skill 派生,**不适用 fast-track**(证据是体验非事故);1 周内 Wjl 0 试用 → 自动 rejected)| 2026-05-28 | `claude-code-setup:claude-automation-recommender` skill 2026-05-28 输出 + today 测试自检 ≥ 6 次"全套等待"事件;**自评 7.1/10 Approve with comments** | _(待 Wjl review:推迟 vs 试一下)_ | _(待触发 / 待 merged + 4 周)_ |

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

# Reflect — Week 21, 2026 — session-handoff Agent 立项 + dogfood

## 头部

| 字段 | 值 |
|---|---|
| 周次 | 2026 第 21 周(2026-05-18 ~ 2026-05-24) |
| 执行者 | 手动 — Wjl + Claude(本会话 2026-05-20) |
| 关联 signals | [`../signals/2026-05.md`](../signals/2026-05.md) §8(累计跨 0008→0009) |
| 关联 proposals | [0009](../proposals/0009-session-handoff-agent.md)(本会话产出)+ [0010](../proposals/0010-frontend-dts-module-augmentation-rule.md)(db-ops 同期产出) |

---

## 1. 观察(Observations)

### Commit 与 PR

- 本周 commit 数(本 branch chore/biz-dict-cleanup-migration):**5**(docs: 2 / chore: 3),其中本会话产出 1(`aabf381`)
- 不符 Conventional 数:**0**(本会话 + db-ops 全部合规)
- PR 合并数:**0**(0009 / 0010 走 solo-review,无 PR 流转)
- 同期跨 session(db-ops session 2026-05-20):4 个 commit(`bc562aa`/`b2095e8`/`63350a1`/`e1783b5`)

### Gate Checklist

- 本周新增实例:**0**(本会话纯协议/Agent 改动,无业务模块阶段流转)
- 异常段触发:**0**

### 风险与事件

- [风险登记册](../风险登记册.md) 新增:无
- 上线 / 回滚 / hotfix:无
- **新增类型事件:跨 session 同工作树 branch 时序漂移** —— 本会话 commit `aabf381` 之后,db-ops session 在同一物理工作树继续 commit 4 次并切 branch 名,我跑 dogfood agent 时主工作树 HEAD 已切到 `chore/biz-dict-cleanup-migration`。我的 commit 不丢(在该 branch 链上),但**台账登记的 branch 名 `feat/ai-multi-provider-v1-v3` 与 HEAD 不一致** — 0008 §1 / §10 未覆盖此场景。

### Claude 行为

| 信号 | 计数 |
|---|---|
| 拒绝高危操作(本会话内 classifier 拦截) | **2 次** — `Remove-Item -Recurse -Force` bulk delete x2(均要求用户 AskUserQuestion 授权后通过) |
| 用户 override | 0 次(2 次 classifier 拦截后均走 AskUserQuestion 显式授权路径,无 bypass) |
| Claude 自发"防止出现问题"动作 | **1 次** — Edit `proposals/README.md` 时撞 0007 编号冲突 → 主动 Read 0008 → 发现 90% 重复 → 自查后撤回 5 文件 → AskUserQuestion 让用户裁决 |

### 跨 session 协作(本周首次显式观察)

| 数据 | 值 | 备注 |
|---|---|---|
| 月初 worktree 数(0008 立项时) | 6 | 0008 §0 基线 |
| 本周中(2026-05-20 早)| 13 | 我 commit aabf381 时 |
| 本周中(同日略晚)| 14 | dogfood subagent 跑时(+1 worktree 在同一会话内产生) |
| 当前(2026-05-24)| 14 | 含 1 个 locked / 1 个 stranded |
| 涨幅(7 天)| +8 / +133% | 远超 0008 §8 衡量目标"维持 5±2" |

---

## 2. 诊断(Diagnoses)

### 模式 1:Explore agent 漏报现有资产 → 重复造轮子风险

- **现象**:本会话 Claude 用 Explore agent 调研"多 session 协作"基础设施,Explore 摘要 (~800 字)报告"无 SESSION_LOG / 无 session-handoff Agent / 项目级 memory 不存在",**完全没提 0007/0008 两个已 merged proposal**。Claude 据此判断为"空白领域",差点新写 5 个文件(SESSION_LOG.md / active-sessions.md / HANDOFF_TEMPLATE.md / session-protocol/README.md / proposal 0007-multi-session-handoff-v1.md)。在 Edit `proposals/README.md` 撞编号冲突时才发现 0008 已存在并覆盖了 90% 设计。
- **根因(5 Whys)**:
  1. 为什么没发现 0008?Explore agent 列出最近 commit 但**摘要时漏报了 proposals/ 目录所有文件名**
  2. 为什么 Explore 会漏?它的指令是"探索现状",但**没有显式要求列出所有 proposals/0NNN-*.md**
  3. 为什么会信任 Explore 摘要而不二次 verify?Claude 默认 Explore 是权威概览,**未在改动 SSoT 关联文件前做 grep "用户关键词"的自检**
  4. 为什么没有自检机制?Agent 矩阵 V3 没有"启动时主动 grep proposals"的硬规则
  5. **根本根因**:Explore agent 设计为"快速摘要",但用户场景"新 proposal 起草"恰恰需要"穷尽现有 proposals 防撞号" — **场景错配**。
- **涉及规范文件**:[.claude/rules.md §L](../../.claude/rules.md)(自进化机制)+ [协作规范.md §5 编号防撞](../协作规范.md)
- **正向反馈**:本会话产出的 `session-handoff` Agent 设计**直接内化了这个教训** — 它启动第一步必 grep proposals/ 全目录,不依赖 Explore 摘要。

### 模式 2:主工作树 branch 占用未规范化 → 同会话 HEAD 漂移

- **现象**:db-ops session 与本会话用同一物理主工作树(D:/【12-trae】/06-.../plm)。本会话 commit aabf381 时 HEAD = `feat/ai-multi-provider-v1-v3`;dogfood agent 跑时 HEAD = `chore/biz-dict-cleanup-migration`。db-ops 没用 .claude/worktrees/ 隔离,直接在主工作树切 branch + commit。
- **根因**:0008 §1 写"main 工作树**只**用来 git pull 和回看历史,不允许直接编辑"。但**没明示"非 main 的主工作树"如何处理 / 是否允许多 session 共享 / 切 branch 时序冲突**。db-ops 把主工作树当作"feat branch 的工作目录",符合习惯但违反并发预期。
- **涉及规范文件**:[协作规范.md §1 隔离层](../协作规范.md) + [§16 入退场流程](../协作规范.md)
- **影响评估**:本次未实际损失(commit 不丢、台账行不撞)。但若 db-ops 切 branch 时本会话有 working tree changes(我 dogfood 期间确实有 untracked),可能产生 stash 漂移。

### 模式 3:dogfood Agent 自我评估机制有效

- **现象**:本会话用 `Agent(general-purpose)` 调一个 fresh subagent 模拟 session-handoff agent 跑了一遍。subagent 报告了**4 个具体设计反馈**(读源 #5 优先级 / HEAD branch 漂移告警 / chore(handoff) 缺失明示 / 防重复造轮子核心价值已验证),其中 3 个是我自己设计时未预见的。
- **根因**:Agent 自评通常受"作者偏见"影响,**让 fresh subagent 用真实工具跑一遍**能产生与作者认知互补的反馈。这与 [reflect/README.md](README.md) 反对的"心理按摩式自评"形成对比 — 真 dogfood 产出可执行 Action,假 dogfood 只产出感受。
- **泛化建议**:**任何新 Agent / 新规范定稿前,SHOULD 跑一遍 fresh subagent dogfood**(< 60s 即可,产出常含意外发现)。

---

## 3. 行动(Actions)

> 每条具体到文件 + 行,或转 proposal。

| # | 建议 | 涉及 | 转 Proposal? | 当前状态 |
|---|---|---|---|---|
| 1 | session-handoff Agent §"读源"列表第 5 项 (reflect/signals) 从"可选"升为 **MUST when 用户关键词命中 协作规范.md §2 SSoT 清单的任一文件** | `.claude/agents/session-handoff.md` 第 30-40 行 | 否(Agent 不在 §2 SSoT,可直接 Edit) | 本会话内执行 |
| 2 | session-handoff Agent §"工作恢复包结构" 🌿 段新增 "**台账漂移告警**" 字段(HEAD branch ≠ 在途任务.md "进行中"行的 branch 列时显式 🔴 红条) | 同上 第 60-75 行 | 否 | 本会话内执行 |
| 3 | session-handoff Agent 在"上次 commit handoff message"未找到 `chore(handoff):` 格式时,**显式输出 "本 branch 上次 commit 未按 0008 §18.3 handoff 格式,只能从 subject+body 推断,建议下次合规"** | 同上 §边界场景表 | 否 | 本会话内执行 |
| 4 | 协作规范.md §1 明示 "**非 main 的主工作树**" 占用规则(单 owner / 切 branch 必先 ping 在途任务.md owner / 多 session 必走 .claude/worktrees/ 隔离) | `99-跨阶段/协作规范.md` §1 末尾 | **是 → 0011-main-worktree-occupation-rule.md** | pending,本会话不动,留草稿到下次起草 |
| 5 | reflect 模板加一栏 "**跨 session 协作事件**"(本周首次需要的字段) | `99-跨阶段/reflect/YYYY-WW.template.md` 第 28 行后 | **是 → 评估**(模板属 SSoT? — 不在 §2 列表,理论可直接 Edit;但模板影响全员复用,SHOULD 走 proposal) | pending |
| 6 | 把"任何新 Agent / 新规范定稿前 SHOULD 跑 dogfood subagent"的最佳实践沉淀 | `.claude/agents/meta-cognitive.md` 或新 SOP | 否(纯实践沉淀)| pending |

---

## 4. 关注下周

- [ ] 0009 走 solo-review approve → merged → 进入 4 周 tracking(2026-05-20 ~ 2026-06-17)
- [ ] 0011 起草(主工作树占用规则)— 等待至少 1 个新案例再做,避免单点过早抽象
- [ ] dogfood subagent 反馈机制纳入 V3 → V4 Agent 矩阵讨论
- [ ] db-ops session 与本会话同主工作树事件 → 监测复发(若 1 周内 ≥ 2 次 → 立 0011 实锤)
- [ ] tracking 期统计:session-handoff Agent 实际调用次数 / "防重复造轮子"命中次数

---

## 5. 链路

- 上周反思:[`2026-W20-collaboration-spec-implementation.md`](2026-W20-collaboration-spec-implementation.md)(0008 立项反思)
- 触发的提案:
  - [`../proposals/0009-session-handoff-agent.md`](../proposals/0009-session-handoff-agent.md)(已落地)
  - 0011(待起草,主工作树占用规则)
- 同期他 session 提案:[`../proposals/0010-frontend-dts-module-augmentation-rule.md`](../proposals/0010-frontend-dts-module-augmentation-rule.md)(db-ops merged)
- 关联 Sprint 回顾:无(本周纯协议工作)
- 关联 signals:[`../signals/2026-05.md`](../signals/2026-05.md) §8 累计跨 0008→0009

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-24 | Claude(Wjl 会话, 本会话内 2026-05-20 起草, W21 周末沉淀)| 初版 — 沉淀 0009 立项 + dogfood 反馈 + 主工作树多 session 漂移真实案例 |

# Proposal 0009: 加 session-handoff Agent —— 跨时间维度交接(补 0008)

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0009 |
| 标题 | 加 `session-handoff` Agent — 把"接着上次"/"catch up"封装成可触发能力,补 [0008](0008-parallel-session-collaboration.md) 的跨时间维度盲区 |
| 状态 | **proposed**(待 solo-review) |
| 类型 | 工具链(纯 Agent,无机制新增) |
| 提出人 | Wjl + Claude(本会话) |
| 提出日期 | 2026-05-20 |
| 评审人 | Wjl(solo-review,参 [0005](0005-solo-sprint-merge.md)) |
| 评审日期 | 待定 |
| Tracking 截止 | merged 后 4 周(预计 2026-06-17) |

---

## 1. 背景

用户在 2026-05-20 会话明确表达"在 claude 中多 session 协同办公写代码,进化相关能力,防止出现问题",AskUserQuestion 选项 = (Session 交接 推荐, 并行多 session)。

[Proposal 0008](0008-parallel-session-collaboration.md)(2026-05-17 merged)已经覆盖了**并行同期协作**的全部主要场景:

- §1 隔离层 worktree
- §3 任务认领台账(在途任务.md)
- §4 同模块串行
- §5 编号防撞
- §6 PR 合并节奏
- §8 冲突处理
- §10 Claude session 行为
- §16 入退场流程
- §18 Claude-Claude 协作专项(含 §18.3 commit handoff 标准)

但 0008 的设计**焦点在并行**(几个 session 同时跑、互相不撞)。**跨时间维度**(用户今天 session 收尾 → 明天/下周新 session 想接上)只在 §7 "跨 session 转交" + §18.3 "commit handoff" 中**隐式存在**,且要求用户人工:

1. 回忆上次做到哪
2. `git log` 找上次 commit
3. 读 commit message 中的 handoff 段
4. 看 在途任务.md "已完成"段(只保留 7 天)
5. 综合判断"该接哪条"

这 5 步**重复劳动 + 容易遗漏 + 时间长后 7 天保留期已过期**。

---

## 2. 证据

- **用户原话(2026-05-20)**:"在 claude 中多 session 协同办公写代码,**进化相关能力**,**防止出现问题**" + AskUserQuestion 选 "Session 交接(推荐)"
- **0008 §16.3 + §18.3** 已规定 commit handoff 格式,但**没有 Agent 化触发** — 用户每次都要自己 `git log` + 读 message
- **本 session 自身证据**:Claude 在本 session 启动时**没有自动调用任何"上次做到哪"工具**,差点重复造轮子(本 proposal 在 0008 已存在情况下被错误地写到 0007 编号)。这正是"防止出现问题"的真实案例 — 如果有 session-handoff Agent,启动时它会扫 在途任务.md "已完成"段 + git log,立即指出"昨天 sleepy-hellman session 已落地 0008 协作规范,你别重复造"
- **0008 没解决的 1 件事**:Claude 视角"接着上次"的工作流封装

---

## 3. 提案

**单一改动** — 新增 1 个 Agent 文件,不引入任何新机制 / 新文件 / 新约束。

### 改动文件清单

| 文件 | 改动类型 | 备注 |
|---|---|---|
| `.claude/agents/session-handoff.md` | **新增**(已写) | 唯一新文件 |
| `99-跨阶段/proposals/README.md` §状态索引 | **追加 1 行**(0009 状态) | 索引维护 |
| `C:\Users\Wjl\...\memory\session_protocol.md` | **新增**(home memory) | 用户级 memory 指针 |
| `C:\Users\Wjl\...\memory\MEMORY.md` | **追加 1 行** | 同上 |

**不改任何 SSoT 文件**(协作规范.md / rules.md / 开发规范.md / 在途任务.md / 模块工作流.md / PRD-MAPPING.md 全部不动)。因此**不触发** [§L.2 SSoT 改动必先 proposal](../../.claude/rules.md) 的二次 proposal 要求。

### Agent 设计要点

**触发**:用户说"接着上次"/"catch up"/"上次的工作"/"我们做到哪了"/"我开了另一个终端"。

**读源**(全部是 0008 已落地的资产 + 现有文件,不引入新):
1. `99-跨阶段/在途任务.md`("进行中" + "已完成" 段)
2. `git log --oneline -20` + `git status` + `git worktree list`
3. `memory/project-quirks.md`(扫近期新增 Q-XX-NN)
4. 上一次 commit message 中的 §18.3 handoff 段(grep `chore(handoff)`)
5. `99-跨阶段/proposals/` 最近 5 个 proposal 状态(避免重做 — **本 session 自己踩的坑**)

**产出**:工作恢复包(Markdown 结构化),含上次焦点 / 未完事项 / git delta / 并行 session 警告 / 推荐 next step(2-3 选项)。

**不做**(明确边界):
- 不写代码(只产出"恢复包",由用户决定下一步)
- 不维护事实型 quirks(那是 `context-memory` Agent)
- 不接管单 session 内 todo(那是 `task-tracker`)
- 不写 commit message(那是 `git-workflow` + 用户)
- 不引入 SESSION_LOG.md 等新机制(0008 在途任务.md 已够)

### 与现有 24 个 Agent 的边界

| Agent | 维度 | session-handoff 对它的关系 |
|---|---|---|
| `context-memory` | 事实型项目知识(quirks) | 我**读**它的输出,不写不替代 |
| `progress-narrator` | 单 session 内汇总 | narrator 在本 session 末尾出表;我在**下一**个 session 开头出恢复包 |
| `task-tracker` | 本 session TodoWrite | 不冲突 — 我跨 session,它本 session |
| `requirement-clarifier` | 拆解模糊指令 | 用户说"接着上次"是模糊 → clarifier 触发本 Agent |

**正交三轴明确**:context-memory(事实)× progress-narrator(本次)× session-handoff(跨次)。

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者(Wjl) | 0 改习惯。可选触发("接着上次") |
| Claude | 多 1 个 Agent 可用,无强制 |
| 现有 Agent 矩阵 | 不变(24 → 25 个 Agent) |
| 0008 已落地规范 | 0 改动。本 Agent 是 0008 的"应用层封装"而非替代 |
| Self-evolution | 多 1 个 tracking 指标(session-handoff 调用次数) |

---

## 5. 风险

| 风险 | 缓解 |
|---|---|
| **再次重复造轮子** — 类似本 session 写 SESSION_LOG/active-sessions 等被 0008 覆盖 | 本 Agent 启动**第一步**就扫 `99-跨阶段/proposals/` 最近 5 个 + `协作规范.md` 目录,主动告警"该资产可能已存在" |
| **Agent 调用率太低** — 用户根本不说"接着上次" | tracking 期统计若 0 调用 → revert,无需复杂回滚(只删 1 个 .md 文件) |
| **与 0008 §10 / §18 重叠** | 文件级无重叠;行为上**互补**:0008 写规则,本 Agent 提供触发式工具 |
| **跨时间 7 天后在途任务.md 已完成段被清** | Agent 退化到 `git log` + commit handoff message scrape(0008 §18.3 commit 格式标准化,可靠) |

---

## 6. 备选方案

- **方案 A**(本提案):只加 1 个 Agent,**无新机制**,读 0008 已有资产
- **方案 B**(本 session 早期错误尝试):加 SESSION_LOG.md + active-sessions.md + HANDOFF_TEMPLATE.md 等 4-5 个新文件;**已撤回** — 与 0008 在途任务.md / §18.3 重叠率 90%
- **方案 C**:直接给 0008 协作规范.md 加 §19 章节 "跨时间维度 session 交接 SOP";**不选** — SOP 与 Agent 是两层,SOP 是人类可读规则,Agent 是 Claude 可执行触发器,层级不混
- **方案 D**:不做,完全靠用户自己 `git log` + 读 0008 §18.3;**不选** — 用户已明确要求"防止出现问题",而本 session 的"差点重复造 0008" = 真实失败案例,需要一个自动扫描资产的 Agent

选 A。**风险最低,可演进,与 0008 100% 兼容。**

---

## 7. 实施计划

```
[x] Step 1: 写本 proposal,状态 = proposed(2026-05-20)
[x] Step 2: 撤回错误的 SESSION_LOG.md / active-sessions.md / HANDOFF_TEMPLATE.md / session-protocol README.md(本 session 早期失误)
[x] Step 3: 改名 0007-multi-session-handoff-v1.md → 0009-session-handoff-agent.md(0007 已被 mcp-integration 占用)
[ ] Step 4: 重写 .claude/agents/session-handoff.md(改读源 = 在途任务.md + git log + project-quirks + 最近 proposals,不再读 SESSION_LOG)
[ ] Step 5: 写 home memory session_protocol.md(指针,引用 0008+0009)
[ ] Step 6: 更新 99-跨阶段/proposals/README.md §状态索引 加 0009 行
[ ] Step 7: solo-review approve → 状态 merged
[ ] Step 8: commit
[ ] Step 9: tracking 4 周(到 2026-06-17),看指标
```

---

## 8. 衡量指标(Tracking 期 4 周)

| 信号 | 基线 | 目标 | 测量 |
|---|---|---|---|
| session-handoff Agent 月调用次数 | 0 | ≥ 2 次/月(实际跨 session 启动且被使用) | Claude 会话日志手工记 |
| 重复造轮子事件 | 1 次/月(本 session 这个) | 0 | 月 reflect |
| catch-up 后用户 5 分钟内进入工作状态 | N/A 基线 | ≥ 80% | 主观自评 |
| 与 0008 兼容性 | N/A | 0 冲突 | git log + reflect |

跟踪期:`2026-05-20` ~ `2026-06-17`。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | 待定 | 待定 | solo-review。本 proposal 已在 §2 证据段诚实记录"本 session 差点重复造 0008"作为失败案例,符合 [.claude/rules.md §L.4 数据完整性](../../.claude/rules.md) |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit
- PR: 待定
- 合入 commit: 待定
- 实际 merged 日期:待定

### Tracking 数据

| 信号 | 基线 | 目标 | W21 | W22 | W23 | W24 |
|---|---|---|---|---|---|---|
| Agent 调用/月 | 0 | ≥ 2 | | | | |
| 重复造轮子事件 | 1 | 0 | | | | |
| 5 min catch-up 成功率 | N/A | ≥ 80% | | | | |
| 与 0008 冲突 | N/A | 0 | | | | |

### 最终判定
- [ ] done
- [ ] reverted(只需删 `.claude/agents/session-handoff.md` 即可,零依赖)

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-20 | Wjl + Claude(本会话) | 初版 — 大幅瘦身自最初的 V1 草稿(原稿有 SESSION_LOG/active-sessions 等 5 文件,与 0008 重叠后撤回);最终方案 = **只加 1 个 Agent**,读源全是 0008 已有资产 |

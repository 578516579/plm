# Proposal 0013: 非 main 主工作树多 session 共享占用规则

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0013(原计划 0011,因 `claude/interesting-goldwasser-449e96` branch 已占 0011/0012/0010 等多个编号,本提案按 [协作规范.md §5 编号防撞](../协作规范.md) "后到者让位"原则改 0013)|
| 标题 | 非 main 主工作树多 session 共享占用规则 — 补 [0008](0008-parallel-session-collaboration.md) §1 隔离层盲点 |
| 状态 | **proposed**(待 solo-review) |
| 类型 | 流程(SSoT 改动 — 协作规范.md / 可能加 .claude/rules.md §O.8) |
| 提出人 | Wjl + Claude(本会话 2026-05-20 ~ 05-24) |
| 提出日期 | 2026-05-24 |
| 评审人 | Wjl(solo-review,参 [0005](0005-solo-sprint-merge.md)) |
| 评审日期 | 待定 |
| Tracking 截止 | merged 后 4 周(预计 2026-06-21) |

---

## 1. 背景

[Proposal 0008](0008-parallel-session-collaboration.md) §1 规定:

> 不允许在 `main` 工作树上**直接编辑**业务/规范文件。`main` 工作树只用来 `git pull` 看 SSoT 最新版与回看历史。
> 每个并行任务 → 一个独立 worktree + 独立分支。Claude session 默认落 `.claude/worktrees/<adjective-name>/`。

但 0008 **未明示**:**非 main 的主工作树**(本仓库的 `D:/【12-trae】/06-项目全生命周期管理/plm/` 这个根目录,即 `git worktree list` 中第一个非 `.claude/worktrees/*` 的工作树)如何处理。实际行为:

- 它的 HEAD 跟随用户最后切的 branch(`feat/X` / `chore/Y` / `claude/Z` 都可能)
- 多个 session(本 Claude session + db-ops session + ...)都直接在它上面编辑、commit、切 branch
- branch 名漂移,在途任务.md 登记的 "分支" 列与实际 HEAD 不一致
- 没人能确定"主工作树现在归谁用"

W21 反思 §2 模式 2 + 本会话连续 3 次实测漂移。

---

## 2. 证据(本会话亲历)

本会话(2026-05-20 ~ 2026-05-24,跨 5 天但实际工作量集中在 05-20)从开始到收尾,主工作树 HEAD branch **被切换至少 4 次**:

| 时间点 | 我的动作 | HEAD branch | 我以为的 branch |
|---|---|---|---|
| 2026-05-20 commit `aabf381` 时 | `docs(session-handoff): 0009 ...` | (实际未知,commit 输出说 feat/ai-multi-provider-v1-v3) | feat/ai-multi-provider-v1-v3 |
| 2026-05-20 dogfood subagent 跑期间 | 调 general-purpose Agent | `chore/biz-dict-cleanup-migration`(db-ops 切了 + 提交 4 个 commit) | 仍是 feat |
| 2026-05-24 commit `63b13a3` 时 | `docs(reflect/session-handoff): W21 ...` | `chore/local-start-backend-script`(又被切) | 已不确定 |
| 2026-05-24 写本 proposal 0013 时 | `git branch --show-current` | `chore/local-start-backend-script`(仍此) | 已观测到现象,写本 proposal |

并发 worktree 数据(0008 §0 基线 vs 本周观察):

| 时点 | worktree 总数 | 涨幅(自 0008 基线)|
|---|---|---|
| 0008 立项时(2026-05-17)| 6 | — |
| 本会话 commit aabf381 时 | 13 | +117% |
| 本会话 dogfood subagent 跑时(同日)| 14 | +133% |
| 本 proposal 起草时(2026-05-24)| 14 | +133% |

我的 commit 不丢(都在新 branch 链上),但**关联性混乱**:`git branch --contains aabf381` 显示在 `chore/biz-dict-cleanup-migration`,而我 commit 时的输出说在 `feat/ai-multi-provider-v1-v3`。`git log --graph --all` 显示这是一条非线性 commit 树,N 个 session 各自分叉 N 个 branch,有些已 merge main 有些没 merge。

**进一步证据** — 本提案起草时 grep 扫:`claude/interesting-goldwasser-449e96` branch 上有 `9e1afe6 docs(reflect): W20 closing reflection + 7 proposals` commit,**新增了 0010 / 0011 / 0012 三个 proposal 文件**,但都未合 main。我的本会话在主工作树看不到这 3 个文件,差点撞号 0011 — 防重复造轮子机制(本会话产物 session-handoff Agent + grep proposals/ 全目录设计)再次救场,迫使我让位到 0013。

---

## 3. 提案

### 3.1 协作规范.md §1 末尾新增段(SSoT 改动,实施期合)

```
### 1.X 非 main 主工作树占用规则(MUST)

非 main 的主工作树(例 D:/【12-trae】/.../plm/)在某一时刻只允许**一个 session 占用**:

- **占用前**:在 [在途任务.md](在途任务.md) "进行中" 段加一行,worktree 列写 "(主工作树 — 当前 owner)" 而非具体路径
- **同主工作树切 branch 前 MUST**:
  (a) 看在途任务.md 该主工作树行的 owner ≠ 我 → 不能切,先 ping owner
  (b) owner = 我或空 → 在台账加新行(可能多个 branch 在同主工作树轮流,新行写 "(主工作树)" + branch 名)
  (c) 把旧 branch 那行 state 改 "暂停"
- **禁止**:多个 session 同时在主工作树编辑 / commit(必走 [.claude/worktrees/](../.claude/worktrees/) 隔离)
- **检测漂移**:任一 session 启动报到时(0008 §10.1)如果 `git branch --show-current` ≠ 自己在台账登记的 branch → 触发 §3.3 处置流程
```

### 3.2 .claude/rules.md 新增 §O.8(SSoT 改动,实施期合)

```
- O.8 主工作树漂移检测:启动报到时如发现 HEAD branch 与本 session 在台账登记的 branch 不一致 → 立即 AskUserQuestion 让用户决定:
  (a) 切回登记的 branch
  (b) 在新 branch 上登记新行(主工作树仍 owner = 我)
  (c) 切到 .claude/worktrees/<new-name>/(隔离)
  详见 [协作规范.md §1.X](../99-跨阶段/协作规范.md)
```

### 3.3 漂移处置流程图

```
session 启动
   ↓
git branch --show-current ── 与台账登记一致? ── YES → 正常工作
                              │
                              NO
                              ↓
                    AskUserQuestion 处置选项:
                    a) 切回登记 branch:git checkout <reg-branch> + git status 确认
                    b) 登记新 branch:在途任务.md 加一行,owner = 自己
                    c) 隔离到 worktree:git worktree add .claude/worktrees/<name> <branch>
                              ↓
                    用户选择 → 执行 → 在反思中记录 + 月度 signals 计数 +1
```

### 3.4 改动文件清单

| 文件 | 改动 | 实施时机 |
|---|---|---|
| `99-跨阶段/协作规范.md` §1 末尾 | **新增 §1.X**(~30 行) | accepted 后 |
| `.claude/rules.md` §O 末尾 | **新增 §O.8**(~10 行) | accepted 后 |
| `99-跨阶段/proposals/README.md` 索引 | 追加 0013 行 | **本 commit**(状态 proposed) |
| `99-跨阶段/在途任务.md` "进行中" 段 | (示例补充)主工作树行写 "(主工作树 — 当前 owner)" 格式 | accepted 后,实施期作示范 |

> **§L.2 合规**:SSoT 改动(协作规范.md / rules.md)等 accepted 后才合。本 PR 只写 proposal 本身 + README 索引,不动 SSoT,符合 §L.2。

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者(Wjl) | 改习惯:不在主工作树多 session;启动报到要看 branch 是否一致 |
| Claude(所有 session)| 启动报到多 1 步(§O.8 漂移检测)。**session-handoff Agent 已含台账漂移告警**(W21 commit 63b13a3 已落地)— 0013 是规范层背书 |
| 现有 14 个 worktree | 大部分在 `.claude/worktrees/` 下符合规则;主工作树需要由本会话清退(等 accepted) |
| db-ops session(同时期) | 需要协调 — 0013 实施前可能继续漂,实施后必须迁出主工作树 |
| 已有 commit / branch | 0 影响,已存在的分叉不强制 rebase |

---

## 5. 风险

| 风险 | 缓解 |
|---|---|
| 习惯改不过来,主工作树继续被多 session 共享 | tracking 期统计"主工作树 branch 切换次数",若 ≥ 2/周 → 起 0014 加 SessionStart hook 自动检测 |
| db-ops session 短期不知道本提案 → 继续漂 | 本 commit message 含 §18.3 handoff 段,其他 session catch up 时能读到;若必要主动联系 |
| §1.X 规则太严,工作流变慢 | 已留 "切回 / 登记 / 隔离" 三选项;不是"必须切回";灵活度高 |
| 0013 与未合的 claude/interesting-goldwasser-449e96 branch 上 0011/0012 内容冲突 | 已扫,无主题冲突(它们是 phase06-okr / phase06-signoff,本提案是主工作树占用),正交 |
| 编号再次撞 | 本提案已用 session-handoff Agent 设计的 `git log --all --diff-filter=A` 扫所有 branch,确认 0013 空闲 |

---

## 6. 备选方案

- **方案 A**(本提案):MUST 单 session 占主工作树 + 切 branch 强制流程 + Claude AskUserQuestion 检测
- **方案 B**:把主工作树视为 `.claude/worktrees/main-mirror/` 用 worktree 接管(主工作树永远是 main + 只读) — 不选:改动太大,需要 git worktree 操作,且不同 OS 行为不一致
- **方案 C**:不做,继续观察 — 不选:本会话已观察 4 次漂移,W21 反思已立 action,延迟立项 = 数据完整性违规(§L.4)
- **方案 D**:在主工作树跑 file lock 进程,任一 session 写 .claude/main-worktree-lock 文件 — 不选:跨平台复杂,与 0008 "不引入锁"主轴矛盾

选 A,与 0008 风格一致(规则约束 + Agent 检测,无自动化锁)。

---

## 7. 实施计划

```
[x] Step 1: 写本 proposal(2026-05-24,本会话内)
[x] Step 2: 编号扫所有 branch,确认 0013 空闲(防本会话第三次撞号)
[ ] Step 3: solo-review approve → 状态 → accepted
[ ] Step 4: 改 99-跨阶段/协作规范.md §1 加 §1.X
[ ] Step 5: 改 .claude/rules.md §O 加 §O.8
[ ] Step 6: 本会话在 在途任务.md 把"主工作树"行格式补全(示范)
[ ] Step 7: 状态 → merged,进入 tracking 4 周
[ ] Step 8: tracking 期统计 §8 指标
```

---

## 8. 衡量指标(Tracking 期 4 周)

| 信号 | 基线(W21)| 目标(2026-06-21)| 测量方式 |
|---|---|---|---|
| 月度主工作树 branch 切换次数 | 4 次/周(本会话观察)| ≤ 1 次/周 | 手工 + git log --first-parent 计数 |
| 同主工作树多 session 共享事件 | ≥ 1 次/周(本会话明确观察)| 0 | reflect 月报手工记 |
| 台账 owner 列与实际 HEAD 一致率 | 未知(估 < 50%)| ≥ 95% | 抽样 |
| 因主工作树漂移触发的"重新读 git status"次数 | 高(本会话 3+ 次)| 0 | session-handoff Agent 调用日志 |
| 编号撞号事件 | 0(W21 防住 2 次:0007 / 0011)| 0 | 起新 proposal 时 grep 全 branch 数 |

跟踪期:`2026-05-24` ~ `2026-06-21`。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | 待定 | 待定 | solo-review。本提案完全基于 W21 反思 + 本会话 4 次实测,无投机成分 |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit
- PR: 待定
- 合入 commit: 待定
- 实际 merged 日期:待定

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| 主工作树切换/周 | 4 | ≤ 1 | | | | |
| 多 session 共享事件 | ≥ 1 | 0 | | | | |
| 台账一致率 | < 50% | ≥ 95% | | | | |
| 编号撞号事件 | 0 | 0 | | | | |

### 最终判定
- [ ] done
- [ ] reverted(若指标恶化 → 走 0014 升级 SessionStart hook)

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-24 | Claude(Wjl 会话)| 初版 — 编号 0011 → 0013 让位(协作规范 §5);触发依据 [W21 reflect](../reflect/2026-W21-session-handoff-agent-dogfood.md) §2 模式 2 + §3 行动 4 (原计划"等 ≥ 2 案例","本周仅观察",本会话已自证 4 案例,超过 threshold);本提案的"扫所有 branch 防撞号"机制由 [session-handoff Agent](../../.claude/agents/session-handoff.md) 设计驱动,本会话 dogfood 二次验证有效 |

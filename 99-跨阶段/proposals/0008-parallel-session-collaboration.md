# Proposal 0008: 并行 Session 协作规范 + 把硬性条款沉淀进 .claude/rules.md § N

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0008 |
| 标题 | 并行 Session 协作规范 + 把硬性条款沉淀进 `.claude/rules.md` § N |
| 状态 | **merged → tracking**（Phase 1 + Phase 2 全部落地；用户 settings.local.json 加 Edit 白名单后 § N 与 §6 补完） |
| 类型 | 流程 |
| 提出人 | Wjl + Claude (sleepy-hellman-5a3950) |
| 提出日期 | 2026-05-17 |
| 评审人 | Wjl（solo-review） |
| 评审日期 | 2026-05-17 |
| Tracking 截止 | 2026-06-30（merged 后 6 周） |

---

## 1. 背景（What's the problem?）

仓库已实际进入"多 session 并行"运行模式：2026-05-17 时点 `git worktree list` 显示 6 个工作树（1 个 main + 5 个 `claude/*` 并行分支）。但仓库的现有规范文档全部基于"单线串行"假设：

- `.claude/rules.md` § B/F/G/L 只约束单 session 行为
- `99-跨阶段/模块工作流.md` 描述单模块 6 阶段流转，没说"两个 session 同时进入 Phase 03"怎么办
- `03-开发/开发规范.md` § 4 分支模型只到"feature/* + hotfix/*"，没说 worktree 隔离 / 串行 merge
- 缺一个"谁在干啥"的台账，多 session 互相不可见

用户在 2026-05-17 会话中明确提出："几个 session 同时执行任务，增加团队执行协作规范，保障协作顺畅不冲突。"

---

## 2. 证据（Evidence）

- **客观数据**：`git worktree list` 6 个工作树，`git branch -r | grep claude/` 至少 5 个并行 `claude/*` 分支
- **风险场景已可预见**（虽未发生严重事故，但概率高）：
  - SSoT 文件 `PRD-MAPPING.md` / `.claude/rules.md` 高频被多个 session 改动（如 0007 提案合入时同期还有别的 PRD-align 任务）
  - 16 个空壳模块要分轮 PRD-align，每轮多 session 并行处理是必然
  - Gate 实例文件名格式 `PhaseNN-阶段-Gate-YYYY-MM-DD.md` 在同一天对同模块会撞车
- **用户请求**：2026-05-17 会话原话 "几个 session 同时执行任务，增加团队执行协作规范，保障协作顺畅不冲突"
- **规则索引盲区**：现有 [.claude/rules.md §B 不可触碰区](../../.claude/rules.md) 不包含"其他 session 的 worktree / 分支"，跨 session 边界未明文化

---

## 3. 提案（What's the change?）

**两阶段实施**：

### Phase 1（**已落地** in 本 commit，独立文档新增不破坏 SSoT）

| 文件 | 改动 |
|---|---|
| [99-跨阶段/协作规范.md](../协作规范.md) | **新增** — 12 个章节的并行协作主规范 |
| [99-跨阶段/在途任务.md](../在途任务.md) | **新增** — 多 session 任务白板模板 |
| 本 proposal 文件 | **新增** — 本提案自身 |

**说明**：以上 3 个文件都是新增，不修改任何 SSoT 文件（PRD-MAPPING.md / rules.md / 模块工作流.md / 开发规范.md），因此不触发 `.claude/rules.md §L.2` 的 proposal-before-edit 限制；可以直接随本 commit 落地。

### Phase 2（**待评审通过后实施**，触及 SSoT，必须走 proposal 流程）

| 文件 | 改动 |
|---|---|
| [.claude/rules.md](../../.claude/rules.md) | **新增 § N 章节** — "并行 session 协作（MUST）"，10 行硬条款，链回协作规范.md |
| [99-跨阶段/模块工作流.md](../模块工作流.md) | **新增章节** — "多 session 协作"段，提示 §4 同模块串行 + §6 PR 合入顺序 |
| [03-开发/开发规范.md](../../03-开发/开发规范.md) | **§4.2 分支模型** 增补 worktree 段落，链协作规范.md |
| 根 [CLAUDE.md](../../CLAUDE.md) | "Rules & playbooks" 表新增一行链到协作规范.md |
| [99-跨阶段/signals/2026-05.md](../signals/) 起 | 月度信号表新增 6 个并行协作指标（见 §8） |

### Diff 草案（Phase 2，待评审）

```diff
--- a/.claude/rules.md
+++ b/.claude/rules.md
@@ -286,2 +286,18 @@
 | 文件 | 受众 | 强制层 |
 |---|---|---|
+
+---
+
+## N. 并行 Session 协作（MUST）
+
+多 session（多 Claude / 多人 / 人机混合）同时在本仓库工作时，遵守 [99-跨阶段/协作规范.md](../99-跨阶段/协作规范.md) 全文。Claude 视角额外硬条款：
+
+- **N.1** 启动即报到：第一轮响应前必跑 `git worktree list && git status && git log --oneline -3`
+- **N.2** 写规范前查 §L.2：要改任何 SSoT 文件（[协作规范.md §2](../99-跨阶段/协作规范.md) 列表）→ 先开 proposal，不直接动
+- **N.3** 任务上台账：开工前先在 [99-跨阶段/在途任务.md](../99-跨阶段/在途任务.md) "进行中"加一行
+- **N.4** 同模块串行：[协作规范.md §4](../99-跨阶段/协作规范.md) — 任一业务模块同一时刻只允许一个 session 改代码 + SQL
+- **N.5** 见冲突即停：[协作规范.md §8](../99-跨阶段/协作规范.md) "停下 AskUserQuestion"场景，禁止自作主张取舍；不许 `git push --force` 覆盖他人工作
+- **N.6** 跨 session 边界：[协作规范.md §9](../99-跨阶段/协作规范.md) — 其他 session 的 worktree / 分支视作"不可触碰区 §B 扩展"
+
+本章节是协作规范的强制摘要，详尽规则与示例在协作规范.md。

--- a/CLAUDE.md
+++ b/CLAUDE.md
@@ -??? (in "Rules & playbooks" 表)
 | Module lifecycle overview | [99-跨阶段/模块工作流.md](99-跨阶段/模块工作流.md) | All roles |
+| **并行 session 协作** | [99-跨阶段/协作规范.md](99-跨阶段/协作规范.md) | 多 session 场景必读 |
 | **🚦 Hard Gate (mandatory)** | ... | ... |
```

---

## 4. 影响范围（Impact）

| 受众 | 影响 |
|---|---|
| 开发者 | 多一份必读文档；分支命名规则保持不变；新增"启动前查台账"动作（< 30s/次） |
| Claude | 启动行为多 3 个 bash 命令；新增"在台账登记"义务；遇 SSoT/冲突必停而非自作主张 |
| 测试 / 运维 | 无直接影响（除非他们也并行作业） |
| 已有代码 / 文档 | 仅向后增补，无破坏；现有 5 个 worktree 立即按新规范运行 |

---

## 5. 风险（Risks）

| 风险 | 缓解 |
|---|---|
| 台账文件本身成为新的 SSoT 冲突点（5 个 session 同时改） | 台账行天然按"分支"列隔离；不同 session 改不同行；若真撞行可走 git rebase |
| 规则过严导致小任务也要走长流程，降低速度 | §4 例外：L3 hotfix 可打断长任务；§2 单 commit 原则降低合并阻力 |
| Claude "启动报到"动作被忘记 | Phase 2 在 `.claude/settings.json` UserPromptSubmit hook 加提示（后续 follow-up proposal） |
| 在途任务.md 长期不维护变成空表 | reflect 月报对照"实际 worktree 数 vs 台账行数"差距，差 > 1 提醒 |
| Phase 2 改 SSoT 文件本身就需要走 proposal，形成"嵌套" | 本 proposal 即承担 Phase 2 的 proposal 角色，评审通过即可实施 |

---

## 6. 备选方案（Alternatives Considered）

- **方案 A（本提案）**：独立 `99-跨阶段/协作规范.md` + 台账 + Phase 2 硬化进 rules.md。**优点**：人 / Claude 同源；新规则与 self-evolution L 机制对齐。**缺点**：3 个新文件 + 后续 rules.md 改动，两批 commit。
- **方案 B**：只在 `.claude/rules.md` 加 § N，不开独立文档。**不选**：内容 200+ 行，rules.md 会过度膨胀；且本规范"人机共用"，rules.md 是 Claude 专属视角不合适。
- **方案 C**：扩展 `03-开发/开发规范.md` §4 一段。**不选**：协作规范是跨阶段的（所有 phase 都需要），不该归到 03-开发 单阶段下。
- **方案 D**：先用飞书文档 / Wiki 写，仓库不落文件。**不选**：Claude 不自动加载外部文档；违反 [CLAUDE.md "文档落位 §H"](../../.claude/rules.md) 要求项目过程文档进仓库。

选 A。

---

## 7. 实施计划（Implementation Plan）

```
[x] Step 1 (commit 0fc27a3, 2026-05-17): 写 proposal 0008 + 协作规范.md + 在途任务.md（独立新增，无 SSoT 改动）
[x] Step 2 (2026-05-17): 用户 AskUserQuestion 明确 solo-review approve（选项"批 Phase 2 + gotcha"）
[x] Step 3 (follow-up commit, 2026-05-17): 改 .claude/rules.md 加 § N 章节（N.1-N.7 硬条款）— 经用户加 settings.local.json Edit 白名单后落地
[x] Step 4 (Phase 2 commit, 2026-05-17): 改 99-跨阶段/模块工作流.md 加"多 Session 协作"段
[x] Step 5 (Phase 2 commit, 2026-05-17): 改 03-开发/开发规范.md §4.2 增补 Worktree 段
[x] Step 6 (Phase 2 commit, 2026-05-17): 改根 CLAUDE.md "Rules & playbooks" 表加一行
[x] Step 7 (Phase 2 commit, 2026-05-17): 把 6 个并行协作指标加入 99-跨阶段/signals/2026-05.md §8 + 修订记录
[x] Step 8 (follow-up commit, 2026-05-17): 沉淀 worktree upstream gotcha 到 ~/.claude/skills/ruoyi-bootstrap/references/gotchas.md §6 — 同上经 settings.local.json 白名单后落地
[ ] Step 9 (可选, 后续): 在 .claude/settings.json UserPromptSubmit hook 加协作规范提示触发词
[ ] Step 10: 进入 tracking 期，6 周后回看 §8 指标（2026-06-30）

~~未完成的 Step 3 / Step 8 都属 agent-self-modification~~ — **已于 follow-up commit 落地**：用户路径 A 在 `.claude/settings.local.json` 加 `Edit(D:/.../plm/**/.claude/rules.md)` 与 `Edit(C:/Users/Wjl/.claude/skills/ruoyi-bootstrap/references/gotchas.md)` 两条白名单 → 重新 Edit 即过。
```

---

## 8. 衡量指标（How will we know it worked?）

Tracking 期内（merged 后 6 周）观察的信号：

| 信号 | 基线 | 目标 |
|---|---|---|
| 月度并行 session 峰值数 | 当前 5 | 5±2 维持 |
| 月度 SSoT 文件 merge 冲突次数 | 未采集（先 1 个月建基线） | 趋势下降 |
| 月度 revert / fixup commit 次数 | 未采集 | < 5 / 月 |
| 同模块并发改动事件数（违反 §4） | 未采集 | 0 |
| Proposal 编号冲突事件 | 未采集 | 0 |
| 协作规范违反导致的回退 | 未采集 | 趋势下降 |
| 在途任务.md "进行中"行数 vs 实际 `git worktree list` 数差距 | 未采集 | 差 ≤ 1（说明 Claude 在按 §3 维护台账） |

跟踪期：2026-05-17 ~ 2026-06-30。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | ✅ 通过 (solo-review) | 2026-05-17 | 用户 AskUserQuestion 明确选择"批 Phase 2 + gotcha"；Phase 2 项目文档 SSoT (4 项) 落地后 `.claude/rules.md § N` 与 `~/.claude/skills/.../gotchas.md §6` 经 classifier 拦截 2 次 → 用户路径 A 加 settings.local.json 白名单 → follow-up commit 全部补完；tracking 期 2026-05-17 → 06-30 若指标恶化走回滚 |

---

## 10. 实施后跟踪（merged 后填）

### 实际 PR / commit

- PR: 见 https://github.com/578516579/plm/pull/new/claude/sleepy-hellman-5a3950
- 合入 commit: `0fc27a3` (Phase 1 — 协作规范.md / 在途任务.md / proposal 0008 / README) + `2d7308d` (Phase 2 项目文档 4 项 — 模块工作流.md / 开发规范.md / CLAUDE.md / signals §8) + 本 commit (Phase 2 收尾 — .claude/rules.md § N + ~/.claude/skills/.../gotchas.md §6 + 状态字段收尾)
- 实际 merged 日期：2026-05-17（完整）

### Tracking 数据

| 信号 | 基线 | 目标 | W21 | W22 | W23 | W24 | W25 | W26 |
|---|---|---|---|---|---|---|---|---|
| 并行 session 峰值 | 5 | 5±2 | | | | | | |
| SSoT merge 冲突 | TBD | ↓ | | | | | | |
| revert/fixup | TBD | <5/月 | | | | | | |
| 同模块并发 | TBD | 0 | | | | | | |
| Proposal 撞号 | TBD | 0 | | | | | | |
| 台账维护差距 | TBD | ≤1 | | | | | | |

### 最终判定
- [ ] done（达成目标，本提案归档）
- [ ] reverted（指标未改善 → 走回滚 PR，分析原因记入此段）

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Claude (Wjl 会话, worktree sleepy-hellman-5a3950) | 初版（Phase 1 同次落地，Phase 2 待评审） |
| 2026-05-17 | Wjl (AskUserQuestion solo-approve) + Claude | Phase 2 部分落地：99-跨阶段/模块工作流.md "多 Session 协作"段 / 03-开发/开发规范.md §4.2 Worktree / 根 CLAUDE.md "Rules & playbooks" / 99-跨阶段/signals/2026-05.md §8 + 修订；状态 proposed → partial merged → tracking；.claude/rules.md § N 与 ~/.claude/skills/.../gotchas.md §6 因 classifier 拦截 agent-self-modification 未落地（Step 3 / Step 8 标 [!]） |
| 2026-05-17 | Wjl (settings.local.json 加白名单, 路径 A) + Claude | Phase 2 收尾：.claude/rules.md § N (N.1-N.7) + ~/.claude/skills/.../gotchas.md §6 落地；状态 partial merged → **merged → tracking**；Step 3 / Step 8 [!] → [x]；所有 Phase 2 改动合规完成 |

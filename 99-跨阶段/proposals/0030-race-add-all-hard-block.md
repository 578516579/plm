# Proposal 0030: 并行 session race add-all 从 nudge 升级到硬拦

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0030 |
| 标题 | session-guard.sh bulk add 默认 exit 2 硬拦 + `CLAUDE_BULK_OK` 显式后门 |
| 状态 | **draft → implementing**(随本 commit 落地,等用户 review 转 merged) |
| 类型 | 工具链(号段 0200-0299)|
| 提出人 | Claude(Wjl 会话,2026-05-28 epic 0028 收官,2 次事故复盘)|
| 提出日期 | 2026-05-28 |
| 评审人 | Wjl(solo-review,待签)|
| 评审日期 | _待定_ |
| Tracking 截止 | 2026-06-25(merged 后 4 周)|

---

## 1. 背景(What's the problem?)

2026-05-28 epic 0028 单日交付期间,**并行 session 在同一 working tree race 偷 staged 文件的事故复发 2 次**:

| # | Commit | 实际偷走的内容 | Commit msg subject(错配)|
|---|---|---|---|
| 1 | `3ae00fd` | P0-1 4 张外键 SQL + 4 Domain + Mapper 共 22 文件 | `test(openspec): vitest E2E ...`(别人 session 的 scope)|
| 2 | `656a6a4` | P0-2C 前端 11 文件 12 跨模块按钮 + composable + spec | `docs(proposal): README 索引登记 0029`(别人 session 的 scope)|

**根因链**:
1. proposal 0008 已立 `session-guard.sh` 检测 `git add . / -A / -u / commit -a`,但 hook 注释 L9-L11 明示「只 nudge,**永远 exit 0**,坏了也不能阻断正常工具调用」
2. nudge 在 stderr 输出后 Claude 继续执行 → bulk add 把别人 staged 的文件一勺烩
3. 2 次事故的 commit msg 都是「另一个 session 的 scope」,因为 bulk add 也带走了对方暂存的 README/proposal 改动
4. 现有 nudge 文案没有拒绝 → 不留心 review stderr 就过去了

**为什么 nudge 没效**:并行 session 的 Claude 看到 stderr 警告但**任务驱动惯性继续干**;`exit 0` 给了"放行"的语义信号,与"警告"自相矛盾。

---

## 2. 证据(Evidence)

- 关联 commit:
  - `3ae00fd`(2026-05-28)— 第 1 次事故,详 [proposals/0028 §10 第 1 次协作事故注解](0028-product-mainline-uplift-epic.md)
  - `656a6a4`(2026-05-28)— 第 2 次事故,详 0028 §10 第 2 次注解
  - `4bfe206` / `622622e`— 两次事故后人工注释 README/0028 的 docs commit
- 关联 proposal:
  - [0008 multi-session collaboration](0008-parallel-session-collaboration.md) — 留下 `session-guard.sh` 但**默认 nudge**
  - [0013 主 worktree 占用](0013-main-worktree-occupation-rule.md)、[0022 dirty tree stop nudge](0022-dirty-tree-stop-nudge.md) — 同主题前置防线
- 关联文件:
  - [.claude/hooks/session-guard.sh](../../.claude/hooks/session-guard.sh) L9-L11 注释 + L72 默认 exit 0
  - [99-跨阶段/active-sessions.md](../active-sessions.md) CLAIMS 块机制
- 关联 quirks:project-quirks.md 缺 Q-COLLAB-01「bulk add 偷 staged 文件」,本提案随附补登记

---

## 3. 提案(What's the change?)

### 3.1 改动文件清单

| 文件 | 改动 |
|---|---|
| `.claude/hooks/session-guard.sh` | 升级 git 分支:bulk + dirty ≥ 1 默认 `exit 2`(硬拦);`CLAUDE_BULK_OK` 环境变量后门放行 |
| `.claude/rules.md` § L(自进化)| 加段「并行 session race add-all 防线 = session-guard hard-block」|
| `CLAUDE.md` gotcha 表 | 8 → 9 条,加 Q-COLLAB-01 一行 |
| `memory/project-quirks.md` | 注册 Q-COLLAB-01 详条 |
| `99-跨阶段/proposals/README.md` | 索引登记 0030 行 |
| `99-跨阶段/在途任务.md` | ledger 加 0030 entry |

### 3.2 Diff 草案(session-guard.sh git 分支核心)

```sh
# === 升级前(L72-L73)===
# 无任何风险信号(非 bulk + 单文件脏 + 无认领冲突)→ 静默放行,不啰嗦
if [ "$bulk" = 0 ] && [ "$n" -lt 2 ] && [ -z "$hits" ]; then exit 0; fi

# === 升级后 ===
# 显式后门:本次 commit 之前 export CLAUDE_BULK_OK="<≥10 字符 reason>"
# 适用场景:epic 多模块批量 commit(如 0028 P0-3 30 文件)、bulk-refactor、模板化改造
if [ "$bulk" = 1 ] && [ -n "$CLAUDE_BULK_OK" ] && [ "${#CLAUDE_BULK_OK}" -ge 10 ]; then
  echo "ℹ️  [session-guard] CLAUDE_BULK_OK 后门生效:" >&2
  echo "   reason: $CLAUDE_BULK_OK" >&2
  echo "   放行本次 bulk add。注意 reason 会进 signals 月度统计。" >&2
  exit 0
fi

# 无任何风险信号 → 静默放行
if [ "$bulk" = 0 ] && [ "$n" -lt 2 ] && [ -z "$hits" ]; then exit 0; fi

# 硬拦核心:bulk add + working tree 有脏文件 = race 必然发生路径
if [ "$bulk" = 1 ] && [ "$n" -ge 1 ]; then
  echo "" >&2
  echo "⛔ [session-guard] HARD-BLOCK: bulk add 在共享 working tree 是协作 race 源头。" >&2
  echo "   ❗ working tree 有 $n 个改动文件,bulk add 会卷进所有 — 含别 session 的 WIP。" >&2
  echo "   📜 历史事故:3ae00fd(P0-1 22 文件被偷)/ 656a6a4(P0-2C 11 文件被偷)" >&2
  echo "" >&2
  echo "   修复方式(任选):" >&2
  echo "   a) 推荐:用显式路径 — git add <文件1> <文件2> ..." >&2
  echo "   b) epic / bulk-refactor 合法 bulk:" >&2
  echo "      export CLAUDE_BULK_OK=\"<reason 不少于 10 字>\" && <原命令>" >&2
  echo "   c) 一次性绕过(计入 signals 月度 bypass):" >&2
  echo "      export CLAUDE_BYPASS_SESSION_GUARD=1 && <原命令>" >&2
  echo "" >&2
  exit 2
fi

# bulk=0 但有他人认领冲突 → 保留原 nudge(L75-L85),不升级
if [ -n "$hits" ]; then
  # ... 原 hits 警告 + exit 0 ...
fi
exit 0
```

### 3.3 后门设计原则

| 维度 | 约定 |
|---|---|
| 触发方式 | 单次 Bash 调用内 `export CLAUDE_BULK_OK="..." && <cmd>`(env 不跨 Bash 调用)|
| reason 最小长度 | 10 字符,防空 reason 滥用 |
| reason 推荐格式 | `<proposal#> <scope>: <N 文件> <原因摘要>` 例 `0028 P0-3A+B: 30 文件 testreport+dora 后端聚合` |
| reason 质量分(C-2 reviewer 补)| reason 格式不在 hook 层强校验(POSIX sh 难判语义);**signals 月报抽样 N 例 reason 做"free-text 质量分"**:① 含 proposal# 引用 / ② 含具体 scope(模块名 / commit hash)/ ③ 含文件数。三项满足 ≥ 2 视为合格;月度合格率 < 50% 触发"加强校验"子提案(候选 0032)。**反模式拒绝清单**:`fix` / `bulk` / `update` / `wip` / `test`(凑 ≥10 字符的敷衍样本)|
| 失效 | 单次 Bash 调用结束 env 即丢,无持久化 |
| 统计 | 后续 signals 加 `bulk_ok_count` 维度,> 5/月触发月度审视是否常态化滥用(本提案 §8 跟踪)|

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Claude(主) | bulk add 命令 + dirty 文件 ≥ 1 时默认拒绝;触发 bulk 合法场景时主动加 `CLAUDE_BULK_OK` |
| 人手动 commit | **不受影响** — PreToolUse hook 只挂 Claude Code 工具栈;人在终端跑 git 不经过 |
| 已有 commit / PR | 无回溯,只对未来生效 |
| 测试 / 运维 | 无 |
| 文档 | CLAUDE.md gotcha 8→9;rules.md §L 加段;project-quirks Q-COLLAB-01 新条 |

---

## 5. 风险

- **风险 1 — 误拦合法 epic commit**(如 0028 P0-3A+B 单 commit 30 文件)
  **缓解**:`CLAUDE_BULK_OK` 后门,reason 显式声明合法范围
- **风险 2 — Claude 在 BULK_OK 后门里写空/敷衍 reason 把后门常态化**
  **缓解**:reason ≥ 10 字符门槛;signals 月度统计触发审视
- **风险 3 — Windows Git Bash 对 `${#var}` 字符串长度兼容**
  **缓解**:POSIX sh 标准语法,Git for Windows MSYS bash 支持;Step 4 双跑验证
- **风险 4 — 把"协作问题"工程化成"工具问题"** — 根因是 session 协议执行不严
  **缓解**:本提案只是**最后一道兜底**;前置仍走 proposal 0008 active-sessions 认领 + 0022 dirty tree stop nudge

---

## 6. 备选方案

- **方案 A — 不做(现状)**:nudge 已证 2 次失效。不选。
- **方案 B — 本提案**:hard-block + `CLAUDE_BULK_OK` 后门。**推荐**,改动面最小。
- **方案 C — pre-commit lint 3:commit msg scope vs staged files 错配校验**
  解析 `feat(<scope>): ...` 取 scope,查 staged 文件是否 80% 落在该 scope 对应路径前缀,否则拦
  **优点**:在 git 层补拦,人/AI 一视同仁
  **缺点**:实现复杂(scope→路径前缀映射表难维护);误报率难调
  **不在本期做**,留给 proposal 0031 候选(仅当 4 周跟踪期内本提案 + Q-COLLAB-01 仍复发再启动)
- **方案 D — `.claude/settings.json` 直接 PreToolUse 用 `deny` exit code**
  `session-guard.sh` 本就是 settings.json 调用的,本提案 §3 改 exit 2 等价
  本提案选 exit 2(脚本控制),后续可改 settings.json hook returns deny

---

## 7. 实施计划

```
[x] Step 1: 升级 .claude/hooks/session-guard.sh git 分支 — bulk + dirty ≥ 1 改 exit 2 + CLAUDE_BULK_OK 后门(commit `9ed456e`)
[x] Step 2: 改 .claude/rules.md §L.5 加协作 race 防线段(commit `2af35df`)
[x] Step 3: 改 CLAUDE.md gotcha 表 8→9, 加 Q-COLLAB-01 一行(同 `2af35df`)
[x] Step 4: memory/project-quirks.md 新建「协作层(COLLAB)」分类 + Q-COLLAB-01 详条(同 `2af35df`)
[x] Step 5: 99-跨阶段/proposals/README.md 索引登记(`9ed456e`)+ 在途任务.md ledger(`2af35df`)
[x] Step 6: 本地双跑 5 自检测试 — syntax OK / HARD-BLOCK exit 2 / CLAUDE_BULK_OK 放行 / 短 reason 拒绝 / 显式路径放行 / BYPASS 绕过 全绿(`9ed456e` 自检)
[ ] Step 7: Wjl solo-review 签字 → 状态 implementing → merged
[ ] Step 8: tracking 4 周(2026-05-28 ~ 2026-06-25),按 §8 信号观察
[ ] Step 9(reviewer C-1 补):**tracking 期满 owner**:2026-06-25 当天 Claude 自动跑 signals 月报扫两项触发条件:
    ① race 复发计数(基线 2,目标 0)
    ② BULK_OK 后门使用次数(目标 ≤ 5/月)+ reason 质量分(目标合格率 ≥ 50%,详 §3.3 反模式清单)
    任一超阈则**同会话内起 proposal 0031 草稿**(lint 3 commit msg scope vs staged files 路径前缀错配,§6 备选 C);均未超阈则提案归档 done
[ ] Step 10(reviewer C-2 补):tracking 期内每月 5 日,Claude 在月度反思中抽样 ≥ 3 例 BULK_OK reason 做质量分检查(§3.3 表),记入 reflect/2026-MM.md
```

---

## 8. 衡量指标

> 跟踪期:2026-05-28 ~ 2026-06-25(merged 后 4 周)

| 信号 | 基线 | 目标 | 数据源 |
|---|---|---|---|
| race add-all 偷 staged 事故复发次数 | 2(单日,3ae00fd + 656a6a4)| **0** | git log + msg 异常 + proposal 0028 §10 类注解 |
| session-guard hard-block 触发次数 | 0(nudge 期)| ≥ 1(证明在工作)| stderr 日志 grep `HARD-BLOCK` |
| `CLAUDE_BULK_OK` 后门使用次数 | n/a | ≤ 5/月 | signals 月报新维度 |
| `CLAUDE_BYPASS_SESSION_GUARD` 绕过次数 | n/a | ≤ 1/月(超即审视)| signals 月报新维度 |
| 平均 bulk-add commit 文件数(BULK_OK 触发时)| n/a | ≤ 40 / commit | git log --stat 抽样 |

**判定**:4 周后信号 1 = 0 且 信号 2 ≥ 1 → done;否则启动 proposal 0031 lint 3。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _2026-05-28_ | solo-review;本 commit 同时落 §3.2 diff,等用户 review 转 merged |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit
- 草稿 + Step 1 实装 commit:_(本 commit hash 待生成)_
- 实际 merged 日期:_待定_

### Tracking 数据

| 信号 | 基线 | 目标 | 实际(周 1)| 实际(周 2)| 实际(周 3)| 实际(周 4)|
|---|---|---|---|---|---|---|
| race 事故复发 | 2 | 0 | | | | |
| HARD-BLOCK 触发 | 0 | ≥ 1 | | | | |
| BULK_OK 后门 | n/a | ≤ 5/月 | | | | |
| BYPASS 绕过 | n/a | ≤ 1/月 | | | | |
| BULK_OK 平均文件数 | n/a | ≤ 40 | | | | |

### 最终判定
- [ ] done(达成目标,本提案归档)
- [ ] partial(信号 1=0 但 BULK_OK 滥用 > 5/月 → 启动 proposal 0031 lint 3)
- [ ] reverted(误拦严重影响开发 → 回滚到 nudge + 走方案 C)

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude(Wjl 会话,0028 epic 收官)| V1.0 — 初稿,2 次事故复盘后立项;同 commit 落 Step 1 实装等待 review 转 merged |
| 2026-05-28 | Claude(独立 reviewer 复盘)| V1.1 — 反向独立评审 7 维度评分卡(详会话 review 报告 §2.1)。**2 处建议改**已落地:**C-1**:§7 加 Step 9 tracking 期满 owner(2026-06-25 Claude 自动跑 signals 扫两项触发,任一超阈起 0031 草稿)+ Step 10 月度 reason 质量分抽样;**C-2**:§3.3 后门设计表加 reason 质量分细则 + 反模式拒绝清单(fix/bulk/update/wip/test)。**Verdict**:🟢 Approve(可签字转 merged),C-1/C-2 不阻塞 merged;**特别建议**:proposal 0030 是 self-evolution 闭环范本(事故 → 同会话立 proposal + 实装 + dogfood + 5 自检 + 提请 review),应入 `.claude/rules.md §L.2` "fast-track 例外条款"适用 hook/工具类小提案 |

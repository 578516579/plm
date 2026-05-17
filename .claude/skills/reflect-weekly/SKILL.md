---
name: reflect-weekly
description: Generate a weekly self-evolution reflection report (YYYY-WW.md) for the PLM repo. Use when the user asks for "周度反思 / 周报反思 / /reflect-weekly / W21 reflect / 这周回顾" or when end-of-week timing makes a weekly checkpoint natural. The skill gathers git/Gate/proposals/signals data, finds patterns, writes a structured report, and surfaces candidate proposals for the user to lift.
---

# reflect-weekly

**Phase B kickoff** of [self-evolution loop](../../../99-跨阶段/reflect/README.md) §周度反思.
Replaces the manual weekly reflection routine documented in `99-跨阶段/reflect/README.md` § 周度反思（`/reflect-weekly`）— 工作模式. Until this skill matured, all reflects were手工写 (W19/W20 dogfood/meta/audit, see `99-跨阶段/reflect/2026-W20*.md`).

This skill is **semi-automated**: gather data automatically (via Bash + Grep + Read), but **synthesis + decisions stay with the user** (per `.claude/rules.md` §L.3 反模式 "反思变成心理按摩").

---

## When to invoke

Trigger conditions (any one suffices):
- User says "/reflect-weekly", "周度反思", "周报反思", "W{NN} reflect", "这周回顾", "本周反思"
- User says "继续自进化" + 距离上一份 reflect 已 ≥ 5 天
- Monday morning 09:00 (if scheduled / cron is wired — Phase D+)
- Sprint 周期结束 + 用户问"该不该写周报"
- 出 P0 故障后 24h 内 (事件触发, 不等周一)

Do NOT invoke when:
- 距离上一份 reflect < 2 天 (除非用户明确要求 ad-hoc 反思)
- 当前会话已经手工写了反思 (双写浪费)

---

## Step-by-step

### Step 1: 确定数据时间窗

Default: ISO week containing today. Override if user specifies (e.g., "W20 reflect").

```bash
# Today's ISO week
TODAY=$(date +%Y-%m-%d)
WEEK=$(date +%G-W%V)                       # e.g. 2026-W21
WEEK_START=$(date -d "$TODAY - $(($(date +%u) - 1)) days" +%Y-%m-%d)
WEEK_END=$(date -d "$WEEK_START + 6 days" +%Y-%m-%d)
echo "Window: $WEEK_START to $WEEK_END ($WEEK)"
```

Output `WEEK` is the filename: `99-跨阶段/reflect/${WEEK}.md`.

### Step 2: 采集 5 类信号 (per `references/checks.md`)

并行 Bash:

```bash
# 1. Commit metrics
git log --since="$WEEK_START" --until="$WEEK_END 23:59" --pretty=format:"%h %s" > /tmp/wk-commits.txt
TOTAL=$(wc -l < /tmp/wk-commits.txt)
FIXES=$(grep -c "^fix" /tmp/wk-commits.txt || echo 0)
BYPASSES=$(git log --since="$WEEK_START" --until="$WEEK_END 23:59" --grep="no-verify" --oneline | wc -l)
COMMITTERS=$(git log --since="$WEEK_START" --until="$WEEK_END 23:59" --pretty=format:"%ae" | sort -u | wc -l)

# 2. Gate instance 新增
NEW_GATES=$(find 99-跨阶段/gate-checklists/instances -name "*.md" -newer .git/HEAD~10 | wc -l)   # heuristic

# 3. proposal / signals 变动
PROP_NEW=$(git log --since="$WEEK_START" --until="$WEEK_END 23:59" --diff-filter=A --name-only -- "99-跨阶段/proposals/*.md" | grep -E '^[0-9]{4}-' | wc -l)

# 4. 风险登记册变动
git log --since="$WEEK_START" --until="$WEEK_END 23:59" --oneline -- 99-跨阶段/风险登记册.md > /tmp/wk-risks.txt

# 5. Sprint backlog 流入 / 完成
BL_ADDED=$(git log --since="$WEEK_START" --until="$WEEK_END 23:59" -p -- "03-开发/Sprint backlog.md" | grep -c "^+| BL-")
```

也用 Grep 查"溯及" 标注、 "未完成" 字眼、 partial 状态。

### Step 3: 读 3 个核心文档作为上下文

- 上一份 reflect: `99-跨阶段/reflect/<上一周>.md` (用于"周-周连续性")
- 当月 signals: `99-跨阶段/signals/<当月>.md` (用于"信号产/处理速率")
- proposals/README.md 状态索引 (用于知道 in-flight proposal)

### Step 4: 找模式 (≥ 3 处 friction)

对照 `references/checks.md` 的 5 类触发条件：
- 模板某 §被频繁 friction / E 段豁免
- bypass / fix 异常高
- 候选堆积速率 > 处理速率
- silent merge (规范改了但 proposal 没立)
- cross-reference 缺口 (instance 未补"溯及")
- 元规则不适用的情况

写下 N 处 friction (≥ 3), 每处含 "现象 / 影响 / 根因 (5 Whys 可选) / 涉及文件路径"。

### Step 5: 用 `references/template.md` 写报告

填空: 头部 / §1 观察 (含量化数据表) / §2 诊断 / §3 行动 / §4 元复盘 / §5 链路 / §6 一句话总结 / 修订记录.

输出文件: `99-跨阶段/reflect/<WEEK>.md`

### Step 6: 显式呼出"候选 proposal" — 防"心理按摩"反模式

报告 §3 行动表中, 每条 friction 必须标注:
- → **转 proposal 0NNN** (升格为正式 proposal, 走 review)
- → **转 BL-YYYY-NNN** (降级到 Sprint backlog)
- → **直接小改** (≤ 5 行规范修订, 当次 commit)
- → **观察** (数据不足, 暂存 signals)

不允许"挂在 reflect 里啥也不做"。

### Step 7: 同会话 commit 反思 + 用户决策升格哪些候选

提交反思的 commit:
```
docs(reflect): YYYY-WW closing reflection — N findings + M proposal candidates
```

然后**主动问用户**:
> 周报识别了 N 个 friction, 其中 M 个建议升格为 proposal. 哪些立即升格?(可选 all / 部分 / 暂缓)

Solo 模式下: 用户答 "all" → 同会话顺势升格 (per [proposal 0040](../../../99-跨阶段/proposals/0040-self-evolution-v2-meta-rules.md) §3.5 solo same-day 路径)。

---

## 输出质量约束 (per `99-跨阶段/reflect/README.md` 反模式)

- ❌ 报告只有"做得好/改进点"两段, 没数据
- ❌ 报告说"一切都好"但 BYPASSES > 0 或 fix > 平均的 1.5 倍
- ❌ §3 行动 没具体到 文件+行号 或 proposal 编号
- ❌ 候选不转 proposal / 不转 BL → 反思变成"心理按摩"

满足质量约束 = 报告至少 100 行 + 量化数据表 + ≥ 3 个 friction + 每个 friction 有 §3 行动。

---

## 与其他 skill 协作

- 本 skill 输出周报 → 用户决定升格候选 → 触发 `/proposal` skill (Phase C 待实现) 自动生成 proposal 文件
- 月度 / 季度反思 → `/reflect-monthly` / `/reflect-quarterly` (待实现)
- 通用代码 / 业务模块场景 → 不调本 skill, 走 [ruoyi-bootstrap](~/.claude/skills/ruoyi-bootstrap/)

---

## 参考文件

- [references/template.md](references/template.md) — YYYY-WW.md 报告模板
- [references/checks.md](references/checks.md) — 5 类信号 + 6 种 friction 模式触发条件
- [references/examples.md](references/examples.md) — W19/W20 已有反思 (dogfood/closing/meta/audit) 作示例

---

## 历史 / 演进

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-17 | 首版,半自动化(数据采集自动 + 写报告半人工);触发自然语 + slash;Phase B kickoff |
| v0.2 | TBD | 全自动写报告(待 LLM 模式稳定);减人工干预到只 review |
| v0.3 | TBD | 触发 `/proposal` skill 自动产 proposal 文件 (Phase C 联动) |

---
name: signals-collect
description: Auto-collect 7 categories of self-evolution signals (commit / Gate / Phase / bug / Claude / risk / OKR) and write to a supplementary signals file. v0.2 adds Phase 耗时 auto-compute via scripts/phase-duration.sh. Use when the user asks for "采集信号 / collect signals / signals 数据更新 / 月度信号汇总 / signal supplementary / 跑一遍信号采集 / Phase 耗时". Outputs to 99-跨阶段/signals/YYYY-MM-supplementary.md (does NOT overwrite main file per signals/README.md convention). Phase D groundwork - provides data input for future auto rule health analysis.
---

# signals-collect

**Phase D groundwork** (2026-05-17 kickoff)。

Phase D 终态 = "metrics-driven rule tuning"。**核心瓶颈是数据**:  
之前 signals/${MONTH}.md 主文件靠人手填,**修订记录滚动加事件** 而非定量字段。本 skill 把 7 类信号的采集**机械化** — Phase D 自动建议升降级需要稳定的输入流。

- **v0.1** (2026-05-17): 7 类信号采集 → supplementary 文件 (基础)
- **v0.2** (2026-05-19): 加 **Phase 耗时自动计算** via [scripts/phase-duration.sh](scripts/phase-duration.sh)
  - 从 Gate instance 文件名解析 Phase + Date
  - 计算 entry / exit / within / gap
  - 跨模块汇总 (平均/中位/异常)
  - 与 4D 期望对照 (per proposal 0007/0010/0011/0012)
- Phase D v0.3+ 会加 **判断层** (基于数据建议规则升降级)。

---

## When to invoke

Trigger:
- User says "/signals-collect", "采集信号", "collect signals", "signals 数据更新", "月度信号汇总", "signal supplementary"
- Mid-month spot-check (周报反思想确认信号速率时)
- Month-end (作为 reflect-monthly 输入)
- 季度末 (作为 reflect-quarterly 输入)
- 任一 reflect skill 报告"数据陈旧, 跑 collect 一遍"

Do NOT invoke:
- 主文件 YYYY-MM.md **从不**被本 skill 覆盖 (per [signals/README.md §采集方式](../../../99-跨阶段/signals/README.md))
- 距上次 collect < 24h (除非用户明确要求)

---

## 输出文件约定

输出: `99-跨阶段/signals/${TARGET_MONTH}-supplementary.md`

格式 (头部 + 7 类信号表 + 元信息):

```markdown
# Signals 补充采集 — YYYY-MM-DD

> 由 [signals-collect skill](../../.claude/skills/signals-collect/SKILL.md) 自动产出。
> 主文件 [YYYY-MM.md](YYYY-MM.md) 不变, 本文件累加事件级数据 → 月底 reflect-monthly 合入主文件。

时间窗: $START ~ $END (累计 N 天)

## 1. Commit ... (各类 7 字段表)
...
```

---

## Step-by-step (5 阶段)

### Step 1: 确定时间窗

```bash
TARGET_MONTH=${1:-$(date +%Y-%m)}
WINDOW_START=${2:-"${TARGET_MONTH}-01"}
WINDOW_END=${3:-$(date +%Y-%m-%d)}    # 默认: 今天
echo "Window: $WINDOW_START to $WINDOW_END"

OUT="99-跨阶段/signals/${TARGET_MONTH}-supplementary.md"
```

### Step 2: 跑 7 类查询 (按 [references/queries.md](references/queries.md))

并行 Bash 7 类:

```bash
# Type 1: Commit
COMMIT_TOTAL=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --oneline | wc -l)
COMMIT_BYPASS=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="no-verify" --oneline | wc -l)
COMMIT_FIX=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^fix" --oneline | wc -l)

# Type 2: Gate
GATE_NEW=$(find 99-跨阶段/gate-checklists/instances -name "*${TARGET_MONTH}*.md" | wc -l)
GATE_SKIP=$(...)  # heuristic
EXCEPTION_RATE=$(...)

# Type 3: Phase 耗时 (v0.2 自动化)
# 调用 scripts/phase-duration.sh — 从 Gate instance 文件名解析 Phase + Date
# 输出 §3.1 各模块 Phase 时间表 + §3.2 跨模块汇总 + §3.3 异常 + §3.4 4D 期望对照
PHASE_DURATION_SECTION=$(bash .claude/skills/signals-collect/scripts/phase-duration.sh)

# Type 4: Bug
BUG_TOTAL=$COMMIT_FIX  # 简化等同
BUG_RECURRING=$(...)   # grep 相同 keyword 在 ≥ 2 个 fix commit 中

# Type 5: Claude 行为
# (无可观察的 git/文件信号; 月底 reflect 手填)
CLAUDE_BLOCK=N/A
CLAUDE_OVERRIDE=N/A

# Type 6: 风险登记册
RISKS_NEW=$(...)
RISKS_CLOSED=$(...)
RISKS_OPEN_P0_P1=$(...)

# Type 7: OKR
# (现 OKR 标 "不维护 KR", per proposal 0011)
KR_ON_TRACK=N/A
```

详细每条命令: 见 [references/queries.md](references/queries.md)。

### Step 3: 计算衍生指标

```bash
# 信号产 / 处理速率
LIFTED=$(grep -c "^- \[x\] " "99-跨阶段/signals/${TARGET_MONTH}.md")
PENDING=$(grep -c "^- \[ \] " "99-跨阶段/signals/${TARGET_MONTH}.md")
DOWN_RATIO=$(awk "BEGIN { print ($LIFTED + $PENDING > 0) ? $LIFTED / ($LIFTED + $PENDING) : 0 }")

# Sprint backlog 待办 / 完成
BL_PENDING=$(grep -c "^| BL-" "03-开发/Sprint backlog.md")
BL_DONE=$(awk '/## 已完成/{flag=1; next} /^## /{flag=0} flag && /^\| BL-/' "03-开发/Sprint backlog.md" | wc -l)
```

### Step 4: 写 supplementary 文件

用 [references/queries.md §template](references/queries.md) 模板写 OUT。包含 7 类数据表 + 衍生指标 + 时间戳 + 上次差。

### Step 5: 提示用户决策

输出末尾询问:

> 已采集 N 类信号写入 OUT, 主文件 YYYY-MM.md 不变。
>
> 下一步建议:
> - 月底 (last 1-3 日): 合入主文件 (调 reflect-monthly skill)
> - 中段 (本周): 不必合, 留作下次 reflect 输入

solo 模式 + 月底时机 → 用户答 "合入" → 触发 reflect-monthly 走合入路径。

---

## 输出质量约束

- [ ] 7 类信号字段全填 (N/A 也算)
- [ ] 时间窗有明确 START / END
- [ ] 衍生指标含"信号产/处理速率" + "Sprint backlog 流入/完成比"
- [ ] 与上次 supplementary 对比 (delta) 表
- [ ] 引用主文件 [YYYY-MM.md](YYYY-MM.md), 不覆盖

任一 fail → 不出文件; 提示用户哪条数据采集失败 (e.g. git log 时间窗有问题)。

---

## 与其他 skill 协作

- 下游主消费者: [reflect-monthly](../reflect-monthly/SKILL.md) §3 (signals 季度汇总) 直接读本文件
- 周报 [reflect-weekly](../reflect-weekly/SKILL.md) §2 (信号采集) — 可调本 skill 或自行采集
- 季度报 [reflect-quarterly](../reflect-quarterly/SKILL.md) §3 — 3 月 supplementary 累加

---

## Phase D 路线 (v0.1 → v0.5+)

| 版本 | 内容 | 时机 |
|---|---|---|
| **v0.1** (2026-05-17) | 7 类采集 → supplementary 文件 | ✅ |
| **v0.2** (2026-05-19) | Phase 耗时计算 (scripts/phase-duration.sh) | ✅ |
| v0.3 | 加 Claude block/override 信号 (从 hook log 或 session 摘要) | Phase D 中 |
| v0.4 | **判断层**: 基于 30 天数据自动 suggest MUST↔SHOULD 升降 | Phase D 完整 |
| v0.5 | 跨项目移植 (signals schema 通用化) | Q3+ |

---

## 参考文件

- [references/queries.md](references/queries.md) — 7 类信号的精确 Bash 命令 + 模板
- [scripts/phase-duration.sh](scripts/phase-duration.sh) — Phase 耗时 auto-compute (v0.2)

---

## 历史

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-17 | 首版; Phase D groundwork; 7 类采集 + 衍生指标; 不覆盖主文件; 月底合入路径 |
| v0.2 | 2026-05-19 | Phase 耗时 auto-compute (scripts/phase-duration.sh); 4 段输出 (§3.1 时间表 / §3.2 汇总 / §3.3 异常 / §3.4 4D 期望对照); 性能优化 (cache + awk 单次扫描) |

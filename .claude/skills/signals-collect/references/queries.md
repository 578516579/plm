# 7 类信号 — 精确 Bash 采集命令

> 与 [signals/README.md §7 类信号](../../../../99-跨阶段/signals/README.md) 1:1 对应。每类含命令 + 期望输出 + 异常阈值。

---

## 通用准备

```bash
TARGET_MONTH=${1:-$(date +%Y-%m)}
WINDOW_START=${2:-"${TARGET_MONTH}-01"}
WINDOW_END=${3:-$(date +%Y-%m-%d)}
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"
```

---

## Type 1: Commit 规范

```bash
# 1.1 commit_total
COMMIT_TOTAL=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --oneline | wc -l)

# 1.2 commit_violation_count (commit-msg hook 拒收数; 历史 commit 无法 trace, 看 reflog)
# 简化: 假设 0 (hook 拒收的不会进 log)
COMMIT_VIOLATION=0

# 1.3 commit_bypass_count (--no-verify 绕过)
# git log 不直接记 --no-verify, 但 commit msg 可能含痕迹 / 或检查 reflog
COMMIT_BYPASS=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="no-verify\|skip.hook" --oneline -i | wc -l)

# 1.4 commit 类型分布
FEAT=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^feat" --oneline | wc -l)
FIX=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^fix" --oneline | wc -l)
DOCS=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^docs" --oneline | wc -l)
REFACTOR=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^refactor" --oneline | wc -l)
PERF=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^perf" --oneline | wc -l)
CHORE=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^chore" --oneline | wc -l)
TEST=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^test" --oneline | wc -l)

# 1.5 unique committer
COMMITTERS=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --pretty=format:"%ae" | sort -u | wc -l)
```

**期望阈值**:
- `COMMIT_BYPASS == 0` → ✅
- `FIX / COMMIT_TOTAL < 0.30` → ✅
- `COMMITTERS == 团队规模` → ✅

---

## Type 2: Gate Checklist

```bash
# 2.1 当月新增 Gate 实例
GATE_NEW=$(find 99-跨阶段/gate-checklists/instances -name "*${TARGET_MONTH}*.md" -type f | wc -l)

# 2.2 跳 Phase 证据 (heuristic: 检查 README 模块进度 vs instances 文件)
# 复杂, 简化为人工检查
GATE_SKIP=0   # 月底人工核对

# 2.3 exception_filled_rate (实例 §E / §I / §J 异常段填写率)
GATE_TOTAL=$(find 99-跨阶段/gate-checklists/instances -name "*.md" -type f | wc -l)
GATE_WITH_EXCEPTION=$(grep -l "^## .* 异常\|^## E\..*异常" 99-跨阶段/gate-checklists/instances/**/*.md 2>/dev/null | wc -l)
EXCEPTION_RATE=$(awk "BEGIN { print ($GATE_TOTAL > 0) ? $GATE_WITH_EXCEPTION / $GATE_TOTAL * 100 : 0 }")

# 2.4 含 friction 关键字的 instance
GATE_FRICTION=$(grep -l "friction\|未达成\|豁免" 99-跨阶段/gate-checklists/instances/**/*.md 2>/dev/null | wc -l)
```

**期望阈值**:
- `EXCEPTION_RATE < 30%` → ✅ (高于 30% 说明模板对场景不适配)
- `GATE_SKIP == 0` → ✅

---

## Type 3: Phase 耗时 (v0.2 自动化)

**v0.2 (2026-05-19)**: 调 [scripts/phase-duration.sh](../scripts/phase-duration.sh) 一站出 4 段:

```bash
# 跑全 7 模块
bash .claude/skills/signals-collect/scripts/phase-duration.sh

# 或单模块
bash .claude/skills/signals-collect/scripts/phase-duration.sh project
```

输出格式 (markdown):
- **§3.1 各模块 Phase 时间表**: 7 模块 × 6 Phase, 每行 entry / exit / within / gap
- **§3.2 跨模块汇总**: 平均 within / 中位 within / 平均 gap per Phase
- **§3.3 异常 / 缺失**: within > 7d (P01-05) / > 14d (P06) / gap > 7d / P01-03 缺
- **§3.4 4D 期望对照**: solo + early × Phase 01-03 ≤ 2d (per proposal 0007/0010/0011/0012)

**算法概要**:
- 解析文件名 `Phase{NN}-<name>-Gate-{YYYY-MM-DD}.md` → (phase, date)
- Phase 06 含 `day7` / `closure` 关键字 → kind=exit, 否则 entry
- entry = 该 Phase 最早日期; exit = 最晚日期
- within = exit - entry; gap = 本 Phase entry - 上 Phase entry
- 异常阈值: P01-05 within > 7d, P06 within > 14d, gap > 7d, P01-03 缺失

**期望趋势** (per 4D 参数化, proposal 0007/0010/0011/0012):
- early × solo: P01-03 ≤ 2d, P04-05 ≤ 5d, P06 cycle = 7d
- stable × small: P01-03 ≤ 7d, P04-05 ≤ 14d, P06 cycle = 30d
- mature × medium+: 按 OKR 周期 (Q1/Q2/...)

**集成方式** (signals-collect 主 skill):
```bash
PHASE_DURATION_SECTION=$(bash .claude/skills/signals-collect/scripts/phase-duration.sh)
# 在 supplementary 文件 §3 段直接 echo "$PHASE_DURATION_SECTION"
```

---

## Type 4: Bug / 缺陷复发

```bash
# 4.1 bug_total (fix commits 简化等同)
BUG_TOTAL=$FIX   # 来自 Type 1

# 4.2 bug_recurring (同关键字出现在 ≥ 2 个 fix commit)
RECURRING=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^fix" --pretty=format:"%s" \
    | sed 's/^fix[^:]*:\s*//' \
    | awk '{print $1, $2, $3}' \
    | sort | uniq -c | awk '$1 >= 2 { print }' \
    | wc -l)

# 4.3 top 3 bug 类别 (从 fix commit message 关键字)
git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --grep="^fix" --pretty=format:"%s" \
    | sed 's/^fix[^:]*:\s*//' \
    | awk -F'[: -]' '{print $1}' \
    | sort | uniq -c | sort -rn | head -3
```

**期望阈值**:
- `RECURRING == 0` → ✅ (复发 = 流程或代码反模式)
- `BUG_TOTAL < 5 / 月` → ✅ (高于说明质量问题)

---

## Type 5: Claude 行为 (v0.3 hook log 接入, per proposal 0202)

PostToolUse hook 在每次 tool call 后写一行到 `.claude/logs/tools/YYYY-MM-DD.log`:
- TSV 格式: `timestamp\ttool_name\texcerpt(≤80 char)`
- 日志 gitignored (本地), 月底 reflect-monthly 读后可归档

```bash
LOG_DIR=".claude/logs/tools"
WS="$WINDOW_START"      # 时间窗起
WE="$WINDOW_END"        # 时间窗止

# 5.1 总 tool 调用
TOOL_TOTAL=$(find "$LOG_DIR" -name "*.log" -newermt "$WS" ! -newermt "$WE +1 day" 2>/dev/null \
    | xargs cat 2>/dev/null | wc -l)

# 5.2 tool 类型分布 (top 5)
TOOLS_BY_TYPE=$(find "$LOG_DIR" -name "*.log" -newermt "$WS" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '{print $2}' | sort | uniq -c | sort -rn | head -5)

# 5.3 --no-verify Bash 调用 (Real "override" 信号)
CLAUDE_NO_VERIFY=$(find "$LOG_DIR" -name "*.log" -newermt "$WS" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '$2=="Bash" {print $3}' | grep -c "no-verify")

# 5.4 Edit/Write canonical 规范文件 (per PreToolUse hint 仍编辑 = override)
CLAUDE_EDIT_CANONICAL=$(find "$LOG_DIR" -name "*.log" -newermt "$WS" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '$2=="Edit" || $2=="Write" {print $3}' \
    | grep -cE "rules\.md|开发规范\.md|Phase[0-9]{2}-.*-Gate\.md|proposals/0000-template\.md|settings\.json")

# 5.5 Subagent 使用分布
AGENT_USAGE=$(find "$LOG_DIR" -name "*.log" -newermt "$WS" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '$2=="Agent" {print $3}' | sort | uniq -c | sort -rn)

# 5.6 Skill 调用分布
SKILL_USAGE=$(find "$LOG_DIR" -name "*.log" -newermt "$WS" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '$2=="Skill" {print $3}' | sort | uniq -c | sort -rn)
```

**期望阈值**:
- `CLAUDE_NO_VERIFY == 0` → ✅ (per rules.md §L.2 silent merge 反模式)
- `CLAUDE_EDIT_CANONICAL` 配 proposal 文件数 ≈ 等量 → ✅ (符合 proposal-first 流程)
- `AGENT_USAGE` 含 4 自建 + 主流预定义 → ✅ (subagent 利用率正常)
- `SKILL_USAGE` 含 reflect/proposal/signals-collect → ✅ (自进化 skill 被实际触发)

**日志生命周期**:
- 按日分文件, 不滚动
- 月底 reflect-monthly 读后, 可手动归档 `mkdir -p .claude/logs/archive/YYYY-MM && mv .claude/logs/tools/YYYY-MM-*.log .claude/logs/archive/YYYY-MM/`
- 季度初可考虑删除超过 90 天的归档

**注意**:
- 日志可能缺失 (e.g. 历史 session / hook 部署前 / hook 失败) — 当 TOOL_TOTAL == 0 时, 标 "N/A (日志未捕)" 而非 0
- 跨日期边界: `find -newermt "$WS"` 含起始日 00:00, 不含 `$WE+1 day` 当日 00:00, 所以正好覆盖 [WS, WE] 闭区间

---

## Type 6: 风险登记册

```bash
RISK_FILE="99-跨阶段/风险登记册.md"

# 6.1 risks_new (本月新增 R-* / TD-*)
# 简化: 数 git log 改 risk 文件的次数
RISK_FILE_TOUCHED=$(git log --since="$WINDOW_START" --until="$WINDOW_END 23:59" --oneline -- "$RISK_FILE" | wc -l)

# 6.2 D.1 真风险 / D.2 技术债 当前总数
RISKS_D1=$(awk '/^## D\.1 真风险/,/^## D\.2/' "$RISK_FILE" | grep -c '^| R-')
RISKS_D2=$(awk '/^## D\.2 已知技术债/,/^## /' "$RISK_FILE" | grep -c '^| TD-')

# 6.3 P0/P1 open
RISKS_OPEN_P0_P1=$(grep -E '^\| R-' "$RISK_FILE" | grep -E 'open.*\|.*P[01]' | wc -l)
```

**期望阈值**:
- `RISKS_OPEN_P0_P1 == 0` 月末 → ✅ (开放 P0/P1 流程必须停下处理)
- `RISKS_D2` 增长 < 50% / 月 → ✅

---

## Type 7: OKR 进度

```bash
OKR_FILE="99-跨阶段/团队 OKR.md"

# 7.1 当前 OKR 状态 (per proposal 0011, 现状可能标 N/A)
if grep -q "本周期不维护数值型 KR" "$OKR_FILE"; then
    KR_STATUS="N/A (per proposal 0011, solo + early 不维护 KR)"
else
    # 解析 KR 表 (复杂, 留 Phase D v0.2)
    KR_STATUS="待 Phase D v0.2 自动解析"
fi
```

---

## 衍生指标 (跨类型)

```bash
# 信号产 / 处理速率 (本月候选)
CAND_LIFTED=$(grep -c "^- \[x\] \`[0-9]" "99-跨阶段/signals/${TARGET_MONTH}.md")
CAND_PENDING=$(grep -c "^- \[ \] \`[0-9]" "99-跨阶段/signals/${TARGET_MONTH}.md")
LIFT_RATIO=$(awk "BEGIN { print ($CAND_LIFTED + $CAND_PENDING > 0) ? $CAND_LIFTED / ($CAND_LIFTED + $CAND_PENDING) * 100 : 0 }")

# Sprint backlog 流入 / 完成
BL_PENDING=$(awk '/^## 待处理/,/^## 已完成/' "03-开发/Sprint backlog.md" | grep -c '^| BL-')
BL_DONE=$(awk '/^## 已完成/,/^## /' "03-开发/Sprint backlog.md" | grep -c '^| BL-')

# Proposal 状态分布
PROP_PROPOSED=$(grep -l "状态 |\s*\*\*proposed\*\*" 99-跨阶段/proposals/[0-9]*.md 2>/dev/null | wc -l)
PROP_MERGED=$(grep -l "状态 |\s*\*\*merged" 99-跨阶段/proposals/[0-9]*.md 2>/dev/null | wc -l)
PROP_TOTAL=$(ls 99-跨阶段/proposals/[0-9]*.md 2>/dev/null | wc -l)
```

---

## Supplementary 文件模板

```markdown
# Signals 补充采集 — YYYY-MM-DD

> 由 [signals-collect skill](../../.claude/skills/signals-collect/SKILL.md) 自动产出。
> 主文件 [YYYY-MM.md](YYYY-MM.md) 不变; 月底 reflect-monthly 合入主文件。

| 字段 | 值 |
|---|---|
| 采集时间 | YYYY-MM-DD HH:MM |
| 时间窗 | $WINDOW_START ~ $WINDOW_END (N 天) |
| 累计 commit | N |

---

## 1. Commit 规范

| 字段 | 当前累计 | 备注 |
|---|---|---|
| commit_total | N | |
| feat | N | |
| fix | N | |
| docs | N | |
| refactor | N | |
| perf | N | |
| chore | N | |
| test | N | |
| commit_bypass_count | N | ⚠️ if > 0 |
| unique committers | N | |

## 2. Gate Checklist

...

## 3. Phase 耗时

(Phase D v0.2 计算)

...

## 4-7 (省略, 按上述命令 fill)

---

## 衍生指标

| 字段 | 值 |
|---|---|
| 信号产 / 处理速率 | $LIFTED / ($LIFTED + $PENDING) = N% |
| Sprint backlog 流入 / 完成 | pending=$N1, done=$N2 |
| Proposal 状态分布 | proposed=$N, merged=$N, total=$N |

---

## 与上次 supplementary 对比 (delta)

(如有上次 supplementary 文件) ...

---

## 异常 / 触发 (本次)

- 触发: [信号字段] [实际值] vs [期望阈值] → [建议动作]
- ...

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| YYYY-MM-DD HH:MM | signals-collect skill | 自动采集 |
```

---

## 反模式

- ❌ 直接覆盖主文件 YYYY-MM.md (per signals/README.md)
- ❌ 把"N/A"当 0 算 (会假阳性)
- ❌ Phase 耗时未实现却假填 (留 N/A 标记)
- ❌ supplementary 文件累积过多 (月底 reflect-monthly 应合入并归档老 supplementary)

---
name: reflect-monthly
description: Generate a monthly self-evolution reflection report (YYYY-MM.md) for the PLM repo. Use when the user asks for "月度反思 / 月报反思 / /reflect-monthly / 2026-05 月报 / 月底回顾" or on month-end / month-start timing. Aggregates 4 weekly reflects + current month's signals, audits MUST/SHOULD spec health, runs tracking-closure judgment on proposals reaching their tracking deadline, and produces the canonical monthly artifact.
---

# reflect-monthly

**Phase B v0.1**（2026-05-17 上线）— mirror of [`reflect-weekly`](../reflect-weekly/SKILL.md) at monthly cadence.

This skill is the **canonical month-end ritual**. It enforces 3 high-stakes activities that周度反思无力承担：

1. **Tracking 终结判定**: 凡 tracking 期到期的 proposal, 必须判 `done` / `reverted` / `extend` 之一 (per [proposals/README.md §生命周期](../../../99-跨阶段/proposals/README.md))。**未及时判 = silent leak**。
2. **MUST / SHOULD 规则健康度审计**: 当月每条 [`rules.md`](../../../.claude/rules.md) §A-§M 条款触发次数; 长期 0 触发 → 建议降级 MUST→SHOULD; 频繁违反 → 建议升级 SHOULD→MUST 或调整阈值。
3. **跨周 friction 聚合**: 4 周 reflect 共找 N 处 friction; 看 N 中是否有"重复 ≥ 2 次"的 → 升格为更高优先级 proposal (跨周持续 = 系统性问题)。

---

## When to invoke

Trigger conditions:
- User says "/reflect-monthly", "月度反思", "月报反思", "YYYY-MM 月报", "月底回顾", "May reflect"
- Month start (第 1-3 日) — 处理上月数据
- Month end (last 1-3 日) — 提前预演 + 看 tracking 即将到期 proposals
- 任一 proposal `Tracking 截止` 字段距今 ≤ 3 天 → 主动建议跑 monthly

Do NOT invoke when:
- 距上月报 < 25 天 (避免月内频跑)
- 当周已跑 reflect-weekly 且无 tracking 到期 (月内中段)

---

## Step-by-step (7 阶段)

### Step 1: 确定月份 + 数据时间窗

```bash
TARGET_MONTH=${1:-$(date +%Y-%m)}              # default: 当前月
MONTH_START="${TARGET_MONTH}-01"
MONTH_END=$(date -d "${MONTH_START} +1 month -1 day" +%Y-%m-%d)
echo "Window: $MONTH_START to $MONTH_END (month=$TARGET_MONTH)"
```

输出 `TARGET_MONTH` 是文件名: `99-跨阶段/signals/${TARGET_MONTH}.md` (signals 主文件已存在) 与衍生月报 `99-跨阶段/reflect/${TARGET_MONTH}.md` (待生成)。

### Step 2: 聚合 4 周 reflect (跨周 friction 找重复)

```bash
# 当月所有 reflect 文件 (周报 + ad-hoc)
ls 99-跨阶段/reflect/ | grep -E "^${TARGET_MONTH/-/-W}|^${TARGET_MONTH}" > /tmp/m-reflects.txt

# 聚合 friction 编号 + 标题
for f in $(cat /tmp/m-reflects.txt); do
    grep -E "^#### F-" "99-跨阶段/reflect/$f" | sort -u
done | awk -F: '{print $2}' | sort | uniq -c | sort -rn > /tmp/m-friction-freq.txt
# 重复 ≥ 2 的 friction = 跨周持续 = 系统性问题
```

### Step 3: 完成 signals 月度汇总 (rewrite §1-§7 量化字段)

读 [signals/README.md §7 类信号](../../../99-跨阶段/signals/README.md) 7 类字段, 用当月全月数据填全。这一步覆盖之前 reflect-weekly mid-month 增量的"修订记录"列表, 改成正式月度 baseline。

```bash
# §1 Commit
git log --since="$MONTH_START" --until="$MONTH_END 23:59" --pretty=format:"%h %s" | wc -l        # commit_total
git log --since="$MONTH_START" --until="$MONTH_END 23:59" --grep="no-verify" --oneline | wc -l  # commit_bypass_count

# §2 Gate Checklist
find 99-跨阶段/gate-checklists/instances -name "*${TARGET_MONTH}*.md" | wc -l                   # gate_instances_added

# §3-§6 ... (per references/checks.md inherited from weekly)

# §7 OKR
grep -A1 "本周期不维护数值型 KR" 99-跨阶段/团队\ OKR.md || echo "OKR 已激活, 查 KR 进度"
```

### Step 4: Tracking 终结判定 (本 skill 核心独有)

按 [references/tracking-closure-checklist.md](references/tracking-closure-checklist.md):

1. **列出当月到期的 tracking proposals**:
   ```bash
   grep -l "Tracking 截止.*${TARGET_MONTH}" 99-跨阶段/proposals/[0-9]*.md
   # 例 2026-05 月底应到期: 0001-0006 (05-29) / 0027-0029 (05-30) / 0007-0012 (05-31) / 0040 / 0100/0101/0200 等
   ```

2. **对每个到期 proposal**: 读 §10 "Tracking 数据" 表, 按 baseline vs 实际填值判:
   - **done**: 全部 tracking 信号达到目标 → 归档, 状态改 `done`
   - **reverted**: 信号无改善 / 恶化 → 走回滚 PR + 写"失败提案"备忘
   - **extend**: 信号部分改善但样本不足 → tracking 期 +2 周, 标 `tracking (extended)`

3. **批量更新**: proposals/README.md 状态索引同步; signals/${TARGET_MONTH}.md 修订记录加"tracking 终结判定一行"

### Step 5: MUST / SHOULD 规则健康度审计 (本 skill 核心独有)

按 [references/spec-health-audit.md](references/spec-health-audit.md):

1. **MUST 频繁违反扫**: 当月有几次 commit 是触发了 MUST 规则随后被人工干预? 用 grep / 看 audit reflect。
2. **MUST 长期 0 触发扫**: 哪些 MUST 条款过去 ≥ 30 天没在 instance / commit / PR 评论中被引用?
3. **SHOULD 实际生效率**: 哪些 SHOULD 已成事实硬约束 (100% 跟随) → 建议升 MUST.

输出: 建议规则升降级的 N 条 proposal 候选 (留下月升格)。

### Step 6: 用 [references/template.md](references/template.md) 写月报

合并 Step 2 (跨周 friction) + Step 3 (signals 汇总) + Step 4 (tracking 终结) + Step 5 (规则健康度) 到一份 `99-跨阶段/reflect/${TARGET_MONTH}.md`。

### Step 7: 主动呼出"月度行动包"

报告末尾必须含 **"下月必做"** 段, 至少 3 项:

- N 项 tracking 已 done → 归档 (动作: 状态改 done + commit)
- M 项 tracking → reverted (动作: 写回滚 PR)
- K 项 tracking → extend (动作: 给定新截止日)
- L 项规则健康度建议 → 留下月转 proposal

同上提示用户决策, solo 模式可同会话顺势 apply。

---

## 输出质量约束

继承 [reflect-weekly SKILL.md §输出质量约束](../reflect-weekly/SKILL.md), 附加月度特有:

- [ ] §4 Tracking 终结判定段含到期 proposal 表, 每条标 done/reverted/extend + 理由
- [ ] §5 规则健康度审计段含 N 条建议 + 提供建议 ID
- [ ] §6 "下月必做" 段含 ≥ 3 项可执行 action
- [ ] signals/${TARGET_MONTH}.md §1-§7 7 类字段全填实 (不留 "TBD" / "待填")

任一 fail → 月报不结案。

---

## 与其他 skill 协作

- 上游: `reflect-weekly` × 4 周输出聚合, 月内 ad-hoc reflects 也合并
- 触发: tracking 终结的 proposal → 同会话状态改 done / reverted
- 下游: Step 7 输出的"规则健康度建议" → 调 `/proposal` skill (Phase C 待) 自动产候选

---

## 参考文件

- [references/template.md](references/template.md) — YYYY-MM.md 月报模板
- [references/tracking-closure-checklist.md](references/tracking-closure-checklist.md) — Tracking 终结 7 步判定
- [references/spec-health-audit.md](references/spec-health-audit.md) — MUST/SHOULD 规则触发频率扫描

---

## 历史

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-17 | 首版; Phase B 第 2 skill; 含 tracking 终结判定 + 规则健康度审计两大独有段 |

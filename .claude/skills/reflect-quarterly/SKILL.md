---
name: reflect-quarterly
description: Generate a quarterly self-evolution reflection report (YYYY-QN.md) for the PLM repo. Use when the user asks for "季度反思 / 季报反思 / /reflect-quarterly / Q1 reflect / 季度回顾 / 半年回顾" or on quarter-end timing. Aggregates 3 monthly reflects + 12 weekly reflects + accepted ADRs vs actual implementation + cross-document spec coherence (rules.md / 开发规范.md / 模块工作流.md three-way sync). Outputs the canonical quarterly artifact + 季度规范重构建议.
---

# reflect-quarterly

**Phase B v0.1 final piece** (2026-05-17 上线, Phase B 3/3 ✅)。

mirror of [reflect-weekly](../reflect-weekly/SKILL.md) / [reflect-monthly](../reflect-monthly/SKILL.md) at quarterly cadence。比 monthly 多 3 段独有:

| § | 任务 | 触发时机 |
|---|---|---|
| §A ADR 一致性审计 | accepted ADR vs 实际代码 / 部署 / Phase 进度 | 季度独有 (月度太频) |
| §B 跨文档 spec coherence | rules.md / 开发规范.md / 模块工作流.md 三者是否仍同步 | 季度独有 (每月扫无意义) |
| §C 季度规范重构建议 | 互相矛盾 / 半年未用 / 频繁踩坑无覆盖盲区 | 季度独有 (积累 ≥ 12 周数据才有信号) |

reflect-quarterly **不是** weekly/monthly 的简单累加, 而是**质变层** — 看半年级别的规范文档结构是否健康, 还是积压成"无人能完全 grasp 的烂摊子"。

---

## When to invoke

Trigger conditions:
- User says "/reflect-quarterly", "季度反思", "QN reflect", "季度回顾", "半年回顾", "六个月规范健康"
- Quarter end (last 1-3 日 of QN) — 计划 QN+1
- Quarter start (第 1-3 日 of QN) — 处理 QN-1 数据 + 整理 QN 目标
- 任一 ADR 数 > 20 (积累足够数据触发 ADR 一致性审计)
- 规范文档总行数 (rules.md + 开发规范.md + 模块工作流.md) 增长 > 50% / 季度 → 主动建议重构审计

Do NOT invoke:
- 距上季度报 < 80 天 (避免季内频跑)
- 距 Phase A kickoff < 3 月 (数据样本不够, 用 monthly 即可)

---

## Step-by-step (8 阶段)

### Step 1: 确定季度 + 数据窗

```bash
TARGET_Q=${1:-$(date +%Y-Q$(( ($(date +%-m) + 2) / 3 )))}   # default: 当前季度, 如 2026-Q2
case $TARGET_Q in
    *-Q1) Q_START="${TARGET_Q%-Q*}-01-01"; Q_END="${TARGET_Q%-Q*}-03-31" ;;
    *-Q2) Q_START="${TARGET_Q%-Q*}-04-01"; Q_END="${TARGET_Q%-Q*}-06-30" ;;
    *-Q3) Q_START="${TARGET_Q%-Q*}-07-01"; Q_END="${TARGET_Q%-Q*}-09-30" ;;
    *-Q4) Q_START="${TARGET_Q%-Q*}-10-01"; Q_END="${TARGET_Q%-Q*}-12-31" ;;
esac
echo "Window: $Q_START to $Q_END ($TARGET_Q)"
```

输出: `99-跨阶段/reflect/${TARGET_Q}.md`

### Step 2: 聚合 3 月报 + 12 周报

```bash
# 当季所有 reflect 文件
ls 99-跨阶段/reflect/ | awk -v s="$Q_START" -v e="$Q_END" '
    /^[0-9]{4}-(W|[0-9])/ {
        # 简化: 假设文件名含日期或周次能映射到 Q
        print
    }
' > /tmp/q-reflects.txt

# 聚合本季 friction 编号 + 标题
for f in $(cat /tmp/q-reflects.txt); do
    grep -E "^#### F-" "99-跨阶段/reflect/$f"
done | sort -u | wc -l   # 本季 friction 总数
```

跨月份找重复 friction (≥ 2 月持续 = 长期系统性问题)。

### Step 3: 完成 signals 季度汇总

读 3 份月度 signals (`99-跨阶段/signals/YYYY-MM.md` × 3), 算 7 类信号的季度均值 / 趋势。如:

```bash
# Commit 总数趋势
for m in 01 02 03; do   # 假设 Q1
    f="99-跨阶段/signals/${TARGET_Q%-Q*}-${m}.md"
    grep "commit_total" "$f" | head -1
done
```

输出: 月度 → 季度趋势图 (如 mermaid xychart, per [signals/README.md §数据存留](../../../99-跨阶段/signals/README.md))。

### Step 4: ADR 一致性审计 (季度独有)

按 [references/adr-consistency-audit.md](references/adr-consistency-audit.md):

1. **列所有 accepted ADR**: `ls 03-开发/ADR/`
2. **逐 ADR 对比实际**: ADR 决策 (如"用 SPR-YYYY-NNNN 编号格式") vs 现存代码 / 部署 / Phase 实施
3. **判定**:
   - ✅ 一致 — ADR 内容仍准确
   - ⚠️ 漂移 — ADR 与实际不符, 需 amend OR 标 superseded
   - ❌ 失效 — ADR 决策已被实践否定, 走 reverted

### Step 5: 跨文档 spec coherence (季度独有)

按 [references/spec-coherence-check.md](references/spec-coherence-check.md):

扫 [rules.md](../../../.claude/rules.md) / [开发规范.md](../../../03-开发/开发规范.md) / [模块工作流.md](../../../99-跨阶段/模块工作流.md) 三者:

- **重复条款**: 同一规则在多处定义 → 选 SSoT, 其他改 reference
- **互相矛盾**: A 文件说 X, B 文件说反 X → 决定保留哪个 + amend
- **半年未引用条款**: 全季度无任何 instance / reflect / commit 引用 → 候选删 / 降 SHOULD
- **频繁踩坑但规范没覆盖盲区**: reflect 反复出现某 friction 但规范无对应条款 → 候选新增条款

### Step 6: 季度规范重构建议 (季度独有)

综合 Step 4 + Step 5, 出建议:

| 建议类型 | 例 |
|---|---|
| **ADR 维护** | "ADR-0001 (生成项目编号规则) 与实际差异 X → amend" |
| **规范合并** | "rules.md §M.7 = 开发规范.md §0 字段命名, 同源, 合并到开发规范 单 SSoT" |
| **规范拆分** | "开发规范.md §1 后端规范 600 行, 拆为 §1A 业务约定 / §1B 系统约定" |
| **规范删除** | "rules.md §X (某 MUST 半年 0 触发) → 删 + 工具替代" |
| **规范升级** | "开发规范.md §Y (某 SHOULD 全季度 100% 遵守) → 升 MUST" |
| **新增条款** | "频繁出现 'XX 用 YY 写法'诉求, 规范无明文 → 加 .claude/rules.md §N" |

每条建议留下季度初转为 proposal (本 skill 不直接产 proposal, 保留人工 review 一道)。

### Step 7: 用 [references/template.md](references/template.md) 写季报

合 Step 2/3/4/5/6 输出到 `99-跨阶段/reflect/${TARGET_Q}.md`. 含 8 段 (头部 / 量化趋势 / ADR 审计 / spec coherence / 重构建议 / 下季度计划 / 元复盘 / 总结)。

### Step 8: 主动呼出 "下季度主线"

报告末必含 "**下季度主线**" — 2-3 条 quarter-scope 目标, 不是 task list:

例:
- 主线 1: 把 rules.md MUST 数从 N 减到 N × 0.7 (削规)
- 主线 2: 启动跨项目移植 (验证机制不只是过拟合 PLM)
- 主线 3: Phase D 自动 rule 健康度建议上线

---

## 输出质量约束

继承 [reflect-weekly](../reflect-weekly/SKILL.md) + [reflect-monthly](../reflect-monthly/SKILL.md) 约束, 附加季度特有:

- [ ] §A ADR 审计含每个 accepted ADR 的判定 (一致 / 漂移 / 失效)
- [ ] §B spec coherence 含至少 1 处"互相矛盾" 或"半年未引用" 或"盲区" 三类发现 (季度不可能全无)
- [ ] §C 重构建议含 ≥ 3 条 + 每条优先级 + 留 proposal 候选编号
- [ ] §"下季度主线" 含 2-3 条 quarter-scope 目标 (不是月度任务)
- [ ] signals 季度趋势含 mermaid xychart 或同等可视化 (per signals/README.md §数据存留)

---

## 与其他 skill 协作

- 上游: 3 份 monthly reflect + 12 份 weekly reflect 聚合
- 出口: 重构建议条 → 季度初转 proposal (走 `/proposal` skill Mode A)
- 触发: 季度末必走; ADR ≥ 20 / 规范行数 ≥ 50% 增长 → 季中也可主动触发

---

## 参考文件

- [references/template.md](references/template.md) — YYYY-QN.md 季报模板
- [references/adr-consistency-audit.md](references/adr-consistency-audit.md) — ADR vs 实际 6 维对比
- [references/spec-coherence-check.md](references/spec-coherence-check.md) — 三文档跨文档一致性扫描

---

## 历史

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-17 | 首版; Phase B 3/3 完成; 含 ADR 审计 + 跨文档 coherence + 季度重构建议三段独有 |

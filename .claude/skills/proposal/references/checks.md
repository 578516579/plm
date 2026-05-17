# /proposal skill — 元规则执行清单

> 调本 skill 时, 0040 / 0041 等元规则都要遵守。这是个简短 checklist。

---

## A. 写新 proposal 前 (Mode A Step 4)

### A.1 [0040 §3.1] 已 Read 目标文件

```bash
# 每个 proposal §3 改动文件清单的 target 都要 Read
for target in <文件列表>; do
    echo "Reading $target ..."
    # Read tool, 不依赖记忆
done
```

确认 §3 写的"段号 / 字段名 / sub-section 编号"逐字与目标文件当前版本一致。

### A.2 [0041 §3.1] 已 Grep 现存代码 (代码层 proposal)

**适用**: proposal 改 `03-开发/开发规范.md` / `.claude/rules.md` §A-§I / `02-设计/API设计.md`

**不适用**: proposal 仅改 Gate Checklist 模板 / proposals 元规则

```bash
# 例: proposal 约束 ServiceImpl FK 校验
# 应该 grep 现存 ServiceImpl 代码:
grep -rE 'Mapper\.selectByPrimaryKey' plm-backend/**/ServiceImpl.java
grep -rE 'checkExists' plm-backend/**/ISprintService.java
```

Grep 命中后:
- 现状合规 → 在 proposal §4 影响范围 写"现存代码全合规"
- 现状不合规 → 派生 BL-YYYY-NNN 入 [Sprint backlog](../../../../03-开发/Sprint%20backlog.md), 在 proposal §10 §"派生迁移项" 表填

未做 grep 而写代码层 proposal → 违反 0041 → reject。

---

## B. Apply proposal 时 (Mode B Step 2)

### B.1 重复 A.1 + A.2 (一致性确认)

apply 时, 目标文件可能在 propose 后被其他 commit 改过。重新 Read 一遍, 确认 §3 diff 仍然适用。

### B.2 spec drift 处理

若发现 §3 diff 不再适用 (e.g. 目标段号变了, 或被其他 proposal 已 apply):

→ proposal §修订记录 加 "scope 修正"行 + 写**修正过程文档化** (per 0040 §3.1 反约束 "不许悄悄改 §3 抹掉痕迹")

---

## C. Solo same-day 路径 (Mode B Step 4)

[0040 §3.5] 要求:

- commit message 必须含 `[solo-review]` 标签
- 单 commit 含 (a) proposal 文件 (proposed → merged 双状态在 §修订记录 体现) (b) 实际规范文件 diff
- §9 评审记录 必填"为什么 solo 单签足够" 段, 不能空 / 不能"——"

不满足任一 → 不算 same-day, 退化为 standard review 节奏。

---

## D. 三处状态一致性 (Mode C)

每次 apply 后, 立即同步:

| 位置 | 状态字段 |
|---|---|
| proposal 文件 §元信息 | `状态: merged → tracking` |
| [proposals/README.md](../../../../99-跨阶段/proposals/README.md) 状态索引 | 同 |
| [signals/YYYY-MM.md](../../../../99-跨阶段/signals/) 修订记录 | 含事件描述 |

漏一处 → 三个月后回看会困惑。

---

## E. Partial 状态正确性 (Mode B / C)

仅当 N 个子项中 K < N 落地, 且 K ≥ 1 时, 标 `merged → tracking (partial)`:

- §10 段必须列"已落地子项" + "延后子项 + 截止日期 + 负责人"
- 子项 ≥ 3 且各自需独立 review → 应该改为**拆 sub-proposal** (per 0040 §3.2)
- 子项 ≤ 2 或同源同次评审 → partial OK

避免: 用 partial 当"我有点事没做完"的借口。

---

## F. Bundle 决策 (Mode A Step 3)

[0040 §3.3] bundle 判据满足任一:

- ✓ 同目标文件
- ✓ 同语义簇 (同根因)
- ✓ 同评审人

违反任一 → 拆:

- ✗ 跨号段 (0001-99 vs 0100+ vs 0200+)
- ✗ 评审节奏不同 (流程 1 周 vs 编码 3 天)
- ✗ 实施 PR 不能合并到同次 commit

例正:
- 0008 bundle 0008+0009 ✓ (同语义)
- 0016 bundle 0016-0021 ✓ (同目标文件 Phase 02)

例反:
- 假设 bundle 0100 + 0040 ✗ (跨 0001-99 流程 vs 0100+ 编码)

---

## G. 5 类反模式 (自检)

每跑 skill 后回头看, 确认未犯:

| 反模式 | 自检 grep |
|---|---|
| Silent merge | 规范文件本周有 commit, proposals 同周无新 proposal → 违反 |
| 写错段号无 §修订记录 | proposal §3 diff 失败但 §修订记录 没"scope 修正"行 |
| Solo 评审无"为什么足够" | grep -L "solo 单签理由" 99-跨阶段/proposals/*.md |
| Partial 滥用 | grep -c "(partial)" → 比例过高 (> 30%) 触发审计 |
| Cross-reference 缺口 | proposal §7 Step "instance 补'溯及'" 写了, 但 instance 实际 0 命中 → BL 入 backlog |

发现 → 立即修复 (不要拖到 reflect-monthly 才发现)。

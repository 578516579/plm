# Reflect — YYYY-QN 季度反思

> 12 周 + 3 月汇总, 含 ADR 一致性审计 + 跨文档 spec coherence + 季度规范重构建议.
> 比月报多 3 段质变层独有: §A ADR / §B coherence / §C 重构建议.

---

## 头部

| 字段 | 值 |
|---|---|
| 季度 | YYYY-QN |
| 时间窗 | YYYY-MM-DD ~ YYYY-MM-DD |
| 执行者 | {Wjl + Claude / 团队} |
| 关联 month reflects | [../YYYY-MM-1.md / -2.md / -3.md] |
| 关联 weekly reflects | 12 份 (W{NN-11} ~ W{NN}) |
| 关联 ADRs accepted | N 个 ([../03-开发/ADR/](../../../03-开发/ADR/)) |
| 上一份季报 | {链 YYYY-QN-1.md / 或"（首份）"} |

---

## 1. 季度量化趋势

### 1.1 7 类信号月度对比

| 类别 | M{Q-2} | M{Q-1} | M{Q} | 趋势 |
|---|---|---|---|---|
| commit_total | | | | ↗/↘/→ |
| commit_bypass | | | | |
| gate_instances_added | | | | |
| fix recurring | | | | |
| claude_block / override | | | | |
| risks new / closed | | | | |
| kr_on_track_pct | | | | |

### 1.2 mermaid xychart (核心指标)

```mermaid
xychart-beta
    title "Quarterly proposal lifecycle"
    x-axis [M{Q-2}, M{Q-1}, M{Q}]
    y-axis "Count" 0 --> 30
    bar "lifted" [N1, N2, N3]
    line "merged" [N1', N2', N3']
```

---

## 2. 跨月持续 friction (≥ 2 月重复 = 长期系统性问题)

| Friction 主题 | 月度命中 | 累计 | 当前 / 建议 |
|---|---|---|---|
| {例: cross-reference 缺口 instance 未补"溯及"} | M{Q-2} / M{Q-1} | 2 月持续 | BL-006 → 升 P0 季度内必清 |
| ... | | | |

---

## §A. ADR 一致性审计 (季度独有)

按 [adr-consistency-audit.md](../../.claude/skills/reflect-quarterly/references/adr-consistency-audit.md) 逐 ADR 审计:

| ADR | 决策 | 实际状态 | 判定 | 后续 |
|---|---|---|---|---|
| [ADR-0001](../03-开发/ADR/0001-...md) | 项目编号 PRJ-YYYY-NNNN | 代码 Domain 生成 PRJ-2026-0001 ✓ | ✅ 一致 | 保留 |
| ... | | | | |

**统计**: 一致 N, 漂移 M, 失效 K.

---

## §B. 跨文档 spec coherence (季度独有)

按 [spec-coherence-check.md](../../.claude/skills/reflect-quarterly/references/spec-coherence-check.md) 4 维扫:

### B.1 重复条款 (同规则多处定义)

| 规则主题 | 在文档 A | 在文档 B | 建议 SSoT |
|---|---|---|---|
| | | | |

### B.2 互相矛盾 (A 文档说 X, B 文档说反 X)

| 主题 | A 说 | B 说 | 决议 |
|---|---|---|---|
| | | | |

### B.3 半年未引用条款 (候选删 / 降级)

| 条款 ID | 上次引用 | 6 月内引用次数 | 建议 |
|---|---|---|---|
| | | | |

### B.4 频繁踩坑但规范无覆盖 (候选新增)

| 现象 | 反复出现于 | 当前缺规范 | 建议新增 |
|---|---|---|---|
| | | | |

---

## §C. 季度规范重构建议 (季度独有)

| # | 建议类型 | 详情 | 优先级 | 留下季度 proposal 候选 |
|---|---|---|---|---|
| C1 | ADR 维护 | | | |
| C2 | 规范合并 | | | |
| C3 | 规范拆分 | | | |
| C4 | 规范删除 | | | |
| C5 | 规范升级 | | | |
| C6 | 新增条款 | | | |

至少 3 条 (季度不可能全无建议)。

---

## 3. spec 演进季度里程碑

| 维度 | 数 |
|---|---|
| 当季新升 proposal | N |
| 当季 apply 落地 | N |
| 季度内 tracking 终结 | done N / reverted M / extend K |
| 季度内 reflect 文件 | N (≥ 12 周 + 3 月 + ad-hoc) |
| 季度内 BL 完成 | N |
| 季度内 skill 新增 | N |
| 季度内 hook 新增 / 升级 | N |

---

## 4. 下季度主线 (≥ 2 条, ≤ 3 条 quarter-scope 目标)

| # | 主线 | 衡量 | 责任人 |
|---|---|---|---|
| **M1** | {例: 削规 — MUST 数 30 → 21} | rules.md MUST 段数 | Wjl + Claude |
| **M2** | {例: 跨项目移植验证} | 第 2 个项目跑通自进化机制 | Wjl |
| **M3** | {例: Phase D 自动 rule 健康度} | 月度 audit 含自动建议 | Wjl |

---

## 5. 元复盘 (季度看自进化机制自身)

| 维度 | 值 |
|---|---|
| 自进化机制行数 (rules.md + 开发规范.md + 模板 + skill) | N (季初 → 季末 增长 %) |
| 已建立 skill 数 | N (季内新增) |
| 已建立 hook 数 | N (季内新增) |
| 反思文件累计 | N (跨季节奏) |
| 团队规模变化 | solo → small? |
| **整体评估**: 机制更"刚 / 柔 / 平衡" | 主观 1-5 评 |

---

## 6. 链路

- 触发: {commit / 季度末 / 用户指令}
- 衍生: 下季度 proposal 候选 N 条 (C1-C{N})
- 关联: 3 月报 / 12 周报 / ADR 全审计 / spec coherence 全扫

---

## 7. 一句话总结

**{3-5 句精炼总结, 含本季度核心质变 + 长期趋势 + 下季度主线}**

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| YYYY-MM-DD | {作者} | 首次创建 |

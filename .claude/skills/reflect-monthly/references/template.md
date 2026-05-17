# Reflect — YYYY-MM 月度反思

> 当月自进化机制 + 业务节奏的整体回顾。汇总 4 周周报 + ad-hoc reflects + signals + tracking 终结 + 规则健康度。
> 比周报多 3 段独有: §4 Tracking 终结判定 / §5 规则健康度审计 / §6 下月必做.

---

## 头部

| 字段 | 值 |
|---|---|
| 月份 | YYYY-MM |
| 时间窗 | YYYY-MM-01 ~ YYYY-MM-DD |
| 执行者 | {Wjl + Claude / 自动 (Phase D+)} |
| 关联 signals 主文件 | [../signals/YYYY-MM.md](../signals/YYYY-MM.md) |
| 关联当月 reflects (周/事件触发) | {链 W{NN}.md × 4 / dogfood / meta / audit} |
| 上一份月报 | {链 YYYY-MM-1.md / 或"（首份）"} |

---

## 1. 量化数据（来自 signals 月度汇总）

完整 7 类字段引用 [signals/${YYYY-MM}.md](../signals/${YYYY-MM}.md), 此处摘要:

| 类别 | 关键字段 | 当月 | 上月 | 环比 |
|---|---|---|---|---|
| 1. Commit | commit_total / fix / bypass | | | |
| 2. Gate | instances_added / skip_evidence / exception_filled_rate | | | |
| 3. Phase | avg_duration / bottleneck | | | |
| 4. Bug | total / recurring / top_3 | | | |
| 5. Claude | block_count / override_count | | | |
| 6. 风险 | new / closed / open_p0_p1 | | | |
| 7. OKR | kr_on_track_pct / kr_at_risk | | | |

---

## 2. 跨周 friction 聚合 (≥ 2 周重复 = 系统性问题)

读当月 N 份 reflect 文件, 抽 F-WW-NN / F-META / F-AUDIT 编号去重:

| Friction 主题 | 出现周次 | 累计提及次数 | 当前处置 | 建议 |
|---|---|---|---|---|
| {例: 24 instance 未补"溯及"} | W20 audit / W21 ? | 2+ | BL-2026-006 P2 | 升 P1 / 开 Sprint 专项 |
| ... | | | | |

**系统性 friction (跨周 ≥ 2 次)** → 优先处理, 不要让它再出现第 3 周。

---

## 3. Spec 演进当月里程碑

| 维度 | 当月动作 | 数 |
|---|---|---|
| 新 proposal 升格 | | N |
| Proposal apply (proposed → merged) | | N |
| Proposal partial → full | | N |
| Sprint backlog 新增 | BL-YYYY-NNN | N |
| Sprint backlog 完成 | | N |
| 规则文档变更 (rules.md / 开发规范.md / 模板) | | N |
| Skill 新增 / 升级 | | N |

---

## 4. Tracking 终结判定 (本月独有 §)

按 [tracking-closure-checklist.md](../../.claude/skills/reflect-monthly/references/tracking-closure-checklist.md) 逐 proposal 判定:

| Proposal | tracking 截止 | 主要 tracking 信号 | 实际 | 判决 | 后续动作 |
|---|---|---|---|---|---|
| [0001](../proposals/0001-internal-tool-track.md) | YYYY-MM-DD | E 段豁免数 baseline 3 → target ≤ 1 | {实际值} | ✅ done / ❌ reverted / ⏳ extend | 状态改 / 回滚 PR / 新截止 |
| ... | | | | | |

**统计**: done = N, reverted = M, extend = K. {如有 reverted, 列每条"失败原因学习要点"}

---

## 5. MUST / SHOULD 规则健康度审计 (本月独有 §)

按 [spec-health-audit.md](../../.claude/skills/reflect-monthly/references/spec-health-audit.md) 扫每条 .claude/rules.md §A-§M:

### 5.1 长期 0 触发的 MUST (建议降 SHOULD)

| 条款 | 上次触发 | 30 天内触发数 | 建议 |
|---|---|---|---|
| {例: §C #2 "DB 弱口令" — 自始 0 触发} | N/A | 0 | 保留 MUST, 但加 cron 自检; 或转 BL 实现 |

### 5.2 频繁违反的 MUST (建议改阈值 / 拆条款 / 上升级)

| 条款 | 当月违反数 | 违反场景 | 建议 |
|---|---|---|---|
| | | | |

### 5.3 已成事实硬约束的 SHOULD (建议升 MUST)

| 条款 | 当月遵守率 | 建议 |
|---|---|---|
| | | |

输出: 建议升降级的 N 条 → 留下月转 proposal (编号留)

---

## 6. 下月必做 (action package)

至少 3 项:

| # | 行动 | 类型 | 截止 | 负责人 |
|---|---|---|---|---|
| **D1** | tracking → done 的 N 项归档: 状态改 + commit | 维护 | 月初 | Claude |
| **D2** | tracking → reverted 的 M 项回滚 PR | 修正 | 月初 | Wjl + Claude |
| **D3** | tracking → extend 的 K 项新截止日 | 维护 | 月初 | Claude |
| **D4** | 规则健康度建议 L 条升格为 proposal | 升级 | 月内 | Claude |
| **D5** | 系统性 friction 优先级提升 | 调度 | 本月 sprint plan | Wjl |

---

## 7. 元复盘 (可选)

| 维度 | 值 |
|---|---|
| 月内反思文件数 | N (≥ 4 周报 + ad-hoc) |
| 月内 proposal 总账新增 | N |
| Sprint backlog 完成率 | N% (完成 / (待办 + 完成)) |
| signals 7 类字段填充完整度 | N/7 |

---

## 8. 一句话总结

**{2-3 句精炼总结, 含本月 spec 演进核心 + 跨月趋势 + 下月主线}**

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| YYYY-MM-DD | {作者} | 首次创建 |

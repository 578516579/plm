# Proposal 0040: 自进化机制 v2 元规则 — 5 处过程质量盲区批量修复

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0040 |
| 标题 | proposal 写前 Read + partial 状态形式化 + bundle 判据 + 降级通道 + solo 评审节奏 |
| 状态 | **merged → tracking** |
| 类型 | 流程（meta — 关于 proposal 机制本身的规则）|
| 提出人 | Wjl + Claude（reflect/2026-W20-self-evolution-process-meta）|
| 提出日期 | 2026-05-17 |
| Bundle | 本提案合并 5 个 F-META friction（F-META-01 ~ F-META-05）|
| 评审截止 | 2026-05-24 |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

W19/W20/W21 三周连续跑自进化机制后，[reflect/2026-W20-self-evolution-process-meta.md](../reflect/2026-W20-self-evolution-process-meta.md) 识别 5 处机制本身的盲区：

| ID | 现象 | 根因 |
|---|---|---|
| F-META-01 | 2/20 proposal 写错段号 (0013 §D / 0016 §3.4) | 模板没要求"写前先 Read 目标文件" |
| F-META-02 | "partial merged" 状态硬塞 | 状态机不含 partial |
| F-META-03 | bundle vs 拆分凭感觉 | 无书面 bundle 判据 |
| F-META-04 | 3 候选 (0023/0024/0026) "降级 Sprint TODO" 但无承接文件 | 降级通道断头 |
| F-META-05 | 评审 SLA 100% 失效 (solo 同日 accept) | proposals/README.md §评审节奏 没 solo 特殊路径 |

5 处同源 — 都是 "机制 v1 baseline 没和业务模板同步演化"。本提案 batch v2 元规则升级。

---

## 2. 证据

- 关联 reflect: [2026-W20-self-evolution-process-meta.md](../reflect/2026-W20-self-evolution-process-meta.md) §1.3 + §2 + §3
- 关联 实际事件: 0013 §3.3 scope 错 (commit `12e7e14` 写时) + 0016 §3.4 scope 错 (同 commit)
- 关联 proposal 状态: 0013 / 0032 标 "merged → tracking (partial)" — 不在 [README §一份 proposal 的生命周期](../proposals/README.md) 状态列表中
- 关联 candidates: signals 候选 0023/0024/0026 标 "code TODO" 但无目的地

---

## 3. 提案

### 3.1 解 F-META-01: proposal 模板加 "写前 Read 目标文件" 必填 (写到 `0000-template.md`)

```diff
 ### 改动文件清单
 
 | 文件 | 改动类型 |
 |---|---|
 | `03-开发/开发规范.md` §X | 修改 |
 | `.claude/rules.md` §A.1 | 新增 |

+**写 proposal 前必填校验**：
+- [ ] 已 `Read` 上表每个目标文件的当前完整内容
+- [ ] §3 写的"段号/字段名"逐字与目标文件当前版本一致（不依赖记忆）
+- [ ] Diff 草案在目标文件实际段位置精确可应用
+
+若 apply 时发现 scope 错位 → 本 proposal 在 §修订记录 写"scope 修正"，并把修正过程文档化（不许悄悄改 §3）。
+
 ### Diff 草案
```

### 3.2 解 F-META-02: 状态机加 "partially-merged" 状态 + 拆解策略 (写到 `proposals/README.md`)

```diff
 ## 一份 proposal 的生命周期
 
 ```
 draft       由 /reflect 自动产出 OR 人工提
    ↓
 proposed    完成填写、关联数据，等 review
    ↓
    ├─→  accepted     评审通过，进入 implementing
    │       ↓
    │    implementing  Claude / 人 在写规范变更 PR
    │       ↓
    │    merged        PR 已 merge，规范升级生效
    │       ↓
    │    tracking      2-4 周观察期，看相关 signals
    │       ↓
    │       ├─→ done         指标好转，提案归档
    │       └─→ reverted     指标无改善 / 恶化 → 走回滚 + 写"失败提案"备忘
    │
    ├─→  rejected    评审不通过，留作记录（rejected 不删，保留学习价值）
    └─→  superseded  被更新的提案替代（指向新提案）
 ```
 
+### "partially-merged" 状态（proposal 0040 引入）
+
+当 proposal 含 N 个改动点，只有 K < N 落地时：
+
+- **首选策略 (拆)**: 把 proposal 拆为 N 个子 proposal (编号 NNNN-a / NNNN-b...)，已落地的 sub-proposal 走 merged 路径，未落地的留 proposed
+- **次选策略 (标 partial)**: 整个 proposal 标 `merged → tracking (partial)`，在 §10 实施跟踪段明确列"已落地子项" + "延后子项 + 截止日期 + 负责人"
+- 拆 / 标的选择标准: 子项 ≥ 3 且各自需独立 review 周期 → 拆；子项 ≤ 2 或同源同次评审 → 标 partial 
+- partial 状态在 [proposals/README.md] 状态索引中显示为 `merged → tracking (partial)`
```

### 3.3 解 F-META-03: bundle 判据段 (写到 `proposals/README.md`)

```diff
 ## 文件命名
 
 `NNNN-<标题简写>.md`，编号递增。例如：
 ...
 
+### Bundle 判据（proposal 0040 引入）
+
+多个 signals 候选可以 bundle 成 1 个 proposal 文件，**前提满足任一**：
+
+- 同目标文件: 全部候选改的是同一个目标规范文件的连续段（如 0016-0021 全在 Phase 02 §B）
+- 同语义簇: 候选有同一根因 (如 0008+0009 都是 "Phase 05 early+solo 简化")
+- 同评审人: 所有候选评审人完全一致
+
+**禁止 bundle 的情况**:
+- 不同号段范围（0001-0099 流程 vs 0100+ 编码 vs 0200+ 工具链）→ 拆
+- 评审节奏不同（流程类 1 周 vs 编码规范 3 天）→ 拆
+- 实施 PR 不能合并到同次 commit → 拆
+
+Bundle 后的 proposal 在 §元信息 加 "Bundle" 字段，列出合并的 signals 候选号。
```

### 3.4 解 F-META-04: 建 Sprint backlog 文件作为降级通道终点

```diff
+# 新建: 03-开发/Sprint backlog.md
+
+# Sprint Backlog — 待 Sprint 计划吸纳的 code TODO
+
+> 自进化机制 [proposals/](../99-跨阶段/proposals/) 的"降级通道终点"（proposal 0040 引入）。
+> signals 候选若被判"非规范变更（性能/重构/纯代码改造）"，降级到本文件等待 Sprint 计划吸纳。
+
+## 待处理
+
+| ID | 来源 | 标题 | 优先级 | 预期 Sprint | 负责人 |
+|---|---|---|---|---|---|
+| BL-2026-001 | signals 候选 0023 | Sprint 健康度统计 `selectSprintStats` 4 次 SQL 改 GROUP BY | P2 | TBD | TBD |
+| BL-2026-002 | signals 候选 0024 | Task 看板 LIMIT 50 从内存切片改 SQL 分别拉 | P2 | TBD | TBD |
+| BL-2026-003 | signals 候选 0026 | Task 看板列从字典 `biz_task_status` 读取 | P3 | TBD | TBD |
+
+## 已完成（归档）
+
+| ID | Sprint | 完成 commit |
+|---|---|---|
+| | | |
+
+---
+
+## 自进化降级判据
+
+signals 候选满足以下条件 → 降级到本 backlog (而不是升格为 proposal):
+
+- 性能优化（不涉及规范变更）
+- 单个代码模块的重构（不影响其他模块）
+- 文档 typo / 链接修正
+- 字典 / 配置数据维护
+
+反过来，**升格为 proposal** 的条件: 涉及规范文件 / Gate 模板 / 多模块约定 / .claude/rules.md。
```

### 3.5 解 F-META-05: solo 模式评审节奏 (写到 `proposals/README.md` §评审节奏)

```diff
 ## 评审节奏
 
 | Proposal 类型 | 评审人 | 评审耗时上限 |
 |---|---|---|
 | 流程 / Gate 类（0001-0099） | 项目经理 + 技术 lead | 1 周内 |
 | 编码规范类（0100-0199） | 后端 lead + 前端 lead | 3 个工作日 |
 | 工具链类（0200-0299） | DevOps + 提出方 | 2 个工作日 |
 | 架构类（0300-0399） | 技术 lead + 必要 ADR | 2 周内 |
 
 超出上限 → 自动升到下次"流程 Sprint"必须决议。
 
+### Solo 模式简化（proposal 0040 引入）
+
+当团队规模 = `solo` 时，按 [Phase 05/06 §H / §I `[solo-review]`] 同样简化:
+
+- 评审耗时上限改为 "0 天（当日 OK）"
+- 评审人 = 提出方自评（commit message 必须带 `[solo-review]` 标签）
+- 同日 propose-accept-merge 不算违规，但同次 commit 必须含:
+  1) proposal 文件 (proposed → merged 双状态在 §修订记录 体现)
+  2) 实际规范文件 diff
+- 转入 small+ 后自动恢复双签 / 多签
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Claude 写新 proposal | §3 新增"写前 Read"checkbox，每次产新 proposal 必走 |
| proposals/README.md 状态索引 | 加 `partial` 状态显示；solo 评审节奏段在尾部加 |
| `0000-template.md` 模板 | §3 加必填校验段 |
| 03-开发/Sprint backlog.md | 新建文件 |
| Claude 处理"降级候选" | 默认在 Sprint backlog.md 加行；不再标"无目的地 code TODO" |

---

## 5. 风险

- **风险 1**: 元规则太多让写 proposal 变重。**缓解**: 5 处改动都是"加 1 个 checkbox"或"加 1 段说明"，不增加 proposal 写作步骤本身。
- **风险 2**: Sprint backlog.md 沦为"什么都往里塞"。**缓解**: §自进化降级判据 明示限制条件 + 优先级必填 + 预期 Sprint 字段强制估期。
- **风险 3**: partial 状态被滥用代替拆。**缓解**: §3.2 写明 "首选拆 / 次选 partial"，partial 状态在 README 状态索引中显式可见。

---

## 6. 备选方案

- A: 5 处各自独立 proposal (0030-0044) — 不选，同源同次反思应该 bundle (per F-META-03 即将定义的 bundle 判据本身递归适用)
- B: 不解 F-META-04 (不建 Sprint backlog)，让降级候选自然丢失 — 不选，丢失 = 反模式
- C (选定): 5 处 bundle 一提案，按 §3.1-§3.5 五段独立 diff

---

## 7. 实施计划

```
[x] Step 1: 写 proposal（本文件）— 2026-05-17
[x] Step 2: 评审 — 2026-05-17 [solo-review]（递归即时验证 F-META-05 新规则）
[x] Step 3: 落地 5 段 diff — 2026-05-17:
    - 99-跨阶段/proposals/0000-template.md (§3 加"写前 Read"校验段)
    - 99-跨阶段/proposals/README.md (生命周期加 partial / 文件命名加 bundle 判据 / 评审节奏加 solo 段)
    - 03-开发/Sprint backlog.md (新建, backfill 3 行 BL-2026-001/002/003)
[ ] Step 4: tracking 期看新 proposal 写作错误率 + bundle 决策可重现性 (W22+)
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| proposal 写后 scope 错率 | 2/20 = 10% (W19-W21) | ≤ 5% (W22+) |
| "merged → tracking (partial)" 状态使用次数 | 2 (硬塞) | 转为正式状态 + 显式拆解 |
| signals 候选 "降级 → backlog" 入 Sprint backlog 文件比例 | 0% (无文件) | 100% (W22+ 新降级必入文件) |
| Sprint backlog.md 待处理项数 | 0 → 3 (本提案 backfill 0023/0024/0026) | 每周 ≥ 1 项被 Sprint 吸纳 |

Tracking 期: merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | 5 friction 全有具体 commit/文件证据；本 proposal 既是规则、又是首次执行该规则的 demo（解 §3.5 同日 propose-accept-merge 路径）|
| Claude | ✅ 实施 | 2026-05-17 | 同日 5 段 diff 落地，含新建 03-开发/Sprint backlog.md |

> Solo 单签理由：本 proposal 5 处变更都是 README/template 级元规则，无业务影响、无外部依赖；W19-W21 三周连续 dogfood 后的事实 friction 总结，定义都基于已发生的 2 次 scope 错 + 2 次 partial 硬塞 + 3 次降级断头 + 100% 评审 SLA 失效 — 不存在"评审窗口能等到新发现"的可能。

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: W20 自进化 0040 apply (待 commit 后回填 hash)
- 实际 merged 日期：2026-05-17

### Tracking 数据

| 信号 | 基线 | 目标 | W21 (本次) | W22 | W23 |
|---|---|---|---|---|---|
| proposal scope 错率 | 2/20 = 10% (W19-W21) | ≤ 5% | rule 已加, 待下一份新 proposal 验证 | 待填 | 待填 |
| partial 状态正式使用 | 2 硬塞 (0013/0032) | 正式状态 | 已正式化 | 待填 | 待填 |
| signals 降级 → Sprint backlog 入文件率 | 0% (无文件) | 100% | **100%** (3/3 backfill BL-2026-001/002/003) | 待填 | 待填 |
| Sprint backlog 中 P0/P1 项每周被吸纳 | N/A | ≥ 1 | 0 (无 P0/P1, 3 项都是 P2/P3) | 待填 | 待填 |
| solo 单签 proposal 含"为何 solo 足够"段比例 | 0% (未要求) | 100% | 100% (本 proposal 已含, §9 段后注) | 待填 | 待填 |

Tracking 期: 2026-05-17 ~ 2026-05-31。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 reflect/2026-W20-self-evolution-process-meta 5 个 F-META friction bundle 升格 |
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + 5 段 diff 全部落地 (0000-template.md §3 / proposals/README.md 生命周期+文件命名+评审节奏 / 03-开发/Sprint backlog.md 新建)。状态 proposed → merged → tracking。本提案既定义了 solo same-day 规则，又首次执行该规则 — 递归元升级完成 |

# Reflect — YYYY-WW {周度闭合反思 / event-triggered ad-hoc}

> {1-2 句话场景描述: 周末闭合 OR 事件触发 ad-hoc OR meta 元反思 OR tracking 审计}
> 关联前述: {链 上一份 reflect / dogfood reflect / signals 当月 / 等}

---

## 头部

| 字段 | 值 |
|---|---|
| 周次 | YYYY-WW |
| 周窗口 | YYYY-MM-DD ~ YYYY-MM-DD |
| 执行者 | {Wjl / Wjl + Claude / 自动 (Phase B+) } |
| 关联 signals | [../signals/YYYY-MM.md](../signals/YYYY-MM.md) |
| 关联 ad-hoc reflects | {链 dogfood / audit / meta 等同周事件触发} |
| 上一份周报 | {链 YYYY-WW-1.md / 或"（无，本周首份）" } |

---

## 1. 观察（Observations）

### 1.1 量化数据（来自 git log + Gate 实例 + signals + 风险登记册）

| 指标 | 值 | 备注 |
|---|---|---|
| commit_total | {N} | {commit 类型分布: feat/fix/docs/refactor/perf/...} |
| commit_violation_count | {N} | commit-msg hook 拒收数 |
| commit_bypass_count (`--no-verify`) | {N} | {0 = 绿; > 0 → 触发 friction} |
| fix commits | {N} | {正文标题列表} |
| unique committer email | {N} | {solo / small+ 验证} |
| gate_instances_added | {N} | {分模块统计} |
| proposals_lifted | {N} | {分号段: 0001-99 流程 / 0100+ 编码 / 0200+ 工具链 / 0040+ meta} |
| proposals_applied | {N} | {merged → tracking 当周新增} |
| backlog_added | {N} | {Sprint backlog.md 当周新增 BL-YYYY-NNN} |
| backlog_completed | {N} | {当周流出 BL} |
| reflect_files_added | {N} | {当周新 reflect 文件数; ≥ 1} |

### 1.2 顺手的部分 ✅（≥ 3 处）

- **{现象}**: {1-2 句解释; 数据 / 文件证据}
- ...

### 1.3 不顺手的部分 ⚠️ — 找到 N 处 friction（≥ 3 处）

#### F-WW-01: {标题 - 一句话}

**现象**: {具体到 文件 / 段号 / 行号 / commit hash}

**影响**: {1-2 句, 数据或假设}

**根因** ({5 Whys 可选}): {根因句}

**风险**: {若不修, W22+ 会发生什么}

**修复路径**: → {转 proposal 0NNN / 转 BL-YYYY-NNN / 直接小改 / 观察}

---

## 2. 诊断（Diagnoses）

### 2.1 主诊断: {一句话, 多 friction 同源}

{展开 2-3 段, 含"5 Whys" 或"对比上一周" 或"对比 baseline"}

### 2.2 次诊断: {一句话}

{展开}

### 2.3 第三诊断: {一句话 / 可省略}

---

## 3. 行动（Actions）

### 3.1 本会话内可落地

| # | 行动 | 类型 | 落地点 |
|---|---|---|---|
| **A1** | {具体到 文件 + 段} | {proposal / BL / 直接改} | {file path / proposal NNNN} |
| ... | | | |

### 3.2 W{WW+1} (下周) 计划

| # | 行动 | 触发条件 |
|---|---|---|
| **B1** | {} | {} |
| ... | | |

### 3.3 月底 / 季度末 必做

| # | 行动 | 触发条件 |
|---|---|---|
| **C1** | {} | {} |

---

## 4. 元复盘 — 关于本反思自身（可选, 适用元层 reflect）

| 维度 | 值 |
|---|---|
| Findings 数 | {N} ({M} pass + {K} fail/需关注) |
| 修复同次 commit | {N} |
| 派生新 friction (留下周升格) | {N} |
| 反思耗时 | ~{N} min ({密度 = findings / min}) |

---

## 5. 链路

- 触发: {commit / 用户指令 / 事件}
- 衍生: {NN 个新 proposal 升格 / NN 条 BL 入 backlog / NN 个直接 patch}
- 关联: {上一份 reflect / 同期 dogfood / 当月 signals}
- 本份 deferred 候选: {NN 条留下周升格}

---

## 6. 一句话总结

**{1-2 句精炼总结, 含核心发现 + 下一步}**

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| YYYY-MM-DD | {作者} | 首次创建 |

---
name: pm-brainstorm
description: PLM 产品脑暴 — 问题空间探索 + 想法发散与收敛 + 假设拆解 + 风险盘点。当用户说"脑暴 / brainstorm / 探索需求 / 发散想法 / 5 Whys / 假设 / Jobs-to-be-Done / JTBD / 思考方向 / 没头绪"时调用. 输出: 01-立项/brainstorm/<topic>-<date>.md 脑暴纪要。**作为 product-manager agent 的子工具**, agent 收到模糊需求时优先调本 skill。
---

# pm-brainstorm — 产品脑暴 skill v0.1

**product-manager agent 的子工具**。当 PM agent 接到模糊需求 (e.g. "想做个监控大屏" / "用户嫌慢" / "竞品出了 X 功能") 而没有清晰下一步时,先调本 skill 走脑暴 → 收敛 → 才进 pm-prd-writer。

区别于 anthropic-skills:product-management:brainstorm (全局通用):
- 本 skill 绑 PLM 上下文: PRD-MAPPING.md / 路线图 / 风险登记册 / signals
- 输出格式与 Phase 01 立项 Gate §B 必产出物对齐
- 收敛阶段直接出"候选 PRD 大纲" 给 pm-prd-writer 接力

---

## 1. 何时调用

- 用户说 "脑暴 / brainstorm / 探索 / 发散" 等关键字
- product-manager agent §2.2 拆需求时若 AskUserQuestion 答案仍模糊 → 转本 skill
- 新模块立项前的"想清楚自己要什么"环节
- 竞品分析触发的"我们要不要做" 决策

不调:
- 需求已明确, 直接进 pm-prd-writer
- 单一选项决策 (转 AskUserQuestion)

---

## 2. 5 步工作流

### Step 1: 问题陈述 (1 句话)

用户原话 → 标准化为:
```
"For <用户角色>, <场景> 时, <痛点>, 期望 <结果>"
```

如:
- 用户原话: "想做个监控大屏"
- 标准化: "For 项目经理, 每天早会要看 6 模块状态时, 散在多处看不全, 期望 1 屏看到所有 active 模块的进度 + 风险 + 缺陷"

### Step 2: 5 Whys 挖根因

5 次反问 "为什么", 找根本驱动力:

```
Q1: 为什么要"1 屏看到所有 active 模块"?
A1: 早会要快速过状态

Q2: 为什么早会要快速过?
A2: 6 模块逐个看耗 30 min

Q3: 为什么逐个看耗时?
A3: 没有聚合视图

Q4: 为什么没有聚合视图?
A4: 之前每个模块独立, 没设计跨模块汇总

Q5: 为什么没设计跨模块汇总?
A5: 早期 1-2 模块够用, 没规划成熟期场景

→ 根因: 设计假设是 early 阶段, 现在转 stable 触发了新需求
```

### Step 3: 假设拆解

把"我以为是这样"显式列出:

| 假设 | 真伪需验证? | 验证方法 |
|---|---|---|
| PM 真的每天看 6 模块 | 是 | 问 1 个真实 PM |
| 30 min 是因为切换成本 | 是 | 量化测一下 (按时刷一遍) |
| 1 屏聚合能解决 | 否 (是产品决策不是假设) | — |

假设错 → 整个需求 invalid, 不必继续。

### Step 4: 发散 — 至少 5 个方向

不要早收敛。用 Crazy 8 (8 分钟出 5+ 方向) 或 SCAMPER (Substitute/Combine/Adapt/Modify/Put-to-other-use/Eliminate/Reverse):

```
方向 A: 聚合 SQL 视图 + 极简 HTML 表
方向 B: 复用 Phase 06 监控看板模板
方向 C: 改成"每日 5 点机器人推送早会摘要" (反向, 不做 UI)
方向 D: Element Plus 大屏组件 + 实时 ws
方向 E: 集成飞书机器人 daily summary
方向 F: 不做, 改 Sprint 计划会议形式
```

至少 5 个 (含 ≥ 1 个"不做"或"反向" 选项, 防过度承诺)。

### Step 5: 收敛 — 评估 + 输出候选 PRD 大纲

对发散方向打分 (业务价值 × 实现难度 × 时间成本 × 复用度):

| 方向 | 业务价值 | 难度 | 时间 | 复用度 | 总分 | 入选? |
|---|---|---|---|---|---|---|
| A | 7 | 3 | 1d | 8 | 23 | ⭐ 短期 |
| D | 9 | 7 | 5d | 5 | 16 | ⏳ 中期 |
| ... | | | | | | |

收敛输出: 1-2 个方向 + 它们的候选 PRD 大纲 (转 pm-prd-writer skill 接力)。

---

## 3. 输出文件

`01-立项/brainstorm/<topic>-<YYYY-MM-DD>.md`, 含:
- §1 问题陈述 (标准化句式)
- §2 5 Whys 根因
- §3 假设拆解表
- §4 发散方向 (≥ 5 个)
- §5 收敛评分 + 入选方向
- §6 候选 PRD 大纲 (短) — 喂给 pm-prd-writer

模板见 [references/template.md](references/template.md)。

---

## 4. 与其他 skill / agent 衔接

| 上游 | pm-brainstorm | 下游 |
|---|---|---|
| product-manager agent §2.2 模糊需求 | → 脑暴纪要 | → pm-prd-writer (写 PRD) |
| 竞品分析 / 用户反馈 | → 脑暴探索 | → pm-priority-matrix (排优先级 vs 其他需求) |
| reflect-monthly §"系统性 friction" | → 脑暴"该不该做长期 fix" | → /proposal skill (规范级 fix 走 proposal) |

---

## 5. 反模式

- ❌ 跳过 §2 5 Whys 直接发散 (根因不清, 发散方向飘)
- ❌ 跳过 §3 假设拆解 (基于伪假设的方向白做)
- ❌ §4 只列 1-2 方向 (没真发散, 是"找借口")
- ❌ §4 不含"不做 / 反向" 选项 (确认偏误, 总在做 UI 而不质疑 UI 是否需要)
- ❌ §5 收敛分数靠"感觉" (必量化 4 维)
- ❌ 输出不接力 pm-prd-writer (脑暴成空想)

---

## 6. 历史

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-19 | 首版; PM agent 配套 4 skill 之一; 5 步流程 (问题陈述 / 5 Whys / 假设拆解 / 发散 ≥ 5 / 收敛量化) |

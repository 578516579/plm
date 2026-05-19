---
name: pm-priority-matrix
description: PLM 优先级矩阵 — RICE / WSJF / MoSCoW 三种量化方法择一对一批需求排序. 当用户说"排优先级 / RICE / WSJF / MoSCoW / 这几个先做哪个 / 哪些进下周 sprint / 优先级矩阵"时调用. 输出: 01-立项/<batch>-priority-matrix-<date>.md 含 表格 + 推荐执行顺序. **product-manager agent 的子工具** — agent §2.3 优先级排序时调本 skill。
---

# pm-priority-matrix — 优先级矩阵 skill v0.1

**product-manager agent 的子工具**, 主走 PM agent §2.3 优先级排序职责。

核心原则: **必量化, 不接受"感觉重要"**。3 种方法择一, 用户用 AskUserQuestion 选。

---

## 1. 何时调用

- 用户说 "排优先级 / 这几个先做哪个 / 下个 sprint 做啥"
- product-manager agent §2.3 触发
- 月初路线图 Now/Next/Later 切换
- Sprint 计划前必走 (新候选 vs 现 backlog 排序)

---

## 2. 3 种方法对比 — 选用指南

```
用户场景 → 选哪个?
│
├─ 多需求批量排序 + 想看哪些 ROI 最高 → RICE
├─ 含成本/时间紧迫性 (大项目延期) → WSJF (Weighted Shortest Job First)
└─ release 范围决策 (本期做哪些) → MoSCoW (Must/Should/Could/Won't)
```

### 2.1 RICE 公式

```
RICE Score = (Reach × Impact × Confidence) / Effort

Reach        : 影响多少用户 / 多少场景 / 月触发次数 (绝对数)
Impact       : 1-3 大 / 0.5 中 / 0.25 小 (单用户影响)
Confidence   : 100% / 80% / 50%  (置信度)
Effort       : 人天 / 人月 (成本)
```

排序: 分数高在前。RICE 偏 "ROI 视角"。

### 2.2 WSJF (Weighted Shortest Job First, SAFe)

```
WSJF = (业务价值 + 时间紧迫性 + 风险规避或机会启动) / 故事点

业务价值       : 1-10 (定性 + 团队对齐)
时间紧迫性     : 1-10 (1 = 拖一年没事 / 10 = 拖 1 周就完)
风险规避/机会 : 1-10 (1 = 不做也行 / 10 = 不做有重大风险或丢机会)
故事点         : Fibonacci 1/2/3/5/8/13 (估算成本, 不是人天)
```

排序: 分数高在前。WSJF 偏 "把短工期高价值的活先做"。

### 2.3 MoSCoW

每需求标:
- **M** Must (本期必做, 不做 = release 取消)
- **S** Should (本期应做, 不做也能 release 但价值打折)
- **C** Could (本期能做就做, 不做下期接力)
- **W** Won't (本期明确不做, 记录为 Later)

不打分, 4 类直接决定 release scope。MoSCoW 偏 "划线决定本期范围"。

---

## 3. 5 步工作流

### Step 1: 列候选

输入候选: 来自 backlog / brainstorm / 用户列表 / 反馈聚合。

每个候选必须含: 1 句话描述 + 大致 effort 估计。

### Step 2: AskUserQuestion 选方法

```
"批量排序 N 个候选 (N=...), 选哪种优先级方法?"
- RICE: ROI 视角, 适合发散候选选 top-K
- WSJF: 含时间紧迫性, 适合大项目排队
- MoSCoW: release 范围决策, 适合"本期做哪些"
```

### Step 3: 逐候选打分 (按方法填表)

对每候选, AskUserQuestion 让用户给值 (不接受"凭感觉"):

```
候选 1: <描述>
  Reach (用户/场景数): ___
  Impact (1-3/0.5/0.25): ___ + 依据 (1 句)
  Confidence (100/80/50%): ___ + 依据
  Effort (人天): ___ + 估算依据
```

或一次性把所有候选列在表里, 用户填全。

### Step 4: 算分排序

输出表格:

```
| # | 候选 | Reach | Impact | Confidence | Effort | RICE Score | 排名 |
|---|------|-------|--------|------------|--------|------------|------|
| 1 | A    | 100   | 1      | 80%        | 5      | 16         | 1 ⭐ |
| 2 | B    | 50    | 0.5    | 100%       | 3      | 8.3        | 2    |
| 3 | C    | 1000  | 0.25   | 50%        | 10     | 12.5       | 3    |
```

WSJF 同理: 用 (BV + TC + RR) / SP 公式。

MoSCoW: 表格分 4 类 + 1 句话依据。

### Step 5: 推荐执行顺序 + 备注

输出:
- Top-K 候选 (按用户期望容量, 如本月 5 项)
- 不入选候选 → 流向: 下个 Sprint / 入 backlog / retire
- 风险标注: 高分但高风险候选用 ⚠️ 标
- 与现有 Sprint backlog (BL-2026-NNN) 的关系: 是新增 / 替换 / 合并

---

## 4. 输出文件

`01-立项/<batch>-priority-matrix-<YYYY-MM-DD>.md`, 含:
- §1 候选清单 (N 个)
- §2 方法选定 (RICE/WSJF/MoSCoW + 选定理由)
- §3 评分表 (含每维度依据)
- §4 排序结果 + Top-K
- §5 推荐执行顺序 + 与现有 backlog 整合
- §6 不入选去向

---

## 5. 与其他 skill / agent 衔接

| 上游 | pm-priority-matrix | 下游 |
|---|---|---|
| pm-brainstorm 收敛 N 方向 | → 排序 → Top-K | → pm-prd-writer (Top-K 进 PRD) |
| product-manager agent §2.3 | → 矩阵 | → 路线图 Now/Next/Later 更新 |
| 月初 backlog 大批量 | → 重排 | → Sprint 计划 (product-management:sprint-planning skill) |
| reflect-monthly 跨周 friction | → 加权打分 | → /proposal skill 升格高优先 |

---

## 6. 反模式

- ❌ "感觉 A 比 B 重要" 而不打分 (违反量化原则)
- ❌ 选了 RICE 但不填 Confidence (3 维都得有)
- ❌ MoSCoW 全标 Must (划线无意义, 必须有 W)
- ❌ 不写每维度评分依据 (回看 1 周后看不懂)
- ❌ Top-K 不输出"不入选去向" (候选丢失)
- ❌ 不与现有 BL-2026-NNN 整合 (双轨)

---

## 7. 历史

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-19 | 首版; PM agent 配套 4 skill 之四 (终); 3 方法 RICE/WSJF/MoSCoW + 5 步流程 + 反模式守门 |

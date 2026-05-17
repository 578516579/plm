# Reflect — 自进化过程本身的元反思 (W20 末)

> Phase A 自进化基础设施跑了 2 周（W19 dogfood + W20 W21 批量），产出 20 个 proposal。
> 这是 dogfood reflect 的**升级**：不是反思业务模块，而是反思"自进化循环本身"是否健康。
> 关联前述: [2026-W20.md (W20 闭合)](2026-W20.md) / [phase01-dogfood](2026-W20-project-phase01-dogfood.md) / [phase03-dogfood](2026-W20-project-phase03-dogfood.md)

---

## 头部

| 字段 | 值 |
|---|---|
| 触发场景 | 本会话连续跑 4 commit 自进化（W20 闭合 → W20 apply → W21 lift → W21 apply）后，发现循环自身有质量盲区 |
| 执行者 | Wjl + Claude |
| 执行日期 | 2026-05-17 (W20 末) |
| 关联 commits | `9e1afe6` / `8b2882d` / `12e7e14` / `2096174` |
| 数据时段 | 2026-05-15 ~ 2026-05-17 (W19 末 + W20 全周) |

---

## 1. 观察（Observations）

### 1.1 量化数据

| 指标 | 值 | 备注 |
|---|---|---|
| 总产出 proposal | 20 | W19 6 + W20 7 + W21 7 |
| 状态 merged → tracking | 13 (10 full + 3 partial) | 65% 落地率 |
| 状态 proposed (待 apply) | 3 | 0100/0101/0200 — touch 高敏感文件 |
| 同日 propose-accept-merge 占比 | 17/20 = 85% | 仅 0027/0028/0029 是 retro 模式 |
| Bundle proposal 数 | 4 (0008/0013/0016/0032) | 平均 bundle 因子 = 13/4 ≈ 3.25 候选/proposal |
| Scope 错误 proposal 数 | 2 (0013 §D + 0016 §3.4) | apply 时才发现写错段号 |
| 候选堆积 → Sprint TODO 下沉 | 3 (0023/0024/0026) | 没有实际"Sprint TODO 文件"承接 |

### 1.2 顺手的部分 ✅

- **Same-day propose-accept-merge** 节奏在 solo 模式下非常顺：W20 / W21 各一日完成"提案 → 评审 → 落地"，没有评审等待期空转
- **Bundle 提案策略** 大幅减少文件数：13 候选合 4 proposal，避免编号爆炸
- **Retro 模式 (User-requested-bypass)** 解决了 silent merge 反模式：0027/0028/0029 都按 rules.md §L.2 例外条款补录
- **维度参数化** 是真的有用：Phase 01-06 模板从 1 维 (L级) 演化到 4 维 (L级 × 类型 × 规模 × 成熟度)，每加 1 维都消解一类 friction
- **proposals/README.md 状态索引 + 候选堆积分区** 让全景可一眼看清，比单独翻 20 个文件强

### 1.3 不顺手的部分 ⚠️ — 5 处过程质量盲区

#### F-META-01: proposal 写错段号没人卡控

**现象**: 0013 §3.3 写"改 Phase 01 §D 风险识别"，但 Phase 01 §D 是"评审记录"; 0016 §3.4 写"改 §C API"，但 Phase 02 §C 是"DoD"。两处 scope 错位都在 apply 时才发现，靠 Claude 手动 read 才纠正。

**影响**: 如果在 review 期就发现，可以早改 proposal；apply 时发现意味着 proposal 文件留下"原 scope 错"的痕迹，回看不知道哪个是对的。

**根因**: proposal 模板 §3 "改动文件清单 + Diff 草案"段没要求**写 proposal 前先 read 目标文件**，作者凭记忆写段号。

#### F-META-02: "partial merged" 状态在状态机中不存在

**现象**: 0013 / 0032 这次都标 "merged → tracking (partial)"，但 [proposals/README.md §生命周期](../proposals/README.md) 的状态列表是: draft / proposed / accepted / implementing / merged / tracking / done / rejected / superseded — **没有 partial**。我硬塞了一个非标准状态。

**影响**: 未来批量统计"merged 数" 时，partial 算不算？回看 0013 不知道剩下的 §D 拆表谁负责什么时候做。

**根因**: 没设计 "部分 merge" 的语义：是不是该拆成两个 proposal (0013-a 头部 + 0013-b §D)？还是状态机加 "partially-merged" 状态？

#### F-META-03: bundle vs 拆分决策没有规则

**现象**: 我做了 4 个 bundle proposal，但每次决策都基于"我觉得 X 和 Y 是一回事"凭感觉。没有书面的"什么时候 bundle"判据。比如 0022/0025 我最终拆成 0100/0101 (不 bundle)，但同源都是 "Phase 03 实现细节"。

**影响**: 下一个人 (或下一次 Claude) 会做不同决定；bundle 边界飘移；proposal 文件粒度难以预测。

**根因**: proposals/README.md 文档化了号段（0001-0099 流程 / 0100-0199 编码规范 / ...），但没文档化"bundle 判据"。

#### F-META-04: "降级为 Sprint TODO" 没承接介质

**现象**: 0023/0024/0026 三个候选被我标"非规范变更，降级为 Sprint backlog code TODO，不走 proposal"。但 PLM 当前没有 `03-开发/Sprint 计划与回顾/` 实际的 Sprint backlog 文件 (目录都还没建)，这 3 个候选实际"没人接"。

**影响**: signals 候选标 [→ code TODO] 但没有目的地 = 丢失。下次会话谁还记得"0024 是个 Task 看板分页优化"？

**根因**: 自进化机制设计了 "升格 → proposal" 通道，但没设计 "降级 → backlog" 通道。

#### F-META-05: review 期/tracking 期参数全是默认值

**现象**: 6 个 proposal (0001-0006) tracking 期 = "merged 后 2-4 周"; 后续都套用 2 周; 各种 proposal 评审截止"1 周 / 3 天 / 2 天" 写得很认真，但 solo 模式同日就 accept 了，**评审 SLA 100% 失效**。

**影响**: 模板里的"评审节奏"段成了形式主义；新读者会以为这里真的有评审窗口。

**根因**: proposals/README.md §评审节奏 表是按"中型团队"标准设计的，没有 solo 模式特殊路径（虽然 0002 已经按"团队规模"调了 Gate 签字，但 proposal 评审节奏没同步调整）。

### 1.4 整体感受

W19 → W20 → W21 三周自进化运行：
- W19: 第 1 轮探索，3+3 proposal （正向验证机制可行）
- W20: 第 2 轮规模化，7+7 proposal （证明能批量处理）
- W21 (本会话): 第 3 轮收敛，4 apply + 3 deferred (开始触及"过程本身的盲区")

→ **现在是从"建机制"过渡到"优化机制"的拐点**。如果不修过程盲区，机制本身的可信度会下降（partial 状态混乱 / scope 错没人查 / 降级候选丢失）。

---

## 2. 诊断（Diagnoses）

### 2.1 主诊断: 自进化机制的"元规则"层未建

5 个盲区都是**关于自进化机制本身**的规则缺口，不是关于业务 Gate 模板的规则缺口。

类比代码：业务代码 (Gate 模板) 已经有 4 维参数化，"框架代码" (proposal 机制) 还停在 v1 baseline，没和业务代码同步演化。

### 2.2 次诊断: 写 proposal 时"先读后写"约束缺失

F-META-01 (scope 错) 的根因是 Claude 凭记忆 / 上次读到的段号写新 proposal。proposal 模板 §3 应该要求作者**先 Read 目标文件再写 diff**，类似代码里"先 read 再 edit"的纪律。

### 2.3 第三诊断: "降级 → backlog" 通道存在但没有终点

F-META-04 暴露了通道断头：signals → reflect → proposal 是闭环；但 signals → reflect → backlog 没有 backlog 文件，所以"降级"就是丢弃。

---

## 3. 行动（Actions）

### 3.1 本会话内可落地

| # | 行动 | 类型 | 转 Proposal? |
|---|---|---|---|
| **A1** | 升格 F-META-01 → 新 proposal: proposal 模板 §3 加 "写前先 Read 目标文件" 必填 checkbox | 流程 | → **0030** (新编号，0030 段已占用作 0200 派生标记，用 0040？） |
| **A2** | 升格 F-META-02 → 新 proposal: 状态机加 "partially-merged" 状态 + 拆解策略 | 流程 | → **0041** (or 同 0040 bundle) |
| **A3** | 升格 F-META-03 → 新 proposal: proposals/README.md 加 "bundle 判据" 段 | 流程 | → **0042** (or bundle) |
| **A4** | 升格 F-META-04 → 新 proposal: 创建 `03-开发/Sprint backlog.md` 作为降级通道终点 | 流程 + 文档 | → **0043** (or bundle) |
| **A5** | 升格 F-META-05 → 新 proposal: proposals/README.md §评审节奏 加 solo 模式特殊段 | 流程 | → **0044** (or bundle) |

5 个 friction 都是同源 (proposal 机制 v1 → v2)，可以**bundle 为单个 proposal 0040 "self-evolution v2: proposal 机制元规则升级"**。

### 3.2 deferred (W22+)

- B1: 实际 apply 0040 (改 proposals/README.md + proposal 模板)
- B2: tracking 0040 至 W23-W24
- B3: 0100/0101/0200 (W21 deferred) 在 0040 元规则升级后再 apply (确保 scope check)

---

## 4. 元复盘 — 关于本元反思自身

第 1 次元反思的元复盘 (递归 1 层):
- **节奏**: dogfood reflect ≈ 2h；周末 W20 reflect ≈ 1h；本 meta reflect ≈ 30 min (因为 5 个 friction 都已在本会话上下文中明显可见)
- **质量信号**: 5 个 friction 都有具体 commit / 文件证据，不是"感觉" → 满足 [reflect/README.md](README.md) "有数据 / 有事实" 约束
- **预期效果**: 0040 落地后，proposal 写作错误率从 ~10% (2/20) 降到 ≤ 5%；partial 状态有正式归宿

---

## 5. 链路

- 触发: 本会话 4 commit 自进化批次
- 衍生: 1 个 bundle proposal 0040 (合并 5 个 F-META candidates)
- 关联: [2026-W20.md](2026-W20.md) 已识别"信号产/处理速率"问题，本反思补"过程质量"维度

---

## 6. 一句话总结

**自进化机制连续运行 2 周后，开始触及"机制本身的盲区"。5 处过程质量 friction (scope 错 / partial 状态 / bundle 判据 / 降级通道 / 评审节奏) 表明现在是建 v2 元规则的时候。bundle 为单提案 0040 处理。**

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首次创建（自进化机制元反思 — 反思反思机制本身）|

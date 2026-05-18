# Reflect — 周/月/季度反思报告

自进化机制的**处理引擎**。读取 [signals](../signals/) → 分析模式 → 输出 [proposals](../proposals/) 的种子。

---

## 三种节奏

| 节奏 | 文件命名 | 调度 | 主要输入 | 主要输出 |
|---|---|---|---|---|
| **周度** | `YYYY-WW.md` | `/loop 7d /reflect-weekly` 每周一 09:00 | 上周 git log + Gate 实例新增 + 风险变动 | 3 条具体改进建议 |
| **月度** | `YYYY-MM.md` | `/loop 30d /reflect-monthly` 每月 1 号 | 上月 signals 7 类数据 | 月度流程健康度报告 + 触发 proposals |
| **季度** | `YYYY-Q[1-4].md` | 手动跑 `/reflect-quarterly` | 上季度全部周报+月报 + ADR + 规范文档 | 规范一致性审计 + 大方向调整 proposals |

> Phase B 之前：手动跑（按下方模板写）。Phase B 之后：上述命令自动跑。

---

## 周度反思（`/reflect-weekly`）— 工作模式

> **Phase B v0.1 已上线**（2026-05-17）→ [`.claude/skills/reflect-weekly/SKILL.md`](../../.claude/skills/reflect-weekly/SKILL.md)
> 调用方式：用户说 `/reflect-weekly` / "周度反思" / "周报反思" / "W{NN} reflect" → Claude 自动加载 skill 走 Step 1-7.

每周一上午（或事件触发后 24h 内），Claude 按 skill 执行：

1. **扫输入**（见 [skill checks.md §A](../../.claude/skills/reflect-weekly/references/checks.md) 5 类信号 Bash）
   - `git log --since="$WEEK_START" --until="$WEEK_END"` (commit 健康度)
   - `find 99-跨阶段/gate-checklists/instances` (Gate 实例新增)
   - `git log -- 99-跨阶段/风险登记册.md` (风险变动)
   - signals / proposals / Sprint backlog 当周流动
2. **找模式**（对照 [skill checks.md §B](../../.claude/skills/reflect-weekly/references/checks.md) 6 种 friction）
3. **写报告**（用 [skill references/template.md](../../.claude/skills/reflect-weekly/references/template.md) 模板）
4. **显式呼出候选**（每条 friction 必标 → proposal / BL / 直接改 / 观察, 防"心理按摩"反模式）

人类干预（solo 模式下精简）：审报告 → 通过的建议**当次同 commit** 升格为 proposal（per [proposal 0040 §3.5](../proposals/0040-self-evolution-v2-meta-rules.md) solo same-day 路径）。

**示例参考**（按场景）：[skill examples.md](../../.claude/skills/reflect-weekly/references/examples.md) 列出 4 类模式 (周末闭合 / dogfood / meta / audit)。

---

## 月度反思（`/reflect-monthly`）— 工作模式

每月 1 号自动跑：

1. 触发 [signals](../signals/) 数据采集
2. 综合 4 周周报 + 当月 signals
3. 评估"规范健康度"（每条 MUST 规则是否被频繁绕过 / 是否完全无触发）
4. 输出月报，标记哪些规则**长期 0 触发**（可能可以删/简化）和哪些**频繁违反**（可能不合理）

---

## 季度反思（`/reflect-quarterly`）— 工作模式

每季度末手动跑（一般是 Sprint 周期外的"流程 Sprint"）：

1. 综合 12 周周报 + 3 月月报
2. 读 [ADR/](../../03-开发/ADR/) 所有 accepted 的决策，对照实际执行情况
3. 读三份核心规范文档（[开发规范.md](../../03-开发/开发规范.md) / [模块工作流.md](../模块工作流.md) / [.claude/rules.md](../../.claude/rules.md)），找：
   - 互相矛盾的条目
   - 半年没用到的条目
   - 频繁踩坑但规范没覆盖的盲区
4. 输出"季度规范重构建议"

---

## 反思的"价值闭环"约束

每份反思报告**必须**有以下 3 段：

1. **观察**：上周/月/季度发生了什么（数据 + 事实）
2. **诊断**：为什么发生（根因，不是表象）
3. **行动**：要改什么（具体到文件 + 行号 + diff，或转成 proposal 编号）

不写"诊断"和"行动"段 → 反思无效，下次会议要补。

---

## 工具与脚本

| 工具 | 用途 | 状态 |
|---|---|---|
| [`/reflect-weekly`](../../.claude/skills/reflect-weekly/SKILL.md) | 周度反思（半自动: 数据采集自动 + 写报告半人工）| ✅ v0.1 (2026-05-17) |
| [`/reflect-monthly`](../../.claude/skills/reflect-monthly/SKILL.md) | 月度反思 + tracking 7 步终结判定 + MUST/SHOULD 规则健康度审计 | ✅ v0.1 (2026-05-17) |
| [`/reflect-quarterly`](../../.claude/skills/reflect-quarterly/SKILL.md) | 季度反思 + ADR 6 维审计 + 跨文档 4 维 coherence + 重构建议 | ✅ v0.1 (2026-05-17) |
| [`/signals-collect`](../../.claude/skills/signals-collect/SKILL.md) | 7 类信号自动采集 → supplementary 文件 (Phase D 输入基础设施) | ✅ v0.1 (2026-05-17) |
| `/proposal` | 候选 → proposal 一命令全链路 | ⏳ Phase C 待 |
| `/loop` skill（系统级，已有）| 调度上述命令 | — |

---

## 反模式

- ❌ 反思报告只有"做得好" + "改进点"两段话，没有数据、没有具体行动
- ❌ 月报标"流程一切都好"但 signals 显示 bypass 次数 > 0
- ❌ 反思建议从来不转 proposal → 反思变成"心理按摩"

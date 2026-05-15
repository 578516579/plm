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

每周一上午，Claude 自动执行：

1. **扫输入**
   - `git log --since "1 week ago" --pretty=format:"%h %s"`（看 commit 规范）
   - `find gate-checklists/instances -newer .git/last-reflect.timestamp`（看新增 Gate）
   - `git log --since "1 week ago" -- 99-跨阶段/风险登记册.md`（看风险变动）
   - 上周会话的 memory 摘要
2. **找模式**（对照 [signals/README.md](../signals/README.md) 的 5 类触发条件）
3. **写报告**（用 `YYYY-WW.md` 模板）
4. **生成种子提案**（可选：在报告"建议"段，标 `→ 可转 proposals/XXXX`）

人类干预：审报告 → 通过的建议手动转 proposal（或让 `/proposal` skill 自动转）。

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

| 工具 | 用途 |
|---|---|
| `/reflect-weekly` | Phase B 实现，自动周报 |
| `/reflect-monthly` | Phase B 实现，自动月报 |
| `/reflect-quarterly` | Phase B 实现，手动触发的季度报告 |
| `/loop` skill（已有）| 调度上述命令 |

---

## 反模式

- ❌ 反思报告只有"做得好" + "改进点"两段话，没有数据、没有具体行动
- ❌ 月报标"流程一切都好"但 signals 显示 bypass 次数 > 0
- ❌ 反思建议从来不转 proposal → 反思变成"心理按摩"

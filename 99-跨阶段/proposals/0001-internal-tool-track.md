# Proposal 0001: 引入"项目类型"维度，为内部工具 / 框架升级提供差异化 Gate 路径

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0001 |
| 标题 | 引入"项目类型"维度（外部产品 / 内部工具 / 框架升级） |
| 状态 | **merged** → tracking |
| 类型 | 流程 |
| 提出人 | Wjl + Claude（来自 Project Phase 01 dogfood） |
| 提出日期 | 2026-05-15 |
| 评审人 | 项目经理 + 技术 lead（小团队下：Wjl 自评 + 标注） |
| 评审日期 | TBD（建议 Phase 02 启动前） |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Gate Checklist 模板设计时默认受众是"面向外部用户的产品"。但 PLM 本身是内部工具，Project 模块 dogfood 时发现：

- 市场调研段（B.1）要求"TAM/SAM/SOM 量化" → 内部工具无意义，要"翻译"语义
- 商业计划段（B.3）要求"收入预测" → 内部工具按"工时节约"算，需要重定义模板
- 关键假设里"市场规模"维度全部 N/A

也就是说，**整个 Phase 01 Gate 在内部工具语境下要做大量"心理翻译"**。

## 2. 证据

- 关联 reflect 实例: [../reflect/2026-W20-project-phase01-dogfood.md](../reflect/2026-W20-project-phase01-dogfood.md) §2.1（模式 #1）→ F5
- 关联 Gate 实例: [../gate-checklists/instances/project/Phase01-立项-Gate-2026-05-15.md](../gate-checklists/instances/project/Phase01-立项-Gate-2026-05-15.md) E 段（F5 豁免）
- 关联 产出物: [01-立项/Project-市场调研.md](../../01-立项/Project-市场调研.md) 开头标"⚠️ 内部工具语境简化版"
- 频次：本次 Phase 01 中触发 1 次。后续 Phase / Task / Requirement 等模块都是内部用，预计**100% 都会踩**

## 3. 提案

引入"项目类型"作为 Gate Checklist 的一阶维度，与 L1/L2/L3 分级正交：

| 类型 | 典型例子 | 必产出物差异 |
|---|---|---|
| **外部产品 (`external-product`)** | 面向用户的 SaaS / App | 全套：市场调研含 TAM/SAM/SOM、商业计划含收入预测、用户访谈 ≥ 5 段 |
| **内部工具 (`internal-tool`)** | PLM、CRM、运维平台 | 简化：市场调研改为"用户痛点调研 + 已有工具对比"、商业计划改为"ROI 工时节约模型"、用户访谈"覆盖所有相关角色"（不强求 5 段） |
| **框架升级 (`framework-upgrade`)** | 技术栈大版本升级、基础库换代 | 跳过：市场调研 / 商业计划；强化"技术调研 + 影响面评估 + 回滚方案" |

### 改动文件清单

| 文件 | 改动类型 |
|---|---|
| `gate-checklists/README.md` §分级 | 新增"二级维度：项目类型"段，定义 3 个值 |
| `gate-checklists/Phase01-立项-Gate.md` 头部 | 新增"项目类型"字段，每个 B 子项标注"适用类型" |
| `gate-checklists/Phase02-...md` ~ `Phase06-...md` 头部 | 同上（轻量改） |
| `gate-checklists/instances/README.md` 表格 | 列加一项"类型" |
| `99-跨阶段/模块工作流.md` §强制执行层 | 提及二级维度 |

### Diff 草案

`gate-checklists/Phase01-立项-Gate.md` 头部修改：

```diff
 | 分级 | L1 / L2 |
+| 项目类型 | external-product / internal-tool / framework-upgrade |
 | Owner（产品） | _名字_ |
```

`gate-checklists/Phase01-立项-Gate.md` B.1 市场调研段：

```diff
 ### B.1 市场调研

 - [ ] 文件：`01-立项/<模块>-市场调研.md`（基于 [模板](../../01-立项/市场调研.md)）
-- [ ] 含：目标市场规模（TAM / SAM / SOM）
-- [ ] 含：至少 3 个竞品的功能矩阵
-- [ ] 含：至少 5 个用户访谈纪要 / 替代证据
-- [ ] 含：明确结论（值得做 / 边做边看 / 不值得做）
+- [ ] 含：（仅 external-product）目标市场规模（TAM / SAM / SOM）
+- [ ] 含：竞品 / 替代工具对比 — external 强制 ≥ 3 个，internal 强制 ≥ 3 个，framework-upgrade 可省
+- [ ] 含：用户访谈纪要 — external ≥ 5 段，internal "覆盖所有相关角色"（≥ 3 段），framework-upgrade 替换为"受影响开发者访谈"
+- [ ] 含：明确结论（值得做 / 边做边看 / 不值得做）
```

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 / PM | 走 Gate 时多填 1 个字段（项目类型）；按类型自动豁免不适用的子项 |
| Claude | rules.md §G 判级逻辑要加二级维度，未来 `/feature` skill 也按类型自动设占位 |
| 已有 Gate 实例 | Project 模块的 Phase 01 Gate 实例已签字 → 不重新审；下一份（Phase 02）按新模板填 |
| 模板文件 | 6 个 PhaseNN-Gate.md 模板 + README + 模块工作流 总计 9 个文件改动 |

## 5. 风险

| 风险 | 缓解 |
|---|---|
| 二级维度让模板更复杂，阅读理解成本上升 | 在 README 用一张速查表说明"类型 × 分级"组合 |
| 类型 hardcode 在模板里，未来新类型（如 internal-API）要再改一遍 | 接受。新增维度本来就要走 proposal |
| 历史实例没有"类型"字段，迁移混乱 | 不迁移，新模板生效起的实例才有"类型"字段 |

## 6. 备选方案

- **方案 A（本提案）**：二级维度"项目类型"，与 L1/L2/L3 正交
- **方案 B**：纯靠 L1/L2/L3 分级，把"内部工具"塞 L2 简化指引段 → 简单但不够明确，仍会 friction
- **方案 C**：每种类型一套独立 Checklist 模板（6 个 × 3 类型 = 18 份）→ 维护爆炸，否决

选 A 因为：扩展性好、改动可控、与分级正交清晰。

## 7. 实施计划

```
[ ] Step 1: 本 proposal 状态 → accepted（评审通过）
[ ] Step 2: Claude 改 6 个 PhaseNN-Gate.md 模板 + README + 模块工作流（写 PR）
[ ] Step 3: 在 [开发规范.md §0 命名总纲] 加"项目类型"字段说明
[ ] Step 4: 在 [.claude/rules.md §G.2 分级判断] 加二级维度判断逻辑
[ ] Step 5: 合 main，commit message: `refactor(rules): add project-type dimension to gate checklists (proposal 0001)`
[ ] Step 6: 进入 tracking 期（2 周）
```

## 8. 衡量指标

Tracking 期内观察：

- 信号 1：下一份 Gate 实例的 E 段豁免数 — 基线 = 3（Project Phase 01）→ 目标 ≤ 1
- 信号 2：用户 / Claude 在 Gate 操作中对"语义翻译"的吐槽数 — 基线 = 1（本次）→ 目标 0
- 信号 3：模板复杂度（行数）— 基线 = 9 文件 × 平均 X 行 → 目标 ≤ +10% 总行数

跟踪期：accepted 当周 + 2 周。

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl (产品/技术 lead) | ✅ 通过 | 2026-05-15 | solo-review，证据扎实（来自 Project Phase 01 dogfood F5）|

## 10. 实施后跟踪

### 实际 PR / commit
- PR: N/A（直接 commit）
- 合入 commit: `refactor(gate): apply proposals 0001/0002/0003 …`（2026-05-15）
- 实际 merged 日期：2026-05-15

### Tracking 数据（merged 后 2 周观察期）

| 信号 | 基线 | 目标 | 实际（W21）| 实际（W22）|
|---|---|---|---|---|
| 下一份 Gate 实例的 E 段豁免数 | 3（Project Phase 01）| ≤ 1 | _待 Phase 02 完成时填_ | _待_ |
| 用户 / Claude 对"语义翻译"吐槽数 | 1 | 0 | _待_ | _待_ |
| 模板总行数增长 | 9 文件 X 行 | ≤ +10% | _待统计_ | _待_ |

跟踪期：2026-05-15 → 2026-05-29（W20→W22）。

### 最终判定
- [ ] done
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-15 | Wjl + Claude | 初版 |

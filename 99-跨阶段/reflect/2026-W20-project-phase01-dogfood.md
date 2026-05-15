# Reflect — Project 模块 Phase 01 Dogfood 复盘

> 这是 Phase A 自进化基础设施落地后的**首次实战使用反馈**。不是周度自动反思，是事件触发的 ad-hoc reflect。
> 等 Phase B 实现 `/reflect-weekly` 之后，类似的反馈会自动汇入周报。

---

## 头部

| 字段 | 值 |
|---|---|
| 触发场景 | Project 模块走完 Phase 01 立项 Gate（dogfood） |
| 执行者 | Wjl + Claude（结对操作） |
| 执行日期 | 2026-05-15 |
| 关联 commit | `c6c1bb5 docs(gate): project phase 01 passed` |
| 关联 Gate 实例 | [../../gate-checklists/instances/project/Phase01-立项-Gate-2026-05-15.md](../gate-checklists/instances/project/Phase01-立项-Gate-2026-05-15.md) |

---

## 1. 观察（Observations）

### 1.1 顺手的部分 ✅

- **Checklist B 段（必产出物）的"清单逼迫感"奏效**：看着勾不动，会逼自己把 PRD / 市场调研 / 商业计划 / 评审纪要都补出来。没有这个清单的话，很容易"PRD 写了就当立项过了"。
- **E 段（异常 / 例外）救场**：当某项不能完美达成时，能正大光明写"豁免理由 + 补救计划"，不用造假数据。这是务实设计。
- **文件命名规范 + 引用路径明确**：`<模块>-PRD.md`、`PhaseNN-阶段名-Gate-<日期>.md`、Checklist 内链产出物——查证时极容易。
- **双签字位 + 修订记录**：审计感强，未来回看历史能 traceable。

### 1.2 不顺手的部分 ⚠️（找到 10 处具体 friction）

| # | 现象 | 实际影响 |
|---|---|---|
| **F1** | 单人项目 L1 全签字位（业务/产品/技术/设计/CFO 5 个角色）有形式主义味道 | 我自己签了 3 个、N/A 了 2 个。读者看会觉得"这评审是假的" |
| **F2** | B.1 "至少 5 段用户访谈"对小团队过严 | 团队 < 10 人时，强凑 5 段会重复或编造。豁免到 E 段感觉不对劲 |
| **F3** | "评审材料提前 2 个工作日"对快节奏 / 单人项目过严 | 单人项目当天就能做完，卡 2 天反而拖。豁免到 E 段 |
| **F4** | E 段豁免一次就 3 条（F1+F2+F3 都在 E 段）→ 模板对该情境过严 | "豁免过多"信号——模板可能本就不适配单人 / 小团队 |
| **F5** | 市场调研模板默认对面向用户的产品而设，内部工具要"翻译"语义 | 我在文档头加了"⚠️ 内部工具简化版"声明。Checklist 没明示哪些字段对内部工具豁免 |
| **F6** | 风险登记册手动同步繁琐 | Checklist 提"识别风险录入风险登记册"，但要分别手动改两个文件 + commit 时记得带上。容易漏 |
| **F7** | "已排上 OKR"打勾时，OKR 文件还是空模板 | OKR 还没填，无处可"打勾确认"。形式上勾了，实际没核实 |
| **F8** | 产出物路径明确但要手动创建文件 | 4 个产出物文件全手敲。Phase B 应有 `/scaffold-phase01 <module>` 一键起 |
| **F9** | PRD 的"开放问题"段（Q1-Q4 留 Phase 02 决）很有用，但 Checklist 没明示 | 我凭直觉留了开放问题，Checklist 没引导 |
| **F10** | 分级判定 L1 / L2 / L3 后没强制写"分级理由" | 实例文件头部只有"分级 = L1"，没记"为什么是 L1"。3 个月后回看不知道怎么判的 |

### 1.3 其他数据

- **Phase 01 实际耗时**：约 2 小时（4 份产出 + 1 份 Gate 实例 + 同步风险登记 + commit）
- **预计 vs 实际**：原 PRD 商业计划估"8 人天 ≈ 1 Sprint"是含开发，Phase 01 单独算 0.5 人天合理
- **产出文件数**：4 份产出物 + 1 份 Gate 实例 = 5 个新 .md，约 528 行
- **commit 数**：1 个（`c6c1bb5`），符合"一件事一个 commit"

---

## 2. 诊断（Diagnoses）

### 2.1 模式 #1：模板默认假设"中型团队 + 重型决策" → 小团队 / 内部工具 friction 高

- **现象**：F1 / F2 / F3 / F4 / F5 都源于此
- **根因**（5 Whys）：
  1. 为什么单人项目要 5 个签字位？→ Checklist 模板抄了"标准企业流程"
  2. 为什么抄标准企业流程？→ 模板设计时假设受众是 ≥ 20 人团队
  3. 为什么没考虑小团队？→ 没有把"团队规模 / 项目类型"作为模板变量
  4. 为什么没参数化？→ 上线时优先把"硬度"做足，分级粒度只到 L1/L2/L3 没到 "L1-Small-team" / "L1-Internal-tool"
  5. 根因：分级模型过粗，未识别"内部工具" / "团队规模"两个正交维度
- **涉及规范文件**：
  - [gate-checklists/README.md](../gate-checklists/README.md) §分级
  - [Phase01-立项-Gate.md](../gate-checklists/Phase01-立项-Gate.md) §D（评审签字）+ §B.1（5 段访谈）
  - [模块工作流.md](../模块工作流.md) §强制执行层

### 2.2 模式 #2：手动同步 / 手动创建产出物文件 → 操作成本高

- **现象**：F6 / F7 / F8
- **根因**：Phase A 是"被动基础设施"，没自动化辅助。复制 / 同步 / 创建工作全靠人。
- **涉及**：缺 Phase B 的 `/feature` / `/scaffold-phase` / `/risks-sync` 命令

### 2.3 模式 #3：决策可追溯性不够

- **现象**：F9 / F10
- **根因**：Checklist 收集了"结论"但没收集"为什么是这个结论"
- **涉及**：Phase01-立项-Gate.md 头部"分级"字段；PRD 模板的"开放问题"段

---

## 3. 行动（Actions）

| # | 建议 | 涉及 | 转 Proposal? | 优先级 |
|---|---|---|---|---|
| **A1** | 引入"项目类型"维度（外部产品 / 内部工具 / 框架升级），各类型有差异化必产出物清单 | gate-checklists/README.md §分级 + 各 PhaseNN-Gate.md 头部 | → **0001-internal-tool-track.md** | P0 |
| **A2** | 引入"团队规模"维度（< 5 / 5-15 / > 15），自动调整签字位与"≥ N 段访谈"硬约束 | gate-checklists/Phase01-...md §D + §B.1 | → **0002-team-size-adjusted-thresholds.md** | P0 |
| **A3** | Checklist 头部"分级"字段后追加"分级理由"必填项 | 6 个 PhaseNN-Gate.md 模板 + instances/README.md 表格列 | → **0003-require-triage-rationale.md** | P1 |
| **A4** | Phase B 实现 `/scaffold-phase <NN> <module>`：一键生成 N 份产出物占位 + 复制 Gate 模板到 instance | （新 skill）| 待 Phase B | P1 |
| **A5** | 写一个轻量级 `/risk-add` 命令，输入风险描述 → 自动追加到风险登记册 + 关联当前 Gate 实例 | （新 skill 或 hook）| 待 Phase B | P2 |
| **A6** | OKR 模板补充"如何与 Gate 进度对应"段，让 Checklist "已排上 OKR" 这条勾起来有据 | 99-跨阶段/团队 OKR.md | 直接改即可 | P2 |
| **A7** | PRD 模板加"开放问题（留下阶段决议）"段 | 01-立项/PRD.md 模板 | 直接改即可 | P2 |
| **A8** | Checklist "评审材料提前 ≥ 2 天"放到 L1 模板，L2/L3 不强制 | Phase01-...md §B.4 | 与 A2 合并 | — |

---

## 4. 关注下一步

- [ ] Phase 02 启动前，先决议 A1/A2/A3 这 3 条 proposal（避免 Phase 02 重复踩同样的坑）
- [ ] Phase 02 设计阶段产出物（系统架构 / 数据库设计 / API 设计）由 Claude + 用户结对完成，看 Phase 02 Gate Checklist 是否同样有 friction
- [ ] 把"开放问题"放进 PRD 后，看 Phase 02 设计评审时这些问题能否一次性闭环

---

## 5. 链路

- 触发本反思的 commit: `c6c1bb5 docs(gate): project phase 01 passed`
- 由本反思衍生的 proposals: 0001 / 0002 / 0003（下一步创建）
- 关联模块: [Project Phase 01 Gate 实例](../gate-checklists/instances/project/Phase01-立项-Gate-2026-05-15.md)

---

## 6. 一句话总结

**Phase A 基础设施可用，但模板默认假设"中型团队 + 重型决策"，对小团队 / 内部工具 friction 偏高。需要在 Phase 02 之前引入"项目类型 × 团队规模"二维参数化。**

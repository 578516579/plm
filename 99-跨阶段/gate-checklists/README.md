# Gate Checklists — 阶段间硬卡控

每个业务模块、每个阶段都必须复制对应的 Checklist 模板，**逐条打勾、签字、归档**，才能进入下一阶段。
不打勾 = 不签字 = **不许进入下一阶段**。这是把 [模块工作流.md](../模块工作流.md) 从"软规范"升级为"硬合同"的执行层。

---

## 使用流程（每个模块每个阶段都走一遍）

1. **复制模板**：从本目录 `PhaseNN-阶段名-Gate.md` 复制一份到模块归档目录：
   ```
   99-跨阶段/gate-checklists/instances/<模块名>/PhaseNN-阶段名-Gate-<YYYY-MM-DD>.md
   ```
   例如：`instances/project/Phase01-立项-Gate-2026-05-20.md`
2. **填头部信息**：模块名 / Owner / 起止日期 / 评审人。
3. **执行 Checklist**：每项打勾（`- [x]`）或写"N/A 因 …"。
4. **触发评审**：到了 Gate 节点，召开评审会，纪要存 [99-跨阶段/会议纪要/](../会议纪要/)。
5. **评审人签字**：在文件底部签字区填名字 + 日期。
6. **commit 入库**：commit message 用 `docs(gate): <module> phase NN passed`，作为不可篡改的审计证据。
7. **更新模块工作流总览**：在 [模块工作流.md](../模块工作流.md) 顶部"模块进度速查"加一行。

**没走完上述 7 步 → Claude 与人类都不允许动手开始下一阶段的产出物。** Claude 在用户要求"进入下一阶段"时，必须先核对当前 Checklist 状态，未达成 → 拒绝并提示补 Gate。

---

## 分级 — 三维参数化

Gate 模板的"硬度"由 **3 个维度** 联合决定。实例文件头部必须填全 3 项，**Gate 严格度 = max(级别) + 类型差异化 + 团队规模阈值**。

> 历史：本文档原仅 L1/L2/L3 一级分类（v1）。Project Phase 01 dogfood 后引入"项目类型"与"团队规模"二级维度（v2，由 proposals 0001/0002 合入）。

### 维度 1 — 级别（必填，必带"分级理由"）

| 级别 | 触发条件（满足任一即升级） | 必走 Gate | 备注 |
|---|---|---|---|
| **L1 重大** | 新业务模块 / 主架构变更 / 数据库结构性变更 / 新增第三方集成 / 安全合规相关 | 01 → 02 → 03 → 04 → 05 → 06 **全部** | 默认按"高就高" |
| **L2 中型** | 已有模块大改 / 新增独立 feature / 新增 / 改 公开 API / 性能优化（>20% 资源占用） | 02（可简化） + 03 + 04 + 05 + 06 | |
| **L3 小型** | bug 修复 / 文档修订 / 小重构 / 依赖小版本升级（patch） / typo | 仅 PR + CR + 04 回归 + 05 上线 | |

**proposal 0003**：实例头部"分级"字段后**必须**追加"分级理由"，注明触发了上表中具体哪条判定列。**禁止**仅写"L1"无理由——拒绝合 commit。

### 维度 2 — 项目类型（必填，proposal 0001）

| 类型 | 典型例子 | 必产出物差异 |
|---|---|---|
| **`external-product`** | 面向用户的 SaaS / App / 公开 API | 全套：市场调研含 TAM/SAM/SOM、商业计划含收入预测、用户访谈 ≥ 团队规模阈值 |
| **`internal-tool`** | PLM、CRM、运维平台、内部 BI | 简化：市场调研改为"内部用户痛点 + 已有工具对比"、商业计划改为"ROI 工时节约模型"、访谈"覆盖所有相关角色" |
| **`framework-upgrade`** | 技术栈大版本升级、基础库换代、CI/CD 重构 | 跳过市场调研 / 商业计划；强化"技术调研 + 影响面评估 + 回滚方案"（Phase 02 ADR 强制） |

### 维度 3 — 团队规模（必填，proposal 0002）

| 团队规模 | 签字角色数下限 | 用户访谈下限 | 评审材料提前期 |
|---|---|---|---|
| **`solo (n=1)`** | 1（自评，必须 commit message 带 `[solo-review]` 标签） | "覆盖所有可触达的相关方"（≥ 1） | 0 天（当日） |
| **`small (2 ≤ n ≤ 5)`** | 2 | ≥ 2 | 0 天 |
| **`medium (6 ≤ n ≤ 15)`** | 3 | ≥ 3 | 1 天 |
| **`large (n > 15)`** | 5（按 v1 原模板默认值） | ≥ 5 | 2 天 |

> **solo 模式滥用防护**：每个 `[solo-review]` commit 必须有理由（在 instance E 段或 commit message body）。后续 reflect 自动统计 `[solo-review]` 比例，过高时触发 proposal 调整。

### 维度 4 — 项目成熟度（必填，proposal 0006）

| 成熟度 | 典型阶段 | 差异化约束 |
|---|---|---|
| **`early (v0.x)`** | 项目早期，1-2 个 Sprint 内首次上线前 | staging 可用 dev 替代；CI 可用本地 `mvn install` 替代；监控降级为基础日志；solo Sprint 文档合并到 Gate 实例 |
| **`stable (v1.x ~ v2.x)`** | 稳定期，已有 staging + CI + 监控 | 标准约束：staging 必需、CI 必需、监控看板必需 |
| **`mature (v3.x+)`** | 成熟期，关键业务 / 高 SLA | 强化约束：双 staging（pre-prod）、灰度策略详细、SLA 监控、安全审计每 release 必走 |

**自动升级规则**：
- 项目上线 prod **满 6 个月** → 强制升 `stable`（不能继续 early）
- 项目上线 prod **满 2 年**或被标"关键业务" → 强制升 `mature`
- 降级（如 mature → stable）需走 proposal 评审

**Phase 05 / 06 早期豁免**（proposal 0007 / 0010 / 0011）：当 `internal-tool + early` 二条件叠加时，下列 Gate 模板段允许"替代方案"：
- Phase 05 §H：发布目标=`dev` 时双人签字可 self-review 等价
- Phase 06 §B：5 指标看板可用"替代方案表"代替正式看板
- Phase 06 §G：solo+early 时 OKR 对照可标 N/A（前提 [团队 OKR.md](../团队%20OKR.md) 顶部显式标注本周期不维护 KR）

### 维度 5 — 测试时机（隐含，proposal 0004）

非"实例填写"维度，而是 Gate 模板 DoD 切分的依据：

| 阶段 | DoD 性质 |
|---|---|
| Phase 03 准出 = **代码骨架 DoD** | 业务代码落位 + 编译通过 + E2E 关键路径手测 + L1 高风险模块（鉴权/支付）的关键路径单测 |
| Phase 04 准出 = **代码 + 测试稳定 DoD** | Service 单测覆盖率 ≥ 70% + Controller 集成测试 + 性能 + 安全 + 回归 |

**L1 高风险白名单**（Phase 03 仍强制关键路径单测）：
- 鉴权 / 授权（涉及 JWT / Spring Security）
- 支付 / 计费
- 数据迁移脚本（涉及生产数据）
- 第三方集成（外部 API 关键链路）

---

## 速查表 — 4D 组合最常见配置

| 你的场景 | 建议组合（级别/类型/规模/成熟度） | 必走 Gate（典型） |
|---|---|---|
| PLM 首个业务模块（Project / Phase / Task）单人推进 | `L1 / internal-tool / solo / early` | 01-06 全套；Phase 03 仅代码骨架 + E2E；Sprint 文档合并入 Gate；staging 用 dev |
| 业务运行半年后新加 Task 子模块（团队已 5 人） | `L1 / internal-tool / small / stable` | 标准 L1 路径；Phase 04 测试覆盖率必达 |
| 外部 SaaS 主要新功能 | `L1 / external-product / medium-large / stable` | 全套 + Phase 01 商业计划含收入预测 |
| Spring Boot 大版本升级 | `L1 / framework-upgrade / 任意规模 / 任意成熟度` | 02 强 ADR + 04 强回归 + 05 强回滚；01 仅简短"动机 + 影响面" |
| 鉴权模块新增（无论项目成熟度）| `L1 / 任意类型 / 任意规模 / 任意成熟度` | Phase 03 强制关键路径单测（白名单豁免） |
| 已有功能 typo / 小 bug | `L3 / 任意 / 任意 / 任意` | 仅 03 PR + 04 回归子集 |

### 怎么定级？

**默认按"高就高"**：拿不准时往上一级走。事后看小题大做无伤大雅，反过来就是事故。

**3 个维度均在 PR / issue 创建时由发起人标注**（issue label / PR 描述 / Gate 实例头部）。Owner 复核。

### 例外通道（仅限 P0 故障）

线上 P0 故障 hotfix：
- 跳过 Gate 01/02，直接进入 Gate 03（开发）+ Gate 04（验证）+ Gate 05（快速发布）
- **必须在 48 小时内补齐 Gate 01/02 文档**（事后立项），并补一次 ADR 说明绕过原因
- 由 oncall 主管 + CTO/技术 leader 双签授权

---

## Checklist 文件清单

| 文件 | Phase | L1 必走 | L2 必走 | L3 必走 |
|---|---|---|---|---|
| [Phase01-立项-Gate.md](Phase01-立项-Gate.md) | 01 立项 | ✅ | ⚠️ 简化 | ❌ |
| [Phase02-设计-Gate.md](Phase02-设计-Gate.md) | 02 设计 | ✅ | ✅（可简化） | ❌ |
| [Phase03-开发-Gate.md](Phase03-开发-Gate.md) | 03 开发 | ✅ | ✅ | ⚠️ 仅 PR/CR 段 |
| [Phase04-测试-Gate.md](Phase04-测试-Gate.md) | 04 测试 | ✅ | ✅ | ⚠️ 仅回归段 |
| [Phase05-上线-Gate.md](Phase05-上线-Gate.md) | 05 上线 | ✅ | ✅ | ✅ |
| [Phase06-运营-Gate.md](Phase06-运营-Gate.md) | 06 运营 | ✅ | ✅ 观察期 | ❌ |

> "⚠️ 简化"：可只填头部 + 关键 3 条；详见各 Checklist 文件内的"L2 简化指引"段。

---

## 实例归档结构

```
gate-checklists/
├── README.md                              （本文件）
├── PhaseNN-阶段名-Gate.md                 （6 个模板，本目录直接放）
└── instances/
    ├── project/                           （每个模块一个目录）
    │   ├── Phase01-立项-Gate-2026-05-20.md
    │   ├── Phase02-设计-Gate-2026-05-30.md
    │   └── ...
    ├── task/
    └── change-request/
```

实例文件 commit 后**不许覆盖**（要修订就追加 "## 修订记录" 章节，注明原因 + 评审人）。这些文件作为审计证据，将来过等保 / ISO 27001 / SOC2 评估时直接出示。

---

## 与其他文档的关系

| 文档 | 角色 |
|---|---|
| [模块工作流.md](../模块工作流.md) | 总览：阶段流程 + 准入 / DoD 的**描述** |
| 本目录 | 执行：可复制的 Checklist **模板** + 实例归档 |
| [开发规范.md](../../03-开发/开发规范.md) | 技术规范：Checklist 中"代码符合规范"具体引用此文档 |
| [.claude/rules.md](../../.claude/rules.md) | Claude 约束：Claude 帮助跨阶段前必须 verify Checklist |

---

## 反模式（拒绝执行）

- ❌ 复制了 Checklist 但不打勾就 commit（伪审计）
- ❌ 一个人签所有评审人位置（绕过评审）
- ❌ 事后倒填 Checklist 当成"那时候真的有做过"（造假）
- ❌ 评审纪要写"我们觉得 OK" 不写理由（评审无效）
- ❌ Gate 出问题悄悄把 L1 降级成 L3（避审）

发现这些行为 → 记入下次 Sprint 回顾 + 触发流程整改。

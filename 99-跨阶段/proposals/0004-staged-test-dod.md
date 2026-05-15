# Proposal 0004: 拆分 Phase 03 / Phase 04 的 DoD —— "代码骨架" vs "代码 + 测试"两阶段

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0004 |
| 标题 | 测试代码不再是 Phase 03 准出条件，挪到 Phase 04 兜底 |
| 状态 | **proposed** |
| 类型 | 流程 |
| 提出人 | Wjl + Claude（来自 Project Phase 03 dogfood） |
| 提出日期 | 2026-05-15 |
| 评审人 | 项目经理 + 技术 lead + QA lead |
| 评审日期 | TBD |

---

## 1. 背景

Phase 03 模板要求 "Service 单元测试覆盖率 ≥ 70%" 作为准出条件。Project 模块 dogfood 时：
- 代码骨架（CRUD + ADR 实现 + 状态机）一个 Sprint 内做完 → 完全合理
- 但同一 Sprint 内**还要写测试** → 工作量翻倍，实际只能把测试推迟，结果在 E 段豁免 "B.4 测试代码本期未写"

这暴露了模板里"代码骨架 DoD"和"代码 + 测试稳定 DoD"被混在一个 Phase 03 中。

## 2. 证据

- 关联 reflect: [../reflect/2026-W20-project-phase03-dogfood.md](../reflect/2026-W20-project-phase03-dogfood.md) §2.1 模式 #1
- 关联 Gate 实例: [../gate-checklists/instances/project/Phase03-开发-Gate-2026-05-15.md](../gate-checklists/instances/project/Phase03-开发-Gate-2026-05-15.md) E 段 F-P03-01
- 频次：Phase 03 dogfood 1 次触发；预期后续每个新业务模块（Phase / Task / Requirement 等）都会撞同样问题
- E 段豁免数 Phase 03 = 3 处（vs Phase 02 = 1 处），其中 F-P03-01 是主因

## 3. 提案

把 Phase 03 / Phase 04 的 DoD 重新切割：

### Phase 03 准出条件（代码骨架 DoD）
- ✅ 业务代码落位 + 编译通过
- ✅ 关键链路 E2E 手测通过（≥ 1 个正常 + 1 个异常用例）
- ✅ Service 必要的逻辑保护已实现（如本期 ADR-0001 / 状态机）
- ❌ **不再要求** Service 单测覆盖率 70%
- ❌ **不再要求** Controller 集成测试

### Phase 04 准出条件（代码 + 测试稳定 DoD）
- ✅ Service 单元测试覆盖率 ≥ 70%（jacoco 报告）
- ✅ Controller 集成测试覆盖核心端点
- ✅ 性能 / 安全 / 回归测试（已有要求）

### 例外路径
- L1 高风险模块（如鉴权 / 支付 / 数据迁移）— Phase 03 仍要求"Service 关键路径单测"（不到 70%，至少有 happy path）
- L3 小型改动 — Phase 03 / 04 测试要求都可简化（已在 0002/v2 模板中体现）

---

## 4. 改动文件

| 文件 | 改动 |
|---|---|
| `Phase03-开发-Gate.md` §B.4 | 改写：测试代码改为"建议产出，非强制"；强制项保留"E2E 手测 + 关键路径单测（L1 鉴权类）" |
| `Phase03-开发-Gate.md` §F (DoD) | 移除"覆盖率 ≥ 70%"作为准出条件 |
| `Phase04-测试-Gate.md` §A 准入条件 | 加一条："上一 Gate（Phase 03）E2E 关键路径通过" |
| `Phase04-测试-Gate.md` §B 必产出物 | 强化"Service 单元测试覆盖率 ≥ 70%" 作为 Phase 04 强制项 |
| `gate-checklists/README.md` | 在分级 / 项目类型表之外加一句说明"代码 vs 测试的 DoD 分布" |

## 5. 影响

- 已完成 Phase 03 的实例（如本项目 Project Phase 03）→ **回溯豁免**：E 段 F-P03-01 不再算违规
- 后续 Phase 04 模板更"硬"，但更合理（测试本来就该在 Phase 04 重点做）
- Phase 03 进入 Phase 04 的准入门槛**降低**，避免"卡在 Phase 03 不能走"

## 6. 风险

| 风险 | 缓解 |
|---|---|
| 测试永远拖到 Phase 04 → Phase 04 工时压力大 | Phase 04 准出条件不变；测试压力反而早暴露 |
| 早期高风险模块（鉴权 / 支付）可能漏测 | 例外路径强制 L1 高风险模块 Phase 03 仍要写关键路径单测 |
| 引入"代码骨架 vs 稳定 DoD"概念让模板更复杂 | README §速查表加一行说明 |

## 7. 备选

- **A**（本提案）：拆 Phase 03 / 04 的 DoD
- **B**：保持现状，要求 Phase 03 必带测试 → 不切实际
- **C**：合并 Phase 03 / 04 为一个大 Phase → 失去阶段感

选 A 因为最务实，与团队真实节奏吻合。

## 8. 实施计划

```
[ ] Step 1: proposal accept
[ ] Step 2: Claude 改 Phase03-开发-Gate.md / Phase04-测试-Gate.md / README.md
[ ] Step 3: 在 Phase 03 Gate instance 末尾追加"按 0004 回溯豁免 F-P03-01"
[ ] Step 4: commit message: refactor(gate): apply proposal 0004 (staged test DoD)
```

## 9. 衡量

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 03 E 段豁免数（与测试相关）| 1 (F-P03-01) | 0 |
| Phase 04 平均耗时（增加测试压力可能拉长）| 待 Project Phase 04 实测 | ≤ 1 Sprint |
| Phase 04 测试覆盖率达成率 | TBD | ≥ 80% 的模块达 70% 覆盖 |

## 10. 评审记录

| 评审人 | 立场 | 日期 |
|---|---|---|
| Wjl | _待 review_ | |

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-15 | Wjl + Claude | 初版 |

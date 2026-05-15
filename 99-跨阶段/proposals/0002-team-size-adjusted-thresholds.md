# Proposal 0002: 按"团队规模"自动调整 Gate Checklist 硬阈值

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0002 |
| 标题 | 按"团队规模"自动调整 Gate Checklist 硬阈值（签字角色数、访谈数、评审材料提前期） |
| 状态 | **proposed** |
| 类型 | 流程 |
| 提出人 | Wjl + Claude（来自 Project Phase 01 dogfood） |
| 提出日期 | 2026-05-15 |
| 评审人 | 项目经理 + 技术 lead |
| 评审日期 | TBD |

---

## 1. 背景

Project Phase 01 dogfood 中，单人项目（n=1）面对 L1 模板的"5 角色签字 / 5 段访谈 / 评审材料提前 2 天"硬阈值无法满足，全部走 E 段豁免。

阈值是为 ≥ 20 人团队设计的，单人 / 小团队语境下变成形式主义。

## 2. 证据

- 关联 reflect: [../reflect/2026-W20-project-phase01-dogfood.md](../reflect/2026-W20-project-phase01-dogfood.md) §2.1 → F1, F2, F3, F4
- 关联 Gate 实例 E 段豁免次数：**3** 次 / Phase 01（应 ≤ 1，否则模板对该情境过严）

## 3. 提案

引入"团队规模"维度，与"项目类型"(0001) 正交，自动调整阈值：

| 团队规模 | 签字角色数下限 | 用户访谈下限 | 评审材料提前期 |
|---|---|---|---|
| **solo (n=1)** | 1（自评，明确标注） | "覆盖所有可触达的相关方"（≥ 1） | 0 天（当日） |
| **small (2 ≤ n ≤ 5)** | 2 | ≥ 2 | 0 天 |
| **medium (6 ≤ n ≤ 15)** | 3 | ≥ 3 | 1 天 |
| **large (n > 15)** | 5（按现有模板） | ≥ 5 | 2 天 |

实例文件头部填"团队规模"后，对应阈值自动适用，无需 E 段豁免。

## 4. 改动文件

| 文件 | 改动 |
|---|---|
| `gate-checklists/README.md` §分级 | 新增"团队规模"维度（与项目类型并列） |
| `gate-checklists/Phase01-...Gate.md` 头部 + B.1 + B.4 + D | 阈值参数化 |
| `gate-checklists/Phase02-...Gate.md` ~ `Phase06-...Gate.md` D 段 | 签字角色数参数化 |

## 5. 影响

- 已签字的 Gate 实例：**不重审**
- 新实例：填"团队规模"字段后自动适配
- Claude `/feature` skill (Phase B)：默认按 settings 中的"团队规模"配置参数化

## 6. 风险

| 风险 | 缓解 |
|---|---|
| solo / small 模式被滥用以绕过应有评审 | 配套要求："solo" 必须在 instance 头部明确写"自评模式 + 团队人数"，且本类型 instance commit message 必须含 `[solo-review]` 标签 |

## 7. 实施

```
[ ] Step 1: 与 0001 合并实施（同次 PR，避免连续两次大改）
[ ] Step 2: PR commit message: refactor(rules): parametrize thresholds by team size (proposal 0002)
```

## 8. 衡量

- 信号：E 段豁免次数 — 基线 3（Project Phase 01）→ 目标 0
- 信号：实例文件头部"团队规模"字段填写率 — 目标 100%

## 9. 评审记录

| 评审人 | 立场 | 日期 |
|---|---|---|
| Wjl | _待 review_ | |

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-15 | Wjl + Claude | 初版 |

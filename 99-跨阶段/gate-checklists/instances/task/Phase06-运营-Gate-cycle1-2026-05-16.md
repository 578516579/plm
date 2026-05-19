# Phase 06 — 运营 Gate · 实例（Task cycle 1 kickoff / day 0）

> **补录** (2026-05-19): 此文件由 Phase D v0.2 [signals-collect](../../../../.claude/skills/signals-collect/scripts/phase-duration.sh)
> dogfood 发现缺失 (per [proposal 0012](../../../proposals/0012-phase06-two-stage-signoff.md) 两段式签字 MUST). 内容根据当时
> 实际状态 reconstructed; 详细 cycle 1 数据见 [day 7 closure](Phase06-运营-Gate-cycle1-day7-2026-05-22.md).

| 字段 | 值 |
|---|---|
| 模块名 | **Task（任务实体）** |
| 分级 | **L1** |
| 项目类型 | `internal-tool` |
| 团队规模 | `solo (n=1)` |
| 项目成熟度 | `early` |
| 周期类型 | **cycle 1 = 上线后 7 天** |
| 周期起止 | 2026-05-16 → 2026-05-23 |
| Owner | Wjl `[solo-review-3conditions-early-dev]` |
| 关联上线版本 | plm-0.1.0 (per [Phase 05 Gate](Phase05-上线-Gate-2026-05-16.md)) |
| 当前状态 | 🔄 **kickoff (day 0)** — 已在 day 7 closure 收尾 |

---

## §A 进入条件

- [x] [Phase 05 Gate](Phase05-上线-Gate-2026-05-16.md) 已签字通过 (2026-05-16)
- [x] 上线 30 min 全绿
- [x] 数据看板 / 替代方案表已配 (early: substrate-only per [proposal 0010](../../../proposals/0010-phase06-substrate-only-metrics.md))

---

## §C 项目状态 (day 0)

- maturity: **early** (v0.1.0)
- 团队规模: solo (n=1)
- 关联上线版本: plm-0.1.0
- task 模块 schema: 见 [02-设计/数据库设计](../../../../02-设计/) 及 PRD-MAPPING.md

---

## §B 监控 (per proposal 0010 substrate-only)

| # | 观察手段 | 触发响应条件 |
|---|---|---|
| 1 | 手动 curl healthcheck | 5xx → 立即查 backend log |
| 2 | journalctl 后端日志 | ERROR 堆栈 → 评估是否回滚 |
| 3 | E2E 套件每日跑 (Task CRUD + 状态机) | 全套件 fail → 阻断下一 cycle |
| 4 | DB 慢查询 EXPLAIN | > 1s → 入下个 Sprint |
| 5 | DB HEX 字符抽样 | 含 EFBFBD → P0 编码事故 |

---

## §I.1 启动签字 (kickoff day 0)

| 角色 | 姓名 | 评审 | 日期 |
|---|---|---|---|
| 运营 / 产品 lead | Wjl `[solo-review-3conditions-early-dev]` | ✅ cycle 1 已启动 | 2026-05-16 |
| 开发 lead | Wjl (兼) | ✅ 已部署且可访问 | 2026-05-16 |

---

## 衔接

- 上一阶段: [Phase 05 上线 Gate](Phase05-上线-Gate-2026-05-16.md)
- 下一阶段: [cycle 1 day 7 closure](Phase06-运营-Gate-cycle1-day7-2026-05-22.md)

---

## 修订记录

| 日期 | 修改人 | 变更 |
|---|---|---|
| 2026-05-16 | Wjl (implicit kickoff) | cycle 1 启动, 但未单独成 Gate 文件 (缺失) |
| 2026-05-19 | Wjl (补录) | Phase D v0.2 dogfood 发现 → 按 proposal 0012 两段式签字 MUST 补录 |

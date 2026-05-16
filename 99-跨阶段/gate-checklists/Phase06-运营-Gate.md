# Phase 06 — 运营 Gate Checklist

> 复制本模板到 `instances/<模块>/Phase06-运营-Gate-<YYYY-MM-DD>.md`。
> **运营是持续过程，本 Gate 按"周期"复用**：上线后第一个 7 天 + 第一个 30 天 + 此后每季度。
> **L3 改动不走本 Gate**（无需运营观察）。

---

## 头部信息（必填）

| 字段 | 值 |
|---|---|
| 模块名 | |
| 分级 | L1 / L2 |
| **分级理由** | _引用 [README §维度 1](../README.md)_ |
| **项目类型** | `external-product` / `internal-tool` / `framework-upgrade` |
| **团队规模** | `solo` / `small` / `medium` / `large` |
| **项目成熟度** | `early` / `stable` / `mature` |
| 周期类型 | 上线后 7 天 / 30 天 / Q1 / Q2 / ... |
| 周期起止 | YYYY-MM-DD ~ YYYY-MM-DD |
| Owner（运营 / 产品 lead） | |
| 关联上线版本 | vX.Y.Z |

---

## A. 准入条件

- [ ] [Phase 05 Gate](Phase05-上线-Gate.md) 已签字通过
- [ ] 上线 30 分钟全绿确认无误
- [ ] 数据看板已配置且数据流入正常

---

## B. 必产出物 — 监控与看板（按 项目成熟度 差异化，proposal 0010）

### B.standard — `stable` / `mature` 适用

- [ ] 已在 [06-运营/数据看板（链接）](../../06-运营/数据看板（链接）/) 维护本模块看板链接
- [ ] 关键指标至少 5 个（业务 / 性能 / 错误率 / 用户行为 / 容量），每个有量化阈值与告警
- [ ] 告警接收人明确（写在 [Runbook.md](../../05-上线/Runbook.md)）

### B.substrate-only — `early` 适用（proposal 0010）

适用条件：项目尚无生产监控基础设施（Prometheus/Grafana/SLS 都没接），仅 dev 环境。

- [ ] 在本 Gate 实例 §J 写"监控替代方案表"，至少含:
  - 5 项当前可用的观察手段（如：手动 curl healthcheck / `journalctl` 日志 / 后端 stdout / E2E 测试套件通过情况 / 数据库慢查询 EXPLAIN）
  - 每项的"触发响应条件"（什么情况下应该停下来 / 应该改进基础设施）
- [ ] 在本 Gate 实例 §J 标"升级路径"：转入 `stable` 时本段失效，必须补齐正式看板链接（链 proposal 0010）。
- [ ] §C 项目成熟度字段已确认为 `early`，且 §A 已部署目标环境 ≤ dev。

---

## C. 必产出物 — 周期汇报

### C.1 周期内的周报

- [ ] 文件：`06-运营/周报月报/YYYY-WW-周报.md`（每周一份）
- [ ] 核心指标本周 vs 上周 vs 环比
- [ ] 完成的事 / 进行中 / 下周计划 / 风险

### C.2 周期复盘（7 天 / 30 天 / 季度末）

- [ ] 文件：`06-运营/<模块>-<周期类型>-复盘-<YYYY-MM-DD>.md`
- [ ] 含：实际指标 vs PRD 中承诺的指标
- [ ] 含：差距分析（原因 / 学到什么）
- [ ] 含：是否达成立项时的"价值假设"

---

## D. 必产出物 — 用户反馈处理

- [ ] [06-运营/用户反馈/](../../06-运营/用户反馈/) 已收集本周期反馈
- [ ] 反馈已分类（bug / feature-req / ux / performance / ...）
- [ ] 高优反馈已立 issue 进下一 Sprint
- [ ] 高频反馈已在月报中点名

---

## E. 必产出物 — A/B 测试（如做实验）

- [ ] 文件：`06-运营/AB 测试记录/YYYY-MM-DD_<模块>_<实验>.md`
- [ ] 假设 / 分组 / 主指标 / 护栏指标 / 时长 / 显著性 已记录
- [ ] 结论明确（全量 / 回滚 / 继续观察）
- [ ] 学到的洞察已沉淀到 PRD 后续版本或 ADR

---

## F. 必产出物 — 缺陷与改进

- [ ] 本周期收到的 P0/P1 缺陷 100% 已处置（修复 / 回滚 / 接受）
- [ ] P0/P1 缺陷已记录到 [99-跨阶段/风险登记册.md](../风险登记册.md) 的"已发生事件"段
- [ ] 触发 P0/P1 的根因分析报告（如 5 Whys）已写

---

## G. 必产出物 — OKR 对照（按 团队规模 × 项目成熟度 差异化，proposal 0011）

### G.standard — `small+` 或 `stable+` 适用

- [ ] 在 [99-跨阶段/团队 OKR.md](../团队%20OKR.md) 中更新本模块对应 KR 的实际数值
- [ ] 若 KR 偏差 > 20%，写一段差距说明 + 下一周期行动

### G.solo-early — `solo + early` 二条件叠加适用（proposal 0011）

可标 `N/A`，**前提同时满足**：

- [ ] [99-跨阶段/团队 OKR.md](../团队%20OKR.md) 顶部已显式标"本周期不维护数值型 KR，下次评审 YYYY-MM-DD"
- [ ] 在本 Gate 实例 §J 写一句"使用 Gate 实例 §I 的'本周期完成情况'代替 KR 对照"

转入 `small+` 或 `stable+` 时本段失效，回到 G.standard。

---

## H. Definition of Done — 两段式（proposal 0012）

### H.1 启动 (day 0) — cycle 开始当日提交

- [ ] §A 进入条件全满足
- [ ] §C 项目状态字段已填（含 maturity / 团队规模）
- [ ] 已部署且可访问（链 demo URL / 后端 health check 输出）
- [ ] §I.1 "启动签字" 已完成
- [ ] **本 Checklist 文件已 commit 入库**（`docs(gate): <module> phase 06 cycle N kickoff`）— 启动占位 commit

### H.2 终态 (day N，cycle 末) — cycle 满规定时长后追加段提交

- [ ] B / C / D / E（如适用）/ F / G 全部满足
- [ ] §J 异常/风险已收尾，或有转下周期的明确计划
- [ ] §I.2 "终态签字" 已完成
- [ ] **本 Checklist 文件已追加 commit 入库**（`docs(gate): <module> phase 06 cycle N closure`）

---

## I. 评审记录与签字 — 两段式（proposal 0012）

### I.1 启动签字 (day 0)

| 角色 | 姓名 | 评审结论 | 签字日期 |
|---|---|---|---|
| 运营 / 产品 lead | | ✅ cycle N 已启动 / ⚠️ 启动有条件 / ❌ 取消启动 | YYYY-MM-DD |
| 开发 lead | | ✅ 已部署且可访问 | YYYY-MM-DD |

### I.2 终态签字 (day N，cycle 末)

按 团队规模 调整必填角色数。`solo`=1（自评 `[solo-review]`）/ `small`=2 / `medium`=3 / `large`=4。

| 角色 | 姓名 | 评审结论 | 签字日期 |
|---|---|---|---|
| 运营 / 产品 lead | | 通过 / 有条件通过 / 不通过（决议进下周期 / 暂停 / 修复后再 cycle） | YYYY-MM-DD |
| 业务方代表（small+ 必填） | | | |
| 开发 lead（涉及修复纳入 Sprint 时必填） | | | |
| 客服代表（external-product 必填） | | | |

---

## J. 异常 / 风险

| 项 | 原因 | 补救计划 | 截止日 | 责任人 |
|---|---|---|---|---|
| | | | | |

---

## K. 进入下一周期 / 闭环回归

判断"下一步"走向：

- [ ] **继续运营**：填下一周期的 Phase 06 Gate
- [ ] **小修小补**：开 L3 改动，走 [Phase 03 Gate](Phase03-开发-Gate.md) 简化路径
- [ ] **大改进**：开 L1/L2 新需求，**回到 [Phase 01 Gate](Phase01-立项-Gate.md)** 重新立项
- [ ] **下线**：填"下线评审"（如有此场景，参考 Phase 05 反向流程）

✅ **签字人确认**：

| 角色 | 签字 | 日期 |
|---|---|---|
| 运营 lead | | |
| 产品 Owner | | |

---

## 修订记录

| 日期 | 修改人 | 原因 | 决议 |
|---|---|---|---|
| 2026-05-17 | Wjl `[solo-review]` + Claude | [proposal 0010](../proposals/0010-phase06-substrate-only-metrics.md) accepted | §B 拆 B.standard / B.substrate-only — early 阶段允许"监控替代方案表" |
| 2026-05-17 | Wjl `[solo-review]` + Claude | [proposal 0011](../proposals/0011-phase06-okr-optional-in-early.md) accepted | §G 拆 G.standard / G.solo-early — solo+early 可标 N/A，前提团队 OKR.md 顶部显式标注 |
| 2026-05-17 | Wjl `[solo-review]` + Claude | [proposal 0012](../proposals/0012-phase06-two-stage-signoff.md) accepted | §H DoD 拆 H.1 启动 / H.2 终态；§I 签字拆 I.1 启动 / I.2 终态 — 模板对齐 Project cycle 1 已实操的两段式 |

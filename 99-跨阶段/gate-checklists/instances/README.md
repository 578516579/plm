# Gate Checklist 实例归档

每个业务模块走每个阶段时，从 [../](../) 上一级目录复制对应模板，落在这里：

```
instances/
├── README.md                 （本文件）
├── <module-1>/
│   ├── Phase01-立项-Gate-2026-05-20.md
│   ├── Phase02-设计-Gate-2026-05-30.md
│   ├── Phase03-开发-Gate-2026-06-15.md
│   └── ...
├── <module-2>/
│   └── ...
```

## 命名规范

`PhaseNN-<阶段名>-Gate-<YYYY-MM-DD>.md`

- `NN`：01 / 02 / 03 / 04 / 05 / 06
- `<阶段名>`：立项 / 设计 / 开发 / 测试 / 上线 / 运营
- `<YYYY-MM-DD>`：本次评审通过的日期（不是开始日期）

## 不可变性

实例文件 **commit 入库后不允许覆盖**。修订必须走文件内的"修订记录"段追加，并注明：
- 修订日期
- 修订人
- 修订原因（哪条不达标 / 新发现的问题 / 评审意见）
- 评审决议

这些文件作为审计证据，将来过等保 / ISO 27001 / SOC2 评估时直接出示。

## 架构状态（2026-05-16 v0.3 重组后）

后端：37 个 Maven 模块（6 core + 4 active business + 26 stubs + plm-admin + root）
前端：30 个 pnpm packages（同 30 业务模块）+ 主壳

详见 [03-开发/模块拆分架构.md](../../../03-开发/模块拆分架构.md) / [模块拆分指南.md](../../../03-开发/模块拆分指南.md)

## 当前模块清单

| 模块 | 当前阶段 | 分级 | 项目类型 | 团队规模 | 项目成熟度 | 最近 Gate 时间 | 实例文件 | Owner |
|---|---|---|---|---|---|---|---|---|
| **Project**（项目主实体）| ✅ Phase 01-05 完成 / 🔄 Phase 06 cycle 1 进行中（day 1/7） | L1 | `internal-tool` | `solo` | `early` | 2026-05-15 | [P01](project/Phase01-立项-Gate-2026-05-15.md) · [P02](project/Phase02-设计-Gate-2026-05-15.md) · [P03](project/Phase03-开发-Gate-2026-05-15.md) · [P04](project/Phase04-测试-Gate-2026-05-15.md) · [P05](project/Phase05-上线-Gate-2026-05-15.md) · [P06-c1](project/Phase06-运营-Gate-cycle1-2026-05-15.md) | Wjl |
| **Requirement**（需求管理）| ✅ Phase 01-03 完成 / 🏗️ v0.3 重组到 plm-requirement / ⏳ Phase 04 待开始 | L1 | `internal-tool` | `solo` | `early` (v0.2) | 2026-05-16 | [P01](requirement/Phase01-立项-Gate-2026-05-16.md) · [P02](requirement/Phase02-设计-Gate-2026-05-16.md) · [P03](requirement/Phase03-开发-Gate-2026-05-16.md) | Wjl |
| **Task**（开发任务+看板）| ✅ Phase 01-03 完成 / 🏗️ v0.3 重组到 plm-task / ⏳ Phase 04 待开始 | L1 | `internal-tool` | `solo` | `early` (v0.2) | 2026-05-16 | [P01](task/Phase01-立项-Gate-2026-05-16.md) · [P02](task/Phase02-设计-Gate-2026-05-16.md) · [P03](task/Phase03-开发-Gate-2026-05-16.md) | Wjl |
| **Sprint**（迭代）| ✅ Phase 01-03 完成 / 🏗️ v0.3 重组到 plm-sprint / ⏳ Phase 04 待开始 | L1 | `internal-tool` | `solo` | `early` (v0.2) | 2026-05-16 | [P01](sprint/Phase01-立项-Gate-2026-05-16.md) · [P02](sprint/Phase02-设计-Gate-2026-05-16.md) · [P03](sprint/Phase03-开发-Gate-2026-05-16.md) | Wjl |
| **Defect**（缺陷管理）| ✅ Phase 01-03 完成 / ⏳ Phase 04 | L1 | `internal-tool` | `solo` | `early` (v0.3) | 2026-05-16 | [P01](defect/Phase01-立项-Gate-2026-05-16.md) · [P02](defect/Phase02-设计-Gate-2026-05-16.md) · [P03](defect/Phase03-开发-Gate-2026-05-16.md) | Wjl |
| **TestCase**（测试用例管理）| ✅ Phase 01-03 完成 / ⏳ Phase 04 | L1 | `internal-tool` | `solo` | `early` (v0.3) | 2026-05-16 | [P01](testcase/Phase01-立项-Gate-2026-05-16.md) · [P02](testcase/Phase02-设计-Gate-2026-05-16.md) · [P03](testcase/Phase03-开发-Gate-2026-05-16.md) | Wjl |
| **Document**（文档管理 合并 5 stub）| ✅ Phase 01-03 完成 / ⏳ Phase 04 | L1 | `internal-tool` | `solo` | `early` (v0.4) | 2026-05-16 | [P01](document/Phase01-立项-Gate-2026-05-16.md) · [P02](document/Phase02-设计-Gate-2026-05-16.md) · [P03](document/Phase03-开发-Gate-2026-05-16.md) | Wjl |
| **Project / Req / Sprint / Task** P04+P05+P06 cycle1 | ✅ Phase 04 + ✅ Phase 05 上线 + ✅ **Phase 06 cycle 1 day 7 closure** (72 case E2E 全过) | L1 | `internal-tool` | `solo` | `early` | 2026-05-22 | [P04](project/Phase04-测试-Gate-2026-05-16.md) · [P05](project/Phase05-上线-Gate-2026-05-16.md) · [P06-c1-d7](project/Phase06-运营-Gate-cycle1-day7-2026-05-22.md) | Wjl |
| **23 stub 业务模块** | 🟡 空壳骨架已建（pom + package.json + README + 占位 view）；剩余 23 个待启动（document 已激活替代 5 stub）；v0.4 排期 6 个（剩余）/ v0.5 排期 6 个 / deferred 7 个；可用 [生成器](../../../plm-backend/scripts/new-business-module.sh)一键脚手架 | — | — | — | — | 2026-05-16 | 见 [Stubs-Roadmap.md](../../../03-开发/Stubs-Roadmap.md) | — |

> 列说明（4 维参数化）：
> - **分级** L1/L2/L3 — 见 [../README §维度 1](../README.md)
> - **项目类型** external-product / internal-tool / framework-upgrade — 见 [../README §维度 2](../README.md)
> - **团队规模** solo / small / medium / large — 见 [../README §维度 3](../README.md)
> - **项目成熟度** early / stable / mature — 见 [../README §维度 4](../README.md)（proposal 0006 引入）

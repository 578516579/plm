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

## 当前模块清单

| 模块 | 当前阶段 | 分级 | 项目类型 | 团队规模 | 最近 Gate 时间 | 实例文件 | Owner |
|---|---|---|---|---|---|---|---|
| **Project**（项目主实体）| ✅ Phase 01 完成 / ⏳ Phase 02 启动 | L1 | `internal-tool` | `solo` | 2026-05-15 | [project/Phase01-立项-Gate-2026-05-15.md](project/Phase01-立项-Gate-2026-05-15.md) | Wjl |

> 列说明：
> - **分级** L1/L2/L3 — 见 [../README §维度 1](../README.md)
> - **项目类型** external-product / internal-tool / framework-upgrade — 见 [../README §维度 2](../README.md)
> - **团队规模** solo / small / medium / large — 见 [../README §维度 3](../README.md)

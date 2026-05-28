# Phase 03 — 开发 Gate Checklist · 实例(dora · 0028 P0-3B follow-up)

> 实例文件,commit 后不可覆盖。模板:[../../Phase03-开发-Gate.md](../../Phase03-开发-Gate.md)
> **前一份(完整 Phase 03)**:[Phase03-开发-Gate-2026-05-27.md](Phase03-开发-Gate-2026-05-27.md)
> **本份**:专门覆盖 0028 P0-3B 真聚合 4 指标 + Quartz 任务 + SPI 扩展的增量验收。

## 头部信息

| 字段 | 值 |
|---|---|
| 模块名 | **DORA** · 分级 **L1** · Owner Wjl · 2026-05-28(增量)|
| 触发 | [proposal 0028 P0-3B](../../../proposals/0028-product-mainline-uplift-epic.md) — DORA 4 指标真聚合 + Quartz 每日 03:00 自动跑 |
| 关联 ADR | [ADR-0011 真聚合](../../../../03-开发/ADR/0011-testreport-dora-real-aggregation.md) + [ADR-0012 SPI 跨模块](../../../../03-开发/ADR/0012-spi-scoped-lookup-cross-module.md) |

## B. 必产出物 — 代码(0028 P0-3B 增量)

- [x] 后端 Domain 加 5 字段:`periodStart` / `periodEnd` / `periodDays` / `isComputed` / `computedAt`(commit `5f93f77`)
- [x] 新 SPI `DoraAggregationSource` 下沉 plm-common(同 P0-2A 范式)
- [x] 3 模块各 `@Component("pipeline"/"release"/"defect")` 实现 SPI
- [x] `DoraMetricServiceImpl.computeMetrics(projectId, periodStart, periodEnd)`:
  - 经 `Map<String, DoraAggregationSource>` 注入 + 按 key 取实现
  - 4 指标算法对齐实际字段命名:
    · `deploy_freq` = COUNT(pipeline.last_run_status='success' in period)
    · `lead_time` = AVG(release.released_at - create_time WHERE status IN ('02','03'))
    · `mttr` = AVG(update_time - create_time WHERE severity IN ('00','01') AND status='03')
    · `change_fail_rate` = COUNT(failed) / COUNT(success+failed) * 100
  - upsert 检查:`is_computed='N'` 跳过(尊重人工);`'Y'` 覆盖
- [x] 新 endpoint `POST /business/dora/refresh-compute?projectId=&periodDays=`
- [x] **Quartz `DoraComputeTask.computeAllProjects(30)`** cron='0 0 3 * * ?' 每日 03:00 全量
  - 单项目失败 try-catch 不阻塞其他 + log.error
- [x] 前端 dora 顶栏加项目 select + 「🔄 重新聚合 4 指标」按钮 + 历史表「来源」列(commit `9467bd1`)
- [x] SQL [business-dora-add-compute-fields.sql](../../../../plm-backend/sql/business-dora-add-compute-fields.sql) + rollback + 联合索引 `(project_id, metric_type, period_start)` + sys_job seed

### B.4 测试代码(Phase 03 准出核心)

- [x] **`mvn -pl plm-dora,plm-quartz,plm-pipeline,plm-release,plm-defect -am test` → 13 模块 BUILD SUCCESS**(`5f93f77` 合入证据)
- [x] plm-dora 28 passed(含本批 6 新 case)
- [x] DoraComputeMetricsTests 6 case @Nested:4 指标全算 / 无数据 / 人工跳过 (`is_computed='N'`)/ 自动覆盖 / MTTR contract / 入参校验
- [x] 配套 3 模块 SPI 单测 + Quartz Job 集成

### 字典差异登记(0028 P0-3B 实际 vs PRD 描述,以 SQL 为准)

| 维度 | PRD 描述 | 实际 SQL 字典码 |
|---|---|---|
| dora metric_type | deployment_frequency 等 | **deploy_freq / lead_time / mttr / change_fail_rate** |
| defect severity (聚合源) | blocker / critical | **00=P0 / 01=P1** |

## C. DoD(L1)

- [x] B.1-B.4 满足;28 passed + 13 模块 BUILD SUCCESS
- [x] Checklist 已 commit
- [x] PRD-MAPPING §30 已加 5 新字段 + 联合索引 + 漂移登记修订(`eb58ffd`)
- [x] ADR-0011 + ADR-0012 补记
- [x] 前份 Phase03-开发-Gate-2026-05-27.md 仍 valid(基础 22 case);本份是 +6 P0-3B 增量

## D. 签字

| 角色 | 姓名 | 结论 | 日期 |
|---|---|---|---|
| 开发 lead | Wjl | _待签_ | _待定_ |
| 测试 lead | Wjl(兼) | _待签(28/28 已绿)_ | _待定_ |

## E. 异常

| # | 项 | 补救 | 截止 |
|---|---|---|---|
| 1 | MTTR 用 update_time 近似(defect 无 resolvedAt) | P1 加 resolved_at + 触发器 | v0.2 |
| 2 | DORA 历史空表;首次 cron 跑会 INSERT 大量数据 | 预演 1 项目验证后再开 cron | 上线前 |
| 3 | 字典层 5 处漂移其中 2 处 (metric_type / period_days) 后端已修齐,前端待 0029 子任务 | proposal 0029 子任务 | proposal 0029 tracking |

## I. 进入 Phase 04 准出

- [x] 后端编译 + 单测 28/28 + 4 指标算法 6 case
- [ ] E2E spec — refresh-compute 按钮端到端 + 选项目 + 4 指标显示 — 待 Phase 04 §I 回填
- [ ] **Quartz DoraComputeTask 上线后观察周** — 启动前置:sys_job 默认 misfire 策略不丢任务;cron 失败入 sys_job_log 报警

## 修订记录

| 日期 | 修改人 | 原因 | 决议 |
|---|---|---|---|
| 2026-05-28 | Claude + Wjl | 0028 epic P0-3B follow-up,前份 2026-05-27 不动;本份是真聚合 + Quartz 增量验收 | _待签_ |

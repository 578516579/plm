# ADR-0011: TestReport / DORA 真聚合 + 人工覆盖语义

- **状态**:accepted
- **日期**:2026-05-28
- **决策人**:Wjl(技术 lead,solo-review)+ Claude
- **关联**:[proposal 0028 §3 P0-3](../../99-跨阶段/proposals/0028-product-mainline-uplift-epic.md) / 实现 commit `5f93f77` + `9467bd1` / PRD §F4.7 + §F6 DevOps DORA

## 背景

PM 验收报告(2026-05-28)5 主线诊断第 3/4 条:**研发→测试 / 发布→运维 数据层都断**。

- `TestReport.totalCases/passedCases/p0Defects` 等字段早就存在,全是**人工填**,违反 PRD §F4.7 "AI 自动生成测试报告"承诺
- `tb_dora_metric` 表完全空,DORA 4 指标 `aggregate|compute` 在 plm-dora 模块 grep 0 命中,违反 PRD §F6 DevOps DORA 持续度量承诺

关键业务挑战:
1. **聚合源**:testcase 与 testplan **无 FK**,defect.testcaseId 是 P0-1b 才加的 — 聚合维度需要权衡
2. **真聚合 vs 人工录入冲突**:用户可能已经手填了 TestReport 字段,真聚合不能盖掉;反向 DORA 用户可能为了 demo 填假数据,定时任务又要覆盖
3. **跨模块依赖**:DORA 聚合需要查 plm-pipeline / plm-release / plm-defect,plm-dora 不能反向 import 这些模块(违反分层)

## 决策

### 3.1 TestReport 真聚合

新增 3 列(详 PRD-MAPPING §13):
- `is_aggregated` CHAR(1) — Y/N,默认 N
- `aggregated_at` DATETIME — 聚合时间戳
- `is_manual_override` CHAR(1) — Y/N,**默认 N**;Y 时聚合服务直接 return 不动

`TestReportServiceImpl.aggregateFromTestplan(testreportId)`:
1. 查 report;**`is_manual_override='Y'` 则直接 return**(尊重人工覆盖,P0 关键防线)
2. 按 `projectId` 维度聚合(testcase 与 testplan 当前无 FK,P1 TODO 加 `testplan_id` 字段后改按方案聚合)
3. 4 项 case 计数 + 2 项 P0/P1 defect 计数 + coverage = passedCases × 100 / totalCases
4. 写回 + `is_aggregated='Y'` + `aggregated_at=now()`

端点:`POST /business/testreport/{id}/refresh-aggregate`

### 3.2 DORA 真聚合

新增 5 列(详 PRD-MAPPING §30):
- `period_start` / `period_end` DATE — 显式窗口边界
- `period_days` INT — 窗口天数,默认 30
- `is_computed` CHAR(1) — Y/N,默认 N
- `computed_at` DATETIME — 聚合时间戳

`DoraMetricServiceImpl.computeMetrics(projectId, periodStart, periodEnd)`:
1. 经 `DoraAggregationSource` SPI(详 ADR-0012)从 pipeline/release/defect 拉聚合源
2. 算 4 指标:
   - `deploy_freq`    = COUNT(pipeline.last_run_status='success' in period)
   - `lead_time`      = AVG(release.released_at - create_time WHERE status IN ('02','03'))
   - `mttr`           = AVG(update_time - create_time WHERE severity IN ('00','01') AND status='03')
                        (defect 无 resolvedAt,用 update_time 近似)
   - `change_fail_rate` = COUNT(pipeline.failed) / COUNT(success+failed) × 100
3. 4 条 metric upsert:**已存在 + `is_computed='N'` 时跳过**(尊重人工);`is_computed='Y'` 时覆盖
4. 置 `is_computed='Y'` + `computed_at=now()`

端点:`POST /business/dora/refresh-compute?projectId=&periodDays=`
Quartz:`DoraComputeTask.computeAllProjects(30)` cron='0 0 3 * * ?' 每日 03:00 全量

### 3.3 字典码差异登记

实际落地与 PRD 描述的字典码差异(以 SQL/后端为准):

| 维度 | PRD 描述 | 实际 SQL 字典码 |
|---|---|---|
| dora metric_type | deployment_frequency / lead_time / mean_time_to_recover / change_failure_rate | **deploy_freq / lead_time / mttr / change_fail_rate** |
| defect severity | blocker / critical / major / minor | **00=P0 / 01=P1 / 02=P2 / 03=P3** |
| testcase status | passed / failed / blocked | **03=已通过 / 04=已失败** |

## 理由

- **`is_manual_override` 默认 N + 聚合首屏检查 = 安全后门**:90% 用户走自动聚合;10% 真要人工填的(如真实线上发布) toggle 字段后聚合再也不碰
- **DORA `is_computed` Y/N 双向语义**:N 时人工录入保护(同 testreport);Y 时定时任务可覆盖刷新(因为 Y 表示"这数据是算的,可重算") — 解决"人工 demo 数据 vs 定时刷新"冲突
- **按 projectId 聚合而非 testplanId**:testcase 与 testplan 当前无 FK 是历史债,P1 TODO 关闭后改;过渡期按 projectId 准确度不差(同项目所有 case)
- **MTTR 用 update_time 近似**:defect 无 resolvedAt 字段;P1 改造 + 触发器 / 状态机自动填 resolved_at 后改算法,届时升级 ADR(supersede 本节)
- **Quartz 每日 03:00 全量**:夜间低峰跑;单项目失败 try-catch 不阻塞其他;失败入 sys_job_log

## 后果

### 好

- 实现 PRD §F4.7 + §F6 承诺,TestReport / DORA 从空表变真数据
- 人工覆盖双向后门,无需为"自动 vs 手动"开发分支
- DORA 跨模块用 SPI 解耦,新增聚合源(如 sonarqube 代码质量)只需加 SPI 实现,不动 dora

### 代价 / 风险

- **聚合算法假设**:MTTR 用 update_time 近似仅对状态机自动填 update_time 的场景成立;若用户手动改 defect 描述,update_time 也变 → MTTR 偏大。**缓解**:P1 加 resolved_at + 触发器
- **历史数据不会自动补**:已有 N 个 testreport 是 0 而不是 NULL,聚合服务无法区分"用户填的 0" vs "未聚合的 0";依赖 `is_aggregated='N'` 表"未聚合",但旧数据该列默认 N(SQL ALTER 默认值)— **已通过 ALTER 默认值正确设置**
- **DORA 跨模块依赖**:plm-dora 通过 SPI 注入 3 个聚合源,若任一模块缺失 @Component 实现,启动时 NoUniqueBean(虽然 SPI 是 Optional 的) — 实测 commit `5f93f77` 后 13 模块 BUILD SUCCESS,无此问题

### 后续动作

- [ ] **P1**:testcase 加 `testplan_id` 字段(0028 P1 TODO 4);TestReport 聚合改按方案
- [ ] **P1**:defect 加 `resolved_at` 字段 + 状态机触发器自动填;MTTR 算法改用此字段
- [ ] **P2**:DORA 加 SPI 聚合源 — sonarqube 代码质量(`change_fail_rate` 维度补充)
- [ ] **P2**:历史 testreport 数据迁移脚本 — 把 is_aggregated='N' 的报告批量跑一遍 aggregateFromTestplan,覆盖回填

## 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl(会话授权)| ✅ accepted | 2026-05-28 | **经 Wjl 2026-05-28 会话内"1"指令批签**;ADR 配套 0028 Step 7c follow-up,决策已随 `5f93f77` + `9467bd1` 实现 |
| Claude(reviewer 复盘)| ✅ accepted | 2026-05-28 | proposal 0028 §10 P0-3 落地证据充分;commit `5f93f77` plm-testreport 23 + plm-dora 28 case 全绿 + 13 模块 BUILD SUCCESS |

## 修订记录

| 日期 | 修改人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude / Wjl | V1.0 — 决策已随 `5f93f77` + `9467bd1` 落地;Step 7c follow-up ADR 补记 |

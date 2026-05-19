---
name: data-etl-pipeline
description: PLM ETL 管道设计 — 增量/全量策略 + Quartz/scheduled 调度 + 错误处理 (重试/死信). 用户说"ETL / 数据管道 / 增量同步 / 调度任务 / Quartz job / batch / watermark"时调用. 输出: 03-开发/ETL-pipeline-<name>.md. **data-engineer agent 的子工具**。
---

# data-etl-pipeline — ETL 管道设计 skill v0.1

**data-engineer agent §2.2 配套**。

## 1. 何时调用
- "ETL / 数据管道 / 增量 / 调度 / Quartz / watermark / cdc"
- data-engineer §2.2 触发, Phase 02 末

## 2. 步骤

### 2.1 选增量策略
- watermark 字段 (update_time) — 简单, 漏 hard-delete
- cdc (binlog / Debezium) — 全, 部署重
- snapshot 全量 — 仅初始化

### 2.2 调度
- PLM 默认: Quartz (plm-quartz 模块, sys_job 表)
- 频率: 业务报表 5-15 min, 月度汇总 daily

### 2.3 错误处理
- 重试: 指数退避, 最多 3 次
- 死信: 失败行写入 etl_dead_letter 表
- 告警: 失败率 > 5% 触发

### 2.4 性能基线
- 单批次 ≤ N min (报表场景 ≤ 5 min)
- 数据量增长 50% 后重测

## 3. 输出模板
```markdown
# ETL Pipeline: <name>

| 字段 | 值 |
|---|---|
| 源表 | tb_<name> |
| 目标 | fact_/dim_ |
| 频率 | every 15 min |
| 增量策略 | watermark on update_time |
| 重试 | 指数退避 (1s/3s/10s) max 3 |
| 死信 | etl_dead_letter |
| 性能基线 | < 5 min / 10万行 |
```

## 4. 衔接
- 上游: data-model-design (fact/dim 设计)
- 下游: backend-coder Quartz Job 实现

## 5. 历史
| v0.1 | 2026-05-19 | 首版; data-engineer 配套 2/4 |

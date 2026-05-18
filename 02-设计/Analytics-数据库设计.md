# Analytics 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Analytics-PRD.md](../01-立项/Analytics-PRD.md) |
| 表 | `tb_analytics_snapshot` |
| 编号规则 | `AS-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-analytics.sql](../plm-backend/sql/business-analytics.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Analytics"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `analytics` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_analytics_snapshot : "author"
    sys_dict_data ||--o{ tb_analytics_snapshot : "period_type/status"
    tb_project ||--o{ tb_analytics_snapshot : "optional (NULL=全局)"
    tb_analytics_snapshot {
        bigint snapshot_id PK
        varchar snapshot_no UK "AS-YYYY-NNNN"
        bigint project_id FK "可空=全局快照"
        varchar title
        varchar period_type "month/quarter/year"
        date snapshot_date
        int requirement_throughput
        decimal sprint_on_time_rate
        decimal defect_density
        decimal auto_test_coverage
        decimal deployment_frequency
        decimal lead_time_hours
        decimal mttr_hours
        decimal change_failure_rate
        decimal ai_hours_saved
        int active_projects
        int projects_at_risk
        longtext ai_recommendations
        char ai_generated "Y/N"
        datetime ai_generated_at
        varchar status "字典 biz_analytics_status 3态"
        bigint author_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-analytics-rollback.sql && mysql plm < sql/business-analytics.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 大规模(效能类)。按 5 个项目 × 12 月度快照 + 4 季度快照 + 1 年度快照 = 85 行/年/项目,5 个项目共 425 行/年,5 年累计 2125 行,加全局快照 < 1 万行。但 `ai_recommendations` LONGTEXT 单行 10-30KB,且高频查询(dashboard 每分钟聚合),需按 `snapshot_date` 范围索引 + 考虑 RANGE BY YEAR(snapshot_date) 分区(>2 年快照归档)。

# Dora 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Dora-PRD.md](../01-立项/Dora-PRD.md) |
| 表 | `tb_dora_metric` |
| 编号规则 | `DORA-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-dora.sql](../plm-backend/sql/business-dora.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Dora"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `dora` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_dora_metric : "author"
    sys_dict_data ||--o{ tb_dora_metric : "type/period/status"
    tb_project ||--o{ tb_dora_metric : "optional (NULL=全局)"
    tb_dora_metric {
        bigint dora_id PK
        varchar dora_no UK "DORA-YYYY-NNNN"
        bigint project_id FK "可空=全局"
        varchar metric_name
        varchar metric_type "deploy_freq/lead_time/mttr/change_fail_rate"
        decimal metric_value
        varchar metric_unit
        varchar period_type "month/quarter"
        date snapshot_date
        longtext trend_chart_json
        longtext heatmap_json
        longtext leadtime_breakdown
        longtext ai_suggestions
        char ai_generated "Y/N"
        datetime ai_generated_at
        varchar status "字典 biz_dora_status 3态"
        bigint author_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-dora-rollback.sql && mysql plm < sql/business-dora.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 大规模(效能/DORA 类)。按 5 个项目 × 4 指标 × 12 月度快照 + 4 季度快照 = 320 行/年/项目,5 项目共 1600 行/年,加全局快照 1700 行/年,5 年累计 8500 行。`trend_chart_json` / `heatmap_json` / `leadtime_breakdown` LONGTEXT 各 3-10KB,加 `ai_suggestions` 20KB,单行总 50KB。需 RANGE BY YEAR(snapshot_date) 分区(>2 年归档),(metric_type, snapshot_date) 复合索引覆盖。

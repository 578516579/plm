# Testreport 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Testreport-PRD.md](../01-立项/Testreport-PRD.md) |
| 表 | `tb_testreport` |
| 编号规则 | `TR-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-testreport.sql](../plm-backend/sql/business-testreport.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Testreport"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `testreport` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_testreport : "reviewer"
    sys_dict_data ||--o{ tb_testreport : "risk_level/status"
    tb_project ||--o{ tb_testreport : "belongs_to"
    tb_sprint ||--o{ tb_testreport : "optional"
    tb_testplan ||--o{ tb_testreport : "based_on"
    tb_testreport {
        bigint testreport_id PK
        varchar testreport_no UK "TR-YYYY-NNNN"
        bigint project_id FK
        bigint sprint_id FK
        bigint testplan_id FK
        varchar title
        int total_cases
        int passed_cases
        int failed_cases
        decimal coverage_rate
        text defect_summary "JSON"
        int p0_defects
        int p1_defects
        int p2_defects
        varchar risk_level "green/amber/red"
        text risk_evaluation
        text recommendations
        char ai_generated "Y/N"
        varchar status "字典 biz_testreport_status 3态"
        datetime generated_at
        bigint reviewer_user_id FK
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-testreport-rollback.sql && mysql plm < sql/business-testreport.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 中规模(质量类)。按 5 个项目 × 26 迭代/年 × 1 测试报告/迭代 = 130 行/年估算,5 年累计 < 1000 行。`defect_summary` JSON 1-5KB,`risk_evaluation` / `recommendations` 各 TEXT ~1-2KB。索引覆盖 testplan_id / status / risk_level。无需分区。

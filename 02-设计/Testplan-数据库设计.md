# Testplan 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Testplan-PRD.md](../01-立项/Testplan-PRD.md) |
| 表 | `tb_testplan` |
| 编号规则 | `TP-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-testplan.sql](../plm-backend/sql/business-testplan.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Testplan"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `testplan` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_testplan : "author"
    sys_dict_data ||--o{ tb_testplan : "status"
    tb_project ||--o{ tb_testplan : "belongs_to"
    tb_sprint ||--o{ tb_testplan : "optional"
    tb_testplan {
        bigint testplan_id PK
        varchar testplan_no UK "TP-YYYY-NNNN"
        bigint project_id FK
        bigint sprint_id FK "可空"
        varchar title
        varchar test_types "CSV 5 类型"
        int test_cycle_days
        text scope
        text strategy
        text tools_recommended
        text resources_plan
        text risk_assessment
        char ai_generated "Y/N"
        varchar status "字典 biz_testplan_status 4态"
        bigint author_user_id FK
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-testplan-rollback.sql && mysql plm < sql/business-testplan.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 中规模(质量类)。按 5 个项目 × 26 迭代/年 × 1 测试方案/迭代 = 130 行/年估算,5 年累计 < 1000 行。`strategy`/`tools_recommended`/`resources_plan`/`risk_assessment` 各 TEXT 1-3KB,合计单行 ~10KB。索引覆盖 project_id / sprint_id / status。无需分区。

# Dbdesign 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Dbdesign-PRD.md](../01-立项/Dbdesign-PRD.md) |
| 表 | `tb_dbdesign` |
| 编号规则 | `DB-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-dbdesign.sql](../plm-backend/sql/business-dbdesign.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Dbdesign"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `dbdesign` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_dbdesign : "author / reviewer"
    sys_dict_data ||--o{ tb_dbdesign : "engine/status"
    tb_project ||--o{ tb_dbdesign : "belongs_to"
    tb_arch ||--o{ tb_dbdesign : "optional ref"
    tb_dbdesign {
        bigint dbdesign_id PK
        varchar dbdesign_no UK "DB-YYYY-NNNN"
        bigint project_id FK
        bigint arch_id FK "可选"
        varchar title
        varchar db_engine "字典 biz_dbdesign_engine"
        longtext er_diagram_content "Mermaid"
        longtext data_dictionary
        longtext ddl_script
        text normalization_check
        char ai_generated "Y/N"
        datetime ai_generated_at
        varchar status "字典 biz_dbdesign_status 4态"
        bigint author_user_id FK
        bigint reviewer_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-dbdesign-rollback.sql && mysql plm < sql/business-dbdesign.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 小规模(设计类)。按 5 个项目 × 3 数据库方案/项目 = 15 行/年估算,5 年累计 < 100 行,表大小 < 50 MB。`er_diagram_content` / `ddl_script` / `data_dictionary` LONGTEXT 单行 30-100KB,DDL 集合通常含 20-50 表 CREATE 语句。无需分区。

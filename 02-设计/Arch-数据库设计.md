# Arch 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Arch-PRD.md](../01-立项/Arch-PRD.md) |
| 表 | `tb_arch` |
| 编号规则 | `ARCH-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-arch.sql](../plm-backend/sql/business-arch.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Arch"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `arch` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_arch : "author / reviewer"
    sys_dict_data ||--o{ tb_arch : "arch_mode/stack/db/ai/deploy/iot/status"
    tb_project ||--o{ tb_arch : "belongs_to"
    tb_prd ||--o{ tb_arch : "optional ref"
    tb_arch {
        bigint arch_id PK
        varchar arch_no UK "ARCH-YYYY-NNNN"
        bigint project_id FK
        bigint prd_id FK "可选"
        varchar title
        varchar arch_mode "字典 biz_arch_mode"
        varchar primary_stack "字典 biz_arch_stack"
        varchar database_choice "字典 biz_arch_database"
        varchar ai_orchestration "字典 biz_arch_ai_engine"
        varchar deployment_type
        varchar iot_protocol
        longtext design_content
        longtext c4_diagram_content "Mermaid C4"
        text nfr_mapping
        char ai_generated "Y/N"
        datetime ai_generated_at
        varchar status "字典 biz_arch_status 4态"
        bigint author_user_id FK
        bigint reviewer_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-arch-rollback.sql && mysql plm < sql/business-arch.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 小规模(设计类)。按 5 个项目 × 2 架构方案/项目 = 10 行/年估算,5 年累计 < 100 行,表大小 < 50 MB。`design_content` / `c4_diagram_content` LONGTEXT 单行平均 30-80KB (Mermaid 全图)。无需分区,索引页面 < 20。

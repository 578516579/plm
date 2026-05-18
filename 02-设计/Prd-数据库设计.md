# Prd 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Prd-PRD.md](../01-立项/Prd-PRD.md) |
| 表 | `tb_prd` |
| 编号规则 | `PRD-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-prd.sql](../plm-backend/sql/business-prd.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Prd"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `prd` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_prd : "author / reviewer"
    sys_dict_data ||--o{ tb_prd : "scene/target_user/status"
    tb_project ||--o{ tb_prd : "belongs_to"
    tb_prd {
        bigint prd_id PK
        varchar prd_no UK "PRD-YYYY-NNNN"
        bigint project_id FK
        varchar title "功能名称"
        text description
        varchar scene_template "字典 biz_prd_scene"
        varchar target_user "字典 biz_prd_target_user"
        longtext content "AI 7 段 Markdown"
        decimal completeness_score "0-100"
        varchar version "v1.0"
        char ai_generated "Y/N"
        datetime ai_generated_at
        varchar status "字典 biz_prd_status 4态"
        bigint author_user_id FK
        bigint reviewer_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-prd-rollback.sql && mysql plm < sql/business-prd.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 小规模(立项类)。按 5 个项目 × 20 PRD/项目 = 100 行/年估算,5 年累计 < 1 万行,表大小 < 100 MB。`content` LONGTEXT 单行平均 20-50KB,`completeness_score` 派生字段无需独立索引。无需分区。

# ManualProduct 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [ManualProduct-PRD.md](../01-立项/ManualProduct-PRD.md) |
| 表 | `tb_manual_product` |
| 编号规则 | `PM-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-manual-product.sql](../plm-backend/sql/business-manual-product.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "ManualProduct"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `manual-product` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_manual_product : "author"
    sys_dict_data ||--o{ tb_manual_product : "status"
    tb_project ||--o{ tb_manual_product : "belongs_to"
    tb_manual_product {
        bigint manualproduct_id PK
        varchar manualproduct_no UK "PM-YYYY-NNNN"
        bigint project_id FK
        varchar title
        varchar product_version "v1.0"
        varchar include_modules "CSV 5 项"
        longtext content "Markdown"
        text screenshots_urls "CSV"
        int screenshots_count
        varchar output_formats "CSV"
        char ai_generated "Y/N"
        datetime generated_at
        varchar status "字典 biz_manualproduct_status 4态"
        bigint author_user_id FK
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-manual-product-rollback.sql && mysql plm < sql/business-manual-product.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 小规模(文档类)。按 5 个项目 × 3 产品手册版本/项目 = 15 行/年估算,5 年累计 < 100 行,表大小 < 50 MB。`content` LONGTEXT 单行 30-100KB(含 5 个章节 + 内嵌图链)。无需分区,索引覆盖 project_id / status。

# Ued 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Ued-PRD.md](../01-立项/Ued-PRD.md) |
| 表 | `tb_ued` |
| 编号规则 | `UED-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-ued.sql](../plm-backend/sql/business-ued.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Ued"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `ued` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_ued : "designer / reviewer"
    sys_dict_data ||--o{ tb_ued : "status"
    tb_requirement ||--o{ tb_ued : "optional ref"
    tb_ued {
        bigint ued_id PK
        varchar ued_no UK "UED-YYYY-NNNN"
        varchar title "设计稿名称"
        varchar figma_url
        varchar figma_file_key "MCP 集成入口"
        varchar version_label
        varchar preview_url
        text annotation_content "标注 JSON"
        longtext review_report "AI 评审 Markdown"
        decimal review_score "0-100"
        text compliance_check "JSON"
        text usability_issues
        varchar agri_component_tags "CSV"
        bigint requirement_id FK
        char ai_generated "Y/N"
        datetime ai_generated_at
        varchar status "字典 biz_ued_status 4态"
        bigint designer_user_id FK
        bigint reviewer_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-ued-rollback.sql && mysql plm < sql/business-ued.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 小规模(设计类)。按 5 个项目 × 20 设计稿/项目 = 100 行/年估算,5 年累计 < 1000 行,表大小 < 100 MB。`review_report` LONGTEXT 单行 10-30KB(AI 评审报告),`annotation_content` / `compliance_check` JSON 各 ~2KB。无需分区。

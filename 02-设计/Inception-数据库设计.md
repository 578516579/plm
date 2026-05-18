# Inception 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Inception-PRD.md](../01-立项/Inception-PRD.md) |
| 表 | `tb_inception` |
| 编号规则 | `INC-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-inception.sql](../plm-backend/sql/business-inception.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Inception"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `inception` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_inception : "submitter / approver"
    sys_dict_data ||--o{ tb_inception : "business_line/inception_type/status"
    tb_inception ||--o| tb_project : "approved → 转项目"
    tb_inception {
        bigint inception_id PK
        varchar inception_no UK "INC-YYYY-NNNN 唯一"
        varchar project_name "项目名称"
        varchar business_line "字典 biz_inception_biz_line"
        varchar inception_type "字典 biz_inception_type"
        text background
        int estimated_duration_months
        varchar estimated_team
        char ai_generated "Y/N"
        longtext ai_proposal_content
        text ai_risks
        datetime ai_generated_at
        varchar status "字典 biz_inception_status 5态"
        varchar reject_reason "status=04 必填"
        bigint submitter_user_id FK
        bigint approver_user_id FK
        datetime approved_at
        bigint project_id FK "转项目后回填"
        datetime create_time
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-inception-rollback.sql && mysql plm < sql/business-inception.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 小规模(立项类)。按 5 个项目 × 5 立项尝试/年 ≈ 25 行/年估算,5 年累计 < 1 万行,表大小 < 100 MB。`ai_proposal_content` LONGTEXT 单行平均 8-20KB,`ai_risks`/`background` TEXT 各 ~2KB,综合每行 < 30KB。单表索引页面 < 50,无需分区。

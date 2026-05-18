# Submission 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Submission-PRD.md](../01-立项/Submission-PRD.md) |
| 表 | `tb_submission` |
| 编号规则 | `SUB-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-submission.sql](../plm-backend/sql/business-submission.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Submission"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `submission` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_submission : "submitter / reviewer"
    sys_dict_data ||--o{ tb_submission : "environment/status"
    tb_project ||--o{ tb_submission : "belongs_to"
    tb_sprint ||--o{ tb_submission : "optional"
    tb_submission {
        bigint submission_id PK
        varchar submission_no UK "SUB-YYYY-NNNN"
        bigint project_id FK
        bigint sprint_id FK
        varchar title
        text scope
        varchar environment "test/pre/dev/staging/prod"
        int expected_test_days
        text risk_notes
        decimal unit_test_coverage "≥60"
        char code_scan_passed "Y/N"
        char prd_completed "Y/N"
        char api_doc_updated "Y/N"
        char quality_gate_passed "服务端计算"
        varchar status "字典 biz_submission_status 5态"
        varchar reject_reason "status=04 必填"
        bigint submitter_user_id FK
        bigint reviewer_user_id FK
        datetime submitted_at
        datetime approved_at
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-submission-rollback.sql && mysql plm < sql/business-submission.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 中规模(质量门禁类)。按 5 个项目 × 26 迭代/年 × 1 提测/迭代 = 130 行/年估算,5 年累计 < 1000 行。每行字段 ≤ 500 字节 + 文本字段 ~5KB,表大小 < 100 MB。需 (project_id, sprint_id, status) 复合索引覆盖高频"待审批"查询。无需分区。

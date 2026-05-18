# Openspec 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Openspec-PRD.md](../01-立项/Openspec-PRD.md) |
| 表 | `tb_openspec` |
| 编号规则 | `SPEC-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-openspec.sql](../plm-backend/sql/business-openspec.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Openspec"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `openspec` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_openspec : "author"
    sys_dict_data ||--o{ tb_openspec : "spec_type/status"
    tb_openspec {
        bigint openspec_id PK
        varchar openspec_no UK "SPEC-YYYY-NNNN"
        varchar spec_name
        varchar spec_type "字典 biz_openspec_type 4类"
        varchar description
        longtext spec_content "YAML/JSON"
        varchar version "语义化"
        varchar agri_kb_ref "x-agrikb-ref"
        char ai_generated "Y/N"
        datetime ai_generated_at
        varchar status "字典 biz_openspec_status 3态"
        bigint author_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

**唯一键**: `UNIQUE(spec_name, version)` — 同名规范同版本禁重复 (701)。

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-openspec-rollback.sql && mysql plm < sql/business-openspec.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 小规模(配置/设计类)。按 5 个项目 × 20 规范/项目 × 平均 3 版本 = 300 行/年估算,5 年累计 < 2000 行,表大小 < 100 MB。`spec_content` LONGTEXT 单行 5-30KB(OpenAPI/AsyncAPI 完整规范)。无需分区,索引覆盖 spec_type / status / (spec_name, version)。

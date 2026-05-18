# Apidesign 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Apidesign-PRD.md](../01-立项/Apidesign-PRD.md) |
| 表 | `tb_apidesign` |
| 编号规则 | `APID-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-apidesign.sql](../plm-backend/sql/business-apidesign.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Apidesign"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `apidesign` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_apidesign : "author / reviewer"
    sys_dict_data ||--o{ tb_apidesign : "status"
    tb_project ||--o{ tb_apidesign : "belongs_to"
    tb_arch ||--o{ tb_apidesign : "optional ref"
    tb_apidesign {
        bigint apidesign_id PK
        varchar apidesign_no UK "APID-YYYY-NNNN"
        bigint project_id FK
        bigint arch_id FK "可选"
        varchar title
        varchar http_method "GET/POST/PUT/DELETE/PATCH"
        varchar path "/api/v1/..."
        text description
        text request_schema
        text response_schema
        longtext openapi_spec "YAML"
        char mock_enabled "Y/N"
        text mock_response
        char ai_generated "Y/N"
        datetime ai_generated_at
        varchar status "字典 biz_apidesign_status 4态"
        bigint author_user_id FK
        bigint reviewer_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

**唯一键**: `UNIQUE(project_id, http_method, path)` — 重复 method+path 抛 701。

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-apidesign-rollback.sql && mysql plm < sql/business-apidesign.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 小规模(设计类)。按 5 个项目 × 50 接口/项目 = 250 行/年估算,5 年累计 < 1500 行,表大小 < 100 MB。`openapi_spec` LONGTEXT 单行 5-20KB,`request_schema`/`response_schema` 各 1-3KB。无需分区,索引页面 < 30。

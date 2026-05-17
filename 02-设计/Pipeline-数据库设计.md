# Pipeline 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Pipeline-PRD.md](../01-立项/Pipeline-PRD.md) |
| 表 | `tb_pipeline` |
| 编号规则 | `PIPE-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-pipeline.sql](../plm-backend/sql/business-pipeline.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "Pipeline"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `pipeline` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)
<待人工填写>:简单 mermaid ER 图,标注 FK 关联

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-pipeline-rollback.sql && mysql plm < sql/business-pipeline.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估
<待人工填写>

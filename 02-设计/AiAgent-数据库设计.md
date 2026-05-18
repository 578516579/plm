# AiAgent 模块 — 数据库设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [AiAgent-PRD.md](../01-立项/AiAgent-PRD.md) |
| 表 | `tb_ai_agent` |
| 编号规则 | `AGT-YYYY-NNNN` |
| 完整 DDL | [plm-backend/sql/business-ai-agent.sql](../plm-backend/sql/business-ai-agent.sql) |
| DBA review | Wjl ✅ (solo) |

## 1. 字段对照表

**单一事实来源**: [PRD-MAPPING.md §2 "AiAgent"](../PRD-MAPPING.md)。本文件**不重复字段表**,字段定义任何 drift 修复走 §M.2 流程。

## 2. 状态机字典

见 [PRD-MAPPING.md §3 状态机汇总](../PRD-MAPPING.md) 的 `ai-agent` 行;SQL 字典数据见 SQL 文件 `sys_dict_data` 段。

## 3. 索引设计

详见 SQL 文件 `PRIMARY KEY` / `UNIQUE KEY` / `KEY` 定义。

## 4. 关系图 (ER)

```mermaid
erDiagram
    sys_user ||--o{ tb_ai_agent : "author"
    sys_dict_data ||--o{ tb_ai_agent : "agent_type/status"
    tb_ai_agent {
        bigint agent_id PK
        varchar agent_no UK "AGT-YYYY-NNNN"
        varchar agent_name
        varchar agent_type "字典 biz_aiagent_type 6类"
        varchar description
        longtext prompt_template
        varchar dify_workflow_id "外部 Dify"
        longtext config_json
        bigint total_calls
        decimal success_rate
        datetime last_invoked_at
        varchar status "字典 biz_aiagent_status 3态"
        bigint author_user_id FK
        datetime create_time
        char del_flag "0/2"
    }
```

## 5. 数据迁移
dev 环境:`mysql plm < sql/business-ai-agent-rollback.sql && mysql plm < sql/business-ai-agent.sql`。
生产部署:留 v1.0 GA 前补。

## 6. 容量预估

**分级**: 大规模(AI 类)。按 30 个 Agent 注册(静态低增长),但 `total_calls` 累加每天 1000 次调用,5 年累计 single agent 调用记录 (调用日志独立表) ≈ 180 万行。本表只存 Agent 元数据 + 移动统计,行数 < 1000,但 `prompt_template`/`config_json` LONGTEXT 各 5-20KB,且 `total_calls`/`success_rate` 高频更新(需考虑写入热点 + 行锁优化)。

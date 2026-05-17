# AiAgent 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [AiAgent-PRD.md](../01-立项/AiAgent-PRD.md) |
| Base URL | `/business/ai-agent` |
| 权限串前缀 | `business:ai-agent:*` |
| Controller | [plm-backend/plm-ai-agent/.../AiAgentController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/ai-agent/list` | `business:ai-agent:list` | 列表 (分页+搜索) |
| GET | `/business/ai-agent/{id}` | `business:ai-agent:query` | 详情 |
| POST | `/business/ai-agent` | `business:ai-agent:add` | 新建 |
| PUT | `/business/ai-agent` | `business:ai-agent:edit` | 修改 |
| DELETE | `/business/ai-agent/{ids}` | `business:ai-agent:remove` | 逻辑删除 |
| POST | `/business/ai-agent/export` | `business:ai-agent:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "AiAgent"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点
<待人工填写>:如有非 CRUD 端点 (e.g. /execute / /run / /ai/generate)

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

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/ai-agent/{id}/transit` | `business:ai-agent:edit` | 状态机推进 (00↔01 / 02↔{00,01}),错误态可重启或停用 |
| POST | `/business/ai-agent/{id}/invoke` | `business:ai-agent:edit` | 调用 Agent (转发到 `dify_workflow_id`,接受 `input` JSON 参数,自动 `total_calls++` + 移动平均 `success_rate`,回写 `last_invoked_at`) |
| POST | `/business/ai-agent/register` | `business:ai-agent:add` | 注册新 Agent (与 `POST /` 区别:同步绑定到 Dify 工作流 ID 并校验工作流存在) |
| POST | `/business/ai-agent/{id}/unregister` | `business:ai-agent:remove` | 注销 Agent (解绑 Dify + 置 status=01 已停止,保留历史调用记录) |
| GET  | `/business/ai-agent/{id}/metrics` | `business:ai-agent:query` | 取 Agent 实时调用指标 (today_calls / success_rate / 错误堆栈最近 N 条) |

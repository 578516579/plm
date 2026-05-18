# ManualOps 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [ManualOps-PRD.md](../01-立项/ManualOps-PRD.md) |
| Base URL | `/business/manual-ops` |
| 权限串前缀 | `business:manual-ops:*` |
| Controller | [plm-backend/plm-manual-ops/.../ManualOpsController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/manual-ops/list` | `business:manual-ops:list` | 列表 (分页+搜索) |
| GET | `/business/manual-ops/{id}` | `business:manual-ops:query` | 详情 |
| POST | `/business/manual-ops` | `business:manual-ops:add` | 新建 |
| PUT | `/business/manual-ops` | `business:manual-ops:edit` | 修改 |
| DELETE | `/business/manual-ops/{ids}` | `business:manual-ops:remove` | 逻辑删除 |
| POST | `/business/manual-ops/export` | `business:manual-ops:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "ManualOps"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/manual-ops/{id}/transit` | `business:manual-ops:edit` | 状态机推进 (00→01→02→{00,03} 含反向边 02→00) |
| POST | `/business/manual-ops/ai/generate/{id}` | `business:manual-ops:edit` | AI 生成运维手册 (调用 §6 `ops-manual-flow`,按 `monitoring_plan` + `alert_channels` + `iot_device_types` 生成 5 章节含 IoT 巡检 SLA) |
| GET  | `/business/manual-ops/{id}/export?format=pdf` | `business:manual-ops:export` | 导出运维手册 (支持 word/pdf/html/markdown) |
| GET  | `/business/manual-ops/{id}/versions` | `business:manual-ops:query` | 取手册历史版本列表 |

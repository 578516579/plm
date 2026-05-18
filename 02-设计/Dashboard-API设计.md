# Dashboard 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Dashboard-PRD.md](../01-立项/Dashboard-PRD.md) |
| Base URL | `/business/dashboard` |
| 权限串前缀 | `business:dashboard:*` |
| Controller | [plm-backend/plm-dashboard/.../DashboardController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/dashboard/list` | `business:dashboard:list` | 列表 (分页+搜索) |
| GET | `/business/dashboard/{id}` | `business:dashboard:query` | 详情 |
| POST | `/business/dashboard` | `business:dashboard:add` | 新建 |
| PUT | `/business/dashboard` | `business:dashboard:edit` | 修改 |
| DELETE | `/business/dashboard/{ids}` | `business:dashboard:remove` | 逻辑删除 |
| POST | `/business/dashboard/export` | `business:dashboard:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Dashboard"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET  | `/business/dashboard/aggregate?ownerUserId={uid}` | `business:dashboard:query` | 聚合查询 6 类 widget 数据 (stats / activeProjects / myTodos / qualitySnapshot / aiMetrics / lifecycle),默认刷新间隔 60 秒 |
| PUT  | `/business/dashboard/{id}/config` | `business:dashboard:edit` | 更新 widget 配置 (`widget_types` CSV / `layout_json` / `refresh_interval`) |
| POST | `/business/dashboard/{id}/set-default` | `business:dashboard:edit` | 设为默认工作台 (置 `is_default='Y'`,Service 自动清同 owner 其他默认) |
| GET  | `/business/dashboard/realtime?ownerUserId={uid}` | `business:dashboard:query` | 实时数据流 (SSE / WebSocket 长连接,推送 widget 增量更新) |

# Analytics 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Analytics-PRD.md](../01-立项/Analytics-PRD.md) |
| Base URL | `/business/analytics` |
| 权限串前缀 | `business:analytics:*` |
| Controller | [plm-backend/plm-analytics/.../AnalyticsController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/analytics/list` | `business:analytics:list` | 列表 (分页+搜索) |
| GET | `/business/analytics/{id}` | `business:analytics:query` | 详情 |
| POST | `/business/analytics` | `business:analytics:add` | 新建 |
| PUT | `/business/analytics` | `business:analytics:edit` | 修改 |
| DELETE | `/business/analytics/{ids}` | `business:analytics:remove` | 逻辑删除 |
| POST | `/business/analytics/export` | `business:analytics:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Analytics"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/analytics/{id}/transit` | `business:analytics:edit` | 状态机推进 (00→01→02 单向终态),违反抛 601 |
| POST | `/business/analytics/refresh` | `business:analytics:edit` | 快照刷新 (按 `period_type` 计算最新 DORA + PLM 14 项指标,创建新行 status=00) |
| POST | `/business/analytics/ai/recommend/{id}` | `business:analytics:edit` | AI 改进建议 (调用 §6 `analytics-recommend-flow`,基于 4 维度阈值生成 `ai_recommendations`) |
| GET  | `/business/analytics/kpi-trend?metric=&periodType=` | `business:analytics:query` | KPI 趋势曲线 (按指标名 + 周期返回 JSON,用于 ECharts 渲染) |

# Competitive 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Competitive-PRD.md](../01-立项/Competitive-PRD.md) |
| Base URL | `/business/competitive` |
| 权限串前缀 | `business:competitive:*` |
| Controller | [plm-backend/plm-competitive/.../CompetitiveController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/competitive/list` | `business:competitive:list` | 列表 (分页+搜索) |
| GET | `/business/competitive/{id}` | `business:competitive:query` | 详情 |
| POST | `/business/competitive` | `business:competitive:add` | 新建 |
| PUT | `/business/competitive` | `business:competitive:edit` | 修改 |
| DELETE | `/business/competitive/{ids}` | `business:competitive:remove` | 逻辑删除 |
| POST | `/business/competitive/export` | `business:competitive:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Competitive"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/competitive/{id}/transit` | `business:competitive:edit` | 状态机推进 (00→01→02 单向),违反抛 601 |
| POST | `/business/competitive/ai/analyze/{id}` | `business:competitive:edit` | AI 综合分析 (调用 §6 `competitive-analysis-flow`,生成 SWOT 四字段 + `ai_analysis_report`) |
| POST | `/business/competitive/{id}/monitor/subscribe` | `business:competitive:edit` | 订阅竞品动态推送 (置 `monitor_enabled=Y`,接受 `monitor_keywords` CSV) |
| GET  | `/business/competitive/{id}/feature-matrix` | `business:competitive:query` | 取 12 维度功能对比矩阵 JSON |
| GET  | `/business/competitive/export-template` | `business:competitive:export` | 导出 Excel 竞品分析模板 |

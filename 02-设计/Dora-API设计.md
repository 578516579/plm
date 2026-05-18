# Dora 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Dora-PRD.md](../01-立项/Dora-PRD.md) |
| Base URL | `/business/dora` |
| 权限串前缀 | `business:dora:*` |
| Controller | [plm-backend/plm-dora/.../DoraController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/dora/list` | `business:dora:list` | 列表 (分页+搜索) |
| GET | `/business/dora/{id}` | `business:dora:query` | 详情 |
| POST | `/business/dora` | `business:dora:add` | 新建 |
| PUT | `/business/dora` | `business:dora:edit` | 修改 |
| DELETE | `/business/dora/{ids}` | `business:dora:remove` | 逻辑删除 |
| POST | `/business/dora/export` | `business:dora:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Dora"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/dora/{id}/transit` | `business:dora:edit` | 状态机推进 (00→01→02 单向终态),违反抛 601 |
| POST | `/business/dora/refresh` | `business:dora:edit` | 快照刷新 (按 `metric_type` × `period_type` 聚合 tb_release / tb_pipeline 数据,自动创建新行 status=00) |
| POST | `/business/dora/ai/suggest/{id}` | `business:dora:edit` | AI DORA 改进建议 (调用 §6 `dora-suggest-flow`,生成 Elite/High/Medium/Low 评估 + 农情专项建议) |
| GET  | `/business/dora/trend?metric=deploy_freq&periodType=month` | `business:dora:query` | 取趋势曲线 + 热力图 + lead-time 拆解 JSON (`trend_chart_json`/`heatmap_json`/`leadtime_breakdown`) |

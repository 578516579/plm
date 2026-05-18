# Testreport 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Testreport-PRD.md](../01-立项/Testreport-PRD.md) |
| Base URL | `/business/testreport` |
| 权限串前缀 | `business:testreport:*` |
| Controller | [plm-backend/plm-testreport/.../TestreportController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/testreport/list` | `business:testreport:list` | 列表 (分页+搜索) |
| GET | `/business/testreport/{id}` | `business:testreport:query` | 详情 |
| POST | `/business/testreport` | `business:testreport:add` | 新建 |
| PUT | `/business/testreport` | `business:testreport:edit` | 修改 |
| DELETE | `/business/testreport/{ids}` | `business:testreport:remove` | 逻辑删除 |
| POST | `/business/testreport/export` | `business:testreport:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Testreport"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/testreport/{id}/transit` | `business:testreport:edit` | 状态机推进 (00→01→{00,02} 含反向边 01→00 审核打回) |
| POST | `/business/testreport/ai/generate` | `business:testreport:edit` | AI 测试报告生成 (调用 §6 `test-report-flow`,输入 testplan_id + 缺陷统计,生成 title/defect_summary/risk_evaluation/recommendations/risk_level) |
| GET  | `/business/testreport/{id}/export-pdf` | `business:testreport:export` | 导出 PDF (含 KPI 仪表盘 + 缺陷分布饼图 + 风险评估章节) |
| POST | `/business/testreport/{id}/approve` | `business:testreport:edit` | 审核通过 (01→02 已发布) |

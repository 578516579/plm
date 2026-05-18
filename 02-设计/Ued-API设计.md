# Ued 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Ued-PRD.md](../01-立项/Ued-PRD.md) |
| Base URL | `/business/ued` |
| 权限串前缀 | `business:ued:*` |
| Controller | [plm-backend/plm-ued/.../UedController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/ued/list` | `business:ued:list` | 列表 (分页+搜索) |
| GET | `/business/ued/{id}` | `business:ued:query` | 详情 |
| POST | `/business/ued` | `business:ued:add` | 新建 |
| PUT | `/business/ued` | `business:ued:edit` | 修改 |
| DELETE | `/business/ued/{ids}` | `business:ued:remove` | 逻辑删除 |
| POST | `/business/ued/export` | `business:ued:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Ued"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/ued/{id}/transit` | `business:ued:edit` | 状态机推进 (00→01→{00,02} 含反向边 / `02→{03}`) |
| POST | `/business/ued/ai/review/{id}` | `business:ued:edit` | AI 设计评审 (调用 §6 `ued-review-flow`,生成 `review_report` + `review_score` + `compliance_check`) |
| POST | `/business/ued/{id}/figma-sync` | `business:ued:edit` | 从 Figma 同步设计稿 (按 `figma_file_key` 调用 Figma MCP,更新 `preview_url` + `annotation_content`) |
| GET  | `/business/ued/{id}/versions` | `business:ued:query` | 取设计稿历史版本列表 |

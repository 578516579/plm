# Apidesign 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Apidesign-PRD.md](../01-立项/Apidesign-PRD.md) |
| Base URL | `/business/apidesign` |
| 权限串前缀 | `business:apidesign:*` |
| Controller | [plm-backend/plm-apidesign/.../ApidesignController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/apidesign/list` | `business:apidesign:list` | 列表 (分页+搜索) |
| GET | `/business/apidesign/{id}` | `business:apidesign:query` | 详情 |
| POST | `/business/apidesign` | `business:apidesign:add` | 新建 |
| PUT | `/business/apidesign` | `business:apidesign:edit` | 修改 |
| DELETE | `/business/apidesign/{ids}` | `business:apidesign:remove` | 逻辑删除 |
| POST | `/business/apidesign/export` | `business:apidesign:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Apidesign"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/apidesign/{id}/transit` | `business:apidesign:edit` | 状态机推进 (00→01→{00,02} 含反向边 / `02→{03}`),违反抛 601 |
| POST | `/business/apidesign/ai/generate/{id}` | `business:apidesign:edit` | AI 接口设计生成 (调用 §6 `detail-design-flow`,本期 mock 返回 OpenAPI YAML + Mock 响应) |
| POST | `/business/apidesign/{id}/mock/toggle` | `business:apidesign:edit` | 开关 Mock 服务 (置 `mock_enabled='Y'/'N'`,接受 `mock_response` JSON) |
| GET  | `/business/apidesign/{id}/openapi.yaml` | `business:apidesign:export` | 导出 OpenAPI 3.0 YAML 文件 |

# ManualProduct 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [ManualProduct-PRD.md](../01-立项/ManualProduct-PRD.md) |
| Base URL | `/business/manual-product` |
| 权限串前缀 | `business:manual-product:*` |
| Controller | [plm-backend/plm-manual-product/.../ManualProductController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/manual-product/list` | `business:manual-product:list` | 列表 (分页+搜索) |
| GET | `/business/manual-product/{id}` | `business:manual-product:query` | 详情 |
| POST | `/business/manual-product` | `business:manual-product:add` | 新建 |
| PUT | `/business/manual-product` | `business:manual-product:edit` | 修改 |
| DELETE | `/business/manual-product/{ids}` | `business:manual-product:remove` | 逻辑删除 |
| POST | `/business/manual-product/export` | `business:manual-product:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "ManualProduct"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/manual-product/{id}/transit` | `business:manual-product:edit` | 状态机推进 (00→01→02→{00,03} 含反向边 02→00 重新生成) |
| POST | `/business/manual-product/ai/generate` | `business:manual-product:edit` | AI 产品手册生成 (调用 §6 `product-manual-flow`,按 `include_modules` CSV + `screenshots_urls` 一键生成 `content` Markdown) |
| GET  | `/business/manual-product/{id}/export?format=pdf` | `business:manual-product:export` | 导出手册 (支持 word/pdf/html/h5 四格式,h5 渲染为响应式 SPA) |
| GET  | `/business/manual-product/{id}/versions` | `business:manual-product:query` | 取产品手册历史版本列表 |

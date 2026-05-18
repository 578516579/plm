# ManualImpl 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [ManualImpl-PRD.md](../01-立项/ManualImpl-PRD.md) |
| Base URL | `/business/manual-impl` |
| 权限串前缀 | `business:manual-impl:*` |
| Controller | [plm-backend/plm-manual-impl/.../ManualImplController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/manual-impl/list` | `business:manual-impl:list` | 列表 (分页+搜索) |
| GET | `/business/manual-impl/{id}` | `business:manual-impl:query` | 详情 |
| POST | `/business/manual-impl` | `business:manual-impl:add` | 新建 |
| PUT | `/business/manual-impl` | `business:manual-impl:edit` | 修改 |
| DELETE | `/business/manual-impl/{ids}` | `business:manual-impl:remove` | 逻辑删除 |
| POST | `/business/manual-impl/export` | `business:manual-impl:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "ManualImpl"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/manual-impl/{id}/transit` | `business:manual-impl:edit` | 状态机推进 (00→01→02→{00,03} 含反向边 02→00 重新草稿) |
| POST | `/business/manual-impl/ai/generate/{id}` | `business:manual-impl:edit` | AI 生成实施手册 (调用 §6 `impl-manual-flow`,按 `deploy_mode` + `os_type` + `db_type` 三维度生成 5 章节 Markdown) |
| GET  | `/business/manual-impl/{id}/export?format=pdf` | `business:manual-impl:export` | 导出实施手册 (支持 word/pdf/html/markdown 四格式) |
| GET  | `/business/manual-impl/{id}/versions` | `business:manual-impl:query` | 取手册历史版本列表 |

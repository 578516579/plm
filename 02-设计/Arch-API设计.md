# Arch 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Arch-PRD.md](../01-立项/Arch-PRD.md) |
| Base URL | `/business/arch` |
| 权限串前缀 | `business:arch:*` |
| Controller | [plm-backend/plm-arch/.../ArchController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/arch/list` | `business:arch:list` | 列表 (分页+搜索) |
| GET | `/business/arch/{id}` | `business:arch:query` | 详情 |
| POST | `/business/arch` | `business:arch:add` | 新建 |
| PUT | `/business/arch` | `business:arch:edit` | 修改 |
| DELETE | `/business/arch/{ids}` | `business:arch:remove` | 逻辑删除 |
| POST | `/business/arch/export` | `business:arch:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Arch"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/arch/{id}/transit` | `business:arch:edit` | 状态机推进 (00→01→{00,02} 含反向边 / `02→{03}`),违反抛 601 |
| POST | `/business/arch/ai/generate/{id}` | `business:arch:edit` | AI 架构设计生成 (调用 §6 `arch-design-flow`,本期 mock 生成 C4 Mermaid + NFR 映射模板) |
| GET  | `/business/arch/{id}/c4-diagram` | `business:arch:query` | 取 C4 容器图 Mermaid 文本(用于前端 mermaid.js 渲染) |
| GET  | `/business/arch/export-template` | `business:arch:export` | 导出 Excel 架构模板 |

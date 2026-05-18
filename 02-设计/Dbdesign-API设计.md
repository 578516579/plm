# Dbdesign 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Dbdesign-PRD.md](../01-立项/Dbdesign-PRD.md) |
| Base URL | `/business/dbdesign` |
| 权限串前缀 | `business:dbdesign:*` |
| Controller | [plm-backend/plm-dbdesign/.../DbdesignController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/dbdesign/list` | `business:dbdesign:list` | 列表 (分页+搜索) |
| GET | `/business/dbdesign/{id}` | `business:dbdesign:query` | 详情 |
| POST | `/business/dbdesign` | `business:dbdesign:add` | 新建 |
| PUT | `/business/dbdesign` | `business:dbdesign:edit` | 修改 |
| DELETE | `/business/dbdesign/{ids}` | `business:dbdesign:remove` | 逻辑删除 |
| POST | `/business/dbdesign/export` | `business:dbdesign:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Dbdesign"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/dbdesign/{id}/transit` | `business:dbdesign:edit` | 状态机推进 (00→01→{00,02} 含反向边 / `02→{03}`),违反抛 601 |
| POST | `/business/dbdesign/ai/generate/{id}` | `business:dbdesign:edit` | AI 数据库设计生成 (调用 §6 `db-design-flow`,本期 mock 返回 ER + 数据字典 + DDL 模板) |
| POST | `/business/dbdesign/{id}/validate` | `business:dbdesign:edit` | DDL 规范校验 (命名/索引/范式),回写 `normalization_check` JSON |
| GET  | `/business/dbdesign/{id}/ddl-export?format=sql` | `business:dbdesign:export` | 导出 DDL 脚本 (.sql 文件) |

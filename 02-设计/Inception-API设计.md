# Inception 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Inception-PRD.md](../01-立项/Inception-PRD.md) |
| Base URL | `/business/inception` |
| 权限串前缀 | `business:inception:*` |
| Controller | [plm-backend/plm-inception/.../InceptionController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/inception/list` | `business:inception:list` | 列表 (分页+搜索) |
| GET | `/business/inception/{id}` | `business:inception:query` | 详情 |
| POST | `/business/inception` | `business:inception:add` | 新建 |
| PUT | `/business/inception` | `business:inception:edit` | 修改 |
| DELETE | `/business/inception/{ids}` | `business:inception:remove` | 逻辑删除 |
| POST | `/business/inception/export` | `business:inception:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Inception"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/inception/{id}/transit` | `business:inception:edit` | 状态机推进 (00→01→{02,04} `02→{03,04}` `04→00`),违反抛 601 |
| POST | `/business/inception/ai/generate/{id}` | `business:inception:edit` | AI 立项建议书生成 (调用 §6 `project-inception-flow`,生成 `ai_proposal_content` + `ai_risks` + `ai_generated_at`) |
| POST | `/business/inception/{id}/convert-to-project` | `business:inception:edit` | 审批通过 (status=03) 后"转项目":调用 `IProjectService.insertProject(...)`,自动填 projectId 回链 |
| GET  | `/business/inception/export-template` | `business:inception:export` | 导出 Excel 立项模板 |

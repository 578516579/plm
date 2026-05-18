# Release 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Release-PRD.md](../01-立项/Release-PRD.md) |
| Base URL | `/business/release` |
| 权限串前缀 | `business:release:*` |
| Controller | [plm-backend/plm-release/.../ReleaseController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/release/list` | `business:release:list` | 列表 (分页+搜索) |
| GET | `/business/release/{id}` | `business:release:query` | 详情 |
| POST | `/business/release` | `business:release:add` | 新建 |
| PUT | `/business/release` | `business:release:edit` | 修改 |
| DELETE | `/business/release/{ids}` | `business:release:remove` | 逻辑删除 |
| POST | `/business/release/export` | `business:release:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Release"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/release/{id}/transit` | `business:release:edit` | 状态机推进 (5 态:00→{01,04} / 01→{02,03} / 02→{03,04} / 03→{04}),违反抛 601 |
| POST | `/business/release/{id}/execute` | `business:release:edit` | 执行发布 (按 `strategy` 触发蓝绿/金丝雀/滚动/直接替换,异步,转 status='01 发布中',成功后置 '02 已发布' + 回写 `released_at`) |
| POST | `/business/release/{id}/rollback` | `business:release:edit` | 回滚发布 (必填 `rollback_reason`,转 status='03 已回滚',回写 `rollback_at`,违反 602) |
| POST | `/business/release/{id}/ai-review` | `business:release:edit` | AI 发布评审 (基于代码变更 + 测试报告生成 `ai_review_score` + `ai_review_notes`,direct_replace 策略且 score<7 时强阻断) |
| GET  | `/business/release/{id}/dora-metrics` | `business:release:query` | 取本次发布的 DORA 4 指标快照 |

# Autotest 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Autotest-PRD.md](../01-立项/Autotest-PRD.md) |
| Base URL | `/business/autotest` |
| 权限串前缀 | `business:autotest:*` |
| Controller | [plm-backend/plm-autotest/.../AutotestController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/autotest/list` | `business:autotest:list` | 列表 (分页+搜索) |
| GET | `/business/autotest/{id}` | `business:autotest:query` | 详情 |
| POST | `/business/autotest` | `business:autotest:add` | 新建 |
| PUT | `/business/autotest` | `business:autotest:edit` | 修改 |
| DELETE | `/business/autotest/{ids}` | `business:autotest:remove` | 逻辑删除 |
| POST | `/business/autotest/export` | `business:autotest:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Autotest"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点
<待人工填写>:如有非 CRUD 端点 (e.g. /execute / /run / /ai/generate)

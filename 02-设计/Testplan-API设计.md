# Testplan 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Testplan-PRD.md](../01-立项/Testplan-PRD.md) |
| Base URL | `/business/testplan` |
| 权限串前缀 | `business:testplan:*` |
| Controller | [plm-backend/plm-testplan/.../TestplanController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/testplan/list` | `business:testplan:list` | 列表 (分页+搜索) |
| GET | `/business/testplan/{id}` | `business:testplan:query` | 详情 |
| POST | `/business/testplan` | `business:testplan:add` | 新建 |
| PUT | `/business/testplan` | `business:testplan:edit` | 修改 |
| DELETE | `/business/testplan/{ids}` | `business:testplan:remove` | 逻辑删除 |
| POST | `/business/testplan/export` | `business:testplan:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Testplan"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/testplan/{id}/transit` | `business:testplan:edit` | 状态机推进 (00→01→02→03 单向) |
| POST | `/business/testplan/ai/generate` | `business:testplan:edit` | AI 测试方案生成 (调用 §6 `test-plan-flow`,生成 strategy/tools_recommended/resources_plan/risk_assessment 4 字段) |
| GET  | `/business/testplan/{id}/cases` | `business:testplan:query` | 取关联测试用例列表 (跨模块查询 tb_testcase WHERE testplan_id=?) |
| POST | `/business/testplan/{id}/cases/bind` | `business:testplan:edit` | 批量挂载用例到方案 (接受 testcaseIds 数组) |

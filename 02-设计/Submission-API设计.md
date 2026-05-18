# Submission 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Submission-PRD.md](../01-立项/Submission-PRD.md) |
| Base URL | `/business/submission` |
| 权限串前缀 | `business:submission:*` |
| Controller | [plm-backend/plm-submission/.../SubmissionController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/submission/list` | `business:submission:list` | 列表 (分页+搜索) |
| GET | `/business/submission/{id}` | `business:submission:query` | 详情 |
| POST | `/business/submission` | `business:submission:add` | 新建 |
| PUT | `/business/submission` | `business:submission:edit` | 修改 |
| DELETE | `/business/submission/{ids}` | `business:submission:remove` | 逻辑删除 |
| POST | `/business/submission/export` | `business:submission:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Submission"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/submission/{id}/transit` | `business:submission:edit` | 状态机推进 (00→01→{02,04} / 02→{03,04} / 04→00 反向),违反抛 601;进入 04 必填 reject_reason(602);进入 03 必 quality_gate_passed=Y(708) |
| POST | `/business/submission/{id}/quality-gate` | `business:submission:edit` | 质量门禁评估 (服务端重算:单测覆盖率≥60 ∧ codeScanPassed ∧ prdCompleted ∧ apiDocUpdated → quality_gate_passed='Y'/'N',不接受前端写入) |
| POST | `/business/submission/{id}/approve` | `business:submission:edit` | 测试经理审批通过 (02→03,自动填 approved_at) |
| POST | `/business/submission/{id}/reject` | `business:submission:edit` | 退回 (转 04,必填 reject_reason,反向边 04→00 重写) |

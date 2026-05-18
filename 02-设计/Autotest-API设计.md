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

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/autotest/{id}/transit` | `business:autotest:edit` | 状态机推进 (00→01↔02 含反向边 02→01 重激活),违反抛 601 |
| POST | `/business/autotest/ai/generate/{id}` | `business:autotest:edit` | AI 生成测试脚本 (调用 §6 `auto-test-flow`,按 `test_suite_type` + `framework` 生成 `script_content`) |
| POST | `/business/autotest/{id}/run` | `business:autotest:edit` | 立即执行套件 (异步,完成后回写 `total_cases`/`passed_cases`/`failed_cases`/`pass_rate`/`execution_duration_sec`/`last_executed_at`) |
| POST | `/business/autotest/{id}/abort` | `business:autotest:edit` | 中止正在运行的套件 |
| POST | `/business/autotest/{id}/ai/root-cause` | `business:autotest:edit` | AI 根因分析 (基于最近一次执行的失败用例,回写 `last_root_cause_analysis` Markdown) |

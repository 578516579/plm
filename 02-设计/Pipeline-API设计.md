# Pipeline 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Pipeline-PRD.md](../01-立项/Pipeline-PRD.md) |
| Base URL | `/business/pipeline` |
| 权限串前缀 | `business:pipeline:*` |
| Controller | [plm-backend/plm-pipeline/.../PipelineController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/pipeline/list` | `business:pipeline:list` | 列表 (分页+搜索) |
| GET | `/business/pipeline/{id}` | `business:pipeline:query` | 详情 |
| POST | `/business/pipeline` | `business:pipeline:add` | 新建 |
| PUT | `/business/pipeline` | `business:pipeline:edit` | 修改 |
| DELETE | `/business/pipeline/{ids}` | `business:pipeline:remove` | 逻辑删除 |
| POST | `/business/pipeline/export` | `business:pipeline:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Pipeline"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/pipeline/{id}/transit` | `business:pipeline:edit` | 状态机推进 (00↔01 启用/停用),cron 触发未填 cron_expr 抛 602 |
| POST | `/business/pipeline/{id}/trigger` | `business:pipeline:edit` | 手动触发流水线 (异步执行,85% 成功率模型,自动累加 `total_runs`/`success_count` + 重算 `success_rate`,回写 `last_run_status`/`last_run_at`) |
| POST | `/business/pipeline/{id}/cancel` | `business:pipeline:edit` | 取消正在运行的流水线 (置 `last_run_status='cancelled'`) |
| GET  | `/business/pipeline/{id}/runs?limit=20` | `business:pipeline:query` | 取最近 N 次运行历史 (从 tb_pipeline_run 子表) |
| GET  | `/business/pipeline/{id}/yaml` | `business:pipeline:query` | 取流水线 YAML 定义 (`yaml_content`) |

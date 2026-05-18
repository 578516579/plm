# Testdata 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [Testdata-PRD.md](../01-立项/Testdata-PRD.md) |
| Base URL | `/business/testdata` |
| 权限串前缀 | `business:testdata:*` |
| Controller | [plm-backend/plm-testdata/.../TestdataController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/testdata/list` | `business:testdata:list` | 列表 (分页+搜索) |
| GET | `/business/testdata/{id}` | `business:testdata:query` | 详情 |
| POST | `/business/testdata` | `business:testdata:add` | 新建 |
| PUT | `/business/testdata` | `business:testdata:edit` | 修改 |
| DELETE | `/business/testdata/{ids}` | `business:testdata:remove` | 逻辑删除 |
| POST | `/business/testdata/export` | `business:testdata:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "Testdata"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/testdata/{id}/transit` | `business:testdata:edit` | 状态机推进 (00→01→02 单向终态),违反抛 601 |
| POST | `/business/testdata/{id}/generate` | `business:testdata:edit` | 触发数据生成 (调用 §6 `data-gen-flow`,按 `field_semantics` + 4 规则生成 `generated_count` 条数据写入 `generated_content`) |
| POST | `/business/testdata/{id}/cleanup` | `business:testdata:remove` | 清理生成数据 (清空 `generated_content`,保留元数据,用于循环复用数据集) |
| GET  | `/business/testdata/{id}/download?format=csv` | `business:testdata:export` | 下载生成数据 (支持 json/sql/csv 三种 `output_format`) |

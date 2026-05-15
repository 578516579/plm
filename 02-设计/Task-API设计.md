# Task 模块 — API 设计

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 关联 PRD | [Task-PRD.md](../01-立项/Task-PRD.md) |
| 鉴权 | JWT Bearer (复用 Project 模式) |
| Base path | `/business/task` |
| 风格 | REST,沿用 Project §1 设计原则 |

## 1. 端点清单 (8 个)

| # | Method | Path | 权限串 | 入参 | 出参 | Log type |
|---|---|---|---|---|---|---|
| 1 | GET | `/business/task/list` | `business:task:list` | `TaskQuery` | `TableDataInfo<Task>` | — |
| 2 | POST | `/business/task/export` | `business:task:export` | `TaskQuery` | Excel | `EXPORT` |
| 3 | GET | `/business/task/{id}` | `business:task:query` | path id | `AjaxResult<Task>` | — |
| 4 | POST | `/business/task` | `business:task:add` | `Task` | `AjaxResult<Void>` | `INSERT` |
| 5 | PUT | `/business/task` | `business:task:edit` | `Task` | `AjaxResult<Void>` | `UPDATE` |
| 6 | DELETE | `/business/task/{ids}` | `business:task:remove` | CSV ids | `AjaxResult<Void>` | `DELETE` |
| 7 | GET | `/business/task/my` | `business:task:list` | `TaskQuery` (assigneeUserId 自动填 currentUser) | `TableDataInfo<Task>` | — |
| 8 | GET | `/business/task/kanban` | `business:task:kanban` | `projectId` + `sprintId` | `AjaxResult<KanbanView>` | — |

> 7 和 8 是 Task 模块**特有**端点(my-tasks 和 kanban),Project / Requirement 都没有。

## 2. 详细契约

### 2.1 列表查询 (6 维筛选)

```http
GET /business/task/list?pageNum=1&pageSize=10&projectId=&requirementId=&sprintId=&status=&priority=&assigneeUserId=
```

**入参** (`TaskQuery`):

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` / `pageSize` | int | ⬜ | |
| `taskNo` | string | ⬜ | 编号模糊 |
| `projectId` | bigint | ⬜ | **Sprint 详情页 / Project 详情页 必传** |
| `requirementId` | bigint | ⬜ | "需求详情下的任务列表" Tab 必传 |
| `sprintId` | bigint | ⬜ | "迭代详情下的任务列表" 必传 |
| `title` | string | ⬜ | 标题模糊 |
| `status` | string | ⬜ | 字典值 00-05 |
| `priority` | string | ⬜ | 00/01/02 |
| `assigneeUserId` | bigint | ⬜ | "我的任务"自动填 |

### 2.2 看板视图 (T-006 特殊端点)

```http
GET /business/task/kanban?projectId=1&sprintId=42
```

**出参** (`KanbanView`):

```json
{
  "code": 200,
  "data": {
    "columns": [
      { "status": "00", "label": "待开发",   "tasks": [ /* Task 数组 */ ], "count": 8 },
      { "status": "01", "label": "开发中",   "tasks": [ ... ], "count": 5 },
      { "status": "02", "label": "代码评审", "tasks": [ ... ], "count": 3 },
      { "status": "03", "label": "测试中",   "tasks": [ ... ], "count": 2 },
      { "status": "04", "label": "已完成",   "tasks": [ ... ], "count": 12 }
    ]
  }
}
```

**实现说明**:
- Service 层一条 SQL `WHERE project_id=? AND (sprint_id=? OR ?=NULL) AND status != '05' AND del_flag='0'` 拉所有未取消任务,然后按 status 分组
- **不分页**,每列最多返回 50 个(`LIMIT 50`),超出在 UI 上点"展开列"才用 list 接口分页拉
- 性能要求 < 800ms (PRD §4),Phase 03 必须 EXPLAIN 验证

### 2.3 新增 (含 task_no 自动生成 + FK 校验)

```http
POST /business/task
{
  "projectId": 1,
  "requirementId": 42,
  "sprintId": null,
  "title": "实现导出 Excel 接口",
  "description": "调用 EasyExcel,...",
  "status": "00",
  "priority": "01",
  "assigneeUserId": 1,
  "estimatedHours": 8.0
}
```

**Service 层校验**:

| 校验项 | 错误码 | 错误消息 |
|---|---|---|
| `title` 非空 | 602 | "任务标题不能为空" |
| `projectId` 在 tb_project 存在 | 702 | "关联项目不存在" |
| `requirementId` 填写时必须在 tb_requirement 存在 | 702 | "关联需求不存在" |
| `sprintId` 填写时必须在 tb_sprint 存在 | 702 | "关联迭代不存在" |
| `status` / `priority` 在字典内 | 604 | |
| 新建 `status` 必须为 `00` 待开发 | 601 | "新建任务状态必须为「待开发」" |
| `mrUrl` 填写时格式必须 http(s):// | 604 | "MR 链接格式错误" |
| `taskNo` 自动 `TASK-YYYY-NNNN` (ADR-0003) | 701 | UNIQUE 兜底 |

### 2.4 修改 (含 6×6 状态机)

```http
PUT /business/task
{ "id": 1, "status": "01" }  // 待开发 → 开发中
```

**状态机校验** (PRD §3.3 6×6):

```
              00  01  02  03  04  05
待开发(00)    —  ✅  ❌  ❌  ❌  ✅
开发中(01)   ✅  —  ✅  ❌  ❌  ✅
代码评审(02) ❌  ✅  —  ✅  ❌  ✅
测试中(03)   ❌  ❌  ✅  —  ✅  ✅
已完成(04)   ❌  ❌  ❌  ❌  —  ❌  (终态)
已取消(05)   ❌  ❌  ❌  ❌  ❌  —   (终态)
```

> **关键**: 02 ↔ 01 双向边(评审打回需重写代码),03 → 02 单向边(测试发现问题打回评审)。终态保护同 Project / Requirement。

**特殊更新逻辑**:
- 状态推进到 `04 已完成` 时,**必须填 `actualHours`**;否则报 602 "请填写实际工时"
- `mrUrl` / `mrBranch` 可随时填,不绑定状态

### 2.5 我的任务 (T-007)

```http
GET /business/task/my?status=01&priority=00
```

Service 层:`SecurityUtils.getUserId()` 取当前 userId → 填入 `TaskQuery.assigneeUserId` → 调 selectList。

### 2.6 详情 / 删除 / 导出

- **详情** `GET /business/task/{id}` — 标准查找
- **删除** `DELETE /business/task/{ids}` — 逻辑删除
- **导出** `POST /business/task/export` — 字段全列,含 project_name / requirement_title / sprint_name 三个关联字段

## 3. 错误码表

| Code | 场景 |
|---|---|
| 200 | 成功 |
| 404 | 任务不存在 |
| 601 | 状态转换违规 / 新建状态非"待开发" |
| 602 | 必填字段空 (title 或 actualHours-when-完成) |
| 604 | 字典/格式不合法 (status/priority/mr_url) |
| 701 | task_no 重复 |
| 702 | 关联项目/需求/迭代不存在 |

## 4. 鉴权与权限 (8 个)

| Action | 权限串 |
|---|---|
| 列表 | `business:task:list` |
| 详情 | `business:task:query` |
| 新增 | `business:task:add` |
| 修改 | `business:task:edit` |
| 删除 | `business:task:remove` |
| 导出 | `business:task:export` |
| 看板 | `business:task:kanban` |
| (my-tasks 复用 list 权限,无新权限点) | — |

## 5. 与兄弟模块的耦合点

| 接口 | 耦合对象 | 处理 |
|---|---|---|
| `list?requirementId=` | Requirement | 弱(可选筛选条件) |
| `list?sprintId=` | Sprint | 强("Sprint 详情下任务列表"必传) |
| 新增/修改 FK 校验 | Project + Requirement + Sprint | Service 层调对应 Service.checkExists() |
| Sprint S-005 删除前检查任务 | Sprint | Sprint Service 调 Task Service `countBySprintId` |
| Sprint S-009 健康度统计 | Sprint | Sprint Service 调 Task Service `countByStatus(sprintId, status)` |

## 6. 前端 review

- ✅ Wjl (solo) self-review
- 看板视图(端点 8)前端实现 **本期只读,无拖拽** — PRD §1.3 已明确推到 v0.3

## 7. 变更记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 初版 |

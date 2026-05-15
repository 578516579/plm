# Sprint 模块 — API 设计

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 关联 PRD | [Sprint-PRD.md](../01-立项/Sprint-PRD.md) |
| 鉴权 | JWT Bearer (复用 Project 模式) |
| Base path | `/business/sprint` |
| 风格 | REST,沿用 Project §1 设计原则 |

## 1. 端点清单 (8 个)

| # | Method | Path | 权限串 | 入参 | 出参 | Log type |
|---|---|---|---|---|---|---|
| 1 | GET | `/business/sprint/list` | `business:sprint:list` | `SprintQuery` | `TableDataInfo<Sprint>` | — |
| 2 | POST | `/business/sprint/export` | `business:sprint:export` | `SprintQuery` | Excel | `EXPORT` |
| 3 | GET | `/business/sprint/{id}` | `business:sprint:query` | path id | `AjaxResult<SprintDetail>` | — |
| 4 | POST | `/business/sprint` | `business:sprint:add` | `Sprint` | `AjaxResult<Void>` | `INSERT` |
| 5 | PUT | `/business/sprint` | `business:sprint:edit` | `Sprint` | `AjaxResult<Void>` | `UPDATE` |
| 6 | DELETE | `/business/sprint/{ids}` | `business:sprint:remove` | CSV ids | `AjaxResult<Void>` | `DELETE` |
| 7 | GET | `/business/sprint/current?projectId=` | `business:sprint:query` | projectId | `AjaxResult<Sprint>` | — |
| 8 | GET | `/business/sprint/{id}/stats` | `business:sprint:stats` | path id | `AjaxResult<SprintStats>` | — |

> 7、8 是 Sprint 特有端点(activeSprint + 健康度统计),复用 list/query 不能简单替代。

## 2. 详细契约

### 2.1 列表查询

```http
GET /business/sprint/list?pageNum=1&pageSize=10&projectId=&status=&startDate=&endDate=
```

**入参** (`SprintQuery`):

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` / `pageSize` | int | ⬜ | |
| `sprintNo` | string | ⬜ | 编号模糊 |
| `projectId` | bigint | ⬜ | 项目筛选(Project 详情页 "迭代管理 Tab" 必传) |
| `name` | string | ⬜ | 迭代名称模糊 |
| `status` | string | ⬜ | 字典 `biz_sprint_status` 00-03 |
| `params.beginPlannedStartDate` | date | ⬜ | 计划开始日期范围下限 |
| `params.endPlannedStartDate` | date | ⬜ | 范围上限 |

### 2.2 详情(带关联任务)

```http
GET /business/sprint/42
```

**出参** (`SprintDetail`):

```json
{
  "code": 200,
  "data": {
    "sprint": { /* Sprint 基本字段 */ },
    "tasks": [ /* 该 Sprint 下所有 Task,复用 TaskService.selectBySprintId() */ ],
    "stats": { /* 简版统计,详细见 §2.7 */
      "planned_task_count": 15,
      "completed_task_count": 12,
      "complete_rate": 0.8
    }
  }
}
```

> 详情接口**合并了 tasks 列表**,前端无需二次调用 Task 接口。这是与 Project / Requirement 不同的"聚合详情"模式。

### 2.3 新增 (含 sprint_no 自动生成 + 默认日期)

```http
POST /business/sprint
{
  "projectId": 1,
  "name": "Sprint 26W21",
  "goal": "导出功能上线",
  "status": "00",
  "plannedStartDate": "2026-05-19",
  "plannedEndDate": "2026-06-02",
  "durationDays": 14
}
```

**Service 层校验**:

| 校验项 | 错误码 | 错误消息 |
|---|---|---|
| `name` 非空 | 602 | "迭代名称不能为空" |
| `projectId` 在 tb_project 存在 | 702 | "关联项目不存在" |
| `plannedStartDate` / `plannedEndDate` 非空 | 602 | "计划开始/结束日期不能为空" |
| `plannedEndDate >= plannedStartDate` | 604 | "计划结束日期不能早于开始日期" |
| 新建 `status` 必须为 `00` 计划中 | 601 | "新建迭代状态必须为「计划中」" |
| `durationDays` 默认填 = (endDate - startDate) + 1 | — | (自动计算,无错误) |
| `sprintNo` 自动 `SPR-YYYY-NNNN` (ADR-0004) | 701 | UNIQUE 兜底 |

**默认日期逻辑**:
- 如果 `plannedEndDate` 为空,自动 = `plannedStartDate + 14 天`(默认 14 天 Sprint)
- `durationDays` 是冗余字段,Service 层自动计算填入

### 2.4 修改 (含 4×4 状态机 + 自动填充 actual 日期)

```http
PUT /business/sprint
{ "id": 42, "status": "01" }  // 计划中 → 进行中
```

**状态机校验** (PRD §3.3 4×4):

```
              00计划中  01进行中  02已完成  03已取消
00计划中       —        ✅       ❌       ✅
01进行中      ❌        —        ✅       ✅
02已完成      ❌        ❌       —        ❌  (终态)
03已取消      ❌        ❌       ❌       —   (终态)
```

**自动填充逻辑** (Sprint 独有):
- `00 → 01`: 自动 `actualStartDate = TODAY()` (Service 层 LocalDate.now())
- `01 → 02`: 自动 `actualEndDate = TODAY()`
- 取消(`* → 03`): 不填充 actual 日期

**"项目级单一活跃迭代"约束** (PRD §3.3,错误码 703):

```java
// SprintServiceImpl.updateStatus()
if ("01".equals(newStatus)) {
    int activeCount = mapper.countActiveByProject(projectId, sprintId);  // 排除自己
    if (activeCount > 0) {
        throw new ServiceException(
            "项目 " + projectId + " 已有进行中的迭代,不能再开新的", 703);
    }
}
```

> **关键**: 这是软约束(DB 层无法用 UNIQUE 实现),Service 层判;并发场景需要 Phase 03 用乐观锁(`@Version` 字段)或分布式锁。

### 2.5 删除 (含关联任务检查,S-005)

```http
DELETE /business/sprint/42
```

**前置检查**:

```java
// SprintServiceImpl.delete()
int taskCount = taskMapper.countBySprintId(sprintId);
if (taskCount > 0) {
    throw new ServiceException(
        "迭代下有 " + taskCount + " 个关联任务,请先解除关联或迁移", 704);
}
```

### 2.6 活跃迭代查询 (S-008)

```http
GET /business/sprint/current?projectId=1
```

**出参**:

```json
{
  "code": 200,
  "data": { /* 该项目状态=01 的唯一 Sprint */ }
}
```

或 `data: null`(项目无活跃迭代时)。

### 2.7 健康度统计 (S-009)

```http
GET /business/sprint/42/stats
```

**出参** (`SprintStats`):

```json
{
  "code": 200,
  "data": {
    "sprintId": 42,
    "plannedTaskCount": 15,
    "completedTaskCount": 12,
    "inProgressTaskCount": 2,
    "remainingTaskCount": 1,
    "completeRate": 0.8,
    "onTime": true,
    "daysOverPlan": 0
  }
}
```

**实现说明**:
- Task 数按 `Task.countByStatusAndSprint(sprintId, status)` 聚合
- `onTime`: `actualEndDate <= plannedEndDate + 2 days` (PRD §1.2 准时定义)
- `daysOverPlan`: 仅在已完成时计算 = `(actualEndDate - plannedEndDate).days`,负数=提前

## 3. 错误码表

| Code | 场景 |
|---|---|
| 200 | 成功 |
| 404 | 迭代不存在 |
| 601 | 状态转换违规 / 新建状态非"计划中" |
| 602 | 必填字段空 (name / dates) |
| 604 | 日期逻辑错误 |
| 701 | sprint_no 重复 |
| 702 | 关联项目不存在 |
| **703** | **项目已有活跃迭代** (业务硬规则) |
| 704 | 迭代有关联任务,不可删除 |

## 4. 鉴权与权限 (7 个)

| Action | 权限串 |
|---|---|
| 列表 | `business:sprint:list` |
| 详情 | `business:sprint:query` |
| 新增 | `business:sprint:add` |
| 修改 | `business:sprint:edit` |
| 删除 | `business:sprint:remove` |
| 导出 | `business:sprint:export` |
| 统计 | `business:sprint:stats` |
| (current 复用 query 权限) | — |

## 5. 与兄弟模块的耦合点

| 接口 | 耦合对象 | 处理 |
|---|---|---|
| 详情 §2.2 含 tasks 列表 | Task | Sprint Service 调 Task Service `selectBySprintId` |
| 删除 §2.5 前置检查 | Task | Sprint Service 调 Task Service `countBySprintId` |
| 统计 §2.7 任务计数 | Task | Sprint Service 调 Task Service `countByStatusAndSprint` |
| 列表 `projectId=` | Project | 弱依赖,FK 校验 |
| Project 详情页"迭代管理 Tab" | Project | 前端组件复用 `sprint/list` 接口 + projectId |

## 6. 前端 review

- ✅ Wjl (solo) self-review
- "项目级单一活跃迭代"约束在 UI 上的体现: 新建/启动迭代前,前端先调 `current?projectId=` 检查,有则给出二次确认

## 7. 变更记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 初版 |

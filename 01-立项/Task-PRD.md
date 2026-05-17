# PRD: Task 模块 — 任务管理 (F3.4)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F3.4 + 原型 kanban.html) |
| 作者 | Wjl |
| PRD § | F3.4 (AgriAI-PLM-完整PRD文档.md §F3.4 迭代与任务管理) |
| 原型 HTML | [kanban.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html) (kanban 看板 4 列拖拽) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Task (F3.4)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(任务散在飞书 / 优先级凭经验 / 工时未追踪 / requirementId FK 断链导致追溯失败)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F3.4 验收标准 + 模块特有衡量指标(task↔requirement 关联率 ≥ 95% / 工时填报覆盖)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **任务依赖关系图谱** — 仅 dependsOnTaskIds CSV,可视化留 v0.3
- **AI 工时预估** — 仅人工填 estimatedHours,AI 预估留 v0.5+
- **任务自动分配(负载均衡)** — 仅 assigneeUserId 手填,AI 分配留 v0.5+
- **跨项目任务复用** — 单 projectId,复用留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:开发 / 测试 / PM / Scrum Master。

### 2.2 典型场景

**S1 任务拆分 + 看板拖拽**(最高频)
<待人工填写>:从 requirement 拆任务 → 落到 kanban 4 列(待办 / 开发中 / 测试中 / 已完成)→ 拖拽改状态

**S2 评审 / 测试打回**(关键流程)
<待人工填写>:02 → 01 反向边(评审打回),03 → 02 反向边(测试打回);打回必填 reviewNote

**S3 工时统计**(高价值)
<待人工填写>:estimatedHours vs actualHours 偏差,sprintOnTimeRate 衡量

**S4 需求追溯**(强 FK)
<待人工填写>:requirementId FK 必填 — "这个 task 来自哪个需求"一键定位

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Task (F3.4)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-task.sql):
- 基础: taskId / taskNo(TASK-YYYY-NNNN)/ projectId(FK)/ sprintId(FK 可选)/ requirementId(FK 强约束)
- 用户输入: title / description / taskType / priority / assigneeUserId / estimatedHours / dependsOnTaskIds(CSV)
- 流程: status(5 态) / actualHours / reviewNote
- AI 派生: aiPriorityScore(可选)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) task 行:`00→01→02→03→04` 含反向边 02↔01 / 03↔02。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 待办 | {01 开发中} | 默认初始状态 |
| 01 | 开发中 | {02 测试中} | — |
| 02 | 测试中 | {01 开发中(评审打回), 03 验证中} | 反向边 02→01 = 评审打回 |
| 03 | 验证中 | {02 测试中(测试打回), 04 已完成} | 反向边 03→02 = 测试打回 |
| 04 | 已完成 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 02→01 / 03→02 必填 reviewNote
- requirementId FK 强约束(702)
- 字典白名单 taskType / priority(604)

---

## 5. AI 能力

### 5.1 AI 端点
(当前 task 模块无独立 AI 端点。AI 价值通过 requirement.aiValue 上溯 + 任务级 aiPriorityScore 派生字段。)

### 5.2 当前阶段实现
n/a(留 v0.5+ AI 工时预估 / 优先级智能排序)

### 5.3 mock 输出 / Dify 工作流
n/a

---

## 6. 验收标准

**PRD §F3.4 验收**:
- ⏳ task ↔ requirement FK 关联率 ≥ 95%
- ⏳ kanban 4 列拖拽改状态延迟 < 500ms

**模块特有验收**:
<待人工填写>:E2E 测试 / 5 态合法/非法转换 + 2 个反向边单测 / FK 强约束 / 字典白名单

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Task-数据库设计.md](../02-设计/Task-数据库设计.md)
- API 设计: [Task-API设计.md](../02-设计/Task-API设计.md)
- 测试计划: [Task-测试计划-2026-05-17.md](../04-测试/Task-测试计划-2026-05-17.md)
- 发布计划: [Task-发布计划-2026-05-17.md](../05-上线/Task-发布计划-2026-05-17.md)
- 原型: [kanban.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html)
- AgriAI PRD: [§F3.4](../prd和原型/AgriAI-PLM-完整PRD文档.md)

# PRD: Task 模块 — 开发任务 (F3.4)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 (实质内容,2026-05-17 由 Claude 基于 PRD-MAPPING §2 + AgriAI PRD §F3.4 + 原型 kanban.html 生成) |
| 作者 | Wjl |
| PRD § | F3.4 (AgriAI-PLM-完整PRD文档.md §F3.4 研发执行管理 — Task 部分) |
| 原型 HTML | [kanban.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html) (modal-newtask: L149 / modal-taskdetail: L175-216 含关联 MR/代码评审/评论/操作历史) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | [ADR-0003](../03-开发/ADR/) Task 编号规则 `TASK-YYYY-NNNN` |
| 关联 OKR | _2026 Q2-O3-KR3: PLM Task 模块上线,Task → 需求/迭代/MR 三链追溯率 100%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Task (F3.4)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的开发任务管理目前用看板工具 (Trello / 飞书项目),4 个具体问题:

1. **需求-任务-MR 三链断裂**:Task 在飞书项目,需求在另一个文档,MR 在 Gitea — **无法回答 "MR abc123 是改什么需求的"**,代码评审无业务上下文。
2. **看板列状态不严**:开发把"测试中"任务拉回"开发中"无校验,**实际是测试反馈了 bug 但没走正规反向边**,后续追溯困难。
3. **工时数据失真**:预估工时 estimatedHours 拍脑袋填,实际工时 actualHours 没人填,**估算精度无法迭代提升**。
4. **代码评审打回无记录**:CR 打回靠 PR 评论,Task 状态没回退到"开发中",**评审节点完全游离于 task 状态机外**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 500 个 Task 的完整生命周期数据,做"需求 → 任务 → MR → 测试" 4 点强 FK 关联,代码评审反向边入状态机。

**衡量指标**:
- **Task → Requirement FK 关联率 ≥ 90%**(基线 30%)
- **Task → Sprint FK 关联率 ≥ 95%**(基线 50%)
- **Task → MR 关联率 ≥ 80%**(基线 0%)
- **预估工时误差 ≤ 30%**(估算 vs 实际,连续 3 月达标后再降到 20%)
- **代码评审打回入状态机率 100%**(CR 打回必走 02→01 反向边)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **Task 子任务**(嵌套任务树)— 留 v0.3,本期单层 Task
- **看板拖拽改 status**(UI 拖拽触发 PUT)— 留 v0.3 前端增强,本期仅表单 PUT
- **AI 工时预估**(基于历史 Task 数据自动估)— 留 v0.5+
- **跨 Sprint Task 自动迁移**(未完成 Task 滚到下个 Sprint)— 留 v0.3
- **Task 依赖图**(blocks/blocked_by 关系)— 留 v0.3
- **AI 代码审查辅助**(MR 自动检查)— [PLM-路线图.md 永不做剥离清单](../99-跨阶段/PLM-路线图.md) 第 3 项

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **开发 (Dev)** | CRUD assigneeUserId=自己的 Task | 接任务 / 推状态 / 关联 MR / 填实际工时 |
| **项目经理 (PM)** | CRUD 自己项目下的 Task | 拆任务 / 指派 / 关联 Requirement+Sprint |
| **测试 (QA)** | 查看 + 评论 | 测试中标识 + 测试不通过打回 |
| **管理员 (admin)** | 全 CRUD | 跨项目跟踪 + 看板视图 |

### 2.2 典型场景

**S1 PM 拆任务**(最高频)
> PM 王把 Requirement REQ-2026-0089 拆成 5 个 Task → 进入 kanban 看板 → 新建 → 标题 "实现土壤含水率监测 API" + 关联需求=REQ-2026-0089 + 看板列="待开发" + 优先级=P0 + 负责人=李工 + 预估工时=16h → 保存 → status='00 待开发' → 飞书通知李工(v0.3 集成,本期人工)

**S2 开发接任务 → 开发中 → 提 MR**(关键流程)
> 李工接任务 → status='00 → 01 开发中' → 编码完成 → 进入 task 详情 → 填 mrUrl="https://gitea/.../mr/42" + mrBranch="feat/REQ-89-soil-moisture" → 推 status='01 → 02 代码评审' → 评审人收通知

**S3 代码评审打回**(反向边 02→01)
> 评审发现 SQL 有性能问题 → 评审人在 task 详情改 status='02 → 01 开发中'(**反向边 02→01,评审打回**)+ 评论说明 → 李工再改

**S4 评审通过 → 测试中**(中频)
> 评审通过 → status='02 → 03 测试中' → QA 收通知拿 mrBranch 部署测试环境

**S5 测试打回**(反向边 03→02)
> QA 测试发现 bug → 改 status='03 → 02 代码评审'(**反向边 03→02,测试打回**)+ 评论 → 评审人再 review

**S6 完成**(终态)
> QA 测试通过 → status='03 → 04 已完成' + actualHours=20h(超估 4h)→ 列表过滤显示已完成,工时统计入 Sprint 累加

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Task (F3.4)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: taskId / taskNo (`TASK-YYYY-NNNN`, ADR-0003) / projectId(FK 必)/ requirementId(FK 可空)/ sprintId(FK 可空)
- 用户输入: title / description / priority(P0-P2)/ assigneeUserId(FK→sys_user)/ estimatedHours
- 流程: status(6 态含反向边)/ actualHours(完成时填)
- MR 关联: mrUrl / mrBranch
- 扩展: tags(本会话审计建议加,留 v0.3)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) task 行:

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 待开发 | {01 开发中} | 默认初始;PM 创建后 |
| 01 | 开发中 | {02 代码评审} | 开发提 MR 后推此态 |
| 02 | 代码评审 | {01 开发中(反向边), 03 测试中} | 评审打回 02→01 / 评审通过 02→03 |
| 03 | 测试中 | {02 代码评审(反向边), 04 已完成} | 测试打回 03→02 / 测试通过 03→04 |
| 04 | 已完成 | {} | 终态 |
| 05 | 已取消 | {} | 终态(任意点取消) |

**特殊规则**:
- 任意态 → 05 已取消 允许(取消是开放路径)
- 04 → 任何态 抛 601(终态保护)
- 新建强制 `status='00'`(违反抛 601)
- 反向边 `02→01`(评审打回)与 `03→02`(测试打回)是项目质量保障的核心机制
- `requirementId` / `sprintId` 可空,但若填必须 FK 存在(702)
- 看板列(原型 `nt-col` 6 选项)由 status 直接驱动,无独立 taskColumn 字段(本会话审计 D3 决定)

---

## 5. AI 能力

### 5.1 当前状态

详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) — **Task 模块本期无 AI 端点**。原型有"AI 推荐 PR 评审人"等设想,但因 [路线图剥离](../99-跨阶段/PLM-路线图.md) 不实施。

### 5.2 留 v0.3 增强方向

- AI 工时预估(基于历史 Task description embedding + actualHours 统计)
- AI 评审人推荐(基于历史 MR review 数据 + 文件 owner)
- AI 任务依赖识别(自动推断 blocks/blocked_by)

---

## 6. 验收标准

[PRD §F3.4 验收](../prd和原型/AgriAI-PLM-完整PRD文档.md):
- ⏳ **Task 看板与代码仓库强关联**(本期 mrUrl/mrBranch 字段已就位,Gitea 双向同步留 v0.3)
- ⏳ **代码评审节点入状态机**(本会话 02↔01 反向边已实现)

**模块特有验收**(本会话已落地):
- 6 态状态机合法转换 + 反向边 02↔01, 03↔02 单测覆盖
- 终态保护 04→任意 抛 601 单测覆盖
- FK 校验:projectId 必 / requirementId / sprintId 可空但若填必须存在(702)
- 字段白名单 status / priority 抛 604
- E2E 测试覆盖完整 6 态 + 2 个反向边

---

## 7. 不做的事 — 详 §1.3

- 子任务 / 拖拽 UI / AI 工时 / 跨 Sprint 迁移 / 依赖图 / AI 代码审查

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Task-数据库设计.md](../02-设计/Task-数据库设计.md)
- API 设计: [Task-API设计.md](../02-设计/Task-API设计.md)
- 测试计划: [Task-测试计划-2026-05-17.md](../04-测试/Task-测试计划-2026-05-17.md)
- 发布计划: [Task-发布计划-2026-05-17.md](../05-上线/Task-发布计划-2026-05-17.md)
- 原型: [kanban.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html)
- AgriAI PRD: [§F3.4](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块:
  - [Requirement-PRD.md](Requirement-PRD.md) (Task.requirementId FK)
  - [Sprint-PRD.md](Sprint-PRD.md) (Task.sprintId FK)
  - [Defect-PRD.md](Defect-PRD.md) (Defect.taskId FK→Task)

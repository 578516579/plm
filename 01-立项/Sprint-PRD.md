# PRD: Sprint 模块 — 迭代管理 (F3.4)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 (实质内容,2026-05-17 由 Claude 基于 PRD-MAPPING §2 + AgriAI PRD §F3.4 + 原型 kanban.html 生成) |
| 作者 | Wjl |
| PRD § | F3.4 (AgriAI-PLM-完整PRD文档.md §F3.4 研发执行管理) |
| 原型 HTML | [kanban.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html) (modal-sprint: 迭代管理弹窗 + 看板视图) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | [ADR-0004](../03-开发/ADR/) Sprint 编号规则 `SPR-YYYY-NNNN` |
| 关联 OKR | _2026 Q2-O3-KR2: PLM Sprint 模块上线,Sprint 准时完成率 ≥ 80%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Sprint (F3.4)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的迭代管理目前用飞书表格 + Sprint 周会口头同步,3 个具体问题:

1. **Sprint 状态散落**:同时有 "Sprint 4 在飞书表格" / "Sprint 3 在测试环境" / "Sprint 5 在规划中" — 没人能一句话说清"当前活跃 Sprint 是哪个";**Sprint 周会前 PM 花 20 分钟对账**。
2. **多活跃 Sprint 混淆**:历史上出现过同一项目下同时 2 个 Sprint 都标 "进行中" 的乌龙,**任务归属不清,工时统计双算**。
3. **实际起止日期无追溯**:计划起止 vs 实际起止常有差异,但只在飞书表格手写,**追溯 "Sprint 3 为什么延期 1 周" 要翻聊天记录**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 30 个 Sprint 的完整数据,做到"一项目一活跃 Sprint"硬约束,追溯计划 vs 实际差异自动入库。

**衡量指标**:
- **Sprint 准时完成率 ≥ 80%**(计划 endDate vs 实际 endDate 差异 ≤ 3 天比例)
- **单活跃 Sprint 约束 100% 生效**(Service 抛 703,违反不可入库)
- **Sprint → Task 关联率 ≥ 95%**(每个 Sprint 必有 ≥ 1 个关联 Task)
- **Sprint 周会准备时间 ≤ 5 分钟**(基线 20 分钟)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **Sprint 燃尽图**(burndown chart 可视化)— 留 v0.3 Analytics 模块承接
- **Sprint 复盘自动生成**(基于 task 完成度统计)— 留 v0.3 AI 增强
- **Sprint 模板复用**(从历史 Sprint 复制规划)— 留 v0.5+
- **跨项目 Sprint 联动**(同一时间窗口多项目对齐)— 留 v0.5+,本期单 projectId
- **Sprint 容量自动校验**(team×durationDays 容量预算 vs task 工时累加)— 留 v0.3
- **AI 推荐 Sprint 目标**(基于历史 Sprint 完成度建议)— 路线图剥离

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **项目经理 (PM)** | CRUD 自己项目下的 Sprint | 规划新 Sprint / 启动 / 收尾 |
| **管理员 (admin)** | 全 CRUD | 跨项目跟踪进度 |
| **开发 / 测试** | 查看 + 评论 | 知道当前 Sprint 范围,接 task |

### 2.2 典型场景

**S1 新 Sprint 规划**(高频)
> 王 PM 上一 Sprint 收尾,要规划 Sprint 4 → 进入 kanban → 点 "迭代管理" → 新建 → 名称="Sprint 4" + 目标="智慧灌溉 v2.1 提测" + 工期=14 天 + 开始日期=2026-05-20 → 保存 → status='00 计划中',系统自动算 plannedEndDate = startDate + 14 天

**S2 Sprint 启动**(关键流程,触发硬约束)
> 王 PM 想启动 Sprint 4 → 改 status='01 进行中' → **Service 校验:同 projectId 下 status='01' 唯一(703)**;若存在则抛 "项目已有活跃迭代 Sprint 3,请先收尾再启新迭代" → PM 先把 Sprint 3 推 02 已完成 / 03 已取消,再启动 Sprint 4 → actualStartDate 自动填 当日

**S3 Sprint 收尾**(终态)
> Sprint 4 末日 → 王 PM 推 status='02 已完成' → actualEndDate 自动填 → 列表上"实际起止"vs"计划起止"可直观对比 → 准时完成率自动入统计

**S4 Sprint 取消**(非常规)
> Sprint 启动 3 天后发现紧急需求穿插,Sprint 4 计划全废 → status='01 → 03 已取消' → task 转下个 Sprint

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Sprint (F3.4)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: sprintId / sprintNo (`SPR-YYYY-NNNN`, ADR-0004) / projectId(FK 必)
- 规划字段: name(默认占位 "Sprint 4")/ goal(一句话目标)/ plannedStartDate / plannedEndDate / durationDays(默认 14)
- 实际字段(服务计算): actualStartDate(00→01 触发)/ actualEndDate(01→02 触发)
- 流程: status(4 态)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) sprint 行:

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 计划中 | {01 进行中, 03 已取消} | 默认初始;启动前可直接取消 |
| 01 | 进行中 | {02 已完成, 03 已取消} | 受单活跃约束(703);01→02 自动填 actualEndDate |
| 02 | 已完成 | {} | 终态 |
| 03 | 已取消 | {} | 终态 |

**特殊规则**:
- **业务硬规则 703**:同 `project_id` 下 `status='01'` 唯一(单活跃 Sprint 约束),违反抛 ServiceException(703,业务硬规则)
- 00→01 转换时,Service 自动填 `actualStartDate = NOW()`
- 01→02 转换时,Service 自动填 `actualEndDate = NOW()`
- 新建强制 `status='00'`(违反抛 601)
- `plannedEndDate` 可由 `startDate + durationDays` 自动推导

---

## 5. AI 能力

### 5.1 当前状态

详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) — **Sprint 模块本期无 AI 端点**。

### 5.2 留远期增强方向

- AI 推荐 Sprint 目标(基于历史 Sprint 完成度 + 当前 backlog)— 路线图剥离
- AI 容量校验(team×durationDays vs task estimatedHours 累加合理性)— 留 v0.3

---

## 6. 验收标准

[PRD §F3.4 验收](../prd和原型/AgriAI-PLM-完整PRD文档.md):
- ⏳ **Sprint 看板与任务强关联**(本期 Task.sprintId FK 已就位,可选关联)
- ⏳ **Sprint 数据沉淀作为复盘依据**(本期数据入库,复盘 AI 留 v0.3)

**模块特有验收**(本会话已落地):
- 4 态状态机合法转换单测覆盖
- **单活跃 Sprint 约束 703** 业务硬规则单测覆盖
- actualStartDate / actualEndDate 自动填触发单测覆盖
- FK 校验:projectId 必,违反抛 702
- E2E 测试覆盖完整 Sprint 生命周期(00→01→02)

---

## 7. 不做的事 — 详 §1.3

- 燃尽图 / 复盘自动 / 模板复用 / 跨项目联动 / 容量校验 / AI 推荐目标

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Sprint-数据库设计.md](../02-设计/Sprint-数据库设计.md)
- API 设计: [Sprint-API设计.md](../02-设计/Sprint-API设计.md)
- 测试计划: [Sprint-测试计划-2026-05-17.md](../04-测试/Sprint-测试计划-2026-05-17.md)
- 发布计划: [Sprint-发布计划-2026-05-17.md](../05-上线/Sprint-发布计划-2026-05-17.md)
- 原型: [kanban.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html)
- AgriAI PRD: [§F3.4](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Task-PRD.md](Task-PRD.md) (Task.sprintId FK→Sprint)

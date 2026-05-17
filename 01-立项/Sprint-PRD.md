# PRD: Sprint 模块 — 迭代管理 (F3.4)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F3.4 + 原型 kanban.html) |
| 作者 | Wjl |
| PRD § | F3.4 (AgriAI-PLM-完整PRD文档.md §F3.4 迭代与任务管理) |
| 原型 HTML | [kanban.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html) (sprint 切换 + 燃尽图) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Sprint (F3.4)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(迭代计划散在 Excel / 任务跨迭代漂移 / 燃尽图人工绘制 / 多迭代并行导致团队焦点分散)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F3.4 验收标准 + 模块特有衡量指标(迭代准时率 / 单活跃迭代约束)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **燃尽图实时计算** — 仅快照 burndown JSON,实时计算留 v0.3
- **多迭代并行** — 单活跃约束 703,放宽留 v0.5+
- **跨迭代任务自动滚动** — 仅手动改 sprintId,自动滚动留 v0.5+
- **迭代回顾会议结构化记录** — 仅 retroNote 文本,结构化留 v0.3

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:Scrum Master / PM / 开发 / 测试。

### 2.2 典型场景

**S1 迭代规划**(最高频)
<待人工填写>:新建 sprint → 规划任务 → 启动 01 → 燃尽图更新

**S2 单活跃约束**(关键校验)
<待人工填写>:同一 projectId 下只允许一个 status='01' 活跃 sprint,违反抛 703

**S3 关联任务保护**(关键校验)
<待人工填写>:已有 task FK 引用本 sprint 时不允许删除,抛 704

**S4 迭代结束 / 取消**(终态)
<待人工填写>:完成 02 / 取消 03

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Sprint (F3.4)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-sprint.sql):
- 基础: sprintId / sprintNo(SP-YYYY-NNNN)/ projectId(FK)
- 用户输入: sprintName / goal / startDate / endDate / capacityHours
- 流程: status / ownerUserId
- 派生: progressPct / actualVelocity / burndownJson

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) sprint 行:`00→{01,03}` `01→{02,03}` `02,03` 终态。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 待启动 | {01 进行中, 03 已取消} | 默认初始状态 |
| 01 | 进行中 | {02 已完成, 03 已取消} | **单活跃约束 703** |
| 02 | 已完成 | {} | 终态 |
| 03 | 已取消 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 同 projectId 下只能有一个 status='01' sprint(703 业务硬规则)
- 已有 task FK 引用时不允许删除(704)

---

## 5. AI 能力

### 5.1 AI 端点
(本模块无独立 AI 端点。AI 价值在 task 模块的优先级评估与 analytics 模块的 sprintOnTimeRate 复盘。)

### 5.2 当前阶段实现
n/a

### 5.3 mock 输出 / Dify 工作流
n/a

---

## 6. 验收标准

**PRD §F3.4 验收**:
- ⏳ 单 sprint 活跃约束硬校验
- ⏳ 燃尽图数据准确率 100%

**模块特有验收**:
<待人工填写>:E2E 测试 / 703 单活跃 / 704 关联任务保护 / FK 校验

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Sprint-数据库设计.md](../02-设计/Sprint-数据库设计.md)
- API 设计: [Sprint-API设计.md](../02-设计/Sprint-API设计.md)
- 测试计划: [Sprint-测试计划-2026-05-17.md](../04-测试/Sprint-测试计划-2026-05-17.md)
- 发布计划: [Sprint-发布计划-2026-05-17.md](../05-上线/Sprint-发布计划-2026-05-17.md)
- 原型: [kanban.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html)
- AgriAI PRD: [§F3.4](../prd和原型/AgriAI-PLM-完整PRD文档.md)

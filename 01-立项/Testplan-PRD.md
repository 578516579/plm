# PRD: TestPlan 模块 — 测试计划 (F4.1)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F4.1 + 原型 testplan.html) |
| 作者 | Wjl |
| PRD § | F4.1 (AgriAI-PLM-完整PRD文档.md §F4.1 测试计划) |
| 原型 HTML | [testplan.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testplan.html) (modal-newtp + tpContent + tpScope) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "TestPlan (F4.1)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(测试计划散在 Excel / 范围与策略人工拍 / 资源排期凭经验 / 与 sprint/testcase 关联断链)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F4.1 验收标准 + 模块特有衡量指标(AI 计划生成时间 / 计划覆盖度)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **测试资源排期甘特图** — 仅文本计划,排期可视化留 v0.3
- **多人协作编辑** — 单 authorUserId,锁机制留 v0.3
- **跨项目测试计划模板复用** — 单 projectId,模板库留 v0.5+
- **AI 风险评估自动评分** — 仅 riskAssessment 文本,评分留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:测试经理 / QA / PM / 评审 admin。

### 2.2 典型场景

**S1 AI 辅助生成测试计划**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newtp 字段 → AI 生成 testStrategy / testScope / resourcePlan / riskAssessment / scheduleContent 5 段

**S2 计划评审 + 关联 sprint**(关键流程)
<待人工填写>:sprintId(可选 FK)关联迭代;评审通过后进入 02 已执行

**S3 计划归档**(终态)
<待人工填写>:执行完成 → 03 已归档

**S4 计划与测试用例关联**(数据完整)
<待人工填写>:testplan ↔ testcase 多对多关联表(留 v0.3 显式;本期通过 sprintId 隐式)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "TestPlan (F4.1)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-testplan.sql):
- 基础: testplanId / testplanNo(TP-YYYY-NNNN)/ projectId(FK)/ sprintId(可选 FK)
- 用户输入: title / testStrategy / testScope / startDate / endDate / resourcePlan
- AI 输出: scheduleContent / riskAssessment / aiGenerated / aiGeneratedAt
- 流程: status(4 态) / authorUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) testplan 行:`00→01→02→03` (4 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已评审} | 默认初始状态 |
| 01 | 已评审 | {02 已执行} | 评审通过 |
| 02 | 已执行 | {03 已归档} | 执行完成 |
| 03 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- FK 校验 projectId 必填(702)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/testplan/ai/generate — Dify 工作流 test-plan-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 字段已留位(aiGenerated),Dify 实接入留 v0.5+。本期占位 mock(返回 5 段标准测试计划模板)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4。

---

## 6. 验收标准

**PRD §F4.1 验收**:
- ⏳ AI 生成测试计划时间 < 5 分钟
- ⏳ 测试计划 5 段覆盖完整(策略/范围/资源/风险/排期)

**模块特有验收**:
<待人工填写>:E2E 测试 / FK 校验 / 状态机校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Testplan-数据库设计.md](../02-设计/Testplan-数据库设计.md)
- API 设计: [Testplan-API设计.md](../02-设计/Testplan-API设计.md)
- 测试计划: [Testplan-测试计划-2026-05-17.md](../04-测试/Testplan-测试计划-2026-05-17.md)
- 发布计划: [Testplan-发布计划-2026-05-17.md](../05-上线/Testplan-发布计划-2026-05-17.md)
- 原型: [testplan.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testplan.html)
- AgriAI PRD: [§F4.1](../prd和原型/AgriAI-PLM-完整PRD文档.md)

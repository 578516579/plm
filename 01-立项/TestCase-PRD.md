# PRD: TestCase 模块 — 测试用例 (F4.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F4.2 + 原型 testcase.html) |
| 作者 | Wjl |
| PRD § | F4.2 (AgriAI-PLM-完整PRD文档.md §F4.2 测试用例) |
| 原型 HTML | [testcase.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testcase.html) (用例列表 + Excel 导入导出) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "TestCase (F4.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(测试用例散在 Excel / 与需求 FK 断链 / AI 生成用例缺失 / 多用例集版本难管)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F4.2 验收标准 + 模块特有衡量指标(用例 → 需求 FK 关联率 ≥ 95% / AI 生成用例采纳率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **用例自动执行** — 仅文本步骤,自动执行留 autotest 模块
- **用例评审打分** — 仅 priority,评分留 v0.3
- **多版本用例对比** — 仅版本字段,对比留 v0.3
- **用例与缺陷反向关联追溯** — 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:QA / 测试 / PM / 开发(评审参与)。

### 2.2 典型场景

**S1 AI 辅助生成用例**(最高频)
<待人工填写>:从 requirement / prd 输入 → AI 生成测试步骤 + 预期结果

**S2 用例归档**(终态)
<待人工填写>:用例稳定后 02 已归档,保留历史可查

**S3 用例 ↔ 需求关联**(强 FK)
<待人工填写>:requirementId FK 必填,追溯完整

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "TestCase (F4.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-testcase.sql):
- 基础: testcaseId / testcaseNo(TC-YYYY-NNNN)/ projectId(FK)/ requirementId(FK)
- 用户输入: title / preconditions / steps / expectedResult / priority / caseType
- AI 输出: aiGenerated / aiGeneratedAt
- 流程: status(3 态) / authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) testcase 行:`00→01→02` (3 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已评审} | 默认初始状态 |
| 01 | 已评审 | {02 已归档} | — |
| 02 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- requirementId FK 强约束(702)
- priority / caseType 字典白名单(604)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/testcase/ai/generate — Dify 工作流 testcase-gen-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 字段已留位(aiGenerated),Dify 实接入留 v0.5+。本期占位 mock(按 requirement 描述生成 3-5 个测试步骤模板)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4。

---

## 6. 验收标准

**PRD §F4.2 验收**:
- ⏳ AI 生成用例采纳率 ≥ 60%
- ⏳ 用例 ↔ 需求 FK 关联率 ≥ 95%

**模块特有验收**:
<待人工填写>:E2E 测试 / FK 校验 / 字典白名单 / 状态机校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [TestCase-数据库设计.md](../02-设计/TestCase-数据库设计.md)
- API 设计: [TestCase-API设计.md](../02-设计/TestCase-API设计.md)
- 测试计划: [TestCase-测试计划-2026-05-17.md](../04-测试/TestCase-测试计划-2026-05-17.md)
- 发布计划: [TestCase-发布计划-2026-05-17.md](../05-上线/TestCase-发布计划-2026-05-17.md)
- 原型: [testcase.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testcase.html)
- AgriAI PRD: [§F4.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)

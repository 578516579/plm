# PRD: Submission 模块 — 提测管理 (F4.4)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F4.4 + 原型 submit.html) |
| 作者 | Wjl |
| PRD § | F4.4 (AgriAI-PLM-完整PRD文档.md §F4.4 提测管理) |
| 原型 HTML | [submit.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/submit.html) (modal-newsubmit + 4 项质量门禁 + 退回 modal) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Submission (F4.4)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(提测靠口头 / 质量门禁手工核查 / 单测覆盖率/代码扫描/PRD完整/API文档缺一项常被开发漏检 / 退回原因散在 IM)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F4.4 验收标准 + 模块特有衡量指标(qualityGatePassed 自动判定 / 提测一次通过率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **质量门禁阈值自定义** — 仅单测覆盖率 ≥ 60 固定,自定义留 v0.5+
- **自动从 Jenkins/SonarQube 拉指标** — 仅人工填,自动拉取留 v0.5+
- **多轮提测对比** — 单次提测,多轮对比留 v0.3
- **AI 退回原因分析** — 仅文本 rejectReason,AI 分析留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:开发 / QA / 测试经理 / 评审 admin。

### 2.2 典型场景

**S1 开发发起提测**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newsubmit 字段(title / scope / environment / expectedTestDays / 4 项门禁)→ 自动算 qualityGatePassed(4 项 ∧)

**S2 质量门禁未通过**(关键校验)
<待人工填写>:任意一项门禁不通过 → qualityGatePassed='N' → 02→03 转态抛 708(质量门禁未通过)

**S3 测试退回**(反向边)
<待人工填写>:04→00 反向边,rejectReason 必填(602)

**S4 提测通过**(终态)
<待人工填写>:02→03 已通过,触发后续测试执行

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Submission (F4.4)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT,§2 模板示例已给出完整表):
- 基础: submissionId / submissionNo(SUB-YYYY-NNNN)/ projectId(FK)
- 用户输入: title / scope / environment(默认 staging)/ expectedTestDays
- 4 项门禁: unitTestCoverage(≥ 60)/ codeScanPassed(Y/N)/ prdCompleted(Y/N)/ apiDocUpdated(Y/N)
- 派生: qualityGatePassed(4 项 ∧,不接受用户输入)
- 流程: status(5 态)/ rejectReason(条件必填)/ submitterUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) submission 行:`00→01→{02,04}` `02→{03,04}` `04→00` (反向边)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已提交} | 默认初始状态 |
| 01 | 已提交 | {02 评审中, 04 已退回} | — |
| 02 | 评审中 | {03 已通过, 04 已退回} | — |
| 03 | 已通过 | {} | 终态 — 必须 qualityGatePassed='Y' |
| 04 | 已退回 | {00 草稿} | 反向边;必填 rejectReason |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 04→00 必填 rejectReason(602)
- **02→03 强制要求 qualityGatePassed='Y'**(违反抛 708)
- environment 字典白名单(604)

---

## 5. AI 能力

### 5.1 AI 端点
(本模块当前阶段无 AI 端点。未来留位 AI 退回原因分析。)

### 5.2 当前阶段实现
n/a

### 5.3 mock 输出 / Dify 工作流
n/a — 留 v0.5+

---

## 6. 验收标准

**PRD §F4.4 验收**:
- ⏳ 4 项质量门禁强制自动判定
- ⏳ 单测覆盖率 ≥ 60% 阈值

**模块特有验收**:
<待人工填写>:E2E 测试 / 5 态合法/非法转换 + 反向边 04→00 单测 / 708 门禁失败 / 602 退回必填。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Submission-数据库设计.md](../02-设计/Submission-数据库设计.md)
- API 设计: [Submission-API设计.md](../02-设计/Submission-API设计.md)
- 测试计划: [Submission-测试计划-2026-05-17.md](../04-测试/Submission-测试计划-2026-05-17.md)
- 发布计划: [Submission-发布计划-2026-05-17.md](../05-上线/Submission-发布计划-2026-05-17.md)
- 原型: [submit.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/submit.html)
- AgriAI PRD: [§F4.4](../prd和原型/AgriAI-PLM-完整PRD文档.md)

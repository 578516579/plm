# PRD: TestReport 模块 — 测试报告 (F4.7)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F4.7 + 原型 testreport.html) |
| 作者 | Wjl |
| PRD § | F4.7 (AgriAI-PLM-完整PRD文档.md §F4.7 测试报告) |
| 原型 HTML | [testreport.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testreport.html) (modal-newtr + trContent + riskLevel 徽章) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "TestReport (F4.7)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(测试报告人工撰写 / 风险等级评估凭经验 / 测试结论与缺陷数据脱节 / 上线建议缺乏数据支撑)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F4.7 验收标准 + 模块特有衡量指标(AI 报告生成时间 / 风险等级评估准确率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **从 autotest / testcase 自动聚合数据** — 留 v0.5+,本期人工填
- **风险等级 AI 自动推断** — 留 v0.5+,本期人工填(green/yellow/red)
- **多版本报告对比** — 单版本,对比留 v0.3
- **AI 上线建议自动决策** — 仅 recommendation 文本,AI 决策留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:测试经理 / QA / PM / 上线评审 admin。

### 2.2 典型场景

**S1 AI 辅助生成报告**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newtr → AI 生成 reportContent / riskAssessment / recommendation

**S2 评审打回**(反向边)
<待人工填写>:01→00 反向边,reviewNote 必填

**S3 风险等级**(关键字段)
<待人工填写>:riskLevel 3 等级(green/yellow/red)— red 触发上线决策评审

**S4 上线建议**(关键产出)
<待人工填写>:recommendation 决策 — 上线/暂缓/不上

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "TestReport (F4.7)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-testreport.sql):
- 基础: testreportId / testreportNo(TR-YYYY-NNNN)/ projectId(FK)/ testplanId(可选 FK)
- 用户输入: title / reportContent / passRate / defectCount / criticalDefectCount / coveragePct / riskLevel / recommendation
- AI 输出: aiGenerated / aiGeneratedAt / riskAssessment
- 流程: status(3 态) / authorUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) testreport 行:`00→01→{00,02}` (3 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始状态 |
| 01 | 评审中 | {00 草稿(打回), 02 已发布} | 反向边 01→00 = 评审打回 |
| 02 | 已发布 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 01→00 必填 reviewNote
- **riskLevel 白名单 green/yellow/red**(违反抛 604)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/testreport/ai/generate — Dify 工作流 test-report-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 字段已留位(aiGenerated),Dify 实接入留 v0.5+。本期占位 mock(按 passRate + criticalDefectCount 阈值生成 mock 风险评估)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4。

---

## 6. 验收标准

**PRD §F4.7 验收**:
- ⏳ AI 生成测试报告时间 < 5 分钟
- ⏳ 风险等级 3 级评估

**模块特有验收**:
<待人工填写>:E2E 测试 / riskLevel 白名单 / 反向边 01→00 单测 / FK 校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Testreport-数据库设计.md](../02-设计/Testreport-数据库设计.md)
- API 设计: [Testreport-API设计.md](../02-设计/Testreport-API设计.md)
- 测试计划: [Testreport-测试计划-2026-05-17.md](../04-测试/Testreport-测试计划-2026-05-17.md)
- 发布计划: [Testreport-发布计划-2026-05-17.md](../05-上线/Testreport-发布计划-2026-05-17.md)
- 原型: [testreport.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testreport.html)
- AgriAI PRD: [§F4.7](../prd和原型/AgriAI-PLM-完整PRD文档.md)

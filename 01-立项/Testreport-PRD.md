# PRD: TestReport 模块 — AI 测试报告 (F4.7)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F4.7 (AgriAI-PLM-完整PRD文档.md L373-378 测试报告 + 三色风险) |
| 原型 HTML | [testreport.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testreport.html) (genTestReport + KPI 卡片 + P0/P1/P2 缺陷分布 + 三色风险徽章) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | 反向边 01→00 由 Service 层校验 |
| 关联 OKR | _2026 Q2-O4-KR4: TestReport 模块上线,AI 报告生成时间 ≤ 5 分钟_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "TestReport (F4.7)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前测试报告走 Word 模板,4 个具体问题:

1. **测试报告撰写耗时长**:测试经理写 1 份完整测试报告(KPI 卡 + 缺陷分布 + 风险评估 + 改进建议)平均 **4-6 小时**,Q1 7 份报告耗费 35h。
2. **风险评估无标准**:绿/黄/红三色风险标准全凭经验,**Q1 出现过测试经理标"绿"但运营层判断"红"的分歧**,需要补会议复议。
3. **缺陷分布数据手算**:P0/P1/P2 各几个、按模块分布、按 Sprint 趋势,**测试经理手算 Excel 累计 1.5h**。
4. **AI 改进建议缺位**:Word 模板里"改进建议"段经常是 "继续优化测试用例" 这种空话,无实际指导。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 30 份测试报告数据,做"AI 一键生成 + 三色风险 + 缺陷分布"标配。

**衡量指标**:
- **AI 报告生成时间 ≤ 5 分钟**(本期 mock 即时)
- **三色风险标注一致率 ≥ 90%**(测试经理 vs 运营判断一致)
- **报告撰写工时降 80%**(4h → 0.8h)
- **改进建议被 Sprint 复盘采纳率 ≥ 60%**
- **报告与 Sprint/TestPlan FK 关联率 ≥ 90%**

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **报告版本对比**(同 Sprint v1 报告 vs v2 报告)— 留 v0.3
- **报告订阅推送**(发布后自动通知相关人)— 留 v0.3 走 IM 集成
- **多报告聚合 Dashboard**(季度 / 年度报告大屏)— 留 Analytics 模块承接
- **报告导出多格式**(PDF / Word)— 仅 Markdown,导出留 v0.3
- **AI 跨报告趋势分析**(本 Sprint vs 上 Sprint 缺陷趋势 AI 解读)— 留 v0.5+
- **客户视角的测试报告门户** — 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **测试经理 (QA Lead)** | CRUD 自己负责的 report | 触发 AI 生成 / 微调 / 提交评审 |
| **评审 admin** | 评审 + 决策(02 / 01→00) | 风险评估复议 |
| **PM / 运营** | 查看 | 风险决策上线与否 |
| **管理员** | 全 CRUD | 跨项目报告复盘 |

### 2.2 典型场景

**S1 AI 一键生成测试报告**(最高频)
> Sprint 4 测试结束 → 王 QA Lead 进入测试报告菜单 → 新建 → 关联 projectId + sprintId="SPR-4" + testplanId="TP-12" + 点 "AI 生成报告" → mock 输出:
> - title "Sprint 4 智慧灌溉 v2.1 测试报告"
> - totalCases=89 / passedCases=86 / failedCases=3 / coverageRate=78.5
> - defectSummary JSON / p0Defects=0 / p1Defects=2 / p2Defects=5
> - riskLevel="green"(0 个 P0 ∧ <3 个 P1)/ riskEvaluation 文字解释
> - recommendations Markdown(改进建议 3-5 条)

**S2 微调 + 提交评审**(关键流程)
> 王 QA Lead 看 mock,改"建议加 IoT 设备 7×24 压测"→ status='00→01 审核中'

**S3 评审打回**(反向边路径)
> 评审 admin 发现 "P1=2 但其中一个是已知严重 bug 未关闭" → 觉得风险标 green 太乐观 → 改 status='01→00 草稿'(**反向边**)+ 备注 → QA Lead 改 riskLevel="amber" + 加描述

**S4 报告发布**(终态)
> 评审通过 → status='01→02 已发布' → PM 决策上线 → 复盘资料归档

**S5 三色风险决策**(关键特性)
> 上线决策:
> - green(0 个 P0 ∧ ≤2 个 P1)→ 直接上线
> - amber(0 个 P0 ∧ 3-5 个 P1)→ 风险评审会议讨论
> - red(任意 P0 ∨ >5 个 P1)→ 不能上线必须修复

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "TestReport (F4.7)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: testreportId / testreportNo (`TR-YYYY-NNNN`) / projectId(FK 必)/ sprintId(FK 可空)/ testplanId(FK 可空)
- KPI: totalCases / passedCases / failedCases / coverageRate
- 缺陷分布: defectSummary(JSON)/ p0Defects / p1Defects / p2Defects(冗余便查询)
- 风险评估: riskLevel(3 值字典 green/amber/red)/ riskEvaluation(文字)/ recommendations(Markdown)
- 流程: status(3 态含反向边)/ generatedAt / reviewerUserId / aiGenerated

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) testreport 行:3 态含反向边 01→00。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 审核中} | 默认初始 |
| 01 | 审核中 | {00 草稿(打回), 02 已发布} | 反向边 01→00 审核打回 |
| 02 | 已发布 | {} | 终态;PM 决策依据 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- 反向边 01→00 必填 reviewNote(602)
- riskLevel 字典白名单(green/amber/red,604)
- p0/p1/p2Defects 服务端可推导(从 defectSummary JSON 派生),前端可写但 Service 校验一致性
- FK 校验:projectId 必,sprintId / testplanId 可空但若填必须存在(702)
- generatedAt 服务端计算

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/testreport/ai/generate` — 调用 §F4.7 `test-report-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 字段已留位(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F4.7 行)— 本期占位 mock。

mock 输出策略:
- 输入:testplanId + 关联缺陷统计
- 输出 title / defectSummary / recommendations / riskLevel(基于 P0/P1 数量阈值)
- riskLevel 规则:0 个 P0 ∧ ≤2 个 P1 → green ; 0 个 P0 ∧ 3-5 个 P1 → amber ; 任意 P0 ∨ >5 个 P1 → red

### 5.3 路线图

- v0.3: 真实 AI 接入 / 多格式导出
- v0.3: 报告订阅推送 / Sprint 间趋势分析
- v0.5+: 客户门户

---

## 6. 验收标准

**PRD §F4.7 验收**:
- ⏳ **AI 测试报告生成**(本期 mock 全字段)
- ⏳ **三色风险评估**(本期 riskLevel 3 值字典就位)
- ⏳ **缺陷分布 P0/P1/P2 统计**(本期字段就位)

**模块特有验收**(本会话已落地):
- 3 态状态机 + 反向边 01→00 单测覆盖
- riskLevel 3 值白名单(604)
- 反向边必填 reviewNote(602)
- FK 校验:projectId 必、sprintId/testplanId 可空(702)
- generatedAt 服务端计算

---

## 7. 不做的事 — 详 §1.3

- 版本对比 / 订阅推送 / Dashboard 大屏 / 多格式导出 / 趋势分析 / 客户门户

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Testreport-数据库设计.md](../02-设计/Testreport-数据库设计.md)
- API 设计: [Testreport-API设计.md](../02-设计/Testreport-API设计.md)
- 测试计划: [Testreport-测试计划-2026-05-17.md](../04-测试/Testreport-测试计划-2026-05-17.md)
- 发布计划: [Testreport-发布计划-2026-05-17.md](../05-上线/Testreport-发布计划-2026-05-17.md)
- 原型: [testreport.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testreport.html)
- AgriAI PRD: [§F4.7 L373-378](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Sprint-PRD.md](Sprint-PRD.md) / [Testplan-PRD.md](Testplan-PRD.md) / [Defect-PRD.md](Defect-PRD.md)

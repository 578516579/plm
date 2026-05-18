# PRD: TestPlan 模块 — 测试方案 (F4.1)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F4.1 (AgriAI-PLM-完整PRD文档.md L334-337 测试方案) |
| 原型 HTML | [testplan.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testplan.html) (5 checkbox testTypes + testCycleDays + AI 生成 strategy/tools/resources/risks) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O4-KR1: PLM TestPlan 模块上线,测试方案 AI 生成时间 ≤ 10 分钟_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "TestPlan (F4.1)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前测试方案撰写走 Word 模板 + 飞书表格,4 个具体问题:

1. **测试方案撰写耗时**:测试经理写一份完整测试方案(范围/策略/工具/资源/风险)平均 **6-8 小时**,Q1 7 个新项目共耗费 50+ 工时。
2. **测试范围圈定不全**:测试 testTypes(功能/接口/性能/自动化/安全 5 类)往往漏一两类,**Q1 某项目上线后才发现安全测试未做,临时补做 3 天**。
3. **风险评估拍脑袋**:5 大测试风险(进度 / 资源 / 技术 / 农情设备 / 数据)全凭经验,**Q1 IoT 设备测试因农情季节性问题 (没赶上灌溉旺季) 推迟 2 周**。
4. **工具推荐参差不齐**:每个项目重复造轮子选工具(Selenium vs Playwright vs Cypress),**Q1 同公司 3 个项目用了 3 个不同的 E2E 框架**,知识无沉淀。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 30 份完整测试方案,做"5 类 testTypes + AI 一键生成 4 段(strategy/tools/resources/risk)" 让测试经理"6-8 小时人工写"做成"10 分钟 AI 生成 + 30 分钟微调"。

**衡量指标**:
- **AI 生成测试方案时间 ≤ 10 分钟**(本期 mock 即时返回)
- **5 testTypes 全选率 ≥ 80%**(5 类都被涵盖)
- **风险评估覆盖 5 大类 100%**(进度/资源/技术/农情设备/数据)
- **测试方案撰写工时降 70%**(6h → 2h)
- **AI 生成内容采纳率 ≥ 70%**

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **测试方案对比 Diff**(同项目 v1 vs v2 方案差异)— 留 v0.3
- **测试方案与实际执行偏差自动追溯**(计划 testTypes vs 实际跑了哪些)— 留 v0.3
- **资源占用自动校验**(plan 占用人力 / 设备 vs 团队可用)— 留 v0.3
- **测试方案模板用户自定义** — 仅 5 testTypes 字典,模板自定义留 v0.5+
- **方案审批工作流**(多人多级审批)— 单审批,留 v0.3
- **跨项目方案知识沉淀** — 留 v0.5+,需 AgriKB

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **测试经理 (QA Lead)** | CRUD 自己项目的 TestPlan | 创建 + 触发 AI 生成 + 微调 |
| **测试 (QA)** | 查看 + 评论 | 执行方案 / 反馈微调 |
| **PM** | 查看 + 评论 | 验证方案覆盖需求 |
| **管理员** | 全 CRUD | 跨项目复盘 |

### 2.2 典型场景

**S1 AI 生成测试方案**(最高频)
> 王 QA Lead 接到 Sprint 4 测试 → 进入 TestPlan 菜单 → 新建 → title "智慧灌溉 v2.1 测试方案" + 关联 projectId + sprintId="SPR-4" + testTypes 5 项全勾(功能 ∧ 接口 ∧ 性能 ∧ 自动化 ∧ 安全)+ testCycleDays=10 → 点 "AI 生成方案" → mock 输出 4 段 Markdown:strategy(测试策略)/ toolsRecommended(Playwright + JMeter + OWASP ZAP)/ resourcesPlan(3 测试 × 10 天 + 5 台设备)/ riskAssessment(5 大风险)

**S2 微调与确认**(关键流程)
> 王 QA Lead 看 mock 加 "农业 IoT 设备 24h 连续采集压测" 进 strategy → 改 status='00→01 已确认' → testers 开始按方案执行

**S3 测试执行 → 收尾**(状态推进)
> 进入执行期 → status='01→02 执行中' → 完成后 → status='02→03 已完成'

**S4 方案废弃**(终态)
> 项目暂停 → status 任意态可标 '99 已废弃'(本期简化为 4 态;废弃留 v0.3)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "TestPlan (F4.1)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: testplanId / testplanNo (`TP-YYYY-NNNN`) / projectId(FK 必)/ sprintId(FK 可空)
- 用户输入: title / testTypes(5 值 CSV 字典)/ testCycleDays / scope
- AI 输出 4 段: strategy / toolsRecommended / resourcesPlan / riskAssessment
- 流程: status(4 态)/ authorUserId / aiGenerated

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) testplan 行:4 态单向(无反向边)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已确认} | 默认初始 |
| 01 | 已确认 | {02 执行中} | 评审通过 |
| 02 | 执行中 | {03 已完成} | 测试团队执行 |
| 03 | 已完成 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- testTypes 5 值字典白名单(每个值都在 functional/api/performance/automation/security,604)
- testTypes 至少选 1 个(602)
- FK 校验:projectId 必,sprintId 可空但若填必须存在(702)

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/testplan/ai/generate` — 调用 §F4.1 `test-plan-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 字段已留位(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F4.1 行)— 本期占位 mock:基于 testTypes CSV + testCycleDays 生成 4 段 Markdown(strategy/tools/resources/risks)。

mock 输出策略:
- testTypes 含 "security" → 推荐 OWASP ZAP / 加安全测试段
- testTypes 含 "performance" → 推荐 JMeter / 加压测策略 + 农业 IoT 24h 连续采集场景
- testTypes 含 "automation" → 推荐 Playwright(项目惯例)
- 资源 = 测试人 × 测试周期 + 5 台设备(默认农业典型规模)
- 风险 = 5 大类(进度/资源/技术/农情设备/数据)

### 5.3 路线图

- v0.3: 真实 AI 接入 / 基于历史方案训练推荐
- v0.3: 方案-执行偏差追溯
- v0.5+: 跨项目方案复用

---

## 6. 验收标准

**PRD §F4.1 验收**:
- ⏳ **AI 生成 4 段方案**(本期 mock strategy/tools/resources/risks)
- ⏳ **5 类 testTypes 全覆盖**(本期 5 值字典就位)
- ⏳ **农业 IoT 风险考量**(本期 mock 含 5 大风险类)

**模块特有验收**(本会话已落地):
- 4 态状态机合法转换单测覆盖
- testTypes 5 值字典白名单(604)
- testTypes 至少选 1 个(602)
- FK 校验:projectId 必、sprintId 可空(702)

---

## 7. 不做的事 — 详 §1.3

- 方案 Diff / 偏差追溯 / 资源校验 / 模板自定义 / 多人审批 / 跨项目沉淀

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Testplan-数据库设计.md](../02-设计/Testplan-数据库设计.md)
- API 设计: [Testplan-API设计.md](../02-设计/Testplan-API设计.md)
- 测试计划: [Testplan-测试计划-2026-05-17.md](../04-测试/Testplan-测试计划-2026-05-17.md)
- 发布计划: [Testplan-发布计划-2026-05-17.md](../05-上线/Testplan-发布计划-2026-05-17.md)
- 原型: [testplan.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testplan.html)
- AgriAI PRD: [§F4.1 L334-337](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Sprint-PRD.md](Sprint-PRD.md)(testplan.sprintId FK)/ [TestReport-PRD.md](Testreport-PRD.md)(testreport.testplanId FK)

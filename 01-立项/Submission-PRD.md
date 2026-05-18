# PRD: Submission 模块 — 提测管理 (F4.4)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 (实质内容,2026-05-17 由 Claude 基于 PRD-MAPPING §2 + AgriAI PRD §F4.4 + 原型 submit.html 生成) |
| 作者 | Wjl |
| PRD § | F4.4 (AgriAI-PLM-完整PRD文档.md L355-359 — 智能提测管理 + AI 质量门禁) |
| 原型 HTML | [submit.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/submit.html) (modal-newsubmit: L158 + submitGate 检查面板 + modal-submitdetail 审批 tabs) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 OKR | _2026 Q2-O3-KR4: PLM 提测模块上线,提测一次通过率 ≥ 75%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Submission (F4.4)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的开发 → 测试交接当前用飞书消息 + Excel 提测单,4 个具体问题:

1. **提测无统一门禁,质量参差**:开发想提就提,**有些 commit 单测都没跑 / API 文档没更新就丢给测试**;Q1 有 30% 提测在 1 天内被退回 ("覆盖率不足"/"PRD 没写完")。
2. **门禁标准模糊**:口头要求 "代码扫描通过 / 文档完整",但**没数字门槛**;不同测试经理判断不同,新人测试不敢退回怕得罪开发。
3. **退回原因不结构化**:被退回时飞书消息一句话"还差点东西",**开发不知道差什么**,反复来回 3-4 轮。
4. **提测数据无法回溯**:某模块"为什么 v0.1.0 上线后线上 bug 多"无法追溯到 "当时提测时哪个门禁项是 N"。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 200 次提测的完整门禁数据,把"凭感觉提测"做成"4 项 AI 量化门禁",提测一次通过率从 70% → 90%。

**衡量指标**:
- **AI 质量门禁通过率 ≥ 90%**(单测覆盖率 ≥ 60% ∧ 代码扫描通过 ∧ PRD 完整 ∧ API 文档更新 4 项 ∧)
- **提测一次通过率 ≥ 75%**(基线 70%,被退回算二次提测)
- **退回原因结构化率 100%**(`rejectReason` 必填,无敷衍的"还差点东西")
- **平均单测覆盖率 ≥ 65%**(连续 3 月监控)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **AI 自动检查代码扫描结果**(对接 SonarQube/Codacy)— 留 v0.3,本期 codeScanPassed 字段由开发自填 Y/N
- **AI 自动检查 PRD 完整度**(对接 PRD 模块检查完整段落)— 留 v0.3,本期 prdCompleted 由开发自填
- **AI 自动检查 API 文档**(对接 Springdoc 检查新增端点是否文档化)— 留 v0.3,本期 apiDocUpdated 由开发自填
- **多人审批流**(测试经理 → 项目经理 → admin 多级)— 单审批,留 v0.3
- **提测单关联缺陷自动生成**(退回时 AI 自动生成 defect)— 留 v0.5+
- **跨提测单趋势分析**(单测覆盖率历史曲线)— 留 v0.3 Analytics 模块承接

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **开发 (Dev)** | 创建提测 / 填 4 项门禁 / 改自己的 status='00→01' | 准备提测 → 跑单测 → 填覆盖率 → 提交 |
| **测试经理 (QA Lead)** | 审批 (status='02→03 已通过 / 04 已退回') | AI 门禁 review → 决策 |
| **测试 (QA)** | 查看 + 评论 | 准备接 03 已通过 的提测 |
| **管理员 (admin)** | 全 CRUD + 复盘 | 跨提测单看数据 |

### 2.2 典型场景

**S1 开发提测**(最高频)
> 李工完成 Sprint 4 → 跑 `mvn test` 看覆盖率 65% → 进入提测菜单 → 新建 → 标题 "智慧灌溉 v2.1 Sprint 4 提测" + 范围(变更点 CSV) + 测试环境=test + 单测覆盖率=65% + 代码扫描=Y + PRD 完整=Y + API 文档=Y → 保存 → status='00 草稿' → quality_gate_passed 自动算="Y"(4 项 ∧)→ 改 status='00 → 01 已提交' → submittedAt 自动填

**S2 AI 质量门禁中**(关键流程)
> 测试经理收通知 → 看提测单 → 改 status='01 → 02 质量门禁中' → 系统自动 review 4 项门禁数据 → 若任一为 N,提示 "AI 门禁未通过"

**S3 通过提测**(终态)
> 测试经理评审通过 → status='02 → 03 已通过' → **Service 校验:qualityGatePassed='Y' 才允许进 03,否则抛 708** → approvedAt 自动填 → 测试团队接手开测

**S4 退回提测**(反向边路径)
> 测试经理发现"PRD 还有验收标准没写完" → status='02 → 04 已退回' → **Service 校验:rejectReason 必填,否则抛 602** → 李工收通知 → 改 status='04 → 00 草稿'(反向边)→ 补完 PRD → 再次提测

**S5 4 项门禁详情查看**(决策辅助)
> 测试经理在 modal-submitdetail tab "门禁详情" 看每项 Y/N + 各项依据 → AI 给"为什么 N"建议(本期 mock)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Submission (F4.4)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: submissionId / submissionNo (`SUB-YYYY-NNNN`) / projectId(FK 必)/ sprintId(FK 可空)
- 用户输入: title / scope (变更点) / environment(5 值字典 test/pre/dev/staging/prod,本会话刚补)/ expectedTestDays / riskNotes
- **AI 质量门禁 4 项** (PRD §F4.4 核心): unitTestCoverage(DECIMAL,≥60 才合格)/ codeScanPassed (Y/N) / prdCompleted (Y/N) / apiDocUpdated (Y/N)
- 服务端计算: qualityGatePassed(4 项 ∧,不接受用户输入)
- 流程: status (5 态含反向边 04→00)/ rejectReason(status=04 必填)/ submitterUserId / reviewerUserId / submittedAt / approvedAt

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) submission 行:5 态含反向边。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已提交} | 默认初始,开发填表 |
| 01 | 已提交 | {02 质量门禁中, 04 已退回} | submittedAt 自动填;测试经理判断走门禁还是直接退回 |
| 02 | 质量门禁中 | {03 已通过, 04 已退回} | AI 门禁 review;依赖 qualityGatePassed |
| 03 | 已通过 | {} | 终态;**进入此态强制 qualityGatePassed='Y'(708)**;approvedAt 自动填 |
| 04 | 已退回 | {00 草稿(反向边)} | 终态分支;**进入此态强制 rejectReason 非空(602)**;反向边 04→00 让开发补完再提 |

**特殊规则**:
- **错误码 708 (PRD §F4.4 专属)**:进入 `03 已通过` 但 `qualityGatePassed != 'Y'` → 抛 ServiceException("AI 质量门禁未通过,不能标记 03 已通过",708)
- 进入 `04 已退回` 必填 `rejectReason`(602)
- `00→01` 自动填 `submittedAt = NOW()`
- `02→03` 自动填 `approvedAt = NOW()`
- 任一门禁字段(unitTestCoverage/codeScan/prd/apiDoc)变更 → Service 自动重算 qualityGatePassed
- environment 白名单校验(test/pre/dev/staging/prod,604)— 本会话 ADR 补 5 值

---

## 5. AI 能力

### 5.1 AI 端点(留 v0.3)

`POST /business/submission/ai/gate-check` — 自动 review 4 项门禁数据并给"为什么 N"建议。本期 mock(直接返回 4 项 Y/N + 文字说明)。

### 5.2 当前实现

详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) — Submission 模块本期 quality_gate 是**服务端纯逻辑计算**(4 项 ∧),不调 AI;v0.3 接 SonarQube/PRD 检查/Springdoc API 文档检查时会引入真 AI。

### 5.3 路线图

- v0.3: 对接 SonarQube → codeScanPassed 由 AI 自动判
- v0.3: 对接 PRD 模块 → prdCompleted 由 AI 检查 PRD §1-7 段落完整
- v0.3: 对接 Springdoc → apiDocUpdated 由 AI 比对新增 Controller endpoint 与 PRD/API 设计文档
- v0.5+: 提测失败 AI 一键建议改进

---

## 6. 验收标准

[PRD §F4.4 验收](../prd和原型/AgriAI-PLM-完整PRD文档.md):
- ✅ **AI 质量门禁 4 项硬约束**(本期已实现,以服务端纯逻辑形式)
- ⏳ **AI 智能门禁集成代码扫描 / PRD / API 文档**(留 v0.3)
- ⏳ **提测失败一键建议改进**(留 v0.5+)

**模块特有验收**(本会话已落地):
- 5 态状态机合法/非法转换 + 反向边 04→00 单测覆盖
- **错误码 708 专属**:进入 03 但 qualityGatePassed=N 抛 708
- rejectReason 强制(进 04 必填,602)
- environment 字典 5 值白名单(test/pre/dev/staging/prod,604)
- qualityGatePassed 自动重算(4 项任一变更触发)
- submittedAt / approvedAt 自动填触发单测覆盖

---

## 7. 不做的事 — 详 §1.3

- AI 自动扫描 / 多人审批 / 自动生成缺陷 / 跨提测单趋势

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Submission-数据库设计.md](../02-设计/Submission-数据库设计.md)
- API 设计: [Submission-API设计.md](../02-设计/Submission-API设计.md)
- 测试计划: [Submission-测试计划-2026-05-17.md](../04-测试/Submission-测试计划-2026-05-17.md)
- 发布计划: [Submission-发布计划-2026-05-17.md](../05-上线/Submission-发布计划-2026-05-17.md)
- 原型: [submit.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/submit.html)
- AgriAI PRD: [§F4.4 L355-359](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Sprint-PRD.md](Sprint-PRD.md) + [Defect-PRD.md](Defect-PRD.md)
- 实施 commit: `ae77e61` (environment 字典补 5 值 + Service 白名单)

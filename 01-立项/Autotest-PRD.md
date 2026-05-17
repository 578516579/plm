# PRD: AutoTest 模块 — 自动化测试 (F4.5)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F4.5 + 原型 autotest.html) |
| 作者 | Wjl |
| PRD § | F4.5 (AgriAI-PLM-完整PRD文档.md §F4.5 自动化测试) |
| 原型 HTML | [autotest.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/autotest.html) (modal-newat + atStats + lastRootCause) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "AutoTest (F4.5)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(自动化脚本散在 Git / 失败用例根因人工排查 / 多框架(playwright/selenium/jmeter/cypress)割裂 / 定时执行排期无登记)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F4.5 验收标准 + 模块特有衡量指标(通过率 ≥ 80% / AI 根因分析采纳率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实测试执行** — 仅 mock 累加 totalCases/passedCases/failedCases,实际执行留 v0.5+
- **多版本套件回归对比** — 单条目,对比留 v0.3
- **失败用例自动重试** — 仅记录失败,重试留 v0.5+
- **Selenium Grid / 分布式执行** — 仅单点,分布式留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:测试开发 / QA / DevOps / PM。

### 2.2 典型场景

**S1 AI 辅助生成脚本骨架**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newat 字段 → genAutoScript → scriptContent 输出 4 框架(playwright/selenium/jmeter/cypress)骨架

**S2 立即执行 + 统计**(高价值)
<待人工填写>:run 按钮 → totalCases / passedCases / failedCases / passRate / executionDurationSec 自动写入

**S3 AI 智能根因分析**(F4.5 核心能力)
<待人工填写>:失败用例触发 lastRootCauseAnalysis,AI 分析常见 5 类失败(元素定位/网络超时/断言失败/数据依赖/环境)

**S4 定时执行**(可选)
<待人工填写>:scheduleEnabled=Y + cron 表达式定时跑

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "AutoTest (F4.5)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: autotestId / autotestNo(AT-YYYY-NNNN)
- 用户输入: title / testSuiteType / framework / targetUrl / scriptContent / scheduleEnabled / scheduleCron
- AI 输出: aiGenerated / aiGeneratedAt / lastRootCauseAnalysis
- 派生统计: totalCases / passedCases / failedCases / passRate(服务计算)/ executionDurationSec / lastExecutedAt
- 流程: status(3 态) / authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) autotest 行:`00→01→02` 含反向边 `02→01`。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已激活} | 默认初始状态 |
| 01 | 已激活 | {02 已禁用} | 可被定时调度执行 |
| 02 | 已禁用 | {01 已激活} | 反向边 — 重新激活 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- testSuiteType 4 个字典白名单(ui/api/perf/regression)抛 604
- framework 4 个字典白名单(playwright/selenium/jmeter/cypress)抛 604
- scheduleEnabled=Y 时 scheduleCron 必填(602)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/autotest/ai/generate/{id} — Dify 工作流 auto-test-flow(详 PRD-MAPPING §6);后续 POST /business/autotest/run/{id} 执行 + 写统计 + 根因分析。

### 5.2 当前阶段实现
🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F4.5 行)— 本期占位 mock 脚本骨架 + mock 根因分析。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4 — Dify 实接入留 v0.5+。

---

## 6. 验收标准

**PRD §F4.5 验收**:
- ⏳ AI 脚本骨架生成时间 < 1 分钟
- ⏳ 通过率 ≥ 80%

**模块特有验收**:
<待人工填写>:E2E 测试 / 字典白名单 / 反向边 02→01 单测。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Autotest-数据库设计.md](../02-设计/Autotest-数据库设计.md)
- API 设计: [Autotest-API设计.md](../02-设计/Autotest-API设计.md)
- 测试计划: [Autotest-测试计划-2026-05-17.md](../04-测试/Autotest-测试计划-2026-05-17.md)
- 发布计划: [Autotest-发布计划-2026-05-17.md](../05-上线/Autotest-发布计划-2026-05-17.md)
- 原型: [autotest.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/autotest.html)
- AgriAI PRD: [§F4.5](../prd和原型/AgriAI-PLM-完整PRD文档.md)

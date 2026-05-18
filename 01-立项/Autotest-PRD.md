# PRD: AutoTest 模块 — 自动化测试管理 (F4.5)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F4.5 (AgriAI-PLM-完整PRD文档.md §F4.5 自动化测试管理) |
| 原型 HTML | [autotest.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/autotest.html) (atTotal / atFailed / 89% 通过率 + genAutoScript + 4m32s 耗时) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O4-KR3: AutoTest 模块上线,自动化测试通过率 ≥ 90%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "AutoTest (F4.5)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前自动化测试散落在各项目 git 仓库,4 个具体问题:

1. **自动化套件无集中管理**:Playwright E2E spec 在前端 git / JMeter 脚本在后端 git / Cypress 脚本在 QA 个人电脑,**测试经理无法一眼看"PLM 一共有几个套件 / 各通过率多少"**。
2. **失败根因分析人肉**:套件失败时需要人工看日志 / 截图 / 报错栈,**Q1 平均每次失败定位耗费 30 分钟**,频次高时一天浪费几小时。
3. **定时执行无统一调度**:有的套件靠 GitLab CI 跑 / 有的靠测试人手动跑,**无统一 cron 编排**,经常忘记跑回归。
4. **AI 根因分析能力缺位**:原型有 lastRootCauseAnalysis 字段,**但当前全空**,失败原因仍靠人肉。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 30 个自动化测试套件数据,做"统一管理 + 定时执行 + AI 根因分析"三件套。

**衡量指标**:
- **自动化测试通过率 ≥ 90%**(passedCases / totalCases)
- **AI 根因分析准确率 ≥ 70%**(失败时 AI 给出的 RCA 与人工诊断一致比例)
- **失败定位时间 ≤ 5 分钟**(基线 30 分钟)
- **套件统一注册率 100%**(所有 spec 必入 tb_autotest)
- **执行频次 ≥ 每周 1 次**(scheduleCron 启用率 ≥ 60%)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **AI 自动生成测试脚本**(从需求自动出 Playwright 代码)— 仅 genAutoScript 占位 mock,真实生成留 v0.3
- **跨套件并行编排**(套件 A 通过才跑套件 B)— 留 v0.3
- **测试结果可视化 Dashboard 大屏**(类 Allure Report)— 仅模块内列表 + 详情,Dashboard 留 v0.3 走 Analytics 模块
- **测试报告自动归档** — 留 v0.3 走 TestReport 模块联动
- **测试环境隔离管理**(每个套件独立环境)— 留 v0.5+
- **CI/CD 触发回归自动跑** — 留 v0.3 pipeline 联动

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **测试 (QA)** | CRUD 自己负责的 autotest | 注册脚本 / 触发执行 / 看根因 |
| **测试经理 (QA Lead)** | 全 CRUD | 维护套件 / 统计通过率 |
| **DevOps** | 查看 + 启 cron | 接 CI/CD 调度 |
| **管理员** | 全 CRUD | 跨项目自动化数据看板 |

### 2.2 典型场景

**S1 注册自动化套件**(高频)
> QA 把项目的 Playwright E2E spec 注册到 PLM → 进入 AutoTest 菜单 → 新建 → title "PLM 工作台 E2E 套件" + testSuiteType="ui"(4 值:ui/api/perf/regression)+ framework="playwright"(4 值:playwright/selenium/jmeter/cypress)+ targetUrl="https://test.agriplm.com" + scriptContent(粘 spec 全文)+ scheduleEnabled='Y' + scheduleCron="0 2 * * *"(每天 2:00) → 保存 → status='00 草稿'

**S2 激活套件 → 执行**(关键流程)
> status='00→01 已激活' → 点 "立即执行" → POST /business/autotest/run/{id} → mock 模拟 totalCases=89 / passedCases=86 / failedCases=3 / passRate=96.6% / executionDurationSec=272(4m32s)/ lastExecutedAt=NOW()

**S3 失败 → AI 根因分析**(F4.5 核心)
> 套件失败 → lastRootCauseAnalysis 字段填充 mock RCA Markdown:"3 个失败用例集中在用户登录功能,可能原因:1. 后端登录 API 改了返回字段;2. 选择器变了;3. 验证码服务变了。建议:..."

**S4 套件停用**(终态)
> 套件过时(功能下线)→ status='01→02 已禁用' → cron 不再触发;但保留历史数据

**S5 套件重启动**(反向边,ADR 已实现)
> 停用 30 天后又要重启 → status='02→01 已激活'(**反向边 02→01**)→ 重新加入回归

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "AutoTest (F4.5)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: autotestId / autotestNo / projectId(FK 必)
- 套件定义: title / testSuiteType(4 值字典)/ framework(4 值字典)/ targetUrl / scriptContent
- 调度: scheduleEnabled(Y/N)/ scheduleCron(cron 表达式)
- 执行结果: totalCases / passedCases / failedCases / passRate(服务端算)/ executionDurationSec / lastExecutedAt
- AI: lastRootCauseAnalysis(Markdown)/ aiGenerated / aiGeneratedAt
- 流程: status(3 态含反向边)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) autotest 行:3 态含反向边 02→01。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已激活} | 默认初始 |
| 01 | 已激活 | {02 已禁用} | cron 自动跑;run 端点专属 |
| 02 | 已禁用 | {01 已激活} | 反向边可重启 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- testSuiteType / framework 4 值白名单(604)
- scheduleEnabled='Y' 时 scheduleCron 必填(602)
- run 端点专属:status 必须 '01 已激活'(601)
- passRate 服务端计算,前端写入被忽略
- FK 校验:projectId 必(702)

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/autotest/ai/generate/{id}` — Dify `auto-test-flow` 生成脚本骨架(本期 mock)
`POST /business/autotest/run/{id}` — 立即执行(本期 mock 跑通 + 自动填执行结果 + AI 根因分析)

### 5.2 当前阶段实现

🔴 未实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F4.5 行)— 本期 mock:
- generate 端点:基于 testSuiteType + framework 输出脚本骨架字符串
- run 端点:mock 89/86/3 通过率 + 272 秒耗时 + 失败时 lastRootCauseAnalysis 填充

### 5.3 路线图

- v0.3: 真实 AI 生成 / 真实跑 Playwright/JMeter
- v0.3: pipeline 联动 / Allure Report 集成
- v0.5+: 跨套件编排 / 环境隔离

---

## 6. 验收标准

**PRD §F4.5 验收**:
- ⏳ **AI 智能根因分析**(本期 mock lastRootCauseAnalysis Markdown)
- ⏳ **支持 4 种框架**(playwright/selenium/jmeter/cypress 字典就位)
- ⏳ **定时执行支持**(scheduleCron + scheduleEnabled 就位)

**模块特有验收**(本会话已落地):
- 3 态状态机 + 反向边 02→01 单测覆盖
- testSuiteType / framework 4 值白名单(604)
- scheduleEnabled='Y' 时 scheduleCron 必填(602)
- passRate 服务端计算
- run 端点专属 status=01 前置校验(601)

---

## 7. 不做的事 — 详 §1.3

- AI 真实生成脚本 / 并行编排 / Dashboard / 报告归档 / 环境隔离 / CI 触发

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Autotest-数据库设计.md](../02-设计/Autotest-数据库设计.md)
- API 设计: [Autotest-API设计.md](../02-设计/Autotest-API设计.md)
- 测试计划: [Autotest-测试计划-2026-05-17.md](../04-测试/Autotest-测试计划-2026-05-17.md)
- 发布计划: [Autotest-发布计划-2026-05-17.md](../05-上线/Autotest-发布计划-2026-05-17.md)
- 原型: [autotest.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/autotest.html)
- AgriAI PRD: [§F4.5](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [TestCase-PRD.md](TestCase-PRD.md)(autotest 跑的是 testcase) / [TestReport-PRD.md](Testreport-PRD.md)

# PRD: TestCase 模块 — 测试用例 (F4.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 (实质内容,2026-05-17 由 Claude 基于 PRD-MAPPING §2 + AgriAI PRD §F4.2 + 原型 testcase.html + ADR-B 决策生成) |
| 作者 | Wjl |
| PRD § | F4.2 (AgriAI-PLM-完整PRD文档.md L339-348) |
| 原型 HTML | [testcase.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testcase.html) (modal-testcase-add: L188 含 nca-title/type/pri/pre/steps/expect) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | [ADR-B Option B](../99-跨阶段/proposals/0300-adr-b-testcase-category-dict.md) category 字典 8 值字符串编码 |
| 关联 OKR | _2026 Q2-O3-KR5: PLM 测试用例模块上线,自动化用例占比 ≥ 50%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "TestCase (F4.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的测试用例当前用 Excel + 飞书文档,4 个具体问题:

1. **用例散落,跨项目复用差**:每个测试在自己 Excel 写用例,Excel 在飞书文档分散;**新项目测试经理 80% 时间重复造轮子写已经写过的用例**(尤其登录/CRUD 等通用流)。
2. **农业专项用例缺位**:原型 modal-testcase-add 提供 4 个类型 (功能/边界/异常/农业专项),但旧 SQL 字典只有 7 个通用值(功能/接口/性能/安全/兼容性/E2E/烟雾),**农业业务的灌溉算法/植保识别专项测试无分类承载**。
3. **自动化追溯断链**:Playwright E2E spec 在 `plm-frontend/e2e/<entity>.spec.ts`,但**与 TestCase 表的 automationScriptPath 字段没强约束**,事后回追"这条用例对应哪个 spec"靠人肉。
4. **执行历史无沉淀**:executionCount / lastExecutedAt 没有自动更新,**回归测试时无法判断"上次跑这条用例是什么时候"**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 500 条测试用例的完整生命周期数据,做 ADR-B 8 值字典(含农业专项),`/execute` 端点自动更新执行历史。

**衡量指标**:
- **自动化用例占比 (`isAutomated='Y'`) ≥ 50%**(基线 30%)
- **农业专项用例占比 (`category='agri'`) ≥ 15%**(基线 0,新字典启用后)
- **跨项目用例复用率 ≥ 30%**(同 title 用例在 ≥ 2 项目出现)
- **回归测试时 lastExecutedAt 时效性**:发布前 1 周内执行的用例 ≥ 80%

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **PRD §F4.2 AI 用例生成**(基于需求自动生成用例)— 留 v0.3 AI 增强,本期手动录
- **跨项目用例库共用**(测试用例资产化)— 留 v0.5+,本期单 projectId
- **用例 → 缺陷自动关联**(执行失败自动建 defect)— 留 v0.3
- **数据驱动测试**(同用例多套数据参数化)— 留 v0.5+
- **CI 集成**(用例执行结果自动回写 status)— 留 v0.3 pipeline 模块联动
- **用例评审流程**(用例本身的评审,类似 PRD 评审)— 当前用例直接生效,留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **测试 (QA)** | CRUD 自己负责的用例 | 录入用例 / 执行 / 标 P/F |
| **测试经理 (QA Lead)** | 全 CRUD + 决策 | 审视用例完整性 / 决定 isAutomated |
| **开发 (Dev)** | 查看 + 评论 | 用例作"需求验收标准"参考 |
| **管理员** | 全 CRUD | 跨项目用例复用决策 |

### 2.2 典型场景

**S1 用例录入**(最高频)
> 王 QA 接到 REQ-2026-0089 → 进入测试用例菜单 → 新建 → 标题 "土壤含水率 API 返回正确范围" + 关联需求=REQ-89 + 类型="agri 农业专项" (ADR-B 新字典) + 优先级=P0 + 前置="部署测试环境" + 步骤(3 行) + 期望结果 + 标签="regression" + 不自动化(`isAutomated='N'`) → 保存 → status='00 草稿' → testcaseNo 自动 `TC-2026-NNNN`

**S2 用例转自动化**(关键流程)
> 后端开发完成,QA 把手动用例转 Playwright → 改 isAutomated='Y' + automationScriptPath="plm-frontend/e2e/soil-moisture.spec.ts" → **Service 校验:isAutomated='Y' 必填 automationScriptPath,违反抛 706(本会话已实现)**

**S3 用例执行 — /execute 专属端点**(核心特性)
> QA 跑回归 → 把用例推 status='01 待执行' → 开始执行时推 '02 执行中' → **POST /business/testcase/{id}/execute { status: '03'|'04', actualResult: "..." }** → Service 自动:
> 1. 校验当前 status='02'(否则抛 601)
> 2. 校验 newStatus 仅 '03' 或 '04'(否则抛 604)
> 3. `executionCount++`
> 4. `lastExecutedAt = NOW()`
> 5. update actualResult

**S4 反向边重测**(质量循环)
> 用例失败 `04` 或通过 `03` 后,需求改了/代码改了要重跑 → status='03/04 → 01 待执行'(**反向边,ADR-B 已实现**)→ 走 S3 流程重新执行

**S5 农业专项用例**(ADR-B 核心收益)
> 灌溉算法测试 → 类型=agri 农业专项(旧字典无此选项,ADR-B 新增)→ 农业领域专项用例数据沉淀,后续 AI 推荐时可按业务线精准匹配

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "TestCase (F4.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: testcaseId / testcaseNo (`TC-YYYY-NNNN`, ADR-0006) / projectId(FK 必) / requirementId(FK 可空)
- 用户输入: title / description / preconditions / steps / expectedResult / actualResult
- 分类: **category** (ADR-B 8 值字符串编码) / priority (P0/P1/P2) / status (5 态含反向边) / tags(CSV,承载 E2E/smoke/regression 等运行特性)
- 自动化: isAutomated (Y/N) / automationScriptPath
- 执行历史: executionCount / lastExecutedAt(由 /execute 端点自动更新)

---

## 4. 状态机

### 4.1 ADR-B 决策回顾

本会话 ADR-B Option B 决议:`category` 字典从旧 7 值数字编码 (`01`~`07`) 升级为 **8 值字符串编码**:
| dict_value | dict_label |
|---|---|
| functional | 功能(默认) |
| boundary | 边界(原型独有) |
| exception | 异常(原型独有) |
| **agri** | **农业专项**(原型 + PRD §F4.2 核心新增) |
| api | 接口 |
| performance | 性能 |
| security | 安全 |
| compatibility | 兼容性 |

舍弃旧 '06' E2E / '07' 烟雾(它们是**测试层级**,转入 `tags` 字段承载,D2 决策)。

### 4.2 状态机定义

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) testcase 行:5 态含反向边 03/04→01 (重测)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 待执行} | 默认初始 |
| 01 | 待执行 | {00 草稿(回退), 02 执行中} | 准备执行 |
| 02 | 执行中 | {01 待执行(撤回), 03 已通过, 04 已失败} | 仅此态可调 /execute 端点 |
| 03 | 已通过 | {01 待执行(反向边·重测)} | 终态可重测 |
| 04 | 已失败 | {01 待执行(反向边·重测)} | 终态可重测 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- **`isAutomated='Y'` 时 `automationScriptPath` 条件必填**,违反抛 706(本会话实现)
- **`/execute` 端点专属规则**:必须 `status='02'`(否则 601),newStatus 仅 03|04(否则 604),自动 `executionCount++` + `lastExecutedAt=NOW()`
- category / priority / status 字段白名单校验(604)— 旧 `01`~`07` 数字编码已废弃,会抛 604(break-change,本会话验证)

---

## 5. AI 能力

### 5.1 当前状态

详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) — TestCase 模块本期标 🟡 字段已留位 (aiGenerated)。

### 5.2 留 v0.3 增强方向 (`testcase-gen-flow`)

- AI 基于需求自动生成测试用例(POST `/business/testcase/ai/generate`,接收 requirementId,Dify 工作流输出 N 条用例草稿)
- AI 推荐自动化候选(从手动用例池挑高频/稳定的转 isAutomated='Y')
- AI 推荐 category(基于 title + description 自动判 functional/agri/boundary 等)

---

## 6. 验收标准

[PRD §F4.2 验收 L339-348](../prd和原型/AgriAI-PLM-完整PRD文档.md):
- ⏳ **AI 用例生成基于需求**(留 v0.3)
- ⏳ **自动化用例覆盖率**(本期目标 ≥ 50%)
- ✅ **农业业务专项用例分类**(本会话 ADR-B Option B 已新增 'agri' 字典值)

**模块特有验收**(本会话已落地):
- 5 态状态机 + 反向边 03/04→01 重测 单测覆盖
- **/execute 端点专属逻辑**:status=02 前置 + newStatus 范围校验 + executionCount/lastExecutedAt 自动填
- ADR-B 8 值字典全合法 + 旧数字编码 '01'~'07' 已废弃抛 604 (mvn -pl plm-testcase test 6/6 绿)
- isAutomated='Y' 必填 automationScriptPath(706)
- FK 校验:projectId 必,requirementId 可空但若填必须存在(702)

---

## 7. 不做的事 — 详 §1.3

- AI 用例生成 / 跨项目复用 / 自动建缺陷 / 数据驱动 / CI 集成 / 用例评审

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [TestCase-数据库设计.md](../02-设计/TestCase-数据库设计.md)
- API 设计: [TestCase-API设计.md](../02-设计/TestCase-API设计.md)
- 测试计划: [TestCase-测试计划-2026-05-17.md](../04-测试/TestCase-测试计划-2026-05-17.md)
- 发布计划: [TestCase-发布计划-2026-05-17.md](../05-上线/TestCase-发布计划-2026-05-17.md)
- 原型: [testcase.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testcase.html)
- AgriAI PRD: [§F4.2 L339-348](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- ADR: [proposal 0300 ADR-B Option B](../99-跨阶段/proposals/0300-adr-b-testcase-category-dict.md)
- 关联模块: [Requirement-PRD.md](Requirement-PRD.md) (TestCase.requirementId FK)
- 实施 commit: `9baac4c` (字段表+ADR-B) → `534c67e` (代码 8 值字符串字典+白名单,6/6 测试) → `7c83e11` (状态升级)

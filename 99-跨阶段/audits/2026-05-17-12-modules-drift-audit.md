# 12 个"早期对齐"模块 PRD-align Drift 审计 (2026-05-17)

> **产生背景**: project (F1.2) 模块 PRD-align 重构发现 5 处字段 drift (commit `3c10238`),
> 怀疑其他 12 个"早期对齐"模块存在类似问题。本审计验证此猜想。
>
> **方法**: 对每个模块对照 4 处来源 — 原型 HTML 表单/列表/JS state、PRD §F 章节、
> 当前 `Domain.java`、当前 `business-<entity>.sql` DDL。
>
> **审计员**: 派遣的 audit agent (general-purpose subagent, 只读模式)
>
> **限制声明**: 本审计由 Claude Code 自动派遣的 audit agent 生成,
> **事实在修复前需人工核对**。审计的 Severity 评级与修复优先级建议仅供参考,
> 实际修复仍需按规则 §M.2 "先字段表后代码" 流程,每个模块走完 §8 9 项 DoD。

---

## 报告

### Requirement (F2.1)
- **来源**: 原型 = `requirements.html:152` (modal-newreq) + `:264-265` (reqdetail 含状态/AI价值); PRD = §F2.1 L243-247; Domain = `plm-requirement/.../domain/Requirement.java`; SQL = `sql/business-requirement.sql`
- **缺字段** (原型/PRD 有,Domain 无): `aiValue` (AI价值评估 `rdm-edit-ai` 高/中/低)
- **多字段**: 无 (`reviewNote` 与状态机配套,可保留)
- **名字/类型偏差**: 无
- **状态机偏差**: 🔴 PRD §F2.1 是 **6 态** (草稿→评审→确认→开发中→完成→验收);Domain/SQL 字典只有 **4 态** (`00`待评审/`01`开发中/`02`已完成/`03`已取消);原型 `rdm-edit-status` 选项 4 态 与字典一致。**需用户/ADR 决策**: 走原型 4 态(实用版) vs PRD 6 态(完整版)
- **Severity**: 🔴 严重 (缺 aiValue + 状态机机制级偏差)

### Sprint (F3.4)
- **来源**: 原型 = `kanban.html` modal-sprint; PRD = §F3.4 L304-308; Domain = `plm-sprint/.../Sprint.java`; SQL = `sql/business-sprint.sql`
- **缺字段**: 无
- **多字段**: 无
- **名字/类型偏差**: 无
- **状态机偏差**: 无 (4 态与 PRD-MAPPING §3 一致)
- **Severity**: 🟢 轻微 (字段层面已 align)

### Task (F3.4)
- **来源**: 原型 = `kanban.html:149` (modal-newtask 5 字段) + `:175-216` (modal-taskdetail); PRD = §F3.4; Domain = `plm-task/.../Task.java`; SQL = `sql/business-task.sql`
- **缺字段**: 无
- **多字段**: 无
- **名字/类型偏差**: 无 (mrUrl/mrBranch 在 modal-taskdetail "关联 MR" 区域有对应)
- **状态机偏差**: 无 (6 态含反向边 02↔01, 03↔02 与 PRD-MAPPING §3 一致)
- **Severity**: 🟢 轻微 (需用户确认 `taskColumn` 看板列是否仅展示视图,不入库)

### TestCase (F4.2)
- **来源**: 原型 = `testcase.html:151-154` (4 类型 checkbox: 功能/边界/异常/农业) + `:188` modal; PRD = §F4.2 L339-348; Domain = `plm-testcase/.../TestCase.java`; SQL = `sql/business-testcase.sql`
- **缺字段**: 无
- **多字段**: 无
- **名字/类型偏差**: 🟡 `category` 字典值偏差 — SQL `biz_testcase_category` 是 7 值 (功能/接口/性能/安全/兼容性/E2E/烟雾);原型是 4 值 (功能/边界/异常/农业专项);**PRD §F4.2 "农业业务场景专项用例" 缺失**
- **状态机偏差**: 无
- **Severity**: 🟡 中等 (category 字典需补"边界/异常/农业专项")

### Defect (F4.6)
- **来源**: 原型 = `defects.html:167` modal-newdefect; PRD = §F4.6 L367-371 (5 态生命周期); Domain = `plm-defect/.../Defect.java`; SQL = `sql/business-defect.sql`
- **缺字段**: 无 (Domain 含 severity/category/reproduceSteps/expected/actual/resolution)
- **多字段**: `expectedResult`/`actualResult` 通用模板字段,可保留
- **名字/类型偏差**: 🟢 `module` 字段 (原型 `dem-module` 所属模块) Domain 缺;可用 `tags` 代偿
- **状态机偏差**: 🟡 **三方不一致**:PRD §F4.6 是 5 态 "发现→确认→修复→验证→关闭";原型 `dem-status` 4 态 (待确认/修复中/待验证/已关闭);Domain 字典 5 态 (新建/已确认/处理中/已解决/已关闭);PRD-MAPPING §3 标 "6 态含重开",但 SQL 字典无"重开"
- **Severity**: 🟡 中等 (状态机三方不一致;module 字段可选)

### Document (F5.5)
- **来源**: 原型 = **无独立 HTML** (合并 5 stub:prd/arch/dbdesign/apidesign/proposal);参考 `projects.html` modal-prdlist + `apidoc.html`;PRD = §F5.5 L414-417 (知识库管理:归档/检索/复用);ADR-0007;Domain = `plm-document/.../Document.java`;SQL = `sql/business-document.sql`
- **缺字段**: 🟡 PRD §F5.5 "语义搜索"/"跨项目知识复用推荐" 无字段承载 (无 `embeddingVector`/`semanticTags`/`reusedCount`);属架构级缺位
- **多字段**: 无 (relatedEntityType/relatedEntityId 是 polymorphic FK 合并设计)
- **名字/类型偏差**: 🟢 **PRD §F5.5 章节名是 "知识库管理",但 Document 实现的是"文档中心" — 概念偏差**。PRD F5.5 实际指 AI 知识库 (语义检索),Document 模块更接近统一存储层。需用户确认: Document 是否应重命名 `business_doc` 而 F5.5 另立 `knowledge-base` 模块
- **状态机偏差**: 🟢 4 态 (草稿/待评审/已发布/已归档) 与 PRD-MAPPING §3 一致
- **Severity**: 🟡 中等 (语义检索字段架构性缺失;命名 vs PRD §F5.5 概念偏差)

### TestPlan (F4.1)
- **来源**: 原型 = `testplan.html:141-152`;PRD = §F4.1 L334-337;Domain = `plm-testplan/.../TestPlan.java`;SQL = `sql/business-testplan.sql`
- **缺字段**: 无
- **多字段**: 无显著 (toolsRecommended/resourcesPlan/riskAssessment/strategy 与 PRD 描述对应)
- **名字/类型偏差**: 🟢 `testTypes` CSV (`functional,api,performance,automation,security`) 与原型 5 checkbox 一致
- **状态机偏差**: 无 (4 态一致)
- **Severity**: 🟢 轻微 (已对齐)

### Submission (F4.4)
- **来源**: 原型 = `submit.html:141-144` (4 项门禁) + modal-newsubmit;PRD = §F4.4 L355-359;Domain = `plm-submission/.../Submission.java`;SQL = `sql/business-submission.sql`
- **缺字段**: 无 (Domain 已对应 4 个门禁 + reject_reason)
- **多字段**: 无显著
- **名字/类型偏差**: 🟡 environment 字典偏差 — 原型 modal-newsubmit 是 `测试环境 TEST / 预发环境 PRE`,SQL 默认 `dev`/注释 `dev/staging/prod`
- **状态机偏差**: 无 (5 态含反向边 04→00,一致)
- **Severity**: 🟡 中等 (Submission 是 PRD-MAPPING §2 字段对照范本,environment 仍 drift)

### TestReport (F4.7)
- **来源**: 原型 = `testreport.html:136` (genTestReport AI 按钮,无 modal);PRD = §F4.7 L373-378;Domain = `plm-testreport/.../TestReport.java`;SQL = `sql/business-testreport.sql`
- **缺字段**: 无
- **多字段**: p0Defects/p1Defects/p2Defects 冗余于 defectSummary JSON,便于查询可保留
- **名字/类型偏差**: 无
- **状态机偏差**: 🟢 PRD-MAPPING §3 标 "含 01→00 打回",**SQL 字典无反向边定义,需 service 端审计**
- **Severity**: 🟢 轻微

### ApiDoc (F5.4)
- **来源**: 原型 = `apidoc.html:137-138` (代码同步,无独立 modal);PRD = §F5.4 L409-412;Domain = `plm-apidoc/.../ApiDoc.java`;SQL = `sql/business-apidoc.sql`
- **缺字段**: 🟢 PRD §F5.4 "API 变更记录与通知" 无字段承载,本期变更订阅未上线
- **多字段**: 无
- **名字/类型偏差**: 无 (httpMethod/path/version 唯一键与 ADR 一致)
- **状态机偏差**: 无 (3 态,一致)
- **Severity**: 🟢 轻微

### ManualProduct (F5.1)
- **来源**: 原型 = `productmanual.html:141` (产品版本) + `:144-148` (5 项 checkbox);PRD = §F5.1 L392-396;Domain = `plm-manual-product/.../ManualProduct.java`;SQL = `sql/business-manual-product.sql`
- **缺字段**: 🟢 PRD §F5.1 多格式输出 (Word/PDF/H5),Domain `outputFormats` 默认 'pdf' CSV — 已支持
- **多字段**: `screenshotsCount` 可由 `screenshotsUrls` CSV 推导,冗余但便于查询
- **名字/类型偏差**: 无
- **状态机偏差**: 🟡 PRD-MAPPING §3 标 "00→01→02→{00,03}",字典/service **缺反向边 02→00**
- **Severity**: 🟢 轻微 (反向边服务层校验)

### Release (DevOps 扩展)
- **来源**: 原型 = `release.html:142` modal-newrelease + `:146-150` (DORA 4 KPI);PRD = 无 (DevOps 扩展);Domain = `plm-release/.../Release.java`;SQL = `sql/business-release.sql`
- **缺字段**: 无
- **多字段**: 无
- **名字/类型偏差**: 🟡 strategy 字典偏差 — 原型 4 选项 (蓝绿/金丝雀10%/滚动/**直接替换**);Domain/SQL 字典只 3 值 (blue_green/canary/rolling),缺 `direct_replace`
- **状态机偏差**: 无 (5 态一致)
- **Severity**: 🟡 中等 (strategy 字典缺 direct_replace)

---

## 优先级建议

| # | 模块 | Severity | 主要 drift | 建议 |
|---|---|:--:|---|---|
| 1 | **Requirement (F2.1)** | 🔴 严重 | 缺 `aiValue` + 状态机 4 vs 6 态分歧 | **下一轮先攻;需 ADR 决策状态机口径** |
| 2 | **TestCase (F4.2)** | 🟡 中等 | category 字典缺农业专项类型 | 字典调整 + Service 白名单 |
| 3 | **Defect (F4.6)** | 🟡 中等 | 状态机三方不一致;`module` 字段缺 | 状态机统一 + ADR |
| 4 | **Document (F5.5)** | 🟡 中等 | 语义检索字段缺;命名与 PRD §F5.5 概念偏差 | 架构决策 (拆分 vs 演化) |
| 5 | **Submission (F4.4)** | 🟡 中等 | environment 字典口径不统一 | 字典统一 |
| 6 | **Release (DevOps)** | 🟡 中等 | strategy 字典缺 `direct_replace` | 字典补值 |
| 7 | TestReport (F4.7) | 🟢 轻微 | 反向边需 service 层校验 | 验证现有 service |
| 8 | ManualProduct (F5.1) | 🟢 轻微 | 反向边 02→00 字典/service 校验 | 验证 |
| 9 | Task (F3.4) | 🟢 轻微 | `taskColumn` 看板列是否入库 | 设计澄清 |
| 10 | TestPlan (F4.1) | 🟢 轻微 | 已对齐 | 仅补 §2 字段表 |
| 11 | ApiDoc (F5.4) | 🟢 轻微 | 变更订阅未实现,本期可不补 | 仅补 §2 字段表 |
| 12 | Sprint (F3.4) | 🟢 轻微 | 字段对齐 | 仅补 §2 字段表 |

**🔴 紧急** (必修): 1 个 — Requirement
**🟡 中等** (应修): 5 个 — TestCase / Defect / Document / Submission / Release
**🟢 轻微** (可推迟): 6 个 — Sprint / Task / TestPlan / TestReport / ApiDoc / ManualProduct

---

## 关键架构性问题(需用户/团队决策)

3 个问题超出"补字段补字典"的范围,需要单独 ADR:

### 1. Requirement 状态机:走 PRD 6 态还是原型 4 态?
- PRD §F2.1: 草稿 → 评审 → 确认 → 开发中 → 完成 → 验收 (6 态)
- 原型 + 当前实现: 待评审 / 开发中 / 已完成 / 已取消 (4 态)
- 4 态合并了 PRD 的 草稿+评审+确认 → 待评审;丢了"验收"
- **决策点**: 评审/确认/验收 这 3 个 PRD 状态在原型里有没有 UI? 没有的话,代码该跟随谁?

### 2. Defect 状态机:三方不一致
- PRD: 5 态 (发现/确认/修复/验证/关闭)
- 原型 modal: 4 态 (待确认/修复中/待验证/已关闭) — 没有"发现"
- Domain SQL 字典: 5 态 (新建/已确认/处理中/已解决/已关闭)
- PRD-MAPPING §3 大表: "6 态含重开"
- 4 个版本都不同。这本身是 SSoT 失效的信号。

### 3. Document 模块概念错位
- PRD §F5.5 章节叫"知识库管理",指 AI 语义检索 + 知识复用
- 当前 Document 模块是"文档中心"(合并 prd/arch/dbdesign/apidesign/proposal 5 stub 的统一存储层)
- 这是两个不同的东西。当前 `plm-document` 占用了 F5.5 的位置但做的是另一件事
- **决策点**: 是把 Document 改名 `business_doc` 让出 F5.5 位置另立 `knowledge-base` 模块,还是接受 Document 演化承担 F5.5 职责?

---

## 后续操作约定

每个模块的修复必须按规则 §M.2 三段式 commit 流程:
1. **文档 commit**: PRD-MAPPING.md §2 加该模块字段表 + 决策记录
2. **代码 commit**: SQL + Domain + Mapper + Service + Test + 前端 + E2E (引用文档 commit hash)
3. **状态升级 commit**: §1 大表对应行 🟢 已对齐 → 🟢 **PRD-aligned**

参考 project 模块的范本: `20b5bb6` → `3c10238` → `522b6df`。

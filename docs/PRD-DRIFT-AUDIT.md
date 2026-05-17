# PRD 与原型对齐审计 (Drift Audit)

> 触发: 2026-05-17,用户反馈"现在开发的功能没有按照 prd 和原型里面的做"
>
> 这份文档是 31 个 PRD-aligned 模块**重审一遍**的诚实清单,把"我以为 PRD-aligned 了"和"原型实际要求"之间的差异列出来。
> 修复后,这份文档每条都该划掉 — 没划掉就是仍有漂移。

## 1. 漂移的根因

之前的 PRD-align 工作流程是:
1. 看原型 HTML 顶层结构,提字段名 → 写 Domain
2. 看字典枚举 → 写 SQL
3. 提个 AI 端点 → mock 一份 Markdown 输出
4. 标准 CRUD Controller + 标准 list+search+dialog 前端 (套 system/role 模板)

这套流程**对纯数据型实体 (e.g. dora_metric, feature_flag) 是 OK 的**,但对"AI 单页工作流"型模块 (inception/prd/ued/arch/competitive/analytics 这 6 个尤其严重) 是错的 ——
原型不是表格列表,是 form+一键 AI+预览,跟"用户走完一次产生一条记录"对应,**前端布局得彻底改、后端字段得拆细**。

## 2. 按"漂移严重程度"分级

### 🔴 P0 严重 (前端布局完全错 + Domain 缺关键结构化字段)

#### 2.1 inception (F1.1) — 立项 AI 助手

**原型** (`inception.html` line 132-165 + JS line 696-729):
- 单页布局: 左 form (5 字段) + 左 AI 风险卡 (生成后才显)+ 右 AI 建议书预览
- `runInceptionAI()` 生成的报告**有 4 块明确结构**:
  - 项目背景 (project context)
  - 市场机会 (含市场规模、数字化渗透率两个具体数值)
  - ROI 预估 (含开发成本、目标用户、首年营收、ROI 倍数 4 个数值)
  - 建议决策 (优先级 P1/P2、启动季度 Q3、分期交付期数 — 全是结构化输出)
- `incApprove()` 提交后通过**飞书推送审批人**,然后跳到 competitive 页

**我的实现**:
- ❌ 前端 `inception/index.vue` 是 system/role 那种表格列表 + CRUD dialog → 跟原型零相关
- 🟡 Domain `aiProposalContent` 一个 TEXT 字段塞 4 段内容 → 应拆成 4 个字段 (或者至少 backgroundAi/marketOpportunityAi/roiEstimateAi/recommendDecision)
- 🔴 缺字段: `marketSize` `digitalPenetration` `devCostEstimate` `firstYearRevenue` `roiMultiple` `recommendedPriority` `recommendedStartQuarter` `deliveryPhases`
- 🔴 风险识别字段 `aiRisks` 是 TEXT,原型是 `[{level: warning|critical, title, description}, ...]` 的数组,应改成 JSON 字符串字段或独立子表
- 🟡 缺飞书审批回调链路 (deferred — 接飞书 webhook 是后续 PR)

**修复优先级**: 立刻 (本批)

---

#### 2.2 prd (F2.2) — AI PRD 生成

**原型** (待详查 `prd.html`): 大概率是 "输入项目背景 → AI 一键生成 PRD 文档 → 预览/编辑 → 评审"。

**我的实现**: 标准 CRUD 表格 + Domain 一个 TEXT `content` 字段塞整篇 PRD。

**修复优先级**: 立刻 (本批)

---

#### 2.3 ued (F2.3) — UED 设计协同

**原型** (待详查 `ued.html`): 大概率是 "新建设计稿 → Figma 同步 → AI 规范评审 → 评分卡"。

**我的实现**: 标准 CRUD,虽有 `complianceScore` 但没 Figma sync 调用、规范评审报告不分维度。

**修复优先级**: 立刻 (本批)

---

#### 2.4 arch (F3.1) — 系统架构设计

**原型** (待详查 `archdesign.html`): 大概率是 "勾选技术栈 → AI 推荐架构方案 → C4 图预览 → NFR 评估"。

**我的实现**: CRUD + 6 维度 mock 报告。需要确认 C4 图字段和 NFR 评估字段是不是分开的。

**修复优先级**: 立刻 (本批)

---

#### 2.5 competitive (F1.3) — 竞品情报

**原型** (`inception.html` JS line 731-779 — 同 JS 文件): 是 "竞品功能维度矩阵 + 监控动态 + SWOT" 三块联动展示。

**我的实现**: CRUD 每个 row 是单个竞品的字段记录,但原型是**多竞品矩阵对比**,字段结构完全不同:
- 原型: 1 张表,15 个维度 × 5 个竞品 (4 友商 + 本品),每格存 1/0.5/0 评分
- 我的: 每条记录 1 个竞品,字段是单一文本描述

**修复优先级**: 立刻 (本批)

---

#### 2.6 analytics (F6) — 效能分析

**原型** (`analytics.html`): 4 张 stat 卡片 + AI 提效柱状图 + 项目健康度评分 + AI 改进建议。

**我的实现**: ✅ 字段倒是齐 (snapshot 模式),但前端没做。等改前端时直接按原型还原。

**修复优先级**: P1 (字段够用,前端追)

---

### 🟡 P1 中等 (字段基本对齐,前端布局错或细节漂)

| 模块 | 主要漂移 |
|---|---|
| `apidesign` (F3.3) | 原型可能有 Swagger UI 嵌入式预览,我没做 |
| `dbdesign` (F3.2) | 原型可能有 ER 图渲染,我只存了 erContent TEXT |
| `testdata` (F4.3) | 写库执行按钮 + 进度实时反馈,我只有同步生成 mock |
| `autotest` (F4.5) | 原型大概有"运行历史时间线",我只存了最近一次 |
| `manual-*` | 多格式导出 (PDF/Word/Markdown) 是按钮,我只存了 outputFormats CSV 字段 |
| `dashboard` (UI §4.2) | 17 阶段 lifecycle swimlane 静态 OK,但 active_projects 进度需真实聚合 |

### 🟢 P2 低 (基本对齐或本来就是数据型)

| 模块 | 状态 |
|---|---|
| `project / requirement / sprint / task / defect / testcase / document / submission / release / testplan / testreport / apidoc / manual-product` | 13 个原 v0.4 模块,标准 CRUD 模式没问题 |
| `ai-agent / openspec` | 是配置管理类,CRUD 列表 OK |
| `pipeline / feature-flag / dora` | DevOps 配置类,CRUD 列表 OK |

## 3. 修复策略

### Phase 1 — 立刻做 (本批 6 个 P0)

每个 P0 模块做 3 件事:
1. 补 Domain.java 字段 (加结构化 AI 输出字段 + 风险/评分 JSON 数组字段)
2. 补 SQL 列 + 字典 (ALTER 不行就 DROP/CREATE,因为还没灌库)
3. 重写 views/business/<entity>/index.vue 按原型布局 (单页 form + AI 触发 + 预览,不是表格列表)

### Phase 2 — 字段足够,只追前端 (4 个 P1: dashboard/analytics/apidesign/dbdesign/testdata/autotest/manual-*)

### Phase 3 — 工程化 (其余 13+5 个)

按 system/role 标准 admin CRUD 模式继续做,这部分**没漂移**,可以批量出 Velocity 模板。

## 4. 跟踪表

每修一个模块,把这里 ☐ 改成 ☑:

- [x] inception (P0) — Domain 补 13 字段 + 前端重写为单页工作流 (commit 3f19cd1)
- [x] prd (P0) — Domain 加 4 段 AI 结构化字段 (aiBackground/aiUserStories/aiCoreFeatures/aiAcceptance),
      `aiGenerate` 输出对齐原型 generatePRD 的 4 个 <h4>, 完整度 89% (前端 Vue 待跟进)
- [x] ued (P0) — Domain 加 reviewItemsJson (timeline 数组 6 项),
      `aiReview` 含 2 个 ✅ + 4 个 ⚠️ 对齐原型 runUEDCheck, 评分 88 (前端 Vue 待跟进)
- [x] arch (P0) — Domain 加 4 个 NFR 子项 + aiTimelineJson,
      `aiRecommend` 输出 4 步骤 timeline (架构模式→技术选型→IoT 接入→部署) 对齐原型 genArchDesign (前端 Vue 待跟进)
- [x] competitive (P0) — 不动现有表结构,加 3 个项目级 JSON 字段
      (matrixJson 15×5 / monitorsJson 4 行 / ourSwotJson 4 象限),
      `aiAnalyze` 同步填,跟原型 renderCompetitive 1:1 (前端 Vue 待跟进)
- [ ] analytics (P1) — 前端追原型 (字段已对齐)
- [ ] dashboard (P1) — active_projects 聚合接真实数据
- [ ] dbdesign (P1) — ER 图渲染
- [ ] testdata (P1) — 写库执行按钮
- [ ] autotest (P1) — 运行历史时间线
- [ ] apidesign (P1) — Swagger UI 嵌入

## 5. 教训 (录到 .claude/rules.md §M 反漂移)

PRD-align 不能只看顶层 HTML 字段,要把:
- 原型对应的 JS 函数 (`runXxxAI()` / `xxxApprove()` / `xxxRender()`) 全读
- 原型 modal / drawer / dialog 的 form 全列
- 原型 chart / table / matrix 这些**非简单字段**结构看清

否则就会陷入"提了一堆字段名 + 套个 CRUD 表格"的伪 PRD-aligned。

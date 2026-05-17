# PRD: Competitive 模块 — 竞品情报 (F1.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F1.3 + 原型 competitive.html) |
| 作者 | Wjl |
| PRD § | F1.3 (AgriAI-PLM-完整PRD文档.md §F1.3 竞品情报) |
| 原型 HTML | [competitive.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/competitive.html) (modal-newcomp + compMatrix + SWOT) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Competitive (F1.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点,需结合 AgriAI PRD §F1.3 竞品情报场景(禅道/LigaAI/Jira 等竞品对比缺失统一收口、SWOT 分析散在 PM 个人笔记、订阅竞品动态人工跟踪)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F1.3 验收标准 + 模块特有衡量指标(竞品库容量 / 监控订阅覆盖 / AI 分析报告生成时间)。

### 1.3 不做的事 (Out of Scope)
本期**不做** (从 AgriAI PRD §F1.3 的高级能力 + 项目路线图剥离清单推断):
- **真实竞品爬虫** — 仅 mock SWOT + 报告模板,Web 抓取留 v0.5+
- **竞品产品定价历史趋势图** — 单点 pricingModel,时序留 v0.3
- **多语言竞品** — 仅中英文,小语种留 v0.5+
- **AI 主动推送竞品动态** — 仅订阅开关 monitorEnabled,推送通道留 v0.3
- **跨项目竞品库共用** — 单 projectId,共享库留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:从原型 competitive.html 中找出涉及角色(PM/产品负责人/竞品分析师/admin)+ 各自典型动作。

### 2.2 典型场景

**S1 新竞品录入 + AI 分析**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newcomp 字段 ID(comp-name / comp-vendor / comp-price / comp-feature 12 维度 JSON / SWOT 四象限)。

**S2 SWOT 对比决策**(高价值)
<待人工填写>:多竞品矩阵对比 + AI 综合报告

**S3 竞品动态订阅**(中频)
<待人工填写>:monitorEnabled=Y + monitorKeywords + lastMonitoredAt 触发周期扫描

**S4 竞品归档**(低频)
<待人工填写>:版本退场后 status='02' 归档,保留历史可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Competitive (F1.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: competitiveId / competitiveNo / projectId(FK)
- 用户输入: competitorName / vendor / website / pricingModel / pricingTier / featureMatrix(12 维 JSON)
- SWOT: strengths / weaknesses / opportunities / threats
- AI 输出: aiAnalysisReport / aiGenerated / aiGeneratedAt
- 监控: monitorEnabled / monitorKeywords / lastMonitoredAt
- 流程: status / authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) competitive 行。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始状态 |
| 01 | 已发布 | {02 已归档} | 完整 SWOT + AI 报告后发布 |
| 02 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- featureMatrix 必须合法 JSON,否则抛 604
- AI 分析触发后 aiGenerated='Y' + aiGeneratedAt 自动写入

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/competitive/ai/analyze/{id} — Dify 工作流 competitive-analysis-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F1.3 行)— 本期占位 mock(返回标准 SWOT + 报告模板)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4 — mock 按 pricingTier + featureMatrix 模板生成 SWOT 四段 + 综合分析报告。

---

## 6. 验收标准

**PRD §F1.3 验收**:
- ⏳ 竞品库 ≥ 10 个主流产品(禅道/LigaAI/Jira/Coding/Lean/Asana 等)
- ⏳ SWOT 分析 4 象限完整,AI 分析报告生成时间 < 30s

**模块特有验收**:
<待人工填写>:E2E 测试 / 单测 / featureMatrix JSON 校验 / FK 校验

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Competitive-数据库设计.md](../02-设计/Competitive-数据库设计.md)
- API 设计: [Competitive-API设计.md](../02-设计/Competitive-API设计.md)
- 测试计划: [Competitive-测试计划-2026-05-17.md](../04-测试/Competitive-测试计划-2026-05-17.md)
- 发布计划: [Competitive-发布计划-2026-05-17.md](../05-上线/Competitive-发布计划-2026-05-17.md)
- 原型: [competitive.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/competitive.html)
- AgriAI PRD: [§F1.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)

# PRD: Analytics 模块 — 效能分析快照 (F6)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F6 + 原型 analytics.html) |
| 作者 | Wjl |
| PRD § | F6 (AgriAI-PLM-完整PRD文档.md §F6 效能分析与复盘) |
| 原型 HTML | [analytics.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/analytics.html) (4 卡片 + AI 改进建议) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "AnalyticsSnapshot (F6)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(效能指标散在各模块 / DORA 4 指标人工算 / 季度复盘人工撰写耗时 / 改进建议凭经验)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F6 验收标准 + 模块特有衡量指标(快照生成时间 / AI 建议采纳率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实跨模块数据聚合** — 当前 mock 数据,真实聚合留 v0.5+(查 sprint / defect / autotest 等)
- **历史趋势可视化** — 仅快照,趋势留 v0.3
- **告警阈值触发** — 仅展示,告警留 v0.5+
- **多组织对比** — 仅本组织,多组织对比留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:管理层 / PM / Scrum Master / 评审 admin。

### 2.2 典型场景

**S1 季度效能快照**(最高频)
<待人工填写>:1 段叙述,引原型 analytics.html 4 卡片(需求吞吐 / 迭代准时率 / 缺陷密度 / 自动化覆盖)+ DORA 4 卡片 + AI 节省工时 + AI 改进建议

**S2 全局 vs 项目快照**(关键场景)
<待人工填写>:projectId 可空 — NULL = 全局快照,否则项目级

**S3 农情专项建议**(农业特色)
<待人工填写>:AI 按 sprintOnTimeRate / defectDensity / autoTestCoverage / changeFailureRate 阈值生成 4 维度建议 + 农情 IoT 专项

**S4 快照归档**(终态)
<待人工填写>:02 已归档,保留历史快照

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "AnalyticsSnapshot (F6)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: snapshotId / snapshotNo(AS-YYYY-NNNN)/ projectId(可空 FK)
- 用户输入: title / periodType / snapshotDate
- 指标: requirementThroughput / sprintOnTimeRate / defectDensity / autoTestCoverage / deploymentFrequency / leadTimeHours / mttrHours / changeFailureRate / aiHoursSaved / activeProjects / projectsAtRisk
- AI 输出: aiRecommendations / aiGenerated / aiGeneratedAt
- 流程: status(3 态) / authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) analytics 行:`00→01→02` (3 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始状态 |
| 01 | 已发布 | {02 已归档} | — |
| 02 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- periodType 字典白名单(month/quarter/year)抛 604
- snapshotDate 必填(602)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/analytics/ai/recommend/{id} — Dify 工作流 analytics-recommend-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 mock 已实现 — 按 4 个关键指标阈值生成 4 维度改进建议 + 农情 IoT 专项建议 Markdown。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4 — Dify 实接入留 v0.5+(对接历史趋势 + 同行业基准)。

---

## 6. 验收标准

**PRD §F6 验收**:
- ⏳ DORA 4 指标完整支持
- ⏳ AI 建议生成时间 < 1 分钟

**模块特有验收**:
<待人工填写>:E2E 测试 / periodType 字典 / 指标范围校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Analytics-数据库设计.md](../02-设计/Analytics-数据库设计.md)
- API 设计: [Analytics-API设计.md](../02-设计/Analytics-API设计.md)
- 测试计划: [Analytics-测试计划-2026-05-17.md](../04-测试/Analytics-测试计划-2026-05-17.md)
- 发布计划: [Analytics-发布计划-2026-05-17.md](../05-上线/Analytics-发布计划-2026-05-17.md)
- 原型: [analytics.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/analytics.html)
- AgriAI PRD: [§F6](../prd和原型/AgriAI-PLM-完整PRD文档.md)

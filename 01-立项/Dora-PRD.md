# PRD: Dora 模块 — DORA 效能指标 (DevOps 扩展)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD DevOps 扩展 + 原型 devops.html) |
| 作者 | Wjl |
| PRD § | DevOps 扩展(AgriAI-PLM-完整PRD文档.md DevOps 子域 DORA) |
| 原型 HTML | [devops.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html) (DORA 4 卡片 + 趋势图 + lead time 拆解) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "DoraMetric (DevOps)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(DORA 4 指标人工算 / 等级评估缺基准 / 改进建议凭经验 / 农情灌溉旺季容灾未演练)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 DevOps 扩展验收标准 + 模块特有衡量指标(等级达 Elite / 改进建议采纳率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实跨模块自动算指标** — 当前 mock,真实算法留 v0.5+
- **DORA 行业基准对标** — 仅 mock 等级评估,真实基准 API 留 v0.5+
- **指标告警阈值** — 仅展示,告警留 v0.5+
- **跨组织对比** — 仅本组织,对比留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:管理层 / DevOps / SRE / PM。

### 2.2 典型场景

**S1 DORA 4 指标录入**(最高频)
<待人工填写>:1 段叙述,4 个指标类型(deploy_freq / lead_time / mttr / change_fail_rate)+ metricValue + 周期 + 日期

**S2 等级评估 + AI 建议**(关键能力)
<待人工填写>:按 metricType + value 阈值 → Elite/High/Medium/Low 评估 + 改进建议

**S3 农情专项建议**(农业特色)
<待人工填写>:灌溉旺季容灾切换演练等建议

**S4 趋势图 + lead time 拆解**(可视化)
<待人工填写>:trendChartJson / leadtimeBreakdown 分阶段(code/review/merge/deploy)JSON

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "DoraMetric (DevOps)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: doraId / doraNo(DORA-YYYY-NNNN)/ projectId(可空 FK)
- 用户输入: metricName / metricType / metricValue / metricUnit / periodType / snapshotDate
- 派生: trendChartJson / heatmapJson(仅 deploy_freq) / leadtimeBreakdown
- AI 输出: aiSuggestions / aiGenerated / aiGeneratedAt
- 流程: status(3 态) / authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) dora 行:`00→01→02` (3 态,单向)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始状态 |
| 01 | 已发布 | {02 已归档} | — |
| 02 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- metricType 4 个白名单(604)
- periodType 字典白名单(month/quarter)抛 604

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/dora/ai/suggest/{id} — Dify 工作流 dora-suggest-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 mock 已实现 — 按 metricType + value 阈值生成等级评估(Elite/High/Medium/Low)+ 农情专项建议(灌溉旺季容灾切换演练)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4。

---

## 6. 验收标准

**DevOps 扩展验收**:
- ⏳ DORA 4 指标完整支持 + 等级评估
- ⏳ AI 改进建议生成时间 < 30s

**模块特有验收**:
<待人工填写>:E2E 测试 / metricType 字典 / 等级阈值算法单测 / heatmapJson 仅 deploy_freq 约束。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Dora-数据库设计.md](../02-设计/Dora-数据库设计.md)
- API 设计: [Dora-API设计.md](../02-设计/Dora-API设计.md)
- 测试计划: [Dora-测试计划-2026-05-17.md](../04-测试/Dora-测试计划-2026-05-17.md)
- 发布计划: [Dora-发布计划-2026-05-17.md](../05-上线/Dora-发布计划-2026-05-17.md)
- 原型: [devops.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html)
- AgriAI PRD: [DevOps 扩展](../prd和原型/AgriAI-PLM-完整PRD文档.md)

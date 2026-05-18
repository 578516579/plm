# PRD: Analytics 模块 — 效能分析快照 (F6)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F6 (AgriAI-PLM-完整PRD文档.md §F6 效能分析与 DORA 指标) |
| 原型 HTML | [analytics.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/analytics.html) + [devops.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html) (KPI 卡 + AI 改进建议) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O6-KR1: Analytics 模块上线,效能数据自动快照覆盖率 100%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "AnalyticsSnapshot (F6)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的效能分析当前走"季度复盘飞书表格",4 个具体问题:

1. **效能数据手算 + 拼**:需求吞吐量 / 迭代准时率 / 缺陷密度 / 自动化覆盖率 / DORA 4 指标 ... 每个季度复盘 **PM 花 8 小时 SQL + Excel 拼数据**,容易出错。
2. **跨项目对比缺位**:全公司在跑 5 个项目,**没有"哪个项目效能最高 / 哪个风险最大"的横向对比**,只能逐个项目复盘。
3. **AI 改进建议缺位**:复盘 PPT 里"建议"段经常是 "继续加强测试覆盖率" 这种空话,**没有基于数据阈值的具体建议**(如 "迭代准时率 67% < 80%,建议把 Sprint 工期从 14 天减到 10 天")。
4. **农业 IoT 专项指标缺位**:农情设备 24h 可用率 / IoT 数据采集完整率 等农业专项 KPI **没人定过**,运营层无法判断 IoT 子项目健康度。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 60 份效能快照数据(每月 / 季度 / 年度),做"自动聚合 14 个 KPI + AI 改进建议 + 农业 IoT 专项"。

**衡量指标**:
- **效能数据自动快照覆盖率 100%**(月度 / 季度 / 年度都生成)
- **数据准备工时降 80%**(基线 8h → 目标 1.5h)
- **AI 改进建议被采纳率 ≥ 50%**(下季度 KR 引用本快照建议)
- **跨项目横向对比可视化 100%**(同一周期可对比 N 个项目)
- **农业 IoT 专项 KPI 覆盖 ≥ 3 项**(设备可用率 / 数据完整率 / 告警响应时间)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **实时数据看板**(WebSocket 推数据)— 仅周期性快照,实时看板留 v0.3 走 Dashboard 模块
- **自定义 KPI 公式**(让用户定义自己的指标)— 仅 14 个标准 KPI,自定义留 v0.5+
- **效能预测**(基于历史趋势预测下月效能)— 留 v0.5+
- **效能数据下钻**(按团队 / 个人 / 模块拆分)— 留 v0.3
- **效能数据对外报告导出**(给客户看)— 仅内部,留 v0.5+
- **跨快照对比 Diff**(本月 vs 上月差异分析)— 留 v0.3

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **管理员 / 老板** | 全 CRUD + 决策依据 | 月度 / 季度复盘 |
| **PM** | CRUD 自己项目的 snapshot | 触发快照 / 看 AI 建议 |
| **测试经理** | 查看 | 看 defectDensity / autoTestCoverage 等质量 KPI |
| **DevOps** | 查看 | 看 DORA 4 指标 |

### 2.2 典型场景

**S1 季度快照生成**(最高频)
> 2026-Q2 末 → 管理员触发"全公司季度效能" → 进入 Analytics 菜单 → 新建 → title "2026-Q2 季度效能" + periodType="quarter" + snapshotDate="2026-04-01" + projectId 空(全局)→ 点 "AI 推荐建议" → mock 从各业务表 SQL 聚合 14 个 KPI(requirementThroughput=87 / sprintOnTimeRate=78.5 / defectDensity=2.4 / autoTestCoverage=68 / deploymentFrequency=3.2 / leadTimeHours=42 / mttrHours=2.1 / changeFailureRate=8.5 / aiHoursSaved=320 / activeProjects=5 / projectsAtRisk=2 等)+ aiRecommendations Markdown 4 维度建议

**S2 单项目快照**(高频)
> PM 要看"灌溉项目 Q2 效能" → 新建 → projectId="PRJ-12" + periodType="quarter" → 只聚合该项目数据 → AI 给建议

**S3 月度快照触发**(关键流程,自动化)
> 每月 1 号 cron 自动跑 → 生成上月快照 → admin 收通知

**S4 AI 改进建议消费**(关键流程)
> AI 建议:"sprintOnTimeRate=78.5% < 80%(灯黄),建议把 Sprint 工期从 14 天减到 10 天",PM 在下个 Sprint 规划时采用 → 下季度快照看是否改善

**S5 快照发布 + 归档**(终态)
> 快照评审 → status='00→01 已发布' → 老板 review → 到下个季度后 → status='01→02 已归档'

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "AnalyticsSnapshot (F6)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: snapshotId / snapshotNo (`AS-YYYY-NNNN`) / projectId(FK 可空,NULL=全局)
- 周期: periodType(3 值:month/quarter/year)/ snapshotDate(周期起点)
- 14 个 KPI:requirementThroughput / sprintOnTimeRate / defectDensity / autoTestCoverage / deploymentFrequency / leadTimeHours / mttrHours / changeFailureRate / aiHoursSaved / activeProjects / projectsAtRisk 等
- AI: aiRecommendations(Markdown)/ aiGenerated / aiGeneratedAt
- 流程: status(3 态)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) analytics 行:3 态单向。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始 |
| 01 | 已发布 | {02 已归档} | 老板 review |
| 02 | 已归档 | {} | 终态;保留历史可查 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- periodType 3 值字典白名单(604)
- snapshotDate 必填(602)
- projectId 可空(全局快照)/ 若填必须存在(702)
- 14 KPI 由服务端 SQL 聚合,前端可看不可写
- aiGeneratedAt 服务端计算

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/analytics/ai/recommend/{id}` — 调用 §F6 `analytics-recommend-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 mock 已实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F6 行)。

mock 输出策略(基于 4 KPI 阈值):
- sprintOnTimeRate < 80% → 建议 "Sprint 工期缩短"
- defectDensity > 3.0 → 建议 "测试用例补充 / 自动化提升"
- autoTestCoverage < 60% → 建议 "强制 isAutomated 比例 / Playwright 培训"
- changeFailureRate > 15% → 建议 "金丝雀发布策略 / 灰度比例降低"

农业 IoT 专项建议:
- 灌溉旺季前 1 个月触发"容灾切换演练"
- 设备数据漂移 > 5% 触发 "传感器校准"

### 5.3 路线图

- v0.3: 真实 AI 接入 / 数据下钻(团队/个人/模块)
- v0.3: 跨快照对比 Diff
- v0.5+: 效能预测 / 自定义 KPI

---

## 6. 验收标准

**PRD §F6 验收**:
- ⏳ **14 个 KPI 自动聚合**(本期 mock 全字段)
- ⏳ **DORA 4 指标**(deploymentFrequency / leadTimeHours / mttrHours / changeFailureRate 字段就位)
- ⏳ **AI 改进建议**(本期 mock 4 维度 + 农业 IoT 专项)

**模块特有验收**(本会话已落地):
- 3 态状态机合法转换单测覆盖
- periodType 3 值字典白名单(604)
- snapshotDate 必填(602)
- 14 KPI 服务端 SQL 聚合,前端不可写
- aiGenerated 服务端计算

---

## 7. 不做的事 — 详 §1.3

- 实时看板 / 自定义 KPI / 预测 / 下钻 / 对外报告 / 对比 Diff

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Analytics-数据库设计.md](../02-设计/Analytics-数据库设计.md)
- API 设计: [Analytics-API设计.md](../02-设计/Analytics-API设计.md)
- 测试计划: [Analytics-测试计划-2026-05-17.md](../04-测试/Analytics-测试计划-2026-05-17.md)
- 发布计划: [Analytics-发布计划-2026-05-17.md](../05-上线/Analytics-发布计划-2026-05-17.md)
- 原型: [analytics.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/analytics.html) + [devops.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html)
- AgriAI PRD: [§F6](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Dora-PRD.md](Dora-PRD.md)(DORA 4 指标横向聚合源) / [Dashboard-PRD.md](Dashboard-PRD.md)(工作台聚合消费)

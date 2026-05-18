# PRD: Dora 模块 — DORA 效能指标 (DevOps)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | DevOps(AgriAI-PLM-完整PRD文档.md DevOps 章节 DORA 4 指标 + 持续改进) |
| 原型 HTML | [devops.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html) (DORA 卡片 + 趋势图 + 热力图 + leadtime breakdown + AI 改进建议) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O6-KR5: Dora 模块上线,DORA 4 指标自动入库覆盖率 100%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "DoraMetric (DevOps)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的 DORA 4 指标(deployment frequency / lead time / MTTR / change failure rate)当前手算,4 个具体问题:

1. **DORA 4 指标无自动采集**:每月复盘需要 SRE 翻 GitLab CI / Jenkins / 缺陷库 + Excel 算 4 指标,**累计 4 小时**。
2. **DORA 等级评估无标准**:Elite / High / Medium / Low 4 个等级的阈值标准(部署频率 / 前置时间)**没人定过**,运营层无法判断"我们处于哪个段位"。
3. **持续改进建议缺位**:复盘 PPT 写"提高部署频率",**没具体建议如"自动化测试覆盖率 +10% 可降低 MTTR 30%"**。
4. **农业灌溉旺季容灾切换演练无追溯**:农情系统在灌溉旺季前要做容灾演练,**当前没数据,只能靠 SRE 个人经验记**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 30 份 DORA 快照数据(月度 / 季度),做"4 指标自动入库 + DORA 等级评估 + AI 持续改进 + 农情专项"。

**衡量指标**:
- **DORA 4 指标自动入库覆盖率 100%**(每月每项目都有)
- **DORA 等级评估 4 级覆盖**(Elite/High/Medium/Low)
- **AI 改进建议被采纳率 ≥ 50%**(下月 KR 引用)
- **leadtime breakdown 拆解到 4 阶段**(code/review/merge/deploy)
- **农情专项建议覆盖率 ≥ 60%**(灌溉旺季演练等)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **DORA 数据实时采集**(从 CI 工具实时拉)— 仅周期性快照,实时留 v0.3
- **跨公司 DORA 对标**(Google DORA Report 行业对比)— 留 v0.5+
- **DORA 趋势预测**(基于历史预测下月)— 留 v0.5+
- **DORA 数据下钻**(按团队 / 个人 / 服务)— 留 v0.3
- **DORA 报告自动外发** — 留 v0.5+
- **DORA 异动告警**(指标突降时通知)— 留 v0.3

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **DevOps / SRE** | CRUD 自己负责的 dora | 录入指标 / 触发 AI 建议 |
| **管理员 / 老板** | 全 CRUD + 决策 | 看 DORA 等级 + 决策改进 |
| **PM / 测试经理** | 查看 | 看 MTTR / change failure rate |

### 2.2 典型场景

**S1 月度 DORA 快照入库**(最高频)
> 月底 → DevOps 进入 DORA 菜单 → 新建 4 条 → 4 条分别对应 metricType:
> - deploy_freq:metricValue=3.2 / metricUnit="次/天" / periodType="month"
> - lead_time:metricValue=42 / metricUnit="小时" / leadtimeBreakdown JSON(code 8h + review 12h + merge 4h + deploy 18h)
> - mttr:metricValue=2.1 / metricUnit="小时"
> - change_fail_rate:metricValue=8.5 / metricUnit="%"
> 触发 AI 建议 → 每项 aiSuggestions Markdown

**S2 DORA 等级评估**(关键流程)
> AI 评估当前段位:
> - deploy_freq 3.2/天 → High(Elite >= 1/天)→ 接近 Elite
> - lead_time 42h → Medium(Elite < 1 天 = 24h)→ 距 Elite 1.75x
> - mttr 2.1h → Elite(< 1 小时为 Elite,2.1h 为 High)
> - change_fail_rate 8.5% → Elite(0-15% 为 Elite)

**S3 AI 持续改进建议**(关键流程)
> AI 输出 aiSuggestions Markdown 含 4 条具体建议:
> 1. "lead_time 42h 中 review 12h 占比最高,建议引入自动 code review 工具"
> 2. "deploy_freq 3.2/天 距 Elite 7/天差 2x,建议自动化测试覆盖率从 68% 提到 80%"
> 3. "农情专项:灌溉旺季 4 月前 1 个月做容灾切换演练 1 次"
> 4. "change_fail_rate 8.5% 已 Elite,持续保持"

**S4 季度 DORA 总览**(关键流程)
> 季度末 → 新建 quarter 周期快照 → 4 metricType 各 1 条 → 系统聚合月度数据展示

**S5 快照归档**(终态)
> 老快照过时 → status='01→02 已归档' → 列表过滤但保留

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "DoraMetric (DevOps)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: doraId / doraNo (`DORA-YYYY-NNNN`) / projectId(FK 可空,NULL=全局)
- 指标: metricName / metricType(4 值字典:deploy_freq/lead_time/mttr/change_fail_rate)/ metricValue / metricUnit
- 周期: periodType(2 值:month/quarter)/ snapshotDate
- 可视化数据: trendChartJson / heatmapJson(仅 deploy_freq) / leadtimeBreakdown(仅 lead_time)
- AI: aiSuggestions(Markdown)/ aiGenerated / aiGeneratedAt
- 流程: status(3 态)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) dora 行:3 态单向。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始 |
| 01 | 已发布 | {02 已归档} | 老板 review |
| 02 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- metricType 4 值字典白名单(604)
- periodType 2 值字典白名单(month/quarter,604)
- metricValue 必填 + 范围合法(602,> 0)
- projectId 可空(全局指标)/ 若填必须存在(702)
- heatmapJson 仅 metricType='deploy_freq' 时有值
- leadtimeBreakdown 仅 metricType='lead_time' 时有值
- aiGeneratedAt 服务端计算

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/dora/ai/suggest/{id}` — 调用 `dora-suggest-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 mock 已实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) dora-suggest-flow 行)。

mock 输出策略(基于 metricType + metricValue 阈值):
- **deploy_freq**:< 1/月 Low / 1/月-1/周 Medium / 1/周-1/天 High / ≥ 1/天 Elite
- **lead_time**:> 6 月 Low / 1-6 月 Medium / 1 周-1 月 High / < 1 周 Elite
- **mttr**:> 7 天 Low / 1-7 天 Medium / 1 天-1 小时 High / < 1 小时 Elite
- **change_fail_rate**:> 60% Low / 30-60% Medium / 15-30% High / 0-15% Elite

每段位输出针对性建议 + 农情专项("灌溉旺季容灾演练" 等)。

### 5.3 路线图

- v0.3: 真实 AI 接入 / 数据下钻(团队/服务)
- v0.3: 异动告警 / 跨快照对比
- v0.5+: 行业对标 / 趋势预测

---

## 6. 验收标准

**DevOps DORA 验收**:
- ⏳ **DORA 4 指标入库**(metricType 4 值字典就位)
- ⏳ **DORA 等级评估 4 级**(本期 mock 输出 Elite/High/Medium/Low)
- ⏳ **AI 持续改进建议**(本期 mock 4 维度 + 农情专项)
- ⏳ **lead_time 4 阶段拆解**(leadtimeBreakdown JSON 就位)

**模块特有验收**(本会话已落地):
- 3 态状态机合法转换单测覆盖
- metricType 4 值 / periodType 2 值字典白名单(604)
- metricValue 范围校验(602)
- aiGenerated 服务端计算
- projectId 可空 / 若填必须存在(702)

---

## 7. 不做的事 — 详 §1.3

- 实时采集 / 行业对标 / 预测 / 下钻 / 报告外发 / 异动告警

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Dora-数据库设计.md](../02-设计/Dora-数据库设计.md)
- API 设计: [Dora-API设计.md](../02-设计/Dora-API设计.md)
- 测试计划: [Dora-测试计划-2026-05-17.md](../04-测试/Dora-测试计划-2026-05-17.md)
- 发布计划: [Dora-发布计划-2026-05-17.md](../05-上线/Dora-发布计划-2026-05-17.md)
- 原型: [devops.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html)
- AgriAI PRD: [DevOps](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html)
- 关联模块: [Analytics-PRD.md](Analytics-PRD.md)(DORA 4 指标横向聚合)/ [Release-PRD.md](Release-PRD.md)(发布数据源)

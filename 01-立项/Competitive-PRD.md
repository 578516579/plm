# PRD: Competitive 模块 — 竞品情报 (F1.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 (实质内容,2026-05-17 由 Claude 基于 PRD-MAPPING §2 + AgriAI PRD §F1.3 + 原型 competitive.html 生成) |
| 作者 | Wjl |
| PRD § | F1.3 (AgriAI-PLM-完整PRD文档.md L224-228) |
| 原型 HTML | [competitive.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/competitive.html) (modal-newcomp + compMatrix + SWOT + 订阅监控) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 OKR | _2026 Q2-O1-KR3: PLM 竞品库容量 ≥ 10 个,AI 竞品分析报告 < 5 分钟_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Competitive (F1.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的竞品情报当前完全依赖 PM 个人努力,3 个具体问题:

1. **竞品信息散落不可复用**:每个 PM 自己收集竞品(禅道/LigaAI/Jira/Trello/飞书项目),信息在个人 OneNote/飞书文档/PDF 报告 — **同一竞品被 3 个 PM 各调研 3 次,SWOT 分析重复劳动**,Q1 团队竞品报告评审会 PM 们才发现"我们都在写禅道"。
2. **SWOT 分析无统一维度**:不同 PM 评同一竞品,优势/劣势的维度完全不同(产品功能 vs 团队体量 vs 定价),**评审会上无法横向比较**;客户问 "你们 vs 禅道核心差异" 时 PM 现编 30 秒。
3. **竞品动态无主动跟踪**:竞品发新版本只能等客户告知或同行群转发,**典型滞后 1-2 月**;v0.1.0 上线后 1 周才知道竞品某厂上线了"AI 工时预估",撞了同款功能。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 10 个竞品的完整 SWOT + 12 维度对比数据,做到"竞品情报随取随用,新功能立项前 5 分钟出对比报告"。

**衡量指标**:
- **竞品库容量 ≥ 10 个**(基线 0,目前散在个人笔记)
- **AI 竞品分析报告生成时间 < 5 分钟**(PRD §F1.3 验收 "≥ 5 个竞品维度",本期 mock 即时)
- **关键竞品(标 isKeyCompetitor=Y)订阅监控覆盖率 100%**(monitorEnabled=Y)
- **立项时引用竞品报告比例 ≥ 70%**(Inception 模块的"市场分析"段引用 competitive 数据)

### 1.3 不做的事 (Out of Scope)

本期**不做** (从 PRD §F1.3 高级能力 + 路线图剥离推断):
- **真实竞品爬虫**(Web 抓取竞品官网 / App Store 评论) — [PLM-路线图.md "永不做剥离"](../99-跨阶段/PLM-路线图.md);本期仅 PM 手动录入 + mock SWOT
- **跨项目竞品库共用**(同竞品在 A/B 项目下复用一份)— 留 v0.5+,本期单 projectId
- **多语言竞品**(英日韩竞品)— 仅中英文,小语种留 v0.5+
- **竞品定价时序图**(月度定价趋势)— 单点 pricingModel 字段,时序留 v0.3
- **AI 主动推送竞品动态**(订阅竞品 → 自动推送 IM)— 仅 monitorEnabled 开关,推送通道留 v0.3 IM 集成
- **15 维度竞品对比矩阵**(PRD 提及但 [路线图剥离清单第 6 项](../99-跨阶段/PLM-路线图.md))— 本期 12 维度对齐原型

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **产品负责人 (PO)** | CRUD 全部竞品 | 录入竞品 / 维护 SWOT / 决策关键竞品 |
| **项目经理 (PM)** | CRUD 自己负责的项目下的竞品 | 立项前查竞品报告 / 录入新发现竞品 |
| **竞品分析师** | CRUD 全部 | 深度调研 / 撰写 SWOT |
| **管理员** | 全 CRUD + 监控管理 | 关键竞品订阅决策 |

### 2.2 典型场景

**S1 新竞品录入 + AI 分析**(最高频)
> 王 PM 立项前发现新竞品 "某 PLM SaaS" → 进入竞品菜单 → 新建 → 填名称 + 厂商 + 官网 + 定价模型 + 12 维度功能特性(`comp-feature` JSON) → 点 "✨ AI 生成 SWOT 分析"  → **5 分钟内** AI 输出 4 象限 SWOT(优势/劣势/机会/威胁,每象限 3-5 条) → PO 评审并微调

**S2 SWOT 对比决策**(高价值)
> 立项 "AI 工时预估" 功能,PO 要评估市场位置 → 进入竞品库 → 筛选 isKeyCompetitor=Y 的关键竞品 → 进入 compMatrix 对比矩阵(原型 12 维度 × N 竞品) → AI 综合报告 → 决定差异化定位

**S3 竞品动态订阅 + 监控**(中频)
> PO 把禅道/LigaAI 标 isKeyCompetitor=Y + monitorEnabled=Y + monitorKeywords="AI 工时,智能评审" → 后端调度(v0.3 加入)周期扫描 → 命中关键词时 lastMonitoredAt 更新 + 飞书通知 PO

**S4 竞品归档**(低频)
> 某竞品厂商停服 → 改 status='02 已归档' → 列表自动过滤,但 SWOT 历史可查(用于"我们当年 vs 已退场对手"复盘)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Competitive (F1.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: competitiveId / competitiveNo (`COMP-YYYY-NNNN`) / projectId(FK 必)
- 竞品信息: name / vendor / officialUrl / pricingModel / productFeatures (JSON 12 维度)
- AI 输出: swotStrengths / swotWeaknesses / swotOpportunities / swotThreats(4 字段 TEXT) / aiGenerated / aiGeneratedAt
- 监控: isKeyCompetitor (Y/N) / monitorEnabled / monitorKeywords / lastMonitoredAt
- 流程: status (3 态)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) competitive 行:`00→01→02` 3 态。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 待分析 | {01 已分析} | 默认初始,刚录入未跑 SWOT |
| 01 | 已分析 | {02 已归档} | SWOT 已完整(aiGenerated='Y' 或人工填) |
| 02 | 已归档 | {} | 终态,可重新启用?不,归档不可逆;若需重启请新建 |

**特殊规则**:
- 推 01 时若 4 个 SWOT 字段任一为空 → 抛 ServiceException(602 "SWOT 不完整不能标记已分析")
- 推 02 时不强制条件(归档是 PO 主观判断)
- isKeyCompetitor='Y' + monitorEnabled='Y' 同时成立时,monitorKeywords 必填(602)

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/competitive/ai/crawl` — 服务端调用 PRD §2.3 `competitive-analysis-flow` Dify 工作流。

### 5.2 当前阶段实现

详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F1.3 行 — **本期占位 mock**(根据 vendor / productFeatures 模板生成 SWOT),Dify 实接入留 v0.5+(届时关联 AgriKB 行业报告语义检索,但 AgriKB 当前剥离)。

### 5.3 mock 输出结构

```
swotStrengths: 根据 productFeatures 12 维度高于均值的部分自动列出
swotWeaknesses: 低于均值部分 + 定价过高警示
swotOpportunities: 该业务线(植保/精准/农资/质量)趋势模板
swotThreats: 本竞品对我方核心差异化点的威胁,模板
```

---

## 6. 验收标准

[PRD §F1.3 验收 L230-233](../prd和原型/AgriAI-PLM-完整PRD文档.md):
- ✅ **AI 竞品分析报告覆盖 ≥ 5 个维度**(本期 12 维度,超额)
- ⏳ **竞品动态订阅推送(钉钉/飞书)**(本期 monitorEnabled 字段就位,推送留 v0.3 IM)

**模块特有验收**:
- 新建竞品 → AI 生成 SWOT → 已分析 → 归档 全流程 E2E 测试绿
- 3 态状态机合法/非法转换覆盖
- SWOT 4 字段在推 01 时校验非空(602)
- monitorEnabled=Y 时 monitorKeywords 必填(602)
- 字段白名单(status / pricingModel 等字典)抛 604

---

## 7. 不做的事 — 详 §1.3

- 真实爬虫 / 跨项目共享 / 多语言 / 时序定价图 / 主动推送 / 15 维度矩阵

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Competitive-数据库设计.md](../02-设计/Competitive-数据库设计.md)
- API 设计: [Competitive-API设计.md](../02-设计/Competitive-API设计.md)
- 测试计划: [Competitive-测试计划-2026-05-17.md](../04-测试/Competitive-测试计划-2026-05-17.md)
- 发布计划: [Competitive-发布计划-2026-05-17.md](../05-上线/Competitive-发布计划-2026-05-17.md)
- 原型: [competitive.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/competitive.html)
- AgriAI PRD: [§F1.3 L224-228](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Inception-PRD.md](Inception-PRD.md) (立项时引用竞品报告)

# PRD: Inception 模块 — AI 立项助手 (F1.1)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 (实质内容,2026-05-17 由 Claude 基于 PRD-MAPPING §2 + AgriAI PRD §F1.1 + 原型 inception.html 生成) |
| 作者 | Wjl (实质内容由 Claude 协助;骨架前置版本由 commit b158d2f 派生) |
| PRD § | F1.1 (AgriAI-PLM-完整PRD文档.md L206-216) |
| 原型 HTML | [inception.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/inception.html) (form: L140-156 + AI 输出: L156-163) |
| 评审状态 | pending(待 Phase 01 Gate 评审) |
| 关联 OKR | _2026 Q2-O2-KR2: PLM 立项 AI 助手上线,立项报告生成时间 ≤ 3 分钟_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Inception (F1.1)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队在新项目立项阶段面临 3 个具体问题:

1. **立项报告人工撰写耗时长**:PM 写 1 份合格立项报告需 1-2 天,内容包括市场背景、ROI、风险评估、ROI 分析,数据散落在飞书表格、行业报告、过往项目复盘里,**收集 + 撰写 + 校审循环常超 5 天**。
2. **立项决策依赖个人经验**:不同 PM 的立项报告质量差异大,新人 PM 容易漏掉风险点;评审会议常因报告不完整退回重做,**评审通过率仅 ~60%**。
3. **立项数据散落,后续难追溯**:立项时的预估工期、团队规模、风险点,转项目后没和 Project 实体强关联,2 个月后回看"当时为什么估 6 个月"要翻飞书文档。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 30 个项目的完整立项数据,作为 Project 模块的前置载体,把立项流程从"5 天人工"压到"3 分钟 AI + 1 天人工 review"。

**衡量指标**:
- **AI 生成立项报告时间 < 3 分钟**(PRD §F1.1 验收标准)
- **立项 → 项目转化率 ≥ 70%**(立项被 admin 批准转 Project 比例)
- **PM 人均月立项产出 ≥ 3 份**(基线:目前 1 份/PM/月)
- **立项报告评审一次通过率 ≥ 80%**(基线 60%)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- 立项报告**自动审批**(必须 admin 手动评审,AI 只给"建议")
- 多人**协作编辑**立项报告(单作者,锁机制留 v0.2)
- **跨项目立项相似度检测**(留 AgriKB 模块,目前剥离)
- **语音输入**支持(原型 §F1.1 提及但本期不实施,留 v0.5+)
- **导出 Word/PDF**(原型有按钮,本期只生成 Markdown,导出留 v0.2)

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **项目经理 (PM)** | CRUD 自己的立项 | 起草 → AI 生成 → 完善 → 提交评审 |
| **管理员 (admin)** | 全 CRUD + 审批 | 评审 + 批准/驳回 + 触发"转项目" |
| **普通成员** | 查看 + 评论 | 跟进进度、留言 (v0.2 开放) |

### 2.2 典型场景

**S1 AI 辅助立项**(最高频)
> 王 PM 想立项"农业病虫害智能识别系统" → 进入立项菜单 → 新建 → 填项目名 + 业务线="植保服务" + 类型="新产品研发" + 背景(自然语言 ~200 字) + 预计工期=6 月 + 团队="前端×2 后端×3 AI×2" → 点"✨ AI 生成立项建议书" → **3 分钟内** AI 输出结构化建议书(背景分析 / ROI 预估 / 风险识别 3-5 条 / 推荐技术方案) → 王 PM review + 微调 → 提交评审 → admin 评审通过 → 自动转 Project (businessLine 等字段从 Inception 复制到 Project)

**S2 风险预警**(高价值)
> 王 PM 提交立项,AI 自动识别 3-5 个风险点 (e.g. "团队 AI 经验不足,建议外聘顾问"、"6 个月工期偏紧,病虫害训练集需 3 个月采集"),在 incRisks 区显示,**避免新手 PM 漏看典型风险**。

**S3 评审决策辅助**(中频)
> admin 收到 5 份立项报告 → AI 给每份打"立项可行性分"(0-10) + 关键决策点 → admin 优先评 ≥7 分的 → 评审会议聚焦"为什么 < 7"的项目 → **评审会议时间从 2 小时压到 1 小时**。

**S4 立项 → 项目转化**(关键流程)
> admin 批准立项 (status='03' 已批准) → 点"转项目"按钮 → 后端调 IProjectService.insertProject(...) 创建 Project,字段映射(详 [PRD-MAPPING.md §2 Project 块"Inception → Project 转项目字段映射"](../PRD-MAPPING.md)):
> - projectName ← projectName
> - businessLine ← businessLine (核心 agri 维度强复制)
> - projectType ← inceptionType (字典 label 不同,值映射待 ADR)
> - status='00', lifecyclePhase='00', progress=0, health='green' (固定值)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Inception (F1.1)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,任何 drift 走 §M.2 流程。

字段一览(详 SSoT):
- projectName / businessLine / inceptionType / background / estimatedDurationMonths / estimatedTeam — 用户输入字段
- aiGenerated / aiProposalContent / aiRisks / aiGeneratedAt — AI 输出字段
- status — 5 态状态机 (00 草稿 → 01 已提交 → 02 审批中 → 03 已批准 / 04 已驳回)
- submitterUserId / approverUserId / approvedAt / rejectReason — 流程字段
- projectId — 转项目后回填 FK

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) inception 行:00→01→{02,04} 02→{03,04} 04→00(反向边)。

**关键转换规则**:
- 新建默认 status='00'
- 进入 04 已驳回 必填 rejectReason(602)
- 进入 03 已批准 后**可触发"转项目"**(调 IProjectService.insertProject)
- 03 是终态,但 projectId 回填后视为"已闭环",列表上加"已转项目"角标

---

## 5. AI 能力

### 5.1 AI 端点

POST /business/inception/ai/generate — 服务端调用 PRD §2.3 project-inception-flow Dify 工作流。

### 5.2 当前阶段实现

详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F1.1 project-inception-flow 行 — **本期占位 mock**(返回 mock Markdown + 写库),Dify 实接入留 v0.5+。

### 5.3 mock 输出结构

```markdown
# 立项建议书:<projectName>

## 1. 市场背景
<根据 business_line 模板填充,e.g. "植保服务"模板:农业病虫害每年减产 20-30%,...>

## 2. ROI 预估
- 预计开发投入: <team×duration×6 万/月>
- 预计 12 个月内回收周期: <模板>

## 3. 风险点 (写入 aiRisks 字段)
- 风险 1: <根据 business_line + duration 模板>
- 风险 2: ...

## 4. 推荐技术方案
<模板>
```

### 5.4 Dify 实接入后(v0.5+)

调真实 LLM (DeepSeek-V3 / Claude 4) + AgriKB 向量检索(本期剥离) + 行业报告数据。

---

## 6. 验收标准

[PRD §F1.1 验收 L230-233](../prd和原型/AgriAI-PLM-完整PRD文档.md):
- ✅ **AI 生成立项报告时间 < 3 分钟** (本期 mock 即时返回,~1s 已满足)
- ⏳ **立项流程审批节点支持钉钉/飞书消息通知** (留 v0.2 IM 集成)

**模块特有验收**:
- 新建立项 → AI 生成 → 提交评审 → 批准 → 转项目 全流程 E2E 测试绿
- 立项报告 Markdown 渲染前端无 XSS / 渲染错位
- businessLine 4 个字典值(植保/精准/农资/质量)与 Project 模块 biz_project_business_line 严格对齐(已做,见 ADR / PRD-MAPPING D1)

---

## 7. 不做的事 (Out of Scope) — 详 §1.3

- 自动审批 / 多人协作 / 跨项目相似度 / 语音输入 / Word PDF 导出

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Inception-数据库设计.md](../02-设计/Inception-数据库设计.md)
- API 设计: [Inception-API设计.md](../02-设计/Inception-API设计.md)
- 测试计划: [Inception-测试计划-2026-05-17.md](../04-测试/Inception-测试计划-2026-05-17.md)
- 发布计划: [Inception-发布计划-2026-05-17.md](../05-上线/Inception-发布计划-2026-05-17.md)
- 原型: [inception.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/inception.html)
- AgriAI PRD: [§F1.1 L206-216](../prd和原型/AgriAI-PLM-完整PRD文档.md)

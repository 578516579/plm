# PRD: Ued 模块 — UED 设计协同 (F2.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F2.3 + 原型 ued.html) |
| 作者 | Wjl |
| PRD § | F2.3 (AgriAI-PLM-完整PRD文档.md §F2.3 UED 设计协同) |
| 原型 HTML | [ued.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/ued.html) (modal-newued + openFigmaSync + uedReview + uedVersions) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Ued (F2.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(Figma 设计稿与 PRD 双向追溯断链 / 设计规范遵从度无统一评估 / 农业 UI 组件库引用混乱 / 设计标注散在评论区)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F2.3 验收标准(UED 与需求双向关联准确率 ≥ 95%)+ 模块特有衡量指标(设计评审平均分 ≥ 80 / Figma 同步成功率)。

### 1.3 不做的事 (Out of Scope)
本期**不做** (从 AgriAI PRD §F2.3 的高级能力 + 项目路线图剥离清单推断):
- **Figma 实时 Webhook 同步** — 仅手动触发 openFigmaSync,实时 Webhook 留 v0.5+
- **设计稿版本可视化 Diff** — 仅 versionLabel,Diff 留 v0.3
- **设计 Token 自动同步到前端** — 仅 annotationContent JSON 存储,Token 自动注入留 v0.5+
- **多人协作标注** — 单 designerUserId,协作留 v0.3
- **用户测试 / 可用性测试集成** — 仅 usabilityIssues TEXT 列表,测试集成留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:从原型 ued.html 中找出涉及角色(UI 设计师 / 交互设计师 / 评审 admin / PM)+ 各自典型动作。

### 2.2 典型场景

**S1 Figma 同步 + AI 评审**(最高频)
<待人工填写>:1 段叙述,引原型 openFigmaSync(figmaUrl + figmaFileKey)→ uedReview → AI 输出 reviewReport + reviewScore + complianceCheck

**S2 设计稿版本迭代**(高价值)
<待人工填写>:uedVersions tab — versionLabel v1.0/v1.1/v2.0,保留历史预览

**S3 设计 ↔ 需求双向关联**(关键流程)
<待人工填写>:requirementId FK → 一键定位"哪个需求的 UED 设计"

**S4 农业组件库标签**(中频)
<待人工填写>:agriComponentTags CSV(地图/作物日历/传感器图/天气卡)对接农业组件库

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Ued (F2.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: uedId / uedNo / projectId(FK)/ requirementId(可选 FK)
- 用户输入: title / figmaUrl / figmaFileKey / versionLabel / previewUrl / agriComponentTags(CSV)
- 标注: annotationContent(JSON 间距/颜色/字体)
- AI 输出: reviewReport / reviewScore(0-100)/ complianceCheck(JSON)/ usabilityIssues / aiGenerated / aiGeneratedAt
- 流程: status / designerUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) ued 行(同 arch 4 态含反向边)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始状态 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 = 评审打回 |
| 02 | 已确认 | {03 已废弃} | 可被下游引用 |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 01→00 必填 reviewNote
- reviewScore 0-100 范围校验(604)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/ued/ai/review/{id} — Dify 工作流 ued-review-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F2.3 行)— 本期占位 mock(返回 mock 评审报告 + 固定 reviewScore=85.0 + 模板化 complianceCheck JSON)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:mock 按 agriComponentTags 注入农业专项检查项;Dify 实接入留 v0.5+(对接 Figma MCP)。

---

## 6. 验收标准

**PRD §F2.3 验收**:
- ⏳ **UED 与需求双向关联准确率 ≥ 95%**(requirementId FK 强约束已落地保底)
- ⏳ AI 评审报告生成时间 < 30s

**模块特有验收**:
<待人工填写>:E2E 测试 / Figma URL 格式校验 / reviewScore 范围 / agriComponentTags 4 个白名单值

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Ued-数据库设计.md](../02-设计/Ued-数据库设计.md)
- API 设计: [Ued-API设计.md](../02-设计/Ued-API设计.md)
- 测试计划: [Ued-测试计划-2026-05-17.md](../04-测试/Ued-测试计划-2026-05-17.md)
- 发布计划: [Ued-发布计划-2026-05-17.md](../05-上线/Ued-发布计划-2026-05-17.md)
- 原型: [ued.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/ued.html)
- AgriAI PRD: [§F2.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)

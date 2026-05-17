# PRD: ManualProduct 模块 — 产品手册 (F5.1)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F5.1 + 原型 productmanual.html) |
| 作者 | Wjl |
| PRD § | F5.1 (AgriAI-PLM-完整PRD文档.md §F5.1 产品手册) |
| 原型 HTML | [productmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/productmanual.html) (modal-newpm + pmContent + 多输出格式) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "ManualProduct (F5.1)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(产品手册靠产品经理人工写 / 与 PRD 双轨脱节 / 多受众版本(管理者/操作员/IT)难维护 / 截图过期)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F5.1 验收标准 + 模块特有衡量指标(AI 手册生成时间 / 多受众覆盖)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **手册自动截图采集** — 仅文本,截图留 v0.5+
- **AI 翻译多语言** — 仅中文,多语言留 v0.5+
- **多受众版本 diff** — 仅单版本,diff 留 v0.3
- **手册问答 chatbot** — 留 v0.5+(对接 AgriKB)

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:产品经理 / 文档工程师 / 客户支持 / 评审 admin。

### 2.2 典型场景

**S1 AI 辅助生成产品手册**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newpm 字段(targetAudience / chapterStructure)→ pmContent 生成

**S2 多输出格式**(关键产出)
<待人工填写>:outputFormats CSV(word/pdf/html/markdown),发布期一键生成多格式

**S3 手册评审**(关键流程)
<待人工填写>:01→00 反向边(评审打回)+ reviewNote 必填

**S4 手册发布 / 归档**(终态)
<待人工填写>:02→03 已发布,可作为客户交付物

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ManualProduct (F5.1)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-manual-product.sql):
- 基础: manualproductId / manualproductNo(PM-YYYY-NNNN)/ projectId(FK)
- 用户输入: title / targetAudience / chapterStructure / outputFormats(CSV)
- AI 输出: content / generatedAt / aiGenerated / aiGeneratedAt
- 流程: status(4 态) / authorUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) manual-product 行:`00→01→02→{00,03}` (4 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 生成中} | 默认初始状态 |
| 01 | 生成中 | {02 已生成} | AI 生成中 |
| 02 | 已生成 | {00 草稿(重新草稿), 03 已发布} | 反向边 02→00 = 重新草稿 |
| 03 | 已发布 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 02→00 视为"重新草稿"重新生成
- outputFormats 4 个字典值(word/pdf/html/markdown)抛 604

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/manual-product/ai/generate — Dify 工作流 product-manual-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 字段已留位(aiGenerated),Dify 实接入留 v0.5+。本期占位 mock(按 targetAudience + chapterStructure 生成模板)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4。

---

## 6. 验收标准

**PRD §F5.1 验收**:
- ⏳ AI 生成产品手册时间 < 10 分钟
- ⏳ 多输出格式(Word/PDF/HTML/Markdown)支持

**模块特有验收**:
<待人工填写>:E2E 测试 / outputFormats CSV 白名单 / 反向边 02→00 单测 / FK 校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [ManualProduct-数据库设计.md](../02-设计/ManualProduct-数据库设计.md)
- API 设计: [ManualProduct-API设计.md](../02-设计/ManualProduct-API设计.md)
- 测试计划: [ManualProduct-测试计划-2026-05-17.md](../04-测试/ManualProduct-测试计划-2026-05-17.md)
- 发布计划: [ManualProduct-发布计划-2026-05-17.md](../05-上线/ManualProduct-发布计划-2026-05-17.md)
- 原型: [productmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/productmanual.html)
- AgriAI PRD: [§F5.1](../prd和原型/AgriAI-PLM-完整PRD文档.md)

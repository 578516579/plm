# PRD: PRD 模块 — AI PRD 生成器 (F2.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F2.2 + 原型 prd.html) |
| 作者 | Wjl |
| PRD § | F2.2 (AgriAI-PLM-完整PRD文档.md §F2.2 AI PRD 生成器) |
| 原型 HTML | [prd.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/prd.html) (modal-newprd + prdContent + prdCompleteness 徽章) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "PRD (F2.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点,需结合 AgriAI PRD §F2.2 业务场景(PRD 撰写耗时长 / 完整度参差不齐 / 农业场景 PRD 模板缺失 / PRD 评审打回率高)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F2.2 验收标准(完整度 ≥ 80%)+ 模块特有衡量指标(AI 生成 PRD 时间 / 评审一次通过率)。

### 1.3 不做的事 (Out of Scope)
本期**不做** (从 AgriAI PRD §F2.2 的高级能力 + 项目路线图剥离清单推断):
- **PRD 版本变更对比可视化 Diff** — 仅 version 字段,可视化 Diff 留 v0.3
- **多人协作编辑** — 单 authorUserId,锁机制留 v0.5+
- **PRD 自动评审打分** — 仅 completenessScore 完整度,评审打分留 v0.5+
- **PRD 模板自定义** — 仅 sceneTemplate 4 个固定模板(灌溉/农销/植保/溯源),自定义留 v0.3
- **导出 Word / PDF** — 仅 Markdown 全文,导出留 v0.2

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:从原型 prd.html 中找出涉及角色(产品经理 / 业务专家 / 评审 admin / 开发 / 测试)+ 各自典型动作。

### 2.2 典型场景

**S1 AI 辅助生成 PRD**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newprd 字段 ID(prd-title / prd-desc / prd-tpl / prd-user)+ 触发 generatePRD 后 prdContent 生成 7 段 Markdown + completenessScore 自动算分。

**S2 评审与打回**(高价值)
<待人工填写>:authorUserId 提交 → reviewerUserId 评审 → 01 评审中 → 反向边打回 00 草稿 + reviewNote

**S3 PRD 确认 → 转入设计**(关键流程)
<待人工填写>:02 已确认后,arch / dbdesign / apidesign 模块可 FK 引用本 PRD;后续设计需求都从此承接

**S4 PRD 废弃**(终态)
<待人工填写>:版本退场 status='03',保留历史可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "PRD (F2.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: prdId / prdNo / projectId(FK)
- 用户输入: title / description / sceneTemplate / targetUser / version
- AI 输出: content(Markdown 7 段) / completenessScore / aiGenerated / aiGeneratedAt
- 流程: status / authorUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) prd 行:`00→01→{00,02}` `02→{03}` `03` 终态。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始状态 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 = 评审打回 |
| 02 | 已确认 | {03 已废弃} | 可被下游 arch/dbdesign/apidesign FK 引用 |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 01→00 必填 reviewNote
- completenessScore 由 generatePRD 服务计算,不接受用户输入(本期 mock 固定 85.0)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/prd/ai/generate/{id} — Dify 工作流 prd-generation-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F2.2 行)— 本期占位 mock(按 sceneTemplate + targetUser 生成 7 段标准 PRD Markdown,completenessScore mock 固定 85.0)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4 — 7 段输出(背景/目标用户/场景/核心功能/非功能/验收/风险),按 sceneTemplate 4 种农业场景注入模板。

---

## 6. 验收标准

**PRD §F2.2 验收**:
- ⏳ **PRD AI 生成完整度 ≥ 80%**(本期 mock 固定 85.0 已满足下限)
- ⏳ AI 生成 PRD 时间 < 5 分钟

**模块特有验收**:
<待人工填写>:E2E 测试 / 单测 / sceneTemplate + targetUser 字典白名单校验(604)/ FK 校验(702)

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Prd-数据库设计.md](../02-设计/Prd-数据库设计.md)
- API 设计: [Prd-API设计.md](../02-设计/Prd-API设计.md)
- 测试计划: [Prd-测试计划-2026-05-17.md](../04-测试/Prd-测试计划-2026-05-17.md)
- 发布计划: [Prd-发布计划-2026-05-17.md](../05-上线/Prd-发布计划-2026-05-17.md)
- 原型: [prd.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/prd.html)
- AgriAI PRD: [§F2.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)

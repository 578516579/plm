# PRD: Openspec 模块 — AI OpenSpec (F3.5)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F3.5 + 原型 aispec.html) |
| 作者 | Wjl |
| PRD § | F3.5 (AgriAI-PLM-完整PRD文档.md §F3.5 AI Agent 与 OpenSpec) |
| 原型 HTML | [aispec.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aispec.html) (规范类型 + 规范内容 + AgriKB 引用) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Openspec (F3.5)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(API/AsyncAPI/AI Function/GraphQL 规范散落 / AgriKB 引用缺乏标注 / 多版本规范并存难维护 / AI 生成规范骨架经验门槛高)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F3.5 验收标准 + 模块特有衡量指标(规范类型覆盖度 / AgriKB 标注率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **规范自动 lint** — 仅文本存储,lint 留 v0.5+
- **规范变更对比 Diff** — 仅 version 字段,Diff 留 v0.3
- **AgriKB 实时检索** — 仅 agriKbRef 引用 ID,实时检索留 v0.5+
- **规范导出 SDK** — 仅 specContent 文本,SDK 生成留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:架构师 / API 设计者 / AI 工程师 / 业务管理员。

### 2.2 典型场景

**S1 4 类规范生成**(最高频)
<待人工填写>:specType 4 个枚举(openapi/asyncapi/ai_function/graphql)→ AI 按类型生成骨架 YAML/JSON

**S2 AgriKB 引用标注**(农业特色)
<待人工填写>:agriKbRef 字段标注 x-agrikb-ref,关联农业知识图谱节点

**S3 版本管理**(关键流程)
<待人工填写>:(specName, version)唯一约束;同名同版本冲突抛 701

**S4 规范弃用**(终态)
<待人工填写>:status='02 已弃用'保留历史可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Openspec (F3.5)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: openspecId / openspecNo(SPEC-YYYY-NNNN)
- 用户输入: specName / specType(4 枚举)/ description / specContent / version / agriKbRef
- AI 输出: aiGenerated / aiGeneratedAt
- 流程: status(3 态) / authorUserId

**唯一键**: (specName, version) 唯一

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) openspec 行:`00→01→02` (终态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始状态 |
| 01 | 已发布 | {02 已弃用} | 可被外部引用 |
| 02 | 已弃用 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- specType 字典白名单(604)
- **(specName, version) 唯一约束**(701)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/openspec/ai/generate/{id} — Dify 工作流 openspec-gen-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 mock 已实现 — 按 specType mock OpenAPI 3.1 / AsyncAPI 3.0 / AI Function / GraphQL 骨架,含 AgriKB x-agrikb-ref 标注。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:Dify 实接入留 v0.5+,接入 AgriKB 知识图谱真实检索。

---

## 6. 验收标准

**PRD §F3.5 验收**:
- ⏳ 4 类规范类型支持
- ⏳ AgriKB 引用标注覆盖

**模块特有验收**:
<待人工填写>:E2E 测试 / specType 字典 / (specName, version) 唯一键 / specContent YAML 语法单测

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Openspec-数据库设计.md](../02-设计/Openspec-数据库设计.md)
- API 设计: [Openspec-API设计.md](../02-设计/Openspec-API设计.md)
- 测试计划: [Openspec-测试计划-2026-05-17.md](../04-测试/Openspec-测试计划-2026-05-17.md)
- 发布计划: [Openspec-发布计划-2026-05-17.md](../05-上线/Openspec-发布计划-2026-05-17.md)
- 原型: [aispec.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aispec.html)
- AgriAI PRD: [§F3.5](../prd和原型/AgriAI-PLM-完整PRD文档.md)

# PRD: DbDesign 模块 — 数据库设计 (F3.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F3.2 + 原型 dbdesign.html) |
| 作者 | Wjl |
| PRD § | F3.2 (AgriAI-PLM-完整PRD文档.md §F3.2 数据库设计) |
| 原型 HTML | [dbdesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/dbdesign.html) (modal-newdb + erDiagram + dbDict + dbSql) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "DbDesign (F3.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(ER 图散在 Visio 文件 / 数据字典与 DDL 不同步 / 命名规范 / 范式 / 索引人工检查耗时)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F3.2 验收标准 + 模块特有衡量指标(AI 生成 DDL 通过率 / 规范检查覆盖)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **DDL 自动落库执行** — 仅生成 SQL 文本,执行留 v0.5+
- **多 DBA 协作 ER 编辑** — 单 authorUserId,协作留 v0.3
- **基线 DDL 对比变更追踪** — 单条目无对比,变更追踪留 v0.3
- **跨库类型自动转换** — 仅 dbEngine 选型,自动转换留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:DBA / 后端架构师 / 评审 admin / 开发(downstream FK 使用)。

### 2.2 典型场景

**S1 AI 辅助生成 ER + DDL**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newdb 字段(db-engine 派生于 arch)→ genDBDesign → 输出 erDiagramContent(Mermaid)+ dataDictionary(Markdown 表)+ ddlScript(CREATE TABLE 集合)

**S2 规范检查**(高价值)
<待人工填写>:normalizationCheck JSON 检查命名 / 索引 / 范式 / 必备审计字段

**S3 评审打回**(关键流程)
<待人工填写>:01→00 反向边 + reviewNote 必填

**S4 国产化数据库**(农业特色)
<待人工填写>:dbEngine 支持 kingbase(信创农业项目场景)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "DbDesign (F3.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: dbdesignId / dbdesignNo / projectId(FK)/ archId(可选 FK)
- 用户输入: title / dbEngine
- AI 输出: erDiagramContent / dataDictionary / ddlScript / normalizationCheck / aiGenerated / aiGeneratedAt
- 流程: status / authorUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) dbdesign 行(同 arch 4 态含反向边)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始状态 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 = 评审打回 |
| 02 | 已确认 | {03 已废弃} | — |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 01→00 必填 reviewNote
- dbEngine 字典白名单(604)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/dbdesign/ai/generate/{id} — Dify 工作流 db-design-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F3.2 行)— 本期占位 mock(返回 mock ER Mermaid + 数据字典表 + DDL 模板)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:mock 按 dbEngine 注入语法差异(MySQL/PG/Kingbase);Dify 实接入留 v0.5+。

---

## 6. 验收标准

**PRD §F3.2 验收**:
- ⏳ AI 生成 ER + DDL 时间 < 3 分钟
- ⏳ DDL 语法可执行率 100%(Mock 期模板预生成保证)

**模块特有验收**:
<待人工填写>:E2E 测试 / dbEngine 字典 / FK 校验 / ER Mermaid 渲染单测

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Dbdesign-数据库设计.md](../02-设计/Dbdesign-数据库设计.md)
- API 设计: [Dbdesign-API设计.md](../02-设计/Dbdesign-API设计.md)
- 测试计划: [Dbdesign-测试计划-2026-05-17.md](../04-测试/Dbdesign-测试计划-2026-05-17.md)
- 发布计划: [Dbdesign-发布计划-2026-05-17.md](../05-上线/Dbdesign-发布计划-2026-05-17.md)
- 原型: [dbdesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/dbdesign.html)
- AgriAI PRD: [§F3.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)

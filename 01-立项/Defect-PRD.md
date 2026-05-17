# PRD: Defect 模块 — 缺陷管理 (F4.6)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F4.6 + 原型 defects.html + ADR-D 决策) |
| 作者 | Wjl |
| PRD § | F4.6 (AgriAI-PLM-完整PRD文档.md §F4.6 缺陷管理) |
| 原型 HTML | [defects.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/defects.html) (缺陷新建 + 详情 + 状态流转) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 关联 ADR | [ADR-D](../99-跨阶段/proposals/) Defect 状态机重设决策(4 主态 + 反向边) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Defect (F4.6)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(缺陷散在 Jira / 严重等级凭经验 / 状态机过复杂(原 PRD 6 态)/ 模块归属不清导致 routing 慢)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F4.6 验收标准 + 模块特有衡量指标(缺陷密度 / 缺陷修复 SLA / 重开率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **PRD 6 态状态机**(新建/确认/修复/验证/关闭/重开)— 走 **ADR-D 4 主态 + 反向边**(理由见 ADR-D)
- **缺陷自动归类** — 仅 module 字段手填,AI 归类留 v0.5+
- **缺陷与 testcase 反向追溯** — 留 v0.5+
- **AI 缺陷根因分析** — 仅 rootCause 文本,AI 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:QA / 测试 / 开发 / PM。

### 2.2 典型场景

**S1 缺陷新建**(最高频)
<待人工填写>:1 段叙述,引原型缺陷新建表单 + module 字段(模块归属)

**S2 测试 → 修复 → 验证 → 关闭**(关键流程)
<待人工填写>:4 主态正向流转

**S3 验证失败重开**(反向边,ADR-D)
<待人工填写>:验证不通过 03 → 02 反向边(缺陷重开)

**S4 缺陷密度统计**(高价值)
<待人工填写>:analytics 模块的 defectDensity 指标(个/KLOC)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Defect (F4.6)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-defect.sql):
- 基础: defectId / defectNo(DEF-YYYY-NNNN)/ projectId(FK)
- 用户输入: title / description / severity / priority / module(本会话 ADR-D 新增)
- 流程: status(4 主态)/ assigneeUserId / reporterUserId / rootCause / resolution
- FK: requirementId(可选)/ testcaseId(可选)

---

## 4. 状态机

### 4.1 ADR-D 决策回顾
PRD §F4.6 描述状态机为 6 态(新建/确认/修复/验证/关闭/重开)。ADR-D 决议:走 4 主态 + 反向边实用版。
- **依据**: 原型 UI 仅 4 主态;6 态实际工作中"确认"与"新建"频繁合并;"重开"等价于反向边而非独立态。
- **缓解**: 通过 `03→02` 反向边表达"验证失败重开",历史可追。

### 4.2 状态机定义

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) defect 行(4 主态 + 反向边)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 新建 | {01 修复中} | 默认初始状态 |
| 01 | 修复中 | {02 已修复} | 开发处理中 |
| 02 | 已修复 | {01 修复中(测试打回), 03 已验证} | — |
| 03 | 已验证 | {02 已修复(验证失败重开), 04 已关闭} | **反向边 03→02 = 缺陷重开** |
| 04 | 已关闭 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 02→01(测试打回)/ 03→02(验证失败重开)必填 rootCause / resolution
- severity / priority / module 字典白名单(604)

---

## 5. AI 能力

### 5.1 AI 端点
(本模块当前阶段无 AI 端点。未来留位 AI 缺陷根因分析 + 自动归类。)

### 5.2 当前阶段实现
n/a

### 5.3 mock 输出 / Dify 工作流
n/a — 留 v0.5+

---

## 6. 验收标准

**PRD §F4.6 验收**:
- ⏳ 缺陷密度 < 2 个/KLOC
- ⏳ 缺陷修复 SLA P0 24h / P1 3 天

**模块特有验收**:
<待人工填写>:E2E 测试 / 4 主态合法/非法转换 + 2 个反向边单测 / 字典白名单。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Defect-数据库设计.md](../02-设计/Defect-数据库设计.md)
- API 设计: [Defect-API设计.md](../02-设计/Defect-API设计.md)
- 测试计划: [Defect-测试计划-2026-05-17.md](../04-测试/Defect-测试计划-2026-05-17.md)
- 发布计划: [Defect-发布计划-2026-05-17.md](../05-上线/Defect-发布计划-2026-05-17.md)
- 原型: [defects.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/defects.html)
- AgriAI PRD: [§F4.6](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- ADR-D 决策记录

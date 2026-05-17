# PRD: ManualImpl 模块 — 实施手册 (F5.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F5.2 + 原型 implmanual.html) |
| 作者 | Wjl |
| PRD § | F5.2 (AgriAI-PLM-完整PRD文档.md §F5.2 实施手册) |
| 原型 HTML | [implmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/implmanual.html) (modal-newim + imContent + 部署组合 select) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "ManualImpl (F5.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(实施手册按客户定制重复劳动 / 部署组合(Docker/K8s + CentOS/Ubuntu/Kylin + MySQL/PG/Kingbase)矩阵爆炸 / 农情大屏接入 / 回滚预案散在)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F5.2 验收标准 + 模块特有衡量指标(AI 实施手册生成时间 / 客户首次部署成功率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实部署脚本一键执行** — 仅文本,执行留 v0.5+
- **农情大屏对接自动化** — 仅描述,自动化留 v0.5+
- **多客户实施手册版本管理** — 单条目,多客户管理留 v0.3
- **AI 实施 troubleshoot** — 仅 5 章节文本,troubleshoot 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:实施工程师 / 项目经理 / 客户 IT 运维 / 评审 admin。

### 2.2 典型场景

**S1 AI 辅助生成实施手册**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newim → 3 维度选(deployMode / osType / dbType)→ 调 §F5.2 impl-manual-flow → 生成 5 章节(环境准备/部署步骤/环境变量/农情大屏接入/回滚预案)

**S2 信创组合**(农业特色)
<待人工填写>:Kylin + Kingbase 国产化组合(信创农业项目)

**S3 手册评审**(关键流程)
<待人工填写>:02→00 反向边(重新草稿)

**S4 手册发布**(终态)
<待人工填写>:03 已发布,交付客户

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ManualImpl (F5.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: manualimplId / manualimplNo(IM-YYYY-NNNN)/ projectId(FK)
- 用户输入: title / deployMode / osType / dbType / envConfig(JSON)
- AI 输出: content / generatedAt / aiGenerated
- 流程: status(4 态) / authorUserId
- 输出: outputFormats(CSV)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) manual-impl 行:`00→01→02→{00,03}` (4 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 生成中} | 默认初始状态 |
| 01 | 生成中 | {02 已生成} | AI 生成中 |
| 02 | 已生成 | {00 草稿(重新草稿), 03 已发布} | 反向边 02→00 |
| 03 | 已发布 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 02→00 视为"重新草稿"
- deployMode / osType / dbType 3 个字典白名单(604)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/manual-impl/ai/generate/{id} — Dify 工作流 impl-manual-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 mock 已实现 — 按 deployMode + osType + dbType 三维度生成 5 章节模板(含农情大屏接入 + 回滚预案)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4 — Dify 实接入留 v0.5+。

---

## 6. 验收标准

**PRD §F5.2 验收**:
- ⏳ AI 生成实施手册时间 < 10 分钟
- ⏳ 5 章节模板完整(环境/部署/环境变量/农情接入/回滚)

**模块特有验收**:
<待人工填写>:E2E 测试 / 3 维度字典白名单 / 反向边 02→00 单测 / envConfig JSON 校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [ManualImpl-数据库设计.md](../02-设计/ManualImpl-数据库设计.md)
- API 设计: [ManualImpl-API设计.md](../02-设计/ManualImpl-API设计.md)
- 测试计划: [ManualImpl-测试计划-2026-05-17.md](../04-测试/ManualImpl-测试计划-2026-05-17.md)
- 发布计划: [ManualImpl-发布计划-2026-05-17.md](../05-上线/ManualImpl-发布计划-2026-05-17.md)
- 原型: [implmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/implmanual.html)
- AgriAI PRD: [§F5.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)

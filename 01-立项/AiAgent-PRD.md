# PRD: AiAgent 模块 — AI Agent 编排 (F3.5)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F3.5 + 原型 aiagents.html) |
| 作者 | Wjl |
| PRD § | F3.5 (AgriAI-PLM-完整PRD文档.md §F3.5 AI Agent 与 OpenSpec) |
| 原型 HTML | [aiagents.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aiagents.html) (Agent 卡片 + 统计 + 调用) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "AiAgent (F3.5)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(各业务模块 AI 能力分散调用 / 提示词版本失控 / Dify 工作流无统一登记 / 调用成功率与频次无监控)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F3.5 验收标准 + 模块特有衡量指标(Agent 平均成功率 ≥ 95% / 单 Agent 日调用峰值)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实 Dify HTTP 调用** — 仅 mock invoke(累加 totalCalls / 移动平均 successRate),Dify proxy 留 v0.5+
- **Agent 版本管理** — 单条目无版本,版本管理留 v0.3
- **Agent 链式编排** — 单 Agent 独立调用,链式留 v0.5+
- **Token 成本统计** — 仅 totalCalls 计数,Token 成本留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:AI 工程师 / 提示词工程师 / 业务管理员。

### 2.2 典型场景

**S1 6 类 Agent 注册**(最高频)
<待人工填写>:agentType 6 个枚举值(requirement/prd/code/test/release/ops)→ 每类挂 Dify workflow

**S2 调用统计移动平均**(关键指标)
<待人工填写>:invoke 触发 → totalCalls += 1 + successRate 移动平均(mock 95%)

**S3 错误态恢复**(关键状态)
<待人工填写>:status='02 错误' → 排查后 → 重启 01 运行中 或 停用 00

**S4 提示词模板维护**(常规)
<待人工填写>:promptTemplate LONGTEXT 存系统提示词,configJson 存高级参数

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "AiAgent (F3.5)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: agentId / agentNo(AGT-YYYY-NNNN)
- 用户输入: agentName / agentType(6 枚举)/ description / promptTemplate / difyWorkflowId / configJson
- 派生统计: totalCalls / successRate / lastInvokedAt
- 流程: status(3 态) / authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) ai-agent 行:`00→{01,02}` `01→{00}` `02→{00,01}`(错误态可重启或停用)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 运行中 | {01 已停止, 02 错误} | 默认初始 / 健康态 |
| 01 | 已停止 | {00 运行中} | 主动停用 |
| 02 | 错误 | {00 运行中, 01 已停止} | 调用失败累计触发;可重启或停用 |

**特殊规则**:
- agentType 6 个白名单(604)
- 调用 invoke 自动累加 totalCalls + 重算 successRate

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/ai-agent/invoke/{id} — Dify proxy 占位(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 mock 已实现(Dify proxy 占位)— 调用累加 totalCalls + 移动平均 successRate 95%。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:Dify 实接入留 v0.5+,通过 Dify HTTP API 转发实际 LLM 调用。

---

## 6. 验收标准

**PRD §F3.5 验收**:
- ⏳ Agent 平均成功率 ≥ 95%
- ⏳ Agent 调用统计延迟 < 1s

**模块特有验收**:
<待人工填写>:E2E 测试 / agentType 字典白名单 / 状态机校验 / 调用统计单测(移动平均算法)

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [AiAgent-数据库设计.md](../02-设计/AiAgent-数据库设计.md)
- API 设计: [AiAgent-API设计.md](../02-设计/AiAgent-API设计.md)
- 测试计划: [AiAgent-测试计划-2026-05-17.md](../04-测试/AiAgent-测试计划-2026-05-17.md)
- 发布计划: [AiAgent-发布计划-2026-05-17.md](../05-上线/AiAgent-发布计划-2026-05-17.md)
- 原型: [aiagents.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aiagents.html)
- AgriAI PRD: [§F3.5](../prd和原型/AgriAI-PLM-完整PRD文档.md)

# PRD: AI Agent 模块 — AI Agent 编排 (F3.5)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F3.5 (AgriAI-PLM-完整PRD文档.md §F3.5 AI 智能体编排) |
| 原型 HTML | [aiagents.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aiagents.html) (Agent 卡片 + 类型分类 + 调用统计 + 提示词模板) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O3-KR4: PLM AI Agent 模块上线,平均成功率 ≥ 95%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "AiAgent (F3.5)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 系统当前的 AI 能力分散在各模块的 `/ai/*` 端点,4 个具体问题:

1. **AI Agent 散落无统一编排**:每个模块自己写 mock 输出 / 自己调 Dify,**21 个 AI 端点散落在 21 个 Service 类**,没有统一管理面板,管理员无法一眼看到 "PLM 一共有多少个 Agent 在跑"。
2. **调用统计与 SLA 黑盒**:今日总调用量 / 成功率 / 平均响应时间 全无数据,**Q1 用户反馈 "AI 生成 PRD 慢"无法定位是哪个 Agent 卡了**。
3. **提示词模板分散难维护**:每个模块 Service 类硬编码 prompt 字符串,**改一个 Agent 的提示词要改 Java 代码 + 重新编译部署**,迭代效率低。
4. **Dify 工作流绑定无追溯**:某些 Agent 接 Dify(impl/ops 手册),某些走纯 mock,**无法在管理面板查"这个 Agent 是 mock 还是 Dify 实接"**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内 21 个 AI 端点全部接入 AI Agent 注册中心,做"统一编排 + 调用统计 + 提示词管理"。

**衡量指标**:
- **AI Agent 平均成功率 ≥ 95%**(移动平均)
- **AI Agent 注册覆盖率 100%**(21 个端点都在 tb_ai_agent 注册)
- **Dify 工作流绑定率 ≥ 60%**(difyWorkflowId 不空的占比)
- **今日总调用数据可见率 100%**(管理面板实时看到 totalCalls)
- **提示词模板热更率**:改 prompt 不需要重新部署 Java(走 promptTemplate 字段)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **Agent A/B 测试**(同一场景两个 Agent 对比效果)— 留 v0.3
- **Agent 自动选择**(根据场景自动选最优 Agent)— 留 v0.5+
- **Agent 多轮对话状态保存**(Memory)— Agent 是无状态调用,有状态对话留 v0.5+
- **Agent 自学习**(基于历史调用反馈优化 prompt)— 留 v1.0+
- **Agent 成本核算**(token 计费)— 留 v0.3,本期不接入计费
- **Agent 跨项目共享市场**(类 ChatGPT GPT Store)— 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **管理员 (admin)** | 全 CRUD + 启停 | 查看所有 Agent / 维护提示词 / 启停 Agent |
| **AI 工程师 (ai_engineer)** | CRUD 自己创建的 Agent | 创建新 Agent / 接 Dify / 调 prompt |
| **业务用户 (PM/QA/Dev)** | 间接消费(通过模块 AI 端点)| 不直接管理 Agent |

### 2.2 典型场景

**S1 创建新 Agent**(高频)
> AI 工程师要创建"灌溉量推荐"Agent → 进入 AI Agents 菜单 → 新建 → agentName "AgriIrrigationRecommendAgent" + agentType="prd"(从 6 值字典:requirement/prd/code/test/release/ops 中选)+ description "基于土壤湿度+天气推荐灌溉量" + promptTemplate "你是一个智慧灌溉专家..." + difyWorkflowId="wf-001" + configJson(高级参数:temperature/max_tokens)→ status='00 运行中'

**S2 业务模块调用 Agent**(最高频,经由 ai/* 端点)
> Prd 模块用户点 "AI 生成 PRD" → Prd Service 内部找 agentType='prd' 的 Agent → POST /business/ai-agent/invoke/{id} → 累加 totalCalls + mock 95% 成功率 + 移动平均 successRate + lastInvokedAt=NOW()

**S3 Agent 停用**(关键流程)
> Agent 表现差或要替换 → admin 改 status='00→01 已停止' → 业务模块再调用时报错 → 业务降级到 mock 或别的 Agent

**S4 错误态处理**(异常路径)
> Agent 连续 10 次失败 → Service 自动改 status='00→02 错误' → 触发告警 → admin 排查 (Dify 工作流挂了 / prompt 失效) → 修复后 status='02→00 运行中' 或 '02→01 已停止'

**S5 提示词热更**(关键流程)
> AI 工程师发现某 Agent 提示词过时 → 改 promptTemplate 字段 + 保存 → 下一次调用立即生效(不需重启 Java)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "AiAgent (F3.5)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: agentId / agentNo (`AGT-YYYY-NNNN`)
- 用户输入: agentName / agentType(6 值字典)/ description / promptTemplate(系统提示词)/ difyWorkflowId / configJson
- 服务计算: totalCalls(累加)/ successRate(移动平均 95%)/ lastInvokedAt
- 流程: status(3 态)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) ai-agent 行:

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 运行中 | {01 已停止, 02 错误} | 默认初始;业务可正常调用 |
| 01 | 已停止 | {00 运行中} | admin 主动停;调用拦截 |
| 02 | 错误 | {00 运行中, 01 已停止} | 连续失败自动转此态;修复后回 00 或停 01 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- agentType 6 值白名单(604)
- 业务调用时检查 status='00',否则拒绝调用
- successRate 服务端计算(EWMA 加权移动平均),不接受前端写入
- totalCalls 累加,前端不可写

---

## 5. AI 能力

### 5.1 业务入口

`POST /business/ai-agent/invoke/{id}` — 由业务模块(prd/inception/ued 等 ai/* 端点)反向调用。本期 mock 95% 成功率 + 累加调用统计;实际接 Dify HTTP API。

### 5.2 当前阶段实现

🟡 mock 已实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F3.5 ai-agent-invoke-flow 行)— 占位 Dify Proxy:
- 输入:agent_id + business_payload
- mock 输出:`{status:'success', response:{...}, durationMs:1200}` 95% 概率
- mock 错误:5% 概率 `{status:'error', errorCode:'AGENT_TIMEOUT'}`
- 副作用:totalCalls++ / successRate 重算 / lastInvokedAt=NOW()

### 5.3 路线图

- v0.3: Dify HTTP API 真实接入 / 真实 token 计费 / 失败重试策略
- v0.3: Agent A/B 测试框架
- v0.5+: Agent 跨项目共享市场 / 自动选择

---

## 6. 验收标准

**PRD §F3.5 验收**:
- ⏳ **AI Agent 注册中心**(本期 agentNo + agentType 字典就位)
- ⏳ **统一编排入口 POST /business/ai-agent/invoke**(本期 mock 95% 成功率)
- ⏳ **调用统计 totalCalls/successRate**(本期服务端计算就位)

**模块特有验收**(本会话已落地):
- 3 态状态机合法转换单测覆盖
- agentType 6 值白名单(604)
- successRate / totalCalls 服务端计算,前端写入被忽略
- 业务调用时拒绝 status=01/02 的 Agent

---

## 7. 不做的事 — 详 §1.3

- A/B 测试 / 自动选择 / 多轮对话 / 自学习 / 成本核算 / 共享市场

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [AiAgent-数据库设计.md](../02-设计/AiAgent-数据库设计.md)
- API 设计: [AiAgent-API设计.md](../02-设计/AiAgent-API设计.md)
- 测试计划: [AiAgent-测试计划-2026-05-17.md](../04-测试/AiAgent-测试计划-2026-05-17.md)
- 发布计划: [AiAgent-发布计划-2026-05-17.md](../05-上线/AiAgent-发布计划-2026-05-17.md)
- 原型: [aiagents.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aiagents.html)
- AgriAI PRD: [§F3.5](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: 21 个 AI 端点的业务模块(prd/inception/ued/arch/dbdesign/apidesign 等)

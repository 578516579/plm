# PRD: Openspec 模块 — AI OpenSpec 规范管理 (F3.5)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F3.5 (AgriAI-PLM-完整PRD文档.md §F3.5 OpenSpec 规范管理) |
| 原型 HTML | [aispec.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aispec.html) (specType 4 选项 + specContent YAML/JSON + AgriKB ref) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | UNIQUE(specName, version) 设计 |
| 关联 OKR | _2026 Q2-O3-KR5: OpenSpec 模块上线,AI 规范生成准确率 ≥ 75%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Openspec (F3.5)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的"规范"管理走"飞书文档贴 YAML",4 个具体问题:

1. **OpenAPI / AsyncAPI / AI Function 规范散落**:REST API 规范在 apidesign 模块,但**WebSocket / 消息队列 (AsyncAPI) / AI Function 调用规范 (function calling JSON Schema) 全没有承载模块**,只存 Word 文档。
2. **规范版本演进无追溯**:某 OpenSpec v1.0 → v2.0 改了什么 / 不兼容点在哪,**翻飞书消息找上下文,平均 2 小时**。
3. **AgriKB 关联缺失**:农业领域规范(灌溉 API / 农情数据 schema)应关联农业知识库,**当前 agriKbRef 全空**。
4. **AI Function 规范缺位**:LLM Tool Use / Function Calling 场景越来越多,**当前 PLM 没地方记 AI Function Spec**(输入 schema / 输出 schema / 调用条件)。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 50 份规范数据,覆盖 OpenAPI 3.1 / AsyncAPI 3.0 / AI Function / GraphQL 4 种类型。

**衡量指标**:
- **AI 规范生成准确率 ≥ 75%**(架构师采纳 AI 生成的 spec 不需大改的比例)
- **UNIQUE(specName, version) 冲突拦截 100%**(避免重名重版)
- **农业 spec agriKbRef 关联率 ≥ 40%**(农业 / IoT / 灌溉 / 农销 等 spec)
- **AI Function Spec 占比 ≥ 20%**(新趋势)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **规范自动校验**(对实际接口实现做 spec 比对)— 留 v0.3 走 apidoc 模块联动
- **规范变更影响分析**(spec 改了 → 哪些下游受影响)— 留 v0.5+
- **GraphQL 完整支持**(schema 演进 / 类型扩展)— 仅占位 4 值字典,GraphQL 完整能力留 v0.5+
- **AsyncAPI 实际消息回放**(对接 Kafka/RabbitMQ)— 留 v0.5+
- **规范导出 Postman 集合 / SDK 自动生成** — 留 v0.3
- **跨项目规范复用推荐** — 留 v0.5+,需 AgriKB 向量库

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **架构师 / AI 工程师** | CRUD 自己负责的 OpenSpec | 创建 spec / 触发 AI 生成 / 维护版本 |
| **评审 admin** | 全 CRUD + 决策 | 评审 spec 合规 |
| **开发** | 查看 + 评论 | 读 specContent 作开发依据 |
| **测试** | 查看 | 设计契约测试用例 |

### 2.2 典型场景

**S1 AI 生成 OpenAPI Spec**(高频)
> 张架构师要写"灌溉控制接口 API Spec" → 新建 → specName "agri-irrigation-api" + specType="openapi"(4 值字典:openapi/asyncapi/ai_function/graphql)+ version="v1.0" + description "灌溉控制 REST 接口规范" + agriKbRef="agrikb-irrigation-control" → 点 "AI 生成" → mock 输出 OpenAPI 3.1 YAML 含农业字段标准(soil_moisture/crop_type/water_amount)+ x-agrikb-ref 标注 → status='00 草稿'

**S2 AsyncAPI 规范**(关键流程)
> 王架构师设计"IoT 设备消息" → 新建 → specName "agri-iot-mqtt" + specType="asyncapi" + version="v1.0" + agriKbRef="agrikb-iot-protocol" → 点 "AI 生成" → mock 输出 AsyncAPI 3.0 YAML 含 MQTT topic 设计 + payload schema

**S3 AI Function Spec**(新场景)
> AI 工程师要让 LLM 调用 "灌溉量计算函数" → 新建 → specName "calc-irrigation-amount" + specType="ai_function" → mock 输出 JSON Schema 含 function_name / parameters / required / examples

**S4 spec 唯一键冲突拦截**(关键流程)
> 王再建 "agri-irrigation-api" v1.0 → Service 校验 UNIQUE(spec_name, version) → 抛 701 "已存在 v1.0,请新建 v1.1 或修改名"

**S5 spec 发布 + 弃用**(终态)
> 评审通过 → status='00→01 已发布' → 下游消费 → 版本演进 → 老 spec status='01→02 已弃用'

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Openspec (F3.5)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: openspecId / openspecNo (`SPEC-YYYY-NNNN`)
- 用户输入: specName / specType(4 值字典)/ description / version(语义化)/ agriKbRef
- 内容: specContent(YAML / JSON 全文)
- 流程: status(3 态)/ authorUserId / aiGenerated / aiGeneratedAt

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) openspec 行:

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始 |
| 01 | 已发布 | {02 已弃用} | 下游可消费 |
| 02 | 已弃用 | {} | 终态;新版本继任 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- specType 4 值白名单(604)
- **UNIQUE(spec_name, version)**:重名同版本抛 701
- version 必填(602)
- specContent 在 status=01 之前必填(602)

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/openspec/ai/generate/{id}` — 调用 §F3.5 `openspec-gen-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 mock 已实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) openspec-gen-flow 行)— 按 specType 分发 mock 输出:
- openapi → OpenAPI 3.1 YAML 骨架 + 农业字段示例
- asyncapi → AsyncAPI 3.0 YAML 含 MQTT/Kafka topic 示例
- ai_function → JSON Schema 含 function_name/parameters
- graphql → GraphQL Schema 类型定义

含 x-agrikb-ref 标注(若 agriKbRef 不空)。

### 5.3 路线图

- v0.3: 真实 AI 接入 / Postman 集合导出
- v0.3: 规范自动校验(对比实现)
- v0.5+: 跨项目复用 / AsyncAPI 消息回放

---

## 6. 验收标准

**PRD §F3.5 验收**:
- ⏳ **AI 生成 4 种类型规范**(本期 mock 覆盖 4 类)
- ⏳ **AgriKB 关联标注**(本期 agriKbRef + x-agrikb-ref 就位)
- ⏳ **版本管理与唯一键约束**(本期 UNIQUE 701 就位)

**模块特有验收**(本会话已落地):
- 3 态状态机合法转换单测覆盖
- specType 4 值白名单(604)
- UNIQUE(spec_name, version) → 701 单测覆盖
- aiGenerated 服务端计算,前端写入被忽略

---

## 7. 不做的事 — 详 §1.3

- 自动校验 / 影响分析 / GraphQL 完整 / AsyncAPI 回放 / 自动 SDK / 跨项目复用

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Openspec-数据库设计.md](../02-设计/Openspec-数据库设计.md)
- API 设计: [Openspec-API设计.md](../02-设计/Openspec-API设计.md)
- 测试计划: [Openspec-测试计划-2026-05-17.md](../04-测试/Openspec-测试计划-2026-05-17.md)
- 发布计划: [Openspec-发布计划-2026-05-17.md](../05-上线/Openspec-发布计划-2026-05-17.md)
- 原型: [aispec.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aispec.html)
- AgriAI PRD: [§F3.5](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [AiAgent-PRD.md](AiAgent-PRD.md) / [Apidesign-PRD.md](Apidesign-PRD.md)

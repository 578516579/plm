# PRD: ApiDesign 模块 — LLD 接口详细设计 (F3.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F3.3 + 原型 apidesign.html) |
| 作者 | Wjl |
| PRD § | F3.3 (AgriAI-PLM-完整PRD文档.md §F3.3 接口详细设计) |
| 原型 HTML | [apidesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidesign.html) (modal-newapi + apiDetailView + Mock 服务) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "ApiDesign (F3.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(接口设计散在 Word / OpenAPI 规范手写错漏 / Mock 服务部署成本高 / 设计期 vs 交付期接口文档双轨)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F3.3 验收标准 + 模块特有衡量指标(OpenAPI 通过率 / Mock 启用率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **OpenAPI 自动校验 lint** — 仅文本存储,lint 留 v0.3
- **Mock 服务真实暴露** — 仅 mockResponse 字段,Mock 服务端点留 v0.5+
- **接口性能基准压测** — 仅设计期,压测留 autotest 模块
- **多版本接口共存路由** — 仅单 method + path 唯一,版本路由留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:后端架构师 / 接口设计者 / 评审 admin / 前端 / 测试。

### 2.2 典型场景

**S1 AI 辅助生成 OpenAPI**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newapi 字段(na-method / na-path / na-desc)→ genAPIDesign → 输出 OpenAPI 3.0 YAML + Mock 响应

**S2 评审打回**(高价值)
<待人工填写>:01→00 反向边 + reviewNote

**S3 唯一键冲突**(关键校验)
<待人工填写>:UNIQUE(project_id, http_method, path)→ 重复抛 701

**S4 Mock 服务联调**(F3.6 联调场景)
<待人工填写>:mockEnabled=Y 开关,前端可调 Mock 接口提前联调

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ApiDesign (F3.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: apidesignId / apidesignNo / projectId(FK)/ archId(可选 FK)
- 用户输入: title / httpMethod / path / description
- AI 输出: requestSchema / responseSchema / openapiSpec(YAML)/ aiGenerated / aiGeneratedAt
- Mock: mockEnabled / mockResponse
- 流程: status / authorUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) apidesign 行(同 arch 4 态含反向边)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始状态 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 = 评审打回 |
| 02 | 已确认 | {03 已废弃} | — |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 01→00 必填 reviewNote
- **UNIQUE(project_id, http_method, path) 冲突抛 701**
- httpMethod 白名单(GET/POST/PUT/DELETE/PATCH)抛 604

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/apidesign/ai/generate/{id} — Dify 工作流 detail-design-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F3.3 行)— 本期占位 mock(生成 OpenAPI YAML 模板 + Mock 响应)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:mock 按 httpMethod 注入差异(GET 无 body / POST 有 body);Dify 实接入留 v0.5+。

---

## 6. 验收标准

**PRD §F3.3 验收**:
- ⏳ AI 生成 OpenAPI 时间 < 1 分钟
- ⏳ OpenAPI 3.0 规范符合率 100%

**模块特有验收**:
<待人工填写>:E2E 测试 / 唯一键冲突 / FK 校验 / OpenAPI YAML schema 校验单测

---

## 7. 不做的事 — 详 §1.3

**与 apidoc 区分**:apidesign = 设计期(00草稿→01评审→02已确认);apidoc = 交付发布期(从代码注释提取的对外文档)。

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Apidesign-数据库设计.md](../02-设计/Apidesign-数据库设计.md)
- API 设计: [Apidesign-API设计.md](../02-设计/Apidesign-API设计.md)
- 测试计划: [Apidesign-测试计划-2026-05-17.md](../04-测试/Apidesign-测试计划-2026-05-17.md)
- 发布计划: [Apidesign-发布计划-2026-05-17.md](../05-上线/Apidesign-发布计划-2026-05-17.md)
- 原型: [apidesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidesign.html)
- AgriAI PRD: [§F3.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)

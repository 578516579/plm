# PRD: ApiDesign 模块 — LLD 接口详细设计 (F3.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F3.3 (AgriAI-PLM-完整PRD文档.md §F3.3 接口详细设计) |
| 原型 HTML | [apidesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidesign.html) (modal-newapi + apiDetailView OpenAPI YAML + Mock 服务开关) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | UNIQUE(method,path) 701 设计 |
| 关联 OKR | _2026 Q2-O3-KR3: PLM ApiDesign 模块上线,前后端联调时间降 50%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "ApiDesign (F3.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前 LLD(接口详细设计)走 "Word 文档 + 飞书表格",4 个具体问题:

1. **接口设计与实现不一致**:Word 文档写"GET /api/irrigation/list 返回 RecommendVO",代码实际返回 PlanVO,**Q1 联调阶段 30% 时间花在前后端对齐字段**,前端经常等待后端改。
2. **OpenAPI 规范缺失**:Word 文档不能直接生成 Swagger UI / Postman 集合,**前端开发"先 mock 着" 等后端真接口,接口对齐有时差 3-5 天**。
3. **重复 method+path 不容易发现**:多个开发同时设计接口,**Q1 出现过 2 次 POST /api/user/save 被两个模块同时声明**,运行期 Spring 启动失败。
4. **Mock 服务能力缺位**:前端要等后端真接口才能开发,**Q1 平均每个接口前端等后端 1.5 天**,影响联调节奏。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 200 份接口详细设计数据,做"OpenAPI + Mock 服务"标配,前后端联调时间降 50%。

**衡量指标**:
- **AI 生成 OpenAPI Spec 准确率 ≥ 80%**(设计与最终实现一致)
- **Mock 服务启用率 ≥ 70%**(每个接口默认 mockEnabled='Y')
- **UNIQUE(method,path) 冲突拦截率 100%**(违反抛 701,Service 强校验)
- **前后端联调时间降 50%**(基线每接口 1.5 天,目标 0.75 天)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **接口压测自动化**(基于 OpenAPI 自动生成 JMeter)— 留 v0.3
- **接口安全扫描**(对 OpenAPI 做 OWASP API Security Top10 扫描)— 留 v0.5+
- **Mock 服务智能数据**(基于 schema 自动构造业务真实感数据)— 仅返回静态 mockResponse,智能数据留 testdata 模块
- **GraphQL / gRPC 支持** — 仅 REST + OpenAPI 3.0,其他规范留 openspec 模块承接
- **接口契约测试**(Pact)— 留 v0.5+
- **接口版本演进自动化**(/v1 → /v2 兼容性自动校验)— 留 v0.3

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **后端开发** | CRUD 自己负责的 apidesign | 触发 AI 生成 OpenAPI / 启 Mock 服务 |
| **前端开发** | 查看 + 评论 | 对 Mock 服务开发 / 提反馈 |
| **架构师** | 评审 + 决策 | 接口设计合规评审 |
| **测试** | 查看 | 读 OpenAPI 设计接口测试 |

### 2.2 典型场景

**S1 AI 生成 OpenAPI**(最高频)
> 李后端要设计 "POST /api/v1/irrigation/recommend 推荐接口" → 进入 API 设计菜单 → 新建 → na-path "/api/v1/irrigation/recommend" + HTTP=POST + na-desc "基于 IoT 数据推荐每日灌溉量" + 关联 archId=ARCH-12 → 点 "AI 生成 OpenAPI" → mock 输出 OpenAPI 3.0 YAML(含 request body schema、response 200/400 schema、tags、parameters)+ mockResponse JSON

**S2 唯一键冲突拦截**(关键流程)
> 王后端同时设计"GET /api/user/list" → 提交时 Service 校验 UNIQUE(project_id, method, path) → 检测到已有 GET /api/user/list → **抛 ServiceException(701)** "接口 path+method 已被 APID-15 占用,请改路径或合并"

**S3 启用 Mock 服务**(前后端并行,F3.6 联调)
> 李后端 status='00→01 评审中' → 评审通过 status='02 已确认' → 设 mockEnabled='Y' + mockResponse JSON 模板 → 前端通过 mock 服务 URL 提前开发,无需等后端真接口

**S4 评审打回**(反向边路径)
> 评审 admin 发现 "响应 schema 没定义错误情况" → 改 status='01→00 草稿'(反向边)→ 后端补 response 400/500 schema

**S5 已确认 → 编码 → apidoc 同步**(关键流程)
> status='02 已确认' → 后端实际开发 Controller → apidoc 模块从代码反向同步对外文档(详 Apidoc-PRD §1.1 区分:apidesign=设计期,apidoc=交付期)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ApiDesign (F3.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: apidesignId / apidesignNo (`APID-YYYY-NNNN`) / projectId(FK 必)/ archId(FK 可空)
- 接口定义: title / httpMethod(5 值:GET/POST/PUT/DELETE/PATCH)/ path(`/api/v1/...`)/ description / requestSchema(JSON Schema)/ responseSchema(JSON Schema)
- OpenAPI: openapiSpec(YAML 全文)
- Mock 服务: mockEnabled(Y/N)/ mockResponse(JSON 模板)
- 流程: status(4 态)/ authorUserId / reviewerUserId / aiGenerated / aiGeneratedAt

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) apidesign 行(同 arch 4 态模式):

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 评审打回 |
| 02 | 已确认 | {03 已废弃} | 终态分支;mockEnabled=Y 可启 Mock 服务 |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- 反向边 01→00 必填 reviewNote(602)
- httpMethod 字段白名单(5 值,604)
- **UNIQUE(project_id, http_method, path)**:同项目同 method+path 重复抛 ServiceException(701)
- FK 校验:projectId 必,archId 可空但若填必须存在(702)
- mockEnabled='Y' 时 mockResponse 必填(条件必填,602)

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/apidesign/ai/generate/{id}` 或 `POST /business/apidesign/ai/openapi` — 调用 §F3.3 `detail-design-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🔴 未实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F3.3 行)— 本期占位 mock。

mock 输出结构:
- openapiSpec: 标准 OpenAPI 3.0 YAML 含 request/response schema、tags、security、examples
- mockResponse JSON: 基于 path 推断的标准响应模板(列表/详情/CUD)

### 5.3 与 apidoc 区分

| 维度 | apidesign | apidoc |
|---|---|---|
| 阶段 | 研发设计期 (PRD §F3.3) | 交付发布期 (PRD §F5.4) |
| 来源 | AI 推荐 / 手工设计 | 代码注释提取 / GitLab 扫描 |
| 状态 | 4 态(草稿→评审→确认→废弃) | 3 态(草稿→已发布→已废弃) |
| 目的 | 联调前对齐 / Mock 服务 | 对外公开文档 / 在线调试 |

### 5.4 路线图

- v0.3: 真实 AI 接入 / 自动生成 Mock 智能数据
- v0.5+: 契约测试 (Pact) 集成

---

## 6. 验收标准

**PRD §F3.3 验收**:
- ⏳ **AI 生成 OpenAPI 3.0 规范**(本期 mock YAML)
- ⏳ **Mock 服务支持**(本期 mockEnabled + mockResponse 字段就位)
- ⏳ **接口设计评审流程**(本期 4 态状态机就位)

**模块特有验收**(本会话已落地):
- 4 态状态机 + 反向边 01→00 单测覆盖
- UNIQUE(project_id, http_method, path) → 701 单测覆盖
- httpMethod 5 值白名单(604)
- mockEnabled='Y' 时 mockResponse 必填(602)
- FK 校验:projectId 必、archId 可空(702)

---

## 7. 不做的事 — 详 §1.3

- 压测自动 / 安全扫描 / 智能 Mock 数据 / GraphQL/gRPC / 契约测试 / 版本演进

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Apidesign-数据库设计.md](../02-设计/Apidesign-数据库设计.md)
- API 设计: [Apidesign-API设计.md](../02-设计/Apidesign-API设计.md)
- 测试计划: [Apidesign-测试计划-2026-05-17.md](../04-测试/Apidesign-测试计划-2026-05-17.md)
- 发布计划: [Apidesign-发布计划-2026-05-17.md](../05-上线/Apidesign-发布计划-2026-05-17.md)
- 原型: [apidesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidesign.html)
- AgriAI PRD: [§F3.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Arch-PRD.md](Arch-PRD.md)(apidesign.archId FK)/ [Apidoc-PRD.md](Apidoc-PRD.md)(交付期姊妹模块)

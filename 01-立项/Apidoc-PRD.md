# PRD: ApiDoc 模块 — API 文档 (F5.4)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F5.4 (AgriAI-PLM-完整PRD文档.md L409-412 API 文档 + 从代码同步) |
| 原型 HTML | [apidoc.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidoc.html) (HTTP 方法列 + 接口路径 + 版本号 + autoExtracted) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | UNIQUE(method,path,version) 设计 |
| 关联 OKR | _2026 Q2-O5-KR4: ApiDoc 模块上线,API 文档与代码一致率 ≥ 95%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "ApiDoc (F5.4)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前对外 API 文档走 Swagger UI(自动) + Word 文档(手写),4 个具体问题:

1. **代码改了文档没改**:开发改 Controller 加字段 / 改 path,**Q1 出现过 6 次 "Swagger 显示一回事,代码实际另一回事"**,客户开发投诉。
2. **版本管理混乱**:API v1.0 / v1.1 / v2.0 都在同一个 Swagger,**客户不知道用哪版**,经常用过时的字段名。
3. **唯一键约束缺位**:`UNIQUE(method,path,version)` 没强约束,**理论上同一接口可重复入库**,导致客户调试 confusion。
4. **在线调试能力弱**:Swagger UI 的"Try it"只能 GET / 简单 POST,**复杂场景(文件上传 / 流式响应 / 鉴权)调试体验差**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 200 个对外 API 文档,做"从代码自动同步 + 版本管理 + 唯一键强约束 + 在线调试"四件套。

**衡量指标**:
- **API 文档与代码一致率 ≥ 95%**(autoExtracted='Y' 比例 + 同步频次)
- **UNIQUE(method,path,version) 冲突拦截 100%**(违反抛 701)
- **客户调试错误率降 60%**(文档准确度 + 版本明确)
- **同步时效 ≤ 24h**(代码 merge 后 24h 内 lastSyncedAt 更新)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **API 变更订阅推送**(订阅了的客户在 API 改时收 IM 通知)— PRD §F5.4 提到但留 v0.3
- **API 文档评论 / 反馈** — 留 v0.3
- **API 文档多语言**(英文)— 仅简体中文,留 v0.5+
- **API 文档访问统计** — 留 v0.3 走 Analytics
- **客户 token / API key 管理** — 留 v0.3
- **API 文档与 OpenSpec 双向同步** — 留 v0.5+

### 1.4 与 apidesign 区分

详 [Apidesign-PRD §5.3](Apidesign-PRD.md):
- **apidesign** = 研发设计期(PRD §F3.3,设计草稿 → 评审 → 已确认)
- **apidoc** = 交付发布期(PRD §F5.4,从代码注释提取的对外文档)

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **后端开发** | CRUD 自己负责的 apidoc | 同步代码 / 维护文档 |
| **技术写作** | 协作编辑 | 微调 description |
| **管理员** | 全 CRUD + 发布 | 评审 + 版本管理 |
| **客户开发** | 仅查看 + 在线调试 | 读 OpenAPI + Try it |

### 2.2 典型场景

**S1 从代码自动同步**(最高频,F5.4 核心)
> CI 流水线在代码 merge 到 main 后触发 → 扫描所有带 @RestController + @Operation 注解的方法 → POST /business/apidoc/ai/extract → 对比 tb_apidoc 现有记录:
> - 新增接口 → 自动 INSERT + autoExtracted='Y' + lastSyncedAt=NOW()
> - 变更接口(同 method+path+version 但 schema 变了)→ 自动 UPDATE
> - 删除接口(代码已删但表中还在)→ 标 status='02 已废弃'

**S2 手动新建文档**(高频)
> 后端开发新发 v1.0 接口 → 进入 API 文档菜单 → 新建 → title "灌溉推荐接口" + httpMethod="POST" + path="/api/v1/irrigation/recommend" + version="v1.0" + description + 粘 OpenAPI 3.0 spec + sourceClass="IrrigationController" + sourceMethod="recommend" + autoExtracted='N' → 保存 → status='00 草稿'

**S3 唯一键冲突拦截**(关键流程)
> 重复创建 POST /api/v1/irrigation/recommend v1.0 → Service 校验 UNIQUE(project_id, http_method, path, version) → 抛 ServiceException(701) "已存在,请改 version 或合并"

**S4 发布对外**(终态)
> 评审通过 → status='00→01 已发布' → 客户开发可访问 Swagger UI + 在线调试

**S5 版本演进**(终态)
> 接口升级 v1.0 → v2.0,新建 v2.0 文档(同 method+path 不同 version 不冲突),老 v1.0 标 status='01→02 已废弃' 保留可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ApiDoc (F5.4)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: apidocId / apidocNo (`API-YYYY-NNNN`) / projectId(FK 必)
- 接口定义: title / httpMethod(5 值:GET/POST/PUT/DELETE/PATCH)/ path / description
- Schema: requestSchema / responseSchema(JSON Schema)/ openapiSpec(OpenAPI 3.0 全文)
- 源码追溯: sourceClass / sourceMethod(F5.4 从代码提取)
- 版本: version(`v1.0` 等)
- 同步: lastSyncedAt / autoExtracted(Y/N)
- 流程: status(3 态)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) apidoc 行:3 态单向。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始 |
| 01 | 已发布 | {02 已废弃} | 对客户可见 + 在线调试 |
| 02 | 已废弃 | {} | 终态;新版本继任 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- httpMethod 字段白名单(5 值,604)
- **UNIQUE(project_id, http_method, path, version)**:违反抛 701
- version 必填(602)
- autoExtracted='Y' 时由系统自动同步,前端不可手改
- lastSyncedAt 服务端计算

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/apidoc/ai/extract` — 调用 §F5.4 `api-doc-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 字段已留位 `autoExtracted`(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F5.4 行)— 本期占位 mock。

mock 输出策略:
- 输入:projectId + GitLab 仓库 URL + 分支
- mock 扫描 Controller 注解 → 输出新增/变更/删除 接口列表
- 自动 INSERT / UPDATE / 标 02 已废弃
- 同步频次:每次 main merge 触发(由 CI 调用)

### 5.3 路线图

- v0.3: 真实 GitLab/GitHub 集成 + AI 智能识别字段含义
- v0.3: 变更订阅推送(PRD §F5.4 未实现部分)
- v0.5+: 评论反馈 / 多语言 / 访问统计

---

## 6. 验收标准

**PRD §F5.4 验收**:
- ⏳ **API 文档从代码自动同步**(本期 mock,真实 GitLab 集成留 v0.3)
- ⏳ **OpenAPI 3.0 规范**(本期字段就位)
- ⏳ **变更订阅推送**(本期未实现,留 v0.3)

**模块特有验收**(本会话已落地):
- 3 态状态机合法转换单测覆盖
- httpMethod 5 值白名单(604)
- UNIQUE(project_id, http_method, path, version) → 701 单测覆盖
- version 必填(602)
- autoExtracted='Y' 时前端不可手改 / lastSyncedAt 服务端计算

---

## 7. 不做的事 — 详 §1.3

- 变更订阅 / 评论反馈 / 多语言 / 访问统计 / token 管理 / OpenSpec 同步

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Apidoc-数据库设计.md](../02-设计/Apidoc-数据库设计.md)
- API 设计: [Apidoc-API设计.md](../02-设计/Apidoc-API设计.md)
- 测试计划: [Apidoc-测试计划-2026-05-17.md](../04-测试/Apidoc-测试计划-2026-05-17.md)
- 发布计划: [Apidoc-发布计划-2026-05-17.md](../05-上线/Apidoc-发布计划-2026-05-17.md)
- 原型: [apidoc.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidoc.html)
- AgriAI PRD: [§F5.4 L409-412](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Apidesign-PRD.md](Apidesign-PRD.md)(设计期姊妹) / [Openspec-PRD.md](Openspec-PRD.md)

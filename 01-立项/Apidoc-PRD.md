# PRD: ApiDoc 模块 — 接口文档 (F5.4)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F5.4 + 原型 apidoc.html) |
| 作者 | Wjl |
| PRD § | F5.4 (AgriAI-PLM-完整PRD文档.md §F5.4 接口文档) |
| 原型 HTML | [apidoc.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidoc.html) (接口列表 + 详情 + autoExtracted) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "ApiDoc (F5.4)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(对外接口文档与代码注释双轨脱节 / 多版本接口同时在用难管理 / 客户调用文档过时常被吐槽 / 与 apidesign 模块定位易混淆)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F5.4 验收标准 + 模块特有衡量指标(自动提取覆盖率 / 文档更新延迟)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实 GitLab 扫码自动提取** — 仅 autoExtracted 字段留位,扫码留 v0.5+
- **客户调用统计** — 仅文档,调用统计留 v0.5+
- **客户 OAuth/AKSK 接入文档** — 仅基础说明,认证细节留 v0.5+
- **多版本接口对比 Diff** — 单版本,Diff 留 v0.3

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:文档工程师 / API 维护者 / 客户开发 / DevOps。

### 2.2 典型场景

**S1 AI 自动提取接口文档**(最高频)
<待人工填写>:1 段叙述,从代码注释 / OpenAPI YAML 自动提取 → autoExtracted='Y'

**S2 文档发布**(关键流程)
<待人工填写>:01 已发布,客户可调用

**S3 文档归档**(终态)
<待人工填写>:02 已归档,旧版本可查

**S4 与 apidesign 区分**(关键)
<待人工填写>:apidesign = 设计期(草稿/评审/确认);apidoc = 交付发布期(从代码提取的对外文档)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ApiDoc (F5.4)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-apidoc.sql):
- 基础: apidocId / apidocNo(API-YYYY-NNNN)/ projectId(FK)
- 用户输入: title / httpMethod / path / version / description / requestSample / responseSample
- AI 输出: openapiSpec(YAML)/ autoExtracted / lastExtractedAt
- 流程: status(3 态) / authorUserId

**唯一键**: (method, path, version) 唯一

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) apidoc 行:`00→01→02` (3 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始状态 |
| 01 | 已发布 | {02 已归档} | 客户可调用 |
| 02 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- **(method, path, version) 唯一约束**(701)
- httpMethod 字典白名单(604)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/apidoc/ai/extract — Dify 工作流 api-doc-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 字段已留位(autoExtracted),Dify 实接入留 v0.5+。本期占位 mock(从手填注释段落生成 OpenAPI 段)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4。

---

## 6. 验收标准

**PRD §F5.4 验收**:
- ⏳ 自动提取覆盖率 ≥ 80%(本期 mock 占位)
- ⏳ 文档更新延迟 < 1 天

**模块特有验收**:
<待人工填写>:E2E 测试 / (method, path, version) 唯一键 / httpMethod 字典 / FK 校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Apidoc-数据库设计.md](../02-设计/Apidoc-数据库设计.md)
- API 设计: [Apidoc-API设计.md](../02-设计/Apidoc-API设计.md)
- 测试计划: [Apidoc-测试计划-2026-05-17.md](../04-测试/Apidoc-测试计划-2026-05-17.md)
- 发布计划: [Apidoc-发布计划-2026-05-17.md](../05-上线/Apidoc-发布计划-2026-05-17.md)
- 原型: [apidoc.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidoc.html)
- AgriAI PRD: [§F5.4](../prd和原型/AgriAI-PLM-完整PRD文档.md)

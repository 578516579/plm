# PRD: Document 模块 — 文档中心 (F5.5 调整版)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F5.5 调整版 + sql/business-document.sql) |
| 作者 | Wjl |
| PRD § | F5.5 调整版承担(原 PRD §F5.5 知识库,本期降级为通用文档中心) |
| 原型 HTML | (合并 5 stub 无独立 HTML,产出 5.1 / 5.2 / 5.3 / 5.4 共用入口) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Document (F5.5)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(项目内文档散在多模块 / 跨类型搜索难统一 / 与 F5.1-F5.4 各模块定位有重叠 / 知识库 v0.5+ 落地前的过渡承载缺位)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2 + 模块特有衡量指标(文档类型覆盖度 / 搜索响应时间)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **AgriKB 知识图谱** — 留 v0.5+(F5.5 原版能力)
- **跨项目知识检索** — 仅单 projectId,跨项目留 v0.5+
- **文档版本对比 Diff** — 仅 version 字段,Diff 留 v0.3
- **AI 文档摘要 / 问答** — 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:全角色读 / PM 维护 / 评审 admin。

### 2.2 典型场景

**S1 通用文档录入**(最高频)
<待人工填写>:1 段叙述,docType 多类型支持(prd / design / api / manual / report / runbook 等)+ 标签 + 链接

**S2 跨模块文档检索**(高价值)
<待人工填写>:title / tags / docType 多维过滤

**S3 文档归档**(终态)
<待人工填写>:02 已归档,保留历史可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Document"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-document.sql):
- 基础: documentId / documentNo(DOC-<TYPE>-YYYY-NNNN)/ projectId(FK)
- 用户输入: title / docType(枚举)/ tags(CSV)/ content / fileUrl / version
- 流程: status(3 态) / authorUserId

---

## 4. 状态机

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已发布} | 默认初始状态 |
| 01 | 已发布 | {02 已归档} | — |
| 02 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- docType 字典白名单(604)
- FK projectId 校验(702)

---

## 5. AI 能力

### 5.1 AI 端点
(本模块当前阶段无 AI 端点。AI 文档摘要 / 问答留 v0.5+。)

### 5.2 当前阶段实现
n/a

### 5.3 mock 输出 / Dify 工作流
n/a — 留 v0.5+ AgriKB 接入

---

## 6. 验收标准

**PRD §F5.5 调整版验收**:
- ⏳ docType 多类型覆盖
- ⏳ 跨类型检索响应 < 1s

**模块特有验收**:
<待人工填写>:E2E 测试 / docType 字典 / FK 校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Document-数据库设计.md](../02-设计/Document-数据库设计.md)
- API 设计: [Document-API设计.md](../02-设计/Document-API设计.md)
- 测试计划: [Document-测试计划-2026-05-17.md](../04-测试/Document-测试计划-2026-05-17.md)
- 发布计划: [Document-发布计划-2026-05-17.md](../05-上线/Document-发布计划-2026-05-17.md)
- AgriAI PRD: [§F5.5(调整版承担)](../prd和原型/AgriAI-PLM-完整PRD文档.md)

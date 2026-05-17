# PRD: Release 模块 — 发布管理 (DevOps 扩展)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD DevOps 扩展 + 原型 release.html) |
| 作者 | Wjl |
| PRD § | DevOps 扩展(AgriAI-PLM-完整PRD文档.md DevOps 子域 发布管理) |
| 原型 HTML | [release.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/release.html) (发布列表 + 回滚 modal) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Release (DevOps)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(发布版本散在 IM / 回滚理由散落 / 多环境(test/staging/prod)发布混乱 / 5 态状态机里"回滚"与"废弃"易混淆)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 DevOps 扩展验收标准 + 模块特有衡量指标(发布成功率 / 回滚率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实部署调度** — 仅状态登记,实际部署留 v0.5+(对接 pipeline)
- **多环境蓝绿 / 金丝雀** — 仅状态,蓝绿留 v0.5+(对接 feature-flag)
- **发布审批多级流** — 仅单审批,多级留 v0.3
- **AI 回滚原因归类** — 仅文本 rollbackReason,AI 归类留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:DevOps / 发布经理 / PM / 评审 admin。

### 2.2 典型场景

**S1 发布计划登记**(最高频)
<待人工填写>:1 段叙述,引原型字段(releaseVersion / releaseEnvironment / releaseNote / scheduledAt / 关联 testreport / pipelineId)

**S2 发布执行**(关键流程)
<待人工填写>:00→01→02 已发布

**S3 回滚**(关键反向场景)
<待人工填写>:02→03 已回滚,**必填 rollbackReason**(602)

**S4 废弃**(终态)
<待人工填写>:04 已废弃,版本不再使用

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Release (DevOps)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT 与 sql/business-release.sql):
- 基础: releaseId / releaseNo(REL-YYYY-NNNN)/ projectId(FK)
- 用户输入: releaseVersion / releaseEnvironment / releaseNote / scheduledAt
- 关联: testreportId(可选 FK)/ pipelineId(可选 FK)
- 派生: actualReleasedAt
- 流程: status(5 态)/ rollbackReason(条件必填)/ authorUserId / approverUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) release 行(5 态含 03 回滚 / 04 废弃)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已审批} | 默认初始状态 |
| 01 | 已审批 | {02 已发布, 04 已废弃} | 评审通过 |
| 02 | 已发布 | {03 已回滚, 04 已废弃} | 上线运行 |
| 03 | 已回滚 | {} | 终态,**必填 rollbackReason** |
| 04 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- **02→03 必填 rollbackReason**(602)
- releaseEnvironment 字典白名单(test/staging/prod)抛 604

---

## 5. AI 能力

### 5.1 AI 端点
(本模块当前阶段无 AI 端点。AI 回滚原因归类 / 发布决策辅助留 v0.5+。)

### 5.2 当前阶段实现
n/a

### 5.3 mock 输出 / Dify 工作流
n/a

---

## 6. 验收标准

**DevOps 扩展验收**:
- ⏳ 5 态状态机完整
- ⏳ 回滚理由强校验

**模块特有验收**:
<待人工填写>:E2E 测试 / 5 态合法/非法转换 / rollbackReason 602 / 字典白名单 / FK 校验。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Release-数据库设计.md](../02-设计/Release-数据库设计.md)
- API 设计: [Release-API设计.md](../02-设计/Release-API设计.md)
- 测试计划: [Release-测试计划-2026-05-17.md](../04-测试/Release-测试计划-2026-05-17.md)
- 发布计划: [Release-发布计划-2026-05-17.md](../05-上线/Release-发布计划-2026-05-17.md)
- 原型: [release.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/release.html)
- AgriAI PRD: [DevOps 扩展](../prd和原型/AgriAI-PLM-完整PRD文档.md)

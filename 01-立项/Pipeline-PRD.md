# PRD: Pipeline 模块 — CI/CD 流水线 (DevOps 扩展)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD DevOps 扩展 + 原型 pipeline.html) |
| 作者 | Wjl |
| PRD § | DevOps 扩展(AgriAI-PLM-完整PRD文档.md DevOps 子域) |
| 原型 HTML | [pipeline.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/pipeline.html) (流水线列表 + Run 卡片 + YAML 编辑) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Pipeline (DevOps)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(CI/CD 工具(Jenkins/GitLab/GitHub/Gitea)各团队不一致 / 流水线 YAML 散在 / 触发方式手工 / 成功率统计缺位)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 DevOps 扩展验收标准 + 模块特有衡量指标(流水线成功率 ≥ 85% / 国产化 Gitea 占比)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实流水线执行调度** — 仅 mock trigger(85% 成功率),实际执行留 v0.5+
- **流水线 YAML AI 生成** — 仅人工写,AI 留 v0.5+
- **多分支并行执行** — 单分支,并行留 v0.3
- **流水线模板复用** — 单条目,模板库留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:DevOps / 开发 / SRE / PM。

### 2.2 典型场景

**S1 流水线注册**(最高频)
<待人工填写>:1 段叙述,引原型字段(pipelineName / repoName / repoBranch / cicdTool / triggerType / cronExpr / yamlContent)

**S2 触发执行 + 统计**(高价值)
<待人工填写>:trigger 按钮 → mock 85% 成功率 → totalRuns + successCount 累加 + successRate 重算

**S3 国产化 Gitea**(农业特色)
<待人工填写>:cicdTool 含 gitea(信创农业项目场景)

**S4 cron 触发约束**(关键校验)
<待人工填写>:triggerType='cron' 时 cronExpr 必填(602)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Pipeline (DevOps)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: pipelineId / pipelineNo(PIPE-YYYY-NNNN)/ projectId(可选 FK)
- 用户输入: pipelineName / repoName / repoBranch / cicdTool / triggerType / cronExpr / yamlContent
- 派生统计: totalRuns / successCount / successRate / lastRunStatus / lastRunAt
- 流程: status(2 态) / authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) pipeline 行:`00↔01` (启用/停用)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 启用 | {01 停用} | 默认初始状态 |
| 01 | 停用 | {00 启用} | 反向边互转 |

**特殊规则**:
- triggerType='cron' 时 cronExpr 必填(602)
- cicdTool / triggerType / lastRunStatus 字典白名单(604)

---

## 5. AI 能力

### 5.1 AI 端点
(本模块当前阶段无 AI 端点。AI 流水线 YAML 生成留 v0.5+。)

### 5.2 当前阶段实现
n/a

### 5.3 mock 输出 / Dify 工作流
trigger 触发 mock 85% 成功率,累加 totalRuns / successCount + 重算 successRate。

---

## 6. 验收标准

**DevOps 扩展验收**:
- ⏳ 4 工具(Jenkins/GitLab/GitHub/Gitea)支持
- ⏳ 流水线成功率 ≥ 85%

**模块特有验收**:
<待人工填写>:E2E 测试 / cron 表达式格式校验 / 字典白名单 / 触发统计单测。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Pipeline-数据库设计.md](../02-设计/Pipeline-数据库设计.md)
- API 设计: [Pipeline-API设计.md](../02-设计/Pipeline-API设计.md)
- 测试计划: [Pipeline-测试计划-2026-05-17.md](../04-测试/Pipeline-测试计划-2026-05-17.md)
- 发布计划: [Pipeline-发布计划-2026-05-17.md](../05-上线/Pipeline-发布计划-2026-05-17.md)
- 原型: [pipeline.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/pipeline.html)
- AgriAI PRD: [DevOps 扩展](../prd和原型/AgriAI-PLM-完整PRD文档.md)

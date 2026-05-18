# PRD: Pipeline 模块 — CI/CD 流水线 (DevOps)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | DevOps(AgriAI-PLM-完整PRD文档.md DevOps 章节 CI/CD 流水线) |
| 原型 HTML | [pipeline.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/pipeline.html) (流水线列表 + 总数/成功数/成功率统计) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | cron 触发必填 cronExpr (602) 决策 |
| 关联 OKR | _2026 Q2-O6-KR3: Pipeline 模块上线,流水线成功率 ≥ 85%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Pipeline (DevOps)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前 CI/CD 走"每项目自己的 GitLab Runner",4 个具体问题:

1. **流水线散落无统一管理**:GitLab CI / GitHub Actions / Jenkins 各项目用不同工具,**管理员无法一眼看"PLM 一共有几条流水线 / 各成功率"**。
2. **触发方式不一致**:有的项目 push 触发 / 有的 cron 定时 / 有的纯手动,**没有统一记录**,出问题排查要逐项目翻 CI 配置。
3. **国产化 CI 工具适配缺位**:Gitea 在国产化场景流行(国产 GitLab 替代),**但当前没记录过哪些项目用 Gitea**。
4. **执行结果与成功率统计缺位**:每条 pipeline 跑了多少次 / 成功多少 / 成功率多少,**只能逐 CI 工具看,无跨工具聚合**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 50 条 Pipeline 数据,做"4 工具支持 + 4 触发方式 + 成功率统计"。

**衡量指标**:
- **流水线成功率 ≥ 85%**(成功 / 总执行)
- **4 工具覆盖率 ≥ 80%**(jenkins/gitlab/github/gitea 至少 3 种被使用)
- **触发方式记录完整率 100%**(triggerType 必填)
- **统一管理覆盖率 ≥ 90%**(各项目流水线必入 tb_pipeline)
- **国产化适配率 ≥ 30%**(cicdTool=gitea 至少 30%)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **真实 CI/CD 工具集成**(实际跑 Jenkins/GitLab job)— 仅注册管理 + mock 触发,真实集成留 v0.3
- **YAML 流水线编辑器**(类 Codecov visual editor)— 仅 yamlContent 字符串字段,编辑器留 v0.5+
- **流水线编排**(A 完成才触发 B)— 留 v0.3
- **执行日志详情** — 仅记录 lastRunStatus,详细日志留 v0.3
- **流水线告警**(失败时 IM 通知)— 留 v0.3 走告警平台
- **CI/CD 成本核算** — 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **DevOps** | CRUD 自己负责的 pipeline | 注册 / 启用 / 触发 |
| **开发** | 查看 + 触发(manual) | 手动触发 build |
| **管理员** | 全 CRUD | 跨项目流水线总览 |

### 2.2 典型场景

**S1 注册新流水线**(高频)
> DevOps 给某项目注册新流水线 → 进入 Pipeline 菜单 → 新建 → pipelineName "AgriPLM Backend Build" + repoName="agriplm/plm-backend" + repoBranch="main" + cicdTool="gitlab"(4 值字典)+ triggerType="push"(4 值:manual/push/cron/tag) + yamlContent(粘 .gitlab-ci.yml 全文)→ status='00 启用'

**S2 cron 触发必填 cronExpr**(关键流程,业务硬规则)
> DevOps 选 triggerType="cron" → 必填 cronExpr "0 2 * * *"(每天 2:00)→ **Service 校验:triggerType=cron 时 cronExpr 非空(602)**

**S3 手动触发**(高频)
> 开发想验证 main 最新代码 → 点 "立即触发" → POST /business/pipeline/trigger/{id} → mock 模拟执行(85% 成功率)→ totalRuns++ / successCount++(如成功) / successRate 重算 / lastRunStatus + lastRunAt 更新

**S4 流水线停用**(关键流程)
> 某流水线已废弃 → status='00→01 停用' → cron / push 不再触发;但保留历史可查

**S5 国产化适配**(关键流程)
> 政府客户要求国产化 → DevOps 选 cicdTool="gitea"(开源国产化 GitLab 替代)+ triggerType="push"

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Pipeline (DevOps)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: pipelineId / pipelineNo (`PIPE-YYYY-NNNN`) / projectId(FK 可空)
- 流水线定义: pipelineName / repoName / repoBranch(默认 main)
- 工具与触发: cicdTool(4 值)/ triggerType(4 值)/ cronExpr(triggerType=cron 必填)
- 配置: yamlContent
- 执行结果: lastRunStatus(4 值)/ lastRunAt / totalRuns / successCount / successRate
- 流程: status(2 态启用/停用)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) pipeline 行:2 态启用/停用。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 启用 | {01 停用} | 默认初始 |
| 01 | 停用 | {00 启用} | 反向可重启 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- cicdTool 4 值字典白名单(jenkins/gitlab/github/gitea,604)
- triggerType 4 值字典白名单(manual/push/cron/tag,604)
- **业务硬规则**:triggerType='cron' 时 cronExpr 必填(602)
- lastRunStatus 4 值字典白名单(success/failed/running/skipped,604)
- successRate 服务端计算(successCount / totalRuns × 100),前端不可写
- FK 校验:projectId 可空 / 若填必须存在(702)

---

## 5. AI 能力

### 5.1 业务入口

`POST /business/pipeline/trigger/{id}` — 模拟执行(85% 成功率)+ 累加统计。

### 5.2 当前实现

- 触发端点:mock 调用,85% 概率成功 → totalRuns++ / successCount++ / lastRunStatus='success'
- 15% 概率失败 → totalRuns++ / lastRunStatus='failed'
- successRate 重算 + lastRunAt=NOW()

**本期无 AI 端点**(不接 AI 推荐工具 / AI 配置生成等)。

### 5.3 路线图

- v0.3: 真实 Jenkins/GitLab API 集成
- v0.3: 失败告警 / 日志详情 / pipeline 编排
- v0.5+: AI 推荐 yaml 配置

---

## 6. 验收标准

**DevOps Pipeline 验收**:
- ⏳ **4 工具支持**(jenkins/gitlab/github/gitea 字典就位)
- ⏳ **4 触发方式**(manual/push/cron/tag 字典就位)
- ⏳ **成功率统计**(totalRuns/successCount/successRate 字段就位)

**模块特有验收**(本会话已落地):
- 2 态状态机合法转换单测覆盖
- cicdTool / triggerType / lastRunStatus 字典白名单(604)
- **cron 触发必填 cronExpr** 业务硬规则单测覆盖(602)
- successRate 服务端计算
- trigger 端点 mock 85% 成功率 + 累加统计单测覆盖

---

## 7. 不做的事 — 详 §1.3

- 真实 CI 集成 / YAML 编辑器 / 编排 / 日志详情 / 告警 / 成本

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Pipeline-数据库设计.md](../02-设计/Pipeline-数据库设计.md)
- API 设计: [Pipeline-API设计.md](../02-设计/Pipeline-API设计.md)
- 测试计划: [Pipeline-测试计划-2026-05-17.md](../04-测试/Pipeline-测试计划-2026-05-17.md)
- 发布计划: [Pipeline-发布计划-2026-05-17.md](../05-上线/Pipeline-发布计划-2026-05-17.md)
- 原型: [pipeline.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/pipeline.html)
- AgriAI PRD: [DevOps](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html)
- 关联模块: [Release-PRD.md](Release-PRD.md)(发布走 Pipeline)/ [FeatureFlag-PRD.md](FeatureFlag-PRD.md)

# PRD: Release 模块 — 发布管理 (DevOps 扩展)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | DevOps 扩展(AgriAI-PLM-完整PRD文档.md DevOps 章节 发布管理 + DORA 4 指标) |
| 原型 HTML | [release.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/release.html) (modal-newrelease 4 策略 + DORA 卡 + AI 评审分) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | D1 strategy 字典 4 值(对齐原型新增 `direct_replace`) + D2 rollbackReason 条件必填 |
| 关联 OKR | _2026 Q2-O7-KR1: Release 模块上线,发布回滚率 ≤ 10%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Release (DevOps 扩展)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前发布管理走"飞书工单 + nginx 流量切",4 个具体问题:

1. **发布记录散落无追溯**:某项目 v1.2 vs v1.3 发布的差异 / 实际发布时间 / 回滚情况,**飞书消息翻 30 分钟才能拼完整时间线**。
2. **发布策略选型混乱**:蓝绿 / 金丝雀 / 滚动 / 直接替换 4 种策略,**项目间用法不一致**;原型支持 4 选项但旧 SQL 字典只有 3 个值(漏 direct_replace)。
3. **回滚原因不结构化**:rollback 时飞书消息 "出问题了" 一句话,**Q1 累计 8 次回滚没填具体原因**,根因分析无法做。
4. **DORA 4 指标分散**:每次发布的 deployment_frequency / lead_time / mttr / change_fail_rate **没在 release 表沉淀,只能事后从 CI 数据反推**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 100 次 release 数据,做"4 策略 + DORA 4 指标 + 回滚原因结构化 + AI 评审"四件套,发布回滚率 ≤ 10%。

**衡量指标**:
- **发布回滚率 ≤ 10%**(03 已回滚 / 全部发布)
- **回滚原因结构化率 100%**(rollbackReason 必填,602)
- **4 发布策略 100% 覆盖**(blue_green / canary / rolling / direct_replace)
- **AI 评审 score < 7 阻断发布**(direct_replace 风险拦截)
- **DORA 4 指标快照率 100%**(每次发布都入 release 表)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **真实 K8s 发布执行**(实际跑 helm install / argocd sync)— 仅记录管理 + mock 评审,真实执行留 v0.3
- **发布编排**(发布顺序:数据库 → 后端 → 前端)— 留 v0.3
- **发布前自动化测试触发**(发布前自动跑 E2E)— 留 v0.3 走 pipeline 联动
- **发布后健康检查自动化** — 留 v0.3
- **客户视角发布日志门户** — 留 v0.5+
- **发布数据自动同步 DORA 模块** — 仅 release 表内字段,跨模块聚合留 v0.3

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **DevOps / SRE** | CRUD 自己负责的 release | 创建 / 执行 / 回滚 |
| **管理员** | 全 CRUD + 决策 | 评审 AI score / 决定 direct_replace |
| **PM** | 查看 + 评论 | 跟踪发布进度 |
| **客户开发** | 查看 已发布 release | 知道当前线上版本 |

### 2.2 典型场景

**S1 创建发布计划**(高频)
> SRE 准备发 v2.2.0 → 进入 Release 菜单 → 新建 → version="v2.2.0" + projectId + sprintId="SPR-4" + strategy="canary"(4 值字典,本会话补 `direct_replace`)+ environment="prod" + releaseNotes(Markdown)+ plannedAt → status='00 计划中'

**S2 strategy 4 值字典(D1 决策)**
> 4 个策略:
> - **blue_green** 蓝绿 — 全切流量,影响最小但资源开销 2x
> - **canary** 金丝雀(10%) — 灰度,风险最小
> - **rolling** 滚动更新 — 资源开销小,但灰度粒度粗
> - **direct_replace** 直接替换 — 停机替换,风险最高 → AI 评审 score < 7 阻断

**S3 发布执行 → 已发布**(关键流程)
> status='00→01 发布中' → 执行 K8s 滚动 / helm 升级 → 成功 → status='01→02 已发布' + **自动填 releasedAt=NOW()** + 填 DORA 4 指标(deploymentFrequency/leadTimeHours/mttrMinutes/changeFailureRate)

**S4 发布回滚(关键反向边,D2)**
> 发布 30 分钟后线上 CPU 告警 → SRE 决定回滚 → status='01→03 已回滚' 或 status='02→03 已回滚' → **Service 校验:必填 rollbackReason(602)** → SRE 填 "v2.2.0 引入内存泄漏导致 OOM" → rollbackAt 自动填

**S5 AI 评审 + direct_replace 拦截**(关键流程)
> direct_replace 发布前 → 触发 AI 评审 → score=6.2(< 7)→ 提示 "AI 评审建议改用 canary,score=6.2 不足 7" → admin 决策是否强行执行(本期不强制阻断,留 v0.3)

**S6 发布废弃**(终态)
> 老 release 计划已经被新版本替代 → status='00→04 已废弃'

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Release (DevOps 扩展)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: releaseId / releaseNo (`REL-YYYY-NNNN`) / version / projectId(FK 必)/ sprintId(FK 可空)
- 发布配置: strategy(4 值字典)/ environment(默认 prod)/ releaseNotes
- 时间戳: plannedAt / releasedAt(01→02 自动填)/ rollbackAt(*→03 自动填)
- 回滚: rollbackReason(status=03 必填)
- DORA: deploymentFrequency / leadTimeHours / mttrMinutes / changeFailureRate
- AI 评审: aiReviewScore(0-10)/ aiReviewNotes
- 流程: status(5 态)/ releasedByUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) release 行:5 态含 03 回滚分支。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 计划中 | {01 发布中, 04 已废弃} | 默认初始 |
| 01 | 发布中 | {02 已发布, 03 已回滚} | 发布过程;失败可直接回滚 |
| 02 | 已发布 | {03 已回滚, 04 已废弃} | 终态分支;后期可回滚 |
| 03 | 已回滚 | {04 已废弃} | rollbackReason 必填 |
| 04 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- strategy 4 值字典白名单(blue_green/canary/rolling/direct_replace,604)
- **业务硬规则 602 rollbackReason 必填**:进入 03 已回滚必须 `rollbackReason` 非空
- 01→02 自动填 releasedAt=NOW()
- *→03 自动填 rollbackAt=NOW()
- **UNIQUE(project_id, version)**:同项目同版本号禁重复,701
- **UNIQUE(release_no)**:release_no 全局唯一
- FK 校验:projectId 必,sprintId 可空但若填必须存在(702)

---

## 5. AI 能力

### 5.1 AI 评审

`aiReviewScore` / `aiReviewNotes` 由 AI 评审输出。本期 mock:
- strategy='direct_replace' → score 5.5-6.5(高风险)
- strategy='canary' → score 7.5-9.0(低风险)
- strategy='blue_green' → score 8.0-9.5(最稳)
- strategy='rolling' → score 7.0-8.5

### 5.2 当前阶段实现

mock 已实现 — 本期无独立 AI 端点,aiReview 在创建 release 时由 Service 同步计算填入(后续接 Dify 真实评审)。

### 5.3 路线图

- v0.3: 真实 AI 接入 / 真实 K8s 发布执行
- v0.3: 发布编排 / 发布前 E2E 触发 / 发布后健康检查
- v0.5+: 客户门户

---

## 6. 验收标准

**DevOps Release 验收**:
- ⏳ **4 发布策略**(本会话 D1 补 direct_replace)
- ⏳ **DORA 4 指标快照**(本期字段就位)
- ⏳ **回滚原因结构化**(D2 rollbackReason 条件必填)
- ⏳ **AI 评审分 0-10**(本期 mock)

**模块特有验收**(本会话已落地):
- 5 态状态机合法转换 + 03 回滚 / 04 废弃 单测覆盖
- strategy 4 值字典白名单(604)— D1 补 direct_replace
- **rollbackReason 进入 03 必填**(602)— D2 决策
- UNIQUE(project_id, version) → 701
- UNIQUE(release_no) → 701
- 01→02 releasedAt / *→03 rollbackAt 自动填
- FK 校验:projectId 必、sprintId 可空(702)

---

## 7. 不做的事 — 详 §1.3

- 真实 K8s 执行 / 编排 / 自动 E2E 触发 / 自动健康检查 / 客户门户 / 跨模块同步

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Release-数据库设计.md](../02-设计/Release-数据库设计.md)
- API 设计: [Release-API设计.md](../02-设计/Release-API设计.md)
- 测试计划: [Release-测试计划-2026-05-17.md](../04-测试/Release-测试计划-2026-05-17.md)
- 发布计划: [Release-发布计划-2026-05-17.md](../05-上线/Release-发布计划-2026-05-17.md)
- 原型: [release.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/release.html)
- AgriAI PRD: [DevOps](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html)
- 关联模块: [Sprint-PRD.md](Sprint-PRD.md)(release.sprintId FK)/ [Pipeline-PRD.md](Pipeline-PRD.md)(发布执行)/ [FeatureFlag-PRD.md](FeatureFlag-PRD.md)/ [Dora-PRD.md](Dora-PRD.md)(DORA 4 指标同源)

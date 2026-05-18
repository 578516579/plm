# PRD: Defect 模块 — 缺陷管理 (F4.6)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 (实质内容,2026-05-17 由 Claude 基于 PRD-MAPPING §2 + AgriAI PRD §F4.6 + 原型 defects.html + ADR-D 决策生成) |
| 作者 | Wjl |
| PRD § | F4.6 (AgriAI-PLM-完整PRD文档.md L367-371) |
| 原型 HTML | [defects.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/defects.html) (modal-newdefect: L167, modal-defectedit: L329-345 含 dem-module/dem-status 4 选项) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | [ADR-D Option A](../99-跨阶段/proposals/0302-adr-d-defect-state-machine.md) 状态机 4 主态 + 反向边 03→00 重开 |
| 关联 OKR | _2026 Q2-O3-KR1: PLM 缺陷模块上线,P0 缺陷 24h 响应率 100%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Defect (F4.6)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前缺陷管理用飞书消息 + 邮件 + 测试报告 Excel,4 个具体问题:

1. **缺陷收口断链**:测试发现 bug 在飞书消息抄送开发,开发改完口头回复,**没有结构化记录**;复盘"上一版本有多少 P0"靠人工翻消息,**Q1 复盘会花 2 小时拼数据**。
2. **状态机不一致触发返工**:同一缺陷在不同人口里"已修复 / 已解决 / 已关闭"语义不同;**测试以为已修复但实际开发只是提交了 commit,验证环节漏掉**,导致 v0.1.0 上线后 3 个 P1 复现。
3. **重开追溯断链**:已关闭缺陷被客户复现时,只能重新建 1 条新缺陷,**与原缺陷的修复历史割裂**,根因分析困难。
4. **指派 / 报告人链路不清**:`assigneeUserId` / `reporterUserId` 在飞书消息里靠 @某人 表达,**没有强 FK 关联到 sys_user**,复盘"谁是 P0 缺陷最多的开发"无法做数据分析。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 500 条缺陷的完整生命周期数据,让缺陷生命周期从"线下口头"做成"线上强状态机 + 强 FK",P0 24h 响应、反向边重开率 ≤ 5%。

**衡量指标**:
- **P0 缺陷 24h 响应率 ≥ 95%**(基线 60%)
- **缺陷 1 周关闭率 ≥ 70%**(基线 40%)
- **反向边重开率 ≤ 5%**(高重开率 = 修复 / 验证质量差,反向指标)
- **缺陷 → 任务关联率 ≥ 80%**(每个缺陷必绑定 taskId 或 sprintId,便于回追)
- **复盘报告生成时间 ≤ 10 分钟**(基线 2 小时,通过 SQL 聚合自动统计)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **PRD §F4.6 完整 5 态状态机**(发现/确认/修复/验证/关闭) — 走 **ADR-D Option A 4 主态实用版**,理由见 [PRD-MAPPING.md §2 Defect 决策记录 D1](../PRD-MAPPING.md)
- **重复缺陷 AI 检测**(同语义 bug 自动聚合)— 留 v0.3 AI 增强
- **跨项目缺陷库**(同类 bug 复用知识)— 留 v0.5+
- **飞书/钉钉自动同步**(缺陷创建/状态变化推送 IM)— 留 v0.3 IM 集成
- **缺陷热力图 / 趋势图**(模块级 P0/P1 分布可视化)— 留 v0.3 Analytics 模块承接
- **客户视角缺陷门户**(客户自主报 bug)— 留 v0.5+,本期仅内部用

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **测试 (QA)** | 创建缺陷 / 改严重级别 / 标"待确认" | 跑测发现 bug,新建 + 指派 |
| **开发 (Dev)** | 接 assigneeUserId 缺陷 / 推 "修复中→待验证" | 修复 + 填 resolution + 提交 commit ref |
| **测试经理 (QA Lead)** | 全 CRUD + 决策"重开 / 直接关闭" | 复盘 + 重开 + 关闭决策 |
| **管理员 (admin)** | 全 CRUD | 跨项目跟踪 P0/P1 缺陷 |
| **报告人 (默认=当前 user)** | 查看 + 评论 | 跟进自己提的缺陷 |

### 2.2 典型场景

**S1 测试发现 bug**(最高频)
> 周一测试王 QA 跑测发现 "项目列表分页失效" → 进入缺陷菜单 → 新建 → 标题 "PRJ 列表 pageSize 失效" + 严重级别=P1 + 分类="功能" + 所属模块="项目管理" + 复现步骤(3 步) + 期望/实际结果 + 标签="regression" + 指派给开发李工 → 保存 → status='00' 待确认 → 飞书自动通知李工(v0.3 集成,本期人工抄送)

**S2 开发修复 → 转待验证**(关键流程)
> 李工接到缺陷 → status='01 修复中' → 修代码 + 提交 commit `abc123` → 进入缺陷详情 → 填 resolution="已修复,通过 commit abc123 ;改动:ProjectMapper.xml LIMIT 子句" → status 推 '02 待验证'(**必填 resolution,违反抛 705**) → 飞书通知王 QA 验证

**S3 验证失败,反向边打回**(关键反向边)
> 王 QA 验证发现 "改完后第 2 页仍空" → 进入详情 → 改 status='01 修复中'(**反向边 02→01**)+ resolution 字段加备注 "第 2 页仍空,可能是 service 层 offset 计算" → 李工再修

**S4 验证通过,关闭**(终态)
> 王 QA 验证通过 → status='03 已关闭' → 列表自动过滤,但保留历史可查

**S5 客户复现,重开**(反向边 03→00,ADR-D 核心特性)
> 缺陷已关闭 2 周后客户报 "第 2 页又出问题了" → admin 改 status='00 待确认'(**反向边 03→00 重开**)+ 在原缺陷追加新 reproduceSteps → 复用历史 resolution 上下文,**比新建 1 条更有追溯性**

**S6 误报 / 重复 → 直接关闭**(快关路径)
> 王 QA 发现刚提的缺陷其实是配置问题 → 直接 status='00 → 03 已关闭'(**00→03 快关**)+ resolution="非缺陷,环境配置问题" → 不走"修复→验证"链路

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Defect (F4.6)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: defectId / defectNo (`DEFECT-YYYY-NNNN` ADR-0005) / projectId(FK 必)/ sprintId(FK 可空)/ taskId(FK 可空)
- 用户输入: title / description / severity (P0-P3) / category(功能/性能/兼容/安全/易用)/ module (本会话新增,ADR-D D2,自由文本)
- 流程: status(4 态)/ assigneeUserId(开发)/ reporterUserId(默认当前 user)/ reproduceSteps / expectedResult / actualResult / resolution
- 扩展: tags (CSV, e.g. regression, flaky, hotfix)

---

## 4. 状态机

### 4.1 ADR-D 决策回顾

PRD §F4.6 L367-371 描述 5 态生命周期 (发现/确认/修复/验证/关闭);原型 modal-defectedit `dem-status` 仅提供 4 个 UI 选项 (待确认/修复中/待验证/已关闭);Domain 旧字典 5 态但 label 不同 (新建/已确认/处理中/已解决/已关闭);PRD-MAPPING §3 原标 "6 态含重开"。**四方分歧**。

**ADR-D Option A 决议**: 走 4 主态实用版 (00 待确认 / 01 修复中 / 02 待验证 / 03 已关闭) + 反向边 03→00 重开。
- **依据**: (a) 原型 UI 4 选项是项目惯例;(b) 合并旧 "新建+已确认" 为 "待确认" 符合 QA 实际工作;(c) "已解决" → "待验证" 改名更准(已解决的"已"在中文里语义模糊);(d) "重开" 用反向边表达比再建一条更追溯。

### 4.2 状态机定义

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) defect 行展开:

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 待确认 | {01 修复中, 03 已关闭} | 默认初始;合并旧"新建+已确认";直接关闭走 S6 快关 |
| 01 | 修复中 | {00 待确认(回退), 02 待验证} | 开发认领后进入;回退到 00 表示"不是我的活" |
| 02 | 待验证 | {01 修复中(打回), 03 已关闭} | 修复完成转此态,**必填 resolution(705)**;验证失败反向打回 01 |
| 03 | 已关闭 | {00 待确认(反向边·重开)} | 终态但允许 03→00 重开,客户复现/Severity 升级触发 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- 进入 `02 待验证` 必填 resolution(705)
- 进入 `03 已关闭` 不强制 resolution(因走 S5 快关时可能无 resolution)
- `severity` / `status` 字段白名单校验(604)
- `assigneeUserId` 不强制(支持"先建未指派,后续认领")
- `reporterUserId` 默认 = 当前登录 user(Service 自动填,前端不传)

---

## 5. AI 能力

### 5.1 当前状态

详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) — **Defect 模块本期无 AI 端点**。原型 modal-newdefect 有 "✨ AI 相似缺陷检测" 按钮,但在 PLM-路线图.md "永不做剥离清单" 第 1 项关联(需 AgriKB 语义检索)。

### 5.2 留 v0.3 AI 增强方向

- AI 重复缺陷检测(新建时 embedding 比对历史 closed defect)
- AI 缺陷严重级别建议(基于 description + reproduceSteps 自动评 P0-P3)
- AI 修复建议(关联历史相似 closed 缺陷的 resolution)

---

## 6. 验收标准

[PRD §F4.6 验收 L367-371](../prd和原型/AgriAI-PLM-完整PRD文档.md):
- ⏳ **缺陷生命周期 100% 入库**(本期可达,前提是 QA 团队培训"不再口头报 bug")
- ⏳ **缺陷与提测单关联**(本期 sprintId/taskId 可关联,提测单 submission 关联待 v0.3 强 FK)

**模块特有验收**(本会话已落地):
- 4 主态状态机合法转换 + 反向边 03→00 重开 单测覆盖
- 进入 02 待验证 必填 resolution(705)单测覆盖
- 状态机非法转换(00→02 跨级 / 03→01 跨级反向)抛 601
- 字段白名单(severity / status)抛 604
- FK 校验:projectId 必,sprintId/taskId 可空但若填则必须存在(702)
- E2E 测试套件 8 个用例全绿(commit `e0a9a21` 实施验证)

---

## 7. 不做的事 — 详 §1.3

- PRD 5 态 / AI 重复检测 / 跨项目库 / IM 同步 / 热力图 / 客户门户

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Defect-数据库设计.md](../02-设计/Defect-数据库设计.md)
- API 设计: [Defect-API设计.md](../02-设计/Defect-API设计.md)
- 测试计划: [Defect-测试计划-2026-05-17.md](../04-测试/Defect-测试计划-2026-05-17.md)
- 发布计划: [Defect-发布计划-2026-05-17.md](../05-上线/Defect-发布计划-2026-05-17.md)
- 原型: [defects.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/defects.html)
- AgriAI PRD: [§F4.6 L367-371](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- ADR: [proposal 0302 ADR-D Option A](../99-跨阶段/proposals/0302-adr-d-defect-state-machine.md)
- 实施 commit: `657c8a8` (字段表+ADR-D) → `e0a9a21` (代码,4 态状态机+module 字段) → `e55fe63` (状态升级)

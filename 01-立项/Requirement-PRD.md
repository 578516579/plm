# PRD: Requirement 模块 — 需求采集与管理 (F2.1)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 (实质内容,2026-05-17 由 Claude 基于 PRD-MAPPING §2 + AgriAI PRD §F2.1 + 原型 requirements.html + ADR-A 决策生成) |
| 作者 | Wjl |
| PRD § | F2.1 (AgriAI-PLM-完整PRD文档.md L243-247) |
| 原型 HTML | [requirements.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/requirements.html) (modal-newreq: L152, reqdetail: L264) |
| 评审状态 | pending(待 Phase 01 Gate 评审) |
| 关联 ADR | [ADR-A](../99-跨阶段/proposals/) Requirement 状态机决策 — 4 态实用版(裁剪 PRD 6 态) |
| 关联 OKR | _2026 Q2-O2-KR3: PLM 需求模块上线,需求 → 任务转化追溯率 100%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Requirement (F2.1)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队的需求管理目前面临 4 个问题:

1. **需求来源混乱**:客户反馈在邮件 / 内部提案在飞书文档 / 竞品分析在 PM 个人笔记 / 运营数据在 BI 报表;**没有统一收口**,每次评审会 PM 花 1 小时收集需求。
2. **AI 优先级评估缺位**:新需求来时,PM 凭经验拍优先级(P0/P1/P2),不同 PM 对同一需求评分差异大;**Q1 评审会 30% 时间花在"为什么是 P0"上**。
3. **需求 → 任务追溯断链**:需求批准后转开发任务,但 task 表没强 FK 关联到 requirement,**事后追溯"这个 bug 来自哪个需求"靠人肉**。
4. **需求状态机不严**:很多需求停留在"开发中"超过 6 个月,实际已被取消但没人改状态;**僵尸需求充斥列表,影响决策**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 200 条需求的完整生命周期数据,把"需求 → 任务"链路从断链做成强 FK 关联,AI 优先级评估让评审会议时间从 2 小时压到 1 小时。

**衡量指标**:
- **需求 → 任务 FK 关联率 ≥ 95%**(基线 30%)
- **AI 价值评估覆盖率 ≥ 80%**(新需求有 aiValue 标签)
- **僵尸需求率 < 5%**(状态 = 开发中但超 60 天无动作)
- **评审会议时长 ≤ 1 小时**(基线 2 小时)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **PRD §F2.1 完整 6 态状态机**(草稿/评审/确认/开发中/完成/验收) — 走 **ADR-A 4 态实用版**,理由见 [PRD-MAPPING.md §2 Requirement 决策记录 D1](../PRD-MAPPING.md)
- **需求关联图谱**(父子关联 / 冲突识别) — PRD §F2.1 提及但本期不做,留 v0.3
- **AI 自动归类去重** — PRD §F2.1 提及但本期 aiValue 字段只做"价值评估",分类去重留 v0.5+
- **多人协作编辑** — 单作者,锁机制留 v0.3
- **跨项目需求**(同需求复用到多项目)— 本期单 projectId FK,跨项目复用留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **项目经理 (PM)** | CRUD 自己项目下的需求 | 起草 → AI 评估 → 评审 → 提交开发 |
| **管理员 (admin)** | 全 CRUD + 评审决策 | 评审 / 打回 / 取消 |
| **开发** | 查看 + 评论 | 接受任务后从需求拉取上下文 |
| **测试** | 查看 + 评论 | 验收需求时回追原始描述 |

### 2.2 典型场景

**S1 客户反馈转需求**(最高频)
> 王 PM 收到客户邮件"希望 PLM 支持飞书消息通知" → 进入需求菜单 → 新建 → 标题"飞书消息通知" + 来源="客户反馈" + 描述(粘贴邮件原文) + 优先级=P1 → 保存 → AI 自动评估 aiValue="M 中价值" → admin review → 批准转开发

**S2 需求评审 + AI 评估**(高价值)
> 周一评审会 5 个新需求 → AI 已自动评 aiValue (H/M/L) → admin 优先评 H 需求 → M/L 需求快速过 → 评审会 1 小时完成

**S3 需求 → 任务转化**(关键流程)
> admin 批准需求 (status='01' 开发中) → PM 拆任务 → 每个 task 必须填 requirementId FK → **追溯"这个 task 来自哪个需求"一键定位**

**S4 评审打回**(反向边)
> admin 评审发现需求描述不清 → 改 status='01' → '00 待评审' (反向边) + reviewNote="请补充验收标准" → PM 改完重新提交

**S5 需求取消**(终态)
> 客户撤回需求 → status='03 已取消' (从 00 或 01 任意点都能取消) → 列表自动过滤 → 后续追溯仍可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Requirement (F2.1)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- title / description / source / priority — 用户输入字段
- status — 4 态状态机 (ADR-A 实用版,详 §4)
- **aiValue** — H 高 / M 中 / L 低 (本次 PRD-align 新增,ADR-A D2)
- assigneeUserId / reviewNote — 流程字段
- projectId — FK→tb_project (必填,需求必须属于项目)

---

## 4. 状态机

### 4.1 ADR-A 决策回顾

PRD §F2.1 L247 描述状态机为"草稿 → 评审 → 确认 → 开发中 → 完成 → 验收"(6 态)。原型 rdm-edit-status 仅提供 4 个 UI 选项 (待评审/开发中/已完成/已取消)。

**ADR-A 决议**: 走 4 态实用版(同原型),不走 PRD 6 态。
- **依据**: (a) 原型 UI 仅 4 选项,UI 必须和状态机一致;(b) 项目其他 19 个 PRD-aligned 模块均"原型优先";(c) 4 态合并草稿/评审/确认为"待评审",符合 PM 实际工作。
- **代价**: 状态机层无法追溯 PRD 6 态(尤其"已验收"独立终态)。
- **缓解**: 后续需要时通过 acceptedAt 字段或独立 acceptance 模块解决。

### 4.2 状态机定义

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) requirement 行:`00→{01,03}` `01→{00,02,03}` `02,03` 终态。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 待评审 | {01 开发中, 03 已取消} | 默认初始状态,合并 PRD 的"草稿/评审/确认" |
| 01 | 开发中 | {00 待评审(打回), 02 已完成, 03 已取消} | admin 批准后进入;评审失败可打回 00 |
| 02 | 已完成 | {} | 终态 |
| 03 | 已取消 | {} | 终态 |

**特殊规则**:
- `01 → 00` 反向边称为"评审打回",必填 reviewNote
- 新建需求强制 status='00' (Service 校验,违反抛 601)
- aiValue 字段不参与状态机,可任意时候编辑

---

## 5. AI 能力

### 5.1 aiValue AI 价值评估

**字段**: aiValue (H 高 / M 中 / L 低),字典 biz_req_ai_value。

**触发时机**: 新建需求时 AI 自动评 / PM 手动改 / admin 评审时改 — 都允许。

**评估维度**(本期 mock):
- 客户量(来源="客户反馈" 默认 +1)
- 优先级(priority='00' P0 紧急默认升级)
- 描述长度(< 50 字降级)

### 5.2 AI 端点(留 v0.5+)

POST /business/requirement/ai/evaluate — 调真实 LLM 评估 + AgriKB 历史复用查找。本期 mock。

详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

---

## 6. 验收标准

[PRD §F2.1 验收 L273-276](../prd和原型/AgriAI-PLM-完整PRD文档.md):
- ⏳ **PRD AI 生成完整度 ≥ 80%** — 实际属于 §F2.2 PRD 模块的指标,本模块不直接负责
- ✅ **需求追踪矩阵自动维护,覆盖率 100%** — task.requirementId FK 强约束已落地
- ⏳ **UED 与需求双向关联准确率 ≥ 95%** — 留 v0.3 ued 模块接入

**模块特有验收**:
- 新建需求 → AI 评估 aiValue → 评审 → 转开发 → 关联 task 全流程 E2E 测试绿(本会话 18/18 通过)
- 状态机 4 态合法/非法转换 + 反向边 01→00 单测覆盖
- aiValue 白名单校验(H/M/L)抛 604
- FK 校验:projectId 必须存在(702)

---

## 7. 不做的事 (Out of Scope) — 详 §1.3

- PRD 6 态(已 ADR-A 决议走 4 态)
- 需求关联图谱 / 冲突识别 / 跨项目复用
- AI 自动归类去重(只做价值评估)

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Requirement-数据库设计.md](../02-设计/Requirement-数据库设计.md)
- API 设计: [Requirement-API设计.md](../02-设计/Requirement-API设计.md)
- 测试计划: [Requirement-测试计划-2026-05-17.md](../04-测试/Requirement-测试计划-2026-05-17.md)
- 发布计划: [Requirement-发布计划-2026-05-17.md](../05-上线/Requirement-发布计划-2026-05-17.md)
- 原型: [requirements.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/requirements.html)
- AgriAI PRD: [§F2.1 L243-276](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 实施 commit: 1afe0ba (字段表+ADR-A) → df35652 (代码,18/18 测试) → 6eb0c95 (状态升级)

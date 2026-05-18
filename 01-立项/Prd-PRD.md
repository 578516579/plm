# PRD: PRD 模块 — AI PRD 生成器 (F2.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F2.2 (AgriAI-PLM-完整PRD文档.md §F2.2 AI PRD 生成器) |
| 原型 HTML | [prd.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/prd.html) (modal-newprd + prdContent + prdCompleteness 徽章) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O2-KR1: PLM PRD 模块上线,AI 生成 PRD 完整度均值 ≥ 85_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "PRD (F2.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前撰写 PRD 用飞书文档 + 个人 Word 模板,4 个具体问题:

1. **PRD 撰写耗时长**:产品经理写一份完整 PRD(7 段:背景/目标/场景/字段/状态机/AI/验收)平均**耗时 4-6 小时**,新手 PM 甚至超过 1 天;Q1 5 个新项目共耗费 PRD 撰写工时 ≥ 80h。
2. **完整度参差不齐**:无统一模板下,**约 40% 的 PRD 漏写"非功能需求 / 验收标准 / 不做的事"3 个关键段落**,导致 Phase 04 测试经理无从下手设计测试方案,反复回头补 PRD。
3. **农业场景 PRD 模板缺位**:灌溉算法 / 植保识别 / 农销溯源 等农业专项 PRD 模板需要从头写,**80% 内容是同行业模板重复**(数据采集 / 算法精度 / 农情合规 / 农场操作员易用性),但没人沉淀过模板。
4. **PRD 评审打回率高**:评审环节常因"完整度不足 / 验收标准模糊"打回,**Q1 平均每份 PRD 打回 2.3 次**,从草稿到确认平均 8 天,严重拖慢立项→设计交接。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 50 份完整 PRD 的全量字段数据,以 AI PRD 生成器把"4-6 小时人工写"做成"15 分钟 AI 生成 + 30 分钟人工微调",PRD 完整度均值 ≥ 85。

**衡量指标**:
- **AI 生成 PRD 完整度均值 ≥ 85**(本期 mock 固定 85.0,Dify 接入后基于段落完整性算分)
- **AI 生成 PRD 时间 ≤ 5 分钟**(PRD §F2.2 验收硬指标)
- **PRD 评审一次通过率 ≥ 60%**(基线 25%,3 次打回降为 1.4 次)
- **农业场景模板使用率 ≥ 70%**(4 个 sceneTemplate 中至少 1 个被选)
- **每份 PRD 撰写工时 ≤ 1h**(基线 4-6h,AI 生成 + 人工微调合计)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **PRD 版本变更可视化 Diff** — 仅 version 字段 + content 全文存储,可视化 Diff(类 Git Blame)留 v0.3
- **多人协作实时编辑**(类似飞书文档多光标)— 单 authorUserId,锁机制留 v0.5+
- **PRD 自动评审打分**(基于段落质量给评级)— 仅 completenessScore 完整度数字,智能评级留 v0.5+
- **PRD 模板用户自定义**(让团队上传自己的行业模板)— 仅 4 个固定 sceneTemplate(灌溉/农销/植保/溯源),自定义留 v0.3
- **导出 Word / PDF / HTML 多格式** — 仅 Markdown 全文,导出留 v0.2(走 Document 模块多格式 outputFormats 同源能力)
- **PRD AI 与原型 HTML 双向同步**(改 PRD 自动改原型字段名)— 路线图剥离

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **产品经理 (PM)** | CRUD 自己项目下的 PRD | 触发 AI 生成 → 微调 7 段 → 提交评审 |
| **业务专家** | 查看 + 评论 | 评审 PRD 业务正确性 |
| **评审 admin** | 全 CRUD + 决策(02 已确认 / 01→00 打回) | 走完整评审流 |
| **开发 / 测试** | 查看 | 读 PRD 作设计 / 测试用例输入 |

### 2.2 典型场景

**S1 AI 辅助生成 PRD**(最高频)
> 王 PM 接到"AI 灌溉推荐引擎"立项 → 进入 PRD 菜单 → 新建 → prd-title "AI 灌溉推荐引擎" + prd-desc 一段自然语言"基于土壤湿度+天气推荐每日灌溉量,目标用户=农场技术员" + prd-tpl="irrigation 灌溉" + prd-user="agronomist 农艺师" → 点 "AI 生成 PRD" → 服务端 mock 输出 7 段 Markdown(背景/目标用户/场景/核心功能/非功能/验收/风险)→ prdContent 渲染 → completenessScore mock 85.0

**S2 PM 微调 + 提交评审**(关键流程)
> 王 PM 看 AI 生成的 7 段,改 "非功能需求" 加 "IoT 设备离线 24h 容错" → 改 status='00→01 评审中' + 指定 reviewerUserId → 系统通知评审人

**S3 评审打回**(反向边路径)
> 评审人发现"验收标准没写具体精度阈值"→ 改 status='01→00 草稿'(**反向边,必填 reviewNote** "灌溉量精度需要写具体百分比")→ PM 收通知补写 → 再次提交

**S4 PRD 确认 → 转入设计**(关键流程,下游联动)
> 评审通过 → status='01→02 已确认' → 下游 arch / dbdesign / apidesign 模块可 FK 引用本 prdId(可选关联,见 Arch-PRD §3 字段)→ 这是设计阶段的入参

**S5 PRD 废弃**(终态)
> 项目 pivot 后老 PRD 不再有效 → status='02→03 已废弃' → 列表过滤但保留历史可查(下游 arch 已经引用过的关系保留)

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "PRD (F2.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: prdId / prdNo (`PRD-YYYY-NNNN`) / projectId(FK 必)
- 用户输入: title / description / sceneTemplate(4 值字典)/ targetUser(3 值字典)/ version(默认 v1.0)
- AI 输出: content(Markdown 7 段)/ completenessScore(0-100)/ aiGenerated / aiGeneratedAt
- 流程: status(4 态)/ authorUserId(作者)/ reviewerUserId(评审人,01→02 时填)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) prd 行:

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始;PM 触发 AI 生成 + 微调 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 评审打回必填 reviewNote |
| 02 | 已确认 | {03 已废弃} | 终态分支;下游 arch/dbdesign/apidesign FK 引用 |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- 反向边 01→00 必填 reviewNote(602)
- `completenessScore` 由 generatePRD 服务计算,**不接受前端写入**(本期 mock 固定 85.0)
- sceneTemplate / targetUser 字段白名单校验(604)
- FK 校验:projectId 必,违反抛 702
- reviewerUserId 在 01→02 时必填,确保审计轨迹

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/prd/ai/generate/{id}` — 调用 §F2.2 `prd-generation-flow` Dify 工作流(详 [PRD-MAPPING §6](../PRD-MAPPING.md))。

### 5.2 当前阶段实现 (mock)

🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F2.2 行)— 本期占位 mock:按 sceneTemplate + targetUser 生成 7 段标准 PRD Markdown,completenessScore mock 固定 85.0。

mock 输出结构(7 段):
1. 背景与现状痛点
2. 目标用户与典型场景
3. 核心功能(基于 sceneTemplate 注入 — 灌溉/植保/农销/溯源各一套模板)
4. 非功能需求(性能/安全/合规)
5. 农业专项考量(IoT 接入 / 离线容错 / 农情边界值)
6. 验收标准
7. 风险与不做的事

### 5.3 Dify 工作流路线图

- v0.3: Dify `prd-generation-flow` 接入 DeepSeek/ChatGLM → 真实 AI 生成 + 基于段落数据完整度算 completenessScore
- v0.5+: AI 自动评审打分(对接 reviewerUserId 工作流,AI 给出"评审建议"草稿)
- v0.5+: 模板自定义(把高分 PRD 反沉淀为团队私有模板)

---

## 6. 验收标准

**PRD §F2.2 验收**:
- ⏳ **PRD AI 生成完整度 ≥ 80%**(本期 mock 固定 85.0 已满足下限)
- ⏳ **AI 生成 PRD 时间 < 5 分钟**(mock 即时返回,Dify 接入后实测)
- ⏳ **支持 4 种农业场景模板**(本期 4 值 sceneTemplate 字典已就位)

**模块特有验收**(本会话已落地):
- 4 态状态机合法转换 + 反向边 01→00 单测覆盖
- sceneTemplate / targetUser 字段白名单校验(604)
- 反向边 01→00 必填 reviewNote(602)
- FK 校验:projectId 必(702)
- completenessScore 服务端计算,前端写入被忽略(单测覆盖)

---

## 7. 不做的事 — 详 §1.3

- 可视化 Diff / 多人协作 / 自动评审打分 / 模板自定义 / 多格式导出 / 双向同步

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Prd-数据库设计.md](../02-设计/Prd-数据库设计.md)
- API 设计: [Prd-API设计.md](../02-设计/Prd-API设计.md)
- 测试计划: [Prd-测试计划-2026-05-17.md](../04-测试/Prd-测试计划-2026-05-17.md)
- 发布计划: [Prd-发布计划-2026-05-17.md](../05-上线/Prd-发布计划-2026-05-17.md)
- 原型: [prd.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/prd.html)
- AgriAI PRD: [§F2.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Arch-PRD.md](Arch-PRD.md) / [Dbdesign-PRD.md](Dbdesign-PRD.md) / [Apidesign-PRD.md](Apidesign-PRD.md)(下游 FK 引用)

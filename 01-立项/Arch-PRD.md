# PRD: Arch 模块 — 系统概要设计 HLD (F3.1)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F3.1 (AgriAI-PLM-完整PRD文档.md §F3.1 系统概要设计) |
| 原型 HTML | [archdesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/archdesign.html) (arch-mode/lang/db/ai/deploy/iot 6 选项 + archContent + archDiagram C4 + archNFR) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O3-KR1: PLM 架构设计模块上线,AI 推荐技术栈采纳率 ≥ 60%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Arch (F3.1)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前的架构设计走 "draw.io + 飞书文档",4 个具体问题:

1. **技术栈选型分歧大,决策周期长**:每个新项目要不要用微服务 / 该选 SpringBoot 还是 Go / 数据库选 PostgreSQL 还是 MySQL,架构师辩论 1-2 周;**Q1 新项目"AI 灌溉"光技术栈选型耗费 8 个工时**,没有数据支撑只靠经验。
2. **C4 图分散无追溯**:架构图存在 draw.io / Lucid / 飞书画板 3 个地方,**Q1 上线后排查"为什么没用消息队列"无法快速定位当时架构决策**。
3. **非功能需求(NFR)拍脑袋**:性能目标 "QPS 1000"、安全等级 "二级等保" 等没有数据支撑,**Q1 某项目上线后实测 QPS 200 才能扛住,差 5 倍**,架构需要重构。
4. **农业 IoT 协议没标准化**:AgriPLM 涉及大量 IoT 设备(土壤传感器/气象站/灌溉控制器),**MQTT vs HTTP 长轮询 vs WebSocket 项目间不一致**,导致设备接入代码无法复用。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 30 份架构设计数据,AI 推荐技术栈让"凭经验"做成"基于历史项目数据",决策周期从 2 周降到 3 天。

**衡量指标**:
- **AI 推荐技术栈采纳率 ≥ 60%**(架构师采用 AI 给的方案 / AI 给的方案数)
- **C4 图归档率 ≥ 90%**(每份 arch 必有 c4DiagramContent)
- **NFR 字段填写率 ≥ 80%**(nfrMapping 不空)
- **架构决策周期 ≤ 3 天**(基线 2 周)
- **IoT 协议标准化采纳率 ≥ 80%**(优先用 MQTT,字典约束)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **架构图实时协作编辑**(类 draw.io 多人协同)— c4DiagramContent 仅 Mermaid 文本,实时编辑留 v0.3
- **架构决策自动评分**(AI 评 "你这个方案得 78 分")— 留 v0.5+
- **技术栈兼容性 AI 校验**(SpringBoot + Kingbase 兼容性自动判断)— 留 v0.3
- **跨项目架构模式复用推荐**(根据当前需求找历史相似 arch)— 留 v0.5+,需 AgriKB 向量库
- **代码扫描反推架构**(从代码自动重建架构图)— 留 v1.0+
- **NFR 自动测试用例生成**(性能目标 → JMeter 脚本)— 留 v0.3 走 testdata 模块联动

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **架构师 (architect)** | CRUD 自己项目的 arch | 触发 AI 推荐 / 微调 6 维度 / 出 C4 图 |
| **评审 admin** | 全 CRUD + 决策(02 / 01→00) | 评审架构合理性 |
| **开发 (Dev)** | 查看 | 读 archContent 作 dbdesign/apidesign 输入 |
| **测试** | 查看 | 读 NFR 设计性能测试方案 |

### 2.2 典型场景

**S1 AI 推荐架构**(最高频)
> 张架构师接到"AI 灌溉推荐"项目立项 → 进入架构设计菜单 → 新建 → title "智慧灌溉 v1.0 HLD" + 关联 prdId=PRD-89 + 点 "AI 推荐架构" → mock 输出 archMode=microservice + primaryStack=java_sb3 + databaseChoice=pg_redis + aiOrchestration=dify_deepseek + deploymentType=k8s + iotProtocol=mqtt → archContent 标准 5 段 Markdown + archDiagram C4 容器图 Mermaid + nfrMapping JSON(QPS/安全/容错)

**S2 架构师微调 + 提交**(关键流程)
> 张架构师评估 mock 推荐,改 databaseChoice 从 pg_redis 改为 kingbase(信创要求)→ archContent 段落小改 → status='00→01 评审中'

**S3 评审打回**(反向边路径)
> 评审 admin 发现 "NFR QPS 目标 1000 但缺少容灾设计" → 改 status='01→00 草稿'(**反向边**)+ 备注 → 架构师补 C4 图加入 Sentinel 限流 + 补 NFR 容灾段落 → 重新提交

**S4 架构确认 → 下游联动**(关键流程)
> 评审通过 → status='01→02 已确认' → dbdesign / apidesign 可 FK 引用本 archId(可选)→ 数据库 / API 详细设计基于本架构展开

**S5 架构废弃**(终态)
> 项目重大 pivot → status='02→03 已废弃' → 保留历史可查

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Arch (F3.1)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: archId / archNo (`ARCH-YYYY-NNNN`) / projectId(FK 必)/ prdId(FK 可空)
- 用户输入 6 维度: archMode(4 值)/ primaryStack(4 值)/ databaseChoice(3 值)/ aiOrchestration(3 值)/ deploymentType(3 值)/ iotProtocol(3 值)
- AI 输出: designContent(Markdown)/ c4DiagramContent(Mermaid C4)/ nfrMapping(JSON)
- 流程: status(4 态)/ authorUserId / reviewerUserId / aiGenerated / aiGeneratedAt

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) arch 行(同 prd 4 态模式):

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 评审打回 |
| 02 | 已确认 | {03 已废弃} | 终态分支;下游 dbdesign/apidesign FK 引用 |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- 反向边 01→00 必填 reviewNote(602)
- 6 维度字段(archMode/primaryStack/databaseChoice/aiOrchestration/deploymentType/iotProtocol)字段白名单校验(604)
- FK 校验:projectId 必,prdId 可空但若填必须存在(702)

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/arch/ai/generate/{id}` 或 `POST /business/arch/ai/recommend` — 调用 §F3.1 `arch-design-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🔴 未实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F3.1 行)— 本期占位 mock:基于关联 prdId 的 sceneTemplate(灌溉/植保/农销/溯源)推荐 6 维度组合 + C4 Mermaid 模板 + NFR JSON 模板。

mock 输出 6 维度组合规则:
- 灌溉 + IoT 强场景 → microservice / java_sb3 / pg_redis / dify_deepseek / k8s / mqtt
- 农销 + 轻量 → monolith / java_sb3 / mysql_redis / dify_chatglm / docker_compose / http_longpoll
- 信创要求 → 任意场景 + databaseChoice=kingbase + primaryStack=java_sb3

### 5.3 路线图

- v0.3: Dify 真实 AI 接入 + 基于历史项目数据训练推荐
- v0.3: 技术栈兼容性自动校验
- v0.5+: 跨项目架构模式复用推荐(需 AgriKB)

---

## 6. 验收标准

**PRD §F3.1 验收**:
- ⏳ **AI 推荐架构方案**(本期 mock 6 维度组合)
- ⏳ **自动生成 C4 容器图**(本期 mock Mermaid 模板)
- ⏳ **NFR 映射记录**(本期 nfrMapping JSON 字段就位)

**模块特有验收**(本会话已落地):
- 4 态状态机 + 反向边 01→00 单测覆盖
- 6 维度字段白名单(604)
- 反向边必填 reviewNote(602)
- FK 校验:projectId 必、prdId 可空(702)

---

## 7. 不做的事 — 详 §1.3

- 实时协作 / 自动评分 / 兼容性校验 / 跨项目复用 / 代码反推 / NFR 测试生成

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Arch-数据库设计.md](../02-设计/Arch-数据库设计.md)
- API 设计: [Arch-API设计.md](../02-设计/Arch-API设计.md)
- 测试计划: [Arch-测试计划-2026-05-17.md](../04-测试/Arch-测试计划-2026-05-17.md)
- 发布计划: [Arch-发布计划-2026-05-17.md](../05-上线/Arch-发布计划-2026-05-17.md)
- 原型: [archdesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/archdesign.html)
- AgriAI PRD: [§F3.1](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Prd-PRD.md](Prd-PRD.md)(arch.prdId FK)/ [Dbdesign-PRD.md](Dbdesign-PRD.md) / [Apidesign-PRD.md](Apidesign-PRD.md)(下游 FK)

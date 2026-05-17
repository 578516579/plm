# PRD: Arch 模块 — 系统概要设计 HLD (F3.1)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F3.1 + 原型 archdesign.html) |
| 作者 | Wjl |
| PRD § | F3.1 (AgriAI-PLM-完整PRD文档.md §F3.1 系统概要设计) |
| 原型 HTML | [archdesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/archdesign.html) (modal-newarch + archContent + archDiagram + archNFR) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Arch (F3.1)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(架构方案散在 Word / 缺 C4 容器图 / 非功能需求散落 / 架构选型评审耗时)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F3.1 验收标准 + 模块特有衡量指标(架构评审通过率 / AI 生成 HLD 时间)。

### 1.3 不做的事 (Out of Scope)
本期**不做** (从 AgriAI PRD §F3.1 的高级能力 + 项目路线图剥离清单推断):
- **架构方案自动跑分** — 仅 designContent / nfrMapping 字段,跑分留 v0.5+
- **多架构方案 A/B 对比** — 单方案,A/B 留 v0.3
- **C4 图实时协作编辑** — 仅 Mermaid 文本,可视化协作留 v0.5+
- **架构组件复用库** — 单条目无组件库引用,复用库留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:架构师 / 技术负责人 / 评审 admin / 开发(downstream FK 引用)。

### 2.2 典型场景

**S1 AI 辅助生成 HLD**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newarch 字段(arch-mode/arch-lang/arch-db/arch-ai/arch-deploy/arch-iot)→ genArchDesign → 输出 archContent / C4 Mermaid / NFR 映射

**S2 架构评审打回**(高价值)
<待人工填写>:01→00 反向边,reviewNote 必填

**S3 架构 → 数据库设计 / API 设计**(关键流程)
<待人工填写>:arch 02 已确认后,dbdesign / apidesign 可 FK 引用本架构

**S4 IoT 协议选型**(农业特色)
<待人工填写>:iotProtocol 字段(mqtt/http_longpoll/websocket)对接农情设备

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Arch (F3.1)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: archId / archNo / projectId(FK)/ prdId(可选 FK)
- 用户输入: title / archMode / primaryStack / databaseChoice / aiOrchestration / deploymentType / iotProtocol
- AI 输出: designContent / c4DiagramContent(Mermaid)/ nfrMapping / aiGenerated / aiGeneratedAt
- 流程: status / authorUserId / reviewerUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) arch 行(4 态含反向边)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始状态 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 = 评审打回 |
| 02 | 已确认 | {03 已废弃} | 可被 dbdesign / apidesign FK 引用 |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 01→00 必填 reviewNote
- 6 个 select 字段字典白名单校验(604)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/arch/ai/generate/{id} — Dify 工作流 arch-design-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F3.1 行)— 本期占位 mock(按 6 个选型字段组合生成标准 C4 Mermaid + NFR 模板)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:mock 按 archMode + primaryStack 注入模板;Dify 实接入留 v0.5+。

---

## 6. 验收标准

**PRD §F3.1 验收**:
- ⏳ AI 生成 HLD 时间 < 5 分钟
- ⏳ C4 Mermaid 图渲染成功率 100%

**模块特有验收**:
<待人工填写>:E2E 测试 / 字典白名单 / FK 校验(projectId 必填 702)

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Arch-数据库设计.md](../02-设计/Arch-数据库设计.md)
- API 设计: [Arch-API设计.md](../02-设计/Arch-API设计.md)
- 测试计划: [Arch-测试计划-2026-05-17.md](../04-测试/Arch-测试计划-2026-05-17.md)
- 发布计划: [Arch-发布计划-2026-05-17.md](../05-上线/Arch-发布计划-2026-05-17.md)
- 原型: [archdesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/archdesign.html)
- AgriAI PRD: [§F3.1](../prd和原型/AgriAI-PLM-完整PRD文档.md)

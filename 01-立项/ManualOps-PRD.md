# PRD: ManualOps 模块 — 运维手册 (F5.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F5.3 + 原型 opsmanual.html) |
| 作者 | Wjl |
| PRD § | F5.3 (AgriAI-PLM-完整PRD文档.md §F5.3 运维手册) |
| 原型 HTML | [opsmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/opsmanual.html) (modal-newom + omContent + 监控 / 告警 / IoT 设备多维) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "ManualOps (F5.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(运维手册按客户定制重复 / 监控方案(Prom/Aliyun/Zabbix)+ 告警渠道(钉钉/飞书/企微/邮件)+ IoT 设备类型(土壤/气象/无人机/灌溉)三维矩阵爆炸 / 农情设备巡检 SLA 缺位 / 应急预案散落)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F5.3 验收标准 + 模块特有衡量指标(AI 运维手册生成时间 / SLA 覆盖度)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **告警通道真实对接** — 仅文档,真实告警留 v0.5+
- **IoT 设备远程巡检** — 仅手册描述,远程巡检留 v0.5+
- **AI 故障预测** — 仅手册,预测留 v0.5+(对接 dora 模块的 MTTR)
- **多客户运维手册版本管理** — 单条目,多版本留 v0.3

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:运维工程师 / SRE / 客户 IT / 评审 admin。

### 2.2 典型场景

**S1 AI 辅助生成运维手册**(最高频)
<待人工填写>:1 段叙述,引原型 modal-newom → 3 维度选(monitoringPlan / alertChannels CSV / iotDeviceTypes CSV)→ 调 §F5.3 ops-manual-flow → 5 章节生成

**S2 IoT 农情设备巡检**(农业特色)
<待人工填写>:iotDeviceTypes 4 类(土壤传感器/气象站/无人机/灌溉控制器)→ 巡检 SLA + 备份策略 + 应急预案

**S3 手册评审 + 发布**(关键流程)
<待人工填写>:02→{00, 03} 反向边支持重新草稿,03 已发布交付

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ManualOps (F5.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: manualopsId / manualopsNo(OM-YYYY-NNNN)/ projectId(FK)
- 用户输入: title / monitoringPlan / alertChannels(CSV)/ iotDeviceTypes(CSV)
- AI 输出: content / generatedAt / aiGenerated
- 流程: status(4 态) / authorUserId
- 输出: outputFormats(CSV)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) manual-ops 行:`00→01→02→{00,03}` (4 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 生成中} | 默认初始状态 |
| 01 | 生成中 | {02 已生成} | AI 生成中 |
| 02 | 已生成 | {00 草稿(重新草稿), 03 已发布} | 反向边 02→00 |
| 03 | 已发布 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- 反向边 02→00 视为"重新草稿"
- monitoringPlan / alertChannels / iotDeviceTypes 3 个字典白名单(604)

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/manual-ops/ai/generate/{id} — Dify 工作流 ops-manual-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🟡 mock 已实现 — 按 monitoringPlan + alertChannels CSV + iotDeviceTypes CSV 生成 5 章节模板(含 IoT 农情设备巡检 SLA + 备份 + 应急)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4 — Dify 实接入留 v0.5+。

---

## 6. 验收标准

**PRD §F5.3 验收**:
- ⏳ AI 生成运维手册时间 < 10 分钟
- ⏳ IoT 农情设备 4 类 SLA 完整

**模块特有验收**:
<待人工填写>:E2E 测试 / 3 维度字典 / 反向边 02→00 单测 / CSV 多选解析单测。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [ManualOps-数据库设计.md](../02-设计/ManualOps-数据库设计.md)
- API 设计: [ManualOps-API设计.md](../02-设计/ManualOps-API设计.md)
- 测试计划: [ManualOps-测试计划-2026-05-17.md](../04-测试/ManualOps-测试计划-2026-05-17.md)
- 发布计划: [ManualOps-发布计划-2026-05-17.md](../05-上线/ManualOps-发布计划-2026-05-17.md)
- 原型: [opsmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/opsmanual.html)
- AgriAI PRD: [§F5.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)

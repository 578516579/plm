# PRD: ManualOps 模块 — 运维手册 (F5.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F5.3 (AgriAI-PLM-完整PRD文档.md §F5.3 运维手册) |
| 原型 HTML | [opsmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/opsmanual.html) (monitoringPlan / alertChannels CSV / iotDeviceTypes CSV) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | 反向边 02→00 由 Service 层校验 |
| 关联 OKR | _2026 Q2-O5-KR3: ManualOps 模块上线,运维 MTTR 降 50%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "ManualOps (F5.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 客户侧运维流程当前走"工程师人脑 + 飞书",4 个具体问题:

1. **运维手册无沉淀**:监控配置 / 告警渠道 / IoT 设备巡检 等运维 know-how 全在工程师人脑,**Q1 老工程师离职后新人接手 3 周才进入状态**。
2. **告警渠道分散**:钉钉 / 飞书 / 企业微信 / 邮件各项目不一致,**Q1 出现过紧急告警漏接(运维只装了钉钉但告警发到飞书)**。
3. **IoT 农情设备巡检 SLA 缺位**:土壤传感器 / 气象站 / 无人机 / 灌溉控制器 等 4 类农情设备,**各客户运维节奏不同,没 SLA 标准**(灌溉旺季容灾切换演练时机模糊)。
4. **应急预案不全**:数据库宕机 / Redis 挂 / IoT 离线 等场景的应急步骤靠口头传递,**Q1 出现过 1 次 PG 主备切换耗费 2h(标准应急流程应 ≤ 30min)**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 15 份运维手册数据,做"监控方案 + 告警渠道 + 4 类农情设备 SLA + 应急预案"五件套,运维 MTTR 降 50%。

**衡量指标**:
- **AI 运维手册生成时间 ≤ 10 分钟**(本期 mock 即时)
- **运维 MTTR 降 50%**(基线 2h → 目标 1h)
- **告警渠道全覆盖率 100%**(每份手册必填 alertChannels)
- **IoT 设备巡检 SLA 覆盖率 ≥ 80%**(4 类设备至少 3 类有 SLA)
- **应急预案完整度 ≥ 90%**(5 大场景:DB/Redis/IoT/网络/电源)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **运维事件管理**(类 PagerDuty 集成)— 留 v0.3
- **自动化运维脚本生成**(基于手册自动生成 Ansible playbook)— 留 v0.3
- **运维知识库 wiki 搜索** — 留 v0.5+,需 AgriKB
- **IoT 设备实时监控大屏** — 留 v0.3 走 Analytics
- **应急预案演练记录** — 留 v0.3
- **多客户运维 SLA 统计大屏** — 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **运维工程师 (SRE)** | CRUD 自己负责的 manualops | 选监控方案 / 设告警渠道 / 维护 SLA |
| **客户 IT 工程师** | 查看 | 按手册操作运维 |
| **管理员** | 全 CRUD + 发布 | 评审 + 沉淀模板 |
| **农情 IoT 工程师** | 协作 | 维护 4 类农情设备 SLA |

### 2.2 典型场景

**S1 AI 生成运维手册**(最高频)
> SRE 小张接到客户 A 运维支持 → 新建 → title "客户 A AgriPLM 运维手册 v1.0" + monitoringPlan="prometheus_grafana"(3 值字典)+ alertChannels="dingtalk,feishu"(CSV 多选 4 值)+ iotDeviceTypes="soil_sensor,weather_station,drone,irrigation_controller"(4 农情设备全选)→ AI 生成 → mock 输出 5 章节:1.监控部署 2.告警渠道配置 3.IoT 设备巡检 SLA 4.备份策略 5.应急预案

**S2 4 农情设备 SLA 关键流程**
> SRE 设置 IoT SLA → 土壤传感器:每周 1 次校准 / 数据漂移 > 5% 告警;气象站:每月 1 次清洁 / 通信失联 > 30 分钟告警;无人机:每飞行前检查 / 电池循环监控;灌溉控制器:灌溉旺季容灾切换演练 / 每月 1 次

**S3 应急预案场景**(关键流程)
> 5 大场景应急步骤:
> - DB 宕机:5 分钟切备 + 重启 + 数据校验
> - Redis 挂:10 分钟切 Sentinel + 缓存重建
> - IoT 离线:30 分钟人工巡检 + 串口诊断
> - 网络抖动:5 分钟 ping + traceroute 诊断 + DNS 切换
> - 电源中断:UPS 维持 + 1h 内告知客户

**S4 反向边重新生成**(关键反向边)
> SRE 选错监控方案 → status='02→00 草稿'(**反向边**)→ 调整后重新生成

**S5 发布**(终态)
> 评审通过 → status='02→03 已发布' → 客户运维工程师可下载

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ManualOps (F5.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: manualopsId / manualopsNo (`OM-YYYY-NNNN`) / projectId(FK 必)
- 监控选型: monitoringPlan(3 值字典:prometheus_grafana/aliyun_cms/zabbix)
- 告警渠道: alertChannels(CSV 4 值:dingtalk/feishu/wework/email)
- IoT 设备: iotDeviceTypes(CSV 4 值:soil_sensor/weather_station/drone/irrigation_controller)
- AI: content(Markdown 5 章节)/ aiGenerated / generatedAt
- 输出: outputFormats(4 值 CSV,默认 pdf)
- 流程: status(4 态含反向边 02→00)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) manual-ops 行:4 态含反向边 02→00(同 manualimpl/manualproduct 模式)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 生成中} | 默认初始 |
| 01 | 生成中 | {02 已生成} | AI 生成 ing |
| 02 | 已生成 | {00 草稿(反向), 03 已发布} | 反向边 02→00 重新生成 |
| 03 | 已发布 | {} | 终态;客户可下载 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- monitoringPlan 3 值白名单(604)
- alertChannels / iotDeviceTypes CSV 字典白名单(每个值都在 4 值集合,604)
- 反向边 02→00 必填 reason(可空)
- FK 校验:projectId 必(702)
- generatedAt 服务端计算

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/manual-ops/ai/generate/{id}` — 调用 §F5.3 `ops-manual-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 mock 已实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F5.3 行)。

mock 输出 5 章节(基于 monitoringPlan + alertChannels CSV + iotDeviceTypes CSV):
1. **监控部署**:按 monitoringPlan 输出标准配置(Prometheus exporter / 阿里云监控 Agent / Zabbix Agent)
2. **告警渠道配置**:按 alertChannels CSV 输出每个渠道的 webhook 配置 + 模板
3. **IoT 农情设备巡检 SLA**:按 iotDeviceTypes CSV 输出每类设备的巡检周期 / 告警阈值
4. **备份策略**:DB 全备/增备 + Redis RDB+AOF + 配置文件备份
5. **应急预案**:5 大场景标准应急步骤

### 5.3 路线图

- v0.3: 真实 AI 接入 / 自动生成 Ansible playbook
- v0.3: PagerDuty / 飞书事件平台集成
- v0.5+: 知识库 wiki / 演练记录

---

## 6. 验收标准

**PRD §F5.3 验收**:
- ⏳ **AI 一键生成运维手册**(本期 mock 5 章节)
- ⏳ **4 农情设备 SLA**(本期 iotDeviceTypes 4 值字典就位)
- ⏳ **告警渠道 4 选**(dingtalk/feishu/wework/email 字典就位)

**模块特有验收**(本会话已落地):
- 4 态状态机 + 反向边 02→00 单测覆盖
- monitoringPlan / alertChannels / iotDeviceTypes 字典白名单(604)
- FK 校验:projectId 必(702)
- generatedAt 服务端计算

---

## 7. 不做的事 — 详 §1.3

- 事件管理 / 脚本生成 / 知识库 / IoT 大屏 / 演练记录 / 多客户 SLA 大屏

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [ManualOps-数据库设计.md](../02-设计/ManualOps-数据库设计.md)
- API 设计: [ManualOps-API设计.md](../02-设计/ManualOps-API设计.md)
- 测试计划: [ManualOps-测试计划-2026-05-17.md](../04-测试/ManualOps-测试计划-2026-05-17.md)
- 发布计划: [ManualOps-发布计划-2026-05-17.md](../05-上线/ManualOps-发布计划-2026-05-17.md)
- 原型: [opsmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/opsmanual.html)
- AgriAI PRD: [§F5.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [ManualProduct-PRD.md](ManualProduct-PRD.md) / [ManualImpl-PRD.md](ManualImpl-PRD.md)(姊妹模块)

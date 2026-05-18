# PRD: ManualImpl 模块 — 实施手册 (F5.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F5.2 (AgriAI-PLM-完整PRD文档.md §F5.2 实施手册) |
| 原型 HTML | [implmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/implmanual.html) (3 selects: deployMode/osType/dbType + envConfig + imContent + imStatus 4 态) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | 反向边 02→00 由 Service 层校验 |
| 关联 OKR | _2026 Q2-O5-KR2: ManualImpl 模块上线,客户实施部署时间降 50%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "ManualImpl (F5.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 客户侧实施部署当前走"飞书消息抄文档 + 远程协助",4 个具体问题:

1. **实施手册按客户重新写**:每个客户的部署环境不同(Docker vs K8s vs 裸机 / CentOS vs Ubuntu vs Kylin / MySQL vs PG vs 信创 Kingbase),**Q1 5 个客户写了 5 份大同小异的实施手册,共耗费 50h**。
2. **环境变量配置易出错**:实施工程师手写 .env 文件经常拼错变量名,**Q1 出现过 7 次"客户环境启动失败因 DB_PASSWORD 拼成 DB_PSW"**,远程支持耗费 12h。
3. **回滚预案缺位**:很多实施手册不写回滚预案,**Q1 客户某次升级失败回滚要 admin 远程指挥 4h**。
4. **信创合规适配缺位**:Kingbase / Kylin OS / 国产化栈的部署说明不沉淀,**每个客户都要重新踩坑**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 15 份完整实施手册数据,做"3 维度选型(deployMode×osType×dbType)+ AI 一键生成 + 回滚预案 + 信创适配"。

**衡量指标**:
- **AI 实施手册生成时间 ≤ 10 分钟**(本期 mock 即时返回 5 章节)
- **客户实施部署时间降 50%**(基线 4 天 → 目标 2 天)
- **环境变量配置错误率降 90%**(基线 7 次/季度 → 目标 0-1 次)
- **回滚成功率 ≥ 95%**(预案就位)
- **信创栈适配率 100%**(dbType=kingbase + osType=kylin 组合可生成)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **实施工单管理**(客户提单 / 工程师分派 / 进度跟踪)— 留 v0.3
- **实施现场视频录屏 / 在线远程协助** — 留 v0.5+
- **实施环境自动健康检查脚本** — 留 v0.3
- **多客户环境差异对比** — 留 v0.5+
- **实施成本核算 / SLA 统计** — 留 v0.3 走 Analytics
- **客户自助实施门户**(客户自己跑实施)— 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **实施工程师 (Implementer)** | CRUD 自己负责的 manualimpl | 选 3 维度 / 触发 AI / 输出手册 |
| **客户 IT 工程师** | 查看 | 按手册执行部署 |
| **管理员** | 全 CRUD + 发布 | 评审 + 沉淀模板 |
| **DevOps** | 协作 | 共享部署 know-how |

### 2.2 典型场景

**S1 AI 生成实施手册**(最高频)
> 实施工程师小李接到客户 A 项目 → 进入实施手册菜单 → 新建 → title "客户 A AgriPLM 实施手册 v1.0" + deployMode="docker_compose"(3 值字典)+ osType="centos7"(3 值字典)+ dbType="postgresql14"(3 值字典)+ envConfig JSON(DB_PASSWORD/REDIS_HOST/...)→ 点 "AI 生成" → mock 输出 5 章节 content Markdown:1.环境准备 2.部署步骤 3.环境变量 4.农情大屏接入 5.回滚预案 → status='00→01 生成中→02 已生成'

**S2 信创栈适配**(关键流程)
> 客户 B 是政府项目要信创栈 → 选 deployMode="kubernetes" + osType="kylin"(银河麒麟)+ dbType="kdb"(人大金仓)→ mock 输出信创专项实施手册(含 K8s + Kylin + Kingbase 三件套部署指令)

**S3 反向边重新生成**(关键反向边)
> 实施工程师发现 deployMode 选错应改 K8s → status='02→00 草稿'(**反向边 02→00**)→ 调整后重新 AI 生成

**S4 发布给客户**(终态)
> 评审通过 → status='02→03 已发布' → 客户 IT 工程师按手册操作

**S5 实施踩坑沉淀**(关键流程)
> 客户实施过程中发现新坑(某 OS 版本特殊) → 工程师编辑本手册的 content,补到"已知问题"段 → 下次同客户复用

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "ManualImpl (F5.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: manualimplId / manualimplNo (`IM-YYYY-NNNN`) / projectId(FK 必)
- 3 维度选型: deployMode(3 值:docker_compose/kubernetes/baremetal)/ osType(3 值:centos7/ubuntu20/kylin)/ dbType(3 值:postgresql14/mysql8/kdb 信创)
- 配置: envConfig(JSON 环境变量)
- AI: content(Markdown 5 章节)/ aiGenerated / generatedAt
- 输出: outputFormats(4 值 CSV)
- 流程: status(4 态含反向边 02→00)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) manual-impl 行:4 态含反向边 02→00。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 生成中} | 默认初始 |
| 01 | 生成中 | {02 已生成} | AI 生成 ing |
| 02 | 已生成 | {00 草稿(反向), 03 已发布} | 反向边 02→00 重新生成 |
| 03 | 已发布 | {} | 终态;客户可下载 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- deployMode / osType / dbType 各 3 值白名单(604)
- outputFormats CSV 字典白名单(word/pdf/html/markdown,604)
- 反向边 02→00 必填 reason(可空)
- FK 校验:projectId 必(702)
- generatedAt 服务端计算

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/manual-impl/ai/generate/{id}` — 调用 §F5.2 `impl-manual-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🟡 mock 已实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F5.2 行)。

mock 输出 5 章节:
1. **环境准备**:按 deployMode + osType 输出标准命令(yum/apt/zypper)
2. **部署步骤**:Docker Compose YAML / K8s Helm Chart / 裸机脚本三选一
3. **环境变量**:基于 envConfig JSON 输出 .env.example
4. **农情大屏接入**:IoT 设备连接 + 数据采集
5. **回滚预案**:5 步标准回滚命令

特殊适配:
- dbType=kdb(信创)→ 加 Kingbase 专项步骤(licence 配置 / 字符集 GB18030)
- osType=kylin → 加银河麒麟特殊兼容性提示

### 5.3 路线图

- v0.3: 真实 AI 接入 / 实施工单管理
- v0.3: 健康检查脚本自动生成
- v0.5+: 视频录屏 / 在线协助 / 客户自助门户

---

## 6. 验收标准

**PRD §F5.2 验收**:
- ⏳ **AI 一键生成实施手册**(本期 mock 5 章节)
- ⏳ **3 维度部署环境支持**(deployMode × osType × dbType)
- ⏳ **信创适配**(Kingbase + Kylin 组合 mock 就位)

**模块特有验收**(本会话已落地):
- 4 态状态机 + 反向边 02→00 单测覆盖
- 3 维度字典各 3 值白名单(604)
- FK 校验:projectId 必(702)
- generatedAt 服务端计算

---

## 7. 不做的事 — 详 §1.3

- 工单管理 / 视频协助 / 健康检查 / 多客户对比 / 成本核算 / 自助门户

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [ManualImpl-数据库设计.md](../02-设计/ManualImpl-数据库设计.md)
- API 设计: [ManualImpl-API设计.md](../02-设计/ManualImpl-API设计.md)
- 测试计划: [ManualImpl-测试计划-2026-05-17.md](../04-测试/ManualImpl-测试计划-2026-05-17.md)
- 发布计划: [ManualImpl-发布计划-2026-05-17.md](../05-上线/ManualImpl-发布计划-2026-05-17.md)
- 原型: [implmanual.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/implmanual.html)
- AgriAI PRD: [§F5.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [ManualProduct-PRD.md](ManualProduct-PRD.md) / [ManualOps-PRD.md](ManualOps-PRD.md)(姊妹模块)

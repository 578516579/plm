# PRD: DbDesign 模块 — 数据库设计 (F3.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F3.2 (AgriAI-PLM-完整PRD文档.md §F3.2 数据库设计) |
| 原型 HTML | [dbdesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/dbdesign.html) (erDiagram + dbDict + dbSql + normalizationCheck) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O3-KR2: PLM 数据库设计模块上线,AI 生成 ER 图准确率 ≥ 75%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "DbDesign (F3.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前数据库设计走 "Navicat 设计器 + 文档贴 DDL",4 个具体问题:

1. **ER 图与代码 DDL 不一致**:Navicat 画完 ER 后,实际开发的 DDL 有改动但 ER 图忘了同步,**Q1 复盘"为什么 user 表多了 status 字段但 ER 图里没有"无法定位**,只能从代码倒推。
2. **范式规范无校验**:命名(snake_case vs camelCase)、索引设计(单字段 vs 联合索引)、范式(1NF/2NF/3NF)全靠人肉 review,**Q1 因索引设计不当导致 1 次线上慢查询事故**。
3. **数据字典缺位**:某字段语义(status='02' 是什么意思 / category 字典哪几个值)散落在飞书文档 / 代码注释 / 飞书会议,**新人对接 1 个模块平均花 4 小时翻历史资料**。
4. **农业数据特性无规范**:农业数据有 IoT 时序 / 地理坐标(china_coord) / 农作物分类(crops dict)等专项,**没人定过这些字段的统一类型与命名约定**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 30 份数据库设计数据,做到"ER + 字典 + DDL"三件套 AI 生成,设计阶段拦截规范问题。

**衡量指标**:
- **AI 生成 ER 准确率 ≥ 75%**(架构师采纳 AI ER 不需大改的比例)
- **DDL 与 ER 一致率 ≥ 95%**(发布前比对一致)
- **规范检查覆盖率 100%**(每份 dbdesign 必跑 normalizationCheck)
- **数据字典完整度 ≥ 90%**(每个字段都有中文释义 + 取值范围)
- **农业字段标准化采纳率 ≥ 70%**

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **数据库迁移自动化**(基于 ER 变更自动生成 ALTER SQL)— 留 v0.3
- **慢查询自动诊断**(基于 DDL 推荐索引)— 留 v0.5+
- **多数据库引擎对比**(同时生成 MySQL/PG/Kingbase 三套 DDL)— 仅按 dbEngine 选择单引擎,留 v0.3
- **数据库性能压测**(JMeter / sysbench 集成)— 留 v0.5+
- **ER 图实时协作**(类 Lucid 多人编辑)— 仅 Mermaid 文本,留 v0.5+
- **跨项目字段命名规范引擎**(根据团队规则强校验)— 仅 normalizationCheck 字段记录违反点,留 v0.3 自动纠正

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **DBA / 架构师** | CRUD 自己项目的 dbdesign | 触发 AI 生成 / 调整 ER / 出 DDL |
| **评审 admin** | 全 CRUD + 决策 | 评审规范合规性 |
| **开发 (Dev)** | 查看 + 评论 | 读 dataDictionary + ddlScript 作开发依据 |
| **测试** | 查看 | 读 ER 设计测试数据(testdata 模块联动)|

### 2.2 典型场景

**S1 AI 生成 ER + 字典 + DDL**(最高频)
> 张 DBA 接到"灌溉项目"数据库设计 → 进入数据库设计菜单 → 新建 → title "智慧灌溉 v1.0 数据库" + 关联 archId=ARCH-12 + dbEngine="postgresql" → 点 "AI 生成" → mock 输出:
> 1. erDiagramContent: Mermaid erDiagram 含 5 张表(灌溉计划/IoT 设备/采集数据/操作日志/告警记录)
> 2. dataDictionary: Markdown 表格 60 个字段,含中文释义 + 取值范围
> 3. ddlScript: CREATE TABLE 集合(PG 方言)
> 4. normalizationCheck: JSON 检查 25 项(命名 ∧ 索引 ∧ 范式 ∧ 农业字段标准 ∧)

**S2 DBA 微调 + 规范检查**(关键流程)
> 张 DBA 看 mock 发现 "device_id 未建索引但常用查询" → 改 ddlScript 加 INDEX → normalizationCheck 重跑 → status='00→01 评审中'

**S3 评审打回**(反向边路径)
> 评审 admin 发现 "缺少农业地理坐标字段类型规范(经纬度精度)" → 改 status='01→00 草稿'(反向边)+ 备注 → DBA 补字典约定:`latitude DECIMAL(10,7)` / `longitude DECIMAL(10,7)` 全项目统一

**S4 已确认 → DDL 落地**(关键流程)
> 评审通过 → status='01→02 已确认' → 运维人员从 ddlScript 字段直接 copy 在测试 DB 执行 → 开发可参照 dataDictionary 写 mapper

**S5 dbdesign 废弃 → 走数据迁移**(终态)
> 重大 schema 重构 → status='02→03 已废弃' → 老版本保留为迁移参考,新版本立项另起

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "DbDesign (F3.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: dbdesignId / dbdesignNo (`DB-YYYY-NNNN`) / projectId(FK 必)/ archId(FK 可空)
- 选型: dbEngine(3 值字典:mysql/postgresql/kingbase)
- AI 输出: erDiagramContent(Mermaid)/ dataDictionary(Markdown)/ ddlScript(SQL)/ normalizationCheck(JSON)
- 流程: status(4 态)/ authorUserId / reviewerUserId / aiGenerated / aiGeneratedAt

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) dbdesign 行(同 arch 4 态模式):

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 评审中} | 默认初始 |
| 01 | 评审中 | {00 草稿(打回), 02 已确认} | 反向边 01→00 评审打回 |
| 02 | 已确认 | {03 已废弃} | 终态分支 |
| 03 | 已废弃 | {} | 终态 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- 反向边 01→00 必填 reviewNote(602)
- dbEngine 字段白名单校验(604,3 值合法)
- FK 校验:projectId 必,archId 可空但若填必须存在(702)

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/dbdesign/ai/generate/{id}` 或 `POST /business/dbdesign/ai/er` — 调用 §F3.2 `db-design-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🔴 未实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F3.2 行)— 本期占位 mock。

mock 输出结构:
- erDiagramContent: Mermaid erDiagram 5 张农业典型表(plan/device/sensor_data/log/alert)
- dataDictionary: 标准 Markdown 表格(60 字段,含农业专项类型规范)
- ddlScript: 按 dbEngine 选 PG / MySQL / Kingbase 方言
- normalizationCheck: 25 项检查 JSON

### 5.3 路线图

- v0.3: 真实 AI 接入 + 基于历史数据训练 ER 推荐
- v0.3: ER 变更自动生成迁移 SQL
- v0.5+: 慢查询诊断 + 索引推荐

---

## 6. 验收标准

**PRD §F3.2 验收**:
- ⏳ **AI 生成 ER 实体关系图**(本期 mock Mermaid)
- ⏳ **AI 生成数据字典**(本期 mock Markdown 表)
- ⏳ **规范化检查**(本期 normalizationCheck JSON 25 项)

**模块特有验收**(本会话已落地):
- 4 态状态机 + 反向边 01→00 单测覆盖
- dbEngine 3 值白名单(604)
- 反向边必填 reviewNote(602)
- FK 校验:projectId 必、archId 可空(702)

---

## 7. 不做的事 — 详 §1.3

- 迁移自动 / 慢查询 / 多引擎并行 / 性能压测 / ER 实时编辑 / 命名规则引擎

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计文档: [Dbdesign-数据库设计.md](../02-设计/Dbdesign-数据库设计.md)
- API 设计: [Dbdesign-API设计.md](../02-设计/Dbdesign-API设计.md)
- 测试计划: [Dbdesign-测试计划-2026-05-17.md](../04-测试/Dbdesign-测试计划-2026-05-17.md)
- 发布计划: [Dbdesign-发布计划-2026-05-17.md](../05-上线/Dbdesign-发布计划-2026-05-17.md)
- 原型: [dbdesign.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/dbdesign.html)
- AgriAI PRD: [§F3.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Arch-PRD.md](Arch-PRD.md)(dbdesign.archId FK)

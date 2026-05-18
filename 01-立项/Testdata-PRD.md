# PRD: TestData 模块 — 测试数据工厂 (F4.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | F4.3 (AgriAI-PLM-完整PRD文档.md §F4.3 测试数据工厂) |
| 原型 HTML | [testdata.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testdata.html) (td-table 5 选 + td-count + td-format + 4 规则 checkbox + AI 识别字段语义) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | — |
| 关联 OKR | _2026 Q2-O4-KR2: TestData 模块上线,测试数据准备时间降 80%_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "TestData (F4.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前测试数据准备走"手写 SQL INSERT + Excel 凑数",4 个具体问题:

1. **测试数据准备耗时长**:每个 Sprint 测试要造 100-500 条业务数据,**测试经理写 INSERT SQL 平均花 2-4 小时**;Q1 累计耗费数据准备工时 ≥ 30h。
2. **数据不真实导致 bug 逃逸**:Excel 凑的数据"用户名 = test1/2/3"、坐标全是 0/0,**测试通过的功能上线后真实数据触发 bug**,Q1 出现 2 次。
3. **农业数据规则无沉淀**:农业场景有特殊规则(中国农田坐标范围 / IoT 时序连续性 / 传感器数值合理范围 / 异常值边界),**测试不熟悉农业,造的数据农艺师一看就假**。
4. **数据 → 测试用例孤立**:测试用例(TestCase 模块)需要的数据没自动准备,**测试人员要手动把用例和数据 join**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 100 份 TestData 数据集,做"AI 识别字段语义 + 4 大农业规则约束 + 3 种格式输出"让测试数据准备时间降 80%。

**衡量指标**:
- **测试数据准备时间降 80%**(基线 2-4h → 目标 0.5h)
- **农业规则采纳率 ≥ 70%**(4 条规则平均启用 ≥ 2.8 条)
- **AI 字段语义识别准确率 ≥ 80%**(产出字段语义 JSON,人工微调比例 < 20%)
- **数据复用率 ≥ 50%**(同类测试用例复用 testdata 实例)
- **生成数据通过率 ≥ 95%**(造数据导入数据库不报错)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **生产数据脱敏导出**(从 prod 库脱敏抽样作测试)— 留 v0.3
- **数据集版本管理**(同一 dataset v1 vs v2)— 仅 generatedAt 时间戳,版本管理留 v0.5+
- **跨表关联数据生成**(主表 + 关联子表保持 FK 一致)— 仅单表生成,关联生成留 v0.3
- **数据集分享 / 团队复用市场** — 留 v0.5+
- **实时回归数据生成**(CI 拉起测试时自动造数据)— 留 v0.3 pipeline 联动
- **隐私合规自动校验**(造的数据是否包含 PII)— 留 v0.3

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **测试 (QA)** | CRUD 自己创建的 testdata | 选表 / 设规则 / 生成 / 导入 DB |
| **测试经理 (QA Lead)** | 全 CRUD | 维护 5 个常用 targetTable 模板 |
| **AI 工程师** | 维护 fieldSemantics 规则库 | 优化 AI 识别准确率 |
| **管理员** | 全 CRUD | 跨项目数据集管理 |

### 2.2 典型场景

**S1 AI 生成农业 IoT 测试数据**(最高频)
> 王 QA 要测灌溉接口需要 1000 条 IoT 传感器数据 → 进入 TestData 菜单 → 新建 → title "灌溉接口压测数据 1k" + targetTable="iot_sensor_data"(5 选其一:user/order/sensor/farm/irrigation_plan)+ generateCount=1000 + outputFormat="sql"(3 选:json/sql/csv)+ 启用 4 规则:中国坐标 ∧ 时间连续 ∧ 传感器范围 ∧ 包含异常值 → 点 "AI 生成" → mock 输出 fieldSemantics JSON("soil_moisture 是 0-100 含水率")+ generatedContent SQL 1000 行 → status='00→01 已生成'

**S2 数据导入测试库**(关键流程)
> QA copy generatedContent → 在测试数据库执行 → 1000 条数据落库 → 跑灌溉接口压测

**S3 数据归档**(终态)
> 测试结束 → status='01→02 已归档' → 列表过滤但保留可复用

**S4 农业规则约束验证**(关键特性)
> 测试不熟农业 → 启用 "中国坐标" → AI 生成的 latitude 限制在 [18°, 53°] / longitude 限制在 [73°, 135°];启用 "传感器范围" → soil_moisture ∈ [0,100],温度 ∈ [-30, 50];启用 "异常值" → 5% 数据含极值用于边界测试

**S5 字段语义识别**(AI 能力)
> 用户选 targetTable="user" → AI 识别 fieldSemantics:`{"username":"中文姓名","email":"邮箱","phone":"中国手机","created_at":"过去 1 年"}` → 用户可微调

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "TestData (F4.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: testdataId / testdataNo / projectId(FK 必)
- 用户输入: title / targetTable(5 值字典)/ targetTableLabel(中文)/ generateCount / outputFormat(3 值:json/sql/csv)
- 4 规则: ruleChinaCoord / ruleTimeContinuity / ruleSensorRange / ruleIncludeOutliers(各 Y/N)
- AI 输出: fieldSemantics(JSON)/ generatedContent / generatedAt
- 流程: status(3 态)/ authorUserId / aiGenerated

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) testdata 行:3 态单向。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已生成} | 默认初始 |
| 01 | 已生成 | {02 已归档} | AI 生成完成,可下载使用 |
| 02 | 已归档 | {} | 终态;保留复用 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- targetTable 5 值字典白名单(604)
- outputFormat 3 值字典白名单(604)
- generateCount > 0 校验(602,本期上限 10000)
- FK 校验:projectId 必(702)
- generatedAt 服务端计算,生成时自动填

---

## 5. AI 能力

### 5.1 AI 端点

`POST /business/testdata/ai/generate/{id}` 或 `POST /business/testdata/generate` — 调用 §F4.3 `data-gen-flow` Dify 工作流。

### 5.2 当前阶段实现 (mock)

🔴 未实现(详 [PRD-MAPPING.md §6](../PRD-MAPPING.md) F4.3 行)— 本期占位 mock。

mock 输出策略:
- fieldSemantics 识别:基于 targetTable 推 5 字段的语义(JSON)
- 中国坐标:latitude [18, 53] / longitude [73, 135]
- 时间连续:每 5 分钟一条 IoT 数据 / 工作日工作时间
- 传感器范围:soil_moisture [0,100] / temp [-30,50] / humidity [0,100]
- 异常值 5%:5% 数据含 NaN / out-of-range

### 5.3 路线图

- v0.3: 真实 AI 接入 / 跨表关联生成
- v0.3: 脱敏抽样 / pipeline 联动
- v0.5+: 数据集分享 / 隐私合规自动校验

---

## 6. 验收标准

**PRD §F4.3 验收**:
- ⏳ **AI 生成农业真实感数据**(本期 4 规则约束就位)
- ⏳ **3 种格式输出**(json/sql/csv)
- ⏳ **字段语义识别**(本期 mock fieldSemantics JSON)

**模块特有验收**(本会话已落地):
- 3 态状态机合法转换单测覆盖
- targetTable / outputFormat 字段白名单(604)
- generateCount 上限校验(602,≤10000)
- FK 校验:projectId 必(702)
- 4 规则各自约束逻辑单测覆盖

---

## 7. 不做的事 — 详 §1.3

- 脱敏抽样 / 版本管理 / 跨表关联 / 数据集分享 / 实时 CI 生成 / 隐私合规

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Testdata-数据库设计.md](../02-设计/Testdata-数据库设计.md)
- API 设计: [Testdata-API设计.md](../02-设计/Testdata-API设计.md)
- 测试计划: [Testdata-测试计划-2026-05-17.md](../04-测试/Testdata-测试计划-2026-05-17.md)
- 发布计划: [Testdata-发布计划-2026-05-17.md](../05-上线/Testdata-发布计划-2026-05-17.md)
- 原型: [testdata.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testdata.html)
- AgriAI PRD: [§F4.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [TestCase-PRD.md](TestCase-PRD.md)(配合测试用例消费)

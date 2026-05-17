# PRD: TestData 模块 — 测试数据工厂 (F4.3)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD §F4.3 + 原型 testdata.html) |
| 作者 | Wjl |
| PRD § | F4.3 (AgriAI-PLM-完整PRD文档.md §F4.3 测试数据工厂) |
| 原型 HTML | [testdata.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testdata.html) (modal-newtd + tdPreview + fieldSemantics) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "TestData (F4.3)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(测试数据手写 SQL 耗时 / 真实感差 / 农业坐标 / 时间序列 / 传感器范围三类农业约束难手工保证 / 边界异常数据靠经验)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 PRD §F4.3 验收标准 + 模块特有衡量指标(AI 生成 1000 条耗时 / 农业约束符合率)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **直接落库执行** — 仅生成 SQL/JSON/CSV 文本,执行留 v0.5+
- **数据脱敏 / 个保合规** — 单纯测试数据,合规留 v0.5+
- **历史数据快照回溯** — 留 v0.5+
- **真实物联网数据回放** — 仅模拟,真实回放留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:QA / 测试 / 开发 / 数据工程师。

### 2.2 典型场景

**S1 AI 生成农业测试数据**(最高频)
<待人工填写>:1 段叙述,引原型 td-table(5 表选)+ td-count + td-format(json/sql/csv)+ ruleChinaCoord/ruleTimeContinuity/ruleSensorRange/ruleIncludeOutliers 4 个农业约束开关 → 调 Dify → tdPreview 输出

**S2 字段语义识别**(关键能力)
<待人工填写>:fieldSemantics JSON 自动识别表字段语义(坐标/温度/湿度/时间/姓名等),驱动智能生成

**S3 包含异常值**(边界测试)
<待人工填写>:ruleIncludeOutliers=Y 启用,覆盖边界 / 极端测试

**S4 数据集归档**(终态)
<待人工填写>:02 已归档,可下载历史数据

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "TestData (F4.3)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: testdataId / testdataNo(TD-YYYY-NNNN)
- 用户输入: title / targetTable / targetTableLabel / generateCount / outputFormat
- 农业约束: ruleChinaCoord / ruleTimeContinuity / ruleSensorRange / ruleIncludeOutliers
- AI 输出: fieldSemantics(JSON)/ generatedContent / generatedAt / aiGenerated
- 流程: status(3 态) / authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) testdata 行:`00→01→02` (3 态)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 草稿 | {01 已生成} | 默认初始状态 |
| 01 | 已生成 | {02 已归档} | 调用 AI 生成后转入 |
| 02 | 已归档 | {} | 终态 |

**特殊规则**:
- 新建强制 status='00'(违反抛 601)
- targetTable 5 个白名单(604)
- outputFormat 3 个白名单(json/sql/csv)抛 604

---

## 5. AI 能力

### 5.1 AI 端点
POST /business/testdata/ai/generate/{id} — Dify 工作流 data-gen-flow(详 PRD-MAPPING §6)

### 5.2 当前阶段实现
🔴 未实现(详 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md) F4.3 行)— 本期占位 mock(按 targetTable + generateCount + 农业约束生成模板数据)。

### 5.3 mock 输出 / Dify 工作流
<待人工填写>:类似 Inception-PRD §5.3-5.4 — Dify 实接入需对接字段语义模型,留 v0.5+。

---

## 6. 验收标准

**PRD §F4.3 验收**:
- ⏳ 1000 条数据生成时间 < 30s
- ⏳ 农业约束符合率 100%(中国农田坐标 / 传感器范围 / 时间连续)

**模块特有验收**:
<待人工填写>:E2E 测试 / 4 个农业约束开关验证 / 字典白名单。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Testdata-数据库设计.md](../02-设计/Testdata-数据库设计.md)
- API 设计: [Testdata-API设计.md](../02-设计/Testdata-API设计.md)
- 测试计划: [Testdata-测试计划-2026-05-17.md](../04-测试/Testdata-测试计划-2026-05-17.md)
- 发布计划: [Testdata-发布计划-2026-05-17.md](../05-上线/Testdata-发布计划-2026-05-17.md)
- 原型: [testdata.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testdata.html)
- AgriAI PRD: [§F4.3](../prd和原型/AgriAI-PLM-完整PRD文档.md)

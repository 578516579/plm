---
name: test-case-designer
description: PLM 测试用例库设计 — 从测试计划 + PRD 状态机 + 错误码 产具体 TC-NNN 用例 (Given-When-Then 格式)。当用户说"设计测试用例 / 写 test case / TC / 用例库 / 等价类 / 边界值 / 状态机覆盖"时调用。输出: 04-测试/测试用例库/<模块>/TC-NNN.md。**tester agent 的子工具** — agent §2.2 触发。
---

# test-case-designer — 测试用例设计 skill v0.1

**tester agent 的子工具**, 主走 §2.2 测试用例库维护职责。

输入: 测试计划覆盖矩阵 → 输出: 具体 Given-When-Then 用例,可直接转 test-engineer 写代码。

---

## 1. 何时调用

- 用户说 "设计测试用例 / 写 TC / 等价类 / 边界值"
- tester agent §2.2 触发
- 测试计划完成后, 进入用例细化
- 新增/修改 PRD 字段时增量加用例

---

## 2. 必读

1. `04-测试/<模块>-测试计划.md` — 覆盖矩阵
2. `PRD-MAPPING.md §<模块>` — 状态机转换矩阵 + 错误码
3. `04-测试/测试用例库/<其他>/` — 类似模块用例作模板

---

## 3. 用例分组 (per tester agent §2.2)

每模块用例库必含 6 类:

### 3.1 CRUD (P0 必含)

- TC-001 创建成功
- TC-002 创建参数缺失 → 400
- TC-003 查询单条
- TC-004 查询列表 (含分页 / 筛选)
- TC-005 更新成功
- TC-006 更新不存在 → 702
- TC-007 删除 (软删 del_flag='2')

### 3.2 状态机 (P0 必含每条边)

按状态转换矩阵, 每条合法边 1 用例 + 每条非法边 1 用例 (抛 601):

```
合法边 (5×5 矩阵中 ✓ 位置, 含反向边):
  TC-008 draft → review
  TC-009 review → approved
  ...
  TC-NNN rejected → draft (反向边, per proposal 0019)

非法边 (5×5 矩阵中 - 位置, 期望 601):
  TC-NNN draft → archived 跳级 → 601
  TC-NNN approved → draft 倒退 → 601
```

### 3.3 FK 校验 (per proposal 0100)

每个 FK 字段 (projectId / sprintId / assigneeUserId 等) 1 用例验证 702:

- TC-NNN POST 含 projectId=99999 → projectService.checkExists 抛 702

### 3.4 编码 (per proposal 0028, HEX 校验)

- TC-NNN POST body 含中文 (--data-binary @file) → DB HEX 不含 EFBFBD

### 3.5 权限 (per rules.md §A 权限串)

- TC-NNN 用无 `business:<entity>:add` 权限的 token POST → 403

### 3.6 异常路径

- 无效输入 (空 / 超长 / 特殊字符)
- ENUM 越界 → 604
- 进入态必填缺 → 705

---

## 4. 用例模板 (Given-When-Then)

每用例 1 文件: `04-测试/测试用例库/<模块>/TC-NNN.md`

```markdown
## TC-NNN: <一句话标题>

| 字段 | 值 |
|---|---|
| 优先级 | P0 / P1 / P2 / P3 |
| 测试方法 | Unit / E2E / Manual |
| 对应 PRD AC | AC-X |
| 对应错误码 | 702 (如适用) |

**Given**:
  <前置状态>

**When**:
  <用户动作 / API 调用>

**Then**:
  - 响应 code = N
  - DB <表> <字段> = <值>
  - HEX(<字段>) 不含 EFBFBD

**实施**:
  - Playwright spec: `e2e/<module>.spec.ts:<test name>`
  - 或 JUnit: `<Module>ServiceTest#test<scenario>()`
```

---

## 5. 工作流

```
[Step 1] Read 测试计划覆盖矩阵 → 每行 AC + 用例号占位
[Step 2] 按 §3 6 类分组, 列具体用例
[Step 3] 每用例填 Given-When-Then 模板
[Step 4] 自检: 状态机每条边都有用例? FK 都校验? 编码 HEX?
[Step 5] 写到 04-测试/测试用例库/<模块>/ 目录
[Step 6] 更新 E2E 矩阵 (新模块行 / 用例数 +N)
```

---

## 6. 衔接

| 上游 | test-case-designer | 下游 |
|---|---|---|
| test-plan-writer 覆盖矩阵 | → 具体 TC-NNN | → test-engineer (写测试代码) |
| tech-lead 状态机 | → 5×5 用例 | → e2e-validator (执行) |
| 用户报缺陷 | → 复现用例 (复用本 skill) | → defect-triage skill |

---

## 7. 反模式

- ❌ 状态机用例少于矩阵元素 (覆盖不全)
- ❌ FK 校验只测正例不测 702 反例
- ❌ 编码用例缺失 (rules.md §D MUST)
- ❌ Given-When-Then 无具体值 (无法测)
- ❌ 用例代码内联 (用例库只写规格, 实现在 test-engineer)

---

## 8. 历史

| v0.1 | 2026-05-19 | 首版; tester 配套 4 skill 之二 |

# TestCase 测试用例管理 — PRD

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| AgriPLM 来源 | 模块 #13 测试用例 Test Case |
| 模块定位 | `plm-testcase` Maven + `@plm/testcase` npm |
| 路线图 | v0.3 P0（与 Defect 并列） |
| 撰写人 | Wjl |
| 日期 | 2026-05-16 |

---

## 1. 模块定位

测试体系的核心一环。当前 `04-测试/测试用例库/` 维护 markdown 文件（Project-functional.md / E2E-测试矩阵.md 等），现在入数据库便于查询、统计、关联缺陷。

### 1.1 必做 v0.3

- 用例编号自动生成（TC-YYYY-NNNN, ADR-0006）
- 用例分类（功能 / 接口 / 性能 / 安全 / 兼容性 / E2E / 烟雾）
- 优先级（P0 / P1 / P2）
- 关联 Project + Requirement（验证哪个需求）
- 用例状态 5×5（草稿 / 待执行 / 执行中 / 已通过 / 已失败）
- 重复执行：每次跑用例结果可累加（execution_count + last_status + last_executed_at）
- 步骤 / 预期结果 / 实际结果（每次执行可更新 actual_result）
- 自动化标识（is_automated, automation_script_path）

### 1.2 显式不做 v0.4+

- 测试套件（多用例组合）— 用 tags CSV 实现简版
- 用例版本化历史 — 当前 update_time 简版即可

---

## 2. 数据模型

### 2.1 字段（18 个 + 6 通用）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `testcase_id` | bigint PK | | 主键 |
| `testcase_no` | varchar(32) unique | ✅ | `TC-YYYY-NNNN` |
| `project_id` | bigint | ✅ | FK→tb_project |
| `requirement_id` | bigint | ⬜ | FK→tb_requirement，可空 |
| `title` | varchar(200) | ✅ | 用例标题 |
| `description` | text | ⬜ | 概述 |
| `category` | varchar(2) | ✅ | 用例类型（biz_testcase_category） |
| `priority` | varchar(2) | ✅ | 优先级（biz_testcase_priority） |
| `status` | varchar(2) | ✅ | 状态（biz_testcase_status） |
| `preconditions` | text | ⬜ | 前置条件 |
| `steps` | text | ✅ | 测试步骤 |
| `expected_result` | text | ✅ | 期望结果 |
| `actual_result` | text | ⬜ | 实际结果（最近一次） |
| `is_automated` | char(1) | ✅ | Y/N |
| `automation_script_path` | varchar(500) | ⬜ | 如 `plm-frontend/e2e/project.spec.ts` |
| `execution_count` | int | ✅ | 累计执行次数（默认 0） |
| `last_executed_at` | datetime | ⬜ | 最近一次执行时间 |
| `tags` | varchar(200) | ⬜ | CSV 标签 |
| + 通用 6 字段 | | | |

### 2.2 字典

#### `biz_testcase_category`（7）

| 值 | 标签 |
|---|---|
| 01 | 功能 |
| 02 | 接口 |
| 03 | 性能 |
| 04 | 安全 |
| 05 | 兼容性 |
| 06 | E2E |
| 07 | 烟雾 |

#### `biz_testcase_priority`（3）

| 值 | 标签 |
|---|---|
| 00 | P0 关键 |
| 01 | P1 主要 |
| 02 | P2 次要 |

#### `biz_testcase_status`（5×5 状态机）

| 值 | 标签 |
|---|---|
| 00 | 草稿 |
| 01 | 待执行 |
| 02 | 执行中 |
| 03 | 已通过 |
| 04 | 已失败 |

### 2.3 状态机 5×5

```
            00 草稿   01 待执行  02 执行中  03 已通过  04 已失败
00 草稿       —       ✅        ❌         ❌         ❌
01 待执行    ✅        —        ✅         ❌         ❌
02 执行中    ❌       ✅        —          ✅         ✅
03 已通过    ❌       ✅ (重测) ❌         —          ❌
04 已失败    ❌       ✅ (重测) ❌         ❌         —
```

**关键边**:
- 02 → 03/04 (执行完成定结果)
- 03/04 → 01 (反向边: **重新执行**,如修复后重测)

---

## 3. 错误码

| Code | 场景 |
|---|---|
| 200 | 成功 |
| 404 | 用例不存在 |
| 601 | 状态机违规 |
| 602 | 必填字段空 (title / steps / expectedResult) |
| 604 | 字典/格式不合法 |
| 701 | testcase_no 重复 |
| 702 | 关联项目/需求不存在 |
| 706 | is_automated=Y 时必须填 automation_script_path |

---

## 4. 端点清单（7 个）

| # | Method | Path | 权限串 |
|---|---|---|---|
| 1 | GET | `/business/testcase/list` | `business:testcase:list` |
| 2 | POST | `/business/testcase/export` | `business:testcase:export` |
| 3 | GET | `/business/testcase/{id}` | `business:testcase:query` |
| 4 | POST | `/business/testcase` | `business:testcase:add` |
| 5 | PUT | `/business/testcase` | `business:testcase:edit` |
| 6 | DELETE | `/business/testcase/{ids}` | `business:testcase:remove` |
| 7 | POST | `/business/testcase/{id}/execute` | `business:testcase:execute` |

**`/execute` 端点**（独有）：
- body: `{ status: "03"|"04", actualResult: "..." }`
- 服务端: 自动 `execution_count += 1`, `last_executed_at = now`,推状态机 02→{03,04}

---

## 5. 验收

- [ ] 创建用例自动生成 TC-YYYY-NNNN
- [ ] 5×5 状态机含反向边 (03/04→01 重测) 全覆盖
- [ ] is_automated=Y 必填 automation_script_path (706)
- [ ] /execute 端点自动累加 execution_count
- [ ] E2E 8 case 全过

---

## 6. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建 |

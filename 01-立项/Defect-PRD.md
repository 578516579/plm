# Defect 缺陷管理 — PRD

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| AgriPLM 来源 | 模块 #17 缺陷管理 Defects |
| 模块定位 | `plm-defect` Maven + `@plm/defect` npm package |
| 路线图 | v0.3 P0 (按 03-开发/模块拆分指南.md §6) |
| 撰写人 | Wjl |
| 日期 | 2026-05-16 |

---

## 1. 模块定位

### 1.1 核心价值

缺陷管理是 Phase 04 测试阶段的**关键产出物**。当前测试用例库已在 `04-测试/测试用例库/` 维护,缺陷库 `04-测试/缺陷库/` 当前空,需要"入库"。

### 1.2 必须做（v0.3 MVP）

- 缺陷登记 + 编号生成（DEFECT-YYYY-NNNN, ADR-0005）
- 缺陷状态机 5×5（新建 / 已确认 / 处理中 / 已解决 / 已关闭）
- 严重级别（P0 阻塞 / P1 严重 / P2 一般 / P3 轻微）
- 关联 Project / Sprint / Task（3 FK）
- 缺陷归类（功能 / 性能 / 兼容性 / 安全 / 易用性 / 其他）
- 重现步骤 + 期望 vs 实际
- 指派给开发负责人
- 标签（用于过滤,如 "regression"、"flaky"）

### 1.3 显式不做（推迟 v0.4+）

- 截图 / 附件上传（用文件链接代替,沉淀到 OSS 后再做）
- 缺陷复发率统计图表（仪表板 v0.4）
- AI 自动归因 / 智能分派（永不做）
- SLA 自动告警

---

## 2. 用户旅程

### 2.1 测试人员发现缺陷

1. 跑 E2E / 手工测试时发现 Bug
2. 进入"缺陷管理"页面 → 点新增
3. 填写: 标题 / 描述 / 严重级别 / 重现步骤 / 期望 / 实际
4. 关联 Project（必填）+ Sprint（可空）+ Task（可空）
5. 指派给负责开发
6. 缺陷状态 = `00 新建`

### 2.2 开发人员处理

1. 进"我的缺陷"或"缺陷管理 → 过滤 assignee=自己"
2. 缺陷推 `00 → 01 已确认`(承认这是 bug)
3. 开始修复,缺陷推 `01 → 02 处理中`
4. 修完提 MR,缺陷推 `02 → 03 已解决`,填写"解决说明"

### 2.3 测试人员验证

1. 缺陷在 `03 已解决` 状态等待回归测试
2. 跑回归,通过则推 `03 → 04 已关闭`,填"关闭说明"
3. 若验证未通过 → `03 → 01 已确认`(打回开发,反向边)

---

## 3. 数据模型

### 3.1 字段定义（17 字段）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `defect_id` | bigint | PK | 主键 |
| `defect_no` | varchar(32) | ✅ unique | `DEFECT-YYYY-NNNN`（ADR-0005） |
| `project_id` | bigint | ✅ | FK→tb_project |
| `sprint_id` | bigint | ⬜ | FK→tb_sprint，可空（线上回归类缺陷无 sprint） |
| `task_id` | bigint | ⬜ | FK→tb_task，可空（如果是某 task 引入的） |
| `title` | varchar(200) | ✅ | 缺陷标题 |
| `description` | text | ⬜ | 详细描述 |
| `severity` | varchar(2) | ✅ | 严重级别（字典 `biz_defect_severity`） |
| `category` | varchar(2) | ✅ | 缺陷分类（字典 `biz_defect_category`） |
| `status` | varchar(2) | ✅ | 状态（字典 `biz_defect_status`） |
| `assignee_user_id` | bigint | ⬜ | 指派开发 |
| `reporter_user_id` | bigint | ✅ | 报告人（默认当前 user） |
| `reproduce_steps` | text | ⬜ | 重现步骤 |
| `expected_result` | text | ⬜ | 期望结果 |
| `actual_result` | text | ⬜ | 实际结果 |
| `resolution` | varchar(500) | ⬜ | 解决说明（推 03 时填） |
| `tags` | varchar(200) | ⬜ | 标签 CSV（如 `regression,flaky`） |
| + 通用 6 字段 | | | create_by/create_time/update_by/update_time/remark/del_flag |

### 3.2 严重级别字典 `biz_defect_severity`

| 字典值 | 标签 | css_class |
|---|---|---|
| 00 | P0 阻塞 | danger |
| 01 | P1 严重 | warning |
| 02 | P2 一般 | info |
| 03 | P3 轻微 | success |

### 3.3 缺陷分类字典 `biz_defect_category`

| 字典值 | 标签 |
|---|---|
| 01 | 功能 |
| 02 | 性能 |
| 03 | 兼容性 |
| 04 | 安全 |
| 05 | 易用性 |
| 99 | 其他 |

### 3.4 状态机 5×5

| 当前\到 | 00 新建 | 01 已确认 | 02 处理中 | 03 已解决 | 04 已关闭 |
|---|---|---|---|---|---|
| **00 新建** | — | ✅ | ❌ | ❌ | ❌（无效缺陷直接关）|
| **01 已确认** | ❌ | — | ✅ | ❌ | ✅（重复/无效缺陷关闭）|
| **02 处理中** | ❌ | ✅（重新分析）| — | ✅ | ❌ |
| **03 已解决** | ❌ | ✅（**反向边 — 回归打回**）| ❌ | — | ✅ |
| **04 已关闭** | ❌ | ❌ | ❌ | ❌ | —（终态）|

**关键边**：
- 反向边 03→01（回归打回开发,重新确认）
- 00→04 / 01→04（无效缺陷直接关）

---

## 4. 错误码

| Code | 场景 |
|---|---|
| 200 | 成功 |
| 404 | 缺陷不存在 |
| 601 | 状态转换违规 / 新建状态非 00 |
| 602 | 必填字段空 (title / severity / category) |
| 604 | 字典值不合法 |
| 701 | defect_no 重复 |
| 702 | 关联项目/迭代/任务/指派用户不存在 |
| 705 | 进入"已解决"必须填 resolution |

---

## 5. 端点清单（6 个）

| # | Method | Path | 权限串 |
|---|---|---|---|
| 1 | GET | `/business/defect/list` | `business:defect:list` |
| 2 | GET | `/business/defect/{id}` | `business:defect:query` |
| 3 | POST | `/business/defect` | `business:defect:add` |
| 4 | PUT | `/business/defect` | `business:defect:edit` |
| 5 | DELETE | `/business/defect/{ids}` | `business:defect:remove` |
| 6 | POST | `/business/defect/export` | `business:defect:export` |

---

## 6. 验收标准

- [ ] 创建缺陷自动生成 DEFECT-YYYY-NNNN 编号
- [ ] 5×5 状态机 25 case 全覆盖（含反向边 03→01）
- [ ] 进入"已解决"必填 resolution
- [ ] 3 FK 校验（Project 必,Sprint/Task 可空但填则校验）
- [ ] 字典 + 7 菜单 + admin 角色授权
- [ ] E2E 8 case 全过
- [ ] 编码 HEX 校验通过

---

## 7. 变更记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建 |

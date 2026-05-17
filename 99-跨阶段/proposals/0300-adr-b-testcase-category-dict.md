# Proposal 0300 (ADR-B): TestCase `category` 字典口径统一

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0300 |
| 标题 | ADR-B: TestCase `category` 字典口径,SQL 7 值 vs 原型 4 值的对齐方案 |
| 状态 | proposed |
| 类型 | 架构 |
| 提出人 | Claude (PRD-align 第二轮审计) |
| 提出日期 | 2026-05-17 |
| 评审人 | 项目负责人 + QA 角色 + 产品经理(原型对齐方决策) |
| 评审日期 | _(待定)_ |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

`plm-testcase` 模块的 `category` 字段(`biz_testcase_category` 字典)在三个事实来源中**不重叠**:

| 来源 | 取值 | 数量 |
|---|---|---|
| 当前 SQL `business-testcase.sql` 字典 | 功能 / 接口 / 性能 / 安全 / 兼容性 / E2E / 烟雾 | **7 值** |
| 原型 `testcase.html` modal-testcase-add `nca-type` | 功能测试 / 边界测试 / 异常场景 / 性能测试 | **4 值** |
| PRD §F4.2 描述 | "支持农业业务场景专项用例" | 1 个语义补丁,无明确字典 |

**问题**:
- 共同 ✓: 仅 "功能" / "性能" 两类
- 原型独有 ✗: 边界 / 异常 / 农业 (PRD 明确点出"农业专项")
- SQL 独有 ✗: 接口 / 安全 / 兼容性 / E2E / 烟雾 (常见 QA 分类但 PRD/原型不强调)

按规则 §M.1 "三者不一致时 ⇒ 停下来用 AskUserQuestion",项目惯例是"原型优先"(已落地在 19 个 PRD-aligned 模块和 ADR-A/Requirement)。

---

## 2. 证据

- 审计报告 [99-跨阶段/audits/2026-05-17-12-modules-drift-audit.md](../audits/2026-05-17-12-modules-drift-audit.md) §TestCase: 标 🟡 中等
- 原型 [prd和原型/AgriPLM-DevOps-原型/agriplm_split/testcase.html](../../prd和原型/AgriPLM-DevOps-原型/agriplm_split/testcase.html) L151-154 `<select id="nca-type">` 4 个 `<option>`
- PRD [prd和原型/AgriAI-PLM-完整PRD文档.md](../../prd和原型/AgriAI-PLM-完整PRD文档.md) §F4.2 L339-348 "支持农业业务场景专项用例"
- 当前 SQL [plm-backend/sql/business-testcase.sql](../../plm-backend/sql/business-testcase.sql) `biz_testcase_category` 字典数据
- 用户请求:2026-05-17 会话明确"扫剩 3 个 🟡 中等,需 ADR 决策"

---

## 3. 提案

**3 个备选方案**,**推荐 Option B (扩展融合 8 值)**。

### Option A — 严格原型 4 值

字典改成原型 4 值:`功能 / 边界 / 异常 / 农业专项`。
- ✅ 与原型 100% 对齐 (符合"原型优先")
- ✅ 4 值简洁,PM 易理解
- ❌ 丢失现有 5 类 (接口 / 安全 / 兼容性 / E2E / 烟雾) — 这些在实际 QA 工作里有用
- ❌ E2E `testcase.spec.ts` 历史可能依赖某些值,改 SQL 后旧测试可能失败

### Option B — 扩展融合 8 值 ⭐ **推荐**

字典融合两边,共 8 值:
| dict_value | dict_label | 来源 |
|---|---|---|
| functional | 功能 | 原型 ∩ SQL |
| boundary | 边界 | 原型独有 |
| exception | 异常 | 原型独有 |
| agri | 农业专项 | 原型 + PRD §F4.2 |
| api | 接口 | SQL 独有 |
| performance | 性能 | 原型 ∩ SQL |
| security | 安全 | SQL 独有 |
| compatibility | 兼容性 | SQL 独有 |

(舍弃 SQL 的 E2E / 烟雾 — 这两个是测试**层级**而非分类,可由其他维度承载)

- ✅ 原型 4 值全保留(满足"原型优先")
- ✅ 农业专项进入字典(满足 PRD §F4.2)
- ✅ 现有 SQL 的 3 个常用值(接口/性能/安全/兼容性)保留
- ✅ 旧测试用例数据无需迁移(SQL 现有值仍合法)
- ⚠️ 字典从 7→8 值,稍多但可控

### Option C — 严格 SQL 7 值

保持现状不动,只把 PRD §F4.2 "农业专项" 视为 Tag(放 `tags` 字段)。
- ✅ 改动最小
- ❌ 违反"原型优先"惯例
- ❌ 与本会话 19 个 PRD-aligned 模块的对齐方法不一致
- ❌ 实际丢了原型设计意图

### 改动文件清单 (按 Option B)

| 文件 | 改动 |
|---|---|
| `plm-backend/sql/business-testcase.sql` | DROP+重建 `biz_testcase_category` 字典 8 值 |
| `plm-backend/plm-testcase/.../TestCase.java` | `@Excel` dictType 已对,无需改 |
| `plm-backend/plm-testcase/.../TestCaseServiceImpl.java` | 加 `VALID_CATEGORY` 白名单 (604) |
| `plm-backend/plm-testcase/.../TestCaseServiceImplTest.java` | 加 3 个白名单 case (合法/非法/null) |
| `plm-frontend/packages/plm-testcase/src/views/index.vue` | 字典自动加载,无需改 |
| `plm-frontend/e2e/helpers/fixtures-testcase.ts` | 检查现有 fixture 默认值是否在新字典内,如不在则改 |
| `PRD-MAPPING.md §2` | 加 TestCase (F4.2) 字段表 |
| `PRD-MAPPING.md §1` | TestCase 行 🟢 已对齐 → 🟢 **PRD-aligned**;统计 28→29 |

### Diff 草案(关键片段)

```diff
--- a/plm-backend/sql/business-testcase.sql
@@ biz_testcase_category 字典数据
- (1, '功能', 'functional', 'biz_testcase_category', ...),
- (2, '接口', 'api',        'biz_testcase_category', ...),
- (3, '性能', 'performance','biz_testcase_category', ...),
- (4, '安全', 'security',   'biz_testcase_category', ...),
- (5, '兼容性', 'compat',   'biz_testcase_category', ...),
- (6, 'E2E', 'e2e',         'biz_testcase_category', ...),
- (7, '烟雾', 'smoke',      'biz_testcase_category', ...);
+ (1, '功能',     'functional',   'biz_testcase_category', 'Y'),
+ (2, '边界',     'boundary',     'biz_testcase_category', 'N'),
+ (3, '异常',     'exception',    'biz_testcase_category', 'N'),
+ (4, '农业专项', 'agri',         'biz_testcase_category', 'N'),
+ (5, '接口',     'api',          'biz_testcase_category', 'N'),
+ (6, '性能',     'performance',  'biz_testcase_category', 'N'),
+ (7, '安全',     'security',     'biz_testcase_category', 'N'),
+ (8, '兼容性',   'compatibility','biz_testcase_category', 'N');
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 | 仅 testcase 模块;字典变更走 SQL 重新导入;白名单 1 行 |
| Claude | 下次会话沿用 §2 字段表 + ADR-B 决策 |
| 测试 | E2E `testcase.spec.ts` 默认 fixture 可能需要改字典值;dev 环境跑一次确认 |
| 已有代码 / 文档 | 现有 testcase 记录如有 `e2e`/`smoke` 取值 → 迁移到 `functional` 或加为 remark;dev 环境影响可控 |

**migration 量级**: dev 仅(无历史数据),生产部署前评估 — 若已有 `e2e`/`smoke` 值的记录,需写 1 条 UPDATE SQL。

---

## 5. 风险

- **风险 1**: 字典从 7→8 值,如 frontend 表单已硬编码字典选项 → 失效。**缓解**: 检查 `plm-testcase/src/views/index.vue` 是否用 `dict-tag` 字典动态加载(应该是,RuoYi 惯例);grep 确认无硬编码。
- **风险 2**: `e2e`/`smoke` 旧数据迁移漏 → service 白名单抛 604。**缓解**: dev 环境 DROP+重建 SQL;生产部署前写 migration UPDATE。
- **风险 3**: 农业专项实际定义不清 → 字典加了但用户不知道何时用。**缓解**: 字典 remark 注释 "农业 IoT/灌溉/植保 等业务场景专项用例";配套加 1 个 sample testcase 数据(PRJ-2026-0001 项目下)。

---

## 6. 备选方案

详见 §3 — Option A (4 值严格原型) / Option B (8 值融合,**推荐**) / Option C (7 值严格 SQL)。

---

## 7. 实施计划(merged 后,3-commit 范本)

```
[ ] Step 1: 本提案 review + 评审通过 → status: accepted
[ ] Step 2: Commit 1 (docs): PRD-MAPPING §2 加 TestCase 字段表 + ADR-B 记录到该模块决策记录 D1
[ ] Step 3: Commit 2 (feat): SQL 字典 + Service 白名单 + 单测 (引用 Commit 1 hash)
[ ] Step 4: Commit 3 (docs): §1 大表 TestCase 行 🟡→🟢 PRD-aligned;统计 28→29
[ ] Step 5: 通知 QA 团队字典变更
[ ] Step 6: 进入 tracking 期 (2 周观察新字典使用率)
```

---

## 8. 衡量指标

- **信号 1**: `biz_testcase_category` 字典各值的实际使用计数 (sample 100 条 testcase)。
  目标:`functional` 应占主体(~60%),`agri/boundary/exception` 至少各有 ≥5 条数据(说明字典扩值有意义)。
- **信号 2**: PRD-align 进度 28→29 (PRD-MAPPING.md §1 统计自动反映)。

跟踪期:merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| _(待用户拍板)_ | _(通过 / 改方案 / 拒绝)_ | | |

---

## 10. 实施后跟踪 (待 merged 后填)

_(merged 后填)_

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Claude | 初稿,Option B 推荐 |

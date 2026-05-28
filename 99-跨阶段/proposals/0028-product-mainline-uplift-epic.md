# Proposal 0028: 产品主线贯通迭代(epic — 把"31 个 CRUD 合集"升级为"全生命周期产品")

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0028 |
| 标题 | 产品主线贯通迭代 — P0-1 跨模块外键 + P0-2 详情页跨模块跳转 + P0-3 真聚合 TestReport/DORA + P0-4 AiButton 紫渐变组件 + P0-5 Dashboard 错态显形 |
| 状态 | proposed → accepted(User-requested,等开工授权) |
| 类型 | 架构 + 编码规范 + UED(epic 跨域) |
| 提出人 | Claude (PM 视角验收) + Wjl |
| 提出日期 | 2026-05-28 |
| 评审人 | Wjl (solo-review) |
| 评审日期 | 2026-05-28 |
| Tracking 截止 | 2026-06-25(merged 后 4 周) |

---

## 1. 背景(What's the problem?)

2026-05-28 PM 视角端到端产品验收发现:**当前 PLM 是「31 个独立 CRUD 的合集」,不是「项目全生命周期管理」产品**。`PRD-MAPPING.md §1` 31 个模块全绿是事实,但只代表"单模块 CRUD 通过率"。真到 PM 关心的 5 条主线流程:

| 主线 | 实测联通度 | 致命断点 |
|---|---|---|
| 立项 → 项目 | ❌ 断裂 | `Inception.projectId` 字段存在但**无 promoteToProject 服务、无 UI 晋升按钮**,审批通过后必须手动到 project 模块再录一遍 |
| 需求 → 设计 → 任务 | ⚠️ 半通 | 数据外键有(`Task.requirementId`),但前端 business 页面 `router.push` 全仓**只有 3 处**,requirement 详情页没有任何跳 prd/arch/ued/dbdesign/apidesign 的按钮 |
| 研发 → 测试 | ❌ 数据层都断 | `Submission` **无 testplanId 列** / `Defect` **无 testcaseId 列** / `TestReport.totalCases/passedCases/p0Defects` 全是人工填字段,**不是聚合** |
| 发布 → 运维 | ❌ 断裂 | `Pipeline` **无 releaseId 列**, `Release` **无 pipelineId 列**,DORA 4 指标 `aggregate\|compute` 在 plm-dora 模块 grep **0 命中** — 纯空表 |
| AI 增强 | ⚠️ Mock 壳 | `POST /ai/evaluate/{id}` 真存在,但 `RequirementServiceImpl.evalLevel()` 是**正则关键词匹配**;openspec ↔ apidesign **0 引用** |

UI 层另发现 4 类系统性缺陷(详 §3-d):AI 按钮 6/6 模块全用 `el-button type="success"` 绿色伪装紫渐变(违反 §N.3 一票否决项)、`el-table` 空态 5/6 裸默认、dashboard 错态被 `Promise.allSettled` 静默吞、颜色 token 30+ 处裸 hex。

**结论**: 继续加新模块只会让"31 个孤岛"变成"35 个孤岛"。**砍掉新模块开发,启动主线贯通迭代**,把工程基建一流的 PLM 提升到产品贯通二流以上。

---

## 2. 证据(Evidence)

- 关联验收报告:2026-05-28 PM 验收会话(本 proposal 同日)
- 关联 PRD:[prd和原型/AgriAI-PLM-完整PRD文档.md](../../prd和原型/AgriAI-PLM-完整PRD文档.md) §F4.7(testreport AI 自动生成)/ §F1.1→F1.2(立项→项目流转)/ §3.5 Phase 1 AI Agent
- 关联 SSoT:[PRD-MAPPING.md §1](../../PRD-MAPPING.md) 31 模块全绿与"贯通率"反差;[businessRoute.ts](../../plm-frontend/src/utils/businessRoute.ts) 只是 entity→path 映射,**无带参跳转辅助函数**
- 关联代码证据:
  - `Inception.java:37 projectId` 已有字段但全仓 `promoteToProject\|createProjectFromInception` 0 命中
  - `Submission.java:20-23` 字段表无 testplanId;`Defect.java:29-37` 字段表无 testcaseId
  - `Pipeline.java:18-20` / `Release.java:20-24` 互无对方 id
  - 全仓 `aggregateFromTestcase\|recompute\|computeDora` 在 plm-testreport / plm-dora 0 命中
  - 6 模块抽样(dashboard/project/requirement/task/defect/release)AI 按钮全 `type="success"` + ✨ emoji,无紫渐变
- 用户请求:2026-05-28 会话原话 "作为产品经理,对plm产品进行验收,先看下是否符合产品逻辑,流程是否可用,是否通畅,整个产品是否完善可用" → 验收报告产出后用户答 "需要" 同意进入 proposal + 开干

---

## 3. 提案(What's the change?)

5 个 P0 子项,**P0-1 是 P0-2/P0-3 的数据基础,必须先做**;P0-4/P0-5 与数据层正交,可并行。

### P0-1 跨模块外键迁移(4 条新列 + 1 反向)

| # | 表 | 新列 | 类型 | FK 目标 | 业务含义 |
|---|---|---|---|---|---|
| a | `tb_submission` | `testplan_id` | BIGINT NULL | `tb_testplan.id` | 提测拉起测试方案 |
| b | `tb_defect` | `testcase_id` | BIGINT NULL | `tb_testcase.id` | 用例失败→缺陷溯源 |
| c | `tb_release` | `pipeline_id` | BIGINT NULL | `tb_pipeline.id` | 发布→流水线 |
| d | `tb_pipeline` | `release_id` | BIGINT NULL | `tb_release.id` | 流水线→所属发布(反向) |

DORA 不加列(指标本质是 pipeline 历史聚合,走 `aggregate where projectId=? group by date` 即可)。

**改动文件清单**:
- 新建 `plm-backend/sql/business-submission-add-testplan-id.sql` + rollback
- 新建 `plm-backend/sql/business-defect-add-testcase-id.sql` + rollback
- 新建 `plm-backend/sql/business-release-add-pipeline-id.sql` + rollback
- 新建 `plm-backend/sql/business-pipeline-add-release-id.sql` + rollback
- 4 个 Domain Java 加字段 + Mapper XML resultMap + 编辑接口校验(目标 FK 必须同 projectId,否则 702)
- PRD-MAPPING §2(submission/defect/release/pipeline 4 节追加字段)+ §5 URL 不变 + §3 状态机不变

**业务硬规则**:
- 提测晋升 `testplan_id` 必填且 testplan.projectId 必须等于 submission.projectId → 702
- 缺陷可空 testcaseId(自由提交场景)
- release.pipelineId 仅在发布单状态 ≥01(已提交)后必填

### P0-2 详情页跨模块跳转 + 一键生成下游

**扩展 `businessRoute.ts` SSoT**(加 4 个辅助函数,强制走 SSoT 不允许 `router.push` 硬编码):

```ts
// 新增到 businessRoute.ts
export function goEntity(entity: string, id?: number | string): Promise<void>
export function goEntityList(entity: string, query: Record<string, any>): Promise<void>
export function goDetail(entity: string, id: number | string): Promise<void>  // 打开详情 modal/router
export function backToParent(currentEntity: string, parentField: string, parentId: number): Promise<void>
```

**5 个详情页加跳转/晋升按钮**:

| 详情页 | 新增按钮 | 目标 |
|---|---|---|
| `inception/index.vue` 审批通过状态 | "晋升为项目" | 调 `POST /business/inception/{id}/promote-to-project` → 跳转 project 详情 |
| `requirement/index.vue` 详情 | "查看 PRD"/"查看 UED"/"查看架构"/"查看 DB 设计"/"查看接口设计" | `goEntityList('prd', {requirementId})` 等 |
| `submission/index.vue` 详情 | "拉起测试方案" | 弹 testplan 选择,写入 testplanId |
| `testcase/index.vue` 失败状态 | "一键提缺陷" | `goEntity('defect', {testcaseId, projectId})` 携带回填 |
| `release/index.vue` | "触发流水线"/"查看流水线" | `goEntity('pipeline', {releaseId})` |

**后端配套 endpoint**:
- 新建 `InceptionController.promoteToProject(id)`:校验 inception.status=approved,创建 Project(回填 inception.projectId),返回 newProjectId
- `SubmissionController.attachTestplan(id, testplanId)`:校验 projectId 一致,update 字段

### P0-3 TestReport / DORA 真聚合

**TestReport 改造**:
- `TestReportServiceImpl.aggregateFromTestplan(testplanId)` 新方法:从 tb_testcase / tb_defect 按 testplanId 实时统计 totalCases/passedCases/failedCases/p0Defects/p1Defects/coverage
- 状态机:新建(00) → 触发聚合 → 已生成(01) 时回填快照;支持手动覆盖(标记 isManual=Y)
- 前端:列表"刷新聚合"按钮 + 详情显示"聚合时间"/"是否手动覆盖"

**DORA 改造**:
- 新增 `DoraServiceImpl.computeMetrics(projectId, periodStart, periodEnd)`:
  - 部署频率 = COUNT(pipeline WHERE status=success AND triggerType=deploy)
  - 前置时间 = AVG(release.actualReleaseAt - release.createTime)
  - MTTR = AVG(defect.resolvedAt - defect.createTime WHERE severity IN ('blocker','critical'))
  - 失败率 = COUNT(pipeline.status=failed) / COUNT(pipeline.*)
- 定时任务 Quartz `DoraComputeJob` 每日凌晨跑;前端"立即刷新"按钮手动触发
- DoraMetric 表 isComputed 字段标记(区分人工 vs 自动)

### P0-4 AiButton 紫渐变组件

**新建** `plm-frontend/src/components/AiButton/index.vue`:

```vue
<template>
  <el-button class="ai-button" :loading="loading" :disabled="disabled" @click="$emit('click')">
    <span class="ai-icon">✨</span>
    <slot />
  </el-button>
</template>

<style scoped>
.ai-button {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  color: #fff;
  border: none;
}
.ai-button:hover { opacity: 0.9; }
.ai-button:disabled { opacity: 0.5; }
.ai-icon { margin-right: 4px; }
</style>
```

**6 模块替换**(grep 当前 `type="success"` + `✨` 模式批量改):dashboard / requirement(2 处)/ task / defect / release。

### P0-5 Dashboard 错态显形

`dashboard/index.vue:~403` `Promise.allSettled` 失败聚合:

```ts
const results = await Promise.allSettled([...])
const failed = results.filter(r => r.status === 'rejected')
if (failed.length > 0) {
  ElMessage.warning(`${failed.length} 个面板数据加载失败,请检查网络或后端`)
}
```

不再静默 fallback 空数组覆盖,**让用户感知到接口挂了**。

### 改动文件清单(汇总)

| 类别 | 文件数 | 备注 |
|---|---|---|
| SQL 迁移脚本 | 4 + 4 rollback | P0-1 |
| Java Domain/Mapper/Service | ~16 (5 × ~3) | P0-1 + P0-2 后端 endpoint + P0-3 服务 |
| Quartz 任务 | 1 | P0-3 DoraComputeJob |
| 前端 view 改造 | ~6 | P0-2 详情页按钮 + P0-3 刷新按钮 + P0-5 dashboard |
| 前端组件 | 1 新建 | P0-4 AiButton |
| 前端 util 扩展 | 1 (businessRoute.ts) | P0-2 |
| 测试 | ~6 ServiceImpl 单测扩展 + E2E ~3 case | 各 P0 配套 |
| 文档 | PRD-MAPPING §2/§5 + 4 ADR(promote/aggregate/AiButton/businessRoute 扩展) | |

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| 开发者 | 4 张表加字段(向后兼容,NULL allowed);AiButton 强制替换 `type="success"` + ✨ 模式;`router.push('/business/...')` 硬编码进一步禁止(已被 gotcha #8 hook 警告) |
| Claude | 后续业务模块写 AI 按钮统一用 `<AiButton>`;跨模块跳转必走 `businessRoute.ts` 4 辅助函数;新规则进 `.claude/rules.md §N`(UED)+ `§M`(模块) |
| 测试 / 运维 | testreport / DORA 之前手填数字将被首次聚合**覆盖**(本期作为重置可接受;若线上已有真实人填数据需先备份) |
| 已有代码 / 文档 | PRD-MAPPING §2 4 节追加字段;新增 4 ADR;`businessRoute.ts` 扩展不破坏现有 `entityToPath` |

---

## 5. 风险(Risks)

| 风险 | 缓解 |
|---|---|
| 5 个 P0 一次提交 review 困难 | 切 5 个独立 PR(P0-1 SQL 一票 / P0-1 Domain/Service / P0-2 跳转 / P0-3 聚合 / P0-4+P0-5 UI 一票) |
| testreport 之前人填数据被聚合覆盖,丢业务 | DDL 不删现有字段,新增 `is_manual_override` 标志;聚合 service 检测该字段为 Y 时跳过 |
| DORA 聚合 SQL 在大 pipeline 表慢 | 加 `idx_pipeline_project_date_status` 联合索引;Quartz 异步跑,前端不阻塞 |
| 4 表加列在生产是 DDL 锁表 | utf8mb4 + InnoDB Online DDL 8.0 算法,字段都 NULL allowed 不阻写;迁移 SQL 单独 PR 走 db-ops 评审 |
| AiButton 强制替换会撞 ongoing 字典抽取工作 | 协作规范 §4 同模块串行,等当前 11 个字典抽取 commit 完后再做 |
| Inception promote 重复触发 | 服务方法加幂等:inception.projectId IS NOT NULL 时直接返回已有 projectId,不重复建项 |
| **release ↔ pipeline 互引的 Maven 循环依赖**(2026-05-28 P0-1 落地实测发现) | **本期 known limitation**:release/pipeline 互引列(`pipeline_id`/`release_id`)只在 DDL + Domain + Mapper 落地,**应用层 FK 校验缺位**。submission/defect 的强 FK 校验(同 projectId → 702)已正常落,业务主收益保留。**修复路径**:P0-2 或后续单独 ADR 推 SPI 接口 `ProjectScopedLookup` 下沉到 plm-common,各 ServiceImpl `@Component` 实现 + Map 注入(详 backend-coder 2026-05-28 会话报告)。当前缓解:数据层 NULL allowed,业务可正常运作,不强约束同 projectId 不会有重大事故;若需临时强校验,在 ReleaseController/PipelineController 层手工 query 对方表 |

---

## 6. 备选方案(Alternatives Considered)

- **方案 A(本提案)**: 1 epic + 5 P0 子项分批 PR,2 周窗口
- **方案 B**: 5 个独立 proposal — 否,5 子项强依赖(P0-1 是 P0-2/P0-3 基础),独立 proposal 割裂叙事且增加协作锁竞争
- **方案 C**: 不动外键,纯 UI 层做"复制 ID 跳转"贴 — 否,违反 PRD §F4.7(testreport AI 自动生成),DORA 也无法真聚合
- **方案 D**: 等 v0.x 集成模块(MCP)做完再统一贯通 — 否,集成更复杂,反过来需要"贯通后"的稳定数据模型

选 **A**(用户验收会话已隐含同意 "需要")。

---

## 7. 实施计划(Implementation Plan)

```
[ ] Step 1: 本 proposal 起草 + README 状态索引 + 在途任务.md ledger + active-sessions CLAIM(本 commit)
[ ] Step 2: P0-1 4 个外键 SQL + Domain/Mapper/Service 改造(db-orchestrator + backend-coder)
    [ ] 2a: submission + testplan_id (SQL + Domain + Service 单测)
    [ ] 2b: defect + testcase_id
    [ ] 2c: release + pipeline_id
    [x] 2d: pipeline + release_id(反向)— ⚠ **应用层 FK 校验留 known limitation**(详 §5 风险表新增行),DDL + Domain + Mapper XML 已落
[ ] Step 3: P0-4 AiButton 组件 + 6 模块批量替换(frontend-coder + ued-orchestrator 评审)
[ ] Step 4: P0-5 Dashboard 错态显形(frontend-coder + 单测)
[ ] Step 5: P0-2 businessRoute 扩展 + 5 详情页按钮 + 后端 promote/attach endpoint(arch-orchestrator + backend-coder + frontend-coder)
[ ] Step 6: P0-3 TestReport/DORA 聚合服务 + Quartz 任务(backend-coder + db-orchestrator)
[ ] Step 7: PRD-MAPPING §2 + 4 ADR + 4 Gate 实例(technical-writer)
[ ] Step 8: 5 PR 分批合并,每 PR 单独 [solo-review];E2E 全套件回归
[ ] Step 9: 进入 tracking 期,signals 加"主线贯通度"段
```

依赖图:`Step 2(P0-1) → Step 5(P0-2 / P0-6 需 FK)→ Step 6(P0-3 聚合需 FK)`;`Step 3(P0-4) || Step 4(P0-5)` 与 Step 2 并行。

---

## 8. 衡量指标(How will we know it worked?)

| 信号 | 基线(2026-05-28) | 目标(2026-06-25) |
|---|---|---|
| 全仓 `plm-frontend/src/views/business/**` 中 `router.push` 出现次数 | 3 | ≥ 15(5 详情页 × 平均 3 处) |
| `goEntity\|goEntityList\|goDetail\|backToParent` 调用次数 | 0 | ≥ 15 |
| TestReport 详情 isAggregated=Y 的记录占比 | 0% | ≥ 80% |
| DORA 4 指标 isComputed=Y 的记录占比 | 0% | 100% |
| 6 模块 AI 按钮 `type="success"` + ✨ 模式残留 | 6/6 | 0/6 |
| dashboard `Promise.allSettled` 后聚合 toast 出现次数(模拟接口挂时) | 0 | ≥ 1 |
| 跨模块跳转 E2E case 覆盖 | 0 | ≥ 5 |

新增 signals 段:`99-跨阶段/signals/2026-06.md §14 主线贯通度`。

跟踪期:2026-05-28 ~ 2026-06-25。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | 待签字 | 2026-05-28 | User-requested:验收报告产出后明确说 "需要"。预期 solo-review-bypass 走 [0021] 真实 commit hash 绑定流程 |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit

- PR: 待开(本 epic 自身一 PR 立项;5 子 PR 后续)
- 合入 commit: 待填(本 proposal 文件初始 commit hash 占位)
- 实际 merged 日期:待填

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| business router.push 数 | 3 | ≥15 | | | | |
| TestReport 自动聚合占比 | 0% | ≥80% | | | | |
| DORA 自动聚合占比 | 0% | 100% | | | | |
| AI 按钮残留模式 | 6/6 | 0/6 | | | | |
| 跨模块 E2E case | 0 | ≥5 | | | | |

### 最终判定

- [ ] done(达成目标,本提案归档)
- [ ] reverted(未达成 → 走回滚 PR,并在此段写"为什么失败")

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude(PM 验收会话)+ Wjl | 初版 — 验收报告 → epic proposal,5 子项拆解 |

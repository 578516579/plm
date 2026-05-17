# PRD/原型 → 代码 权威映射表 (PRD-MAPPING)

> **本文件是 PLM 项目的「单一事实来源 (SSoT)」**。
> 所有业务模块的字段、状态机、错误码、UI 文案、URL 路径,**必须**能在本表中追溯到 PRD §章节 + 原型 HTML 文件。
> 任何与本表不一致的实现都视为「跑偏 (drift)」,必须通过 [规则 §M] 流程修正或更新本表。

- **文档版本**: V1.0 / 2026-05-16
- **基准 PRD**: `prd和原型/AgriAI-PLM-完整PRD文档.md` V1.0
- **基准原型**: `prd和原型/AgriPLM-DevOps-原型/agriplm_split/*.html` (31 个 HTML)

---

## 1. PRD 六大功能域 → 模块矩阵

PRD §2.2 的四域 (立项 / 设计 / 研发 / 质量) + §3.2 扩展两域 (文档 / 效能) = **六域**;映射到 30 个业务 Maven 模块 (不含 admin/common/framework/system/generator/quartz/proposal 等基础设施)。

| PRD § | 域 | 模块 (plm-*) | 原型 HTML | 表名 (tb_*) | 编号规则 | 状态 |
|:--:|---|---|---|---|---|:--:|
| F1.1 | 立项 | `inception` | `inception.html` | `tb_inception` | `INC-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F1.2 | 立项 | `project` | `projects.html` | `tb_project` | `PRJ-YYYY-NNNN` (ADR-0001) | 🟢 **PRD-aligned** |
| F1.3 | 立项 | `competitive` | `competitive.html` | `tb_competitive` | `COMP-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F2.1 | 设计 | `requirement` | `requirements.html` | `tb_requirement` | `REQ-YYYY-NNNN` | 🟢 已对齐 |
| F2.2 | 设计 | `prd` | `prd.html` | `tb_prd` | `PRD-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F2.3 | 设计 | `ued` | `ued.html` | `tb_ued` | `UED-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F2.4 | 设计 | (评审 → `requirement`) | (并入需求) | (字段) | n/a | 🟢 |
| F3.1 | 研发 | `arch` | `archdesign.html` | `tb_arch` | `ARCH-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F3.2 | 研发 | `dbdesign` | `dbdesign.html` | `tb_dbdesign` | `DB-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F3.3 | 研发 | `apidesign` | `apidesign.html` | `tb_apidesign` | `APID-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F3.4 | 研发 | `sprint`+`task` | `kanban.html` | `tb_sprint`,`tb_task` | `SP-`,`TASK-` | 🟢 已对齐 |
| F3.5 | 研发 | `ai-agent`,`openspec` | `aiagents.html`,`aispec.html` | `tb_ai_agent`,`tb_openspec` | `AGT-YYYY-NNNN`,`SPEC-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F3.6 | 研发 | (联调 → `task`) | (并入任务) | (字段) | n/a | 🟢 |
| F4.1 | 质量 | `testplan` | `testplan.html` | `tb_testplan` | `TP-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F4.2 | 质量 | `testcase` | `testcase.html` | `tb_testcase` | `TC-YYYY-NNNN` | 🟢 已对齐 |
| F4.3 | 质量 | `testdata` | `testdata.html` | `tb_testdata` | `TD-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F4.4 | 质量 | `submission` | `submit.html` | `tb_submission` | `SUB-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F4.5 | 质量 | `autotest` | `autotest.html` | `tb_autotest` | `AT-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F4.6 | 质量 | `defect` | `defects.html` | `tb_defect` | `DEF-YYYY-NNNN` | 🟢 已对齐 |
| F4.7 | 质量 | `testreport` | `testreport.html` | `tb_testreport` | `TR-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F5.1 | 文档 | `manual-product` | `productmanual.html` | `tb_manual_product` | `PM-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F5.2 | 文档 | `manual-impl` | `implmanual.html` | `tb_manual_impl` | `IM-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F5.3 | 文档 | `manual-ops` | `opsmanual.html` | `tb_manual_ops` | `OM-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F5.4 | 文档 | `apidoc` | `apidoc.html` | `tb_apidoc` | `API-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F5.5 | 文档 | `document` | (并入文档中心) | `tb_document` | `DOC-YYYY-NNNN` | 🟢 已对齐 |
| F6 | 效能 | `analytics`,`dashboard` | `analytics.html`,`dashboard.html` | `tb_analytics_snapshot`,`tb_dashboard` | `AS-YYYY-NNNN`,`DASH-YYYY-NNNN` | 🟢 **PRD-aligned** |
| 扩展 | DevOps | `release` | `release.html` | `tb_release` | `REL-YYYY-NNNN` | 🟢 **PRD-aligned** |
| 扩展 | DevOps | `pipeline` | `pipeline.html` | `tb_pipeline` | `PIPE-YYYY-NNNN` | 🟢 **PRD-aligned** |
| 扩展 | DevOps | `feature-flag` | `featureflag.html` | `tb_feature_flag` | `FF-YYYY-NNNN` | 🟢 **PRD-aligned** |
| 扩展 | DevOps | `dora` | `devops.html` | `tb_dora_metric` | `DORA-YYYY-NNNN` | 🟢 **PRD-aligned** |

**状态图例**:
- 🟢 **PRD-aligned** (粗体): §2 有完整字段对照表 + Domain/Mapper/Service/SQL/E2E 全对齐 + 测试绿
- 🟢 已对齐 (无粗): 早期模块,代码可跑通,但 §2 尚无字段对照表;字段是否真对齐原型/PRD **待审计**
- 🟡 字段表已提案 / 代码待对齐: §2 有字段表但代码 commit 未落地(如 project 当前状态)
- 🟡 空壳 (历史): 有 Maven 模块但缺 Domain/Service/SQL
- 🔴 空: 连 Maven 模块都没建

**统计** (2026-05-17 重核,旧 "31 全对齐 🎉" 是阶段性数字,精确状态如下):

- 🟢 **PRD-aligned** (粗体,§2 字段表 + 代码 + mvn test 绿) = **19 个**: inception / **project** / competitive / prd / ued / arch / dbdesign / apidesign / testdata / autotest / manual-impl / manual-ops / analytics / dashboard / ai-agent / openspec / pipeline / feature-flag / dora
- 🟢 早期对齐 (代码已跑通,§2 字段表待补,**字段是否真对齐 PRD/原型未审计**) = **12 个**: requirement / sprint / task / testcase / defect / document / testplan / submission / testreport / apidoc / manual-product / release
- 🟡 字段表已提案 / 代码待对齐 = **0 个**
- 🟡 空壳 = **0 个**
- 🔴 空 = **0 个**

**审计待办** (跑偏检测 §M.6):
- ✅ project (本次完成): 5 处字段 drift 修复 + 双状态机 + 字典补全
- ⏳ **其余 12 个"早期对齐"模块极可能存在类似 drift**,建议下一轮专项审计(逐模块对比 原型 HTML 表单 / PRD §F 章节 / 当前 Domain.java),按 §M.2 "先字段表后代码" 流程修复

---

## 2. 字段对照规则 (强制)

每个业务模块的 `Domain.java` 字段必须能从对应原型 HTML 表单里**逐项**对应,不允许擅自增删。增减字段必须先改本表。

**模板**: 每个模块在 PRD-MAPPING.md 维护一段「字段对照表」,例: Submission (F4.4)

| 字段 | 列名 | 原型来源 (submit.html 表单元素) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `title` | `title` | `提测标题 *` (输入框) | varchar(200) | ✅ | — |
| `scope` | `scope` | `提测范围` (textarea) | TEXT | ❌ | — |
| `environment` | `environment` | `测试环境 select` | varchar(20) | ❌ | 默认 'staging' |
| `expectedTestDays` | `expected_test_days` | `期望测试周期(天)` | INT | ❌ | — |
| `unitTestCoverage` | `unit_test_coverage` | `质量门禁 - 单测覆盖率` | DECIMAL(5,2) | ❌ | ≥60 才通过 |
| `codeScanPassed` | `code_scan_passed` | `质量门禁 - 代码扫描` | CHAR(1) | ❌ | Y/N |
| `prdCompleted` | `prd_completed` | `质量门禁 - PRD 完整` | CHAR(1) | ❌ | Y/N |
| `apiDocUpdated` | `api_doc_updated` | `质量门禁 - API 文档` | CHAR(1) | ❌ | Y/N |
| `qualityGatePassed` | `quality_gate_passed` | (服务端计算,4 项 ∧) | CHAR(1) | (auto) | 不接受用户输入 |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 见 §3 |
| `rejectReason` | `reject_reason` | `退回原因` (modal) | varchar(500) | 条件必填 | status=04 必填 |

后续 16 个空壳模块在 PRD-align 落地时,**先在本文件追加同样的字段对照表,经过 review 后再写代码**。

### Inception (F1.1) — 立项 AI 助手 [inception.html L140~163]

| 字段 | 列名 | 原型来源 (inception.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `inceptionId` | `inception_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `inceptionNo` | `inception_no` | (业务编号) | varchar(32) | ✅ | `INC-YYYY-NNNN` |
| `projectName` | `project_name` | `项目名称 *` (`inc-name`) | varchar(200) | ✅ | — |
| `businessLine` | `business_line` | `业务线` select (`inc-biz`) | varchar(20) | ❌ | 字典 `biz_inception_biz_line`:植保服务/精准农业/农资流通/质量溯源 |
| `inceptionType` | `inception_type` | `项目类型` select (`inc-type`) | varchar(20) | ❌ | 字典 `biz_inception_type`:新产品研发/版本迭代/技术重构/平台建设 |
| `background` | `background` | `背景与诉求` textarea (`inc-bg`) | TEXT | ❌ | 长文本 |
| `estimatedDurationMonths` | `estimated_duration_months` | `预计工期(月)` (`inc-dur`) | INT | ❌ | — |
| `estimatedTeam` | `estimated_team` | `预计团队规模` (`inc-team`) | varchar(200) | ❌ | 自由文本 (如"前端×2 后端×3") |
| `aiGenerated` | `ai_generated` | (服务计算) | CHAR(1) | (default 'N') | `runInceptionAI` 触发后置 'Y' |
| `aiProposalContent` | `ai_proposal_content` | `📋 立项建议书预览` (`incReport`) | LONGTEXT | ❌ | Markdown,AI 生成 |
| `aiRisks` | `ai_risks` | `⚠️ AI风险识别` (`incRisks`) | TEXT | ❌ | 风险点列表 |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | 生成时间 |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 见 §3 |
| `rejectReason` | `reject_reason` | (审批驳回原因) | varchar(500) | 条件必填 | status=04 必填 |
| `submitterUserId` | `submitter_user_id` | (提交人) | BIGINT(20) | ✅ | sys_user FK |
| `approverUserId` | `approver_user_id` | (审批人) | BIGINT(20) | ❌ | sys_user FK,审批时填 |
| `approvedAt` | `approved_at` | (审批时间) | DATETIME | ❌ | 02→03 自动填 |
| `projectId` | `project_id` | (审批通过后关联项目) | BIGINT(20) | ❌ | 转项目后回填 FK |
| `createBy` etc. | (BaseEntity) | — | — | — | RuoYi 标准审计字段 |

**状态机** (5 态,PRD §F1.1 验收"立项流程审批节点"):
- `00` 草稿 → `{01}`
- `01` 已提交 → `{02, 04}`
- `02` 审批中 → `{03, 04}`
- `03` 已批准 → `{}` (终态,可触发"转项目")
- `04` 已驳回 → `{00}` (反向边,允许打回重写)

**AI 入口**: `POST /business/inception/ai/generate` — 服务端调用 PRD §2.3 `project-inception-flow` 工作流,本期只占位 (返回 mock 数据 + 写库),Dify 实接入 Phase 后续。

### Project (F1.2) — 项目档案管理 [projects.html L143 表单 + L313 列表 + PRD §F1.2 L218-222]

> **状态**: 🟢 **PRD-aligned** (字段表 commit `20b5bb6` → 代码 commit `3c10238`,mvn test 29/29 绿)

**PRD/原型 → 字段映射**:

| 字段 | 列名 | 原型来源 | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `id` | `id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `projectNo` | `project_no` | (业务编号) | varchar(32) | ✅ | `PRJ-YYYY-NNNN` (ADR-0001) |
| `projectName` | `project_name` | `项目名称 *` (`np-name`) + 列 `项目名称` | varchar(200) | ✅ | — |
| `businessLine` | `business_line` | `业务线` select (`np-biz`) + 列 `业务线` + PRD §F1.2 | varchar(20) | ✅ | 字典 `biz_project_business_line`:植保服务/精准农业/农资流通/质量溯源 (与 Inception `biz_inception_biz_line` 4 个值对齐,Inception→Project "转项目" 时直接复制) |
| `projectType` | `project_type` | PRD §F1.2 "项目基础信息.类型" (原型表单未直接显式) | varchar(20) | ❌ | 字典 `biz_project_type`:研发/改造/运维 (保留现行字典) |
| `priority` | `priority` | PRD §F1.2 "项目基础信息.优先级" (原型表单未直接显式) | varchar(8) | ❌ | 字典 `biz_project_priority`:P0/P1/P2/P3 |
| `lifecyclePhase` | `lifecycle_phase` | 列 `阶段` (`agriplm.js:313`) + PRD §F1.2 "阶段甘特图" | varchar(2) | ✅ | 字典 `biz_project_phase`:`00` 规划中 / `01` 研发中 / `02` 测试中 / `03` 验收中,默认 `00`,见状态机 |
| `progress` | `progress` | 列 `进度` (`agriplm.js:313`) + PRD §F1.2 "进度" | INT (0-100) | ❌ | 0-100 整数百分比,服务端可由 Sprint/Task 完成度推导 (本期允许手填) |
| `health` | `health` | 列 `健康度` (`agriplm.js:313`) + PRD §F1.2 "三色预警" | varchar(8) | ❌ | 字典 `biz_project_health`:`green` 健康 / `amber` 注意 / `red` 风险 |
| `managerUserId` | `manager_user_id` | `负责人` (`np-owner` 自由文本) + 列 `负责人` | BIGINT(20) | ❌ | FK→`sys_user.user_id` (升级:原型为自由文本,实现升级为 FK,见 §决策记录 D1) |
| `startDate` | `start_date` | `开始日期` (`np-start`) | DATE | ❌ | — |
| `endDate` | `end_date` | `截止日期` (`np-end`) + 列 `截止日期` | DATE | ❌ | — |
| `status` | `status` | (业务必需,原型未显式) | varchar(2) | ✅ | 字典 `biz_project_status`:`00` 进行中 / `01` 暂停 / `02` 已完成 / `03` 已取消,默认 `00`,见 §决策记录 D2 |
| `description` | `description` | (原型未显式) | TEXT | ❌ | 项目描述 — 扩展字段,见 §决策记录 D3 |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | `0`=正常 / `2`=删除 |

**字段决策记录** (实现与原型有差异处):
- **D1: `managerUserId` 从自由文本 → user_id FK 升级** — 原型 `np-owner` 是文本输入,但项目级负责人需要权限校验/通知/数据范围隔离,使用 user_id FK 更合理。前端表单仍可显示"姓名+下拉",实质存 user_id。
- **D2: 保留 `status` 总状态字段** — 原型只显式展示"阶段",但项目需要表达"暂停/取消"等业务总状态,这是 lifecyclePhase 无法承载的(阶段是交付推进的轴,status 是项目可用性的轴)。两字段并行: `status=00 进行中` 时 `lifecyclePhase` 才推进;`status≠00` 时阶段冻结。
- **D3: 保留 `description` 字段** — 原型表单未现,但项目级描述是几乎所有 PLM 系统必备的;标注为扩展字段。
- **D4: 删除 `budget` 字段** — 原 `tb_project` 有 DECIMAL(18,2) `budget` 字段。PRD §F1.2 + 原型 5 项表单 + 列表 7 列均未提及;判定为 RuoYi 脚手架生成时的样板字段,不属于 AgriPLM 业务模型,实施代码 commit 时移除。
- **D5: `priority` 字典值** — PRD 未指定具体取值,采用 RuoYi/通用约定 P0/P1/P2/P3。

**状态机 1 — `status` (项目总状态)**:
- `00` 进行中 → `{01, 02, 03}`
- `01` 暂停 → `{00, 03}` (恢复 / 取消)
- `02` 已完成 → `{}` (终态)
- `03` 已取消 → `{}` (终态)

非法转换抛 ServiceException(601),与其他 PRD-aligned 模块一致。

**状态机 2 — `lifecyclePhase` (交付阶段,仅在 `status=00` 时演进)**:
- `00` 规划中 → `{01}`
- `01` 研发中 → `{00, 02}` (回退或前进)
- `02` 测试中 → `{01, 03}` (回退或前进)
- `03` 验收中 → `{02}` (验收回退;验收完成应改 `status=02 已完成` 而非阶段)

实现注意:`updateProject` 调用时,若同一次请求改 `status` 又改 `lifecyclePhase` → 先校验 status 转换,再校验 phase 转换;若 status 变成非 `00` → 拒绝 phase 改动(抛 601)。

**Inception → Project "转项目" 字段映射** (PRD §F1.1 验收"立项审批通过可触发转项目"):

| Inception 字段 | Project 字段 |
|---|---|
| `projectName` | `projectName` |
| `businessLine` | `businessLine` (必须复制,不允许丢失) |
| `inceptionType` | `projectType` (字典 label 不同,值映射策略待 ADR;本期建议 inception_type 直接写入 project_type) |
| `estimatedDurationMonths` | (推导 startDate / endDate,可由后端按当日+月数计算) |
| (固定值) | `status='00'`, `lifecyclePhase='00'`, `progress=0`, `health='green'` |

转项目接口由 Inception 服务端调用 `IProjectService.insertProject(...)`,projectId 回填到 Inception。

### PRD (F2.2) — AI PRD 生成器 [prd.html L140~165]

| 字段 | 列名 | 原型来源 (prd.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `prdId` | `prd_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `prdNo` | `prd_no` | (业务编号) | varchar(32) | ✅ | `PRD-YYYY-NNNN` |
| `projectId` | `project_id` | `关联项目` select (`prd-proj`) | BIGINT(20) | ✅ | FK → tb_project |
| `title` | `title` | `功能名称` (`prd-title`) | varchar(200) | ✅ | 例: AI 灌溉推荐引擎 |
| `description` | `description` | `需求描述(自然语言)` textarea (`prd-desc`) | TEXT | ❌ | AI 输入 |
| `sceneTemplate` | `scene_template` | `业务场景模板` select (`prd-tpl`) | varchar(20) | ❌ | 字典 `biz_prd_scene`: irrigation/agri_sales/pest_control/traceability |
| `targetUser` | `target_user` | `目标用户` select (`prd-user`) | varchar(20) | ❌ | 字典 `biz_prd_target_user`: farmer/agronomist/admin |
| `content` | `content` | `📄 PRD预览` (`prdContent`) | LONGTEXT | ❌ | Markdown 全文 (AI 生成 7 段) |
| `completenessScore` | `completeness_score` | `prdCompleteness` 徽章 | DECIMAL(5,2) | ❌ | 0-100,§F2.2 验收 ≥80 |
| `version` | `version` | `版本` (prdlist modal) | varchar(20) | ❌ | 默认 `v1.0`,变更对比用 |
| `aiGenerated` | `ai_generated` | (服务计算) | CHAR(1) | (default 'N') | `generatePRD` 触发后 'Y' |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | 生成时间 |
| `status` | `status` | (状态徽章 `b bg/bam/bgr`) | varchar(2) | ✅ | 见 §3 |
| `authorUserId` | `author_user_id` | (作者) | BIGINT(20) | ✅ | sys_user FK |
| `reviewerUserId` | `reviewer_user_id` | (评审人) | BIGINT(20) | ❌ | 01→02 转态时填 |
| `createBy` etc. | (BaseEntity) | — | — | — | RuoYi 标准 |

**状态机** (3 态 + 1 终态,源:原型 prdlist modal 徽章 + PRD §F2.2 "草稿→评审→确认"):
- `00` 草稿 → `{01}`
- `01` 评审中 → `{00, 02}` (反向边:打回到草稿)
- `02` 已确认 → `{03}` (终态可废弃)
- `03` 已废弃 → `{}` (终态)

**AI 入口**: `POST /business/prd/ai/generate/{id}` — 调用 §2.3 `prd-generation-flow` (本期 mock,Dify 后续接入)。生成后自动算 `completenessScore` (mock: 固定 85.0,PRD §F2.2 验收 ≥80% 通过)。

### Competitive (F1.3) — 竞品情报 [competitive.html L132~159 + PRD §1.3]

| 字段 | 列名 | 原型来源 (competitive.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `competitiveId` | `competitive_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `competitiveNo` | `competitive_no` | (业务编号) | varchar(32) | ✅ | `COMP-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目) | BIGINT(20) | ✅ | FK → tb_project |
| `competitorName` | `competitor_name` | `compMatrix` 行 — 竞品名称 | varchar(100) | ✅ | 例:禅道/LigaAI/Jira |
| `vendor` | `vendor` | `compMatrix` 公司 | varchar(100) | ❌ | — |
| `website` | `website` | `compMatrix` 链接 | varchar(200) | ❌ | — |
| `pricingModel` | `pricing_model` | `compMatrix` 价格列 | varchar(200) | ❌ | 例: "$17.5/用户/月" |
| `pricingTier` | `pricing_tier` | (价格档) | varchar(20) | ❌ | 字典 `biz_competitive_tier`: free/midrange/enterprise |
| `featureMatrix` | `feature_matrix` | `compMatrix` 12 维度 JSON | TEXT | ❌ | PRD §1.3 立项管理/PRD生成/MCP/Dify 等 |
| `strengths` | `strengths` | `SWOT 分析 — S` | TEXT | ❌ | 优势 |
| `weaknesses` | `weaknesses` | `SWOT 分析 — W` | TEXT | ❌ | 劣势 |
| `opportunities` | `opportunities` | `SWOT 分析 — O` | TEXT | ❌ | 机会 |
| `threats` | `threats` | `SWOT 分析 — T` | TEXT | ❌ | 威胁 |
| `aiAnalysisReport` | `ai_analysis_report` | `runCompAnalysis()` 输出 | LONGTEXT | ❌ | AI 综合报告 Markdown |
| `aiGenerated` | `ai_generated` | (服务计算) | CHAR(1) | (default 'N') | — |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | — |
| `monitorEnabled` | `monitor_enabled` | `+ 订阅推送` 按钮 | CHAR(1) | (default 'N') | Y=订阅竞品动态 |
| `monitorKeywords` | `monitor_keywords` | (订阅关键词) | varchar(500) | ❌ | CSV |
| `lastMonitoredAt` | `last_monitored_at` | `compMonitor` tab | DATETIME | ❌ | 最近监控时间 |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 见 §3 |
| `authorUserId` | `author_user_id` | (创建人) | BIGINT(20) | ✅ | sys_user FK |
| `createBy` etc. | (BaseEntity) | — | — | — | RuoYi 标准 |

**状态机** (3 态):
- `00` 草稿 → `{01}`
- `01` 已发布 → `{02}`
- `02` 已归档 → `{}` (终态)

**AI 入口**: `POST /business/competitive/ai/analyze/{id}` — 调用 §2.3 `competitive-analysis-flow` (本期 mock: 返回标准 SWOT + 报告模板)。

### Arch (F3.1) — 系统概要设计 HLD [archdesign.html L138~169]

| 字段 | 列名 | 原型来源 (archdesign.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `archId` | `arch_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `archNo` | `arch_no` | (业务编号) | varchar(32) | ✅ | `ARCH-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目) | BIGINT(20) | ✅ | FK → tb_project |
| `prdId` | `prd_id` | (关联 PRD) | BIGINT(20) | ❌ | 可选 FK → tb_prd |
| `title` | `title` | (架构方案名) | varchar(200) | ✅ | — |
| `archMode` | `arch_mode` | `架构模式` select (`arch-mode`) | varchar(20) | ❌ | 字典 `biz_arch_mode`: microservice/monolith/serverless/layered |
| `primaryStack` | `primary_stack` | `主要语言/框架` (`arch-lang`) | varchar(50) | ❌ | 字典 `biz_arch_stack`: java_sb3/go_gin/python_fastapi/nodejs |
| `databaseChoice` | `database_choice` | `数据库` (`arch-db`) | varchar(50) | ❌ | 字典 `biz_arch_database`: pg_redis/mysql_redis/kingbase |
| `aiOrchestration` | `ai_orchestration` | `AI编排` (`arch-ai`) | varchar(50) | ❌ | 字典 `biz_arch_ai_engine`: dify_deepseek/dify_chatglm/self_langchain |
| `deploymentType` | `deployment_type` | `部署方式` (`arch-deploy`) | varchar(20) | ❌ | 字典 `biz_arch_deployment`: k8s/docker_compose/baremetal |
| `iotProtocol` | `iot_protocol` | `IoT接入协议` (`arch-iot`) | varchar(20) | ❌ | 字典 `biz_arch_iot_protocol`: mqtt/http_longpoll/websocket |
| `designContent` | `design_content` | `archContent` (架构方案描述) | LONGTEXT | ❌ | Markdown |
| `c4DiagramContent` | `c4_diagram_content` | `📐 C4容器图` (`archDiagram`) | LONGTEXT | ❌ | Mermaid C4 图 |
| `nfrMapping` | `nfr_mapping` | `⚡ 非功能需求映射` (`archNFR`) | TEXT | ❌ | 性能/安全/兼容性映射 |
| `aiGenerated` | `ai_generated` | (服务计算) | CHAR(1) | (default 'N') | `genArchDesign` 触发后 'Y' |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | — |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 见 §3 |
| `authorUserId` | `author_user_id` | (架构师) | BIGINT(20) | ✅ | sys_user FK |
| `reviewerUserId` | `reviewer_user_id` | (评审人) | BIGINT(20) | ❌ | 01→02 时填 |

**状态机** (4 态,含反向边 01→00):
- `00` 草稿 → `{01}`
- `01` 评审中 → `{00, 02}` (反向: 评审打回)
- `02` 已确认 → `{03}`
- `03` 已废弃 → `{}` (终态)

**AI 入口**: `POST /business/arch/ai/generate/{id}` — 调用 §2.3 `arch-design-flow` (本期 mock: 返回标准 C4 Mermaid + NFR 模板)。

### DbDesign (F3.2) — 数据库设计 [dbdesign.html L134~153]

| 字段 | 列名 | 原型来源 (dbdesign.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `dbdesignId` | `dbdesign_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `dbdesignNo` | `dbdesign_no` | (业务编号) | varchar(32) | ✅ | `DB-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目) | BIGINT(20) | ✅ | FK → tb_project |
| `archId` | `arch_id` | (关联架构) | BIGINT(20) | ❌ | 可选 FK → tb_arch |
| `title` | `title` | (设计标题) | varchar(200) | ✅ | — |
| `dbEngine` | `db_engine` | (引擎选型,由 arch 派生) | varchar(20) | ❌ | 字典 `biz_dbdesign_engine`: mysql/postgresql/kingbase |
| `erDiagramContent` | `er_diagram_content` | `📊 ER实体关系图` (`erDiagram`) | LONGTEXT | ❌ | Mermaid erDiagram |
| `dataDictionary` | `data_dictionary` | `📋 数据字典` (`dbDict`) | LONGTEXT | ❌ | Markdown 表格 |
| `ddlScript` | `ddl_script` | `💻 建表SQL` (`dbSql`) | LONGTEXT | ❌ | CREATE TABLE 集合 |
| `normalizationCheck` | `normalization_check` | (规范检查) | TEXT | ❌ | 命名/索引/范式 JSON |
| `aiGenerated` | `ai_generated` | (服务计算) | CHAR(1) | (default 'N') | `genDBDesign` 触发后 'Y' |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | — |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 见 §3 |
| `authorUserId` | `author_user_id` | (DBA) | BIGINT(20) | ✅ | — |
| `reviewerUserId` | `reviewer_user_id` | (评审人) | BIGINT(20) | ❌ | — |

**状态机** (4 态,含反向边 01→00 沿用 arch 模式):
- `00` 草稿 → `{01}`
- `01` 评审中 → `{00, 02}` (反向)
- `02` 已确认 → `{03}`
- `03` 已废弃 → `{}` (终态)

**AI 入口**: `POST /business/dbdesign/ai/generate/{id}` — 调用 §2.3 `db-design-flow` (本期 mock: 返回 ER + 字典 + DDL 模板)。

### ApiDesign (F3.3) — LLD 接口详细设计 [apidesign.html L134~149 + modal-newapi]

| 字段 | 列名 | 原型来源 | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `apidesignId` | `apidesign_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `apidesignNo` | `apidesign_no` | (业务编号) | varchar(32) | ✅ | `APID-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目) | BIGINT(20) | ✅ | FK |
| `archId` | `arch_id` | (关联架构) | BIGINT(20) | ❌ | 可选 FK |
| `title` | `title` | (接口设计标题/资源名) | varchar(200) | ✅ | — |
| `httpMethod` | `http_method` | `modal-newapi` HTTP方法 | varchar(10) | ✅ | GET/POST/PUT/DELETE/PATCH |
| `path` | `path` | `modal-newapi` 接口路径 (`na-path`) | varchar(500) | ✅ | `/api/v1/...` |
| `description` | `description` | `modal-newapi` 接口描述 (`na-desc`) | TEXT | ❌ | — |
| `requestSchema` | `request_schema` | (派生 — 请求 JSON Schema) | TEXT | ❌ | — |
| `responseSchema` | `response_schema` | (派生 — 响应 JSON Schema) | TEXT | ❌ | — |
| `openapiSpec` | `openapi_spec` | `apiDetailView` (OpenAPI 3.0) | LONGTEXT | ❌ | YAML |
| `mockEnabled` | `mock_enabled` | (Mock 服务开关) | CHAR(1) | (default 'N') | F3.6 联调:Y → Mock 服务暴露 |
| `mockResponse` | `mock_response` | (Mock 响应体) | TEXT | ❌ | Mock 服务返回 JSON |
| `aiGenerated` | `ai_generated` | (服务计算) | CHAR(1) | (default 'N') | `genAPIDesign` 触发后 'Y' |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | — |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 见 §3 |
| `authorUserId` | `author_user_id` | (设计者) | BIGINT(20) | ✅ | — |
| `reviewerUserId` | `reviewer_user_id` | (评审人) | BIGINT(20) | ❌ | — |

**唯一键**: `UNIQUE(project_id, http_method, path)` → 重复 method+path 抛 701。

**状态机** (4 态,同 arch 模式 含反向边 01→00):
- `00` 草稿 → `{01}` / `01` 评审中 → `{00,02}` / `02` 已确认 → `{03}` 已废弃终态

**AI 入口**: `POST /business/apidesign/ai/generate/{id}` — `detail-design-flow` (mock: 生成 OpenAPI YAML 模板 + Mock 响应)。

**与 apidoc 区分**:
- `apidesign` = 研发设计期产出 (PRD §F3.3,设计中接口);状态 00 草稿→01 评审→02 已确认
- `apidoc` = 交付发布期 (PRD §F5.4,从代码注释提取的对外文档);源于 GitLab 代码扫描

### Ued (F2.3) — UED 设计协同 [ued.html L134-160]

| 字段 | 列名 | 原型来源 | 类型 | 必填 |
|---|---|---|---|:--:|
| `title` | `title` | 设计稿名称 | varchar(200) | ✅ |
| `figmaUrl` | `figma_url` | "从 Figma 同步设计稿" (openFigmaSync) | varchar(500) | ❌ |
| `figmaFileKey` | `figma_file_key` | Figma 文件 key (MCP 集成入口) | varchar(100) | ❌ |
| `versionLabel` | `version_label` | 设计稿版本管理 (uedVersions) | varchar(20) | ❌ |
| `previewUrl` | `preview_url` | 缩略图 | varchar(500) | ❌ |
| `annotationContent` | `annotation_content` | 标注内容 (间距/颜色/字体) | TEXT JSON | ❌ |
| `reviewReport` | `review_report` | AI 设计评审报告 (uedReview) | LONGTEXT | ❌ |
| `reviewScore` | `review_score` | AI 评分 0-100 | DECIMAL(5,2) | ❌ |
| `complianceCheck` | `compliance_check` | 设计规范遵从度 | TEXT JSON | ❌ |
| `usabilityIssues` | `usability_issues` | 可用性问题列表 | TEXT | ❌ |
| `agriComponentTags` | `agri_component_tags` | 农业 UI 组件库标签 CSV (4 种) | varchar(200) | ❌ |
| `requirementId` | `requirement_id` | 关联需求 (FK) | BIGINT | ❌ |
| `aiGenerated`/`aiGeneratedAt` | 同名 | 服务计算 | CHAR/DATETIME | ❌ |
| `status` | `status` | 4 态状态机 (同 arch) | varchar(2) | ✅ |
| `designerUserId`/`reviewerUserId` | 同名 | 设计师/评审人 | BIGINT | 视情况 |

**状态机** (4 态同 arch,含反向边 01→00):`00 草稿 → 01 评审中 → {00,02} → 03 已废弃`
**AI 入口**: `POST /business/ued/ai/review/{id}` — Dify `ued-review-flow`

### TestData (F4.3) — 测试数据工厂 [testdata.html L134-172]

| 字段 | 列名 | 原型来源 | 类型 | 必填 |
|---|---|---|---|:--:|
| `title` | `title` | 数据集名称 | varchar(200) | ✅ |
| `targetTable` | `target_table` | 数据表 select (td-table, 5 选) | varchar(50) | ✅ |
| `targetTableLabel` | `target_table_label` | 中文标签 | varchar(100) | ❌ |
| `generateCount` | `generate_count` | 生成数量 (td-count) | INT | ✅ |
| `outputFormat` | `output_format` | 输出格式 (td-format, json/sql/csv) | varchar(20) | ✅ |
| `fieldSemantics` | `field_semantics` | AI 识别字段语义 (fieldSemantics) | TEXT JSON | ❌ |
| `ruleChinaCoord` | `rule_china_coord` | 坐标限定中国农田 | CHAR(1) | ❌ |
| `ruleTimeContinuity` | `rule_time_continuity` | 时间序列业务连续性 | CHAR(1) | ❌ |
| `ruleSensorRange` | `rule_sensor_range` | 数值符合传感器范围 | CHAR(1) | ❌ |
| `ruleIncludeOutliers` | `rule_include_outliers` | 包含异常值 (边界测试) | CHAR(1) | ❌ |
| `generatedContent` | `generated_content` | 生成的数据 (tdPreview) | LONGTEXT | ❌ |
| `generatedAt` | `generated_at` | 生成时间 | DATETIME | ❌ |
| `aiGenerated` | `ai_generated` | Y/N | CHAR(1) | ❌ |
| `status` | `status` | 3 态状态机 | varchar(2) | ✅ |
| `authorUserId` | `author_user_id` | 创建人 | BIGINT | ✅ |

**状态机** (3 态):`00 草稿 → 01 已生成 → 02 已归档` (终态)
**AI 入口**: `POST /business/testdata/ai/generate/{id}` — Dify `data-gen-flow`,基于字段语义生成农业场景真实感数据

### AutoTest (F4.5) — 自动化测试 [autotest.html L134-156]

| 字段 | 列名 | 原型来源 | 类型 | 必填 |
|---|---|---|---|:--:|
| `title` | `title` | 测试套件名称 | varchar(200) | ✅ |
| `testSuiteType` | `test_suite_type` | 套件类型 (ui/api/perf/regression) | varchar(20) | ✅ |
| `framework` | `framework` | 测试框架 (playwright/selenium/jmeter/cypress) | varchar(20) | ✅ |
| `targetUrl` | `target_url` | 测试目标 URL | varchar(500) | ❌ |
| `scriptContent` | `script_content` | 脚本内容 (genAutoScript) | LONGTEXT | ❌ |
| `scheduleEnabled` | `schedule_enabled` | 定时执行开关 Y/N | CHAR(1) | ❌ |
| `scheduleCron` | `schedule_cron` | cron 表达式 | varchar(50) | ❌ |
| `totalCases` | `total_cases` | 用例总数 (atTotal stat) | INT | ❌ |
| `passedCases` | `passed_cases` | 通过用例 (89% 通过) | INT | ❌ |
| `failedCases` | `failed_cases` | 失败用例 (atFailed=3) | INT | ❌ |
| `passRate` | `pass_rate` | 通过率 (服务端计算) | DECIMAL(5,2) | (auto) |
| `executionDurationSec` | `execution_duration_sec` | 执行耗时 (4m32s = 272s) | INT | ❌ |
| `lastExecutedAt` | `last_executed_at` | 上次执行时间 | DATETIME | ❌ |
| `lastRootCauseAnalysis` | `last_root_cause_analysis` | **AI 智能根因分析** (F4.5 核心) | LONGTEXT | ❌ |
| `aiGenerated`/`aiGeneratedAt` | 同名 | AI 生成脚本标志 | CHAR/DATETIME | ❌ |
| `status` | `status` | 3 态状态机 | varchar(2) | ✅ |
| `authorUserId` | `author_user_id` | 创建人 | BIGINT | ✅ |

**状态机** (3 态含反向边):`00 草稿 → 01 已激活 → 02 已禁用`,`02→01` 反向边可重新激活
**AI 入口**:
- `POST /business/autotest/ai/generate/{id}` — Dify `auto-test-flow` 生成脚本骨架
- (后续) `POST /business/autotest/run/{id}` — 立即执行 + 自动写入 totalCases/passedCases/failedCases + AI 根因分析

### ManualImpl (F5.2) — 实施手册 [implmanual.html + PRD §F5.2]

| 字段 | 列名 | 原型来源 (implmanual.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `manualimplId` | `manualimpl_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `manualimplNo` | `manualimpl_no` | (业务编号) | varchar(32) | ✅ | `IM-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目) | BIGINT(20) | ✅ | FK → tb_project |
| `title` | `title` | 手册标题 | varchar(200) | ✅ | 例: AgriPLM Docker 实施手册 |
| `deployMode` | `deploy_mode` | 部署模式 select | varchar(30) | ❌ | 字典 `biz_manualimpl_deploy`: docker_compose/kubernetes/baremetal |
| `osType` | `os_type` | 操作系统 select | varchar(30) | ❌ | 字典 `biz_manualimpl_os`: centos7/ubuntu20/kylin |
| `dbType` | `db_type` | 数据库 select | varchar(30) | ❌ | 字典 `biz_manualimpl_db`: postgresql14/mysql8/kdb (信创) |
| `envConfig` | `env_config` | 环境变量 textarea (JSON) | TEXT | ❌ | JSON 格式环境变量 |
| `content` | `content` | `imContent` 预览 | LONGTEXT | ❌ | AI 生成的 Markdown 全文 |
| `outputFormats` | `output_formats` | (隐式 PDF/Word/Markdown) | varchar(100) | ❌ | CSV: word/pdf/html/markdown; 默认 pdf |
| `aiGenerated` | `ai_generated` | (服务计算) | char(1) | ✅ | Y/N |
| `generatedAt` | `generated_at` | (服务计算) | DATETIME | ❌ | 02 状态自动填 |
| `status` | `status` | `imStatus` | char(2) | ✅ | 字典 `biz_manualimpl_status`: 00草稿/01生成中/02已生成/03已发布 |
| `authorUserId` | `author_user_id` | 创建者 | BIGINT(20) | ✅ | FK → sys_user |

状态机: `00→{01}` `01→{02}` `02→{00,03}` `03` 终态 (02→00 重新草稿)

**AI 入口**: `POST /business/manual-impl/ai/generate/{id}` — 调用 §F5.2 `impl-manual-flow` (本期 mock: 按 deployMode + osType + dbType 三维度生成 5 章节实施手册 Markdown，含部署步骤、环境变量、农情大屏接入、回滚预案)。

### ManualOps (F5.3) — 运维手册 [opsmanual.html + PRD §F5.3]

| 字段 | 列名 | 原型来源 (opsmanual.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `manualopsId` | `manualops_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `manualopsNo` | `manualops_no` | (业务编号) | varchar(32) | ✅ | `OM-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目) | BIGINT(20) | ✅ | FK → tb_project |
| `title` | `title` | 手册标题 | varchar(200) | ✅ | 例: AgriPLM 运维手册 |
| `monitoringPlan` | `monitoring_plan` | 监控方案 select | varchar(30) | ❌ | 字典 `biz_manualops_monitoring`: prometheus_grafana/aliyun_cms/zabbix |
| `alertChannels` | `alert_channels` | 告警渠道多选 | varchar(200) | ❌ | CSV 值，字典 `biz_manualops_alert`: dingtalk/feishu/wework/email |
| `iotDeviceTypes` | `iot_device_types` | IoT 设备类型多选 | varchar(300) | ❌ | CSV 值，字典 `biz_manualops_iot`: soil_sensor/weather_station/drone/irrigation_controller |
| `content` | `content` | `omContent` 预览 | LONGTEXT | ❌ | AI 生成的 Markdown 全文 |
| `outputFormats` | `output_formats` | (隐式) | varchar(100) | ❌ | CSV: word/pdf/html/markdown; 默认 pdf |
| `aiGenerated` | `ai_generated` | (服务计算) | char(1) | ✅ | Y/N |
| `generatedAt` | `generated_at` | (服务计算) | DATETIME | ❌ | 02 状态自动填 |
| `status` | `status` | `omStatus` | char(2) | ✅ | 字典 `biz_manualops_status`: 同 manualimpl 4 态 |
| `authorUserId` | `author_user_id` | 创建者 | BIGINT(20) | ✅ | FK → sys_user |

状态机: `00→{01}` `01→{02}` `02→{00,03}` `03` 终态

**AI 入口**: `POST /business/manual-ops/ai/generate/{id}` — 调用 §F5.3 `ops-manual-flow` (本期 mock: 按 monitoringPlan + alertChannels CSV + iotDeviceTypes CSV 生成 5 章节运维手册，含 IoT 农情设备巡检 SLA、备份策略、应急预案)。

### AnalyticsSnapshot (F6) — 效能分析快照 [analytics.html + devops.html + PRD §F6]

| 字段 | 列名 | 原型来源 | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `snapshotId` | `snapshot_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `snapshotNo` | `snapshot_no` | (业务编号) | varchar(32) | ✅ | `AS-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目,可空) | BIGINT(20) | ❌ | NULL=全局快照 |
| `title` | `title` | 快照标题 | varchar(200) | ✅ | 例: 2026-Q2 季度效能 |
| `periodType` | `period_type` | 周期 selector (本月/本季度/本年) | varchar(20) | ✅ | 字典 `biz_analytics_period`: month/quarter/year |
| `snapshotDate` | `snapshot_date` | (周期起点) | DATE | ✅ | 例: 2026-04-01 |
| `requirementThroughput` | `requirement_throughput` | analytics.html 第1卡片 | INT | ❌ | 需求吞吐量 |
| `sprintOnTimeRate` | `sprint_on_time_rate` | analytics.html 第2卡片 | DECIMAL(5,2) | ❌ | 迭代准时率 % |
| `defectDensity` | `defect_density` | analytics.html 第3卡片 | DECIMAL(8,2) | ❌ | 缺陷密度 个/KLOC |
| `autoTestCoverage` | `auto_test_coverage` | dashboard.html 第4卡片 | DECIMAL(5,2) | ❌ | 自动化覆盖率 % |
| `deploymentFrequency` | `deployment_frequency` | devops.html DORA 卡片 | DECIMAL(10,2) | ❌ | 部署频率 次/天 |
| `leadTimeHours` | `lead_time_hours` | devops.html `#leadtime-breakdown` | DECIMAL(10,2) | ❌ | 前置时间 小时 |
| `mttrHours` | `mttr_hours` | devops.html DORA 卡片 | DECIMAL(10,2) | ❌ | 平均恢复时间 小时 |
| `changeFailureRate` | `change_failure_rate` | devops.html DORA 卡片 | DECIMAL(5,2) | ❌ | 变更失败率 % |
| `aiHoursSaved` | `ai_hours_saved` | analytics.html 第4卡片 | DECIMAL(10,2) | ❌ | AI 节省工时 |
| `activeProjects` | `active_projects` | dashboard.html 卡片 | INT | ❌ | 在办项目数 |
| `projectsAtRisk` | `projects_at_risk` | dashboard.html "2个风险项目" | INT | ❌ | 风险项目数 |
| `aiRecommendations` | `ai_recommendations` | analytics.html "AI改进建议" | LONGTEXT | ❌ | AI 生成 Markdown |
| `aiGenerated` | `ai_generated` | (服务计算) | char(1) | ✅ | Y/N |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | AI 生成时间 |
| `status` | `status` | 状态 | char(2) | ✅ | 字典 `biz_analytics_status`: 00草稿/01已发布/02已归档 |
| `authorUserId` | `author_user_id` | 创建者 | BIGINT(20) | ✅ | FK → sys_user |

状态机: `00→{01}` `01→{02}` `02→{}` (终态)

**AI 入口**: `POST /business/analytics/ai/recommend/{id}` — 调用 §F6 `analytics-recommend-flow` (本期 mock: 按 sprintOnTimeRate / defectDensity / autoTestCoverage / changeFailureRate 阈值生成 4 维度改进建议 + 农情 IoT 专项建议 Markdown)。

### Dashboard (UI §4.2) — 工作台 [dashboard.html + UI §4.2]

| 字段 | 列名 | 原型来源 (dashboard.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `dashboardId` | `dashboard_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `dashboardNo` | `dashboard_no` | (业务编号) | varchar(32) | ✅ | `DASH-YYYY-NNNN` |
| `title` | `title` | 工作台名称 | varchar(200) | ✅ | 例: 张总默认工作台 |
| `ownerUserId` | `owner_user_id` | 所属用户 | BIGINT(20) | ✅ | FK → sys_user; (owner, is_default=Y) 唯一 |
| `layoutJson` | `layout_json` | (前端布局) | LONGTEXT | ❌ | widget grid 布局 JSON |
| `widgetTypes` | `widget_types` | (启用 widget 多选) | varchar(500) | ❌ | CSV 字典 `biz_dashboard_widget`: stats/active_projects/my_todos/quality_snapshot/lifecycle/ai_metrics |
| `refreshInterval` | `refresh_interval` | (刷新间隔) | INT | ❌ | 秒，默认 60 |
| `isDefault` | `is_default` | (默认标记) | char(1) | ✅ | Y/N; 同用户切 Y 时自动清除其他 |
| `status` | `status` | 状态 | char(2) | ✅ | 字典 `biz_dashboard_status`: 00启用/01停用 |

聚合查询: `GET /business/dashboard/aggregate?ownerUserId={uid}` — 返回 6 类 widget 数据 (stats / activeProjects / myTodos / qualitySnapshot / aiMetrics / lifecycle)。本期返回 mock，后续接真实跨模块聚合。

业务规则: 同用户在 `tb_dashboard` 中只能有一个 `is_default='Y'` 的预设。Service.insert/update 检测到设为 default 时调用 `clearDefaultForOwner()` 取消同用户的旧默认。

### AiAgent (F3.5) — AI Agent 编排 [aiagents.html + PRD §F3.5]

| 字段 | 列名 | 原型来源 (aiagents.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `agentId` | `agent_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `agentNo` | `agent_no` | (业务编号) | varchar(32) | ✅ | `AGT-YYYY-NNNN` |
| `agentName` | `agent_name` | Agent 名称 | varchar(200) | ✅ | — |
| `agentType` | `agent_type` | Agent 类型卡片 | varchar(30) | ✅ | 字典 `biz_aiagent_type`: requirement/prd/code/test/release/ops |
| `description` | `description` | 描述 | varchar(500) | ❌ | — |
| `promptTemplate` | `prompt_template` | 提示词模板 | LONGTEXT | ❌ | Agent 系统提示词 |
| `difyWorkflowId` | `dify_workflow_id` | Dify 工作流 ID | varchar(64) | ❌ | 关联外部 Dify |
| `configJson` | `config_json` | (高级配置) | LONGTEXT | ❌ | JSON 配置 |
| `totalCalls` | `total_calls` | "今日总调用" 统计 | BIGINT | ❌ | 默认 0 |
| `successRate` | `success_rate` | "平均成功率" 卡片 | DECIMAL(5,2) | ❌ | % 移动平均 |
| `lastInvokedAt` | `last_invoked_at` | (服务计算) | DATETIME | ❌ | 最近调用 |
| `status` | `status` | 状态 | char(2) | ✅ | 字典 `biz_aiagent_status`: 00运行中/01已停止/02错误 |
| `authorUserId` | `author_user_id` | 创建者 | BIGINT | ✅ | FK → sys_user |

状态机: `00→{01,02}` `01→{00}` `02→{00,01}` (错误态可重启或停用)

**业务入口**: `POST /business/ai-agent/invoke/{id}` — 模拟调用 (累加 totalCalls + 移动平均 successRate 95%),实际接 Dify HTTP API。

### Openspec (F3.5) — AI OpenSpec [aispec.html + PRD §F3.5]

| 字段 | 列名 | 原型来源 (aispec.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `openspecId` | `openspec_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `openspecNo` | `openspec_no` | (业务编号) | varchar(32) | ✅ | `SPEC-YYYY-NNNN` |
| `specName` | `spec_name` | 规范名称 | varchar(200) | ✅ | (specName, version) 唯一 |
| `specType` | `spec_type` | 规范类型 select | varchar(30) | ✅ | 字典 `biz_openspec_type`: openapi/asyncapi/ai_function/graphql |
| `description` | `description` | 描述 | varchar(500) | ❌ | — |
| `specContent` | `spec_content` | 规范内容 (YAML/JSON) | LONGTEXT | ❌ | AI 生成或手写 |
| `version` | `version` | 版本号 | varchar(30) | ✅ | 语义化版本 |
| `agriKbRef` | `agri_kb_ref` | "AgriKB引用" 字段 (x-agrikb-ref) | varchar(200) | ❌ | 增强标注 |
| `aiGenerated` | `ai_generated` | (服务计算) | char(1) | ✅ | Y/N |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | — |
| `status` | `status` | 状态 | char(2) | ✅ | `biz_openspec_status`: 00草稿/01已发布/02已弃用 |
| `authorUserId` | `author_user_id` | 创建者 | BIGINT | ✅ | FK → sys_user |

状态机: `00→{01}` `01→{02}` `02→{}` (终态)

**AI 入口**: `POST /business/openspec/ai/generate/{id}` — 按 specType mock OpenAPI 3.1 / AsyncAPI 3.0 / AI Function / GraphQL 骨架,含 AgriKB x-agrikb-ref 标注。

### Pipeline (DevOps) — CI/CD 流水线 [pipeline.html]

| 字段 | 列名 | 原型来源 (pipeline.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `pipelineId` | `pipeline_id` | (主键) | BIGINT(20) | ✅ | — |
| `pipelineNo` | `pipeline_no` | (业务编号) | varchar(32) | ✅ | `PIPE-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目) | BIGINT(20) | ❌ | FK → tb_project |
| `pipelineName` | `pipeline_name` | 流水线名称 | varchar(200) | ✅ | — |
| `repoName` | `repo_name` | 代码仓库列 | varchar(200) | ✅ | org/repo |
| `repoBranch` | `repo_branch` | (分支) | varchar(100) | ❌ | 默认 main |
| `cicdTool` | `cicd_tool` | (工具选择) | varchar(30) | ❌ | 字典 `biz_pipeline_tool`: jenkins/gitlab/github/gitea (国产化) |
| `triggerType` | `trigger_type` | (触发方式) | varchar(20) | ❌ | 字典 `biz_pipeline_trigger`: manual/push/cron/tag |
| `cronExpr` | `cron_expr` | (Cron 表达式) | varchar(50) | ❌ | `triggerType=cron` 时必填 |
| `yamlContent` | `yaml_content` | (YAML 流水线定义) | LONGTEXT | ❌ | — |
| `lastRunStatus` | `last_run_status` | 状态列 | varchar(20) | ❌ | `biz_pipeline_result`: success/failed/running/skipped |
| `lastRunAt` | `last_run_at` | 执行时间列 | DATETIME | ❌ | — |
| `totalRuns` | `total_runs` | "总流水线数" | INT | ❌ | 默认 0 |
| `successCount` | `success_count` | "成功数" 卡片 | INT | ❌ | 默认 0 |
| `successRate` | `success_rate` | "成功率" 卡片 | DECIMAL(5,2) | ❌ | % 实时计算 |
| `status` | `status` | 启用/停用 | char(2) | ✅ | `biz_pipeline_status`: 00启用/01停用 |
| `authorUserId` | `author_user_id` | 创建者 | BIGINT | ✅ | FK → sys_user |

状态机: `00↔01` (启用/停用); cron 触发必填 cronExpr (602)

**业务入口**: `POST /business/pipeline/trigger/{id}` — 模拟执行 (85% 成功率),累加 totalRuns/successCount + 重算 successRate。

### FeatureFlag (DevOps) — Feature Flag [featureflag.html]

| 字段 | 列名 | 原型来源 (featureflag.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `flagId` | `flag_id` | (主键) | BIGINT(20) | ✅ | — |
| `flagNo` | `flag_no` | (业务编号) | varchar(32) | ✅ | `FF-YYYY-NNNN` |
| `flagKey` | `flag_key` | Flag Key 输入 | varchar(120) | ✅ | snake_case; (flagKey, environment) 唯一 |
| `title` | `title` | 功能说明 | varchar(200) | ✅ | — |
| `description` | `description` | 详细描述 | varchar(500) | ❌ | — |
| `environment` | `environment` | 环境 select | varchar(20) | ✅ | 字典 `biz_ff_env`: test/staging/prod |
| `rolloutPercentage` | `rollout_percentage` | 灰度百分比 slider | INT | ✅ | 0-100 |
| `rolloutStrategy` | `rollout_strategy` | 策略 radio | varchar(20) | ✅ | 字典 `biz_ff_strategy`: all_on(100)/canary(1-99)/all_off(0) |
| `targetUserSegment` | `target_user_segment` | (目标用户分群) | varchar(500) | ❌ | CSV 用户ID 或表达式 |
| `status` | `status` | 开启/关闭 | char(2) | ✅ | `biz_ff_status`: 00开启/01关闭 |
| `authorUserId` | `author_user_id` | 创建者 | BIGINT | ✅ | FK → sys_user |

状态机: `00↔01` (开启/关闭); 策略-百分比一致性硬校验 (canary 必须 1-99)

**业务入口**: `GET /business/feature-flag/check?flagKey=&environment=&userId=` — 实时判定。canary 用 `Math.abs(Long.hashCode(userId)) % 100 < rolloutPercentage`。

### DoraMetric (DevOps) — DORA 效能指标 [devops.html]

| 字段 | 列名 | 原型来源 (devops.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `doraId` | `dora_id` | (主键) | BIGINT(20) | ✅ | — |
| `doraNo` | `dora_no` | (业务编号) | varchar(32) | ✅ | `DORA-YYYY-NNNN` |
| `projectId` | `project_id` | (关联项目,可空) | BIGINT(20) | ❌ | NULL=全局 |
| `metricName` | `metric_name` | 指标名 | varchar(200) | ✅ | — |
| `metricType` | `metric_type` | DORA 4 指标 | varchar(30) | ✅ | 字典 `biz_dora_type`: deploy_freq/lead_time/mttr/change_fail_rate |
| `metricValue` | `metric_value` | 指标数值 | DECIMAL(12,2) | ✅ | — |
| `metricUnit` | `metric_unit` | 单位 | varchar(30) | ❌ | 次/天/小时/% |
| `periodType` | `period_type` | 周期 selector | varchar(20) | ✅ | `biz_dora_period`: month/quarter |
| `snapshotDate` | `snapshot_date` | 记录日期 | DATE | ✅ | — |
| `trendChartJson` | `trend_chart_json` | `#dora-trend-chart` | LONGTEXT | ❌ | 趋势图 JSON |
| `heatmapJson` | `heatmap_json` | `#deploy-heatmap` | LONGTEXT | ❌ | 仅 deploy_freq |
| `leadtimeBreakdown` | `leadtime_breakdown` | `#leadtime-breakdown` | LONGTEXT | ❌ | code/review/merge/deploy 阶段拆解 JSON |
| `aiSuggestions` | `ai_suggestions` | "AI 持续改进建议" | LONGTEXT | ❌ | Markdown |
| `aiGenerated` | `ai_generated` | (服务计算) | char(1) | ✅ | Y/N |
| `aiGeneratedAt` | `ai_generated_at` | (服务计算) | DATETIME | ❌ | — |
| `status` | `status` | 状态 | char(2) | ✅ | `biz_dora_status`: 00草稿/01已发布/02已归档 |
| `authorUserId` | `author_user_id` | 创建者 | BIGINT | ✅ | FK → sys_user |

状态机: `00→01→02` (草稿→已发布→已归档,单向)

**AI 入口**: `POST /business/dora/ai/suggest/{id}` — 按 metricType + value 阈值生成 DORA 等级评估 (Elite/High/Medium/Low) + 农情专项建议 (灌溉旺季容灾切换演练)。

---

## 3. 状态机汇总 (PRD §3.2 + 原型)

| 模块 | PRD § | 状态机 | 反向边 / 特殊 |
|---|:--:|---|---|
| `inception` | **F1.1** | `00→01→{02,04}` `02→{03,04}` `04→00` (反向) | 03 终态可"转项目" |
| `competitive` | **F1.3** | `00→01→02` (3 态) | 02 终态可归档 |
| `arch` | **F3.1** | `00→01→{00,02}` `02→{03}` (4 态) | 01→00 评审打回 |
| `dbdesign` | **F3.2** | 同 arch (4 态) | 01→00 评审打回 |
| `apidesign` | **F3.3** | 同 arch (4 态) | UNIQUE(method,path)→701 |
| `project` | **F1.2** | `status`: `00→{01,02,03}` `01→{00,03}` `02,03` 终态 / `lifecyclePhase`: `00→01→{00,02}` `02→{01,03}` `03→{02}` (status=00 时演进) | 双字段:总状态 + 交付阶段;**§2 已更新提案,代码 commit 待落地** |
| `requirement` | F2.1 | `00→{01,03}` `01→{00,02,03}` `02,03` 终态 | 01→00 打回 |
| `prd` | **F2.2** | `00→01→{00,02}` `02→{03}` `03` 终态 | 01→00 评审打回 |
| `sprint` | F3.4 | `00→{01,03}` `01→{02,03}` `02,03` 终态 | — |
| `task` | F3.4 | `00→01→02→03→04` 含 02↔01, 03↔02 反向边 | 评审打回 / 测试打回 |
| `defect` | F4.6 | 6 态 (新建→确认→修复→验证→关闭→重开) | 重开是反向边 |
| `testplan` | **F4.1** | `00→01→02→03` (4 态) | — |
| `testcase` | F4.2 | `00→01→02` (3 态) | — |
| `submission` | **F4.4** | `00→01→{02,04}` `02→{03,04}` `04→00` | 反向边 04→00, 错误码 708 |
| `testreport` | **F4.7** | `00→01→{00,02}` (3 态) | 01→00 打回 |
| `apidoc` | **F5.4** | `00→01→02` (3 态) | — |
| `manual-product` | **F5.1** | `00→01→02→{00,03}` (4 态) | 02→00 重新草稿 |
| `manual-impl` | **F5.2** | `00→01→02→{00,03}` (4 态) | 02→00 重新草稿 |
| `manual-ops` | **F5.3** | `00→01→02→{00,03}` (4 态) | 02→00 重新草稿 |
| `analytics` | **F6** | `00→01→02` (3 态) | 单向 |
| `dashboard` | UI §4.2 | `00↔01` (启用/停用) | 同用户 is_default 唯一 |
| `ai-agent` | **F3.5** | `00→{01,02}` `01→{00}` `02→{00,01}` | 错误态可重启或停用 |
| `openspec` | **F3.5** | `00→01→02` (3 态) | (specName,version) 唯一 |
| `pipeline` | DevOps | `00↔01` (启用/停用) | cron 必填 cronExpr (602) |
| `feature-flag` | DevOps | `00↔01` (开启/关闭) | 策略-百分比一致性硬校验 |
| `dora` | DevOps | `00→01→02` (3 态) | 单向 |
| `release` | DevOps | 5 态 含 03 回滚 / 04 废弃 | 03 需 rollbackReason |
| (其他空壳) | — | 待 PRD-align 时定义 | — |

---

## 4. 错误码规范 (项目级常量)

| 码 | 含义 | 来源 | 示例 |
|:--:|---|---|---|
| 200 | 成功 | RuoYi 标准 | — |
| 401 | 未认证 | RuoYi | — |
| 403 | 无权限 | RuoYi | — |
| 404 | 资源不存在 | RuoYi | — |
| 500 | 系统异常 | RuoYi | — |
| **601** | 状态转换违规 | 项目自定义 | Requirement 02→01 |
| **602** | 必填字段缺失 | 项目自定义 | Submission 退回无原因 |
| **604** | 字段格式非法 | 项目自定义 | TestReport riskLevel 不在 green/yellow/red |
| **701** | 唯一键冲突 | 项目自定义 | ApiDoc (method,path,version) |
| **702** | 外键不存在 | 项目自定义 | projectId 无对应项目 |
| **703** | 业务硬规则 | 项目自定义 | Sprint 单活跃 |
| **704** | 业务硬规则 | 项目自定义 | Sprint 关联任务 |
| **708** | 质量门禁未通过 | F4.4 | Submission 02→03 但 gate≠Y |

**规范**: 新增码段之前先在本表登记,不允许散落在代码注释里。

---

## 5. URL & 权限命名 (强制)

- 业务 API 路径: `/business/<entity>` (小写连字符)
- 权限串: `business:<entity>:<action>` (action ∈ {list, query, add, edit, remove, export})
- 字典 type: `biz_<entity>_<dim>` (例: `biz_release_status`, `biz_testreport_risk`)

---

## 6. AI 能力清单 (PRD §2.3 → Dify 工作流绑定)

| PRD § | 工作流名 | 调用模块 | 调用入口 | 当前状态 |
|:--:|---|---|---|:--:|
| F1.1 | `project-inception-flow` | inception | `POST /business/inception/ai/generate` | 🔴 未实现 |
| F1.3 | `competitive-analysis-flow` | competitive | `POST /business/competitive/ai/crawl` | 🔴 未实现 |
| F2.2 | `prd-generation-flow` | prd | `POST /business/prd/ai/generate` | 🔴 未实现 |
| F2.3 | `ued-review-flow` | ued | `POST /business/ued/ai/review` | 🔴 未实现 |
| F3.1 | `arch-design-flow` | arch | `POST /business/arch/ai/recommend` | 🔴 未实现 |
| F3.2 | `db-design-flow` | dbdesign | `POST /business/dbdesign/ai/er` | 🔴 未实现 |
| F3.3 | `detail-design-flow` | apidesign | `POST /business/apidesign/ai/openapi` | 🔴 未实现 |
| F3.5 | `coding-assist-flow` | ai-agent | `POST /business/ai-agent/invoke` | 🔴 未实现 |
| F4.1 | `test-plan-flow` | testplan | `POST /business/testplan/ai/generate` | 🟡 字段已留位 (`aiGenerated`) |
| F4.2 | `testcase-gen-flow` | testcase | `POST /business/testcase/ai/generate` | 🟡 字段已留位 |
| F4.3 | `data-gen-flow` | testdata | `POST /business/testdata/generate` | 🔴 未实现 |
| F4.5 | `auto-test-flow` | autotest | `POST /business/autotest/run` | 🔴 未实现 |
| F4.7 | `test-report-flow` | testreport | `POST /business/testreport/ai/generate` | 🟡 字段已留位 |
| F5.1 | `product-manual-flow` | manual-product | `POST /business/manual-product/ai/generate` | 🟡 字段已留位 |
| F5.2 | `impl-manual-flow` | manual-impl | `POST /business/manual-impl/ai/generate/{id}` | 🟡 mock 已实现 |
| F5.3 | `ops-manual-flow` | manual-ops | `POST /business/manual-ops/ai/generate/{id}` | 🟡 mock 已实现 |
| F5.4 | `api-doc-flow` | apidoc | `POST /business/apidoc/ai/extract` | 🟡 字段已留位 (`autoExtracted`) |
| F6   | `analytics-recommend-flow` | analytics | `POST /business/analytics/ai/recommend/{id}` | 🟡 mock 已实现 |
| F3.5 | `ai-agent-invoke-flow`     | ai-agent  | `POST /business/ai-agent/invoke/{id}`        | 🟡 mock (Dify proxy 占位) |
| F3.5 | `openspec-gen-flow`        | openspec  | `POST /business/openspec/ai/generate/{id}`   | 🟡 mock 已实现 |
| DevOps | `dora-suggest-flow`      | dora      | `POST /business/dora/ai/suggest/{id}`        | 🟡 mock 已实现 |

**约定**: AI 调用统一走 `POST /business/<entity>/ai/<verb>`,内部通过 Dify HTTP API 转发。当前阶段先把字段位预留,Dify 接入 Phase 安排在文档体系收尾后。

---

## 7. 16 个空壳模块的 PRD-align 实施优先级 (路线图)

按 PRD §3.5 迭代规划 (Phase 1~4) 优先级排序:

### P0 (Phase 1 MVP 范围,优先攻)
1. `inception` (F1.1) — 立项 AI 助手,**当前连 Maven 模块都没建**
2. `prd` (F2.2) — AI PRD 生成,系统的差异化卖点
3. `competitive` (F1.3) — 竞品情报,F1 域完整

### P1 (Phase 2)
4. `arch` (F3.1) — HLD 概要设计
5. `dbdesign` (F3.2) — 数据库设计
6. `apidesign` (F3.3) — LLD 接口详细设计
7. `ued` (F2.3) — UED 设计协同
8. `testdata` (F4.3) — 测试数据工厂
9. `autotest` (F4.5) — 自动化测试管理

### ✅ P2 (Phase 3) — 全部完成
10. ✅ `manual-impl` (F5.2) — 实施手册
11. ✅ `manual-ops` (F5.3) — 运维手册
12. ✅ `analytics` (F6) — 效能分析 (DORA + PLM 指标 + AI 复盘)
13. ✅ `dashboard` (UI §4.2) — 工作台预设 + 6-widget 聚合查询

### ✅ P3 (扩展 / Phase 4) — 全部完成
14. ✅ `ai-agent` — AI Agent 编排 (6 类 Agent + Dify 工作流)
15. ✅ `openspec` — AI OpenSpec (4 类规范 + AgriKB 引用)
16. ✅ `pipeline` — CI/CD 流水线 (含 Gitea 国产化)
17. ✅ `feature-flag` — 灰度发布 / 紧急开关 / 环境隔离
18. ✅ `dora` — DORA 4 指标 + 持续改进建议

🎉 **全部 31 个业务模块 PRD-aligned 完成,空壳清零!**

**每个模块 PRD-align 落地的 DoD (完成定义)** — 见 §8。

---

## 8. 单模块 PRD-align 落地 DoD (Definition of Done)

每个空壳模块完成 PRD-align 必须满足 **9 项硬指标**:

```
□ 1. 在 PRD-MAPPING.md §2 追加字段对照表 (Domain 字段 ↔ 原型表单元素)
□ 2. business-<entity>.sql 建表 + 字典 (biz_<entity>_*)
□ 3. Domain.java 字段完整,Excel/JsonFormat 注解到位
□ 4. Mapper.xml 完整 resultMap + 动态 trim + selectMaxSeqOfYear
□ 5. Mapper.java 接口含 selectMaxSeqOfYear
□ 6. ServiceImpl.java 包含 (a) FK 校验 702 (b) 状态机校验 601 (c) 编号生成
□ 7. Controller 6 个标准端点 + 权限串 business:<entity>:*
□ 8. E2E spec 至少 1 个测试,覆盖 POST /business/<entity> 返回 code=200
□ 9. 跑 mvn install BUILD SUCCESS + 跑 E2E 测试绿
```

---

## 9. 字典数据规范

每个新模块字典数据写入位置:
- `dict_type` 行:`(dict_name, dict_type='biz_<entity>_<dim>', status='0', remark='N 状态机')`
- `dict_data` 行:`(dict_sort, dict_label, dict_value, dict_type, css_class='', list_class, is_default, status)`

`list_class` 配色映射:
- `info` (灰) = 草稿/未启动
- `warning` (黄) = 进行中/审核中
- `primary` (蓝) = 关键活跃态
- `success` (绿) = 完成/已发布 (终态)
- `danger` (红) = 失败/退回/废弃 (终态)

---

## 10. 变更流程 (规范执行)

任何对本表的修改 = 任何字段/状态/路径的改动,流程:

1. **提议**: 在 `99-跨阶段/proposals/` 提一份 `<YYYY-MM-DD>-prd-mapping-<topic>.md`,说明变更动机 + 影响面
2. **校验**: 引用 PRD § + 原型 HTML 行号作为证据
3. **审核**: 项目负责人 + 至少 1 个相关角色 review
4. **落地**: 同时改 PRD-MAPPING.md + 代码,**两者必须同一 commit**
5. **回归**: 跑全量 E2E suite

**禁止**: 改代码但不改本表,或反之。两者必须同步。

---

## 附录 A. 模块依赖图 (Maven)

```
plm-admin (entry)
  ├── plm-framework
  │    └── plm-system
  │         └── plm-common
  └── 业务模块 × 30
       └── (✅ 全部 31 个已对齐 — PRD-align 完结)
            inception, competitive, project, requirement, prd, ued,
            arch, dbdesign, apidesign, sprint, task, ai-agent, openspec,
            testplan, testcase, testdata, submission, autotest, defect,
            testreport, manual-product, manual-impl, manual-ops,
            apidoc, document, release, analytics, dashboard,
            pipeline, feature-flag, dora
```

所有业务模块 → `plm-project` (FK 检查) + `plm-common` (BaseEntity) + `plm-system` (sys_user 关联)。

---

*文档结束 | 本文件版本变更通过 git history 追踪 | 任何分歧以本文件为准*

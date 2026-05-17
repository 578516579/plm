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
| F2.1 | 设计 | `requirement` | `requirements.html` | `tb_requirement` | `REQ-YYYY-NNNN` (ADR-0002) | 🟢 **PRD-aligned** |
| F2.2 | 设计 | `prd` | `prd.html` | `tb_prd` | `PRD-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F2.3 | 设计 | `ued` | `ued.html` | `tb_ued` | `UED-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F2.4 | 设计 | (评审 → `requirement`) | (并入需求) | (字段) | n/a | 🟢 |
| F3.1 | 研发 | `arch` | `archdesign.html` | `tb_arch` | `ARCH-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F3.2 | 研发 | `dbdesign` | `dbdesign.html` | `tb_dbdesign` | `DB-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F3.3 | 研发 | `apidesign` | `apidesign.html` | `tb_apidesign` | `APID-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F3.4 | 研发 | `sprint`+`task` | `kanban.html` | `tb_sprint`,`tb_task` | `SPR-`,`TASK-` (ADR-0004/0003) | 🟢 **PRD-aligned** |
| F3.5 | 研发 | `ai-agent`,`openspec` | `aiagents.html`,`aispec.html` | `tb_ai_agent`,`tb_openspec` | `AGT-YYYY-NNNN`,`SPEC-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F3.6 | 研发 | (联调 → `task`) | (并入任务) | (字段) | n/a | 🟢 |
| F4.1 | 质量 | `testplan` | `testplan.html` | `tb_testplan` | `TP-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F4.2 | 质量 | `testcase` | `testcase.html` | `tb_testcase` | `TC-YYYY-NNNN` (ADR-0006) | 🟢 **PRD-aligned** |
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

- 🟢 **PRD-aligned** (粗体,§2 字段表 + 代码 + 测试绿) = **29 个**: inception / **project** / **requirement** / competitive / prd / ued / arch / dbdesign / apidesign / **sprint** / **task** / **testcase** / **testplan** / **testreport** / testdata / autotest / **submission** / **manual-product** / manual-impl / manual-ops / **apidoc** / analytics / dashboard / ai-agent / openspec / pipeline / feature-flag / dora / **release**
- 🟢 早期对齐 (待 ADR-D/C 决策) = **2 个**: defect / document
- 🟡 字段表已提案 / 代码待对齐 = **0 个**
- 🟡 空壳 = **0 个**
- 🔴 空 = **0 个**

(SSoT 矛盾已全部消除 — submission/release 历史粗体标记本会话已补字段表对齐)

**审计待办** (跑偏检测 §M.6):
- ✅ project (完成): 5 处字段 drift 修复 + 双状态机 + 字典补全 (commit `20b5bb6`→`3c10238`→`522b6df`)
- ✅ requirement (完成): aiValue 字段 + ADR-A 4 态状态机决策 (commit `1afe0ba`→`df35652`→`6eb0c95`)
- ✅ 6 个 🟢 轻微模块补 §2 字段表: Sprint / Task / TestPlan / TestReport / ApiDoc / ManualProduct (commit `8d1d543`)
- ✅ Release (完成): strategy 字典补 `direct_replace` + §2 字段表 (commit `86064c5`)
- ✅ Submission (完成): biz_submission_environment 5 值字典 + Service 白名单 + §2 字段表 (本 commit)
- ✅ **12 个"早期对齐"模块 drift 审计已完成** (2026-05-17),详见 [99-跨阶段/audits/2026-05-17-12-modules-drift-audit.md](99-跨阶段/audits/2026-05-17-12-modules-drift-audit.md)

**审计结果摘要 (剩余 3 个 🟡 中等待修,全部需 ADR)**:
- TestCase (F4.2): category 字典缺农业专项类型 (需 ADR-B 字典口径)
- Defect (F4.6): 状态机三方不一致 (PRD 5 态 / 原型 4 态 / Domain 5 态 label 不同 / MAPPING §3 标 6 态);`module` 字段缺;**需 ADR 统一状态机**
- Document (F5.5): 与 PRD §F5.5 "知识库管理" 概念错位,需 **ADR-C** 决策 (重命名 vs 拆出 knowledge-base 模块)

审计发现 3 个**架构性问题需 ADR 决策** (Requirement 状态机口径 / Defect 状态机三方不一致 / Document 与 PRD §F5.5 知识库的概念错位),不能仅靠"补字段补字典"解决 — 详见审计报告"关键架构性问题"段。

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

### Requirement (F2.1) — 需求采集与管理 [requirements.html L152 表单 + L264 状态选项 + PRD §F2.1 L243-247]

> **状态**: 🟢 **PRD-aligned** (字段表 commit `1afe0ba` → 代码 commit `df35652`,mvn test 18/18 绿)

**PRD/原型 → 字段映射**:

| 字段 | 列名 | 原型来源 | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `requirementId` | `requirement_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `requirementNo` | `requirement_no` | (业务编号) | varchar(32) | ✅ | `REQ-YYYY-NNNN` (ADR-0002) |
| `projectId` | `project_id` | (隐式 FK,模块从 project 上下文进入) | BIGINT(20) | ✅ | FK→tb_project.id |
| `title` | `title` | `需求标题 *` (`nr-title`) | varchar(200) | ✅ | — |
| `description` | `description` | `详细描述` (`nr-desc`) | TEXT | ❌ | Markdown 兼容 |
| `source` | `source` | `来源` select (`nr-src` 4 值) | varchar(2) | ✅ | 字典 `biz_req_source`:`01` 客户反馈 / `02` 内部提案 / `03` 运营数据 / `04` 竞品分析 |
| `priority` | `priority` | `优先级` select (`nr-pri` 3 值) | varchar(2) | ✅ | 字典 `biz_req_priority`:`00` P0 紧急 / `01` P1 重要 / `02` P2 一般 |
| `status` | `status` | `状态` select (`rdm-edit-status` 4 值) | varchar(2) | ✅ | 字典 `biz_req_status`:见状态机 (4 态实用版,ADR-A) |
| `aiValue` | `ai_value` | `AI价值评估` select (`rdm-edit-ai` 高/中/低) + PRD §F2.1 "AI 优先级初评" | varchar(2) | ❌ | 字典 `biz_req_ai_value`:`H` 高价值 / `M` 中价值 / `L` 低价值 (本次新增,见 §决策记录 D2) |
| `assigneeUserId` | `assignee_user_id` | (modal-newreq 未显式;在 reqdetail 内显式) | BIGINT(20) | ❌ | FK→sys_user.user_id |
| `reviewNote` | `review_note` | (状态推进时填,modal-reqdetail 评审区) | varchar(500) | ❌ | 评审纪要;扩展字段,见 §决策记录 D3 |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | `0`=正常 / `2`=删除 |

**字段决策记录** (实现与 PRD/原型有差异处):
- **D1 = ADR-A: 状态机走 4 态实用版,不走 PRD §F2.1 6 态完整版** — PRD §F2.1 L247 描述状态机为"草稿→评审→确认→开发中→完成→验收"(6 态);原型 `rdm-edit-status` 提供 4 选项 (待评审/开发中/已完成/已取消);当前实现采纳 4 态。
   - **依据**: (a) 原型 UI 仅提供 4 选项,UI 与状态机必须一致;(b) 项目其他 19 个 PRD-aligned 模块均"原型优先" — 与原型保持一致是项目宪法级惯例;(c) 4 态合并了 PRD 的"草稿/评审/确认"三个早期态(在实际 PM 工作中本来就常常合并),减少状态切换次数。
   - **代价**: 状态机层无法追溯 PRD 完整 6 态;特别是"已验收"独立终态被裁掉。
   - **缓解**: 后续若需求侧需要"已验收"终态,通过加 `acceptedAt DATETIME` 字段标记验收时间点(数据维度可追溯),或在 Phase 06 引入独立 acceptance 模块。本期不实施。
   - **审批/留痕**: 本决策同时记录在本字段表 D1 和 §3 状态机汇总表的 requirement 行,作为单一事实来源。
- **D2: 加 `aiValue` 字段** — 原型 modal-reqdetail 的 `rdm-edit-ai` select 显示"高/中/低 AI 价值评估",PRD §F2.1 L245 提到"AI 优先级初评"。当前 Domain 缺,本次补上。值域采用 H/M/L 单字符语义编码(与其他业务模块的两位数惯例区分,因为 priority 已经占用 P0/P1/P2)。
- **D3: 保留 `reviewNote` 扩展字段** — 原型 modal-newreq 未直接显式,但 modal-reqdetail 的评审区域需要承载评审纪要,且状态机推进的审计追溯需要文字说明。

**状态机** (status, 4 态实用版 — ADR-A):
- `00` 待评审 → `{01, 03}` (启动开发 / 取消)
- `01` 开发中 → `{00, 02, 03}` (评审打回 / 完成 / 取消)
- `02` 已完成 → `{}` (终态)
- `03` 已取消 → `{}` (终态)

非法转换抛 ServiceException(601)。`aiValue` / `priority` / `source` 字段在 service 入口校验白名单,非法抛 604。

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

### Sprint (F3.4) — 迭代管理 [kanban.html modal-sprint + PRD §F3.4 L304-308]

> **状态**: 🟢 **PRD-aligned** (本次审计 2026-05-17 后补字段表;代码早已实现且 drift 审计 🟢 轻微 = 字段已对齐)

| 字段 | 列名 | 原型来源 (kanban.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `sprintId` | `sprint_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `sprintNo` | `sprint_no` | (业务编号) | varchar(32) | ✅ | `SPR-YYYY-NNNN` (ADR-0004) |
| `projectId` | `project_id` | (隐式 FK,从 kanban URL 上下文) | BIGINT(20) | ✅ | FK→tb_project.id |
| `name` | `name` | `迭代名称` (`ns-sprint-name` 占位 "Sprint 4") | varchar(200) | ✅ | — |
| `goal` | `goal` | `目标` (`ns-sprint-goal` 一句话) | varchar(500) | ❌ | — |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 字典 `biz_sprint_status`,4 态 |
| `plannedStartDate` | `planned_start_date` | `开始日期` (`ns-sprint-start`) | DATE | ❌ | — |
| `plannedEndDate` | `planned_end_date` | (推导自 start + durationDays) | DATE | ❌ | — |
| `actualStartDate` | `actual_start_date` | (服务计算) | DATE | ❌ | `00→01` 自动填 |
| `actualEndDate` | `actual_end_date` | (服务计算) | DATE | ❌ | `01→02` 自动填 |
| `durationDays` | `duration_days` | `工期(天)` (`ns-sprint-days` 默认 14) | INT | ❌ | 默认 14 |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**状态机** (4 态): `00 计划中 → 01 进行中 → 02 已完成` / `01 → 03 已取消`。`02/03` 终态。

**业务硬规则**: 同一 `project_id` 下 `status='01'` (进行中) 唯一 — 抛 ServiceException(703)。

### Task (F3.4) — 任务管理 [kanban.html modal-newtask L149 + modal-taskdetail L175-216 + PRD §F3.4]

> **状态**: 🟢 **PRD-aligned** (字段对齐;审计 🟢 轻微)

| 字段 | 列名 | 原型来源 (kanban.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `taskId` | `task_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `taskNo` | `task_no` | (业务编号) | varchar(32) | ✅ | `TASK-YYYY-NNNN` (ADR-0003) |
| `projectId` | `project_id` | (隐式 FK) | BIGINT(20) | ✅ | FK→tb_project.id |
| `requirementId` | `requirement_id` | `关联需求` select (`tdm-req`) | BIGINT(20) | ❌ | FK→tb_requirement,可空 |
| `sprintId` | `sprint_id` | (kanban 上下文) | BIGINT(20) | ❌ | FK→tb_sprint,可空 |
| `title` | `title` | `任务标题 *` (`nt-title` / `tdm-edit-title`) | varchar(200) | ✅ | — |
| `description` | `description` | `详细描述` (`tdm-edit-desc`) | TEXT | ❌ | — |
| `status` | `status` | `看板列` select (`nt-col` / `tdm-col` 6 值) | varchar(2) | ✅ | 字典 `biz_task_status`,6 态含反向边 |
| `priority` | `priority` | `优先级` select (`nt-pri` / `tdm-pri` P0-P2) | varchar(2) | ✅ | 字典 `biz_task_priority` |
| `assigneeUserId` | `assignee_user_id` | `负责人` select (`nt-owner` / `tdm-owner` 4 选项) | BIGINT(20) | ❌ | FK→sys_user;原型自由文本→升级 FK |
| `estimatedHours` | `estimated_hours` | `预估工时(h)` (`nt-hours` / `tdm-hours` 默认 8) | DECIMAL(6,2) | ❌ | — |
| `actualHours` | `actual_hours` | (完成时填) | DECIMAL(6,2) | ❌ | — |
| `mrUrl` | `mr_url` | `🔗 关联MR/代码评审` (`tdm-mr-list`) | varchar(500) | ❌ | — |
| `mrBranch` | `mr_branch` | (MR 分支名) | varchar(200) | ❌ | — |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**状态机** (6 态含反向边): `00 待开发 → 01 开发中 → 02 代码评审 → 03 测试中 → 04 已完成` / `05 已取消`,含 `02→01` (评审打回反向边) 和 `03→02` (测试打回反向边),与 PRD-MAPPING §3 一致。

**待用户确认**: 原型 `taskColumn` 看板列(待开发/开发中/代码评审/测试中/已完成)与 status 6 态对齐,**无独立 taskColumn 字段** — 看板列由 status 直接驱动。

### TestPlan (F4.1) — 测试方案 [testplan.html L141-152 + PRD §F4.1 L334-337]

> **状态**: 🟢 **PRD-aligned** (字段已对齐;审计 🟢 轻微)

| 字段 | 列名 | 原型来源 (testplan.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `testplanId` | `testplan_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `testplanNo` | `testplan_no` | (业务编号) | varchar(32) | ✅ | `TP-YYYY-NNNN` |
| `projectId` | `project_id` | `关联项目` select | BIGINT(20) | ✅ | FK→tb_project.id |
| `sprintId` | `sprint_id` | `关联迭代` select | BIGINT(20) | ❌ | FK→tb_sprint.id,可空 |
| `title` | `title` | `方案标题` (输入框) | varchar(200) | ✅ | — |
| `testTypes` | `test_types` | `测试类型` 5 checkbox (功能/接口/性能/自动化/安全) | varchar(200) | ✅ | CSV: functional,api,performance,automation,security |
| `testCycleDays` | `test_cycle_days` | `测试周期(天)` (input number) | INT | ❌ | — |
| `scope` | `scope` | (PRD §F4.1 "范围界定") | TEXT | ❌ | — |
| `strategy` | `strategy` | (PRD §F4.1 "策略选择") | TEXT | ❌ | — |
| `toolsRecommended` | `tools_recommended` | (PRD §F4.1 "工具推荐") | TEXT | ❌ | AI 生成 |
| `resourcesPlan` | `resources_plan` | (PRD §F4.1 "资源分配") | TEXT | ❌ | AI 生成 |
| `riskAssessment` | `risk_assessment` | (PRD §F4.1 "风险评估") | TEXT | ❌ | AI 生成 |
| `aiGenerated` | `ai_generated` | (服务计算,F4.1 AI 一键生成测试方案) | CHAR(1) | ❌ | Y/N |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 字典 `biz_testplan_status`,4 态 |
| `authorUserId` | `author_user_id` | (创建者) | BIGINT(20) | ❌ | FK→sys_user |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**状态机** (4 态): `00 草稿 → 01 已确认 → 02 执行中 → 03 已完成` (单向)。

**AI 入口**: `POST /business/testplan/ai/generate` — Dify 工作流生成 strategy/tools/resources/risks 4 字段。

### TestReport (F4.7) — 测试报告 [testreport.html `genTestReport()` + PRD §F4.7 L373-378]

> **状态**: 🟢 **PRD-aligned** (字段已对齐;审计 🟢 轻微;反向边 01→00 由 Service 层校验)

| 字段 | 列名 | 原型来源 (testreport.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `testreportId` | `testreport_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `testreportNo` | `testreport_no` | (业务编号) | varchar(32) | ✅ | `TR-YYYY-NNNN` |
| `projectId` | `project_id` | (隐式 FK) | BIGINT(20) | ✅ | FK→tb_project.id |
| `sprintId` | `sprint_id` | (关联迭代) | BIGINT(20) | ❌ | FK→tb_sprint.id |
| `testplanId` | `testplan_id` | (关联测试方案) | BIGINT(20) | ❌ | FK→tb_testplan.id |
| `title` | `title` | (报告标题,AI 生成) | varchar(200) | ✅ | — |
| `totalCases` | `total_cases` | "总用例数" KPI | INT | ❌ | — |
| `passedCases` | `passed_cases` | "通过用例" KPI | INT | ❌ | — |
| `failedCases` | `failed_cases` | "失败用例" KPI | INT | ❌ | — |
| `coverageRate` | `coverage_rate` | "覆盖率%" KPI | DECIMAL(5,2) | ❌ | — |
| `defectSummary` | `defect_summary` | (缺陷汇总 JSON) | TEXT | ❌ | — |
| `p0Defects` / `p1Defects` / `p2Defects` | (同名) | (按 P0/P1/P2 分布卡片) | INT | ❌ | 冗余于 defectSummary,便于查询 |
| `riskLevel` | `risk_level` | `上线风险` 三色徽章 (PRD §F4.7) | varchar(8) | ✅ | 字典 `biz_testreport_risk`: green/amber/red |
| `riskEvaluation` | `risk_evaluation` | (风险评估文本,AI 生成) | TEXT | ❌ | — |
| `recommendations` | `recommendations` | (AI 改进建议) | TEXT | ❌ | — |
| `aiGenerated` | `ai_generated` | (服务计算) | CHAR(1) | ❌ | Y/N |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 字典 `biz_testreport_status`,3 态含反向边 |
| `generatedAt` | `generated_at` | (服务计算) | DATETIME | ❌ | AI 生成时间戳 |
| `reviewerUserId` | `reviewer_user_id` | (审核人) | BIGINT(20) | ❌ | FK→sys_user |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**状态机** (3 态含反向边): `00 草稿 → 01 审核中 → 02 已发布`,反向边 `01→00` 审核打回。

**AI 入口**: `POST /business/testreport/ai/generate` — 输入 testplanId + 缺陷统计,Dify 生成 title/defectSummary/recommendations/riskLevel。

### ApiDoc (F5.4) — API 文档 [apidoc.html L137-138 "从代码同步" + PRD §F5.4 L409-412]

> **状态**: 🟢 **PRD-aligned** (字段已对齐;审计 🟢 轻微;F5.4 变更订阅未上线,本期可不补)

| 字段 | 列名 | 原型来源 (apidoc.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `apidocId` | `apidoc_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `apidocNo` | `apidoc_no` | (业务编号) | varchar(32) | ✅ | `API-YYYY-NNNN` |
| `projectId` | `project_id` | (隐式 FK) | BIGINT(20) | ✅ | FK→tb_project.id |
| `title` | `title` | (接口标题) | varchar(200) | ✅ | — |
| `httpMethod` | `http_method` | "HTTP方法" 标签 | varchar(10) | ✅ | GET/POST/PUT/DELETE |
| `path` | `path` | "接口路径" | varchar(500) | ✅ | 例 /api/v1/irrigation/recommend |
| `description` | `description` | "接口描述" | TEXT | ❌ | — |
| `requestSchema` | `request_schema` | (请求体 JSON Schema) | TEXT | ❌ | — |
| `responseSchema` | `response_schema` | (响应体 JSON Schema) | TEXT | ❌ | — |
| `openapiSpec` | `openapi_spec` | (OpenAPI 3.0 完整规范) | LONGTEXT | ❌ | — |
| `sourceClass` | `source_class` | (源 Controller 类名,F5.4 从代码提取) | varchar(200) | ❌ | — |
| `sourceMethod` | `source_method` | (源方法名) | varchar(200) | ❌ | — |
| `version` | `version` | (版本号) | varchar(20) | ✅ | 例 v1.0 |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 字典 `biz_apidoc_status`,3 态 |
| `lastSyncedAt` | `last_synced_at` | (最后从代码同步时间) | DATETIME | ❌ | — |
| `autoExtracted` | `auto_extracted` | (是否自动提取,F5.4) | CHAR(1) | ❌ | Y/N |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**唯一键**: `UNIQUE(project_id, http_method, path, version)` — 重复抛 ServiceException(701)。

**状态机** (3 态): `00 草稿 → 01 已发布 → 02 已废弃` (单向)。

**未实现** (PRD §F5.4 待补): API 变更记录 (`changeLog`) + 订阅推送 (`subscriberUserIds`)。本期未上线变更订阅功能,不补字段。

### ManualProduct (F5.1) — 产品手册 [productmanual.html L141-152 + PRD §F5.1 L392-396]

> **状态**: 🟢 **PRD-aligned** (字段已对齐;审计 🟢 轻微;反向边 02→00 由 Service 层校验)

| 字段 | 列名 | 原型来源 (productmanual.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `manualproductId` | `manualproduct_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `manualproductNo` | `manualproduct_no` | (业务编号) | varchar(32) | ✅ | `PM-YYYY-NNNN` |
| `projectId` | `project_id` | (隐式 FK) | BIGINT(20) | ✅ | FK→tb_project.id |
| `title` | `title` | (手册标题,AI 生成) | varchar(200) | ✅ | — |
| `productVersion` | `product_version` | `产品版本` select | varchar(20) | ✅ | 例 v1.0 |
| `includeModules` | `include_modules` | 5 项 checkbox (系统概述/快速上手/功能详细/FAQ/视频教程) | varchar(500) | ❌ | CSV |
| `content` | `content` | (手册正文 Markdown) | LONGTEXT | ❌ | AI 生成 |
| `screenshotsUrls` | `screenshots_urls` | `截图上传` 区域 | TEXT | ❌ | CSV 截图 URL 列表 |
| `screenshotsCount` | `screenshots_count` | (推导,便于查询) | INT | ❌ | 冗余于 screenshotsUrls |
| `outputFormats` | `output_formats` | (导出格式,PRD §F5.1 多格式) | varchar(100) | ❌ | CSV: word/pdf/html/h5,默认 pdf |
| `aiGenerated` | `ai_generated` | (服务计算) | CHAR(1) | ❌ | Y/N |
| `generatedAt` | `generated_at` | (AI 生成时间) | DATETIME | ❌ | — |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 字典 `biz_manualproduct_status`,4 态含反向边 |
| `authorUserId` | `author_user_id` | (创建者) | BIGINT(20) | ❌ | FK→sys_user |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**状态机** (4 态含反向边): `00 草稿 → 01 生成中 → 02 已生成 → 03 已发布`,反向边 `02→00` 重新生成。

**AI 入口**: `POST /business/manual-product/ai/generate` — Dify 工作流根据 includeModules + screenshots 一键生成 content。

### Release (DevOps 扩展) — 发布管理 [release.html L593 modal-newrelease + DORA 4 指标]

> **状态**: 🟢 **PRD-aligned** (本次审计补字段表 + strategy 字典补 `direct_replace` 对齐原型 4 选项)

| 字段 | 列名 | 原型来源 (release.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `releaseId` | `release_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `releaseNo` | `release_no` | (业务编号) | varchar(32) | ✅ | `REL-YYYY-NNNN` |
| `version` | `version` | `版本号` (`nr-version` 占位 v2.2.0) | varchar(50) | ✅ | 例 v1.2.3 |
| `projectId` | `project_id` | `项目` select (`nr-proj`) | BIGINT(20) | ✅ | FK→tb_project.id |
| `sprintId` | `sprint_id` | (关联迭代) | BIGINT(20) | ❌ | FK→tb_sprint.id |
| `strategy` | `strategy` | `发布策略` select (`nr-strategy` 4 值) | varchar(20) | ✅ | 字典 `biz_release_strategy`:`blue_green` 蓝绿 / `canary` 金丝雀(10%) / `rolling` 滚动更新 / `direct_replace` 直接替换 — 对齐原型 4 选项 (D1) |
| `environment` | `environment` | `目标环境` select (`nr-env` STAGING/PROD) | varchar(20) | ✅ | 默认 `prod` |
| `releaseNotes` | `release_notes` | `发布说明` textarea (`nr-note`) | TEXT | ❌ | Markdown |
| `plannedAt` | `planned_at` | (计划发布时间) | DATETIME | ❌ | — |
| `releasedAt` | `released_at` | (实际发布时间) | DATETIME | ❌ | `01→02` 时自动填 |
| `rollbackAt` | `rollback_at` | (回滚时间) | DATETIME | ❌ | `*→03` 时自动填 |
| `rollbackReason` | `rollback_reason` | (回滚原因) | varchar(500) | 条件必填 | `status=03` 必填,见决策记录 D2 |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 字典 `biz_release_status`,5 态 |
| `aiReviewScore` | `ai_review_score` | "AI 发布评审分" | DECIMAL(3,1) | ❌ | 0-10 分 |
| `aiReviewNotes` | `ai_review_notes` | "AI 发布评审意见" | TEXT | ❌ | — |
| `deploymentFrequency` | `deployment_frequency` | DORA 4 指标 — 部署频率 (`dora-row`) | DECIMAL(5,2) | ❌ | — |
| `leadTimeHours` | `lead_time_hours` | DORA — 变更前置时间(小时) | DECIMAL(8,2) | ❌ | — |
| `mttrMinutes` | `mttr_minutes` | DORA — 平均恢复时间(分钟) | DECIMAL(8,2) | ❌ | — |
| `changeFailureRate` | `change_failure_rate` | DORA — 变更失败率(%) | DECIMAL(5,2) | ❌ | — |
| `releasedByUserId` | `released_by_user_id` | (发布人) | BIGINT(20) | ✅ | FK→sys_user |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**字段决策记录**:
- **D1: strategy 字典 4 值** — 原型 `nr-strategy` 提供 4 选项 (蓝绿/金丝雀10%/滚动/**直接替换**)。本次审计补 `direct_replace` 字典值 + service 白名单,对齐原型。`direct_replace` 因风险最高 (停机替换),建议触发 AI 评审 score < 7 时强校验。
- **D2: `rollbackReason` 条件必填** — `status` 转入 `03 已回滚` 时强制要求 `rollback_reason` 非空 (现有 service 已实现,抛 602)。

**状态机** (5 态含反向边/分支):
- `00` 计划中 → `{01 发布中, 04 已废弃}`
- `01` 发布中 → `{02 已发布, 03 已回滚}`
- `02` 已发布 → `{03 已回滚, 04 已废弃}` (允许后期回滚)
- `03` 已回滚 → `{04 已废弃}`
- `04` 已废弃 → `{}` (终态)

**唯一键**: `UNIQUE(project_id, version)` — 同项目同版本号禁重复;`UNIQUE(release_no)`。

**关联**: DORA 4 指标可由 `plm-dora` 模块的 `tb_dora_metric` 横向聚合;本表内字段是单次发布的快照。

### Submission (F4.4) — 提测管理 [submit.html L158 modal-newsubmit + PRD §F4.4 L355-359]

> **状态**: 🟢 **PRD-aligned** (本次补 §2 字段表 + environment 字典对齐原型,消除 SSoT 矛盾)

| 字段 | 列名 | 原型来源 (submit.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `submissionId` | `submission_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `submissionNo` | `submission_no` | (业务编号) | varchar(32) | ✅ | `SUB-YYYY-NNNN` |
| `projectId` | `project_id` | (隐式 FK) | BIGINT(20) | ✅ | FK→tb_project.id |
| `sprintId` | `sprint_id` | (关联迭代) | BIGINT(20) | ❌ | FK→tb_sprint.id |
| `title` | `title` | `提测标题 *` (`ns-title`) | varchar(200) | ✅ | — |
| `scope` | `scope` | `提测范围` (`ns-scope`) | TEXT | ❌ | 涉及需求/任务 CSV |
| `environment` | `environment` | `测试环境` select (`ns-env` 2 选项) | varchar(20) | ✅ | 字典 `biz_submission_environment` 5 值 (D1),默认 `test` |
| `expectedTestDays` | `expected_test_days` | `期望测试周期(天)` (`ns-days` 默认 3) | INT | ❌ | 默认 5 |
| `riskNotes` | `risk_notes` | (风险提示) | TEXT | ❌ | — |
| `unitTestCoverage` | `unit_test_coverage` | `质量门禁 - 单测覆盖率` (`submitGateResult`) | DECIMAL(5,2) | ❌ | ≥60 才通过 (PRD §F4.4) |
| `codeScanPassed` | `code_scan_passed` | `质量门禁 - 代码扫描` | CHAR(1) | ❌ | Y/N |
| `prdCompleted` | `prd_completed` | `质量门禁 - PRD 完整` | CHAR(1) | ❌ | Y/N |
| `apiDocUpdated` | `api_doc_updated` | `质量门禁 - API 文档已更新` | CHAR(1) | ❌ | Y/N |
| `qualityGatePassed` | `quality_gate_passed` | (服务端 AI 计算:4 项 ∧) | CHAR(1) | (auto) | **不接受前端写入**;Service 重算 |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 字典 `biz_submission_status`,5 态含反向边 |
| `rejectReason` | `reject_reason` | (退回原因) | varchar(500) | 条件必填 | `status=04` 必填 (602) |
| `submitterUserId` | `submitter_user_id` | (提测人) | BIGINT(20) | ✅ | FK→sys_user |
| `reviewerUserId` | `reviewer_user_id` | (测试经理审批人) | BIGINT(20) | ❌ | FK→sys_user |
| `submittedAt` | `submitted_at` | (提交时间) | DATETIME | ❌ | `00→01` 自动填 |
| `approvedAt` | `approved_at` | (通过时间) | DATETIME | ❌ | `02→03` 自动填 |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**字段决策记录**:
- **D1: environment 字典 5 值兼容方案** — 原型 `modal-newsubmit` 提供 2 选项 (测试环境 TEST / 预发环境 PRE);SQL 历史注释为 `dev/staging/prod` 3 值;E2E `submission.spec.ts` 使用 `staging`。本次创建 `biz_submission_environment` 字典覆盖 **5 个值**: `test`(默认) / `pre` / `dev` / `staging` / `prod`,Service 加白名单 (604)。原型核心场景(测试 + 预发)用 test/pre;兼容 E2E 用 staging;dev/prod 保留覆盖特殊场景。SQL 默认值 `dev` → `test` 对齐原型第 1 选项。
- **D2: `qualityGatePassed` 服务端计算字段** — 不接受前端写入。Service `computeQualityGate(s)` 按 PRD §F4.4 计算:单测覆盖率 ≥60 ∧ codeScanPassed=Y ∧ prdCompleted=Y ∧ apiDocUpdated=Y → 'Y',否则 'N'。任意门禁字段变更触发重算。

**状态机** (5 态含反向边 04→00):
- `00` 草稿 → `{01}`
- `01` 已提交 → `{02, 04}`
- `02` 质量门禁中 → `{03, 04}`
- `03` 已通过 → `{}` (终态)
- `04` 已退回 → `{00}` (反向边,允许打回重写)

**特殊业务规则**:
- 进入 `03` (已通过) 必须 `qualityGatePassed='Y'`,否则抛 ServiceException(708)
- 进入 `04` (已退回) 必须有 `rejectReason`,否则抛 ServiceException(602)
- `00→01` 自动填 `submittedAt`;`02→03` 自动填 `approvedAt`

### TestCase (F4.2) — 测试用例 [testcase.html L188 modal-testcase-add + PRD §F4.2 L339-348]

> **状态**: 🟢 **PRD-aligned** (字段表 commit `9baac4c` → 代码 commit `534c67e`,mvn test 6/6 绿,proposal [0300 ADR-B Option B](99-跨阶段/proposals/0300-adr-b-testcase-category-dict.md) 落地)

**PRD/原型 → 字段映射**:

| 字段 | 列名 | 原型来源 (testcase.html) | 类型 | 必填 | 备注 |
|---|---|---|---|:--:|---|
| `testcaseId` | `testcase_id` | (主键) | BIGINT(20) | ✅ | AUTO_INCREMENT |
| `testcaseNo` | `testcase_no` | (业务编号) | varchar(32) | ✅ | `TC-YYYY-NNNN` (ADR-0006) |
| `projectId` | `project_id` | (隐式 FK) | BIGINT(20) | ✅ | FK→tb_project.id |
| `requirementId` | `requirement_id` | (可关联需求) | BIGINT(20) | ❌ | FK→tb_requirement,可空 |
| `title` | `title` | `用例标题 *` (`nca-title`) | varchar(200) | ✅ | — |
| `description` | `description` | (用例描述) | TEXT | ❌ | — |
| `category` | `category` | `用例类型` select (`nca-type` 4 值) + PRD §F4.2 "农业专项" | varchar(20) | ✅ | 字典 `biz_testcase_category` 8 值 (ADR-B Option B,见 §决策记录 D1) |
| `priority` | `priority` | `优先级` select (`nca-pri` P0-P2) | varchar(2) | ✅ | 字典 `biz_testcase_priority`:`00` P0 / `01` P1 / `02` P2 |
| `status` | `status` | (状态机) | varchar(2) | ✅ | 字典 `biz_testcase_status`,5 态含反向边 03/04→01 (重测) |
| `preconditions` | `preconditions` | `前置条件` (`nca-pre`) | TEXT | ❌ | — |
| `steps` | `steps` | `测试步骤` (`nca-steps`) | TEXT | ✅ | — |
| `expectedResult` | `expected_result` | `预期结果` (`nca-expect`) | TEXT | ✅ | — |
| `actualResult` | `actual_result` | (最近一次执行结果) | TEXT | ❌ | — |
| `isAutomated` | `is_automated` | (是否自动化) | CHAR(1) | ✅ | Y/N,默认 N;Y 必填 automationScriptPath (706) |
| `automationScriptPath` | `automation_script_path` | (自动化脚本路径) | varchar(500) | 条件必填 | `isAutomated=Y` 必填 |
| `executionCount` | `execution_count` | (累计执行次数) | INT | ❌ | 默认 0;`/execute` 自增 |
| `lastExecutedAt` | `last_executed_at` | (最近执行时间) | DATETIME | ❌ | `/execute` 自动填 |
| `tags` | `tags` | (CSV 标签,例 e2e,smoke,regression) | varchar(200) | ❌ | 用于承载非分类维度 (D2) |
| `delFlag` | `del_flag` | (软删除) | CHAR(1) | (auto) | — |

**字段决策记录**:
- **D1 = ADR-B (proposal 0300 Option B): category 字典 8 值字符串编码** — 融合原型 4 值 + SQL 历史 3 值,共 8 个 dict_value:
  | dict_value | dict_label | 来源 |
  |---|---|---|
  | `functional` | 功能 | 原型 ∩ SQL (默认值) |
  | `boundary` | 边界 | 原型独有 |
  | `exception` | 异常 | 原型独有 |
  | `agri` | 农业专项 | 原型 + PRD §F4.2 |
  | `api` | 接口 | SQL 独有 |
  | `performance` | 性能 | 原型 ∩ SQL |
  | `security` | 安全 | SQL 独有 |
  | `compatibility` | 兼容性 | SQL 独有 |
  
  舍弃 SQL 原值 `06 E2E` / `07 烟雾` — 这两个是测试**层级**而非分类,转入 `tags` 字段承载 (D2)。
  Column 类型 `VARCHAR(2)` → `VARCHAR(20)` 配合字符串字典值,与 `biz_project_business_line` / `biz_release_strategy` 等保持风格一致。
- **D2: `tags` 承载非分类维度** — E2E / smoke / regression / nightly / agriIoT 等测试层级或细分场景应进 `tags` 字段(CSV),不进 `category`。这样 category 保持"业务/质量维度",tags 保持"运行特性"。
- **D3: priority 字典保持两位数** — `00`/`01`/`02`,与 SQL 现状一致,不动。
- **D4: `is_automated='Y'` 时 `automationScriptPath` 条件必填** — Service 抛 ServiceException(706)。

**状态机** (5 态含反向边 03/04→01):
- `00` 草稿 → `{01}`
- `01` 待执行 → `{00, 02}` (退回草稿 / 进入执行)
- `02` 执行中 → `{01, 03, 04}` (撤回 / 通过 / 失败)
- `03` 已通过 → `{01}` (反向边:重测)
- `04` 已失败 → `{01}` (反向边:重测)

非法转换抛 ServiceException(601)。**`/execute` 专属端点**:status 必须 `02 执行中`,接受 `03|04` 作为新状态,自动 `executionCount++` 和 `lastExecutedAt=now()`,违反抛 601/604。

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
| `requirement` | **F2.1** | `00→{01,03}` `01→{00,02,03}` `02,03` 终态 | 01→00 打回;**ADR-A**: 4 态实用版裁掉 PRD 6 态 (草稿/评审/确认/验收),依据原型 + 项目"原型优先"惯例,§2 D1 详 |
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

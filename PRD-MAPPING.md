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
| F1.2 | 立项 | `project` | `projects.html` | `tb_project` | `PROJ-YYYY-NNNN` | 🟢 已对齐 |
| F1.3 | 立项 | `competitive` | `competitive.html` | `tb_competitive` | `COMP-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F2.1 | 设计 | `requirement` | `requirements.html` | `tb_requirement` | `REQ-YYYY-NNNN` | 🟢 已对齐 |
| F2.2 | 设计 | `prd` | `prd.html` | `tb_prd` | `PRD-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F2.3 | 设计 | `ued` | `ued.html` | `tb_ued` | `UED-YYYY-NNNN` | 🟡 空壳 |
| F2.4 | 设计 | (评审 → `requirement`) | (并入需求) | (字段) | n/a | 🟢 |
| F3.1 | 研发 | `arch` | `archdesign.html` | `tb_arch` | `ARCH-YYYY-NNNN` | 🟡 空壳 |
| F3.2 | 研发 | `dbdesign` | `dbdesign.html` | `tb_dbdesign` | `DB-YYYY-NNNN` | 🟡 空壳 |
| F3.3 | 研发 | `apidesign` | `apidesign.html` | `tb_apidesign` | `APID-YYYY-NNNN` | 🟡 空壳 |
| F3.4 | 研发 | `sprint`+`task` | `kanban.html` | `tb_sprint`,`tb_task` | `SP-`,`TASK-` | 🟢 已对齐 |
| F3.5 | 研发 | `ai-agent`,`openspec` | `aiagents.html`,`aispec.html` | `tb_ai_agent`,`tb_openspec` | `AGT-`,`SPEC-` | 🟡 空壳 |
| F3.6 | 研发 | (联调 → `task`) | (并入任务) | (字段) | n/a | 🟢 |
| F4.1 | 质量 | `testplan` | `testplan.html` | `tb_testplan` | `TP-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F4.2 | 质量 | `testcase` | `testcase.html` | `tb_testcase` | `TC-YYYY-NNNN` | 🟢 已对齐 |
| F4.3 | 质量 | `testdata` | `testdata.html` | `tb_testdata` | `TD-YYYY-NNNN` | 🟡 空壳 |
| F4.4 | 质量 | `submission` | `submit.html` | `tb_submission` | `SUB-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F4.5 | 质量 | `autotest` | `autotest.html` | `tb_autotest` | `AT-YYYY-NNNN` | 🟡 空壳 |
| F4.6 | 质量 | `defect` | `defects.html` | `tb_defect` | `DEF-YYYY-NNNN` | 🟢 已对齐 |
| F4.7 | 质量 | `testreport` | `testreport.html` | `tb_testreport` | `TR-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F5.1 | 文档 | `manual-product` | `productmanual.html` | `tb_manual_product` | `PM-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F5.2 | 文档 | `manual-impl` | `implmanual.html` | `tb_manual_impl` | `IM-YYYY-NNNN` | 🟡 空壳 |
| F5.3 | 文档 | `manual-ops` | `opsmanual.html` | `tb_manual_ops` | `OM-YYYY-NNNN` | 🟡 空壳 |
| F5.4 | 文档 | `apidoc` | `apidoc.html` | `tb_apidoc` | `API-YYYY-NNNN` | 🟢 **PRD-aligned** |
| F5.5 | 文档 | `document` | (并入文档中心) | `tb_document` | `DOC-YYYY-NNNN` | 🟢 已对齐 |
| F6 | 效能 | `analytics`,`dashboard` | `analytics.html`,`dashboard.html` | `tb_analytics_*` | n/a (汇总表) | 🟡 空壳 |
| 扩展 | DevOps | `release` | `release.html` | `tb_release` | `REL-YYYY-NNNN` | 🟢 **PRD-aligned** |
| 扩展 | DevOps | `pipeline` | `pipeline.html` | `tb_pipeline` | `PIPE-YYYY-NNNN` | 🟡 空壳 |
| 扩展 | DevOps | `feature-flag` | `featureflag.html` | `tb_feature_flag` | `FF-YYYY-NNNN` | 🟡 空壳 |
| 扩展 | DevOps | `dora` | `devops.html` | `tb_dora_metric` | n/a (指标快照) | 🟡 空壳 |

**状态图例**:
- 🟢 已对齐 (PRD/原型 ↔ Domain/Mapper/Service/SQL/E2E 完整 + 测试绿)
- 🟡 空壳 (有 Maven 模块 + pom.xml,但缺 Domain/Service/SQL,**Phase B 拉起的占位**)
- 🔴 空 (连 Maven 模块都没建)

**统计**:
- 🟢 已对齐 = **16 个** (含 inception + prd + competitive,P0 闭环)
- 🟡 空壳 = **14 个**
- 🔴 空 = **0 个**

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

---

## 3. 状态机汇总 (PRD §3.2 + 原型)

| 模块 | PRD § | 状态机 | 反向边 / 特殊 |
|---|:--:|---|---|
| `inception` | **F1.1** | `00→01→{02,04}` `02→{03,04}` `04→00` (反向) | 03 终态可"转项目" |
| `competitive` | **F1.3** | `00→01→02` (3 态) | 02 终态可归档 |
| `project` | F1.2 | `0→{1,4}` `1→{2,3,4}` `2→{1}` `3,4` 终态 | 暂停可恢复 |
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
| F5.2 | `impl-manual-flow` | manual-impl | `POST /business/manual-impl/ai/generate` | 🔴 未实现 |
| F5.3 | `ops-manual-flow` | manual-ops | `POST /business/manual-ops/ai/generate` | 🔴 未实现 |
| F5.4 | `api-doc-flow` | apidoc | `POST /business/apidoc/ai/extract` | 🟡 字段已留位 (`autoExtracted`) |

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

### P2 (Phase 3)
10. `manual-impl` (F5.2) — 实施手册
11. `manual-ops` (F5.3) — 运维手册
12. `analytics` (F6) — 效能分析
13. `dashboard` (UI §4.2) — 工作台聚合接口

### P3 (扩展 / Phase 4)
14. `ai-agent` — AI Agent 编排
15. `openspec` — AI OpenSpec
16. `pipeline` / `feature-flag` / `dora` — DevOps 扩展三件套

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
       ├── (P0 — 已对齐 13)
       │    project, requirement, sprint, task, defect, testcase,
       │    document, submission, release, testplan, testreport,
       │    apidoc, manual-product
       └── (空壳 16+1,待 PRD-align)
            inception(🔴), competitive, prd, ued, arch, dbdesign,
            apidesign, testdata, autotest, manual-impl, manual-ops,
            analytics, dashboard, ai-agent, openspec, pipeline,
            feature-flag, dora
```

所有业务模块 → `plm-project` (FK 检查) + `plm-common` (BaseEntity) + `plm-system` (sys_user 关联)。

---

*文档结束 | 本文件版本变更通过 git history 追踪 | 任何分歧以本文件为准*

# PRD-MAPPING — AgriPLM·AI ↔ PLM 实现单一事实来源（SSoT）

> 本文件是 [CLAUDE.md "PRD/原型驱动开发"](CLAUDE.md) + [.claude/rules.md §M](.claude/rules.md) 强制要求的 SSoT。
> 所有业务字段、状态机、错误码、URL、菜单文案必须能追溯到 PRD § + 原型 HTML 表单元素。

---

## 1. 模块进度速览

| # | 模块 | PRD § | 原型 HTML | 后端模块 | 状态 |
|---|---|---|---|---|---|
| 1 | 项目 Project | F1.2 | projects.html | plm-project | 🟢 PRD-aligned |
| 2 | 需求 Requirement | F2.1 | requirements.html | plm-requirement | 🟢 PRD-aligned |
| 3 | 迭代 Sprint | F3.4 | kanban.html (含 sprint) | plm-sprint | 🟢 PRD-aligned |
| 4 | 任务 Task | F3.4 | kanban.html | plm-task | 🟢 PRD-aligned |
| 5 | 缺陷 Defect | F4.6 | defects.html | plm-defect | 🟢 PRD-aligned |
| 6 | 测试用例 TestCase | F4.2 | testcase.html | plm-testcase | 🟢 PRD-aligned |
| 7 | 文档 Document | F2.2/F3.1/F3.2 | prd.html/archdesign.html/dbdesign.html | plm-document | 🟢 PRD-aligned |
| 8 | 提测 Submission | F4.4 | submit.html | plm-submission | 🟢 PRD-aligned |
| 9 | 发布 Release | F4.7+ | release.html | plm-release | 🟢 PRD-aligned |
| 10 | 测试方案 TestPlan | F4.1 | testplan.html | plm-testplan | 🟢 PRD-aligned |
| 11 | 测试报告 TestReport | F4.7 | testreport.html | plm-testreport | 🟢 PRD-aligned |
| 12 | API 文档 ApiDoc | F5.4 | apidoc.html | plm-apidoc | 🟢 PRD-aligned |
| 13 | 产品手册 ManualProduct | F5.1 | productmanual.html | plm-manual-product | 🟢 PRD-aligned |
| 14 | 立项 Inception | F1.1 | inception.html | plm-inception | 🔴 缺模块 |
| 15 | UED | F2.3 | ued.html | plm-ued | 🟡 空壳 |
| 16 | 竞品 Competitive | F1.3 | competitive.html | plm-competitive | 🟡 空壳 |
| 17 | PRD 文档 | F2.2 | prd.html | plm-prd | 🟡 空壳 |
| 18 | 架构设计 Arch | F3.1 | archdesign.html | plm-arch | 🟡 空壳 |
| 19 | 数据库设计 DbDesign | F3.2 | dbdesign.html | plm-dbdesign | 🟡 空壳 |
| 20 | 接口设计 ApiDesign | F3.3 | apidesign.html | plm-apidesign | 🟡 空壳 |
| 21 | 测试数据 TestData | F4.3 | testdata.html | plm-testdata | 🟡 空壳 |
| 22 | 自动化测试 AutoTest | F4.5 | autotest.html | plm-autotest | 🟡 空壳 |
| 23 | 实施手册 ManualImpl | F5.2 | implmanual.html | plm-manual-impl | 🟡 空壳 |
| 24 | 运维手册 ManualOps | F5.3 | opsmanual.html | plm-manual-ops | 🟡 空壳 |
| 25 | 效能分析 Analytics | - | analytics.html | plm-analytics | 🟡 空壳 |
| 26 | 工作台 Dashboard | - | dashboard.html | plm-dashboard | 🟡 空壳 |
| 27 | AI Agent | - | aiagents.html | plm-ai-agent | 🟡 空壳 |
| 28 | OpenSpec | - | aispec.html | plm-openspec | 🟡 空壳 |
| 29 | Pipeline | - | pipeline.html | plm-pipeline | 🟡 空壳 |
| 30 | Feature Flag | - | featureflag.html | plm-feature-flag | 🟡 空壳 |
| 31 | DORA | - | devops.html | plm-dora | 🟡 空壳 |
| **32** | **MCP Server** | **§2.5/§3.4/§4.1** | **settings.html#MCP集成 (Tab3)** | **plm-mcp** | **🆕 v0.x（Proposal 0007）** |
| **33** | **集成对接 Integration** | **§3.1/§3.5 Phase2** | **settings.html#MCP集成 (Tab3)** | **plm-integration** | **🆕 v0.x（Proposal 0007）** |

---

## 2. 字段对照表（Domain ↔ 原型表单元素 ↔ DB 列）

> 每个 PRD-aligned 模块一节。当前已填 §1-13（**全部 13 个 PRD-aligned 模块**）+ §32-33（MCP/Integration）。16 个空壳模块（competitive/prd/ued/arch/dbdesign/apidesign/testdata 等）的对照表暂参见 [02-设计/<模块>-数据库设计.md](02-设计/) 和 [02-设计/<模块>-API设计.md](02-设计/)。

### §1. Project（plm-project）

**领域**: 项目主数据。**PRD 出处**: F1.2 / 原型 [projects.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/projects.html)

#### 表 `tb_project`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| projectNo | project_no | VARCHAR | ADR-0001 | 编号 PRJ-YYYY-NNNN，自动生成 |
| projectName | project_name | VARCHAR | 原型 projects.html 表单 "项目名称" | 必填 |
| projectType | project_type | VARCHAR | 字典 `biz_project_type` | 类型 |
| status | status | VARCHAR | 字典 `biz_project_status` | 0=未启动 1=进行中 2=暂停 3=已完成 4=已取消 |
| managerUserId | manager_user_id | BIGINT | F1.2 负责人 | FK→sys_user.user_id |
| startDate | start_date | DATE | 原型 "开始日期" | |
| endDate | end_date | DATE | 原型 "结束日期" | |
| budget | budget | DECIMAL | 原型 "预算" | |
| description | description | VARCHAR | - | 详细描述 |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §2. Requirement（plm-requirement）

**领域**: 需求条目。**PRD 出处**: F2.1 / 原型 [requirements.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/requirements.html)

#### 表 `tb_requirement`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| requirementId | id | BIGINT | - | 主键 |
| requirementNo | requirement_no | VARCHAR | ADR-0002 | 编号 REQ-YYYY-NNNN |
| projectId | project_id | BIGINT | F2.1 所属项目 | FK→tb_project.id（必填） |
| title | title | VARCHAR | 原型 requirements.html "需求标题" | 必填 |
| description | description | TEXT | 原型 "详细描述" | Markdown 兼容 |
| source | source | VARCHAR | 字典 `biz_req_source` | 来源 |
| priority | priority | VARCHAR | 字典 `biz_req_priority` | |
| status | status | VARCHAR | 字典 `biz_req_status` | 00=待评审 01=开发中 02=已完成 03=已取消 |
| assigneeUserId | assignee_user_id | BIGINT | F2.1 指派人 | FK→sys_user.user_id（可空） |
| reviewNote | review_note | VARCHAR | 状态推进时简要纪要 | |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §3. Sprint（plm-sprint）

**领域**: 迭代/Sprint。**PRD 出处**: F3.4 / 原型 [kanban.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html) 迭代部分

#### 表 `tb_sprint`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| sprintId | id | BIGINT | - | 主键 |
| sprintNo | sprint_no | VARCHAR | ADR-0004 | 编号 SPR-YYYY-NNNN |
| projectId | project_id | BIGINT | F3.4 | FK→tb_project.id（必填） |
| name | name | VARCHAR | 原型 sprint modal "迭代名称" | 必填，如 "Sprint 26W21" |
| goal | goal | VARCHAR | 原型 "目标" | 一句话目标 |
| status | status | VARCHAR | 字典 `biz_sprint_status` | 00=计划中 01=进行中 02=已完成 03=已取消 |
| plannedStartDate | planned_start_date | DATE | 原型 "开始日期" | 必填 |
| plannedEndDate | planned_end_date | DATE | - | 由 plannedStartDate + durationDays 推算 |
| actualStartDate | actual_start_date | DATE | - | 状态 00→01 时自动填（不由用户输入） |
| actualEndDate | actual_end_date | DATE | - | 状态 01→02 时自动填 |
| durationDays | duration_days | INT | 原型 "工期(天)" | 默认 14 |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

**业务硬规则 703**: 同一 project_id 下 status='01' (进行中) 的迭代必须唯一。

### §4. Task（plm-task）

**领域**: 任务条目。**PRD 出处**: F3.4 / 原型 [kanban.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/kanban.html) 任务部分

#### 表 `tb_task`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| taskId | id | BIGINT | - | 主键 |
| taskNo | task_no | VARCHAR | ADR-0003 | 编号 TASK-YYYY-NNNN |
| projectId | project_id | BIGINT | F3.4 | FK→tb_project.id（必填） |
| requirementId | requirement_id | BIGINT | F3.4 | FK→tb_requirement.id（可空） |
| sprintId | sprint_id | BIGINT | F3.4 | FK→tb_sprint.id（可空） |
| title | title | VARCHAR | 原型 task modal "任务标题" | 必填 |
| description | description | TEXT | 原型 "详细描述" | |
| status | status | VARCHAR | 字典 `biz_task_status` | 00=待开发 01=开发中 02=代码评审 03=测试中 04=已完成 05=已取消 |
| priority | priority | VARCHAR | 字典 `biz_task_priority` | |
| assigneeUserId | assignee_user_id | BIGINT | 原型 "负责人" | FK→sys_user.user_id |
| estimatedHours | estimated_hours | DECIMAL | 原型 "预估工时" | |
| actualHours | actual_hours | DECIMAL | 原型 "实际工时" | 进入"已完成"必填 (601) |
| mrUrl | mr_url | VARCHAR | 原型 "关联MR" | 格式 http(s)://（604 校验） |
| mrBranch | mr_branch | VARCHAR | - | 分支名 |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §5. Defect（plm-defect）

**领域**: 缺陷条目。**PRD 出处**: F4.6 / 原型 [defects.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/defects.html)

#### 表 `tb_defect`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| defectId | id | BIGINT | - | 主键 |
| defectNo | defect_no | VARCHAR | ADR-0005 | 编号 DEFECT-YYYY-NNNN |
| projectId | project_id | BIGINT | F4.6 | FK→tb_project.id（必填） |
| sprintId | sprint_id | BIGINT | F4.6 | FK→tb_sprint.id（可空） |
| taskId | task_id | BIGINT | F4.6 | FK→tb_task.id（可空） |
| title | title | VARCHAR | 原型 defects.html "标题" | 必填 |
| description | description | TEXT | 原型 "详细描述" | |
| severity | severity | VARCHAR | 字典 `biz_defect_severity` | 严重级别 |
| category | category | VARCHAR | 字典 `biz_defect_category` | 分类 |
| status | status | VARCHAR | 字典 `biz_defect_status` | 00=新建 01=已确认 02=处理中 03=已解决 04=已关闭 |
| assigneeUserId | assignee_user_id | BIGINT | 原型 "指派人" | FK→sys_user.user_id |
| reporterUserId | reporter_user_id | BIGINT | 原型 "报告人" | FK→sys_user.user_id (默认当前用户) |
| reproduceSteps | reproduce_steps | TEXT | 原型 "重现步骤" | |
| expectedResult | expected_result | TEXT | 原型 "期望结果" | |
| actualResult | actual_result | TEXT | 原型 "实际结果" | |
| resolution | resolution | VARCHAR | - | 进入 03(已解决) 必填 → 705 |
| tags | tags | VARCHAR | - | CSV |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §6. TestCase（plm-testcase）

**领域**: 测试用例。**PRD 出处**: F4.2 / 原型 [testcase.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/testcase.html)

#### 表 `tb_testcase`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| testcaseId | id | BIGINT | - | 主键 |
| testcaseNo | testcase_no | VARCHAR | ADR-0006 | 编号 TC-YYYY-NNNN |
| projectId | project_id | BIGINT | F4.2 | FK→tb_project.id（必填） |
| requirementId | requirement_id | BIGINT | F4.2 | FK→tb_requirement.id（可空，关联需求） |
| title | title | VARCHAR | 原型 testcase.html "标题" | 必填 |
| description | description | TEXT | - | |
| category | category | VARCHAR | 字典 `biz_testcase_category` | 分类 |
| priority | priority | VARCHAR | 字典 `biz_testcase_priority` | |
| status | status | VARCHAR | 字典 `biz_testcase_status` | 00=草稿 01=待执行 02=执行中 03=已通过 04=已失败 |
| preconditions | preconditions | TEXT | 原型 "前置条件" | |
| steps | steps | TEXT | 原型 "测试步骤" | 必填 |
| expectedResult | expected_result | TEXT | 原型 "期望结果" | 必填 |
| actualResult | actual_result | TEXT | 原型 "实际结果" | execute 端点回填 |
| isAutomated | is_automated | CHAR(1) | 原型 "自动化" | Y/N |
| automationScriptPath | automation_script_path | VARCHAR | - | is_automated='Y' 时必填 → 706 |
| executionCount | execution_count | INT | - | /execute 自增 |
| lastExecutedAt | last_executed_at | DATETIME | - | /execute 自动填 |
| tags | tags | VARCHAR | - | |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §7. Document（plm-document）

**领域**: 单表合并 5 种文档 (PRD/Arch/DbDesign/ApiDesign/Proposal/UED 等) — 用 doc_type 字段区分。**PRD 出处**: F2.2/F3.1/F3.2 / 原型 [prd.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/prd.html) / [archdesign.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/archdesign.html) / [dbdesign.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/dbdesign.html)

#### 表 `tb_document`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| documentId | id | BIGINT | - | 主键 |
| documentNo | document_no | VARCHAR | ADR-0007 | 编号 DOC-<TYPE>-YYYY-NNNN（按 type 分别累加） |
| projectId | project_id | BIGINT | - | FK→tb_project.id（必填） |
| relatedEntityType | related_entity_type | VARCHAR | - | 关联实体类型 (e.g. project / requirement / sprint) |
| relatedEntityId | related_entity_id | BIGINT | - | 关联实体主键 (与 relatedEntityType 联用) |
| docType | doc_type | VARCHAR | 字典 `biz_doc_type` | prd/arch/db_design/api_design/proposal/ued/test_plan/test_report/api_doc/manual_product/manual_impl/manual_ops (12 种, 校验 604) |
| title | title | VARCHAR | 原型各 doc 页 "标题" | 必填 |
| content | content | TEXT | 原型 "正文" | Markdown |
| version | version | VARCHAR | 原型 "版本" | e.g. v1.0 |
| status | status | VARCHAR | 字典 `biz_doc_status` | 00=草稿 01=待评审 02=已发布 03=已归档 |
| authorUserId | author_user_id | BIGINT | 原型 "作者" | FK→sys_user.user_id |
| reviewerUserId | reviewer_user_id | BIGINT | 原型 "审核人" | 进入 02(已发布) 必填 → 707 |
| tags | tags | VARCHAR | 原型 "标签" | |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §8. Submission（plm-submission）

**领域**: 提测管理 — 含 AI 质量门禁。**PRD 出处**: F4.4 / 原型 [submit.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/submit.html)

#### 表 `tb_submission`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| submissionId | id | BIGINT | - | 主键 |
| submissionNo | submission_no | VARCHAR | ADR | 编号 SUB-YYYY-NNNN |
| projectId | project_id | BIGINT | F4.4 | FK→tb_project.id（必填） |
| sprintId | sprint_id | BIGINT | F4.4 | FK→tb_sprint.id（可空） |
| title | title | VARCHAR | 原型 "提测标题" | 必填 |
| scope | scope | TEXT | 原型 "范围" | 本次提测范围 |
| environment | environment | VARCHAR | 原型 "环境" | dev/sit/uat/prod |
| expectedTestDays | expected_test_days | INT | 原型 "期望测试天数" | 1-60 |
| riskNotes | risk_notes | TEXT | 原型 "风险备注" | |
| unitTestCoverage | unit_test_coverage | DECIMAL(5,2) | **F4.4 AI 门禁** | 单测覆盖率 %，≥60 为通过 |
| codeScanPassed | code_scan_passed | CHAR(1) | F4.4 AI 门禁 | Y/N |
| prdCompleted | prd_completed | CHAR(1) | F4.4 AI 门禁 | Y/N |
| apiDocUpdated | api_doc_updated | CHAR(1) | F4.4 AI 门禁 | Y/N |
| qualityGatePassed | quality_gate_passed | CHAR(1) | 派生字段 | 4 项全 Y → Y;否则 N (708 错误码) |
| status | status | VARCHAR | 字典 `biz_submission_status` | 00=草稿 01=已提交 02=质量门禁中 03=已通过 04=已退回 |
| rejectReason | reject_reason | VARCHAR | - | 进入 04 必填 → 602 |
| submitterUserId | submitter_user_id | BIGINT | - | FK→sys_user (默认当前用户) |
| reviewerUserId | reviewer_user_id | BIGINT | - | FK→sys_user |
| submittedAt | submitted_at | DATETIME | - | 00→01 时自动填 |
| approvedAt | approved_at | DATETIME | - | 02→03 时自动填 |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §9. Release（plm-release）

**领域**: 发布管理 — 蓝绿 / 金丝雀 / 滚动 三策略 + DORA 4 指标 + AI 评审。**PRD 出处**: F4.7+ / 原型 [release.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/release.html)

#### 表 `tb_release`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| releaseId | id | BIGINT | - | 主键 |
| releaseNo | release_no | VARCHAR | ADR | 编号 REL-YYYY-NNNN |
| version | version | VARCHAR | 原型 "版本号" | 如 v1.0.0,必填 |
| projectId | project_id | BIGINT | - | FK→tb_project.id（必填） |
| sprintId | sprint_id | BIGINT | - | FK→tb_sprint.id（可空） |
| strategy | strategy | VARCHAR | 字典 `biz_release_strategy` | blue_green / canary / rolling |
| environment | environment | VARCHAR | 原型 "环境" | dev/sit/uat/prod |
| releaseNotes | release_notes | TEXT | 原型 "发布说明" | 变更内容 |
| plannedAt | planned_at | DATETIME | 原型 "计划时间" | |
| releasedAt | released_at | DATETIME | - | 01→02 时自动填 |
| rollbackAt | rollback_at | DATETIME | - | 进入 03 时自动填 |
| rollbackReason | rollback_reason | VARCHAR | - | 进入 03 必填 |
| status | status | VARCHAR | 字典 `biz_release_status` | 00=计划中 01=发布中 02=已发布 03=已回滚 04=已废弃 |
| aiReviewScore | ai_review_score | DECIMAL(5,2) | 原型 "AI 评分" | 0-100 |
| aiReviewNotes | ai_review_notes | TEXT | 原型 "AI 评审说明" | AI 自动生成摘要 |
| deploymentFrequency | deployment_frequency | DECIMAL(10,2) | **DORA 1** | 部署频率 |
| leadTimeHours | lead_time_hours | DECIMAL(10,2) | **DORA 2** | 前置时间 (小时) |
| mttrMinutes | mttr_minutes | DECIMAL(10,2) | **DORA 3** | 平均恢复时间 (分钟) |
| changeFailureRate | change_failure_rate | DECIMAL(5,2) | **DORA 4** | 变更失败率 % |
| releasedByUserId | released_by_user_id | BIGINT | - | FK→sys_user (实际发布人) |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §10. TestPlan（plm-testplan）

**领域**: 测试方案 — AI 生成策略 / 范围 / 资源 / 风险。**PRD 出处**: F4.1 / 原型 [testplan.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/testplan.html)

#### 表 `tb_testplan`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| testplanId | id | BIGINT | - | 主键 |
| testplanNo | testplan_no | VARCHAR | ADR | 编号 TP-YYYY-NNNN |
| projectId | project_id | BIGINT | F4.1 | FK→tb_project.id（必填） |
| sprintId | sprint_id | BIGINT | F4.1 | FK→tb_sprint.id（可空） |
| title | title | VARCHAR | 原型 "方案标题" | 必填 |
| testTypes | test_types | VARCHAR | 原型 "测试类型" | CSV: 功能/性能/安全/兼容性 等 (必填) |
| testCycleDays | test_cycle_days | INT | 原型 "测试周期" | 默认 10 |
| scope | scope | TEXT | 原型 "测试范围" | |
| strategy | strategy | TEXT | 原型 "测试策略" | 入口/出口准则 |
| toolsRecommended | tools_recommended | TEXT | 原型 "工具推荐" | AI 建议 |
| resourcesPlan | resources_plan | TEXT | 原型 "资源计划" | 人力/环境/数据 |
| riskAssessment | risk_assessment | TEXT | 原型 "风险评估" | 风险点+缓解 |
| aiGenerated | ai_generated | CHAR(1) | 原型 "AI 生成" | Y/N |
| status | status | VARCHAR | 字典 `biz_testplan_status` | 00=草稿 01=已确认 02=执行中 03=已完成 |
| authorUserId | author_user_id | BIGINT | - | FK→sys_user |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §11. ApiDoc（plm-apidoc）

**领域**: API 文档 — 从代码注释自动提取 + OpenAPI 规范 + 在线调试。**PRD 出处**: F5.4 / 原型 [apidoc.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/apidoc.html)

#### 表 `tb_apidoc`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| apidocId | id | BIGINT | - | 主键 |
| apidocNo | apidoc_no | VARCHAR | ADR | 编号 API-YYYY-NNNN |
| projectId | project_id | BIGINT | F5.4 | FK→tb_project.id（必填） |
| title | title | VARCHAR | 原型 "接口标题" | 必填 |
| httpMethod | http_method | VARCHAR | 原型 "HTTP方法" | GET / POST / PUT / DELETE / PATCH / HEAD / OPTIONS (校验集合) |
| path | path | VARCHAR | 原型 "接口路径" | 如 /business/xxx |
| description | description | TEXT | 原型 "描述" | Markdown |
| requestSchema | request_schema | TEXT | 原型 "请求 Schema" | JSON Schema |
| responseSchema | response_schema | TEXT | 原型 "响应 Schema" | JSON Schema |
| openapiSpec | openapi_spec | TEXT | 原型 "OpenAPI 规范" | OpenAPI 3.0 片段 |
| sourceClass | source_class | VARCHAR | - | 源 Controller 类全限定名 |
| sourceMethod | source_method | VARCHAR | - | 源方法名 |
| version | version | VARCHAR | 原型 "版本" | 如 v1,必填 |
| status | status | VARCHAR | 字典 `biz_apidoc_status` | 00=草稿 01=已发布 02=已废弃 |
| lastSyncedAt | last_synced_at | DATETIME | - | autoExtracted='Y' 时自动填 |
| autoExtracted | auto_extracted | CHAR(1) | - | Y/N |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

**唯一键**: (http_method, path, version) → 重复抛 701。

### §12. ManualProduct（plm-manual-product）

**领域**: 产品手册 — AI 一键生成 + 截图自动描述 + 多格式导出。**PRD 出处**: F5.1 / 原型 [productmanual.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/productmanual.html)

#### 表 `tb_manual_product`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| manualproductId | id | BIGINT | - | 主键 |
| manualproductNo | manualproduct_no | VARCHAR | ADR | 编号 MP-YYYY-NNNN |
| projectId | project_id | BIGINT | F5.1 | FK→tb_project.id（必填） |
| title | title | VARCHAR | 原型 "手册标题" | 必填 |
| productVersion | product_version | VARCHAR | 原型 "产品版本" | 如 v1.0.0,必填 |
| includeModules | include_modules | VARCHAR | 原型 "包含模块" | CSV 模块列表 |
| content | content | LONGTEXT | 原型 "正文" | Markdown,AI 一键生成 |
| screenshotsUrls | screenshots_urls | TEXT | 原型 "截图 URL 列表" | CSV/换行分隔 |
| screenshotsCount | screenshots_count | INT | 原型 "截图数" | |
| outputFormats | output_formats | VARCHAR | 原型 "导出格式" | word / pdf / html / h5 (CSV) |
| aiGenerated | ai_generated | CHAR(1) | 原型 "AI 生成" | Y/N |
| generatedAt | generated_at | DATETIME | - | 进入 02(已生成) 时自动填 |
| status | status | VARCHAR | 字典 `biz_manualproduct_status` | 00=草稿 01=生成中 02=已生成 03=已发布 |
| authorUserId | author_user_id | BIGINT | - | FK→sys_user |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §13. TestReport（plm-testreport）

**领域**: 测试报告 — AI 自动生成 + 上线风险评级 (绿/黄/红)。**PRD 出处**: F4.7 / 原型 [testreport.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/testreport.html)

#### 表 `tb_testreport`

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| testreportId | id | BIGINT | - | 主键 |
| testreportNo | testreport_no | VARCHAR | ADR | 编号 TR-YYYY-NNNN |
| projectId | project_id | BIGINT | F4.7 | FK→tb_project.id（必填） |
| sprintId | sprint_id | BIGINT | F4.7 | FK→tb_sprint.id（可空） |
| testplanId | testplan_id | BIGINT | F4.7 | FK→tb_testplan.id（可空） |
| title | title | VARCHAR | 原型 "报告标题" | 必填 |
| totalCases | total_cases | INT | 原型 "总用例数" | |
| passedCases | passed_cases | INT | 原型 "通过用例" | |
| failedCases | failed_cases | INT | 原型 "失败用例" | |
| coverageRate | coverage_rate | DECIMAL(5,2) | 原型 "覆盖率%" | 0-100 |
| defectSummary | defect_summary | TEXT | 原型 "缺陷摘要" | |
| p0Defects | p0_defects | INT | 原型 "P0 缺陷数" | 严重 |
| p1Defects | p1_defects | INT | 原型 "P1 缺陷数" | 重要 |
| p2Defects | p2_defects | INT | 原型 "P2 缺陷数" | 一般 |
| riskLevel | risk_level | VARCHAR | 字典 `biz_testreport_risk` | green / yellow / red (校验集合) |
| riskEvaluation | risk_evaluation | TEXT | 原型 "风险评价" | 详细说明 |
| recommendations | recommendations | TEXT | 原型 "改进建议" | 后续措施 |
| aiGenerated | ai_generated | CHAR(1) | 原型 "AI 生成" | Y/N |
| status | status | VARCHAR | 字典 `biz_testreport_status` | 00=草稿 01=审核中 02=已发布 |
| generatedAt | generated_at | DATETIME | - | AI 生成时自动填 |
| reviewerUserId | reviewer_user_id | BIGINT | - | FK→sys_user |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

### §32. MCP Server（plm-mcp）

**领域**: 把 PLM 自己的业务能力（项目/需求/任务/用例/文档/数据）通过 MCP 协议暴露给外部 LLM Agent（Claude Code、Cursor、Copilot 等）。
**PRD 出处**: §2.5 工具集清单 / §3.4 实现细节 / §4.1 信息架构 / 原型 [settings.html:157-180](prd和原型/AgriPLM-DevOps-原型/agriplm_split/settings.html)

#### 表 `tb_mcp_server` —— MCP Server 注册表

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| serverCode | server_code | VARCHAR(64) | §2.5 工具集分类 | 唯一编码：plm-core / plm-testcase / ... |
| serverName | server_name | VARCHAR(128) | §3.4 工具注册 | 展示名 |
| protocol | protocol | VARCHAR(16) | §3.4 "基于 MCP 协议规范" | `stdio` / `sse` / `http` |
| endpoint | endpoint | VARCHAR(512) | §3.4 | http 模式下的访问 URL |
| authType | auth_type | VARCHAR(16) | §3.4 "OAuth 2.0" | `none` / `token` / `oauth2` |
| oauthClientId | oauth_client_id | VARCHAR(128) | §3.4 "支持企业 SSO 对接" | OAuth 客户端 ID |
| oauthClientSecretEncrypted | oauth_client_secret_enc | VARCHAR(1024) | §3.4 | AES-256-GCM 密文 |
| toolsJson | tools_json | TEXT | §2.5 工具集 JSON Schema | 暴露的工具列表（JSON Schema 数组） |
| status | status | CHAR(1) | 原型 mcpTable 状态列 | 0=启用 1=停用 2=异常 |
| lastHealthAt | last_health_at | DATETIME | 原型 mcpTable "最后健康检查" | 心跳时间戳 |
| description | description | VARCHAR(500) | - | 描述 |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

#### 表 `tb_mcp_tool_audit` —— MCP 工具调用审计

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| serverId | server_id | BIGINT | §3.4 OAuth | FK→tb_mcp_server.id |
| toolName | tool_name | VARCHAR(128) | §2.5 工具命名 | 如 `project.list` / `requirement.create` |
| callerType | caller_type | VARCHAR(16) | §3.4 鉴权 | `user`/`agent`/`system` |
| callerId | caller_id | VARCHAR(128) | §3.4 | username / agent token id |
| paramsJson | params_json | TEXT | - | 调用参数 |
| resultStatus | result_status | CHAR(1) | - | 0=成功 1=失败 2=超时 |
| resultBrief | result_brief | VARCHAR(2000) | - | 截断到 2KB 的响应摘要（详细到日志） |
| latencyMs | latency_ms | INT | - | 耗时（ms） |
| createBy/createTime | (RuoYi 标准 2 字段) | | | 不可逻辑删除（审计） |

### §33. 集成对接 Integration（plm-integration）

**领域**: 管理与 飞书 / GitLab / 钉钉 / Jira / Figma / 禅道 / ZTF 等外部系统的连接器配置 + Webhook 入站事件。
**PRD 出处**: §3.1 产品边界 "通过 MCP/CLI 与 Jira、GitLab、飞书、钉钉、Figma、禅道、ZTF 等系统双向同步" / §3.5 Phase 2 "GitLab/飞书集成" / 原型 [settings.html:138-187](prd和原型/AgriPLM-DevOps-原型/agriplm_split/settings.html) "MCP集成" Tab

#### 表 `tb_integration_connector` —— 集成连接器配置

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| connectorCode | connector_code | VARCHAR(64) | §3.1 | 唯一编码：FEISHU-MAIN / GITLAB-OPS / ... |
| connectorName | connector_name | VARCHAR(128) | - | 展示名 |
| connectorType | connector_type | VARCHAR(32) | §3.1 / §3.5 Phase2 | 字典 `biz_integration_type`：feishu/gitlab/dingtalk/jira/figma/zentao/ztf |
| endpoint | endpoint | VARCHAR(512) | - | 外部系统 base URL (GitLab self-hosted 用) |
| authType | auth_type | VARCHAR(16) | - | 字典 `biz_integration_auth`：app_secret/access_token/oauth2/pat |
| credentialEncrypted | credential_enc | VARCHAR(2048) | - | AES-256-GCM 加密的 JSON（含 app_id/app_secret/token/refresh_token 等） |
| webhookUrl | webhook_url | VARCHAR(512) | - | 本系统对外暴露的 webhook 入口（计算字段，仅查询返回） |
| webhookSecret | webhook_secret | VARCHAR(256) | - | 验签密钥（明文存数据库 OK，因为只用作 HMAC，不能反推出更敏感数据） |
| configJson | config_json | TEXT | - | 类型特定配置（机器人 chat_id / 项目映射等） |
| status | status | CHAR(1) | 原型表格 状态列 | 字典 `biz_integration_status`：0=启用 1=停用 2=异常 |
| lastSyncAt | last_sync_at | DATETIME | 原型表格 "最后同步" | |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

#### 表 `tb_integration_webhook_event` —— 入站 Webhook 事件流水

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| connectorId | connector_id | BIGINT | - | FK→tb_integration_connector.id |
| eventType | event_type | VARCHAR(128) | 各外部系统的 webhook 类型 | 如 `feishu.im.message.receive_v1` / `gitlab.merge_request` |
| externalEventId | external_event_id | VARCHAR(128) | - | 外部 event id（幂等键） |
| payloadJson | payload_json | LONGTEXT | - | 原始 payload |
| signature | signature | VARCHAR(512) | - | 签名头（验签用） |
| signatureVerified | signature_verified | CHAR(1) | - | 0=验签失败 1=通过 |
| processStatus | process_status | CHAR(1) | - | 字典 `biz_webhook_status`：0=待处理 1=处理中 2=成功 3=失败 4=已忽略 |
| processError | process_error | VARCHAR(2000) | - | 失败原因 |
| retryCount | retry_count | INT | - | 重试次数 |
| createTime / processTime | (RuoYi 标准) | | | 不可删除（审计） |
| sourceIp | source_ip | VARCHAR(64) | - | 调用方 IP（防滥用） |

---

## 3. 状态机汇总

### §1 Project 状态机（tb_project.status）

```
0未启动 → 1进行中 / 4已取消
1进行中 → 2暂停 / 3已完成 / 4已取消
2暂停   → 1进行中 / 4已取消
3已完成 (终态)
4已取消 (终态)
```
非法转换 → `ServiceException(701)`。落地: [ProjectServiceImpl:33](plm-backend/plm-project/src/main/java/cn/com/bosssfot/dv/plm/project/service/impl/ProjectServiceImpl.java)。

### §2 Requirement 状态机（tb_requirement.status, 4×4）

```
00待评审 → 01开发中 / 03已取消
01开发中 → 00待评审(打回) / 02已完成 / 03已取消
02已完成 (终态)
03已取消 (终态)
```
非法转换 → `ServiceException(601)`。落地: [RequirementServiceImpl:44](plm-backend/plm-requirement/src/main/java/cn/com/bosssfot/dv/plm/requirement/service/impl/RequirementServiceImpl.java)。

### §3 Sprint 状态机（tb_sprint.status, 4×4）

```
00计划中 → 01进行中 / 03已取消    [01→ 自动填 actualStartDate]
01进行中 → 02已完成 / 03已取消    [02→ 自动填 actualEndDate]
02已完成 (终态)
03已取消 (终态)
```
非法转换 → `ServiceException(601)`。**业务硬规则 703**: 同一 project 下进行中迭代唯一。落地: [SprintServiceImpl:50](plm-backend/plm-sprint/src/main/java/cn/com/bosssfot/dv/plm/sprint/service/impl/SprintServiceImpl.java)。

### §4 Task 状态机（tb_task.status, 6×6 含反向边）

```
00待开发  → 01开发中 / 05已取消
01开发中  → 00待开发(回退) / 02代码评审 / 05已取消
02代码评审 → 01开发中(打回) / 03测试中 / 05已取消
03测试中  → 02代码评审(打回) / 04已完成 / 05已取消    [04→ 强制要求 actualHours]
04已完成 (终态)
05已取消 (终态)
```
非法转换 → `ServiceException(601)`。落地: [TaskServiceImpl:48](plm-backend/plm-task/src/main/java/cn/com/bosssfot/dv/plm/task/service/impl/TaskServiceImpl.java)。

### §5 Defect 状态机（tb_defect.status, 5×5 含反向边）

```
00新建    → 01已确认
01已确认  → 02处理中 / 04已关闭(重复/无效直接关)
02处理中  → 01已确认(重新分析) / 03已解决
03已解决  → 01已确认(反向打回) / 04已关闭          [进入 03 强制要求 resolution]
04已关闭 (终态)
```
非法转换 → `ServiceException(701)`;resolution 缺失 → `ServiceException(705)`。落地: [DefectServiceImpl:49](plm-backend/plm-defect/src/main/java/cn/com/bosssfot/dv/plm/defect/service/impl/DefectServiceImpl.java)。

### §6 TestCase 状态机（tb_testcase.status, 5×5 含反向边）

```
00草稿   → 01待执行
01待执行 → 00草稿(回退) / 02执行中
02执行中 → 01待执行(撤回) / 03已通过 / 04已失败
03已通过 → 01待执行(重测)
04已失败 → 01待执行(重测)
```
非法转换 → `ServiceException(701)`;is_automated='Y' 缺 automation_script_path → 706。/execute 端点专属:status 02→03|04 + execution_count+1 + last_executed_at。落地: [TestCaseServiceImpl:47](plm-backend/plm-testcase/src/main/java/cn/com/bosssfot/dv/plm/testcase/service/impl/TestCaseServiceImpl.java)。

### §7 Document 状态机（tb_document.status, 4×4 含反向边）

```
00草稿   → 01待评审
01待评审 → 00草稿(反向打回) / 02已发布                [进入 02 强制要求 reviewerUserId]
02已发布 → 01待评审(反向重审) / 03已归档
03已归档 (终态)
```
非法转换 → `ServiceException(701)`;reviewerUserId 缺失 → `ServiceException(707)`;doc_type 不在 12 种枚举内 → 604。落地: [DocumentServiceImpl:37](plm-backend/plm-document/src/main/java/cn/com/bosssfot/dv/plm/document/service/impl/DocumentServiceImpl.java)。

### §8 Submission 状态机（tb_submission.status, 5×5 含反向边）

```
00草稿     → 01已提交                          [自动填 submittedAt]
01已提交   → 02质量门禁中 / 04已退回(快速拒)
02质量门禁中 → 03已通过(qualityGatePassed=Y) / 04已退回   [03 自动填 approvedAt]
03已通过 (终态)
04已退回   → 00草稿(反向打回重做)             [必填 rejectReason]
```
非法转换 → `ServiceException(701)`;rejectReason 缺失 → 602;qualityGatePassed!=Y 进入 03 → 708;单测覆盖率 < 60% 自动 qualityGatePassed=N。落地: [SubmissionServiceImpl:46](plm-backend/plm-submission/src/main/java/cn/com/bosssfot/dv/plm/submission/service/impl/SubmissionServiceImpl.java)。

### §9 Release 状态机（tb_release.status, 5 态）

```
00计划中 → 01发布中 / 04已废弃
01发布中 → 02已发布 / 03已回滚                 [02 自动填 releasedAt]
02已发布 → 03已回滚 / 04已废弃
03已回滚 → 04已废弃                            [进入 03 自动填 rollbackAt + 必填 rollbackReason]
04已废弃 (终态)
```
非法转换 → `ServiceException(701)`;rollbackReason 缺失 → 602;strategy ∉ {blue_green, canary, rolling} → 604。落地: [ReleaseServiceImpl:43](plm-backend/plm-release/src/main/java/cn/com/bosssfot/dv/plm/release/service/impl/ReleaseServiceImpl.java)。

### §10 TestPlan 状态机（tb_testplan.status, 4 态含反向边）

```
00草稿   → 01已确认
01已确认 → 00草稿(回退) / 02执行中
02执行中 → 03已完成
03已完成 (终态)
```
非法转换 → `ServiceException(701)`;testCycleDays 默认 10;aiGenerated 默认 N。落地: [TestPlanServiceImpl:37](plm-backend/plm-testplan/src/main/java/cn/com/bosssfot/dv/plm/testplan/service/impl/TestPlanServiceImpl.java)。

### §11 ApiDoc 状态机（tb_apidoc.status, 3 态）

```
00草稿   → 01已发布
01已发布 → 02已废弃
02已废弃 (终态)
```
非法转换 → `ServiceException(701)`;唯一键 (http_method, path, version) 重复 → 701;httpMethod ∉ {GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS} → 604;autoExtracted='Y' 自动填 lastSyncedAt。落地: [ApiDocServiceImpl:41](plm-backend/plm-apidoc/src/main/java/cn/com/bosssfot/dv/plm/apidoc/service/impl/ApiDocServiceImpl.java)。

### §12 ManualProduct 状态机（tb_manual_product.status, 4 态含反向边）

```
00草稿   → 01生成中
01生成中 → 02已生成                                  [02 自动填 generatedAt]
02已生成 → 00草稿(重新编辑) / 03已发布
03已发布 (终态)
```
非法转换 → `ServiceException(701)`。落地: [ManualProductServiceImpl:38](plm-backend/plm-manual-product/src/main/java/cn/com/bosssfot/dv/plm/manualproduct/service/impl/ManualProductServiceImpl.java)。

### §13 TestReport 状态机（tb_testreport.status, 3 态含反向边）

```
00草稿   → 01审核中
01审核中 → 00草稿(打回) / 02已发布
02已发布 (终态)
```
非法转换 → `ServiceException(701)`;riskLevel ∉ {green, yellow, red} → 604;AI 生成时自动填 generatedAt。落地: [TestReportServiceImpl:39](plm-backend/plm-testreport/src/main/java/cn/com/bosssfot/dv/plm/testreport/service/impl/TestReportServiceImpl.java)。

### §32 MCP Server 状态机（tb_mcp_server.status）

```
启用(0) ⇄ 停用(1)
启用(0) → 异常(2) [心跳失败 N 次]
异常(2) → 启用(0) [手动恢复或心跳恢复]
异常(2) → 停用(1)
```

| 当前 | 允许目标 |
|---|---|
| 0 启用 | 1 停用 / 2 异常 |
| 1 停用 | 0 启用 |
| 2 异常 | 0 启用 / 1 停用 |

非法转换 → `ServiceException(701)`。

### §33 Integration Connector 状态机（tb_integration_connector.status）

同上结构：启用(0) ⇄ 停用(1)；启用(0) → 异常(2)；异常(2) → 启用(0) / 停用(1)。

### §33 Webhook Event 状态机（tb_integration_webhook_event.process_status）

```
待处理(0) → 处理中(1)
处理中(1) → 成功(2) / 失败(3) / 已忽略(4)
失败(3) → 处理中(1) [重试]
```

非法转换 → `ServiceException(701)`。

---

## 4. 错误码登记表

| 代码 | 名称 | HTTP | 出处 | 示例 |
|---|---|---|---|---|
| 601 | 参数为空/非法 | 400 | 通用（已用） | `项目名称不能为空` |
| 604 | 业务规则冲突 | 400 | 通用（已用） | `起始日期不能晚于结束日期` |
| 701 | 非法状态转换 | 400 | 通用（已用） | `状态「停用」不能直接转到「异常」` |
| **801** | **MCP Server 不存在** | 404 | 本提案 (Proposal 0007) | `MCP Server [server_code] 不存在` |
| **802** | **MCP 工具未注册** | 404 | 本提案 | `工具 [project.list] 在 [plm-core] 中未注册` |
| **803** | **MCP OAuth 失败** | 401 | 本提案 | `OAuth 验证失败 / token 已过期` |
| **804** | **MCP 工具调用失败** | 502 | 本提案 | `MCP Server 返回非预期错误` |
| **805** | **集成连接器未配置** | 404 | 本提案 | `Connector [FEISHU-MAIN] 未找到` |
| **806** | **集成 token 过期 / 无效** | 401 | 本提案 | `tenant_access_token 已失效，请重新刷新` |
| **807** | **Webhook 验签失败** | 401 | 本提案 | `飞书 verification_token 不匹配 / GitLab X-Gitlab-Token 失败` |
| **808** | **外部 API 速率限制** | 429 | 本提案 | `飞书 OpenAPI 触发限流，请稍候` |
| **809** | **凭据加密 key 未配置** | 500 | 本提案（启动期 fail-fast） | `MCP_ENCRYPT_KEY 未设置，拒绝启动` |
| **810** | **不支持的 connector_type** | 400 | 本提案 | `connector_type = xxx 暂不支持，仅支持: feishu/gitlab/...` |

---

## 5. URL 路径与菜单

### REST API 路径

| 模块 | 列表 | 详情 | 新增 | 修改 | 删除 |
|---|---|---|---|---|---|
| MCP Server | `GET /business/mcp/server/list` | `GET /business/mcp/server/{id}` | `POST /business/mcp/server` | `PUT /business/mcp/server` | `DELETE /business/mcp/server/{ids}` |
| MCP 工具审计 | `GET /business/mcp/audit/list` | `GET /business/mcp/audit/{id}` | (只读) | (只读) | (只读) |
| MCP 协议端点 | `POST /mcp/tools/list` / `POST /mcp/tools/call` | - | - | - | - |
| 集成连接器 | `GET /business/integration/connector/list` | `GET /business/integration/connector/{id}` | `POST /business/integration/connector` | `PUT /business/integration/connector` | `DELETE /business/integration/connector/{ids}` |
| 集成 - 测试连通性 | `POST /business/integration/connector/{id}/test` | - | - | - | - |
| Webhook 事件 | `GET /business/integration/webhook/list` | `GET /business/integration/webhook/{id}` | (只读) | (只读，仅重试) | (只读) |
| Webhook 入站 - 飞书 | `POST /integration/webhook/feishu/{connectorId}` | - | - | - | - |
| Webhook 入站 - GitLab | `POST /integration/webhook/gitlab/{connectorId}` | - | - | - | - |

**注意**：
- `/business/*` 走 Spring Security JWT + `@PreAuthorize("@ss.hasPermi('business:<entity>:<action>')")`
- `/mcp/*` 走 OAuth/Token，独立鉴权过滤器（PRD §3.4）
- `/integration/webhook/*` 公网入口，走 HMAC 验签，不走 JWT；在 [SecurityConfig](plm-backend/plm-framework/src/main/java/cn/com/bosssfot/dv/plm/framework/config/SecurityConfig.java) 的 `permitAll` 列表中开放

### 菜单 ID 段（避开现有）

- 业务管理 = 2000（已用）
- **MCP 集成 = 2400**（一级目录）
  - MCP Server = 2410
    - 查询 2411 / 新增 2412 / 修改 2413 / 删除 2414 / 导出 2415
  - 调用审计 = 2420
    - 查询 2421 / 导出 2425
- **外部集成 = 2500**（一级目录）
  - 连接器配置 = 2510
    - 查询 2511 / 新增 2512 / 修改 2513 / 删除 2514 / 测试 2516
  - Webhook 事件 = 2520
    - 查询 2521 / 重试 2522 / 导出 2525

### 权限串

- `business:mcp:server:list/add/edit/remove/export/query`
- `business:mcp:audit:list/query/export`
- `business:integration:connector:list/add/edit/remove/test/query`
- `business:integration:webhook:list/retry/query/export`

---

## 6. 字典类型登记

| 字典类型 | 字典名 | 使用列 | 数据项 |
|---|---|---|---|
| `biz_mcp_protocol` | MCP 协议类型 | mcp_server.protocol | stdio / sse / http |
| `biz_mcp_auth` | MCP 鉴权类型 | mcp_server.auth_type | none / token / oauth2 |
| `biz_mcp_status` | MCP 状态 | mcp_server.status | 0=启用 1=停用 2=异常 |
| `biz_audit_result` | 审计结果 | mcp_tool_audit.result_status | 0=成功 1=失败 2=超时 |
| `biz_integration_type` | 集成类型 | integration_connector.connector_type | feishu / gitlab / dingtalk / jira / figma / zentao / ztf |
| `biz_integration_auth` | 集成鉴权 | integration_connector.auth_type | app_secret / access_token / oauth2 / pat |
| `biz_integration_status` | 集成状态 | integration_connector.status | 0=启用 1=停用 2=异常 |
| `biz_webhook_status` | Webhook 处理状态 | integration_webhook_event.process_status | 0=待 1=中 2=成 3=败 4=略 |

---

## 7. v0.x 路线图

- ✅ Project / Requirement / Sprint / Task / Defect / TestCase / Document / Submission / Release / TestPlan / TestReport / ApiDoc / ManualProduct
- 🆕 **MCP / Integration**（本提案 0007）
- 下一批：Inception / UED / Competitive / PRD / Arch / DbDesign / ApiDesign

---

## 8. 9 项 DoD（每个 PRD-aligned 模块必过）

> 见 [.claude/rules.md §M.2](.claude/rules.md)

1. ☐ 在本文件 §2 追加该模块的字段对照表
2. ☐ §3 状态机入文件
3. ☐ §4 错误码入文件
4. ☐ §5 URL/菜单/权限入文件
5. ☐ §6 字典入文件
6. ☐ Domain/Mapper/Service/Controller/XML 5 件套齐
7. ☐ SQL DDL + 回滚脚本 + 菜单 seed
8. ☐ Phase 02 设计 Gate Checklist 实例签字
9. ☐ Phase 03 开发 Gate Checklist 实例签字

---

## 9. 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 初版；初始化 SSoT；按 Proposal 0007 加入 §32 MCP / §33 Integration |
| 2026-05-17 | Wjl + Claude | §2 补全 §1-4 Project/Requirement/Sprint/Task 字段对照表；§3 补全 4 模块状态机；配合 Vue 前端补缺(managerUserId / projectType filter / kanban priority+assignee filter+卡片负责人 / requirement 指派人搜索)与 ServiceImpl 现代化(Map.of + enhanced switch) |
| 2026-05-17 | Wjl + Claude | 第二批: §2 补 §5 Defect / §6 TestCase / §7 Document 字段对照表;§3 补 3 模块状态机;Vue 解锁(Defect 报告人+指派人搜索 / TestCase 需求ID 搜索 / Document 关联类型+ID+作者+审核人 4 项搜索) + 后端 Document Mapper 加 reviewerUserId 过滤;3 个 ServiceImpl 现代化同样模式 |
| 2026-05-18 | Wjl + Claude | 第三批: §2 补 §8 Submission / §9 Release / §10 TestPlan 字段对照表(含 AI 门禁 4 项 / DORA 4 指标);§3 补 3 模块状态机(含 04→00 反向+回滚必填);3 个 Vue 由空壳 stub(11 行)重写为完整 CRUD ~280 行均;3 个 TS types 由 5 字段扩到完整 domain 映射;3 个 ServiceImpl 现代化同样模式 |
| 2026-05-18 | Wjl + Claude | **第四批: 全部 13 个 PRD-aligned 模块字段对照表/状态机完工!** §2 补 §11 ApiDoc / §12 ManualProduct / §13 TestReport(含 OpenAPI Schema / 截图/导出格式 / 风险评级+缺陷统计);§3 补 3 模块状态机(ApiDoc 3 态+唯一键 / ManualProduct 4 态含 02→00 反向 / TestReport 3 态含 01→00 反向);3 个 Vue 由 stub 重写为完整 CRUD ~300+ 行;3 个 TS types 扩到完整 domain 映射;3 个 ServiceImpl 现代化同样模式。**10 个 ServiceImpl 全部完成现代化收尾**。 |

# E2E 自动化测试矩阵

| 字段 | 值 |
|---|---|
| 版本 | **v3.0** |
| 框架 | Playwright 1.60+ (Chromium) |
| 落地日期 | 2026-05-16（v1.0） / 2026-05-17（v2.0 全 13 模块） / 2026-05-27（v3.0 全 31 模块） |
| 当前总用例数 | **241 case (37 spec 文件)** — 业务 219 + 跨切 22 |
| 跨层 SSoT | 完整覆盖表（E2E + 后端单测）见 [测试计划.md §1.1](../测试计划.md)；本文聚焦 E2E 维度的逐 case 矩阵 |
| 强制级别 | **MUST** — Phase 03→04 准入条件之一（[.claude/rules.md §G.4](../../.claude/rules.md)） |
| 关联 skill | [.claude/skills/plm-e2e/SKILL.md](../../.claude/skills/plm-e2e/SKILL.md) |

---

## 1. 套件总览

### 1.1 原 13 模块 — 深度覆盖（状态机 + FK + 编码 完整断言）

| # | 模块 | spec 文件 | case 数 | 深度 |
|---|---|---|---|---|
| 1 | Project | [project.spec.ts](../../plm-frontend/e2e/project.spec.ts) | 5 | UI + 路由 + 列表 + 搜索 |
| 2 | Requirement | [requirement.spec.ts](../../plm-frontend/e2e/requirement.spec.ts) | 9 | 4×4 状态机 + 反向边 + FK + 评审 + UI |
| 3 | Sprint | [sprint.spec.ts](../../plm-frontend/e2e/sprint.spec.ts) | 6 | 4×4 + **703 单一活跃** + actual_dates + stats + UI |
| 4 | Task | [task.spec.ts](../../plm-frontend/e2e/task.spec.ts) | 10 | 6×6 含 02↔01 / 03→02 反向边 + 看板 + 我的任务 + 3 FK |
| 5 | Defect | [defect.spec.ts](../../plm-frontend/e2e/defect.spec.ts) | 8 | 5×5 + 反向边 03→01 + 必填 resolution + 3 FK |
| 6 | TestCase | [testcase.spec.ts](../../plm-frontend/e2e/testcase.spec.ts) | 8 | 5×5 + 反向边 03/04→01 + /execute + 706 |
| 7 | Document | [document.spec.ts](../../plm-frontend/e2e/document.spec.ts) | 8 | 文档类型 + 版本 + 评审流 |
| 8 | Submission | [submission.spec.ts](../../plm-frontend/e2e/submission.spec.ts) | 5 | 5×5 + **708 质量门禁 4 项** + 反向边 04→00 |
| 9 | Release | [release.spec.ts](../../plm-frontend/e2e/release.spec.ts) ✨ v2.0 | 4 | CRUD + UK 唯一 + 编码守门员 + FK |
| 10 | TestPlan | [testplan.spec.ts](../../plm-frontend/e2e/testplan.spec.ts) ✨ v2.0 | 3 | CRUD + 编码守门员 + FK |
| 11 | TestReport | [testreport.spec.ts](../../plm-frontend/e2e/testreport.spec.ts) ✨ v2.0 | 4 | CRUD + risk_level 白名单 + 编码守门员 + FK |
| 12 | ApiDoc | [apidoc.spec.ts](../../plm-frontend/e2e/apidoc.spec.ts) ✨ v2.0 | 4 | CRUD + UK(method+path+ver) + 编码守门员 + FK |
| 13 | ManualProduct | [manual-product.spec.ts](../../plm-frontend/e2e/manual-product.spec.ts) ✨ v2.0 | 3 | CRUD + 编码守门员 + FK |

**小计：~77 case，原 13 模块深度覆盖。**

### 1.2 后续转 🟢 模块（2026-05-25 ~ 27 批次 — 真实 E2E）

| # | 模块 | spec | case | 深度 |
|---|---|---|---|---|
| 14 | Inception | [inception.spec.ts](../../plm-frontend/e2e/inception.spec.ts) | 11 | 4 态状态机 + AI 评估 + FK + 编码 |
| 16 | Competitive | [competitive.spec.ts](../../plm-frontend/e2e/competitive.spec.ts) | 11 | 状态机 + AI 分析 + FK + 编码 |
| 17 | PRD | [prd.spec.ts](../../plm-frontend/e2e/prd.spec.ts) | 11 | 状态机 + AI 生成 + 完整度 + 编码 |
| 20 | ApiDesign | [apidesign.spec.ts](../../plm-frontend/e2e/apidesign.spec.ts) | 15 | 4 态状态机 + UK + HTTP method 白名单 + OpenAPI 生成 |
| 23 | ManualImpl | [manual-impl.spec.ts](../../plm-frontend/e2e/manual-impl.spec.ts) | 11 | 状态机 + FK + 编码 |
| 24 | ManualOps | [manual-ops.spec.ts](../../plm-frontend/e2e/manual-ops.spec.ts) | 11 | 状态机 + FK + 编码 |
| 25 | Analytics | [analytics.spec.ts](../../plm-frontend/e2e/analytics.spec.ts) | 10 | 快照 + 维度聚合 + 编码 |
| 27 | AI Agent | [ai-agent.spec.ts](../../plm-frontend/e2e/ai-agent.spec.ts) | 10 | 状态机 + AI 调用 + 编码 |
| 28 | OpenSpec | [openspec.spec.ts](../../plm-frontend/e2e/openspec.spec.ts) | 11 | 状态机 + spec 生成 + 编码 |
| 29 | Pipeline | [pipeline.spec.ts](../../plm-frontend/e2e/pipeline.spec.ts) | 10 | 状态机 + 阶段 + 编码 |
| 30 | FeatureFlag | [feature-flag.spec.ts](../../plm-frontend/e2e/feature-flag.spec.ts) | 10 | 开关状态 + 灰度 + 编码 |
| 31 | DORA | [dora.spec.ts](../../plm-frontend/e2e/dora.spec.ts) | 10 | 4 指标 + 周期 + 编码 |
| 22 | AutoTest | [autotest.spec.ts](../../plm-frontend/e2e/autotest.spec.ts) | 4 | CRUD + 706 脚本 + FK + 编码 |
| 26 | Dashboard | [dashboard.spec.ts](../../plm-frontend/e2e/dashboard.spec.ts) | 3（+button-fix 6） | 卡片 + 按钮跳转回归 |

**小计：138 case。** 逐 case 断言明细待补（见 §2.17），当前以 spec 文件为准。

### 1.3 仅占位待扩（1 case — 仅验 POST 200 + 页面可达）

| 模块 | spec | 后端单测 | 待补 |
|---|---|---|---|
| UED | [ued.spec.ts](../../plm-frontend/e2e/ued.spec.ts) | ✅ 28 case | E2E 扩状态机 + AI 检查 + FK + 编码 |
| Arch | [arch.spec.ts](../../plm-frontend/e2e/arch.spec.ts) | ✅ 35 case | E2E 扩状态机 + C4 + NFR + FK + 编码 |
| DbDesign | [dbdesign.spec.ts](../../plm-frontend/e2e/dbdesign.spec.ts) | ✅ 31 case | E2E 扩状态机 + ER/DDL + FK + 编码 |
| TestData | [testdata.spec.ts](../../plm-frontend/e2e/testdata.spec.ts) | ❌ 无 | 🔴 双缺口：补 ServiceImplTest + E2E 扩 |

### 1.4 跨切关注 / 烟雾测试

| 文件 | case 数 | 用途 |
|---|---|---|
| [encoding.spec.ts](../../plm-frontend/e2e/encoding.spec.ts) | 6 | **乱码守门员**（P0，必跑） |
| [navigation.spec.ts](../../plm-frontend/e2e/navigation.spec.ts) | 3 | 菜单可达性烟雾测试 |
| [all-pages.spec.ts](../../plm-frontend/e2e/all-pages.spec.ts) | 5 | 全页面可达性巡检 |
| [menu-sidebar-click.spec.ts](../../plm-frontend/e2e/menu-sidebar-click.spec.ts) | 1 | 侧边栏菜单点击跳转（gotcha #8 回归） |
| [dashboard-button-fix.spec.ts](../../plm-frontend/e2e/dashboard-button-fix.spec.ts) | 6 | 工作台按钮跳转回归（gotcha #8） |
| [screenshot-tour.spec.ts](../../plm-frontend/e2e/screenshot-tour.spec.ts) | 1 | 截图巡检 |

**跨切合计 22 case。**

---

## 2. 完整用例矩阵（按 spec 文件）

### 2.1 `encoding.spec.ts` (MANDATORY — 必跑 — P0 阻塞守门员)

| Case ID | 名称 | 断言点 |
|---|---|---|
| Enc-01 | Project 中文 projectName+description 无乱码 | DB HEX 不含 `EFBFBD` |
| Enc-02 | Requirement 中文 title 无乱码 | DB HEX 不含 `EFBFBD` |
| Enc-03 | Sprint 中文 name+goal 无乱码 | DB HEX 不含 `EFBFBD` |
| Enc-04 | Task 中文 title+description 无乱码 | DB HEX 不含 `EFBFBD` |
| Enc-05 | UI 层浏览器表单提交中文,DB 无乱码 | 通过 Vue 表单走 axios 链路 → DB HEX 校验 |
| Enc-06 | 反向自检: `assertNoMojibake` 能识别 EFBFBD 字节 | sanity test 测试本身可靠性 |

### 2.2 `project.spec.ts`

| Case ID | 名称 |
|---|---|
| TC-Proj-Home | 登录后首页加载 |
| TC-Proj-Route | 项目管理路由直接访问 |
| TC-Proj-List | 列表能看到 PRJ-2026-0001 |
| TC-Proj-Dialog | 新增对话框可打开 (无提交避免污染) |
| TC-Proj-Search | 搜索条件能输入 |

### 2.3 `requirement.spec.ts`

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-Req-F001 | CRUD + 编号 | requirementNo 符合 REQ-YYYY-NNNN |
| TC-Req-F005a | 4×4 状态机 + 反向边 01→00 | 反向"打回"边允许 |
| TC-Req-F005b | 非法转换全覆盖 | 所有 illegal 转换返 601 |
| TC-Req-F008 | FK projectId 不存在 | 返 702 |
| TC-Req-F009 | 新建状态必须为 00 | 非 00 返 601 |
| TC-Req-UI | 需求管理菜单 UI 可访问 | 列表 + 新增对话框可打开 |

### 2.4 `sprint.spec.ts`

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-Spr-F001 | CRUD + ADR-0004 编号 | sprintNo 符合 SPR-YYYY-NNNN |
| TC-Spr-F003 | **actual_start_date 自动填充** | 00→01 时 actualStartDate 自动 = today |
| TC-Spr-F004 | **业务硬规则 703 项目级单一活跃** | 同项目第二个 sprint 进 01 返 703 |
| TC-Spr-F005 | current 端点 | 返回当前活跃 sprint 或 null |
| TC-Spr-F006 | **健康度统计 stats** | 6 个统计字段齐 |
| TC-Spr-UI | 迭代管理菜单 UI 可访问 | 列表渲染 |

### 2.5 `task.spec.ts`

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-Task-F001 | CRUD + ADR-0003 编号 | taskNo 符合 TASK-YYYY-NNNN |
| TC-Task-F004 | **反向边 02→01 (评审打回)** | 允许 |
| TC-Task-F005 | **反向边 03→02 (测试打回)** | 允许 |
| TC-Task-F006 | 进入 04 必填 actualHours | 不填返 602,带则 200 |
| TC-Task-F008a | FK Sprint 不存在 | 返 702 + msg含"迭代" |
| TC-Task-F008b | FK Requirement 不存在 | 返 702 + msg含"需求" |
| TC-Task-F009 | MR URL 格式校验 | 非 http(s):// 返 604 |
| TC-Task-F010 | 看板 5 列 | columns.length=5, 标签正确 |
| TC-Task-F011 | 我的任务 | rows 中 assigneeUserId 全是当前 admin |
| TC-Task-UI | 任务管理+看板+我的任务 三个菜单可访问 | 都不报 JS 错误 |

### 2.6 `defect.spec.ts`

| Case ID | 名称 |
|---|---|
| TC-Def-F001 | CRUD + 编号 |
| TC-Def-F002 | 5×5 状态机 |
| TC-Def-F003 | 反向边 03→01 (回归打回) |
| TC-Def-F004 | 进入 03 必填 resolution |
| TC-Def-F005~F007 | 3 FK (project / sprint / task) |
| TC-Def-F008 | 非法转换全覆盖 |

### 2.7 `testcase.spec.ts`

| Case ID | 名称 |
|---|---|
| TC-TestCase-F001 | CRUD + ADR-0006 TC-YYYY-NNNN |
| TC-TestCase-F002 | 706 自动化用例必填脚本路径 |
| TC-TestCase-F003 | 反向边 03→01 + 04→01 重测 |
| TC-TestCase-F004 | /execute 端点 + execution_count + last_executed_at |
| TC-TestCase-F005 | /execute 不能直接传非 03/04 状态 |
| TC-TestCase-F006 | 非法转换全覆盖 |
| TC-TestCase-F007 | 必填字段校验 |
| TC-TestCase-UI | 测试用例管理菜单可访问 |

### 2.8 `document.spec.ts`

| Case ID | 名称 |
|---|---|
| TC-Doc-F001~F008 | 文档类型 + 版本号 + 评审流 + FK + 编码 |

### 2.9 `submission.spec.ts`

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-Sub-F001 | CRUD + 提测单创建 | 200 |
| TC-Sub-F002 | AI 质量门禁 4 项全 Y 才通过 | qualityGatePassed=Y |
| TC-Sub-F003 | 单测覆盖率 < 60% 不通过门禁 | qualityGatePassed=N |
| TC-Sub-F004 | 状态机 + **708 (进入 03 必须门禁通过)** | 600/708 |
| TC-Sub-F005 | 退回必填原因 + 反向边 04→00 | 602/200 |

### 2.10 `release.spec.ts` ✨ v2.0

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-Rel-F001 | 创建发布单 + REL-YYYY-NNNN 编号 | 编号格式 + status=00 |
| TC-Rel-F002 | 编码守门员: releaseNotes + rollbackReason | DB HEX 无 EFBFBD |
| TC-Rel-F003 | 同 project + version 唯一约束 | UK uk_release_project_version |
| TC-Rel-F004 | FK projectId 不存在 → 702 | 错误码 |

### 2.11 `testplan.spec.ts` ✨ v2.0

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-TP-F001 | 创建测试方案 + TP-YYYY-NNNN 编号 | 编号格式 + testCycleDays |
| TC-TP-F002 | 编码守门员: title + scope + strategy | DB HEX 无 EFBFBD |
| TC-TP-F003 | FK projectId 不存在 → 702 | 错误码 |

### 2.12 `testreport.spec.ts` ✨ v2.0

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-TR-F001 | 创建测试报告 + TR-YYYY-NNNN 编号 (黄灯) | 编号格式 + riskLevel |
| TC-TR-F002 | 编码守门员: riskEvaluation + recommendations | DB HEX 无 EFBFBD |
| TC-TR-F003 | risk_level 字典白名单 (green/yellow/red) | 非法值被拒 |
| TC-TR-F004 | FK projectId 不存在 → 702 | 错误码 |

### 2.13 `apidoc.spec.ts` ✨ v2.0

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-API-F001 | 创建 API 文档 + API-YYYY-NNNN 编号 | 编号格式 + status=00 |
| TC-API-F002 | 编码守门员: title + description | DB HEX 无 EFBFBD |
| TC-API-F003 | 同 method+path+version 唯一约束 | UK uk_apidoc_method_path |
| TC-API-F004 | FK projectId 不存在 → 702 | 错误码 |

### 2.14 `manual-product.spec.ts` ✨ v2.0

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-PM-F001 | 创建产品手册 + PM-YYYY-NNNN 编号 | 编号格式 + status=00 |
| TC-PM-F002 | 编码守门员: title + includeModules + content | DB HEX 无 EFBFBD |
| TC-PM-F003 | FK projectId 不存在 → 702 | 错误码 |

### 2.15 `navigation.spec.ts`

| Case ID | 名称 |
|---|---|
| Nav-Home | 登录后首页 PLM 标题 |
| Nav-Menu | 30 模块菜单可达性烟雾测试 |
| Nav-Stub | stub packages 文件结构完整 |

### 2.16 占位 spec（1 case — 仅验 POST 返 200 + 页面可达，待扩）

> ⚠ 空壳模块已清零；下列 4 个是 E2E **深度**未达标（详 §1.3）。原 inception / competitive / prd / apidesign 已转真实覆盖（详 §1.2），不再属此列。

| spec | 模块 |
|---|---|
| `ued.spec.ts` | UED |
| `arch.spec.ts` | 架构设计 |
| `dbdesign.spec.ts` | 数据库设计 |
| `testdata.spec.ts` | 测试数据（🔴 后端单测亦缺） |

### 2.17 §1.2 批次模块逐 case 明细（待补）

§1.2 的 14 个后续转 🟢 模块（inception/competitive/prd/apidesign/manual-impl/manual-ops/analytics/ai-agent/openspec/pipeline/feature-flag/dora/autotest/dashboard，共 138 case）已有真实 spec，但本矩阵的**逐 case 断言表**尚未逐一回填。在补齐前，以各 `*.spec.ts` 源文件的 `test('TC-…')` 标题为准（命名遵循 [测试策略.md §7 军规 5](../测试策略.md) `TC-{模块}-{编号}` 格式）。

---

## 3. 跨用例覆盖核对（业务设计点）

| 业务点 | PRD/设计文档引用 | 覆盖用例 |
|---|---|---|
| ADR-0001 PRJ 编号 | Project PRD §3.2 | TC-Proj-List |
| ADR-0002 REQ 编号 | Requirement PRD §3 | TC-Req-F001 |
| ADR-0003 TASK 编号 | Task PRD §3 | TC-Task-F001 |
| ADR-0004 SPR 编号 | Sprint PRD §3 | TC-Spr-F001 |
| ADR-0006 TC 编号 | TestCase PRD §3 | TC-TestCase-F001 |
| REL 编号 | Release PRD §F4.7+ | TC-Rel-F001 |
| TP 编号 | TestPlan PRD §F4.1 | TC-TP-F001 |
| TR 编号 | TestReport PRD §F4.7 | TC-TR-F001 |
| API 编号 | ApiDoc PRD §F5.4 | TC-API-F001 |
| PM 编号 | ManualProduct PRD §F5.1 | TC-PM-F001 |
| Project 5×5 状态机 | Project PRD §3.3 | TC-Proj-Route |
| Requirement 4×4 状态机 | Requirement PRD §3.3 | TC-Req-F005a/b |
| Sprint 4×4 状态机 | Sprint PRD §3.3 | TC-Spr-F003 + F004 |
| Task 6×6 状态机 含反向边 | Task PRD §3.3 | **TC-Task-F004/F005** |
| Defect 5×5 状态机 | Defect PRD §F4.6 | TC-Def-F002/F003 |
| TestCase 5×5 状态机 | TestCase PRD §F4.2 | TC-TestCase-F003 |
| Submission 5×5 状态机 | Submission PRD §F4.4 | TC-Sub-F004/F005 |
| 业务硬规则 703 项目级单一活跃 | Sprint PRD §1.2 | **TC-Spr-F004** |
| 业务硬规则 706 自动化必填脚本 | TestCase PRD §1.3 | **TC-TestCase-F002** |
| 业务硬规则 708 质量门禁 4 项 | Submission PRD §1.4 | **TC-Sub-F002/F003/F004** |
| Sprint actual_dates 自动填充 | Sprint API §2.4 | TC-Spr-F003 |
| Sprint stats 跨模块 | 模块拆分架构 §2.3 | **TC-Spr-F006** |
| 进入"已完成"必填实际工时 | Task API §2.4 | TC-Task-F006 |
| MR URL 格式 http(s):// | Task API §2.3 | TC-Task-F009 |
| 看板 5 列分组 + 每列 ≤ 50 | Task API §2.2 | TC-Task-F010 |
| 我的任务 SecurityUtils.getUserId() | Task API §2.5 | TC-Task-F011 |
| FK 校验 (Project + Req + Sprint) | 各模块 API | TC-Task-F008a/b + TC-Req-F008 + 等 |
| TestReport risk_level 字典白名单 | TestReport PRD §F4.7 | TC-TR-F003 |
| ApiDoc method+path+version 唯一 | ApiDoc PRD §F5.4 | TC-API-F003 |
| Release project+version 唯一 | Release PRD §F4.7+ | TC-Rel-F003 |
| **UTF-8 全栈无乱码** | 字符编码规范.md | **Enc-01~06 + 各模块编码守门员 case** |

✅ 业务关键点 100% 覆盖。

---

## 4. 执行命令速查

| 场景 | 命令 |
|---|---|
| **全套件** | `npm run test:e2e` 或 `npx playwright test` |
| 看着跑 | `npm run test:e2e:headed` |
| 单步调试 | `npm run test:e2e:debug` |
| **冒烟测试** (encoding + navigation) | `npm run test:e2e:smoke` |
| 仅 encoding（P0 守门员） | `npm run test:e2e:encoding` |
| 4 大业务模块 | `npm run test:e2e:business` |
| 质量模块 (defect+testcase+sub+rel) | `npm run test:e2e:quality` |
| 文档模块 (doc+apidoc+pm) | `npm run test:e2e:docs` |
| Phase 04 准入相关 (tp+tr+sub) | `npm run test:e2e:phase4` |
| 单模块（5 个新模块） | `npm run test:e2e:release / :apidoc / :manual-product / :testplan / :testreport` |
| 按 case 名过滤 | `npx playwright test -g "703"` |
| HTML 报告 | `npm run test:e2e:report` |

**前置条件**（[plm-e2e skill](../../.claude/skills/plm-e2e/SKILL.md) §前置检查）:
1. 后端启动: `java -Dfile.encoding=UTF-8 -jar plm-admin/target/plm-admin.jar --server.port=8081`
2. 前端启动: `npm run dev` (端口 80)
3. MySQL + Redis 运行
4. 环境变量 `DB_PASSWORD` 设置

---

## 5. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建,覆盖 41 case (encoding + 4 业务 + navigation) |
| v2.0 | 2026-05-17 | 全面覆盖 13/13 PRD-aligned 模块；补 5 个浅 spec → 各 3-4 case (release/apidoc/manual-product/testplan/testreport)；列出 16 空壳 + 1 缺模块 spec；登记 plm-e2e skill 引用。总 case ~91。 |
| v3.0 | 2026-05-27 | **全 31 模块同步**：§1.1 改为"原 13 深度覆盖"；§1.2 新增 14 个后续转 🟢 模块（~127 case，inception 含原"缺模块"已落地）；§1.3 占位收敛到 4 个（UED/Arch/DbDesign/TestData，TestData 双缺口）；§1.4 跨切 3→6 spec=22；§2.16 占位列表纠正（移除 inception/competitive/prd/apidesign）；新增 §2.17 批次模块逐 case 明细待补说明。总 case ~91→**241 / 37 spec**。与 [测试计划.md v0.5](../测试计划.md) 对齐。 |

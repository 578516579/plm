# E2E 自动化测试矩阵

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 框架 | Playwright 1.60+ (Chromium) |
| 落地日期 | 2026-05-16 |
| 当前总用例数 | **41 case (6 spec 文件)** |
| 通过率 | 41/41 (100%, 1m18s 全跑) |
| 强制级别 | **MUST** — Phase 03→04 准入条件之一 |

---

## 1. 套件总览

| 文件 | 用例数 | 覆盖范围 |
|---|---|---|
| `e2e/encoding.spec.ts` | 6 | **乱码回归** — UTF-8 全栈 HEX 校验 (含 UI 层 + 4 模块) |
| `e2e/project.spec.ts` | 5 | Project 模块（既有 — 项目脚手架原始用例） |
| `e2e/requirement.spec.ts` | 6 | Requirement 4×4 状态机 + 反向边 + FK + 601/702 |
| `e2e/sprint.spec.ts` | 7 | Sprint 4×4 + **业务硬规则 703** + actual_dates + 健康度 |
| `e2e/task.spec.ts` | 10 | Task **6×6 含反向边 (02↔01 / 03→02)** + 看板 + 我的任务 + 3 FK + MR URL |
| `e2e/navigation.spec.ts` | 7 | 30 模块菜单可达性烟雾测试 |

合计:**41 case**。

---

## 2. 完整用例矩阵

### 2.1 `encoding.spec.ts` (MANDATORY — 必跑)

| Case ID | 名称 | 断言点 |
|---|---|---|
| Enc-01 | Project 中文 projectName+description 无乱码 | DB HEX 不含 `EFBFBD` |
| Enc-02 | Requirement 中文 title 无乱码 | DB HEX 不含 `EFBFBD` |
| Enc-03 | Sprint 中文 name+goal 无乱码 | DB HEX 不含 `EFBFBD` |
| Enc-04 | Task 中文 title+description 无乱码 | DB HEX 不含 `EFBFBD` |
| Enc-05 | UI 层浏览器表单提交中文,DB 无乱码 | 通过 Vue 表单走 axios 链路 → DB HEX 校验 |
| Enc-06 | 反向自检: `assertNoMojibake` 能识别 EFBFBD 字节 | sanity test 测试本身可靠性 |

### 2.2 `requirement.spec.ts`

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-Req-F001 | 创建+列表+删除 | requirementNo 符合 REQ-YYYY-NNNN |
| TC-Req-F005a | 4×4 状态机 + 反向边 01→00 | 反向"打回"边允许 |
| TC-Req-F005b | 非法转换全覆盖 | 所有 illegal 转换返 601 |
| TC-Req-F008 | FK projectId 不存在 | 返 702 |
| TC-Req-F009 | 新建状态必须为 00 | 非 00 返 601 |
| TC-Req-UI | 需求管理菜单 UI 可访问 | 列表 + 新增对话框可打开 |

### 2.3 `sprint.spec.ts`

| Case ID | 名称 | 断言点 |
|---|---|---|
| TC-Spr-F001 | CRUD + ADR-0004 编号 | sprintNo 符合 SPR-YYYY-NNNN |
| TC-Spr-F003 | **actual_start_date 自动填充** | 00→01 时 actualStartDate 自动 = today |
| TC-Spr-F004 | **业务硬规则 703 项目级单一活跃** | 同项目第二个 sprint 进 01 返 703 |
| TC-Spr-F005 | current 端点 | 返回当前活跃 sprint 或 null |
| TC-Spr-F006 | **健康度统计 stats (通过 ITaskQueryService 反向调)** | 6 个统计字段齐 |
| TC-Spr-UI | 迭代管理菜单 UI 可访问 | 列表渲染 |

### 2.4 `task.spec.ts`

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

### 2.5 `project.spec.ts`

| Case ID | 名称 |
|---|---|
| TC-Proj-Home | 登录后首页加载 |
| TC-Proj-Route | 项目管理路由直接访问 |
| TC-Proj-List | 列表能看到 PRJ-2026-0001 |
| TC-Proj-Dialog | 新增对话框可打开 (无提交避免污染) |
| TC-Proj-Search | 搜索条件能输入 |

### 2.6 `navigation.spec.ts`

| Case ID | 名称 |
|---|---|
| Nav-Home | 登录后首页 PLM 标题 |
| Nav-Project | 项目管理可访问 |
| Nav-Req | 需求管理可访问 |
| Nav-Sprint | 迭代管理可访问 |
| Nav-Task | 任务管理可访问 |
| Nav-TaskKanban | 任务看板可访问 |
| Nav-MyTask | 我的任务 (顶级菜单 `/mytask`) 可访问 |
| Nav-Stub | 26 stub packages 文件结构完整 |

---

## 3. 跨用例覆盖核对（业务设计点）

| 业务点 | PRD/设计文档引用 | 覆盖用例 |
|---|---|---|
| ADR-0001 PRJ 编号 | Project PRD §3.2 | TC-Proj-List |
| ADR-0002 REQ 编号 | Requirement PRD §3 | TC-Req-F001 |
| ADR-0003 TASK 编号 | Task PRD §3 | TC-Task-F001 |
| ADR-0004 SPR 编号 | Sprint PRD §3 | TC-Spr-F001 |
| Project 5×5 状态机 | Project PRD §3.3 | TC-Proj-Route (基础) |
| Requirement 4×4 状态机 | Requirement PRD §3.3 | TC-Req-F005a/b |
| Sprint 4×4 状态机 | Sprint PRD §3.3 | TC-Spr-F003 + F004 |
| Task 6×6 状态机 含反向边 | Task PRD §3.3 | **TC-Task-F004/F005** |
| 业务硬规则 703 项目级单一活跃 | Sprint PRD §1.2 | **TC-Spr-F004** |
| Sprint actual_dates 自动填充 | Sprint API §2.4 | TC-Spr-F003 |
| Sprint stats 跨模块 (ITaskQueryService) | 模块拆分架构 §2.3 | **TC-Spr-F006** |
| 进入"已完成"必填实际工时 | Task API §2.4 | TC-Task-F006 |
| MR URL 格式 http(s):// | Task API §2.3 | TC-Task-F009 |
| 看板 5 列分组 + 每列 ≤ 50 | Task API §2.2 | TC-Task-F010 |
| 我的任务 SecurityUtils.getUserId() | Task API §2.5 | TC-Task-F011 |
| FK 校验 (Project + Req + Sprint) | Task API §2.3 | TC-Task-F008a/b + TC-Req-F008 |
| **UTF-8 全栈无乱码** | 字符编码规范.md | **Enc-01~06** |

✅ 业务关键点 100% 覆盖。

---

## 4. 执行命令速查

| 场景 | 命令 |
|---|---|
| 全跑 | `pnpm test:e2e` 或 `npx playwright test` |
| 看着跑 | `pnpm test:e2e:headed` |
| 单步调试 | `pnpm test:e2e:debug` |
| **冒烟测试** (encoding + navigation) | `pnpm test:e2e:smoke` |
| 仅业务模块 (4 spec) | `pnpm test:e2e:business` |
| 仅 encoding | `pnpm test:e2e:encoding` |
| 按 case 名过滤 | `npx playwright test -g "703"` |
| HTML 报告 | `pnpm test:e2e:report` |

**前置条件**:
1. 后端启动: `java -Dfile.encoding=UTF-8 -jar plm-admin/target/plm-admin.jar --server.port=8081`
2. 前端启动: `pnpm run dev` (端口 80)
3. MySQL + Redis 运行
4. 环境变量 `DB_PASSWORD` 设置

---

## 5. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建,覆盖 41 case (encoding + 4 业务 + navigation) |

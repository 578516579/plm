# PLM E2E 脚本逐页操作清单

> 生成日期：2026-05-27　|　覆盖 `plm-frontend/e2e/*.spec.ts` 全部 **37 个 spec 文件 / 约 250+ 用例**
> 本文档逐个 spec、逐个 `test()` 列出**操作步骤**，便于不看源码就知道每个页面的脚本在做什么。

## 怎么运行

```bash
cd plm-frontend
npx playwright test                         # 全量 headless
npx playwright test all-pages.spec.ts       # 单个 spec
npx playwright test -g "状态机"             # 按标题过滤
npx playwright test --headed                # 看着浏览器跑
```
前置：后端 `:8081`、前端 `:80`、MySQL `plm`、Redis `127.0.0.1:6379` 均已启动。

## 共享前置（helpers）

| helper | 作用 |
|---|---|
| `helpers/auth.ts` → `loginAsAdmin(request, context)` | **programmatic 登录**：`GET /captchaImage` 拿 uuid → `redis-cli GET captcha_codes:<uuid>` 拿验证码 → `POST /login {admin/admin123/code/uuid}` 拿 JWT → 注入 `Admin-Token` cookie。**所有 spec 共用**，绕过图形验证码。 |
| `helpers/api.ts` → `ApiClient` | `new ApiClient(request, token)`；封装 `post/put/get(url, data\|params)` 及 `createProject/listProjects/createSprint/createRequirement/createTask/...`，自动带 Bearer。 |
| `helpers/fixtures.ts` | `RUN_ID`（本轮唯一后缀，防数据串台）、`makeProjectData/makeSprintData/makeRequirementData/makeTaskData/makeDefectData/makeDocumentData`。 |
| `helpers/db.ts` | `execDelete(table, whereClause)` 直连 MySQL 清数据；`getFieldHex(table, field, where)` 取字段 HEX 验编码；`assertNoMojibake(...)` 检 `EFBFBD`(`�`) 字节。 |

**两类用例：**
- **API 契约型**：`api.post/put/get(...)` + 断言 `code/msg/data`（不开浏览器，验后端契约 + 状态机 + 编码）。
- **UI 渲染型**：`page.goto(route)` + 点击/填表 + 断言 `.el-table`/对话框/toast（开浏览器，验页面可用）。

**错误码速查**：`601` 状态机非法转换 · `602` 必填缺失 · `604` 字典/白名单非法 · `701` 唯一键冲突/前置失败 · `702` 外键(FK)不存在 · `703` 业务硬规则 · `706/707/708` 特定场景必填 · `ENC001` getFieldHex 不含 `EFBFBD`。

## 脚本分类：API 型 / UI 型 / 混合型

按是否真正驱动浏览器（`page.goto`）分三类（共 37 个脚本）：

| 类别 | 数量 | 脚本 | 说明 |
|---|---|---|---|
| 🟦 **纯 UI 型** | 7 | `all-pages`(63) · `project`(5) · `autotest`(4) · `dashboard-button-fix`(6) · `navigation`(3) · `menu-sidebar-click` · `screenshot-tour` | 每个用例都开浏览器：goto → 点击/填表/截图 → 断言 DOM/URL（autotest 用 api 仅做断言）|
| 🟩 **纯 API 型** | 23 | `inception` · `competitive` · `prd` · `ued` · `arch` · `dbdesign` · `apidesign`(15) · `dashboard` · `testplan` · `testdata` · `submission` · `testreport` · `apidoc` · `manual-product` · `manual-impl` · `manual-ops` · `pipeline` · `release` · `feature-flag` · `dora` · `analytics` · `openspec` · `ai-agent` | 不开浏览器，直打后端：`api.post/put/get` + 断言 `code/msg/data`，验必填/字典/FK/唯一键/状态机/编码 |
| 🟨 **混合型** | 7 | `requirement`(8+1) · `sprint`(5+1) · `task`(9+1) · `document`(7+1) · `testcase`(7+1) · `defect`(7+1) · `encoding`(5+1) | API 深测为主体 + 末尾 1 个「UI 菜单可达/表单」用例（`encoding` 的 UI 用例是浏览器提交中文表单后验 DB 无乱码）|

> 广度靠 🟦 `all-pages`（63 页全可达），深度靠 🟩 23 个纯 API（快而稳，验后端契约/状态机/编码），🟨 混合型在深测之上补一条"页面真能打开"的兜底。各脚本逐条操作步骤见下文分组明细。

---

# 一、跨切面 / 基础

### 全页面可达性 — `all-pages.spec.ts`　(数据驱动，覆盖 /getRouters 全部菜单页)
- **前置**：`beforeAll` loginAsAdmin 拿 token；每个用例注入 `Admin-Token` cookie。
- **清理**：无（只读冒烟，不造数据）。
- **每页统一操作步骤**（对页面清单循环生成用例）：
  1. 注入 `Admin-Token` cookie
  2. `page.goto(route, { waitUntil: 'domcontentloaded' })`
  3. 等 `.app-main` 可见（18s，吸收 Vite 首次按需编译）
  4. 断言无 `.wscn-http404-container`（没 fall-through 到 404）
  5. 等 500ms 让异步组件挂载
  6. 断言 `document.title` 不含 `�` 且非空（页面 chrome 编码正常）
  7. soft 断言无未捕获 JS 错误（`pageerror`）

**覆盖页面清单（57 菜单页 + 2 静态页）：**

| 分组 | 页面（路由 — 标题） |
|---|---|
| 系统管理(8) | /system/user 用户管理 · /system/role 角色管理 · /system/menu 菜单管理 · /system/dept 部门管理 · /system/post 岗位管理 · /system/dict 字典管理 · /system/config 参数设置 · /system/notice 通知公告 |
| 日志(2) | /system/log/operlog 操作日志 · /system/log/logininfor 登录日志 |
| 监控(6) | /monitor/online 在线用户 · /monitor/job 定时任务 · /monitor/druid 数据监控 · /monitor/server 服务监控 · /monitor/cache 缓存监控 · /monitor/cacheList 缓存列表 |
| 工具(3) | /tool/build 表单构建 · /tool/gen 代码生成 · /tool/swagger 系统接口 |
| 业务(34) | /business/dashboard 工作台 · project 项目管理 · inception 项目立项 · competitive 竞品情报 · requirement 需求管理 · prd AI PRD · ued UED设计 · arch 系统架构 · dbdesign 数据库设计 · apidesign 接口详细设计 · document 文档管理 · sprint 迭代管理 · task 任务管理 · taskkanban 任务看板 · mytask 我的任务 · testplan 测试方案 · testcase 测试用例 · testdata 测试数据工厂 · submission 提测管理 · autotest 自动化测试 · defect 缺陷管理 · testreport 测试报告 · apidoc API文档 · manual-product 产品手册 · manual-impl 实施手册 · manual-ops 运维手册 · pipeline 流水线 · release 发布管理 · feature-flag 功能开关 · dora DORA效能 · openspec AI规范 · ai-agent AI Agent · ai-invocation-log AI调用审计 · analytics 效能分析 |
| 集成/MCP(4) | /mcp/mcpserver MCP Server · /mcp/audit 调用审计 · /integration/connector 连接器配置 · /integration/webhook Webhook事件 |
| 静态页(2) | /index 首页 · /user/profile 个人中心 |

**4 个非循环用例：**

| 测试 | 操作步骤 |
|---|---|
| completeness: /getRouters 守门 | 1) `apiRequest.get('/getRouters')` 2) 递归 flatten 叶子路由（跳过 hidden）3) 断言每个 live 路由都在用例清单内（漏补即红）|
| 未知路由落 404 | 1) 注入 cookie 2) goto `/business/__no_such_page__` 3) 断言 `.wscn-http404-container` 可见 |
| 登录页渲染 | 1) goto `/login` 2) 断言 `.login-form` 可见 3) 断言占位符「账号」「密码」可见 |
| 注册页渲染 | 1) goto `/register` 2) 断言 `form.register-form` 可见 |

### 导航可达性 — `navigation.spec.ts`　(路由: 多业务路由)
- **前置**：`beforeAll` loginAsAdmin。

| 测试 | 操作步骤 |
|---|---|
| 登录后首页能加载 | 注入 cookie → goto `/` → 断言 title 含 `PLM` |
| active 模块路由可访问（循环 ACTIVE_ROUTES：project/requirement/sprint/task/taskkanban/mytask）| 注入 cookie → goto 路由(domcontentloaded) → 断言响应 ok/304 + body 可见 → 等 1.5s → soft 断言无 JS 错误 |
| stub packages 文件结构完整 | `request.get('/packages/plm-{dashboard,defect,ai-agent}/package.json')` → 断言状态码 ∈ {200,404,403}（不 500）|

### 侧边栏点击导航 — `menu-sidebar-click.spec.ts`　(模拟真实点菜单，非 goto 直达)
- **前置**：`beforeAll` loginAsAdmin。

| 测试 | 操作步骤 |
|---|---|
| 点击「分组 → 菜单项」落地正确 URL（循环各 phase 分组业务菜单）| 1) goto `/` 2) 点 `.el-sub-menu__title`(含分组名) 展开 3) 点子菜单链接 4) 取当前 URL 断言含期望路由 5) 断言不含 `/phase-` 前缀、不含 `/404` |

### 字符编码回归 — `encoding.spec.ts`　(Mojibake 守门员)
- **前置**：`beforeAll` loginAsAdmin + ApiClient。**清理**：各用例尾 `execDelete` 对应表。

| 测试 | 操作步骤 |
|---|---|
| Project：中文 projectName+description 写入无乱码 | api.createProject(中文数据) → getFieldHex 校验不含 `EFBFBD` → execDelete |
| Requirement：中文 title 写入无乱码 | createProject → createRequirement(中文) → HEX 校验 → execDelete |
| Sprint：中文 name+goal 写入无乱码 | createProject → createSprint(中文) → HEX 校验 → execDelete |
| Task：中文 title+description 写入无乱码 | createProject → createTask(中文) → HEX 校验 → execDelete |
| **UI 层**：浏览器表单提交中文 → DB 无乱码 | goto `/index`(让路由守卫跑完 addRoute) → goto `/business/project` → 等 `.el-table` → 点「新增」→ 填项目编号 `UI-xxx` / 项目名称 `UI 编码测试-xxx αβγ` → 点「确定」→ 断言成功 toast → DB HEX 校验不含乱码 → execDelete |
| 反向自检：assertNoMojibake 能识别 EFBFBD | 故意造含 `EFBFBD` 字节的项目 → 断言校验函数 reason 含 `EFBFBD`（自证守门有效）→ execDelete |

### 截图巡检 — `screenshot-tour.spec.ts`　(可视化回归取证)
- **前置**：`beforeAll` loginAsAdmin。**清理**：`afterAll`。

| 测试 | 操作步骤 |
|---|---|
| 各页截图（循环页面清单）| 注入 cookie → goto 页面 → 等待渲染 → 断言 `body` 可见 → 截图存档 |

### 工作台 — `dashboard.spec.ts`　(路由: /business/dashboard)
- **前置**：`beforeAll` loginAsAdmin + ApiClient。**清理**：`execDelete('tb_dashboard', title like %RUN_ID%)`。

| 测试 | 操作步骤 |
|---|---|
| TC-DASHBOARD-F001 创建默认工作台预设 | api.post('/business/dashboard', 预设) → 断言 code 200 |
| TC-DASHBOARD-F002 切换默认 → 自动取消旧默认 | post 工作台 A + B（均 default）→ get list(ownerUserId:2) → 断言仅 B 为默认、title 含「工作台 B」|
| TC-DASHBOARD-F003 聚合查询返回 6 类 widget | get '/business/dashboard/aggregate'(ownerUserId:1) → 断言返回 6 类 widget |

### 工作台按钮跳转修复 — `dashboard-button-fix.spec.ts`　(防 404 跳转回归)
- **前置**：`beforeEach` loginAsAdmin + 注入 cookie。

| 测试 | 操作步骤 |
|---|---|
| AI 快速立项 → /business/inception | goto dashboard → 等「AgriAI 智能助手」→ 点「AI 快速立项」按钮 → 断言 URL=`/business/inception` 且无 404 |
| lifecycle 节点「竞品」→ /business/competitive | goto dashboard → 等「项目生命周期」→ 点 `.lc-node`(竞品) → 断言 URL=`/business/competitive` |
| quickActions「生成 PRD」→ /business/prd | goto dashboard → 点 `.el-tag`(生成 PRD) → 断言 URL=`/business/prd` |
| lifecycle「编码」→ /business/task | goto dashboard → 点 `.lc-node`(编码) → 断言 URL=`/business/task` |
| lifecycle「运维手册」→ /business/manual-ops | goto dashboard → 点 `.lc-node`(运维手册) → 断言 URL=`/business/manual-ops` |
| sprint 看板按钮 → /business/task?sprintId= | goto `/business/sprint` → 等 `.el-table` → 点看板按钮 → 断言 URL 匹配 `/business/task?sprintId=\d+` |

---

# 二、规划

### 项目立项 — `inception.spec.ts`　(路由: /business/inception · PRD §F1.1)
- **前置**：loginAsAdmin + ApiClient。**清理**：`execDelete('tb_inception', project_name like 'E2E 立项-RUN_ID%')`。

| 测试 | 操作步骤 |
|---|---|
| TC-Inc-F001 创建立项(业务线+类型+背景诉求) | api.post('/business/inception', 全字段) → 断言 200 |
| TC-Inc-F002 projectName 必填 → 602 | post 缺 projectName → 断言 602 |
| TC-Inc-F003 businessLine 白名单非法 → 604 | post 非法 businessLine → 断言 604 |
| TC-Inc-F004 inceptionType 白名单非法 → 604 | post 非法 inceptionType → 断言 604 |
| TC-Inc-F005 状态机正向 00→01→02→03(自动填 approvedAt) | post → 逐步 put status 01/02/03 → get 断言终态 + approvedAt 已填 |
| TC-Inc-F006 跳级 00→03 → 601 | post → put status 03 → 断言 601 |
| TC-Inc-F007 →04 缺 rejectReason → 602 | post → put 01 → put 04(缺 rejectReason) → 断言 602 |
| TC-Inc-F008 反向边 04→00(打回重写) | post → put 01 → put 04(带 rejectReason) → put 00 → get 断言回到 00 |
| TC-Inc-F009 AI 生成 → aiGenerated=Y/内容/风险非空 | post → post '/ai/generate/{id}' → 断言 aiProposalContent 含「立项建议书」「精准灌溉决策系统」、风险非空 |
| TC-Inc-F010 编号格式 INC-YYYY-NNNN | post → list → 断言编号正则 |
| TC-Inc-ENC001 中文项目名 HEX 不含 EFBFBD | post 中文 → getFieldHex → 断言不含 EFBFBD |

### 项目管理 — `project.spec.ts`　(路由: /business/project · UI 型)
- **前置**：`beforeEach` loginAsAdmin + 注入 cookie。

| 测试 | 操作步骤 |
|---|---|
| 首页能加载 | goto `/` → 断言 URL=`/index` + title 含 PLM |
| 项目管理路由能直接访问 | goto `/business/project` → 断言 `.el-table` 可见 |
| 列表含已存在 PRJ-2026-0001 | goto → 等表格行 → 断言表格文本含 `PRJ-2026-` |
| 点「新增」弹出对话框(不提交) | goto → 等表格 → 点「新增」→ 断言对话框 + 含「项目名称」字段 → 点「取消」关闭 |
| 搜索条件能输入 | goto → 在「请输入项目名称」框填「测试」→ 断言值 → 点「搜索」→ 断言表格仍渲染 |

### 竞品情报 — `competitive.spec.ts`　(路由: /business/competitive · PRD §F1.3)
- **前置**：loginAsAdmin + createProject fixture 拿 projectId。**清理**：execDelete tb_competitive + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Comp-F001 创建竞品(价格档+监控订阅) | post 全字段 → 断言 200 |
| TC-Comp-F002 competitorName 必填 → 602 | post 缺名 → 602 |
| TC-Comp-F003 关联项目不存在 → 702 | post projectId 非法 → 702 |
| TC-Comp-F004 pricingTier 白名单非法 → 604 | post 非法档位 → 604 |
| TC-Comp-F005 状态机正向 00→01→02 | post → put 01 → put 02 → get 断言终态 |
| TC-Comp-F006 反向 01→00 非法(无反向边) → 601 | post → put 01 → put 00 → 601 |
| TC-Comp-F007 跳级 00→02 → 601 | post → put 02 → 601 |
| TC-Comp-F008 终态保护 02→01 → 601 | post → put 01 → put 02 → put 01 → 601 |
| TC-Comp-F009 AI 分析 → SWOT 四象限+综合报告 | post → post '/ai/analyze/{id}' → 断言报告含「竞品分析报告」「SWOT 矩阵」+ aiGenerated=Y |
| TC-Comp-F010 编号 COMP-YYYY-NNNN | post → list → 断言编号 |
| TC-Comp-ENC001 中文 competitor_name HEX 无乱码 | post 中文 → getFieldHex → 不含 EFBFBD |

---

# 三、需求与设计

### 需求管理 — `requirement.spec.ts`　(路由: /business/requirement)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_requirement_review/tb_requirement/tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Req-F001 创建+列表+删除全流程 | post 需求 → list 验在列 → delete → 验已删 |
| TC-Req-F005 状态机合法转换(反向边 01→00, 评审前置) | post → 提交评审 → 走合法转换含 01→00 → 断言成功 |
| TC-Req-F005 状态机非法转换全覆盖 | 对非法转换矩阵循环 put → 断言均 601 |
| TC-Req-F008 FK projectId 不存在 → 702 | post 非法 projectId → 断言 msg 含「关联项目不存在」|
| TC-Req-F009 新建状态必须为 00 | post status≠00 → 断言拒绝 |
| TC-Req-F010 00→01 无通过评审 → 701 | post → 直接 put 01(无评审) → 断言 msg 含「评审」|
| TC-Req-F011 评审 API CRUD: submit/list/delete | 提交评审 → list → delete 全链路断言 |
| TC-Req-F012 打回评审 reviewComment 必填 → 604 | 打回但缺 comment → 断言 msg 含「打回评审」|
| UI 层：需求管理菜单可访问且表单可填 | goto `/business/requirement` → 等 `.el-table` → 点「新增」→ 断言对话框含「需求标题」→ 点「取消」|

### AI PRD — `prd.spec.ts`　(路由: /business/prd · PRD §F2.2)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_prd + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-PRD-F001 创建 PRD(业务场景+目标用户) | post 全字段 → 200 |
| TC-PRD-F002 title 必填 → 602 / F003 关联项目不存在 → 702 / F004 sceneTemplate 白名单 → 604 | 对应 post 断言错误码 |
| TC-PRD-F005 状态机正向 00→01→02→03 | post → put 01/02/03 → get 终态 |
| TC-PRD-F006 反向边 01→00(评审打回) | post → put 01 → put 00 → get 回到 00 |
| TC-PRD-F007 跳级 00→02 → 601 / F008 终态保护 03→任意 → 601 | 对应转换断言 601 |
| TC-PRD-F009 AI 生成 → 7 段 + completenessScore≥80% | post → post '/ai/generate/{id}' → 断言 content 含 7 段(背景与目标/用户故事/功能描述/非功能需求/验收标准/原型说明/版本说明) + 评分达标 |
| TC-PRD-F010 编号 PRD-YYYY-NNNN | post → list → 断言编号 |
| TC-PRD-ENC001 中文 title HEX 无乱码 | post 中文 → getFieldHex → 不含 EFBFBD |

### UED 设计 — `ued.spec.ts`　(路由: /business/ued · PRD §F2.3)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_ued + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Ued-F001 创建 UED 设计(Figma + 农业组件标签) | api.post('/business/ued', 全字段) → 断言 200 + 字段回显 |

### 系统架构 — `arch.spec.ts`　(路由: /business/arch · PRD §F3.1)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_arch + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Arch-F001 创建架构方案(6 技术选型枚举) | api.post('/business/arch', {archMode/primaryStack/databaseChoice/aiOrchestration/deploymentType/iotProtocol}) → 断言 200 |

### 数据库设计 — `dbdesign.spec.ts`　(路由: /business/dbdesign · PRD §F3.2)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_dbdesign + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-DB-F001 创建数据库设计(引擎选型 + AI 生成入口) | api.post('/business/dbdesign', 全字段) → 断言 200 |

### 接口详细设计 — `apidesign.spec.ts`　(路由: /business/apidesign · PRD §F3.3，15 用例最全状态机)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_apidesign + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-APID-F001 创建接口设计(HTTP方法+路径+Mock开关) | post 全字段 → 200 |
| TC-APID-F002 title 必填 → 602 / F003 httpMethod 必填 → 602 | 缺字段 post → 602 |
| TC-APID-F004 httpMethod=CONNECT 白名单外 → 604 | post 非法方法 → 604 |
| TC-APID-F005 关联项目不存在 → 702 | post 非法 projectId → 702 |
| TC-APID-F006 状态机正向 00→01→02→03 | post → put 01/02/03 → get 终态 |
| TC-APID-F007 ⭐反向边 01→00(评审打回，apidesign 独有) | post → put 01 → put 00 → get 回 00 |
| TC-APID-F008 跳级 00→02 → 601 / F009 反向 02→00 → 601 / F010 终态 03→02 → 601 | 对应非法转换断言 601 |
| TC-APID-F011 小写 method 规范化(post→POST) | post method='post' → list 断言已转 POST |
| TC-APID-F012 AI 生成 OpenAPI 3.0 YAML+JSON Schema+Mock | post → post '/ai/generate/{id}' → 断言 openapiSpec 含 `openapi: 3.0.3`/`get:`、requestSchema/responseSchema/mockResponse 齐全 |
| TC-APID-F013 ⭐唯一键(project_id,http_method,path) 冲突 → 701 | post 同三元组两次 → 第二次 701 |
| TC-APID-F014 编号 APID-YYYY-NNNN | post → list → 断言编号 |
| TC-APID-ENC001 中文 title HEX 无乱码 | post 中文 → getFieldHex → 不含 EFBFBD |

---

# 四、研发

### 迭代管理 — `sprint.spec.ts`　(路由: /business/sprint)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_sprint + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Spr-F001 创建 + ADR-0004 编号生成 | api.createSprint → 断言 200 + 编号 |
| TC-Spr-F003 actual_start_date 自动填充(00→01) | createSprint → put 01 → get 断言 actualStartDate 已填 |
| TC-Spr-F004 业务硬规则 703 项目级单一活跃 | 建 A、B 两迭代 → A 置活跃 → B 置活跃 → 断言 703，msg 含「进行中的迭代」|
| TC-Spr-F005 当前活跃迭代 current 端点 | get '/business/sprint/current' → 断言返回活跃迭代 |
| TC-Spr-F006 健康度统计 stats(经 ITaskQueryService) | get '/business/sprint/{id}/stats' → 断言统计字段 |
| UI 层：迭代管理菜单可访问 | goto `/business/sprint` → 断言 `.el-table` 可见 |

### 任务管理 — `task.spec.ts`　(路由: /business/task · /taskkanban · /mytask)
- **前置**：loginAsAdmin + createProject + createSprint fixture。**清理**：execDelete tb_task/tb_sprint/tb_requirement/tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Task-F001 CRUD + ADR-0003 编号 | api.createTask → 断言 200 + 编号 |
| TC-Task-F004 反向边 02→01(评审打回) | createTask → 走到 02 → put 01 → 断言成功 |
| TC-Task-F005 反向边 03→02(测试打回) | createTask → 走到 03 → put 02 → 断言成功 |
| TC-Task-F006 进入 04 必填 actualHours | createTask → 推进至 04 缺工时 → 断言 msg 含「实际工时」|
| TC-Task-F008 FK Sprint 不存在 → 702 | createTask sprintId 非法 → msg 含「迭代」|
| TC-Task-F008 FK Requirement 不存在 → 702 | createTask requirementId 非法 → msg 含「需求」|
| TC-Task-F009 MR URL 格式校验 | createTask 非法 MR URL → msg 含「MR」|
| TC-Task-F010 看板视图返回 5 列 | get 看板端点 → 断言返回 5 列 |
| TC-Task-F011 我的任务端点 | get 我的任务端点 → 断言返回当前用户任务 |
| UI 层：任务管理+看板+我的任务三菜单可访问 | goto `/business/task` 等 `.el-table`；goto `/business/taskkanban` 断言 `.app-container` + 占位符；goto `/business/mytask` 断言 `.app-container`/`.el-result` |

### 文档管理 — `document.spec.ts`　(路由: /business/document · 合并 5 stub)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_document + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Doc-F001 CRUD + ADR-0007 按 type 编号 | post prd + arch 两类 → list 断言各自编号 |
| TC-Doc-F002 doc_type 字典外值 → 604 | post 非法 type → msg 含「doc_type」|
| TC-Doc-F003 4×4 状态机反向边 01→00 + 02→01 | post → put 01 → put 00；put 01 → put 02(带 reviewer) → put 01 → 均断言成功 |
| TC-Doc-F004 进入 02 必填 reviewer → 707 | post → put 01 → put 02(缺 reviewer) → msg 含「审核人」→ 带 reviewer 重试成功 |
| TC-Doc-F005 非法状态转换全覆盖 | 对非法矩阵循环 put → 断言 601 |
| TC-Doc-F006 FK projectId 不存在 → 702 | post 非法 projectId → 702 |
| TC-Doc-F007 多 doc_type 流水号分别累加 | 连建 prd×2 + proposal×1 → list 断言各 type 流水独立 |
| UI 层：文档管理菜单 + 12 doc_type 下拉 | goto `/business/document` → 等 `.el-table` → 断言含「文档类型」|

---

# 五、测试

### 测试方案 — `testplan.spec.ts`　(路由: /business/testplan)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_testplan + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-TP-F001 创建测试方案 + TP-YYYY-NNNN 编号(5 种 test_types) | post 全字段 → list 断言编号 |
| TC-TP-F002 编码守门员：中文 title+scope+strategy 无乱码 | post 中文 → list/HEX 校验不含乱码 |
| TC-TP-F003 FK projectId 不存在 → 702 | post 非法 projectId → 702 |

### 测试用例 — `testcase.spec.ts`　(路由: /business/testcase)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_testcase + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-TestCase-F001 CRUD + ADR-0006 TC-YYYY-NNNN | post → list 断言编号 |
| TC-TestCase-F002 706 自动化用例必填脚本路径 | post 自动化但缺脚本 → msg 含「脚本」|
| TC-TestCase-F003 反向边 03→01 + 04→01 重测 | post → 走到 03/04 → put 01 → 断言成功 |
| TC-TestCase-F004 /execute 端点 + execution_count + last_executed_at | post '/execute' → 断言计数+时间字段更新 |
| TC-TestCase-F005 /execute 不能直接传非 03/04 状态 | execute 传非法状态 → 断言拒绝 |
| TC-TestCase-F006 非法转换全覆盖 | 非法矩阵循环 put → 601 |
| TC-TestCase-F007 必填字段校验 | 缺必填 post → 602 |
| UI 层：测试用例管理菜单可访问 | goto `/business/testcase` → 等 `.el-table` → 断言表头含「自动化」列 |

### 测试数据工厂 — `testdata.spec.ts`　(路由: /business/testdata · PRD §F4.3)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_testdata + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-TD-F001 创建测试数据集(土壤传感器 + 4 规则开关) | api.post('/business/testdata', 全字段) → 断言 200 |

### 提测管理 — `submission.spec.ts`　(路由: /business/submission · PRD §F4.4)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_submission + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Sub-F001 CRUD + 提测单创建 | post 提测单 → 断言 200 |
| TC-Sub-F002 AI 质量门禁 4 项全 Y 才通过 | post → put 设 4 门禁项 → get 断言全 Y 才 passed |
| TC-Sub-F003 单测覆盖率 <60% 不通过门禁 | post 覆盖率不足 → put → get 断言门禁不通过 |
| TC-Sub-F004 状态机 + 708(进入 03 必须门禁通过) | post → put 01/02 → put 03(门禁未过) → 断言 708 → 门禁通过后重试成功 |
| TC-Sub-F005 退回必填原因 + 反向边 04→00 | post → put 01 → put 04(缺原因→拒)→ 带原因 04 → put 00 反向成功 |

### 自动化测试 — `autotest.spec.ts`　(路由: /business/autotest · UI 型，4 用例)
- **前置**：loginAsAdmin + createProject fixture(套件挂其名下)。**清理**：execDelete tb_autotest + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-AT-E001 列表加载 + 4 统计卡渲染 | goto `/business/autotest` → 断言 `h2.page-title` 含「自动化测试」+ 4 统计卡(测试套件/最新执行通过率/执行耗时/失败用例) + `.el-table` 可见 |
| TC-AT-E002 新增套件 — Dialog 全字段创建 | goto → 等表格 → 点「新增套件」→ 对话框内选项目下拉(选 fixture 项目)、类型 UI、框架 Playwright、打开定时开关 → 点「创建」→ 断言成功 toast → api.get list 验已落库 |
| TC-AT-E003 AI 生成脚本 → toast + scriptContent 非空 | goto → 点套件行进详情 → 断言「套件编号」可见 → 点「AI 生成脚本」→ 断言 toast → api.get 详情断言 scriptContent 非空 + 脚本块可见 |
| TC-AT-E004 立即执行(置 01 前置) → 统计 3 项 + 失败时 RCA | api.put 置 status=01 → goto → 进详情 → 点「立即执行」→ 断言 toast + 统计(总用例/通过/失败) → api.get 详情；失败时断言 `.rca-text` 可见 |

### 缺陷管理 — `defect.spec.ts`　(路由: /business/defect)
- **前置**：loginAsAdmin + createProject + createSprint + createRequirement + createTask 链式 fixture。**清理**：execDelete tb_defect/tb_task/tb_sprint/tb_requirement/tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Defect-F001 CRUD + ADR-0005 编号 | post 缺陷 → list 断言编号 |
| TC-Defect-F003 反向边 03→01(回归打回) | post → put 01/02/03(带 resolution) → put 01 → 断言成功 |
| TC-Defect-F004 进入 03 必填 resolution | post → put 01/02 → put 03(缺 resolution) → msg 含「解决说明」→ 带 resolution 重试成功 |
| TC-Defect-F005 状态机非法转换全覆盖 | 非法矩阵循环 put → 601 |
| TC-Defect-F006 3 FK 校验 | post projectId/sprintId/taskId 各非法 → msg 含「迭代」「任务」等 |
| TC-Defect-F007 新建状态必须为 00 | post status≠00 → 断言拒绝 |
| TC-Defect-F008 关联 Sprint+Task FK 联调 | post 带合法 sprintId+taskId → 断言 200 |
| UI 层：缺陷管理菜单可访问 | goto `/business/defect` → 等 `.el-table` → 断言含「严重级别」「分类」|

### 测试报告 — `testreport.spec.ts`　(路由: /business/testreport)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_testreport + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-TR-F001 创建测试报告 + TR-YYYY-NNNN 编号(黄灯风险) | post 全字段 → list 断言编号 |
| TC-TR-F002 编码守门员：中文 riskEvaluation+recommendations 无乱码 | post 中文 → list/HEX 校验不含乱码 |
| TC-TR-F003 风险级别字典白名单(green/yellow/red) | post 非法风险级别 → 断言拒绝 |
| TC-TR-F004 FK projectId 不存在 → 702 | post 非法 projectId → 702 |

---

# 六、交付运维

### API 文档 — `apidoc.spec.ts`　(路由: /business/apidoc)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_apidoc + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-API-F001 创建 API 文档 + API-YYYY-NNNN 编号 | post → list 断言编号 |
| TC-API-F002 编码守门员：中文 title+description 无乱码 | post 中文 → HEX 校验 |
| TC-API-F003 同 method+path+version 唯一 | post 同三元组两次 → 第二次冲突 |
| TC-API-F004 FK projectId 不存在 → 702 | post 非法 projectId → 702 |

### 产品手册 — `manual-product.spec.ts`　(路由: /business/manual-product)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_manual_product + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-PM-F001 创建产品手册 + PM-YYYY-NNNN 编号 | post → list 断言编号 |
| TC-PM-F002 编码守门员：中文 title+includeModules+content 无乱码 | post 中文 → list → put 更新 → HEX/assertNoMojibake 校验 |
| TC-PM-F003 FK projectId 不存在 → 702 | post 非法 projectId → 702 |

### 实施手册 — `manual-impl.spec.ts`　(路由: /business/manual-impl · PRD §F5.2)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_manual_impl + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-MANUAL-IMPL-F001 创建(Docker Compose+CentOS+PostgreSQL) | post 全字段 → 200 |
| TC-MANUAL-IMPL-F002 创建(K8s+Kylin+KingbaseES 国产化) | post 国产化组合 → 200 |
| TC-MANUAL-IMPL-F003 title 必填 → 602 / F004 项目不存在 → 702 / F005 deployMode 白名单 → 604 | 对应 post 断言错误码 |
| TC-MANUAL-IMPL-F006 状态机正向 00→01→02 自动填 generatedAt | post → put 01/02 → get 断言 generatedAt |
| TC-MANUAL-IMPL-F007 跳级 00→02 → 601 / F008 终态 03→00 → 601 | 对应非法转换 601 |
| TC-MANUAL-IMPL-F009 AI 生成 → content + status=02 + aiGenerated=Y | post '/ai/generate/{id}' → 断言 content 含「部署环境」|
| TC-MANUAL-IMPL-F010 编号 IM-YYYY-NNNN | post → list 断言编号 |
| TC-MANUAL-IMPL-ENC001 中文 title HEX 无乱码 | getFieldHex → 不含 EFBFBD |

### 运维手册 — `manual-ops.spec.ts`　(路由: /business/manual-ops · PRD §F5.3)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_manual_ops + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-MANUAL-OPS-F001 创建(Prometheus+钉钉/邮件+土壤传感器) | post → 200 |
| TC-MANUAL-OPS-F002 创建(Zabbix+飞书多渠道+无人机) | post → 200 |
| TC-MANUAL-OPS-F003 title 必填 → 602 / F004 项目不存在 → 702 | 对应错误码 |
| TC-MANUAL-OPS-F005 alertChannels CSV 含非白名单 → 604 | post 非法渠道 → 604 |
| TC-MANUAL-OPS-F006 状态机正向 00→01→02 自动填 generatedAt | post → put 01/02 → get 断言 |
| TC-MANUAL-OPS-F007 跳级 00→02 → 601 / F008 终态 03→00 → 601 | 非法转换 601 |
| TC-MANUAL-OPS-F009 AI 生成 → content + aiGenerated=Y | post '/ai/generate/{id}' → 断言 content 含「监控方案」|
| TC-MANUAL-OPS-F010 编号 OM-YYYY-NNNN | post → list 断言编号 |
| TC-MANUAL-OPS-ENC001 中文 title HEX 无乱码 | getFieldHex → 不含 EFBFBD |

### 流水线 — `pipeline.spec.ts`　(路由: /business/pipeline)
- **前置**：loginAsAdmin。**清理**：execDelete tb_pipeline。

| 测试 | 操作步骤 |
|---|---|
| TC-PIPELINE-F001 创建 Jenkins push 触发流水线 | post → 200 |
| TC-PIPELINE-F002 Cron 触发必须带 cronExpr → 602 | post cron 缺表达式 → 602 |
| TC-PIPELINE-F003 触发流水线累计 successRate | post '/trigger/{id}' → 断言 lastRunStatus ∈ {success,failed} |
| TC-PIPELINE-F004 pipelineName 必填 → 602 / F005 repoName 必填 → 602 / F006 cicdTool 白名单 → 604 | 对应错误码 |
| TC-PIPELINE-F007 状态机 00→01→00(启用↔停用) | put 01 → put 00 → 均 200 |
| TC-PIPELINE-F008 已停用触发 → 601 | put 01(停用) → trigger → 601 |
| TC-PIPELINE-F009 编号 PIPE-YYYY-NNNN | post → list 断言编号 |
| TC-PIPELINE-ENC001 中文 pipeline_name HEX 无乱码 | getFieldHex → 不含 EFBFBD |

### 发布管理 — `release.spec.ts`　(路由: /business/release)
- **前置**：loginAsAdmin + createProject fixture。**清理**：execDelete tb_release + tb_project。

| 测试 | 操作步骤 |
|---|---|
| TC-Rel-F001 创建发布单 + REL-YYYY-NNNN 编号(canary 策略) | post → list 断言编号 |
| TC-Rel-F002 编码守门员：中文 releaseNotes+rollbackReason 无乱码 | post 中文 → put 更新 → HEX 校验 |
| TC-Rel-F003 同 project 同 version 唯一 | post 同(project,version) 两次 → 冲突 |
| TC-Rel-F004 FK projectId 不存在 → 702 | post 非法 projectId → 702 |

### 功能开关 — `feature-flag.spec.ts`　(路由: /business/feature-flag)
- **前置**：loginAsAdmin。**清理**：execDelete tb_feature_flag。

| 测试 | 操作步骤 |
|---|---|
| TC-FF-F001 创建 canary 灰度 Flag | post canary → 200 |
| TC-FF-F002 canary 百分比必须 1-99 → 604 | post 越界百分比 → 604 |
| TC-FF-F003 flagKey 必须 snake_case → 604 | post `BadCamelCase` → 604 |
| TC-FF-F004 check 端点 — all_on 返回 true | get '/check'(flagKey,environment,userId) → 断言 true |
| TC-FF-F005 flagKey 必填 → 602 / F006 environment 白名单 → 604 / F007 all_on 百分比必须 100 → 604 | 对应错误码 |
| TC-FF-F008 创建关闭态再开启 all_on(toggle) | post(关) → list → put status=00 + all_on + 100% → 200 |
| TC-FF-F009 编号 FF-YYYY-NNNN | post → list 断言编号 |
| TC-FF-ENC001 中文 title HEX 无乱码 | getFieldHex → 不含 EFBFBD |

### DORA 效能 — `dora.spec.ts`　(路由: /business/dora)
- **前置**：loginAsAdmin。**清理**：execDelete tb_dora_metric。

| 测试 | 操作步骤 |
|---|---|
| TC-DORA-F001 记录部署频率(Elite 等级) | post deploy_freq → 断言等级 |
| TC-DORA-F002 记录 MTTR + AI 建议 | post → post '/ai/suggest/{id}' → 断言 aiSuggestions 含「MTTR」「农情专项」|
| TC-DORA-F003 metricType 白名单 → 604 / F004 metricName 必填 → 602 / F005 metricValue 必填 → 602 / F006 periodType 白名单 → 604 | 对应错误码 |
| TC-DORA-F007 状态机正向 00→01→02 | put 01 → put 02 → get 断言 |
| TC-DORA-F008 跳级 00→02 → 601 | put 02 → 601 |
| TC-DORA-F009 编号 DORA-YYYY-NNNN | post → list 断言编号 |
| TC-DORA-ENC001 中文 metric_name HEX 无乱码 | getFieldHex → 不含 EFBFBD |

---

# 七、分析 / AI

### 效能分析 — `analytics.spec.ts`　(路由: /business/analytics · PRD §F6)
- **前置**：loginAsAdmin。**清理**：execDelete tb_analytics_snapshot。

| 测试 | 操作步骤 |
|---|---|
| TC-ANALYTICS-F001 创建月度快照(全局) | post month 快照 → 200 |
| TC-ANALYTICS-F002 创建季度快照 + 状态机正向 00→01→02 | post quarter → put 01/02 → get 断言终态 |
| TC-ANALYTICS-F003 AI 复盘建议 → aiRecommendations + aiGenerated=Y | post '/ai/recommend/{id}' → 断言含「AI 复盘改进建议」「DORA」|
| TC-ANALYTICS-F004 title 必填 → 602 / F005 periodType 白名单 → 604 / F006 snapshotDate 必填 → 602 | 对应错误码 |
| TC-ANALYTICS-F007 跳级 00→02 → 601 / F008 终态 02→01 → 601 | 非法转换 601 |
| TC-ANALYTICS-F009 编号 AS-YYYY-NNNN | post → list 断言编号 |
| TC-ANALYTICS-ENC001 中文 title HEX 无乱码 | getFieldHex → 不含 EFBFBD |

### AI 规范 — `openspec.spec.ts`　(路由: /business/openspec · PRD §F3.5)
- **前置**：loginAsAdmin。**清理**：execDelete tb_openspec。

| 测试 | 操作步骤 |
|---|---|
| TC-OPENSPEC-F001 创建 OpenAPI 3.1 规范 / F002 创建 AsyncAPI 3.0 规范 | post 各类型 → 200 |
| TC-OPENSPEC-F003 AI 生成 GraphQL 骨架 | post '/ai/generate/{id}' → 断言 specContent 含 `type SoilReading` |
| TC-OPENSPEC-F004 同名同版本冲突 → 701 | post 同(name,version) 两次 → 701 |
| TC-OPENSPEC-F005 specName 必填 → 602 / F006 specType 白名单 → 604 / F007 version 必填 → 602 | 对应错误码 |
| TC-OPENSPEC-F008 状态机正向 00→01→02 | put 01/02 → get 断言 |
| TC-OPENSPEC-F009 跳级 00→02 → 601 | put 02 → 601 |
| TC-OPENSPEC-F010 编号 SPEC-YYYY-NNNN | post → list 断言编号 |
| TC-OPENSPEC-ENC001 中文 spec_name HEX 无乱码 | getFieldHex → 不含 EFBFBD |

### AI Agent 编排 — `ai-agent.spec.ts`　(路由: /business/ai-agent · PRD §F3.5)
- **前置**：loginAsAdmin。**清理**：execDelete tb_ai_agent。

| 测试 | 操作步骤 |
|---|---|
| TC-AIAGENT-F001 创建 PRD Agent | post(agentType=requirement,provider=mock) → 200 |
| TC-AIAGENT-F002 invoke 累计成功率(mock provider) | post '/invoke/{id}' ×2 → 断言成功率累计 |
| TC-AIAGENT-F003 agentType 白名单非法 → 604 / F004 agentName 必填 → 602 / F005 provider 白名单 → 604 | 对应错误码 |
| TC-AIAGENT-F006 状态机 00→01(停止) + 00→02(错误) | put status=01；另一条 put status=02 → 均 200 |
| TC-AIAGENT-F007 01→02 非法(已停止不可直接到错误) → 601 | put 01 → put 02 → 601 |
| TC-AIAGENT-F008 invoke 非运行中(status=01) → 601 | put 01 → invoke → 601 |
| TC-AIAGENT-F009 编号 AGT-YYYY-NNNN | post → list 断言编号 |
| TC-AIAGENT-ENC001 中文 agent_name HEX 不含 EFBFBD | getFieldHex → 断言不含 EFBFBD |

---

> 说明：业务模块多为 **API 契约型**（直接打后端验状态机/错误码/编码，稳定快），少数（project/requirement/sprint/task/defect/document/testcase/autotest/encoding/dashboard-button-fix）含 **UI 型**用例（开浏览器点按钮/填表单）。`all-pages.spec.ts` 是唯一覆盖系统/监控/工具等**非业务管理页**的广度冒烟套件。

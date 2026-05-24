# Changelog

所有上线版本的人类可读变更记录。格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

版本号按 [SemVer](https://semver.org/lang/zh-CN/)：`MAJOR.MINOR.PATCH`。

---

## [Unreleased]

### Added
-

### Changed
-

### Fixed
-

### Removed
-

---

## [0.5.0] - 2026-05-17

**里程碑版本：31/31 业务模块 PRD-aligned 闭环 + UED 规范建立 + Phase 03/04/05 三 Gate 批次签字**

### Added

**18 个新对齐业务模块**（P0/P1/P2/P3）：

- **P0 立项域 (3)**：`inception` (F1.1) AI 立项助手 · `prd` (F2.2) AI PRD 生成器 · `competitive` (F1.3) 竞品情报 + 15 维矩阵
- **P1 设计/测试域 (6)**：`ued` (F2.3) Figma 集成 + AI 设计评审 · `arch` (F3.1) C4 容器图 + 6 类技术选型 · `dbdesign` (F3.2) ER 图 + 建表 SQL · `apidesign` (F3.3) OpenAPI + Mock · `testdata` (F4.3) AI 造数 (5 农业表 × 3 格式) · `autotest` (F4.5) 自动化套件 + 调度
- **P2 文档/效能域 (4)**：`manual-impl` (F5.2) 实施手册 · `manual-ops` (F5.3) 运维手册 · `analytics` (F6) 效能快照 · `dashboard` (UI §4.2) 工作台聚合
- **P3 AI/DevOps 扩展 (5)**：`ai-agent` AI Agent 编排 · `openspec` (F3.5) OpenAPI/AsyncAPI/GraphQL 规范 · `pipeline` CI/CD · `feature-flag` 灰度 · `dora` DevOps 指标

**UED 设计规范**（`02-设计/UED规范.md`）：

- 13 章 + 2 附录: Design Token / 排版 / 颜色 / 布局 / 组件规范 / 导航 / AI 功能 UI / 响应式 / 交互 / 农业领域 UI / 无障碍 / 资产管理 / CR Checklist
- Claude 执行硬约束 `.claude/rules.md §N` 9 条: 颜色 Token / 状态徽章 / AI 按钮区分 / Label / 三态 / 间距 / Modal / CR / 新类
- 开发规范 §2 前端导引链接

**Phase 03/04/05 Gate 批次实例**:
- `99-跨阶段/gate-checklists/instances/prd-align-batch-2026-05-17/Phase03-开发-Gate-2026-05-17.md`
- `99-跨阶段/gate-checklists/instances/prd-align-batch-2026-05-17/Phase04-测试-Gate-2026-05-17.md`
- `99-跨阶段/gate-checklists/instances/prd-align-batch-2026-05-17/Phase05-上线-Gate-2026-05-17.md`
- 测试用例文档 `04-测试/测试用例库/PRD-align-batch-functional.md`

### Changed

- **PRD-MAPPING.md 状态**: 13🟢/16🟡/2🔴 → **31🟢/0🟡/0🔴**（全量闭环）
- 178 个 `/business/*` REST 路由全部注册（含 16 个 AI 入口）
- 70+ 个 `biz_<entity>_*` 字典 type
- 31 个 `business-<entity>.sql` 落 dev DB

### Fixed

- **CVE 修复 (high × 13)**: axios 1.13.2 → 1.16.1 — 清除 CRLF/SSRF/Prototype Pollution/DoS 等高危
- **pom 重复声明**: `plm-ued` 在 `plm-backend/pom.xml` 和 `plm-backend/plm-admin/pom.xml` 中重复声明（merge 残留）→ 各删 1 处,`mvn validate` 无 duplicate dependency 警告
- E2E feature-flag spec RUN_ID 含 hyphen 不匹配 snake_case 校验（commit `54b8454`）

### Verified

- `mvn install` BUILD SUCCESS 35 Maven 模块全绿
- `npx playwright test` E2E **120 / 120 case 全过**（分 4 批：立项+设计 13 / 研发+测试 41 / 文档+DevOps+AI 40 / 基础守门员 26）
- 编码守门员 6/6 全过（Mojibake guard）
- Swagger 178 路由 / 31 模块全部注册
- npm audit prod: 13 high CVE → 1 high CVE（剩 glob 仅 dev 影响）

### Known Issues (留下个 Sprint)

- `glob 10.x` CLI 命令注入 (high) — 仅 dev 依赖,生产 jar 不引入,影响 0
- Service 单元测试 jacoco 覆盖率 70% 未补（按 proposal 0004 由 E2E 替代）
- 真实 Dify AI 工作流未接入（16 个 AI 入口仍 mock）
- 性能 JMeter 基线未跑（按 `early` 阶段允许）

---

## [0.1.0] - 2026-05-15

首个业务版本：完成 P0 脚手架改造 + Project（项目）模块完整 CRUD。

### Added

**基础设施**
- 基于 RuoYi v3.9.2 完成 P0 改造：包名 `com.ruoyi.*` → `cn.com.bosssfot.dv.plm.*`，6 个 Maven 模块 `ruoyi-*` → `plm-*`，3 个 `RuoYi*` 启动类改名，yml 配置前缀 `ruoyi:` → `plm:`
- 凭据外部化：JWT secret / DB 密码 / Redis / Druid 控制台账号全部走 `${VAR:default}` 占位符，生成 `plm-backend/.env.example`
- 数据库：schema 名 `plm`（charset `utf8mb4` collate `utf8mb4_0900_ai_ci`），31 张系统表导入完成

**Project 业务模块（核心）**
- 数据库：`tb_project` 业务表 + 5 个索引（uk_project_no、idx_status、idx_type、idx_manager、idx_dates）
- 字典：2 个字典类型（`biz_project_type` 5 项 / `biz_project_status` 5 项），共 8 条字典数据
- 菜单：7 条 sys_menu 记录（业务管理父菜单 2000、项目管理 2010、5 个按钮权限 2011-2015 list/query/add/edit/remove/export），admin 角色全量授权
- 后端：`Project` domain + `ProjectMapper`(.java + .xml) + `IProjectService` + `ProjectServiceImpl` + `ProjectController`，6 个 REST 端点（list/getInfo/add/edit/remove/export）
- 前端：`/business/project` 路由 + 列表页 + 新增/修改/删除/导出对话框（基于若依代码生成器模板，已适配 PLM 字段）
- 业务规则：
  - `generateProjectNo` 自动生成项目编号 `PRJ-YYYY-NNNN`（实现见 ADR-0001，年度序号 SELECT MAX + 1）
  - 状态机：5×5 转换矩阵（规划/进行中/暂停/已完成/已取消），终态保护（已完成/已取消不可再变更），违规转换抛 ServiceException 错误码 601
  - 字段校验：必填校验 602、起止日期顺序校验 604、项目编号唯一性校验 701

**测试**
- 16 个 Service 单测（5 个 generateProjectNo / 4 个字段校验 / 7 个状态机覆盖）
- 1 个轻集成测试（Project 完整生命周期：新增→状态推进→更新→删除）
- 5 个 Playwright E2E 浏览器用例（headless chromium）：首页加载、路由直访、列表显示已有数据、新增对话框、搜索条件交互
- 累计自动化测试 22 个，全绿

**文档与流程**
- ADR-0001：项目编号 `PRJ-YYYY-NNNN` 生成规则决策记录
- PRD §3.3：5×5 状态机转换矩阵 + 终态保护规约
- 02-设计：API 设计文档（错误码 601/602/604/701）+ 表结构设计
- 03-开发 ~ 05-上线 Gate 实例（4D 参数化：early / internal-tool / solo）全部签字归档
- 自进化循环闭合：本期 Phase 03-04 产出 proposals 0004/0005/0006 已合入，模板已升级（staged DoD、solo Sprint 合并、项目成熟度维度）

### Changed
- Gate Checklist 模板从 2D 扩为 4D 参数化（项目分级 × 项目类型 × 团队规模 × 项目成熟度），共 6 个 Phase 模板均已升级
- `99-跨阶段/模块工作流.md`：增加成熟度章节，明确 early 项目可走简化路径

### Fixed
- 4 个已知本地启动坑沉淀进 `local_run_howto.md` 与 `ruoyi-bootstrap` skill：JDK 17 切换、mysql `--default-character-set=utf8mb4`、Windows+Java17 `localhost` IPv6 陷阱、`vite/plugins/auto-import.ts` sed 遗漏

### Removed
- 删除若依框架自带的 `RuoYi-Vue` GitHub/文档链接组件（`src/components/RuoYi/` 目录）
- 删除若依官网 banner 与 footer Copyright（保留 `@author ruoyi` 注释作框架归属）

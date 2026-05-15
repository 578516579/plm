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

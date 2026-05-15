# Claude 行为硬约束

本文件由 Claude Code 在每次会话中加载（与根 [CLAUDE.md](../CLAUDE.md) 互补）。**Claude 在本仓库工作时必须遵守**，与人类的 [03-开发/开发规范.md](../03-开发/开发规范.md) 同源同步——人类规范变了，本文件也同步更新。

下面规则按"硬"程度分级：**MUST**（绝不违反）、**SHOULD**（默认遵守、有正当理由可破）、**MAY**（建议）。

---

## A. 包名 / 目录 / 命名（MUST）

- 业务代码 **必须** 放在 `cn.com.bosssfot.dv.plm.system.business.<entity>` 子包下（除非用户显式要求新建 `plm-business` 独立模块）。
- Controller **必须** 放在 `plm-admin/web/controller/business/`，不要塞进 `plm-system`。
- 新 SQL 文件 **必须** 命名为 `plm-backend/sql/business-<entity>.sql`，不要混进 `ry_*.sql`。
- 业务表 **必须** 用 `tb_<entity_snake>` 前缀；业务字典 type **必须** 用 `biz_<entity>_<field>` 前缀，**禁止**用 `sys_` 前缀防止与系统字典冲突。
- 权限串 **必须** 用 `business:<entity>:<action>`（list / query / add / edit / remove / export），不要混进 `system:*`。

## B. 不可触碰区（MUST）

下列内容是**框架/上游归属**或**业务初始数据**，未经用户明确指示**禁止**改动：

- `@author ruoyi` Javadoc 与 `Copyright (c) ruoyi` 注释 → 保留为框架归属
- `plm-backend/sql/ry_*.sql` 中的 `若依` 字眼（sys_dept "若依科技"、sys_user nickName、sys_menu "若依官网"、sys_notice 3 公告）→ 演示数据，整体替换属于业务建模而非代码清理
- `README.md / LICENSE / doc/若依环境使用手册.docx` → 上游框架文档
- `plm-backend/.github/`（若存在）→ 仓库级 CI 配置
- 生成器 Velocity 模板 `.vm` 文件结构 → 保留以保证在线代码生成器仍可用

要批量改这些，先用 AskUserQuestion 确认。

## C. Secret / 凭据（MUST）

- **永远不要**把真实 password / token / API key / JWT secret 写进 yml 或代码。
- 任何敏感值都走 `${VAR:默认值}` 占位符；同步更新 [plm-backend/.env.example](../plm-backend/.env.example) 文档化。
- 用户在对话中提供了凭据（如本会话之前的 `aa8945163`）→ 只在当次 shell 命令里使用，**不要**落地到任何文件（包括 `.env` 实际值文件）。
- 发现凭据已经误入 git → 立刻警示用户，建议轮换并 `git filter-repo` 清理历史。

## D. 4 个已知坑（MUST 警觉，参见根 CLAUDE.md "Gotchas"）

写命令时**默认带上**避坑参数：

1. `mvn ...` 前一定 `export JAVA_HOME=<JDK 17 path>`
2. `mysql ...` 一定带 `--default-character-set=utf8mb4`
3. 后端启动一定 `export REDIS_HOST=127.0.0.1`（不要 `localhost`）
4. 前端做包名重命名时 **一定** 同步扫 `plm-frontend/vite/plugins/auto-import.ts`

碰到类似症状（Lettuce 超时、Data too long、mvn 用 JDK 8、auto-import 错），先查 [references/gotchas.md](../../.claude/skills/ruoyi-bootstrap/references/gotchas.md) 再去 debug。

## E. 业务模块生成（SHOULD）

- 用户要"加 Project / Phase / Task 等业务 CRUD" → **优先**用 `ruoyi-bootstrap` skill 的 Phase 7 模板生成（保证结构、权限、字典、菜单一致）。
- 模板没有的实体（如 `Risk`、`Document`）→ 复制 `assets/business-templates/project/` 改字段，不要从零写。
- 改完模板后建议把它沉淀回 skill（让以后能复用）。

## F. 提交规范（MUST）

- Commit message **必须** 遵 Conventional Commits（type/scope/subject 三段）。
- 由 `.githooks/commit-msg` 校验，不许用 `--no-verify` 绕过（用户明确要求时除外）。
- 多文件改动按"一件事一个 commit"拆分；不要把"加业务模块 + 改 CI + 修 typo"塞一个 commit。
- 涉及生成器/工具/skill 改动，commit 关联到产生它的指令（在 body 写"用户在会话中要求 …"）。

## G. 评审与卡控（SHOULD）

- 帮用户进入下一生命周期阶段前，提醒检查 [99-跨阶段/模块工作流.md](../99-跨阶段/模块工作流.md) 的"准出条件"。
- 帮用户做下列动作前**必须**先确认：
  - `DROP DATABASE` / `DROP TABLE` / 大批量删除文件
  - `git reset --hard` / `git push --force`
  - 改 main 分支保护规则
  - 改生产环境的 Redis / MySQL / Druid 等共享资源
- 用户授权过的是"一次性"还是"长期"，要分清；默认按一次性处理。

## H. 文档落位（MUST）

- 项目过程文档 **必须** 放进对应阶段目录 `0?-<阶段名>/` 或 `99-跨阶段/`，不要散在源码目录或根目录。
- 命名按 [开发规范.md §6.2](../03-开发/开发规范.md)：时序文档用日期前缀，索引类用目录 + README。
- 新增跨模块约定 → 同步更新本文件 + [开发规范.md](../03-开发/开发规范.md)，**两者必须一致**。

## I. 工具 / 依赖（SHOULD）

- 新增 Maven / npm 依赖前提示用户：版本号、用途、安全性（先扫一遍 CVE）。
- 重大依赖升级（如 Spring Boot major、Vue major）走 ADR 流程，不直接动 pom / package.json。
- 不要随手 `mvn dependency:upgrade` 或 `npm update` 整体升级；按"一次升一个、跑测试、commit"节奏来。

## J. 与用户沟通（MAY）

- 用户用中文 → 默认中文回复，技术术语保留英文。
- 用户描述简短 → 给 2-3 句话回答 + 一个清晰的下一步选项，不要堆长篇 plan。
- 文件改动多 → 用 TodoWrite 让用户看到进度。
- 大改动（删文件、改包名等）前用 AskUserQuestion 而不是问纯文本问题。

## K. 自我更新（SHOULD）

发现本文件某条规则与现实冲突（比如约定已演进） → 提醒用户更新本文件，不要私下"按新规则"工作让规则脱节。

每次重大重构（如 P0 改名、技术栈大升级）后，复审本文件 + 根 CLAUDE.md + 03-开发/开发规范.md 三者是否仍同步。

---

## 索引：相关规则文件

| 文件 | 受众 | 强制层 |
|---|---|---|
| [本文件 .claude/rules.md](rules.md) | Claude 自动加载 | 软（Claude 自觉） |
| 根 [CLAUDE.md](../CLAUDE.md) | Claude + 人类（开仓库即看） | 软 |
| [03-开发/开发规范.md](../03-开发/开发规范.md) | 人类开发者 | 软（CR 时检查） |
| [99-跨阶段/模块工作流.md](../99-跨阶段/模块工作流.md) | 人类 + Claude | 软（评审卡控） |
| [.editorconfig](../.editorconfig) | 编辑器 | 硬（自动应用） |
| [.githooks/commit-msg](../.githooks/commit-msg) | git | 硬（commit 时拒绝） |

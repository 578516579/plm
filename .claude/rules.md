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
5. **后端启动一定带** `-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`（Windows JDK 默认 GBK 会污染 HTTP body 解析，参见 [`03-开发/字符编码规范.md`](../03-开发/字符编码规范.md)）
6. **curl 测试含中文请求体一定走 `--data-binary @file.json`**（不能用内联 `-d '{"x":"中文"}'`，MSYS bash 会改字节）
7. 任何"DB 字段 HEX 含 `EFBFBD`"视为 P0 编码污染，先停下来排查再继续

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

## G. 评审与卡控（MUST — 已升级为硬约束）

### G.1 Gate Checklist（MUST）

进入任何生命周期下一阶段（01→02、02→03、03→04、04→05、05→06）前，**必须**先核对当前阶段的 Gate Checklist 实例是否已签字 commit：

- 实例位置：`99-跨阶段/gate-checklists/instances/<模块名>/PhaseNN-阶段名-Gate-<YYYY-MM-DD>.md`
- 若实例不存在 / 未打勾 / 未签字 / 未 commit → **拒绝执行**用户的"进入下一阶段"指令，先提示补 Gate
- 若用户坚持跳过 → 询问理由 + 用 AskUserQuestion 让用户选"补 Gate / 走 P0 hotfix 通道 / 取消"，并在对话中明确记录"用户授权跳过 Gate"
- 完成跨阶段操作后，提醒用户更新 [模块工作流总览](../99-跨阶段/模块工作流.md) 的进度行

### G.2 分级判断（MUST）

收到任何 改动请求 / "帮我加 XX 模块" 时，先按 [gate-checklists/README.md §分级](../99-跨阶段/gate-checklists/README.md) 判级：

- 拿不准 → **按"高就高"**（往上一级走）
- L1 触发条件之一：新业务模块、主架构变更、DB 结构性变更、新增第三方集成、安全合规相关
- L2 触发条件之一：已有模块大改、新增独立 feature、新增/改公开 API、性能优化 > 20%
- L3：bug / typo / 小重构 / patch 升级

判定 L1 / L2 后必须告诉用户："这属于 LX，需要走 Phase NN/NN/NN 的 Gate Checklist"。

### G.3 高危操作（MUST）

帮用户做下列动作前**必须**先确认（一次性 vs 长期，默认按一次性）：

- `DROP DATABASE` / `DROP TABLE` / 大批量删除文件
- `git reset --hard` / `git push --force`
- 改 main 分支保护规则
- 改生产环境的 Redis / MySQL / Druid 等共享资源
- 修改任何 `gate-checklists/instances/` 下**已签字**的 Checklist 文件（必须走"修订记录"追加，不许覆盖）

### G.4 E2E 自动化测试（MUST — Phase 03 → 04 准入条件）

任何业务模块 Phase 03 完成后,声明"开发完毕"前**必须**:

1. 启动后端（带 `-Dfile.encoding=UTF-8` 等 4 个编码标志）+ 前端 `npm run dev`
2. 在 `plm-frontend/` 跑 `npm run test:e2e:encoding`（乱码守门员 — 6 case 全过）
3. 跑 `npm run test:e2e` 全套件 41 case,**任何 fail 不允许进 Phase 04**
4. 把通过证据（最后一行 `41 passed`）写进 Phase 03 Gate 实例的 §I "进入 Phase 04 准出确认" 段

新增业务模块时,需要：
- 复用 [E2E-测试矩阵.md](../04-测试/测试用例库/E2E-测试矩阵.md) 模式新建 `e2e/<module>.spec.ts`
- 至少覆盖：CRUD + 状态机合法/非法 + FK 校验 + 编码 HEX 校验
- 添加 npm script `test:e2e:<module>`

详细操作见 [`04-测试/测试用例库/E2E-运行手册.md`](../04-测试/测试用例库/E2E-运行手册.md)。

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

## L. 自进化机制（MUST）

本仓库启用了"自进化反馈环"（signals → reflect → proposals → 规范变更 → tracking）。Claude 必须遵守以下流程约束：

### L.1 会话末沉淀（MUST）

每个回合结束时（由 `.claude/settings.json` 的 Stop hook 自动提示），Claude 必须主动评估本回合是否产生了"值得沉淀的知识"：

| 类型 | 触发条件 | 沉淀位置 |
|---|---|---|
| **新 gotcha** | 用户纠错 / 踩到未知坑 / 同类问题第 2 次出现 | `.claude/skills/ruoyi-bootstrap/references/gotchas.md` |
| **架构决策** | 选型 / 不可逆决策 / 模块拆分合并 | `03-开发/ADR/NNNN-<标题>.md` |
| **规范矛盾** | 发现两份规则文档自相矛盾 / 规范与实际不符 | `99-跨阶段/proposals/NNNN-<标题>.md` |
| **Gate 状态变化** | 本回合改动了某 Phase 的产出物 | 更新 `99-跨阶段/gate-checklists/instances/<module>/...` |
| **信号事件** | Claude 拒绝 / 用户 override / 高危命令 | 留待月度 signals 自动采集 |

判断逻辑：如果回合内**没有**触及上述任何场景 → 跳过沉淀；**有**则在回合末主动提出（"我建议把这条记入 gotchas，要做吗？"），不要等用户问。

### L.2 Proposal 流程（MUST）

涉及修改下列**规范文档**的请求，必须先写 proposal，不允许直接改文件：

- [03-开发/开发规范.md](../03-开发/开发规范.md)
- [99-跨阶段/模块工作流.md](../99-跨阶段/模块工作流.md)
- [99-跨阶段/gate-checklists/Phase*.md](../99-跨阶段/gate-checklists/)（模板，非 instances）
- [.claude/rules.md](rules.md)（本文件）
- [.claude/settings.json](settings.json)

流程：
1. Claude 帮用户在 `99-跨阶段/proposals/NNNN-<标题>.md` 创建 proposal（用 `0000-template.md` 模板）
2. proposal 必须含"证据"（链 signals / reflect / 事故 / gotcha / 用户明确请求之一）
3. proposal 走 review 通过 → 才能动规范文件
4. merged 后进 tracking 期 → 看相关 signals 是否好转

**例外**：用户明确说"绕过 proposal 直接改"时允许，但必须在 proposal 文件里事后补录（标 "User-requested-bypass"）。

### L.3 反思引擎（SHOULD）

`/reflect-weekly` / `/reflect-monthly` / `/reflect-quarterly` 命令实现后（Phase B+），Claude 应主动在合适时机建议触发，例如：
- 周一开会前
- Sprint 结束前
- 月初 / 季度初
- 出了 P0 故障后

反思报告产出后，**必须**追问"哪几条要转 proposal"，避免反思变成"心理按摩"（[reflect/README.md](../99-跨阶段/reflect/README.md) 反模式段）。

### L.4 数据完整性（MUST）

- `signals/`、`reflect/`、`proposals/` 目录下的历史文件**永久保留**，不允许删除。`rejected` 状态的提案也保留作为学习材料。
- 修改已 `merged` 状态的 proposal → 走"修订记录"追加；修改已签字的 `gate-checklists/instances/` 文件 → 同上。
- 信号数据隐藏（如把 `commit_bypass_count` 调小）是严重违规，**禁止**。

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

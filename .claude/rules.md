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

## F. 提交与分支规范（MUST）

- Commit message **必须** 遵 Conventional Commits（type/scope/subject 三段）。
- 由 `.githooks/commit-msg` 校验，**不许** `--no-verify` 绕过（用户明确要求时除外）。
- 多文件改动按"一件事一个 commit"拆分；不要把"加业务模块 + 改 CI + 修 typo"塞一个 commit。
- 涉及生成器/工具/skill 改动，commit 关联到产生它的指令（在 body 写"用户在会话中要求 …"）。
- **分支名必须遵 `<type>/<desc>` 规范**，由 `.githooks/pre-push` 校验；`claude/` 前缀分支为工具自动创建，免校验。
- **禁止直接推 `main` / `release/*`**（pre-push hook 强制，Claude 帮用户操作时同样禁止）。

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
3. 跑 `npm run test:e2e` 全套件,**任何 fail 不允许进 Phase 04**(当前用例总数见 [E2E-测试矩阵.md](../04-测试/测试用例库/E2E-测试矩阵.md) §1)
4. 把通过证据（最后一行 `N passed`）写进 Phase 03 Gate 实例的 §I "进入 Phase 04 准出确认" 段

**工具链(Claude 必用)**:

| 资源 | 用途 |
|---|---|
| [`.claude/skills/plm-e2e/SKILL.md`](skills/plm-e2e/SKILL.md) | E2E 流程的 SSoT — Claude 决策树/前置检查/失败排查/Gate 落档 |
| `/e2e-run [smoke\|encoding\|business\|<模块>]` | 跑测试(自动前置自检) |
| `/e2e-encoding` | 单跑编码守门员(6 case) |
| `/e2e-smoke` | 冒烟测试(typo 修复后用) |
| `/e2e-debug <spec> [-g <case>]` | Playwright Inspector 单步调试 |

**Claude 行为约束**:
- 用户说"开发完毕 / 提测 / Phase 04 / 自动化测试" → 必须主动建议触发 `plm-e2e` skill，不要被动等用户问
- 用户跑 `npm run test:e2e*` 命令前，PreToolUse hook 会提醒前置 4 项检查
- 测试 fail → 不要"再跑一次试试"，按 [E2E-运行手册.md §4](../04-测试/测试用例库/E2E-运行手册.md) 失败排查表归类
- DB HEX 含 `EFBFBD` → **P0 阻塞**，立即停下，**不允许**绕过 encoding 套件

**新增业务模块时**:
- 复用 [E2E-测试矩阵.md](../04-测试/测试用例库/E2E-测试矩阵.md) 模式新建 `plm-frontend/e2e/<module>.spec.ts`(以 [`defect.spec.ts`](../plm-frontend/e2e/defect.spec.ts) / [`testcase.spec.ts`](../plm-frontend/e2e/testcase.spec.ts) 为模板)
- 至少覆盖：CRUD + 状态机合法/非法 + FK 校验 + 编码 HEX 校验 + UI 菜单可达性
- 添加 npm script `test:e2e:<module>`
- 把新 case 登记进 [E2E-测试矩阵.md §2](../04-测试/测试用例库/E2E-测试矩阵.md)

详细操作见 [`04-测试/测试用例库/E2E-运行手册.md`](../04-测试/测试用例库/E2E-运行手册.md) 与 [`plm-e2e` skill](skills/plm-e2e/SKILL.md)。

### G.5 测试编排与自进化（MUST — Test Orchestration）

E2E 是准入的"最后一道"，但**整个测试过程**(测什么/分几层/谁来做/算不算过/怎么进化)由 **`test-orchestrator` agent + `plm-test-orchestrate` skill** 统一编排。proposal 0023 落地。

**G.5.1 测试金字塔分层（MUST）** — 不许用 E2E 测本该单测覆盖的逻辑分支(金字塔别倒挂)：

| 层 | 工具 | 何时必做 |
|---|---|---|
| L1 单元 | JUnit5 + Mockito | ServiceImpl 有分支/状态机/校验逻辑 |
| L2 组件 | Vitest + MSW | 复杂前端组合式逻辑/store/工具函数 |
| L3 契约 | 5 层命名对齐(interface↔domain↔column↔DTO↔resultMap) | 改了 domain/DTO/Mapper XML/前端 interface |
| L4 E2E | Playwright | Phase 03→04 准入(强制) / 改业务字段·状态机·FK·编码 |
| 守门 | encoding.spec(6 case) | 每次准入**一票否决** |

**G.5.2 编排总管（MUST）** — 满足下列任一,Claude **必须**先走 `test-orchestrator`(出计划+DAG)再分派,不许散乱手测：
- 测试任务跨 ≥ 3 个子 agent(test-engineer / api-contract-keeper / e2e-validator / troubleshooter / security-reviewer)
- 用户说"提测 / Phase 04 准入 / 全回归 / 这模块测试怎么搭 / 覆盖够不够"
- 子 agent **不能再 spawn 子 agent**:总管出 DAG,主 Claude 按序调。

**G.5.3 Gate 裁决（MUST）** — 判"通过"的充要条件(并入 §G.4)：encoding 6/6 且 DB HEX 无 `EFBFBD` · 全套件 `N passed` 且 **0 fail / 0 did-not-run**(flake 经 `--retries=1` 复测仍绿)· 新模块覆盖 CRUD+状态机合法/非法+FK+编码+UI 可达 5 类 · 契约改动经 api-contract-keeper 确认 · 证据为**本轮真实输出**已落 Gate §I。任一不满足 = **驳回**,指明回哪个 agent 修,**禁**"再跑一次试试"、**禁**贴历史证据。

**G.5.4 自进化（MUST）** — 每轮测试编排收口必须沉淀测试 signals(见 [signals §8 测试编排](../99-跨阶段/signals/README.md))：`e2e_flake_count` 与 `e2e_real_fail_count` **必须分开记**、`coverage_gap`、`rca_category`(env/schema/stale-jvm/code/contract/encoding)、`gate_evidence_backfill_attempt`(应=0)。触发提案:同类 flake 月 ≥3 → 稳定性提案;覆盖缺口反复 → 补模板提案。详见 [测试工作流.md](../99-跨阶段/测试工作流.md)。

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

## M. PRD/原型驱动开发（MUST — 防跑偏硬规则）

**核心原则**：本仓库是 **AgriPLM·AI** 的实现，**所有业务模块的字段、状态机、错误码、UI 文案、URL 路径，必须能追溯到** `prd和原型/AgriAI-PLM-完整PRD文档.md` 的 §章节 + `prd和原型/AgriPLM-DevOps-原型/agriplm_split/*.html` 的具体表单/按钮元素。

**单一事实来源**：[`PRD-MAPPING.md`](../PRD-MAPPING.md)（项目根）。Claude 任何业务相关动作都先读这个文件确认对应模块的 PRD § / 原型 HTML / 字段对照表 / 状态机 / 错误码，然后再写代码。

### M.1 PRD 与原型的权威性（MUST）

- PRD 文档 + 原型 HTML + `PRD-MAPPING.md` 三者**完全一致**时，以 `PRD-MAPPING.md` 为准（它是规范化产物）。
- 三者不一致时，**停下来不要写代码**，先用 AskUserQuestion 让用户决策：
  - 选项 A：改代码对齐 PRD（Drift 是 bug）
  - 选项 B：改 PRD-MAPPING.md 对齐当前需求（PRD 演化，需走 §L.2 Proposal）
  - 选项 C：把分歧记入 `99-跨阶段/proposals/` 等评审
- **禁止**：凭直觉添加 PRD 未提及的字段、状态、菜单项、错误码。

### M.2 新模块落地强制 9 项 DoD（MUST）

任何「空壳模块 PRD-align 落地」必须按 [PRD-MAPPING.md §8 DoD](../PRD-MAPPING.md) 9 项 checkbox 全过：

```
□ 1. 在 PRD-MAPPING.md §2 追加该模块的"字段对照表"（Domain ↔ 原型元素 — 逐字段）
□ 2. business-<entity>.sql 建表 + 字典（biz_<entity>_*）
□ 3. Domain.java 字段完整，Excel/JsonFormat 注解到位
□ 4. Mapper.xml 完整 resultMap + 动态 trim + selectMaxSeqOfYear
□ 5. Mapper.java 接口含 selectMaxSeqOfYear
□ 6. ServiceImpl.java 包含 (a) FK 校验 702 (b) 状态机校验 601 (c) 编号生成
□ 7. Controller 6 端点 + 权限串 business:<entity>:*（list/query/add/edit/remove/export）
□ 8. E2E spec 至少 1 测试，POST /business/<entity> 返回 code=200
□ 9. mvn install BUILD SUCCESS + E2E suite 全绿
```

**先字段表后代码**：字段对照表 commit 必须**先于**代码 commit（字段对照可以独立 commit，但代码 commit 必须 reference 对照表的 commit hash）。

### M.3 字段命名映射（MUST）

| 来源 | 命名规则 |
|---|---|
| 原型 HTML 表单 `<label>提测标题 *</label>` | Java field `title`，列 `title` |
| 原型 HTML 表单 `<label>期望测试周期(天)</label>` | Java field `expectedTestDays`，列 `expected_test_days` |
| 原型 HTML 表单下拉 `测试环境` | Java field `environment`，列 `environment`，字典 `biz_<entity>_environment` |
| 原型 HTML AI 按钮 `AI质量门禁检查` | 服务端字段 `qualityGatePassed`（服务端计算，**不接受**前端写入） |

- 服务端计算字段（qualityGatePassed / aiReviewScore 等）在 Mapper update 时**不要**信任前端值，必须根据其他字段重算。
- 任何 ENUM 类字段（status / strategy / riskLevel）在 service 入口校验**白名单**，不在范围 → 抛 604。

### M.4 状态机来自原型（MUST）

新模块的状态机**只能**来自：
- PRD §3.2 该模块的功能描述里的状态术语（草稿/评审/确认/...）
- 原型 HTML 该模块的状态徽章 CSS 类（`.bg`=已确认绿 / `.bam`=评审中黄 / `.bgr`=草稿灰 / `.bd`=失败红）
- [PRD-MAPPING.md §3](../PRD-MAPPING.md) 状态机汇总

禁止凭"参考其他模块顺便补全状态"。每个状态必须在 PRD 或原型里能指出原文。

### M.5 错误码统一登记（MUST）

新增任何错误码必须先在 [PRD-MAPPING.md §4](../PRD-MAPPING.md) 错误码表登记（代码含义 + 出处 + 示例），不允许在代码注释里"裸"用一个数字（例如 `throw new ServiceException("...", 999)` 在表里没登记 = 违规）。

### M.6 跑偏检测（Drift Detection）（SHOULD）

收到「加 XX 模块」「新增字段 YY」「改状态 ZZ」请求时：

1. **先查表**：打开 [PRD-MAPPING.md](../PRD-MAPPING.md) 找该模块
2. **逐项对照**：
   - 用户要的字段在原型 HTML 里能找到吗？
   - 用户要的状态在 PRD §3.2 里能找到吗？
   - 用户要的 API 路径符合 `/business/<entity>` 规则吗？
3. **冲突时停下**：若用户要求与原型/PRD 冲突，先用 AskUserQuestion 列两个选项（按原型 / 按用户），用户选"按用户"则要求**同步更新 PRD-MAPPING.md**（不允许只改代码不改表）。

### M.7 跨模块一致性（SHOULD）

不同模块的同一概念必须用同名字段：
- 项目关联：都用 `projectId`（列 `project_id`）
- 迭代关联：都用 `sprintId`（列 `sprint_id`）
- 作者/责任人：按角色区分 `authorUserId` / `assigneeUserId` / `reviewerUserId`（不要混用 `userId`）
- AI 生成标志：都用 `aiGenerated` CHAR(1) Y/N
- 软删除：都用 `delFlag` CHAR(1)（'0' 正常 / '2' 已删）

发现某新模块要用"奇怪"字段（如 `creatorId` 而不是 `createBy`） → 检查是否有 PRD 依据，没有就用现有命名规约。

### M.8 16 个空壳模块的攻坚优先级（REFERENCE）

按 [PRD-MAPPING.md §7](../PRD-MAPPING.md) 路线图：
- **P0（Phase 1 MVP，已完成 🟢）**：`inception` / `prd` / `competitive`
- **P1（Phase 2，启动）**：~~`ued`（🟢 已完成）~~ / `arch` / `dbdesign` / `apidesign` / `testdata` / `autotest`
- **P2（Phase 3）**：`manual-impl` / `manual-ops` / `analytics` / `dashboard`
- **P3（扩展）**：`ai-agent` / `openspec` / `pipeline` / `feature-flag` / `dora`

每次攻坚一个模块走完 §M.2 的 9 项 DoD + 提交一次 `feat(prd-align): <entity> PRD-aligned per F? + <html>`。

### M.9 产品设计编排与自进化（MUST — Product Design Orchestration）

§M.1~M.8 是 PRD 驱动的"硬规则",但**整个产品设计过程**(需求拆什么/字段哪来/原型对不对得上/算不算设计就绪/怎么进化)由 **`product-orchestrator` agent + `plm-product-design` skill** 统一编排。proposal 0024 落地。本节与开发后的 **§G.5 测试编排** 串成完整生命周期:**设计就绪 Gate = Phase 02→03 准入**,测试 Gate = Phase 03→04 准入。

**M.9.1 产品设计漏斗分层（MUST）** — 从一句模糊想法收敛到可 100% 追溯到 PRD+原型 的规格,越下层越收敛:

| 层 | 子 agent | 何时必做 |
|---|---|---|
| L1 澄清 | `requirement-clarifier` | 用户给模糊词(全部/优化下/弄一下) |
| L2 范围 | `scope-decider` | 跨 ≥2 模块 / 开放目标 |
| L3 PRD 建模 | `prd-author` | 新模块/新字段/新状态 — 字段对照表+状态机+错误码 |
| L4 数据/架构 | `db-modeler` + `system-architect` | 建表 / 引入抽象层 |
| L5 原型对齐 | `ux-prototype-aligner` | 任何 UI 呈现 — §N(UED) 守门 |
| L6 契约 | `api-contract-keeper` | 为开发铺路,5 层命名一致 |
| L7 文档 | `technical-writer` | 概念稳定后出设计 .md |

铁律:**字段对照表(L3)必须先于代码 commit**(§M.2);原型指不出的字段**不进 L3**,回 L1 走 §M.1。

**M.9.2 编排总管（MUST）** — 满足下列任一,Claude **必须**先走 `product-orchestrator`(出漏斗计划+DAG)再分派,不许散乱手设计:
- 产品设计任务跨 ≥ 3 个子 agent
- 用户说"这模块怎么设计 / 加 XX 需求 / Phase 02 准入 / PRD 对得上吗 / 需求拆一下"
- 子 agent **不能再 spawn 子 agent**:总管出 DAG,主 Claude 按序调。

**M.9.3 设计就绪 Gate 裁决（MUST）** — 判"设计就绪 / 可进开发"(Phase 02→03 准入)的充要条件:**可追溯**(每个字段/状态/错误码/文案指得出 PRD § + 原型元素) · **字段对照表先行**(§M.2) · **状态合法**(只来自 PRD §3.2 / 原型徽章类 / PRD-MAPPING §3,§M.4) · **错误码登记**(§M.5) · **原型保真**(无 §N 违规) · **三者一致**(PRD/原型/MAPPING)。任一不满足 = **驳回**,指明回哪个 agent 修,**禁**"先开发着回头补设计"、**禁**凭直觉补字段(§M.1)。

**M.9.4 自进化（MUST）** — 每轮产品设计编排收口必须沉淀产品设计 signals(见 [signals 产品设计编排段](../99-跨阶段/signals/README.md)):`prd_drift_count`、`prototype_deviation_count`、`untraceable_field_count`(应趋 0)、`field_table_lag`(字段表晚于代码 commit,应=0)、`prd_change_via_proposal`(PRD 演化正确走 proposal 的占比)。触发提案:同类 PRD drift 月 ≥3 → PRD-MAPPING 补全提案;某类字段反复指不出原型 → 原型补画提案;§N 违规集中某子项 → UED 加 hook 提案。详见 [产品设计工作流.md](../99-跨阶段/产品设计工作流.md)。

## N. GitHub 工作流（MUST — 涉及 PR / Issue / Release 时）

### N.1 `gh` CLI 使用前提

在执行任何 `gh` 命令前，先检查：

```bash
gh auth status
```

若未登录，先提示用户执行 `gh auth login`，**不要**假设已认证。  
若 `gh` 未安装，输出安装命令：`winget install --id GitHub.cli`（Windows）。

### N.2 PR 创建（MUST）

帮用户创建 PR 时，**必须**：
1. 确认当前分支不是 `main`/`release/*`
2. 确认分支已 push 到 remote（`git status` 检查 upstream）
3. 使用 `gh pr create` 并**带完整 body**（从 `.github/PULL_REQUEST_TEMPLATE.md` 读取框架）
4. PR title 必须符合 Conventional Commits 格式
5. 若 PR 关联 Issue，在 body 写 `Closes #<n>`

不要只给用户"你可以运行 gh pr create"，要主动生成完整命令。

### N.3 Issue 创建（MUST）

帮用户创建 Issue 时：
- Bug → 使用 `bug` label
- 功能需求 → 使用 `enhancement` label  
- 必须让用户知道 Issue 编号，便于后续 `gh issue develop <n>`

### N.4 CI 状态检查（SHOULD）

push 或 PR 后，如用户关心 CI 结果：

```bash
gh run list --limit 5
gh run watch <run-id>   # 实时等待
```

失败时用 `gh run view <run-id> --log-failed` 定位原因，不要让用户去浏览器翻。

### N.5 高危 GitHub 操作（MUST 确认）

以下操作**必须**先用 AskUserQuestion 确认：
- `gh release create` — 会公开发布，影响所有用户
- `gh repo edit --visibility` — 改仓库可见性
- `gh api ... -X DELETE` — 删除远端资源
- 修改分支保护规则（`gh api .../protection`）

### N.6 不可绕过的平台限制（MUST）

- 不要帮用户绕过 GitHub 的分支保护（如用 API 直接推 main）
- 不要帮用户删除已合并 PR 对应的 commit 历史
- 不要帮用户修改已发布 Release 的 tag（需要先在对话里确认理由）

---

## O. 并行 Session 协作（MUST）

> **编号让位备注**：本章节原编号 § N（"并行 Session 协作"），与 main 上 commit `75f59b4` 加的 § N（"GitHub 工作流"）撞号。按 [99-跨阶段/协作规范.md §5 编号防撞](../99-跨阶段/协作规范.md) "后到者让位"原则，本节让位为 § O。

多 session（多 Claude / 多人 / 人机混合）同时在本仓库工作时，遵守 [99-跨阶段/协作规范.md](../99-跨阶段/协作规范.md) 全文。Claude 视角额外硬条款：

- **O.1** 启动即报到：第一轮响应前必跑 `git worktree list && git status && git log --oneline -3`，看清自己在哪、相对 main 偏离多少
- **O.2** 写规范前查 §L.2：要改任何 SSoT 文件（详见 [协作规范.md §2](../99-跨阶段/协作规范.md) 列表）→ 先开 proposal，不直接动；Claude 不允许"自批自审"自己刚写的 proposal 后立刻动 SSoT，必须用户 AskUserQuestion 明确授权
- **O.3** 任务上台账：开工前先在 [99-跨阶段/在途任务.md](../99-跨阶段/在途任务.md) "进行中"加一行；状态变化原地改
- **O.4** 同模块串行：任一业务模块同一时刻只允许一个 session 改代码 + SQL（详见 [协作规范.md §4](../99-跨阶段/协作规范.md)）
- **O.5** 见冲突即停：[协作规范.md §8](../99-跨阶段/协作规范.md) "停下 AskUserQuestion"场景禁止自作主张取舍；**禁止** `git push --force` 覆盖他人工作
- **O.6** 跨 session 边界：其他 session 的 worktree / 分支视作 §B 不可触碰区扩展（不 cd 进入、不 edit、不 git 操作）
- **O.7** Push 前显式 refspec：worktree 默认上游可能指向"父分支"（见 ruoyi-bootstrap skill gotchas §6），首次 push 必走 `git push -u origin <branch>:<branch>`，确认 `git rev-parse --abbrev-ref @{u}` 等于 `origin/<本地分支>`

本章节是协作规范的强制摘要，详尽规则与示例在 [协作规范.md](../99-跨阶段/协作规范.md)。

---

## P. 前端 TypeScript 类型声明约束（MUST — 防 .d.ts 模块污染)

**血泪背景**:2026-05-20 在 [feat/ai-multi-provider-v1-v3](https://) 分支跑 `npx vue-tsc --noEmit`,227 个错误,其中 130+ 个是 `TS2305: Module '"vue"' has no exported member 'ref/reactive/computed/onMounted/createApp'`,另 74 个是派生 `TS7006: implicit any`。两次错误方向(怀疑 auto-imports.d.ts、怀疑 vue.d.mts 损坏)都不是真凶。真凶是 [src/types/global.d.ts](../plm-frontend/src/types/global.d.ts) 里这段:

```ts
import type { DefineComponent } from 'vue'         // type-only,不让文件成为 module
// ...
declare module 'vue' {                              // 因为文件是 ambient script
  interface ComponentInternalInstance { proxy: any }   // 这段变成"重新声明 vue 模块"
}                                                   // 而不是 augmentation,覆盖掉 ref/reactive 等
```

TS 5.x + `moduleResolution: bundler` 下,**`import type` 不足以让一个 `.d.ts` 文件成为 module**。文件被判为 ambient global script 时,`declare module 'X' { ... }` 是**重新声明**而非 augmentation,会把目标模块的真实 named exports 全部覆盖。

详尽根因诊断与修复路径见 [memory/project-quirks.md Q-CODE-03](../memory/project-quirks.md) + [memory/frontend_ts_vue_augmentation.md](../memory/frontend_ts_vue_augmentation.md)。

### P.1 .d.ts 文件分工(MUST)

PLM 前端 `plm-frontend/src/types/` 下的 `.d.ts` 文件**必须**按下表分工,**禁止**混合:

| 用途 | 文件 | 文件头要求 | 内容只允许 |
|---|---|---|---|
| **Module augmentation**(为已有的 module 补字段 / interface 合并) | `global.d.ts` / `components.d.ts` 等 | 顶部必须有 `export {}` 或真实顶层 `import/export`(非 `import type`) | `declare module 'X' { ... }` 给已存在的 module 加 member;`declare global { interface Y { ... } }` 给全局接口加字段 |
| **Ambient shim**(声明"通配模式" / 没 d.ts 的三方库) | `shims-vue.d.ts` / `shims-*.d.ts` | 顶部**不得**有任何 `import`/`export`(包括 `import type`) | `declare module '*.vue' { ... }`;`declare module 'js-cookie' { ... }`(为没 .d.ts 的库声明类型) |

**判定红线**:看 .d.ts 文件第 1 行——
- 出现 `import type { X } from 'Y'` **或** `import` **或** `export` → 这是 module,只能放 augmentation
- 没有任何顶层 import/export → 这是 ambient,只能放"声明一个新模块/通配"

混合违规的典型症状:`Module '"vue"' has no exported member 'ref'`(全 program 同时报)。

### P.2 改动 .d.ts 前必查(MUST)

修改 / 新建 `src/types/*.d.ts` 前必须:

1. `grep -nE "^(import|export)" <file>` 看文件类型(module / ambient)
2. 如果要加 `declare module 'X' { ... }`:
   - augment 已有 module(如 `vue`、`pinia`、`element-plus`) → 放进 module 文件
   - 声明新 module(三方库无 d.ts) → 放进 ambient 文件
3. 改完后跑 `npx vue-tsc --noEmit 2>&1 | grep -E "TS2305.*Module .'vue'" | wc -l`,**必须 = 0**

### P.3 自验最小测试(MUST — 调 .d.ts 后必跑)

修改任何 `src/types/*.d.ts` 后,临时写一个 `src/__type-check.ts`:

```ts
import { ref, reactive, computed, onMounted, watch, createApp } from 'vue'
export const _ = { ref, reactive, computed, onMounted, watch, createApp }
```

跑 `npx vue-tsc --noEmit -p tsconfig.json 2>&1 | grep "__type-check"`。**必须零输出**,否则说明 vue 模块又被污染。验证完删除该文件。

### P.4 与 unplugin-auto-import 的边界(MUST)

`plm-frontend/auto-imports.d.ts` 是 **generated file**,**禁止**手工编辑。该文件末尾的 `declare global { export type { Component, ... } from 'vue' }` 是 unplugin-auto-import 0.18.x 的默认输出,看起来可疑但不是 vue 报错的根因(2026-05-20 验证:删除后错误数不变)。

如果将来该文件被证实有害,改 [vite/plugins/auto-import.ts](../plm-frontend/vite/plugins/auto-import.ts) 配置控制其生成行为,而不是后修生成产物。

### P.5 开发规范契约同步(MUST)

[03-开发/开发规范.md §2.4](../03-开发/开发规范.md) 明文要求 `types/api/common.ts` 含 `BaseEntity` 与 `PageQuery`。**任何对 `types/api/common.ts` 的删减必须同步更新开发规范.md**;反之,开发规范.md 列出的导出必须在 common.ts 中存在(2026-05-20 发现 13 个 packages 报错就是因为 common.ts 只有 `PageDomain` 没有 `PageQuery`,通过加 `export type PageQuery = PageDomain` 别名修复)。

---

## N. UED / 前端视觉约束（MUST — 与 §M PRD 驱动联动）

本节是 [02-设计/UED规范.md](../02-设计/UED规范.md) 的 Claude 执行摘要。规范全文以 UED规范.md 为准，本节只列"Claude 必须执行的强制项"。

### N.1 颜色 Token（MUST）

- 前端 CSS / Vue `<style>` **禁止写裸十六进制颜色**（如 `#2d7a4f`），必须用 `var(--gp)` 等变量。
- 唯一例外：`agriplm.css :root` 中 Token 本身的定义行，以及与 Token 无关的纯黑白透明叠加（`rgba(0,0,0,...)` 等）。
- 新增颜色变量 → 先在 UED规范.md §1.1 登记，再用；不允许在组件里临时创建匿名颜色。

### N.2 状态徽章颜色（MUST）

任何状态字段（status / level / priority）的展示，**必须**用 UED规范.md §5.2 的标准徽章类（`.b.bg / .bam / .brd / .bbl / .bgr / .bai`），对应颜色与 PRD-MAPPING.md §3 状态机保持一致：

- `草稿` → `.bgr`　`待评审/评审中` → `.bam`　`已确认/已完成` → `.bg`　`失败/拒绝` → `.brd`　`AI生成` → `.bai`
- 禁止自定义 `background-color` 做状态显示（会绕过 Token 和无障碍对比度规则）。

### N.3 AI 按钮区分（MUST）

- 触发 AI 工作流的按钮 **必须** 用 `.btn-ai`（紫蓝渐变）+ `✨` 前缀。
- 普通保存 / 确认 / 取消按钮 **禁止** 用 `.btn-ai`，用 `.btn-p` / `.btn-s`。
- 判断依据：按钮点击后是否调用 AI 接口 → 是则必须 `.btn-ai`，否则禁止。

### N.4 表单 Label（MUST）

- 每个 `<input> / <select> / <textarea>` **必须** 有对应 `<label>`（或 `aria-label`）。
- 不允许用 `placeholder` 代替 label（`placeholder` 在 focus 后消失，违反无障碍）。
- 必填字段：label 末尾加 ` *`（文字），不用红色星号。

### N.5 空态 / 加载态 / 错误态（MUST）

写前端组件时，以下三个状态必须同时实现（不允许只写正常数据态）：

| 状态 | 实现方式 |
|---|---|
| 空数据 | 居中 icon + 文字（如 `📭 暂无数据`），不留白 |
| 加载中 | 表格/列表用骨架屏；AI 生成中按钮用 `.dot-anim` |
| 错误 | 通知 `.notif-item.err` + 保持原有内容（不清空） |

### N.6 间距合规（SHOULD）

新增间距值必须是 4px 的倍数（4 / 8 / 12 / 14 / 16 / 18 / 20 / 24px）。奇数或非 4 倍数间距（如 3/5/7/9/11/13/15px）需有原型精确标注依据。

### N.7 模态框约束（MUST）

- 模态框 **禁止嵌套**（Modal 内不弹 Modal）。
- 操作按钮行：主操作在左，`取消` 在右，危险操作（删除）用 `.btn-rd.btn-sm` 放在主操作右侧。
- 模态框必须有关闭按钮（`.mclose`）且支持点击遮罩关闭。

### N.8 UED CR 准入（SHOULD）

PR 涉及 UI 改动时，PR 描述中包含 UED规范.md §13 的 8 项自检 Checklist。Claude 提交包含 UI 变更的 commit 前，主动过一遍该 Checklist。

### N.9 新组件/新 CSS 类（SHOULD）

现有 CSS 类无法覆盖新需求时：先检查 UED规范.md §附录A；没有合适类时，先在 UED规范.md 对应章节注册（先改规范，再写代码），新类命名遵循 `agriplm.css` 缩写风格。

---

## 索引：相关规则文件

| 文件 | 受众 | 强制层 |
|---|---|---|
| [本文件 .claude/rules.md](rules.md) | Claude 自动加载 | 软（Claude 自觉） |
| 根 [CLAUDE.md](../CLAUDE.md) | Claude + 人类（开仓库即看） | 软 |
| [03-开发/开发规范.md](../03-开发/开发规范.md) | 人类开发者 | 软（CR 时检查） |
| [02-设计/UED规范.md](../02-设计/UED规范.md) | 设计师 + 前端 + Claude | 软（CR 时检查） |
| [99-跨阶段/模块工作流.md](../99-跨阶段/模块工作流.md) | 人类 + Claude | 软（评审卡控） |
| [.editorconfig](../.editorconfig) | 编辑器 | 硬（自动应用） |
| [.githooks/commit-msg](../.githooks/commit-msg) | git | 硬（commit 时拒绝） |
| [.githooks/pre-push](../.githooks/pre-push) | git | 硬（push 时拒绝非规范分支/保护分支直推） |
| [.github/workflows/ci.yml](../.github/workflows/ci.yml) | GitHub Actions | 硬（PR 必须 CI Gate 绿） |

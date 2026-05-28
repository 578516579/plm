# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

PLM (项目全生命周期管理 / Project Lifecycle Management) — a Spring Boot 4 + Vue 3 application bootstrapped from RuoYi (若依) v3.9.2. A P0 rename has already converted the upstream `com.ruoyi.*` scaffold into a properly-namespaced project (see commit `2679a61`). **No PLM business modules have been built yet** — the codebase is the renamed scaffold plus standard admin modules (user/role/menu/dict/etc.).

- **Java package root**: `cn.com.bosssfot.dv.plm`
- **Module prefix**: `plm-` (the 6 Maven modules: `plm-admin / plm-common / plm-framework / plm-generator / plm-quartz / plm-system`)
- **Maven artifact**: `cn.com.bosssfot.dv.plm:plm:3.9.2`
- **Schema name**: `plm` (not the upstream `ry-vue`)
- **Display name in UI / banner / app config prefix**: `PLM` / `plm:`

## Stack

| Layer | Tech |
|---|---|
| Backend | Spring Boot 4.0.3, JDK 17, MyBatis (mybatis-spring-boot 4.0.1), Druid 1.2.28, Spring Security + JWT (jjwt 0.9.1), Redis (Lettuce), Quartz, springdoc-openapi 3.0.2, fastjson2 |
| Frontend | Vue 3.5, Vite 6.4, TypeScript 5.6, Element Plus 2.13, Pinia, vue-router 4, ECharts, axios |
| DB | MySQL 8.x, schema `plm`, charset `utf8mb4` |

## Running locally

There are **4 environment-specific gotchas** documented in [memory/local_run_howto.md](.claude/projects/D---12-trae--06-----------plm/memory/local_run_howto.md) — read it before debugging startup issues. The commands below have those gotchas baked in.

```bash
# === 1. Database (first time / reset) ===
MYSQL='/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'   # adjust per machine
"$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 -e \
  "DROP DATABASE IF EXISTS plm; CREATE DATABASE plm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
cd plm-backend
"$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm < sql/ry_20260417.sql   # ⚠ MUST pass --default-character-set
"$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm < sql/quartz.sql
# Verify: 31 tables, sys_user shows admin/若依 and ry/若依 with correct CJK.

# --- Business modules + menu regroup (run in order; idempotent) ---
for f in sql/business-*.sql; do
  "$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm < "$f"
done
"$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm < sql/menu-regroup-by-phase.sql   # 8 阶段分组重组
"$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm < sql/menu-fill-missing-8.sql     # 补 8 个 PRD-aligned 缺菜单
# Verify: 10 可见一级目录 (2400/2500 + 2900-2970), 旧 2000 业务管理 visible=1
# Rollback: 对应 *-rollback.sql 反向逆操作

# === 2. Backend build (Maven needs JDK 17, NOT system default) ===
export JAVA_HOME="/c/Program Files/Microsoft/jdk-17.0.18.8-hotspot"   # adjust per machine
cd plm-backend
mvn clean install -DskipTests -T 4 --no-transfer-progress

# === 3. Backend run ===
export JAVA_HOME="/c/Program Files/Microsoft/jdk-17.0.18.8-hotspot"
export DB_PASSWORD='...'        # required, no safe default
export REDIS_HOST=127.0.0.1     # ⚠ do NOT use 'localhost' on Windows + Java 17 (IPv6 trap)
cd plm-backend
java -jar plm-admin/target/plm-admin.jar --server.port=8081
# Use --server.port=8080 if 8080 isn't taken (Apache httpd often holds it on Windows dev).
# Expect "Started PlmApplication in NN seconds".

# === 4. Frontend ===
cd plm-frontend
npm install --registry=https://registry.npmmirror.com   # first time only
export VITE_BACKEND_URL=http://localhost:8081           # match backend port
npm run dev                                              # serves on :80

# === 5. End-to-end smoke test ===
curl -s http://localhost/dev-api/captchaImage | head -c 200
# Expect: {"msg":"操作成功","code":200,"captchaEnabled":true,"uuid":"..."}
```

Default admin login: `admin / admin123`. Druid console: http://localhost:8081/druid (`plm/123456` unless overridden by `DRUID_USERNAME`/`DRUID_PASSWORD`).

## Secrets — environment-variable contract

All sensitive yml values are wrapped in `${VAR:default}` placeholders. The default values let local dev run zero-config; **production must inject real values**. The full variable list lives in [plm-backend/.env.example](plm-backend/.env.example).

Spring Boot does not auto-read `.env` files. Inject via IDE run config, shell `export`, docker-compose `env_file:`, or K8s Secret. The `.env` file itself is gitignored (see root `.gitignore`).

The 9 gotchas, one-liner(完整 quirks 知识库在 [memory/project-quirks.md](memory/project-quirks.md)):

| # | Symptom | Fix | 复发 |
|---|---|---|---|
| 1 | `mvn compile` fails: source 17 unsupported | `export JAVA_HOME=<JDK 17 path>` | 4+ |
| 2 | SQL import fails: `Data too long for 'dept_name'` | `mysql --default-character-set=utf8mb4 ...` | 2 |
| 3 | Backend hangs: `LettuceConnection: Command timed out` | `export REDIS_HOST=127.0.0.1` (not `localhost`) | 3+ |
| 4 | `Failed to resolve import @/utils/ruoyi` on certain frontend pages | `vite/plugins/auto-import.ts` was missed during rename — re-run sed there | 1 |
| 5 | `mvn install` fails: `Unable to rename '.../plm-admin.jar' to '.../plm-admin.jar.original'` | Backend 在跑锁住 jar — 先 `taskkill //PID <pid> //F` 再 build。从 V2 quirks (Q-BUILD-02) promote, 复发 9+ | 9+ |
| 6 | Backend 抛 ServiceException 字符串但 grep 当前代码无此字符串 | Stale JVM 进程加载旧字节码(切 branch 后没重启)— 对比 `ls -la <jar>` mtime vs `wmic process get CreationDate` startTime,kill + rebuild | 1 |
| 7 | `business-*.sql` 漏写 `sys_menu` INSERT → 前端无菜单入口、功能不可达 | `.githooks/pre-commit` lint 1 自动拦截(缺 `INSERT INTO sys_menu`);仅扩字典/子表的脚本顶部加 `-- @no-menu: <原因>` 豁免。Q-DB-04,首例 81bc1ba | 1+ |
| 8 | `sys_menu` path 列改动后,前端 `/business/<entity>` 按钮 404 大面积复发 | `.githooks/pre-commit` lint 2 自动 grep `plm-frontend/src/views/**` 给清单;跳转必须经 `src/utils/businessRoute.ts` SSoT,**禁止硬编码 `router.push('/business/...)`**。Q-BIZ-04,首例 5c4e70d+7b14807 | 2+ |
| 9 | 并行 session 在共享 working tree 里用 `git add . / -A / -u / commit -a` → 偷别 session 已 staged 但未 commit 的文件,commit msg 与实际改动失配 | `.claude/hooks/session-guard.sh` (proposal 0030 升级)bulk add + dirty>=1 → exit 2 硬拦;合法 bulk 走 `export CLAUDE_BULK_OK="<≥10字 reason>"` 后门;紧急绕过 `export CLAUDE_BYPASS_SESSION_GUARD=1`(计入 signals bypass)。**永远显式 `git add file1 file2 ...`**。Q-COLLAB-01,首例 3ae00fd+656a6a4 单日 2 次 | 2 |

## Architecture (where things live)

Standard RuoYi layered architecture with `plm-`-prefixed module names. The dependency graph is:

```
plm-admin  (entry point: PlmApplication; Controllers in cn.com.bosssfot.dv.plm.web.controller)
   ↓ depends on
plm-framework  (Spring Security config, AOP, data-scope filters, JWT filter)
   ↓ depends on
plm-system  (sys_* domain/mapper/service for user/role/menu/dept/dict/config/notice)
   ↓ depends on
plm-common  (BaseController, BaseEntity, annotations, common utils, PlmConfig)

plm-quartz      depends on plm-common (scheduled-job sys_job/sys_job_log)
plm-generator   depends on plm-framework (code generator using gen_table/gen_table_column + Velocity .vm templates)
```

Per module: each business package follows `domain/`, `mapper/` (Java interface + XML in `src/main/resources/mapper/`), `service/` (interface + `impl/`), and Controllers in `plm-admin/web/controller/<area>/`.

Key boundary configs:
- Spring config prefix for `PlmConfig` is `plm:` in [plm-admin/src/main/resources/application.yml](plm-backend/plm-admin/src/main/resources/application.yml) (was `ruoyi:` upstream).
- DataSource is single-master Druid by default; slave is wired but disabled. See [application-druid.yml](plm-backend/plm-admin/src/main/resources/application-druid.yml).
- Token header is `Authorization` (Bearer JWT), validated by `JwtAuthenticationTokenFilter` in `plm-framework`.
- Permission strings use the standard RuoYi format: `@PreAuthorize("@ss.hasPermi('system:user:add')")`. New business modules should follow `business:<entity>:<action>`.
- Frontend dev proxy: `/dev-api/*` → `${VITE_BACKEND_URL}` (default `http://localhost:8080`). See [vite.config.ts](plm-frontend/vite.config.ts).

## Frontend specifics

- Page title comes from `VITE_APP_TITLE` in [`.env.development`/`.env.staging`/`.env.production`](plm-frontend/.env.development).
- `auto-import` plugin in [vite/plugins/auto-import.ts](plm-frontend/vite/plugins/auto-import.ts) injects `selectDictLabel` from `@/utils/plm` (renamed from upstream `@/utils/ruoyi`). When changing utility-file names, sync this plugin or you'll hit gotcha #4.
- Generated declarations: `auto-imports.d.ts` is autogenerated by `unplugin-auto-import`. Don't hand-edit.

## Intentionally preserved upstream artifacts

The P0 rename deliberately left these alone — **do NOT mass-rewrite them without asking**:

- `@author ruoyi` Javadoc and `Copyright (c) ruoyi` comments → framework attribution.
- SQL seed data in [sql/ry_20260417.sql](plm-backend/sql/ry_20260417.sql) containing `若依` strings (sys_dept "若依科技", sys_user nickName, sys_menu "若依官网", sys_notice 3 公告) → these are demo records meant to be replaced wholesale during PLM business modeling, not piecemeal-edited.
- `README.md`, `LICENSE`, `doc/若依环境使用手册.docx` → upstream framework docs, retained for future upgrades.
- Code-generator Velocity templates in [plm-generator/src/main/resources/vm/](plm-backend/plm-generator/src/main/resources/vm/) → content already updated by the rename; structure preserved so the in-app generator at `/tool/gen` still works.

## Available skills

**`ruoyi-bootstrap`** — A skill is installed at `~/.claude/skills/ruoyi-bootstrap/` that automates the entire RuoYi-scaffold-to-named-project flow (the very flow that built this repo's commits `4e9777d → 2679a61 → e2e37c3`). It also ships a `Project` CRUD module template that can be dropped in via Phase 7. Trigger it on future fresh-RuoYi imports by saying "用若依新建项目", "把若依改名", or similar.

**`plm-module-uplift`** — 固化「业务模块 🟡 空壳 → 🟢 PRD-aligned」SOP(proposal 0015;2026-05-25 单日 6 模块同流程显形)。装在 `~/.claude/skills/plm-module-uplift/`,模板从真实 🟢 模块(apidesign / ued)抽取。触发词:「把 X 模块从 🟡 改成 🟢」「🟡→🟢 落地」「uplift module」或直接说 🟡 模块名(manual-impl / manual-ops / analytics / ai-agent / openspec / pipeline / feature-flag / dora)。**只搭骨架,业务逻辑仍要人写**。与 ruoyi-bootstrap 区分:后者脚手架→正名(P0 一次性),本 skill 空壳→PRD-aligned(N 次复用)。

**`integration-connector`** — 固化「接入第三方系统做数据同步(集成连接器)」SOP(proposal 0019;飞书/GitLab/禅道第 3 次同范式)。装在 `~/.claude/skills/integration-connector/`,模板从真实禅道集成(commit 9d37d03)抽取:ConnectorAdapter + WebhookController + Inbound/OutboundSyncService(防回环三道防线,见 [Q-INTEG-01](memory/project-quirks.md))+ 业务 Event 钩子 + 设计文档模板。触发词:「接入 XX 第三方系统做同步」「双向同步 <系统名>」「integrate <system>」。与 plm-module-uplift **正交**(那个造业务模块,本 skill 接外部系统)。

## 🎯 PRD/原型驱动开发 — 单一事实来源 (SSoT)

本仓库是 **AgriPLM·AI** 的实现 (PRD V1.0 in `prd和原型/AgriAI-PLM-完整PRD文档.md`, 31 个原型 HTML in `prd和原型/AgriPLM-DevOps-原型/agriplm_split/*.html`)。

**所有业务字段、状态机、错误码、URL、菜单文案,必须能追溯到 PRD § + 原型 HTML 表单元素**。规范化的对照表是 [PRD-MAPPING.md](PRD-MAPPING.md) (项目根)。

工作流:
1. 收到「加 XX 模块 / 改 YY 字段」请求 → **先查** [PRD-MAPPING.md](PRD-MAPPING.md)
2. 用户要的东西不在 PRD/原型里 → 停下来 AskUserQuestion 让用户选 (按 PRD 走 / 走 proposal 改 PRD-MAPPING.md)
3. PRD-align 落地走 [PRD-MAPPING.md §8 9 项 DoD](PRD-MAPPING.md)
4. 详细硬规则见 [.claude/rules.md §M](.claude/rules.md)

**实现进度速览** (详 [PRD-MAPPING.md §1](PRD-MAPPING.md)):
- 🟢 PRD-aligned **31 个(全部业务模块)**: 规划 inception/project/competitive/dashboard · 需求设计 requirement/prd/ued/arch/dbdesign/apidesign · 研发 sprint/task/document · 测试 testplan/testcase/testdata/submission/autotest/defect/testreport · 交付运维 apidoc/manual-product/manual-impl/manual-ops/pipeline/release/feature-flag/dora · 分析 analytics · AI openspec/ai-agent
- 🟡 空壳 **0 个**（2026-05-27 末批 8 模块 manual-impl/manual-ops/analytics/ai-agent/openspec/pipeline/feature-flag/dora 全部 🟢，单测 200 case 全绿）
- 🔴 缺模块 0 个
- 🆕 v0.x: MCP / Integration（Proposal 0007，真实接入中）

## Rules & playbooks

Three layers of project-wide rules — read these before non-trivial work:

| Layer | Where | Audience |
|---|---|---|
| Claude hard constraints (auto-loaded) | [.claude/rules.md](.claude/rules.md) | Claude — naming, untouchable zones, secret handling, gotchas reminders, commit format, **Gate enforcement (§G)** |
| Human dev standards | [03-开发/开发规范.md](03-开发/开发规范.md) | Engineers — naming/coding/SQL/PR/test/security rules with examples |
| Module lifecycle overview | [99-跨阶段/模块工作流.md](99-跨阶段/模块工作流.md) | All roles — Phase 01 → 06 entry/exit conditions, DoD, gate reviews |
| **🤝 Parallel session collaboration** | [99-跨阶段/协作规范.md](99-跨阶段/协作规范.md) + [99-跨阶段/在途任务.md](99-跨阶段/在途任务.md) | Multi-worktree / multi-session scenarios — **§0-§12** worktree isolation / SSoT resource lock / task ledger / conflict handling; **§13-§18** role matrix / sync cadence (daily/weekly/monthly/quarterly) / escalation L0-L4 / on-boarding & hand-off / rollback playbook / Claude-Claude protocol. **Required reading** when running parallel Claude sessions. |
| **🚦 Hard Gate (mandatory)** | [99-跨阶段/gate-checklists/](99-跨阶段/gate-checklists/) | All roles — **6 copy-and-fill Checklist templates**; instance files in `instances/<module>/` are the audit trail. Without a signed checklist commit, **the module is NOT allowed to advance to the next phase**. Triage by L1/L2/L3 (see Checklists README). |

Tool-enforced:
- [.editorconfig](.editorconfig) — auto-applied indent/charset/EOL by editors
- [.githooks/commit-msg](.githooks/commit-msg) — Conventional Commits validation. **First-time setup per clone**: `git config core.hooksPath .githooks`
- [.githooks/pre-commit](.githooks/pre-commit) — `business-*.sql` 模板 lint(必含 `sys_menu` INSERT,否则加 `-- @no-menu` 豁免)+ `sys_menu` path 改动→前端硬编码 `/business/` grep。proposal 0016 落地。绕过:`git commit --no-verify`(计入 signals)。
- [.githooks/pre-push](.githooks/pre-push) — 分支名 `<type>/<desc>` 校验 + 禁止直推 `main`/`release/*`。
- [.claude/settings.json](.claude/settings.json) — Claude Code hooks (Stop / PreToolUse / UserPromptSubmit) for runtime reminders. Design + troubleshooting in [.claude/hooks-design.md](.claude/hooks-design.md).

## Self-evolution loop

The repo runs a closed feedback loop on its own rules. Read [.claude/rules.md §L](.claude/rules.md) before working in this area.

```
signals/       ← objective monthly data (commit bypass / Gate skips / Phase duration / bug recurrence / Claude block&override)
reflect/       ← weekly/monthly/quarterly reports — observations → diagnoses → actions
proposals/     ← formal change requests with evidence → review → merge → tracking
   ↓
rules + workflow + Gate templates EVOLVE  ← rule docs are versioned code, not stone tablets
```

Key entry points:
- [99-跨阶段/signals/README.md](99-跨阶段/signals/README.md) — what gets measured
- [99-跨阶段/reflect/README.md](99-跨阶段/reflect/README.md) — how reflection runs (weekly/monthly/quarterly)
- [99-跨阶段/proposals/README.md](99-跨阶段/proposals/README.md) — how a rule change actually lands

Roadmap (current = Phase A complete):
- ✅ Phase A — passive substrate: directories, templates, Stop/PreToolUse hooks
- ⏳ Phase B — `/reflect-weekly` & `/reflect-monthly` skills (auto-generate reports)
- ⏳ Phase C — `/proposal` skill (one-command proposal lifecycle)
- ⏳ Phase D — metrics-driven rule tuning (auto-suggest MUST↔SHOULD downgrades)

## Memory references

Project-specific context lives under `.claude/projects/.../memory/`:
- `project_status.md` — what's done / what's not
- `local_run_howto.md` — toolchain paths on this machine + the 4 gotchas in detail
- `ruoyi_bootstrap_skill.md` — pointer to the skill above

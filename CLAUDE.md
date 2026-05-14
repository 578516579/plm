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

The 4 gotchas, one-liner:

| # | Symptom | Fix |
|---|---|---|
| 1 | `mvn compile` fails: source 17 unsupported | `export JAVA_HOME=<JDK 17 path>` |
| 2 | SQL import fails: `Data too long for 'dept_name'` | `mysql --default-character-set=utf8mb4 ...` |
| 3 | Backend hangs: `LettuceConnection: Command timed out` | `export REDIS_HOST=127.0.0.1` (not `localhost`) |
| 4 | `Failed to resolve import @/utils/ruoyi` on certain frontend pages | `vite/plugins/auto-import.ts` was missed during rename — re-run sed there |

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

## Available skill: `ruoyi-bootstrap`

A skill is installed at `~/.claude/skills/ruoyi-bootstrap/` that automates the entire RuoYi-scaffold-to-named-project flow (the very flow that built this repo's commits `4e9777d → 2679a61 → e2e37c3`). It also ships a `Project` CRUD module template that can be dropped in via Phase 7. Trigger it on future fresh-RuoYi imports by saying "用若依新建项目", "把若依改名", or similar.

## Memory references

Project-specific context lives under `.claude/projects/.../memory/`:
- `project_status.md` — what's done / what's not
- `local_run_howto.md` — toolchain paths on this machine + the 4 gotchas in detail
- `ruoyi_bootstrap_skill.md` — pointer to the skill above

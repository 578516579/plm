# PLM 项目 quirks 知识库

> 维护: context-memory Agent
> 用途: 跨会话记忆项目特有的"坑/约定/历史决策",避免重复踩雷
> 不重复: CLAUDE.md 说"做什么/不做什么";本文说"为什么 + 现象 + 修复"
> 沉淀: 每个 quirk Q-<类>-NN 格式,带复发次数和首次发现 commit

---

## 环境层 (ENV)

### Q-ENV-01 — Redis 不能用 localhost

| 字段 | 内容 |
|---|---|
| 现象 | backend 启动后 `LettuceConnection: Command timed out` |
| 根因 | Windows + Java 17 IPv6 优先,Lettuce 长连接走 `::1`,但 Redis 默认 bind `127.0.0.1`(IPv4) |
| 修复 | `export REDIS_HOST=127.0.0.1`(显式 IPv4) |
| 首次发现 | CLAUDE.md gotcha #3 |
| 复发次数 | 3+(每次新机器搭建都踩) |

### Q-ENV-02 — MySQL 导入字符集

| 字段 | 内容 |
|---|---|
| 现象 | `ERROR 1406: Data too long for 'dept_name'` |
| 根因 | MySQL client 默认 latin1,把汉字转成多字节,超长字段拒绝 |
| 修复 | `mysql --default-character-set=utf8mb4 ... plm < sql/xxx.sql` |
| 首次发现 | CLAUDE.md gotcha #2 |
| 复发次数 | 2 |

### Q-ENV-03 — JDK 默认不是 17

| 字段 | 内容 |
|---|---|
| 现象 | `mvn compile` 报 `source 17 unsupported` |
| 根因 | 系统默认 JDK 可能是 11 / 21,但项目要求 17 |
| 修复 | `export JAVA_HOME='/c/Program Files/Microsoft/jdk-17.0.18.8-hotspot'` |
| 复发次数 | 4+(每个新 shell 都需要) |

### Q-ENV-04 — C 盘满启动失败

| 字段 | 内容 |
|---|---|
| 现象 | `Failed to write to ...AppData\Local\Temp\...` / 启动超时 |
| 根因 | Windows JVM 默认临时目录在 C 盘 `AppData\Local\Temp`,大型项目临时文件溢出 |
| 修复 | `setx JAVA_TOOL_OPTIONS "-Djava.io.tmpdir=D:/tmp"` + 同步 `MAVEN_OPTS`/`TMP`/`TEMP` |
| 复发次数 | 1(已永久解决) |

---

## 构建层 (BUILD)

### Q-BUILD-01 — vite 新 view 必须重启 dev

| 字段 | 内容 |
|---|---|
| 现象 | 新增 `views/business/xxx/index.vue` 后,浏览器一直 "正在加载系统资源,请耐心等待" |
| 根因 | `import.meta.glob('./../../views/**/*.vue')` 是**启动时静态扫描的常量**,vite HMR 不更新 |
| 修复 | 重启 `npm run dev`(干掉 :80 进程再启) |
| 首次发现 | commit ac01b2f(AI 调用审计 view 加入后) |
| 复发次数 | 2 |

### Q-BUILD-02 — mvn install 失败 jar 锁

| 字段 | 内容 |
|---|---|
| 现象 | `Unable to rename '.../plm-admin.jar' to '.../plm-admin.jar.original'` |
| 根因 | backend 在运行,JVM 锁住 jar,spring-boot-maven-plugin 不能 repackage |
| 修复 | 1) `netstat -ano \| grep ":8081 "` 找 PID 2) `taskkill //PID $PID //F` 3) `sleep 3` 4) `mvn install` |
| 复发次数 | 9+(每次后端改 java 代码都遇到) |

### Q-BUILD-03 — npm run build 命令名

| 字段 | 内容 |
|---|---|
| 现象 | `npm error Missing script: "build"` |
| 根因 | 项目 package.json 只有 `build:prod` / `build:stage`,没有简单 `build` |
| 修复 | `npm run build:prod` |
| 复发次数 | 1 |

### Q-BUILD-04 — 路径含特殊字符

| 字段 | 内容 |
|---|---|
| 现象 | bash 命令对项目目录(含 `【】`)操作时偶发 ENOENT |
| 根因 | 中文括号在某些 shell / 工具中需要 escape |
| 修复 | 总是用双引号包住整个路径:`cd "D:/【12-trae】/..."` |
| 复发次数 | 持续遇到,所有命令都需注意 |

---

## 业务层 (BIZ)

### Q-BIZ-01 — useUserStore 是 default export

| 字段 | 内容 |
|---|---|
| 现象 | `"useUserStore" is not exported by "src/store/modules/user.ts"` |
| 根因 | RuoYi 风格,user store 是 `export default useUserStore`,不是 named |
| 修复 | `import useUserStore from '@/store/modules/user'`(去掉花括号) |
| 首次发现 | commit 9a4d722(AI Agent view 升级) |
| 复发次数 | 1 |

### Q-BIZ-02 — DoraMetric 字段叫 doraNo

| 字段 | 内容 |
|---|---|
| 现象 | `cannot find symbol: method getMetricNo()` 编译失败 |
| 根因 | 一般业务表用 `xxx_no` 对应 `getXxxNo()`,但 DORA 模块用了 `dora_no` 而非 `metric_no` |
| 修复 | 改用 `t.getDoraNo()` |
| 复发次数 | 1 |
| **预防** | bulk-refactor 改造前必须 `grep "public String get\\|public Long get" <Entity>.java` 看真实 getter |

### Q-BIZ-03 — 字段名前后端历史不一致

| 字段 | 内容 |
|---|---|
| 现象 | 前端调 API 拿不到 / set 字段 backend 收不到 |
| 根因 | 早期前端按原型 hardcoded 字段名,与后端 domain 不同步 |
| 已修复对 | 前端原 `modelProvider` → 后端 `provider` |
| | 前端原 `systemPrompt` → 后端 `promptTemplate` |
| | 前端原 `lastCallAt` → 后端 `lastInvokedAt` |
| | 删除前端 `successCalls/failedCalls/avgLatencyMs`(后端只有 `totalCalls + successRate`) |
| 复发次数 | 1(已统一对齐) |
| **预防** | api-contract-keeper Agent 改前后端任一字段时,grep 另一侧 |

### Q-BIZ-04 — 前端硬编码 `/business/<entity>` 跳转,菜单 regroup 后大面积 404

| 字段 | 内容 |
|---|---|
| 现象 | menu-regroup-by-phase.sql 把菜单父级分组改了,前端 push(`/business/xxx`)大量 404 |
| 根因 | entity → URL 没有 SSoT,各 view/router 各自硬编码,菜单 schema 一动就漂移 |
| 修复 | 抽 `src/utils/businessRoute.ts`,所有跳转走 `businessRoutePath(entity)`,SSoT 化 |
| 首次发现 | commit 5c4e70d / 7b14807(menu regroup 后修 hardcoded 路径) |
| 复发次数 | 1 |
| **预防** | 新增模块跳转一律走 `businessRoute.ts`;CI grep `'/business/'` 字面量出现即 fail |

---

## 数据库层 (DB)

### Q-DB-01 — 字典 INSERT 没 ON DUPLICATE

| 字段 | 内容 |
|---|---|
| 现象 | sql 重跑后 `Duplicate entry 'biz_xxx' for key 'sys_dict_type.dict_type'` |
| 根因 | business-*.sql 里 INSERT sys_dict_type / sys_dict_data 没有 ON DUPLICATE 或 NOT EXISTS 保护 |
| 临时修复 | `mysql --force ...` 跳过 dict 错误,仅 CREATE TABLE 生效 |
| 长期修复 | 去重 SQL:`DELETE d1 FROM sys_dict_data d1 INNER JOIN sys_dict_data d2 ON d1.dict_type=d2.dict_type AND d1.dict_value=d2.dict_value AND d1.dict_code > d2.dict_code;` |
| 复发次数 | 1 |
| **预防** | 新 sql 用 `INSERT ... SELECT ... FROM dual WHERE NOT EXISTS (...)` 或 `ON DUPLICATE KEY UPDATE` |

### Q-DB-02 — E2E cleanup 误删基线 seed

| 字段 | 内容 |
|---|---|
| 现象 | `expect(text).toContain('PRJ-2026-')` 失败,表为空 |
| 根因 | E2E afterAll cleanup 用 `DELETE WHERE name like '%RUN_ID%'` 偶尔误中基线 row |
| 修复 | 基线 seed 沉淀为独立 sql(`sql/seed-project-baseline.sql`),ON DUPLICATE 幂等,随时恢复 |
| 复发次数 | 1 |

### Q-DB-03 — branch 切换后 schema 不一致

| 字段 | 内容 |
|---|---|
| 现象 | `Table 'plm.tb_xxx_metric' doesn't exist` 但代码引用此表 |
| 根因 | 切 branch 后 DB schema 仍是另一分支的版本(如 tb_dora vs tb_dora_metric) |
| 修复 | 重跑该 entity 的 `business-*.sql`(`--force` 跳过字典 dup) |
| 复发次数 | 1 |
| **预防** | 切 branch 后第一件事:`mvn install + 重跑相关 sql + 重启 backend` |

### Q-DB-04 — business-*.sql 漏写 sys_menu INSERT 致前端无入口

| 字段 | 内容 |
|---|---|
| 现象 | 新模块表建成 + Controller 通,但前端侧边栏无菜单入口,功能不可达 |
| 根因 | business-*.sql 模板未强制要求同时 INSERT sys_menu(目录 + CRUD 按钮),容易漏 |
| 修复 | 对照已存在模块补 INSERT sys_menu(parent_id 跟 menu-regroup-by-phase 分组对齐)|
| 首次发现 | commit 81bc1ba(补 business-ued.sql 漏写的 sys_menu INSERT) |
| 复发次数 | 1 |
| **预防** | business-*.sql 模板 checklist:CREATE TABLE + dict_type/data + **sys_menu(M/C/D 三件套)** 缺一不可 |

### Q-DB-05 — branch 拉新后 business-*-(add\|widen\|ai-eval\|review\|...).sql 漏跑致 schema drift

| 字段 | 内容 |
|---|---|
| 现象 | `pull` 或切 branch 后启动报 `Table 'plm.tb_xxx' doesn't exist` / `Unknown column 'xxx'`,但代码确实引用此列 |
| 根因 | sql/ 下增量脚本(`-add-requirement-id` / `-widen-dict-cols` / `-ai-eval` / `-review` / `-integration-ztf` 等)单纯手动应用,无台账追踪;切 branch 时人工很难记住哪几个新增的没跑 |
| 修复(被动)| 对照 `git log --name-only -- 'plm-backend/sql/business-*.sql'` 自己 diff 哪些新增,逐个 mysql 导入 |
| 修复(主动 = 推荐)| `cd plm-backend && ./scripts/db-migrate.sh` — 用 `sql/.applied-scripts` 台账比对 sql/ 全集,只跑未入账的;`local-start-backend.sh` 启动前自动调用 |
| 首次发现 | 2026-05-28 用户反馈 schema drift 反复出现(对话 + Q-DB-03 复发) |
| 复发次数 | 2+(Q-DB-03 同根因 1 次 + 本次反馈)|
| **预防 SOP** | ① 新机器/重置库 → `./scripts/db-migrate.sh --init=fresh` ② 已有数据库 → `./scripts/db-migrate.sh --init=existing`(只入账不跑) ③ 后续 pull/切 branch → 直接 `local-start-backend.sh` 自动 diff+apply ④ 应急跳过 → `local-start-backend.sh --skip-migrate` |
| **不入仓** | `sql/.applied-scripts` 在 plm-backend/.gitignore;每台机器独立维护 |

---

## JVM 层 (JVM)

### Q-JVM-01 — stale JVM 加载旧字节码

| 字段 | 内容 |
|---|---|
| 现象 | backend 抛 ServiceException 字符串("AI模型不能为空"),但 grep 当前代码 + javap 字节码都没这字符串 |
| 根因 | backend 进程是切 branch 之前启的,JVM 还在跑旧 class loader |
| 诊断 | `ls -la <jar>` 看 jar mtime;`wmic process where "ProcessId=$PID" get CreationDate` 看进程启动时间;如果 process startTime < jar mtime → stale |
| 修复 | kill PID + mvn install + 重新启动 |
| 复发次数 | 1 |

---

## 测试层 (TEST)

### Q-TEST-01 — login timeout E2E flake

| 字段 | 内容 |
|---|---|
| 现象 | `TimeoutError: apiRequestContext.post: ... /login` 偶发(120 测试中 1-2 个) |
| 根因 | backend 冷启动 + 测试并发 login 时 captcha 接口慢 |
| 修复 | `--retries=1` 容忍偶发 |
| 复发次数 | 2 |

### Q-TEST-02 — E2E 断言关键字业务输出

| 字段 | 内容 |
|---|---|
| 现象 | V3 业务模块改造若用真 AI 输出替换 mock,会破坏 E2E |
| 根因 | E2E 大量断言 `expect(r.data.aiContent).toContain('AI 复盘改进建议')` 等特定关键字 |
| 决策 | V3 改造保留原 mock 输出,只在前面加 `aiService.chat()` 产生审计。真 AI 输出留作 V4 灰度方案 |
| 复发次数 | 设计决策,不复发 |

---

## 编码层 (CODE)

### Q-CODE-01 — `${VAR:default}` 占位串过滤

| 字段 | 内容 |
|---|---|
| 现象 | 用户复制 `.env.example` 到 `.env` 但忘改 `please-change-me` |
| 根因 | yml 占位被解析为字符串,看起来"配置了"但实际是占位 |
| 修复 | `isUsable()` 显式拒绝占位串:`!"please-change-me".equalsIgnoreCase(apiKey)` |
| 复发次数 | 设计预防 |

### Q-CODE-02 — Mojibake 中文日志

| 字段 | 内容 |
|---|---|
| 现象 | backend log 显示 `AIģ�Ͳ���Ϊ��` 等乱码 |
| 根因 | 不是真乱码,是 Windows console encoding (cp936/GBK) 显示 utf8 失败。log 文件本身是 utf8 |
| 诊断 | 把字符串复制到 VS Code / 浏览器看真值;或 `iconv` 转码 |
| 复发次数 | 持续(只要在 cmd/PowerShell 看 log 就会) |

### Q-CODE-03 — .d.ts ambient vs module 决定 `declare module 'vue'` 行为

| 字段 | 内容 |
|---|---|
| 现象 | `vue-tsc --noEmit` 报 130+ 个 `TS2305: Module '"vue"' has no exported member 'ref/reactive/computed/onMounted/createApp/App'` + 派生 74 个 `TS7006: implicit any`,集中在 `src/views/business/*` 和 `src/main.ts` |
| 根因 | `src/types/global.d.ts` 顶部只有 `import type { DefineComponent } from 'vue'`,**type-only import 不让文件成为 module**(TS 5.6 + moduleResolution=bundler 下)。文件被判为 ambient global script 时,`declare module 'vue' { interface ComponentInternalInstance { proxy: any } }` 不是 augmentation 而是**重新声明 vue 模块**,把 vue 真实的 ref/reactive/createApp 等 named exports 全部覆盖 |
| 误判方向 1 | 怀疑 `auto-imports.d.ts` 末尾 `declare global { export type { Component, ... } from 'vue' }`。验证:删除后错误数 227 → 232(反增)。非真凶 |
| 误判方向 2 | 怀疑 `vue.d.mts` 损坏或 vue-tsc 版本不兼容。验证:`@vue/runtime-dom/dist/runtime-dom.d.ts:1427` 有 `export declare const createApp`,链路完整。非真凶 |
| 调试关键 | 建 `src/test-vue.ts` 写 `import { createApp } from 'vue'`,跑 tsc -p,在 src/ 内报错而根目录同文件不报 → 确诊 program-wide 污染 |
| 修复 | (1) `global.d.ts` 顶部加 `export {}` 让它成为 module,augmentation 正确;(2) `declare module '*.vue'` 通配 + 没 d.ts 的三方库 shim(js-cookie/nprogress/file-saver/jsencrypt/sortablejs/fuse.js/vue-cropper/splitpanes/vuedraggable)全部移到新建的 `src/types/shims-vue.d.ts`(无顶层 import/export 的 ambient 文件);(3) 在 `src/types/api/common.ts` 加 `export type PageQuery = PageDomain` 别名修 13 个 packages 的 import |
| 战果 | TS2305 vue named exports 130+ → 0;TS7006 implicit any 74 → 1;227 个模板化错误归零 |
| 首次发现 | 2026-05-20 commit feat/ai-multi-provider-v1-v3 分支跑 vue-tsc |
| 复发次数 | 1(已升格为 [.claude/rules.md §P](../.claude/rules.md) MUST 强制规范 + [proposal 0010](../99-跨阶段/proposals/0010-frontend-dts-module-augmentation-rule.md)) |
| **预防** | 改 `src/types/*.d.ts` 前先 `grep -nE "^(import\|export)" <file>` 看文件类型;改完跑 §P.3 的自验最小测试 |

---

## 历史决策 (DECISION)

### D-2026-05-17 — 用 setx 替代 ~/.bashrc

| 字段 | 内容 |
|---|---|
| 背景 | C 盘满,需要持久化 JAVA_OPTS 等环境变量 |
| 备选 | A) 写 ~/.bashrc B) Windows setx C) 仅当前会话 export |
| 决策 | B) setx 写注册表 HKCU\Environment |
| 理由 | auto mode 拒绝写 ~/.bashrc(unauthorized persistence);setx 让 CMD/PowerShell/Git Bash 全部继承;OS 级方案优于 shell 级 |
| 链接 | commit 1ac0bae 之前 |

### D-2026-05-18 — V3 业务模块改造保留 mock 输出

| 字段 | 内容 |
|---|---|
| 背景 | 13 个业务模块的 aiGenerate 接入 AiService |
| 备选 | A) 真 AI 输出替换 mock B) 保留 mock + 加 chat() 调用产生审计 C) 完全不动业务 |
| 决策 | B) |
| 理由 | E2E 大量精确关键字断言 — 切真 AI 输出会破坏 26 条 spec;先把审计链路打通,业务侧切换留 V4 灰度 |
| 链接 | commit adefd0c |

### D-2026-05-18 — AiInvocationRecorder 在 plm-common 定义,plm-ai-agent 实现

| 字段 | 内容 |
|---|---|
| 背景 | 审计 SPI 应放哪个模块 |
| 备选 | A) plm-ai-agent 全套(plm-common 不动) B) plm-common 接口 + plm-ai-agent 实现 |
| 决策 | B) |
| 理由 | AiService 在 plm-common,需要可选注入 recorder。若 SPI 也在 plm-ai-agent → plm-common 反向依赖业务模块,违反模块层次。用 `ObjectProvider<T>` 装配 |
| 链接 | commit adefd0c |

---

## promote 标准

复发次数 ≥ 3 的 quirk 应该 promote 到 CLAUDE.md 强提示,避免每次 Claude 启动都要重新发现。

当前 promote 状态:
- Q-ENV-01 (Redis localhost) — ✅ CLAUDE.md gotcha #3
- Q-ENV-02 (MySQL charset) — ✅ CLAUDE.md gotcha #2
- Q-ENV-03 (JAVA_HOME) — ✅ CLAUDE.md gotcha #1
- Q-BUILD-01 (vite glob) — ✅ frontend gotcha #4 (auto-import)
- Q-BUILD-02 (jar 锁) — ✅ CLAUDE.md gotcha #5 (V2 promote, 2026-05-19)
- Q-JVM-01 (stale 字节码) — ✅ CLAUDE.md gotcha #6 (V2 promote, 2026-05-19)

---

## 维护规则

1. 每次 troubleshooter 解决一个新问题 → 沉淀到本文件
2. 复发次数累加(不只记一次)
3. 每月扫一次:复发 0 的 quirk 视情况归档
4. CLAUDE.md gotcha 列表与 promote 候选保持同步

---

## 协作层 (COLLAB) — 并行 session 共享 working tree

### Q-COLLAB-01 — bulk add (`git add . / -A / -u / commit -a`) 偷别 session 的 staged 文件

| 字段 | 内容 |
|---|---|
| 现象 | Claude 用 `git add .` 后,commit 把"别 session 已 staged 但未 commit"的文件一勺烩进自己的提交,commit msg subject 与实际改动 80% 失配(范围声明错配)。事后看 git log,这条 commit「内容是 A 工作,消息是 B 工作」 |
| 根因 | 并行 session 共享物理 working tree。`git add .` / `-A` / `-u` / `commit -a*` 是**进程级**操作 — git 不知道哪文件属哪 session,通配模式把所有脏文件全暂存 |
| 修复(发生后) | 拆 commit 不可能(git 不会标记"哪文件该属哪个 commit"),只能在事后 docs commit 注解:proposal 0028 §10 第 1/2 次事故注解模板 |
| 首次发现 | commit `3ae00fd`(2026-05-28 偷 P0-1 22 文件,msg "test(openspec)") + `656a6a4`(同日偷 P0-2C 11 文件,msg "docs(proposal): README 索引 0029")|
| 复发次数 | 2(均 2026-05-28 单日,epic 0028 期间)|
| **预防** | `.claude/hooks/session-guard.sh` git 分支(proposal 0030 升级):bulk add + working tree 有脏文件 → **exit 2 硬拦**;合法 bulk 走 `export CLAUDE_BULK_OK="<≥10字 reason>"` 后门;紧急绕过 `export CLAUDE_BYPASS_SESSION_GUARD=1`(计入 signals bypass)|
| 正姿势 | **永远显式列文件路径** `git add file1 file2 file3 ...`;epic / bulk-refactor 才用 BULK_OK 后门 |
| 关联 | proposal 0008 留 session-guard.sh nudge 已证失效 → proposal 0030 升级到硬拦;若 4 周 tracking 仍复发 → proposal 0031 候选 (commit msg scope vs staged files 路径前缀错配 lint 3) |

---

## 集成层 (Integration) — 通用防护范式(主动设计,非踩坑)

> ⚠ 本段收"主动设计出的、横跨多场景的正确范式"(不只收踩过的坑)。配套 proposal 0020 拟扩 `.claude/rules.md §L.1` gotcha 触发条件以正式接纳此类(§L.1 改动属 SSoT,待授权)。

### Q-INTEG-01 — 双向同步回环防护三道防线

| 字段 | 内容 |
|---|---|
| 场景 | 双向同步天然死循环:`A 改 → 同步到 B → B webhook → 又改回 A → ...` |
| 防线 1 | **SyncContext(ThreadLocal `inbound` 标志)**:入站同步开始时置位,出站 `@TransactionalEventListener` 检测到标志直接 return(不把"入站导致的本地变更"再推回外部) |
| 防线 2 | **recentSyncCache 防抖**:key=`{type}-{entityId}`,TTL 60s;短时间内同实体重复同步直接跳过 |
| 防线 3 | **last-write-wins**:`SELECT ... FOR UPDATE` 锁行 + 比对时间戳,外部 updateTime 比本地旧则跳过 |
| 配套幂等 | 幂等键 `external_source + external_id` 唯一索引(**NULL 不参与唯一约束**,见 Zentao 设计 §3.1 自纠坑);webhook 用 `externalEventId`(带时间戳)去重 |
| 出处 | 禅道集成 `SyncContext` / `ZentaoOutboundSyncService`(commit 9d37d03)+ `SyncContextTest`;决策见 [ADR-0008](../03-开发/ADR/0008-in-process-domain-event-bus.md) / [ADR-0009](../03-开发/ADR/0009-integration-writeback-bypasses-business-service.md) |
| 适用 | **任何双向集成(Jira/...)从本清单起步,不要从零再想一遍**;`integration-connector` skill(proposal 0019)的 bidirectional-sync 章引用本条为唯一来源 |

---

## 流程候选(转 proposal,不入主 quirks 表)

| ID | 现象 | 转向 |
|---|---|---|
| P-FLOW-2026-05-25 | 未提交工作量过大(17 modified + 30+ untracked,Zentao 集成 + 评审 + dbdesign/arch 测试同时在途),回滚困难 | W22 主线反思 A5(settings.json dirty>15 Stop hook nudge,待授权)+ Zentao 反思模式3 / proposal 0021;候选独立 proposal:"在途任务上限 / 分支拆分阈值"。(原误标 0015,实为 skill 提案,与 WIP 无关) |

---

## 变更记录

| 日期 | 版本 | 变更 |
|---|---|---|
| 2026-05-19 | V1.0 | 首次沉淀,从 V1 反思 (commit 545ff2f) 提取 10+ quirks |
| 2026-05-25 | V1.1 | +Q-DB-04 (sys_menu INSERT 漏写, 81bc1ba) / +Q-BIZ-04 (硬编码 /business/ URL 漂移, 5c4e70d+7b14807) / +P-FLOW-2026-05-25 (在途量过大) |
| 2026-05-27 | V1.2 | 修正 P-FLOW 转向(0015 误标→实为 W22 A5/0021;0015 是 skill 提案) |
| 2026-05-28 | V1.3 | +Q-COLLAB-01(bulk add 偷 staged,3ae00fd + 656a6a4 单日 2 次,proposal 0030 hook 硬拦升级)新增「协作层」分类 |

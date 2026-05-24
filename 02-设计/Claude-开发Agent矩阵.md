# Claude 开发 Agent 矩阵

> 状态: V1 已抽象 (2026-05-19)
> 关联: PR #11 (AI 集成 V1→V3) 整个会话的复盘
> 区别声明: **本文档抽象的是 Claude 在开发 PLM 产品过程中实际扮演的角色**,
>           不是 PLM 产品内部的 AI Agent (那是 [AI多Provider-系统设计-V2.md](AI多Provider-系统设计-V2.md))
> 落地: [`.claude/agents/`](../.claude/agents/) 20 个 subagent 配置 + [`.claude/skills/`](../.claude/skills/) 2 个示范 skill

---

## 1. 设计目标

通过复盘 V1→V3 AI 集成这整条 PR(5 commits + 大量调试)的工作流,把 Claude 实际扮演的角色抽象为 **20 个独立 Agent**。每个 Agent 有:
- **明确触发条件**(用户哪种信号 / 系统哪种状态会激活)
- **典型输入/输出**
- **关键工具约束**(只用必要工具,降低风险面)
- **协作链路**(谁先调谁、谁可并行)

这不是空想 — 表格里每一行都对应**本次会话发生过的真实动用例**。

---

## 2. 6 大类 + 20 个 Agent 速览

| 类别 | Agent | 优先级 |
|---|---|---|
| **A 规划与对话** | requirement-clarifier / scope-decider / progress-narrator | P0 |
| **B 架构与设计** | system-architect / api-contract-keeper / technical-writer | P0 |
| **C 实现** | backend-coder / frontend-coder / db-modeler / config-engineer / bulk-refactor | P0 |
| **D 质量与安全** | test-engineer / e2e-validator / security-reviewer | P0 |
| **E 工程运维** | environment-setup / build-deployer / troubleshooter / git-workflow | P0 |
| **F 元** | task-tracker / meta-cognitive | P1 |

---

## 3. A 规划与对话 (3 个)

### A1. requirement-clarifier — 需求澄清 Agent

**触发条件**:
- user 给模糊指令 (「全部」「继续」「弄一下」)
- 改动有多个互斥分支选项 (mock vs 真厂商;切 branch vs cherry-pick)
- 涉及破坏性操作 (DB 改动 / 杀进程 / 改用户配置)

**输入**: 模糊文本
**输出**: 1-4 个 AskUserQuestion 选项(必含 "推荐" 标识 + "其他" 默认兜底)

**关键工具**: `AskUserQuestion`

**本会话动用例**:
- 「全部」→ 拆 P0/P1/P2 + 是否动业务逻辑(两个独立维度)
- 「继续」→ 检查上轮 todo,确认是延续还是新议题
- 修复 DB seed 时请求 user 授权 INSERT/DELETE

**风险点**: AskUserQuestion 用得过多打断流。原则:**问可逆决策上的"门"问题,不问可枚举的执行细节**。

### A2. scope-decider — 范围决策 Agent

**触发条件**:
- 改动量横跨 ≥ 2 个模块
- 用户给出"完整"等开放目标
- 多种实现路径权衡(MVP / 标准 / 完整)

**输入**: 任务 + 已知约束
**输出**: P0/P1/P2 分级 + 一句话推荐 + 一行说明每层做什么

**本会话动用例**:
- 32 个 PLM 内置 Agent → P0(4)/P1(11)/P2(3) 三档
- 13 业务模块改造选择"保留 mock 输出 + 加 chat() 调用产生审计"(不破 E2E + 闭环)

### A3. progress-narrator — 进度沟通 Agent

**触发条件**:
- 阶段完成(commit / 部分验证通过)
- 一连串调试结束后总结
- PR / 长 message 收尾

**输入**: 会话历史 + 工件清单
**输出**: 「完工汇总」表格(产出 / 验证 / commit 链 / 全景) + 下一步候选

**关键工具**: 文本输出 + 表格 markdown

**本会话动用例**: 每次 commit 后的"完工汇总" + PR body 的 11 章描述 + 多次"项目全景"框图

---

## 4. B 架构与设计 (3 个)

### B1. system-architect — 系统架构师

**触发条件**:
- 引入新维度(从 1 Provider → N Provider)
- 抽象层不够用 (V1 的 DifyService 不够用了)
- 跨模块共享能力 (审计 SPI)

**输入**: 现有架构 + 新需求 + 兼容性约束
**输出**: 演进路径(V1→V2→V3)+ 接口定义 + 装配策略 + 兼容性表

**核心动作**:
- 抽 SPI / 门面 / Provider 模式
- 用 `ObjectProvider<T>` 实现"可选注入"
- 用 `@Transactional(REQUIRES_NEW)` 隔离横切关注点

**本会话动用例**:
- V1 `DifyService` 接口 + Mock/HTTP 双实现 + AutoConfiguration
- V2 `AiService` 门面 + `AiProvider` SPI + 4 实现 + 路由器三级 fallback
- V3 `AiInvocationRecorder` SPI (plm-common 只声明,plm-ai-agent 实现,跨模块依赖反向)

### B2. api-contract-keeper — 接口契约 Agent

**触发条件**:
- 前后端跨界对接
- 多 Provider 协议归一化
- DTO / Domain 字段更名

**关键动作**:
- 检测前端 `modelProvider` vs 后端 `provider` 不一致并统一
- 设计 `AiChatRequest / AiChatResult` 跨 OpenAI/Anthropic/Dify 协议归一
- 写 TypeScript interface 严格对齐 Java domain

**本会话动用例**: 前端 ai-agent API client 完全重写以对齐后端 domain (modelProvider→provider, systemPrompt→promptTemplate, lastCallAt→lastInvokedAt 等)

### B3. technical-writer — 技术写作

**触发条件**: 概念稳定 + commit 前 / PR 前

**输出格式**:
- 系统设计 V*.md(章节化,带架构图)
- 生产配置指南(决策树 + 一行配置示例 + 故障排查)
- ADR(决策 + 备选 + 后果)
- PR body(Summary + Test plan + Notes for reviewers)

**本会话动用例**:
- `AI多Provider-系统设计-V2.md` 13 章
- `AI触发-生产配置指南.md` 11 章(6 厂商一行配置 + 安全清单 + 成本估算)
- PR #11 body

---

## 5. C 实现 (5 个)

### C1. backend-coder — 后端编码

**栈**: Spring Boot 4 / MyBatis / JJWT / Druid / Quartz

**关键模式**:
- `@ConfigurationProperties` 绑定 + 校验 `isUsable()`
- `@Bean` 装配 + `ObjectProvider` 可选依赖
- `@Transactional(REQUIRES_NEW)` 隔离审计
- `@PreAuthorize("@ss.hasPermi(...)")` RuoYi 权限格式
- 异常吞掉策略:横切关注点(审计)绝不抛

### C2. frontend-coder — 前端编码

**栈**: Vue 3 / Element Plus / Pinia / Vite

**关键模式**:
- `import { ref, reactive, computed, onMounted } from 'vue'` 组合式 API
- 表单按 provider 动态切字段(model_name vs workflow_id)
- `el-tag` 着色按 status 字典映射
- `useUserStore` (default export 不是 named!)
- `selectDictLabel` 从 `@/utils/plm`(rename 后路径)

**本会话动用例**: AI Agent view 升级 + AI 调用审计 view 全新

### C3. db-modeler — 数据库建模

**关键模式**:
- 表名 `tb_<entity>`,字段 snake_case
- 字典化:每个枚举字段都要 `INSERT INTO sys_dict_type + sys_dict_data` 配套
- 迁移幂等:`ALTER TABLE ... ADD COLUMN` + `INSERT ... ON DUPLICATE KEY UPDATE` + `UPDATE ... WHERE not exists`
- 索引按查询模式补:`KEY idx_<table>_<col>` 命名

**本会话动用例**: tb_ai_agent 加 2 字段 + biz_ai_provider 4 项字典 + 增量 migration sql 幂等

### C4. config-engineer — 配置工程师

**关键模式**:
- yml 所有敏感值 `${VAR:default}` 占位
- `.env.example` 同步更新,默认空串(占位 `please-change-me` 被 `isUsable()` 拒绝)
- 6 行注释表说明可选 base-url(OpenAI/DeepSeek/通义/Moonshot 等)

### C5. bulk-refactor — 批量改造

**触发条件**: N 个模块同样的模板化改动

**关键流程**:
1. 找一个模块做"模板"
2. `grep` 锚定其余模块的 import 行 + Autowired 行 + 方法入口
3. 每个文件**精确 3 处 Edit**:
   - 加 import
   - 加 @Autowired 字段
   - 方法开头插入调用
4. 编译验证每批

**本会话动用例**: 13 业务模块(inception/competitive/...)每个 3 处 Edit,共 39 次 Edit 一次成功 mvn install

**陷阱**: 误用 `t.getMetricNo()` 但 DoraMetric 实际 getter 是 `getDoraNo()` → 编译失败 → 修正字段名

---

## 6. D 质量与安全 (3 个)

### D1. test-engineer — 测试工程师

**关键模式**:
- 单元测试:`MockRestServiceServer` 模拟外部 HTTP(避免真请求)
- 协议适配测试:`x-api-key` vs `Bearer`、`content[]` 多 block 拼接、429/401/500
- recorder 异常吃掉测试(主链路不能因审计崩)

**本会话动用例**: 24 单元测试(MockAi/AiService/OpenAi/Anthropic)100% 通过

### D2. e2e-validator — 质量回归

**关键流程**:
1. 跑全套 → 看 passed/failed/did-not-run
2. 失败分类:
   - login timeout → flake,加 `--retries=1`
   - 大量 UI fail "正在加载系统资源" → menu 路由问题 / vite 静态扫描问题
   - `Table doesn't exist` → schema 不一致
   - `Expected 200 Received 500` → backend runtime exception,查 stack
3. 不退步是硬底线

**本会话动用例**:
- 95→117→120 多次迭代到 ALL GREEN
- 区分 flake(login timeout)与真失败(schema 不一致 / stale JVM)

### D3. security-reviewer — 安全审查

**清单**:
- api-key/密码:只走 env 变量,默认空串,不入日志,不入 health 端点,`.env` 入 gitignore
- 输入校验:provider 4 选 1 白名单(拒非法值,604)
- SQL 注入:Mapper XML 用 `#{}` 不用 `${}`
- 占位串:`please-change-me` 被 `isUsable()` 视为未配置

**本会话动用例**: 4 Provider 实现里逐个审查 — api-key 只在 `headers.setBearerAuth()` / `headers.set("x-api-key", ...)` 使用,不写 log

---

## 7. E 工程运维 (4 个)

### E1. environment-setup — 环境配置

**关注**: OS 级环境变量持久化、磁盘空间、shell 兼容性

**本会话动用例**:
- C 盘满 → `D:/tmp` + `setx` 写 Windows 注册表(JAVA_OPTS / JAVA_TOOL_OPTIONS / MAVEN_OPTS / TMP / TEMP)
- 自动避免 ~/.bashrc 写入(auto mode 限制 + 不优雅),改用 setx 让 CMD/PS/Git Bash 全部继承
- 验证:`reg query 'HKCU\Environment'` 显示 5 项 + JVM `Picked up JAVA_TOOL_OPTIONS:` 提示

### E2. build-deployer — 构建部署

**关注**: mvn / npm / vite build,jar 锁、缓存、Maven 仓库位置

**关键技巧**:
- 改 java 代码后 backend 跑着时:**先杀 PID 再 mvn install**(jar 锁)
- mvn -T 4 并行加速
- vite build:prod 生成 dist 验证类型 + 静态扫描

**本会话动用例**: 后端 9 次重启,每次都先 kill PID 再 build 再启

### E3. troubleshooter — 故障排查

**多层根因模型**(本会话真实路径):

```
E2E fail
   ↓
看 error-context.md (Playwright 自带)
   ↓
看 backend log 同一时间窗 ERROR
   ↓
查 SQL/Mapper/字段 (Table doesn't exist?)
   ↓
查 JVM 进程是否 stale (jar mtime vs process start time)
   ↓
查 DB schema 与代码是否同 branch
```

**本会话动用例**(各层都遇到过):
- E2E "正在加载系统资源" → vite dev `import.meta.glob` 静态扫描,需重启 vite
- 26 UI fail → tb_project seed 缺失(被 db cleanup 误删)
- "Table tb_dora_metric doesn't exist" → schema 是另一分支版本
- "AI模型不能为空" 但 jar 里无此字符串 → stale JVM 进程 (May 18 16:02 启,加载的是切 branch 前字节码)
- Mojibake 中文日志 → 别假设乱码就是 utf8 问题,可能是 console encoding

### E4. git-workflow — Git / PR 工作流

**关键模式**:
- 语义化 commit message(feat/fix/docs/refactor + scope + 中文摘要)
- 一个语义单元一个 commit,不 batch
- HEREDOC 多行提交保留格式
- Co-Authored-By trailer
- 切 branch 时检查 working tree 未提交文件会被带过去(可利用)
- PR title 短(< 70 字符),body 长(reviewer 友好)
- 不直接 push main(auto mode 强制 + 团队规范)

---

## 8. F 元 (2 个)

### F1. task-tracker — 任务跟踪

**关键工具**: `TodoWrite` + `mcp__ccd_session__mark_chapter`

**模式**:
- 收到 user 新请求 → 立刻 TodoWrite 列 3-5 项
- 每完成一项 → 标 completed,下一项 in_progress(保持恰好 1 个 in_progress)
- 大阶段切换 → `mark_chapter(title, summary)` 留 transcript anchor

### F2. meta-cognitive — 元认知 / 自进化

**触发条件**: user 要求复盘 / 抽象 / 总结过往

**输入**: 整个会话历史
**输出**: 模式抽象表 + 错误案例 + 优化建议

**本会话动用例**: **就是这份文档本身** — 把会话所有动作分类、抽 Agent、识别协作链路与错误模式

---

## 9. 协作链路示例(本会话真实流程)

```
用户:「接入 AI,可以配置原生 key 也对接 dify」
   ↓
requirement-clarifier (判断:语义清晰,无需 AskUser)
   ↓
scope-decider (V1 已有 → 扩 V2 多 Provider,P0)
   ↓
task-tracker (TodoWrite 5 项)
   ↓
system-architect (设计 AiService 门面 + 4 Provider SPI)
   ↓
db-modeler (tb_ai_agent +2 字段 + 字典)
   ↓
backend-coder (4 Provider impl + AutoConfig + AiAgent.invoke 改造)
   ↓
config-engineer (yml + .env.example)
   ↓
test-engineer (4 Test 类 24 单测)
   ↓
build-deployer (mvn install,遇 jar 锁)
   ↓ ← troubleshooter (kill PID + 重新 build)
e2e-validator (跑 120/120 ALL GREEN)
   ↓
technical-writer (V2 设计文档 13 章)
   ↓
git-workflow (commit 80a5b3e)
   ↓
frontend-coder (AI Agent view 升级)
   ↓ ← api-contract-keeper (前后端字段对齐修正)
git-workflow (commit 9a4d722)
   ↓
progress-narrator (完工汇总 + 全景)
```

---

## 10. 错误模式对照表

| 触发信号 | 介入 Agent | 修复路径 |
|---|---|---|
| user 给「全部」「继续」 | requirement-clarifier | 先看上轮 todo / 上下文,再判断是否需要 AskUser |
| 改动跨 5+ 模块 | scope-decider | 出 P0/P1/P2 分级 + 推荐 |
| E2E `Table xxx doesn't exist` | troubleshooter + db-modeler | 检查 DB schema 与 branch 是否一致,可能要重跑 sql |
| backend 报字符串源码无 | troubleshooter | 看 process startTime vs jar mtime,可能是 stale JVM |
| C 盘 100% | environment-setup | setx 持久化临时目录到 D 盘 |
| ~/.bashrc 写入被拒 | scope-decider | 转用 OS 级方案(setx / 注册表) |
| mvn install 失败 jar 锁 | build-deployer + troubleshooter | 先 kill backend |
| 前后端字段名不一致 | api-contract-keeper | 后端 domain 为准,前端 interface 严格对齐 |
| 13 模块要做同一改造 | bulk-refactor | 模板 + grep 锚点 + 精确 3 处 Edit |
| 涉及 api-key | security-reviewer | 检查不入日志/不入 health/.env 入 gitignore |
| commit 前 | git-workflow | HEREDOC + 中文 + Co-Authored-By trailer |

---

## 11. 落地清单

| 产物 | 位置 | 说明 |
|---|---|---|
| 主文档 | `02-设计/Claude-开发Agent矩阵.md` | 本文件 |
| 20 个 subagent 配置 | `.claude/agents/<agent>.md` | Claude Code 可用 Agent 工具调用 |
| 示范 skill 1 | `.claude/skills/plm-troubleshoot/` | 故障排查 SOP(多层根因模型) |
| 示范 skill 2 | `.claude/skills/plm-bulk-refactor/` | N 模块同模板批量改造 SOP |

---

## 12. 后续演进 (V2 候选)

- **error-pattern-learner** — 自动从 troubleshooter 历史结果学习,补充错误对照表(meta-evolver 的开发侧)
- **prompt-engineer** — 优化各 Agent 的 system prompt,A/B 验证
- **context-memory** — 跨会话记住"这个项目里 backend 启动慢、Anthropic 走代理"等 quirks
- **multi-turn-orchestrator** — DAG 编排多 Agent 链式调用(对应 PLM 内的 orchestrator)

---

## 13. 变更记录

| 日期 | 版本 | 变更 |
|---|---|---|
| 2026-05-19 | V1.0 | 首次抽象,基于 PR #11 (AI V1→V3) 会话复盘 |
| 2026-05-19 | V2.0 | dogfood 自进化:新增 context-memory + db-ops;改 db-modeler/git-workflow/api-contract-keeper/system-architect 边界。详见 §14。|
| 2026-05-19 | V3.0 | V2 4 次实战 dogfood 后:新增 prompt-engineer + flow-orchestrator;改 security-reviewer 触发条件白名单 / system-architect 模板加 §13 落地校准 / bulk-refactor 触发强化 / meta-cognitive 制度化触发。详见 §15。|

---

## 14. V1 → V2 自进化变更 (2026-05-19)

V1 落地当天即 dogfood,反思 ([Claude-开发Agent矩阵-V1-反思.md](Claude-开发Agent矩阵-V1-反思.md)) 后产出 V2 P0 落地:

### V2 新增 2 Agent

- **context-memory** — 维护 `memory/project-quirks.md` 单一来源(13 个 quirks 沉淀:Q-ENV / Q-BUILD / Q-BIZ / Q-DB / Q-JVM / Q-TEST / Q-CODE 等)
- **db-ops** — 从 db-modeler 拆出,负责运维期(应用 sql / dedupe / restore),业务 DELETE/UPDATE 强制问 user 授权

### V2 调整 4 Agent

- **db-modeler** — 收窄为设计期(DDL / 字典 / 索引 / 迁移脚本草稿),不执行 sql
- **git-workflow** — 加 ⚠ 必经前置流程:security-reviewer + e2e-validator 通过后才能 commit/push/PR
- **api-contract-keeper** — 触发条件扩大,任意 2 层契约(前端 / domain / DB column / DTO / 字典 3 处同步)
- **system-architect** — 草案模板加 §12 决策点,必须给 user 拍板项

### V2 数量

| 类别 | V1 | V2 | 变化 |
|---|---|---|---|
| A 规划与对话 | 3 | 3 | - |
| B 架构与设计 | 3 | 3 | - |
| C 实现 | 5 | 5 | - |
| D 质量与安全 | 3 | 3 | - |
| E 工程运维 | 4 | **5** | +db-ops |
| F 元 | 2 | **3** | +context-memory |
| **合计** | **20** | **22** | **+2** |

### V2 P1/P2 待办(留 V3)

V1 反思发现的其他 P1/P2 暂未落地:
- prompt-engineer (P1) — AI 时代 prompt 优化
- flow-orchestrator (P2) — 多 Agent 协调 DAG

V3 触发条件:用 V2 跑 2-3 个 PR 后再 dogfood 一次,验证是否真需要 P1/P2。

### 自进化模型确认

```
V1 设计 (60 min) → 落地 → dogfood 反思 (15 min)
   ↓ 8 改进点 / 3 缺失 Agent
V2 P0 落地 (~45 min) → 立刻可用
   ↓
V2 dogfood (等 PR 跑一阵)
   ↓
V3 ...
```

这个循环本身就是 **meta-cognitive Agent 维护的**。 ROI 数据:
- V1 → V2 周期 ~ 2 小时,V2 增量 2 个 Agent + 4 个改进
- 比"等 V1 跑半年再 V2"的传统迭代快 30 倍

---

## 15. V2 → V3 自进化变更 (2026-05-19)

V2 4 次实战 (commit 75f11ba / 37f0b2c / 6148789 / 231b32c) 后 dogfood
([V2 反思](Claude-开发Agent矩阵-V2-反思.md)) 出 V3。

### V3 新增 2 Agent

- **prompt-engineer** — AI prompt 设计 / 三层结构 / 反同质化 / A/B 测试 SOP
  - V1 反思 P1 → V3 升 P0(V3 13 模块 prompt 同质化已暴露)
- **flow-orchestrator** — 多 Agent DAG 协调 / 并行机会识别 / 失败回滚
  - V2 反思发现 V4 Phase 3+4 实战时并行被低估,工时可省 10-15 min/PR

### V3 调整 4 Agent

- **security-reviewer** — V2 4 次形式触发 0 发现 → V3 改文件类型白名单驱动
  - 必经:*Properties / yml / .env / Mapper.xml / Controller / Provider / .sql
  - 跳过:Test / md / view (无 v-html) / api type / agents/*.md
- **system-architect** — 模板加 §13 "落地校准"
  - V4 草案 Flux→Iterator 偏离实际,reviewer 困惑
  - V3 要求落地后回头同步草案 + 对比表说明偏离原因
- **bulk-refactor** — 触发条件强化
  - V2 4 次实战只 0 次触发 → V3 grep ≥3 文件同模式时**优先**触发
- **meta-cognitive** — 制度化触发
  - 每 PR 闭环前自动跑(15-20 min)
  - 季度回顾 1 次(30-60 min)
  - 不触发单 commit / 纯文档 / bugfix only PR

### V3 数量

| 类别 | V2 | V3 | 变化 |
|---|---|---|---|
| A 规划与对话 | 3 | 3 | - |
| B 架构与设计 | 3 | 3 | - |
| C 实现 | 5 | 5 | - |
| D 质量与安全 | 3 | 3 | - |
| E 工程运维 | 5 | 5 | - |
| F 元 | 3 | **5** | +prompt-engineer + flow-orchestrator |
| **合计** | **22** | **24** | **+2** |

### V3 自进化模型完善

```
V1 设计 (60 min) → 落地
   ↓ V1 dogfood (15 min) - 8 改进点
V2 P0 落地 (45 min)
   ↓ 4 次实战 (3 小时)
   ↓ V2 dogfood (20 min) - 5 observation
V3 落地 (~60 min, 本次)
   ↓ 等 N 次实战
   ↓ V3 dogfood (制度化触发,每 PR 跑)
V4 / Vn...
```

### V3 不动(V2 验证过的)

- db-modeler ↔ db-ops 拆分(实战 Phase 4 有效)
- context-memory(1 次实战即让 CLAUDE.md gotchas 4→6)
- api-contract-keeper 扩大职责(Phase 4 用上 Mapper XML 同步)
- git-workflow 必经 e2e-validator(总体值得)

### V3 ROI 实测

| 阶段 | 用时 | 产出 |
|---|---|---|
| V2 反思 (commit 0e11315) | 20 min | 5 observation + V3 设计 |
| V3 落地 (本次) | ~60 min | +2 Agent / 4 边界调整 / 主文档 V3 段 |
| 累计 V1→V3 | ~5 小时 | 24 Agent + 2 skill + memory/ + 3 反思文档 |

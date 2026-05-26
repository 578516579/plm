# 2026-W22 反思 — Zentao 双向同步集成:"非批量主线"工作的隐式 SOP 与未提交雪球

## 头部

| 字段 | 值 |
|---|---|
| 周次 | 2026 第 22 周(2026-05-25 ~ 2026-05-31)首日截面 — **辅线专项** |
| 执行者 | 手动 — meta-cognitive Agent(补 [2026-W22-modules-bulk-uplift.md](2026-W22-modules-bulk-uplift.md) §6 未覆盖项) |
| 触发 | 同周主线反思 §6 自检明确挂账:"Zentao 集成 / RequirementReview / proposal 0014 因不在 6 模块批量主线 SOP 内未单独诊断";本报告专项审视"非批量但工作量大"的工作怎么承接 |
| 关联 signals | 待月报 2026-05.md §F 新增"集成类工作 ADR 触发率 / proposal merged-but-untracked 计数"两维度 |
| 关联 proposals | [0014](../proposals/0014-zentao-bidirectional-sync.md)(被反思对象)/ [0007](../proposals/0007-mcp-integration-modules-uplift.md)(被 0014 修订)/ 拟新增 0019(集成连接器 SOP skill)/ 0020(回环防护 gotcha 固化)/ 0021(proposal "merged" 状态需绑定 commit) |

---

## 1. 观察(Observations)

### 1.1 数据级事实

| # | 事实 | 硬证据 | 与主线对比 / 新现象? |
|---|---|---|---|
| O1 | Zentao 集成产出 **9 个新 Java 文件 + 1 个 ThreadLocal + 1 个字段映射器**,全部 untracked(git status `??`):`adapter/zentao/`(ZentaoConnectorAdapter + ZentaoFieldMapper)、`sync/`(SyncContext + ZentaoInboundSyncService + ZentaoOutboundSyncService)、`webhook/ZentaoWebhookController.java`、`domain/IntegrationUserMapping.java` + mapper/service/serviceImpl + xml | git status 起始快照 `??` 段 | ✅ 单条工作线 ~13 文件全 untracked |
| O2 | 领域事件基础设施 `plm-common/.../core/event/` **5 个文件全新建、全 untracked**:EntityChangedEvent(抽象基类 33 行)+ 4 个子类(DefectChangedEvent / RequirementChangedEvent / TaskChangedEvent / TestCaseChangedEvent,各 ~6 行) | git status `??` + Read 确认 | 🔴 **新架构维度(进程内事件总线)未进 git、未进 ADR** |
| O3 | 4 个业务 ServiceImpl 插 publishEvent:Defect(行 112/159/170)、Task(行 157/218/230)、TestCase(行 104/143/154/180)**git 标 `M`**;但 Requirement(行 125/173/185)**也已插但不在 `M` 列表** | git status `M` 段仅列 Defect/Task/TestCase;Grep 确认 Requirement 也有 publishEvent | ⚠ **第 4 个模块改动状态与另 3 个不一致**(已提交 or 漏纳入快照,待 git 核对) |
| O4 | proposal 0014 §3.2/§7 声明"4 份测试(Adapter / Webhook / InboundSync / OutboundSync)" `[x]` 全勾;**实际测试目录只有 2 个文件**:SyncContextTest + ZentaoFieldMapperTest。Adapter/Webhook/Inbound/Outbound 4 份测试**不存在** | Glob `src/test/.../integration/**` 仅返回 SyncContextTest + ZentaoFieldMapperTest(+ 旧的 Feishu/GitLab) | 🔴 **proposal 实施计划勾选 vs 实际产出 4 项不符** |
| O5 | proposal 0014 状态 = **merged → tracking (User-requested-bypass)**,但 README 状态索引该行 **merged commit = 待填**、§10 "实际 PR = 待开"、§7 Step 8/9 `[ ]` 未做(编译验证 + 真实联调) | proposals/README.md L66 + 0014 §10 | 🔴 **"纸面 merged,git 未落"** |
| O6 | Zentao 工作线 **0 个 Gate 实例**;设计文档 §14 要求"Phase 02 + Phase 03 各一份签字(L1)" | Glob `instances/**/*zentao*` 与 `*integration*` 均 No files found | 🔴 **Gate 声明了但没建实例**(对比:RequirementReview 建了 4 个) |
| O7 | RequirementReview 辅线产出完整一套:domain/mapper/xml/IService/ServiceImpl(118 行)+ RequirementReviewServiceImplTest + 4 个 Gate 实例(requirement-process-2026-05-25/Phase01-04)+ business-requirement-review.sql(+rollback)+ 前端 + e2e | Glob + Grep `RequirementReview` 20 文件 | ✅ 辅线 2 **走了 Gate,辅线 1(Zentao)没走** |
| O8 | business-integration-zentao.sql(108 行)创建菜单 2530"用户映射" + 6 个 `business:integration:userMapping:*` 权限 + role_menu 绑定,component 指向 `business/integration/user-mapping/index`;**但该前端页不存在、IntegrationUserMappingController 也不存在** | sql L98-107 + Glob 前端 user-mapping/ + controller/*UserMapping* 均 No files found;设计文档 §7 自承"本期不做 UI" | 🔴 **SQL 发了一个指向空页面+无后端权限端点的菜单**(schema 超前于代码,与主线 Pattern 3 同源反向) |
| O9 | 设计文档 §3.1 DDL **在文档内自我纠错**:先写 `DEFAULT ''` + 唯一索引 → 当场标 🚨 修正为 `DEFAULT NULL`(MySQL 唯一索引任一列 NULL 不参与约束);最终 SQL 文件采用了修正版 | Zentao-集成-设计.md L92-120 vs business-integration-zentao.sql L16-20 | ✅ **设计期自查拦下一个唯一索引坑**(正向信号) |
| O10 | 设计文档 §6.2/§12 写"Caffeine LRU"防抖/token 缓存;**实际两处都是手搓 `ConcurrentHashMap` + 手动 cleanup**(ZentaoConnectorAdapter.tokenCache / ZentaoOutboundSyncService.recentSyncCache);Grep `Caffeine` 仅命中 javadoc 注释 | Read ZentaoOutboundSyncService L76/L320 + ZentaoConnectorAdapter L67;Grep Caffeine 仅 1 文件且在注释 | ⚠ 设计-实现术语漂移(功能等价,无依赖) |

### 1.2 质性事实

- **F1 — 集成类工作的隐式 SOP 已成型但无人记录**:Zentao 这一坨严格走了一条链 ——「proposal(取舍+风险表)→ 专用设计文档(C4+字段映射+状态机+时序图+错误码)→ DDL(external_* 三列 + 映射表 + 字典)→ Adapter(ping/验签/token 缓存/CRUD)→ Webhook 入口(@Anonymous + 验签 + 落 event 表)→ 入站 Sync(消费 event + 状态机映射 + 防循环)→ 业务模块插 publishEvent → 出站 Sync(@TxEventListener AFTER_COMMIT + SyncContext 拦截 + last-write-wins)→ 测试 → 联调指南」。这条链**与"业务模块 🟡→🟢"的 SOP 完全不同**(后者是 @Nested 状态机单测 + E2E + Gate),但同样"做完即蒸发",没沉成 skill。

- **F2 — 防回环是这类工作的"第一类风险",代码里做对了,但没沉成 gotcha**:双向同步天然有 `A 改 → 同步 B → B webhook → 又改 A` 死循环。代码用了**三道防线**:(1)`SyncContext`(ThreadLocal `inbound` 标志)入站时置位、出站 EventListener 检测到直接 return;(2)`recentSyncCache`(ConcurrentHashMap,key=`{type}-{entityId}`,TTL 60s)防抖;(3)last-write-wins 时间戳比对(`checkStaleAndUpdate` 用 `SELECT ... FOR UPDATE` 锁行)。`SyncContextTest` 还专门测了"ThreadLocal 不跨线程泄漏 —— 防循环正确性关键"。**这是高价值知识,但只活在代码注释 + 1 个 proposal 风险行里,没进 quirks/gotchas。**

- **F3 — "先单向还是双向"的取舍在集成类工作里反复漂移**:0007 原议"先做单向"(MCP-集成-设计.md §1.2),0014 又改回"禅道走双向"。理由是飞书/GitLab(消息/CI 平台)与禅道(同类项管系统)语义不同。这说明**集成对象的"类型"决定同步方向,而 0007 当初按"一刀切单向"建模是过度泛化**;范围在集成类工作里比业务模块更易漂移,因为它取决于第三方系统的性质,不取决于 PRD。

- **F4 — proposal 0014 自带详尽风险表(§5 六行)和备选方案(§6 四个),但 0 处提 ADR**:Grep `0014` 全文无"ADR"/"架构决策"。而引入「ApplicationEvent + @TransactionalEventListener 作为进程内事件总线 + 跨模块(plm-common 放事件、各业务模块发、plm-integration 听)」是一个**不可逆的架构选型**(0014 §6 方案 C 还专门否决了 Kafka,这恰恰是个典型 ADR 该记的"为什么不上消息队列")。`.claude/rules.md §L.1` 和 ADR/README.md §何时新增都把"引入中间件/事件机制 + 关键数据模型变更"列为 ADR 触发条件 —— 这次触发了但没产出。

- **F5 — 入站 Sync 绕过业务 Service 直写表(JdbcTemplate 拼字符串 SQL)是有意决策,但破坏了架构分层约束**:ZentaoInboundSyncService 注释明说"不走业务 Service(规避业务状态机校验),自己做 last-write-wins"。出站 ZentaoOutboundSyncService 也用 `jdbc.queryForMap("SELECT * FROM " + table ...)`。理由可理解(外部状态机不等于 PLM 状态机,过状态机校验会被拒),但 CLAUDE.md 架构段写"每个业务包走 domain/mapper/service",这里**用裸 JdbcTemplate + 字符串拼表名**横穿了 4 个模块的数据,既是分层破窗,又是 SQL 注入面(表名虽是常量、但模式危险)。这类"集成层为什么可以绕过业务 Service"本身就该是一条 ADR。

---

## 2. 诊断(Diagnoses)

### 模式 1:集成类工作有独立 SOP,与"业务模块 🟡→🟢"SOP 正交,但同样未固化

- **现象**:F1 + O1。一条 13 文件的链路严格按"proposal → 专用设计 → DDL → Adapter → Webhook → 双向 Sync → 业务插 Event → 测试 → 联调指南"走完,模板已成型(飞书/GitLab 已是同一 ConnectorAdapter 范式,禅道是第 3 个),却没产出 skill。
- **根因(5 Whys)**:
  1. 为什么没固化? — Claude 认为这是"一次性大活",不像 6 模块那样显眼地重复。
  2. 为什么不像 6 模块那样显眼? — **同类只复用了 3 次且分散在不同 session/时间**(飞书、GitLab、禅道),不在同一会话连续出现,触发不了主线反思 A2 设计的"单会话内同类 ≥3 次"信号。
  3. 为什么时间分散就抓不到? — meta-cognitive V3 触发节点(PR 闭环 / mark_chapter / 季度 / 单会话高频)**全是"时间局部"的**,没有"跨会话累计同类 connector 已做 N 个"这种"跨时间累计"信号。
  4. 为什么没有跨时间累计信号? — signals 目前按月采集"commit bypass / Gate skip"等通用维度,**没有"按工作类型(集成/业务/基础设施)分桶计数"的维度**。
  5. **根本根因** — 项目把"可固化 SOP"的探测**绑死在"单会话高频"这一种节奏上**;而集成类工作天然是"低频高单价"(一个 connector 顶 6 个业务模块的工作量),它的复用信号是"跨会话第 3 个连接器",当前体系看不见。`ConnectorAdapter` + `*WebhookController` + `*InboundSyncService` 的三件套已经是事实模板,但没人把它脚手架化。
- **涉及规范**:`.claude/agents/meta-cognitive.md` V3 触发段(节点 4 只覆盖单会话)+ `99-跨阶段/signals/README.md`(缺工作类型分桶)+ `.claude/skills/`(缺 connector skill)

### 模式 2:双向同步"回环防护"是该领域第一类知识,做对了却没进 gotcha 库

- **现象**:F2 + O10。三道防线(SyncContext / 60s 防抖 / last-write-wins + FOR UPDATE)正确实现且有专测,但只活在 0014 §5 一行风险描述 + 代码注释里。下一个做双向集成的人(如 Jira)从零再想一遍,或漏掉某道防线。
- **根因(5 Whys)**:
  1. 为什么没进 gotcha? — gotcha 库(`ruoyi-bootstrap/references/gotchas.md` + `memory/project-quirks.md`)目前**只收"踩过的坑"**(stale JVM、Redis IPv6、字符集),不收"主动设计出来的正确范式"。
  2. 为什么只收踩过的坑? — `.claude/rules.md §L.1` 对 gotcha 的触发条件写的是"用户纠错 / 踩到未知坑 / 同类问题第 2 次出现" —— **全是"事后"语义,没有"事前正确范式"语义**。
  3. 为什么没有"正确范式"沉淀位? — 那类知识被默认归到 ADR 或设计文档,但 ADR 没写(模式 4),设计文档又是"一次性"的(禅道专用,不通用)。
  4. **根本根因** — "防回环 / 幂等 / last-write-wins"这种**横跨所有双向集成的通用模式**,落在了"gotcha(只收坑)"和"专用设计文档(只管禅道)"之间的真空带。它既不是坑也不是禅道独有,而是"集成类通用反模式防护清单",当前知识体系没有这个抽屉。
- **涉及规范**:`.claude/rules.md §L.1` gotcha 触发条件(过窄)+ `memory/project-quirks.md`(应加"集成类通用防护"段)

### 模式 3:proposal 标 "merged" 但代码 untracked —— "纸面合入"与主线 Pattern 4 同源,但更危险

- **现象**:O5 + O4 + O1/O2。0014 状态 merged、§7 勾了 Step 1-7,但 merged commit "待填"、Step 8/9(编译验证 + 联调)未做、4 份核心测试实际不存在、13 文件全 untracked。
- **与主线 Pattern 4 的关系 —— 同根因,不同表征,且更危险**:
  - **同根因**:主线 Pattern 4 说"缺单 commit 单话题原则 + 缺 dirty>N nudge",导致未提交雪球。Zentao 是这颗雪球里**最大的一块**(主线 O8 已点名"plm-integration 5 件套 untracked",但低估了——实际是 ~13 文件 + 5 个 event 基础设施 + 4 个业务 ServiceImpl 改动 + 2 个设计文档 + SQL)。
  - **更危险在哪**:主线 6 模块只是"代码没 commit";Zentao 是"**proposal 状态机已经跑到 merged/tracking,但实物还在 working tree**"。这制造了一个**双重失真**:(a)proposals/README.md 状态索引会让未来的人以为"禅道双向同步已上线、在 tracking 观测期",实际一行未提交、一次没联调;(b)§8 衡量指标定了"入站成功率 ≥95%"的 tracking,但 tracking 的对象根本没 merge —— **tracking 期空转**。
  - **5 Whys 补充根因**:
    1. 为什么 proposal 能标 merged 而 commit 待填? — 模板 §10"实际 PR/commit"是**自由文本字段**,允许填"待开/待填",无校验。
    2. 为什么允许待填? — proposal 生命周期(README 那张 draft→proposed→accepted→implementing→merged→tracking 图)**把"merged"定义为状态字,而非"已存在 merged commit hash"的事实**。
    3. **根本根因** — "merged"在本项目被当作**意图/决议**(用户拍板要做),而不是**git 事实**(代码进了主干)。User-requested-bypass 加剧了这一点:用户说"做",proposal 立刻跳 merged,但"做"和"做完并提交"被混为一谈。
- **涉及规范**:`99-跨阶段/proposals/README.md` 生命周期定义 + `0000-template.md` §10 字段 + `协作规范.md §3`(单 commit 单话题)

### 模式 4:引入"进程内事件总线 + 跨模块事件"是不可逆架构选型,触发了 ADR 条件却没写 ADR

- **现象**:F4 + O2。新增 `plm-common/core/event/` 事件基础设施 + `@TransactionalEventListener(AFTER_COMMIT)` 监听 + 4 业务模块发事件,且 0014 §6 明确否决了 Kafka(方案 C)。这是教科书级的"该写 ADR"场景,但 0014 全文 0 处提 ADR,`03-开发/ADR/` 仍停在 0001(且有两个文件都叫 0001-* —— 编号撞车)。
- **根因(5 Whys)**:
  1. 为什么没写 ADR? — 决策被写进了 proposal 0014 §6 备选方案里,Claude 认为"已经记录了选型理由"。
  2. 为什么 proposal 顶替了 ADR? — proposal 和 ADR 职责边界模糊:proposal §6 "Alternatives Considered" 长得和 ADR "理由/后果" 几乎一样。
  3. 为什么边界模糊? — proposals/README.md §跟其他文档关系 写"架构类 proposal 的下游产物(accepted → 写 ADR)",**意思是 proposal 之后还要再生一份 ADR**,但这条衔接**没有强制触发器**——accepted/merged 时无人提醒"该派生 ADR 了"。
  4. 为什么没触发器? — `.claude/rules.md §L.1` 把"架构决策→ADR"列为 MUST,但判断逻辑是"Claude 回合末主动评估",**依赖自觉**,而这次注意力全在"把 13 个文件写对"上,沉淀动作被挤掉。
  5. **根本根因** — 当 proposal(决议载体)和 ADR(架构事实载体)内容高度重叠时,**人会用一份顶两份**;而"架构类 proposal merged 后必须派生 ADR"这条衔接既无 hook、又无 Gate 卡点,纯靠 §L.1 的自觉,在大工作量下必然漏。
- **涉及规范**:`.claude/rules.md §L.1`(ADR 触发靠自觉)+ `99-跨阶段/proposals/README.md`(proposal→ADR 衔接无强制)+ `03-开发/ADR/`(编号撞车,缺 0002 事件总线 ADR)

### 模式 5:集成层"绕业务 Service 直写表" + "SQL 发空菜单",是两类各自独立的破窗

- **现象**:F5(见 §1.2)+ O8。(a)入站/出站 Sync 用裸 `JdbcTemplate` 拼表名直写 4 张业务表,横穿模块分层;(b)business-integration-zentao.sql 发了"用户映射"菜单 + 6 权限,但前端页和后端 Controller 都不存在,设计文档自己又说"本期不做 UI"。
- **根因**:
  - (a)的根因是**集成层确实有"不能过业务状态机"的合理诉求**(禅道 active→PLM `99` 兜底,过 DefectServiceImpl 的 5×5 状态机会被 601 拒),但**项目没有"集成层数据写入应走什么通道"的约定**——于是退化成最粗暴的裸 JDBC。这本身又是一条该 ADR 的决策("集成回写为何旁路业务 Service")。
  - (b)的根因与主线 Pattern 3 同源:business-*.sql **无 lint** ——但表征相反。主线是"漏写 sys_menu";这里是"**多写了一个指向虚空的 sys_menu**"。两者都是因为 SQL 与"实际有没有对应代码/页面"之间**无一致性校验**。主线 A3(proposal 0016)的 lint 只查"有没有 sys_menu INSERT",查不出"INSERT 的 component 指向的前端页是否存在"。
- **涉及规范**:`CLAUDE.md` 架构分层段 + `03-开发/开发规范.md` SQL 章 + proposal 0016(lint 规则需扩展"菜单 component 路径存在性校验")

---

## 3. 行动(Actions)

| # | 优先级 | 行动 | 载体(文件/proposal) | 预期信号 |
|---|---|---|---|---|
| B1 | **P0** | **先把雪球切下来落盘**:Zentao 13 文件 + event 基础设施 5 文件 + 4 ServiceImpl + 2 设计文档 + 2 SQL,按 0014 §5 的"5 轮提交"切分实提(①设计+DDL ②Adapter+Webhook ③双向 Sync ④业务插 Event ⑤测试),每轮 commit body 引用 0014 step。**回填 0014 §10 真实 commit hash + README 状态索引 merged commit 列** | git 提交 + Edit `0014 §10` + `proposals/README.md` L66 | 0014 从"纸面 merged"变"git 事实 merged";working tree dirty 从 ~13(Zentao)→ 0 |
| B2 | **P0** | **补 0014 §7 勾错的 4 份测试**:ZentaoConnectorAdapterTest(MockServer ping 200/401)、ZentaoWebhookControllerTest(验签 ok/fail + 幂等 externalEventId)、ZentaoInboundSyncServiceTest(payload→落库 + stale 跳过)、ZentaoOutboundSyncServiceTest(SyncContext.inbound 拦截 + 防抖 + last-write-wins);**或**把 §7 的 `[x]` 改回 `[ ]` 诚实标注未做 | 新建 4 测试文件;若不补则 Edit `0014 §7` | 测试覆盖与 proposal 声明一致;防回环三道防线全部有测(目前只有 SyncContext 单测) |
| B3 | **P0** | proposal 模板 §10"实际 PR/commit"加**强制校验语义**:状态置 `merged` 时,merged commit hash 字段**不允许为空/待填**;无 commit 只能停在 `accepted`/`implementing`。沉淀到 README 生命周期图:**merged = 存在 merged commit 的 git 事实,不是决议** | **→ proposal 0021-proposal-merged-requires-commit.md**(0001-0099 流程段);改 `0000-template.md` + `README.md` 生命周期定义 | 未来 proposal 不再出现 merged/tracking 但 commit 待填;tracking 期不空转 |
| B4 | **P1** | **集成连接器 SOP 固化为 skill**:`~/.claude/skills/integration-connector/`,脚手架含 ConnectorAdapter(ping/验签/token 缓存)+ WebhookController(@Anonymous+验签+落 event)+ Inbound/OutboundSyncService(SyncContext 防循环 + last-write-wins 模板)+ 4 业务 Event 钩子 + 测试骨架 + 专用设计文档模板(C4+字段映射+状态机+错误码+时序图)。触发语:"接入 XX 第三方系统做同步" | **→ proposal 0019-integration-connector-skill.md**(与主线 0015 plm-module-uplift skill 并列,**不合并**:0015 是业务模块 SOP,0019 是集成 SOP,正交) | 下一个 connector(Jira)从 ~1 week → ~2 day;跨会话第 4 个 connector 走 skill 路径 |
| B5 | **P1** | **双向同步通用防护清单沉淀为 gotcha/quirks**:把"防回环三道防线(ThreadLocal inbound 标志 / N 秒防抖 / last-write-wins + FOR UPDATE)+ 幂等键(external_source+external_id 唯一索引,NULL 不参与约束)+ webhook 幂等(externalEventId 带时间戳)"写成"集成类通用反模式防护"段 | **→ proposal 0020-bidirectional-sync-loop-guard-gotcha.md** + 改 `memory/project-quirks.md` 加 Q-INTEG-01 段;并扩 `.claude/rules.md §L.1` gotcha 触发条件:加"主动设计出的通用正确范式"语义 | 下一个双向集成不再从零想防回环;§L.1 能收"正确范式"不只收"坑" |
| B6 | **P1** | **补派生 ADR**:为"引入进程内领域事件总线(ApplicationEvent + @TransactionalEventListener AFTER_COMMIT,否决 Kafka)"写 `ADR-0002`;为"集成层回写旁路业务 Service(裸 JDBC,因外部状态机≠PLM 状态机)"写 `ADR-0003`。**顺手修 ADR 目录两个 0001-* 撞号** | Edit/新建 `03-开发/ADR/0002-*.md` + `0003-*.md`;重命名撞号文件 | ADR 不再停在 0001;架构类 proposal(0014)有对应 ADR 落点 |
| B7 | **P1** | meta-cognitive V3 触发表 + signals 加**"跨会话同类工作类型累计"信号**:按工作类型(集成 / 业务模块 / 基础设施)分桶计数,某桶累计 ≥3 → nudge"该抽 skill 了"。补主线 A2"单会话高频"的盲点(集成是跨会话低频高单价) | **→ Edit `.claude/agents/meta-cognitive.md`** V3 段(不走 proposal)+ `signals/README.md` 加"工作类型分桶"维度 | 下一个(第 4 个)connector 在动手前就被提示"已 3 个同类,先 skill 化" |
| B8 | P2 | 扩 proposal 0016 的 business-*.sql lint:除"必须含 sys_menu INSERT"外,加"**INSERT 的 component 路径 + perms 必须有对应前端 view 文件 / 后端 @PreAuthorize 端点**",拦 O8 类"发空菜单" | 并入 **proposal 0016**(主线已立) scope | business-*.sql 不再发指向虚空的菜单;O8 类未来 4 周 0 复发 |
| B9 | P2 | "proposal→ADR 派生"加弱触发器:架构类 proposal 置 merged 时,Stop hook/checklist 提示"是否需派生 ADR?";补 §L.1 与 README §文档关系的衔接 hook | 并入 **proposal 0021** scope 或 Edit `.claude/settings.json` Stop hook | 架构类 proposal merged 后不再漏 ADR(模式 4 根因 5 的衔接补上) |

### 行动依赖图

```
B1 (雪球落盘 + 回填 commit) ──┬──→ B2 (补 4 测试 / 或诚实改 [ ])
                            └──→ B3 (proposal merged 须绑 commit) ──→ B9 (proposal→ADR 弱触发)
                                                                       ↑
B6 (补 ADR-0002 事件总线 / ADR-0003 旁路 Service) ─────────────────────┘

B4 (集成 connector skill 0019) ──┐
                                ├──→ 下一个 connector(Jira)验证 SOP 收敛
B7 (跨会话工作类型分桶信号) ──────┘     (B7 喂信号给 B4 触发时机)

B5 (回环防护 gotcha 0020) ──→ 喂给 B4 skill 的"防护清单"段

B8 (sql lint 扩 component 存在性) ── 并入主线 0016,独立推进
```

依赖要点:**B1 是一切的前提**(代码不落盘,B2 测试无意义、0014 tracking 空转、B6 ADR 描述的架构还在 working tree)。B4(集成 skill)依赖 B5(防护清单)作为其核心内容章节。B7(跨会话信号)是 B4 的"何时该抽"触发器,补主线 A2 的盲点。

---

## 4. 关注下周(W23)

- [ ] **B1 雪球落盘必须本周内完成,不与主线 6 模块雪球叠加滚到下周**(主线反思 §4 已挂"本日 working tree 雪球分批 commit 收尾",Zentao 是其中最大块 —— 两份反思在此处汇合,合并为一个收尾动作)
- [ ] 0014 §10 回填真实 commit hash + README 状态索引补 merged commit;否则 0014 的 tracking 期(→06-30)从一开始就是空转
- [ ] B2 决断:要么补 4 份测试,要么把 0014 §7 的 `[x]` 改 `[ ]`——**不允许 proposal 勾选与实际长期不符**
- [ ] B6 两条 ADR 起草(事件总线 / 旁路 Service),顺手修 ADR 目录 0001-* 撞号
- [ ] **真实禅道联调(0014 §7 Step 9 + 联调指南 §3 的 4 个用例,尤其 §3.4 防循环验证)** —— 这是验证三道防线在真环境是否真的不死循环的唯一手段;在此之前 §8 的"同步死锁次数 ≤0"是纸面指标
- [ ] proposal 0019(集成 skill)/ 0020(回环 gotcha)/ 0021(merged 须绑 commit)起草并走 solo-review

---

## 5. 链路

- **被反思对象**:[proposals/0014-zentao-bidirectional-sync.md](../proposals/0014-zentao-bidirectional-sync.md)(本报告大量引用其 §5 风险表 / §6 备选方案 / §7 实施计划 / §10 tracking)
- **被 0014 修订的上游**:[proposals/0007-mcp-integration-modules-uplift.md](../proposals/0007-mcp-integration-modules-uplift.md)(§1.2"先做单向"被改)+ [02-设计/MCP-集成-设计.md](../../02-设计/MCP-集成-设计.md)
- **配套设计文档**:[02-设计/Zentao-集成-设计.md](../../02-设计/Zentao-集成-设计.md)(15 节,含 §3.1 自纠 DDL 坑)+ [02-设计/Zentao-集成-联调指南.md](../../02-设计/Zentao-集成-联调指南.md)
- **同周主线反思(本报告补其 §6 挂账项)**:[2026-W22-modules-bulk-uplift.md](2026-W22-modules-bulk-uplift.md) —— 两份在 **Pattern 3(sql menu lint)** 和 **Pattern 4(未提交雪球)** 上交汇:本报告 O8 是主线 Pattern 3 的反向表征、模式 3 是主线 Pattern 4 的最大且更危险的实例
- **触发的提案**:**0019**(集成 connector skill,与主线 0015 并列)+ **0020**(回环防护 gotcha)+ **0021**(proposal merged 须绑 commit);主线已预留 0017/0018 给 reflect-template / schema-impact-scanner,本报告**不占用 0017/0018**(说明见末段)
- **涉及规范沉淀**:`.claude/rules.md §L.1`(gotcha 触发过窄 + ADR 衔接靠自觉)/ `03-开发/ADR/`(缺事件总线 ADR)/ `memory/project-quirks.md`(缺集成类通用防护段)

---

## 6. 自检 — 反思方法论本身

> meta-cognitive 第 2 层自指:本专项反思是否符合自身原则?

| 原则 | 自检结果 |
|---|---|
| 基于证据不空想 | ✅ 10 条观察全部有硬证据:O1/O2 据 git status `??` 段 + Read 确认文件存在;O4 据 Glob 测试目录(仅 2 文件 vs 声明 4);O5 据 README L66 + 0014 §10 原文;O6/O8 据 Glob "No files found";O9 据设计文档 L92-120 与 SQL L16-20 比对;F4 据 Grep `0014` 无 ADR 命中;模式 4 据 `.claude/rules.md §L.1` + ADR/README §何时新增原文 |
| 可执行不抽象 | ✅ 9 条行动均带 file 路径 / proposal 编号(0019/0020/0021)/ 预期信号;B1 给出按 0014 §5 的 5 轮提交切法 |
| 承认局限 | ⚠ **三点未核实到位**:(1)**无 Bash 工具**,本环境只有 Read/Grep/Glob,tracked/untracked 状态据**会话起始 git status 快照**推断,未实跑 `git log`/`git diff` 核对 commit hash 与精确行数(行数用 Read 行号近似);(2)O3 "RequirementServiceImpl 改动不在 M 列表但已插 publishEvent"——可能它已在更早 commit 提交、也可能快照截断(git status 原文被 truncate),**未能区分**;(3)真实禅道未联调,三道防线"在真环境不死循环"仅基于代码静态阅读 + SyncContextTest 单测,**运行时正确性未验证** |
| 不动手 | ✅ 本报告纯草稿,全程只用 Read/Grep/Glob,无任何 Edit/Write;由主会话落盘到 `99-跨阶段/reflect/2026-W22-zentao-integration.md` |

**与主线反思的边界**:本报告刻意只诊断"非批量主线但工作量大"的工作线(Zentao + RequirementReview 辅线),不重复主线已覆盖的 6 模块 SOP(模式 1)/ schema N 阶导数(模式 2)/ SESSION_LOG 缺位(模式 5)。交汇点(雪球收尾、sql lint)显式指回主线对应 Pattern,避免重复立项。

---

## 7. 建议转 proposal 的行动项 + 建议编号

> 现有最大编号 0016;主线反思已把 **0017 预留给 reflect-template-sop-detection、0018 预留给 schema-impact-scanner**(均为主线候选,本报告与之无内容重叠,故**不合并、不占用 0017/0018**,从 **0019** 起新开)。

- **proposal 0019 — `integration-connector-skill.md`(P1,号段 0200-0299 工具链/skill)**:把集成连接器三件套(ConnectorAdapter + WebhookController + Inbound/OutboundSyncService + 业务 Event 钩子 + 专用设计文档模板 + 测试骨架)固化为 skill。与主线 0015(plm-module-uplift skill)**并列不合并** —— 0015 是"业务模块 🟡→🟢"SOP,0019 是"集成同步"SOP,两条 SOP 正交。证据:飞书/GitLab/禅道已是同一 ConnectorAdapter 范式的第 1/2/3 次复用(F1 + O1)。

- **proposal 0020 — `bidirectional-sync-loop-guard-gotcha.md`(P1,号段 0300-0399 架构/技术债,或并入 0019 作为其"防护清单"章)**:把"双向同步回环防护三道防线 + 幂等键设计 + webhook 幂等"沉淀为集成类通用反模式防护;同时扩 `.claude/rules.md §L.1` 让 gotcha 触发条件能收"主动设计出的正确范式"(现仅收"踩过的坑")。证据:F2 + SyncContext/recentSyncCache/checkStaleAndUpdate 三处代码 + SyncContextTest。

- **proposal 0021 — `proposal-merged-requires-commit.md`(P0,号段 0001-0099 流程)**:proposal 状态 `merged` 必须绑定真实 merged commit hash,§10 不允许"待填/待开";无 commit 只能停 `accepted`/`implementing`。修 `0000-template.md` + README 生命周期定义("merged 是 git 事实非决议")。证据:O5(0014 merged 但 commit 待填、PR 待开、Step 8/9 未做、测试不存在),这是主线 Pattern 4 在 proposal 状态机上的恶化表征。

- **(并入主线 0016,非新提案)**:business-*.sql lint 扩"菜单 component 路径 + perms 须有对应前端 view / 后端端点"的存在性校验,拦 O8 类"发指向虚空的菜单"。证据:O8(sql 发了 user-mapping 菜单 + 6 权限,但前端页 / Controller 均不存在,设计文档自承不做 UI)。

- **(补做,非提案,走 §L.1 既有 ADR 流程)**:补 `ADR-0002`(引入进程内领域事件总线,否决 Kafka)+ `ADR-0003`(集成层回写旁路业务 Service 的理由),并修 `03-开发/ADR/` 两个 `0001-*` 撞号文件。证据:F4 + F5 + O2(0014 全文无 ADR、引入事件机制 + 旁路分层均为不可逆架构选型,触发 `.claude/rules.md §L.1` 与 ADR/README §何时新增)。

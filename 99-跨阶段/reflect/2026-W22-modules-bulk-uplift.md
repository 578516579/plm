# 2026-W22 反思 — 模块批量改造日的 SOP 显形

## 头部

| 字段 | 值 |
|---|---|
| 周次 | 2026 第 22 周(2026-05-25 ~ 2026-05-31)首日截面 |
| 执行者 | 手动 — meta-cognitive Agent + Wjl(2026-05-25 夜) |
| 触发 | 单日 30+ commit + ~200 单测 case + 6 模块同 SOP 复用 → ROI 异常,显形"未固化的隐式 SOP" |
| 关联 signals | 待月报 2026-05.md §F 新增"SOP 复用率"维度 |
| 关联 proposals | 0015(skill 固化)/ 0016(SQL 模板 lint)/ 0017(reflect 模板检测 — P2 候选) |

---

## 1. 观察(Observations)

### 1.1 数据级事实

| # | 事实 | 与过去对比 | 新现象? |
|---|---|---|---|
| O1 | 单日 commit ≥ 30(主线 6 模块 × ~4 commit + 辅线 5 + 在途未提交 ~10 件套) | 上周首日均值 ~5,周内峰值 ~8 | ✅ **6× 跃迁** |
| O2 | 单测累计 ~200 case 一日产出(dashboard 20+inception 28+competitive 28+ued 28+apidesign 35+prd 33,在途 dbdesign ~30) | 之前最高单日 ~50 | ✅ **4× 跃迁** |
| O3 | E2E spec 1→11 case 扩展在 5 模块上重复执行(inception/competitive/prd/UED/dashboard) | W21 全周仅 1 次 | ✅ **规模化** |
| O4 | Gate 实例新增 ~20 个(8 模块 × 3-4 个 Phase) | 全 W21 新增 0 | ✅ **0→20 阶跃** |
| O5 | PRD-MAPPING.md §1 状态色 🟡→🟢 单日翻 6 个 | 每周节奏 ~1-2 个 | ✅ **3-6× 节奏** |
| O6 | 同一 SOP(@Nested 6 项 + 单测 28-35 case + E2E 1→11 + Gate 3-4 个 + 状态色 + 台账 + `[solo-review]` commit)在 6 模块严格复用 | 之前 SOP 是隐式 | ✅ **首次显形** |
| O7 | 同日踩 2 个同类坑:81bc1ba business-ued.sql 漏 sys_menu INSERT + 5c4e70d dashboard 按钮 404(menu URL 变了硬编码全失效) | 同类坑过去 7 天 0 次 | ✅ **schema 改动 N 阶导数破坏** |
| O8 | working tree dirty ~25 件(Zentao 集成 + RequirementReview + plm-arch/dbdesign 测试 + MCP 设计文档堆一起) | 往日 < 5 | ✅ **未提交雪球 5× 比往日大** |
| O9 | SESSION_LOG.md 跨 session append-only 文件**不存在**(memory/session_protocol.md V1 设计要求落地) | 协议设计完未落地 | 🔴 **协议落地缺位** |

### 1.2 质性事实

- **F1** — 6 模块 commit message 句式 1:1 同构(`feat(<module>): ServiceImpl 增强 + 单测 N case 全绿 + Gate 实例 M 个 [solo-review]`),模板成型**但没人去看模板**。
- **F2** — 在途任务.md 每行带"路径互不重叠"声明,显示并行协调成本上升,**但全凭 Claude 自觉声明,无自动校验工具**。
- **F3** — schema 改动 N 阶导数效应初次显形:menu-regroup → 前端硬编码 404 → 7b14807 抽 SSoT。**反向链路无 hook**:下次改 sys_menu 时无人提醒"先 grep 前端硬编码"。

---

## 2. 诊断(Diagnoses)

### 模式 1:SOP 隐式成熟,具备"应固化为 skill"的所有条件,但触发器缺位

- **现象**:O6/F1。6 模块走完全一致的 SOP,Claude 实际在脑里跑了 6 次相同模板,**没产出 skill 文件,也没产出 prompt template**。
- **根因(5 Whys)**:
  1. 为什么没固化? — SOP 是边做边浮现的,前 3 个模块还在试探正确性。
  2. 为什么试探完没固化? — 没有"N=3 同类任务后必须停下来抽 skill"的触发规则。
  3. 为什么没触发规则? — meta-cognitive Agent V3 触发节点只覆盖"PR 闭环 / mark_chapter / 季度",未覆盖"单会话内同类高频"。
  4. 为什么没覆盖? — 当时设计 V3 没预想到单日 6 个模块的节奏。
  5. **根本根因** — Claude 觉得"反正脑里有",但**第 7 个模块来时上下文一丢就重新发明**;ruoyi-bootstrap skill 已证明固化 SOP 为 skill 是高 ROI,但缺一条"高频信号→自动 nudge 抽 skill"反馈链。
- **涉及规范**:`.claude/agents/meta-cognitive.md` V3 触发段 + `.claude/rules.md §L` 自进化机制

### 模式 2:Schema-driven 改动产生 N 阶导数破坏,扫描机制缺位

- **现象**:O7/F3。`5c4e70d` 揭示:sys_menu URL 改了,前端硬编码 router.push('/business/...) 失效。7b14807 抽 SSoT **是症状治疗,不是预防机制**。
- **根因**:**所有 schema 类改动**(sys_menu 重组 / business-*.sql 字典扩展 / 字段重命名)都缺"下游影响清单生成器" — 应在 SQL 改动 commit 时自动列出"这次改动影响的前端文件清单",而非靠人事后 grep。
- **涉及规范**:`CLAUDE.md` gotcha 表 + `.githooks/pre-commit`

### 模式 3:business-*.sql 模板不完整 → 同类漏菜单坑反复

- **现象**:O7 中 `81bc1ba` "补 business-ued.sql 漏写的 sys_menu INSERT"。此前 dashboard/competitive 估计也有类似,被 menu-fill-missing-8.sql 兜底掩盖。
- **根因**:business-*.sql 文件**无 lint,无模板校验**。每个模块由不同会话起草,有的写完整(sys_menu+sys_role_menu),有的只写字典表。模板存在但未规约。
- **涉及规范**:`CLAUDE.md` Running locally 段、`03-开发/开发规范.md` SQL 章

### 模式 4:未提交工作量雪球 → 回滚单元失稳

- **现象**:O8。Zentao 集成(plm-integration 5 件套 untracked)+ RequirementReview 子表+UI + plm-arch/dbdesign 测试 untracked 共 ~25 文件 dirty,与主线 6 模块改造**叠加同 branch 同 worktree**。
- **根因**:协作规范 §6 有"单 PR 单模块原则",**但缺"单 commit 单话题原则"**。30+ commit 节奏下自觉断点变模糊。**缺"working tree dirty > N 时自动 nudge commit"的 hook**。
- **涉及规范**:`协作规范.md §3` + `.claude/settings.json` Stop hook

### 模式 5:SESSION_LOG 协议有设计无落地

- **现象**:O9。本日工作量等于一次小型 sprint,但 99-跨阶段/SESSION_LOG.md **不存在**。下个会话只能靠 git log + 在途任务.md 反推,**会失上下文**。
- **根因**:协议设计完未植入"session 收尾时自动 append"工作流。session-handoff Agent (proposal 0009) 设计了**读**端,**写**端的触发点没人接。
- **涉及规范**:`memory/session_protocol.md` + `.claude/agents/session-handoff.md`

---

## 3. 行动(Actions)

| # | 优先级 | 行动 | 载体(文件/proposal) | 预期信号 |
|---|---|---|---|---|
| A1 | **P0** | "PLM 模块从空壳→PRD-aligned"SOP 固化为 skill,模板含 @Nested 6 项 + 单测 28-35 case 骨架 + Gate 实例脚手架 + PRD-MAPPING 状态色 + 在途任务台账 + commit 句式 + `[solo-review]` 标签 | **→ proposal 0015-plm-module-uplift-skill.md**(参照 ruoyi-bootstrap skill 范式;skill 路径 `~/.claude/skills/plm-module-uplift/`)| 下一个 🟡→🟢 改造 ~30 min/模块 → ≤ 10 min;同周 ≥ 3 模块走 skill 路径 |
| A2 | **P0** | meta-cognitive Agent V3 触发表加第 5 条:"单会话内同类任务 ≥ 3 次重复(基于 commit 句式或 file path 模式)→ 主动 nudge:'要不要现在抽 skill?'" | **→ Edit `.claude/agents/meta-cognitive.md`** V3 制度化触发段;不走 proposal(meta-cognitive.md 不在 §2 SSoT) | 下次"6 同类模块流水线",第 3 个模块完成时主动提议固化,不等 6 个全做完 |
| A3 | **P0** | git pre-commit hook:business-*.sql 文件必须含 `INSERT INTO sys_menu` + `INSERT INTO sys_role_menu`,否则报警 | **→ proposal 0016-business-sql-template-lint.md**(号段 0200-0299 工具链);载体 `.githooks/pre-commit` 新增脚本片段 | 漏菜单坑(81bc1ba 类)未来 4 周 0 复发 |
| A4 | P1 | CLAUDE.md gotcha 表加第 7 条:"sys_menu URL 改动 → 必跑前端硬编码 grep 扫描"。.githooks/pre-commit 在 detect `sql/menu-*.sql` 变更时自动跑 grep 并粘 commit body | **→ Edit `CLAUDE.md` gotcha 表** + 同 proposal 0016 范围 | 下次 sys_menu schema 改动**不再**在前端产生 404 类 N 阶导数 bug |
| A5 | P1 | working tree dirty > 15 时 Stop hook nudge:"建议分批 commit,N 件分散在 M 个模块"。**不强制阻塞,只提醒** | **→ Edit `.claude/settings.json` Stop hook**;不走 proposal(纯 hook 调整) | 单日最大 dirty 文件数 ~25 → ~10 |
| A6 | P1 | session-handoff Agent 在"会话末段"主动产出 SESSION_LOG.md append 草稿,用户确认后 commit(挂 chore(handoff) message) | **→ Edit `.claude/agents/session-handoff.md`** 加"写端职责"段 | SESSION_LOG.md 本周首次落地;下个会话 30 秒内通过该文件理解今日 30+ commit 上下文 |
| A7 | P2 | reflect/YYYY-WW.template.md 加第 6 段"SOP 显形检测":本周 commit 是否出现 N=3+ 同模板复用,若是标 "🔔 候选 skill 化" | **→ 候选 proposal 0017-reflect-template-sop-detection.md** | reflect 主动捕捉"隐式 SOP 成熟"信号,不再靠 meta-cognitive ad-hoc 触发 |
| A8 | P2 | 抽象"schema-driven 改动 → 下游影响清单"工具(SQL diff → 影响文件 grep 路径列表) | **→ 候选 proposal 0018-schema-impact-scanner.md**;**条件转化**:A3+A4 落地后观察 4 周,若仍出现 ≥1 次 schema-driven 下游破坏才立项 | 暂不立项,先观察 |

### 行动依赖图

```
A1 (skill 固化) ──┐
                 ├──→ 下一个 🟡→🟢 改造直接复用,验证 SOP 收敛
A2 (meta-cog 触发) ─┘

A3 (sql lint) ─→ A4 (sys_menu grep) ─→ A8 (schema impact 扫描,先观察)

A5 (dirty 文件 nudge) — 独立改进
A6 (SESSION_LOG 写端) — 接 A2 触发器
A7 (reflect 模板) ─→ 喂给 A2 meta-cognitive 信号
```

---

## 4. 关注下周(W23)

- [ ] 验证 A1 skill 在"下一批 🟡→🟢"(testdata / autotest / analytics / aiagent / openspec / pipeline / featureflag / dora / manualimpl / manualops 至少 1 个)能否减少耗时
- [ ] A3+A4 hook 触发计数:若 0 次说明无人改 sql/menu(预期);≥1 次看是否拦下 81bc1ba 类问题
- [ ] **本日 working tree 雪球分批 commit 收尾**(Zentao 5 件套 / RequirementReview / plm-arch+dbdesign 测试) → **不拖到下周**
- [ ] proposal 0015 / 0016 起草并走 solo-review

---

## 5. 链路

- 上周反思:[2026-W21-session-handoff-agent-dogfood.md](2026-W21-session-handoff-agent-dogfood.md) — 触发 0009/0010,本周延续"Agent + 协议落地"路径
- 触发的提案:**0015**(skill 固化)+ **0016**(sql 模板+menu grep lint)+ 0017(reflect 模板检测候选)+ 0018(schema impact scanner 候选,条件转化)
- 关联 Sprint 回顾:本日工作量 ≈ 一次小型 sprint,**建议在 03-开发/Sprint 计划与回顾/ 加 "2026-W22-单日批量回顾"**(突破"按周回顾"惯性)
- 协议对照:[memory/session_protocol.md](../../memory/session_protocol.md) V1 / [协作规范.md](../协作规范.md)
- quirks 同步:`memory/project-quirks.md` 已加 Q-DB-04 / Q-BIZ-04 + 流程候选 P-FLOW-2026-05-25(本日)

---

## 6. 自检 — 反思方法论本身

> meta-cognitive 第 2 层自指:本反思是否符合自身原则?

| 原则 | 自检结果 |
|---|---|
| 基于证据不空想 | ✅ 10 条观察均有 commit / 数据 / 文件路径支撑 |
| 可执行不抽象 | ✅ 8 条行动均含 file 路径 + proposal 编号 + 预期信号 |
| 承认局限 | ⚠ "新现象 vs 已有模式"区分基于 W20-W21 两周数据,样本仍少;O9 SESSION_LOG.md 不存在为推断,未 Glob 全仓 |
| 不动手 | ✅ 本报告纯草稿,无 Edit/Write 操作(由主会话落盘) |

**未覆盖场景**:今日的 Zentao 集成 / RequirementReview 评审环节 / proposal 0014 双向同步设计,因不在"6 模块批量改造"主线 SOP 内**未单独诊断**。下次反思应单独审视"非批量主线但工作量大"的工作怎么承接(辅线 5 commit + 在途未提交 10 件套是否也藏 SOP)。

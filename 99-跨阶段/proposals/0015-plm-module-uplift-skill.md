# Proposal 0015: 把 "PLM 模块空壳→PRD-aligned" SOP 固化为 `plm-module-uplift` skill

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0015 |
| 标题 | 把 "PLM 模块空壳→PRD-aligned" SOP 固化为 `plm-module-uplift` skill |
| 状态 | **draft** |
| 类型 | 流程 |
| 提出人 | Claude(meta-cognitive 经 `/reflect-weekly` 2026-W22)+ Wjl |
| 提出日期 | 2026-05-25 |
| 评审人 | Wjl(solo-review) |
| 评审日期 | 待 |
| Tracking 截止 | 2026-06-22(merged 后 4 周) |

---

## 1. 背景(What's the problem?)

2026-05-25 单日 6 个模块(dashboard / inception / competitive / ued / apidesign / prd)从 🟡 空壳 → 🟢 PRD-aligned,每个走**严格相同**的 SOP:`ServiceImpl @Nested 6 项增强` → `ServiceImplTest 单测 28-35 case` → `前端 E2E spec 1→11 case` → `Gate 实例 Phase 01/02/03(/04)` → `PRD-MAPPING §1 状态色 + §7 路线图 + §9 修订记录` → `在途任务.md` → `feat(<m>): ... + 单测 N case 全绿 + Gate M 个 [solo-review]` commit。SOP 模板成型但**仍以隐式形式分布在 Claude 上下文**,**未固化为可触发资产**。下一批待改造的 🟡 模块还有 ~10 个(testdata / autotest / analytics / aiagent / openspec / pipeline / featureflag / dora / manualimpl / manualops),手动平均 ~30 min/模块,总耗 ~5 小时,**每次会话上下文一切换就重新发明 SOP**。

---

## 2. 证据(Evidence)

- **数据级**:今日 git log 主线 6 模块各 ~4 commit 1:1 同构(`4e107de feat(prd) ...` / `fa20577 test(prd) ...` / `bcf7ce6 docs(gate) ...` / `9755071 docs(mapping) ...` 复用同句式 6 遍),累计 ~200 单测 case 一日产出。
- **reflect 报告**:[reflect/2026-W22-modules-bulk-uplift.md](../reflect/2026-W22-modules-bulk-uplift.md) **模式 1**"SOP 隐式成熟,具备应固化为 skill 的所有条件,但触发器缺位",**根本根因**:"Claude 觉得反正脑里有,但第 7 个模块来时上下文一丢就重新发明" + **A1 行动**(P0)直接指向本提案。
- **范式先例**:[~/.claude/skills/ruoyi-bootstrap/SKILL.md](C:/Users/Wjl/.claude/skills/ruoyi-bootstrap/SKILL.md) 已证明 — RuoYi 脚手架→正名项目从手动 ~2h 降到 skill 触发 ≤15 min,**且首次会话不需要重新教育**。本提案套用其 Phase 0-7 结构。
- **关联提案**:[0016-business-sql-template-lint](0016-business-sql-template-lint.md)(待起草)— skill 内置的 `business-<module>.sql` 模板必须同时被 pre-commit hook lint 验证,避免 81bc1ba/dashboard 404 类菜单漏写坑复发。
- **用户请求**:2026-05-25 会话用户明确要求"起草 proposal 0015"。

---

## 3. 提案(What's the change?)

### 3.1 skill 物理路径与文件清单

```
~/.claude/skills/plm-module-uplift/
├── SKILL.md                        ← frontmatter description 触发词 + Phase 0-7 主流程
├── references/
│   ├── service-impl-nested.md      ← @Nested 6 项(GenerateNo×4 / Validation / Defaults / StateMachine / AiGenerate / Delete)模式说明
│   ├── unit-test-cases.md          ← 28-35 case 矩阵的规模选择规则与典型 case 名清单
│   ├── e2e-spec-11case.md          ← Playwright E2E spec 11 case 设计(CRUD + 状态机 + AI + 字典 + 列表)
│   ├── gate-instance-fill.md       ← Phase 01/02/03/(04) 实例 .md 各段必填项
│   ├── prd-mapping-update.md       ← §1 状态色 / §7 路线图挪入 / §9 修订记录三处同步动作
│   └── commit-message.md           ← `feat(<m>): ... + 单测 N case 全绿 + Gate M 个 [solo-review]` 模板及变体
└── assets/templates/
    ├── ServiceImplTest.java.tpl    ← 6 @Nested 占位 + import / setup / mockito 骨架
    ├── e2e-spec.ts.tpl             ← 11 case 占位(test.describe 嵌套 + helpers/api 引用)
    ├── gate-phase01.md.tpl         ← 立项 Gate
    ├── gate-phase02.md.tpl         ← 需求与设计 Gate
    ├── gate-phase03.md.tpl         ← 开发与测试 Gate(代码骨架 DoD,见 proposal 0004)
    ├── gate-phase04.md.tpl         ← 测试稳定 Gate(可选)
    ├── business-__module__.sql.tpl ← 含 sys_menu + sys_role_menu + 字典 INSERT(关联 proposal 0016 lint)
    └── commit-template.txt         ← commit message 句式
```

### 3.2 SKILL.md frontmatter description(草案)

```
name: plm-module-uplift
description: "Uplift a PLM business module from 🟡 (empty shell) to 🟢 (PRD-aligned).
Runs the established SOP that turned dashboard/inception/competitive/ued/apidesign/prd
green on 2026-05-25: ServiceImpl @Nested 6-cluster + Mockito unit tests 28-35 cases +
Playwright E2E spec extended 1→11 cases + Gate instances Phase 01/02/03 + PRD-MAPPING.md
§1/§7/§9 sync + 在途任务.md ledger entry + standardized [solo-review] commit. Invoke
whenever the user says 把 X 模块从 🟡 改成 🟢, PRD-aligned <module>, 🟡→🟢 落地,
下一个模块批量改造, uplift module, or names any module in 🟡 list."
```

### 3.3 Phase 0-7 主流程(SKILL.md 主体)

| Phase | 名称 | 关键动作 | 验证 |
|---|---|---|---|
| **0** | Pre-flight | `git status` 干净 + 询问模块名 + 查 PRD-MAPPING §1 确认当前色为 🟡 + 询问阶段(规划 / 需求与设计 / ...)+ 查 sys_menu 父 ID(2900-2970 段) | echo 配置后用户 confirm |
| **1** | 模板渲染 | 用模块名替换 `assets/templates/` 全部占位符 → 写入目标路径(plm-backend/plm-`<m>`/src/test/...、plm-frontend/e2e/`<m>`.spec.ts、99-跨阶段/gate-checklists/instances/`<m>`/...、plm-backend/sql/business-`<m>`.sql) | 文件已写入 + 占位无残留(grep `__MODULE__` 应 0 命中) |
| **2** | ServiceImpl 增强 | 按 `references/service-impl-nested.md` 在现有 ServiceImpl 嵌入 6 @Nested 类骨架,引导用户填业务逻辑(skill 不替代业务理解) | 编译通过 `mvn compile -pl plm-<m> -am -q` |
| **3** | 单测填充 | 在 ServiceImplTest.java 骨架上,根据模块复杂度选 case 数(简单 28 / 中等 32 / 复杂 35;见 `references/unit-test-cases.md` 矩阵)→ 用户实现 case body | `mvn test -pl plm-<m> -q` 全绿 |
| **4** | E2E spec | 在 e2e/`<m>`.spec.ts 骨架上完成 11 case(API helpers 用 e2e/helpers/api.ts) | `npx playwright test e2e/<m>.spec.ts` 全绿(可后置) |
| **5** | Gate 实例 + business-sql | 3-4 个 Gate Phase 实例 .md 按模板填(分级理由必填,见 proposal 0003)+ business-`<m>`.sql 含 sys_menu INSERT(将被 proposal 0016 hook lint 校验) | 实例 .md commit 时 commit-msg hook 通过 |
| **6** | PRD-MAPPING 三段同步 | §1 状态色 🟡→🟢 / §7 路线图条目挪入"完工"列 / §9 修订记录加行 + 在途任务.md 加完工行 | grep `<module>.*🟢` 在 §1 命中 |
| **7** | commit & 自检 | 按 commit-template.txt 句式 commit + 校验 `[solo-review]` 标签 + 自检 PRD-MAPPING §1 该模块所有 9 项 DoD 是否打勾 | commit-msg hook 通过 + DoD 9 项 ≥7 项打勾(剩 ≤2 项标"后置说明") |

每个 Phase 可逐步验证、可暂停、可独立回滚(`git reset --hard HEAD~1`)。

### 3.4 触发关键词(写入 description 段,语义化触发)

`把 X 模块从 🟡 改成 🟢` / `PRD-aligned <module>` / `🟡→🟢 落地` / `下一个模块批量改造` / `uplift module` / 直接说出 🟡 模块名(testdata / autotest / analytics 等)。

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| 开发者(Wjl)| 多 1 个 skill 资产,下次 🟡 模块改造从"翻今日 6 个 commit 看怎么干"→"一句话触发,~10 min 完成骨架";不强制使用,可绕过 |
| Claude | 触发后省 4-6 轮决策(模板从哪抄、case 选 28/35、commit 句式、Gate 几个) → 上下文节省 ~20K tokens/模块;**风险**:模板过于刚性时,Claude 会被诱导套模板而忽略模块独特性(见 §5) |
| 已完工 6 模块(dashboard 等)| 反向校准:写完 skill 后回看 6 模块产出物是否完全匹配模板,若有差异 → 要么模板做向后兼容,要么 6 模块补差异;**预期 ≤2 处小差异** |
| 待改造 10 模块 | 直接享受 skill 红利;testdata/autotest 这类与 6 已完工**业务不同**的模块,skill 只搭骨架,业务实现仍需人/Claude 写 |
| 已有规范文档 | 无破坏;`CLAUDE.md` "Available skill" 段从 1 个 skill(ruoyi-bootstrap)→ 2 个,需加一行链 |

---

## 5. 风险(Risks)

| 风险 | 缓解 |
|---|---|
| **R1 模板过于刚性,模块独特性被压扁**(如 dashboard 6-widget 聚合首屏 / aiagent 流式 SSE / dora DORA 指标聚合 — 这些与 CRUD 模板 1:1 复用度低)| skill Phase 0 显式问"该模块是否纯 CRUD?",若否 → skill 仅生成 Gate + PRD-MAPPING 同步 + commit 段,跳过 Phase 2-4;**SKILL.md 反模式段**写明"skill 只搭骨架,业务逻辑仍要人写,不要为套模板而扭曲实现" |
| **R2 模板漂移**(项目 SOP 之后改了,skill 没改 → skill 产出物开始落后)| 在 [.claude/rules.md §L](../../.claude/rules.md) 加一条:"修改 ServiceImpl @Nested 6 项 / 单测模板 / E2E 11 case / Gate 实例段落结构,必须同步 `~/.claude/skills/plm-module-uplift/`,否则 PR 拒收";reflect 月报新增"skill 落后周数"信号 |
| **R3 与 ruoyi-bootstrap 同路径同前缀,易混淆触发**(用户说"用若依新建模块"可能误中 plm-module-uplift)| description 触发词严格不重叠 — ruoyi-bootstrap 是"脚手架→正名"(P0 一次性),plm-module-uplift 是"空壳→PRD-aligned"(N 次复用);两个 SKILL.md 互相在末尾 cross-link 一句 |
| **R4 skill 触发后 Claude 仍要写大量业务代码,期望管理失败**(用户以为 skill = 一键完成)| description 显式写"Phase 2-3 业务逻辑由用户填,skill 仅搭骨架";Phase 0 echo 时再次提醒预计耗时 8-10 min(骨架)+ 业务逻辑时间(模块复杂度决定) |
| **R5 模板里的 sys_menu URL / 父 ID 硬编码**,后续 menu-regroup 又一次破坏 | Phase 5 模板生成 business-`<m>`.sql 时**从 sys_menu 实时查父 ID**(而非硬编码),与 proposal 0016 的 lint 双重防御 |

---

## 6. 备选方案(Alternatives Considered)

| 方案 | 描述 | 不选原因 |
|---|---|---|
| **A 现状(不固化)**| 继续手动,下一个模块再来一遍 | 下批 10 模块 ~5 小时;**且每次会话切换就忘** — reflect 模式 1 已识别为根本风险 |
| **B 写 prompt template,不做 skill**| 在 memory/ 加一份"PLM 模块改造 SOP.md",Claude 启动 mark_chapter 时读 | 不可触发(Claude 不会主动读未引用文件)、不可参数化、无 assets 模板分发能力 |
| **C 做成 bulk-refactor Agent 子能力**| 调用已有 `bulk-refactor` Agent,加一个 `--mode=plm-uplift` 参数 | Agent 是**单次触发,不持久**;skill 是常驻资产,description 自带触发匹配,适合"高频重复 SOP" |
| **D 把 SOP 写进 .claude/rules.md §M**| 用规则文档形式约束 | rules.md 是约束,不是模板分发渠道;且 §M 已有"PRD-align 落地走 §8 9 项 DoD",不需重复 |

**选 A 的替代 = 永远手动**,选 B/C/D 都是半截方案。**确定选当前提案的"做 skill"方案**。

---

## 7. 实施计划(Implementation Plan)

```
[ ] Step 1: solo-review approve(Wjl)— 2026-05-25 / 26
[ ] Step 2: 在 ~/.claude/skills/plm-module-uplift/ 创建目录 + SKILL.md + 6 references/ + 7 assets/templates/(Claude,~2h)
[ ] Step 3: 用 testdata 模块作为 pilot 跑一次 skill 全流程,验证 Phase 0-7 + 实测耗时 + 收集产出物与已完工 6 模块的结构 diff(Claude + Wjl,~30 min)
[ ] Step 4: 根据 Step 3 反馈调整模板(预期 ≤2 处小改),再跑 autotest 模块第二次验证(Claude + Wjl,~30 min)
[ ] Step 5: 在根 CLAUDE.md "Available skill" 段加一行链 + .claude/rules.md §L 加"模板漂移防御"条款(Claude + Wjl,~15 min)
[ ] Step 6: 把本 proposal 状态从 draft → merged,加入 proposals/README.md 索引表(Claude,~5 min)
[ ] Step 7: 进入 tracking 期(4 周),按 §8 信号定期采样
[ ] Step 8: tracking 结束 → done(指标达标)or reverted(模板被屡次绕过)
```

---

## 8. 衡量指标(How will we know it worked?)

| 信号 | 基线(2026-05-25 手动)| 目标(merged 后 4 周)|
|---|---|---|
| 单模块 🟡→🟢 骨架耗时 | ~30 min/模块 | ≤10 min/模块(skill Phase 0-7 骨架部分;不含业务逻辑) |
| 同周走 skill 路径的模块数 | 0 | ≥3(预计在 testdata / autotest / analytics 上首批用上) |
| skill 产出物 vs 已完工 6 模块产出物的结构一致度 | 不适用 | **≥95%**(diff `wc -l` + 段落标题对齐;允许 ≤5% 业务字段差异) |
| 跨会话"重新发明 SOP"次数 | ~1 次/会话(meta-cognitive 反思承认) | 0(skill 触发后 Claude 不再凭脑内记忆复述 SOP) |
| skill 触发后人工补救率(改完发现要重写大段)| 不适用 | ≤20%(若 >20% → 模板设计有问题,需迭代) |

跟踪期:**2026-05-25 ~ 2026-06-22**(merged 后 4 周)。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | 待 | | solo-review 模式 |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit
- PR: 待
- 合入 commit: 待
- 实际 merged 日期:待

### Tracking 数据

| 信号 | 基线 | 目标 | 实际(W1)| 实际(W2)| 实际(W3)| 实际(W4)|
|---|---|---|---|---|---|---|
| 骨架耗时(min)| 30 | ≤10 | | | | |
| 走 skill 模块数 | 0 | ≥3 | | | | |
| 结构一致度 | n/a | ≥95% | | | | |
| 重新发明 SOP 次数 | 1/会话 | 0 | | | | |
| 人工补救率 | n/a | ≤20% | | | | |

### 最终判定
- [ ] done(达成目标,本提案归档)
- [ ] reverted(若 ≥2 个模块绕过 skill 自己手写 → 模板设计失败,走回滚 PR + "失败提案"备忘)

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-25 | 王俊磊 / Claude(prompt-engineer Agent)| 初稿 V1.0 |

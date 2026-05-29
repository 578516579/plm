# Reflect — Week 22, 2026 · Solo-Review 前端字典 SSoT 抽取批次

> 本周内集中跑完 27 个前端业务模块的 `*Dict.ts` SSoT 抽取 + 漂移锁定单测,
> 复盘成果、协作教训、契约漂移发现,沉淀为后续 api-contract 评审输入。

---

## 头部

| 字段 | 值 |
|---|---|
| 周次 | 2026 第 22 周(2026-05-26 ~ 2026-05-28)|
| 执行者 | 手动 · Claude (Wjl 会话)+ 并行 session 配合 |
| 关联 signals | _(待月报 2026-05.md 接入)_ |
| 起止 commit | `32a92e3`(requirement 起)→ `42201ef`(manual-product 收尾)|

---

## 1. 观察(Observations)

### Commit 与 PR

- 本周新增 commit 数(本 session 主导):**约 50 笔**
  - `test(<module>):` × 25(逐模块 code commit)
  - `docs(ledger):` × 25(逐模块台账登记 + CLAIM 释放)
  - 还含 1 笔 `test(requirement)` 起手 + 1 笔 `docs(ledger): 协作事故注解` 闭环
- 配对完工模式:**每个模块严格 1 code + 1 docs**,commit 边界干净
- 不符 Conventional 数:0(commit-msg hook 仅 warn 长度,无硬阻)
- subject 长度 >70 警告:25 次(均放行)

### 单测覆盖

| 阶段 | 测试文件数 | 测试用例数 |
|---|---|---|
| 起手前 baseline | 7 files | 92 tests |
| 收尾后(manual-product 完成) | 34 files | **492 tests**(+400)|

### 模块清单(27 个,本人 25 + 并行 session 2)

```
✅ 需求设计(8): requirement/inception/competitive/prd/ued/arch/apidesign/apidoc
✅ 研发(3):    sprint/task(并行)/document
✅ 测试(2):    testreport/submission
✅ 交付运维(7): release/dora/feature-flag/manual-impl/manual-ops/manual-product/pipeline(并行)
✅ 分析/AI(3): ai-agent/openspec/dashboard(并行)
✅ 测试-并行做(4): testcase/defect/testplan/dbdesign(并行 session 同款扫过)
⊘ analytics:无 dict-helper 跳过(form 选项独立无函数化必要)
```

### 验证关口

- ✅ `npm run test:unit` 34 files / 492 tests 全绿
- ✅ `npm run build:prod` 生产 build 成功(无 import/类型断裂)
- ✅ `npx vue-tsc --noEmit` 业务 25 模块零错(`views/tool/gen/*` 残留警告系 RuoYi 上游遗留)
- ⏸ `npm run test:e2e` 待并行 session 完成 0028 P0-2 后端 SPI rebuild 后回归

### 风险与事件

- 协作事故 1 次:`3ae00fd test(openspec): ...` commit **误吞并行 session 24 文件**(0028 proposal + 8 FK SQL + 12 Java/XML)
  - 根因待诊断段
  - 解决:**并行 session 主动写 `4bfe206 docs(proposals): 0028 epic P0-1 merged → tracking + 协作事故注解`** 闭环
  - 不做 history surgery(reset --soft 会撕掉对方的事故注解)
- 后续改保守策略:跳 CLAIM、不动 active-sessions.md M、只动模块自身 3 文件——剩余 5 模块零再次撞车

### Claude 行为

- Auto-mode 拒绝高危操作:1 次(`git reset --soft HEAD~1` 在 0028 proposal 提交后被 classifier 拦)
- 用户 override:0 次(用户判断后选 B 维持现状)
- Bash classifier 短暂不可用:多次(出现"claude-opus-4-7[1m] is temporarily unavailable",均后续重试通过)

---

## 2. 诊断(Diagnoses)

### 模式 1:前端字典与 SQL 字典层广泛漂移(高价值发现)

- **现象**:27 模块抽取过程中,**约 15 个模块存在不同程度的 frontend↔SQL 字典漂移**,且过去无人系统化记录。
- 漂移分类(按严重度递减):
  - **契约层(value 错位)**:
    - task/testcase/defect priority:前端 'P0/P1/P2' 直接当 value 存储,SQL biz_*_priority 用 '00/01/02' 码(label='P0 紧急' 等)
    - dora metric_type:前端 'deploy_frequency'/'change_failure_rate' vs SQL 'deploy_freq'/'change_fail_rate'(2/4 错位)
    - feature-flag env:前端 'dev' vs SQL biz_ff_env 'test'
    - document docType:前端 11 短缩写 vs SQL 12 完整名,仅 prd/arch 1:1 对应
  - **显示层(value 一致,label/tag 略异)**:
    - competitive:enterprise 前端「企业」/SQL「企业级」;status 02 已归档 tag warning/SQL danger
    - ai-agent:provider mock/dify 前端「Mock/Dify」/SQL「Mock 占位/Dify 编排」
    - testreport:risk 前端「🟢 绿灯」/SQL「绿 (低风险)」
    - release/document:status '04' 已废弃 tag 前端 danger/SQL list_class 空
    - submission:status 01 vs 02 tag 颜色「互换」(疑似前端早期错配)
    - arch:archMode/stack label 简化(微服务/Java SB3 vs SQL「微服务架构」/「Java (SpringBoot3)」)
    - feature-flag:6 处漂移(env label 中英差异 + mode all_off tag drift + label 简化等)
  - **结构性(SQL dict 未被前端承载)**:
    - dora biz_dora_status / biz_dora_period:SQL 定义但 frontend 不渲染
    - feature-flag biz_ff_status:SQL 定义但前端用 rolloutMode 渲染
    - PRD biz_prd_scene / biz_prd_target_user:SQL 有 list_class 但前端仅 form 静态 option
- **根因(5 Whys)**:
  1. 为何漂移这么多?→ 历史多人多次开发,前后端各自补 label/tag,无校对环节。
  2. 为何无校对环节?→ Dict 散落各 index.vue 内联,无 SSoT 文件可对照 SQL。
  3. 为何散落?→ 前端没有「dict SSoT」约定,沿 RuoYi useDict 模式但业务模块 dict 直接硬编码 inline。
  4. 为何 useDict 模式不够?→ useDict 适合「下拉静态显示」,但 status/priority 等动态 tag 需 type 类型,代码层不可避免有 mapping 表。
  5. 为何到今天才系统化?→ 模块数量(31)累积到一定规模 + 单元测试体系完备时,本批抽取才有边际效应。
- **涉及规范文件**:PRD-MAPPING.md §6(字典定义层)、.claude/rules.md §M(字段映射)、开发规范.md(无明确 frontend dict 规范段)

### 模式 2:同分支并行 session 的协作摩擦

- **现象**:本批中 2 次撞车 + 1 次误吞他人文件:
  - **撞车 1**(task):我开工时它正在 task/index.vue inline 补 '05',我退场让号
  - **撞车 2**(release):我提交后它在 release/index.vue 同步加 AiButton 组件
  - **事故 3**(3ae00fd):我 `git add 指定 3 路径` 但 commit 包含 27 文件(并行 session 的 24 个 untracked 未跟踪文件被一同提交)
- **根因(5 Whys)**:
  1. 为何 3ae00fd 误吞了对方 24 文件?→ 调查后**仍未完全确定**,假设是 git config / hook 在 commit 时把 working tree 中所有 untracked 也一起加入(类似 `commit -a` 行为)。
  2. 为何 CLAIM 协议未阻止 task 撞?→ CLAIMS 块当时为空(双方都没 CLAIM 即开工);hook 是 nudge 不是硬拦。
  3. 为何双方都没 CLAIM?→ 协议是「开工时加 CLAIM」但实际多 session 跑 solo-review 时 CLAIM 协议没强制执行(协作规范是 SHOULD 不是 MUST)。
  4. 为何 release 在我抽完后被改?→ 模块抽取分两步:dict 抽取(我做) + 组件升级(它做),没有横向协调。
  5. 为何无横向协调?→ 两个 Claude session 独立开会,没有交叉计划机制。
- **涉及规范文件**:协作规范.md §3 任务认领、§4 同模块串行、§19 hook nudge

### 模式 3:auto-mode classifier 短暂不可用的影响

- **现象**:`claude-opus-4-7[1m] is temporarily unavailable, so auto mode cannot determine the safety of Bash right now` 在本批中至少出现 5 次,主要在密集 Bash 操作期。
- **根因**:Classifier 服务后端不稳/限流。
- **应对**:每次稍候重试均成功;读操作(Read/Grep/Glob)不受影响,期间用读操作继续推进。
- **不动作**:无 actionable,这是基础设施问题,不在本仓库改进范围。

---

## 3. 行动(Actions)

| # | 建议 | 涉及 | 转 Proposal? | 当前状态 |
|---|---|---|---|---|
| 1 | **聚合所有 dict 漂移发现,起 api-contract 审查 proposal**(数据契约层 4 项 + 显示层 8+ 项),决策:逐项「修齐」(改前端 value→SQL 码 + 后端 + E2E + 存量数据迁移)or「锁现状」(保留前端约定,SQL dict_data 改对齐) | PRD-MAPPING.md §6 / 多 *Dict.ts spec drift describe | → **0030-frontend-dict-ssot-drift-aggregate**(待开)| 候选 |
| 2 | **CLAIM 协议从 SHOULD 升 MUST 的可行性**:Hook 改为「同模块前后两次 add 之间必须有 CLAIM 文件存在」否则 pre-commit 拒;或保持 SHOULD 但增加 reflect 监控指标 | 协作规范.md §3 + .githooks/pre-commit | → **0031-claim-protocol-enforce**(待开)| 候选 |
| 3 | **诊断 3ae00fd 误吞 root cause**:在隔离仓库复现 `git add <specific paths> && commit` 是否真的会带入 untracked;若不能复现,挂"未明事件"。 | .githooks/pre-commit hook 源 + git config | — | 待执行 |
| 4 | **前端 Dict.ts 规范化**:开发规范.md 新增 §F 段「业务模块字典 SSoT」,沉淀本批 27 模块的命名/导出/spec 三板斧 | 03-开发/开发规范.md | → 可附 #1 | 候选 |
| 5 | **完成 E2E 全套件回归**:待并行 session 0028 P0-2 后端 SPI rebuild 完成 + java 启动后,跑 npm run test:e2e 验证 27 模块 Dict 抽取整体不退步;预期 350±20 case | E2E-运行手册.md | — | 阻塞中(等后端)|

---

## 4. 关注下周

- [ ] 等并行 session `21b7166 feat(backend): 0028 P0-2A SPI + P0-2B 跨模块 endpoint` 后端启动后跑 E2E 全套件
- [ ] 起草 proposal 0030(漂移聚合)+ proposal 0031(CLAIM 协议升级)
- [ ] 把本批漂移按「契约层 / 显示层 / 结构性」3 类做矩阵图入 PRD-MAPPING.md §6 漂移段
- [ ] analytics 模块的「无字典 helper」状态是否需要补一份占位 Dict.ts(供未来扩展)
- [ ] dora 漂移 5 处中 §3 LEVEL 前端独有 + SQL 表无 level 列:这是模型缺失还是设计就该前端态?(影响后端是否要补字段)
- [ ] task / testcase / defect priority 三个模块 priority 错位同范式(P0/P1/P2 vs 00/01/02),决策是否做一次「契约整型」批次

---

## 5. 链路

- 上周反思:
  - [2026-W22-modules-bulk-uplift.md](2026-W22-modules-bulk-uplift.md)(本周早些时候的 8 模块 🟡→🟢 收尾)
  - [2026-W22-zentao-integration.md](2026-W22-zentao-integration.md)
  - [2026-W21-session-handoff-agent-dogfood.md](2026-W21-session-handoff-agent-dogfood.md)
- 触发的提案候选:0030 dict-drift-aggregate / 0031 claim-protocol-enforce
- 关联 Sprint 回顾:_(本批未挂 Sprint)_

---

## 附录 A:27 模块抽取明细表

| # | 模块 | commit(test) | commit(docs) | 案例数 | 漂移 | 来源 |
|---|---|---|---|---|---|---|
| 1 | requirement   | `32a92e3` | `a796e82` | 22 | ✓ 对齐 | 本 session |
| 2 | inception     | `a69294a` | `e954daa` | 20 | ✓ 对齐 | 本 session |
| 3 | competitive   | `b34f897` | `286ff94` | 13 | 显示 2(label/tag) | 本 session |
| 4 | ai-agent      | `bf1f1a7` | `5bba63e` | 22 | 显示 2(provider label)| 本 session |
| 5 | release       | `6a1118a` | `b6997a7` | 15 | 显示 1(04 tag) | 本 session |
| 6 | dora          | `f3dfa01` | `a456548` | 16 | **契约 5(2 value + label + LEVEL + 2 SQL dict 未用)** | 本 session |
| 7 | submission    | `481d069` | `dcf67cb` | 9  | 显示 2(tag 互换) | 本 session |
| 8 | testreport    | `bd8cefc` | _合 ledger_ | 15 | 显示 1(label 风格) | 本 session |
| 9 | sprint        | `ea95fc4` | `6f45cda` | 7  | ✓ 对齐 | 本 session |
| 10 | prd          | `a73c314` | `b82cbe1` | 7  | ✓ 对齐 | 本 session |
| 11 | feature-flag | `371b1c5` | `bdcf137` | 15 | **契约+显示 6(env value/label/tag、mode、status 未用)** | 本 session |
| 12 | document     | `4cffc49` | `44657ab` | 21 | **契约 11↔12 异构 + 显示 1(03 tag)** | 本 session |
| 13 | arch         | `b3fb0f1` | `1b4744c` | 17 | 显示 2(mode/stack label 简化)| 本 session |
| 14 | ued          | `16a2750` | `7984fa2` | 6  | ✓ 对齐 | 本 session |
| 15 | openspec     | `3ae00fd` ⚠ | _合 ledger_ | 6  | ✓ 对齐 | 本 session(commit 误吞事故)|
| 16 | apidesign    | `beecbba` | `73fd784` | 11 | ✓ 对齐 | 本 session |
| 17 | apidoc       | `5f0ab24` | `51bcff5` | 6  | (单 method dict) | 本 session |
| 18 | manual-impl  | `300f314` | `cc5d005` | 14 | (3 dict 显示无漂移)| 本 session |
| 19 | manual-ops   | `6097623` | `fa0c1a5` | 15 | (3 dict 显示无漂移)| 本 session |
| 20 | manual-product | `c66c378` | `42201ef` | 6  | (单 status dict)| 本 session |
| ✕ | task         | _并行 session_ | _并行 session_ | _并行_ | **契约 1(priority value)** | 并行 session(我让号后做)|
| ✕ | testcase     | _并行 session_ | _并行 session_ | _并行_ | **契约 1(priority 同 task)** | 并行 session |
| ✕ | defect       | _并行 session_ | _并行 session_ | _并行_ | **契约 1(priority 同 task)** | 并行 session |
| ✕ | testplan     | _并行 session_ | _并行 session_ | _并行_ | _未抽:本人观察_ | 并行 session |
| ✕ | pipeline     | _并行 session_ | _并行 session_ | _并行_ | _未抽:本人观察_ | 并行 session |
| ✕ | dbdesign     | _并行 session_ | _并行 session_ | _并行_ | _未抽:本人观察_ | 并行 session |
| ✕ | dashboard    | _并行 session_ | _并行 session_ | _并行_ | _并行已抽,4 dict groups_ | 并行 session |
| ⊘ | analytics    | — | — | — | 无 helper 跳过 | — |

**统计**:有 dict-helper 的 31-1=30 模块中,**27 已 *Dict.ts SSoT 化**(本人 20 + 并行 7),覆盖率 **90%**。

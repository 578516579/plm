# Proposal 0026: UED 设计编排自进化系统（ued-orchestrator + ued-designer + accessibility-reviewer + skill + rule + workflow + signals）

> §L.2 记录：本提案对应**用户明确请求**的规范改动（改 `.claude/rules.md` 加 §N.10 + 新增 `99-跨阶段/UED设计工作流.md` + signals 段），用户原话"在开发过程中，UED的设计流程总结成一个agent和多个子agent，有相关的skill、rule，workflow"。按 §L.2 例外条款 **User-requested** 同步落地并在此事后补录。是 [proposal 0023 测试编排](0023-test-orchestration-self-evolution.md) / [0024 产品设计编排](0024-product-design-orchestration.md) / [0025 数据库设计编排] 的**第四个同范式**(测试·开发后 → 产品·需求维度 → 数据库·数据维度 → **UED·UI 维度**)。

---

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0026 |
| 标题 | UED 设计编排自进化系统 |
| 状态 | **merged → tracking**（User-requested;commit `511aa17`(feat) + `e1fa150`(docs process) + `4374451`(docs signals) + 本回填)|
| 类型 | 流程 / 工具链 |
| 提出人 | 用户 + Claude |
| 提出日期 | 2026-05-27 |
| 评审人 | 用户（项目 owner,AskUserQuestion 选"全套现在落地 + proposal 记录" + "+2 新子agent",空选回落推荐项）|
| 评审日期 | 2026-05-27 |
| Tracking 截止 | 2026-06-24（merged 后 4 周）|

> **编号让位备注**:本提案与并行 session 的"数据库设计编排"(rules.md §M.10 已声明 `proposal 0025`)同期落地。按 [协作规范.md §5 编号防撞](../协作规范.md) "后到者让位",数据库取 0025,本提案(UED)取 **0026**。rule 章节同理避让:db 取 §M.10(PRD 驱动段),本提案取 **§N.10**(UED 段),互不撞号。

---

## 1. 背景（What's the problem?）

项目已有丰富的 UED **硬规则**(§N.1~N.9 + [02-设计/UED规范.md](../../02-设计/UED规范.md) SSoT:Token/排版/颜色/布局/组件/导航/AI UI/响应式/交互/无障碍/资产/CR Checklist),也有一个 UED 相关守门子 agent(`ux-prototype-aligner` 原型保真,proposal 0024 建),但缺三样:

1. **一个把"开发前的 UI/交互设计"编排成一条线的总管**:信息架构怎么排、交互流怎么走、用哪些 Token/组件、原型对不对得上、无障碍达不达标、什么算"UED 设计就绪可以让前端写"、跑偏(前端自由发挥)怎么处置、结果如何回流自进化——目前散在 §N/UED规范.md 里,靠 Claude 临场拼。
2. **UED 设计域的"建模"执行者**:现有 `ux-prototype-aligner` 只**守门**(核 UI 对不对得上原型),没有从原型**推导出 UI 规格**(信息架构/交互/Token/组件选型)的"建模"角色——这是 UED 版的 `prd-author`(prd-author 建模字段,缺一个建模 UI 的对位)。
3. **无障碍专属守门**:§11 WCAG AA(对比度/focus/label/focus trap/不靠颜色)是独立维度,`ux-prototype-aligner` 只覆盖 label(§N.4),无对比度/focus/色觉无关性的专属审查者。

`product-orchestrator`(0024)把"UI"当**一个盒子**(L5 一道 `ux-prototype-aligner` 核查);本提案**把那个盒子拆开**,建一个**能自己做、自己进化**的 UED 设计体系——与产品设计(需求维度)、数据库设计(数据维度)并列的 **Phase 02 第三个设计维度总管**。

## 2. 证据（Evidence）

- **用户请求**：2026-05-27 会话原话——"在开发过程中，UED的设计流程总结成一个agent和多个子agent，有相关的skill、rule，workflow"。该句是 [proposal 0023](0023-test-orchestration-self-evolution.md) / [0024](0024-product-design-orchestration.md) §2 用户请求的**近逐字孪生**(测试/产品经理 → UED),确认意图是"把编排系统的整套机制,对位复制给 UED 设计"。
- **§L.2 User-requested-bypass**：用户在 AskUserQuestion 中被询问"落地范围"与"新子 agent 数量",空选回落到推荐项("全套现在落地 + proposal 记录" + "+2 新子agent"),明确授权先落地后记录、并新建 2 个 UED 专属子 agent。
- 关联现状：UED 域无总管 agent;UED 硬规则(§N)散落,无"漏斗分层 + UED 设计就绪裁决 + 自进化"统一编排层;signals 无 UED 专项指标(token 违规 / 无障碍偏离 / 组件复用缺口未统计)。
- **同范式佐证**:同期并行 session 已对位建"数据库设计编排"(db-orchestrator + db-schema-reviewer + plm-db-design + §M.10),证明"编排系统对位复制到各设计维度"是项目当前主动演进方向。

## 3. 提案（What's the change?）

### 改动文件清单

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `.claude/agents/ued-orchestrator.md` | 新增 | UED 设计编排总管:漏斗+分派+裁决+沉淀 |
| `.claude/agents/ued-designer.md` | 新增 ★ | U1-U3 UI 规格建模:从原型推导信息架构/交互/Token/组件 → UI 规格表(prd-author 的视觉孪生) |
| `.claude/agents/accessibility-reviewer.md` | 新增 ★ | U5 无障碍守门:WCAG AA 对比度/focus/label/focus trap/不靠颜色,§11 一票否决 |
| `.claude/skills/plm-ued-design/SKILL.md` | 新增 | 编排 5 步法 SOP |
| `.claude/rules.md` §N.10 | 新增 | UED 设计编排与自进化硬约束(漏斗/总管/UED 设计就绪裁决/自进化)|
| `99-跨阶段/UED设计工作流.md` | 新增 | 全流程 + 角色矩阵 + 升级路径 + 进化节律 + 与产品/测试工作流衔接 |
| `99-跨阶段/signals/2099-12.template.md` §11 | 新增 | UED 设计编排 signals 模板 |
| `99-跨阶段/signals/2026-05.md` §12 | 新增 | 当月 UED signals 实例(落地基线)|
| `.claude/agents/ux-prototype-aligner.md` | 编辑 | 补一行:现同时被 product-orchestrator(L5)与 ued-orchestrator(U4)复用 |

★ = 用户选"+2 新子agent"对应新建的 UED 专属子 agent。`ux-prototype-aligner` 复用自 0024(原型保真守门),不重建。

### 核心设计

- **UED 设计漏斗**：U1 信息架构(ued-designer+system-architect) / U2 交互流(ued-designer) / U3 视觉·Token·组件(ued-designer ★) / U4 原型保真(ux-prototype-aligner 复用) / U5 无障碍(accessibility-reviewer ★) / U6 交付(technical-writer) + AI UI 旁路(prompt-engineer);从一句 UI 需求收敛到可 100% 追溯到 UED规范.md + 原型 的规格。
- **总管不动手**：`ued-orchestrator` 只出"漏斗计划 + DAG + 裁决标准",子 agent 不能再 spawn,主 Claude 按 DAG 顺序调。
- **UED 设计就绪 Gate(并入 §N.10.3)**：Token 合规(§N.1/N.6) + 组件选自库(§N.9) + 状态徽章正确(§N.2) + AI 区分(§N.3) + 三态齐全(§N.5) + 原型保真(§N) + 无障碍达标(§11) + 设计交付 DoD(§12.3) + 与产品设计一致。与 product/db 维度共同构成 Phase 02→03 准入。
- **自进化闭环**：每轮收口记 UED signals(token 违规 / 组件复用缺口 / 无障碍违规 / 三态缺失 / 规格滞后)→ reflect 发现模式 → proposal 改规则/工具(如加 stylelint hook / axe-core)。
- **生命周期闭合**：与 0024/0025/0023 串成 `product-orchestrator`(需求) ∥ `db-orchestrator`(数据) ∥ `ued-orchestrator`(UI) 三维度皆绿(Phase 02→03)→ coder 开发 → `test-orchestrator`(测得过,Phase 03→04)。

## 4. 影响范围（Impact）

| 受众 | 影响 |
|---|---|
| 前端开发者 | 多一个"这页面 UI 怎么设计 / 用什么组件 / 无障碍达标吗"的统一入口;Phase 02 UED 准入更标准化 |
| Claude | rules §N.10 下次会话起生效;遇 UI/交互设计任务优先走 ued-orchestrator;遇"原型缺页面"必回 §M.1、"规范缺组件"必走 §N.9 |
| 产品质量 | token 违规 / 无障碍偏离 / 组件复用缺口从此可量化;"前端自由发挥"被主动拦截 |
| 已有资产 | 不改 §N.1~N.9 / UED规范.md / `ux-prototype-aligner` 职责(向后兼容),只在其上加编排层 + 2 个新子 agent + 1 行复用说明 |

## 5. 风险（Risks）

- 风险 1：与 `product-orchestrator`(0024)的 L5 `ux-prototype-aligner` 职责重叠、用户混淆 → 缓解:三处(agent/skill/workflow)写明边界——product 管"需求→规格"把 UI 当一个盒子,ued 把盒子拆开管 UI 全维度;`ux-prototype-aligner` 是两个总管共用的守门子 agent,不重建。
- 风险 2：与并行 session 的"数据库设计编排"(0025)同期改 SSoT(rules.md / signals / proposals/README)→ 缓解:严格避号(0026 / §N.10 / signals §12·§11,让 db 的 0025 / §M.10 / §11·§10);rules.md 两段不同区域(§M vs §N)textual 冲突低;**本提案不 auto-commit**,留用户统一 review 后决定 commit 策略(rules.md 含 db 未提交的 §M.10,需分辨)。
- 风险 3：总管变成"形式主义",2-3 agent 小任务也摆 DAG → 缓解:反模式明列,小改动直接顺序调(skill Step 1 给定向路径)。
- 风险 4：UED signals 没人填 = 自进化空转 → 缓解:§N.10.4 设为 MUST,收口即记;后续 `/reflect-monthly` 自动采集。
- 风险 5：体系建好但当前 31 模块已全 🟢,短期无新页面触发、无法 dogfood → 缓解:tracking 期(至 2026-06-24)盯首个新 UI 需求设计是否真走编排;若整个 tracking 期 0 次使用 → §10 判 reverted/简化。

## 6. 备选方案（Alternatives Considered）

- 方案 A：只加总管 agent,复用 `ux-prototype-aligner` + system-architect + technical-writer,不新建 → **未选**(用户选"+2 新子agent"):UED 最核心的两个动作(UI 规格建模 / 无障碍审查)无专属执行者,总管会"分管"一个缺核心成员的 roster。
- 方案 B：+1(只加 ued-designer),无障碍并进 ux-prototype-aligner → 未选:WCAG AA 对比度/focus/focus trap 是独立、可量化的守门维度,与原型保真正交,合并会稀释两者的"一票否决"清晰度。
- 方案 C：把 UED 编排并进 product-orchestrator(不另立总管)→ 未选:product 已管需求维度漏斗(7 层),再塞 UI 全维度会过载;且 UED 的 SSoT 是 UED规范.md 而非 PRD-MAPPING,裁决标准不同。
- 方案 D：纯文档(只写 workflow)→ 未选:无 agent/skill 则 Claude 不自动触发,"自己去做"落空(与 0023/0024 同理)。

## 7. 实施计划（Implementation Plan）

```
[x] Step 1: 建 ued-orchestrator agent(UED 设计编排总管)
[x] Step 2: 建 ued-designer agent(U1-U3 UI 规格建模)
[x] Step 3: 建 accessibility-reviewer agent(U5 无障碍守门)
[x] Step 4: 建 plm-ued-design skill(编排 5 步法)
[x] Step 5: rules.md 加 §N.10(注:db 占 §M.10,UED 用 §N.10 避让)
[x] Step 6: 建 UED设计工作流.md
[x] Step 7: signals 模板加 §11 + 当月实例加 §12 UED 设计编排
[x] Step 8: 建本 proposal 0026 + proposals/README 索引行
[x] Step 9: 编辑 ux-prototype-aligner.md 补"双总管复用"一行
[x] Step 10: commit(用户授权"提交";4-commit:511aa17 feat / e1fa150 docs process / 4374451 docs signals / 本回填)→ 转 merged 回填 commit hash
[ ] Step 11: 进入 tracking 期,看首个新 UI 需求设计是否走 ued-orchestrator 编排
[ ] Step 12: 后续 — 模块工作流.md Phase 02 段加链接到本工作流(小改,下次顺带)
```

## 8. 衡量指标（How will we know it worked?）

- `ued_token_violation_count` / `a11y_violation_count`：从"未统计"改善为**每轮设计分开记录**(基线=0/未统计 → 目标=tracking 期有真实数据)。
- UI 设计任务是否经 ued-orchestrator 编排：tracking 期内新页面/新 UI 需求设计 100% 走编排(基线=0 → 目标=首个用例即走)。
- `ued_handoff_lag`：UI 规格晚于前端实现 commit 的次数,维持 = 0(不倒挂)。

跟踪期：`2026-05-27` ~ `2026-06-24`（4 周）。

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| 用户（owner）| 通过（AskUserQuestion 选"全套现在落地 + proposal 记录" + "+2 新子agent",空选回落推荐）| 2026-05-27 | §L.2 User-requested 授权 |

---

## 10. 实施后跟踪（merged 后填）

### 实际 PR / commit
- PR: —（分支 `chore/local-start-backend-script`,solo-review）
- 合入 commit: `511aa17`(feat: 3 agent + plm-ued-design skill + ux-prototype-aligner 双总管复用)+ `e1fa150`(docs process: §N.10 rule + UED设计工作流 + proposal + README索引 + signals模板§11 + 在途任务)+ `4374451`(docs signals: 2026-05 §12 当月基线)+ 本回填(本提案 → merged)。commit 边界**干净**:并行 db session 的 §M.10 已于 `b2634e9` 先行提交进 HEAD,本会话对 rules.md 改动经 `git diff` 确认**仅 §N.10**,未夹带他人工作。
- 实际 merged 日期：2026-05-27

### Tracking 数据

| 信号 | 基线 | 目标 | 实际（周 1）| 实际（周 2）| 实际（周 N）|
|---|---|---|---|---|---|
| ued_token_violation_count / a11y_violation_count | 0/未统计 | 每轮分开记 | | | |
| UI 设计任务经 ued-orchestrator 编排 | 0 | 首个用例即走 | | | |
| ued_handoff_lag | 0 | 维持 0 | | | |

### 最终判定
- [ ] done（UED 编排被持续使用 + signals 持续记录）
- [ ] reverted（整个 tracking 期 0 次使用 → 回滚或简化为纯 workflow 文档）

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude | 首次创建：9 artifacts 落地记录(对位 proposal 0024 产品设计 / 0025 数据库设计;让号 0026 / §N.10)|

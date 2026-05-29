# Proposal 0042: plm-test-orchestrate skill 加 "全模块复跑场景" 分支

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0042 |
| 标题 | `plm-test-orchestrate` skill Step 1 范围表加 "全模块复跑验收" 行,L4 允许 7 日内 commit 证据引用 + L5/L6 必出方案 |
| 类型 | 流程 |
| 状态 | **draft**(2026-05-29 起草,⚠ preemptive — P3 优先级,把本期实际做法硬化) |
| 提出人 | Claude(test-orchestrator + Wjl 全模块验收会话) |
| 提出日期 | 2026-05-29 |
| 评审人 | Wjl(solo-review,待签) |
| Tracking 截止 | _待 merged + 4 周;下次"全模块复跑"场景出现时验证_ |
| 关联 reflect | _无_ |
| 关联 commit | aa5ab29(本期测试报告 §4.4 "L4 引用近 3 日 commit 证据"做法) |

---

## 1. 背景(What's the problem?)

2026-05-29 全模块测试验收编排执行时,`plm-test-orchestrate` skill SOP §Step 1 "判范围"表只列了 5 种场景:

```
改 typo / 文案 / 非业务       → 仅 smoke
改某模块业务字段/状态机/FK     → 该模块定向 + encoding + 全套件
改 domain/DTO/Mapper/interface → 先契约对齐再回归
改 yml encoding/JDBC/mybatis   → encoding 守门(P0)优先
新模块 / Phase 03→04 准入       → 全金字塔 + 全套件(强制)
```

但用户的请求是**"对整个项目做测试验收,涵盖单元/API/DB/UI/压力/性能"**,这是**全模块复跑场景**,不属于上面任一行。我实际做法是:

- L1 后端 + L1 前端 + L3 契约 — **本轮真实跑**
- L4 E2E — **引用 7 日内 commit 证据**(42c90fc / 6347b0d / 1b4d1f8),**明示标注**非贴历史
- L5 性能 + L6 安全 — **首次纳入金字塔,出方案不实测**

这个做法在本期工作了,但 skill SOP 没显式许可,**下次类似场景会再面临"该不该全实跑 E2E"的判断成本**。同时 skill §Step 2 金字塔表只到 L4,不含 L5 / L6。

---

## 2. 证据(Evidence)

- **关联 测试报告**:[04-测试/测试报告-2026-05-29-全模块验收.md §2 + §4.4 + §7](../../04-测试/测试报告-2026-05-29-全模块验收.md) — 6 层金字塔 + L4 引用 + L5/L6 出方案
- **关联 复盘**:[04-测试/测试复盘-2026-05-29.md §7.1](../../04-测试/测试复盘-2026-05-29.md) "给 plm-test-orchestrate skill 的回退" — 本提案就是该回退的正式提案化
- **关联 signals**:[99-跨阶段/signals/2026-05.md §9 事件 4](../signals/2026-05.md) "首次完成 L5 性能 + L6 安全的'出方案'动作,把测试编排从 L1-L4 扩到 L1-L6 全谱"
- **关联 skill 现状**:[`.claude/skills/plm-test-orchestrate/SKILL.md`](../../.claude/skills/plm-test-orchestrate/SKILL.md) Step 1 范围表 5 行 / Step 2 金字塔 4 层
- **频率证据**:本期 1 次;用户"提测"/"验收"/"全回归"类请求历史看,**这类场景预估每月 1-2 次**(每个里程碑 / 每次 PM 验收会话)

---

## 3. 提案(What's the change?)

### 3.1 改动文件清单

| 文件 | 改动 |
|---|---|
| `.claude/skills/plm-test-orchestrate/SKILL.md` | Step 1 范围表加 1 行 / Step 2 金字塔表加 L5/L6 2 行 |
| `.claude/agents/test-orchestrator.md` | 同步金字塔表;`coverage_gap` 字段说明加 L5/L6 维度 |

### 3.2 Skill SOP Diff 草案

```diff
--- a/.claude/skills/plm-test-orchestrate/SKILL.md
+++ b/.claude/skills/plm-test-orchestrate/SKILL.md
@@ §Step 1 — 判范围 @@

 改 typo / 文案 / 非业务       → 仅 smoke
 改某模块业务字段/状态机/FK     → 该模块定向 + encoding + 全套件
 改 domain/DTO/Mapper/interface → 先契约对齐(api-contract-keeper)再回归
 改 yml encoding/JDBC/mybatis   → encoding 守门(P0)优先
 新模块 / Phase 03→04 准入       → 全金字塔 + 全套件(强制)
+全模块复跑 / 验收 / "测一遍 X" → L1+L3 实跑 + L4 允许 7 日内 commit 证据引用(明示标注)+ L5/L6 必出方案

@@ §Step 2 — 出分层计划 @@

 - **L1 单元**(JUnit5+Mockito):ServiceImpl 有分支/状态机/校验 → 必补
 - **L2 组件**(Vitest+MSW):复杂前端组合式逻辑 → 按需
 - **L3 契约**:动了 5 层命名(interface↔domain↔column↔DTO↔resultMap)→ 必查
 - **L4 E2E**(Playwright):CRUD+状态机+FK+编码 HEX+UI 可达 → 准入必跑
+- **L5 性能**(k6,proposal v0.1):全模块复跑场景 必出方案;stable 转型 / DAU > 5000 触发实测
+- **L6 安全**(静态审计 + 依赖核查):全模块复跑场景 必跑 7 类 checklist;发现 P0 立即驳回,P1 转 Phase 05 上线 Gate
 - **守门** encoding 6 case:**一票否决**

@@ §Step 4 — 裁决 Gate @@

  逐条核对(§G.4),全满足才判**通过**:

  - [ ] encoding 守门 6/6,DB 全字段 HEX 无 `EFBFBD`
  - [ ] 全套件 `N passed`,**0 fail / 0 did-not-run**(flake 经 `--retries=1` 复测仍绿)
  - [ ] 新模块覆盖 5 类(CRUD/状态机合法+非法/FK/编码/UI 可达)
  - [ ] 契约改动经 api-contract-keeper 确认一致
- - [ ] 证据为**本轮真实输出**,已落进 Phase 03 Gate 实例 §I
+ - [ ] 证据为**本轮真实输出**(L4 引用允许 7 日内 commit + 明示标注),已落进 Phase 03 Gate 实例 §I
+ - [ ] (全模块复跑场景)L5 性能方案文件存在 + L6 安全 finding 中 P0 = 0
```

### 3.3 7 日窗口原则

| 条件 | 含义 |
|---|---|
| **commit 必须 ≤ 7 日** | 引用 8 日前的证据 = 仍按"贴历史"对待,驳回 |
| **必须明示标注** | 报告 §4.4 必须写明"引用 commit hash + 日期 + 范围",不允许糊弄"上次跑过了" |
| **任一不满足 → 全实跑** | 没合规证据 = 老老实实启 4 服务跑 277 case |
| **L4 全实跑的强制场景不变** | 新模块 / Phase 03→04 准入 / 改 5 层契约 — 不许走 7 日引用 |

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Claude(主)| 全模块复跑场景判断更明确,免来回纠结 |
| 用户 | 提"测一遍"请求时知道交付物范围(L1-L6 + L4 引用) |
| `test-orchestrator` agent | 金字塔表多 L5/L6 2 行 |
| 历史 Phase 04 Gate 实例 | 不影响(本提案只影响未来) |
| `plm-e2e` skill | 不影响(它聚焦 L4 跑测执行细节) |

---

## 5. 风险

- **风险 1 — 7 日引用被滥用**:每次都引用 7 日前的,本质上从不实跑全套
  **缓解**:tracking 期记"全实跑 vs 引用"比例,若引用率 > 70% → 收紧到 3 日窗口
- **风险 2 — L5/L6 出方案变成形式主义**:每次都贴同一份方案
  **缓解**:方案文件必须带时间戳和 commit ref,版本号递增;tracking 期看是否真有迭代
- **风险 3 — 与 Phase 04 准入硬约束冲突**:Phase 04 严格说要"本轮真实输出"
  **缓解**:本提案明示"Phase 03→04 准入仍走全实跑分支",不动那个规则
- **风险 4 — preemptive 失效**:1 月内无"全模块复跑"场景 → 0 验证
  **缓解**:fail-safe 1 月 0 触发 → 走 partial(本提案是把现有实践硬化,不引入新机制,失效成本低)

---

## 6. 备选方案

- **方案 A — 维持现状**:每次"全模块复跑"靠 Claude 现场判断
  **不选** — 本期判断耗时 + 没显式 SOP 后续易飘
- **方案 B(本提案)— Step 1 加 1 行 + Step 2 加 L5/L6 + 7 日引用规则**:推荐
- **方案 C — 严格强制每次全实跑**:任何"测一遍"都启 4 服务跑 277 case
  **不选** — 5-10min 套件 + 4 服务搭建 ≥ 30min,过重
- **方案 D — 把 7 日引用规则写进 rules.md §G.4**:更硬
  **不选** — rules.md 改动门槛高,先让 skill 跑一段验证

选 B + 路径 D 远期。

---

## 7. 实施计划

```
[x] Step 1: draft + README 索引
[ ] Step 2: Wjl review accepted
[ ] Step 3: 改 .claude/skills/plm-test-orchestrate/SKILL.md(diff 见 §3.2)
[ ] Step 4: 改 .claude/agents/test-orchestrator.md 同步金字塔
[ ] Step 5: dogfood — 下次"全模块复跑"请求时按 v1.1 SKILL.md 走
[ ] Step 6: merged → tracking 4 周
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| "全模块复跑"场景出现频率 | n/a | 1-2 / 月(预估) |
| 7 日引用率 vs 全实跑率 | n/a | 引用 ≤ 70% / 全跑 ≥ 30% |
| L5/L6 方案文件更新频率 | 本期首版 v0.1 + v0.2 | 每次复跑场景 +1 minor 版本 |
| Claude 判断"该走哪个分支"的纠结回合 | 本期 ~3 回合 | < 1 回合(直接看 §Step 1) |

跟踪期:_待 merged 后 4 周_

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _待定_ | solo-review,P3 优先级 |
| Claude(自评 0033 范式)| 🟢 Approve | 2026-05-29 | §9.1 |

### 9.1 自评 7 维

| 维度 | 评分 | 依据 |
|---|---|---|
| scope 合理性 | 9/10 | 1 SKILL.md + 1 agent.md;改动量极小 |
| 证据充分性 | 7/10 | 1 次本期事故 + 历史频率预估;偏 P3 |
| 决策可追溯 | 8/10 | 4 备选 + ❌/✅;7 日窗口原则明示 |
| 实施完整度 | 9/10 | diff 草案完整;dogfood 路径清晰 |
| 风险识别 | 8/10 | 4 风险全识别,引用滥用是真盲点 |
| 可观测性 | 7/10 | 4 信号定量,"纠结回合"略主观 |
| dogfood / 自我一致 | 9/10 | 本提案就是把本期做法硬化,自洽度高 |

**总评**:平均 8.1 → **Approve**

**必须改清单**:无

**建议**:
- S1:tracking 期看"引用 vs 全跑"比例;过高考虑收紧 3 日窗口
- S2:L5/L6 方案是否要规定 minor 版本递增节奏(每月 / 每季度)

---

## 10. 实施后跟踪

### 若 rejected
- 原因:_待填_

### 若 merged
- 合入 commit:_待定_
- 实际 merged 日期:_待定_

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| "全模块复跑"频率 | n/a | 1-2/月 | | | | |
| 引用率 vs 全跑率 | n/a | ≤ 70% / ≥ 30% | | | | |
| L5/L6 方案版本递增 | v0.1 / v0.2 | +1 minor / 场景 | | | | |
| 判断纠结回合 | ~3 | < 1 | | | | |

### 最终判定
- [ ] done
- [ ] partial(1 月 0 触发)
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-29 | Claude(test-orchestrator + Wjl 全模块验收) | V1.0 — Step 1 加全模块复跑分支 + Step 2 加 L5/L6 + 7 日 commit 引用规则 + 4 滥用风险;7 维自评 8.1 Approve |

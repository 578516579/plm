# Self-Evolution — 自进化机制 canonical overview

> **一句话定义**：本仓库的规范、流程、Gate Checklist、Claude 规则、hooks、skill —— 都是**可演化的代码**，按"信号 → 反思 → 提案 → 评审 → 合入 → 跟踪 → 终结"7 阶段闭环演化。这套机制本身也按同样规则演化（递归）。
>
> **本文件 = 唯一前门**。看完本文件 = 理解整套机制；7 个分散的 README 之后再深入读。

---

## 0. 核心信念

| # | 信念 | 含义 |
|---|---|---|
| 1 | 规范是代码 | 演进路径必须像代码一样走"提案 → 评审 → 合入 → 跟踪" |
| 2 | 数据说话不靠感觉 | 规则有效性靠 signals 量化，不靠"感觉" |
| 3 | 反思必须出 action | "反思变成心理按摩" = 反模式 |
| 4 | 机制本身也演化 | 元规则 0040/0041 已证明递归适用 |
| 5 | solo 与 small+ 节奏不同 | 4 维参数化 (L级 × 类型 × 规模 × 成熟度) |

---

## 1. 7 阶段闭环

```
┌────────────────────────────────────────────────────────────────┐
│ Stage 1: Signal       ── 客观数据采集 (commit / Gate / 风险 / OKR)│
│           ↓                                                     │
│ Stage 2: Reflect      ── 找模式，识别 friction (周/月/季度 / 事件触发) │
│           ↓                                                     │
│ Stage 3: Propose      ── 升格候选为正式 proposal (或降级为 Sprint backlog) │
│           ↓                                                     │
│ Stage 4: Review       ── 评审 (solo: 同日; small+: 按号段 SLA)    │
│           ↓                                                     │
│ Stage 5: Merge/Apply  ── 改规范文件 / 模板 / hook / skill         │
│           ↓                                                     │
│ Stage 6: Track        ── 2-4 周观察期, 看 signals 是否改善         │
│           ↓                                                     │
│ Stage 7: Close        ── 月度判 done / reverted / extend          │
└────────────────────────────────────────────────────────────────┘
                    每个 Stage 都有反模式守门
```

---

## 2. 各层定义

### 2.1 数据层 — signals/

**输入端**。每月一份 `99-跨阶段/signals/YYYY-MM.md`，记录 [7 类信号](signals/README.md):

| # | 信号类 | 字段示例 |
|---|---|---|
| 1 | Commit 规范 | total / violation / bypass |
| 2 | Gate Checklist | instances_added / skip_evidence / exception_filled_rate |
| 3 | Phase 耗时 | avg_duration / bottleneck |
| 4 | Bug / 缺陷复发 | total / recurring / top_3 |
| 5 | Claude 行为 | block / override |
| 6 | 风险 | new / closed / open_p0_p1 |
| 7 | OKR 进度 | kr_on_track_pct / at_risk |

**主文件 + 修订记录** 模式：主文件月初 baseline，修订记录滚动加事件。

---

### 2.2 处理层 — reflect/

**找模式 + 出 action**。3 种节奏 + 4 种类型:

| 节奏 | 文件 | 触发 | Skill |
|---|---|---|---|
| 周度 | `YYYY-WW.md` | 周一 09:00 / 事件触发 | [reflect-weekly](../.claude/skills/reflect-weekly/SKILL.md) v0.1 ✅ |
| 月度 | `YYYY-MM.md` | 月初 / tracking ≤ 3 天 | [reflect-monthly](../.claude/skills/reflect-monthly/SKILL.md) v0.1 ✅ |
| 季度 | `YYYY-QN.md` | 季末 / ADR ≥ 20 / 规范增 > 50% | [reflect-quarterly](../.claude/skills/reflect-quarterly/SKILL.md) v0.1 ✅ |

类型 (灵活组合):

| 类型 | 文件名后缀 | 何时用 |
|---|---|---|
| Closing | `YYYY-WW.md` | 周末标准闭合 |
| Dogfood | `YYYY-WW-<module>-phase{NN}-dogfood.md` | 跑完一个 Gate 立即触发 |
| Meta | `YYYY-WW-self-evolution-process-meta.md` | 反思机制本身 (递归) |
| Audit | `YYYY-WW-tracking-audit-mid.md` | tracking 中段验证 |

**每份 reflect 必含**: 观察(数据) / 诊断(根因) / 行动(具体到文件+段) — [README.md 反模式](reflect/README.md#反模式)。

---

### 2.3 输出层 — proposals/

**演化执行**。每提案文件 `NNNN-<标题>.md`,11 段(背景/证据/提案/影响/风险/备选/实施/衡量/评审/跟踪/修订)。状态机:

```
draft → proposed → accepted → implementing → merged → tracking ─┬→ done
                                                                  ├→ reverted
                                                                  └→ extend
       ├→ rejected
       └→ superseded
       partial: merged → tracking (partial)  — 0040 引入
```

号段规则 (避免冲突):
- `0001-0099` 流程 / Gate Checklist 类
- `0100-0199` 编码规范 / 代码风格
- `0200-0299` 工具链 / hook
- `0300-0399` 架构 / 技术债
- `0900-0999` 实验

Bundle 判据 (0040 §3.3): 同目标文件 / 同语义簇 / 同评审人 三选一。

Skill: [`/proposal`](../.claude/skills/proposal/SKILL.md) v0.1 ✅ — 候选升格 + apply 自动化。

---

### 2.4 降级层 — Sprint backlog / 风险登记册 D.2

**非规范变更**走旁路:

| 降级目标 | 文件 | 适用 |
|---|---|---|
| code TODO | [03-开发/Sprint backlog.md](../03-开发/Sprint%20backlog.md) | 性能 / 重构 / 单模块代码改 |
| 技术债 | [99-跨阶段/风险登记册.md](风险登记册.md) §D.2 | 已知会要还的工作量 (区别于 D.1 不确定性) |

判据: 见 [Sprint backlog.md §自进化降级判据](../03-开发/Sprint%20backlog.md)。

---

### 2.5 元规则层 — .claude/rules.md §L + 0040/0041

Claude 行为硬约束 [.claude/rules.md §L](../.claude/rules.md):

| § | 内容 |
|---|---|
| §L.1 会话末沉淀 | 每回合评估是否产生"值得沉淀的知识" (gotcha / ADR / 矛盾 / Gate / signal) |
| §L.2 Proposal 流程 | 改规范文件必须先写 proposal; 例外 = User-requested-bypass 必须事后补录 |
| §L.3 反思引擎 | 合适时机主动建议触发 `/reflect-*`; 报告产出后追问"哪几条转 proposal" |
| §L.4 数据完整性 | signals/reflect/proposals 永久保留; 修 merged proposal 走"修订记录"追加 |

元规则升级 (递归):

| 元规则 | 内容 | 落地点 |
|---|---|---|
| [0040](proposals/0040-self-evolution-v2-meta-rules.md) v2 | partial 状态 / bundle 判据 / Sprint backlog 通道 / solo same-day 节奏 / 写前 Read | proposals/README.md + 0000-template.md + Sprint backlog.md |
| [0041](proposals/0041-meta-rule-grep-existing-code.md) v2.1 | 扩展 §3.1: 规范类 proposal 写前 grep 现存代码合规性 | 0000-template.md §3 第 4 checkbox |

**solo 模式 same-day 节奏** (0040 §3.5):
- 评审耗时上限 = 0 天
- 评审人 = 提出方 (commit message 必须 `[solo-review]`)
- 同次 commit 含 proposal 文件 + 实际规范 diff
- 必填"为什么 solo 单签足够" 段

---

### 2.6 自动化层 — hooks + skills

| 层 | 实例 | 目的 |
|---|---|---|
| **Hooks** | `.githooks/commit-msg` | 拒非 Conventional Commits (硬阻断) |
| | `.githooks/pre-commit` | 跑 check-encoding.sh --staged (硬阻断, 0200 + BL-009) |
| | `.claude/settings.json` Stop hook | 会话末提示沉淀 |
| | `.claude/settings.json` PreToolUse Bash | 高危命令警示 + git commit 编码 hint |
| | `.claude/settings.json` UserPromptSubmit | 接收用户指令时提示 |
| **Skills** | [reflect-weekly](../.claude/skills/reflect-weekly/) v0.1 | 周度反思半自动 |
| | [reflect-monthly](../.claude/skills/reflect-monthly/) v0.1 | 月度 + tracking 终结 + 规则健康度 |
| | [reflect-quarterly](../.claude/skills/reflect-quarterly/) v0.1 | 季度 + ADR 一致性 + 跨文档 coherence + 重构建议 |
| | [proposal](../.claude/skills/proposal/) v0.1 | 候选升格 / apply / 状态管理 |
| | [signals-collect](../.claude/skills/signals-collect/) v0.1 | 7 类信号自动采集 → supplementary 文件 (Phase D 输入) |
| | [ruoyi-bootstrap](~/.claude/skills/ruoyi-bootstrap/) | 业务模块脚手架 (与自进化无直接关系, 但产 dogfood 数据) |

---

## 3. 角色矩阵

| 角色 | Stage 1 Signal | Stage 2 Reflect | Stage 3 Propose | Stage 4 Review | Stage 5 Apply | Stage 6 Track | Stage 7 Close |
|---|---|---|---|---|---|---|---|
| **PM / Owner** | 提供业务节奏数据 | 周/月报必看 | 高优先升格决策 | 流程类 (0001-0099) | 与开发协作 | OKR 对照 | 月度 closure 主持 |
| **Tech Lead** | git log / Gate 数据 | 参与诊断 | 架构类 / 编码类决策 | 编码/架构 (0100+/0300+) | 实施 | 技术信号 | 月度 closure |
| **Claude** | 自动采集 | 半自动写报告 | 自动产 proposal 文件 | solo 模式 [solo-review] | 自动 apply (diff) | 自动数据更新 | 7 步 checklist 走 |
| **开发者** | 触发事件 | dogfood reflect 实操者 | 候选提交 | — | 落地代码 | 实操反馈 | — |

**solo 模式**: Owner = Tech Lead = 开发者 = Wjl, Claude 兼任工具人。`[solo-review]` 标记必须出现在 commit。

---

## 4. Skill 调用决策树

```
用户说什么 / 当前时机
│
├─ "/reflect-weekly" or 周一/事件后
│  └─ → reflect-weekly skill
│
├─ "/reflect-monthly" or 月初/tracking ≤ 3 天
│  └─ → reflect-monthly skill (含 tracking 终结 7 步)
│
├─ "/reflect-quarterly" or 季度末
│  └─ → reflect-quarterly skill (待)
│
├─ "升格候选 N" / "把 N 转 proposal" / "应用 N"
│  └─ → proposal skill (lift / apply / status 三模式)
│
├─ "/scaffold-phase NN <module>" / "新建业务模块"
│  └─ → ruoyi-bootstrap skill (Phase 7)
│
└─ 其他: 不调 skill, 直接处理
```

---

## 5. Phase 进度

```
✅ Phase A (passive substrate) — 2026-05-15 完成
   directories / templates / Stop+PreToolUse hooks / commit-msg hook

✅ Phase B (skill 半自动) — kicked off 2026-05-17, 3/3 完成同日
   ✅ reflect-weekly v0.1
   ✅ reflect-monthly v0.1
   ✅ reflect-quarterly v0.1 (Q3 起首次触发)

🟡 Phase C (proposal lifecycle) — kicked off 2026-05-17
   ✅ proposal skill v0.1 — 候选升格 / apply / status / bundle 判定一站式

🟡 Phase D (data-driven rule tuning) — kicked off 2026-05-17, 1/4
   ✅ signals-collect v0.1 (输入基础设施)
   ⏳ v0.2 Phase 耗时自动计算
   ⏳ v0.3 hook log 接入 (PostToolUse 日志 → signals)
   ⏳ v0.4 判断层: 基于 30 天数据 auto-suggest MUST↔SHOULD 升降
```

---

## 6. 健康度指标 (本机制好不好)

| 指标 | 现状 (W19-W21) | 阈值 | 状态 |
|---|---|---|---|
| 信号产 / 处理速率 | 28 / 20+ = ~1.4× | < 2× | 🟢 |
| Proposal 平均 propose-merge 间隔 | < 1 天 (solo same-day) | < 1 周 | 🟢 |
| `--no-verify` bypass | 0 / 月 | ≤ 1 / 月 | 🟢 |
| Reflect / 月 | ≥ 4 (W19+W20 6 份) | ≥ 4 | 🟢 |
| 候选堆积 ([ ] / [x]) | 0% / 100% | < 50% | 🟢 |
| 跨周持续 friction | 0 (W22 待验证) | ≤ 2 | 🟢 (待) |
| Sprint backlog 完成率 | 1 / 9 = 11% (W21 BL-009) | ≥ 30% / 月 | 🟡 (本月初始期) |
| Tracking 终结 done 比例 | TBD (5 月底首次) | ≥ 60% / 月 | ⏳ |

---

## 7. 反模式 — 都不许做

| # | 反模式 | 守门规则 |
|---|---|---|
| 1 | "反思变成心理按摩"（报告漂亮但无 action）| reflect/README.md 反模式; reflect-weekly skill §输出质量 |
| 2 | Silent merge（规范改了但 proposal 没立）| .claude/rules.md §L.2 + 0040 §3.5 强约束 commit 含双产物 |
| 3 | Cross-reference 缺口（proposal 承诺 instance 回标但未做）| reflect-monthly skill §跨周 friction 聚合 / BL-2026-006 已立项 |
| 4 | "merged" 即归档（不追 tracking）| proposals/README.md §生命周期 必经 tracking 期 |
| 5 | "未观察到 = 验证有效"（假阴性 done）| tracking-closure-checklist.md Step 4 显式守门 |
| 6 | "假性达成"（分母 < 3）| tracking-closure-checklist.md Step 5 显式守门 |
| 7 | Partial 状态滥用（应该拆 sub-proposal 却挂 partial 跑）| 0040 §3.2 拆 / 标 partial 判据 |
| 8 | Bundle 拆错（同号段不同评审人塞一起）| 0040 §3.3 bundle 判据 |
| 9 | 跳过 Solo same-day "为什么足够"理由 | 0040 §3.5 必填段 |
| 10 | scope 错位悄悄改 §3 抹痕迹 | 0040 §3.1 强约束"§修订记录写 scope 修正" |
| 11 | 写规范类 proposal 没 grep 现存代码 | 0041 §3.1 第 4 checkbox |
| 12 | MUST 累积无限多, 团队最终不读 | reflect-monthly §5 健康度审计每月扫 |

---

## 8. 紧急通道 — 跳过机制的情况

仅在以下条件,允许跳过自进化机制 (但事后必补):

| 场景 | 跳过什么 | 必补什么 | 时限 |
|---|---|---|---|
| P0 线上事故 hotfix | Gate 01/02 完整流程 | 补 Gate 01/02 文档 + ADR | 48h |
| 用户明确"绕过 proposal 直接改" | proposal 评审 | 事后写 proposal 标 `User-requested-bypass` | 当周 |
| 元规则发现严重 bug | 当前规则不适用 | 立即升格修正 proposal (本身也走 solo same-day) | 当会话 |

例: 0027/0028/0029 是事故触发的 silent merge → W20 周末补录为 retroactive proposals。

---

## 9. 文件索引 (一页对照)

按 stage 找文件:

| Stage | 文件 / 目录 |
|---|---|
| 1 信号 | [signals/](signals/) (主文件 + supplementary + README) |
| 2 反思 | [reflect/](reflect/) (周报 + 事件触发 + audit + meta) |
| 3 提案 | [proposals/](proposals/) (22+ 文件 + README 状态索引 + 0000-template) |
| 3b 降级 | [03-开发/Sprint backlog.md](../03-开发/Sprint%20backlog.md) |
|        | [99-跨阶段/风险登记册.md §D.2](风险登记册.md) |
| 4 评审 | proposals/README.md §评审节奏 |
| 5 落地 | 各 [Gate Checklist](gate-checklists/) / [开发规范.md](../03-开发/开发规范.md) / [rules.md](../.claude/rules.md) |
| 6 跟踪 | 每个 proposal §10 tracking 表 |
| 7 终结 | reflect-monthly §4 tracking 7 步 |

按角色看:

| 角色 | 入门读 |
|---|---|
| 新 Claude session | [rules.md §L](../.claude/rules.md) + 本文件 §1 §2 §4 |
| 新开发者 | 本文件 §1 §3 §6 + [reflect/README.md](reflect/README.md) |
| 新 PM / Owner | 本文件 §1 §3 §7 + [signals/README.md](signals/README.md) |

---

## 10. 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首次创建 — canonical overview 整合 7 个分散 README + 2 元规则 + 4 skill |

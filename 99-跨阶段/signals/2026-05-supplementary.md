# Signals 补充采集 — 2026-05-19

> 由 [signals-collect skill](../../.claude/skills/signals-collect/SKILL.md) v0.1 **首次 dogfood** 产出。
> 主文件 [2026-05.md](2026-05.md) 不变; 月底 (2026-06-01 ~ 03) reflect-monthly skill 合入主文件。
> 本文件是 Phase D groundwork 的**第一份真实数据**。

---

## 头部

| 字段 | 值 |
|---|---|
| 采集时间 | 2026-05-19 (Sunday 末) |
| 时间窗 | 2026-05-15 ~ 2026-05-19 (5 天累计, P0 改名后的全部业务期) |
| 累计 commit | 48 |
| Dogfood Mode | ✅ — 验证 signals-collect skill v0.1 可用性 |

---

## 1. Commit 规范

| 字段 | 值 | 备注 |
|---|---|---|
| commit_total | **48** | 含 P0 改名 (2 commit) + Phase A 基建 (4) + 业务模块 dogfood (15+) + W20-W21 自进化批次 (18) + Phase B/C/D skill (5) |
| feat | 16 | 业务模块 PRD-align + 自进化 skill 上线 |
| fix | **1** | `913d431 fix(encoding)` — W20 周六事故根治 |
| docs | 16 | reflect / proposals / signals 文档 |
| refactor | 10 | apply proposal 改 Gate 模板 / 规范 |
| perf | 1 | `453c3ac perf(hook)` BL-2026-009 完成 |
| chore | 2 | Phase A 基建 / commit-msg hook |
| test | 2 | E2E Playwright 框架 |
| commit_violation_count | 0 | commit-msg hook 全程通过 |
| **commit_bypass_count (`--no-verify`)** | **0** | ✅ |
| unique committer email | **1** | `578516579@qq.com` solo 验证 |

**Health 评估**: 🟢
- `bypass == 0` ✅
- `fix / total = 1/48 = 2%` ≪ 30% 阈值 ✅
- `solo committer == 团队规模 1` ✅

---

## 2. Gate Checklist

| 字段 | 值 | 备注 |
|---|---|---|
| gate_instances_added (2026-05) | **40** | 极活跃: project (6) + 7 其他模块各 3-4 + cycle 多份 |
| gate_total (累计) | 41 | 含 P0 演示 1 份 |
| gate_skip_evidence | 0 | 所有跨阶段都有签字 commit |
| gate_with_exception_section | **25 / 41** = 60% | 含 §E 或 异常 段的实例 |
| GATE 含 friction 关键字 | 待统计 (v0.2 加 query) | |

**Health 评估**: 🟢 (active substrate); ⚠️ exception_rate 60% > 30% 阈值 → 触发 friction 信号 (W20 已识别,正逐步通过 proposal 化解)

---

## 3. Phase 耗时 (v0.2 auto-compute)

✅ **v0.2 已实现** (2026-05-19): 调 [scripts/phase-duration.sh](../../.claude/skills/signals-collect/scripts/phase-duration.sh) 一站产出。
runtime ≈ 2:18 (MSYS bash + 7 模块 × 6 Phase = 42 date diff 调用)。

### 3.1 各模块 Phase 时间表

| 模块 | Phase | entry | exit | within | gap |
|---|---|---|---|---|---|
| defect | P01 | 2026-05-16 | 2026-05-16 | 0d | — |
| defect | P02 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| defect | P03 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| defect | P04 | — | — | — | — |
| defect | P05 | — | — | — | — |
| defect | P06 | — | — | — | — |
| document | P01 | 2026-05-16 | 2026-05-16 | 0d | — |
| document | P02 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| document | P03 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| document | P04 | — | — | — | — |
| document | P05 | — | — | — | — |
| document | P06 | — | — | — | — |
| project | P01 | 2026-05-15 | 2026-05-15 | 0d | — |
| project | P02 | 2026-05-15 | 2026-05-15 | 0d | 0d |
| project | P03 | 2026-05-15 | 2026-05-15 | 0d | 0d |
| project | P04 | 2026-05-15 | 2026-05-16 | 1d | 0d |
| project | P05 | 2026-05-15 | 2026-05-16 | 1d | 0d |
| project | P06 | 2026-05-15 | 2026-05-22 | 7d | 0d |
| requirement | P01 | 2026-05-16 | 2026-05-16 | 0d | — |
| requirement | P02 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| requirement | P03 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| requirement | P04 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| requirement | P05 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| requirement | P06 | 2026-05-22 | 2026-05-22 | 0d | 6d |
| sprint | P01 | 2026-05-16 | 2026-05-16 | 0d | — |
| sprint | P02 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| sprint | P03 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| sprint | P04 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| sprint | P05 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| sprint | P06 | 2026-05-22 | 2026-05-22 | 0d | 6d |
| task | P01 | 2026-05-16 | 2026-05-16 | 0d | — |
| task | P02 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| task | P03 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| task | P04 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| task | P05 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| task | P06 | 2026-05-22 | 2026-05-22 | 0d | 6d |
| testcase | P01 | 2026-05-16 | 2026-05-16 | 0d | — |
| testcase | P02 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| testcase | P03 | 2026-05-16 | 2026-05-16 | 0d | 0d |
| testcase | P04 | — | — | — | — |
| testcase | P05 | — | — | — | — |
| testcase | P06 | — | — | — | — |

### 3.2 跨模块汇总

| Phase | 完成模块数 | 平均 within | 中位 within | 平均 gap |
|---|---|---|---|---|
| P01 | 7 | 0.0d | 0d | —d |
| P02 | 7 | 0.0d | 0d | 0.0d |
| P03 | 7 | 0.0d | 0d | 0.0d |
| P04 | 4 | 0.2d | 0d | 0.0d |
| P05 | 4 | 0.2d | 0d | 0.0d |
| P06 | 4 | 1.8d | 0d | 4.5d |

### 3.3 异常 / 缺失

- ✅ 无异常 (所有 Phase within ≤ 阈值, gap ≤ 7d, P01-03 instance 全)

**Dogfood 观察**: 4 模块 (requirement/sprint/task/testcase 中已到 P06 的 3 个) Phase 06 cycle1 kickoff Gate 文件**缺失** (per proposal 0012 两段式签字)。仅有 day7 closure + cycle2 kickoff (合并为同日)。脚本目前未单独标这种"两段式不完整" — v0.3 候选检查项。

### 3.4 4D 参数化期望对照 (per proposal 0007/0010/0011/0012)

| 维度 | 期望 within | 实际中位 | 状态 |
|---|---|---|---|
| solo + early × Phase 01-03 | ≤ 2d | 0d (P01) | ✅ |
| solo + early × Phase 04-05 | ≤ 5d | 0d (P05) | ✅ |
| solo + early × Phase 06 cycle | = 7d (per proposal 0012) | 0d (P06) | ⚠️ 见 §3.3 (仅 project 模块达标 7d, 其余 3 模块文件不全) |

**bottleneck (v0.1 估计 vs v0.2 实测)**:
- v0.1 (W20 reflect 手估): Phase 03 ~3h
- v0.2 实测: 全 Phase 01-05 within 中位 ≤ 1d, solo + AI 协作 + 模板生成的速度优势已明显
- 真实 bottleneck 转移到 Phase 06 (运营 cycle), 这是预期 (运营本身需要时间观察)

---

## 4. Bug / 缺陷复发

| 字段 | 值 |
|---|---|
| bug_total (`fix:` commits) | **1** (encoding 事故) |
| bug_recurring (heuristic) | **0** |
| top fix 类别 | `fix(encoding)` × 1 |

**Health**: 🟢 — 0 复发, 唯一 fix 也是 W20 周六事故根治 (后已通过 proposal 0028 三层防御处理)

---

## 5. Claude 行为

| 字段 | 值 |
|---|---|
| claude_block_count | N/A (待 Phase D v0.3 PostToolUse hook 接入) |
| claude_override_count | N/A (同上) |

⚠️ v0.3 计划: PostToolUse hook 输出到 `.claude/logs/`,signals-collect grep 统计。

---

## 6. 风险登记册

| 字段 | 值 | 备注 |
|---|---|---|
| D.1 真风险数 | **4** | R-001 + R-Proj-01/02 + R-SelfEvo-01 |
| D.2 已知技术债 | **3** | TD-2026-001 (Stage 2 vite 占位) + TD-2026-002 (deploy.sh 延后) + TD-2026-003 (高敏感 proposal 待 apply) |
| risks_new (本月) | 4 | 包括 R-SelfEvo-01 |
| risks_closed (本月) | 0 | — |
| **risks_open_p0_p1** | **0** | ✅ 月末无开放 P0/P1 风险 |

**Health**: 🟢 — 0 P0/P1 open

---

## 7. OKR 进度

| 字段 | 值 |
|---|---|
| 当前阶段 | `solo + early` (per proposal 0011) |
| 维护数值 KR | **N/A** (本周期不维护) |
| 下次评审 | 2026-08-01 (Q3 起或团队 ≥ 2 人时启用) |

---

## 衍生指标

| 字段 | 值 | 评估 |
|---|---|---|
| signals 候选 lifted | **22** | 全部已处置 |
| signals 候选 pending | **0** | ✅ 首次清零 |
| Sprint backlog 待办 | **8** | BL-2026-001 ~ 008 (BL-009 已完成) |
| Sprint backlog 已完成 | **1** | BL-2026-009 (W21 同会话完成) |
| Backlog 流入 / 完成 比 | 8/1 = 8× | ⚠️ 流入远快, 需 W22 Sprint plan 加大吸纳 |
| Proposal 总数 | **23** (22 + 0000-template) | 比 reflects/README 历史值 (21) 增 2: 0040+0041 meta |
| Proposal status: merged → tracking | 16 | 70% 进度 |
| Proposal status: merged (partial 变体) | 6 | 30% — 含 partial / amend / draft 等 |
| Proposal status: proposed (待 apply) | **0** | ✅ 首次清零 |

---

## 自进化机制规模 (元元)

| 制品 | 行数 / 数量 |
|---|---|
| `.claude/rules.md` | 293 行 |
| `03-开发/开发规范.md` | 444 行 |
| `99-跨阶段/模块工作流.md` | 172 行 |
| 累计规范行数 | **909 行** (季度增长率待 Q3 末算) |
| `.claude/skills/` 目录数 | **5** (reflect-weekly/monthly/quarterly + proposal + signals-collect) |
| `99-跨阶段/proposals/` 文件 | 23 |
| `99-跨阶段/reflect/` 文件 | 7 (5 reflect + README + template) |

---

## Dogfood 发现 — skill v0.1 bug

本次首次 dogfood signals-collect v0.1, 立即捕 2 处 bug:

### Bug v0.1-1: `--until="$WE 23:59"` 引号嵌套破裂

**现象**: 第一次跑 queries.md 中的 `git log --until="$WE 23:59"`, bash 解析时 `23:59` 被当作下一参数, git 报 `fatal: invalid object name '23'`。

**根因**: SKILL.md Step 1 写的 `WINDOW_END=${3:-$(date +%Y-%m-%d)}` 然后在命令中拼 `"$WINDOW_END 23:59"` 在 chain command 中触发解析问题。

**修复 (v0.2)**: 改 references/queries.md 模板:
```bash
WINDOW_END="${3:-$(date +%Y-%m-%d) 23:59}"   # 预拼好整字符串
# 或更稳:
WE="$(date +%Y-%m-%d)"
WE_END="${WE} 23:59"
git log --since="$WS" --until="$WE_END" ...
```

### Bug v0.1-2: Chained && + 多行 awk 在 Windows MSYS bash 中段失败

**现象**: 多个 `&&` 链接 + 内嵌 awk 范围匹配, 其中一行 awk 模式不匹配时返回 exit 1, 后续命令全跳过 (set -e 影响)。

**根因**: queries.md 模板隐含假设 awk 始终成功; 实际 awk 范围 `/^## 已完成/,/^## /` 在文件无下个 `^## ` 时不匹配。

**修复 (v0.2)**: 改 queries.md 模板:
- 用 `|| true` 抑制中间命令 exit
- 或拆为独立命令而非 chain
- 或显式 `awk ... 2>/dev/null || echo 0`

---

## 异常 / 触发

| 触发条件 | 当前状态 | 行动 |
|---|---|---|
| exception_filled_rate > 30% | **60%** ⚠️ | 已通过 0007/0010/0011/0012 proposal 4 维参数化处理大部分; 余留待 W22 监控 |
| BL 流入 / 完成 比 > 5× | **8×** ⚠️ | W22 Sprint plan 必须吸纳 ≥ 2 项 P1 (BL-2026-004/005/007/008) |
| candidates pending == 0 | ✅ | — (首次完全清零, 验证机制可持续) |
| signals-collect skill bug 数 | 2 | 升 0042 candidate (skill v0.2 fix) 或入 BL-2026-010 |

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-19 | signals-collect skill v0.1 dogfood + Wjl | 首次产 supplementary 文件 + 捕 v0.1 2 处 bug |
| 2026-05-19 (晚) | signals-collect skill v0.2 dogfood | §3 Phase 耗时段从手估升级为 auto-compute (scripts/phase-duration.sh); 实测 Phase 01-05 全 ≤ 1d, P06 4.5d gap; 发现 3 模块缺 P06 cycle1 kickoff Gate |

---

## 下次更新

- **建议时机**: 2026-05-29 (tracking 终结期前) → 跑一次, 给 reflect-monthly 准备数据
- **建议触发器**: 用户说 "/signals-collect" 或 monthly skill 自动调用
- **下次必新增字段**: claude_block_count / override_count (Phase D v0.3 hook log 接入后)

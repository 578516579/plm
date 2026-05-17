# MUST / SHOULD 规则健康度审计 — 月度扫描清单

> 月度独有动作。审计当月 [.claude/rules.md](../../../../.claude/rules.md) §A-§M 每条 MUST/SHOULD 条款的"触发频率"和"违反频率", 用 data 判定是否需要升 / 降 / 拆 / 删。
> 防"规则越攒越多"反模式 — 一切规则都需要定期 review。

---

## 准备 — 列出当月所有 MUST / SHOULD 条款

```bash
# 从 .claude/rules.md 提取 § + 条款一句话
grep -E "^(- |\d+\.)" .claude/rules.md | grep -E "MUST|SHOULD|必须|应当" | wc -l   # ~50-80 条

# 用 grep 列出每条
grep -nE "^##\ [A-Z]\." .claude/rules.md   # 段标题
```

逐段 §A-§M 扫描。

---

## 维度 1: 长期 0 触发的 MUST → 候选降 SHOULD

**触发条件**: 某 MUST 条款过去 ≥ 30 天未出现在:
- 任何 instance 文件的 §K friction 段
- 任何 audit reflect F-AUDIT 段
- 任何 commit message
- 任何 PR comment (如有)

```bash
# 例: §C.5 "默认管理员 admin/admin123 已修改密码或禁用"
KEYWORD="admin123"
TIME_AGO="30 days ago"
HITS=$(git log --since="$TIME_AGO" --grep="$KEYWORD" --oneline | wc -l)
GATE_HITS=$(grep -rl "$KEYWORD" 99-跨阶段/gate-checklists/ | wc -l)
echo "Rule §C.5 (admin123): commits=$HITS, gate refs=$GATE_HITS"
```

判定:
- 总命中 = 0 → 候选降 SHOULD (规则可能不再适用 / 现状已合规无需强约束)
- 总命中 = 1-2 → 保留 MUST, 留观察
- 总命中 ≥ 3 → 活跃 MUST, 不动

**注意**: 0 触发 ≠ 规则没价值。如某 MUST 是"防 P0 事故", 即使从未触发, 因为正是它防住了事故, 不能降级。需结合 §"风险评估" (proposal 0006 维度 4 成熟度) 综合判断。

---

## 维度 2: 频繁违反的 MUST → 候选改阈值 / 拆条款 / 上升级 / 弃用

**触发条件**: 某 MUST 当月被违反 ≥ 2 次。

```bash
# 例: rules §F "Commit 必须 Conventional Commits"
TARGET_MONTH=${1:-$(date +%Y-%m)}
COMMIT_MSG_REJECTED=$(git log --since="${TARGET_MONTH}-01" --until="$(date -d "${TARGET_MONTH}-01 +1 month -1 day" +%Y-%m-%d) 23:59" --grep="^[^a-z]" --oneline | wc -l)
NO_VERIFY_BYPASS=$(git log --since="${TARGET_MONTH}-01" --grep="no-verify" --oneline | wc -l)
echo "Rule §F violations: format-rejected=$COMMIT_MSG_REJECTED, bypassed=$NO_VERIFY_BYPASS"
```

判定:
- 违反 ≥ 2 → 进一步分析根因:
  - 根因是 "规则不合理" → 候选改阈值 / 拆条款 (例: solo 模式特殊路径)
  - 根因是 "规则未广而告之" → 候选升 MUST + 加 hook 强约束
  - 根因是 "规则不可执行" → 候选弃用, 走更可靠的替代 (e.g. lint)

---

## 维度 3: SHOULD 已成事实硬约束 → 候选升 MUST

**触发条件**: 某 SHOULD 条款过去 ≥ 30 天遵守率 = 100% 且团队明示"应该按这个做"。

```bash
# 例: §K.2 "每次重大重构后, 复审 rules.md/CLAUDE.md/开发规范.md 三者是否仍同步"
# 这是 SHOULD, 但实践中每次 P0 后都做了 → 升 MUST 合理
```

判定方式相对主观, 需要看:
- 过去 ≥ 3 次重大事件时 SHOULD 是否都被自然遵守
- 团队反馈"这条已经是事实硬约束, 不写 MUST 不对"

输出: 升级候选 + 说明 "为什么升 MUST 不会增加额外约束力"。

---

## 维度 4: 规则文档结构性问题

扫整体而非单条:

1. **规则总数过多**: rules.md §A-§M + M.1-M.8 已 30+ 条款。维度增加 = 复杂度增加 = onboarding 成本增加。建议月度扫"重复/可合并"条款。
2. **MUST/SHOULD/MAY 比例**: 理想结构是 MUST 少而硬, SHOULD 多而灵活, MAY 是"提议". 若 MUST 过多 (> 60%), 流程僵化。
3. **跨文档一致性**: rules.md / 开发规范.md / 模块工作流.md 三者是否仍同步? (per .claude/rules.md §K)

输出: 结构性建议留下月 proposal。

---

## 输出格式 — 月报 §5 段

| 条款 ID | 类别 | 当月数据 | 建议 | 优先级 |
|---|---|---|---|---|
| §C.5 | MUST | 0 触发 (30 天) | 保留 (防 P0), 加 cron 自检 | P3 |
| §F | MUST | 0 违反 / 0 bypass | 健康 | — |
| §G.1 | MUST | 2 instance 跳过签字 (W21) | 已立 BL-2026-006 回标 | — |
| §M.6 (drift) | SHOULD | 100% 遵守 | 升 MUST? 看下月 1 个 case | P2 |
| §K.2 (重审 rules.md) | SHOULD | 100% (W19 W20 都做了) | 升 MUST 候选 | P2 |
| ... | | | | |

最后给一个**当月规则健康度评分** (主观):

- 🟢 健康 (违反 < 1 / 月 + 0 触发 < 20%)
- 🟡 注意 (违反 1-3 / 月 OR 0 触发 20-40%)
- 🔴 警告 (违反 ≥ 4 / 月 OR 0 触发 > 40%) — 必走流程 Sprint 修

---

## 反模式 — 别在月度 audit 时做的事

- ❌ 看到 0 触发就立刻删规则 (可能这条正在静默防止事故)
- ❌ 看到违反就立刻升级硬约束 (可能根因是规则不合理, 不是不遵守)
- ❌ 把审计本身做成"心理按摩" (报告写漂亮但不出 action)
- ❌ MUST 越加越多 (无限累积, 团队最终不读规则)

每月对每条规则的"是否值得保留 / 是否合理"做一次主动 challenge — 这是规则演化的核心动力。

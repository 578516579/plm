---
name: proposal
description: Manage proposal lifecycle for the PLM self-evolution system. Use when the user wants to lift a signals candidate to a formal proposal, apply a merged proposal to target files, update proposal status, judge bundling vs splitting, or downgrade a candidate to Sprint backlog. Trigger phrases include "升格 0NNN / 转 proposal / apply 0NNN / 把 0NNN 落地 / lift candidate / 候选 0NNN 怎么处理 / 创建提案 / proposal status".
---

# proposal

**Phase C v0.1** — proposal lifecycle one-command skill. 把 `signals 候选 → proposal 文件 → apply 到目标 → 状态管理 → tracking` 全链路收成一站式。

This skill **替代** 手工流程: 之前每次升格/apply 都要走 8-10 步 manual (write file / Read targets / grep / write diff / update README / 修订 / commit)。本 skill 模式化这 8-10 步。

依赖: 已建立的 [proposals/0000-template.md](../../../99-跨阶段/proposals/0000-template.md), [proposals/README.md](../../../99-跨阶段/proposals/README.md), [signals/YYYY-MM.md](../../../99-跨阶段/signals/), [03-开发/Sprint backlog.md](../../../03-开发/Sprint%20backlog.md)。

---

## When to invoke

3 入口 (按用户意图):

### Mode A: **Lift** (signals 候选 → 正式 proposal)

Trigger: "升格 0NNN", "把候选 NNNN 转 proposal", "lift 0NNN", "0NNN 升级"

输入: signals 候选号 (e.g. `0017`) + 可选: bundle 进哪个 (`bundle into 0016`)

输出: 新 proposal 文件 + signals 候选状态改 `[x]` + proposals/README.md 新增行

### Mode B: **Apply** (proposed → merged → tracking)

Trigger: "apply 0NNN", "把 0NNN 落地", "0NNN 应用", "execute 0NNN"

输入: proposal 编号 (e.g. `0101`)

输出: 改目标规范文件 + proposal 状态更新 + 修订记录 + (可能派生) BL 入 backlog

### Mode C: **Status / Audit** (中段或月底状态管理)

Trigger: "0NNN status", "查 proposal 状态", "audit proposals" (不重 audit, 仅指 proposal 内的状态一致性)

输入: 一个 proposal 号 OR `all`

输出: 状态报告 / 修正不一致 (proposal §元信息 vs README 索引 vs signals 候选三处一致)

---

## Mode A — Lift workflow (6 步)

### Step A1: 验证候选存在 + 读上下文

```bash
# 找候选在 signals 中的位置
TARGET_MONTH=$(date +%Y-%m)
SIG_FILE="99-跨阶段/signals/${TARGET_MONTH}.md"
grep -n "^- \[.\] \`${CANDIDATE_ID}\`" "$SIG_FILE"
```

读对应的 reflect 文件 (找候选触发源)。读 [reflect/README.md](../../../99-跨阶段/reflect/README.md) 的"反模式"提醒。

### Step A2: 决策 — 升格 / 降级 / bundle

按 [references/decision-tree.md](references/decision-tree.md):
- 涉及规范变更 → 升格 proposal (Mode A 继续)
- 性能 / 重构 / 单代码模块 → 降级 BL-YYYY-NNN (走 backlog 通道)
- 多个同源候选 → bundle 判据 (per [0040 §3.3](../../../99-跨阶段/proposals/0040-self-evolution-v2-meta-rules.md))

### Step A3: 选号段 + 起编号

```bash
# 按号段分配 (per proposals/README.md §文件命名)
# 0001-0099 流程 / 0100-0199 编码 / 0200-0299 工具链 / 0300-0399 架构 / 0900-0999 实验
HIGHEST_IN_SEGMENT=$(ls 99-跨阶段/proposals/ | grep -oE "^${SEGMENT_START}[0-9]+" | sort -n | tail -1)
NEW_ID=$(printf "%04d" $((${HIGHEST_IN_SEGMENT/${SEGMENT_PREFIX}/} + 1)))
```

如 bundle: 复用现有 proposal 编号, 加 `Bundle` 字段。

### Step A4: 复制模板 + 填空

```bash
cp 99-跨阶段/proposals/0000-template.md "99-跨阶段/proposals/${NEW_ID}-${SHORT_TITLE}.md"
```

按 0040 §3.1 + 0041 §3.1 规则 (必读不可跳):

- [ ] 已 `Read` 每个目标文件的当前完整内容
- [ ] §3 段号 / 字段名 与目标文件当前版本逐字一致
- [ ] Diff 草案在目标段位置精确可应用
- [ ] **若约束代码层**: 已 `Grep` 被约束的现存代码, 确认现状合规或评估迁移成本

未通过任一 → 停下, 不写 proposal。

### Step A5: 同步更新 signals + proposals/README.md

```diff
# signals/YYYY-MM.md "触发的 Proposal" 段
- - [ ] `${CANDIDATE_ID}`（候选）— ...
+ - [x] `${CANDIDATE_ID}` **已升格** → [proposals/${NEW_ID}-${SHORT_TITLE}.md](...)

# proposals/README.md 状态索引
| [${NEW_ID}](${NEW_ID}-${SHORT_TITLE}.md) | ${TITLE} | **proposed** | ${TODAY} | ${TRIGGER} | — | （待 merged）|
```

### Step A6: Commit

```
docs(propose): lift candidate ${CANDIDATE_ID} → proposal ${NEW_ID} (${TITLE})
```

如 solo 模式同会话即将 apply → 接 Mode B (合并 commit 即可, per 0040 §3.5)。

---

## Mode B — Apply workflow (7 步)

### Step B1: 读 proposal §3 (改动文件清单 + diff 草案)

```bash
sed -n '/^## 3/,/^## 4/p' 99-跨阶段/proposals/${PROPOSAL_ID}-*.md
```

### Step B2: 按 0040 §3.1 + 0041 §3.1 写前校验

逐文件 `Read`. 若约束代码层, 逐目标 `Grep` 现存代码合规性。

发现 spec vs reality drift → 在 proposal §修订记录 写 "scope 修正" 行 (per 0040 §3.1 反约束), 不许悄悄改 §3。

### Step B3: 执行 diff

`Edit` (或 `Write`) 每个目标文件。注意 Phase 05/06 等模板有"按维度差异化" 模式, 应按 4 维参数化样式落地。

### Step B4: 更新 proposal §元信息 + §7 实施计划 + §9 评审记录 + §10 跟踪

`proposed → merged → tracking`. §9 评审记录 必填 "solo 单签理由" 段 (per 0040 §3.5)。§10 tracking 数据填 baseline (实际填值留 W22+ 跟踪)。

### Step B5: 派生 BL (若适用)

按 0041 §3.1 第 4 checkbox: 如 grep 发现现存代码不合规, 派生 `BL-YYYY-NNN` 入 [Sprint backlog](../../../03-开发/Sprint%20backlog.md), 写工作量 / 优先级 / 责任人。

### Step B6: 同步 proposals/README.md 状态索引 + signals 修订记录

`proposed → merged → tracking` 一行更新。signals 修订记录加事件描述。

### Step B7: Commit

```
refactor(self-evolution): apply ${PROPOSAL_ID} — ${WHAT} [solo-review]
```

如同 commit 内含多个 proposals apply → 合 commit 描述。

---

## Mode C — Status / Audit workflow (3 步)

### Step C1: 列状态

```bash
PROPOSAL_ID=$1   # OR "all"

if [ "$PROPOSAL_ID" = "all" ]; then
    for f in 99-跨阶段/proposals/[0-9]*.md; do
        STATUS=$(grep '| 状态 |' "$f" | head -1 | sed 's/.*状态[^|]*|\s*\*\*\([^*|]*\)\*\*.*/\1/')
        echo "$(basename $f .md): $STATUS"
    done
else
    grep -A 2 "## 元信息" 99-跨阶段/proposals/${PROPOSAL_ID}-*.md
fi
```

### Step C2: 三处一致性扫

每个 proposal 应在 3 处状态一致:
- proposal 文件 §元信息 "状态"
- [proposals/README.md](../../../99-跨阶段/proposals/README.md) 状态索引 "状态" 列
- [signals/YYYY-MM.md](../../../99-跨阶段/signals/) 修订记录 (历史描述)

不一致 → 报告 + 主动同步。

### Step C3: 输出建议

- `proposed` 超过 3 天未 apply → 提醒 (solo 模式应同日完)
- `merged → tracking` 已过 tracking 期 → 建议跑 reflect-monthly tracking 终结 7 步
- `merged → tracking (partial)` → 建议 W22+ 续 apply

---

## 输出质量约束

3 处自检 (每次跑本 skill 都过):

1. **proposal 文件完整性**: 11 段 (§元信息 / §1-§10 / §修订记录) 全有, 无空段
2. **状态一致性**: 3 处状态一致 (Step C2)
3. **0040/0041 遵守**: §3 段含 "已 Read" + "已 Grep (如约束代码层)" checkbox 全打勾, 或 §修订记录有 scope 修正记录

---

## 与其他 skill 协作

- 上游: [reflect-weekly](../reflect-weekly/SKILL.md) §3 §6 输出"候选转 proposal" 建议 → 触发本 skill Mode A
- 同伴: [reflect-monthly](../reflect-monthly/SKILL.md) §4 tracking 终结 → 触发本 skill Mode C 更新状态 → 或 reverted 时触发 Mode B 回滚 apply
- 下游: apply 后的代码修改 → 业务模块 dogfood → 反过来产生新 reflect 数据 (loop 闭合)

---

## 参考文件

- [references/decision-tree.md](references/decision-tree.md) — 升格 / 降级 / bundle / 拆分 决策树
- [references/template-shortcut.md](references/template-shortcut.md) — proposal 11 段模板的速写指南
- [references/checks.md](references/checks.md) — 0040 / 0041 元规则在写新 proposal 时的执行清单

---

## 历史

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-17 | Phase C kickoff;3 Mode (lift/apply/status) 一站式;沉淀 W19-W21 期间 22 proposal 的手工流程 |
| v0.2 | TBD | 全自动 lift (LLM 直接产出 proposal 文件); Mode B 含 dry-run preview |
| v0.3 | TBD | 跨 proposal cross-reference 校验 (e.g. 一改 0040 自动找 0041 是否要同步改) |

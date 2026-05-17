# 周度反思 — 信号采集 + friction 识别清单

> 本文件是 `reflect-weekly` skill 的"检查清单"。每次跑 skill 时按此 grep / Bash 一遍, 不依赖记忆。

---

## A. 5 类信号 (与 [signals/README.md](../../../../99-跨阶段/signals/README.md) 7 类对应, 周度精简版)

### A.1 Commit 健康度

```bash
# 当周时间窗
START=$(date -d "monday" -8day +%Y-%m-%d)
END=$(date -d "$START + 6 days" +%Y-%m-%d)

git log --since="$START" --until="$END 23:59" --pretty=format:"%h %s" > /tmp/wk-commits.txt

# 5 项指标
echo "total=$(wc -l < /tmp/wk-commits.txt)"
echo "feat=$(grep -c '^[a-f0-9]* feat' /tmp/wk-commits.txt || echo 0)"
echo "fix=$(grep -c '^[a-f0-9]* fix' /tmp/wk-commits.txt || echo 0)"
echo "docs=$(grep -c '^[a-f0-9]* docs' /tmp/wk-commits.txt || echo 0)"
echo "refactor=$(grep -c '^[a-f0-9]* refactor' /tmp/wk-commits.txt || echo 0)"

# 异常信号
git log --since="$START" --until="$END 23:59" --grep="no-verify" --oneline   # MUST = 0
git log --since="$START" --until="$END 23:59" --pretty=format:"%ae" | sort -u  # 唯一 committer 数
```

**异常阈值**:
- `--no-verify` bypass > 0 → 必查每条 bypass 理由
- fix > total × 0.30 → 表明本周质量问题严重
- unique committer > 团队规模 → 数据异常 (有外部 PR 干扰?)

### A.2 Gate Checklist 流动

```bash
# 当周新增 Gate 实例
find 99-跨阶段/gate-checklists/instances -name "*.md" \
    -newer .git/refs/heads/main -mtime -7 | wc -l   # heuristic

# Gate 实例 §J / §K friction 段命中
grep -l "friction\|未达成\|豁免" 99-跨阶段/gate-checklists/instances/**/*.md
```

**异常阈值**:
- §K 段 friction 数 > 3 / 模块 → 模板对该场景不适配
- gate_instances 突增 (> 上周 1.5×) → 项目节奏加快, 可能是 sprint sprint

### A.3 Proposal 流动

```bash
git log --since="$START" --until="$END 23:59" --diff-filter=A --name-only \
    -- "99-跨阶段/proposals/*.md" | grep -E '^99-.*/[0-9]{4}-' | sort -u  # 新升格

git log --since="$START" --until="$END 23:59" --diff-filter=M --name-only \
    -- "99-跨阶段/proposals/*.md" | grep -E '^99-.*/[0-9]{4}-' | sort -u  # apply / amend

# proposed (尚未 apply) 状态扫
grep -l "状态.*proposed" 99-跨阶段/proposals/[0-9]*.md | wc -l
```

**异常阈值**:
- `proposed` 状态 > 5 个 → backlog 过深, 需要批量 apply
- `merged → tracking (partial)` > 3 → partial 滥用, 应该拆 sub-proposal
- 1 周内新升格 > 10 个 → 候选堆积失衡, 警惕"快速但浅"

### A.4 Signals / 风险登记册 / Sprint backlog 流动

```bash
# 当月 signals 修订记录条数
grep -c '^| 2026' 99-跨阶段/signals/$(date +%Y-%m).md

# 风险登记册 D.1 / D.2 行数
grep -c '^| R-' 99-跨阶段/风险登记册.md       # D.1 真风险
grep -c '^| TD-' 99-跨阶段/风险登记册.md       # D.2 技术债

# Sprint backlog 待办 / 完成
grep -c '^| BL-' "03-开发/Sprint backlog.md"
grep -A1 "## 已完成" "03-开发/Sprint backlog.md" | grep -c '^| BL-' || echo 0
```

**异常阈值**:
- BL 待办 / 完成比 > 5 → 流入快于流出, 需 Sprint 加大吸纳
- D.1 (真风险) P0/P1 数 > 0 月末未 close → 流程必须停下处理

### A.5 Reflect / Audit 文件

```bash
ls -la 99-跨阶段/reflect/ | tail -10                # 看近期产出节奏
grep -l "F-AUDIT\|F-META" 99-跨阶段/reflect/*.md   # 元/审计反思数
```

**异常阈值**:
- 1 周内反思文件 = 0 → 反思纪律破裂
- 反思文件 > 3 → 过度反思 (反讽: 反思变成主任务而非业务)

---

## B. 6 种 friction 模式 (常见可识别的根因类)

每次跑 skill 时, 至少 grep 1 次以下每种模式:

### B.1 模板债务（某 Phase 模板对某场景不适配）

**触发**: §K friction 段在 ≥ 3 个模块的同一 Phase 重复出现同关键字

```bash
grep -h "friction\|豁免" 99-跨阶段/gate-checklists/instances/**/Phase{NN}*.md | sort | uniq -c | sort -rn | head -5
```

### B.2 Silent merge（规范改了但 proposal 没立）

**触发**: 关键规范文件本周有 commit, 但 99-跨阶段/proposals/ 同周没新 proposal 涉及

```bash
# 规范文件本周有变更?
git log --since="$START" --until="$END 23:59" -p \
    -- "03-开发/开发规范.md" ".claude/rules.md" "99-跨阶段/gate-checklists/Phase*.md" \
    | grep -c "^+"

# 同期 proposal 文件本周有新增?
git log --since="$START" --until="$END 23:59" --diff-filter=A --name-only \
    -- "99-跨阶段/proposals/*.md" | wc -l
```

如果规范变更 line count > 0 但 proposal 新增数 = 0 → silent merge 反模式, 必报。

### B.3 Cross-reference 缺口（proposal 承诺 instance 回标但未做）

```bash
# 承诺 vs 实际
grep -l "溯及" 99-跨阶段/proposals/[0-9]*.md | wc -l                           # 承诺数
grep -rl "溯及" 99-跨阶段/gate-checklists/instances/ 2>/dev/null | wc -l       # 实际数
```

承诺 / 实际 比例 < 0.5 → 警告。

### B.4 候选堆积（信号产 > 处理）

```bash
# 当月 signals 候选标 [ ] 数 vs [x] 数
grep -c '^- \[ \] `[0-9]' 99-跨阶段/signals/$(date +%Y-%m).md   # 未升格
grep -c '^- \[x\] `[0-9]' 99-跨阶段/signals/$(date +%Y-%m).md   # 已升格
```

`[ ]` / `[x]` > 1.5 → 警告堆积。

### B.5 元规则不适用（W21 audit F-AUDIT 类）

**触发**: 0040/0041 等元规则有正式落地, 但 W{WW} 中至少 1 次执行没遵守

```bash
# 写过 proposal 但没写 §修订记录的 scope-Read 段
for f in 99-跨阶段/proposals/[0-9]*.md; do
    if ! grep -q "已 \`Read\`\|grep 现存代码\|solo-review" "$f"; then
        echo "  ⚠️ $f - 缺规则遵守证据"
    fi
done
```

### B.6 派生 friction 未升格（W{WW-1} 留下的尾巴）

读上一份 reflect 文件结尾的"派生 → W21 升格"段:

```bash
grep -A 5 "派生.*W{当周} 升格\|deferred\|留 W" "99-跨阶段/reflect/$(ls -t 99-跨阶段/reflect/*.md | head -2 | tail -1)"
```

若上周说"留 W21 升格 X 项", 本周 W21 reflect 必须显式回答"已升格 / 仍 deferred / cancelled"。

---

## C. 输出质量 self-check（写完报告前自查）

按 [SKILL.md §输出质量约束](../SKILL.md#输出质量约束) 自检:

- [ ] 报告 ≥ 100 行
- [ ] §1 含量化数据表 (≥ 8 行)
- [ ] §1.3 friction ≥ 3 处 (每处含现象 / 影响 / 根因 / 修复路径)
- [ ] §3 每条 friction 标 → proposal NNNN / BL-YYYY-NNN / 直接改 / 观察
- [ ] §3.1 行动条目都"具体到 文件 + 段号 + diff"
- [ ] §6 一句话总结存在且未省略
- [ ] §修订记录有当日条目

任一 fail → reflect 不结案, 补完再 commit。

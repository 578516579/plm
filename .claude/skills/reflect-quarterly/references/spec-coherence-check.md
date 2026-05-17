# 跨文档 spec coherence 4 维扫描 — 季度独有

> 季度独有动作。扫 [`rules.md`](../../../../.claude/rules.md) / [`开发规范.md`](../../../../03-开发/开发规范.md) / [`模块工作流.md`](../../../../99-跨阶段/模块工作流.md) 三者, 找重复 / 矛盾 / 失效 / 盲区。
> 防"规则越攒越多, 团队最终不读"反模式。

---

## 准备 — 三文档行数 / 段数基线

```bash
for f in ".claude/rules.md" "03-开发/开发规范.md" "99-跨阶段/模块工作流.md"; do
    LINES=$(wc -l < "$f")
    SECTIONS=$(grep -cE "^## " "$f")
    SUBSEC=$(grep -cE "^### " "$f")
    echo "$f: $LINES 行 / $SECTIONS 段 / $SUBSEC 子段"
done
```

记录在季报 §5 元复盘 (跟踪季度增长率)。

---

## 维度 1: 重复条款 (同规则多处定义)

### 扫法 1: grep 标志性关键字

```bash
# 例: "必须 utf8mb4" 在 3 个文档中分别出现几处?
KEYWORD="utf8mb4"
for f in ".claude/rules.md" "03-开发/开发规范.md" "99-跨阶段/模块工作流.md"; do
    COUNT=$(grep -c "$KEYWORD" "$f")
    echo "$f: $COUNT"
done
```

如 ≥ 2 个文档命中 → 重复条款 (除非有"link to SSoT"模式)。

### 扫法 2: 模糊语义聚类 (人工)

对每个文档主要章节标题, 看是否其他文档有相似 (e.g. "命名 / Naming" 在多处定义)。

### 判定 & 行动

| 情况 | 行动 |
|---|---|
| 同条款不同表述 (Drift) | 选 SSoT, 其他改 `→ 详见 X §Y` reference |
| 同条款完全一致 (Sync, 但冗余) | 选 SSoT, 其他删 |
| 不同主题撞同关键字 | 加 disambiguation 注 (不算 drift) |

---

## 维度 2: 互相矛盾 (A 说 X, B 说反 X)

### 扫法 — 用对偶关键字 grep

```bash
# 例: ADR-driven design 用 Velocity 模板, 同时三规范是否一致?
# (假设性例子, 实际审计需领域知识)

# 例: 业务包路径
grep -E "system\.business|plm-business" .claude/rules.md 开发规范.md 模块工作流.md
```

### 实操经验

历史矛盾通常出现在:
- 命名规则 (大小写 / 复数 / 缩写)
- Phase 数量 (5 个 / 6 个 / 7 个 — 看不同文档对 "Phase 06 运营" 是否在主流程内)
- 错误码起点 (700 vs 701 vs 702)

### 判定 & 行动

| 矛盾类型 | 决议方法 |
|---|---|
| 一方过时 | 改它对齐 SSoT |
| 两方都合理但角度不同 | 加 "Context" 标识 (Claude 视角 vs 人类视角) |
| 矛盾无解 | 走 proposal review 决议, 写 ADR |

---

## 维度 3: 半年未引用条款 (候选删 / 降 SHOULD)

### 扫法 — 每条 MUST/SHOULD 的引用次数

```bash
# 提取所有 §A-§M 条款 (示意)
grep -nE "^## [A-Z]\." .claude/rules.md

# 然后对每个 § ID, grep 6 月内的 instance / reflect / commit / proposal 是否引用
KEYWORD="§G.4 E2E"
HITS=$(git log --since="6 months ago" --grep="$KEYWORD" --oneline | wc -l)
GATE_HITS=$(grep -rl "$KEYWORD" 99-跨阶段/gate-checklists/ | wc -l)
REFLECT_HITS=$(grep -rl "$KEYWORD" 99-跨阶段/reflect/ | wc -l)
echo "${KEYWORD}: commit=$HITS gate=$GATE_HITS reflect=$REFLECT_HITS"
```

### 判定

| 总命中 | 行动 |
|---|---|
| 0 命中 (6 月) | 候选删 (确认 ADR 无相关, 且无防 P0 价值后) |
| 0 命中 + 防 P0 类 (如默认弱口令) | 保留 MUST, 但加 cron 自检 |
| 1-3 命中 | 保留, 留观察 |
| ≥ 4 命中 | 活跃, 不动 |

---

## 维度 4: 频繁踩坑但规范无覆盖 (候选新增)

### 扫法 — reflect 反复出现的 friction

```bash
# 季度内 reflect 抽出所有 F-WW-NN / F-META / F-AUDIT 标题
for f in 99-跨阶段/reflect/*Q*-*.md 99-跨阶段/reflect/*W{当季 12 周}.md; do
    grep -E "^#### F-" "$f"
done | sort | uniq -c | sort -rn | head -20
```

重复 ≥ 2 次的 friction → 看现有规范是否覆盖。

### 判定

| 现状 | 行动 |
|---|---|
| 规范有覆盖 + 仍频繁触发 | → 维度 2 / 3 处理 (规则不合理或不执行) |
| 规范无覆盖 + ≥ 2 次出现 | → 候选新增 .claude/rules.md §N 或 开发规范.md §X |
| 规范无覆盖 + 仅 1 次出现 | 观察, 数据不足 |

---

## 整体输出 — 季报 §B 段

| 维度 | 发现数 | 建议 → 下季 proposal |
|---|---|---|
| 1 重复条款 | N | C1-A: 合并到 SSoT |
| 2 互相矛盾 | M | C1-B: 选定决议 |
| 3 半年未引用 | K | C1-C: 删 / 降 SHOULD |
| 4 频繁踩坑无覆盖 | L | C1-D: 新增条款 |

至少 1 处发现 (季度不可能全无 — 否则审计不彻底)。

---

## 反模式

- ❌ "三文档完全独立" → 应该有 SSoT + reference, 不应内容重复
- ❌ Claude 看的 rules.md vs 人看的 开发规范.md 长期 drift
- ❌ 模块工作流.md 描述阶段但没 Gate 模板对齐
- ❌ 半年只增不删 — 规则膨胀但实际遵守率下降

每季度做一次"主动质疑"(rules.md 这条还有用吗?) — 这是规则演化健康度的核心动力。

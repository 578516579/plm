# Tracking 终结判定 — 7 步 checklist

> 月底必做。每个 tracking 期到期的 proposal 都走完本 7 步, 给出 `done` / `reverted` / `extend` 之一的明确判决。
> 来源: [proposals/README.md §一份 proposal 的生命周期](../../../../99-跨阶段/proposals/README.md), 这里把"判决"从抽象规则降到可操作步骤。

---

## 准备 — 列出到期 proposals

```bash
# 当月即将到期 (或已到期) 的 tracking proposals
TARGET_MONTH=${1:-$(date +%Y-%m)}

for f in 99-跨阶段/proposals/[0-9]*.md; do
    if grep -q "Tracking 截止.*${TARGET_MONTH}" "$f"; then
        echo "===== $f ====="
        grep -E "^\|.*Tracking 截止|^\| 状态" "$f"
    fi
done
```

输出 N 个 proposal 文件。逐个走 Step 1-7。

---

## Step 1: 重读 §8 衡量指标 + §10 Tracking 数据

```bash
sed -n '/^## 8/,/^## 9/p' 99-跨阶段/proposals/${PROPOSAL}.md   # baseline + target
sed -n '/^## 10/,/^## 修订/p' 99-跨阶段/proposals/${PROPOSAL}.md  # 实际填值
```

确认 §8 的"信号"和"目标值"未变 (proposal merged 后不应改; 改过的话 §修订记录有体现)。

---

## Step 2: 用当月 signals + git log 实测每个 tracking 信号

针对 §8 表中每行信号, 实测当月值:

| 信号类型 | 实测命令示例 |
|---|---|
| commit 数 | `git log --since=...$START --until=$END --grep=$PATTERN \| wc -l` |
| fix 复发 | `git log --since=$START --grep="^fix.*$KEYWORD" \| wc -l` |
| Phase §K friction 数 | `grep -rl "$KEYWORD" 99-跨阶段/gate-checklists/instances/` |
| 规则被 grep 命中比例 | `grep -rl "$RULE_ID" 项目代码/` |
| BL 完成率 | `grep -c "✅" 03-开发/Sprint backlog.md \| §已完成` |

---

## Step 3: 比对实际 vs 目标 → 初步判决

判决规则:

| 实际数据 | 判决 |
|---|---|
| 全部信号 ≥ 目标 | **done** |
| 任一信号严重 ≤ baseline (退化) | **reverted** |
| 部分信号达成 / 数据样本 < 3 | **extend** |
| 信号 N/A (依赖未发生事件) | **extend** + 注明"等 W{NN} 触发" |

---

## Step 4: 检查"未观察到 = 没坏"反模式

⚠️ 关键: 很多 tracking 信号是"事故复发数 = 0 → 良好"。但 "0 复发" 可能源于:
- (a) 规则真生效, 防止了事故 ✓
- (b) 当月没人做相关业务, 不可能复发 ✗ (假阳性)
- (c) 事故复发但被压在不显眼角落, 没被 reflect 捕 ✗

判 done 之前**必查**:
1. 当月是否有相关业务活动 (e.g. 0028 防 EFBFBD → 当月是否真做了含中文的 DB 写入?)
2. 是否有审计 reflect 二次验证 (类型 4 audit reflect 体现)

否则: extend, 注明"未观察到 ≠ 验证有效"。

---

## Step 5: 检查"假性达成"反模式

⚠️ 另一关键: 信号"达成"可能是因为相关事情压根没发生:
- 例: tracking 信号 = "新业务模块加 FK 校验比例 100%" — 当月新增模块数 = 0 → 100% / 0 = 形式上 100%。

判 done 之前**必查**: 分母 ≥ 3 (3 个有效观察)。否则: extend。

---

## Step 6: 写判决记录

在该 proposal §10 末加一行:

```markdown
### 月度判决 (YYYY-MM-DD)

| 信号 | baseline | target | 实测 | 判决 |
|---|---|---|---|---|
| {sig1} | {bl} | {tg} | {实} | {pass/fail} |
| ... | | | | |

**整体判决**: ✅ done / ❌ reverted / ⏳ extend (新截止 YYYY-MM-DD)

理由 (1-3 句): {精炼原因, 解释 Step 4/5 检查结果}
```

同时在 proposals/README.md 状态索引同步更新 `状态` 列。

---

## Step 7: 跟进动作

按判决执行:

### Done

- proposal §元信息 状态: `merged → tracking` → `done`
- proposals/README.md 状态: 同上
- 归档: 移动到 "已完成 (done)" 区, 或在原表打 ✓
- signals/${TARGET_MONTH}.md 修订记录加: "{proposal} 判 done, baseline X → 实 Y"

### Reverted

- proposal §元信息 状态: → `reverted`
- 写"失败提案"备忘 (在 proposal 末加新段 "## 失败学习"):
  - 当初为什么觉得行? (回看 §1 背景 + §2 证据)
  - 实际为什么不行? (data-driven, 不是"感觉")
  - 替代方案? (留下月转 proposal)
- 回滚 PR (改 rules.md / 开发规范.md / 模板, 还原 proposal 落地前状态)
- proposals/README.md: 标 reverted + 留备忘链接

### Extend

- proposal §元信息: 加 "tracking 期延长" 一行 (新截止 YYYY-MM-DD)
- proposal §10 末加新行: "本期延期理由 + 期望下月达成的条件"
- proposals/README.md: 截止日字段更新, 状态保持 `merged → tracking (extended)`

---

## 批量处理建议

逐 proposal 跑 Step 1-7 慢。可批量化:

1. 先 grep 列出所有到期 proposal, 跑 Step 2 一次性收集所有实测数据 (Bash 脚本)
2. 集中判 Step 3-5 (人工 / 半人工)
3. 批量改 §10 + proposals/README.md (1 个 commit)
4. 分类执行 Step 7 (done 简单, reverted 工作量大)

输出: 一次 commit `docs(tracking): YYYY-MM month-end closure — N done / M reverted / K extend`

# Proposal 0039: 测试基线漂移自动同步 — 实测 vs 计划差 > 10% 即提醒

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0039 |
| 标题 | 测试基线漂移自动同步 — `mvn test` / `vitest run` 与测试计划 case 数差 > 10% 即提示同步 |
| 状态 | **draft**(2026-05-29 起草,⚠ preemptive — 自 1 次事故复现 + 1 次自我节制需求) |
| 类型 | 工具链 |
| 提出人 | Claude(test-orchestrator + Wjl 全模块验收会话) |
| 提出日期 | 2026-05-29 |
| 评审人 | Wjl(solo-review,待签) |
| Tracking 截止 | _待 merged + 4 周;1 周(2026-06-05)内 0 试用 → 走自动 rejected_ |
| 关联 reflect | _无_(本期"全模块验收会话"派生,reflect 未起草) |
| 关联 commit | bf221f3 / 224a09c / aa5ab29 / 41beb31(本期 4 commit 暴露问题) |

---

## 1. 背景(What's the problem?)

2026-05-29 全模块测试验收编排中发现"测试基线滞后实测":

- **前端**:测试计划 v0.9 标 **117 case / 8 文件**,实测 `vitest run` **500 case / 34 文件**(**4.3×**)
- **后端**:测试计划 v0.9 标 **868 case**,实测 `mvn test` surefire 累加 **1055 case**(**+187**)

根因:commit `6347b0d`「全谱测试基础设施 + 11 模块契约 + 性能 + seed」(2026-05-29 早晨)大幅扩面,但测试计划/策略文档同步晚一步(同日下午才在 commit `41beb31` 中刷正)。

**风险**:若 Phase 04 准入 Gate 用滞后的"计划 868"做基线判定 → 实际只跑通 868 也算"达标",而**真实门槛是 1055**,变相放低标准。

本期通过 `41beb31` 已手工把基线刷到 v1.0 / v1.6,但**没有机制保证下次扩面后文档及时同步**。

---

## 2. 证据(Evidence)

- **关联 signals**:[99-跨阶段/signals/2026-05.md §9 测试编排事件 4](../signals/2026-05.md) — 本月新增 `coverage_gap = "测试计划 v0.9 前后端基线均滞后实测"(文档同步问题非测试缺口)`
- **关联 commit**:
  - `6347b0d` 2026-05-29 全谱测试基础设施 + 11 模块契约 + 性能 + seed(case 数实际跳变源头)
  - `41beb31` 2026-05-29 测试计划 v0.9→v1.0 + 策略 v1.5→v1.6 基线刷正(手工同步,**滞后约半天**)
- **关联 实测数据**:
  | 维度 | 文档基线 | 实测 | 差异 |
  |---|---|---|---|
  | 后端 `mvn test` | 868 | 1055 | **+21.5%** |
  | 前端 `vitest run` | 117 | 500 | **+327%** |
  | 后端测试模块数 | 31 业务 | 35(31+4 横切) | +12.9% |
- **关联 复盘**:[04-测试/测试复盘-2026-05-29.md §3 O-1](../../04-测试/测试复盘-2026-05-29.md) "实测 vs 计划严重正向漂移 — 文档滞后"
- **关联 历史**:不是首次。v0.6/v0.7/v0.8/v0.9 修订记录看 测试计划.md 平均每 1-3 天就要手工同步一次 case 数,**累计 5+ 次手工修订**

⚠ **本提案是 preemptive draft**(1 次事故复现 + 自我节制需求,**勉强达到 fast-track 边缘但不依赖**);走完整 proposal 流程。

---

## 3. 提案(What's the change?)

### 3.1 改动文件清单

| 文件 | 改动 |
|---|---|
| `.claude/hooks/test-baseline-check.sh` | 新建脚本 |
| `.claude/settings.json` | Stop hook 段新增 1 行调度(或 PostToolUse Edit `*ServiceImplTest.java` / `*.spec.ts` 触发) |
| `04-测试/测试计划.md` 头部 | 加"最近实测时间戳"段(便于 hook 比较新旧) |

### 3.2 hook 脚本草案

```sh
#!/bin/sh
# test-baseline-check.sh — Stop hook 收口前对比 mvn/vitest 实测 vs 测试计划文档
# 永远 exit 0,只 nudge

PLAN="04-测试/测试计划.md"
[ -f "$PLAN" ] || exit 0

# 1) 文档基线提取 — 头部"当前规模"行
plan_be=$(grep -oE '后端单元 *[0-9]+ case' "$PLAN" | grep -oE '[0-9]+' | head -1)
plan_fe=$(grep -oE 'Vitest *[0-9]+ case' "$PLAN" | grep -oE '[0-9]+' | head -1)

# 2) 实测提取(本会话 mvn/vitest 最近输出 — 缓存在 .claude/cache/last-test-counts)
cache=".claude/cache/last-test-counts"
[ -f "$cache" ] || exit 0
. "$cache"   # 期望 export real_be=1055 real_fe=500 captured_at=...

# 3) 计算漂移
diff_be=$(awk -v p="$plan_be" -v r="$real_be" 'BEGIN{if(p>0)printf "%.1f", (r-p)*100/p; else print "0"}')
diff_fe=$(awk -v p="$plan_fe" -v r="$real_fe" 'BEGIN{if(p>0)printf "%.1f", (r-p)*100/p; else print "0"}')

# 4) 漂移 > 10% 输出 nudge
nudge() {
  awk -v d="$1" 'BEGIN{if(d+0>=10 || d+0<=-10) exit 0; exit 1}'
}
if nudge "$diff_be" || nudge "$diff_fe"; then
  echo "⚠️ [test-baseline-check] 测试基线漂移:" >&2
  echo "   后端:plan $plan_be  vs  real $real_be  (Δ ${diff_be}%)" >&2
  echo "   前端:plan $plan_fe  vs  real $real_fe  (Δ ${diff_fe}%)" >&2
  echo "   → 请同步 04-测试/测试计划.md 头部规模数 + §8 修订记录" >&2
fi
exit 0
```

### 3.3 缓存 `.claude/cache/last-test-counts` 来源

由 `mvn test` 后 / `npm run test:unit` 后 PostToolUse Bash hook 写入(简单 grep 结果即可),本提案不强制实装该写入端,**只要 hook 找不到 cache 就静默 skip**(渐进式)。

### 3.4 设计原则

| 原则 | 说明 |
|---|---|
| **永远 exit 0** | nudge 模式,不阻断 Stop |
| **静默 skip** | 没 cache 或基线非数字 → 不报警 |
| **10% 阈值** | 实测调优;过严则每次微改都报警,过松则像本期 +21%/+327% 才发现 |
| **可禁用** | `export CLAUDE_SKIP_TEST_BASELINE=1` |

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Claude(主)| Stop 前看到 nudge → 主动 Edit 测试计划 §8 修订记录(本期就是这个流程) |
| 人手动改测试 | 不受影响(Stop hook 仅 Claude 路径) |
| CI / 已有 commit | 无回溯 |
| 测试计划/策略文档 | 多了一段"实测时间戳"格式约定 |

---

## 5. 风险

- **风险 1 — cache 不存在时哑火**:依赖 PostToolUse 写入,如果没装那一端 → hook 永远静默
  **缓解**:第一阶段就允许哑火;第二阶段(Step 4)再装 PostToolUse 写入端
- **风险 2 — 文档 grep 正则脆**:文档头部规模行格式变更 → grep 拿不到数
  **缓解**:模板化头部规模行格式;grep 失败时静默 skip + 日志 1 行
- **风险 3 — 10% 阈值过严**:正常迭代单日 +5-9% 时频繁误报
  **缓解**:Tracking 期记 nudge 次数 → 调阈值;首版可放 15%
- **风险 4 — preemptive 失效**:1 周内无人触发新基线漂移 → 无样本验证
  **缓解**:fail-safe 1 周 0 触发 → 自动 rejected(同 0033/0035 范式)

---

## 6. 备选方案

- **方案 A — 不做(现状)**:每次扩面靠人记得同步,本期已证不靠谱
- **方案 B(本提案)— Stop hook + cache 对比 + nudge**:最轻量,推荐
- **方案 C — 在 commit-msg hook 直接对比**:commit `test(*)` 类自动校验。**不选** — commit-msg 阶段慢,nudge 应更前置
- **方案 D — GitHub Action 跑 weekly 同步检查**:发 PR 改基线。**长期可考虑**,本期太重
- **方案 E — 推迟到 W23 reflect 触发**:本期就 1 次事故,样本不足。**已就近接受 preemptive,fail-safe 兜底**

选 B + fail-safe E。

---

## 7. 实施计划

```
[ ] Step 1: 本 draft 起草(本 commit)+ README 索引登记
[ ] Step 2: ⏸ 等 Wjl review 转 proposed
    fail-safe:1 周(2026-06-05)0 触发 / 0 试用 → 自动 rejected
[ ] Step 3: 写 .claude/hooks/test-baseline-check.sh + settings.json Stop hook 段
[ ] Step 4: 写 PostToolUse Bash 端:vitest / mvn test 输出 grep → 写 .claude/cache/last-test-counts
[ ] Step 5: 3 场景自检
    ① real 与 plan 一致 → 静默 ② real > plan 12% → nudge 一行 ③ CLAUDE_SKIP=1 → 禁用
[ ] Step 6: dogfood(改一个 ServiceImplTest 加 5 case 不改 plan → 看是否 nudge)
[ ] Step 7: Wjl review accepted → merged
[ ] Step 8: tracking 4 周
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| 测试基线漂移 nudge 次数 | n/a | 1-5/月(过多 = 阈值过严,过少 = 同步及时) |
| 文档基线滞后实测 > 10% 的窗口期(从扩面 commit 到刷正 commit 的小时数) | 本期 ~6 小时 | < 1 小时 |
| 人工/Claude 误把"旧基线"当达标的事件 | n/a 但风险存在 | 0 |
| `CLAUDE_SKIP_TEST_BASELINE=1` 使用次数 | n/a | ≤ 2/月 |

跟踪期:_待 merged 后 4 周_

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _待定_ | solo-review;preemptive draft,fail-safe 1 周 |
| Claude(自评 0033 范式)| 🟡 Approve with comments | 2026-05-29 | §9.1 |

### 9.1 自评 7 维(0033 范式)

| 维度 | 评分 | 依据 |
|---|---|---|
| scope 合理性 | 8/10 | 1 hook + 1 cache + 1 文档约定;边界清晰 |
| 证据充分性 | 7/10 | 1 次本期事故 +187/+327% + 5+ 次历史手工修订;**勉强达 fast-track 边缘** |
| 决策可追溯 | 8/10 | 5 备选 + ❌/✅;明确选 B + fail-safe |
| 实施完整度 | 6/10 | Step 4 cache 写入端需另外实装,本期只画了草案 |
| 风险识别 | 8/10 | 4 风险全识别,缓解到位 |
| 可观测性 | 7/10 | 4 信号定量;"窗口期小时数"较难自动测 |
| dogfood / 自我一致 | 6/10 | 本期就是基线刷正现场,但本提案不解决"本期漏掉",只解决下次 |

**总评**:平均 7.1 → Approve with comments

**必须改清单**:
- M1:Wjl 决定 fail-safe 1 周接受 OR 推迟到 reflect 触发
- M2:阈值 10% vs 15% 取舍

---

## 10. 实施后跟踪

### 若 rejected
- 原因:_待填_
- 学习:_待填_

### 若 merged
- 合入 commit:_待定_
- 实际 merged 日期:_待定_

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| nudge 次数 | n/a | 1-5/月 | | | | |
| 滞后窗口小时数 | ~6 | < 1 | | | | |
| 漏判事件 | n/a | 0 | | | | |
| SKIP 使用 | n/a | ≤ 2/月 | | | | |

### 最终判定
- [ ] done
- [ ] partial
- [ ] reverted
- [ ] rejected(1 周 0 触发)

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-29 | Claude(test-orchestrator + Wjl 全模块验收) | V1.0 — preemptive draft 走完整流程;1 次事故 + 5+ 历史 = 勉强 fast-track 边缘;fail-safe 1 周 + 7 维自评 7.1 |

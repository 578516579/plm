# Proposal 0035: PostToolUse Edit/Write 增量测试 hook

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0035 |
| 标题 | PostToolUse hook:Edit/Write 改动文件后自动跑相关测试(增量反馈) |
| 状态 | **draft**(2026-05-28 起草,⚠ preemptive — automation-recommender skill 派生,体验级证据非事故复现;走完整 proposal 流程,**不适用 fast-track**) |
| 类型 | 工具链 |
| 提出人 | Claude(`claude-code-setup:claude-automation-recommender` skill 派生 + Wjl 选项 A) |
| 提出日期 | 2026-05-28 |
| 评审人 | Wjl(solo-review,待签) |
| 评审日期 | _待定_ |
| Tracking 截止 | _待 merged + 4 周;1 周(2026-06-04)内无人试用 → rejected_ |
| 关联 reflect | _无_(automation-recommender 派生,非 reflect §6 候选)|
| 关联 skill | `~/.claude/plugins/cache/.../claude-automation-recommender`(2026-05-28 加载) |

---

## 1. 背景(What's the problem?)

`.claude/settings.json` 当前 hooks 现状:**8 个 PreToolUse**(高危命令检测 / Gate immutable / E2E 前置 / session-guard ×2 / commit-msg / pre-commit / pre-push) + **0 个 PostToolUse**。

today 22 commit 测试自检过程显示:
- 跑全套 vitest 14.16s(34 文件 / 500 case)
- 跑后端 5 模块 mvn test ~75s
- session-guard.sh 5 自检 < 1s

**痛点**:Edit/Write 完一个文件后,**手动判断"该跑哪些测试"+ 手动触发**。常见反模式:
- 改 1 个 `*Dict.ts` 跑全套 vitest 14s,**实际相关 spec < 1s**
- 改 1 个 `*ServiceImpl.java` 跑 `mvn -pl plm-X test` ~30s,**实际相关测试类 < 5s**
- 不跑测试就 commit → CI 红 → 回滚(today 没发生但前期有过)

**反馈环最后一段缺失**:Pre 防错(已立体)→ 写代码 → ❌ Post 验证(空白)→ commit

⚠ **本提案是 preemptive draft** — automation-recommender skill 加载后产出,**证据是"体验级"(跑测试 N 次)而非"事故复现"**;**不适用 rules §L.2 fast-track 例外**(fast-track 证据要求 ≥ 2 commit hash 复现的事故,本提案不符)。走完整 proposal 流程 draft → proposed → accepted → implementing → merged。

---

## 2. 证据(Evidence)

- **关联 skill 输出**:claude-automation-recommender 2026-05-28 报告 §⚡ Hooks 推荐 "PostToolUse: 改动文件智能增量跑测试"(高 ROI 盲点 — 0 PostToolUse)
- **数据级事实**:
  | 改动类型 | 全套测试 | 相关测试增量 | 比率 |
  |---------|---------|------------|------|
  | 改 1 个 `*Dict.ts` | vitest 14s | 1 spec < 1s | **14×** |
  | 改 1 个 `*ServiceImpl.java` | mvn 单模块 ~30s | 1 Test 类 ~5s | **6×** |
  | 改 1 个 `*.vue` | vitest 14s | 1-2 component spec ~1s | **10×** |
  | 改 1 个 `*Mapper.xml` | mvn 单模块 ~30s | 1 Test 类 ~5s | **6×** |
  | 改 1 个 `business-*.sql` | (无现成单测) | (无) | n/a |
- **today 体验级事件**(非事故,体验复现):
  - P0-3A TestReport 改造完跑 plm-testreport 单模块 23 case(~15s)
  - P0-3B DORA 改造完跑 5 模块 13 BUILD SUCCESS(~75s)
  - P0-2C 前端 12 按钮改完跑 vitest 全套 500 case(~14s)
  - **共 ≥ 6 次"全套等待"事件**(每次 P0 完工)— 增量 hook 可省 ~80%
- **8 PreToolUse hooks 对比**:已有 8 个 Pre 防错,Post 0 个 — 反馈环不对称
- **proposal 0030 dogfood 启示**:同会话验证是高 ROI 路径 → Post hook 让"写完即知"无延迟

---

## 3. 提案(What's the change?)

### 3.1 改动文件清单

| 文件 | 改动 |
|---|---|
| `.claude/settings.json` | PostToolUse 段加 1 hook(增量测试 dispatcher)|
| `.claude/hooks/post-edit-test.sh` | 新建脚本,文件路径匹配 → 跑对应测试 |
| `CLAUDE.md` | gotcha 表或运行段提一句"Edit 后自动跑增量测试" |

### 3.2 Diff 草案(post-edit-test.sh)

```sh
#!/bin/sh
# PostToolUse:Edit/Write 触发,文件路径匹配 → 跑对应测试
# 只 nudge + 输出测试结果,不阻断 Claude 后续工作流(exit 0 always)

fp="${CLAUDE_TOOL_INPUT_file_path:-}"
[ -n "$fp" ] || exit 0

case "$fp" in
  # 前端 Dict / Vue / TS
  *Dict.ts|*Dict.spec.ts)
    module=$(basename $(dirname "$fp"))
    spec="plm-frontend/src/views/business/$module/__tests__/${module}Dict.spec.ts"
    [ -f "$spec" ] || exit 0
    echo "🧪 [post-edit-test] 改动 $module Dict.ts → 跑 ${module}Dict.spec.ts" >&2
    (cd plm-frontend && npx vitest run "$spec" --reporter=basic 2>&1 | tail -5) >&2
    ;;
  *.vue)
    # 可选:component spec 匹配,本期不实装(Vue spec 命名约定未统一)
    exit 0
    ;;
  # 后端 ServiceImpl / Controller / Mapper
  *ServiceImpl.java|*Controller.java)
    module=$(echo "$fp" | sed -nE 's#.*/(plm-[^/]+)/.*#\1#p')
    test_class=$(basename "$fp" .java)Test
    [ -n "$module" ] || exit 0
    echo "🧪 [post-edit-test] 改动 $module → 跑 $test_class" >&2
    (cd plm-backend && mvn -pl "$module" test -Dtest="$test_class" -q 2>&1 | tail -5) >&2
    ;;
  # Mapper XML 跟随 Mapper interface 测试
  *Mapper.xml)
    # 同上 ServiceImpl 路径
    ;;
  *)
    exit 0  # 其他文件不动
    ;;
esac
exit 0  # 永远 exit 0,不阻断
```

### 3.3 settings.json 集成

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "sh \"${CLAUDE_PROJECT_DIR:-.}/.claude/hooks/post-edit-test.sh\""
          }
        ]
      }
    ]
  }
}
```

### 3.4 关键设计原则

| 原则 | 说明 |
|------|------|
| **永远 exit 0** | 与 session-guard.sh nudge 模式同款 — 测试失败只 stderr 提示,**不阻断 Claude 后续 Edit/commit**(避免 Claude 工作流卡死)|
| **文件路径白名单** | 只对 `*Dict.ts` / `*ServiceImpl.java` / `*Controller.java` 触发;其他文件(.md / .yml / 配置)静默放行 |
| **超时硬上限** | 单次 hook 跑测试 > 10s → kill + stderr 警告(防卡 Claude 主流程)|
| **可禁用后门** | `export CLAUDE_SKIP_POST_TEST=1` 临时禁用(如 bulk refactor / Edit 100 文件场景)|

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Claude(主)| Edit/Write 文件后看到 stderr 的测试结果(~1-5s),早 1 步知道是否破坏既有逻辑 |
| 人手动 Edit | **不受影响**(PostToolUse 只挂 Claude Code 工具栈)|
| CI / GitHub Actions | 不影响,本 hook 是本地 Claude 反馈环增量 |
| 已有 commit / PR | 无回溯,只对未来生效 |
| session-guard.sh | 与本 hook 完全正交(Pre 防 vs Post 验) |

---

## 5. 风险

- **风险 1 — hook 跑得慢卡 Claude 主流程**(改 ServiceImpl 后跑 mvn 30s,Claude 卡 30s 等)
  **缓解**:超时硬上限 10s + kill;Claude 还可设 `CLAUDE_SKIP_POST_TEST=1` 临时禁用
- **风险 2 — 文件路径白名单误触发 / 漏触发**
  **缓解**:精确 case 匹配 + tracking 期收集误判 case 调整;白名单暂只覆盖 4 类高频文件(Dict.ts / ServiceImpl / Controller / Mapper.xml)
- **风险 3 — 测试结果嘈杂**(每次 Edit 都有 5 行 stderr)
  **缓解**:`--reporter=basic` 只输出 pass/fail 总数 + 失败摘要;测试全绿时 stderr 1 行 "✓ N passed"
- **风险 4 — preemptive draft 失效**(automation-recommender 派生,1 周内无人想试 → over-engineering)
  **缓解**:**Tracking 截止条款 fail-safe**:1 周(2026-06-04)内 Wjl 没说"试一下"或没起任何 Step 3 → 本提案自动 rejected
- **风险 5 — 与 GitHub CI 重叠**(CI 已跑全套测试)
  **缓解**:本 hook 是**写代码即知**(秒级),CI 是**push 后再知**(分钟级);提前 1 个反馈环节,不重叠

---

## 6. 备选方案

- **方案 A — 不做(现状)**:Edit 后人工判断跑哪些测试。**适合现状**,但反馈环不对称(8 Pre vs 0 Post)。
- **方案 B(本提案)— PostToolUse hook + 文件路径白名单 + nudge 模式**:轻量级,无侵入。**推荐**。
- **方案 C — Edit 后强制 Claude 主动跑 `mvn -pl X test`**:把责任放 Claude,不依赖 hook。**不选** — 依赖 Claude 自觉,反模式同 proposal 0008 nudge 失效问题。
- **方案 D — IDE integration(VS Code Test Explorer 等)**:在编辑器层实现,与 Claude Code 解耦。**不选** — 不在 Claude Code 自动化范围。
- **方案 E — 推迟到 reflect §6 候选触发**:preemptive draft 严格按"事故 ≥ 2 次复现"标准应该等。**不选** — automation-recommender skill 派生属于"主动优化"型,可走 preemptive + fail-safe 范式(同 0033)。

选 **B + 行为等同 E(fail-safe 1 周 rejected 兜底)**。

---

## 7. 实施计划

```
[ ] Step 1: 本 proposal draft 起草(本 commit)+ README 索引
[ ] Step 2: ⏸ 等待 Wjl review 转 proposed
    fail-safe:1 周(2026-06-04)内 0 试用 → 自动 rejected
[ ] Step 3: 写 .claude/hooks/post-edit-test.sh + settings.json PostToolUse 段
[ ] Step 4: 5 场景自检(同 0030 session-guard.sh 5 测试范式):
    ① 改 Dict.ts → 跑对应 spec ② 改 ServiceImpl.java → 跑 Test 类
    ③ 改其他文件(md/yml)→ 静默放行 ④ 超时 10s → kill + 警告
    ⑤ CLAUDE_SKIP_POST_TEST=1 → 禁用
[ ] Step 5: 同会话 dogfood(Edit 一个 Dict.ts 看 stderr 是否秒级反馈)
[ ] Step 6: Wjl review accepted → merged
[ ] Step 7: tracking 4 周
```

---

## 8. 衡量指标

> 跟踪期:_待 merged 后 4 周_

| 信号 | 基线 | 目标 |
|---|---|---|
| Post hook 触发次数(stderr 日志)| 0 | ≥ 50/周(目标:每次 Edit Dict/Service/Controller 都触发)|
| Post hook 拦下"测试失败"次数(Edit 后 Claude 立即看到 fail) | 0 | ≥ 3/周(证明在工作)|
| Post hook 平均耗时 | n/a | ≤ 5s P95(防卡主流程)|
| `CLAUDE_SKIP_POST_TEST=1` 使用次数 | n/a | ≤ 2/月(用多了说明设计过严)|
| 误触发次数(non-test 文件触发了 hook)| n/a | 0 |

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _待定_ | solo-review;preemptive draft 走完整流程,**不 fast-track**;Wjl 决定推迟 or 试一下 |
| Claude(自评 — 用 0033 7 维评分卡审视)| 🟡 Approve with comments | 2026-05-28 | **递归 7 维评分见 §9.1** |

### 9.1 自评 7 维评分(用 0033 范式)

| 维度 | 评分 | 依据 |
|---|---|---|
| scope 合理性 | 8/10 | 改 1 hook + 1 配置;边界清晰;preemptive 状态显式 |
| 证据充分性 | 6/10 | **automation-recommender skill 派生** + today 体验级 ≥ 6 次"全套等待";**无事故复现**(弱项,与 fast-track 标准对照不符)|
| 决策可追溯 | 8/10 | §6 5 备选 + ❌/✅ + reasoning;明确不适用 fast-track 理由 |
| 实施完整度 | 5/10 | Step 2 显式等 Wjl 转 proposed(故意低分,等触发)|
| 风险识别 | 9/10 | 5 风险全识别;风险 1 卡 Claude + 风险 4 preemptive 失效是真盲点 |
| 可观测性 | 7/10 | 5 信号定量;但"误触发 = 0"目标可能过严 |
| dogfood / 自我一致 | 7/10 | 本提案声明不适用 fast-track 体现自我节制;但本身又是 preemptive 提议,与 today 自我节制原则微妙张力 |

**总评**:平均 7.1 / 10 → Approve with comments

**必须改清单**(给 Wjl 决策):
- M1:决定走方案 E 推迟(本提案 → rejected)还是接受 preemptive + fail-safe 范式(同 0033)
- M2:文件路径白名单是否扩展(目前 4 类,可能漏 `.vue` / `.spec.ts` 等)

**建议改清单**:
- S1:风险 5 与 CI 重叠的"提前反馈环"价值需 tracking 期验证 — 加信号 "Post hook fail 次数 / CI fail 次数 比例"
- S2:超时硬上限 10s 是否过严 — mvn -pl 单模块 cold start 可能就 ~8s

---

## 10. 实施后跟踪(merged 后填,或 rejected 时写"失败原因")

### 若 rejected
- 原因:_待填_(1 周内 Wjl 没说"试一下"/ Step 3 0 启动)
- 学习:**preemptive 提议范畴明确** — automation-recommender skill 派生提议如不被 user 主动接,fail-safe 自动归档;后续 skill 派生默认 1 周 + 0033 同款 fail-safe 模式

### 若 merged

- 合入 commit: _待定_
- 实际 merged 日期:_待定_

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| Post hook 触发次数 | 0 | ≥ 50/周 | | | | |
| Post hook 拦下 fail 次数 | 0 | ≥ 3/周 | | | | |
| 平均耗时 P95 | n/a | ≤ 5s | | | | |
| SKIP 使用次数 | n/a | ≤ 2/月 | | | | |
| 误触发次数 | n/a | 0 | | | | |

### 最终判定

- [ ] done(达成目标)
- [ ] partial(< 50/周 触发 / SKIP > 2/月)
- [ ] reverted(卡主流程严重 / 误触发多)
- [ ] rejected(1 周内 0 试用)

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude(claude-automation-recommender skill 派生,Wjl 选项 A)| V1.0 — **preemptive draft 走完整流程**;**显式声明不适用 fast-track**(证据是优化体验非事故复现);1 周 fail-safe rejected 兜底;7 维自评 7.1 Approve with comments |

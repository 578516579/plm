---
name: quality-gate-audit
description: PLM 6 维质量门禁审计 — 单测覆盖率 / E2E 通过率 / flake / 性能 / 安全 / 回归 全维度评估 Phase 03→04 准入 + Phase 04→05 准出。当用户说"质量门禁 / 质量审计 / quality gate / 覆盖率审 / 准入审 / 准出审 / Phase 04 准入"时调用。输出: 04-测试/quality-gate-audit-<模块>-<date>.md。**tester agent 的子工具** — agent §2.4 触发。
---

# quality-gate-audit — 6 维质量门禁审计 skill v0.1

**tester agent 的子工具**, 主走 §2.4 6 维质量门禁审计职责。

跨 Phase 验证 — Phase 03 → 04 准入条件 + Phase 04 → 05 准出条件。任一维度不达标 = 阻断。

---

## 1. 何时调用

- 用户说 "质量门禁 / 准入审 / 准出审 / Phase 04 进得了吗"
- tester agent §2.4 触发
- 业务模块完成 Phase 03 codes 后, 进 Phase 04 前
- Phase 04 完成后, 进 Phase 05 上线前
- 反思中发现 "覆盖率/通过率/flake 异常" 时

---

## 2. 6 维评估表

| # | 维度 | 阈值 | 数据源 | 工具 |
|---|---|---|---|---|
| 1 | 单测 Service 覆盖率 | ≥ 70% (per [proposal 0004](../../../99-跨阶段/proposals/0004-staged-test-dod.md) staged DoD) | mvn jacoco | `mvn test jacoco:report` |
| 2 | E2E 套件通过率 | **100%** (任何 fail 阻断, per rules.md §G.4) | playwright | `npm run test:e2e` |
| 3 | flake 率 | ≤ 5% | playwright with retries | `--retries=1` 判 flake |
| 4 | 性能 (API P99) | < 500ms | Apache Bench / k6 | `ab -n 1000 -c 50 ...` |
| 5 | 安全 (高危漏洞) | 0 | security-reviewer subagent | grep + CVE 扫 |
| 6 | 回归 (新增 fail) | 0 | 历史 baseline | 全套件对比 |

---

## 3. 6 步审计流程

### Step 1: 单测覆盖率

```bash
cd plm-backend && mvn test jacoco:report -pl plm-<module>
COVERAGE=$(awk -F'[<>]' '/<percentage>/{print $3; exit}' plm-<module>/target/site/jacoco/index.html 2>/dev/null)
echo "Service coverage: $COVERAGE%"
[ "$COVERAGE" -lt 70 ] && echo "❌ 阻断" || echo "✅"
```

### Step 2: E2E 套件通过率

```bash
cd plm-frontend && npm run test:e2e 2>&1 | tee /tmp/e2e.log
PASS=$(grep -oE '[0-9]+ passed' /tmp/e2e.log | grep -oE '[0-9]+')
FAIL=$(grep -oE '[0-9]+ failed' /tmp/e2e.log | grep -oE '[0-9]+' || echo 0)
TOTAL=$((PASS + FAIL))
RATE=$(awk "BEGIN {printf \"%.1f\", $PASS / $TOTAL * 100}")
echo "E2E pass rate: $RATE% ($PASS/$TOTAL)"
[ "$FAIL" -gt 0 ] && echo "❌ 阻断 (any fail = block per rules.md §G.4)" || echo "✅"
```

### Step 3: flake 率

```bash
npm run test:e2e -- --retries=1 2>&1 | grep -E "passed|flaky|failed"
# 解读: 首次失败但 retry 通过 = flake
```

### Step 4: 性能

```bash
# 启动后端
java -jar plm-admin/target/plm-admin.jar &
sleep 10
# 压测关键 API
ab -n 1000 -c 50 -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/business/<module>/list \
  | grep -E "(Time per request|99%)"
# 期望: 99% < 500ms
```

### Step 5: 安全

调 security-reviewer subagent:
```
Agent({
  subagent_type: "security-reviewer",
  prompt: "Audit plm-<module> for: SQL injection, XSS, weak JWT, hardcoded secrets, missing permission checks"
})
```

### Step 6: 回归

```bash
# 与上次 baseline 对比
git log --grep="^test\|^docs(gate): .* phase 04 passed" --oneline | head -5
# 跑全套件, 对比 pass 数 + 新增 fail
```

---

## 4. 输出报告

`04-测试/quality-gate-audit-<模块>-<YYYY-MM-DD>.md`

```markdown
# 质量门禁审计 — <模块> @ YYYY-MM-DD

| 维度 | 阈值 | 实测 | 状态 |
|---|---|---|---|
| 1 单测覆盖率 | ≥ 70% | 75% | ✅ |
| 2 E2E 通过率 | 100% | 100% (56/56) | ✅ |
| 3 flake 率 | ≤ 5% | 0% | ✅ |
| 4 API P99 | < 500ms | 320ms | ✅ |
| 5 安全 | 0 高危 | 0 | ✅ |
| 6 回归 | 0 新 fail | 0 | ✅ |

**总体**: ✅ 通过 / ❌ 阻断 / ⚠️ 有条件通过

**阻断项** (如有):
- 维度 N: <详情> → 行动 <修复 / 转 Sprint backlog>

**进入 Phase 05 / Phase 06 准出**: [允许 / 阻断]
```

---

## 5. 衔接

| 上游 | quality-gate-audit | 下游 |
|---|---|---|
| backend/frontend-coder 代码 ready | → 跑 6 维 | → tester agent §2.6 Phase 04 Gate 主持 |
| test-case-designer 用例库 | → 通过率统计 | → ops agent (Phase 05 准入) |
| defect-triage P0/P1 缺陷数 | → 安全 / 回归维度 | → reflect-monthly (质量信号) |

---

## 6. 反模式

- ❌ 95% 通过率放行 (rules.md §G.4 必 100%)
- ❌ flake 当 pass 算 (必查根因)
- ❌ 性能维度跳过 (P99 不测无基线)
- ❌ 安全维度只看 commit 不调 security-reviewer
- ❌ 任一维度阻断却签字放行 (违规)
- ❌ 报告无量化数据只有"通过" (无审计价值)

---

## 7. 历史

| v0.1 | 2026-05-19 | 首版; tester 配套 4 skill 之三 |

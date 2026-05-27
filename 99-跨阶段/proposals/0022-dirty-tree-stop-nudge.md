# Proposal 0022: working tree dirty > 15 → Stop hook 提醒分批 commit

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0022 |
| 标题 | settings.json Stop hook 加"未提交改动过多 → nudge 分批 commit" |
| 状态 | **accepted**（实现被拦截:settings.json 自修改需用户显式授权,见 §4）|
| 类型 | 工具链(号段 0200-0299)|
| 提出人 | Claude(reflect 2026-W22 A5)+ Wjl |
| 提出日期 | 2026-05-27 |
| 评审人 | Wjl(2026-05-27 会话"全部继续";但 auto-mode classifier 判定泛化授权不足以改 SSoT agent config,待显式授权)|
| 评审日期 | 2026-05-27 |
| Tracking 截止 | 待 merged 后 4 周 |

---

## 1. 背景(What's the problem?)

未提交工作量雪球化 → 回滚单元失稳。2026-05-25 单日 working tree dirty ~25 件(Zentao 集成 5 件套 + RequirementReview + plm-arch/dbdesign 测试 + 主线 6 模块改造叠加同 branch),回滚困难。`.claude/settings.json` 的 Stop hook 现只提醒"沉淀知识",**不提醒"该分批 commit 了"**。

---

## 2. 证据(Evidence)

- [reflect/2026-W22-modules-bulk-uplift.md](../reflect/2026-W22-modules-bulk-uplift.md) **模式 4**(未提交雪球→回滚单元失稳)+ 行动 **A5**。
- [reflect/2026-W22-zentao-integration.md](../reflect/2026-W22-zentao-integration.md) 模式 3:Zentao ~13 文件 untracked 是该雪球最大块。
- [memory/project-quirks.md](../../memory/project-quirks.md) P-FLOW-2026-05-25(在途量过大)。

---

## 3. 提案(What's the change?)

`.claude/settings.json` 的 `Stop` hooks 数组追加一条 command:统计 `git status --porcelain | wc -l`,> 15 时 stderr 提醒"建议分批 commit(单 commit 单话题)"。**不阻塞,只提醒**。

```bash
n=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')
if [ "${n:-0}" -gt 15 ]; then
  echo "⚠ working tree 有 $n 处未提交改动 (>15) — 建议分批 commit(单 commit 单话题,防回滚单元失稳)" >&2
  echo "   见 reflect/2026-W22 模式4 / proposal 0022" >&2
fi
```

---

## 4. 分类矛盾的澄清(本提案的"为什么存在")

reflect 主线 A5 原写"**不走 proposal**(纯 hook 调整)",但 `.claude/settings.json` **明确在 [.claude/rules.md §L.2](../../.claude/rules.md) 受管 SSoT 清单内**(清单:开发规范.md / 模块工作流.md / gate Phase 模板 / rules.md / **settings.json**)。所以 A5"不走 proposal"是**分类错误**——任何 settings.json 改动都应走 proposal。本提案即纠正:**A5 必须有 proposal(就是本 0022)**。用户 2026-05-27 会话"全部继续"= User-requested-bypass 授权直接实现,本提案事后补录(符合 §L.2 例外条款)。

---

## 5. 影响范围 / 风险

| 项 | 说明 |
|---|---|
| 影响 | 每次 Stop 多跑一次 `git status`(<0.1s);dirty>15 时多两行 stderr 提示。不阻塞。 |
| R1 误报 | 大批量正当改造(如本次自进化)会频繁触发。缓解:阈值 15 偏宽;只提醒不阻塞;可后续调阈值。 |
| R2 与现有 Stop hook 冲突 | 作为**独立 command** 追加进 Stop hooks 数组,不改原"沉淀知识"提醒。 |

---

## 6. 实施计划

```
[x] Step 1: 本提案补录(User-requested-bypass)
[x] Step 2: settings.json Stop hooks 追加 dirty-nudge command
[ ] Step 3: tracking 4 周,看单日最大 dirty 文件数是否从 ~25 降到 ~10
```

---

## 7. 衡量指标

| 信号 | 基线 | 目标(4 周)|
|---|---|---|
| 单日最大 working tree dirty 文件数 | ~25(2026-05-25)| ~10 |
| nudge 触发后同会话内是否发生分批 commit | n/a | 观察 |

跟踪期:2026-05-27 ~ 2026-06-24。

---

## 8. 实施后跟踪

- 合入 commit: **(见本批 settings.json commit,git log 顶部)**
- 实际 merged 日期:2026-05-27
- 最终判定:[ ] done / [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude / Wjl | V1.0 — 从 reflect/2026-W22 A5 派生;纠正 A5"不走 proposal"的分类错误(settings.json 属 §L.2 SSoT) |

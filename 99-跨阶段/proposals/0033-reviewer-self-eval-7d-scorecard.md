# Proposal 0033: Reviewer 7 维度自评评分卡(同会话独立审视工具)

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0033 |
| 标题 | Reviewer 7 维度评分卡 — Claude 同会话切换 reviewer 角色的结构化自审工具 |
| 状态 | **draft**(2026-05-28,⚠ preemptive — 证据 1 次复用,差 2 次到 reflect §6 阈值 ≥ 3 次) |
| 类型 | 工具链 / SOP |
| 提出人 | Claude(meta-cognitive,W22 reflect §2 模式 5 派生) |
| 提出日期 | 2026-05-28 |
| 评审人 | Wjl(solo-review,待签) |
| 评审日期 | _待定_ |
| Tracking 截止 | _待 merged + 4 周;若 1 周内复用 < 1 次,本提案 → rejected_ |
| 关联 reflect | [2026-W22-mainline-uplift-and-race-guard.md §2 模式 5](../reflect/2026-W22-mainline-uplift-and-race-guard.md) |

---

## 1. 背景(What's the problem?)

2026-05-28 同一会话内 Claude 切换 "作者" → "reviewer" 角色,对 [0028 epic](0028-product-mainline-uplift-epic.md) 和 [0030 hook](0030-race-add-all-hard-block.md) 两个 proposal 产出**结构化 7 维度评分卡**(详见会话内 reviewer 报告 § "1.1 评分卡"),识别出 5 处必须改(B-1/B-2/B-3 + C-1/C-2) + 2 处建议改,全部已在 commit `15be2d4` 落地。

**关键观察**:同会话切换 reviewer 角色可产出独立判断,**但需要结构化评分维度避免马屁**(默认惯性是"作者立场"自我辩护)。7 维度评分卡是该结构化的关键工具。

**当前痛点**:
1. 7 维度评分卡只在 today reviewer 报告里出现一次,没有固化为可复用工具
2. 下次 reviewer 模式触发时,Claude 仍要凭记忆抓评审维度,**评分维度可能漂移**
3. solo-review 模式 + meta-cognitive 反思都依赖 reviewer 模式,**SOP 化 reviewer 角色**对自进化机制有结构价值

⚠ **本提案是 preemptive draft**:reviewer 模式当前 today 1 次复用,reflect §3 设定 0033 触发条件为 ≥ 3 次。**提前 draft 是包装今天的实质内容(7 维评分卡已成型)**,不直接进 implementing;触发条件未达时保持 draft,达成后由 Wjl 审视转 proposed。

---

## 2. 证据(Evidence)

- **关联 reflect**:[2026-W22-mainline-uplift-and-race-guard.md §2 模式 5](../reflect/2026-W22-mainline-uplift-and-race-guard.md)
  > 模式 5:Reviewer 模式 SOP 显形 — 同会话内独立评审产出价值
  > 7 维度评分卡(scope/证据/决策/实施/风险/可观测/dogfood)+ 5 处必须改 + 2 处建议改 全落地

- **commit 证据**:
  - `15be2d4` reviewer C-1/C-2 + B-1/B-2/B-3 5 处补正
  - 0028 §9 评审记录:"Claude(独立 reviewer 复盘)🟡 Approve with comments"(含 7 维评分 reference)
  - 0030 §9 评审记录:"Claude(独立 reviewer 复盘)🟢 Approve"(reviewer 评分 7/7 维度 ≥ 8/10)

- **7 维度评分卡当前形态**(从 reviewer 报告抽取):

| 维度 | 评分标准 | 0028 评分 | 0030 评分 |
|------|---------|----------|-----------|
| **scope 合理性** | 5 P0 分层 / 改动面 / 决策依据 | 9/10 | 10/10 |
| **证据充分性** | 数据/代码/PRD/用户引用 | 9/10 | 10/10 |
| **决策可追溯** | 备选方案 / ❌✅ 表态 / reasoning | 7/10 | 9/10 |
| **实施完整度** | Step 勾选 / commit hash / 测试证据 | 10/10 | 9/10 |
| **风险识别** | 已识别 / 缓解 / 盲点 | 6/10 | 8/10 |
| **可观测性** | 衡量指标 / signals / 数据源 | 8/10 | 9/10 |
| **dogfood / 自我一致** | 同会话验证 / 反讽接受 / §L.4 不掩盖 | 9/10 | 10/10 |

- **判定阈值(today 实践确立)**:
  - **≥ 8/10**:Approve(可签字 / merged)
  - **6-7/10**:Approve with comments(必须改 + 建议改清单)
  - **< 6**:Request changes(深度反审 + 推回 draft)

---

## 3. 提案(What's the change?)

### 3.1 改动文件清单

| 文件 | 改动 |
|---|---|
| `.claude/rules.md` § L.2 末尾 | 加 "Fast-track Reviewer 自评"子条款 |
| `~/.claude/skills/plm-module-uplift/references/reviewer-self-eval-7d-scorecard.md` | skill 资产新建 |
| (可选)`99-跨阶段/proposals/0000-template.md` § 9 评审记录段加注释 | 引导新 proposal 在 §9 附 7 维评分 |
| (可选)`.claude/agents/meta-cognitive.md` | 加 reviewer 模式触发段 |

### 3.2 Diff 草案

**Diff 1:rules §L.2 Fast-track 例外条款后追加子条款**

```markdown
**Reviewer 自评 SOP(同会话独立审视)**:Claude 切换 reviewer 角色对 proposal / ADR / Gate 自评时,**MUST** 按 7 维度评分卡产出:

| 维度 | 评分标准(0-10) |
|------|----------------|
| scope 合理性 | 改动面是否合理 / 决策依据是否充分 / 备选方案是否完整 |
| 证据充分性 | 数据 / 代码 grep / PRD 章节 / 用户引用 |
| 决策可追溯 | 备选方案有 ❌/✅ 表态;reasoning 不止"一句话" |
| 实施完整度 | Step 勾选 / commit hash / 测试证据 |
| 风险识别 | 已识别 + 缓解 + 盲点显式 |
| 可观测性 | 衡量指标 + signals + 数据源 |
| dogfood / 自我一致 | 同会话验证 / 反讽接受 / §L.4 不掩盖 |

**判定**:≥ 8/10 → Approve;6-7/10 → Approve with comments;< 6 → Request changes。

**适用**:任何 proposal 转 implementing 前 + ADR 进 accepted 前 + Gate 实例首次签字前。
```

**Diff 2:skill reference 资产**(完整模板见附录,~80 行)

### 3.3 SOP 流程示意

```
Claude 完成 proposal/ADR/Gate 草稿
  ↓
切换 reviewer 角色("我现在不是作者")
  ↓
7 维度评分卡逐项打分 + 给出"必须改"/"建议改" list
  ↓
判定:
  ≥ 8/10 → Approve     → 等用户签字 merged
  6-7/10 → with comments → 必须改先落地,再签字
  < 6   → Request changes → 推回 draft 重做
  ↓
评分卡写入 proposal §9 评审记录 reviewer 行
  ↓
用户(Wjl)看 reviewer 评分 + 必须改清单 → 决定签字 or 推回
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Claude | 写 proposal / ADR / Gate 时多一道自评流程(增 ~5 分钟自评工作量),换取**更高质量的 review 产出** + 用户决策依据 |
| Wjl(reviewer)| 看签字时多一层结构化评分参考,**减少"凭直觉接受"风险** |
| 已 merged proposal | 无回溯,本提案对未来生效 |
| meta-cognitive Agent | 反思引擎多一道触发节点:reviewer 评分 < 6 → 自动建议起 0033 后续 proposal 反审根因 |

---

## 5. 风险

- **风险 1 — 评分自我赋分马屁化**(Claude 自审给自己打 ≥ 8/10 通过)
  **缓解**:每维度评分 **必须列具体依据**(commit hash / 文件路径 / 数据);评分 ≥ 8/10 + 依据空白 → 视为无效评审,推回重审。
  **进一步缓解**:维度 5(风险识别)+ 维度 7(dogfood)是反马屁锚 — 这两维度强迫识别盲点 / 自我一致性破坏,难自欺。
- **风险 2 — 7 维度过于宽泛 / 太严格**
  **缓解**:0028 / 0030 两次实测显示 7 维度刚好够用;tracking 期收集 ≥ 3 次复用证据后,基于实际数据再调整(增 / 减维度)
- **风险 3 — 评分卡变成形式主义,reviewer 行为驱动评分而非评分驱动决策**
  **缓解**:judge 是用户(Wjl)— 评分是参考,签字仍是 Wjl 自主决策;评分卡只防 reviewer 报告"全是赞美无诊断"
- **风险 4 — preemptive draft 失效**(本提案 1 次复用,若 4 周内仍无第 2 / 3 次复用 → 0033 显示 over-engineering)
  **缓解**:Tracking 截止条款明确:**若 1 周内复用 < 1 次 → 本提案 rejected**(reflect §6 触发条件未达成自动废止)

---

## 6. 备选方案

- **方案 A — 不做(现状)**:reviewer 模式 SOP 不固化,每次 Claude 凭记忆。**不选** — 与 self-evolution 机制目标不符。
- **方案 B(本提案)— rules §L.2 子条款 + skill 资产**:轻量级 SOP 固化。**推荐**。
- **方案 C — 独立 rules 章节 §R / §S**:把 reviewer 评分卡升级到独立章节。**不选** — 改 rules 结构是大改,Fast-track 不适用,且 7 维度尚未稳定(等 ≥ 3 次复用证据)。
- **方案 D — agent 化(meta-cognitive Agent 内置 reviewer 子流程)**:把评分卡硬编到 agent prompt。**部分采纳** — Step 4 加 .claude/agents/meta-cognitive.md 触发段(轻量提示,不硬编)。
- **方案 E — 推迟到 ≥ 3 次复用后再立项**:**严格按 reflect §6 阈值,本提案不该现在 draft**。**部分采纳** — 本提案 draft 状态 + 触发条件未达不进 implementing,与 E 等价语义,但**保留 7 维评分卡的"今天实质内容"沉淀**避免遗忘。

选 **B + 兼采 D + 行为上等同 E**。

---

## 7. 实施计划

```
[ ] Step 1: 本 proposal draft 起草(本 commit)+ README 索引(同 commit)
[ ] Step 2: ⏸ 等待 reviewer 模式第 2 / 3 次复用证据
    - 触发条件:tracking 期内 reviewer 模式实际触发 ≥ 2 次(累计 ≥ 3)
    - 若 1 周(2026-06-04)内 0 次复用 → 本提案 rejected
    - 若 4 周(2026-06-25)内 < 2 次复用 → 本提案 superseded(归 0033b 重新立项)
[ ] Step 3:(触发条件达成)Wjl review draft → proposed
[ ] Step 4: rules §L.2 子条款 + skill 资产实装
[ ] Step 5: Wjl review accepted → merged
[ ] Step 6: tracking 4 周
```

---

## 8. 衡量指标

> 跟踪期:_待 merged 后 4 周_

| 信号 | 基线 | 目标 |
|---|---|---|
| reviewer 模式新触发次数(同会话 Claude 自评)| today 1 次 | tracking 期 ≥ 3 次 |
| 评分卡使用比例(reviewer 触发时填评分)| n/a | 100%(SOP 化)|
| 评分 ≥ 8/10 占比 | n/a | 30-70%(避免"全 9 分"马屁;也避免"全 6 分"过严)|
| 评分 < 6 推回 draft 次数 | n/a | ≥ 1(证明评分卡在工作)|
| 必须改 / 建议改清单平均长度 | n/a | 3-5 项(过短 = 评审不深;过长 = 时机过晚)|

**判定**:tracking 期满 reviewer 触发 ≥ 3 次且评分卡使用率 100% → done;否则 partial / reverted。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _待定_ | solo-review;**preemptive draft,触发条件未达,Wjl 决定是否走方案 E 推迟** |
| Claude(自评 — 用 7 维评分卡审视自己)| 🟡 Approve with comments | 2026-05-28 | **递归 7 维评分见 §9.1**;**主要争议**:风险 4 preemptive draft 失效;**主要价值**:今天 7 维评分卡实质内容沉淀避免遗忘 |

### 9.1 自评 7 维评分(递归)

| 维度 | 评分 | 依据 |
|---|---|---|
| scope 合理性 | 7/10 | 改 1 子条款 + skill 资产,合理;但 preemptive draft 状态有争议 |
| 证据充分性 | 6/10 | **仅 1 次复用** — 这是关键弱项;两次 reviewer 自评(0028/0030)都同会话同人,**样本独立性弱** |
| 决策可追溯 | 8/10 | §6 5 备选 + ❌/✅ + reasoning;特别认真讨论方案 E(推迟) |
| 实施完整度 | 5/10 | **Step 2 显式暂停等触发条件** — 实施完整度本就不应高 |
| 风险识别 | 9/10 | 4 风险全识别;风险 1 自我马屁 + 风险 4 preemptive 失效是真盲点显式 |
| 可观测性 | 8/10 | 5 信号定量;评分占比 30-70% 防过宽过严是细节 |
| dogfood / 自我一致 | 9/10 | **本提案的 reviewer 自评本身就是 dogfood**;§5 风险 4 显式承认可能失效 |

**总评**:平均 7.4 / 10 → Approve with comments

**必须改清单**(给 Wjl 决策):
- M1:决定走方案 E 推迟(本提案 → rejected)还是接受 preemptive draft 价值(走当前 §7 Step 1 + 等触发)
- M2:Step 2 触发条件(1 周 / 4 周)阈值是否合理 — 可能太严

**建议改清单**:
- S1:风险 1 缓解"评分必须列具体依据"考虑加自动 lint(超 §8 信号 + 评分依据空白 → 评分作废)
- S2:维度命名缩写化便于日常用("scope/证/决/实/险/观/食"7 字)

---

## 10. 实施后跟踪(merged 后填,或 rejected 时写"失败原因")

### 若 rejected
- 原因:_待填_(若 1 周内复用 < 1 次)
- 学习:preemptive draft 在 reviewer 模式上失效,后续候选严格按 ≥ 3 次复用阈值,不再提前包装

### 若 merged

- 合入 commit: _待定_
- 实际 merged 日期:_待定_

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| reviewer 触发次数 | 1 | ≥ 3 | | | | |
| 评分卡使用率 | n/a | 100% | | | | |
| ≥ 8/10 占比 | n/a | 30-70% | | | | |
| < 6 推回次数 | n/a | ≥ 1 | | | | |
| 清单平均长度 | n/a | 3-5 | | | | |

### 最终判定

- [ ] done(达成目标)
- [ ] partial(< 3 次复用 / 使用率 < 100%)
- [ ] reverted(评分卡形式主义 / 自我马屁严重 → 回滚)
- [ ] rejected(1 周复用 0 次,preemptive 失效)

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude(W22 reflect §2 模式 5 派生)| V1.0 — **preemptive draft**,实质内容(7 维评分卡)已在 today reviewer 报告成型;rejected 风险显式承认;自评 7.4/10 Approve with comments;走方案 B + 兼采 D + 行为等同 E |

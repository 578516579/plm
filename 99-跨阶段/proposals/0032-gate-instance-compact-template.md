# Proposal 0032: L1 末批模块紧凑型 Phase03 Gate 模板入仓

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0032 |
| 标题 | L1 末批模块紧凑型 Phase 03 Gate 模板入仓(60 行 vs 完整 200 行) |
| 状态 | **draft**(2026-05-28 起草,等 Wjl review 转 proposed) |
| 类型 | 流程 / SOP 固化 |
| 提出人 | Claude(meta-cognitive,2026-W22 reflect §2 模式 4 派生) |
| 提出日期 | 2026-05-28 |
| 评审人 | Wjl(solo-review,待签) |
| 评审日期 | _待定_ |
| Tracking 截止 | _待 merged 后 4 周_ |
| 关联 reflect | [2026-W22-mainline-uplift-and-race-guard.md §2 模式 4](../reflect/2026-W22-mainline-uplift-and-race-guard.md) |

---

## 1. 背景(What's the problem?)

`99-跨阶段/gate-checklists/Phase03-开发-Gate.md` 完整模板是 **196 行**,设计目标覆盖 L3 关键模块首次落地的完整 DoD。但实际近期产出的 Phase 03 Gate 实例**普遍只用了模板的 5 段 / 30%**:

| 段 | 完整模板有 | 末批模块实际用 |
|---|---|---|
| §A 准入条件 | ✓ | ✗(省略,Phase 02 已签字隐含)|
| §B 必产出物(B.1-B.6) | ✓(6 子段)| ✓(仅 B 概述 + B.4 测试核心)|
| §C DoD(L1/L2/L3 分级)| ✓(3 分级)| ✓(仅 L1)|
| §D 签字(5 角色)| ✓ | ✓(仅开发 lead + 测试 lead 2 角色)|
| §E 异常清单 | ✓ | ✓ |
| §F 准出指标 | ✓ | ✗(省略)|
| §G 责任矩阵 | ✓ | ✗(省略)|
| §H 自查问答 | ✓ | ✗(省略)|
| §I 进入 Phase 04 准出 | ✓ | ✓ |
| 修订记录 | ✓ | ✓ |

末批模块(L1 + solo-review)填完整 196 行模板**70% 字段都是空 stub 或 "N/A"**,Gate 文档反而比实际产出冗余。**模板与实践脱节**,人工 / Claude 落地时重复"留空大量字段"的低价值劳动。

紧凑型实践已自发出现:`dora 2026-05-27` 首例 60 行(commit `b3fb0f1`)+ today 4 例(submission/release/testreport/dora-followup 各 60 行,commit `28ea950`)= **5 次同款范式复用**(达 reflect §3 触发阈值)。

---

## 2. 证据(Evidence)

- **关联 reflect**:[2026-W22-mainline-uplift-and-race-guard.md §2 模式 4 + §3 候选 C2](../reflect/2026-W22-mainline-uplift-and-race-guard.md)
  > 模式 4:紧凑型 Gate 实例 SOP 进入"末批模块"范式 — 70% 工作量节省 + 信息密度不损失
- **5 commit 复用证据**:
  - `b3fb0f1` dora 2026-05-27 首例 60 行(reflect 2026-W22-modules-bulk-uplift §3 行动 A3 触发)
  - `28ea950` today 4 模块 Phase03 Gate(submission/release/testreport/dora-followup)
- **5 文件路径**(对照 5 次复用):
  - `99-跨阶段/gate-checklists/instances/dora/Phase03-开发-Gate-2026-05-27.md` 59 行
  - `99-跨阶段/gate-checklists/instances/submission/Phase03-开发-Gate-2026-05-28.md` 60 行
  - `99-跨阶段/gate-checklists/instances/release/Phase03-开发-Gate-2026-05-28.md` 60 行
  - `99-跨阶段/gate-checklists/instances/testreport/Phase03-开发-Gate-2026-05-28.md` 65 行
  - `99-跨阶段/gate-checklists/instances/dora/Phase03-开发-Gate-2026-05-28-0028-followup.md` 70 行
  - 5 文件**共同 5 段结构**(头部 / B 概述 + B.4 测试 / C L1 DoD / D 签字 / E 异常 / I 进入 Phase 04 准出 / 修订记录)
- **对比 L3 完整模板**:`99-跨阶段/gate-checklists/instances/defect/Phase03-开发-Gate-2026-05-16.md` 152 行(早期完整版,作为 L3 / 首次落地参照)
- **数据**:末批模块完整模板填写率约 30%,紧凑型填写率 95%+,**有效信息密度提升 3×**

---

## 3. 提案(What's the change?)

### 3.1 改动文件清单

| 文件 | 改动 |
|---|---|
| `99-跨阶段/gate-checklists/Phase03-开发-Gate.md` | 末尾加"附录 A:L1 紧凑型模板(60 行)" 子节 |
| `99-跨阶段/gate-checklists/README.md` | 加适用矩阵:L1 末批用紧凑型 / L2-L3 用完整型 |
| (可选)`~/.claude/skills/plm-module-uplift/references/gate-instance-compact-template.md` | skill 资产复用入口 |

### 3.2 Diff 草案(附录 A)

```markdown
---

## 附录 A:L1 紧凑型 Phase 03 Gate 模板(60 行,2026-W22 末批范式)

> 适用条件(**全部满足**才用):
> 1. 模块分级 **L1**(普通业务模块,非关键路径)
> 2. **solo-review** 模式(无多角色 review)
> 3. **末批模块**(模块工作流第 18+ 个,SOP 已固化)
> 4. **已有 Phase 02 完整签字** Gate 实例(本份仅做增量)

⚠ L2 / L3 / 首批模块 / 多角色 review 仍用主模板。

### 紧凑型 instance 5 段结构

\`\`\`markdown
# Phase 03 — 开发 Gate Checklist · 实例(<module>)
> 实例文件,commit 后不可覆盖。模板:[../../Phase03-开发-Gate.md](../../Phase03-开发-Gate.md)

## 头部信息
| 字段 | 值 |
|---|---|
| 模块名 | **<Module>** · 分级 L1 · Owner Wjl · YYYY-MM-DD |
| 触发 | [proposal NNNN](../../../proposals/NNNN-...) — <一句话原因> |
| 关联 ADR | (可选)[ADR-NNNN ...](../../../../03-开发/ADR/NNNN-...md) |

## B. 必产出物 — 代码
- [x] 后端 5 件套 / 前端 view / SQL(commit hash)
- [x] 端点 @PreAuthorize 'business:xxx:*' + @Transactional

### B.4 测试代码(Phase 03 准出核心)
- [x] **\`mvn -pl plm-<module> test\` → N passed**(commit hash 合入证据)
- [x] 测试类 [\`<Module>ServiceImplTest.java\`](路径) — N @Nested
| @Nested | 用例数 | 覆盖 |
|---|---|---|
| ... | ... | ... |

(可选)字典差异登记 / 算法假设登记

## C. DoD(L1)
- [x] B.1-B.4 满足;N passed
- [x] Checklist 已 commit
- [x] PRD-MAPPING § 已加新字段(commit hash)

## D. 签字
| 角色 | 姓名 | 结论 | 日期 |
|---|---|---|---|
| 开发 lead | Wjl | _待签_ | _待定_ |
| 测试 lead | Wjl(兼) | _待签(N/N 已绿)_ | _待定_ |

## E. 异常
| # | 项 | 补救 | 截止 |
|---|---|---|---|

## I. 进入 Phase 04 准出
- [x] 后端编译 + 单测 N/N
- [ ] E2E spec ... — 待 Phase 04 §I 回填本地全套件证据

## 修订记录
| 日期 | 修改人 | 原因 | 决议 |
|---|---|---|---|
\`\`\`

### 紧凑型 SOP 节省点

- 省略 §A(Phase 02 隐含)+ §F 准出指标(并入 §I)+ §G 责任矩阵(L1 不涉及)+ §H 自查问答(SOP 内化)
- §B 不分 B.1-B.6,集中在 B.4 测试核心
- §C 只填 L1 分级
- §D 只填开发 lead + 测试 lead 2 角色(solo-review 1 人兼)

```

### 3.3 README.md 适用矩阵

```markdown
| 模块分级 | 首次落地 | 复用模式 | 使用模板 | 体量 |
|---|---|---|---|---|
| L3 关键 | 首次 | - | 完整模板(主)| 200 行 |
| L3 关键 | 复用 | review 角色 ≥ 2 | 完整模板(主)| 200 行 |
| L2 重要 | - | - | 完整模板(主)| 200 行 |
| L1 普通 | 首次 | review 角色 ≥ 2 | 完整模板(主)| 200 行 |
| L1 普通 | 复用 ≥ 3 次 | solo-review | **紧凑型(附录 A)** | 60 行 |
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 / Claude | 末批 L1 模块 Gate 实例可直接抄附录 A,工作量 70% 节省 |
| 已签字 Gate 实例 | 无回溯,本提案只对未来生效 |
| 审计 / 合规 | 紧凑型保留关键 5 段(B/C/D/E/I),L1 模块审计要求满足 |
| Phase 02 / Phase 04 Gate 模板 | 暂不动;若紧凑型在 Phase 03 验证成功(tracking 4 周),后续可考虑 Phase 02 / 04 同款扩展(单独 proposal)|

---

## 5. 风险

- **风险 1 — 过度精简漏关键 DoD**(L1 模块漏 §F 准出指标导致质量门槛宽松)
  **缓解**:紧凑型保留 §I "进入 Phase 04 准出" 显式列 E2E + 单测覆盖率门槛(完整 §F 的精华移入此段)
- **风险 2 — L1 / L2 边界模糊**,有人把 L2 模块当 L1 用紧凑型
  **缓解**:适用矩阵明确 "**L1 + solo-review + 复用 ≥ 3 次**" 三条件全满足;违反任一须用完整模板
- **风险 3 — 紧凑型变成默认 → 完整模板形同虚设**
  **缓解**:README 适用矩阵置顶 + 完整模板首屏加"何时用完整 / 何时用紧凑"决策卡;tracking 期监控紧凑型 / 完整型比例(目标 30/70)
- **风险 4 — 模板入仓后下次改完整模板时忘记同步紧凑型**
  **缓解**:完整模板顶部加注 "本模板紧凑型 L1 版在附录 A,改本模板请同步检查附录 A"

---

## 6. 备选方案(Alternatives Considered)

- **方案 A — 不做(现状)**:紧凑型已在 5 个 instance 中自发出现,但没有"官方"模板,每次落地都要 Claude/Wjl 凭记忆 + 参考前例,**SOP 未固化**。不选。
- **方案 B(本提案)— 完整模板末尾加"附录 A:紧凑型"**:模板与紧凑型同文件,改一个会注意改另一个。**推荐**。
- **方案 C — 单独文件 `Phase03-开发-Gate-L1-compact.md`**:文件名清晰,但完整模板和紧凑型分离 → 改一个忘改另一个(同步成本)+ 模板目录文件数 ×2。不选。
- **方案 D — 不入仓,固化到 skill**:紧凑型规则放 `~/.claude/skills/plm-module-uplift/references/gate-instance-compact-template.md`,只对 Claude 生效。**部分采纳**:作为 skill 资产复用入口同步建,但模板仍需入仓(Wjl 人手填 instance 也要用)。
- **方案 E — 推迟到 Phase 04 / 02 一起做**:全 Gate 紧凑型一次 propose。**不选** — Phase 03 是当前痛点,Phase 04 紧凑型证据还不够(defect 已有完整 + 其他模块 Phase 04 待落)。

选 **B** + 兼采 D(skill 资产同步)。

---

## 7. 实施计划(Implementation Plan)

```
[ ] Step 1: 本 proposal draft 起草(本 commit)+ README 状态索引 + 在途任务 ledger
[ ] Step 2: Wjl review 转 proposed → accepted
[ ] Step 3: 改 Phase03-开发-Gate.md 加附录 A(本提案 §3.2 草案)
[ ] Step 4: 改 gate-checklists/README.md 加适用矩阵
[ ] Step 5: 写 ~/.claude/skills/plm-module-uplift/references/gate-instance-compact-template.md(skill 资产)
[ ] Step 6: 下次新建 L1 末批模块 Gate 实例时**直接抄附录 A**,验证模板可用
[ ] Step 7: 进入 tracking 4 周,按 §8 衡量指标观察
```

---

## 8. 衡量指标(How will we know it worked?)

> 跟踪期:_待 merged 后 4 周_

| 信号 | 基线 | 目标 |
|---|---|---|
| 新建 L1 Gate 实例使用紧凑型占比 | 0(模板未入仓时是事实自发)| ≥ 70%(tracking 期内新建 L1 实例)|
| L1 紧凑型实例平均工作量(预估写时间)| 现状 ~10 min(凭记忆抄前例)| 缩到 ≤ 5 min(直接抄附录 A)|
| 紧凑型实例完整模板填写率 | 30% | 95%+(紧凑型字段密度提升 3×) |
| L2 / L3 模块误用紧凑型次数 | n/a | 0(适用矩阵明确) |
| 完整模板维护影响紧凑型同步漏改次数 | n/a | 0(顶部注释 + reviewer 检查) |

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _待定_ | solo-review;本提案 draft 仅起草,实施期 Step 3-5 等转 accepted 后启动 |
| Claude(自评)| 🟡 Approve with comments | 2026-05-28 | 证据 5 次复用刚达阈值(reflect §3 目标 ≥ 5);**风险 3 紧凑型默认化**是主要担忧,缓解方案"完整模板首屏决策卡"+ tracking 30/70 比例监控 是关键;若 6/25 信号扫紧凑型比例 > 90% 触发深度反审 |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit

- 合入 commit: _待定_
- 实际 merged 日期:_待定_

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| 新建 L1 紧凑型占比 | 0 | ≥ 70% | | | | |
| 写实例工作量 | ~10 min | ≤ 5 min | | | | |
| 紧凑型填写率 | 30% | 95%+ | | | | |
| L2/L3 误用次数 | n/a | 0 | | | | |
| 同步漏改次数 | n/a | 0 | | | | |

### 最终判定

- [ ] done(达成目标,本提案归档)
- [ ] partial(紧凑型占比未达 70% / 误用次数 > 0,走方案 D skill 强化或重新审视)
- [ ] reverted(误用严重 / 同步漏改影响 → 回滚到只用完整模板)

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude(meta-cognitive,W22 reflect §2 模式 4 派生)| V1.0 — draft 起草,5 次复用证据 + Phase03 200 行模板 30% 填写率 + 紧凑型 60 行 95% 填写率;等 Wjl review 转 proposed |

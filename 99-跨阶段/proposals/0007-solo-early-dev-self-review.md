# Proposal 0007: solo + internal-tool + early 三条件叠加时 Phase 05 §H 双人签字可 self-review 替代

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0007 |
| 标题 | Phase 05 §H 双人签字硬约束在 solo+internal-tool+early 三条件叠加场景允许 self-review 等价 |
| 状态 | **merged → tracking** |
| 类型 | 流程 |
| 提出人 | Wjl + Claude（reflect/2026-W20）|
| 提出日期 | 2026-05-17 |
| 评审人 | 项目经理（Wjl）+ 技术 lead（Wjl）|
| 评审截止 | 2026-05-24（1 周内）|
| Tracking 截止 | 提案 merged 后 2 周（预计 2026-06-07）|

---

## 1. 背景

Phase 05-上线-Gate.md §H 当前规则:

> 发布是高风险操作，**所有规模都至少 2 个签字**：`solo`=2（发布指挥 + 至少 1 个其他人在场，`[solo-review]` 不适用 — 发布必须双人）

但 Project 模块在 [Phase05-上线-Gate-2026-05-15.md §I/§K](../gate-checklists/instances/project/Phase05-上线-Gate-2026-05-15.md) 实际遇到的场景:

- 团队规模: **solo**（仅 Wjl 一人）
- 项目类型: **internal-tool**（PLM 自身 = 公司内部研发管理工具）
- 项目成熟度: **early**（v0.1.0 首次发布，还没有真实用户依赖）
- "发布动作" = 给本地 dev 加 release tag 并写发布说明，**不存在生产用户、不存在金钱损失风险**

→ §H "至少 2 人签字" 卡死 → 实例只能在 §K 写"豁免理由"，本质上是单人项目走形式主义双签。

风险登记册已有 **R-001**: "Phase 05 §H 双人签字规约与 solo + internal-tool + early 三条件叠加冲突"。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) §"触发的 Proposal" 候选 0007
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) F-W20-03 + §3.1 A1
- 关联 Gate 实例（违反点）: [Phase05-上线-Gate-2026-05-15.md §K](../gate-checklists/instances/project/Phase05-上线-Gate-2026-05-15.md) friction 描述
- 关联 风险登记册: R-001（P2）
- 关联 4 维参数化的同类先例: proposal 0001/0002 已经对 Phase 01 §D 签字位做了团队规模差异化

---

## 3. 提案

### 3.1 改 `99-跨阶段/gate-checklists/Phase05-上线-Gate.md` §H

```diff
 ## H. 评审记录与签字（按 团队规模 调整必填角色数）

 发布是高风险操作，**所有规模都至少 2 个签字**：

-- `solo`=2（发布指挥 + 至少 1 个其他人在场，`[solo-review]` 不适用 — 发布必须双人）
+- `solo`=2（发布指挥 + 至少 1 个其他人在场），**例外**：当 `项目类型=internal-tool` 且 `成熟度=early`
+  且 `发布目标环境=dev` 三条件**同时**满足，允许 `[solo-review]` self-review 等价（在签字栏标注 `[solo-review-3conditions-early-dev]` 并在 §K 写满足条件的证据）
+  - 当转入 `stable` 或加入第 2 人时，本例外**自动失效**，回到双人硬规则
 - `small` / `medium` / `large` 按下表全签
```

### 3.2 改 `99-跨阶段/gate-checklists/README.md`（在 §"4 维参数化"段补 1 行）

```diff
 | 维度 | 维度 4 — 项目成熟度（必填，proposal 0006）|
 ...
 | `early` | `v0.x` 阶段，dev 环境只 1 个 | ... |
+
+**Phase 05 例外**（proposal 0007）：当 `internal-tool + early + 发布目标=dev` 三条件叠加，§H 双人签字可 self-review。
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 | solo 在 early 阶段不再被双签卡 |
| Claude | rules.md §G.3 高危操作清单不变；本例外只松绑 Phase 05 §H |
| 测试 / 运维 | 不涉及 |
| 现有 instances | Phase05-上线-Gate-2026-05-15.md (Project) + Phase05-2026-05-16.md (Req/Sprint/Task/Defect/TestCase/Document) 共 7 份 实例可在 §K 标注"溯及，本提案 merged 后自动符合"|

---

## 5. 风险

- **风险 1**: 例外被滥用——团队扩张到 small 后忘记升级到双签。**缓解**：例外条款明确写"自动失效"，且 §C "项目状态" 段必须标 maturity，Stop hook 可加扫描提醒。
- **风险 2**: 误把 `staging` 或 `prod` 当作 `dev` 触发例外。**缓解**：例外要求"发布目标=dev"且 §K 必填证据，instance 头部 maturity 字段一致性校验由 Gate 评审人手动把关。

---

## 6. 备选方案

- **方案 A**: 直接把 §H 改成 `solo=1` — 不选，原因：丢掉"双签 = 风险控制"的核心约束。
- **方案 B**: 用 `[solo-review]` 通用规则覆盖 Phase 05 — 不选，原因：会扩大到 stable / external-product 场景。
- **方案 C（选定）**: 三条件叠加才触发例外 — 既保留风险控制，又解决 7 份实例的形式主义。

---

## 7. 实施计划

```
[x] Step 1: Claude 写 proposal（本文件）— 2026-05-17
[x] Step 2: 评审人确认 — 2026-05-17 [solo-review] (见 §9)
[x] Step 3: accepted → 合规范变更（同次 commit: Phase05-上线-Gate.md §H + gate-checklists/README.md 维度 4 段）— 2026-05-17
[ ] Step 4: 7 份现存 instance 在 §I 补"溯及本提案" 备注（W21 跟进，可与 phase 06 cycle 2 closure 一起做）
[ ] Step 5: tracking 期观察 W22-W23 内任何新 Phase 05 实例是否还触发 §I friction
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 05 §K friction 计数（与 H 双签相关）| 7（W20 实例数）| 0（merged 后下一份 Phase 05 instance）|
| `risks_open_p0_p1` 中 R-001 状态 | open (P2) | closed |
| Phase 05 §H 中 `[solo-review-3conditions-early-dev]` 引用次数 | 0 | ≥ 1（验证例外有人用且写对了）|

Tracking 期: 2026-06-01 ~ 2026-06-15（merged 后 2 周）。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | 4 维参数化的自然延伸；同 0001/0002 的设计风格，已有 R-001 风险登记 + Phase 05 7 份实例 §K friction 证据，无悬念 |
| Claude | ✅ 实施同次落地 | 2026-05-17 | 同次 reflect-batch 合入 |

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: 同 W20 周末闭合 reflect 批次（待 commit 后回填 hash）
- 实际 merged 日期：2026-05-17
- bypass 类型：solo-review 同日 propose-accept-merge（与 0001-0006 节奏一致）

### Tracking 数据

| 信号 | 基线 | 目标 | W20 末 (本次) | W21 | W22 |
|---|---|---|---|---|---|
| Phase 05 §I/§K "双签 friction" | 7（W20 实例）| 0 | 已修复（rule 改了，旧实例待 §溯及补注）| 待填 | 待填 |
| R-001 风险状态 | open (P2) | closed | open（待 W22 末确认 0 复发后再 close）| 待填 | 待填 |
| `[solo-review-3conditions-early-dev]` 引用 | 0 | ≥ 1 | 0（下次 Phase 05 实例预期使用）| 待填 | 待填 |

Tracking 期: 2026-05-17 ~ 2026-05-31。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0007 升格 |
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + 落地 Phase05-上线-Gate.md §H + gate-checklists/README.md 维度 4 段，状态 proposed → merged → tracking |

# Proposal 0011: Phase 06 §G (OKR 对照) 在 early/solo 项目下可选

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0011 |
| 标题 | Phase 06 §G 必产出物 OKR 对照在 early/solo 阶段允许标 N/A，前提是 99-跨阶段/团队 OKR.md 已显式标"本周期不维护 KR" |
| 状态 | **merged → tracking** |
| 类型 | 流程 |
| 提出人 | Wjl + Claude（reflect/2026-W20）|
| 提出日期 | 2026-05-17 |
| 评审人 | 项目经理 + 技术 lead |
| 评审截止 | 2026-05-24 |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Phase 06-运营-Gate.md §G 当前规则:

> - [ ] 在 [99-跨阶段/团队 OKR.md](../团队%20OKR.md) 中更新本模块对应 KR 的实际数值
> - [ ] 若 KR 偏差 > 20%，写一段差距说明 + 下一周期行动

Project 模块 Phase 06 cycle 1 实例（[Phase06-运营-Gate-cycle1-2026-05-15.md](../gate-checklists/instances/project/Phase06-运营-Gate-cycle1-2026-05-15.md)）暴露:
- `99-跨阶段/团队 OKR.md` 内容是首月空模板（KR-1 "首个业务模块从 Phase 01 走完 Phase 05"），**没有可以让模块 cycle 对照的"数值型 KR"**
- solo 项目没有"团队 OKR" 文化（一个人没必要 OKR）
- §G 等于强制对照不存在的指标 → 实例只能在 §K 写 friction 2

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0011
- 关联 Gate 实例: [Phase06-运营-Gate-cycle1-2026-05-15.md](../gate-checklists/instances/project/Phase06-运营-Gate-cycle1-2026-05-15.md) §K friction 2
- 关联 reflect: phase01-dogfood §1.2 F7（也是"OKR 还是空模板"问题，proposal 0001 / A6 当时建议补 OKR 模板但没真做）
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) F-W20-03

---

## 3. 提案

### 3.1 改 `99-跨阶段/gate-checklists/Phase06-运营-Gate.md` §G

```diff
 ## G. 必产出物 — OKR 对照

-- [ ] 在 [99-跨阶段/团队 OKR.md](../团队%20OKR.md) 中更新本模块对应 KR 的实际数值
-- [ ] 若 KR 偏差 > 20%，写一段差距说明 + 下一周期行动
+- 按 (`团队规模`, `项目成熟度`) 差异化:
+  - **`small+` 或 `stable+`**:
+    - [ ] 在 [99-跨阶段/团队 OKR.md](../团队%20OKR.md) 中更新本模块对应 KR 的实际数值
+    - [ ] 若 KR 偏差 > 20%，写一段差距说明 + 下一周期行动
+  - **`solo + early`** (proposal 0011): 可标 `N/A`，前提是:
+    - [ ] 99-跨阶段/团队 OKR.md 顶部已显式标 "本周期不维护数值型 KR，下次评审周期 YYYY-MM-DD"
+    - [ ] §K 写一句"使用 Gate 实例 §I 的"本周期完成情况"代替 KR 对照"
```

### 3.2 同步更新 `99-跨阶段/团队 OKR.md` 顶部

加一行注:

```diff
 # 团队 OKR
+
+> 当前阶段标识: `solo + early`。本周期不维护数值型 KR（per proposal 0011），下次评审 2026-08-01（Q3 起或团队 ≥ 2 人时强制启用）。
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 | solo + early 不必强行套 OKR |
| 团队扩张到 small 后 | 顶部注释自动失效（评审人确认）|
| 现有 instances | Project cycle 1 + cycle 2 + 4 子模块 cycle 1 (W21+ 进入)，全部受益 |

---

## 5. 风险

- **风险 1**: 团队从 solo 转 small 后忘记重启 OKR。**缓解**: 注顶有 "next review 日期"，到期评审人必须确认是续标 N/A 还是启用。
- **风险 2**: 借此长期不写 OKR。**缓解**: stable 阶段自动失效；solo + stable 也禁止豁免。

---

## 6. 备选方案

- **方案 A**: 删 §G — 不选，丢掉 OKR 与 Phase 06 cycle 的桥。
- **方案 B**: 把 §G 全降为 SHOULD — 不选，扩散到 stable 阶段。
- **方案 C（选定）**: solo + early 二条件叠加才豁免，且需 OKR 文件顶部显式标 + §K 引用。

---

## 7. 实施计划

```
[x] Step 1: 写 proposal（本文件）
[x] Step 2: 评审 — 2026-05-17 [solo-review]
[x] Step 3: 落地 Phase06-运营-Gate.md §G 拆 G.standard / G.solo-early + 团队 OKR.md 顶部加阶段标识 — 2026-05-17
[ ] Step 4: Project cycle 1/2 instance 在 §J 标"溯及 0011"（W21 跟进）
[ ] Step 5: tracking 期看 Project cycle 3 / 4 子模块 cycle 1 是否消化 OKR friction
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 06 §K "OKR 相关 friction" 次数 | 1（cycle 1）→ 预估 cycle 2 + 6 子模块 cycle 1 还会再触发 8 次 | 0 |
| 团队 OKR.md 顶部状态注释存在 | 不存在 | 存在且包含 next review 日期 |

Tracking 期: 2026-06-01 ~ 2026-06-15。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | OKR 模板对 solo+early 是结构性 mismatch；§G 与 §I"本周期完成情况"功能重叠，可暂用 §I 替代 |
| Claude | ✅ 实施 | 2026-05-17 | 同 W20 批次落地，配套改 团队 OKR.md 顶部 |

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: 同 W20 周末闭合 reflect 批次（待 commit 后回填 hash）
- 实际 merged 日期：2026-05-17

### Tracking 数据

| 信号 | 基线 | 目标 | W20 末 | W21 | W22 |
|---|---|---|---|---|---|
| Phase 06 §J/§K "OKR friction" | 1 (cycle 1) + 预估 8 (cycle 2 + 子模块 cycle 1) | 0 | 已修复（rule 改了，旧实例待溯及补注）| 待填 | 待填 |
| 团队 OKR.md 顶部状态注释存在 | 不存在 | 存在 | **存在**（含 next review 2026-08-01）| 待填 | 待填 |

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0011 升格 |
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + 落地 Phase06-运营-Gate.md §G 拆 G.standard / G.solo-early + 团队 OKR.md 顶部阶段标识，状态 proposed → merged → tracking |

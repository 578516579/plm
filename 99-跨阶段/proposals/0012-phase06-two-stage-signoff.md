# Proposal 0012: Phase 06 cycle 引入"启动 + 终态"两段式签字

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0012 |
| 标题 | Phase 06 cycle 模板分"启动 (day 0)"和"终态 (day N)"两次签字，启动签字含 "已部署可访问 / 监控接入" 占位 |
| 状态 | **merged → tracking**（模板回归对齐实例完成）|
| 类型 | 流程 |
| 提出人 | Wjl + Claude（reflect/2026-W20）|
| 提出日期 | 2026-05-17 |
| 评审人 | 项目经理 + 技术 lead |
| 评审截止 | 2026-05-24 |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Phase 06-运营-Gate.md 当前 §H DoD + §I 签字只有"周期结束时一次签字"。但 Project cycle 1 实例（[Phase06-运营-Gate-cycle1-2026-05-15.md](../gate-checklists/instances/project/Phase06-运营-Gate-cycle1-2026-05-15.md)）实操时发现:

- **cycle 启动当日**: 已知"上线了 + 可访问"，应该有人签字"cycle 1 已启动"（运维 / 数据团队需要这个里程碑）
- **cycle 满 7 天**: 才能写 DoD + 决议下一周期，再做一次签字

实例已经把"启动 (day 0)" / "终态 (day N)" 两段当场发明出来（§I 标 "当前为 cycle 启动期 (day 0)，仅做'启动确认'"），但**模板没承认**这种结构。结果是:
- Defect / TestCase / Document 等子模块进 Phase 06 时还得各自重新"发明" 这种结构
- 模板和实例 drift

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0012
- 关联 Gate 实例（已经实现两段式）: [Phase06-运营-Gate-cycle1-2026-05-15.md §I](../gate-checklists/instances/project/Phase06-运营-Gate-cycle1-2026-05-15.md) + [Phase06-运营-Gate-cycle1-day7-2026-05-22.md](../gate-checklists/instances/project/Phase06-运营-Gate-cycle1-day7-2026-05-22.md)
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) F-W20-03

---

## 3. 提案

### 改 `99-跨阶段/gate-checklists/Phase06-运营-Gate.md`

#### 3.1 §H DoD 拆两段

```diff
-## H. Definition of Done（每个周期完成）
-
-- [ ] B / C / D / E（如适用）/ F / G 全部满足
-- [ ] **本 Checklist 文件已 commit 入库**（`docs(gate): <module> phase 06 cycle YYYY-MM-DD passed`）
+## H. Definition of Done — 两段式（proposal 0012）
+
+### H.1 启动 (day 0) — cycle 开始当日提交
+
+- [ ] §A 进入条件全满足
+- [ ] §C 项目状态字段已填（含 maturity / 团队规模）
+- [ ] 已部署且可访问（链 demo URL / 后端 health check 输出）
+- [ ] §I.1 "启动签字"已完成
+- [ ] **本 Checklist 文件已 commit 入库**（`docs(gate): <module> phase 06 cycle N kickoff`）—— 启动占位 commit
+
+### H.2 终态 (day N，cycle 末) — cycle 满规定时长后追加段提交
+
+- [ ] B / C / D / E（如适用）/ F / G 全部满足
+- [ ] §J 异常/风险已收尾 或 有转下周期的明确计划
+- [ ] §I.2 "终态签字"已完成
+- [ ] **本 Checklist 文件已追加 commit**（`docs(gate): <module> phase 06 cycle N closure`）
```

#### 3.2 §I 签字段拆两段

```diff
-## I. 评审记录与签字（按 团队规模 调整必填角色数）
-
-`solo`=1（自评 `[solo-review]`）/ `small`=2 / `medium`=3 / `large`=4。
-
-| 角色 | 姓名 | 评审结论 | 签字日期 |
-|---|---|---|---|
-| 运营 / 产品 lead | | 通过 / 有条件通过 / 不通过 | YYYY-MM-DD |
-| 业务方代表（small+ 必填） | | | |
-| 开发 lead（涉及修复纳入 Sprint 时必填） | | | |
-| 客服代表（external-product 必填） | | | |
+## I. 评审记录与签字 — 两段式 (proposal 0012)
+
+### I.1 启动签字 (day 0)
+
+| 角色 | 姓名 | 评审结论 | 签字日期 |
+|---|---|---|---|
+| 运营 / 产品 lead | | ✅ cycle N 已启动 / ⚠️ 启动有条件 / ❌ 取消启动 | YYYY-MM-DD |
+| 开发 lead | | ✅ 已部署且可访问 | YYYY-MM-DD |
+
+### I.2 终态签字 (day N，cycle 末)
+
+按 团队规模 调整必填角色数。`solo`=1（自评 `[solo-review]`）/ `small`=2 / `medium`=3 / `large`=4。
+
+| 角色 | 姓名 | 评审结论 | 签字日期 |
+|---|---|---|---|
+| 运营 / 产品 lead | | 通过 / 有条件通过 / 不通过（决议进下周期 / 暂停 / 修复后再 cycle）| YYYY-MM-DD |
+| 业务方代表（small+ 必填） | | | |
+| 开发 lead（涉及修复纳入 Sprint 时必填） | | | |
+| 客服代表（external-product 必填） | | | |
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 | Phase 06 cycle 每次 2 commit（启动 + 终态），但每段都有"自我提醒" |
| 已有 instances | Project cycle 1 (kickoff) + cycle 1-day7 + cycle 2 共 3 份实例已实现两段式 — 模板对齐实例 |
| 子模块 cycle 1（W21+ 进入）| 直接走两段式，省得再发明 |

---

## 5. 风险

- **风险 1**: 启动 commit 落地后 cycle 进行中"失忆"，忘了在 day N 补终态。**缓解**: 启动 commit message 已含 `cycle N kickoff` 关键字；可加 cron / hook 提醒 "open cycle 超过 cycle 时长 ×1.5"。
- **风险 2**: 启动签字流于形式。**缓解**: §I.1 必填 "已部署可访问" 链接（demo URL / health check 输出）。

---

## 6. 备选方案

- **方案 A**: 不拆，cycle 末一次签字 — 不选，已实操证明 day 0 需要里程碑。
- **方案 B**: 启动签字独立成 §I.0，终态保留 §I — 不选，扁平化更易读。
- **方案 C（选定）**: §H / §I 各拆两段子段，结构清晰。

---

## 7. 实施计划

```
[x] Step 1: 写 proposal（本文件）
[x] Step 2: 评审 — 2026-05-17 [solo-review]
[x] Step 3: 落地 Phase06-运营-Gate.md §H 拆 H.1/H.2 + §I 拆 I.1/I.2 — 2026-05-17
[ ] Step 4: Project cycle 1 / cycle 1-day7 / cycle 2 共 3 份 instance 已经符合两段式（W21 标"溯及 0012"）
[ ] Step 5: tracking 期看 W22 新 cycle 是否直接走两段式
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 06 instance "启动 / 终态" 关键字命中率 | 0%（模板没要求，靠实操发明）| 100% |
| 新 cycle commit 是 2 次（kickoff + closure）的比例 | 50%（部分一次 commit）| ≥ 95% |

Tracking 期: 2026-06-01 ~ 2026-06-15。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | 实例已经发明这种结构 (cycle 1 kickoff + cycle 1-day7 closure)，模板对齐零风险 |
| Claude | ✅ 实施 | 2026-05-17 | 同 W20 批次落地 |

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: 同 W20 周末闭合 reflect 批次（待 commit 后回填 hash）
- 实际 merged 日期：2026-05-17

### Tracking 数据

| 信号 | 基线 | 目标 | W20 末 | W21 | W22 |
|---|---|---|---|---|---|
| Phase 06 instance "启动/终态"关键字命中率 | 0%（模板没要求）| 100% | 模板已要求，待 W21 新实例验证 | 待填 | 待填 |
| 新 cycle commit 是 2 次 (kickoff + closure) 比例 | ~33%（项目 cycle 1 部分实现）| ≥ 95% | 待填 | 待填 | 待填 |

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0012 升格（实例已实现，本提案做模板回归对齐）|
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + 落地 Phase06-运营-Gate.md §H 拆 H.1/H.2 + §I 拆 I.1/I.2，状态 proposed → merged → tracking |

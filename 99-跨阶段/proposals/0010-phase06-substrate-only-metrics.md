# Proposal 0010: Phase 06 §B (5 指标 + 告警) 在 early/internal-tool 下允许 "substrate-only" 路径

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0010 |
| 标题 | Phase 06 监控看板 5 指标 + 告警在 early/internal-tool 阶段可用"替代方案表"替代正式看板 |
| 状态 | **merged → tracking** |
| 类型 | 流程 |
| 提出人 | Wjl + Claude（reflect/2026-W20）|
| 提出日期 | 2026-05-17 |
| 评审人 | 项目经理 + 技术 lead |
| 评审截止 | 2026-05-24 |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Phase 06-运营-Gate.md §B 当前规则:

> - [ ] 已在 [06-运营/数据看板（链接）](../../06-运营/数据看板（链接）/) 维护本模块看板链接
> - [ ] 关键指标至少 5 个（业务 / 性能 / 错误率 / 用户行为 / 容量），每个有量化阈值与告警
> - [ ] 告警接收人明确（写在 [Runbook.md](../../05-上线/Runbook.md)）

Project 模块 Phase 06 cycle 1 实例（[Phase06-运营-Gate-cycle1-2026-05-15.md](../gate-checklists/instances/project/Phase06-运营-Gate-cycle1-2026-05-15.md)）实际场景:
- **early 阶段没有生产部署，只有 dev**
- **没有任何正式监控基础设施**（Prometheus/Grafana/SLS/CloudWatch 都没接）
- 强行写"5 指标 + 告警接收人"等于伪造数据

实例只能在 §K 写 friction 1: "5 指标看板对 early/internal-tool 不适用"。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0010
- 关联 Gate 实例: [Phase06-运营-Gate-cycle1-2026-05-15.md](../gate-checklists/instances/project/Phase06-运营-Gate-cycle1-2026-05-15.md) §K friction 1
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) F-W20-03

---

## 3. 提案

### 改 `99-跨阶段/gate-checklists/Phase06-运营-Gate.md` §B

```diff
 ## B. 必产出物 — 监控与看板

-- [ ] 已在 [06-运营/数据看板（链接）](../../06-运营/数据看板（链接）/) 维护本模块看板链接
-- [ ] 关键指标至少 5 个（业务 / 性能 / 错误率 / 用户行为 / 容量），每个有量化阈值与告警
-- [ ] 告警接收人明确（写在 [Runbook.md](../../05-上线/Runbook.md)）
+- 按"项目成熟度"差异化:
+  - **`stable` / `mature`**:
+    - [ ] 已在 [06-运营/数据看板（链接）](../../06-运营/数据看板（链接）/) 维护本模块看板链接
+    - [ ] 关键指标至少 5 个（业务 / 性能 / 错误率 / 用户行为 / 容量），每个有量化阈值与告警
+    - [ ] 告警接收人明确（写在 [Runbook.md](../../05-上线/Runbook.md)）
+  - **`early`** (proposal 0010):
+    - [ ] 已在本 Gate 实例 §K 写"监控替代方案表"，至少含:
+      - 当前可用的 5 项观察手段（如：手动 curl healthcheck / `journalctl` / 后端日志文件 / 单元测试 / E2E 测试通过情况）
+      - 每项的"触发响应条件"（什么情况下应该停下来 / 该改进基础设施）
+      - 升级路径：转 `stable` 时本段失效，必须补齐正式看板（链 proposal 0010）
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 | early 阶段不必伪造监控 |
| 现有 instance | Project cycle 1 + cycle 2 共 2 份可标"溯及 proposal 0010" |
| 升级路径 | 自动捆绑"转 stable 必须升级"约束，避免一旦松绑永远松绑 |

---

## 5. 风险

- **风险 1**: 团队懒得维护"替代方案表"，把它当过场。**缓解**: §K 段当前已强制有 friction 描述，本段同样不允许空 — 评审人卡控。
- **风险 2**: `early → stable` 转型时遗漏升级。**缓解**: §C 项目状态字段是 stable 时，§B 模板自动校验是否有正式看板链接。

---

## 6. 备选方案

- **方案 A**: 取消 §B 5 指标硬约束 — 不选，丢掉 stable 阶段的可观测性约束。
- **方案 B**: 改 5 → 3 / 1 个最低门槛 — 不选，1 个有意义指标对 early 仍可能假数据。
- **方案 C（选定）**: 用"替代方案表"替代 — 强制写明可用手段 + 升级路径。

---

## 7. 实施计划

```
[x] Step 1: 写 proposal（本文件）
[x] Step 2: 评审 — 2026-05-17 [solo-review]
[x] Step 3: accepted → 落地 Phase06-运营-Gate.md §B（拆 B.standard / B.substrate-only）— 2026-05-17
[ ] Step 4: Project cycle 1/2 instance 在 §J 标"溯及 0010"（W21 跟进）
[ ] Step 5: tracking 期看 Project cycle 3 / Defect cycle 1 是否走通 substrate-only 路径
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 06 §J/§K "监控相关 friction" 次数 | 2（cycle 1 + cycle 2）| 0 |
| `early` 模块 §B.substrate-only 的"替代方案表" 完整度 | N/A | ≥ 5 项观察手段 |
| 转入 `stable` 时切换到 §B.standard 的命中率 | N/A | 100% |

Tracking 期: 2026-05-17 ~ 2026-05-31。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | early 阶段无监控基础设施是事实约束，"替代方案表"模式与 §B.standard 并列保留升级路径 |
| Claude | ✅ 实施 | 2026-05-17 | 同 W20 批次落地 |

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: 同 W20 周末闭合 reflect 批次（待 commit 后回填 hash）
- 实际 merged 日期：2026-05-17

### Tracking 数据

| 信号 | 基线 | 目标 | W20 末 | W21 | W22 |
|---|---|---|---|---|---|
| Phase 06 §J 监控 friction | 2 | 0 | 已修复（rule 改了，旧实例待溯及补注）| 待填 | 待填 |
| substrate-only 路径调用率（early 模块）| 0 | ≥ 1 | 0（下次 cycle 启用）| 待填 | 待填 |

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0010 升格 |
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + 落地 Phase06-运营-Gate.md §B 拆 B.standard / B.substrate-only，状态 proposed → merged → tracking |

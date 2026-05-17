# Proposal 11 段模板的速写指南

> [完整模板](../../../../99-跨阶段/proposals/0000-template.md) 是 SSoT. 这里给速写要点 — 每段最少必填什么。

---

## §元信息 (8 字段)

```yaml
编号: NNNN           # 按号段递增 (per decision-tree.md §A)
标题: <一句话>
状态: proposed       # 7 选 1: draft/proposed/accepted/implementing/merged/tracking/done/rejected/superseded
类型: 流程/编码规范/工具链/架构/实验  # 5 选 1
Bundle: candidates 00X+00Y+00Z (如适用)
提出人: Wjl + Claude  # solo 模式
提出日期: YYYY-MM-DD
Tracking 截止: YYYY-MM-DD  # merged 后 2-4 周
```

---

## §1 背景 — 1 段话

为什么要做这件事？描述现状的痛点。例:
> Phase 05 §H 当前规则 "solo=2 双签必须" 在 internal-tool + early + dev 三条件下成形式主义,Project 模块 7 份实例都通过 §K 豁免。

---

## §2 证据 — 必含 ≥ 1 类

- 关联 signals/YYYY-MM.md 候选 NNNN: ___
- 关联 reflect/YYYY-WW.md: ___
- 关联事故 / gotcha / 用户请求: ___

**禁止**: "感觉规范有点啰嗦" 类无证据型。

---

## §3 提案 — diff 草案精确到段号

```diff
+## §X.Y 标题
+
+具体规则内容...
+
+错误码 N0X 已在 PRD-MAPPING.md §4 登记 (如适用)
```

**写前校验** (per 0040 §3.1 + 0041 §3.1):
- [ ] 已 Read 目标文件
- [ ] 段号 / 字段名 逐字对齐
- [ ] Diff 精确可应用
- [ ] (代码层 proposal) 已 Grep 现存代码

---

## §4 影响范围 — 5 类受众

| 受众 | 影响 |
|---|---|
| 开发者 | |
| Claude | rules.md 变更 / 下次会话生效 |
| 测试 / 运维 | |
| 已有代码 / 文档 | 数量级 + migration 路径 |
| 现有 instances | 几份实例可标 "溯及" |

---

## §5 风险 — ≥ 1 条 + 缓解

- 风险: __; 缓解: __

---

## §6 备选方案 — 3 选 1

- 方案 A: ___ ; 不选原因: ___
- 方案 B: ___ ; 不选原因: ___
- 方案 C (选定): ___ ; 选定原因: ___

---

## §7 实施计划 — checkbox list

```
[x] Step 1: 写 proposal
[ ] Step 2: 评审 (solo: 同日 [solo-review] / small+: 按号段 SLA)
[ ] Step 3: accepted → apply diff (具体到文件)
[ ] Step 4: instances 补"溯及" (如适用) → 否则入 BL-YYYY-NNN
[ ] Step 5: tracking 期观察 signal
```

---

## §8 衡量指标 — baseline → target 表

| 信号 | 基线 | 目标 |
|---|---|---|
| | | |

Tracking 期: YYYY-MM-DD ~ YYYY-MM-DD.

---

## §9 评审记录 — solo 必填"为什么 solo 足够"

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | YYYY-MM-DD | <理由> |

> Solo 单签理由 (必填段): {1-3 句, 说明"为什么不需要更多评审人"}

---

## §10 实施后跟踪 — apply 后填

```
实际合入 commit: <hash>
实际 merged 日期: YYYY-MM-DD
派生迁移项 (如适用): BL-YYYY-NNN
```

Tracking 数据表 + 月度判决 (留 reflect-monthly 月底填)。

---

## §修订记录 — 每次变更都加 1 行

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| YYYY-MM-DD | Wjl + Claude | 首版从 signals 候选 NNNN 升格 |
| YYYY-MM-DD | Wjl + Claude | apply per 0040 §3.5 solo same-day. 状态 proposed → merged → tracking |

---

## 时间预算

- Lift only (Mode A): ~ 5 min (用 skill 自动化)
- Lift + Apply (Mode A+B): ~ 15-20 min (含 Read / Grep + diff)
- Bundle 升格 (N 候选 → 1 proposal): ~ 30 min (含决策)

solo 模式同日完成是基线。超过 1 小时仍在 propose 阶段 → 复杂度过高, 拆。

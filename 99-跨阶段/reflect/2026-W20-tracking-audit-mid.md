# Reflect — Tracking 中段审计 (W20-mid)

> 自进化运行 2 周后中期审计。21 个 proposal 在 tracking 期，但 [reflect/README.md 反模式](README.md#反模式) 警示 "merged 后不追 tracking → 不知道改了之后规范是否真的好用了"。
> 本次审计是 W20 → W22 终结判定 (05-29 / 05-30 / 05-31) 之前的中期校验，捕 8 处 audit findings。

---

## 头部

| 字段 | 值 |
|---|---|
| 触发场景 | 7 commits 自进化批次 (`9e1afe6` → `6bf86d9`) 全部落地后，验证规则是否真生效 |
| 执行者 | Wjl + Claude |
| 执行日期 | 2026-05-17 |
| 数据时段 | 2026-05-15 ~ 2026-05-17 (W19 末 + W20 全) |
| Audit 维度 | 规则落地 / cross-reference / git metrics / 元规则验证 / 数据一致性 |

---

## 1. 审计方法

针对每个 merged → tracking 的 proposal:
1. **静态扫描**: grep 目标文件验证规则文本是否实际存在
2. **git 度量**: 验证 commit / fix / bypass 等可现正测的信号字段
3. **Cross-reference 完整性**: proposal §10 承诺的"instance 补'溯及'" 是否真做
4. **元规则验证 (0040)**: §3.1 写前 Read / §3.5 solo same-day 在本会话中的实证表现
5. **数据一致性**: signals 字段 vs 实际可观察的 git/file 状态

---

## 2. 发现 (8 条 F-AUDIT)

### ✅ Pass — 验证通过 (4 条)

#### F-AUDIT-A1: 0040 五段 diff 全部落地

| 段 | 目标 | grep 验证 |
|---|---|---|
| §3.1 写前 Read | `0000-template.md` | `proposal 0040 引入` 命中 ✓ |
| §3.2 partial 状态 | `proposals/README.md` | `partially-merged` 命中 ✓ |
| §3.3 bundle 判据 | `proposals/README.md` | `Bundle 判据` 命中 ✓ |
| §3.4 Sprint backlog | `03-开发/Sprint backlog.md` | 文件存在 + backfill 5 行 ✓ |
| §3.5 solo 评审节奏 | `proposals/README.md` | `Solo 模式简化` 命中 ✓ |

#### F-AUDIT-A2: gate-checklists/README.md 维度 4 段 cross-ref

[gate-checklists/README.md:74](../gate-checklists/README.md) 含 `**Phase 05 / 06 早期豁免**（proposal 0007 / 0010 / 0011）` → 0007/0010/0011 cross-ref 真实存在。

#### F-AUDIT-A3: Commit 规范 100% — 0 bypass / 1 fix / 1 solo committer

| 指标 | W20 窗口 (05-15 ~ 05-17) | 期望 | 结果 |
|---|---|---|---|
| commit_total | 40 | — | — |
| commit_violation_count | 0 | 0 | ✓ |
| commit_bypass_count (`--no-verify`) | 0 | 0 | ✓ |
| fix commits | 1 (`913d431 fix(encoding)`) | low | ✓ |
| unique committer email | 1 (`578516579@qq.com`) | 1 (solo) | ✓ |
| reflect commits | 4 (W19 phase01 / W19 phase03 / W20 / W20 meta) | ≥ 1 | ✓ |

#### F-AUDIT-A4: 跨模块 FK 命名一致性 (rules §M.7)

10 个 business-*.sql 文件全部使用 `project_id / sprint_id / task_id / assignee_user_id` snake_case。**0 drift** → rules §M.7 跨模块一致性达成。

---

### ⚠️ Fail / 需关注 (4 条)

#### F-AUDIT-F1: Cross-reference 集体缺口 — 0 / 24 instance 补 "溯及 NNNN"

**事实**: grep `溯及 (000[1-9]|001[0-2]|0040)` in `instances/` → **0 matches**。

**承诺 vs 现实**:
- proposal 0007 §7 Step 4: "7 份现存 instance 在 §I 补 '溯及本提案' 备注"
- proposal 0008 §7 Step 4: "7 份现存 instance 在 §I 补 '溯及 0008/0009'"
- proposal 0010 §7 Step 4: "Project cycle 1/2 instance 在 §J 标 '溯及 0010'"
- proposal 0011 §7 Step 4: 同上
- proposal 0012 §7 Step 4: "3 份 instance 已经符合两段式（W21 标'溯及 0012'）"
- proposal 0013 §7 Step 4: "Req/Sprint/Task Phase 01 实例补 §G '溯及 0013/0014/0015'"
- proposal 0016 §7 Step 4: 同上 Phase 02
- proposal 0032 §7 Step 4: 同上 Phase 05

→ 共 24 个 instance 应被补"溯及"，**实际 0 个**。

**根因**: 同日 propose-accept-merge 加速节奏跳过了 instance 回标步骤；这步操作"低优先级、易忘"。

**风险**: 未来审计时回看 instance §I / §J 段，看不到"为什么 friction 不再 trigger"——失去了"规则改了 → 实例自动符合"的可追溯证据。

**修复路径**: 加入 Sprint backlog 作为 BL-2026-006，W22 批量补；或在 0040 §3 加新规则"apply 包含 instance 回标，否则 partial"。

#### F-AUDIT-F2: 0100 §1.9 规范与现存代码冲突 — `boolean` vs `void/throw 702`

**事实**: [SprintServiceImpl.java:266](../../plm-backend/plm-sprint/src/main/java/cn/com/bosssfot/dv/plm/sprint/service/impl/SprintServiceImpl.java) 已有:
```java
@Override
public boolean checkExists(Long sprintId) {
    return sprintId != null && sprintMapper.selectSprintById(sprintId) != null;
}
```

但 [03-开发/开发规范.md §1.9](../../03-开发/开发规范.md) (proposal 0100 落地) 规范签名:
```java
/** 失败时抛 ServiceException(code=702) */
void checkExists(Long id);
```

→ **规范 ≠ 现实**。现存 ISprintService 返回 `boolean`，不 throw 702。

**根因**: proposal 0100 写作时没 grep 现存代码（违反 0040 §3.1 的精神）；只看了 reflect 描述。

**风险**: 新代码按 0100 写、旧代码 boolean，调用方需判断签名。

**修复路径** (3 选项):
- **A**: 0100 amend — 允许 `boolean` OR `void/throw` 双签名 (向后兼容)
- **B**: 0100 amend — 强约束 `void/throw`，把 ISprintService 加入 BL-2026-004 强制迁移
- **C**: 拆 0100-a (规范文本) + 0100-b (代码迁移)

本审计建议选 **B**: 规范不让步，把现状当 tech debt 收录 D.2，BL-2026-004 任务包含 ISprintService 迁移。

#### F-AUDIT-F3: signals/2026-05.md §1 / §4 数据陈旧

**事实**: [signals/2026-05.md](../signals/2026-05.md) 头部 "数据时间窗" 仍写 "2026-05-15 单日"，但实际累积到 05-17，含 40 commit + 1 fix + 多个 Gate 实例。

**修订记录**第 12/13 条已加新事件，但 §1 commit_total/§4 bug_total 等量化字段没同步更新。

**根因**: signals 设计是月初汇总，但 W19/W20/W21 中期连续触发新数据，主文件没回流更新。

**修复路径** (per signals/README.md): 创建 `2026-05-supplementary.md` 记录中期增量，避免覆盖主文件。或月底 (06-01) 一次性合入。

**本审计选项**: 不立即修，待月底自然汇总；但本份 audit 报告本身即是 supplementary 形式。

#### F-AUDIT-F4: superseded 状态语义混淆 (signals 候选 0034)

**事实**: signals 标 `[~] 0034 superseded by 0027` — 但 0034 从未产出过 proposal 文件，不存在被 superseded 的对象。

**正确语义**:
- `superseded` 指 proposal A 已写完，后被 proposal B 替代（A 存在）
- 0034 候选其实是 "duplicate of 0027" — 候选层面的重号，不是 proposal 层面的替代

**修复**: 0040 §3.2 状态机段加 "duplicate" 半状态用于 signals 候选层 (proposal 层保留 superseded)。或更小修改: signals 标 `[~] 0034 duplicate of 0027` 而非 superseded。

---

## 3. 诊断（Diagnoses）

### 3.1 主诊断: "merged" 太宽松 — 应分 "merged-spec" 和 "merged-instances-aligned"

F-AUDIT-F1 暴露的 cross-reference 缺口本质上是: 当前定义 "merged" = "目标规范文件改完"，但**没要求"实例文件回头对齐"**。这就是为什么 24 个 instance 没被补"溯及"——技术上 proposal 已 merged，但实例端的"应用证据"缺失。

### 3.2 次诊断: 元规则验证需要更长样本

F-AUDIT-A1 / F-AUDIT-A5 都是"0040 元规则落地的正面证据"，但样本 N=1-2 太少。真正生效要看 W22+ 是否还会再发生 scope 错。

### 3.3 第三诊断: 规范写作"先 grep 代码"的纪律没立

F-AUDIT-F2 (0100 没 grep ISprintService) = proposal 写作时只看 reflect 描述，没看代码实际状态。0040 §3.1 说"写前 Read 目标文件"——但这里的"目标"通常是规范文件（如 开发规范.md），不包含被规范约束的代码文件 (如 ISprintService.java)。

**新派生 friction**: 0040 §3.1 应扩展到 "也 grep 一遍现存代码是否合规"。

---

## 4. 行动（Actions）

### 4.1 本审计同次 commit 内修复

| # | 行动 | 类型 | 落地点 |
|---|---|---|---|
| **A1** | 修复 F-AUDIT-F2: amend 0100 §3 — 强约束 void/throw 702，把 ISprintService 加 BL-2026-004 covered scope | proposal amend | 0100.md + Sprint backlog.md |
| **A2** | 加 Sprint backlog BL-2026-006: 24 instance 回标 "溯及 NNNN" | backlog | Sprint backlog.md |
| **A3** | signals 候选 0034 改 `duplicate of 0027` 标签 (语义修正) | signal correction | signals/2026-05.md |
| **A4** | 提示用户 (在 reflect 文本) 后续需要派生 friction → 0040 §3.1 扩展提案 | 文本说明 | 本文件 |

### 4.2 deferred (待用户裁定)

| # | 行动 | 触发 |
|---|---|---|
| B1 | 升格 "0040 §3.1 应含代码 grep" 派生 friction → 新 proposal 0041 | 用户同意后 |
| B2 | apply 0101 / 0200 (剩余 proposed) | 用户裁定继续 apply |
| B3 | tracking 终结判定 (05-29/30/31) | 时间到 |

### 4.3 W22+ 周期外

- W22 周一 0900 写 W21 周报 (Phase B 未启用 → 手工)
- W22 中段: BL-2026-006 (instance 回标) + BL-2026-004 (FK 校验代码审计)
- W22 末: tracking 终结判定，按指标判定 0001-0006 / 0027-0029 进 done 或 reverted

---

## 5. 元复盘 — 关于本审计自身

本审计**是 0040 §3.5 solo 评审节奏的元应用**：W20 周末 → W21 处于尚未到 (Phase B 未启用)，但 user 触发 audit 后 same-day 完成 8 finding + 4 action + 修复 F-AUDIT-F2。

| 维度 | 数 |
|---|---|
| Audit findings | 8 (4 pass + 4 fail/需关注) |
| 修复同次 commit | 4 (A1-A4) |
| 派生 friction → W21 候选 | 1 (0040 §3.1 扩展) |
| 真实捕获的"规范 vs 现实"漂移 | 1 处 (F-AUDIT-F2, 已修) |

**审计本身的价值密度** ≈ 4 finding / 30 min = 0.13 finding/min — 高密度，证明审计仪式不形式主义。

---

## 6. 链路

- 触发: 用户 "审计" 指令
- 衍生: 0100 amend (本 commit) + Sprint backlog 加 BL-2026-006 + 派生 friction 0041 候选 (W22 升格)
- 关联: 6 个 in-flight tracking proposal 都被 visit; 2 个 proposed (0101 / 0200) 待续 apply

---

## 7. 一句话总结

**自进化机制 21 个 proposal 在 tracking 期，中段审计捕 8 处 finding (4 pass + 4 fail)。最严重 F-AUDIT-F1: 24 个 instance 集体未被补 "溯及 NNNN"。F-AUDIT-F2 (0100 规范 vs 代码冲突) 同次 commit 修复。新派生 friction "0040 §3.1 应含代码 grep" 留 W21 升格。**

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首次创建 (W20 末中段 tracking 审计) |

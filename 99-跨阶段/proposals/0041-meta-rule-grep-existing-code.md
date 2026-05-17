# Proposal 0041: 扩展 0040 §3.1 — 写规范类 proposal 前还要 grep 被约束的现存代码

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0041 |
| 标题 | proposal 模板 §3 加第 4 项 checkbox：grep 现存代码合规性 |
| 状态 | **merged → tracking** (solo same-day per [proposal 0040](0040-self-evolution-v2-meta-rules.md) §3.5) |
| 类型 | 流程 (meta — proposal 机制本身的规则)；扩展 [0040](0040-self-evolution-v2-meta-rules.md) §3.1 |
| 提出人 | Wjl + Claude（reflect/2026-W20-tracking-audit-mid 派生）|
| 提出日期 | 2026-05-17 |
| Bundle | 单候选（不 bundle）|
| 评审 / Tracking | 2026-05-17（[solo-review] same-day per 0040 §3.5）; tracking 至 2026-05-31 |

---

## 1. 背景

[W20 tracking 中段审计](../reflect/2026-W20-tracking-audit-mid.md) F-AUDIT-F2 暴露：

- [proposal 0100](0100-fk-validation-via-service-checkexists.md) 落地 03-开发/开发规范.md §1.9 时规定 `void checkExists(Long) / throw 702`
- 但 grep 时发现 [`ISprintService.java`](../../plm-backend/plm-sprint/src/main/java/cn/com/bosssfot/dv/plm/sprint/service/ISprintService.java) 已有 `boolean checkExists(Long sprintId)` — 签名冲突
- 0100 写作时只 Read 了规范文件 (per 0040 §3.1)，**没 grep 被规范约束的现存代码** → 规范出炉即与现实脱节

0040 §3.1 当前只保护 "规范文件位置准确"，没保护 "规范与现存代码兼容"。0041 补这一缺口。

---

## 2. 证据

- 关联 reflect: [reflect/2026-W20-tracking-audit-mid.md](../reflect/2026-W20-tracking-audit-mid.md) F-AUDIT-F2 + §3.3 第三诊断
- 实际事件: 03-开发/开发规范.md §1.9 = void/throw 702 vs ISprintService.java = boolean
- 已修补: 03-开发/开发规范.md §1.9 加"现存代码兼容性"注 (后补) + BL-2026-004 scope 扩展含签名迁移
- 本提案目标: **防止未来再发生** 类似漂移

---

## 3. 提案

### 改 `99-跨阶段/proposals/0000-template.md` §3 — 加第 4 个 checkbox + 1 个说明段

```diff
 ### 写 proposal 前必填校验（proposal 0040 引入）

 - [ ] 已 `Read` 上表每个目标文件的**当前完整内容**
 - [ ] §3 写的"段号 / 字段名 / sub-section 编号"**逐字与目标文件当前版本一致**（不依赖记忆 / 不依赖 outdated 版本）
 - [ ] Diff 草案在目标文件实际段位置**精确可应用**（没有"先重排段再改"的隐含前提）
+- [ ] **若 proposal 约束代码层（开发规范 / rules.md / SQL / API 签名 等）**：已 `Grep` 被约束的**现存代码**，确认现状合规或评估迁移成本（per [proposal 0041](0041-meta-rule-grep-existing-code.md) — 扩展 0040 §3.1）
+  - 适用：proposal 改 `03-开发/开发规范.md` / `.claude/rules.md` §A-§I / `02-设计/API设计.md`
+  - 不适用：proposal 仅改 Gate Checklist 模板 / proposals 元规则（无对应代码层）
+  - Grep 后若发现现状不合规 → 在 §10 列**迁移项**（路径 / 工作量 / 责任人），同次 commit 派生 `BL-NNNN-XXX` 入 [Sprint backlog](../../03-开发/Sprint%20backlog.md)

 > 若 apply 时发现 scope 错位 → 本 proposal 在 §修订记录 写"scope 修正"，并把修正过程文档化（不许悄悄改 §3 抹掉痕迹）。
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 未来写规范/rules 类 proposal | 多 1 步 grep（实际 1-2 命令）|
| Claude 自动加载 rules.md | 不变 (本规则只触发于 propose 时刻) |
| 已经 merged 的 0100 | 不溯及；本提案防的是未来 |
| Sprint backlog | 若 grep 发现迁移需求，自动入 backlog (新组织模式) |

---

## 5. 风险

- **风险 1**: grep 范围模糊（grep 哪些代码？）。**缓解**: checkbox 内已写明适用范围 + 反范围。
- **风险 2**: grep 命中后写 backlog 增加工作量。**缓解**: 迁移成本透明 = 优势,不是劣势；让"规范容易写、迁移悄悄拖"反模式不再发生。

---

## 6. 备选方案

- A: amend 0040 §3.1 直接加 checkbox — 不选，0040 是历史记录,扩展用新 proposal 更可追溯
- B: 加到 .claude/rules.md K (自我更新段) — 不选，那段是 SHOULD 软规则,本提案是 MUST 工作流
- C (选定): 加到 0000-template.md §3 checkbox 同段 — 与 0040 §3.1 同位置可见

---

## 7. 实施计划

```
[x] Step 1: 写 proposal (本文件)
[x] Step 2: solo-review accept — 2026-05-17 (per 0040 §3.5)
[x] Step 3: 落地 — 同次 commit:
    - 99-跨阶段/proposals/0000-template.md §3 加 checkbox 4 + 说明段
[ ] Step 4: tracking 期看 W22+ 新 proposal 是否真做 grep 步骤
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| 规范类 proposal 写后规范-代码冲突率 | 1/2 = 50% (W21 0100 中招) | 0% (W22+) |
| 规范类 proposal 含"已 grep 现存代码"chk 项打勾的比例 | 0/21 (历史) → 1/21 (本提案) | 100% (W22+ 规范类) |
| grep 命中 → 派生 BL 入 backlog 的比例 | N/A | 100% (确认有迁移需求时) |

Tracking 期: 2026-05-17 ~ 2026-05-31.

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | 0040 §3.5 solo 节奏直接生效; 派生自 W20 audit F-AUDIT-F2 实际事故, 范围 tight (1 checkbox)，单 commit 落地 |
| Claude | ✅ 实施 | 2026-05-17 | 按 0040 §3.1 (本提案的母规则) 先 Read 目标 0000-template.md §3 — checkbox 段当前共 3 项,本扩展为第 4 项 |

> Solo 单签理由：F-AUDIT-F2 是已发生的 1 个数据点 + 系统性根因；扩展规则是"加 1 个 checkbox 这么小的调整"，不存在评审能找出新发现的可能。

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: 同次 commit (待 commit 后回填 hash)
- 实际 merged 日期：2026-05-17

### Tracking 数据

| 信号 | 基线 | 目标 | W21 | W22 | W23 |
|---|---|---|---|---|---|
| 规范类 proposal 写后规范-代码冲突率 | 50% (1/2) | 0% | 规则已建,待 W22+ 新规范类 proposal 验证 | 待填 | 待填 |
| 第 4 checkbox 打勾率 (规范类 proposal) | N/A | 100% | 本提案 §9 已自验 ✓ | 待填 | 待填 |

Tracking 期: 2026-05-17 ~ 2026-05-31。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版 + 同日 solo-review accept + apply (per 0040 §3.5); 落地 0000-template.md §3 加 checkbox 4 |

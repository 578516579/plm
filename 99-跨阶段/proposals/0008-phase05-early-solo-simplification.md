# Proposal 0008: Phase 05 §B.1 上线 Checklist + §C 凭据红线在 early+solo 下简化

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0008 |
| 标题 | Phase 05 §B.1 上线 Checklist 在 early+solo 可合并到发布计划；§C 凭据红线对 dev 环境标 N/A |
| 状态 | **merged → tracking** |
| 类型 | 流程 |
| 提出人 | Wjl + Claude（reflect/2026-W21 批量升格批次）|
| 提出日期 | 2026-05-17 |
| Bundle | 本提案合并 signals 候选 **0008 + 0009** |
| 评审截止 | 2026-05-31（2 周内）|
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Phase05-上线-Gate.md §B.1 + §C 当前都是"假设有生产环境"的硬约束：

- **§B.1 上线 Checklist** 要求复制 [05-上线/上线%20Checklist.md] 实例 + 全段打勾。但 `early + solo` 项目无独立 staging / 监控基础设施，每项都"打勾或 N/A"，最终实例 §I 异常段大段豁免。
- **§C 凭据与权限红线** 要求"生产 JWT_SECRET / DB_PASSWORD / Druid 已替换 / .env 未提交 / admin/admin123 修改"。但 `early` 阶段没有生产部署，5 条全部 N/A，模板没明示这种 dev-only 场景的合规路径。

Project Phase 05 实例（[Phase05-上线-Gate-2026-05-15.md](../gate-checklists/instances/project/Phase05-上线-Gate-2026-05-15.md)）+ 6 个子模块 Phase 05 实例（05-16）共 7 份实例 §I/§K 都受 friction。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0008 + 0009
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) B1 W21 批量升格清单
- 关联 Gate 实例: project Phase 05 + 6 子模块 Phase 05 共 7 份
- 已有先例: 同批 proposal 0007 也是 Phase 05 在 solo+early 三条件叠加豁免（语义同源）

---

## 3. 提案

### 3.1 改 `99-跨阶段/gate-checklists/Phase05-上线-Gate.md` §B.1（候选 0008）

```diff
 ### B.1 上线 Checklist

-- [ ] 文件：基于 [05-上线/上线 Checklist.md](../../05-上线/上线%20Checklist.md) 复制实例
-- [ ] **代码 & 构建** 全部打勾
-- [ ] **数据库** 全部打勾（DDL 已演练 / 备份完成 / 迁移在低峰）
-- [ ] **配置 & 凭据** 全部打勾（生产 JWT_SECRET / DB_PASSWORD / REDIS_PASSWORD / DRUID_PASSWORD 已更新；默认弱口令禁用）
-- [ ] **监控 & 告警** 全部打勾
-- [ ] **沟通** 全部打勾
+- 按 (`团队规模`, `项目成熟度`) 差异化：
+  - **`small+` 或 `stable+`**: 全部 6 项打勾（含复制独立 Checklist 实例）
+  - **`solo + early`** (proposal 0008): 可合并到 [05-上线/发布计划.md](../../05-上线/发布计划.md) 实例同一文件
+    - [ ] 发布计划文件含"上线 Checklist 6 段"小节
+    - [ ] 6 段中"代码 & 构建" / "数据库" / "沟通" 必填打勾；"配置 & 凭据" 走 §C 链路；"监控 & 告警" 走 Phase 06 §B 链路（substrate-only per proposal 0010）
```

### 3.2 改 `99-跨阶段/gate-checklists/Phase05-上线-Gate.md` §C（候选 0009）

```diff
 ## C. 凭据与权限红线（强制）

+**适用范围（proposal 0009）**：本节强制范围 = 发布目标环境 ∈ {`staging`, `prod`}。
+若 `发布目标=dev` 且 `项目成熟度=early`，本节整体标 N/A 并在 §I 写"已挂账 → 首次 staging/prod 部署 Gate"（创建一个引用本 §C 的延期 issue）。
+三条件失效（升 stable / 加 staging / 加 prod 部署）自动恢复强制。
+
 - [ ] 生产 `JWT_SECRET` 已替换为 ≥ 32 字符强随机值（`openssl rand -base64 48`）
 - [ ] 生产 DB 密码非默认 `please-change-me` / `password`
 - [ ] 生产 Druid 监控台已设强口令或仅内网可达
 - [ ] `.env` 文件未提交进 git（再 grep 一次 `git log -p | grep -E "JWT_SECRET|DB_PASSWORD"`）
 - [ ] 默认管理员 `admin/admin123` 已修改密码或禁用
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 现有 7 份 Phase 05 instances | 可在 §I 标"溯及 proposal 0008/0009" |
| solo + early 后续模块 Phase 05 | §B.1 可合并 / §C 可标 N/A |
| 升级到 stable 时 | §C "挂账"挂的延期 issue 在该次部署 Gate 必须 close |

---

## 5. 风险

- **风险 1**: §C 挂账后未跟进 → 转 stable 时漏。**缓解**: 挂账 issue 必须 link 到本 §C 实例 + Phase 05 instance §I。stable 转型 Gate 必填校验。
- **风险 2**: 简化变成"逐项 N/A" 不写理由。**缓解**: §C N/A 仍要求 §I 写"挂账"具体内容 + 时间预期。

---

## 6. 备选方案

- A: 删 §B.1 / §C 整段 — 不选，丢失生产部署约束
- B: 把所有约束改 SHOULD — 不选，扩散到 stable
- C（选定）: early + solo + dev 三条件叠加才豁免，与 0007 风格一致

---

## 7. 实施计划

```
[x] Step 1: 写 proposal（本文件）
[ ] Step 2: 评审 — 2026-05-31 之前
[ ] Step 3: accepted → 改 Phase05-上线-Gate.md §B.1 + §C
[ ] Step 4: 7 份现存 instance 在 §I 补"溯及 0008/0009"
[ ] Step 5: tracking 期看 W22 后新 Phase 05 实例是否还触发 §I friction
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 05 §I/§K "B.1 / §C 相关 friction" | 7 (W20 instances) | 0 |
| stable 转型挂账 issue 关闭率 | N/A | 100% (W22+ 首次 staging 部署时) |

Tracking 期: merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | 与 0007/0010/0011 同源风格——early+solo 三/二条件叠加豁免，与已有 4 维参数化完全对齐 |
| Claude | ✅ 实施 | 2026-05-17 | 同 W21 apply 批次落地 |

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: 同 W21 apply 批次（待 commit 后回填 hash）
- 实际 merged 日期：2026-05-17

### Tracking 数据

| 信号 | 基线 | 目标 | W20 末 | W21 | W22 |
|---|---|---|---|---|---|
| Phase 05 §I/§K "B.1/§C friction" | 7 (W20 instances) | 0 | rule 已改, 旧实例待溯及 | 待填 | 待填 |
| stable 转型挂账 issue 关闭率 | N/A | 100% | N/A (尚未首次 staging 部署)| 待填 | 待填 |

Tracking 期: 2026-05-17 ~ 2026-05-31。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0008+0009 bundle 升格 |
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + 落地 Phase05-上线-Gate.md §B.1 拆 B.1.standard/B.1.solo-early + §C 加适用范围段，状态 proposed → merged → tracking |

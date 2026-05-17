# Proposal 0016: Phase 02 设计模板债务总清理（6 项必填增强）

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0016 |
| 标题 | Phase 02 §B 设计文档加 6 项必填项 — 主键命名 / 反向边提示 / 并发选型 / 复合视图决策 / 父项目沿用 / 聚合详情决策 |
| 状态 | **merged → tracking** |
| 类型 | 流程 |
| 提出人 | Wjl + Claude（reflect/2026-W21 批量升格）|
| 提出日期 | 2026-05-17 |
| Bundle | 本提案合并 signals 候选 **0016 + 0017 + 0018 + 0019 + 0020 + 0021** |
| 评审截止 | 2026-05-31 |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

W20 周六 Req/Sprint/Task 三个子模块走 Phase 02 设计时，6 处设计文档"该填没填"的缺漏暴露：

| 候选 | 缺漏点 | 后果 |
|---|---|---|
| 0016 | §B.1 系统架构没"沿用父项目架构"许可路径 | 子实体 §G 写 friction |
| 0017 | §B.2 数据库设计没"主键命名规范"必填项 | Project 用 `id`，Req/Sprint/Task 用 `<table>_id`，命名 drift |
| 0018 | API §1 没"REST 资源 vs 复合视图（看板/聚合）"约束 | Task 看板接口设计反复 |
| 0019 | §B.3 状态机矩阵没"反向边 UI 提示"必填项 | Task 状态切换误操作隐患 |
| 0020 | API §1 没"聚合详情 vs 瘦详情"决策树 | Sprint 详情拖到 Phase 03 才决议 |
| 0021 | §B.2 没"并发处理选型"必填项 | Sprint @Version vs 悲观锁拖到 Phase 03 决议 |

每一处单独都不大，但 6 处叠加让 Phase 02 设计文档"看起来完整、实际有结构性 gap"，到 Phase 03 实现时频繁回头补 Phase 02。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0016-0021
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) F-W20-04 + B1 W21 批量
- 关联 Gate 实例: Req/Sprint/Task Phase 02 实例（3 份），§G 段共 6 处 friction

---

## 3. 提案

### 3.1 改 `99-跨阶段/gate-checklists/Phase02-设计-Gate.md` §B.1 — 加架构沿用许可（0016）

```diff
 ### B.1 系统架构图
 ...
+- 若 Phase 01 头部 "子实体模式 = 父实体子模块"（proposal 0013）：可标 "沿用父项目 [<父模块>] §B.1 系统架构"，但仍须补：
+  - [ ] 子实体在父架构中的位置图（一句话 + 简图）
+  - [ ] 子实体引入的新依赖（如有）
```

### 3.2 §B.2 数据库设计 — 加 2 项必填（0017 + 0021）

```diff
 ### B.2 数据库设计
 ...
 - [ ] DDL 草案已写（`<module>-DDL.sql`）
+- [ ] **主键命名规范明示**（proposal 0017）：选 `id` (BIGINT AUTO_INCREMENT) 或 `<table>_id` 之一，**全模块统一**；与父/兄弟模块不一致时在本节说明
+- [ ] **并发处理选型**（proposal 0021）：列举本模块所有可能并发更新的实体，逐个选 `@Version 乐观锁` / `SELECT FOR UPDATE 悲观锁` / `分布式锁` / `无需并发控制`，每项写理由
 - [ ] 索引设计完整（外键 / 唯一约束 / 查询热点字段）
```

### 3.3 §B.3 状态机矩阵 — 加反向边 UI 提示（0019）

```diff
 ### B.3 状态机矩阵
 ...
+- [ ] **反向边 UI 提示**（proposal 0019）：所有逆向状态切换（如已发布 → 草稿）必须列 "UI 二次确认文案" 或 "需要 reviewer 权限"，避免误操作
```

### 3.4 §C API 设计 — 加 2 决策树（0018 + 0020）

```diff
 ## C. API 设计
 ...
+### C.0 设计前置约束（proposal 0018 + 0020）
+
+- [ ] **REST 资源 vs 复合视图 决策树**（proposal 0018）：
+  - 默认 REST 资源 `/business/<entity>`
+  - 看板 / 聚合统计 / 跨实体的视图 → 用 `/business/<entity>/views/<view-name>` 命名（非 REST 资源），并在文档明示"非通用 CRUD"
+- [ ] **聚合详情 vs 瘦详情 决策树**（proposal 0020）：
+  - 详情接口默认"瘦"（仅本实体字段 + FK id）
+  - 列表场景包含子集合（如 Sprint 详情含子 Task list）→ 命名 `/business/<entity>/<id>/with-<sub>`，并写"为什么不在前端二次拉取"理由
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 设计师 / 开发者 | Phase 02 模板增 6 项必填，写 Phase 02 多 ~10-15 分钟 |
| 现有 Req/Sprint/Task Phase 02 实例 | 在 §G 标"溯及 0016-0021"；不强制补填（追溯成本太高） |
| 后续模块 Phase 02 | 6 项必填直接走，避免 Phase 03 回头修 |

---

## 5. 风险

- **风险 1**: 6 项必填让 Phase 02 模板"看起来很重"，劝退轻量级模块。**缓解**: 6 项每项都"先填一句、不行再 N/A"，不要求 essay。
- **风险 2**: "沿用父项目" 滥用 — 子实体不思考差异化。**缓解**: §B.1 仍要求"子实体位置图 + 新依赖" 必填。

---

## 6. 备选方案

- A: 6 项各自独立 proposal — 不选，文件爆炸（已经 14 个 deferred）
- B: 只取最重要 3 项（0017/0020/0021）— 不选，6 项都是同周期同源 friction
- C（选定）: 6 项 bundle 成 1 总提案，按 Phase 02 模板 §B.1/§B.2/§B.3/§C 4 段分组

---

## 7. 实施计划

```
[x] Step 1: 写 proposal
[ ] Step 2: 评审
[ ] Step 3: 改 Phase02-设计-Gate.md §B.1 / §B.2 / §B.3 / §C
[ ] Step 4: Req/Sprint/Task Phase 02 实例补 §G "溯及 0016-0021"
[ ] Step 5: tracking 期看 W22+ 新模块 Phase 02 → Phase 03 回头修次数
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 02 §G "设计盲区" friction | 6 (3 实例 × 2 平均) | ≤ 1 |
| Phase 03 提交中 "modify Phase 02 设计文档" commit 数 | ≥ 1（W20 周六多次）| 0 |

Tracking 期: merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | 6-候选合一最大单提案；scope 注：proposal §3.4 误写 "改 §C API"，实际 §B.3 是 API 设计（已修正 apply 路径）|
| Claude | ✅ 实施 | 2026-05-17 | 同 W21 apply 批次落地 |

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: 同 W21 apply 批次（待 commit 后回填 hash）
- 实际 merged 日期：2026-05-17
- 实际改动: Phase02-设计-Gate.md §B.1 (架构沿用) + §B.2 (主键命名 + 并发选型) + §B.3 (REST/聚合决策树) + §B.3.2 状态机端点 (反向边 UI 提示)。原 proposal 文本写 §C 是笔误，已按 §B.3 应用。

### Tracking 数据

| 信号 | 基线 | 目标 | W20 末 | W21 | W22 |
|---|---|---|---|---|---|
| Phase 02 §G "设计盲区 friction" | 6 (3 实例×2 平均) | ≤ 1 | 已改, 旧实例待 W21 标"溯及 0016" | 待填 | 待填 |
| Phase 03 "modify Phase 02 设计文档" commit 数 | ≥ 1 | 0 | 已改 (rule), 待 W21+ 新模块 Phase 02 验证 | 待填 | 待填 |

Tracking 期: 2026-05-17 ~ 2026-05-31。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0016-0021 bundle 升格（6 候选合 1 提案）|
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + 落地 Phase02-设计-Gate.md §B.1 + §B.2 + §B.3 + §B.3.2 状态机端点段。Scope 修正：原 proposal §3.4 写 "§C API" 应为 "§B.3 API"，按 §B.3 落地。状态 proposed → merged → tracking |

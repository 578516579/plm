# 升格 / 降级 / Bundle / 拆分 — 决策树

> 收到 signals 候选时, 用本树定 next action。每个分支末叶都明示"动作 + 文件路径"。

---

## 顶层: 这是不是规范变更?

```
候选要改的目标是 ___
│
├─ rules.md / 开发规范.md / Gate Checklist 模板 / 02-设计模板 / API 规范
│  → 升格为 proposal (走 §A 分支)
│
├─ 单代码模块的性能 / 重构 / 字典化 / hard-code 清除
│  → 降级到 Sprint backlog BL-YYYY-NNN (走 §B 分支)
│
├─ 已知会要还的工作量 (不影响业务确定性)
│  → 入 99-跨阶段/风险登记册.md §D.2 TD-YYYY-NNN (走 §C 分支)
│
├─ Hook / settings.json / skill / .claude/* 配置
│  → 升格 proposal (走 §A) 用 0200-0299 号段
│
└─ 文档 typo / 链接修复 / 字典数据维护
   → 不走自进化机制, 直接 commit `chore(docs): fix typo` (走 §D 分支)
```

---

## §A: 升格为 proposal — 选号段

```
新 proposal 的核心改动是 ___
│
├─ 流程 / Gate Checklist 模板 / 模块工作流
│  → 0001-0099
│
├─ 编码规范 (Service/Mapper/Domain 等约定)
│  → 0100-0199
│
├─ 工具链 (hook / 脚本 / settings.json / githooks)
│  → 0200-0299
│
├─ 架构决策 (跨多模块设计) / 技术债
│  → 0300-0399 + 必走 ADR 流程
│
└─ 实验性 (高风险, 高失败容忍, 不一定 merge)
   → 0900-0999
```

号段内编号: 取当前段最大 + 1。

---

## §A.1: 单候选 vs Bundle 多候选

```
你手头要升格的候选 ___ 个
│
├─ 单个 (N = 1)
│  → 不 bundle, 单 proposal
│
└─ 多个 (N ≥ 2)
   │
   按 [proposal 0040 §3.3](../../../../99-跨阶段/proposals/0040-self-evolution-v2-meta-rules.md) bundle 判据:
   │
   ├─ 满足任一: 同目标文件 / 同语义簇 / 同评审人
   │  └─ → bundle 为 1 个 proposal, §元信息 加 "Bundle: 候选 NNNN+NNNN+..." 字段
   │
   └─ 违反任一: 跨号段 / 评审节奏不同 / 实施 PR 不能合并
      └─ → 拆为 N 个独立 proposal
```

例:
- 0008+0009 ✓ bundle (同语义 "Phase 05 early+solo")
- 0013+0014+0015 ✓ bundle (同目标 Phase 01 模板)
- 0022+0025 ✗ 拆 (前者 0100 编码段, 后者 0101 安全段, 跨号段)

---

## §B: 降级到 Sprint backlog — BL 字段填写

```
BL-YYYY-NNN
│
├─ 优先级
│  ├─ P0: 影响当前 release (罕见, 应 hotfix 不应 backlog)
│  ├─ P1: 下个 Sprint 必做
│  ├─ P2: 季度内
│  └─ P3: 有空再做 (可能 retire)
│
├─ 工作量
│  ├─ S: < 半天
│  ├─ M: 半天 ~ 2 天
│  ├─ L: 2 ~ 5 天
│  └─ XL: > 5 天 (XL 多半应该拆)
│
└─ 字段必填:
   - ID: BL-YYYY-NNN
   - 来源: 链 candidate / proposal / reflect
   - 标题: 1 句具体的可执行动作
   - 预期 Sprint / 负责人 (可 TBD)
   - 备注
```

文件: [03-开发/Sprint backlog.md](../../../../03-开发/Sprint%20backlog.md) §待处理 加新行。

---

## §C: 加入风险登记册 D.2 已知技术债

```
TD-YYYY-NNN
│
├─ 字段必填:
│  - ID
│  - 技术债项 (1 句话, 说明"什么没做")
│  - 来源 (commit / proposal / reflect)
│  - 影响域 (后端 / 前端 / 部署 / 自进化机制)
│  - 预期偿还 Sprint (可 TBD)
│  - 关联 backlog (如有 BL 联动)
```

与 BL 区别:
- BL = "待执行的工作单元", 清晰可上 sprint
- TD = "已知会要还的整体债务", 描述性, 用于风险沟通

---

## §D: 直接 commit — 不走自进化机制

适用范围:
- 错别字 / 链接 404 / Markdown 渲染错
- 字典数据 / 配置数据 (不涉及结构)
- 临时 demo / 探索代码

直接 `chore(docs):` / `chore(config):` / `chore(experiment):` commit。**不入** signals 候选, **不入** reflect, **不入** proposal。

---

## 反模式 (决策树容易走错的)

| 走错 | 现象 | 应该走 |
|---|---|---|
| 把性能优化升 proposal | 0023/0024/0026 早期错走法 | →§B BL-YYYY-NNN |
| 把规范改动直接 commit | 想"小改不值得 proposal" | →§A 升 proposal (0040 §L.2 强约束) |
| 5 个候选硬塞 1 proposal | bundle 边界飘移 | →§A.1 拆 (违反同号段判据) |
| 把字典 typo 入 backlog | backlog 沦为啥都塞 | →§D 直接 commit |
| 紧急事故先 silent merge 想"后补 proposal" | 永远没补 | 0027/0028/0029 退路: 同会话 retroactive proposal (User-requested-bypass) |

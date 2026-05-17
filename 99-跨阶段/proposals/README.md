# Proposals — 流程改进提案

自进化机制的**输出端**。把 [reflect](../reflect/) 出来的"建议"转化为可执行的小 PR：改规范、改 hook、改 Gate Checklist 等。

> **核心理念**：规范本身是"代码"，演进路径必须像代码一样走"提案 → 评审 → 合入 → 跟踪效果"。

---

## 一份 proposal 的生命周期

```
draft       由 /reflect 自动产出 OR 人工提
   ↓
proposed    完成填写、关联数据，等 review
   ↓
   ├─→  accepted     评审通过，进入 implementing
   │       ↓
   │    implementing  Claude / 人 在写规范变更 PR
   │       ↓
   │    merged        PR 已 merge，规范升级生效
   │       ↓
   │    tracking      2-4 周观察期，看相关 signals
   │       ↓
   │       ├─→ done         指标好转，提案归档
   │       └─→ reverted     指标无改善 / 恶化 → 走回滚 + 写"失败提案"备忘
   │
   ├─→  rejected    评审不通过，留作记录（rejected 不删，保留学习价值）
   └─→  superseded  被更新的提案替代（指向新提案）
```

### "partially-merged" 状态（proposal 0040 引入）

当 proposal 含 N 个改动点，只有 K < N 落地时：

- **首选策略（拆）**: 把 proposal 拆为 N 个子 proposal（编号 `NNNN-a` / `NNNN-b` ...），已落地的 sub-proposal 走 merged 路径，未落地的留 proposed
- **次选策略（标 partial）**: 整个 proposal 标 `merged → tracking (partial)`，在 §10 实施跟踪段明确列：
  - 已落地子项清单
  - 延后子项 + 截止日期 + 负责人
- **拆/标 选择标准**:
  - 子项 ≥ 3 且各自需独立 review 周期 → **拆**
  - 子项 ≤ 2 或同源同次评审 → **标 partial**
- partial 状态在本表 [状态索引](#状态索引手动维护) 显示为 `merged → tracking (partial)`

---

## 文件命名

`NNNN-<标题简写>.md`，编号递增。例如：

- `0001-pre-push-build-check.md`
- `0002-relax-L3-coverage-threshold.md`
- `0003-add-jdk17-jenv-check-to-hook.md`

号段建议（避免编号冲突）：
- `0001-0099`：流程 / Gate Checklist 类
- `0100-0199`：编码规范 / 代码风格类
- `0200-0299`：工具链 / hook 类
- `0300-0399`：架构 / 技术债类
- `0900-0999`：实验性提案（高失败容忍）

### Bundle 判据（proposal 0040 引入）

多个 signals 候选可以 bundle 成 1 个 proposal 文件，**前提满足任一**：

- **同目标文件**: 全部候选改的是同一个目标规范文件的连续段（例: 0016-0021 全在 Phase 02 §B）
- **同语义簇**: 候选有同一根因（例: 0008+0009 都是 "Phase 05 early+solo 简化"）
- **同评审人**: 所有候选评审人完全一致

**禁止 bundle 的情况**：

- 跨号段范围（0001-0099 流程 vs 0100+ 编码 vs 0200+ 工具链）→ **拆**
- 评审节奏不同（流程类 1 周 vs 编码规范 3 天）→ **拆**
- 实施 PR 不能合并到同次 commit → **拆**

Bundle 后的 proposal 在 §元信息 加 "Bundle" 字段，列出合并的 signals 候选号。

---

## 状态索引（手动维护）

> **本表是所有 proposal 的元数据快照**。新增/状态变更时同步更新。

| 编号 | 标题 | 状态 | 提出 | 关联触发 | merged commit | tracking 期 |
|---|---|---|---|---|---|---|
| [0001](0001-internal-tool-track.md) | 引入"项目类型"维度（外部产品/内部工具/框架升级） | **merged → tracking** | 2026-05-15 | [reflect/2026-W20-project-phase01-dogfood](../reflect/2026-W20-project-phase01-dogfood.md) F5 | apply 0001/0002/0003（2026-05-15）| 2026-05-15 → 05-29 |
| [0002](0002-team-size-adjusted-thresholds.md) | 按"团队规模"自动调整 Gate 阈值 | **merged → tracking** | 2026-05-15 | reflect F1/F2/F3/F4 | 同上 | 同上 |
| [0003](0003-require-triage-rationale.md) | Gate 实例头部"分级理由"必填 | **merged → tracking** | 2026-05-15 | reflect F10 | 同上 | 同上 |
| [0004](0004-staged-test-dod.md) | 拆 Phase 03 / 04 的 DoD（代码骨架 vs 测试稳定） | **merged → tracking** | 2026-05-15 | [reflect/2026-W20-project-phase03-dogfood](../reflect/2026-W20-project-phase03-dogfood.md) F-P03-01 | apply 0004/0005/0006（2026-05-15）| 2026-05-15 → 05-29 |
| [0005](0005-solo-sprint-merge.md) | solo 模式 Sprint 文档可并入 Gate 实例 | **merged → tracking** | 2026-05-15 | reflect F-P03-02 | 同上 | 同上 |
| [0006](0006-project-maturity-stage.md) | 引入"项目成熟度"维度（early/stable/mature），4 维参数化 | **merged → tracking** | 2026-05-15 | reflect F-P03-03 | 同上 | 同上 |
| [0007](0007-solo-early-dev-self-review.md) | Phase 05 §H 双人签字在 solo+internal-tool+early 允许 self-review | **merged → tracking** | 2026-05-17 | [reflect/2026-W20](../reflect/2026-W20.md) F-W20-03 / signals 0007 / 风险 R-001 | apply 0007/0010/0011/0012（2026-05-17）| 2026-05-17 → 05-31 |
| [0010](0010-phase06-substrate-only-metrics.md) | Phase 06 §B 5 指标在 early/internal-tool 允许"替代方案表" | **merged → tracking** | 2026-05-17 | reflect F-W20-03 / signals 0010 | 同上 | 同上 |
| [0011](0011-phase06-okr-optional-in-early.md) | Phase 06 §G OKR 对照在 solo+early 可标 N/A | **merged → tracking** | 2026-05-17 | reflect F-W20-03 / signals 0011 / phase01-dogfood F7 | 同上 | 同上 |
| [0012](0012-phase06-two-stage-signoff.md) | Phase 06 cycle 拆"启动 (day 0) + 终态 (day N)"两段式签字 | **merged → tracking** | 2026-05-17 | reflect F-W20-03 / signals 0012 / cycle 1 实例已实现 | 同上 | 同上 |
| [0027](0027-business-module-scaffold-script.md) | 业务模块生成器 `new-business-module.sh` | **merged → tracking** (retro, User-requested-bypass) | 2026-05-17 追溯 | reflect F-W20-02 / signals 0027+0034 | `75b3233` (2026-05-16) | 2026-05-17 → 05-30 |
| [0028](0028-encoding-runtime-hardrules.md) | rules.md §D #5-7 编码硬规则 + check-encoding 脚本 | **merged → tracking** (retro, User-requested-bypass) | 2026-05-17 追溯 | reflect F-W20-02 / signals 0028 / 周六编码事故 | `913d431` (2026-05-16) | 2026-05-17 → 05-30 |
| [0029](0029-curl-data-binary-cn-body.md) | curl 中文 body 强制 `--data-binary @file` | **merged → tracking** (retro, User-requested-bypass) | 2026-05-17 追溯 | reflect F-W20-02 / signals 0029 / 同 0028 事故 | `913d431` (2026-05-16) | 2026-05-17 → 05-30 |
| [0008](0008-phase05-early-solo-simplification.md) | Phase 05 §B.1 上线 Checklist / §C 凭据红线在 early+solo 简化 | **merged → tracking** | 2026-05-17 | reflect W21 批次 / signals bundle 0008+0009 | apply W21 process batch（2026-05-17）| 2026-05-17 → 05-31 |
| [0013](0013-phase01-sub-entity-template.md) | Phase 01 加子实体扩展模式 + 立项时序字段 + 风险/技术债拆表 | **merged → tracking** | 2026-05-17 | reflect W21 批次 / signals bundle 0013+0014+0015 | apply W21 process batch + §D partial 续（2026-05-17）| 2026-05-17 → 05-31 |
| [0016](0016-phase02-design-template-debt.md) | Phase 02 §B 设计文档加 6 项必填（架构沿用/主键/反向边/并发/复合视图/聚合）| **merged → tracking** | 2026-05-17 | reflect W21 批次 / signals bundle 0016-0021 | 同上 | 同上 |
| [0032](0032-early-deploy-ergonomics.md) | early 部署链路 — 目标环境字段 + 演练路径 + deploy.sh | **merged → tracking** (partial; deploy.sh 实现延后) | 2026-05-17 | reflect W21 批次 / signals bundle 0032+0033 | 同上 | 同上 |
| [0100](0100-fk-validation-via-service-checkexists.md) | FK 跨表校验走 `Service.checkExists()`，禁 Mapper 直读 | **merged → tracking** | 2026-05-17 | reflect W21 批次 / signals 0022 | apply 0100 编码规范类首落地（2026-05-17 `[solo-review]`）| 2026-05-17 → 05-31 |
| [0101](0101-mr-url-host-whitelist.md) | 业务 URL 字段 host 白名单校验（防钓鱼）| **proposed** | 2026-05-17 | reflect W21 批次 / signals 0025 | — | （待 merged，独立 apply）|
| [0200](0200-encoding-pretooluse-hook.md) | 编码自检脚本接入 git pre-commit + Claude PreToolUse hook | **proposed** | 2026-05-17 | reflect W21 批次 / signals 0030（派生自 0028）| — | （待 merged，需改 settings.json/githooks，独立 apply）|
| [0040](0040-self-evolution-v2-meta-rules.md) | self-evolution v2 元规则：写前 Read / partial 状态 / bundle 判据 / Sprint backlog 通道 / solo 评审节奏 | **merged → tracking** | 2026-05-17 | [reflect/2026-W20-self-evolution-process-meta](../reflect/2026-W20-self-evolution-process-meta.md) F-META-01~05 (5-friction bundle) | apply 0040 元规则升级（2026-05-17 `[solo-review]`）| 2026-05-17 → 05-31 |

### 候选堆积处置回顾（W21 批量升格已清空）

> W20 周末闭合反思识别 28 候选；W21 批次后**0 个候选 deferred**。

| 候选号 | 处置 | 去向 |
|---|---|---|
| 0007 / 0010 / 0011 / 0012 | ✅ W20 批次同日升格 + apply | proposals 0007/0010/0011/0012 (merged → tracking) |
| 0027 / 0028 / 0029 | ✅ W20 批次追溯补录 | proposals 0027/0028/0029 (merged → tracking, retro) |
| 0008 / 0009 | ✅ W21 批次 bundle 升格 | [0008](0008-phase05-early-solo-simplification.md) (proposed) |
| 0013 / 0014 / 0015 | ✅ W21 批次 bundle 升格 | [0013](0013-phase01-sub-entity-template.md) (proposed) |
| 0016 ~ 0021 | ✅ W21 批次 6-候选合一 | [0016](0016-phase02-design-template-debt.md) (proposed) |
| 0022 | ✅ W21 批次升格（编码规范类首）| [0100](0100-fk-validation-via-service-checkexists.md) (proposed) |
| 0023 / 0024 / 0026 | ➡ 降级为 Sprint backlog code TODO | 不走 proposal 流程（性能/重构纯代码改造）|
| 0025 | ✅ W21 批次升格（编码规范-安全类）| [0101](0101-mr-url-host-whitelist.md) (proposed) |
| 0030（派生）| ✅ W21 批次升格（工具链类首）| [0200](0200-encoding-pretooluse-hook.md) (proposed) |
| 0032 / 0033 | ✅ W21 批次 bundle 升格 | [0032](0032-early-deploy-ergonomics.md) (proposed) |
| 0034 | ✅ superseded by 0027 | 重复编号 |

**总账**：W19-W21 共产出 **20 个正式 proposal** (6 W19 dogfood-direct + 7 W20 + 7 W21)；27 个候选**全部处置完毕** (14 个 lift → 14 proposal 文件 [部分 bundle] + 3 个降级 Sprint TODO + 1 个 superseded + 9 个跨批次 1-to-1 lift)；**0 deferred**。下次"候选堆积"计数重置，由 W21+ 新的 Gate 实例 friction 重新触发。

> **回顾意义**：自进化机制经 2 周连续运行，处理速率 (lift+下沉+合并) ≈ 27/2 ≈ 13.5/周，可持续支撑 dogfood 速率 (W20 含 25+ Gate 实例)。

---

## 触发来源（一份 proposal 不能凭空写）

每份 proposal 必须有"证据"。允许的触发来源：

| 来源 | 例子 | 证据格式 |
|---|---|---|
| signals 数据 | "上月 commit_bypass_count = 5" | 链 signals/YYYY-MM.md |
| reflect 报告建议 | "周报点出 Phase 03 平均超时 30%" | 链 reflect/YYYY-WW.md |
| 真实事故 | "线上 P0 故障，根因是规范盲区" | 链事故复盘文件 |
| gotcha 频次 | "同一坑 3 次会话踩到" | 链 gotchas.md 段落 |
| 用户明确请求 | "我们想要 XX" | 标"User-requested"，简单写背景即可 |

**禁止**："感觉规范有点啰嗦" / "我觉得 X 应该改 Y" — 没有数据/事故支撑的提案直接 rejected。

---

## 评审节奏

| Proposal 类型 | 评审人 | 评审耗时上限 |
|---|---|---|
| 流程 / Gate 类（0001-0099） | 项目经理 + 技术 lead | 1 周内 |
| 编码规范类（0100-0199） | 后端 lead + 前端 lead | 3 个工作日 |
| 工具链类（0200-0299） | DevOps + 提出方 | 2 个工作日 |
| 架构类（0300-0399） | 技术 lead + 必要 ADR | 2 周内 |

超出上限 → 自动升到下次"流程 Sprint"必须决议。

### Solo 模式简化（proposal 0040 引入）

当团队规模 = `solo` 时，按 Phase 05 / 06 §I `[solo-review]` 同款节奏：

- 评审耗时上限 → **0 天（当日 OK）**
- 评审人 = 提出方自评（commit message 必须带 `[solo-review]` 标签）
- 同日 propose-accept-merge **不算违规**，但同次（或下一次）commit 必须含：
  1. **proposal 文件**（status 从 proposed → merged → tracking，§修订记录 体现双状态）
  2. **实际规范文件 diff**（templates / rules.md / .claude/settings.json / 等）
- 转入 `small+` 团队后**自动恢复**多人评审节奏（上表）；本豁免不溯及历史 solo 单签的 proposal

> 这条规则解决了 W19/W20/W21 期间评审 SLA 100% 失效的形式主义问题。
> 反过来：solo 单签 proposal 必须在评审记录段说明"为什么 solo 单签足够"——可以一句话，但不能空。

---

## 模板

复制 [0000-template.md](0000-template.md) 起步。

---

## 反模式

- ❌ proposal 写得很长但没有"数据支撑"段
- ❌ accepted 后没人写 PR、长期挂在 implementing
- ❌ merged 后没追 tracking → 不知道改了之后规范是否真的好用了
- ❌ 用 proposal 当吵架工具（"我早就说过 X 不行"）→ 评审会要拍板，不是辩论
- ❌ rejected 后偷偷删文件 → 必须保留 rejected 提案作为学习材料

---

## 跟其他文档的关系

| 文档 | 关系 |
|---|---|
| [signals/](../signals/) | proposal 的"输入证据" |
| [reflect/](../reflect/) | proposal 的"种子建议来源" |
| [03-开发/ADR/](../../03-开发/ADR/) | 架构类 proposal 的下游产物（accepted → 写 ADR） |
| [开发规范.md](../../03-开发/开发规范.md) / [模块工作流.md](../模块工作流.md) / [.claude/rules.md](../../.claude/rules.md) | proposal 的"修改对象" |

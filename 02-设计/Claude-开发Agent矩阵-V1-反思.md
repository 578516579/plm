# V1 Agent 矩阵 dogfood 反思 → V2 改进建议

> 状态: V1 落地后立即 dogfood (2026-05-19)
> 方法: 用 `meta-cognitive` Agent 反思 `Claude-开发Agent矩阵.md` 自身
> 触发: 用户「继续」后,选 dogfood 验证

---

## 1. 反思方法论

按 meta-cognitive Agent 的 5 步:
1. ✅ **回溯会话** — V1 主文档 + 20 subagent
2. ✅ **分类动作** — 看每个 Agent 在本会话被实际"触发"了多少次
3. ✅ **抽共性** — 哪些动作模式重复 3+ 次
4. ✅ **错误模式** — 已有 troubleshooter 表
5. ✅ **协作链路** — 已有主文档链路图

加上一个新维度:**实际触发频率** vs **设计预期频率**。

---

## 2. 实际触发频率分析

回溯本会话(PR #11 6 个 commit),数每个 V1 Agent 的真实触发次数:

| Agent | 触发次数 | 设计预期 | 偏差 |
|---|---|---|---|
| requirement-clarifier | 6 (AskUserQuestion 6 次) | 高 | ✅ 准 |
| scope-decider | 3 (P0/P1/P2 决策) | 中 | ✅ 准 |
| progress-narrator | 8 (每次完工汇总) | 高 | ✅ 准 |
| system-architect | 3 (V1/V2/V3 各 1) | 中 | ✅ 准 |
| api-contract-keeper | 1 (前后端字段对齐) | 中 | ⚠ 低估 |
| technical-writer | 4 (V2 设计 + V3 设计 + 生产指南 + 本矩阵) | 中 | ✅ 准 |
| backend-coder | ~30 (新建 + 改造) | 高 | ✅ 准 |
| frontend-coder | ~10 (Agent view + 审计 view + API client) | 中 | ✅ 准 |
| db-modeler | 6 (DDL + 字典 + 迁移 + seed 3 次) | 中 | ✅ 准 |
| config-engineer | 3 (yml + .env + 设计) | 低 | ✅ 准 |
| bulk-refactor | 1 (13 模块改造) | 低 | ✅ 准 |
| test-engineer | 1 (24 单元测试一次写完) | 中 | ⚠ 低估 |
| e2e-validator | ~8 (反复跑 E2E) | 高 | ✅ 准 |
| security-reviewer | 0 (没主动跑) | 中 | ❌ **未触发** |
| environment-setup | 2 (D:/tmp setx + .m2 验证) | 低 | ✅ 准 |
| build-deployer | ~12 (反复 build + 启停) | 高 | ✅ 准 |
| troubleshooter | 5 (大问题:vite stale / schema / JVM stale / seed / dict dup) | 中 | ⚠ 低估 |
| git-workflow | 6 (6 个 commit + PR + branch) | 高 | ✅ 准 |
| task-tracker | ~15 (TodoWrite 每阶段) | 高 | ✅ 准 |
| meta-cognitive | 2 (本次 + 32 Agent 设计) | 低 | ✅ 准 |

### 🔴 发现 1: security-reviewer 设计了但 0 触发

V1 触发条件写"涉及 api-key / 密码 / 敏感数据 / 权限时使用"。本会话有大量涉及 api-key 的改动(4 Provider + ai/health 端点),但我**没主动调用 security-reviewer**。

**根因**: V1 没把 security-reviewer 集成到 git-workflow 标准流程。

**V2 修复**: git-workflow Agent 标准流程加一步「commit 前调 security-reviewer」。

---

## 3. Agent 设计的 5 类问题

### 🟡 问题 1: scope-decider 与 system-architect 边界模糊

V1 中 scope-decider 关注「做哪些 P0/P1/P2」,system-architect 关注「如何抽象」。但实际场景:
- 「V2 多 Provider 怎么做」既是范围(支持几个厂商)也是抽象(SPI vs 多个独立 service)
- 我自己写 V1 时也分不清是哪个 agent 在做

**V2 建议**:
- scope-decider 收窄为「在已知方案下做 prio 切分」
- system-architect 扩大为「方案设计 + 抽象层 + tradeoff」
- 二者顺序明确:**先 architect 出方案,再 decider 切 prio**

### 🟡 问题 2: db-modeler 内部职责过多

V1 db-modeler 包含 6 类任务:
- DDL 设计
- 字典化
- 索引规划
- 迁移脚本(幂等)
- seed 数据
- 字典 dedupe

后 3 类(迁移/seed/dedupe)更偏运维,本会话也是切 branch 后才大量触发。

**V2 建议**: 拆出 `db-ops` Agent:
- db-modeler — 设计期(DDL / 字典 / 索引)
- db-ops — 运维期(应用 sql / dedupe / restore / 数据修复)

### 🟡 问题 3: api-contract-keeper 触发条件太窄

V1 写「前后端跨界 / 多协议归一化」。实际本会话还有:
- Java domain 与 DB column 不一致(Mapper XML resultMap 漏字段)
- 业务模块 getter 名差异(`getDoraNo` vs 误用 `getMetricNo`)

**V2 修复**: 扩大触发条件到「任意 2 层之间的命名/字段契约」:
- 前端 interface ↔ 后端 domain
- 后端 domain ↔ DB column
- DTO ↔ Domain
- 业务 entity getter ↔ 业务调用方

### 🔴 问题 4: 缺失 prompt-engineer Agent

V3 中 13 个业务模块每个都写了 system prompt(「你是 PLM 资深需求分析师」「你是 PLM PRD 资深产品经理」...)。这些 prompt:
- 同质化(都是「你是 PLM 资深 XX」开头)
- 没有针对模型 / 任务 / 场景的特化
- 没有 A/B 验证

**V2 建议**: 加 `prompt-engineer` Agent:
- 触发:写新 AI 调用 / 优化现有 prompt
- 工作流:模板 → 特化 → 真厂商小流量 A/B → 落库 prompt 模板

### 🔴 问题 5: 缺失 context-memory Agent

本会话多次提到的"项目 quirks":
- backend 启动慢(48s / 130s)
- Redis 不能用 localhost(IPv6)
- vite import.meta.glob 静态扫描
- C 盘满需要 D:/tmp
- mojibake 不是真乱码
- mysql 字符集要 --default-character-set=utf8mb4

V1 把这些散在 CLAUDE.md 和 troubleshooter agent 里。**没有专门 Agent 维护这套知识**。

**V2 建议**: 加 `context-memory` Agent:
- 触发:发现新 quirk / 用户跨会话来"接着上次"
- 维护单一 source of truth:`memory/project-quirks.md`
- 与 CLAUDE.md 协同(CLAUDE.md 是给所有 Claude 的 instruction,project-quirks 是知识库)

---

## 4. 协作链路问题

### 🟡 链路缺口: security-reviewer 未自动接入 git-workflow

V1 协作链路图里 security-reviewer 是孤立的,实际应该:

```
backend-coder / frontend-coder / config-engineer 改完
   ↓
test-engineer + e2e-validator
   ↓
[新增] security-reviewer ← 必经,扫描 api-key / SQL 注入 / 权限缺失
   ↓
git-workflow commit
```

### 🟡 链路缺口: e2e-validator → troubleshooter 闭环

V1 写了"e2e-validator 失败时 → troubleshooter",但没写**troubleshooter 修完返回 e2e-validator**。本会话发生 3 次「e2e fail → 修 → 重跑 → 还 fail → 再修」的循环。

**V2 建议**: 链路图加双向边 + 计数(限 3 次循环,超过则升级问 user)。

---

## 5. V2 矩阵 (24 个 Agent)

V1 → V2 变化:

| 操作 | V1 | V2 | 理由 |
|---|---|---|---|
| 保留 | 20 个 | 20 个 | 大部分准确 |
| **拆分** | db-modeler | db-modeler + **db-ops** 🆕 | 设计 vs 运维分离 |
| **新增** | - | **prompt-engineer** 🆕 | AI prompt 优化盲区 |
| **新增** | - | **context-memory** 🆕 | 项目 quirks 单一来源 |
| **新增** | - | **flow-orchestrator** 🆕 | 协调多 Agent 链式调用,避免人脑串 |
| **调整边界** | scope-decider | scope-decider(收窄) | 仅做 prio 切分 |
| **调整边界** | system-architect | system-architect(扩大) | 含方案设计 |
| **调整边界** | api-contract-keeper | api-contract-keeper(扩大) | 任意 2 层契约 |
| **加入工作流** | security-reviewer | security-reviewer(必经) | git-workflow 前置 |

**V2 总数: 24 个**(V1 的 20 + 4 个新增/拆分)。

---

## 6. 错误模式表更新

V1 错误模式表新增 3 条:

| 触发信号 | 介入 Agent | 修复路径 |
|---|---|---|
| commit 前未审 api-key | git-workflow + **security-reviewer** | 改 git-workflow 流程必经 security |
| e2e fail → 修 → 再 fail 循环 3+ 次 | **scope-decider** | 升级:回退改动 / 问 user |
| AI 输出质量差 / 模板雷同 | **prompt-engineer** 🆕 | 特化 prompt + A/B 真厂商 |

---

## 7. dogfood 验证结论

### ✅ V1 整体准确

20 个 Agent 中 17 个触发频率 ≈ 设计预期。这表明抽象基本反映真实工作流。

### ⚠ V1 缺口

1. **security-reviewer 设计了但没自动接入** → 修协作链路
2. **api-contract-keeper 触发条件太窄** → 扩大职责
3. **db-modeler 职责过载** → 拆 db-ops

### 🔴 V1 完全漏掉

1. **prompt-engineer** — AI 时代的核心能力
2. **context-memory** — 跨会话知识
3. **flow-orchestrator** — 多 Agent 协调(目前靠人脑或我自己串)

### 📊 信号量

- 设计 → 落地 (V1 矩阵)耗时:约 1 小时
- dogfood 反思耗时:约 15 分钟
- 发现 8 个改进点,3 个关键缺口
- **dogfood ROI**: 高(单位时间产出比设计阶段更有信号)

---

## 8. V2 落地建议

V1 已合并到 PR #11。V2 不必立刻全做:

- 🔴 **P0**:加 `context-memory` Agent + 把 security-reviewer 接入 git-workflow
- 🟡 **P1**:加 `prompt-engineer` Agent + 拆 db-ops
- 🟢 **P2**:加 `flow-orchestrator` + 调整 scope-decider / system-architect 边界

V2 建议作为独立 PR(避免污染 PR #11)。

---

## 9. 元-元观察(meta-meta-cognitive)

`meta-cognitive` Agent 本身这次的表现:

✅ **工作流程严谨** — 回溯 → 分类 → 抽共性 → 错误模式 → 协作链路 5 步都走了
✅ **基于证据,不空想** — 每个发现都有"本会话 X 次触发"或"V1 链路图 Y 节点"作支撑
✅ **承认局限** — 列出"V1 缺口"和"V1 完全漏掉",不护短
⚠ **可能盲点** — 还没用 V1 跑过完整的开发循环(只是这个 PR),数据样本小

**给自己的 V2 注记**: meta-cognitive Agent 的输出周期不应 < 一个完整 PR(否则数据太少)。

---

## 变更记录

| 日期 | 版本 | 变更 |
|---|---|---|
| 2026-05-19 | V1.0 | 首次 dogfood 反思,出 V2 改进建议 |

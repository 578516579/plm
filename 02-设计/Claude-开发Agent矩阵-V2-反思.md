# V2 Agent 矩阵 dogfood 反思 → V3 改进建议

> 状态: V2 实战 4 次后 dogfood (2026-05-19)
> 方法: `meta-cognitive` Agent 第 2 轮自反思
> 数据样本: V2 后 4 个 commits (75f11ba / 37f0b2c / 6148789 / 231b32c)
> 上版: [V1 反思](Claude-开发Agent矩阵-V1-反思.md)
>
> ⚠ **V3 不直接落地**,留作 user 决策。本文仅产出"如果做 V3 应该改什么"。

---

## 1. V2 实战 4 次回顾

| # | Commit | 主题 | 用到的 V2 Agents | 工时 |
|---|---|---|---|---|
| 1 | 75f11ba | promote quirks 到 CLAUDE.md | context-memory + git-workflow + security-reviewer | ~15 min |
| 2 | 37f0b2c | V4 Phase 1 SPI | system-architect + backend-coder + test-engineer + e2e-validator + build-deployer + git-workflow | ~45 min |
| 3 | 6148789 | V4 Phase 3 Controller + 前端 | system-architect(调整) + backend-coder + frontend-coder + api-contract-keeper + build-deployer + e2e-validator + git-workflow | ~1.5h |
| 4 | 231b32c | V4 Phase 4 审计字段 | db-modeler + db-ops + backend-coder + api-contract-keeper + test-engineer + e2e-validator + technical-writer + git-workflow | ~30 min |

**总计**:4 次实战,~3 小时,12 个不同 Agent 触发(部分多次)。

---

## 2. V2 触发频率分析(基于 4 次实战)

| Agent | V2 触发次数 | V1 设计预期 | 偏差 |
|---|---|---|---|
| git-workflow | 4 (每次 commit) | 高 | ✅ 准 |
| build-deployer | 4+ (每次 build/启停) | 高 | ✅ 准 |
| e2e-validator | 4 (每次 commit 前) | 高 | ✅ 准 |
| security-reviewer | 4 (V2 流程必经) | 中 | ⚠ 形式触发,实质 0 发现 |
| backend-coder | 3 | 高 | ✅ 准 |
| **context-memory** | 1 | 中 | ✅ 价值高 |
| **db-modeler** | 1 | 中 | ✅ 准 |
| **db-ops** | 1 | 中 | ✅ 准(职责拆分有效) |
| system-architect | 2 | 中 | ⚠ 草案与落地偏离 |
| api-contract-keeper | 2 | 中 | ✅ 扩大职责有效 |
| frontend-coder | 1 | 中 | ✅ 准 |
| test-engineer | 2 | 中 | ✅ 准 |
| technical-writer | 1 | 低 | ✅ 准 |
| task-tracker | 持续 | 持续 | ✅ 准 |
| meta-cognitive | 1 (本次) | 低 | ✅ 准 |

### 🟡 发现 1: security-reviewer 形式触发,实质 0 发现

V2 必经流程要求每次 commit 前调 security-reviewer。4 次都跑了,但**4 次结果都是"无问题"**:
- 75f11ba 纯文档 → 无密钥
- 37f0b2c plm-common 流式 SPI → 无密钥
- 6148789 Controller + frontend → 无密钥
- 231b32c DB 字段 + Mapper → 无密钥/SQL 注入

✅ 好处:形成"过流程"肌肉记忆,真涉密时不漏
⚠ 副作用:每次"无问题"的预审增加工时成本(每次 ~30s),信噪比低

**V3 建议**:加触发条件细化
- 只在涉及 `application.yml` / `.env*` / `*Properties.java` / api 调用相关代码时**必经**
- 纯文档 / 纯单测 / 纯 mapper XML(参数化绑定)→ **跳过**(默认信任)

### 🟡 发现 2: V4 草案与落地架构偏离

system-architect dogfood (commit 545ff2f) 设计了 `Flux<AiChatChunk>`,但 Phase 1 实际改用 `Iterator<AiChatChunk>`。

✅ 偏离合理 — V4 Phase 1 落地时发现 Flux 引入 WebFlux 依赖冲突
⚠ 但草案文档没及时同步,reviewer 看草案与代码不一致会困惑

**V3 建议**:system-architect Agent 模板加"§13 落地校准"
- 落地 commit 后必须更新草案
- 用对比表说明"草案 vs 实际"差异
- 加 `落地状态: 草案 / 部分落地 / 已落地` 字段

### 🟡 发现 3: bulk-refactor 只触发 1 次

V2 4 次实战 0 次触发 bulk-refactor(V1 在 V3 13 模块改造时用过 1 次)。

✅ 不一定要合并 — 工具型 Agent 不在乎频率,在乎正确触发时刻有用
⚠ 但描述要强调"什么时候触发",避免错过批量场景

**V3 建议**:bulk-refactor 触发条件更严格
- 当前:"N 个模块同模板化改动"
- V3 加"如果 grep 出 ≥3 个文件需要同模式改动,**优先**用 bulk-refactor SOP"

### 🟢 发现 4: db-modeler ↔ db-ops 拆分有效

V2 Phase 4 完美演绎:
- db-modeler 设计 migration sql(纯文件改动)
- db-ops 应用到 DB(调 mysql 命令)
- 两步分离,各自责任清晰

如果合并,我可能在"刚写完 sql"就立即 mysql 执行 → 没机会审查 sql。拆分强制"先 review 再 apply"。

✅ V1 反思决策正确。

### 🔴 发现 5: 缺失的 prompt-engineer 仍然缺失

V4 Phase 3 涉及"Mock 流式输出格式"(`[mock] system="..." user="..."` 按空格分 token)。这种 mock prompt 设计**没有专门 Agent**审查。

V3 13 模块改造时的 13 个 system prompt(都是"你是 PLM 资深 XXX")也没人审查同质化问题。

V1 反思已发现,V2 没补,V3 应该补:
- prompt-engineer Agent 设计 system prompt 模板
- 模板特化:provider / task / 角色不同 → prompt 差异化
- 真厂商接入后 A/B 测对比

**V3 建议**:**P0 加 prompt-engineer**(从 V1 P1 升 P0)

### 🟢 发现 6: meta-cognitive 实战 ROI 高

V1 反思 15 min 发现 8 改进点 + 3 缺失;V2 反思 (本次) 20 min 发现 6 个 observation + 5 个 V3 决策点。

dogfood 是高 ROI 活动,**应该制度化**:
- 每个 PR 闭环前自动跑一次 meta-cognitive
- 不必等积累 PR,小批量也能反思

**V3 建议**:
- task-tracker 在 `mark_chapter` 大节点时自动触发 meta-cognitive(轻量,~5 min)
- 或:git-workflow 在准备 merge PR 前自动跑

---

## 3. V2 → V3 改进矩阵

### V3 P0(高价值,V1 已识别)

| 操作 | 内容 |
|---|---|
| **新增 prompt-engineer** | AI prompt 设计 / A/B 优化 / 模板库 |
| **调整 security-reviewer 触发** | 涉密文件必经;纯文档/单测跳过 |

### V3 P1

| 操作 | 内容 |
|---|---|
| **system-architect 模板加 §13** | 落地后回头校准,草案与实际同步 |
| **bulk-refactor 触发条件强化** | grep ≥3 文件同模式 → 优先触发 |

### V3 P2

| 操作 | 内容 |
|---|---|
| **新增 flow-orchestrator** | 多 Agent 协调 DAG(V2 实战靠 Claude 串,但开始累) |
| **meta-cognitive 制度化触发** | mark_chapter 大节点自动反思 |

### V3 不动

- db-modeler / db-ops 拆分 — 实战验证有效
- context-memory — 1 次实战价值已显现,继续累积
- api-contract-keeper 扩大 — Phase 4 用上了
- git-workflow 必经 security + e2e — 总体值得保留(虽然 security 太宽)

---

## 4. V2 实战中真实协作链路

观察:V2 设计的协作链路图相对线性,但**实际工作经常并行 / 跳步**。

### 实战链路(V4 Phase 4 真实流程)

```
user 「继续」
   ↓
task-tracker (TodoWrite 6 项)
   ↓
db-modeler (写 migration sql + 全量 DDL 同步) ──┐
   ↓                                          │
db-ops (mysql 应用 + verify SHOW COLUMNS) ────┘ 并行/紧接
   ↓
backend-coder (AiChatResult 加字段 + AiServiceImpl Iterator 包装)
   ↓ 同时
api-contract-keeper (Mapper XML 加映射 + insert + summary)
   ↓
test-engineer (1 个新单测)
   ↓
build-deployer (kill backend + mvn install + 单测 + 启动)
   ↓
e2e-validator (跑 120/120,1 flake retry 过)
   ↓
technical-writer (V4 草案文档同步实际落地)
   ↓
security-reviewer (预审 — 无问题)
   ↓
git-workflow (commit + push)
   ↓
progress-narrator (本次完工汇总)
```

### 发现:并行机会被低估

V2 的协作链路图画成"线性 ↓",但实战:
- db-modeler 写 sql 时,backend-coder 可以并行准备 domain getter
- frontend-coder 写 view 时,backend-coder 可以并行 Controller

**V3 建议**:协作链路图加并行虚线,标 ⊕ 表示可并行节点。

---

## 5. ROI 数据更新

| 阶段 | 用时 | 产出 |
|---|---|---|
| V1 设计 + 落地 | ~60 min | 20 Agent + 主文档 + 2 skill |
| V1 dogfood (反思 1) | ~15 min | 8 改进点 + 3 缺失 Agent + V4 草案 |
| V2 P0 落地 | ~45 min | +2 Agent, 4 改进, memory/ |
| V2 实战 1 (promote quirks) | ~15 min | CLAUDE.md gotchas 4→6 |
| V2 实战 2 (V4 Phase 1) | ~45 min | AiChatChunk + chatStream + 5 单测 |
| V2 实战 3 (V4 Phase 3) | ~1.5h | SseEmitter + 前端 fetch SSE + 流式 UI |
| V2 实战 4 (V4 Phase 4) | ~30 min | streaming + first_token_ms 字段 + 1 单测 |
| **V2 dogfood (反思 2,本次)** | **~20 min** | **5 observation + V3 P0/P1/P2 决策** |

**V2 实战阶段总耗时**: ~3 小时;**meta-cognitive 反思 2 次合计**: 35 min(其中 dogfood 占工时的 ~10%)。

dogfood 与实战的比例约 1:5 — 合理。如果反思 > 30% 实战就过度优化了。

---

## 6. V2 信号指标

### 正信号 ✅

- E2E 0 退步(4 次 commit 都 120/120 ALL GREEN)
- 单测 24 → 29 → 30 单调递增,无回归
- security-reviewer 形成肌肉记忆,流程 0 违规
- db-modeler/db-ops 拆分有效,迁移幂等 + dedupe 都用上了
- context-memory 1 次 promote 即让 CLAUDE.md gotchas 4→6
- Iterator vs Flux 决策被记录在 V4 文档 §10.5 + memory/project-quirks.md

### 负信号 ⚠

- security-reviewer 形式触发(每次预审 30s,实质 0 发现)
- V4 草案 ↔ 实际架构偏离(Flux → Iterator),文档延迟同步
- prompt-engineer 缺口仍未填补(V1 反思就发现,V2 没动)
- 协作链路图低估并行机会

### 中性观察

- bulk-refactor 频率低,但工具型 Agent 无需高频
- meta-cognitive 频率低,但每次 ROI 高

---

## 7. V3 决策点(留 user 拍板)

1. **V3 是否立刻做,还是等更多数据?**
   - 立刻做:5 改进点都有 V2 实战证据
   - 等数据:V2 仅 4 次实战,样本量不大;再积累几个 PR 后做 V3 可能发现更多

2. **prompt-engineer 优先级 P0 还是 P1?**
   - P0(本反思建议):AI 时代核心,V3 13 模块的 prompt 同质化已暴露
   - P1:真厂商接入(V4 Phase 2)前再做,样本更丰富

3. **security-reviewer 触发条件调整方式**
   - A 选项:加"涉密文件白名单",白名单内必经,其他跳过
   - B 选项:保持必经,接受 30s/次 形式成本
   - C 选项:改为"按文件类型自动判断"(.java + .yml + .env* 必经)

4. **flow-orchestrator 需不需要做**
   - 需要:V2 实战靠 Claude 自己串 Agent,开始觉得累
   - 不需要:线性串 Agent 也够用,加 DAG 反而增加复杂度

5. **meta-cognitive 制度化时机**
   - A:每个 PR 闭环前自动跑
   - B:每个 mark_chapter 大节点跑
   - C:保持手动触发(user 主动要求才反思)

---

## 8. 元-元-元观察

V1 dogfood 用了 meta-cognitive,产出 V2;
V2 dogfood 用了 meta-cognitive,产出 V3 建议;
**会不会出现 V3 dogfood 用了 meta-cognitive,产出 V4?**

按 V1 反思 §9 的"不递归 3 层"原则:
- 第 1 层 meta-cognitive:反思**工作过程** → 抽 Agent
- 第 2 层 meta-cognitive:反思**Agent 矩阵** → 调整 Agent
- 第 3 层 meta-cognitive:反思**反思方法本身** → 通常不必要

但 V2 → V3 没到第 3 层,**只是不同时间点的第 2 层**(随着实战数据增长重做)。

合理的反思频率:
- 每个完整 PR(8-12 commits)后做一次第 2 层反思
- 每年 1-2 次"反思反思方法"的第 3 层(可选)

**V3 建议**:反思周期定为"每个 PR 闭环 + 季度回顾"。

---

## 9. 变更记录

| 日期 | 版本 | 变更 |
|---|---|---|
| 2026-05-19 | V2-反思 V1.0 | V2 4 次实战后 dogfood 第二轮 |

## 10. 关联文档

- [V1 主文档](Claude-开发Agent矩阵.md) — 20 + V2 增量 2 = 22 Agent
- [V1 反思](Claude-开发Agent矩阵-V1-反思.md) — 第一轮 dogfood
- 本文(V2 反思)— 第二轮 dogfood
- 未来 V3 矩阵 — 如果 user 决定做,可基于本文 §3 落地

## 11. V3 决策待定

V3 P0/P1/P2 都已设计完,**留 user 拍板**:
- 选项 A:全部 V3 落地(独立 PR,工时 ~1h)
- 选项 B:只 V3 P0 (prompt-engineer + security-reviewer 调整,工时 ~30min)
- 选项 C:不做 V3,继续用 V2 累积数据
- 选项 D:做但放慢节奏,1 个 Agent 一个 PR

未决定前,V2 继续可用,不影响现有 22 Agent 工作。

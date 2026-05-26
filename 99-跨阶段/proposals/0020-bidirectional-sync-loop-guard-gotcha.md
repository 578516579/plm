# Proposal 0020: 双向同步回环防护沉淀为 gotcha + 扩 §L.1 收"正确范式"

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0020 |
| 标题 | 双向同步回环防护清单沉淀 + gotcha 触发条件扩"主动设计的正确范式" |
| 状态 | **draft** |
| 类型 | 架构 / 技术债(号段 0300-0399)|
| 提出人 | Claude(meta-cognitive / reflect 2026-W22-zentao-integration)+ Wjl |
| 提出日期 | 2026-05-27 |
| 评审人 | Wjl(solo-review) |
| 评审日期 | 待 |
| Tracking 截止 | 2026-06-24 |

---

## 1. 背景(What's the problem?)

双向同步天然有死循环风险(`A 改 → 同步 B → B webhook → 又改 A`)。禅道集成代码做对了**三道防线**,但这份"该领域第一类知识"只活在代码注释 + 1 行 proposal 风险描述里。下一个做双向集成的人(Jira)从零再想,可能漏掉某道防线。根因更深:gotcha 库目前**只收"踩过的坑"**(事后),没有抽屉装"主动设计出的通用正确范式"(事前)——这类知识掉进了"gotcha(只收坑)"和"专用设计文档(只管禅道)"之间的真空带。

---

## 2. 证据(Evidence)

- [reflect/2026-W22-zentao-integration.md](../reflect/2026-W22-zentao-integration.md) **模式 2** + F2 + O10。三道防线实证(代码 Read):
  1. `SyncContext`(ThreadLocal `inbound` 标志):入站置位,出站 `@TransactionalEventListener` 检测到直接 return;`SyncContextTest` 专测"ThreadLocal 不跨线程泄漏";
  2. `recentSyncCache`(ConcurrentHashMap,key=`{type}-{entityId}`,TTL 60s)防抖;
  3. last-write-wins(`checkStaleAndUpdate` 用 `SELECT ... FOR UPDATE` 锁行 + 时间戳比对)。
- gotcha 触发条件过窄:[.claude/rules.md §L.1](../../.claude/rules.md) 现写"用户纠错 / 踩到未知坑 / 同类问题第 2 次出现"——**全是事后语义**。
- 关联坑:reflect O9(唯一索引 NULL 不参与约束,设计期自纠)也属"集成类通用知识",同样无处安放。

---

## 3. 提案(What's the change?)

### 改动文件清单

| 文件 | 改动 | SSoT? |
|---|---|---|
| `memory/project-quirks.md` | 新增"集成类通用防护"段 + `Q-INTEG-01` | 否(memory)|
| `.claude/rules.md §L.1` | gotcha 触发表加一行"主动设计的通用正确范式" | **是(§L.2 受管)→ 需授权** |

### Diff 1:project-quirks.md 新增 Q-INTEG-01(草案)

```
## 集成类通用防护(主动设计范式, 非踩坑)

### Q-INTEG-01 — 双向同步回环防护三道防线
| 场景 | A 改 → 同步 B → B webhook → 又改 A 死循环 |
| 防线1 | SyncContext(ThreadLocal inbound 标志):入站置位, 出站 EventListener 见标志 return |
| 防线2 | recentSyncCache(key={type}-{entityId}, TTL 60s)防抖 |
| 防线3 | last-write-wins:SELECT ... FOR UPDATE 锁行 + 外部 updateTime 比本地旧则跳过 |
| 配套 | 幂等键 external_source+external_id 唯一索引(NULL 不参与约束); webhook 用 externalEventId 去重 |
| 出处 | 禅道集成 SyncContext/ZentaoOutboundSyncService; SyncContextTest |
| 适用 | 任何双向集成(Jira/...)从本清单起步, 不要从零想 |
```

### Diff 2:.claude/rules.md §L.1 gotcha 触发条件加一行(草案)

```diff
 | **新 gotcha** | 用户纠错 / 踩到未知坑 / 同类问题第 2 次出现 | gotchas.md |
+| **新正确范式** | 主动设计出"横跨多场景的通用正确做法"(如防回环/幂等/分布式锁范式),且预判会被复用 | gotchas.md / project-quirks.md "通用防护"段 |
```

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| Claude | 做下个双向集成时先查 Q-INTEG-01;回合末沉淀时,"主动设计的正确范式"也纳入 gotcha 评估(不只收坑)|
| 开发者 | 集成防回环有清单可循,review 时对照 |
| proposal 0019(集成 skill)| 其 `bidirectional-sync.md` reference 直接引用本 Q-INTEG-01 作为唯一来源 |

---

## 5. 风险(Risks)

- **R1 — gotcha 库膨胀**:把"正确范式"也塞进来可能稀释"坑"的信噪比。缓解:单列"集成类通用防护"段,与"踩坑"段物理分开;只收"预判会复用"的范式,不收一次性技巧。
- **R2 — §L.1 是 SSoT,改动需授权**:本提案 Diff 2 触及 [.claude/rules.md](../../.claude/rules.md),按 §L.2 必须 review 通过 + 用户授权方可改。Diff 1(project-quirks)不受限,可先落。

---

## 6. 备选方案(Alternatives Considered)

- **A 写进禅道专用设计文档**:已经在那(部分),但"专用"=下个集成看不到。**不够**。
- **B 只写 ADR**:ADR 记"为什么这么设计",但"防回环清单"是操作性 checklist,更适合 gotcha/quirks。**互补,不替代**(ADR-0008 事件总线另立,见 reflect B6)。
- **C 并入 0019 skill 不单独立 proposal**:可行,但"扩 §L.1 收正确范式"是独立的元规则改进,值得单独评审。**故拆出**。

---

## 7. 实施计划(Implementation Plan)

```
[ ] Step 1: solo-review approve
[ ] Step 2: 改 memory/project-quirks.md 加"集成类通用防护"段 + Q-INTEG-01(不需 SSoT 授权, 可先落)
[ ] Step 3: §L.1 改动 → 用户 AskUserQuestion 明确授权后才改 .claude/rules.md(§L.2 / §O.2)
[ ] Step 4: 0019 skill 的 bidirectional-sync.md reference 引用 Q-INTEG-01
[ ] Step 5: tracking 4 周(下个双向集成是否复用清单)
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| 双向集成防回环防线遗漏数 | n/a(无清单)| 0(下个集成对照 Q-INTEG-01)|
| gotcha 库"正确范式"类条目 | 0 | ≥1(Q-INTEG-01)|
| §L.1 能否收"事前范式" | 否 | 是 |

跟踪期:2026-05-27 ~ 2026-06-24。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | 待 | | solo-review;Diff 2 涉 §L.1 需额外授权 |

---

## 10. 实施后跟踪(merged 后填)

- 合入 commit:待(依 0021)
- 最终判定:[ ] done / [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude(meta-cognitive)/ Wjl | 初稿 V1.0,从 reflect/2026-W22-zentao-integration 模式 2 / B5 派生 |

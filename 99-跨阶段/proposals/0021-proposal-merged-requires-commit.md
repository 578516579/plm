# Proposal 0021: proposal 状态 `merged` 必须绑定真实 merged commit hash

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0021 |
| 标题 | proposal `merged` 必须绑定真实 commit;无 commit 只能停 `accepted`/`implementing` |
| 状态 | **merged → tracking** (solo-review) |
| 类型 | 流程(号段 0001-0099) |
| 提出人 | Claude(meta-cognitive / reflect 2026-W22-zentao-integration)+ Wjl |
| 提出日期 | 2026-05-27 |
| 评审人 | Wjl(solo-review) |
| 评审日期 | 待 |
| Tracking 截止 | 2026-06-24(merged 后 4 周) |

---

## 1. 背景(What's the problem?)

proposal 生命周期把 `merged` 当作**决议状态字**(用户拍板"要做"),而非**git 事实**(代码已进主干)。结果出现"纸面 merged":proposal 标 `merged → tracking`,但 merged commit "待填"、代码全在 working tree、tracking 期对着一个没合入的东西空转。`User-requested-bypass` 加剧此问题——用户说"做",proposal 立刻跳 `merged`,但"做"和"做完并提交"被混为一谈。

---

## 2. 证据(Evidence)

- **首例 = proposal 0014(禅道双向同步)**:[reflect/2026-W22-zentao-integration.md](../reflect/2026-W22-zentao-integration.md) 模式 3 实锤:
  - README 状态索引 0014 行 = `merged → tracking (User-requested-bypass)`,但 **merged commit = "待填"**;
  - 0014 §10 "实际 PR = 待开";§7 Step 8/9(编译验证 + 真实联调)`[ ]` 未做;
  - 0014 §7 勾了"4 份测试 `[x]`",**实际测试目录只有 2 个文件**(SyncContextTest + ZentaoFieldMapperTest);
  - 13 个集成文件 + 5 个 event 基础设施 + 4 个 ServiceImpl 改动**全部 untracked**(本会话 2026-05-27 起始 git status `??`/`M` 段证实)。
- **双重失真**:(a)README 让人误以为"禅道已上线、在观测期";(b)0014 §8 定的"入站成功率 ≥95%" tracking 指标对象根本没 merge。
- **关联**:reflect 主线 [2026-W22-modules-bulk-uplift.md](../reflect/2026-W22-modules-bulk-uplift.md) Pattern 4(未提交雪球)——本提案是它在 **proposal 状态机层面的恶化表征**。

---

## 3. 提案(What's the change?)

### 改动文件清单

| 文件 | 改动 |
|---|---|
| `99-跨阶段/proposals/0000-template.md` | §元信息 + §10 加"merged commit hash 必填"约束语 |
| `99-跨阶段/proposals/README.md` | 生命周期图 + 状态索引表头加注:`merged` = 存在 merged commit 的 git 事实 |

### 规则(文字约束,非脚本)

1. **`merged` 的定义**:proposal 状态置 `merged` 的**充要条件**是"存在至少 1 个已落 git 的 commit hash 实现了本提案"。README 状态索引该行 `merged commit` 列**不允许**填"待填/待开/TBD"。
2. **没有 commit 时的合法状态**:
   - 已评审通过、还没写代码 → `accepted`
   - 正在写代码、尚未 commit → `implementing`
   - **决议要做但代码在 working tree 未提交 → 仍是 `implementing`,不是 `merged`**
3. **`User-requested-bypass` 的边界**:bypass 的是"proposal 评审流程"(用户直接拍板做),**不是** bypass "merged 需要 commit"这一事实约束。User-requested-bypass 的 proposal 同样要等代码落 git 才能标 `merged`。
4. **回填动作**:存量违规的 0014 必须回填——要么补 commit 后填 hash 改回 `merged`,要么状态降级为 `implementing`(见本提案 §7 Step 1 与 reflect 2026-W22-zentao B1/B3)。

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| Claude | 帮用户标 proposal `merged` 前,必须先确认有对应 commit hash;否则用 `implementing`。下次会话起生效。 |
| 开发者(Wjl)| README 状态索引更可信——`merged` 行一定有 commit,tracking 期不空转 |
| 历史 proposal | 0001-0008 已是真 merged(有 commit/follow-up),合规;**0014 是唯一存量违规**,需回填(§7) |

---

## 5. 风险(Risks)

- **R1 — 增加摩擦**:User-requested-bypass 本意是"快"。缓解:`implementing` 状态完全允许"已拍板、在做",只是不冒充 `merged`;摩擦极小(改个状态字)。
- **R2 — commit hash 在 squash/rebase 后失效**:缓解:填"代表性 commit"即可,允许 `0fc27a3 + follow-up` 这种(0008 已是此格式);hash 失效不影响"曾经 merged"的事实判断。

---

## 6. 备选方案(Alternatives Considered)

- **方案 A — 不做**:`merged` 继续可空,未来还会出现 0014 类"纸面 merged",自进化环的"输出端"可信度崩坏。**不选**。
- **方案 B — 加 git hook 自动校验 README 表里 merged 行必须有 hash**:成本高、易误报(hash 格式多样);**本期不做**,先靠文字约束 + Claude 自觉,4 周后看是否仍复发再考虑工具化(候选并入 0016/未来工具链提案)。
- **方案 C — 取消 `merged` 状态,只保留 `done`**:破坏性太大,`tracking` 阶段需要"已合入但还在观测"的语义。**不选**。

---

## 7. 实施计划(Implementation Plan)

```
[ ] Step 1: 回填 0014 —— 决断:补 commit(走 reflect 2026-W22-zentao B1 五轮提交)后填 hash;
            或先把 0014 状态从 merged 降级为 implementing + README 同步(诚实标注)。需用户决策。
[ ] Step 2: 改 0000-template.md §10 + §元信息,加"merged commit 必填"约束语
[ ] Step 3: 改 README.md 生命周期图 + 状态索引表头加注 merged 定义
[ ] Step 4: 本提案自身置 merged 时,以本提案的实现 commit 为示范(吃自己的狗粮)
[ ] Step 5: tracking 4 周,观察是否再出现 merged-but-no-commit
```

> Step 2/3 改的是 `0000-template.md` 与 `README.md`,二者**不在** [.claude/rules.md §L.2](../../.claude/rules.md) 的受管 SSoT 清单内(该清单为:开发规范.md / 模块工作流.md / gate Phase 模板 / rules.md / settings.json),故实现不需额外 SSoT 授权;但仍走 solo-review。

---

## 8. 衡量指标(How will we know it worked?)

| 信号 | 基线 | 目标(4 周)|
|---|---|---|
| `merged` 但 commit 待填的 proposal 数 | 1(0014)| 0 |
| tracking 期对象无 commit(空转 tracking)| 1 | 0 |
| proposal 状态与 git 事实一致率 | ~92%(13 个里 1 个失真)| 100% |

跟踪期:2026-05-27 ~ 2026-06-24。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | 待 | | solo-review |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit
- 合入 commit:**ea5cd37**(`docs(proposals): 实现 0021 — proposal merged 须绑真实 commit`)
- 实际 merged 日期:2026-05-27
- 自指验证:本提案是第一个按自身规则填真实 commit hash 的 proposal;0015(5e9a17f)/0016(c2e8b99)/0019(5ee6676)同批回填,0022 因实现被拦截**故意停在 accepted**(正是本规则要的效果)

### 最终判定
- [ ] done(4 周内 0 复发)
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude(meta-cognitive)/ Wjl | 初稿 V1.0,从 reflect/2026-W22-zentao-integration 模式 3 / B3 派生 |

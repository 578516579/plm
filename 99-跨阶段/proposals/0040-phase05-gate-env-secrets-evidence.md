# Proposal 0040: Phase 05 上线 Gate §C 加 "JWT/Druid/Redis env 实证截图" 必填项

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0040 |
| 标题 | Phase 05 上线 Gate §C 凭据章节加 "F-001/F-002/F-003 env 注入实证截图" 必填项 |
| 状态 | **draft**(2026-05-29 起草) |
| 类型 | 流程 / Gate Checklist |
| 提出人 | Claude(test-orchestrator 安全审计派生 + Wjl 全模块验收会话) |
| 提出日期 | 2026-05-29 |
| 评审人 | Wjl(solo-review,待签) |
| Tracking 截止 | _待 merged + 4 周;首次 prod 上线时硬触发_ |
| 关联 reflect | _无_(本期"全模块验收会话"派生) |
| 关联 commit | bf221f3(安全审计 v0.2 落地)/ aa5ab29(测试报告 §4.6 安全 finding) |

---

## 1. 背景(What's the problem?)

2026-05-29 全模块测试验收的安全审计层(L6)发现 3 个 P1 finding,**全部是"开发零依赖默认值"在生产漏 env 注入的场景**:

- **F-001** `application.yml:150` JWT_SECRET 默认 `abcdefghijklmnopqrstuvwxyz` → 漏 env → JWT 可被任意人伪造
- **F-002** `application-druid.yml:50-51` Druid 监控台默认 `plm/123456` → 漏 env → 数据源密码/慢SQL/热点SQL 全暴露
- **F-003** `application.yml:131` REDIS_PASSWORD 默认空 → 漏 env → 任意人 FLUSHALL

当前 PLM 是开发零依赖默认值模式(开发体验好),**但 Phase 05 上线 Gate 模板缺乏强制的"env 注入实证"门槛** — 上线人员可能直接 `java -jar plm-admin.jar` 启动 prod,3 个默认值生效都没人发现。

[安全审计.md v0.2 §5 一票否决项](../../04-测试/安全审计.md) 已硬化了这 3 条文字规约,但没绑到 Gate Checklist。**Gate 不强制 = 流程会忘**。

---

## 2. 证据(Evidence)

- **关联 安全审计**:[04-测试/安全审计.md v0.2](../../04-测试/安全审计.md) §2.1 凭据章节 / §3 F-001/F-002/F-003 / §5 一票否决项(新硬化但未绑 Gate)
- **关联 测试报告**:[04-测试/测试报告-2026-05-29-全模块验收.md §4.6 + §5](../../04-测试/测试报告-2026-05-29-全模块验收.md) "通过(条件性) — F-001/F-002/F-003 在 Phase 05 上线 Gate 必须实证 prod env 已注入"
- **关联 signals**:[99-跨阶段/signals/2026-05.md §9 事件 4](../signals/2026-05.md) "L6 安全 7 finding(0 P0 / 3 P1 默认值)"
- **关联 历史 gotcha**:[CLAUDE.md gotcha #3](../../CLAUDE.md) Redis 用 localhost(IPv6 坑)— 同类"默认值在某环境失效"的复发模式
- **关联 现有 Gate**:[99-跨阶段/gate-checklists/Phase05-上线-Gate.md](../gate-checklists/) §C 凭据章节当前**无 env 实证项**,只列了清单未要求截图

---

## 3. 提案(What's the change?)

### 3.1 改动文件清单

| 文件 | 改动 |
|---|---|
| `99-跨阶段/gate-checklists/Phase05-上线-Gate.md` | §C 凭据章节新增 3 行硬约束(JWT/Druid/Redis)+ "实证截图"列 |
| `99-跨阶段/gate-checklists/instances/README.md` | 加一句"Phase 05 §C 的 3 行 env 实证不允许空填" |
| `.githooks/pre-commit` | 可选:扫 prod 上线 commit msg(`feat(deploy): prod`)→ 校验同 commit 是否带 Gate 实例引用 |

### 3.2 Gate Checklist 模板段草案

```diff
--- a/99-跨阶段/gate-checklists/Phase05-上线-Gate.md
+++ b/99-跨阶段/gate-checklists/Phase05-上线-Gate.md
@@ §C 凭据 / 安全 @@
- - [ ] 生产凭据已通过安全渠道发放
- - [ ] .env 未提交 git

+ ### §C 3 项 P1 默认值 env 实证(MUST,本节不允许 N/A)
+
+ | 项 | 验证命令 | 截图位置 | 结果 |
+ |---|---|---|---|
+ | **F-001 JWT_SECRET** 已注入强随机值 | `ssh prod 'systemctl show plm-admin -p Environment' \| grep JWT_SECRET` 输出长度 ≥ 32 且 ≠ `abcdefghijklmnopqrstuvwxyz` | `__附 1__` | ☐ pass ☐ fail |
+ | **F-002 DRUID** 强制关或强密码 | 选项 A:yml `stat-view-servlet.enabled: false`(grep 出);选项 B:`DRUID_USERNAME/PASSWORD` env 已注入 ≠ `plm/123456` | `__附 2__` | ☐ pass ☐ fail |
+ | **F-003 REDIS_PASSWORD** 非空 | `redis-cli -h prod-redis ping` 不带 -a 报 `NOAUTH` + 带 -a 注入值后 `PONG` | `__附 3__` | ☐ pass ☐ fail |
+
+ > 上述 3 行任一 fail / 任一截图缺失 → **驳回**,不允许 prod 切流量。豁免必须走 [proposal](../../proposals/) 显式记录,**严禁口头放行**。
```

### 3.3 设计原则

| 原则 | 说明 |
|---|---|
| **截图实证非口头** | 默认值是看不见的,截图强制把"实际生效配置"显形 |
| **2-of-3 路径** | F-002 给 2 个等价选项(关 console / 强密码),不强制单一路径 |
| **跑命令而非看 yml** | yml 可以写占位,实际 env 才决定 → 必须跑 `systemctl show`/`redis-cli` 验真 |
| **不许 N/A** | 即使 dev 环境的 Phase 05 实例(本项目当前都是 dev),也要标 N/A 理由 + 引用本提案,不允许空白 |

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 上线 owner | Phase 05 Gate 多 3 个截图任务(预估 +5min) |
| Claude | 起 Phase 05 Gate 实例时知道 §C 必填 3 行 |
| 已有 Phase 05 实例 | **不溯及既往**(本项目当前 dev,Phase 05 实例都标 dev 引用);**仅对未来 prod 上线生效** |
| 安全审计 | F-001/F-002/F-003 闭环路径硬化 |

---

## 5. 风险

- **风险 1 — 截图被伪造**:上线 owner 自己截 dev 环境截图当 prod
  **缓解**:截图必须显示 prod hostname / IP 部分;PR review 时交叉验证。本项目当前 solo,远期上 reviewer 后才能完全闭环
- **风险 2 — F-002 选项 A "关 console" 太重**:Druid 关掉后排错难
  **缓解**:给选项 B(强密码)同价路径
- **风险 3 — Gate 实例越来越重**:加上其他 P1 finding 类似硬化,§C 段会膨胀
  **缓解**:每个硬化项独立小节;紧凑型 Gate 模板(proposal 0032)默认折叠 §C
- **风险 4 — 本项目当前无 prod**,准入门槛挂空
  **缓解**:本提案 merged 后即生效,首次 prod 上线时硬触发;tracking 期看是否真有效

---

## 6. 备选方案

- **方案 A — 维持现状**:安全审计 §5 文字硬化已经够。**不选** — 流程不绑 Gate 会忘
- **方案 B(本提案)— Gate §C 加 3 行实证截图**:推荐
- **方案 C — 自动化扫**:写脚本 `prod-secrets-audit.sh` 远程 ssh 跑 → output 进 Gate。**长期目标**,首版手工截图先夯实流程
- **方案 D — 写进 .githooks/pre-push**:推 main 时校验。**过严** — Gate 是审批工具,hook 是技术工具,职责不混
- **方案 E — 推迟到首次 prod 上线时再起**:**反 PaaS** — 等出事再加流程是错误顺序

选 B + 远期演进到 C。

---

## 7. 实施计划

```
[x] Step 1: 本 draft 起草(本 commit)+ README 索引
[ ] Step 2: Wjl review 转 proposed → accepted
[ ] Step 3: 修 99-跨阶段/gate-checklists/Phase05-上线-Gate.md §C(diff 见 §3.2)
[ ] Step 4: 修 instances/README.md 加"不允许 N/A"说明
[ ] Step 5: 通知协作规范:Phase 05 § J friction 表加一行"3 项 env 实证须截图"
[ ] Step 6: 选填:写 plm-backend/scripts/prod-secrets-audit.sh 雏形(方案 C 路径)
[ ] Step 7: merged 后,tracking 至首次 prod 上线
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Phase 05 实例 §C 3 项实证完整率 | n/a(0 prod 实例)| **100%**(首次 prod 上线时) |
| 因默认值未覆盖导致的 prod 安全事故数 | 0(尚无 prod) | **0**(本提案的核心目标) |
| §C 段平均填写时长 | n/a | < 10min(3 截图 + 1 验证脚本) |
| 走豁免 proposal 的次数 | n/a | 0(豁免应是极端情况) |

跟踪期:_待 merged 后 4 周 + 首次 prod 上线追加 4 周_

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _待定_ | solo-review |
| Claude(自评 0033 范式)| 🟢 Approve | 2026-05-29 | §9.1 |

### 9.1 自评 7 维

| 维度 | 评分 | 依据 |
|---|---|---|
| scope 合理性 | 9/10 | 1 模板段 + 1 README 说明;边界清晰 |
| 证据充分性 | 9/10 | 安全审计 3 P1 finding + 同类 gotcha #3(默认值复发模式) |
| 决策可追溯 | 8/10 | 5 备选 + ❌/✅;选 B + 远期 C |
| 实施完整度 | 8/10 | Step 1-7 具体,Step 3 Diff 草案完整 |
| 风险识别 | 8/10 | 4 风险全识别,截图伪造缓解承认局限 |
| 可观测性 | 7/10 | 4 信号定量;"事故 = 0"是被动指标 |
| dogfood / 自我一致 | 9/10 | 本期就是安全审计驱动 Gate 强化 |

**总评**:平均 8.3 → **Approve**

**必须改清单**:无(P1 级 finding 闭环路径,流程类提案推荐合)

---

## 10. 实施后跟踪

### 若 rejected
- 原因:_待填_

### 若 merged
- 合入 commit:_待定_
- 实际 merged 日期:_待定_

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| §C 实证完整率 | n/a | 100% | | | | |
| 默认值导致的 prod 事故 | 0 | 0 | | | | |
| 平均填写时长 | n/a | < 10min | | | | |
| 豁免 proposal | n/a | 0 | | | | |

### 最终判定
- [ ] done
- [ ] partial
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-29 | Claude(test-orchestrator 安全审计派生 + Wjl 全模块验收) | V1.0 — Phase 05 Gate §C 加 F-001/F-002/F-003 env 实证截图必填;diff 草案 + 2-of-3 路径选项;7 维自评 8.3 Approve |

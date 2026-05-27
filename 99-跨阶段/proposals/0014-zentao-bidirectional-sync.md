# Proposal 0014: 禅道(ZenTao)双向同步 — 修订 Proposal 0007 / 设计文档 §1.2 "先做单向" 取舍

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0014(0010-0012 被 `claude/interesting-goldwasser-449e96` branch 占用,0013 已用,本提案取下一可用号) |
| 标题 | 在 plm-integration 模块新增禅道连接器,做禅道 ↔ PLM 的 **双向**同步(bug/story/task/testcase),修订 Proposal 0007 §3.3 + 02-设计/MCP-集成-设计.md §1.2 "先做单向" 取舍 |
| 状态 | **merged**(User-requested-bypass,solo-review) |
| 类型 | 架构 |
| 提出人 | Wjl + Claude |
| 提出日期 | 2026-05-25 |
| 评审人 | Wjl(solo-review,参 [0005](0005-solo-sprint-merge.md)) |
| 评审日期 | 2026-05-25 |
| Tracking 截止 | 2026-06-30(与 Proposal 0007 同步对齐) |

---

## 1. 背景(What's the problem?)

[Proposal 0007](0007-mcp-integration-modules-uplift.md) 把 MCP/Integration 模块从 v0.5+ 提到当前迭代,首批 connector 类型包括 **禅道(zentao)** —— 但在 [02-设计/MCP-集成-设计.md §1.2 不包含](../../02-设计/MCP-集成-设计.md) 明确写:

> 双向同步的冲突合并策略(先做单向:PLM ↔ 外部主从单一方向)

这一取舍在飞书/GitLab 场景下合理(飞书主要做出站通知,GitLab 主要做入站 webhook 触发流水线),但**对禅道场景不成立**:

- 禅道是**同类型**的项目管理系统,与 PLM 业务模型高度重叠(bug↔defect / story↔requirement / task↔task / case↔testcase),不是消息平台或 CI 平台
- 用户在 2026-05-25 会话明确选择:**方案 C(双向同步 ~1+ week)**,并确认有真实禅道实例 + 测试账号可联调
- 单向只能覆盖一种迁移方向(禅道 → PLM **或** PLM → 禅道),实际业务需要双方互为补充(禅道有的 bug 流转到 PLM 推进,PLM 新建的 defect 反推送禅道做测试团队对账)

---

## 2. 证据(Evidence)

- **关联 Proposal**:[0007](0007-mcp-integration-modules-uplift.md) §3.3 "首批接入 = 飞书 + GitLab + MCP Server 自身 + 钉钉/Jira/Figma/**禅道**/ZTF"(已 merged,字典 `biz_integration_type` 包含 `zentao` 值,见 [business-integration.sql:78](../../plm-backend/sql/business-integration.sql))
- **关联设计文档**:[02-设计/MCP-集成-设计.md §1.2](../../02-设计/MCP-集成-设计.md) `双向同步的冲突合并策略(先做单向:PLM ↔ 外部主从单一方向)` —— **本 Proposal 明确修订该条**
- **用户请求**:2026-05-25 会话原话 "增加和禅道系统的对接,给出方案和开发" + AskUserQuestion 选项:
    1. 范围 = **C. + 双向同步(~1+ week)**
    2. 环境 = **有真实禅道实例 + 测试账号**
- **PRD 对照**:禅道无原型,但 [PRD-MAPPING.md §33](../../PRD-MAPPING.md) `plm-integration` 行覆盖该集成范围;且字典已含 zentao 值,设计上已为 zentao 预留位

---

## 3. 提案(What's the change?)

### 3.1 SSoT 文档修订

| 文件 | 改动类型 | 影响 |
|---|---|---|
| [02-设计/MCP-集成-设计.md §1.2](../../02-设计/MCP-集成-设计.md) | 修改 | 把 "先做单向" 改为 "**禅道**与 PLM 走双向 + last-write-wins(update_time 比对);**飞书/GitLab** 仍按 0007 原议做单向" |
| [02-设计/Zentao-集成-设计.md](../../02-设计/Zentao-集成-设计.md) | 新建 | 禅道专用设计文档(API 流派、字段映射、状态机映射、冲突策略) |
| [PRD-MAPPING.md §33](../../PRD-MAPPING.md) | 修改 | 在 §33 之后新增 §33.1 "Zentao 子映射"(字段对照表 + 状态映射 + 错误码 813-820) |

### 3.2 代码新增

| 文件 | 改动类型 |
|---|---|
| `plm-backend/plm-integration/.../adapter/zentao/ZentaoConnectorAdapter.java` | 新建 |
| `plm-backend/plm-integration/.../webhook/ZentaoWebhookController.java` | 新建 |
| `plm-backend/plm-integration/.../service/ZentaoInboundSyncService.java` | 新建(入站:禅道 → PLM) |
| `plm-backend/plm-integration/.../service/ZentaoOutboundSyncService.java` | 新建(出站:PLM → 禅道) |
| `plm-backend/plm-common/.../core/event/*ChangedEvent.java` | 新建 4 个 ApplicationEvent DTO(Defect / Requirement / Task / TestCase) |
| `plm-backend/plm-defect/plm-requirement/plm-task/plm-testcase` 各 `ServiceImpl` | 修改:insert/update/delete 成功后 publishEvent(@TransactionalEventListener AFTER_COMMIT) |
| `plm-backend/sql/business-integration-zentao.sql` | 新建:ALTER tb_defect/tb_requirement/tb_task/tb_testcase 加 external_source/external_id/external_url 三列 + 唯一索引;新建 tb_integration_user_mapping;禅道相关字典 |
| `plm-backend/sql/business-integration-zentao-rollback.sql` | 新建:回滚脚本 |
| `plm-backend/plm-integration/src/test/.../` | 新建 4 份测试(Adapter / Webhook / InboundSync / OutboundSync) |

### 3.3 §1.2 设计文档 Diff 草案

```diff
--- a/02-设计/MCP-集成-设计.md
+++ b/02-设计/MCP-集成-设计.md
@@ -20,7 +20,8 @@
 - agriplm-cli(Node.js 包发布到 npm,独立项目 v0.6+)
 - Dify 工作流引擎接入(依然在 [99-跨阶段/AgriPLM-模块映射-2026-05-16.md] 剥离清单)
 - Figma 设计稿语义解析(v0.5+)
-- 双向同步的冲突合并策略(先做单向:PLM ↔ 外部主从单一方向)
+- ~~双向同步的冲突合并策略~~ **见 Proposal 0014**:禅道 ↔ PLM 走双向,采用 last-write-wins(update_time 比对);飞书/GitLab 仍单向(飞书主要出站通知、GitLab 主要入站 webhook)
```

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| 开发者 | 4 个业务模块(plm-defect/plm-requirement/plm-task/plm-testcase)的 ServiceImpl 加 publishEvent — 模板化改造,~20 处 Edit;plm-common 加 4 个 Event DTO;plm-integration 加 4 个新文件 |
| Claude | 后续会话中处理"禅道里的 bug 怎么映到 PLM" 类问题有 SSoT 可查(PRD-MAPPING.md §33.1) |
| 测试 / 运维 | 需要在 connector 配置页填禅道 endpoint + 账号 + webhook secret;禅道侧需在「通用 → Webhook」配出站 URL `https://plm/dev-api/integration/webhook/zentao/{connectorId}` |
| 已有代码 / 文档 | 向后扩展:**只增不减**;4 张业务表新增 3 列 + 1 个唯一索引,无破坏(`del_flag=0` 的现有行 external_source/external_id 留空);现有 connector 列表页/webhook 事件页**不动**(已是通用页) |

---

## 5. 风险(Risks)

| 风险 | 缓解 |
|---|---|
| **双向同步无限循环**(PLM 改 → 推禅道 → 禅道 webhook → PLM 改 → ...) | 入站 sync 时设线程本地 `SyncContext.inbound=true`,出站 EventListener 检查到该标志直接 return;同一对象 60s 内重复同步用 Caffeine LRU 抑制 |
| **冲突合并 last-write-wins 误覆盖** | (a) 比对 PLM 侧 `update_time` 与禅道 `lastEditedDate`,旧的不写入;(b) 关键字段(severity / priority / status)写入前先 SELECT FOR UPDATE 锁行;(c) 写入失败落 `tb_integration_webhook_event.process_error` 让事件流可见 |
| **禅道 API 版本差异**(v15 / v18 / 企业版) | 用 v18+ REST API(/api/v1/tokens + /api/v1/products/{id}/bugs 形态),v15- 老 sessionID 流派**本期不兼容**,在 ping() 返回提示;真实联调发现版本差异时退化到 ZentaoConnectorAdapter 的 v3 流派(留 TODO) |
| **凭据泄露**(账号 + 密码存 DB) | 沿用 AES-256-GCM(MCP_ENCRYPT_KEY)— Proposal 0007 已落地;DB 备份场景下密文不可解 |
| **用户映射缺失**(禅道 account 不存在于 sys_user) | 落 tb_integration_user_mapping 记录;入站时 user_id=null 容忍,出站时映射缺失 → 默认指给 connector 创建者 |
| **工作量大,review 困难** | 切 5 轮提交:①Proposal+设计文档+DDL ②Adapter+Webhook ③双向 SyncService ④业务模块插 Event ⑤测试+真实联调 |

---

## 6. 备选方案(Alternatives Considered)

- **方案 A(本提案)**:双向 + last-write-wins + 60s 防抖。禅道侧改 → webhook → PLM 落库;PLM 侧改 → AppEvent → REST API 反写禅道
- **方案 B**:走 Quartz 定时全量拉取(每 5min),禅道为主源。**不选**:延迟 5min,且对禅道 API 压力大,且无法做 PLM 反向写
- **方案 C**:中间引入 Kafka 异步队列做事件总线。**不选**:架构开销大,当前 Phase 用 Spring @EventListener 已够;设计文档明说 Phase 2 才考虑队列
- **方案 D**:维持 0007 单向(PLM 为读端) — **用户明确否决,选 A**

选 A(用户确认)。

---

## 7. 实施计划(Implementation Plan)

```
[x] Step 1: 写 Proposal 0014 + 02-设计/Zentao-集成-设计.md + PRD-MAPPING §33.1(本 commit)
[x] Step 2: DDL 加 external_source/external_id/external_url + tb_integration_user_mapping + 禅道字典
[x] Step 3: ZentaoConnectorAdapter(ping / verifyWebhookSignature / getToken 缓存 / list+create+update 4 类资源)
[x] Step 4: ZentaoWebhookController + ZentaoInboundSyncService(禅道 → PLM)
[x] Step 5: 4 个 ApplicationEvent DTO + 4 个业务 ServiceImpl 插 publishEvent
[x] Step 6: ZentaoOutboundSyncService(PLM → 禅道,带 SyncContext 防循环)
[x] Step 7: 单元测试(Adapter + Webhook + Inbound + Outbound 4 份,MockServer 模拟禅道)
[ ] Step 8: mvn install -T 4 编译验证全栈
[ ] Step 9: 真实禅道联调(需用户提供 endpoint + 账号 + webhook secret)
```

---

## 8. 衡量指标(How will we know it worked?)

| 信号 | 基线 | 目标 |
|---|---|---|
| 禅道 webhook 落 PLM 业务表的成功率(`process_status=2` / 总有效签名事件) | 0% | ≥ 95% |
| PLM 改一条 defect → 1 分钟内禅道 bug 同步更新成功率 | 0% | ≥ 90% |
| 同步循环死锁次数(同一对象 5 分钟内 ≥3 次跨向同步) | n/a | ≤ 0 |
| 冲突合并误覆盖(用户反馈"我刚改的被覆盖了") | n/a | ≤ 1 起 / 月 |
| 用户映射缺失导致出站失败次数 | n/a | ≤ 5 起 / 月(可接受,缺映射时默认指给 admin) |

跟踪期:2026-05-25 ~ 2026-06-30。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | ✅ 通过 (solo-review) | 2026-05-25 | User-requested-bypass:与 Proposal 0007 同期 tracking 截止 2026-06-30,届时统一评估是否归档 |

---

## 10. 实施后跟踪(merged 后填)

### 实际 PR / commit

- 合入 commit: **9d37d03**(`feat(integration): Zentao 双向同步 — 领域事件钩子 + webhook + 出入站 sync + 用户映射`,2026-05-27 由并行 session 提交)
- 实际 merged 日期:2026-05-27
- ⚠ **代码已 merged,但 §7 实施计划未全完**(见 [reflect/2026-W22-zentao-integration.md](../reflect/2026-W22-zentao-integration.md)):
  - **O4 / B2**:§3.2/§7 声明的 4 份测试实际只存 2 份(SyncContextTest + ZentaoFieldMapperTest);Adapter / Webhook / Inbound / Outbound 4 份**待补**
  - **O8**:business-integration-zentao.sql 的"用户映射"菜单 2530 + 6 权限指向**尚不存在**的前端页 / Controller(设计文档自承本期不做 UI)→ 待清理或补实现
  - **§7 Step 9**:真实禅道联调未做(防回环三道防线运行时正确性未验证)
  - 架构决策已补 [ADR-0008 事件总线](../../03-开发/ADR/0008-in-process-domain-event-bus.md) / [ADR-0009 集成旁路](../../03-开发/ADR/0009-integration-writeback-bypasses-business-service.md)(reflect B6)

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 | W24 | W25 |
|---|---|---|---|---|---|---|
| 禅道→PLM 成功率 | 0% | ≥95% | | | | |
| PLM→禅道 同步成功率 | 0% | ≥90% | | | | |
| 同步循环死锁次数 | n/a | 0 | | | | |
| 冲突合并误覆盖次数 | n/a | ≤1/月 | | | | |

### 最终判定
- [ ] done(达成目标,本提案归档)
- [ ] reverted(未达成 → 走回滚 PR,并在此段写"为什么失败")

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-25 | Wjl + Claude | 初版 + User-requested-bypass merged |

# Proposal 0019: 把"第三方系统集成连接器" SOP 固化为 `integration-connector` skill

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0019 |
| 标题 | 集成连接器三件套 SOP 固化为 `integration-connector` skill |
| 状态 | **merged → tracking** (solo-review) |
| 类型 | 工具链 / skill(号段 0200-0299) |
| 提出人 | Claude(meta-cognitive / reflect 2026-W22-zentao-integration)+ Wjl |
| 提出日期 | 2026-05-27 |
| 评审人 | Wjl(solo-review) |
| 评审日期 | 待 |
| Tracking 截止 | 2026-06-24 |

---

## 1. 背景(What's the problem?)

接入第三方系统(飞书 / GitLab / 禅道)做数据同步,每次都严格走同一条 13 文件的链路,但这条 SOP **从未固化**,每个连接器隔着不同会话从头发明。与主线 [0015 `plm-module-uplift`](0015-plm-module-uplift-skill.md)(业务模块 🟡→🟢 SOP)**正交**:0015 管"造业务模块",本提案管"接外部系统"。两条 SOP 互不替代。

---

## 2. 证据(Evidence)

- **三次复用同一范式**:[reflect/2026-W22-zentao-integration.md](../reflect/2026-W22-zentao-integration.md) F1 + O1。飞书 / GitLab / 禅道都走 `ConnectorAdapter`(ping/验签/token 缓存/CRUD)+ `*WebhookController`(@Anonymous + 验签 + 落 event 表)+ `*InboundSyncService` / `*OutboundSyncService`(SyncContext 防循环 + last-write-wins)+ 业务模块插 publishEvent + 专用设计文档(C4 + 字段映射 + 状态机 + 时序图 + 错误码)。禅道是第 3 次。
- **未固化的代价**:reflect 模式 1 根因——meta-cognitive 的"单会话内同类 ≥3 次"触发器(主线 A2)抓不到集成类工作,因为它是**跨会话低频高单价**(一个 connector ≈ 6 个业务模块工作量),复用信号是"跨会话第 3 个连接器",当前体系看不见。
- **范式先例**:[~/.claude/skills/ruoyi-bootstrap](C:/Users/Wjl/.claude/skills/ruoyi-bootstrap/SKILL.md) 与本批 0015 都证明"固化高 ROI SOP 为 skill"有效。

---

## 3. 提案(What's the change?)

### 3.1 skill 物理路径与文件清单

```
~/.claude/skills/integration-connector/
├── SKILL.md                       ← 触发词 + Phase 0-8 主流程
├── references/
│   ├── connector-adapter.md       ← ping / 验签 / token 缓存 / CRUD 范式
│   ├── webhook-controller.md      ← @Anonymous + 签名校验 + 落 event 表 + 幂等(externalEventId)
│   ├── bidirectional-sync.md      ← Inbound/Outbound + SyncContext 防循环 + last-write-wins(链 proposal 0020)
│   ├── field-mapping.md           ← external_source/external_id/external_url 三列 + 字段映射器 + 状态机映射
│   └── integration-design-doc.md  ← 专用设计文档模板(C4 + 字段映射表 + 状态机 + 错误码 + 时序图)
└── assets/templates/
    ├── ConnectorAdapter.java.tpl
    ├── WebhookController.java.tpl
    ├── InboundSyncService.java.tpl
    ├── OutboundSyncService.java.tpl
    ├── EntityChangedEvent.java.tpl     ← 业务事件钩子(plm-common/core/event)
    ├── business-integration-__sys__.sql.tpl   ← external_* 列 + 映射表 + 字典(含 sys_menu, 过 pre-commit lint)
    └── integration-design-doc.md.tpl
```

### 3.2 SKILL.md 触发词(草案)

`接入 XX 第三方系统做同步` / `加一个 connector` / `双向同步 <系统名>` / `integrate <system>` / `禅道/Jira/GitLab/飞书 集成`。

### 3.3 Phase 0-8 主流程

| Phase | 名称 | 关键动作 |
|---|---|---|
| 0 | Pre-flight | 问系统名 + 同步方向(单向/双向,**依据该系统类型**,见 reflect F3)+ 查 plm-integration 现有 adapter |
| 1 | 专用设计文档 | 渲染 integration-design-doc 模板(C4 + 字段映射 + 状态机映射 + 错误码 + 时序图) |
| 2 | DDL | external_source/external_id/external_url 三列 + 映射表 + 字典;**唯一索引列允许 NULL**(reflect O9 坑) |
| 3 | ConnectorAdapter | ping / 验签 / token 缓存 / CRUD |
| 4 | WebhookController | @Anonymous + 验签 + 落 event 表 + externalEventId 幂等 |
| 5 | 双向 Sync | Inbound(消费 + 状态机映射 + 防循环)+ Outbound(@TxEventListener AFTER_COMMIT + SyncContext 拦截 + last-write-wins);**三道防线见 0020** |
| 6 | 业务插 Event | 4 业务 ServiceImpl publishEvent(EntityChangedEvent 子类) |
| 7 | 测试 | 4 份测试:Adapter(MockServer)/ Webhook(验签+幂等)/ Inbound(落库+stale)/ Outbound(防循环+防抖+last-write-wins) |
| 8 | Gate + 联调 | L1 集成 → Phase 02/03 Gate 实例 + 真实环境联调(尤其防循环验证) |

---

## 4. 影响范围(Impact)

| 受众 | 影响 |
|---|---|
| Claude | 下个 connector(Jira)触发即搭好三件套骨架;省去"翻禅道怎么做的"。**CLAUDE.md "Available skill" 段从 2 → 3** |
| 开发者 | 集成类工作从 ~1 week → ~2 day(骨架部分);防回环不再从零想 |
| 已有 connector(飞书/GitLab/禅道)| 反向校准:skill 模板应与三者结构 ≥90% 一致 |

---

## 5. 风险(Risks)

| 风险 | 缓解 |
|---|---|
| R1 与 0015/ruoyi-bootstrap 触发混淆 | description 触发词严格区分:0015=造业务模块、ruoyi-bootstrap=脚手架正名、本 skill=接外部系统。三者 SKILL.md 末尾 cross-link |
| R2 不同第三方系统差异大,模板套不上(消息平台 vs 项管系统同步方向不同)| Phase 0 显式问"系统类型 + 同步方向";单向系统跳过 Phase 5 的 Outbound |
| R3 模板含 7 中第 5 的双向同步范式漂移 | bidirectional-sync.md reference 引用 proposal 0020 的防护清单作为唯一来源,模板漂移时改一处 |
| R4 模板里 Inbound 用裸 JDBC 旁路业务 Service(reflect F5 破窗)被无脑复制 | 模板里把"旁路 Service"做成**显式注释 + 指向 ADR-0009**(见 reflect B6),并提示"能走业务 Service 就走" |

---

## 6. 备选方案(Alternatives Considered)

- **A 不固化**:下个 connector 再发明一遍;reflect 模式 1 已识别为系统性盲点。**不选**。
- **B 并入 0015**:0015 是业务模块 SOP,与集成 SOP 正交,合并会让 0015 description 触发词污染。**不选,两 skill 并列**。
- **C 只写 reference 不做 skill**:不可触发、无模板分发。**不选**。

---

## 7. 实施计划(Implementation Plan)

```
[ ] Step 1: solo-review approve
[ ] Step 2: 从禅道(最新最全)+ 飞书/GitLab 真实代码抽取模板 → ~/.claude/skills/integration-connector/
            ⚠ 依赖 reflect 2026-W22-zentao B1(禅道代码先落 git),否则模板源在 working tree 不稳定
[ ] Step 3: 以 Jira(或下一个真实需求)为 pilot 跑一次,实测耗时 + 结构 diff
[ ] Step 4: 把 proposal 0020 的防护清单嵌入 bidirectional-sync.md reference
[ ] Step 5: CLAUDE.md "Available skill" 段加链;状态 → merged(须有 commit, 见 0021)
[ ] Step 6: tracking 4 周
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| 单 connector 骨架耗时 | ~1 week | ≤2 day(骨架) |
| 下个 connector 走 skill 路径 | 0 | 1(Jira 或下一需求) |
| skill 产出 vs 禅道/飞书/GitLab 结构一致度 | n/a | ≥90% |
| 防回环三道防线遗漏数 | n/a | 0(模板强制带) |

跟踪期:2026-05-27 ~ 2026-06-24。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | 待 | | solo-review |

---

## 10. 实施后跟踪(merged 后填)

- 合入 commit(repo 锚点):**5ee6676**(CLAUDE.md Available skills 注册);skill 本体 13 文件在 `~/.claude/skills/integration-connector/`
- 实际 merged 日期:2026-05-27
- Step 完成度:Step 2(建 skill 13 文件)✅;模板从禅道 9d37d03 抽取,防回环对齐 0020,旁路按 ADR-0009 注释。**pilot(下一个 connector 如 Jira)= tracking 验证**
- 最终判定:[ ] done / [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude(meta-cognitive)/ Wjl | 初稿 V1.0,从 reflect/2026-W22-zentao-integration 模式 1 / B4 派生 |

# ADR-0009: 集成层回写旁路业务 Service(裸 JdbcTemplate)

> 编号说明:见 [ADR-0008](0008-in-process-domain-event-bus.md) 顶部(0001~0007 = 编号规则系列,架构类 ADR 从 0008 起)。

- **状态**:accepted(**有保留** — 见后果"代价")
- **日期**:2026-05-25
- **决策人**:Wjl(技术 lead,solo-review)+ Claude
- **关联**:[reflect/2026-W22-zentao-integration.md 模式5 / F5](../../99-跨阶段/reflect/2026-W22-zentao-integration.md) / 实现 commit `9d37d03`(`ZentaoInboundSyncService` / `ZentaoOutboundSyncService`)

> 补记:决策已随 0014 落地;reflect 2026-W22-zentao F5 指出"集成层为何可旁路业务 Service"本身就该是一条 ADR。

## 背景

禅道入站同步(外部改 → 写回 PLM)时,外部实体的状态不一定满足 PLM 业务状态机。例:禅道缺陷 `active` 态映射进来,若走 `DefectServiceImpl.updateDefect()`,会被 PLM 的 5×5 状态机校验(错误码 601)**拒绝**。但同步的语义是"如实反映外部状态",不是"发起一次合法业务操作"。因此入站同步**不能**走业务 Service 的状态机校验。

## 决策

集成层(`ZentaoInboundSyncService` / `ZentaoOutboundSyncService`)**旁路业务 Service**,直接用 `JdbcTemplate` 读写业务表,自己实现 last-write-wins(`SELECT ... FOR UPDATE` + 时间戳比对),不经过业务状态机校验。

## 理由

- 外部状态机 ≠ PLM 状态机:同步要"镜像外部事实",过 PLM 校验会把合法的外部状态当非法拒绝;
- last-write-wins 是同步层自己的并发策略,与业务层乐观锁/状态机正交;
- 入站量大时绕过 Service 层的对象映射开销更低。

## 后果

### 好
- 同步不被业务状态机误拒;同步层并发语义自洽。

### 代价(技术债,需后续收敛)
- **破坏分层**:CLAUDE.md 架构约定"每个业务包走 domain/mapper/service",这里横穿 4 个模块的表;
- **SQL 注入面**:`"SELECT * FROM " + table` 拼表名 —— 表名虽是受控常量,但**模式危险**,未来若 table 来自外部输入即漏洞。**必须**保证表名永远是白名单常量,禁止拼接任何外部值;
- **绕过软删除/审计字段逻辑**:裸 JDBC 不自动填 `del_flag`/`update_by`/`update_time`,需手动保证;
- **复制传播风险**:[integration-connector skill(0019)](../../99-跨阶段/proposals/0019-integration-connector-skill.md) 会把此模式模板化 —— 模板里**必须**显式注释"旁路是有意决策,见本 ADR;能走业务 Service 就走,不要无脑裸 JDBC"。

### 收敛方向(技术债)
- [ ] 中期:抽一个 `IntegrationWriteGateway`,封装"白名单表 + 自动填审计字段 + last-write-wins",取代散落的裸 JDBC 拼接;
- [ ] 表名白名单用枚举/常量集中管理,杜绝拼接外部值;
- [ ] 关联 [reflect 2026-W22-zentao B5/0020](../../99-跨阶段/proposals/0020-bidirectional-sync-loop-guard-gotcha.md):防回环 + 写回范式一起沉淀。

## 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | ✅ accepted(有保留)| 2026-05-25 | solo-review;SQL 注入面 + 分层破坏列为待收敛技术债 |

## 修订记录

| 日期 | 修改人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude(meta-cognitive)/ Wjl | 补记 ADR(reflect 2026-W22-zentao F5/模式5)|

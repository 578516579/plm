# ADR-0008: 进程内领域事件总线(ApplicationEvent + @TransactionalEventListener)

> 编号说明:ADR-0001~0007 已被各业务模块"编号规则"系列占用(0001 PRJ / 0002 REQ / 0003 TASK / 0004 SPR / 0005 DEFECT / 0006 TC / 0007 DOC),故架构类 ADR 从 0008 起。reflect/2026-W22-zentao 原拟 0002/0003 系基于不完整视图,已纠正。

- **状态**:accepted
- **日期**:2026-05-25
- **决策人**:Wjl(技术 lead,solo-review)+ Claude
- **关联**:[proposal 0014 §6](../../99-跨阶段/proposals/0014-zentao-bidirectional-sync.md) / [reflect/2026-W22-zentao-integration.md 模式4](../../99-跨阶段/reflect/2026-W22-zentao-integration.md) / 实现 commit `9d37d03`

> 本 ADR 为补记:决策已于 2026-05-25 随禅道双向同步(0014)落地实现,但当时未单独建 ADR(reflect 2026-W22-zentao 模式4 指出此缺口)。

## 背景

禅道双向同步需要"业务实体(缺陷/任务/测试用例/需求)变更时,把变更推送到外部系统"。这要求业务模块(plm-defect/task/testcase + 需求)在增删改后**通知** plm-integration 模块。直接让业务 Service 依赖 plm-integration 会形成**反向依赖**(plm-integration 依赖业务模块,业务模块又依赖 integration),破坏分层。需要一种**解耦**的通知机制。

## 决策

引入**进程内领域事件总线**:
- 事件基类 `EntityChangedEvent`(抽象)+ 4 子类(Defect/Requirement/Task/TestCaseChangedEvent)放 **plm-common/core/event/**(最底层,谁都能依赖);
- 业务 ServiceImpl 在 add/update/delete 后 `applicationEventPublisher.publishEvent(...)`;
- plm-integration 的出站 sync 用 **`@TransactionalEventListener(phase = AFTER_COMMIT)`** 监听,**事务提交后**才同步(避免业务回滚了却已推外部);
- 不引消息队列(Kafka/RabbitMQ),用 Spring 自带的 ApplicationEvent。

## 理由

- **解耦 + 不反向依赖**:事件在 plm-common,业务模块只 publish 不知道谁 listen;plm-integration 单向依赖 plm-common。
- **AFTER_COMMIT 语义正确**:业务事务回滚 → 事件不投递 → 外部系统不被错误同步。
- **为什么不上 Kafka(0014 §6 方案 C 否决)**:
  - 当前是**单体进程内**通知,无跨服务/跨进程需求;Kafka 引入 broker 运维 + 消息序列化 + 消费位点管理,**重**;
  - PLM 当前规模(单实例)用进程内事件足够;真有水平扩展/异步削峰需求时再升级(届时另立 ADR supersede 本条);
  - 进程内事件**零额外基础设施**,与现有 Spring 栈无缝。

## 后果

### 好
- 业务模块与集成模块解耦,新增 connector(Jira 等)只需加 listener,不改业务模块;
- 事件机制可复用给未来"审计日志 / 站内通知 / WebSocket 推送"等场景。

### 代价 / 风险
- **进程内 = 不跨实例**:多实例部署时,事件只在产生它的那个实例内投递。若未来后端多实例 + 出站 sync 需要全局唯一处理 → 必须升级为 MQ(本 ADR 失效,届时 supersede);
- **事件丢失无重试**:进程崩溃时 AFTER_COMMIT 未执行的事件丢失,无持久化/重投。可接受(同步是幂等的,且有 last-write-wins + 定时对账兜底);
- 监听器异常不应回滚业务事务(AFTER_COMMIT 已在事务外),但需保证 listener 自身异常被捕获不影响主流程。

### 后续动作
- [ ] 多实例部署前重新评估本 ADR(进程内 → MQ 的触发条件:后端 replica > 1 且出站 sync 要求全局单次)
- [ ] 出站 listener 必须 try-catch 自身异常 + 记录失败事件供对账(关联 [reflect 2026-W22-zentao B2](../../99-跨阶段/reflect/2026-W22-zentao-integration.md):出站 sync 测试需覆盖)

## 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | ✅ accepted | 2026-05-25 | solo-review;ADR 文件 2026-05-27 补记 |

## 修订记录

| 日期 | 修改人 | 改了什么 |
|---|---|---|
| 2026-05-27 | Claude(meta-cognitive)/ Wjl | 补记 ADR(决策 2026-05-25 已随 9d37d03 落地,reflect 2026-W22-zentao 模式4 指出缺 ADR)|

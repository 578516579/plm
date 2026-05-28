# ADR-0012: 跨模块 SPI 模式 — ProjectScopedLookup + DoraAggregationSource

- **状态**:accepted
- **日期**:2026-05-28
- **决策人**:Wjl(技术 lead,solo-review)+ Claude
- **关联**:[proposal 0028 §3 P0-2A](../../99-跨阶段/proposals/0028-product-mainline-uplift-epic.md) / 实现 commit `21b7166` + `5f93f77` / 解决 [P0-1 known limitation](../../99-跨阶段/proposals/0028-product-mainline-uplift-epic.md#5-风险)

## 背景

P0-1 落地时(commit `3ae00fd`)发现 **`release ↔ pipeline` 互引外键的 Maven 循环依赖**:
- `Release.pipelineId` 需要在 `ReleaseServiceImpl` 校验 pipeline 存在且同 projectId → 需 `import PipelineMapper`
- `Pipeline.releaseId` 反向需要 `import ReleaseMapper`
- 两模块互 import → Maven 循环依赖 → BUILD FAILED

当时缓解措施:`pipeline_id` / `release_id` 只在 DDL + Domain + Mapper 落地,**应用层 FK 校验缺位**(known limitation,详 0028 §5)。

随后 P0-3B DORA 聚合(commit `5f93f77`)遇到**同款问题**:`plm-dora` 需聚合 pipeline / release / defect,但反向 import 这 3 模块同样有循环依赖。

需要一种**架构模式**统一解决"plm-dora / plm-release / plm-pipeline 等业务模块互相需要对方数据但不能反向 import"。

## 决策

引入 **SPI(Service Provider Interface)模式**,SPI 接口下沉 `plm-common`(最底层,谁都能依赖),业务模块实现 SPI 并 @Component 注册,需要方通过 `Map<String, SPI>` Spring 注入按 key 取实现。

### 3.1 ProjectScopedLookup SPI(P0-1 known limit 关闭)

```java
// plm-common/src/main/java/.../spi/ProjectScopedLookup.java
public interface ProjectScopedLookup {
  /** 给定 entity id 返回 projectId;不存在返回 null */
  Long lookupProjectId(Long id);
}
```

各模块实现:
```java
// plm-pipeline/src/main/java/.../spi/PipelineProjectScopedLookup.java
@Component("pipeline")  // bean name = entity 名
public class PipelineProjectScopedLookup implements ProjectScopedLookup {
  @Autowired private PipelineMapper mapper;
  public Long lookupProjectId(Long id) {
    Pipeline p = mapper.selectByPipelineId(id);
    return p == null ? null : p.getProjectId();
  }
}
```

使用方(release / pipeline 互验,不反向 import):
```java
// plm-release/.../ReleaseServiceImpl.java
@Autowired private Map<String, ProjectScopedLookup> lookups;  // Spring 注入所有 bean by name

private void validatePipelineSameProject(Long pipelineId, Long projectId) {
  ProjectScopedLookup pipelineLookup = lookups.get("pipeline");
  if (pipelineLookup == null) return; // SPI 未注册 → 优雅降级
  Long pipelineProjectId = pipelineLookup.lookupProjectId(pipelineId);
  if (pipelineProjectId != null && !pipelineProjectId.equals(projectId)) {
    throw new ServiceException("流水线项目不一致", 702);
  }
}
```

### 3.2 DoraAggregationSource SPI(P0-3B 跨模块聚合)

```java
// plm-common/src/main/java/.../spi/DoraAggregationSource.java
public interface DoraAggregationSource {
  /** 给定 projectId + 时间窗,返回模块的聚合贡献 */
  AggregationResult aggregate(Long projectId, LocalDate periodStart, LocalDate periodEnd);
}
```

各模块(`plm-pipeline` / `plm-release` / `plm-defect`)实现:
```java
@Component("pipeline")  // bean name = 聚合源类型
public class PipelineDoraAggregationSource implements DoraAggregationSource { ... }
```

`plm-dora` 注入 + 按 key 取:
```java
@Autowired private Map<String, DoraAggregationSource> sources;
sources.get("pipeline").aggregate(projectId, ps, pe); // deploy_freq + change_fail_rate
sources.get("release").aggregate(projectId, ps, pe);  // lead_time
sources.get("defect").aggregate(projectId, ps, pe);   // mttr
```

## 理由

- **SPI 下沉 `plm-common` 是单向依赖正确方向**:`plm-common` 不依赖任何业务模块;业务模块都依赖 `plm-common`;SPI 实现是业务模块的"出口",注入方是其他业务模块的"入口";双方都通过 `plm-common` 间接通信,**零循环依赖**
- **`Map<String, SPI>` 按 bean name 注入**:Spring 自动收集所有实现 + 按 `@Component("name")` 索引;调用方 `map.get("xxx")` 拿实现 — 简洁,无 ServiceLoader / SPI.load() 反射开销
- **`null` 返回 = SPI 未注册或 entity 不存在**:调用方一律检查 `if (impl == null) return;` 优雅降级 — 解耦不引启动时强依赖
- **bean name 用 entity 名(`@Component("pipeline")`/`("release")`/`("defect")`)**:语义明确;Spring DI 默认按 type 注入会冲突(多实现),按 name 解 + Map 注入解决
- **不引入 ServiceLoader / META-INF/services**:Spring 已经提供更轻量的 bean 集合注入,无需额外配置
- **不引入消息队列**:这是**同步查询**场景(校验/聚合),需要返回值;事件总线(ADR-0008)是异步通知场景,职责不同

## 后果

### 好

- **关闭 P0-1 known limitation**:release ↔ pipeline 应用层 FK 校验全部到位(commit `21b7166`)
- **DORA 4 指标真聚合落地**(commit `5f93f77`):零循环依赖
- **可复用范式**:未来跨模块查询(如"requirement → 关联的 task / defect / testcase 数量统计")可同款 SPI;集成 connector(zentao / jira)也可用此模式接入业务事件后的回查
- **测试友好**:Mock SPI 实现 ↔ 业务 ServiceImpl,无 import 真实 mapper

### 代价 / 风险

- **bean name 字面量耦合**:`map.get("pipeline")` 是字符串,无编译期检查;若 bean name 写错(typo / 重命名)运行时静默 null。**缓解**:在 plm-common SPI 接口里定义 `LookupKeys.PIPELINE = "pipeline"` 字符串常量;调用方一律走常量
- **SPI 接口稳定性**:接口签名变更需所有实现方同步;短期可加 default method 兼容
- **`Map<String, SPI>` 注入需 Spring 4.x+**(本项目 Spring Boot 4 已满足)
- **SPI 数量增长可能导致接口爆炸**:当前 2 个 SPI 控制良好;若超过 5 个建议合并 / 重新审视抽象边界(不要为每对模块都建独立 SPI)
- **无声明式注册**:`plm-common` 的 SPI 接口不知道有几个实现 — 启动时 grep 一下 META-INF 或 bean name 是验证手段

### 后续动作

- [ ] **P1**:plm-common 加 `LookupKeys` / `AggregationSourceKeys` 常量类,替换业务代码里的字符串字面量
- [ ] **P2**:proposal 0029 / connector 集成扩展时复用此 SPI 模式(应在 `~/.claude/skills/integration-connector/references/` 写一条范本)
- [ ] **P2**:plm-common 加 `@PostConstruct` 启动时 log 所有注册的 SPI 实现 bean name,便于运维检查
- [ ] **P2**:`Map<String, SPI>` 注入在 Spring Boot 4 + Lambda 配置类下偶有边界 — `~/.claude/skills/plm-bulk-refactor/references/` 加一条 SPI 集合注入示例

## 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl(会话授权)| ✅ accepted | 2026-05-28 | **经 Wjl 2026-05-28 会话内"1"指令批签**;ADR 配套 0028 Step 7d follow-up;0028 epic 唯一架构性决策,reviewer 评分卡称其亮点 — 同 commit `21b7166` 既做 P0-2A 又解 P0-1 known limit;`5f93f77` 同款 SPI 范式复用 |
| Claude(reviewer 复盘)| ✅ accepted | 2026-05-28 | proposal 0028 §10 P0-2A 落地证据充分;commit `21b7166` plm-common 30 / plm-release 20 / plm-pipeline 25 case 全绿;commit `5f93f77` DoraAggregationSource 同款复用 |

## 修订记录

| 日期 | 修改人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude / Wjl | V1.0 — 决策已随 `21b7166` + `5f93f77` 落地;Step 7d follow-up ADR 补记 |

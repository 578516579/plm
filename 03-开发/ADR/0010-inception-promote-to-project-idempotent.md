# ADR-0010: Inception → Project 幂等晋升语义

- **状态**:accepted
- **日期**:2026-05-28
- **决策人**:Wjl(技术 lead,solo-review)+ Claude
- **关联**:[proposal 0028 §3 P0-2B](../../99-跨阶段/proposals/0028-product-mainline-uplift-epic.md) / 实现 commit `21b7166` / PRD §F1.1→F1.2

## 背景

PM 验收报告(2026-05-28)5 主线诊断第 1 条:**立项 → 项目断裂**。`Inception.projectId` 字段早就存在,但全仓 `promoteToProject` / `createProjectFromInception` 0 命中 — 立项审批通过后必须手动到 project 模块再录一遍,违反 PRD §F1.1 → §F1.2 自动流转承诺。

业务挑战:
1. **幂等性**:同一 inception 重复点"晋升"按钮,不能重复建 project(会出 N 个 PRJ-YYYY-NNNN 重号项目)
2. **回填**:晋升后 inception.projectId 必须锁定,后续看板从这条主键查产物
3. **失败 rollback**:project 创建过程中失败,inception 不能被标"已晋升"

## 决策

`InceptionServiceImpl.promoteToProject(inceptionId)` 实现:

1. **幂等保护**(首屏检查):
   ```java
   Inception inception = mapper.selectById(inceptionId);
   if (inception.getProjectId() != null) {
     // 已晋升,返回已有 projectId,不再次建项
     return inception.getProjectId();
   }
   ```
2. **状态前置校验**:`inception.status` 必须 = `02`(审批通过),否则 → 错误码 `703 状态非法`
3. **建项 + 回写**(同一事务):
   ```java
   @Transactional(rollbackFor = Exception.class)
   public Long promoteToProject(Long inceptionId) {
     // 3a. createProject(inception → ProjectMapping)
     Project p = mapProjectFromInception(inception);
     projectMapper.insert(p);
     // 3b. 回写 projectId(关键:同一事务,保 rollback 一致性)
     inception.setProjectId(p.getProjectId());
     inception.setPromotedAt(LocalDateTime.now());
     mapper.updateById(inception);
     return p.getProjectId();
   }
   ```
4. **endpoint**:`POST /business/inception/{id}/promote-to-project` + `@PreAuthorize("@ss.hasPermi('business:inception:edit')")`

## 理由

- **幂等保护放首屏 + 同事务回写 = 两道防线**:首屏拦重复请求(O(1));同事务保 createProject 失败时 inception.projectId 不被错误设置
- **不引入分布式锁**:单实例进程,DB 行锁 + 业务幂等检查已足够;若未来多实例 + 高频晋升需求,可加 Redis 分布式锁(届时升级 ADR)
- **不做异步任务**:立项→项目是用户主动操作,UI 期望同步响应("点完按钮立刻跳到新 project 详情");异步反而劣化体验
- **projectId NULL allowed**:Inception 表 NULL 表"尚未晋升",NOT NULL 表"已晋升锁定" — 一字段二义,实现简洁

## 后果

### 好

- 1 个 endpoint + 1 个按钮(commit `21b7166` 前端 `inception/index.vue`)即解断点;符合 PRD §F1.1→F1.2 流转承诺
- 幂等可重复点击,前端无需禁用按钮(loading 即可),容错好
- 同事务保数据一致,无 inception 标晋升但 project 不存在的"幽灵"状态

### 代价 / 风险

- **inception 字段映射到 project 是有损的**:project 有但 inception 没有的字段(如 startAt/endAt 等)需要默认值或后续编辑补全 — 在 UI 上提示"晋升后请补全启动日期"
- **rollback 边界依赖事务**:若 createProject 走的 mapper.insert 没在 `@Transactional` 内(如其它 service 调用导致脱事务),inception.projectId 会被错误设置 — 单元测试覆盖 + 显式 `@Transactional(rollbackFor = Exception.class)`
- **审计追溯**:`promotedAt` 时间戳 + `inception.projectId` 反向锁,可追"何时由哪 inception 晋升来" — 但无法追"晋升过程中改了什么字段映射规则";若映射规则变更,旧数据不会自动重新映射

### 后续动作

- [ ] **P1**:requirement 详情页 5 跳页(prd/ued/arch/dbdesign/apidesign)做同款幂等晋升(规模更小,字段对应更清晰)
- [ ] **可选**:晋升时支持"复制 N 条相关 requirement / competitive 一并迁移"批量晋升(当前是 1→1 单条)
- [ ] **配套**:dashboard 主页加"待晋升 inception 数量"指标,提醒 PM 审批后及时晋升

## 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | _待签_ | _待定_ | solo-review;ADR 配套 0028 Step 7b follow-up |
| Claude(reviewer 复盘)| ✅ accepted | 2026-05-28 | proposal 0028 §10 P0-2 落地证据充分;commit `21b7166` 测试覆盖 inception 33 case 全绿 |

## 修订记录

| 日期 | 修改人 | 改了什么 |
|---|---|---|
| 2026-05-28 | Claude / Wjl | V1.0 — 决策已随 `21b7166` 落地;Step 7b follow-up ADR 补记 |

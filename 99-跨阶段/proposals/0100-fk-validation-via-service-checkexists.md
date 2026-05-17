# Proposal 0100: FK 校验统一走 Service.checkExists() 模式

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0100（首个编码规范类提案）|
| 标题 | 跨表 FK 校验在 ServiceImpl 必须走 `<TargetService>.checkExists()`，禁止直接 Mapper.selectByPrimaryKey() 绕过业务规则 |
| 状态 | **proposed** |
| 类型 | 编码规范 |
| 提出人 | Wjl + Claude（reflect/2026-W21 批量升格）|
| 提出日期 | 2026-05-17 |
| 来源 | signals 候选 **0022** |
| 评审截止 | 2026-05-24（编码规范 3 工作日内）|
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Requirement 模块 Phase 03 §J friction 2：FK 校验代码直接调 `projectMapper.selectByPrimaryKey(projectId)` 判断"项目存在"。看上去能 work，但绕过了 Project 的业务规则：

- Project Service 的 `getById()` 可能含 `delFlag = '0'` 软删过滤
- Project Service 可能含状态校验（如禁止给"已归档"项目加 Req）
- Project Service 未来加权限校验（按 Phase 06 cycle 反馈）时，Mapper 直读绕过权限

→ FK 关联只验"主键存在"是不够的，必须走业务方的"语义存在性"。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0022
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) B2 W21 批量升格
- 关联 Gate 实例: Requirement Phase 03 §J friction 2 + 后续 Sprint/Task/Defect/TestCase 模块均有同模式

---

## 3. 提案

### 改 `03-开发/开发规范.md` 加新章节 §X "FK 跨表校验规范"

```diff
+## §X. FK 跨表校验（强制，proposal 0100）
+
+ServiceImpl 中任何引用其他实体的 FK 字段（如 `projectId`, `sprintId`, `assigneeUserId`）必须：
+
+1. **不能直接调 Mapper.selectByPrimaryKey(otherId) 验证存在**
+2. **必须调对应 Service 的 checkExists(otherId) 方法**
+3. checkExists 由各 Service 实现，约定签名：
+   ```java
+   /** 校验业务存在性（含 delFlag / 状态机 / 权限）。失败时抛 ServiceException(code=702) */
+   void checkExists(Long id);
+   ```
+4. 若 checkExists 未实现，先在目标 Service 加占位实现（仅 delFlag 过滤），不允许 Mapper 直读规避
+
+错误码 702 已在 [PRD-MAPPING.md §4](../PRD-MAPPING.md) 登记，错误消息 "<entity> 不存在或已被删除"。
+
+### 反例
+
+```java
+// ❌ 反例：直接调 Mapper
+if (projectMapper.selectByPrimaryKey(req.getProjectId()) == null) {
+    throw new ServiceException("项目不存在", 702);
+}
+
+// ✅ 正例：调 Service
+projectService.checkExists(req.getProjectId());
+```
```

### 同步更新 `.claude/rules.md` §A 加 1 行

```diff
 - 权限串 **必须** 用 `business:<entity>:<action>`（list / query / add / edit / remove / export），不要混进 `system:*`。
+- **FK 跨表校验必须走 `<TargetService>.checkExists(id)`，禁用 Mapper.selectByPrimaryKey 直读** (proposal 0100)
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 已有 active 8 模块（plm-project/req/sprint/task/defect/testcase/document/inception）| 审计现有 FK 校验代码，违规处改 checkExists() 调用 |
| 业务模块生成器 `new-business-module.sh` | 默认产出的 ServiceImpl FK 校验模板已用 Service.checkExists（W22 验证） |
| Claude | rules.md 加载后默认按新规则产代码 |

---

## 5. 风险

- **风险 1**: 跨 Service 循环依赖（Service A 调 Service B.checkExists，B 又调 A 的）。**缓解**: checkExists 只读，无业务联动 → 不会回调原 Service。
- **风险 2**: checkExists 在大查询场景多次调用导致 N+1。**缓解**: 同事务内 `@Cacheable` 或显式批量 `checkExistsAll(Collection)`。

---

## 6. 备选方案

- A: 留 Mapper 直读但加注释 — 不选，难以审计
- B: AOP 切面强制注入 Service 校验 — 不选，黑魔法降低可读性
- C（选定）: 显式 Service.checkExists() 调用 + rules.md 硬约束

---

## 7. 实施计划

```
[x] Step 1: 写 proposal
[ ] Step 2: 评审（后端 lead solo-review）
[ ] Step 3: 改 03-开发/开发规范.md §X + .claude/rules.md §A
[ ] Step 4: 审计 8 active 模块 ServiceImpl FK 校验代码，违规处修
[ ] Step 5: 更新 new-business-module.sh 模板，默认产 Service.checkExists() 调用
[ ] Step 6: tracking 期看新 Service 是否 0 复发 Mapper 直读
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| ServiceImpl 中 `Mapper.selectByPrimaryKey` 直读模式调用数（grep 全仓库） | 当前 N（待 grep）| 0 |
| 新增 ServiceImpl 默认 checkExists 调用率（基于生成器）| 0% | 100% |

Tracking 期: merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| _(待，后端 lead)_ | | | |

---

## 10. 实施后跟踪

待 merged 后回填。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0022 升格（编码规范类首提案）|

# Inception 模块 — 功能用例库 (骨架,2026-05-17)

测试粒度:用户视角的"做什么 + 预期"。技术细节看 [Inception-api.md](Inception-api.md)。

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit e682574) |
| 关联测试计划 | [../Inception-测试计划-2026-05-17.md](../Inception-测试计划-2026-05-17.md) |
| 关联 PRD | [../../01-立项/Inception-PRD.md](../../01-立项/Inception-PRD.md) |
| E2E spec | [plm-frontend/e2e/inception.spec.ts](../../plm-frontend/e2e/inception.spec.ts) |

## 用例汇总

| 用例 ID | 标题 | 前置 | 步骤 | 预期 | 优先级 | 状态 |
|---|---|---|---|---|---|---|
| TC-Inception-F001 | 创建基础流程 | 已登录 admin | 1.进入Inception菜单 2.点新增 3.填必填项 4.保存 | 列表显示,编号自动生成 | P0 | 待执行 |
| TC-Inception-F002 | 必填字段校验 | 已登录 | 不填必填项创建 | 返 602 | P0 | 待执行 |
| TC-Inception-F003 | 字段白名单校验 | 已登录 | 传非字典值 | 返 604 | P1 | 待执行 |
| TC-Inception-F004 | 状态机合法转换 | 已有数据 | 按 §3 状态机推进 | 200 | P0 | 待执行 |
| TC-Inception-F005 | 状态机非法转换 | 已有数据 | 违反 §3 | 返 601 | P0 | 待执行 |
| TC-Inception-F006 | 终态保护 | 已到终态 | 尝试再推 | 返 601 | P1 | 待执行 |

## 通过率统计

首轮跑后回填。预期 P0 ≥ 95% / P1 ≥ 90% / P2 ≥ 80%。
- 总用例数: 6
- 通过: 0
- 失败: 0
- 阻塞: 0
- 未执行: 6

## 自动化对应

| 自动化层 | 覆盖用例 | 文件 |
|---|---|---|
| Service 单测 | TC-Inception-F002 (校验), F004/F005 (状态机) | `plm-inception/src/test/.../InceptionServiceImplTest.java` (如有) |
| E2E (Playwright) | TC-Inception-F001 (主流程), F004 (状态机推进) | `plm-frontend/e2e/inception.spec.ts` |

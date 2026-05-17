# Testreport 模块 — 功能用例库 (骨架,2026-05-17)

测试粒度:用户视角的"做什么 + 预期"。技术细节看 [Testreport-api.md](Testreport-api.md)。

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit e682574) |
| 关联测试计划 | [../Testreport-测试计划-2026-05-17.md](../Testreport-测试计划-2026-05-17.md) |
| 关联 PRD | [../../01-立项/Testreport-PRD.md](../../01-立项/Testreport-PRD.md) |
| E2E spec | [plm-frontend/e2e/testreport.spec.ts](../../plm-frontend/e2e/testreport.spec.ts) |

## 用例汇总

| 用例 ID | 标题 | 前置 | 步骤 | 预期 | 优先级 | 状态 |
|---|---|---|---|---|---|---|
| TC-Testreport-F001 | 创建基础流程 | 已登录 admin | 1.进入Testreport菜单 2.点新增 3.填必填项 4.保存 | 列表显示,编号自动生成 | P0 | <待人工补> |
| TC-Testreport-F002 | 必填字段校验 | 已登录 | 不填必填项创建 | 返 602 | P0 | <待人工补> |
| TC-Testreport-F003 | 字段白名单校验 | 已登录 | 传非字典值 | 返 604 | P1 | <待人工补> |
| TC-Testreport-F004 | 状态机合法转换 | 已有数据 | 按 §3 状态机推进 | 200 | P0 | <待人工补> |
| TC-Testreport-F005 | 状态机非法转换 | 已有数据 | 违反 §3 | 返 601 | P0 | <待人工补> |
| TC-Testreport-F006 | 终态保护 | 已到终态 | 尝试再推 | 返 601 | P1 | <待人工补> |

## 通过率统计

<待人工填写>:运行测试后更新

## 自动化对应

| 自动化层 | 覆盖用例 | 文件 |
|---|---|---|
| Service 单测 | <待补> | `plm-testreport/src/test/.../TestreportServiceImplTest.java` (如有) |
| E2E (Playwright) | <待补> | `plm-frontend/e2e/testreport.spec.ts` |

# ManualOps 模块 — 测试计划 (骨架,2026-05-17)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f) |
| 关联 PRD | [ManualOps-PRD.md](../01-立项/ManualOps-PRD.md) |
| 关联 API 设计 | [ManualOps-API设计.md](../02-设计/ManualOps-API设计.md) |
| E2E spec | [plm-frontend/e2e/manual-ops.spec.ts](../plm-frontend/e2e/manual-ops.spec.ts) |
| 测试经理 | Wjl (solo) |

## 1. 测试范围
- 单元测试 (Service 层): mvn -pl plm-manual-ops test
- E2E 测试 (前端 + 后端集成): npm run test:e2e -g "ManualOps"
- 状态机覆盖: 见 [PRD-MAPPING.md §3](../PRD-MAPPING.md) `manual-ops` 行,合法 + 非法转换全覆盖

## 2. 测试用例库
- [ManualOps-functional.md](测试用例库/ManualOps-functional.md) — 功能用例 (用户视角:做什么 + 预期),覆盖 CRUD 主流程 + 状态机推进 + 必填校验
- [ManualOps-api.md](测试用例库/ManualOps-api.md) — 接口契约用例 (HTTP code + JSON 字段),覆盖 6 标准 CRUD + 状态机/AI 等特殊端点 + 错误码 601/602/604/702
- [ManualOps-e2e.md](测试用例库/ManualOps-e2e.md) — Playwright 端到端,覆盖菜单访问 + 列表分页 + 新增编辑表单 + 角色权限 + 关键状态机

## 3. 通过标准
- mvn test 单测全绿
- E2E 套件相关 case 全绿
- 字段白名单 (604) + 状态机 (601) + FK (702) + 业务规则 (其他) 覆盖

## 4. 测试数据
fixtures 见 [plm-frontend/e2e/helpers/fixtures.ts](../plm-frontend/e2e/helpers/fixtures.ts) 或 fixtures-manual-ops.ts。

## 5. 风险

| 等级 | 风险 | 缓解 |
|---|---|---|
| P0 | 大文本内容性能 (单 ManualOps 文档 > 1MB 时编辑器卡顿 / 加载超时) | 分页加载 + 内容懒加载 + 后端流式返回 + 前端虚拟滚动 |
| P0 | 多人同时编辑导致版本冲突 (脏写或回滚) | 乐观锁 update_time + 服务端 version 校验 + 冲突 diff UI |
| P1 | 状态机非法转换 (草稿 → 已归档跳过审批) | Service 单测覆盖合法+非法 + transitTo() 白名单 + 返 601 |
| P1 | FK 级联导致孤儿引用 (如删除分类时关联文档未处理) | 软删 del_flag + E2E 覆盖删除场景 + DBA review FK 索引 |
| P2 | UI 兼容性 (富文本编辑器在 Edge 1366x768 渲染差异) | E2E 覆盖 Chrome+Edge + 视口断点 + 富文本 sanitize |

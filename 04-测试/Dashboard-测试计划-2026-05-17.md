# Dashboard 模块 — 测试计划 (骨架,2026-05-17)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f) |
| 关联 PRD | [Dashboard-PRD.md](../01-立项/Dashboard-PRD.md) |
| 关联 API 设计 | [Dashboard-API设计.md](../02-设计/Dashboard-API设计.md) |
| E2E spec | [plm-frontend/e2e/dashboard.spec.ts](../plm-frontend/e2e/dashboard.spec.ts) |
| 测试经理 | Wjl (solo) |

## 1. 测试范围
- 单元测试 (Service 层): mvn -pl plm-dashboard test
- E2E 测试 (前端 + 后端集成): npm run test:e2e -g "Dashboard"
- 状态机覆盖: 见 [PRD-MAPPING.md §3](../PRD-MAPPING.md) `dashboard` 行,合法 + 非法转换全覆盖

## 2. 测试用例库
- [Dashboard-functional.md](测试用例库/Dashboard-functional.md) — 功能用例 (用户视角:做什么 + 预期),覆盖 CRUD 主流程 + 状态机推进 + 必填校验
- [Dashboard-api.md](测试用例库/Dashboard-api.md) — 接口契约用例 (HTTP code + JSON 字段),覆盖 6 标准 CRUD + 状态机/AI 等特殊端点 + 错误码 601/602/604/702
- [Dashboard-e2e.md](测试用例库/Dashboard-e2e.md) — Playwright 端到端,覆盖菜单访问 + 列表分页 + 新增编辑表单 + 角色权限 + 关键状态机

## 3. 通过标准
- mvn test 单测全绿
- E2E 套件相关 case 全绿
- 字段白名单 (604) + 状态机 (601) + FK (702) + 业务规则 (其他) 覆盖

## 4. 测试数据
fixtures 见 [plm-frontend/e2e/helpers/fixtures.ts](../plm-frontend/e2e/helpers/fixtures.ts) 或 fixtures-dashboard.ts。

## 5. 风险

| 等级 | 风险 | 缓解 |
|---|---|---|
| P0 | 大数据量聚合查询慢 (单次 dashboard 加载 > 5s 影响用户体验) | 快照预计算 + Redis 缓存层 + 增量聚合 + EXPLAIN 关键 SQL |
| P0 | 图表渲染慢 (ECharts 数据点 > 10000 时浏览器卡顿) | 数据采样 / 降维 + 后端聚合后传输 + 虚拟滚动 + 图表懒加载 |
| P1 | 状态机非法转换 (snapshot 状态跳变导致脏读) | Service 单测覆盖合法+非法 + 快照只读约束 + 返 601 |
| P1 | FK 级联删除孤儿数据 (如删除 dashboard 时关联 widget 未处理) | 软删 del_flag + 删除二次确认 + E2E 覆盖删除场景 |
| P2 | UI 兼容性 (图表在 Chrome / Edge 不同视口渲染差异) | E2E 覆盖 Chrome+Edge + 视口 1366x768 + 图表缩放断点 |

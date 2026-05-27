# @plm/dashboard

| 字段 | 值 |
|---|---|
| 模块中文名 | 工作台 / 首屏聚合页 |
| 当前状态 | **PRD-aligned (v0.1)** |
| 后端对应 | `plm-dashboard` (Maven 模块,见 plm-backend) |
| PRD 参考 | [AgriAI-PLM PRD §4.2](../../../prd和原型/AgriAI-PLM-完整PRD文档.md) + 原型 `dashboard.html` |

## 视图

- `views/index.vue` — 首屏聚合页,渲染 6 类 widget:
  1. 欢迎区 + AI 立项入口
  2. AI 助手卡(textarea + 6 个快捷指令)
  3. 4 大顶部统计卡(在办项目/AI 生成文档/缺陷/自动化覆盖率)
  4. 在办项目进度卡
  5. 我的待办卡
  6. 17 阶段项目生命周期可视化 + 本迭代质量快照
  7. AI 改进建议

数据走 `GET /business/dashboard/aggregate`,本期返回 mock 值;下个迭代接入真实跨模块查询。

## API

- `aggregateDashboard(ownerUserId?)` — 聚合查询,返回 `DashboardAggregate`
- `listDashboard / getDashboard / addDashboard / updateDashboard / delDashboard` — 用户自定义工作台预设 CRUD(`tb_dashboard`)

## 路由

`/business/dashboard` → 主视图。菜单挂载 sys_menu id=2310-2315(`menu-seed-prd-aligned-modules.sql`)。

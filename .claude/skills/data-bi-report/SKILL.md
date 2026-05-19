---
name: data-bi-report
description: PLM BI 报表设计 — 报表布局 + ECharts 图表选型 + dashboard 端点 (composite view) + 钻取过滤. 用户说"BI / 报表 / dashboard / ECharts / 仪表盘 / 钻取 / 图表选型"时调用. 输出: 02-设计/<dashboard>-报表设计.md. **data-engineer agent 的子工具**。
---

# data-bi-report — BI 报表设计 skill v0.1

**data-engineer agent §2.3 配套**。PLM 用 ECharts (在 plm-frontend), dashboard 端点遵循 composite view ADR。

## 1. 何时调用
- "BI / 报表 / dashboard / ECharts / 图表"
- data-engineer §2.3 触发

## 2. 步骤

### 2.1 报表布局
- KPI 卡片区 (顶部, 1-5 个核心指标)
- 趋势图区 (中部, 折线 / 面积)
- 维度分布区 (柱 / 饼 / 雷达)
- 明细钻取表 (底部, ElTable)

### 2.2 ECharts 图表选型
- **趋势** → 折线 (multi-series) / 面积
- **构成** → 饼 (≤ 5 项) / 玫瑰 / 旭日 (层级)
- **比较** → 柱 (≤ 10 类) / 条 (类别多)
- **关系** → 散点 / 桑基 (流量)
- **地理** → 中国地图 (柱/热力)

### 2.3 dashboard 端点
- 单端点返 1 个 composite DTO, 避免 N 次调用
- 路径: `/dashboard/<scope>/<view>` (e.g. `/dashboard/sprint/overview`)
- 查询参数: `from / to / projectId / sprintId / aggBy`

### 2.4 钻取 / 过滤
- 顶层卡片点击 → 跳详情视图 + filter 透传
- 时间窗筛选: today / 7d / 30d / custom
- 钻取层级: 项目 → Sprint → Task

## 3. 输出模板
```markdown
# <dashboard> 报表设计

## 业务问题
## 布局 (mermaid 或 ASCII)
## ECharts 配置 (option JSON 示例)
## dashboard 端点 (URL + 查询参数 + DTO 结构)
## 钻取 / 过滤
```

## 4. 衔接
- 上游: data-model-design fact/dim 表
- 下游: frontend-coder Vue 组件 + backend-coder dashboard 端点

## 5. 历史
| v0.1 | 2026-05-19 | 首版; data-engineer 配套 3/4 |

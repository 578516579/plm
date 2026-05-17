# PRD: Dashboard 模块 — 工作台 (UI §4.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD UI §4.2 + 原型 dashboard.html) |
| 作者 | Wjl |
| PRD § | UI §4.2 (AgriAI-PLM-完整PRD文档.md UI §4.2 工作台预设) |
| 原型 HTML | [dashboard.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/dashboard.html) (widget grid + 默认工作台) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "Dashboard (UI §4.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(各角色登录后默认页千篇一律 / 6 类 widget 数据散在各模块 / 用户偏好布局无持久化 / 多角色 widget 配置成本高)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 UI §4.2 验收标准 + 模块特有衡量指标(默认工作台加载时间 / widget 类型覆盖)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **真实跨模块聚合** — 当前 mock,真实聚合留 v0.5+
- **拖拽布局编辑器** — 仅 layoutJson 存,可视化编辑器留 v0.3
- **多组织共享 widget** — 单 ownerUserId,共享留 v0.5+
- **widget 定制开发 SDK** — 仅 6 个固定类型,SDK 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:全角色 / PM / 管理者 / 开发 / 测试。

### 2.2 典型场景

**S1 用户登录默认工作台**(最高频)
<待人工填写>:1 段叙述,通过 (ownerUserId, isDefault='Y') 唯一约束保证默认工作台

**S2 6 类 widget 聚合**(高价值)
<待人工填写>:GET /business/dashboard/aggregate 返回 stats / activeProjects / myTodos / qualitySnapshot / lifecycle / aiMetrics

**S3 默认切换**(业务规则)
<待人工填写>:同用户切 isDefault='Y' 时自动清除其他默认

**S4 工作台停用**(终态)
<待人工填写>:01 停用,不影响其他默认

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Dashboard (UI §4.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: dashboardId / dashboardNo(DASH-YYYY-NNNN)
- 用户输入: title / ownerUserId(FK)/ widgetTypes(CSV)/ refreshInterval / isDefault
- 派生: layoutJson(前端写)
- 流程: status(2 态)

**唯一约束**: (ownerUserId, isDefault='Y') 同用户只允许一个默认

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) dashboard 行:`00↔01` (启用/停用)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 启用 | {01 停用} | 默认初始状态 |
| 01 | 停用 | {00 启用} | 反向边互转 |

**特殊规则**:
- 同 ownerUserId 只允许一个 isDefault='Y'(切默认自动清除其他)
- widgetTypes 6 个白名单(stats/active_projects/my_todos/quality_snapshot/lifecycle/ai_metrics)抛 604

---

## 5. AI 能力

### 5.1 AI 端点
(本模块当前阶段无 AI 端点。aiMetrics widget 透出来自 analytics 模块的 aiHoursSaved 指标。)

### 5.2 当前阶段实现
n/a — widget 聚合占位 mock,真实聚合留 v0.5+

### 5.3 mock 输出 / Dify 工作流
n/a

---

## 6. 验收标准

**UI §4.2 验收**:
- ⏳ 默认工作台加载时间 < 1s
- ⏳ 6 类 widget 全覆盖

**模块特有验收**:
<待人工填写>:E2E 测试 / 默认唯一约束单测 / widgetTypes CSV 白名单。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Dashboard-数据库设计.md](../02-设计/Dashboard-数据库设计.md)
- API 设计: [Dashboard-API设计.md](../02-设计/Dashboard-API设计.md)
- 测试计划: [Dashboard-测试计划-2026-05-17.md](../04-测试/Dashboard-测试计划-2026-05-17.md)
- 发布计划: [Dashboard-发布计划-2026-05-17.md](../05-上线/Dashboard-发布计划-2026-05-17.md)
- 原型: [dashboard.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/dashboard.html)
- AgriAI PRD: [UI §4.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)

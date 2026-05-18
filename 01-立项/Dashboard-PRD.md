# PRD: Dashboard 模块 — 工作台 (UI §4.2)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | UI §4.2(AgriAI-PLM-完整PRD文档.md UI §4.2 工作台) |
| 原型 HTML | [dashboard.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/dashboard.html) (6-widget 聚合 + 默认工作台 + 刷新间隔) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | 同用户 is_default 唯一约束 |
| 关联 OKR | _2026 Q2-O6-KR2: Dashboard 模块上线,工作台 6 widget 聚合响应 ≤ 500ms_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "Dashboard (UI §4.2)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 用户登录后首页当前是空白菜单首页,4 个具体问题:

1. **登录后无统一聚合视图**:用户登录后要逐个进 5-6 个菜单(项目 / 任务 / 缺陷 / Sprint / 报告)才能知道"今天的待办 / 项目健康度 / AI 指标",**平均切换菜单 8 次才形成上下文**。
2. **多用户工作台无个性化**:PM / 测试 / 开发 关心的指标不同,**但当前没"我的工作台"概念**,所有人看一样的菜单。
3. **6 widget 聚合源缺位**:原型 dashboard.html 提到 6 类 widget(统计 / 在办项目 / 我的待办 / 质量快照 / 生命周期 / AI 指标),**但没有统一聚合 API**,前端只能逐个 API 拉数据(N+6 请求)。
4. **默认工作台无统一定义**:不同用户首次登录该看什么 widget 缺少约定。

### 1.2 目标 (北极星指标)

**目标**:6 个月内为每个 PLM 用户提供个性化默认工作台,6 widget 聚合响应 ≤ 500ms。

**衡量指标**:
- **6 widget 聚合响应 ≤ 500ms**(GET /business/dashboard/aggregate 接口)
- **用户默认工作台覆盖率 100%**(每个用户必有 1 个 is_default='Y')
- **菜单切换次数降 50%**(基线 8 次 → 目标 4 次)
- **同用户 is_default 唯一约束 100%**(Service 切默认时自动清旧)
- **widget 启用 ≥ 4 项**(默认 6 项可裁)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **widget 拖拽自定义布局**(类 Grafana)— 仅 layoutJson 字段存,前端拖拽留 v0.3
- **widget 自定义类型**(用户自己写 widget 组件)— 仅 6 个内置类型,自定义留 v0.5+
- **多工作台切换**(快速从 "PM 工作台" 切到 "QA 工作台")— 仅 is_default 单选,多工作台留 v0.3
- **工作台分享给其他用户** — 仅 ownerUserId 私有,分享留 v0.5+
- **工作台数据导出 / 截图** — 留 v0.3
- **跨项目工作台聚合**(同时看 5 个项目)— 仅单项目 / 全局二选一,跨项目留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **PM** | CRUD 自己的 Dashboard | 创建 + 选 widget + 设默认 |
| **开发 / 测试 / SRE** | CRUD 自己的 Dashboard | 个性化按岗位选 widget |
| **管理员** | 全 CRUD + 跨用户查看 | 维护 widget 类型字典 |

### 2.2 典型场景

**S1 用户首次登录,系统自动建默认工作台**(最高频,新用户引导)
> 新用户首次登录 → 后端检测 tb_dashboard 无 (owner=user, is_default='Y') 记录 → 自动 INSERT 1 条 → title="<用户名> 默认工作台" + widgetTypes="stats,active_projects,my_todos,quality_snapshot,lifecycle,ai_metrics"(6 项全启)+ isDefault='Y' + refreshInterval=60 + status='00 启用'

**S2 聚合查询**(最高频,首页加载)
> 用户登录后访问首页 → GET /business/dashboard/aggregate?ownerUserId=<uid> → 后端 6 子查询并行:
> 1. stats(项目总数 / 任务总数 / 缺陷总数)
> 2. activeProjects(in-progress 项目列表)
> 3. myTodos(我的待办 task 列表)
> 4. qualitySnapshot(缺陷密度 / 测试通过率 / 覆盖率)
> 5. lifecycle(6 阶段进度图)
> 6. aiMetrics(AI 节省工时 / Agent 调用数)
> 响应 ≤ 500ms

**S3 PM 个性化工作台**(关键流程)
> PM 觉得 "ai_metrics" 不重要,自定义 widgetTypes 改为 5 项 → 保存 → 下次登录看到 5 widget

**S4 切默认工作台**(关键流程 + 业务硬规则)
> PM 创建第 2 个工作台 + 设 isDefault='Y' → Service 自动检测同 owner 已有 default → 调用 `clearDefaultForOwner()` 把旧默认改 'N' → **保证同 user 只有 1 个 default**

**S5 工作台停用**(终态)
> 用户不再需要某非默认工作台 → status='00→01 停用' → 列表过滤

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "Dashboard (UI §4.2)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: dashboardId / dashboardNo (`DASH-YYYY-NNNN`) / ownerUserId(FK 必)
- 布局: layoutJson(widget grid 布局)
- widget 选项: widgetTypes(CSV 6 值字典)
- 配置: refreshInterval(秒,默认 60)/ isDefault(Y/N)
- 流程: status(2 态启用/停用)

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) dashboard 行:2 态启用/停用。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 启用 | {01 停用} | 默认初始 |
| 01 | 停用 | {00 启用} | 反向可重启 |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- widgetTypes CSV 字典白名单(每个值在 stats/active_projects/my_todos/quality_snapshot/lifecycle/ai_metrics,604)
- **业务硬规则**:同 ownerUserId 下 isDefault='Y' 唯一(Service `clearDefaultForOwner` 自动维护)
- FK 校验:ownerUserId 必,sys_user.userId 存在(702)
- refreshInterval 范围 [10, 3600] 秒(602)

---

## 5. AI 能力

### 5.1 AI 端点

**Dashboard 模块本期无 AI 端点**。aggregate 接口是纯 SQL 聚合。

### 5.2 当前实现

- GET /business/dashboard/aggregate?ownerUserId={uid} → 返回 6 类 widget 数据
- 本期 6 子查询为 mock 数据(后续接真实跨模块聚合)

### 5.3 路线图

- v0.3: 真实跨模块 SQL 聚合(从 project/task/defect/sprint/testreport/aiagent 聚合)
- v0.3: 多工作台切换 / 拖拽布局
- v0.5+: widget 自定义 / 工作台分享

---

## 6. 验收标准

**UI §4.2 验收**:
- ⏳ **6 widget 聚合**(stats / active_projects / my_todos / quality_snapshot / lifecycle / ai_metrics)
- ⏳ **聚合响应 ≤ 500ms**(本期 mock 即时返回)
- ⏳ **默认工作台个性化**(本期 isDefault + clearDefaultForOwner 业务硬规则)

**模块特有验收**(本会话已落地):
- 2 态状态机合法转换(启用 ↔ 停用)单测覆盖
- widgetTypes CSV 字典白名单 6 值(604)
- 同 ownerUserId 下 isDefault='Y' 唯一硬规则单测覆盖
- FK 校验:ownerUserId 必(702)
- aggregate 接口 mock 6 类 widget 返回

---

## 7. 不做的事 — 详 §1.3

- 拖拽自定义 / 自定义 widget / 多工作台切换 / 分享 / 导出 / 跨项目聚合

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [Dashboard-数据库设计.md](../02-设计/Dashboard-数据库设计.md)
- API 设计: [Dashboard-API设计.md](../02-设计/Dashboard-API设计.md)
- 测试计划: [Dashboard-测试计划-2026-05-17.md](../04-测试/Dashboard-测试计划-2026-05-17.md)
- 发布计划: [Dashboard-发布计划-2026-05-17.md](../05-上线/Dashboard-发布计划-2026-05-17.md)
- 原型: [dashboard.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/dashboard.html)
- AgriAI PRD: [UI §4.2](../prd和原型/AgriAI-PLM-完整PRD文档.md)
- 关联模块: [Analytics-PRD.md](Analytics-PRD.md)(qualitySnapshot widget 数据源)/ [Project-PRD.md](Project-PRD.md)(activeProjects widget)/ [Task-PRD.md](Task-PRD.md)(myTodos widget)

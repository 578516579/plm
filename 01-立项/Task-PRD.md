# PRD: Task(开发任务)— PLM v0.2 第 2 个模块

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 作者 | Wjl |
| 最近更新 | 2026-05-16 |
| 评审状态 | pending(待 Phase 01 Gate 评审) |
| 关联 OKR | _2026 Q2-O1-KR1.2: PLM v0.2 上线需求/任务/迭代三件套_ |
| 父需求(可选) | Requirement (`tb_requirement`, v0.2 同期立项) |
| 父迭代(可选) | Sprint (`tb_sprint`, v0.2 同期立项) |

---

## 1. 背景与目标

### 1.1 现状痛点

Requirement 解决"做什么",但**"具体哪个人做、做到第几步、和代码 MR 怎么挂的"** 还是散在飞书表格 + 各种 Git 仓库的 PR 标题里:

1. **任务粒度对不上代码**: 一个 Requirement 拆成几个 Task 才能交付,飞书表里只能写"REQ-89 - 王工 - 进行中",但实际写了几个 MR、改了哪些文件,需要去 GitLab 翻历史。
2. **看板不在 PLM 里**: 当前没有"5 列看板"(待开发 → 开发中 → 代码评审 → 测试中 → 已完成)的可视化,周会全靠飞书表格筛选。
3. **工时估算无据可查**: 估了 8 小时实际做了 15 小时这种偏差,事后没法回溯到具体任务做 root-cause。

### 1.2 目标(北极星指标)

**目标**: Task 实体 + 5 列看板成为团队**唯一的代码工作流可视化**,所有 MR 都能反向找到 Task。

**衡量指标**:
- 月活任务数 ≥ 100(活跃定义: 当月有状态变化的任务)
- 任务-需求关联率 ≥ 80%(剩 20% 允许是"运维/打杂"无明确需求的任务)
- MR 反向关联率 ≥ 90%(每个合并的 MR 在 PLM 任务里都能找到记录)
- 工时估算偏差 ≤ 30%(实际工时 / 估算工时 区间在 [0.7, 1.3])

### 1.3 不做的事(Out of Scope)

本期**不做**:
- 5 列看板的**前端拖拽实现** — 推到 v0.3+,本期只支持下拉框改状态
- AI 拆分任务(`task-split-flow`)— AgriPLM Dify 工作流剥离,人工拆
- MR 关联的**自动同步**(从 GitLab webhook 主动推) — 推到 v0.5+ MCP,本期手动填 MR 号
- 评论 / 操作日志(`tdm-comments`/`tdm-history`)— v0.3
- 燃尽图 — v0.4 (合并到 Sprint 的报表里做)
- 多人任务 / 子任务嵌套 — 永不做,保持单负责人 + 平铺

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **PM** | CRUD 项目下所有任务 | 拆任务、指派、调状态 |
| **开发** | 改自己被指派的任务 | 接任务、推进状态、填实际工时 |
| **admin** | 全 CRUD | 全局调整 |

### 2.2 典型场景

**S1 PM 拆任务**(最高频)
> PM 在 Requirement REQ-2026-0042 详情页看到"客户要求列表导出 Excel" → 拆 3 个 Task:`后端开导出接口`/`前端调按钮`/`测试用例补充` → 各填标题/负责人/估算工时/优先级 → 三个 Task 自动挂到 REQ-2026-0042 + Sprint-26W20。

**S2 开发推进状态**
> 王工进任务列表 → 筛"我的任务" → 找到 TASK-2026-0123 → 改状态 `待开发`→`开发中` → 系统记修改人 + 时间。

**S3 看板视图(简化版)**
> 任务列表页面提供"看板模式"切换,显示 5 列(待开发/开发中/代码评审/测试中/已完成),每列下显示该状态的任务卡。**不支持拖拽,只读视图**,推进状态走详情页下拉框。

**S4 关联 MR**
> 王工把 GitLab 的 MR `!482` 关联到 TASK-2026-0123 → 系统记 MR 信息(分支名 / 状态 / 创建人) → 在任务详情显示。

---

## 3. 功能需求

### 3.1 字段定义 (`tb_task`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| task_id | bigint AUTO_INCREMENT PK | ✅ | — | 主键 |
| task_no | varchar(32) UNIQUE | ✅ | 自动生成 | `TASK-YYYY-NNNN`(ADR-0001 模式) |
| project_id | bigint NOT NULL | ✅ | — | FK→tb_project |
| requirement_id | bigint | | NULL | FK→tb_requirement(可空,允许"无关联需求"的运维/打杂任务) |
| sprint_id | bigint | | NULL | FK→tb_sprint(可空,允许"backlog 未排期" 的任务) |
| title | varchar(200) NOT NULL | ✅ | — | 任务标题 |
| description | text | | NULL | 详细描述,Markdown 兼容 |
| status | varchar(2) NOT NULL | ✅ | '00' | 字典 `biz_task_status`: 00待开发/01开发中/02代码评审/03测试中/04已完成/05已取消 |
| priority | varchar(2) NOT NULL | ✅ | '02' | 字典 `biz_task_priority`: 00=P0/01=P1/02=P2 |
| assignee_user_id | bigint | | NULL | FK→sys_user(负责人) |
| estimated_hours | decimal(5,1) | | NULL | 预估工时(小时) |
| actual_hours | decimal(5,1) | | NULL | 实际工时(状态进入"已完成"时填) |
| mr_url | varchar(500) | | NULL | 关联 MR/PR 链接(单个,多 MR 留 v0.3 做关联表) |
| mr_branch | varchar(100) | | NULL | MR 分支名 |
| **+ 6 通用字段** | (create_by/create_time/update_by/update_time/remark/del_flag) | | | BaseEntity |

**索引**:
- `uk_task_no` (task_no)
- `idx_project_id` (project_id)
- `idx_requirement_id` (requirement_id)
- `idx_sprint_id` (sprint_id)
- `idx_assignee_user_id` (assignee_user_id) — 查"我的任务"
- `idx_status_priority` (status, priority) — 看板查询

### 3.2 功能清单

| 编号 | 名称 | 优先级 | 验收标准 |
|---|---|---|---|
| **T-001** | 列表查询(项目/需求/迭代/状态/优先级/负责人六维筛选) | P0 | URL `/business/task/list?projectId=&requirementId=&sprintId=&status=&priority=&assigneeUserId=` 分页;响应 < 500ms |
| **T-002** | 新增任务 | P0 | task_no 自动 `TASK-YYYY-NNNN`;requirement_id/sprint_id 校验存在(若填) |
| **T-003** | 修改任务(基本信息) | P0 | 必填校验;返回错误码 602/604 |
| **T-004** | 状态推进(6 状态机) | P0 | 6×6 转换矩阵 + 终态保护(`已完成`/`已取消` 不可退回);违规返回 601 |
| **T-005** | 删除任务(逻辑) | P1 | del_flag = '2' |
| **T-006** | 看板视图(只读) | P1 | 5 列 (待开发/开发中/代码评审/测试中/已完成) 聚合查询;`已取消` 不在看板显示 |
| **T-007** | 我的任务 | P0 | `GET /business/task/my` 自动按 SecurityUtils.getUserId() 过滤 |
| **T-008** | 关联 MR | P1 | mr_url / mr_branch 字段更新;URL 格式校验(http(s)://) |
| **T-009** | 导出 Excel | P1 | 字段全列;UTF-8 with BOM |
| **T-010** | 9 sys_menu + admin 角色全量授权 | P0 | 业务管理 → 任务管理 二级菜单 + 8 按钮权限(含"看板视图"权限) |

### 3.3 状态机 6×6 转换矩阵

```
              待开发  开发中  代码评审  测试中  已完成  已取消
待开发(00)     —      ✅      ❌       ❌      ❌      ✅
开发中(01)    ✅      —      ✅       ❌      ❌      ✅
代码评审(02)  ❌      ✅      —        ✅      ❌      ✅
测试中(03)    ❌      ❌      ✅       —       ✅      ✅
已完成(04)    ❌      ❌      ❌       ❌      —       ❌  (终态保护)
已取消(05)    ❌      ❌      ❌       ❌      ❌      —   (终态保护)
```

**关键转换说明**:
- `代码评审` → `开发中` 允许(评审打回需求改代码)
- `测试中` → `代码评审` 允许(测试发现要再改代码)
- `已完成`/`已取消` 是终态保护(沿用 Project §3.3 / Requirement §3.3 规约)

### 3.4 错误码

| 错误码 | 含义 | 触发场景 |
|---|---|---|
| 601 | 状态转换违规 | 终态被改 / 非法跳转 |
| 602 | 必填字段缺失 | title/project_id 空 |
| 604 | 参数错误 | status/priority/mr_url 格式不合法 |
| 701 | task_no 已存在 | INSERT 唯一约束冲突 |
| 702 | 关联项目/需求/迭代不存在 | FK 校验失败 |

---

## 4. 非功能需求

| 维度 | 要求 |
|---|---|
| 性能 | 列表 1k 行查询 < 500ms;看板视图 < 800ms(聚合查询) |
| 可用性 | 同 Project |
| 安全 | 8 个权限点 `business:task:*` |
| 审计 | 状态推进 + MR 关联 记 OPERATE 日志 |
| 数据完整性 | FK 强制约束;mr_url 字段长度限制 500 |
| 兼容性 | 与 Project / Requirement 完全兼容 |

---

## 5. 依赖与约束

**依赖**:
- `tb_project` (已 ready)
- `tb_requirement` (v0.2 同期立项,Phase 02 设计联调约束)
- `tb_sprint` (v0.2 同期立项)
- `sys_user` / `sys_dict_data` / `sys_menu`

**约束**:
- 任务编号 `TASK-YYYY-NNNN`(同 ADR-0001 模式)
- 不引入新外部依赖

---

## 6. ADR-0003 (草案): TASK 编号规则

沿用 ADR-0001 模式,只是前缀 `TASK-`,年内序号空间独立。

---

## 7. 评审计划

- **评审日期**: 2026-05-16
- **评审人**: Wjl `[solo-review]`
- **预期结论**: unconditional pass

---

## 修订记录

| 日期 | 版本 | 修改人 | 变更 |
|---|---|---|---|
| 2026-05-16 | v1.0 | Wjl | 首次创建,基于 AgriPLM PRD §3.3.1 Kanban 通用化(去 AI 拆分/MCP 同步) |

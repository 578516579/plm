# PRD: Sprint(迭代)— PLM v0.2 第 3 个模块

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 作者 | Wjl |
| 最近更新 | 2026-05-16 |
| 评审状态 | pending(待 Phase 01 Gate 评审) |
| 关联 OKR | _2026 Q2-O1-KR1.2: PLM v0.2 上线需求/任务/迭代三件套_ |
| 父项目 | Project (`tb_project`, v0.1.0 已上线) |

---

## 1. 背景与目标

### 1.1 现状痛点

Project / Requirement / Task 三个实体都能挂上 Sprint,但**当前没有"时间盒"概念**:

1. **没有"本迭代"概念**: 看任务列表只能按状态筛,但"这个迭代要交付的全部任务"看不到。
2. **迭代节奏不可视**: 进行中 vs 已完成 vs 计划中,没法一眼区分;每个迭代何时开始何时结束没记录。
3. **复盘无数据**: 已完成的迭代没有"实际完成 vs 计划"对比,无法形成可量化的迭代健康度指标。

### 1.2 目标(北极星指标)

**目标**: Sprint 作为 PM 团队的**统一时间盒**,所有 Task 必须挂到一个 Sprint 才能进入"开发中"状态(backlog 例外)。

**衡量指标**:
- 迭代准时完成率 ≥ 75%(实际结束日 ≤ 计划结束日 + 2 天)
- 任务挂载率 ≥ 80%(每个"开发中"状态的 Task 都挂到了 Sprint)
- 迭代健康度评分 ≥ 70%(完成任务数 / 计划任务数,Phase 06 运营时统计)

### 1.3 不做的事(Out of Scope)

本期**不做**:
- 燃尽图 — v0.4
- 速度图(Velocity Chart)— v0.4
- AI 拆分任务 / AI 推荐迭代目标 — 永不做(剥离 AgriPLM Dify 工作流)
- Sprint 模板复制 — v0.3+
- 多团队 / 多 Sprint 并行 — 永不做,简化为"项目级"单 Sprint
- 工时燃尽 — v0.4

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **PM** | CRUD 项目下的迭代 | 创建迭代、设目标、开/结迭代 |
| **开发** | 查看自己参与的迭代 | 看迭代里"我的任务"清单 |
| **admin** | 全 CRUD | 全局调整 |

### 2.2 典型场景

**S1 创建迭代**(最高频)
> 周一晨会 → PM 进 Project 详情 → 新建迭代 Sprint 26W21 → 填名称/开始日期=2026-05-19/周期=14 天/目标="导出功能上线" → 状态默认"计划中" → 保存。

**S2 开始迭代**
> 周一开始 → PM 把 Sprint 26W21 状态从"计划中"改为"进行中" → 系统记录 actual_start_date = 2026-05-19。

**S3 关联任务到迭代**
> Task 详情页 → 选 Sprint = Sprint 26W21 → 保存 → 任务自动出现在该 Sprint 的任务清单。

**S4 结束迭代 + 复盘数据**
> 迭代最后一天 → PM 把状态改为"已完成" → actual_end_date 自动填当前日期 → 系统统计该 Sprint 下所有 Task 的完成数 / 计划数 / 准时率,作为下次复盘的输入。

---

## 3. 功能需求

### 3.1 字段定义 (`tb_sprint`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| sprint_id | bigint AUTO_INCREMENT PK | ✅ | — | 主键 |
| sprint_no | varchar(32) UNIQUE | ✅ | 自动生成 | `SPR-YYYY-NNNN`(ADR-0001 模式) |
| project_id | bigint NOT NULL | ✅ | — | FK→tb_project (Sprint 隶属项目) |
| name | varchar(100) NOT NULL | ✅ | — | 迭代名称(如 "Sprint 26W21") |
| goal | varchar(500) | | NULL | 迭代目标(一句话) |
| status | varchar(2) NOT NULL | ✅ | '00' | 字典 `biz_sprint_status`: 00计划中/01进行中/02已完成/03已取消 |
| planned_start_date | date NOT NULL | ✅ | — | 计划开始日 |
| planned_end_date | date NOT NULL | ✅ | — | 计划结束日(默认 +14 天) |
| actual_start_date | date | | NULL | 实际开始(状态转"进行中"时自动填) |
| actual_end_date | date | | NULL | 实际结束(状态转"已完成"时自动填) |
| duration_days | int | | 14 | 周期天数(冗余字段,便于查询) |
| **+ 6 通用字段** | (create_by/create_time/update_by/update_time/remark/del_flag) | | | BaseEntity |

**索引**:
- `uk_sprint_no` (sprint_no)
- `idx_project_status` (project_id, status) — 查"项目下的活跃迭代"最高频
- `idx_planned_dates` (planned_start_date, planned_end_date) — 时间范围筛选

### 3.2 功能清单

| 编号 | 名称 | 优先级 | 验收标准 |
|---|---|---|---|
| **S-001** | 列表查询(项目/状态/日期范围三维筛选) | P0 | URL `/business/sprint/list?projectId=&status=&startDate=&endDate=` 分页返回;响应 < 500ms |
| **S-002** | 新增迭代 | P0 | sprint_no 自动 `SPR-YYYY-NNNN`;planned_end_date 默认 = planned_start_date + duration_days |
| **S-003** | 修改迭代 | P0 | 必填校验;返回错误码 602/604 |
| **S-004** | 状态推进(4 状态机) | P0 | 4×4 转换矩阵 + 终态保护;状态转换时**自动填充 actual_start_date / actual_end_date** |
| **S-005** | 删除迭代(逻辑) | P1 | del_flag = '2';**有关联 Task 时 P0 阻断**,提示"先解除关联或迁移任务" |
| **S-006** | Sprint 详情 + 关联任务清单 | P0 | `GET /business/sprint/{sprintId}` 返回 Sprint 基本信息 + 该 Sprint 下所有 Task 列表(复用 Task §T-001 接口) |
| **S-007** | 项目详情页的"迭代管理 Tab" | P1 | 在 Project 前端嵌入,后端复用 S-001 接口 + `projectId` 参数 |
| **S-008** | 当前活跃迭代(项目级) | P0 | `GET /business/sprint/current?projectId=` 返回该项目状态=`进行中`的迭代(最多 1 个) |
| **S-009** | 迭代健康度统计(基础) | P1 | `GET /business/sprint/{id}/stats` 返回 {planned_task_count, completed_task_count, complete_rate, on_time} |
| **S-010** | 7 sys_menu + admin 角色全量授权 | P0 | 业务管理 → 迭代管理 二级菜单 + 6 按钮权限 |

### 3.3 状态机 4×4 转换矩阵

```
                计划中  进行中  已完成  已取消
计划中(00)       —      ✅      ❌      ✅
进行中(01)      ❌      —       ✅      ✅
已完成(02)      ❌      ❌      —       ❌  (终态保护)
已取消(03)      ❌      ❌      ❌      —   (终态保护)
```

**关键约束**:
- `计划中` → `进行中`: 自动写入 `actual_start_date = TODAY()`
- `进行中` → `已完成`: 自动写入 `actual_end_date = TODAY()`
- **项目级单一活跃 Sprint 约束**: 同一 project_id 下,状态=`进行中` 的 Sprint 数量必须 ≤ 1;违反返回错误码 703

### 3.4 错误码

| 错误码 | 含义 | 触发场景 |
|---|---|---|
| 601 | 状态转换违规 | 终态被改 / 非法跳转 |
| 602 | 必填字段缺失 | name/project_id/planned_dates 任一为空 |
| 604 | 参数错误 | 日期格式错 / planned_end_date 早于 planned_start_date |
| 701 | sprint_no 已存在 | INSERT 唯一约束冲突 |
| 702 | 关联项目不存在 | FK 校验失败 |
| **703** | **项目已有活跃迭代** | 同 project_id 下已存在 status='01' 进行中的 Sprint,不允许再开新的 |
| 704 | 迭代有关联任务,不可删除 | S-005 阻断条件 |

---

## 4. 非功能需求

| 维度 | 要求 |
|---|---|
| 性能 | 列表 < 500ms;统计 §S-009 < 1s(基础聚合) |
| 可用性 | 同 Project |
| 安全 | 6 权限点 `business:sprint:*` |
| 审计 | 状态推进 + 删除 记 OPERATE 日志 |
| 数据完整性 | FK 强制约束;**S-005 删除前检查关联 Task** |
| 兼容性 | 与 Project / Requirement / Task 完全兼容 |

---

## 5. 依赖与约束

**依赖**:
- `tb_project` (已 ready)
- `tb_task` (v0.2 同期立项,S-005 删除前检查依赖、S-009 统计依赖)
- `sys_user` / `sys_dict_data` / `sys_menu`

**约束**:
- 迭代编号 `SPR-YYYY-NNNN`
- **项目级单一活跃迭代约束**(§3.3,错误码 703)
- 不引入新外部依赖

---

## 6. ADR-0004 (草案): SPR 编号规则

沿用 ADR-0001 模式,前缀 `SPR-`,年内序号空间独立。

---

## 7. 评审计划

- **评审日期**: 2026-05-16
- **评审人**: Wjl `[solo-review]`
- **预期结论**: unconditional pass

---

## 修订记录

| 日期 | 版本 | 修改人 | 变更 |
|---|---|---|---|
| 2026-05-16 | v1.0 | Wjl | 首次创建,基于 AgriPLM PRD §3.3.1 Kanban modal-sprint 通用化 |

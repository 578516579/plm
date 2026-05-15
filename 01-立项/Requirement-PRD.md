# PRD: Requirement(需求)— PLM v0.2 第 1 个模块

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

Project v0.1.0 上线后,项目 (`tb_project`) 实体已就位,但**项目下的具体"做什么"还在飞书文档里散着**:

1. **需求和项目脱节**: 客户反馈 / 内部提案 / 运营数据 这三类需求源头记在飞书文档,跟 PLM 里的项目挂不上关系,统计"X 项目下今年所有需求"做不了。
2. **优先级靠记忆**: 每周复盘时"哪些需求是 P0 哪些是 P1"全靠拍脑袋,没有可追溯的依据。
3. **状态不可见**: 一个需求是"待评审" / "开发中" / "已完成" / "已取消" 没法在 PLM 里直接看,只能去问对应的 PM。

### 1.2 目标(北极星指标)

**目标**: 让本 PLM 平台成为"团队所有需求的唯一可信源",对接到 Project 实体上,后续 Task / Sprint / 测试用例都能向上引用 Requirement。

**衡量指标**:
- 月活需求数 ≥ 50(活跃定义: 当月状态变化或新增的需求)
- 需求-项目关联率 = 100%(每个需求必须挂到某个 Project)
- 优先级标注覆盖率 ≥ 95%(P0/P1/P2 三选一不能空)
- 评审完成率 ≥ 80%(`待评审` → `开发中`/`已取消` 状态推进闭环)

### 1.3 不做的事(Out of Scope)

本期**不做**:
- AI 价值评估 / AI 优先级分析 — AgriPLM 的 Dify 工作流不引入,人工填
- 追踪矩阵可视化(Requirement ↔ Task ↔ TestCase 图谱)— v0.3+
- 评论功能 — v0.2.1+
- 需求变更审批流(走 Spring Workflow / Activiti)— 永不做,改用"评审纪要文件 + 状态机"轻量化
- 多版本对比 / 历史回溯图 — v0.3+
- 客户反馈集成(从 Zendesk/工单系统拉) — 永不做

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **PM(产品经理)** | CRUD 自己项目下的需求 | 录入需求、改优先级、推进状态 |
| **admin** | 全 CRUD | 全局调整 |
| **开发** | 查看 + 评论(v0.2.1) | 看到需求详情、提反馈 |

### 2.2 典型场景

**S1 客户反馈录入需求**(最高频)
> 客户提"列表导出 Excel 太慢" → PM 在 PLM 选项目 → 新增需求 → 填标题/详细描述/来源=客户反馈/优先级=P1/关联项目 → 状态默认"待评审" → 提交。

**S2 评审决策**
> PM 周会评审 → 在需求详情页改状态 `待评审`→`开发中` 或 `待评审`→`已取消` → 系统记录修改人 + 时间 + 评审纪要文件链接(可选)。

**S3 项目下的需求列表**
> 总监问"客户 X 定制项目这个月做了哪些需求?" → 进 Project 详情 → 关联需求 Tab → 列表显示所有挂到这个 Project 的需求,按优先级 / 状态分组。

**S4 全局优先级看板**
> 周一晨会 → 进需求列表 → 筛选 状态=`待评审` + 优先级=P0/P1 → 一屏决策。

---

## 3. 功能需求

### 3.1 字段定义 (`tb_requirement`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| requirement_id | bigint AUTO_INCREMENT PK | ✅ | — | 主键 |
| requirement_no | varchar(32) UNIQUE | ✅ | 自动生成 | 编号 `REQ-YYYY-NNNN` (规则同 Project) |
| project_id | bigint NOT NULL | ✅ | — | FK→tb_project.project_id |
| title | varchar(200) NOT NULL | ✅ | — | 需求标题(最长 200 字符) |
| description | text | ⚠️ | NULL | 详细描述,Markdown 兼容 |
| source | varchar(2) NOT NULL | ✅ | '01' | 字典 `biz_req_source`: 01客户反馈/02内部提案/03运营数据/04竞品分析 |
| priority | varchar(2) NOT NULL | ✅ | '02' | 字典 `biz_req_priority`: 00=P0紧急/01=P1重要/02=P2一般 |
| status | varchar(2) NOT NULL | ✅ | '00' | 字典 `biz_req_status`: 00待评审/01开发中/02已完成/03已取消 |
| assignee_user_id | bigint | | NULL | FK→sys_user.user_id(指派给的开发) |
| review_note | varchar(500) | | NULL | 评审简要纪要(状态推进时填) |
| **+ 6 个通用字段** | (create_by/create_time/update_by/update_time/remark/del_flag) | | | 同 RuoYi BaseEntity |

**索引**:
- `uk_requirement_no` (requirement_no)
- `idx_project_id` (project_id) — 查"项目下所有需求"
- `idx_status` (status) — 看板筛选
- `idx_priority_status` (priority, status) — 高优先级看板

### 3.2 功能清单

| 编号 | 名称 | 优先级 | 验收标准 |
|---|---|---|---|
| **R-001** | 列表查询(项目/状态/优先级/来源四维筛选) | P0 | URL `/business/requirement/list?projectId=&status=&priority=&source=` 返回分页列表;响应 < 500ms |
| **R-002** | 新增需求 | P0 | requirement_no 自动生成 `REQ-YYYY-NNNN`(沿用 Project ADR-0001 规则);校验 title 非空、project_id 存在 |
| **R-003** | 修改需求(基本信息) | P0 | 必填字段校验同 R-002;返回 错误码 602(必填空)/604(参数错误) |
| **R-004** | 状态推进(状态机) | P0 | 4×4 转换矩阵 + 终态保护(已完成/已取消不可退回);违规返回 错误码 601 |
| **R-005** | 删除需求(逻辑删除) | P1 | del_flag = '2';终态项不可删除直接物理删除 |
| **R-006** | 导出 Excel | P1 | 字段同列表;UTF-8 with BOM |
| **R-007** | 项目详情页的"关联需求 Tab" | P1 | 在 Project 详情前端组件中嵌入,后端复用 R-001 接口 + `projectId` 参数 |
| **R-008** | 按需求编号精确查询 | P2 | `GET /business/requirement/{requirementNo}` 返回单条 |
| **R-009** | 按需求 ID 查详情 | P0 | 同 Project 模式,前端 dialog 渲染 |
| **R-010** | 7 个 sys_menu + admin 角色全量授权 | P0 | 业务管理 → 需求管理 二级菜单 + 6 个按钮权限 |

### 3.3 状态机 4×4 转换矩阵

```
                        待评审  开发中  已完成  已取消
当前状态：待评审(00)      —      ✅     ❌     ✅
            开发中(01)    ✅     —      ✅     ✅
            已完成(02)    ❌     ❌     —      ❌  (终态保护)
            已取消(03)    ❌     ❌     ❌     —  (终态保护)
```

> 终态保护规约同 Project §3.3 — `已完成`/`已取消` 不可再转换为任何状态;违规抛 ServiceException(601 "状态转换违规")。

### 3.4 错误码 (沿用 Project §3.2 命名规约)

| 错误码 | 含义 | 触发场景 |
|---|---|---|
| 601 | 状态转换违规 | 终态被改 / 非法跳转(如 00→02) |
| 602 | 必填字段缺失 | title/project_id 任一为空 |
| 604 | 参数错误 | source/priority/status 不在字典枚举 |
| 701 | 需求编号已存在 | INSERT 时 requirement_no 冲突 |
| 702 | 项目不存在 | INSERT/UPDATE 时 project_id 在 tb_project 找不到 |

---

## 4. 非功能需求

| 维度 | 要求 |
|---|---|
| 性能 | 列表 1k 行下查询响应 < 500ms;新增 < 200ms |
| 可用性 | 与 Project 一致(本地 dev,无 SLA) |
| 安全 | `@PreAuthorize("@ss.hasPermi('business:requirement:*')")` 七权限点 |
| 审计 | 沿用 `@Log(title=,businessType=)` RuoYi 框架,状态推进记 OPERATE 日志 |
| 数据完整性 | DB 层 FK 约束 project_id → tb_project;requirement_no 唯一索引 |
| 兼容性 | 与 Project v0.1.0 完全兼容,不动 tb_project |

---

## 5. 依赖与约束

**依赖**:
- `tb_project` v0.1.0 (已 ready)
- `sys_user` (RuoYi 自带,assignee_user_id 引用)
- `sys_dict_data` (RuoYi 自带,3 个新字典类型)
- `sys_menu` (RuoYi 自带,加 7 条)

**约束**:
- requirement_no 编号规则与 Project 一致 (ADR-0001 → 改为 `REQ-YYYY-NNNN`,见 ADR-0002 草案)
- 不引入新外部依赖,保持现有技术栈不变

---

## 6. ADR-0002 (草案): REQ 编号规则

> 沿用 Project ADR-0001 模式,只是前缀和年份序号空间独立。

**决策**: 需求编号格式 `REQ-YYYY-NNNN`,年内序号空间独立于其他实体。

**实现**: `ProjectMapper` 已有 `selectMaxSeqOfYear` 的模式,Requirement Service 复用。

---

## 7. 评审计划

- **评审日期**: 2026-05-16
- **评审人**: Wjl `[solo-review]`
- **预期结论**: unconditional pass(参考 Project Phase 01 成功路径)

---

## 修订记录

| 日期 | 版本 | 修改人 | 变更 |
|---|---|---|---|
| 2026-05-16 | v1.0 | Wjl | 首次创建,基于 AgriPLM PRD §3.2.1 通用化 |

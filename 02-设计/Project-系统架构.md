# Project 模块 — 系统架构

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 关联 PRD | [Project-PRD.md](../01-立项/Project-PRD.md) v1.0 |
| 状态 | 设计中（待 Phase 02 Gate 评审通过） |

## 1. 架构总览

Project 模块是 PLM 第一个业务实体，**采用单实体 + 子包落位**方案（不新建 Maven 模块）。整体架构沿用若依标准分层：

```
浏览器
  │ (HTTPS, JWT in Authorization header)
  ▼
plm-frontend  (Vite + Vue 3 + Element Plus)
  └─ /views/business/project/index.vue
  └─ /api/business/project.ts → 调用 /dev-api/business/project/*
  │
  │ Vite dev proxy /dev-api → http://localhost:8081
  ▼
plm-admin (Spring Boot 4 + JWT filter)
  └─ web/controller/business/ProjectController.java
  │
  ▼
plm-system (Service / Mapper / Domain)
  └─ system/business/project/{domain,mapper,service,service/impl}/
  │
  ▼
MySQL 8 (schema=plm)               Redis (缓存 / 验证码)
  └─ tb_project                    └─ login_tokens:*
  └─ sys_user (FK manager_user_id)
  └─ sys_dict_data (biz_project_*)
  └─ sys_menu (business/project)
```

## 2. 组件清单

| 组件 | 模块位置 | 职责 | Owner |
|---|---|---|---|
| `Project` (Domain) | `plm-system/.../system/business/project/domain/` | 实体字段定义（继承 BaseEntity） | Wjl |
| `ProjectMapper` | 同上 mapper/ | MyBatis 接口 + XML | Wjl |
| `IProjectService` + `ProjectServiceImpl` | 同上 service/ | 业务逻辑（状态机校验、审计字段填充） | Wjl |
| `ProjectController` | `plm-admin/.../web/controller/business/` | REST 端点（含 `@PreAuthorize` 权限） | Wjl |
| 前端列表页 | `plm-frontend/src/views/business/project/index.vue` | 列表 / 搜索 / CRUD 对话框 / 导出 | Wjl |
| 前端 API | `plm-frontend/src/api/business/project.ts` | axios 调用封装 | Wjl |
| 前端类型 | `plm-frontend/src/types/api/business/project.ts` | TS 接口定义 | Wjl |

## 3. 关键业务路径（端到端时序）

### 3.1 立项流程（最高频场景 S1）

```
PM 用户  │ 浏览器（Vue）│ Vite 代理 │ Spring Boot   │ MySQL │ Redis │
─────────┼───────────────┼──────────┼───────────────┼───────┼───────┤
1. 点击「新增」                                                       
2. 填写表单 (项目编号/名称/类型/负责人/起止/预算)                       
3. 提交 ──▶  POST /dev-api/business/project                            
                       ──▶ POST /business/project (JWT)               
                                ──▶ ProjectController.add()           
                                ──▶ Service: 校验唯一编号 / 设审计字段
                                ──▶ Mapper.insert (utf8mb4)            
                                                ──▶ INSERT INTO tb_project
                                                ◀──  affected=1        
                                ◀── 200 AjaxResult.success             
                       ◀── 200 (Vite 透传)                             
4. 列表自动刷新                                                       
```

### 3.2 状态推进流程（场景 S2，触发状态机校验）

```
PM   │ Service 层 status state-machine
─────┼──────────────────────────────────
1. 当前状态 0(未启动)，点击「启动」
2. PUT /business/project { id, status: '1' }
3. ProjectController.edit() → Service.update()
4. Service 加载当前 status (0)，校验 0→1 合法（查 PRD §3.3 转换矩阵）
5. 合法 → update_by/time 填充 → Mapper.update
6. 非法（如 3→1）→ throw ServiceException("当前状态不允许此操作", 701)
7. GlobalExceptionHandler 返回 AjaxResult.error(701, msg)
```

## 4. 部署拓扑

本期不改部署：与 PLM 主系统同进同退。

| 环境 | URL | 数据情况 | 备注 |
|---|---|---|---|
| dev | localhost:80 → 8081 | 本地 plm schema | 当前在用 |
| staging | TBD | 脱敏样本数据 | Phase 04 准备 |
| prod | TBD | 真实数据 | Phase 05 上线 |

## 5. 与现有 PLM 架构的集成点

| 集成点 | 用法 |
|---|---|
| `sys_user` | `tb_project.manager_user_id` FK；查询时 join 取 nickname |
| `sys_dict_data` | `biz_project_type` / `biz_project_status` 两个新字典 |
| `sys_menu` | 业务管理父菜单 (2000) + 项目管理 (2010) + 5 个按钮权限 (2011-2015) |
| `sys_role_menu` | 给 admin (role_id=1) 默认全部菜单权限 |
| `sys_oper_log` | 所有写操作通过 `@Log` 注解自动入库（审计） |
| `ruoyi-bootstrap` skill Phase 7 模板 | **一键生成上述全部代码 + SQL**（节省 50%+ 工时） |

## 6. 技术选型理由

| 决策 | 选择 | 理由 |
|---|---|---|
| 业务模块落位 | `plm-system/.../system/business/` 子包 | v0.1 单实体，无必要为一张表加 Maven 模块；后续 Phase/Task 多了再拆 → 在 [立项评审纪要 D4](../01-立项/Project-立项评审纪要-2026-05-15.md) 已决 |
| 编号规则 | 见 [ADR-0001](../03-开发/ADR/0001-project-no-rule.md) | 本 Phase 02 评审决议 |
| 状态机实现位置 | Service 层硬编码转换矩阵 | 不引入 SCSF / Spring StateMachine 等重型框架，v0.1 状态枚举少（5 个）够简单 |
| 主键 | bigint auto_increment | 与若依 `sys_*` 表保持一致；不引入 UUID |
| 字典 vs 枚举 | 字典 (`biz_project_*`) | 与若依风格一致；可视化运维（admin 改字典值不需要发版） |

## 7. 不引入的技术（明确边界）

- ❌ 不引入 ElasticSearch / 搜索引擎 — `tb_project` 量级 < 万，MySQL like 够用
- ❌ 不引入消息队列 — 无异步事件需求
- ❌ 不引入分布式锁 — `project_no` 唯一索引 + DB 约束保证
- ❌ 不引入工作流引擎（Flowable / Activiti） — 状态机简单到不需要框架

## 8. 风险与缓解

| 风险 | 概率 | 影响 | 缓解 |
|---|---|---|---|
| `project_no` 重号 | 低 | 中 | 唯一索引 + Service 层 try-catch DuplicateKeyException |
| 状态机校验被前端绕过 | 中 | 低 | 后端 Service 层强校验（不依赖前端拦截）|
| 字典类型与 `sys_*` 冲突 | 低 | 高 | 强制 `biz_` 前缀（已在 [.claude/rules.md §A](../.claude/rules.md) 约束） |

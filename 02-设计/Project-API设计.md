# Project 模块 — API 设计

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 关联 PRD | [Project-PRD.md](../01-立项/Project-PRD.md) |
| 鉴权 | JWT Bearer (header: `Authorization`) |
| Base path | `/business/project` (前端走 Vite 代理 `/dev-api/business/project`) |
| 风格 | REST 紧贴若依现有风格 |

## 1. 设计原则

- **REST 资源风格**，url 用名词复数 `/business/project`
- **响应统一包装**：`AjaxResult { code: int, msg: string, data: T }` 或 `TableDataInfo` (分页)
- **错误码**：HTTP 状态码 + 业务 code（见 §4）
- **分页参数**：`pageNum` / `pageSize`（沿用若依 PageHelper）
- **权限串**：`business:project:<action>`（list / query / add / edit / remove / export）

## 2. 端点清单

| # | Method | Path | 权限串 | 入参 | 出参 | Log type |
|---|---|---|---|---|---|---|
| 1 | GET | `/business/project/list` | `business:project:list` | `ProjectQuery` (query string) | `TableDataInfo<Project>` | — |
| 2 | POST | `/business/project/export` | `business:project:export` | `ProjectQuery` (body) | Excel 文件流 | `EXPORT` |
| 3 | GET | `/business/project/{id}` | `business:project:query` | path `id` | `AjaxResult<Project>` | — |
| 4 | POST | `/business/project` | `business:project:add` | `Project` (body) | `AjaxResult<Void>` | `INSERT` |
| 5 | PUT | `/business/project` | `business:project:edit` | `Project` (body) | `AjaxResult<Void>` | `UPDATE` |
| 6 | DELETE | `/business/project/{ids}` | `business:project:remove` | path `ids` (CSV) | `AjaxResult<Void>` | `DELETE` |

## 3. 详细契约

### 3.1 列表查询

```http
GET /business/project/list?pageNum=1&pageSize=10&projectName=&projectType=&status=
Authorization: Bearer <jwt>
```

**入参** (`ProjectQuery`):

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` | int | ⬜ (默认 1) | 页码 |
| `pageSize` | int | ⬜ (默认 10, 最大 100) | 每页 |
| `projectNo` | string | ⬜ | 编号模糊匹配 |
| `projectName` | string | ⬜ | 名称模糊匹配 |
| `projectType` | string | ⬜ | 类型字典值 (`rnd`/`upgrade`/`ops`) |
| `status` | string | ⬜ | 状态字典值 (`0-4`) |
| `managerUserId` | bigint | ⬜ | 项目经理过滤 |
| `params.beginStartDate` | date | ⬜ | 起始日期范围下限 |
| `params.endStartDate` | date | ⬜ | 起始日期范围上限 |

**出参** (`TableDataInfo`):

```json
{
  "msg": "查询成功",
  "code": 200,
  "rows": [ /* Project 数组 */ ],
  "total": 42
}
```

### 3.2 新增 (含状态机入口校验)

```http
POST /business/project
Content-Type: application/json
Authorization: Bearer <jwt>

{
  "projectNo": "PRJ-2026-001",
  "projectName": "客户 X 定制开发",
  "projectType": "rnd",
  "status": "0",
  "managerUserId": 1,
  "startDate": "2026-06-01",
  "endDate": "2026-12-31",
  "budget": 50.00,
  "description": "面向客户 X 的定制研发项目"
}
```

**字段校验**（Service 层，失败抛 ServiceException）：

| 校验项 | 错误码 | 错误消息 |
|---|---|---|
| `projectName` 不能为空 | 601 | "项目名称不能为空" |
| `projectNo` 不能为空 | 601 | "项目编号不能为空" |
| `projectNo` 全局唯一（DB UNIQUE 索引兜底） | 602 | "项目编号已存在" |
| `projectType` 必须在字典内 | 603 | "项目类型不合法" |
| 新建时 `status` 必须为 `0` 未启动 | 701 | "新建项目状态必须为「未启动」" |
| `startDate ≤ endDate`（如都填） | 604 | "起始日期不能晚于结束日期" |

**响应**：
```json
{ "code": 200, "msg": "操作成功" }
```

### 3.3 修改（含状态机转换校验）

```http
PUT /business/project
{
  "id": 1,
  "status": "1"  /* 推进到进行中 */
}
```

**状态机校验**（PRD §3.3 转换矩阵，Service 层硬编码）：

```java
// 在 ProjectServiceImpl.update() 中
if (newStatus != null && !newStatus.equals(old.getStatus())) {
    if (!StatusMachine.canTransit(old.getStatus(), newStatus)) {
        throw new ServiceException(
            "状态 " + dictLabel(old.getStatus()) + " 不能直接转到 " + dictLabel(newStatus),
            701
        );
    }
}
```

> **决议**（开放问题 Q3 from PRD）：**后端必须强校验**，不依赖前端拦截。前端可以同时校验作为 UX 优化，但**不能跳过后端校验**。

### 3.4 详情 / 删除 / 导出

- **详情** `GET /business/project/{id}` — 标准查找；查不到返 `AjaxResult.error(404, "项目不存在")`
- **删除** `DELETE /business/project/{ids}` — 逻辑删除（`UPDATE ... SET del_flag='2'`），不真删；可批量（`ids` 是 CSV）
- **导出** `POST /business/project/export` — body 同 list 查询参数；返回 Excel 流，文件名 `项目数据_yyyyMMddHHmmss.xlsx`

## 4. 错误码表

业务码段位（与 [开发规范.md §1.6](../03-开发/开发规范.md) 对齐）：

| 段位 | 用途 | 本模块用到 |
|---|---|---|
| 200 | 成功 | ✅ |
| 401 | 未登录 | (JWT filter 拦截) |
| 403 | 无权限 | (PreAuthorize 拦截) |
| 404 | 资源不存在 | ✅ 详情查无 / id 错 |
| 500 | 系统异常 | (GlobalExceptionHandler 兜底) |
| 601-605 | 业务参数错误 | ✅ 字段校验 |
| 701-705 | 业务规则错误 | ✅ 状态机非法转换 |

### Project 模块完整错误码

| Code | 场景 | HTTP |
|---|---|---|
| 200 | 成功 | 200 |
| 404 | 项目不存在 | 200 (body code, RuoYi 风格) |
| 601 | 必填字段为空 | 200 (body) |
| 602 | 项目编号重复 | 200 (body) |
| 603 | 字典值不合法 | 200 (body) |
| 604 | 日期逻辑错误 | 200 (body) |
| 701 | 状态机非法转换 | 200 (body) |

> 注：若依风格的"HTTP 200 + body code"在 RESTful 严格主义看略奇怪，但全项目一致性优先（v0.1 不改）。后续考虑切到"HTTP 4xx + 标准 Problem Details"是 v1.0 之外的事，需另起 ADR。

## 5. 鉴权与权限

- **JWT** 走若依现有 `JwtAuthenticationTokenFilter`，无新逻辑
- **API 权限串**统一前缀 `business:project:*`：

| Action | 权限串 | 谁有 |
|---|---|---|
| 列表 | `business:project:list` | role_id=1 (admin) 全部 |
| 详情 | `business:project:query` | 同上 |
| 新增 | `business:project:add` | 同上 |
| 修改 | `business:project:edit` | 同上 |
| 删除 | `business:project:remove` | 同上 |
| 导出 | `business:project:export` | 同上 |

v0.2 引入"项目经理只能改自己负责的项目" → `@PreAuthorize` 加 SpEL 或自定义注解（本期不做）。

## 6. 在线文档

- **本地 Swagger UI**：http://localhost:8081/swagger-ui.html
- 启动后端后，从此能看到 ProjectController 的所有端点 + 自动生成的 ProjectQuery/Project schema
- Phase 03 开发完成后，OpenAPI JSON 可在 `/v3/api-docs` 拿到

## 7. 版本策略

- 当前 v1.0：URL 不带版本前缀（沿用 RuoYi 风格）
- 破坏性变更（如改 `status` 取值含义）→ 必须走 ADR + 提前 1 个 Sprint 通知前端
- v2.0 可考虑加 `/v2/business/project` 共存

## 8. 前端 review

- ✅ 前端 lead（Wjl，solo）已 review API 契约
- 一致接受。无 blocking 问题。

## 9. 变更记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-15 | 初版 |

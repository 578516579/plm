# Project 模块 — 接口用例

测试粒度：HTTP 请求/响应级别。

## 用例

| 用例 ID | 接口 | 入参 | 期望响应 | 验证 | 状态 |
|---|---|---|---|---|---|
| TC-Proj-API-001 | GET /business/project/list | (none) | code=200, rows=[] (空) | 路由 + 权限 | ✅ E2E 已跑 |
| TC-Proj-API-002 | GET /business/project/list?pageSize=10 | pageSize | code=200, rows ≤ 10 | 分页参数 | ✅ Phase 03 E2E |
| TC-Proj-API-003 | POST /business/project | { name, type, manager, ... }（无 projectNo）| code=200, msg="操作成功" | 自动生成 projectNo | ✅ E2E + 单测 |
| TC-Proj-API-004 | POST /business/project | { (无 name) } | code=601 | 字段校验 | ✅ 单测 |
| TC-Proj-API-005 | POST /business/project | { name, status:"1" } | code=701 | 初始状态保护 | ✅ 单测 |
| TC-Proj-API-006 | GET /business/project/{id} | id=1 | code=200, data={..} | 详情查询 | ✅ E2E |
| TC-Proj-API-007 | GET /business/project/99999 | id=99999 | code=200, data=null（沿用若依规范）| 不存在场景 | ⏸️ 需补 |
| TC-Proj-API-008 | PUT /business/project | { id, status: "1" } | code=200（0→1 合法）| 状态机 | ✅ E2E |
| TC-Proj-API-009 | PUT /business/project | { id, status: "0" }（当前 1） | code=701 | 状态机非法 | ✅ E2E + 单测 |
| TC-Proj-API-010 | PUT /business/project | { id=99999, status: "1" } | code=200/code=404 | 项目不存在 | ✅ 单测 |
| TC-Proj-API-011 | DELETE /business/project/1 | path id | code=200 | 逻辑删除 | ⏸️ 实现但 E2E 未跑 |
| TC-Proj-API-012 | DELETE /business/project/1,2,3 | path ids CSV | code=200 | 批量删除 | ⏸️ 同上 |
| TC-Proj-API-013 | POST /business/project/export | query body | xlsx 文件流 | 导出 | ⏸️ E2E 未跑 |
| TC-Proj-API-014 | * 端点 + 无 token | — | code=401 | 鉴权拦截 | ⏸️ 需补 |
| TC-Proj-API-015 | * 端点 + 无权限 token | 普通用户的 token | code=403 | PreAuthorize 拦截 | ⏸️ 需补（待 v0.2 普通用户角色） |

## 权限拦截测试（按 PRD / API §5）

每个端点的 `@PreAuthorize` 都已在 ProjectController 代码中存在：

| 端点 | 权限串 | 测试方法 |
|---|---|---|
| GET list | `business:project:list` | 用 admin token（含此权限）✅ |
| GET /{id} | `business:project:query` | 同上 |
| POST | `business:project:add` | 同上 |
| PUT | `business:project:edit` | 同上 |
| DELETE | `business:project:remove` | 同上 |
| POST export | `business:project:export` | 同上 |

> 完整的"无权限 token 应被拒"测试待 v0.2 引入普通用户角色后补。

## 错误码覆盖矩阵（对照 API §4）

| Code | 触发场景 | 覆盖状态 |
|---|---|---|
| 200 | 各 happy path | ✅ |
| 401 | 无 token | ⏸️ 需补 |
| 404 | 项目不存在 | ✅（单测）|
| 601 | 必填空 | ✅（单测 + E2E 错码可见）|
| 602 | 编号重复 | ✅（撞号重试单测）|
| 604 | 日期顺序错 | ✅（单测）|
| 701 | 状态机非法 / 初始状态错 | ✅（E2E + 单测多个）|

---
name: api-design
description: PLM API 设计 — REST 资源路径 + 6 标准端点 + 复合视图决议 + 错误码 + 权限串 全套 Phase 02 §B.3 设计文档。当用户说"API 设计 / REST 设计 / 端点设计 / 复合视图 / dashboard 端点 / 权限串 / 错误码 API"时调用。输出: 02-设计/<模块>-API设计.md。**tech-lead agent 的子工具** — agent §2.4 触发。
---

# api-design — API 设计 skill v0.1

**tech-lead agent 的子工具**, 主走 §2.4 API 设计职责。

---

## 1. 何时调用

- 用户说 "API 设计 / REST 设计 / 端点设计"
- tech-lead agent §2.4 触发
- 新模块 Phase 02 §B.3 必产出
- 现有模块加端点 / 改契约

---

## 2. 必读

1. `01-立项/<模块>-PRD.md` — 验收标准 / 用户故事
2. `PRD-MAPPING.md §<模块>` 字段 + 状态机 + 错误码
3. `02-设计/<模块>-数据库设计.md` (db-design 产) 字段表
4. `02-设计/<其他>-API设计.md` 类似模块作模板
5. grep 现存 `plm-admin/web/controller/business/<其他>Controller.java` (per 0041)

---

## 3. 7 维设计

### 3.1 REST 资源路径

```
GET    /business/<entity>           list (分页)
GET    /business/<entity>/{id}      query
POST   /business/<entity>           add
PUT    /business/<entity>           edit
DELETE /business/<entity>/{id}      remove
GET    /business/<entity>/export    export (Excel)
```

6 标准端点 (per ruoyi-bootstrap skill Phase 7 + rules.md §A 权限串)。

### 3.2 复合视图决议 (per proposal 0018+0020 bundle)

业务需要"看板 / 仪表盘"等聚合数据时, 必决议:

| 方案 | 适用 | 路径 | trade-off |
|---|---|---|---|
| **REST 资源** | 单一实体 CRUD | `/business/<entity>` | 标准 / 缓存友好 |
| **聚合视图** | 跨实体 / 看板 / 详情聚合 | `/business/<entity>/board` / `/dashboard` | 一次拉数据 / 客户端简单 / 不好缓存 |

不允许"为标准而拆 API" (per proposal 0020): 看板场景就该 1 个聚合端点, 不分 N 次 REST。

### 3.3 请求 / 响应 Schema

每端点定义:

```yaml
POST /business/testcase
Request:
  Content-Type: application/json
  Body: {
    title: string (required, maxlength 200)
    projectId: long (required, FK)
    environment: enum [dev/staging/prod] (required)
    ...
  }
Response: 200
  Body: {
    code: 200
    msg: "操作成功"
    data: {
      id: long
      testcaseNo: string (TC-YYYY-NNNN)
      ...
    }
  }
Errors:
  400: 参数缺失 / 字段格式错
  401: 未登录 (JWT 过期)
  403: 权限不足 (无 business:testcase:add)
  604: ENUM 越界 (environment 非白名单)
  702: projectId FK 失效 (projectService.checkExists)
  708: URL 字段 host 不在白名单 (per proposal 0101)
```

### 3.4 错误码 (登记到 PRD-MAPPING.md §4)

新增码必先登记 (per rules.md §M.5):

| 错误码 | 含义 | 触发 |
|---|---|---|
| 200 | 成功 | — |
| 400 | 参数缺失/格式 | service 入口 @Valid |
| 401 | 未登录 | JwtAuthenticationTokenFilter |
| 403 | 权限不足 | @PreAuthorize |
| 601 | 状态机违规 | 非法转换 |
| 604 | ENUM 越界 | 白名单校验 |
| 702 | <Entity> 不存在 | checkExists |
| 705 | 进入态必填缺 | 状态机进入态 |
| 708 | URL host 不在白名单 | UrlValidator |

### 3.5 权限串 (per rules.md §A)

格式: `business:<entity>:<action>`

```java
@PreAuthorize("@ss.hasPermi('business:testcase:list')")    GET list
@PreAuthorize("@ss.hasPermi('business:testcase:query')")   GET /{id}
@PreAuthorize("@ss.hasPermi('business:testcase:add')")     POST
@PreAuthorize("@ss.hasPermi('business:testcase:edit')")    PUT
@PreAuthorize("@ss.hasPermi('business:testcase:remove')")  DELETE
@PreAuthorize("@ss.hasPermi('business:testcase:export')")  GET /export
```

不混 `system:*` (那是 RuoYi 系统模块)。

### 3.6 分页 / 排序 / 筛选

list 端点统一参数 (RuoYi 框架):

```
GET /business/<entity>
  ?pageNum=1&pageSize=10
  &orderByColumn=createTime&isAsc=desc
  &<filter1>=value&<filter2>=value
```

### 3.7 服务端计算字段

per rules.md §M.3: 服务端字段 (qualityGatePassed / aiReviewScore 等) Mapper update **不接受**前端值。

设计标注:

```yaml
field: qualityGatePassed
type: char(1) Y/N
**server-computed**: true (前端写入会被 Mapper 忽略)
trigger: AI 按钮 → /business/testcase/{id}/check-quality 计算
```

---

## 4. 输出

`02-设计/<模块>-API设计.md` 7 段:
1. REST 资源路径表 (6 端点)
2. 复合视图决议 (如有 board / dashboard)
3. 请求/响应 schema (每端点)
4. 错误码登记 (含 PRD-MAPPING.md §4 增量)
5. 权限串
6. 分页/排序/筛选
7. 服务端计算字段

---

## 5. 衔接

| 上游 | api-design | 下游 |
|---|---|---|
| pm-prd-writer 验收标准 | → 端点定义 | → backend-coder Controller |
| db-design 字段表 | → 请求/响应 schema | → frontend-coder API client |
| 错误码 | → PRD-MAPPING.md §4 | → tester (用例覆盖每错误码) |

---

## 6. 反模式

- ❌ "为标准而拆" 把看板拆成 N 个 REST 端点 (per proposal 0020 反约束)
- ❌ 权限串混 system:* (per rules.md §A)
- ❌ 错误码不登记 PRD-MAPPING.md §4 (per rules.md §M.5)
- ❌ 服务端计算字段不标 (前端误以为可写入)
- ❌ 列表端点不含分页参数 (大表 OOM)
- ❌ FK 字段不在 schema 标"802 触发 checkExists"
- ❌ 看板端点的 trade-off 没决议 (Phase 03 才发现选错)

---

## 7. 历史

| v0.1 | 2026-05-19 | 首版; tech-lead 配套 4 skill 之三 |

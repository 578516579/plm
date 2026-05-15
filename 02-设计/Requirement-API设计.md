# Requirement 模块 — API 设计

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 关联 PRD | [Requirement-PRD.md](../01-立项/Requirement-PRD.md) |
| 鉴权 | JWT Bearer (复用 Project 模式) |
| Base path | `/business/requirement` |
| 风格 | REST,沿用 Project §1 设计原则 (略,不重复) |

## 1. 端点清单

| # | Method | Path | 权限串 | 入参 | 出参 | Log type |
|---|---|---|---|---|---|---|
| 1 | GET | `/business/requirement/list` | `business:requirement:list` | `RequirementQuery` | `TableDataInfo<Requirement>` | — |
| 2 | POST | `/business/requirement/export` | `business:requirement:export` | `RequirementQuery` | Excel | `EXPORT` |
| 3 | GET | `/business/requirement/{id}` | `business:requirement:query` | path id | `AjaxResult<Requirement>` | — |
| 4 | POST | `/business/requirement` | `business:requirement:add` | `Requirement` | `AjaxResult<Void>` | `INSERT` |
| 5 | PUT | `/business/requirement` | `business:requirement:edit` | `Requirement` | `AjaxResult<Void>` | `UPDATE` |
| 6 | DELETE | `/business/requirement/{ids}` | `business:requirement:remove` | CSV ids | `AjaxResult<Void>` | `DELETE` |

## 2. 详细契约

### 2.1 列表查询

```http
GET /business/requirement/list?pageNum=1&pageSize=10&projectId=&status=&priority=&source=
Authorization: Bearer <jwt>
```

**入参** (`RequirementQuery`):

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` / `pageSize` | int | ⬜ | 同 Project |
| `requirementNo` | string | ⬜ | 编号模糊匹配 |
| `projectId` | bigint | ⬜ | 项目筛选(高频,**Project 详情页"关联需求 Tab"必传**) |
| `title` | string | ⬜ | 标题模糊 |
| `source` | string | ⬜ | 字典 `biz_req_source`: 01-04 |
| `priority` | string | ⬜ | 字典 `biz_req_priority`: 00/01/02 |
| `status` | string | ⬜ | 字典 `biz_req_status`: 00-03 |
| `assigneeUserId` | bigint | ⬜ | 指派人筛选 |

**出参**: `TableDataInfo` (同 Project)

### 2.2 新增 (含 requirement_no 自动生成 + 字段校验)

```http
POST /business/requirement
{
  "projectId": 1,
  "title": "列表导出 Excel 太慢",
  "description": "客户反馈导出 5000 行要 30 秒...",
  "source": "01",
  "priority": "01",
  "status": "00"
}
```

**Service 层字段校验**:

| 校验项 | 错误码 | 错误消息 |
|---|---|---|
| `title` 非空 | 602 | "需求标题不能为空" |
| `projectId` 非空且在 `tb_project` 存在 | 702 | "关联项目不存在" |
| `source` 在字典 `biz_req_source` 内 | 604 | "需求来源不合法" |
| `priority` 在字典 `biz_req_priority` 内 | 604 | "优先级不合法" |
| 新建时 `status` 必须为 `00` 待评审 | 601 | "新建需求状态必须为「待评审」" |
| `requirementNo` 自动生成(同 Project §2.2 generateProjectNo 模式) | 701 | "需求编号已存在"(DB UNIQUE 兜底) |

**`requirementNo` 生成规则** (ADR-0002):

```java
// RequirementServiceImpl.generateRequirementNo()
int year = LocalDate.now().getYear();
int maxSeq = mapper.selectMaxSeqOfYear(year);  // SELECT MAX(SUBSTR(requirement_no, 10)) FROM tb_requirement WHERE requirement_no LIKE 'REQ-YYYY-%'
return String.format("REQ-%d-%04d", year, maxSeq + 1);
```

### 2.3 修改 (含状态机 4×4 转换校验)

```http
PUT /business/requirement
{
  "id": 1,
  "status": "01"  // 待评审 → 开发中
}
```

**状态机校验** (沿用 Project §2.3 模式):

```java
if (newStatus != null && !newStatus.equals(old.getStatus())) {
    if (!RequirementStatusMachine.canTransit(old.getStatus(), newStatus)) {
        throw new ServiceException("状态 " + ... + " 不能转到 " + ..., 601);
    }
}
```

**转换矩阵** (PRD §3.3,4×4):

```
              00待评审  01开发中  02已完成  03已取消
00待评审       —        ✅       ❌       ✅
01开发中      ✅        —        ✅       ✅
02已完成      ❌        ❌       —        ❌  (终态)
03已取消      ❌        ❌       ❌       —   (终态)
```

> 终态保护规约同 Project §3.3。`status` 推进时若填了 `reviewNote` 字段,一并更新(评审简要纪要)。

### 2.4 详情 / 删除 / 导出

- **详情** `GET /business/requirement/{id}` — 同 Project 模式
- **删除** `DELETE /business/requirement/{ids}` — 逻辑删除 (del_flag='2');**终态需求不可删** 改为物理删除是 v0.3 决议
- **导出** `POST /business/requirement/export` — 字段全列,含 project_name 关联查询

## 3. 错误码表

沿用全局段位 (开发规范 §1.6):

| Code | 场景 |
|---|---|
| 200 | 成功 |
| 404 | 需求不存在 |
| 601 | 状态转换违规 / 新建状态非"待评审" |
| 602 | 必填字段空 (title) |
| 604 | 字典值不合法 (source/priority/status) |
| 701 | requirement_no 重复 |
| 702 | 关联项目不存在 |

## 4. 鉴权与权限

| Action | 权限串 |
|---|---|
| 列表 | `business:requirement:list` |
| 详情 | `business:requirement:query` |
| 新增 | `business:requirement:add` |
| 修改 | `business:requirement:edit` |
| 删除 | `business:requirement:remove` |
| 导出 | `business:requirement:export` |

> v0.3+ 加"PM 只能改自己项目下的需求"(`@DataScope`),本期 admin 全权。

## 5. 与 Project 模块的耦合点

| 接口 | 耦合 | 处理 |
|---|---|---|
| `/list?projectId=` | 强 | 必须先校验 project_id 存在 |
| 新增/修改 | 强 | projectId 必填 + 校验存在 |
| Project 前端"关联需求 Tab" | 弱 | 前端组件复用 `requirement/list` 接口 + projectId 参数,无新后端 |

## 6. 在线文档

- Swagger UI:启动后端后 http://localhost:8081/swagger-ui.html 可见 RequirementController

## 7. 前端 review

- ✅ Wjl (solo) self-review,无 blocking

## 8. 变更记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 初版 |

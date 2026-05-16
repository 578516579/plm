# Defect 模块 — API 设计

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| Base path | `/business/defect` |
| 鉴权 | JWT Bearer |

## 1. 端点清单（6 个）

| # | Method | Path | 权限串 | Log type |
|---|---|---|---|---|
| 1 | GET | `/business/defect/list` | `business:defect:list` | — |
| 2 | POST | `/business/defect/export` | `business:defect:export` | EXPORT |
| 3 | GET | `/business/defect/{id}` | `business:defect:query` | — |
| 4 | POST | `/business/defect` | `business:defect:add` | INSERT |
| 5 | PUT | `/business/defect` | `business:defect:edit` | UPDATE |
| 6 | DELETE | `/business/defect/{ids}` | `business:defect:remove` | DELETE |

## 2. 状态机 5×5

```
            00新建  01已确认  02处理中  03已解决  04已关闭
00新建       —      ✅       ❌       ❌       ❌
01已确认    ❌      —        ✅       ❌       ✅ (重复/无效)
02处理中    ❌      ✅       —        ✅       ❌
03已解决    ❌      ✅       ❌       —        ✅
04已关闭    ❌      ❌       ❌       ❌       — (终态)
```

**关键边**：
- 03→01 反向边（回归打回）
- 01→04 / 02→? 无效缺陷快关 (仅 01→04 允许)

## 3. 校验

| 校验项 | 错误码 |
|---|---|
| title 非空 | 602 |
| severity / category 在字典内 | 604 |
| projectId 必填且存在 | 702 |
| sprintId / taskId 填则校验 | 702 |
| 新建 status 必须 00 | 601 |
| 进入 03 必填 resolution | 705 |
| 状态机非法转换 | 601 |

## 4. 与兄弟模块耦合点

| 接口 | 耦合 |
|---|---|
| 新增/修改 projectId/sprintId/taskId FK | 调 Project/Sprint/Task Mapper.selectById |
| 删除前置检查 | （v0.3 无,生产数据慎删） |

## 5. 变更记录

| 版本 | 日期 |
|---|---|
| v1.0 | 2026-05-16 |

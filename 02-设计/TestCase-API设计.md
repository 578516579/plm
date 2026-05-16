# TestCase 模块 — API 设计

| 字段 | 值 |
|---|---|
| Base path | `/business/testcase` |
| 鉴权 | JWT Bearer |

## 1. 端点（7 个）

| # | Method | Path | 权限 |
|---|---|---|---|
| 1 | GET | `/business/testcase/list` | list |
| 2 | POST | `/business/testcase/export` | export |
| 3 | GET | `/business/testcase/{id}` | query |
| 4 | POST | `/business/testcase` | add |
| 5 | PUT | `/business/testcase` | edit |
| 6 | DELETE | `/business/testcase/{ids}` | remove |
| 7 | POST | `/business/testcase/{id}/execute` | execute |

## 2. 状态机 5×5（含反向边）

```
            00 草稿  01 待执行  02 执行中  03 已通过  04 已失败
00 草稿      —       ✅        ❌         ❌         ❌
01 待执行   ✅        —        ✅         ❌         ❌
02 执行中   ❌       ✅        —          ✅         ✅
03 已通过   ❌       ✅ (重测) ❌         —          ❌
04 已失败   ❌       ✅ (重测) ❌         ❌         —
```

## 3. 校验

| 校验项 | 错误码 |
|---|---|
| title / steps / expectedResult 非空 | 602 |
| projectId 必填存在 | 702 |
| requirementId 填则校验 | 702 |
| 新建 status 必须 00 | 601 |
| is_automated='Y' 必填 automation_script_path | 706 |
| 状态机非法转换 | 601 |

## 4. `/execute` 端点

```http
POST /business/testcase/{id}/execute
{ "status": "03", "actualResult": "全部通过" }
```

服务端逻辑：
1. 校验当前 status = '02' 执行中（必须先推到执行中才能 execute）
2. 校验 status param ∈ {'03', '04'}
3. 更新: status = param.status, actualResult = param.actualResult,
        execution_count += 1, last_executed_at = NOW()
4. 返回 AjaxResult

## 5. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建 |

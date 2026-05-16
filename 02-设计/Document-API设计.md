# Document — API 设计

Base: `/business/document` | JWT Bearer

## 端点（6）

| # | Method | Path | 权限 |
|---|---|---|---|
| 1 | GET | `/business/document/list` | list |
| 2 | POST | `/business/document/export` | export |
| 3 | GET | `/business/document/{id}` | query |
| 4 | POST | `/business/document` | add |
| 5 | PUT | `/business/document` | edit |
| 6 | DELETE | `/business/document/{ids}` | remove |

## 状态机 4×4 含反向边

```
        00 草稿  01 待评审  02 已发布  03 已归档
00 草稿  —       ✅        ❌         ❌
01 待评审 ✅(反) —          ✅         ❌
02 已发布 ❌     ✅(反·重审) —         ✅
03 已归档 ❌     ❌        ❌         — (终态)
```

## 校验

- title / version / docType / projectId 必填 → 602
- doc_type 字典内 → 604
- 新建 status 必须 00 → 601
- 进入 02 必填 reviewer_user_id → 707
- FK projectId 存在 → 702

## 修订

| 版本 | 日期 |
|---|---|
| v1.0 | 2026-05-16 |

# @plm/defect

| 字段 | 值 |
|---|---|
| 模块中文名 | 缺陷管理 |
| 当前状态 | **active** (Phase 03 完成 2026-05-16) |
| 后端模块 | `plm-defect` Maven |
| 路由 | `/business/defect` |
| 字典 | `biz_defect_severity` / `biz_defect_category` / `biz_defect_status` |

## 5×5 状态机

```
00 新建 → 01 已确认 → 02 处理中 → 03 已解决 → 04 已关闭
                              ↑       ↓
                            (反向)
                            01 已确认 ← 03 (回归打回)
```

## 用法

```ts
import defectRoutes from '@plm/defect/router'
import { listDefect, addDefect } from '@plm/defect/api'
import type { DefectForm } from '@plm/defect/types'
```

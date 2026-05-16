# @plm/task

| 字段 | 值 |
|---|---|
| 模块中文名 | 任务 |
| 当前状态 | **active** (Phase 03 已交付) |
| 后端对应模块 | `plm-task` (Maven 模块) |
| 入口文件 | `src/index.ts` |
| 路由 | `src/router.ts` |

## 用法

```ts
// 主壳中
import taskRoutes from '@plm/task/router'
import { list任务, add任务 } from '@plm/task/api'
import type { 任务Form } from '@plm/task/types'
```

## 目录结构

```
src/
├── api/index.ts          REST 接口封装
├── types/index.ts        TypeScript 类型
├── views/index.vue       主列表页
└── router.ts             模块路由定义 (export default RouteRecordRaw[])
```

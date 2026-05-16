# @plm/sprint

| 字段 | 值 |
|---|---|
| 模块中文名 | 迭代 |
| 当前状态 | **active** (Phase 03 已交付) |
| 后端对应模块 | `plm-sprint` (Maven 模块) |
| 入口文件 | `src/index.ts` |
| 路由 | `src/router.ts` |

## 用法

```ts
// 主壳中
import sprintRoutes from '@plm/sprint/router'
import { list迭代, add迭代 } from '@plm/sprint/api'
import type { 迭代Form } from '@plm/sprint/types'
```

## 目录结构

```
src/
├── api/index.ts          REST 接口封装
├── types/index.ts        TypeScript 类型
├── views/index.vue       主列表页
└── router.ts             模块路由定义 (export default RouteRecordRaw[])
```

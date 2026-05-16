# @plm/requirement

| 字段 | 值 |
|---|---|
| 模块中文名 | 需求 |
| 当前状态 | **active** (Phase 03 已交付) |
| 后端对应模块 | `plm-requirement` (Maven 模块) |
| 入口文件 | `src/index.ts` |
| 路由 | `src/router.ts` |

## 用法

```ts
// 主壳中
import requirementRoutes from '@plm/requirement/router'
import { list需求, add需求 } from '@plm/requirement/api'
import type { 需求Form } from '@plm/requirement/types'
```

## 目录结构

```
src/
├── api/index.ts          REST 接口封装
├── types/index.ts        TypeScript 类型
├── views/index.vue       主列表页
└── router.ts             模块路由定义 (export default RouteRecordRaw[])
```

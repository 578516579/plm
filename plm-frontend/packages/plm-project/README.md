# @plm/project

| 字段 | 值 |
|---|---|
| 模块中文名 | 项目 |
| 当前状态 | **active** (Phase 03 已交付) |
| 后端对应模块 | `plm-project` (Maven 模块) |
| 入口文件 | `src/index.ts` |
| 路由 | `src/router.ts` |

## 用法

```ts
// 主壳中
import projectRoutes from '@plm/project/router'
import { list项目, add项目 } from '@plm/project/api'
import type { 项目Form } from '@plm/project/types'
```

## 目录结构

```
src/
├── api/index.ts          REST 接口封装
├── types/index.ts        TypeScript 类型
├── views/index.vue       主列表页
└── router.ts             模块路由定义 (export default RouteRecordRaw[])
```

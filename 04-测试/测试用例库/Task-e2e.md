# Task 模块 — E2E 自动化测试 (骨架,2026-05-17)

工具: [Playwright](https://playwright.dev/) (headless Chromium)。
脚本位置: [plm-frontend/e2e/task.spec.ts](../../plm-frontend/e2e/task.spec.ts)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit e682574) |
| 关联测试计划 | [../Task-测试计划-2026-05-17.md](../Task-测试计划-2026-05-17.md) |
| fixtures | `plm-frontend/e2e/helpers/fixtures.ts` 或 fixtures-task.ts |
| 状态机矩阵 | `plm-frontend/e2e/helpers/fixtures.ts` Task_STATUS_TRANSITIONS |

## 运行

```bash
cd plm-frontend
export DB_PASSWORD=...
npm run test:e2e -g "Task"           # 跑本模块
npm run test:e2e                          # 全套
```

## 测试场景覆盖

| 场景 | E2E 测试名 | 状态 |
|---|---|---|
| CRUD + 编号自动生成 | TC-Task-F001 | 待执行 |
| 字段校验 (必填 / 白名单 / FK) | TC-Task-F002~003 | 待执行 |
| 状态机合法 / 非法转换 | TC-Task-F004~005 | 待执行 |
| 终态保护 | TC-Task-F006 | 待执行 |

## 前置依赖

- 后端 8081 running
- 前端 80 running (Vite dev server)
- DB plm 库已含相关测试数据 (fixtures 注入)
- 字典数据已 import (sys_dict_data biz_task_*)

## 已知问题

首轮执行后回填。预计可能遇到的 flaky 类型: Playwright 等待时机 (network idle) / 字典加载竞态 / Chrome vs Edge 渲染差异。

# FeatureFlag 模块 — E2E 自动化测试 (骨架,2026-05-17)

工具: [Playwright](https://playwright.dev/) (headless Chromium)。
脚本位置: [plm-frontend/e2e/feature-flag.spec.ts](../../plm-frontend/e2e/feature-flag.spec.ts)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit e682574) |
| 关联测试计划 | [../FeatureFlag-测试计划-2026-05-17.md](../FeatureFlag-测试计划-2026-05-17.md) |
| fixtures | `plm-frontend/e2e/helpers/fixtures.ts` 或 fixtures-feature-flag.ts |
| 状态机矩阵 | `plm-frontend/e2e/helpers/fixtures.ts` FeatureFlag_STATUS_TRANSITIONS |

## 运行

```bash
cd plm-frontend
export DB_PASSWORD=...
npm run test:e2e -g "FeatureFlag"           # 跑本模块
npm run test:e2e                          # 全套
```

## 测试场景覆盖

| 场景 | E2E 测试名 | 状态 |
|---|---|---|
| CRUD + 编号自动生成 | TC-FeatureFlag-F001 | <待补> |
| 字段校验 (必填 / 白名单 / FK) | TC-FeatureFlag-F002~003 | <待补> |
| 状态机合法 / 非法转换 | TC-FeatureFlag-F004~005 | <待补> |
| 终态保护 | TC-FeatureFlag-F006 | <待补> |

## 前置依赖

- 后端 8081 running
- 前端 80 running (Vite dev server)
- DB plm 库已含相关测试数据 (fixtures 注入)
- 字典数据已 import (sys_dict_data biz_feature-flag_*)

## 已知问题

<待人工填写>:运行时发现的 flaky / 跨平台问题

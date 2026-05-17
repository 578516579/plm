# FeatureFlag 模块 — 测试计划 (骨架,2026-05-17)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f) |
| 关联 PRD | [FeatureFlag-PRD.md](../01-立项/FeatureFlag-PRD.md) |
| 关联 API 设计 | [FeatureFlag-API设计.md](../02-设计/FeatureFlag-API设计.md) |
| E2E spec | [plm-frontend/e2e/feature-flag.spec.ts](../plm-frontend/e2e/feature-flag.spec.ts) |
| 测试经理 | Wjl (solo) |

## 1. 测试范围
- 单元测试 (Service 层): mvn -pl plm-feature-flag test
- E2E 测试 (前端 + 后端集成): npm run test:e2e -g "FeatureFlag"
- 状态机覆盖: 见 [PRD-MAPPING.md §3](../PRD-MAPPING.md) `feature-flag` 行,合法 + 非法转换全覆盖

## 2. 测试用例库
- [FeatureFlag-functional.md](测试用例库/FeatureFlag-functional.md) <待人工填写>
- [FeatureFlag-api.md](测试用例库/FeatureFlag-api.md) <待人工填写>
- [FeatureFlag-e2e.md](测试用例库/FeatureFlag-e2e.md) <待人工填写>

## 3. 通过标准
- mvn test 单测全绿
- E2E 套件相关 case 全绿
- 字段白名单 (604) + 状态机 (601) + FK (702) + 业务规则 (其他) 覆盖

## 4. 测试数据
fixtures 见 [plm-frontend/e2e/helpers/fixtures.ts](../plm-frontend/e2e/helpers/fixtures.ts) 或 fixtures-feature-flag.ts。

## 5. 风险
<待人工填写>

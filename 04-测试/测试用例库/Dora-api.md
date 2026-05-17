# Dora 模块 — 接口用例 (骨架,2026-05-17)

测试粒度:HTTP 请求/响应级别。

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit e682574) |
| 关联 API 设计 | [../../02-设计/Dora-API设计.md](../../02-设计/Dora-API设计.md) |
| Base URL | `/business/dora` |
| Controller | `plm-backend/plm-dora/.../DoraController.java` |

## 用例

| 用例 ID | 接口 | 入参 | 期望响应 | 验证 | 状态 |
|---|---|---|---|---|---|
| TC-Dora-API-001 | GET /business/dora/list | (none) | code=200, rows=[] | 路由 + 权限 | <待补> |
| TC-Dora-API-002 | GET /business/dora/list?pageSize=10 | pageSize | code=200, rows ≤ 10 | 分页 | <待补> |
| TC-Dora-API-003 | POST /business/dora | 完整必填字段 | code=200 + 自动编号 | 新建 | <待补> |
| TC-Dora-API-004 | POST /business/dora | (缺必填) | code=602 | 必填校验 | <待补> |
| TC-Dora-API-005 | POST /business/dora | (非字典值) | code=604 | 白名单 | <待补> |
| TC-Dora-API-006 | GET /business/dora/{id} | id=1 | code=200, data={..} | 详情 | <待补> |
| TC-Dora-API-007 | PUT /business/dora | { id, status: 合法 } | code=200 | 状态机 | <待补> |
| TC-Dora-API-008 | PUT /business/dora | { id, status: 非法 } | code=601 | 状态机非法 | <待补> |
| TC-Dora-API-009 | DELETE /business/dora/{ids} | path ids CSV | code=200 | 逻辑删除 | <待补> |
| TC-Dora-API-010 | * 端点 + 无 token | — | code=401 | 鉴权 | <待补> |

## 权限拦截

按 PRD §5 / [API 设计](../../02-设计/Dora-API设计.md) §权限串前缀 `business:dora:*`。

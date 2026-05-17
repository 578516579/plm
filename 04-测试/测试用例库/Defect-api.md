# Defect 模块 — 接口用例 (骨架,2026-05-17)

测试粒度:HTTP 请求/响应级别。

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit e682574) |
| 关联 API 设计 | [../../02-设计/Defect-API设计.md](../../02-设计/Defect-API设计.md) |
| Base URL | `/business/defect` |
| Controller | `plm-backend/plm-defect/.../DefectController.java` |

## 用例

| 用例 ID | 接口 | 入参 | 期望响应 | 验证 | 状态 |
|---|---|---|---|---|---|
| TC-Defect-API-001 | GET /business/defect/list | (none) | code=200, rows=[] | 路由 + 权限 | <待补> |
| TC-Defect-API-002 | GET /business/defect/list?pageSize=10 | pageSize | code=200, rows ≤ 10 | 分页 | <待补> |
| TC-Defect-API-003 | POST /business/defect | 完整必填字段 | code=200 + 自动编号 | 新建 | <待补> |
| TC-Defect-API-004 | POST /business/defect | (缺必填) | code=602 | 必填校验 | <待补> |
| TC-Defect-API-005 | POST /business/defect | (非字典值) | code=604 | 白名单 | <待补> |
| TC-Defect-API-006 | GET /business/defect/{id} | id=1 | code=200, data={..} | 详情 | <待补> |
| TC-Defect-API-007 | PUT /business/defect | { id, status: 合法 } | code=200 | 状态机 | <待补> |
| TC-Defect-API-008 | PUT /business/defect | { id, status: 非法 } | code=601 | 状态机非法 | <待补> |
| TC-Defect-API-009 | DELETE /business/defect/{ids} | path ids CSV | code=200 | 逻辑删除 | <待补> |
| TC-Defect-API-010 | * 端点 + 无 token | — | code=401 | 鉴权 | <待补> |

## 权限拦截

按 PRD §5 / [API 设计](../../02-设计/Defect-API设计.md) §权限串前缀 `business:defect:*`。

# AiAgent 模块 — 接口用例 (骨架,2026-05-17)

测试粒度:HTTP 请求/响应级别。

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit e682574) |
| 关联 API 设计 | [../../02-设计/AiAgent-API设计.md](../../02-设计/AiAgent-API设计.md) |
| Base URL | `/business/ai-agent` |
| Controller | `plm-backend/plm-ai-agent/.../AiAgentController.java` |

## 用例

| 用例 ID | 接口 | 入参 | 期望响应 | 验证 | 状态 |
|---|---|---|---|---|---|
| TC-AiAgent-API-001 | GET /business/ai-agent/list | (none) | code=200, rows=[] | 路由 + 权限 | 待执行 |
| TC-AiAgent-API-002 | GET /business/ai-agent/list?pageSize=10 | pageSize | code=200, rows ≤ 10 | 分页 | 待执行 |
| TC-AiAgent-API-003 | POST /business/ai-agent | 完整必填字段 | code=200 + 自动编号 | 新建 | 待执行 |
| TC-AiAgent-API-004 | POST /business/ai-agent | (缺必填) | code=602 | 必填校验 | 待执行 |
| TC-AiAgent-API-005 | POST /business/ai-agent | (非字典值) | code=604 | 白名单 | 待执行 |
| TC-AiAgent-API-006 | GET /business/ai-agent/{id} | id=1 | code=200, data={..} | 详情 | 待执行 |
| TC-AiAgent-API-007 | PUT /business/ai-agent | { id, status: 合法 } | code=200 | 状态机 | 待执行 |
| TC-AiAgent-API-008 | PUT /business/ai-agent | { id, status: 非法 } | code=601 | 状态机非法 | 待执行 |
| TC-AiAgent-API-009 | DELETE /business/ai-agent/{ids} | path ids CSV | code=200 | 逻辑删除 | 待执行 |
| TC-AiAgent-API-010 | * 端点 + 无 token | — | code=401 | 鉴权 | 待执行 |

## 权限拦截

按 PRD §5 / [API 设计](../../02-设计/AiAgent-API设计.md) §权限串前缀 `business:ai-agent:*`。

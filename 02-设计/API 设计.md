# API 设计

后端对外/对内 API 的契约文档。前端、第三方接入方、测试都按这份文档对齐。

## 1. 设计原则
> REST / RPC 风格选择、错误码规范、分页约定、版本策略、鉴权方式（JWT Bearer 等）。

## 2. 资源与端点清单
> 按业务模块分组，每个端点列：method / path / 权限 / 请求体 / 响应体。

## 3. 错误码表
> code → message → HTTP status → 含义。

## 4. 在线文档
> Swagger UI：http://localhost:8081/swagger-ui.html（本地开发）
> 正式环境的 OpenAPI JSON 地址待补。

## 5. 变更记录
> 破坏性变更必须记录在此并同步前端 / 接入方。

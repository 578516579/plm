# PRD-MAPPING — AgriPLM·AI ↔ PLM 实现单一事实来源（SSoT）

> 本文件是 [CLAUDE.md "PRD/原型驱动开发"](CLAUDE.md) + [.claude/rules.md §M](.claude/rules.md) 强制要求的 SSoT。
> 所有业务字段、状态机、错误码、URL、菜单文案必须能追溯到 PRD § + 原型 HTML 表单元素。

---

## 1. 模块进度速览

| # | 模块 | PRD § | 原型 HTML | 后端模块 | 状态 |
|---|---|---|---|---|---|
| 1 | 项目 Project | F1.2 | projects.html | plm-project | 🟢 PRD-aligned |
| 2 | 需求 Requirement | F2.1 | requirements.html | plm-requirement | 🟢 PRD-aligned |
| 3 | 迭代 Sprint | F3.4 | kanban.html (含 sprint) | plm-sprint | 🟢 PRD-aligned |
| 4 | 任务 Task | F3.4 | kanban.html | plm-task | 🟢 PRD-aligned |
| 5 | 缺陷 Defect | F4.6 | defects.html | plm-defect | 🟢 PRD-aligned |
| 6 | 测试用例 TestCase | F4.2 | testcase.html | plm-testcase | 🟢 PRD-aligned |
| 7 | 文档 Document | F2.2/F3.1/F3.2 | prd.html/archdesign.html/dbdesign.html | plm-document | 🟢 PRD-aligned |
| 8 | 提测 Submission | F4.4 | submit.html | plm-submission | 🟢 PRD-aligned |
| 9 | 发布 Release | F4.7+ | release.html | plm-release | 🟢 PRD-aligned |
| 10 | 测试方案 TestPlan | F4.1 | testplan.html | plm-testplan | 🟢 PRD-aligned |
| 11 | 测试报告 TestReport | F4.7 | testreport.html | plm-testreport | 🟢 PRD-aligned |
| 12 | API 文档 ApiDoc | F5.4 | apidoc.html | plm-apidoc | 🟢 PRD-aligned |
| 13 | 产品手册 ManualProduct | F5.1 | productmanual.html | plm-manual-product | 🟢 PRD-aligned |
| 14 | 立项 Inception | F1.1 | inception.html | plm-inception | 🟢 PRD-aligned |
| 15 | UED | F2.3 | ued.html | plm-ued | 🟢 PRD-aligned |
| 16 | 竞品 Competitive | F1.3 | competitive.html | plm-competitive | 🟢 PRD-aligned |
| 17 | PRD 文档 | F2.2 | prd.html | plm-prd | 🟢 PRD-aligned |
| 18 | 架构设计 Arch | F3.1 | archdesign.html | plm-arch | 🟢 PRD-aligned |
| 19 | 数据库设计 DbDesign | F3.2 | dbdesign.html | plm-dbdesign | 🟢 PRD-aligned |
| 20 | 接口设计 ApiDesign | F3.3 | apidesign.html | plm-apidesign | 🟢 PRD-aligned |
| 21 | 测试数据 TestData | F4.3 | testdata.html | plm-testdata | 🟢 PRD-aligned |
| 22 | 自动化测试 AutoTest | F4.5 | autotest.html | plm-autotest | 🟡 空壳 |
| 23 | 实施手册 ManualImpl | F5.2 | implmanual.html | plm-manual-impl | 🟡 空壳 |
| 24 | 运维手册 ManualOps | F5.3 | opsmanual.html | plm-manual-ops | 🟡 空壳 |
| 25 | 效能分析 Analytics | - | analytics.html | plm-analytics | 🟡 空壳 |
| 26 | 工作台 Dashboard | - | dashboard.html | plm-dashboard | 🟡 空壳 |
| 27 | AI Agent | §2.3/§3.1 | aiagents.html | plm-ai-agent | 🟢 PRD-aligned |
| 28 | OpenSpec | - | aispec.html | plm-openspec | 🟡 空壳 |
| 29 | Pipeline | - | pipeline.html | plm-pipeline | 🟡 空壳 |
| 30 | Feature Flag | - | featureflag.html | plm-feature-flag | 🟡 空壳 |
| 31 | DORA | - | devops.html | plm-dora | 🟡 空壳 |
| **32** | **MCP Server** | **§2.5/§3.4/§4.1** | **settings.html#MCP集成 (Tab3)** | **plm-mcp** | **🆕 v0.x（Proposal 0007）** |
| **33** | **集成对接 Integration** | **§3.1/§3.5 Phase2** | **settings.html#MCP集成 (Tab3)** | **plm-integration** | **🆕 v0.x（Proposal 0007）** |

---

## 2. 字段对照表（Domain ↔ 原型表单元素 ↔ DB 列）

> 每个 PRD-aligned 模块一节。当前只填了 §32-33 两个新模块；其他已 PRD-aligned 模块的对照表参见 [02-设计/<模块>-数据库设计.md](02-设计/) 和 [02-设计/<模块>-API设计.md](02-设计/)。

### §32. MCP Server（plm-mcp）

**领域**: 把 PLM 自己的业务能力（项目/需求/任务/用例/文档/数据）通过 MCP 协议暴露给外部 LLM Agent（Claude Code、Cursor、Copilot 等）。
**PRD 出处**: §2.5 工具集清单 / §3.4 实现细节 / §4.1 信息架构 / 原型 [settings.html:157-180](prd和原型/AgriPLM-DevOps-原型/agriplm_split/settings.html)

#### 表 `tb_mcp_server` —— MCP Server 注册表

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| serverCode | server_code | VARCHAR(64) | §2.5 工具集分类 | 唯一编码：plm-core / plm-testcase / ... |
| serverName | server_name | VARCHAR(128) | §3.4 工具注册 | 展示名 |
| protocol | protocol | VARCHAR(16) | §3.4 "基于 MCP 协议规范" | `stdio` / `sse` / `http` |
| endpoint | endpoint | VARCHAR(512) | §3.4 | http 模式下的访问 URL |
| authType | auth_type | VARCHAR(16) | §3.4 "OAuth 2.0" | `none` / `token` / `oauth2` |
| oauthClientId | oauth_client_id | VARCHAR(128) | §3.4 "支持企业 SSO 对接" | OAuth 客户端 ID |
| oauthClientSecretEncrypted | oauth_client_secret_enc | VARCHAR(1024) | §3.4 | AES-256-GCM 密文 |
| toolsJson | tools_json | TEXT | §2.5 工具集 JSON Schema | 暴露的工具列表（JSON Schema 数组） |
| status | status | CHAR(1) | 原型 mcpTable 状态列 | 0=启用 1=停用 2=异常 |
| lastHealthAt | last_health_at | DATETIME | 原型 mcpTable "最后健康检查" | 心跳时间戳 |
| description | description | VARCHAR(500) | - | 描述 |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

#### 表 `tb_mcp_tool_audit` —— MCP 工具调用审计

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| serverId | server_id | BIGINT | §3.4 OAuth | FK→tb_mcp_server.id |
| toolName | tool_name | VARCHAR(128) | §2.5 工具命名 | 如 `project.list` / `requirement.create` |
| callerType | caller_type | VARCHAR(16) | §3.4 鉴权 | `user`/`agent`/`system` |
| callerId | caller_id | VARCHAR(128) | §3.4 | username / agent token id |
| paramsJson | params_json | TEXT | - | 调用参数 |
| resultStatus | result_status | CHAR(1) | - | 0=成功 1=失败 2=超时 |
| resultBrief | result_brief | VARCHAR(2000) | - | 截断到 2KB 的响应摘要（详细到日志） |
| latencyMs | latency_ms | INT | - | 耗时（ms） |
| createBy/createTime | (RuoYi 标准 2 字段) | | | 不可逻辑删除（审计） |

### §33. 集成对接 Integration（plm-integration）

**领域**: 管理与 飞书 / GitLab / 钉钉 / Jira / Figma / 禅道 / ZTF 等外部系统的连接器配置 + Webhook 入站事件。
**PRD 出处**: §3.1 产品边界 "通过 MCP/CLI 与 Jira、GitLab、飞书、钉钉、Figma、禅道、ZTF 等系统双向同步" / §3.5 Phase 2 "GitLab/飞书集成" / 原型 [settings.html:138-187](prd和原型/AgriPLM-DevOps-原型/agriplm_split/settings.html) "MCP集成" Tab

#### 表 `tb_integration_connector` —— 集成连接器配置

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| connectorCode | connector_code | VARCHAR(64) | §3.1 | 唯一编码：FEISHU-MAIN / GITLAB-OPS / ... |
| connectorName | connector_name | VARCHAR(128) | - | 展示名 |
| connectorType | connector_type | VARCHAR(32) | §3.1 / §3.5 Phase2 | 字典 `biz_integration_type`：feishu/gitlab/dingtalk/jira/figma/zentao/ztf |
| endpoint | endpoint | VARCHAR(512) | - | 外部系统 base URL (GitLab self-hosted 用) |
| authType | auth_type | VARCHAR(16) | - | 字典 `biz_integration_auth`：app_secret/access_token/oauth2/pat |
| credentialEncrypted | credential_enc | VARCHAR(2048) | - | AES-256-GCM 加密的 JSON（含 app_id/app_secret/token/refresh_token 等） |
| webhookUrl | webhook_url | VARCHAR(512) | - | 本系统对外暴露的 webhook 入口（计算字段，仅查询返回） |
| webhookSecret | webhook_secret | VARCHAR(256) | - | 验签密钥（明文存数据库 OK，因为只用作 HMAC，不能反推出更敏感数据） |
| configJson | config_json | TEXT | - | 类型特定配置（机器人 chat_id / 项目映射等） |
| status | status | CHAR(1) | 原型表格 状态列 | 字典 `biz_integration_status`：0=启用 1=停用 2=异常 |
| lastSyncAt | last_sync_at | DATETIME | 原型表格 "最后同步" | |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

#### 表 `tb_integration_webhook_event` —— 入站 Webhook 事件流水

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| connectorId | connector_id | BIGINT | - | FK→tb_integration_connector.id |
| eventType | event_type | VARCHAR(128) | 各外部系统的 webhook 类型 | 如 `feishu.im.message.receive_v1` / `gitlab.merge_request` |
| externalEventId | external_event_id | VARCHAR(128) | - | 外部 event id（幂等键） |
| payloadJson | payload_json | LONGTEXT | - | 原始 payload |
| signature | signature | VARCHAR(512) | - | 签名头（验签用） |
| signatureVerified | signature_verified | CHAR(1) | - | 0=验签失败 1=通过 |
| processStatus | process_status | CHAR(1) | - | 字典 `biz_webhook_status`：0=待处理 1=处理中 2=成功 3=失败 4=已忽略 |
| processError | process_error | VARCHAR(2000) | - | 失败原因 |
| retryCount | retry_count | INT | - | 重试次数 |
| createTime / processTime | (RuoYi 标准) | | | 不可删除（审计） |
| sourceIp | source_ip | VARCHAR(64) | - | 调用方 IP（防滥用） |

### §34. AI Agent 编排（plm-ai-agent）

**领域**: 管理开发过程中各个岗位的 AI Agent，包括需求分析/PRD生成/代码审查/测试用例/发布评审/运维巡检 6 类 Agent。
**PRD 出处**: §2.3 AI能力矩阵（18个Dify工作流）/ §3.1 目标用户（7个产研岗位）/ 原型 [aiagents.html](prd和原型/AgriPLM-DevOps-原型/agriplm_split/aiagents.html) `state.aiAgents`

#### 表 `tb_ai_agent` —— AI Agent 配置表

| Java field | 列 | 类型 | PRD/原型出处 | 说明 |
|---|---|---|---|---|
| id | id | BIGINT | - | 主键 |
| agentNo | agent_no | VARCHAR(32) | 原型 `a.id` AGT-001 | 唯一编号 AGT-YYYY-NNNN |
| agentName | agent_name | VARCHAR(128) | 原型 `a.name` | 如「需求分析Agent」 |
| agentRole | agent_role | VARCHAR(32) | §3.1 7个产研岗位 | 字典 `biz_agent_role` |
| agentType | agent_type | VARCHAR(32) | §2.3 工作流分类 | 字典 `biz_agent_type` |
| modelName | model_name | VARCHAR(128) | 原型 `a.model` | DeepSeek-V3 / Claude Sonnet 4.6 / DeepSeek-R1 |
| difyFlowId | dify_flow_id | VARCHAR(128) | §2.3 18个工作流ID | 如 `requirements-flow` |
| toolsJson | tools_json | TEXT | 原型 `a.tools` | JSON数组，如 `["agrikb_search","req_template"]` |
| status | status | CHAR(1) | 原型 `a.status` 运行中/待机 | 字典 `biz_agent_status`：0=运行中 1=待机 2=异常 |
| callsToday | calls_today | INT | 原型 `a.calls_today` | 今日调用次数（每日凌晨重置） |
| successRate | success_rate | DECIMAL(5,2) | 原型 `a.success_rate` | 成功率 0.00~100.00 |
| avgLatency | avg_latency | VARCHAR(32) | 原型 `a.avg_latency` | 平均响应时长，如 1.8s |
| description | description | VARCHAR(500) | 原型 `a.desc` | 功能描述 |
| createBy/createTime/updateBy/updateTime/remark/delFlag | (RuoYi 标准 6 字段) | | | |

---

## 3. 状态机汇总

### §32 MCP Server 状态机（tb_mcp_server.status）

```
启用(0) ⇄ 停用(1)
启用(0) → 异常(2) [心跳失败 N 次]
异常(2) → 启用(0) [手动恢复或心跳恢复]
异常(2) → 停用(1)
```

| 当前 | 允许目标 |
|---|---|
| 0 启用 | 1 停用 / 2 异常 |
| 1 停用 | 0 启用 |
| 2 异常 | 0 启用 / 1 停用 |

非法转换 → `ServiceException(701)`。

### §33 Integration Connector 状态机（tb_integration_connector.status）

同上结构：启用(0) ⇄ 停用(1)；启用(0) → 异常(2)；异常(2) → 启用(0) / 停用(1)。

### §33 Webhook Event 状态机（tb_integration_webhook_event.process_status）

```
待处理(0) → 处理中(1)
处理中(1) → 成功(2) / 失败(3) / 已忽略(4)
失败(3) → 处理中(1) [重试]
```

非法转换 → `ServiceException(701)`。

### §34 AI Agent 状态机（tb_ai_agent.status）

```
运行中(0) → 待机(1)   [人工暂停]
待机(1)   → 运行中(0) [人工启动]
运行中(0) → 异常(2)   [系统写入，心跳失败]
异常(2)   → 运行中(0) [人工恢复]
异常(2)   → 待机(1)   [人工关停]
```

| 当前 | 人工操作允许目标 |
|---|---|
| 0 运行中 | 1 待机 |
| 1 待机 | 0 运行中 |
| 2 异常 | 0 运行中 / 1 待机 |

非法转换 → `ServiceException(701)`。

---

## 4. 错误码登记表

| 代码 | 名称 | HTTP | 出处 | 示例 |
|---|---|---|---|---|
| 601 | 参数为空/非法 | 400 | 通用（已用） | `项目名称不能为空` |
| 604 | 业务规则冲突 | 400 | 通用（已用） | `起始日期不能晚于结束日期` |
| 701 | 非法状态转换 | 400 | 通用（已用） | `状态「停用」不能直接转到「异常」` |
| **801** | **MCP Server 不存在** | 404 | 本提案 (Proposal 0007) | `MCP Server [server_code] 不存在` |
| **802** | **MCP 工具未注册** | 404 | 本提案 | `工具 [project.list] 在 [plm-core] 中未注册` |
| **803** | **MCP OAuth 失败** | 401 | 本提案 | `OAuth 验证失败 / token 已过期` |
| **804** | **MCP 工具调用失败** | 502 | 本提案 | `MCP Server 返回非预期错误` |
| **805** | **集成连接器未配置** | 404 | 本提案 | `Connector [FEISHU-MAIN] 未找到` |
| **806** | **集成 token 过期 / 无效** | 401 | 本提案 | `tenant_access_token 已失效，请重新刷新` |
| **807** | **Webhook 验签失败** | 401 | 本提案 | `飞书 verification_token 不匹配 / GitLab X-Gitlab-Token 失败` |
| **808** | **外部 API 速率限制** | 429 | 本提案 | `飞书 OpenAPI 触发限流，请稍候` |
| **809** | **凭据加密 key 未配置** | 500 | 本提案（启动期 fail-fast） | `MCP_ENCRYPT_KEY 未设置，拒绝启动` |
| **810** | **不支持的 connector_type** | 400 | 本提案 | `connector_type = xxx 暂不支持，仅支持: feishu/gitlab/...` |
| **811** | **AI Agent 不存在** | 404 | §34 | `Agent [id] 不存在` |
| **812** | **AI Agent 非法状态转换** | 400 | §34 | `状态「运行中」不能转为「异常」（系统写入）` |

---

## 5. URL 路径与菜单

### REST API 路径

| 模块 | 列表 | 详情 | 新增 | 修改 | 删除 |
|---|---|---|---|---|---|
| MCP Server | `GET /business/mcp/server/list` | `GET /business/mcp/server/{id}` | `POST /business/mcp/server` | `PUT /business/mcp/server` | `DELETE /business/mcp/server/{ids}` |
| MCP 工具审计 | `GET /business/mcp/audit/list` | `GET /business/mcp/audit/{id}` | (只读) | (只读) | (只读) |
| MCP 协议端点 | `POST /mcp/tools/list` / `POST /mcp/tools/call` | - | - | - | - |
| 集成连接器 | `GET /business/integration/connector/list` | `GET /business/integration/connector/{id}` | `POST /business/integration/connector` | `PUT /business/integration/connector` | `DELETE /business/integration/connector/{ids}` |
| 集成 - 测试连通性 | `POST /business/integration/connector/{id}/test` | - | - | - | - |
| Webhook 事件 | `GET /business/integration/webhook/list` | `GET /business/integration/webhook/{id}` | (只读) | (只读，仅重试) | (只读) |
| Webhook 入站 - 飞书 | `POST /integration/webhook/feishu/{connectorId}` | - | - | - | - |
| Webhook 入站 - GitLab | `POST /integration/webhook/gitlab/{connectorId}` | - | - | - | - |
| AI Agent | `GET /business/ai-agent/list` | `GET /business/ai-agent/{id}` | `POST /business/ai-agent` | `PUT /business/ai-agent` | `DELETE /business/ai-agent/{ids}` |
| AI Agent 状态切换 | - | - | - | `PUT /business/ai-agent/{id}/status` | - |

**注意**：
- `/business/*` 走 Spring Security JWT + `@PreAuthorize("@ss.hasPermi('business:<entity>:<action>')")`
- `/mcp/*` 走 OAuth/Token，独立鉴权过滤器（PRD §3.4）
- `/integration/webhook/*` 公网入口，走 HMAC 验签，不走 JWT；在 [SecurityConfig](plm-backend/plm-framework/src/main/java/cn/com/bosssfot/dv/plm/framework/config/SecurityConfig.java) 的 `permitAll` 列表中开放

### 菜单 ID 段（避开现有）

- 业务管理 = 2000（已用）
- **MCP 集成 = 2400**（一级目录）
  - MCP Server = 2410
    - 查询 2411 / 新增 2412 / 修改 2413 / 删除 2414 / 导出 2415
  - 调用审计 = 2420
    - 查询 2421 / 导出 2425
- **外部集成 = 2500**（一级目录）
  - 连接器配置 = 2510
    - 查询 2511 / 新增 2512 / 修改 2513 / 删除 2514 / 测试 2516
  - Webhook 事件 = 2520
    - 查询 2521 / 重试 2522 / 导出 2525
- **AI 能力 = 2600**（一级目录）
  - AI Agent 编排 = 2610
    - 查询 2611 / 新增 2612 / 修改 2613 / 删除 2614 / 导出 2615

### 权限串

- `business:mcp:server:list/add/edit/remove/export/query`
- `business:mcp:audit:list/query/export`
- `business:integration:connector:list/add/edit/remove/test/query`
- `business:integration:webhook:list/retry/query/export`
- `business:ai-agent:list/add/edit/remove/export/query`

---

## 6. 字典类型登记

| 字典类型 | 字典名 | 使用列 | 数据项 |
|---|---|---|---|
| `biz_mcp_protocol` | MCP 协议类型 | mcp_server.protocol | stdio / sse / http |
| `biz_mcp_auth` | MCP 鉴权类型 | mcp_server.auth_type | none / token / oauth2 |
| `biz_mcp_status` | MCP 状态 | mcp_server.status | 0=启用 1=停用 2=异常 |
| `biz_audit_result` | 审计结果 | mcp_tool_audit.result_status | 0=成功 1=失败 2=超时 |
| `biz_integration_type` | 集成类型 | integration_connector.connector_type | feishu / gitlab / dingtalk / jira / figma / zentao / ztf |
| `biz_integration_auth` | 集成鉴权 | integration_connector.auth_type | app_secret / access_token / oauth2 / pat |
| `biz_integration_status` | 集成状态 | integration_connector.status | 0=启用 1=停用 2=异常 |
| `biz_webhook_status` | Webhook 处理状态 | integration_webhook_event.process_status | 0=待 1=中 2=成 3=败 4=略 |
| `biz_agent_role` | AI Agent 岗位 | ai_agent.agent_role | product_manager / ued / dev / test / pm / impl / dept_head |
| `biz_agent_type` | AI Agent 类型 | ai_agent.agent_type | requirement / prd_gen / code_review / test_case / release / ops |
| `biz_agent_status` | AI Agent 状态 | ai_agent.status | 0=运行中 1=待机 2=异常 |

---

## 7. v0.x 路线图

- ✅ Project / Requirement / Sprint / Task / Defect / TestCase / Document / Submission / Release / TestPlan / TestReport / ApiDoc / ManualProduct
- 🆕 **MCP / Integration**（本提案 0007）
- 下一批：Inception / UED / Competitive / PRD / Arch / DbDesign / ApiDesign

---

## 8. 9 项 DoD（每个 PRD-aligned 模块必过）

> 见 [.claude/rules.md §M.2](.claude/rules.md)

1. ☐ 在本文件 §2 追加该模块的字段对照表
2. ☐ §3 状态机入文件
3. ☐ §4 错误码入文件
4. ☐ §5 URL/菜单/权限入文件
5. ☐ §6 字典入文件
6. ☐ Domain/Mapper/Service/Controller/XML 5 件套齐
7. ☐ SQL DDL + 回滚脚本 + 菜单 seed
8. ☐ Phase 02 设计 Gate Checklist 实例签字
9. ☐ Phase 03 开发 Gate Checklist 实例签字

---

## 9. 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 初版；初始化 SSoT；按 Proposal 0007 加入 §32 MCP / §33 Integration |
| 2026-05-18 | Wjl + Claude | §34 AI Agent 编排模块 PRD-aligned；补字段对照表/状态机/错误码/URL/字典 |
| 2026-05-18 | Wjl + Claude | Group A 7 模块前端升级 🟡→🟢：competitive/prd/ued/arch/dbdesign/apidesign/testdata；新增 types/api/views/router/index |

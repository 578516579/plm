# Dify AI 编排集成 — 系统设计

> 状态: **V1 落地完成** (2026-05-17) — Mock 默认 + HTTP 真调可选
> 关联: [PRD §2.3 AI能力矩阵](../prd和原型/AgriAI-PLM-完整PRD文档.md) · [原型 aiagents.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/aiagents.html) · 表 `tb_ai_agent`
> 错误码: **708** "AI 调用失败"

---

## 1. 设计目标

| 目标 | 怎么做 |
|---|---|
| 业务模块解耦 — 不关心 Dify 细节 | 抽 `DifyService` 门面接口,放 `plm-common` |
| 本地零依赖 — 开发机不需要 Dify 实例 | 默认装配 `DifyServiceMockImpl`,返回占位 outputs |
| CI / E2E 稳定 — 120+ 测试不抖动 | `plm.dify.enabled=false` 默认,Mock 始终 success=true |
| 生产可切真调 — 一行配置切换 | `DIFY_ENABLED=true` + `DIFY_API_KEY=xxx` |
| 故障隔离 — Dify 挂掉不阻塞 PLM | HTTP 异常吃掉,返回 `success=false`,业务侧 708 |
| 审计 — 调用次数/成功率/耗时/tokens | 写回 `tb_ai_agent` 的 totalCalls/successRate/lastInvokedAt |
| 安全 — api-key 不入库不入 git | 走 `${DIFY_API_KEY}` 环境变量,`.env.example` 占位 |

---

## 2. 架构

```
                                    plm-business 模块 (ai-agent / inception / competitive / ...)
                                                |
                                                v
                ┌───────────────────────────────────────────────────────────┐
                │  DifyService (接口)  —— plm-common.dify                    │
                │   .runWorkflow(workflowId, inputs) → DifyWorkflowResult    │
                │   .runWorkflowByType(agentType, inputs) → ...              │
                │   .isLive()  // true=HTTP, false=Mock                      │
                └───────────────────────────────────────────────────────────┘
                          │                                  │
              isUsable()=true                       isUsable()=false
                          │                                  │
                          v                                  v
              DifyServiceHttpImpl                  DifyServiceMockImpl
              ─────────────────────                ─────────────────────
              POST /workflows/run                  返回 {mock:true, echo:inputs}
              response_mode=blocking               success=true (永不报错)
              Bearer <api-key>                     不发任何网络请求
              5s 建连 / 60s 读                     E2E / 本地启动友好
```

**装配开关** (`DifyProperties.isUsable()`):

```java
enabled=true && apiKey 非空 && apiKey != "please-change-me" && baseUrl 非空
```

---

## 3. 配置契约

### application.yml — `plm.dify.*`

| Key | 默认 | 说明 |
|---|---|---|
| `enabled` | `${DIFY_ENABLED:false}` | 总开关 |
| `base-url` | `${DIFY_BASE_URL:https://api.dify.ai/v1}` | Dify Service API 根地址(私有部署改成内网地址) |
| `api-key` | `${DIFY_API_KEY:}` | **必须**走环境变量 |
| `connect-timeout-ms` | 5000 | TCP 建连超时 |
| `read-timeout-ms` | 60000 | 推理超时,workflow 通常 10~60s |
| `default-user` | `plm-system` | blocking 模式需要的 user 字段 |
| `workflows.requirement` 等 6 项 | 空 | agent_type → workflow_id 兜底路由 |

### 环境变量 (`.env.example` / K8s Secret)

```
DIFY_ENABLED=false                    # ← 生产改 true
DIFY_BASE_URL=https://api.dify.ai/v1  # 或私有 Dify 地址
DIFY_API_KEY=                         # ← 生产填 Bearer
DIFY_WF_REQUIREMENT=wf-xxxxxxxx       # 可选,tb_ai_agent.dify_workflow_id 优先
... (共 6 个)
```

---

## 4. 路由优先级

```
AiAgent.invoke(id)
  ├─ tb_ai_agent.dify_workflow_id ≠ null  → difyService.runWorkflow(wfId, inputs)
  └─ 否则                                  → difyService.runWorkflowByType(agent_type, inputs)
                                              └─ 查 plm.dify.workflows[type]
                                                 └─ 命中  → runWorkflow
                                                 └─ 未命中 → fail("agent_type 未映射")
```

---

## 5. Dify Service API 调用细节

**端点**: `POST {base-url}/workflows/run`
**Headers**: `Authorization: Bearer <api-key>` · `Content-Type: application/json`

**请求体**:
```json
{
  "workflow_id": "wf-xxxxxxxx",
  "inputs": { "agent_no": "...", "agent_name": "...", "agent_type": "ops", "description": "...", "prompt_template": "..." },
  "response_mode": "blocking",
  "user": "plm-system"
}
```

**响应**:
```json
{
  "workflow_run_id": "wfr-...",
  "task_id": "task-...",
  "data": {
    "status": "succeeded",         // 或 "failed"
    "outputs": { ... },
    "elapsed_time": 12.34,
    "total_tokens": 1234,
    "error": null
  }
}
```

**失败兜底**: 任何 4xx/5xx/Timeout/JSON 解析异常 → `DifyWorkflowResult.fail(msg)`,业务层抛 `ServiceException(708, ...)`。

---

## 6. 19 workflow 路由蓝图 (PRD §2.3)

V1 配置层只映射 6 类核心 Agent(对齐 `tb_ai_agent.agent_type`):

| agent_type | 对应 PRD workflow | env 变量 |
|---|---|---|
| requirement | requirements-flow / prd-generation-flow | `DIFY_WF_REQUIREMENT` |
| prd | prd-generation-flow | `DIFY_WF_PRD` |
| code | coding-assist-flow | `DIFY_WF_CODE` |
| test | testcase-gen-flow / test-plan-flow / unit-test-flow | `DIFY_WF_TEST` |
| release | (待定:发布相关) | `DIFY_WF_RELEASE` |
| ops | ops-manual-flow / 监控告警 | `DIFY_WF_OPS` |

剩余 13 个 workflow (project-inception-flow / competitive-analysis-flow / arch-design-flow / db-design-flow / ued-review-flow / detail-design-flow / api-doc-flow / data-gen-flow / product-manual-flow / impl-manual-flow / test-report-flow) 由各业务模块 `aiGenerate()` 方法 **后续接入** — V1 保留 mock 实现,接入时改一行 `difyService.runWorkflow(...)`。

---

## 7. 调用方接入指南 (后续 18 模块迁移模板)

```java
// 改造前 (各 *ServiceImpl.aiGenerate)
public Foo aiGenerate(Long id) {
    Foo f = mapper.selectFooById(id);
    f.setAiGenerated("# Mock\n...");          // ← hardcoded mock
    mapper.updateFoo(f);
    return f;
}

// 改造后
@Autowired private DifyService difyService;

public Foo aiGenerate(Long id) {
    Foo f = mapper.selectFooById(id);
    Map<String,Object> inputs = Map.of("title", f.getTitle(), ...);
    DifyWorkflowResult r = difyService.runWorkflowByType("requirement", inputs);
    if (!r.isSuccess()) throw new ServiceException("AI 生成失败: " + r.getErrorMessage(), 708);
    f.setAiGenerated(String.valueOf(r.getOutputs().get("markdown")));
    mapper.updateFoo(f);
    return f;
}
```

---

## 8. 健康检查端点

`GET /business/ai-agent/dify/health` — 不需 PreAuthorize(可后续加权限),返回:

```json
{
  "code": 200, "msg": "操作成功",
  "enabled": false,
  "usable": false,
  "live": false,
  "baseUrl": "https://api.dify.ai/v1",
  "workflowsMapped": 0,
  "mode": "mock"
}
```

前端可在「系统设置 / AI Agent 管理」页顶部展示徽章:🟢 真调 / 🟡 Mock。

---

## 9. 安全 & 合规

| 风险 | 缓解 |
|---|---|
| api-key 泄漏 | `.env.example` 占位空串;实际值走环境变量;`/dify/health` 不返回 key |
| Dify 实例不可用阻塞 PLM 主流程 | HTTP 异常吃掉 → mock 不可能拿不到响应 → 业务侧只抛 708 |
| 调用大量 token 失控成本 | totalCalls + successRate + 单次 tokens 写库,可定期审计 |
| 私有数据外泄 | 私有化部署 Dify (改 `base-url`);Service API 已限于 prompt+inputs,不传文件 |
| 超时 hang | `connect-timeout-ms=5000` / `read-timeout-ms=60000` 硬上限 |

---

## 10. 测试覆盖

- `tests-business/ai-agent.spec.ts:35` (TC-AIAGENT-F002) — invoke 累计 + successRate>0 ✅
- mock 模式始终 success=true → successRate=100,断言 `> 0` 通过
- 真调模式需在 staging 跑独立测试(配 `DIFY_ENABLED=true` + `DIFY_API_KEY`),不在 CI 范围

---

## 11. 后续 (V2 路线)

| 项 | 优先级 |
|---|---|
| `ai_invocation_log` 表 — 每次调用入库,审计/追溯/计费 | P1 |
| streaming 模式 — SSE 推送 (前端实时显示 token) | P2 |
| 19 模块全量接入 — 把 18 个 `aiGenerate` 改造为真调 | P1 |
| Dify file_upload — 上传 PRD/设计稿 给 workflow | P2 |
| 限流 / 配额 — Bucket4j 或 Redis sliding window | P2 |
| 多 LLM 提供商 — DeepSeek/Claude/通义切换 (走 Dify 内部配置即可) | P3 |
| MCP 集成 — Dify 调 Claude Code (PRD 提到的 coding-assist-flow) | P2 |

---

## 12. 变更记录

| 日期 | 版本 | 变更 |
|---|---|---|
| 2026-05-17 | V1.0 | 首版落地:`plm-common.dify` 包 + Mock/HTTP 双实现 + AiAgent.invoke 改造 + health 端点 |

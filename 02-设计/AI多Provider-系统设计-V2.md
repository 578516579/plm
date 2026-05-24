# AI 多 Provider 集成 — 系统设计 V2

> 状态: **V2 落地完成** (2026-05-18) — 4 Provider 自动装配 + 路由 + 健康检查
> 上版: [Dify集成-系统设计.md](Dify集成-系统设计.md) (V1,Dify 单 provider)
> 关联: [PRD §2.3 AI能力矩阵](../prd和原型/AgriAI-PLM-完整PRD文档.md) · 表 `tb_ai_agent`
> 错误码: **708** "AI 调用失败" · **604** "无效的 provider"

---

## 1. 设计目标(相对 V1)

V1 只支持 Dify workflow,本期扩展为**多 Provider 并存**:

| 目标 | 怎么做 |
|---|---|
| 支持 LLM 原生 API | 新增 `OpenAiCompatibleProvider` + `AnthropicProvider`,直接打 LLM 厂商 API,无需 Dify 中间层 |
| 支持国产化 LLM | OpenAI 协议兼容 DeepSeek/通义/Moonshot/SiliconFlow/智谱 GLM,改 base-url 即可 |
| 业务 agent 可配置 provider | `tb_ai_agent` 加 `provider` + `model_name` 字段,前端表单可选 |
| 按 agent 粒度路由 | `AiServiceImpl.pick()` 按 `request.provider` 路由,降级到 default,再降级到 mock |
| 保留 Dify 编排能力 | `DifyAiProvider` 委托 V1 的 `DifyService`,workflow 模式完全兼容 |
| 故障隔离 | 任一 provider 异常都返回 `success=false`,业务侧 708 |
| 0 → 1 切换零代码 | 改 yml/env + 重启,无需改 java |

---

## 2. 架构

```
                    业务模块 (plm-ai-agent / plm-inception / plm-competitive / ...)
                                          │
                                          ▼
                ┌──────────────────────────────────────────────┐
                │     AiService (统一门面 - plm-common.ai)      │
                │      .chat(AiChatRequest) → AiChatResult     │
                │      .providerStatus()                       │
                │      .defaultProvider()                      │
                └──────────────────────────────────────────────┘
                                          │
                       AiServiceImpl 按 req.provider 路由
                                          │
            ┌────────────┬───────────────┼───────────────┬────────────┐
            ▼            ▼               ▼               ▼            ▼
       MockAiProvider  OpenAiCompat  AnthropicProvider  DifyAiProvider
       (默认装配)       (兼容协议)     (Claude API)      (委托 V1)
       永远 available  isUsable() 判定 isUsable() 判定   isLive() 判定
            │                              │                │
            │                              │                ▼
            │                              │        DifyService (V1 已落地)
            │                              │        ├ DifyServiceHttpImpl
            │                              │        └ DifyServiceMockImpl
            ▼                              ▼
       {echo prompt}            POST /chat/completions  POST /v1/messages
                                 Bearer <api-key>        x-api-key + version
```

---

## 3. 表结构变化

### 新增字段 (`tb_ai_agent`)

| 字段 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `provider` | `varchar(20)` NOT NULL | `'mock'` | 字典 `biz_ai_provider`:mock / dify / openai / anthropic |
| `model_name` | `varchar(120)` NULL | NULL | provider≠dify 时是模型名(gpt-4o-mini / deepseek-chat / claude-sonnet-4-5);provider=dify 时可空(走 `dify_workflow_id`) |

保留: `dify_workflow_id` 仅当 `provider='dify'` 时使用。

### 新增字典 `biz_ai_provider`

| dict_value | dict_label | 说明 |
|---|---|---|
| mock | Mock 占位 | 本地/降级,永远可用 |
| dify | Dify 编排 | workflow 编排,适合多节点流程 |
| openai | OpenAI 兼容 | 覆盖 OpenAI/DeepSeek/通义/Moonshot 等 |
| anthropic | Anthropic | Claude Messages API |

迁移脚本: `sql/migration-2026-05-18-ai-multi-provider.sql`(幂等,可重复执行)。

---

## 4. 配置契约

### `application.yml` — `plm.ai.*`

```yaml
plm:
  ai:
    default-provider: ${AI_DEFAULT_PROVIDER:mock}   # 业务 agent 未指定时兜底
    openai:
      enabled: ${AI_OPENAI_ENABLED:false}
      base-url: ${AI_OPENAI_BASE_URL:https://api.openai.com/v1}
      api-key: ${AI_OPENAI_API_KEY:}
      default-model: ${AI_OPENAI_DEFAULT_MODEL:gpt-4o-mini}
      connect-timeout-ms: 5000
      read-timeout-ms: 60000
    anthropic:
      enabled: ${AI_ANTHROPIC_ENABLED:false}
      base-url: ${AI_ANTHROPIC_BASE_URL:https://api.anthropic.com}
      api-key: ${AI_ANTHROPIC_API_KEY:}
      default-model: ${AI_ANTHROPIC_DEFAULT_MODEL:claude-sonnet-4-5}
      version: ${AI_ANTHROPIC_VERSION:2023-06-01}
      connect-timeout-ms: 5000
      read-timeout-ms: 60000
  dify: { ... }    # V1 配置保留,DifyAiProvider 内部委托
```

### 一行换厂商 (OpenAI 兼容协议)

| 服务 | base-url | 典型 model |
|---|---|---|
| OpenAI | `https://api.openai.com/v1` | `gpt-4o-mini` / `gpt-4o` |
| DeepSeek | `https://api.deepseek.com/v1` | `deepseek-chat` / `deepseek-reasoner` |
| 通义千问 | `https://dashscope.aliyuncs.com/compatible-mode/v1` | `qwen-max` / `qwen-turbo` |
| Moonshot | `https://api.moonshot.cn/v1` | `moonshot-v1-8k` |
| SiliconFlow | `https://api.siliconflow.cn/v1` | 各厂商在该平台模型名 |
| 智谱 GLM | `https://open.bigmodel.cn/api/paas/v4` | `glm-4-plus` |

只需改 yml 中的 2 行 + env 中的 api-key,无需改 java 代码。

---

## 5. 路由优先级 (AiServiceImpl.pick())

```
chat(request)
  │
  ├─ request.provider 非空 且 providers[req.provider].isAvailable() → 用之
  │
  ├─ default-provider (plm.ai.default-provider) 已装配且可用 → 用之
  │
  └─ mock (永远装配,永远 available) → 兜底
```

**装配策略**:全部 4 个 provider 都注册成 Spring Bean(Mock 永远,其余永远注册,运行期通过 `isAvailable()` 判定)。这样:
- 重启不需要改容器结构
- 运行期热切换:改 env + 重启即可启用/禁用某 provider

---

## 6. 各 Provider 协议细节

### 6.1 OpenAiCompatibleProvider

- **端点**: `POST {base-url}/chat/completions`
- **Header**: `Authorization: Bearer <api-key>` · `Content-Type: application/json`
- **请求**:
  ```json
  {
    "model": "deepseek-chat",
    "messages": [
      { "role": "system", "content": "你是 PLM 资深架构师" },
      { "role": "user", "content": "请生成需求文档..." }
    ],
    "temperature": 0.5,
    "max_tokens": 2000,
    "stream": false
  }
  ```
- **响应**: `choices[0].message.content` + `usage.{prompt_tokens, completion_tokens, total_tokens}`

### 6.2 AnthropicProvider

- **端点**: `POST {base-url}/v1/messages`
- **Header**: `x-api-key: <api-key>` · `anthropic-version: 2023-06-01`
- **关键差异**(相对 OpenAI):
  - `system` 是顶层字段,不在 `messages` 里
  - `max_tokens` 必填
  - 响应 `content` 是数组,取 `[0].text`
  - `usage.{input_tokens, output_tokens}` (不是 prompt/completion)

### 6.3 DifyAiProvider

委托 V1 的 `DifyService.runWorkflow(workflowId, inputs)`:
- `req.model` 字段被复用为 Dify workflow_id
- `req.difyInputs` 直接作为 workflow inputs
- `req.system` / `req.firstUserContent()` 自动合并进 inputs 的 `system` / `query` key
- 输出归一:依次取 `outputs.text` / `outputs.output` / 整个 outputs 序列化

### 6.4 MockAiProvider

- 不发任何网络请求
- 输出固定 `"[mock] system=... user=..."`
- 永远 `success=true`
- 保护本地零依赖启动 + CI/E2E 稳定

---

## 7. 接入示例

### 业务模块 (任意 plm-* 业务模块) 用 AiService

```java
@Autowired private AiService aiService;

public Foo aiGenerate(Long id) {
    Foo f = mapper.selectById(id);
    AiChatRequest req = AiChatRequest.builder(f.getProvider())   // "openai" / "anthropic" / "dify" / "mock"
        .model(f.getModelName())                                   // "deepseek-chat" / "claude-sonnet-4-5" / Dify workflow_id
        .system("你是 PLM 资深需求分析师")
        .user("请基于以下信息生成需求文档:\n" + f.getTitle())
        .temperature(0.5)
        .maxTokens(2000)
        .callerTag("foo#" + id)
        .build();

    AiChatResult r = aiService.chat(req);
    if (!r.isSuccess()) throw new ServiceException("AI 失败:" + r.getError(), 708);
    f.setAiGenerated(r.getText());
    f.setLastAiTokens(r.getTotalTokens());
    mapper.update(f);
    return f;
}
```

### AiAgent 模块(已落地)

`AiAgentServiceImpl.invoke()` 内部就是上述模板的实现 — 把 tb_ai_agent 当前行的 provider / model / promptTemplate / description 喂进 AiChatRequest。

---

## 8. 健康检查端点

### `GET /business/ai-agent/ai/health` (本期新增)

```json
{
  "code": 200, "msg": "操作成功",
  "defaultProvider": "mock",
  "providers": {
    "mock": true,
    "openai": false,
    "anthropic": false,
    "dify": false
  },
  "openaiEnabled": false,
  "openaiBaseUrl": "https://api.openai.com/v1",
  "openaiModel": "gpt-4o-mini",
  "anthropicEnabled": false,
  "anthropicBaseUrl": "https://api.anthropic.com",
  "anthropicModel": "claude-sonnet-4-5",
  "difyUsable": false
}
```

### `GET /business/ai-agent/dify/health` (V1,保留)

只看 Dify 一家的状态,V1 行为不变。

---

## 9. 安全 & 合规

| 风险 | 缓解 |
|---|---|
| api-key 泄漏 | 全部 `${ENV:default}`;占位为空串;`/ai/health` 不返回 key,只返回 enabled/baseUrl |
| 私有数据外泄 | 私有部署:openai 改成自家网关地址;anthropic 走代理或 AWS Bedrock 等;dify 走私有 Dify |
| 任一厂商挂掉影响 PLM | 每个 Provider 异常都被吃掉,fallback 链 mock 兜底,业务侧 708 |
| Token 失控成本 | `AiChatResult.totalTokens` 写库,可按 agent 聚合审计 |
| 超时 hang | 各 Provider 独立 timeout,默认 5s/60s |
| 跨境合规 | 国内项目可只配 DeepSeek/通义,base-url 永不出境 |

---

## 10. V1 → V2 兼容性

| 项 | V1 | V2 | 兼容性 |
|---|---|---|---|
| `DifyService` API | 提供 | **保留** | ✅ V1 调用方无需改 |
| `tb_ai_agent.dify_workflow_id` | 主路由字段 | 仅 `provider=dify` 时使用 | ✅ V1 已写入的数据被自动迁移为 `provider='dify'` |
| `AiAgentServiceImpl.invoke()` | 直接调 DifyService | 改走 AiService | ⚠ 内部变化,外部 REST API 不变 |
| `tb_ai_agent` 表结构 | 无 provider/model | 新增 2 列 | 迁移脚本幂等 |
| `application.yml` | 仅 `plm.dify.*` | 增加 `plm.ai.*` | ✅ V1 配置不动 |
| E2E `ai-agent.spec.ts` | TC-AIAGENT-F002 | **不变**,默认 mock 仍 success=true | ✅ 0 退步 |

---

## 11. 测试覆盖

### 现有 E2E 兼容(已验证 120/120 通过)
- `ai-agent.spec.ts` TC-AIAGENT-F001/F002/F003 → mock 路径
- 全套件 120 测试 → 任何业务调用 AI 都走 mock,稳定 success=true

### 生产真调验证(本期范围外,建议 staging 跑)
- 配 `AI_OPENAI_ENABLED=true` + 真实 deepseek key + AI_OPENAI_BASE_URL=https://api.deepseek.com/v1
- 创建 agent provider='openai', model_name='deepseek-chat'
- 调 `/business/ai-agent/invoke/{id}` → 后端日志看 `[Ai-openai] xxx ok, tokens=N, elapsed=Xms`
- 改 provider='anthropic' + 真实 anthropic key → 同样验证

---

## 12. 后续 V3 路线

| 项 | 优先级 |
|---|---|
| 18 个业务模块的 `aiGenerate/aiAnalyze` 改造为真调 (模板已在 §7) | P1 |
| `ai_invocation_log` 审计表 — 每次调用入库(provider/model/tokens/elapsed/error) | P1 |
| streaming 模式(SSE 推 token) — 用户体验提升 | P2 |
| 多 provider 内部负载均衡(同时配多家,故障转移) | P2 |
| Token 配额 / 限流 (Bucket4j / Redis sliding window) | P2 |
| Tool use / function calling 支持 | P2 |
| AWS Bedrock / Azure OpenAI / GCP Vertex 适配器 | P3 |
| MCP 集成 — Dify 调 Claude Code | P3 |

---

## 13. 变更记录

| 日期 | 版本 | 变更 |
|---|---|---|
| 2026-05-17 | V1.0 | Dify 集成首版 — `plm-common.dify` 包 + Mock/HTTP 双实现 |
| 2026-05-18 | V2.0 | **多 Provider** — `plm-common.ai` 包(AiService 门面 + 4 Provider) + `tb_ai_agent.provider/model_name` + `/ai/health` |

# AI V4 Streaming — 架构草案

> 状态: 草案 (2026-05-19) — system-architect Agent dogfood 产出
> 上版: [V3 多 Provider + 审计](AI多Provider-系统设计-V2.md)
> 触发: PLM V4 路线 "streaming 模式(SSE 推 token,前端实时显示)"
> 此文档**仅设计**,不含落地代码

---

## 1. 设计目标(相对 V3)

| 项 | V3 | V4 |
|---|---|---|
| 调用模式 | 阻塞 chat → 完整 text | **流式** chat → token 逐个推 |
| 用户体验 | 等待 3-10s 后一次性看到结果 | 立即看到首 token,逐字渲染 |
| 协议 | REST POST → JSON | **Server-Sent Events (SSE)** |
| Provider 兼容 | 4 个统一 chat() | chat() 不变 + 加 chatStream() |
| 审计 | invoke 结束写 log | 流结束写 log + 加 firstTokenMs 指标 |
| 取消 | 不支持 | **支持中断**(用户点取消停止生成) |
| 兼容性 | - | V3 chat() **完全保留**,业务可选切流式 |

---

## 2. 架构(扩展 V3,非重构)

```
业务层
  │
  ├─ chat(req) → AiChatResult        ← V3 原有,保留
  └─ chatStream(req) → Flux<AiChatChunk>  ← V4 新增,响应式流
                              │
                              ▼
              ┌──────────────────────────────┐
              │   AiService (扩展 2 个方法)   │
              └──────────────────────────────┘
                              │
              Map<String, AiProvider>
              ├─ MockAiProvider         (chatStream: 模拟分块 echo)
              ├─ OpenAiCompatibleProvider (chatStream: SSE 解析 OpenAI 流)
              ├─ AnthropicProvider       (chatStream: SSE 解析 Anthropic 流)
              └─ DifyAiProvider          (chatStream: Dify streaming workflow)
                              │
                              ▼
                  AiInvocationRecorder (V3 原有 + 加 firstTokenMs / chunks)
```

---

## 3. SPI 扩展

### AiProvider 加 chatStream

```java
public interface AiProvider {
    // V3 原有
    AiChatResult chat(AiChatRequest req);

    /**
     * 流式聊天 (V4 新增)
     *
     * @return Flux 异步推 chunk;onComplete 后调用方可在 doOnComplete 拿到聚合结果
     */
    default Flux<AiChatChunk> chatStream(AiChatRequest req) {
        // 默认实现:把阻塞 chat 包成单 chunk Flux (向后兼容)
        return Flux.fromSupplier(() -> {
            AiChatResult r = chat(req);
            return AiChatChunk.complete(r);
        });
    }

    String name();
    boolean isAvailable();
}
```

`default` 实现保证 **V3 实现的 Provider 不动也能用 V4 接口**(降级为单 chunk)。

### 新 DTO: AiChatChunk

```java
public class AiChatChunk implements Serializable {
    /** 增量文本(本次新增 token,前端 append) */
    private String deltaText;

    /** 当前累积全文(便于前端中断时拿当前进度) */
    private String accumulatedText;

    /** 是否最终 chunk */
    private boolean done;

    /** 仅 done=true 时填:finishReason / tokens / model / requestId */
    private String finishReason;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private String model;
    private String requestId;

    /** 错误(中途失败) */
    private String error;
}
```

---

## 4. 各 Provider streaming 协议适配

### 4.1 OpenAI SSE 协议

请求:
```json
POST /v1/chat/completions
{
  "model": "...",
  "messages": [...],
  "stream": true
}
```

响应(SSE 流):
```
data: {"choices":[{"delta":{"content":"Hello"},"index":0}]}

data: {"choices":[{"delta":{"content":" world"},"index":0}]}

data: {"choices":[{"delta":{},"index":0,"finish_reason":"stop"}]}

data: [DONE]
```

Provider 解析每行 `data:`,JSON 反序列化 → `choices[0].delta.content` → emit AiChatChunk。

`[DONE]` 标记触发 `AiChatChunk.done=true`(注意:OpenAI streaming 不返回 token 数,需另一个 API 查 usage 或客户端粗估)。

### 4.2 Anthropic SSE 协议

请求:
```json
POST /v1/messages
{
  "model": "...",
  "max_tokens": 1024,
  "messages": [...],
  "stream": true
}
```

响应:多种 event:
```
event: message_start
data: {"message": {...}, "usage": {"input_tokens": 25}}

event: content_block_delta
data: {"delta": {"type": "text_delta", "text": "Hello"}}

event: message_delta
data: {"delta": {"stop_reason": "end_turn"}, "usage": {"output_tokens": 12}}

event: message_stop
data: {}
```

Provider 解析 `event:` + `data:`,关注 `content_block_delta` 推增量,`message_stop` 终止。

Anthropic 流**返回完整 usage**(比 OpenAI 友好)。

### 4.3 Dify streaming workflow

`response_mode=streaming` 的 workflow_run 接口:
```
data: {"event":"workflow_started", ...}
data: {"event":"text_chunk", "data":{"text":"Hello"}}
data: {"event":"text_chunk", "data":{"text":" world"}}
data: {"event":"workflow_finished", "data":{"status":"succeeded","total_tokens":100}}
```

### 4.4 Mock streaming

模拟分块:
```java
public Flux<AiChatChunk> chatStream(AiChatRequest req) {
    String mock = "[mock] " + req.firstUserContent();
    String[] tokens = mock.split(" ");
    return Flux.fromArray(tokens)
        .delayElements(Duration.ofMillis(50))   // 模拟 50ms/token
        .scan(new AccumulatedState(), (state, tok) -> state.append(tok + " "))
        .map(state -> AiChatChunk.delta(state.lastToken, state.accumulated))
        .concatWith(Mono.just(AiChatChunk.done(...)));
}
```

---

## 5. Controller 端点

```java
@GetMapping(value = "/business/ai-agent/invoke-stream/{id}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<AiChatChunk>> invokeStream(@PathVariable Long id) {
    AiAgent agent = aiAgentService.selectAiAgentById(id);
    // ... 构造 AiChatRequest 同 V3
    return aiService.chatStream(req)
        .map(chunk -> ServerSentEvent.<AiChatChunk>builder()
            .event(chunk.isDone() ? "done" : "delta")
            .data(chunk)
            .build());
}
```

注意:Spring WebFlux 必须在 plm-admin 引入 `spring-boot-starter-webflux`,与现有 MVC 共存(SSE 用 Reactor 流)。

---

## 6. 前端消费

Vue + EventSource:

```typescript
async function invokeStream(agentId: number) {
  return new Promise((resolve, reject) => {
    const es = new EventSource(`/dev-api/business/ai-agent/invoke-stream/${agentId}`)
    let accumulated = ''

    es.addEventListener('delta', (e: MessageEvent) => {
      const chunk = JSON.parse(e.data)
      accumulated += chunk.deltaText
      // 实时渲染到 UI
      streamingText.value = accumulated
    })

    es.addEventListener('done', (e: MessageEvent) => {
      const final = JSON.parse(e.data)
      es.close()
      resolve(final)
    })

    es.onerror = (err) => { es.close(); reject(err) }
  })
}
```

---

## 7. 审计扩展

`tb_ai_invocation_log` 加 2 字段:

```sql
ALTER TABLE tb_ai_invocation_log
    ADD COLUMN streaming        TINYINT(1) DEFAULT 0    COMMENT '0 阻塞 / 1 流式' AFTER success,
    ADD COLUMN first_token_ms   BIGINT     DEFAULT NULL COMMENT '首 token 延迟(ms) - 流式专有' AFTER elapsed_ms;
```

`first_token_ms` 是流式核心 UX 指标(用户感受的"等待"时间)。

---

## 8. 取消 / 中断支持

前端关闭 EventSource → 后端通过 Reactor 的 `doOnCancel` 检测:

```java
return aiService.chatStream(req)
    .map(...)
    .doOnCancel(() -> {
        log.info("用户中断 streaming");
        // 已用 token 仍要审计
        recorder.record(req, partialResult);
    });
```

OpenAI / Anthropic Provider 内部用 `HttpClient` 流式取消 connection。

---

## 9. V3 → V4 兼容性

| 项 | V3 | V4 | 兼容性 |
|---|---|---|---|
| `AiService.chat(req)` | 存在 | **保留**,行为不变 | ✅ |
| `AiProvider.chat()` | 抽象方法 | **保留**,行为不变 | ✅ |
| `AiProvider.chatStream()` | - | 默认实现降级为单 chunk | ✅ V3 Provider 不改也能用 |
| `tb_ai_invocation_log` | 13 字段 | + 2 字段(可空) | 迁移幂等 |
| 前端调用 | `POST /invoke` | **保留**;新增 `GET /invoke-stream` | ✅ |
| 现有 E2E | 不变 | 不变 | ✅ 0 退步 |
| 业务模块 aiGenerate | 走 chat() | 仍走 chat()(不切流式) | ✅ |

**V4 是叠加性扩展**,业务层可选切流式。

---

## 10. 落地路径(若决定做)

按 Phase 1-3:

### Phase 1: 后端 SPI 扩展 (1-2 天)

- AiProvider 加 `chatStream` 默认实现
- AiChatChunk DTO
- AiService.chatStream() 路由器(同 chat() 的 fallback 链)
- MockAiProvider 实现(模拟分块,可立刻 E2E 测)

### Phase 2: 真厂商 Provider 实现 (3-5 天)

- OpenAiCompatibleProvider 加 SSE 解析(需引入 `spring-boot-starter-webflux` 或保留 RestTemplate + line-by-line 读 InputStream)
- AnthropicProvider 加 SSE 解析(协议复杂,event:多 type)
- DifyAiProvider 加 streaming workflow(已有 Dify SDK 支持)

### Phase 3: 前端 + 审计 (2-3 天)

- AiAgentController 加 `/invoke-stream/{id}` SSE 端点
- 前端 EventSource 消费 + 实时渲染
- 审计表加 2 字段 + 迁移
- AiAgent view 加"流式生成"按钮(provider 支持时启用)

### Phase 4: 单测 + E2E (1-2 天)

- 4 Provider chatStream 单测(MockServer 模拟 SSE 流)
- 流式 E2E spec(测 deltaText 累积 / done 触发 / 取消)

**总工时**: 约 7-12 天(单人)

---

## 11. 风险与决策点

| 风险 | 概率 | 缓解 |
|---|---|---|
| Spring MVC + WebFlux 共存复杂 | 中 | 只在 plm-admin 一处引入 WebFlux,业务模块仍 MVC |
| 真厂商 SSE 协议差异大 | 高 | 每个 Provider 独立解析 |
| 审计实时性(流中失败时审计) | 中 | doOnCancel + doOnError 兜底 |
| token 计数不准(OpenAI 流式不返 usage) | 中 | 客户端粗估或追加一次 usage 查询 |
| 前端浏览器 EventSource 兼容性 | 低 | 现代浏览器全支持,IE 已死 |
| 取消时 Provider 真停了吗 | 中 | OpenAI/Anthropic 实测;Dify 看 SDK |

---

## 12. 决策点(需要 user 拍板)

1. **现在做还是等 V3 数据反馈一阵?** — V3 刚上,真厂商接入冒烟尚未跑;V4 工时大,建议先用 V3 数据反馈 1-2 周
2. **Phase 1+2 做完直接发布,还是 4 phase 一次性?** — Phase 1+2 可以让 mock 流式跑通,Phase 3+4 把真厂商串起来
3. **WebFlux 还是用阻塞 InputStream 解析 SSE?** — WebFlux 体验好但引入复杂依赖;InputStream 简单但 line-by-line 易卡

---

## 13. dogfood 验证:system-architect Agent 表现

本文档完全按 V1 system-architect Agent 模板产出:
- ✅ ASCII 架构图 在前
- ✅ 接口定义(代码 snippet)
- ✅ 装配策略(default 方法降级)
- ✅ 兼容性表(V3 → V4)
- ✅ 决策点 + 风险评估
- ✅ 落地 phase 分解

**Agent 模板可用性**: 高。从空白到 13 章草案约 25 分钟,质量足够 reviewer 提问。

**发现 1 个改进点**: V1 system-architect 没说"草案应给 user 决策点选项",本文添加了 §12。**V2 system-architect 模板建议加这一节**。

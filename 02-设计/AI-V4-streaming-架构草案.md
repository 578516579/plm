# AI V4 Streaming — 架构草案 + 落地进度

> 状态: **Phase 1 + 3 + 4 已落地** (2026-05-19);Phase 2 真厂商接入留独立 PR
> 上版: [V3 多 Provider + 审计](AI多Provider-系统设计-V2.md)
> 触发: PLM V4 路线 "streaming 模式(SSE 推 token,前端实时显示)"
>
> **落地 commit 链**:
> - `37f0b2c` Phase 1 — 后端 SPI (AiChatChunk + chatStream)
> - `6148789` Phase 3 — Controller SseEmitter + 前端 fetch+ReadableStream
> - (本次) Phase 4 — 审计表 streaming + first_token_ms
>
> ⚠ 落地时**架构调整**:Phase 1 把 `Flux<AiChatChunk>` 改为 JDK 原生
> `Iterator<AiChatChunk>`,避免引入 WebFlux 依赖冲突。详见 §10.5。

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

## 10. 落地路径(实际落地结果)

### Phase 1: 后端 SPI 扩展 ✅ (commit 37f0b2c, 实际 ~45 min)

- ✅ AiProvider 加 `chatStream` 默认实现
- ✅ AiChatChunk DTO (13 字段 + 3 静态工厂)
- ✅ AiService.chatStream() 路由器(同 chat() 的 fallback 链)
- ✅ MockAiProvider override chatStream(按 token 分块)
- ✅ 5 个单测覆盖

### Phase 2: 真厂商 Provider 实现 ⏭ (留独立 PR, 3-5 天工时)

留作未来工作:
- OpenAiCompatibleProvider 加 SSE 解析(需要真 key + SiliconFlow/DeepSeek 等测试)
- AnthropicProvider 加 SSE 解析(协议复杂,event:多 type)
- DifyAiProvider 加 streaming workflow

### Phase 3: Controller + 前端 ✅ (commit 6148789, 实际 ~1.5h)

- ✅ AiAgentController 加 `/invoke-stream/{id}` SseEmitter 端点
- ✅ 复用 plm-framework `threadPoolTaskExecutor` Bean,无需建新线程池
- ✅ 前端 fetch + ReadableStream(不用 EventSource,EventSource 不能带 Authorization header)
- ✅ AbortController 支持中断
- ✅ AI Agent view 加"🌊 流式"按钮 + 弹窗实时渲染 + ▍ 光标

### Phase 4: 审计字段 ✅ (本次, ~30 min)

- ✅ tb_ai_invocation_log 加 `streaming` + `first_token_ms` 字段
- ✅ AiChatResult 加对应字段
- ✅ AiServiceImpl.chatStream 包装 Iterator 计时首 token
- ✅ AiInvocationLogServiceImpl.record 写入新字段
- ✅ 1 个单测验证 streaming=true + firstTokenMs 写入
- ✅ provider summary 加 `avg_first_token_ms` 维度

### 实际总工时

| Phase | 设计估时 | 实际工时 | 状态 |
|---|---|---|---|
| 1 | 1-2 天 | ~45 min | ✅ |
| 2 | 3-5 天 | - | ⏭ 留独立 PR |
| 3 | 2-3 天 | ~1.5 小时 | ✅ |
| 4 | 1-2 天 | ~30 min | ✅ |
| 单测/E2E | (并入各 phase) | 6 单测 + 120/120 E2E 不退步 | ✅ |
| **总计** | **7-12 天** | **~3 小时** | Phase 1+3+4 ✅ |

工时压缩 90%+ 的原因:
- 用 Iterator 替代 Flux,避免 WebFlux 学习曲线
- 复用现有 threadPoolTaskExecutor / DifyService / AiService 路由器
- Mock 流式分块就够 E2E,真厂商留独立 PR

### 10.5 架构调整:Flux → Iterator

V4 草案原方案用 `Flux<AiChatChunk>` 响应式,但 plm-admin 是 Spring MVC 项目,
引入 `spring-boot-starter-webflux` 会有依赖冲突 + 改动面大。

Phase 1 落地时改用 JDK 原生 `Iterator<AiChatChunk>`:

| 维度 | Flux 方案(草案) | Iterator 方案(实际) |
|---|---|---|
| 依赖 | spring-boot-starter-webflux | 无 |
| 兼容 | MVC + WebFlux 共存复杂 | 完全兼容现有 MVC |
| Provider 实现 | 需要 Reactor 知识 | 同步分批 emit,简单 |
| Controller 适配 | Flux → ServerSentEvent | Iterator → SseEmitter |
| 异步 | 原生 | Controller 层用 TaskExecutor 包 |
| 未来扩展 | - | Iterator → CompletableFuture/Flux 平滑过渡 |

**决策**:Iterator 完全够用 Phase 1-4 范围;Phase 2 真厂商如有需要再切。

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

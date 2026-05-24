package cn.com.bosssfot.dv.plm.common.ai.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.ai.AiInvocationRecorder;
import cn.com.bosssfot.dv.plm.common.ai.AiProperties;
import cn.com.bosssfot.dv.plm.common.ai.AiProvider;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatChunk;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * AiService 默认实现 — 按 request.provider 路由到对应 {@link AiProvider}。
 *
 * <p>路由 fallback 链:</p>
 * <ol>
 *   <li>request.provider 非空且对应 provider 已装配且 available → 用该 provider</li>
 *   <li>取 plm.ai.default-provider 兜底</li>
 *   <li>default 也不可用 → 走 mock (Mock 永远可用)</li>
 * </ol>
 *
 * @author plm
 */
public class AiServiceImpl implements AiService {
    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final Map<String, AiProvider> providers;
    private final AiProperties props;
    /** 可选的审计 SPI;无实现则跳过审计 */
    private AiInvocationRecorder recorder;

    public AiServiceImpl(List<AiProvider> providerList, AiProperties props) {
        this.props = props;
        this.providers = new LinkedHashMap<>();
        for (AiProvider p : providerList) {
            this.providers.put(p.name(), p);
        }
        log.info("[AiService] 装配完成,providers={}, default={}",
                this.providers.keySet(), props.getDefaultProvider());
    }

    /** Setter — 在 AiAutoConfiguration 中按需注入 */
    public void setRecorder(AiInvocationRecorder recorder) {
        this.recorder = recorder;
        log.info("[AiService] 审计 recorder 已接入: {}", recorder == null ? "(none)" : recorder.getClass().getSimpleName());
    }

    @Override
    public AiChatResult chat(AiChatRequest request) {
        String want = request.getProvider();
        AiProvider p = pick(want);
        if (p == null) {
            log.warn("[AiService] 路由失败,want={}, default={}, available={}",
                    want, props.getDefaultProvider(), providers.keySet());
            return AiChatResult.fail(want == null ? props.getDefaultProvider() : want,
                    "无可用 AI provider — 请配置 plm.ai.{openai|anthropic}.api-key 或 plm.dify.api-key,或将 default-provider 设为 mock");
        }
        // 把实际选中的 provider 名回写,便于上游审计
        request.setProvider(p.name());
        log.debug("[AiService] route → {} (want={})", p.name(), want);
        AiChatResult result = p.chat(request);
        // 审计 — 失败/异常不影响业务主链路
        if (recorder != null) {
            try { recorder.record(request, result); }
            catch (Exception e) { log.warn("[AiService] 审计记录失败(已吞掉): {}", e.toString()); }
        }
        return result;
    }

    @Override
    public Iterator<AiChatChunk> chatStream(AiChatRequest request) {
        String want = request.getProvider();
        AiProvider p = pick(want);
        if (p == null) {
            log.warn("[AiService] stream 路由失败,want={}", want);
            AiChatChunk err = AiChatChunk.error(
                want == null ? props.getDefaultProvider() : want,
                "无可用 AI provider — 流式调用"
            );
            return List.of(err).iterator();
        }
        request.setProvider(p.name());
        log.debug("[AiService] stream route → {} (want={})", p.name(), want);
        Iterator<AiChatChunk> upstream = p.chatStream(request);

        // 包装迭代器 — V4 Phase 4:
        // 1. 记录第一个 delta chunk 到达时间 → firstTokenMs (流式 UX 核心指标)
        // 2. done chunk 时把 firstTokenMs + streaming=true 写进 fake result 入审计
        final long streamStart = System.currentTimeMillis();
        final long[] firstTokenMs = { -1L };   // -1 = 还没收到第一个 token

        return new Iterator<AiChatChunk>() {
            @Override
            public boolean hasNext() { return upstream.hasNext(); }

            @Override
            public AiChatChunk next() {
                AiChatChunk chunk = upstream.next();

                // 记录第一个非 done chunk 的到达时间
                if (firstTokenMs[0] < 0 && !chunk.isDone() && chunk.getDeltaText() != null
                        && !chunk.getDeltaText().isEmpty()) {
                    firstTokenMs[0] = System.currentTimeMillis() - streamStart;
                }

                if (chunk.isDone() && recorder != null) {
                    try {
                        AiChatResult fakeResult = chunkToResult(chunk);
                        fakeResult.setStreaming(true);                        // V4 Phase 4
                        fakeResult.setFirstTokenMs(firstTokenMs[0] < 0 ? 0 : firstTokenMs[0]);  // V4 Phase 4
                        recorder.record(request, fakeResult);
                    } catch (Exception e) {
                        log.warn("[AiService] stream 审计记录失败(已吞掉): {}", e.toString());
                    }
                }
                return chunk;
            }
        };
    }

    private static AiChatResult chunkToResult(AiChatChunk chunk) {
        AiChatResult r;
        if (chunk.getError() != null && !chunk.getError().isBlank()) {
            r = AiChatResult.fail(chunk.getProvider(), chunk.getError());
        } else {
            r = AiChatResult.ok(chunk.getProvider(), chunk.getModel(), chunk.getAccumulatedText());
        }
        r.setFinishReason(chunk.getFinishReason());
        r.setPromptTokens(chunk.getPromptTokens());
        r.setCompletionTokens(chunk.getCompletionTokens());
        r.setTotalTokens(chunk.getTotalTokens());
        r.setRequestId(chunk.getRequestId());
        r.setElapsedMs(chunk.getElapsedMs());
        return r;
    }

    /** 路由优先级:精确匹配 → 默认 → mock 兜底 */
    private AiProvider pick(String want) {
        if (want != null && !want.isBlank()) {
            AiProvider p = providers.get(want.toLowerCase());
            if (p != null && p.isAvailable()) return p;
        }
        String def = props.getDefaultProvider();
        if (def != null && !def.isBlank()) {
            AiProvider p = providers.get(def.toLowerCase());
            if (p != null && p.isAvailable()) return p;
        }
        return providers.get("mock");  // 始终装配
    }

    @Override
    public Map<String, Boolean> providerStatus() {
        Map<String, Boolean> m = new LinkedHashMap<>();
        providers.forEach((k, v) -> m.put(k, v.isAvailable()));
        return m;
    }

    @Override
    public String defaultProvider() { return props.getDefaultProvider(); }
}

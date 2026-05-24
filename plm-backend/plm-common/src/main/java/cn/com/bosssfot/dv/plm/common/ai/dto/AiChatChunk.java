package cn.com.bosssfot.dv.plm.common.ai.dto;

import java.io.Serializable;

/**
 * 流式聊天的单个 chunk (V4 Phase 1)
 *
 * <p>每个 chunk 携带:</p>
 * <ul>
 *   <li>{@code deltaText} - 本次增量 token,前端 append 累积</li>
 *   <li>{@code accumulatedText} - 截至本 chunk 的累积全文(中断时也可拿到)</li>
 *   <li>{@code done} - 是否最终 chunk,true 时填 finishReason/tokens/model</li>
 *   <li>{@code error} - 中途失败时非空</li>
 * </ul>
 *
 * <p>设计权衡(Phase 1):</p>
 * <ul>
 *   <li>用 JDK 原生 {@code Iterator<AiChatChunk>} 而非 {@code Flux},避免引入 WebFlux 与现有 MVC 冲突</li>
 *   <li>Provider 同步推送 chunk,Controller 层适配到 {@code SseEmitter} (Phase 3 实现)</li>
 *   <li>Phase 4 如需异步,Iterator → CompletableFuture/Flux 平滑过渡(本接口不变)</li>
 * </ul>
 *
 * @author plm
 */
public class AiChatChunk implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 本次增量文本(token 或多 token);前端 append 到 UI */
    private String deltaText = "";

    /** 截至本 chunk 累积的全文;中断时拿这个 */
    private String accumulatedText = "";

    /** 是否最终 chunk */
    private boolean done = false;

    /** 仅 done=true 时填 */
    private String finishReason;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private String model;
    private String provider;
    private String requestId;
    private long elapsedMs;

    /** 中途失败时非空 */
    private String error;

    public static AiChatChunk delta(String deltaText, String accumulated) {
        AiChatChunk c = new AiChatChunk();
        c.deltaText = deltaText == null ? "" : deltaText;
        c.accumulatedText = accumulated == null ? "" : accumulated;
        return c;
    }

    public static AiChatChunk done(String provider, String model, String accumulated, String finishReason) {
        AiChatChunk c = new AiChatChunk();
        c.done = true;
        c.provider = provider;
        c.model = model;
        c.accumulatedText = accumulated == null ? "" : accumulated;
        c.finishReason = finishReason;
        return c;
    }

    public static AiChatChunk error(String provider, String errMsg) {
        AiChatChunk c = new AiChatChunk();
        c.done = true;
        c.provider = provider;
        c.error = errMsg;
        return c;
    }

    /** 把同步 AiChatResult 包成单 chunk(default 实现用) */
    public static AiChatChunk fromResult(AiChatResult r) {
        AiChatChunk c = new AiChatChunk();
        c.done = true;
        c.deltaText = r.getText() == null ? "" : r.getText();
        c.accumulatedText = c.deltaText;
        c.provider = r.getProvider();
        c.model = r.getModel();
        c.finishReason = r.getFinishReason();
        c.promptTokens = r.getPromptTokens();
        c.completionTokens = r.getCompletionTokens();
        c.totalTokens = r.getTotalTokens();
        c.requestId = r.getRequestId();
        c.elapsedMs = r.getElapsedMs();
        c.error = r.getError();
        return c;
    }

    public String getDeltaText() { return deltaText; }
    public void setDeltaText(String v) { this.deltaText = v; }
    public String getAccumulatedText() { return accumulatedText; }
    public void setAccumulatedText(String v) { this.accumulatedText = v; }
    public boolean isDone() { return done; }
    public void setDone(boolean v) { this.done = v; }
    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String v) { this.finishReason = v; }
    public long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(long v) { this.promptTokens = v; }
    public long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(long v) { this.completionTokens = v; }
    public long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(long v) { this.totalTokens = v; }
    public String getModel() { return model; }
    public void setModel(String v) { this.model = v; }
    public String getProvider() { return provider; }
    public void setProvider(String v) { this.provider = v; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String v) { this.requestId = v; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long v) { this.elapsedMs = v; }
    public String getError() { return error; }
    public void setError(String v) { this.error = v; }
}
